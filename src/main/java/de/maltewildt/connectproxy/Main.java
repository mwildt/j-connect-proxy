package  de.maltewildt.connectproxy;


import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

import java.util.List;
import java.util.Optional;

public class Main {

    public static  Reader open(String source)  {
        final String[] parts = source.split("://", 2);

        try {
            return switch (parts[0]) {
                case "file" -> new FileReader(parts[1]);
                case "env" -> new StringReader(System.getenv(parts[1]));
                default -> new StringReader(source);
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println("Connect Proxy");


        final String source = Optional.of("PROXY_RULE_SOURCE")
                .map(System::getenv)
                .orElse("file://proxy.rules");

        final List<ProxyRule> proxyRules = ProxyRule.read(open(source));
        
        new ConnectProxy(Integer.parseInt(args[0]),proxyRules).Listen();
    }
}


