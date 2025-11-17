package net.seesharpsoft.spring.suite.boot;

import net.seesharpsoft.spring.data.web.OffsetLimitPageHandlerMethodArgumentResolver;
import net.seesharpsoft.spring.data.web.SpecificationHandlerMethodArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(ConfigurationProperties.class)
public class SuiteWebConfiguration extends WebMvcConfigurationSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuiteWebConfiguration.class);

    @Autowired
    ConfigurationProperties properties;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = ConfigurationProperties.WEB_RESOLVER_SPECIFICATION)
    SpecificationHandlerMethodArgumentResolver specificationHandlerMethodArgumentResolver(
            Converter<String, Specification<?>> specificationConverter) {

        LOGGER.debug("Creating SpecificationHandlerMethodArgumentResolver bean with converter: {} and expressionDialect: {}",
                specificationConverter, properties.getExpressionDialect());

        return new SpecificationHandlerMethodArgumentResolver(specificationConverter);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = ConfigurationProperties.WEB_RESOLVER_LIMIT_OFFSET)
    SortHandlerMethodArgumentResolver sortHandlerMethodArgumentResolver() {

        LOGGER.debug("Creating SortHandlerMethodArgumentResolver bean "
                + "(pageable handler enabled: {}, specification handler enabled: {})",
                properties.isPageableHandlerEnabled(),
                properties.isSpecificationHandlerEnabled());

        return new SortHandlerMethodArgumentResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = ConfigurationProperties.WEB_RESOLVER_LIMIT_OFFSET)
    OffsetLimitPageHandlerMethodArgumentResolver pageableHandlerMethodArgumentResolver(
            @Lazy SortHandlerMethodArgumentResolver sortHandlerMethodArgumentResolver) {

        LOGGER.debug("Creating OffsetLimitPageHandlerMethodArgumentResolver bean using SortHandlerMethodArgumentResolver: {}",
                sortHandlerMethodArgumentResolver);

        return new OffsetLimitPageHandlerMethodArgumentResolver(sortHandlerMethodArgumentResolver);
    }
}