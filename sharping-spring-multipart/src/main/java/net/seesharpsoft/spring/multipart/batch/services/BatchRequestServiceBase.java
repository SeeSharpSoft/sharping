package net.seesharpsoft.spring.multipart.batch.services;

import net.seesharpsoft.spring.multipart.batch.BatchMediaType;
import net.seesharpsoft.spring.multipart.batch.BatchRequest;
import net.seesharpsoft.spring.multipart.batch.BatchResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;

public abstract class BatchRequestServiceBase implements BatchRequestService {

    @Override
    public BatchResponse process(BatchRequest batchRequest,
                                 BatchRequestProperties properties,
                                 HttpServletRequest servletRequest,
                                 HttpServletResponse servletResponse) throws ServletException, IOException {
        BatchResponse batchResponse = new BatchResponse();
        for (BatchRequest.Entity singleRequest : batchRequest.getParts()) {
            HttpHeaders httpHeaders = new HttpHeaders();
            if (properties != null && properties.getIncludeOriginalHeader()) {
                Enumeration<String> headerNames = servletRequest.getHeaderNames();
                while(headerNames.hasMoreElements()) {
                    String header = headerNames.nextElement();
                    httpHeaders.put(header, Collections.list(servletRequest.getHeaders(header)));
                }
            }
            httpHeaders.putAll(singleRequest.getHeaders());

            BatchResponse.Entity singleResponse = processSingleRequest(
                    getSingleRequestUri(singleRequest, servletRequest),
                    singleRequest.getMethod(),
                    httpHeaders,
                    singleRequest.getBody(),
                    servletRequest,
                    servletResponse);
            batchResponse.addPart(singleResponse);
        }
        servletResponse.setHeader(HttpHeaders.CONTENT_TYPE, BatchMediaType.MULTIPART_BATCH_VALUE);
        return batchResponse;
    }

    /**
     * Process a single request from a batch.
     *
     * @param targetUri the target uri for the single request
     * @param httpMethod the method
     * @param httpHeaders the headers
     * @param body the body
     * @param servletRequest the original request
     * @param servletResponse the original response
     * @return a single response
     */
    abstract protected BatchResponse.Entity processSingleRequest(
            URI targetUri,
            HttpMethod httpMethod,
            HttpHeaders httpHeaders,
            byte[] body,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) throws ServletException, IOException;

    protected URI getSingleRequestUri(BatchRequest.Entity singleRequest, HttpServletRequest servletRequest) throws MalformedURLException {
        String url = singleRequest.getUrl();
        String uriString = null;
        if (url.startsWith("/")) {
            int queryIndex = url.indexOf('?');
            String path = queryIndex == -1 ? url : url.substring(0, queryIndex);
            String query = queryIndex == -1 ? "" : url.substring(queryIndex + 1);
            UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
            uriString = builder.scheme(servletRequest.getScheme()).host(servletRequest.getServerName()).port(servletRequest.getServerPort()).path(path).query(query).toUriString();
        } else {
            uriString = UriComponentsBuilder.fromHttpUrl(url).toUriString();
        }
        return URI.create(uriString);
    }
}
