package de.maltewildt.connectproxy;


import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

class HttpRequestTest {

    @Test
    void testCONNECT() throws Exception {
        final var req = HttpRequest.read(new BufferedReader(new StringReader("""
        CONNECT /irgendwas?hallo=Welt HTTP/1.1
        Host: proxy.target.tld
        
        """)));
        assertThat(req.method()).isEqualTo("CONNECT");
        assertThat(req.target()).isEqualTo("/irgendwas?hallo=Welt");
        assertThat(req.version()).isEqualTo("HTTP/1.1");
        assertThat(req.isConnect()).isTrue();

        assertThat(req.host()).isEqualTo("proxy.target.tld");
    }

    @Test
    void testGET() throws Exception {
        final var req = HttpRequest.read(new BufferedReader(new StringReader("""
        GET /irgendwas?hallo=Welt HTTP/1.1
        Host: proxy.target.tld
        
        """)));
        assertThat(req.method()).isEqualTo("GET");
        assertThat(req.target()).isEqualTo("/irgendwas?hallo=Welt");
        assertThat(req.version()).isEqualTo("HTTP/1.1");
        assertThat(req.isConnect()).isFalse();
    }

}
