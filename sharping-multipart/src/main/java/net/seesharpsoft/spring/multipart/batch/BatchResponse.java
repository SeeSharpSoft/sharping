package net.seesharpsoft.spring.multipart.batch;

import net.seesharpsoft.spring.multipart.MultipartEntity;
import net.seesharpsoft.spring.multipart.MultipartMessage;
import org.springframework.http.HttpStatus;

/**
 * Entity to be used for internal batch response handling.
 */
public class BatchResponse extends MultipartMessage<BatchResponse.Entity> {

    /**
     * Single response entry.
     */
    public static class Entity extends MultipartEntity {

        HttpStatus status;

        public HttpStatus getStatus() {
            return status;
        }

        public void setStatus(HttpStatus status) {
            this.status = status;
        }
    }
}
