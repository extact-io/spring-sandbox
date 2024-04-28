package sample.spring;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

class HelloResourceTest {

    @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
    @TestPropertySource(properties = "spring.config.additional-location=classpath:test-application.yml")
    @Nested
    class NormalPattern {

        private HelloResource helloResource;

        @BeforeEach
        void beforeEach(@Value("${url}") String url) {
            this.helloResource = createHttpInterface(url);
        }

        @Test
        void tesHello() {
            var expected = "Hello";
            var actual = helloResource.hello();
            assertThat(actual).isEqualTo(expected);
        }
    }

    @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
    @TestPropertySource(properties = "spring.config.additional-location=classpath:test-application.yml")
    @TestPropertySource(properties = "config.val=Changed Hello")
    @Nested
    class ChangeConfigPattern {

        private HelloResource helloResource;

        @BeforeEach
        void beforeEach(@Value("${url}") String url) {
            this.helloResource = createHttpInterface(url);
        }

        @Test
        void tesHello() {
            var expected = "Changed Hello";
            var actual = helloResource.hello();
            assertThat(actual).isEqualTo(expected);
        }
    }

    interface HelloResource {
        @GetExchange("/hello")
        String hello();
    }

    HelloResource createHttpInterface(String url) {
        RestClient restClient = RestClient.builder().baseUrl(url).build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(HelloResource.class);
    }
}
