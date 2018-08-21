package net.seesharpsoft.spring.suite.boot;

import static net.seesharpsoft.spring.suite.boot.ConfigurationProperties.PROPERTIES_ROOT;

@org.springframework.boot.context.properties.ConfigurationProperties(PROPERTIES_ROOT)
public class ConfigurationProperties {

    public static final String PROPERTIES_ROOT = "sharping.suite";
    public static final String EXPRESSION_ROOT = PROPERTIES_ROOT + ".expression";
    public static final String EXPRESSION_DIALECT = EXPRESSION_ROOT + ".dialect";

    private ExpressionDialect expressionDialect = ExpressionDialect.None;
    
    public ExpressionDialect getExpressionDialect() {
        return expressionDialect;
    }

    public void setExpressionDialect(ExpressionDialect expressionDialect) {
        this.expressionDialect = expressionDialect;
    }

    
}
