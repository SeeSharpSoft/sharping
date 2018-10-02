package net.seesharpsoft.spring.multipart.boot.services;

import net.seesharpsoft.spring.multipart.batch.services.DispatcherBatchRequestService;
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class BootDispatcherBatchRequestService extends DispatcherBatchRequestService implements FilterChain {

    protected DelegatingFilterProxyRegistrationBean filterRegistrationBean;

    public BootDispatcherBatchRequestService(DispatcherServlet dispatcherServlet,
                                             DelegatingFilterProxyRegistrationBean filterRegistrationBean) {
        super(dispatcherServlet);
        this.filterRegistrationBean = filterRegistrationBean;
    }

    @Override
    protected void dispatchRequest(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        if (filterRegistrationBean == null) {
            doFilter(request, response);
        } else {
            filterRegistrationBean.getFilter().doFilter(request, response, this);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
        this.servlet.service(servletRequest, servletResponse);
    }
}
