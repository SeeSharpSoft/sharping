package net.seesharpsoft.spring.multipart.example;

import net.seesharpsoft.spring.multipart.batch.BatchMessageConverter;
import net.seesharpsoft.spring.multipart.batch.BatchMultipartResolver;
import net.seesharpsoft.spring.multipart.batch.services.BatchRequestService;
import net.seesharpsoft.spring.multipart.batch.services.DispatcherBatchRequestService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Configuration
public class AppConfiguration extends WebMvcConfigurationSupport {

    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(batchRequestMessageConverter());
        addDefaultHttpMessageConverters(converters);
    }

    @Bean
    HttpMessageConverter batchRequestMessageConverter() {
        return new BatchMessageConverter();
    }

    @Bean
    BatchRequestService batchService() {
        return new DispatcherBatchRequestService();
    }

    @Bean
    MultipartResolver multipartResolver() {
        return new BatchMultipartResolver();
    }

}
