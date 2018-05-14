package net.seesharpsoft.spring.multipart.boot.demo.service;

import net.seesharpsoft.spring.multipart.batch.BatchRequest;
import net.seesharpsoft.spring.multipart.batch.BatchResponse;
import net.seesharpsoft.spring.multipart.batch.services.BatchRequestProperties;
import net.seesharpsoft.spring.multipart.batch.services.DispatcherBatchRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class MyDispatcherBatchRequestService extends DispatcherBatchRequestService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResponse process(BatchRequest batchRequest,
                                 BatchRequestProperties properties,
                                 HttpServletRequest servletRequest,
                                 HttpServletResponse servletResponse) throws ServletException, IOException {
        return super.process(batchRequest, properties, servletRequest, servletResponse);
    }
}
