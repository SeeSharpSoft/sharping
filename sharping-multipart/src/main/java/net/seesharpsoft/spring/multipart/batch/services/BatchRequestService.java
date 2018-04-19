package net.seesharpsoft.spring.multipart.batch.services;

import net.seesharpsoft.spring.multipart.batch.BatchRequest;
import net.seesharpsoft.spring.multipart.batch.BatchResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface BatchRequestService {

    /**
     * Process a batch request.
     *
     * @param batchRequest the batch request entity
     * @param properties the request properties
     * @param servletRequest the original request
     * @param servletResponse the original response
     * @return a batch response
     * @throws IOException if an input or output error occurs while processing the request
     * @throws ServletException if the servlet request cannot be handled
     */
    BatchResponse process(BatchRequest batchRequest,
                          BatchRequestProperties properties,
                          HttpServletRequest servletRequest,
                          HttpServletResponse servletResponse) throws ServletException, IOException;
}
