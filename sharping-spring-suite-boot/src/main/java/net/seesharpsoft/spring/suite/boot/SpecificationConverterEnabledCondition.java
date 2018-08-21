package net.seesharpsoft.spring.suite.boot;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class SpecificationConverterEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        return conditionContext.getEnvironment().getProperty(ConfigurationProperties.EXPRESSION_DIALECT, ExpressionDialect.class, ExpressionDialect.None) != ExpressionDialect.None;
    }
}