package de.maltewildt.connectproxy;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectProxy {

    private final int port;
    private final List<ProxyRule> proxyRules;

    public ConnectProxy(int port, List<ProxyRule> proxyRules) {
        this.port = port;
        this.proxyRules = proxyRules;
    }

    public void Listen() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {

            System.out.printf("start ConnectProxy to listen for incoming tcp-connections on port %d%n",this.port);

            AtomicInteger index = new AtomicInteger(0);
            this.proxyRules.forEach(pr -> System.out.printf("Proxy Rule %02d: %s%n", index.getAndIncrement(), pr.toString()));

            while (true) {
                final Socket clientSocket = serverSocket.accept();
                new Thread(new ProxyHandler(clientSocket, proxyRules)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ProxyHandler implements Runnable {

        private final Socket clientSocket;
        private final List<ProxyRule> proxyRules;

        public ProxyHandler(Socket clientSocket, List<ProxyRule> proxyRules) {
            this.clientSocket = clientSocket;
            this.proxyRules = proxyRules;
        }

        @Override
        public void run() {
            System.out.printf("handle incoming connection for client %s%n", clientSocket.getRemoteSocketAddress().toString());
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

                final HttpRequest request = HttpRequest.read(reader);
                if (null == request) {
                    HttpResponse.badRequest().write(writer);
                    return;
                }

                if (!request.isConnect()) {
                    HttpResponse.methodNotAllowed().write(writer);
                    return;
                }

                final HttpUtils.SocketAddress socket;
                try {
                     socket = HttpUtils.SocketAddress.parse(request.host(), 443);
                } catch (HttpUtils.SocketAddress.FormatException socketAddressException) {
                    HttpResponse.badRequest().write(writer);
                    return;
                }

                try (Socket upstreamSocket = connect(socket, writer)) {
                    System.out.printf("established upstream connection to %s%n", request.host());

                    Thread forward = new Thread(() -> forwardData(clientSocket, upstreamSocket));
                    Thread backward = new Thread(() -> forwardData(upstreamSocket, clientSocket));
                    forward.start();
                    backward.start();
                    forward.join();
                    backward.join();
                } catch (HttpUtils.SocketAddress.FormatException socketAddressException) {
                    socketAddressException.printStackTrace();
                } catch (InterruptedException e) {
                    System.out.println("Interrupted while waiting for incoming connection"  );
                    e.printStackTrace();
                } catch (ConnectStrategy.ConnectException e) {
                    System.out.printf("No Connection Established %s%n", e.getMessage());
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException ignored) {
                }
            }
        }

        private Socket connect(HttpUtils.SocketAddress targetHttpSocketAddress, BufferedWriter downStreamWriter) throws ConnectStrategy.ConnectException {
            return this.proxyRules.stream()
                    .filter(rule -> rule.match(targetHttpSocketAddress))
                    .findFirst()
                    .map(ProxyRule::connectStrategy)
                    .orElse(new ConnectStrategy.Block())
                    .connect(targetHttpSocketAddress, downStreamWriter);
        }


        private void forwardData(Socket in, Socket out) {
            try (final InputStream inputStream = in.getInputStream();
                 final OutputStream outputStream = out.getOutputStream()) {
                final byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.flush();
                }
            } catch (IOException ignored) {

            }
        }
    }

}
