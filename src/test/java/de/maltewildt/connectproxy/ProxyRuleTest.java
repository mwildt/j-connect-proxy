package de.maltewildt.connectproxy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ProxyRuleTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "* direct",
            " *   direct  ",
            "\t*\tdirect\t",
    })
    void parseDirect(final String value) {
        final ProxyRule rule = ProxyRule.parse(value);
        assertThat(rule.host()).isEqualTo("*");
        assertThat(rule.connectStrategy()).isInstanceOf(ConnectStrategy.Direct.class);
    }

    @Test
    void parseBlock() {
        final ProxyRule rule = ProxyRule.parse("www.google.com block");
        assertThat(rule.host()).isEqualTo("www.google.com");
        assertThat(rule.connectStrategy()).isInstanceOf(ConnectStrategy.Block.class);
    }

    @Test
    void parseConnectNoHost() {
        final ProxyRule rule = ProxyRule.parse("* proxy localhost");
        assertThat(rule.host()).isEqualTo("*");
        assertThat(rule.connectStrategy())
                .isInstanceOf(ConnectStrategy.Proxy.class);

        assertThat(rule.connectStrategy())
                .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(ConnectStrategy.Proxy.class))
                .extracting(ConnectStrategy.Proxy::getProxyAddress)
                .extracting(HttpUtils.SocketAddress::toString)
                .isEqualTo("localhost:443");

    }

}