package net.seesharpsoft.spring.multipart.boot.demo.service;

import net.seesharpsoft.spring.multipart.batch.BatchRequest;
import net.seesharpsoft.spring.multipart.batch.BatchResponse;
import net.seesharpsoft.spring.multipart.batch.services.BatchRequestProperties;
import net.seesharpsoft.spring.multipart.batch.services.DispatcherBatchRequestService;
import net.seesharpsoft.spring.multipart.boot.services.BootDispatcherBatchRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import java.io.IOException;

//@Service
public class MyDispatcherBatchRequestService extends DispatcherBatchRequestService {

    public MyDispatcherBatchRequestService(
            BatchRequestProperties batchRequestProperties,
            DispatcherServlet dispatcherServlet
    ) {
        super(batchRequestProperties, dispatcherServlet);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResponse process(BatchRequest batchRequest,
                                 HttpServletRequest servletRequest,
                                 HttpServletResponse servletResponse) throws ServletException, IOException {
        return super.process(batchRequest, servletRequest, servletResponse);
    }
}
