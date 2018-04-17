package net.seesharpsoft.spring.multipart.boot;

import net.seesharpsoft.spring.multipart.MultipartRfc2046MessageConverter;
import net.seesharpsoft.spring.multipart.batch.BatchMessageConverter;
import net.seesharpsoft.spring.multipart.batch.BatchMultipartResolver;
import net.seesharpsoft.spring.multipart.batch.services.BatchRequestProperties;
import net.seesharpsoft.spring.multipart.batch.services.BatchRequestService;
import net.seesharpsoft.spring.multipart.batch.services.DispatcherBatchRequestService;
import net.seesharpsoft.spring.multipart.batch.services.RestBatchRequestService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Configuration
@EnableConfigurationProperties(ConfigurationProperties.class)
public class MultipartConfiguration extends WebMvcConfigurationSupport implements BeanPostProcessor {

    @Autowired
    ConfigurationProperties properties;

    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(batchMessageConverter());
        converters.add(rfc2046MessageConverter());
        addDefaultHttpMessageConverters(converters);
    }

    @Bean
    @ConditionalOnMissingBean
    BatchMessageConverter batchMessageConverter() {
        return new BatchMessageConverter();
    }

    @Bean
    MultipartRfc2046MessageConverter rfc2046MessageConverter() {
        return new BatchMessageConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    @Conditional(AutostartEnabledCondition.class)
    BatchRequestService batchRequestService() {
        switch (properties.getMode()) {
            case None:
                return null;
            case LocalDispatch:
                return new DispatcherBatchRequestService();
            case HttpRequest:
                return new RestBatchRequestService();
            default:
                throw new RuntimeException(String.format("mode '%s' not handled", properties.getMode()));
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @Conditional(AutostartEnabledCondition.class)
    BatchRequestProperties batchRequestProperties() {
        return new BatchRequestProperties(properties.getProperties());
    }

    /******** BeanPostProcessor *******/

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (Boolean.TRUE.equals(properties.getWrapMultiPartResolver()) && bean instanceof MultipartResolver) {
            return new BatchMultipartResolver((MultipartResolver)bean);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /******** BeanPostProcessor - END *******/
}
