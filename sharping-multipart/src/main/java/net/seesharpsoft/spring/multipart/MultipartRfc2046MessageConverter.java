package net.seesharpsoft.spring.multipart;

import net.seesharpsoft.spring.multipart.batch.BatchMediaType;
import net.seesharpsoft.spring.util.SharpIO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts multipart request messages.
 *
 * http://www.rfc-editor.org/rfc/rfc2046.txt
 */
public class MultipartRfc2046MessageConverter implements HttpMessageConverter {

    protected static final String BOUNDARY_BOUNDARY = "--";

    protected static final Charset DEFAULT_CHARSET = Charset.forName("UTF8");

    protected static final MediaType DEFAULT_BODY_MEDIATYPE = new MediaType("text", "plain", Charset.forName("US-ASCII"));

    protected static final String CRLF = "\r\n";

    @Override
    public boolean canRead(Class clazz, MediaType mediaType) {
        return MultipartMessage.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean canWrite(Class clazz, MediaType mediaType) {
        return MultipartMessage.class.isAssignableFrom(clazz);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Collections.singletonList(BatchMediaType.MULTIPART_BATCH);
    }

    @Override
    public Object read(Class clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        String content = readBody(inputMessage);
        MultipartMessage request = createMultipartMessage();
        String boundary = getBatchBoundary(inputMessage.getHeaders());
        request.setParts(parseEntries(inputMessage, content, boundary));
        return request;
    }

    protected MultipartMessage createMultipartMessage() {
        return new MultipartMessage();
    }

    private List<MultipartEntity> parseEntries(HttpInputMessage inputMessage, String content, String boundary) {
        if (content == null) {
            return Collections.EMPTY_LIST;
        }
        String[] parts = content.split(getMessageSplitRegex(boundary), -1);
        parts = Arrays.copyOfRange(parts, 1, parts.length - 1);

        return Arrays.stream(parts)
                .map(part -> parseEntity(inputMessage, part))
                .collect(Collectors.toList());
    }

    protected MultipartEntity parseEntity(HttpInputMessage inputMessage, String content) {
        int headerContentSplitIndex = content.startsWith(CRLF) ? -CRLF.length() : content.indexOf(CRLF + CRLF);
        String headerPart = headerContentSplitIndex < 0 ? "" : content.substring(0, headerContentSplitIndex);
        String bodyPart = content.substring(headerContentSplitIndex + (CRLF + CRLF).length(), content.length());

        MultipartEntity entity = createMultipartEntity();
        applyEntityHeaders(entity, headerPart);
        applyEntityBody(entity, bodyPart);
        return entity;
    }

    protected MultipartEntity createMultipartEntity() {
        return new MultipartEntity();
    }

    protected void applyEntityHeaders(MultipartEntity entity, String partHeader) {
        HttpHeaders headers = new HttpHeaders();
        if (entity.getHeaders() != null) {
            headers.putAll(entity.getHeaders());
        }
        String[] headerEntries = partHeader == null ? new String[0] : partHeader.split(CRLF);
        for(String header : headerEntries) {
            int headerNameValueSplit = header.indexOf(":");
            if (headerNameValueSplit != -1) {
                headers.add(header.substring(0, headerNameValueSplit).trim(), header.substring(headerNameValueSplit + 1).trim());
            }
        }
        entity.setHeaders(headers);
    }

    protected void applyEntityBody(MultipartEntity entity, String part) {
        MediaType bodyMediaType = entity.getHeaders().getContentType();
        if (bodyMediaType == null) {
            bodyMediaType = DEFAULT_BODY_MEDIATYPE;
        }
        Charset charset = bodyMediaType.getCharset();
        if (charset == null) {
            charset = DEFAULT_BODY_MEDIATYPE.getCharset();
        }
        entity.setBody(part.getBytes(charset));
    }

    private String getMessageSplitRegex(String boundary) {
        return String.format("(^|%s)%s(%s)? *(%s|$)", CRLF, boundary, BOUNDARY_BOUNDARY, CRLF);
    }

    private String getBatchBoundary(HttpHeaders httpHeaders) {
        MediaType contentType = httpHeaders.getContentType() == null ? BatchMediaType.MULTIPART_BATCH : httpHeaders.getContentType();
        String boundary = contentType.getParameter("boundary");
        if (boundary == null) {
            boundary = BatchMediaType.MULTIPART_BATCH.getParameter("boundary");
        }
        if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
            boundary = boundary.substring(1, boundary.length() - 1);
        }
        return BOUNDARY_BOUNDARY + boundary;
    }

    private String readBody(HttpInputMessage inputMessage) throws IOException {
        byte[] buffer = SharpIO.readAsByteArray(inputMessage.getBody());
        return new String(buffer, DEFAULT_CHARSET);
    }


    /**************************** RESPONSE ***********************************/

    @Override
    public void write(Object o, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        MultipartMessage<?> response = (MultipartMessage)o;
        OutputStreamWriter writer = new OutputStreamWriter(outputMessage.getBody(), DEFAULT_CHARSET);
        String boundary = getBatchBoundary(outputMessage.getHeaders());
        for (MultipartEntity entry : response.getParts()) {
            writer.write(boundary);
            writer.write(CRLF);
            writePartHeader(writer, entry);
            writer.write(CRLF);
            writePartContent(writer, entry);
        }
        writer.write(boundary);
        writer.write(BOUNDARY_BOUNDARY);
        writer.flush();
        writer.close();
    }

    protected void writePartHeader(OutputStreamWriter writer, MultipartEntity entry) throws IOException {
        writer.write(HttpHeaders.CONTENT_TYPE);
        writer.write(": ");
        writer.write("application/http");
        writer.write(CRLF);
        writer.write("Content-Transfer-Encoding");
        writer.write(": ");
        writer.write("binary");
        writer.write(CRLF);
    }

    protected void writePartContent(OutputStreamWriter writer, MultipartEntity entry) throws IOException {
        byte[] body = entry.getBody();
        if (body != null) {
            writer.write(new String(body, DEFAULT_CHARSET));
            writer.write(CRLF);
        }
    }
}
