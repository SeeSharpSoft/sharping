package net.seesharpsoft.spring.suite.boot;

import net.seesharpsoft.UnhandledSwitchCaseException;
import net.seesharpsoft.spring.data.jpa.SpecificationConverter;
import net.seesharpsoft.spring.data.jpa.expression.Dialects;
import net.seesharpsoft.spring.data.web.SpecificationHandlerMethodArgumentResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
@EnableConfigurationProperties(ConfigurationProperties.class)
public class SuiteConfiguration extends WebMvcConfigurationSupport implements BeanPostProcessor {

    @Autowired
    ConfigurationProperties properties;
    
    @Autowired
    ConversionService conversionService;
    
    @Conditional(SpecificationConverterEnabledCondition.class)
    @Bean
    Converter<String, Specification> specificationConverter() {
        switch (properties.getExpressionDialect()) {
            case OData:
                return new SpecificationConverter(Dialects.ODATA, conversionService);
            case Default:
                return new SpecificationConverter(Dialects.JAVA, conversionService);
            case None:
                throw new UnsupportedOperationException();
            default:
                throw new UnhandledSwitchCaseException(properties.getExpressionDialect());
        }
    }
    
    @Bean
    @ConditionalOnMissingBean
    SpecificationHandlerMethodArgumentResolver specificationHandlerMethodArgumentResolver(Converter<String, Specification> specificationConverter) {
        return new SpecificationHandlerMethodArgumentResolver(specificationConverter);
    }

    /******** BeanPostProcessor *******/

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /******** BeanPostProcessor - END *******/
}
