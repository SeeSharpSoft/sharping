package net.seesharpsoft.spring.multipart;

import org.springframework.http.HttpHeaders;

/**
 * Base class for single batch entries. Used for batch-request and -response.
 */
public class MultipartEntity {

    private HttpHeaders headers;

    public HttpHeaders getHeaders() {
        return this.headers == null ? null : HttpHeaders.readOnlyHttpHeaders(this.headers);
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = new HttpHeaders();
        this.headers.putAll(headers);
    }

    public void removeHeader(String header) {
        this.headers.remove(header);
    }

    private byte[] body;

    public byte[] getBody() {
        return body == null ? null : body.clone();
    }

    public void setBody(byte[] body) {
        this.body = body == null ? null : body.clone();
    }
}
