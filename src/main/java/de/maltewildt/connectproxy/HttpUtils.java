package de.maltewildt.connectproxy;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class HttpUtils {

    public static class HttpHeader {

        private final String name, value;

        HttpHeader(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public static Predicate<HttpHeader> hasName(String name) {
            return header -> header.name.equals(name);
        }

        public static List<HttpHeader> read(BufferedReader reader) throws IOException {
            final List<HttpHeader> headers = new ArrayList<>();
            String headerLine = reader.readLine();
            while (!headerLine.isEmpty()) {
                final String[] parts = headerLine.split(":", 2);
                headers.add(new HttpHeader(parts[0], parts[1].trim()));
                headerLine = reader.readLine();
            }
            return headers;
        }

        public String name() {
            return name;
        }

        public String value() {
            return value;
        }

        public String format() {
            return "%s: %s".formatted(name, value);
        }
    }

    public record SocketAddress(String host, int port) {

        public static SocketAddress parse(String input, int defaultPort) throws FormatException {
            if (null == input || input.isEmpty()) {
                throw new FormatException("Input is null or empty");
            }
            final String[] parts = input.split(":", 2);
            if (parts.length == 2) {
                try {
                    return new SocketAddress(parts[0], Integer.parseInt(parts[1]));
                } catch (NumberFormatException e) {
                    throw new FormatException(e);
                }
            }
            return new SocketAddress(parts[0], defaultPort);
        }

        @Override
        public String toString() {
            return "%s:%d".formatted(host, port);
        }

        public static class FormatException extends IllegalArgumentException {

            public FormatException(String inputIsNullOrEmpty) {
                super(inputIsNullOrEmpty);
            }

            public FormatException(NumberFormatException e) {
                super(e);
            }
        }
    }


}
