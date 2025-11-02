package net.seesharpsoft.spring.multipart.boot.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.seesharpsoft.spring.multipart.batch.services.BatchRequestProperties;
import net.seesharpsoft.spring.multipart.batch.services.DispatcherBatchRequestService;
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;

public class BootDispatcherBatchRequestService extends DispatcherBatchRequestService implements FilterChain {

    protected DelegatingFilterProxyRegistrationBean filterRegistrationBean;

    public BootDispatcherBatchRequestService(
            BatchRequestProperties batchRequestProperties,
            DispatcherServlet dispatcherServlet,
            DelegatingFilterProxyRegistrationBean filterRegistrationBean
    ) {
        super(batchRequestProperties, dispatcherServlet);
        this.filterRegistrationBean = filterRegistrationBean;
    }

    @Override
    protected void dispatchRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (filterRegistrationBean == null) {
            doFilter(request, response);
        } else {
            filterRegistrationBean.getFilter().doFilter(request, response, this);
        }

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        this.servlet.service(servletRequest, servletResponse);
    }
}
