package net.seesharpsoft.spring.multipart.boot.controller;

import net.seesharpsoft.spring.multipart.batch.BatchRequest;
import net.seesharpsoft.spring.multipart.batch.services.BatchRequestProperties;
import net.seesharpsoft.spring.multipart.batch.services.BatchRequestService;
import net.seesharpsoft.spring.multipart.batch.BatchResponse;
import net.seesharpsoft.spring.multipart.boot.AutostartEnabledCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static net.seesharpsoft.spring.multipart.boot.ConfigurationProperties.PROPERTIES_ENDPOINT_DEFAULT;
import static net.seesharpsoft.spring.multipart.boot.ConfigurationProperties.PROPERTIES_ENDPOINT_PATH;

@RestController
@Conditional(AutostartEnabledCondition.class)
public class BatchRequestController {

    @Autowired
    BatchRequestService batchRequestService;

    @Autowired
    BatchRequestProperties batchRequestProperties;

    /**
     * Process a batch request.
     * @param batchRequest the batch request entity
     * @param servletRequest the original request
     * @param servletResponse the original response
     * @return a batch response
     * @throws IOException
     * @throws ServletException
     */
    @RequestMapping(value =  "${" + PROPERTIES_ENDPOINT_PATH + ":" + PROPERTIES_ENDPOINT_DEFAULT + "}", method = RequestMethod.POST)
    public BatchResponse batch(@RequestBody BatchRequest batchRequest,
                               HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) throws IOException, ServletException {
        return batchRequestService.process(batchRequest, batchRequestProperties, servletRequest, servletResponse);
    }

}
