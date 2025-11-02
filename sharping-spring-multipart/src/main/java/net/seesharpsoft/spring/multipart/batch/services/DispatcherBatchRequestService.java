package net.seesharpsoft.spring.multipart.batch.services;

import net.seesharpsoft.spring.multipart.batch.BatchHttpServletRequest;
import net.seesharpsoft.spring.multipart.batch.BatchHttpServletResponse;
import net.seesharpsoft.spring.multipart.batch.BatchResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;


public class DispatcherBatchRequestService extends BatchRequestServiceBase {

    protected DispatcherServlet servlet;

    public DispatcherBatchRequestService(
            BatchRequestProperties batchRequestProperties,
            DispatcherServlet dispatcherServlet
    ) {
        super(batchRequestProperties);
        this.servlet = dispatcherServlet;
    }

    @Override
    protected BatchResponse.Entity processSingleRequest(
            URI targetUri,
            HttpMethod httpMethod,
            HttpHeaders httpHeaders,
            byte[] body,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse)
            throws ServletException, IOException {

        BatchHttpServletRequest requestWrapper = new BatchHttpServletRequest(servletRequest, targetUri, httpMethod, httpHeaders, body, httpHeaders.getContentType());
        BatchHttpServletResponse responseWrapper = new BatchHttpServletResponse(servletResponse);

        this.dispatchRequest(requestWrapper, responseWrapper);

        BatchResponse.Entity responseEntry = new BatchResponse.Entity();
        responseEntry.setBody(responseWrapper.getContent());
        responseEntry.setHeaders(responseWrapper.getHeaderObject());
        responseEntry.setStatus(HttpStatus.valueOf(responseWrapper.getStatus()));
        return responseEntry;
    }

    protected void dispatchRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        servlet.service(request, response);
    }
}
