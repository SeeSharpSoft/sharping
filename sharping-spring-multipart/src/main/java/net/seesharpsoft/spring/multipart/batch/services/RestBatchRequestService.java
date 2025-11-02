package net.seesharpsoft.spring.multipart.batch.services;

import net.seesharpsoft.spring.multipart.batch.BatchResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;

public class RestBatchRequestService extends BatchRequestServiceBase {

    public RestBatchRequestService(BatchRequestProperties batchRequestProperties) {
        super(batchRequestProperties);
    }

    @Override
    protected BatchResponse.Entity processSingleRequest(
            URI targetUri,
            HttpMethod httpMethod,
            HttpHeaders httpHeaders,
            byte[] body,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        RestTemplate restTemplate = new RestTemplate();

        HttpEntity httpEntity = new HttpEntity(body, httpHeaders);

        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(targetUri, httpMethod, httpEntity, byte[].class);

        BatchResponse.Entity result = new BatchResponse.Entity();
        result.setStatus(responseEntity.getStatusCode());
        result.setHeaders(responseEntity.getHeaders());
        result.setBody(responseEntity.getBody());
        return result;
    }
}
