package de.maltewildt.connectproxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class HttpRequest {

    private HttpRequest(RequestLine requestLine, List<HttpUtils.HttpHeader> header) {
        this.requestLine = requestLine;
        this.header = header;
    }

    public static HttpRequest connect(HttpUtils.SocketAddress targetHttpSocketAddress) {
        return new HttpRequest(
                new RequestLine("CONNECT", targetHttpSocketAddress.toString(), "HTTP/1.1"),
                List.of(new HttpUtils.HttpHeader("Host", targetHttpSocketAddress.toString()))
        );
    }

    private record RequestLine(String method, String target, String version) {
        static RequestLine of(String statusLine) {
            if (statusLine == null || statusLine.trim().isEmpty()) {
                return null;
            }
            String[] parts = statusLine.split(" ", 3);
            if (parts.length != 3) {
                return null;
            }
            return new RequestLine(parts[0], parts[1], parts[2]);
        }

    }

    private final RequestLine requestLine;
    private final List<HttpUtils.HttpHeader> header;

    public static HttpRequest read(BufferedReader reader) throws Exception {
        final RequestLine line = RequestLine.of(reader.readLine());
        return (line == null) ? null : new HttpRequest(
                line,
                HttpUtils.HttpHeader.read(reader)
        );
    }

    public void write(BufferedWriter writer) throws IOException {
        writer.write(String.format("%s %s %s\r\n", requestLine.method, requestLine.target, requestLine.version));
        String headers = this.header.stream()
                .map(HttpUtils.HttpHeader::format)
                .map("%s\r\n"::formatted)
                .collect(Collectors.joining());

        writer.write(headers);
        writer.write("\r\n");
        writer.flush();
    }

    public boolean isConnect() {
        return "CONNECT".equals(this.requestLine.method);
    }


    public String host() {
        return this.header.stream()
                .filter(HttpUtils.HttpHeader.hasName("Host"))
                .findFirst()
                .map(HttpUtils.HttpHeader::value)
                .orElse(null);
    }

    public String method() {
        return this.requestLine.method;
    }

    public String version() {
        return  this.requestLine.version;
    }

    public String target() {
        return  this.requestLine.target;
    }
}
