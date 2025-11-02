package net.seesharpsoft.spring.multipart.boot.controller;

import net.seesharpsoft.spring.multipart.batch.BatchRequest;
import net.seesharpsoft.spring.multipart.batch.services.BatchRequestService;
import net.seesharpsoft.spring.multipart.batch.BatchResponse;
import net.seesharpsoft.spring.multipart.boot.AutostartEnabledCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static net.seesharpsoft.spring.multipart.boot.ConfigurationProperties.PROPERTIES_ENDPOINT_DEFAULT;
import static net.seesharpsoft.spring.multipart.boot.ConfigurationProperties.PROPERTIES_ENDPOINT_PATH;

@RestController
@Conditional(AutostartEnabledCondition.class)
public class BatchRequestController {

    @Autowired
    BatchRequestService batchRequestService;

    /**
     * Process a batch request.
     * @param batchRequest the batch request entity
     * @param servletRequest the original request
     * @param servletResponse the original response
     * @return a batch response
     * @throws IOException if an input or output error occurs while processing the request
     * @throws ServletException if the servlet request cannot be handled
     */
    @RequestMapping(value =  "${" + PROPERTIES_ENDPOINT_PATH + ":" + PROPERTIES_ENDPOINT_DEFAULT + "}", method = RequestMethod.POST)
    public BatchResponse batch(@RequestBody BatchRequest batchRequest,
                               HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) throws IOException, ServletException {
        return batchRequestService.process(batchRequest, servletRequest, servletResponse);
    }

}
