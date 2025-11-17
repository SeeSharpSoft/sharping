package net.seesharpsoft.spring.suite.boot;

import net.seesharpsoft.UnhandledSwitchCaseException;
import net.seesharpsoft.spring.data.domain.SelectableRepositoryFactory;
import net.seesharpsoft.spring.data.domain.SqlParser;
import net.seesharpsoft.spring.data.domain.impl.SelectableRepositoryFactoryImpl;
import net.seesharpsoft.spring.data.domain.impl.SqlParserImpl;
import net.seesharpsoft.spring.data.jpa.SpecificationConverter;
import net.seesharpsoft.spring.data.jpa.expression.Dialects;
import net.seesharpsoft.spring.data.jpa.expression.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.domain.Specification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;

@Configuration
@EnableConfigurationProperties(ConfigurationProperties.class)
public class SuiteDataConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuiteDataConfiguration.class);

    @Autowired
    ConfigurationProperties properties;

    @Bean
    @ConditionalOnMissingBean
    @Conditional(SpecificationConverterEnabledCondition.class)
    Converter<String, Specification> specificationConverter(@Lazy ConversionService conversionService) {
        LOGGER.debug("Creating Specification converter with expressionDialect={}", properties.getExpressionDialect());

        switch (properties.getExpressionDialect()) {
            case Sql:
                LOGGER.trace("Using SQL dialect for SpecificationConverter");
                return new SpecificationConverter(Dialects.SQL, conversionService);
            case OData:
                LOGGER.trace("Using OData dialect for SpecificationConverter");
                return new SpecificationConverter(Dialects.ODATA, conversionService);
            case Default:
                LOGGER.trace("Using default (JAVA) dialect for SpecificationConverter");
                return new SpecificationConverter(Dialects.JAVA, conversionService);
            case None:
                LOGGER.warn("ExpressionDialect.NONE configured - SpecificationConverter cannot be created");
                throw new UnsupportedOperationException();
            default:
                LOGGER.error("Unhandled ExpressionDialect: {}", properties.getExpressionDialect());
                throw new UnhandledSwitchCaseException(properties.getExpressionDialect());
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = ConfigurationProperties.SELECTABLE_ENABLED)
    SqlParser sqlParser(@Lazy ConversionService conversionService) {
        LOGGER.debug("Creating SqlParser bean (selectable enabled: {})",
                properties.isSelectableRepositoryEnabled());
        return new SqlParserImpl(new Parser(Dialects.SQL, conversionService));
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = ConfigurationProperties.SELECTABLE_ENABLED)
    SelectableRepositoryFactory selectableRepositoryFactory(EntityManager entityManager, SqlParser parser) {
        LOGGER.debug("Creating SelectableRepositoryFactory bean with EntityManager={} and SqlParser={}",
                entityManager, parser);
        return new SelectableRepositoryFactoryImpl(entityManager, parser);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = ConfigurationProperties.SELECTABLE_ENABLED)
    static SharpingRegistryPostProcessor selectableRepositoryRegistryPostProcessor(Environment environment) {
        Logger log = LoggerFactory.getLogger(SuiteDataConfiguration.class);
        log.debug("Creating SharpingRegistryPostProcessor bean with Environment={}", environment);
        return new SharpingRegistryPostProcessor(environment);
    }
}