package net.seesharpsoft.spring.multipart.batch;

import net.seesharpsoft.spring.multipart.MultipartEntity;
import net.seesharpsoft.spring.multipart.MultipartMessage;
import net.seesharpsoft.spring.multipart.MultipartRfc2046MessageConverter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class BatchMessageConverter extends MultipartRfc2046MessageConverter {

    @Override
    public boolean canRead(Class clazz, MediaType mediaType) {
        return BatchRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean canWrite(Class clazz, MediaType mediaType) {
        return BatchResponse.class.isAssignableFrom(clazz);
    }

    @Override
    protected MultipartMessage createMultipartMessage() {
        return new BatchRequest();
    }

    @Override
    protected MultipartEntity createMultipartEntity() {
        return new BatchRequest.Entity();
    }

    @Override
    protected void applyEntityBody(MultipartEntity entity, String part) {
        int headerContentSplitIndex = part.indexOf(CRLF + CRLF);
        boolean hasBody = headerContentSplitIndex != -1;
        String headerPart = !hasBody ? part : part.substring(0, headerContentSplitIndex);
        String bodyPart = !hasBody ? "" : part.substring(headerContentSplitIndex + (CRLF + CRLF).length(), part.length());

        applyBodyHeader((BatchRequest.Entity)entity, headerPart);
        super.applyEntityBody(entity, bodyPart);
    }

    protected void applyBodyHeader(BatchRequest.Entity entity, String content) {
        int headerContentSplitIndex = content.indexOf(CRLF);
        boolean hasHeader = headerContentSplitIndex != -1;
        String urlPart = hasHeader ? content.substring(0, headerContentSplitIndex) : content;
        String[] targetUrlParts = urlPart.split(" ");
        entity.setMethod(HttpMethod.resolve(targetUrlParts[0]));
        entity.setUrl(targetUrlParts[1]);

        applyEntityHeaders(entity, hasHeader ? content.substring(headerContentSplitIndex, content.length()) : null);
    }

    /******************************** RESPONSE ***************************/

    @Override
    protected void writePartContent(OutputStreamWriter writer, MultipartEntity entry) throws IOException {
        writeResponseStatus(writer, (BatchResponse.Entity)entry);
        byte[] body = entry.getBody();
        if (body != null) {
            writePartContentHeader(writer, entry.getHeaders(), body.length);
            writer.write(CRLF);
            writer.write(new String(body, DEFAULT_CHARSET));
            writer.write(CRLF);
        }
    }

    private void writeResponseStatus(OutputStreamWriter writer, BatchResponse.Entity entry) throws IOException {
        HttpStatus status = entry.getStatus();

        if (status == null) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
        }

        writer.write("HTTP/1.1 ");
        writer.write(status.value() + "");
        writer.write(" ");
        writer.write(status.getReasonPhrase());
        writer.write(CRLF);
    }

    private void writePartContentHeader(OutputStreamWriter writer, HttpHeaders headers, int contentLength) throws IOException {
        MediaType contentType = headers.getContentType();

        if (contentType == null) {
            contentType = MediaType.ALL;
        }

        writer.write(HttpHeaders.CONTENT_TYPE);
        writer.write(": ");
        writer.write(contentType.toString());
        writer.write(CRLF);
        writer.write(HttpHeaders.CONTENT_LENGTH);
        writer.write(": ");
        writer.write(contentLength + "");
        writer.write(CRLF);
    }
}
