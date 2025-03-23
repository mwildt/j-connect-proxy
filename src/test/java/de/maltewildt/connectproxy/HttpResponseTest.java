package de.maltewildt.connectproxy;


import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

class HttpResponseTest {

    @Test
    void test() throws Exception {
        final var respose = HttpResponse.read(new BufferedReader(new StringReader("""
        HTTP/1.1 200 OK FROM PROXY
        Content-Type: application/json
         
        """)));
        assertThat(respose.status()).isEqualTo("OK FROM PROXY");
        assertThat(respose.statusCode()).isEqualTo(200);
        assertThat(respose.version()).isEqualTo("HTTP/1.1");
        assertThat(respose.header("Content-Type")).isEqualTo("application/json");
    }

}
