package net.seesharpsoft.spring.multipart.batch.services;

import jakarta.annotation.PreDestroy;
import net.seesharpsoft.spring.multipart.batch.BatchMediaType;
import net.seesharpsoft.spring.multipart.batch.BatchRequest;
import net.seesharpsoft.spring.multipart.batch.BatchResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class BatchRequestServiceBase implements BatchRequestService {

    protected final ExecutorService executorService;
    protected BatchRequestProperties batchRequestProperties;

    public BatchRequestServiceBase(BatchRequestProperties batchRequestProperties) {
        this.batchRequestProperties = batchRequestProperties;

        // Initialize thread pool if parallel processing is enabled
        if (batchRequestProperties.isParallelProcessing()) {
            int threadPoolSize = batchRequestProperties.getThreadPoolSize();
            this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        } else {
            this.executorService = null;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public BatchResponse process(BatchRequest batchRequest,
                                 HttpServletRequest servletRequest,
                                 HttpServletResponse servletResponse) throws ServletException, IOException {

        BatchResponse batchResponse = new BatchResponse();

        boolean parallelProcessing = batchRequestProperties.isParallelProcessing();

        if (parallelProcessing && executorService != null) {
            processInParallel(batchRequest, servletRequest, servletResponse, batchResponse);
        } else {
            processSequentially(batchRequest, servletRequest, servletResponse, batchResponse);
        }

        servletResponse.setHeader(HttpHeaders.CONTENT_TYPE, BatchMediaType.MULTIPART_BATCH_VALUE);
        return batchResponse;
    }

    private void processSequentially(BatchRequest batchRequest,
                                     HttpServletRequest servletRequest,
                                     HttpServletResponse servletResponse,
                                     BatchResponse batchResponse) throws ServletException, IOException {
        for (BatchRequest.Entity singleRequest : batchRequest.getParts()) {
            HttpHeaders httpHeaders = prepareHeaders(servletRequest, singleRequest);

            BatchResponse.Entity singleResponse = processSingleRequest(
                    getSingleRequestUri(singleRequest, servletRequest),
                    singleRequest.getMethod(),
                    httpHeaders,
                    singleRequest.getBody(),
                    servletRequest,
                    servletResponse);
            batchResponse.addPart(singleResponse);
        }
    }

    private void processInParallel(BatchRequest batchRequest,
                                   HttpServletRequest servletRequest,
                                   HttpServletResponse servletResponse,
                                   BatchResponse batchResponse) throws ServletException, IOException {
        List<BatchRequest.Entity> parts = batchRequest.getParts();
        List<CompletableFuture<BatchResponse.Entity>> futures = new ArrayList<>();

        for (BatchRequest.Entity singleRequest : parts) {
            HttpHeaders httpHeaders = prepareHeaders(servletRequest, singleRequest);

            CompletableFuture<BatchResponse.Entity> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return processSingleRequest(
                            getSingleRequestUri(singleRequest, servletRequest),
                            singleRequest.getMethod(),
                            httpHeaders,
                            singleRequest.getBody(),
                            servletRequest,
                            servletResponse);
                } catch (Exception e) {
                    return createErrorResponse(singleRequest, e);
                }
            }, executorService);

            futures.add(future);
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            for (CompletableFuture<BatchResponse.Entity> future : futures) {
                batchResponse.addPart(future.get());
            }
        } catch (Exception e) {
            throw new ServletException("Error processing batch requests in parallel", e);
        }
    }

    private HttpHeaders prepareHeaders(HttpServletRequest servletRequest,
                                       BatchRequest.Entity singleRequest) {
        HttpHeaders httpHeaders = new HttpHeaders();

        if (batchRequestProperties.getIncludeOriginalHeader()) {
            Enumeration<String> headerNames = servletRequest.getHeaderNames();
            while(headerNames.hasMoreElements()) {
                String header = headerNames.nextElement();
                httpHeaders.put(header, Collections.list(servletRequest.getHeaders(header)));
            }
        }
        httpHeaders.addAll(singleRequest.getHeaders() == null ? new HttpHeaders() : singleRequest.getHeaders());

        return httpHeaders;
    }

    private BatchResponse.Entity createErrorResponse(BatchRequest.Entity request, Exception e) {
        BatchResponse.Entity errorResponse = new BatchResponse.Entity();
        errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        errorResponse.setBody(("Error processing request: " + e.getMessage()).getBytes());
        return errorResponse;
    }

    protected Charset getUrlEncoding() {
        return StandardCharsets.UTF_8;
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

    protected URI getSingleRequestUri(BatchRequest.Entity singleRequest, HttpServletRequest servletRequest) throws MalformedURLException, UnsupportedEncodingException {
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
        return URI.create(URLDecoder.decode(uriString, getUrlEncoding().name()));
    }
}
