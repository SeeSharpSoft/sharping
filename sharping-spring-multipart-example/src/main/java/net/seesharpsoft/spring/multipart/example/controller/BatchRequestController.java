package net.seesharpsoft.spring.multipart.example.controller;

import net.seesharpsoft.spring.multipart.batch.BatchRequest;
import net.seesharpsoft.spring.multipart.batch.services.BatchRequestProperties;
import net.seesharpsoft.spring.multipart.batch.services.BatchRequestService;
import net.seesharpsoft.spring.multipart.batch.BatchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class BatchRequestController {

    @Autowired
    BatchRequestService batchRequestService;

    @RequestMapping(value = "/my/own/multipart/endpoint", method = RequestMethod.POST)
    public BatchResponse batch(@RequestBody BatchRequest batchRequest,
                               HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) throws IOException, ServletException {
        return batchRequestService.process(batchRequest, servletRequest, servletResponse);
    }
}
