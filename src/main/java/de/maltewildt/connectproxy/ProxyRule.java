package de.maltewildt.connectproxy;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record ProxyRule(String host, ConnectStrategy connectStrategy) {

    static List<ProxyRule> read(Reader reader) {
        return new BufferedReader(reader).lines()
                .flatMap(line -> Arrays.stream(line.split("\\|")))
                .map(ProxyRule::parse)
                .collect(Collectors.toList());
    }

    public static ProxyRule parse(String s) {
        String[] parts = s.trim().split("\\s+", 2);
        String host = parts[0];
        if (parts.length == 1) {
            throw new RuntimeException("format");
        }
        parts = parts[1].trim().split("\\s+", 2);
        String type = parts[0];

        return switch (type) {
            case "direct" -> new ProxyRule(host, new ConnectStrategy.Direct());
            case "block" -> new ProxyRule(host, new ConnectStrategy.Block());
            case "proxy" -> {
                if (parts.length != 2) {
                    throw new RuntimeException("no proxy given");
                }
                final HttpUtils.SocketAddress proxyAddress = HttpUtils.SocketAddress.parse(parts[1], 443);
                yield new ProxyRule(host, new ConnectStrategy.Proxy(proxyAddress));
            }
            default -> throw new RuntimeException("unknown proxy type: " + type);
        };
    }

    public boolean match(HttpUtils.SocketAddress address) {
        if (host.equals("*")) {
            return true;
        } else {
            return address.host().startsWith(host);
        }
    }

    @Override
    public String toString() {
        return "%s -> %s".formatted(host, connectStrategy.toString());
    }
}
