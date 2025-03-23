package de.maltewildt.connectproxy;

import java.io.*;
import java.net.Socket;


public sealed interface ConnectStrategy permits ConnectStrategy.Block, ConnectStrategy.Proxy, ConnectStrategy.Direct {

    final class Block implements ConnectStrategy {

        @Override
        public Socket connect(HttpUtils.SocketAddress targetHttpSocketAddress, BufferedWriter downstreamWriter) throws ConnectException {
            try {
                System.out.printf("block connection to %s %n", targetHttpSocketAddress);
                HttpResponse.forbidden().write(downstreamWriter);
                throw new ConnectException("forbidden to connect to %s".formatted(targetHttpSocketAddress.toString()));
            } catch (IOException ex) {
                throw new ConnectException("unable to write to downstream ", ex);
            }
        }

        @Override
        public String toString() {
            return "Block";
        }
    }

    final class Proxy implements ConnectStrategy {

        private final HttpUtils.SocketAddress proxyAddress;

        public Proxy(HttpUtils.SocketAddress proxyAddress) {
            this.proxyAddress = proxyAddress;
        }

        public HttpUtils.SocketAddress getProxyAddress() {
            return proxyAddress;
        }

        @Override
        public Socket connect(HttpUtils.SocketAddress targetHttpSocketAddress, BufferedWriter downstreamWriter) throws ConnectException {
            try {
                System.out.printf("try to establish proxied connection to %s via %s%n", targetHttpSocketAddress, proxyAddress.toString());
                final Socket upstreamSocket = new Socket(proxyAddress.host(), proxyAddress.port());
                final BufferedWriter upstreamWriter = new BufferedWriter(new OutputStreamWriter(upstreamSocket.getOutputStream()));
                final BufferedReader upstreamReader = new BufferedReader(new InputStreamReader(upstreamSocket.getInputStream()));
                HttpRequest.connect(targetHttpSocketAddress).write(upstreamWriter);

                try {
                    final HttpResponse upstreamResponse = HttpResponse.read(upstreamReader);
                    upstreamResponse.write(downstreamWriter);
                    if (!upstreamResponse.isOk()) {
                        throw new ConnectException("not OK from proxy upstream");
                    } else {
                        return upstreamSocket;
                    }
                } catch (Exception e) {
                    HttpResponse.badGateway().write(downstreamWriter);
                    throw new ConnectException("unable to read response from proxy upstream", e);
                }
            } catch (IOException e) {
                try {
                    HttpResponse.badGateway().write(downstreamWriter);
                } catch (IOException ex) {
                    throw new ConnectException("unable to read response from upstream", e);
                }
                throw new ConnectException("unable to connect to proxy lccalhost:8888", e);
            }
        }

        @Override
        public String toString() {
            return "Proxy [%s]".formatted(proxyAddress);
        }
    }

    final class Direct implements ConnectStrategy {

        @Override
        public Socket connect(HttpUtils.SocketAddress targetHttpSocketAddress, BufferedWriter downstreamWriter) throws ConnectException {
            try {
                System.out.println("try to establish direct connection to " + targetHttpSocketAddress);
                final Socket socket = new Socket(targetHttpSocketAddress.host(), targetHttpSocketAddress.port());
                HttpResponse.connectionEstablished().write(downstreamWriter);
                return socket;
            } catch (IOException e) {
                throw new ConnectException("unable to connect to host %s".formatted(targetHttpSocketAddress), e);
            }
        }

        @Override
        public String toString() {
            return "Direct";
        }
    }

    Socket connect(HttpUtils.SocketAddress targetHttpSocketAddress, BufferedWriter downstreamWriter) throws ConnectException;

    class ConnectException extends Exception {

        public ConnectException(String notOkFromUpstream) {
            super(notOkFromUpstream);
        }

        public ConnectException(String notOkFromUpstream, Exception e) {
            super(notOkFromUpstream, e);
        }

    }
}
