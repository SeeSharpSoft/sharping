package net.seesharpsoft.spring.multipart;

import net.seesharpsoft.spring.multipart.test.util.HttpInputMessageDummy;
import net.seesharpsoft.commons.util.SharpIO;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class MultipartRfc2046MessageConverterUT {

    private Map<String, Integer> fileName2NoOfPartsMap = new HashMap() {{
        this.put("input/batch_simplemessage", 2);
        this.put("input/batch_example", 3);
    }};

    private MultipartRfc2046MessageConverter messageConverter;

    @Before
    public void beforeEach() {
        messageConverter = new MultipartRfc2046MessageConverter();
    }

    private byte[] readResource(String fileName) throws IOException {
        byte[] bytes = SharpIO.readAsByteArray(this.getClass().getClassLoader().getResourceAsStream(fileName));
        bytes = SharpIO.bytesToStream(bytes).filter(content -> content != 13).collect(SharpIO.toByteArray());
        return bytes;
    }

    private HttpHeaders createHttpHeaders(String boundary) {
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = new MediaType("multipart", "mixed", new HashMap<String, String>() {{
            this.put("boundary", boundary);
        }});
        headers.setContentType(mediaType);
        return headers;
    }

    private HttpHeaders readHttpHeaders(String fileName) throws IOException {
        String headerString = new String(readResource(fileName), StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        Arrays.stream(headerString.split("\n"))
                .map(headerLine -> headerLine.split(":"))
                .forEach(header -> headers.add(header[0].trim(), header[1].trim()));

        return headers;
    }

    private void assertCorrectHeaders(MultipartMessage<MultipartEntity> message, String fileName, int part) throws IOException {
        HttpHeaders expectedHeaders = readHttpHeaders(String.format("%s_header_%s.txt", fileName, part));
        assertThat(message.getParts().get(part - 1).getHeaders(), equalTo(expectedHeaders));
    }

    private void assertCorrectBody(MultipartMessage<MultipartEntity> message, String fileName, int part) throws IOException {
        byte[] expectedBody = readResource(String.format("%s_body_%s.txt", fileName, part));
        assertThat(message.getParts().get(part - 1).getBody(), equalTo(expectedBody));
    }

    /********************* FULL EXAMPLE TEST *********************/

    @Test
    public void read_should_work_for_all_examples() throws IOException {
        for (Map.Entry<String, Integer> entry : fileName2NoOfPartsMap.entrySet()) {
            HttpInputMessage inputMessage = new HttpInputMessageDummy(
                    readHttpHeaders(String.format("%s_header.txt", entry.getKey())),
                    readResource(String.format("%s_body.txt", entry.getKey())));

            MultipartMessage<MultipartEntity> result = (MultipartMessage)messageConverter.read(MultipartMessage.class, inputMessage);

            assertThat(result.getParts().size(), equalTo(entry.getValue()));
            for (int i = 1; i < entry.getValue() + 1; ++i) {
                assertCorrectHeaders(result, entry.getKey(), i);
                assertCorrectBody(result, entry.getKey(), i);
            }
        }
    }

    /**************** BASIC TESTS ****************/

    @Test
    public void read_should_return_multipart_message() throws IOException {
        HttpInputMessage inputMessage = new HttpInputMessageDummy(
                createHttpHeaders("\"simple boundary\""),
                readResource("input/rfc2046_simplemessage.txt"));

        Object result = messageConverter.read(MultipartMessage.class, inputMessage);

        assertThat(result, instanceOf(MultipartMessage.class));
    }

    @Test
    public void read_should_return_correct_number_of_parts() throws IOException {
        HttpInputMessage inputMessage = new HttpInputMessageDummy(
                createHttpHeaders("\"simple boundary\""),
                readResource("input/rfc2046_simplemessage.txt"));

        MultipartMessage result = (MultipartMessage)messageConverter.read(MultipartMessage.class, inputMessage);

        assertThat(result.getParts().size(), equalTo(2));
    }

    @Test
    public void read_should_return_correct_header_of_parts() throws IOException {
        HttpInputMessage inputMessage = new HttpInputMessageDummy(
                createHttpHeaders("\"simple boundary\""),
                readResource("input/rfc2046_simplemessage.txt"));

        MultipartMessage<MultipartEntity> result = (MultipartMessage)messageConverter.read(MultipartMessage.class, inputMessage);

        MultipartEntity firstEntity = result.getParts().get(0);
        MultipartEntity secondEntity = result.getParts().get(1);

        assertThat(firstEntity.getHeaders().size(), equalTo(0));
        assertThat(secondEntity.getHeaders().size(), equalTo(1));
        assertThat(secondEntity.getHeaders().getContentType(), equalTo(new MediaType("text", "plain", Charset.forName("us-ascii"))));
    }

    @Test
    public void read_should_return_correct_body_of_parts() throws IOException {
        HttpInputMessage inputMessage = new HttpInputMessageDummy(
                createHttpHeaders("\"simple boundary\""),
                readResource("input/rfc2046_simplemessage.txt"));

        MultipartMessage<MultipartEntity> result = (MultipartMessage)messageConverter.read(MultipartMessage.class, inputMessage);

        MultipartEntity firstEntity = result.getParts().get(0);
        MultipartEntity secondEntity = result.getParts().get(1);

        assertThat(firstEntity.getBody(), equalTo( readResource("input/rfc2046_simplemessage_1.txt")));
        assertThat(secondEntity.getBody(), equalTo( readResource("input/rfc2046_simplemessage_2.txt")));
    }


}
