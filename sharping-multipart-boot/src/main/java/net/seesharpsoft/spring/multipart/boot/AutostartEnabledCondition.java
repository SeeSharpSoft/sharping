package net.seesharpsoft.spring.multipart.boot;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static net.seesharpsoft.spring.multipart.boot.ConfigurationProperties.PROPERTIES_ROOT;

public class AutostartEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        return conditionContext.getEnvironment().getProperty(String.format("%s.mode", PROPERTIES_ROOT), RequestProcessingMode.class, RequestProcessingMode.None) != RequestProcessingMode.None;
    }
}