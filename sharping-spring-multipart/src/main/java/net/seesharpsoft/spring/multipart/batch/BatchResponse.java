package net.seesharpsoft.spring.multipart.batch;

import net.seesharpsoft.spring.multipart.MultipartEntity;
import net.seesharpsoft.spring.multipart.MultipartMessage;
import org.springframework.http.HttpStatusCode;

/**
 * Entity to be used for internal batch response handling.
 */
public class BatchResponse extends MultipartMessage<BatchResponse.Entity> {

    /**
     * Single response entry.
     */
    public static class Entity extends MultipartEntity {

        HttpStatusCode status;

        public HttpStatusCode getStatus() {
            return status;
        }

        public void setStatus(HttpStatusCode status) {
            this.status = status;
        }
    }
}
