package de.maltewildt.connectproxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HttpResponse {

    HttpResponse(ResponseLine requestLine, List<HttpUtils.HttpHeader> header) {
        this.requestLine = requestLine;
        this.header = header;
    }

    public static HttpResponse badRequest() {
        return HttpResponse.of("HTTP/1.1", 400, "Bad Request");
    }

    public static HttpResponse methodNotAllowed() {
        return of("HTTP/1.1", 405, "Method not Method Not Allowed");
    }

    public static HttpResponse connectionEstablished() {
        return of("HTTP/1.1", 200, "Connection Established");
    }

    public static HttpResponse badGateway() {
        return of("HTTP/1.1", 502, "Bad Gateway");
    }

    public static HttpResponse forbidden() {
        return of("HTTP/1.1", 403, "Forbidden");
    }


    public String header(String s) {
        return header.stream()
                .filter(HttpUtils.HttpHeader.hasName(s))
                .findFirst()
                .map(HttpUtils.HttpHeader::value)
                .orElse("");
    }

    public boolean isOk() {
        return 200 == this.statusCode();
    }

    private record ResponseLine(String version, int statusCode, String status) {
        public static ResponseLine of(String statusLine) {
            if (statusLine == null || statusLine.trim().isEmpty()) {
                return null;
            }
            String[] parts = statusLine.split(" ", 3);
            if (parts.length != 3) {
                return null;
            }
            return new ResponseLine(
                    parts[0],
                    Integer.parseInt(parts[1]),
                    parts[2]);
        }
    }

    private final HttpResponse.ResponseLine requestLine;
    private final List<HttpUtils.HttpHeader> header;

    public static HttpResponse of(String version, int code, String status) {
        return new HttpResponse(
                new HttpResponse.ResponseLine(version, code, status),
                new ArrayList<>()
        );
    }

    public static HttpResponse read(BufferedReader reader) throws Exception {
        return new HttpResponse(
                HttpResponse.ResponseLine.of(reader.readLine()),
                HttpUtils.HttpHeader.read(reader)
        );
    }

    public void write(BufferedWriter writer) throws IOException {
        writer.write(String.format("%s %d %s\r\n", requestLine.version, requestLine.statusCode, requestLine.status));
        String headers = this.header.stream()
                .map(HttpUtils.HttpHeader::format)
                .map("%s\r\n"::formatted)
                .collect(Collectors.joining());

        writer.write(headers);
        writer.write("\r\n");

        writer.flush();
    }

    public int statusCode(){
        return this.requestLine.statusCode;
    }

    public String status() {
        return this.requestLine.status;
    }

    public String version() {
        return this.requestLine.version;
    }


}
