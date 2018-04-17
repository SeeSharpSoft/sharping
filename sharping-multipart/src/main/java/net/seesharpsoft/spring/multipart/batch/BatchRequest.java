package net.seesharpsoft.spring.multipart.batch;

import net.seesharpsoft.spring.multipart.MultipartEntity;
import net.seesharpsoft.spring.multipart.MultipartMessage;
import org.springframework.http.HttpMethod;

/**
 * Entity to be used for internal batch request handling.
 */
public class BatchRequest extends MultipartMessage<BatchRequest.Entity> {

    /**
     * Single request entry.
     */
    public static class Entity extends MultipartEntity {

        String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        HttpMethod method;

        public HttpMethod getMethod() {
            return method;
        }

        public void setMethod(HttpMethod method) {
            this.method = method;
        }
    }

}
