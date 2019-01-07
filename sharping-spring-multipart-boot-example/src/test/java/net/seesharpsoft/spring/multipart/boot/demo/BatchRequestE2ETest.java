package net.seesharpsoft.spring.multipart.boot.demo;

import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class BatchRequestE2ETest {

    HttpHeaders createHeaders(String username, String password){
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")) );
            String authHeader = "Basic " + new String( encodedAuth );
            set( "Authorization", authHeader );
        }};
    }

    @Test
    public void simple_batch_request_should_fail_without_authorization() {
        RestTemplate restTemplate = new RestTemplate();

        HttpStatus status = null;

        try {
            restTemplate.exchange(
                    "http://localhost:8080/my/own/multipart/endpoint",
                    HttpMethod.POST,
                    new HttpEntity("--batch\n" +
                            "Content-Type: application/http\n" +
                            "Content-Transfer-Encoding: binary\n" +
                            "\n" +
                            "GET /hello HTTP/1.1\n" +
                            "Host: host\n" +
                            "\n" +
                            "--batch\n" +
                            "Content-Type: application/http\n" +
                            "Content-Transfer-Encoding: binary\n" +
                            "\n" +
                            "GET /dummy?name=Trevor HTTP/1.1\n" +
                            "Host: host\n" +
                            "\n" +
                            "--batch\n" +
                            "Content-Type: application/http\n" +
                            "Content-Transfer-Encoding: binary\n" +
                            "\n" +
                            "GET /dummy?name=Peter&name=Lustig HTTP/1.1\n" +
                            "--batch--"),
                    String.class);
        } catch(HttpClientErrorException exc) {
            status = exc.getStatusCode();
        }

        assertThat(status, equalTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void simple_batch_request_should_work_with_correct_authorization() {
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> result = restTemplate.exchange(
                "http://localhost:8080/my/own/multipart/endpoint",
                HttpMethod.POST,
                new HttpEntity("--batch\n" +
                        "Content-Type: application/http\n" +
                        "Content-Transfer-Encoding: binary\n" +
                        "\n" +
                        "GET /hello HTTP/1.1\n" +
                        "Host: host\n" +
                        "\n" +
                        "--batch\n" +
                        "Content-Type: application/http\n" +
                        "Content-Transfer-Encoding: binary\n" +
                        "\n" +
                        "GET /dummy?name=Trevor HTTP/1.1\n" +
                        "Host: host\n" +
                        "\n" +
                        "--batch\n" +
                        "Content-Type: application/http\n" +
                        "Content-Transfer-Encoding: binary\n" +
                        "\n" +
                        "GET /dummy?name=Peter&name=Lustig HTTP/1.1\n" +
                        "--batch--",
                        createHeaders("John", "password")),
                String.class);

        assertThat(result.getBody(), equalTo("--batch\n" +
                "Content-Type: application/http\n" +
                "Content-Transfer-Encoding: binary\n" +
                "\n" +
                "HTTP/1.1 200 OK\n" +
                "Content-Type: text/plain;charset=UTF-8\n" +
                "Content-Length: 27\n" +
                "\n" +
                "Greetings from Spring Boot!\n" +
                "--batch\n" +
                "Content-Type: application/http\n" +
                "Content-Transfer-Encoding: binary\n" +
                "\n" +
                "HTTP/1.1 200 OK\n" +
                "Content-Type: application/json;charset=UTF-8\n" +
                "Content-Length: 35\n" +
                "\n" +
                "{\"id\":1,\"content\":\"Hello, Trevor!\"}\n" +
                "--batch\n" +
                "Content-Type: application/http\n" +
                "Content-Transfer-Encoding: binary\n" +
                "\n" +
                "HTTP/1.1 200 OK\n" +
                "Content-Type: application/json;charset=UTF-8\n" +
                "Content-Length: 41\n" +
                "\n" +
                "{\"id\":2,\"content\":\"Hello, Peter,Lustig!\"}\n" +
                "--batch--"));
    }

}
