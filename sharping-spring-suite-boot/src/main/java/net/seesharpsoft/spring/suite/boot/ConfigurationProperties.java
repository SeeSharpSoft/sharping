package net.seesharpsoft.spring.suite.boot;

import static net.seesharpsoft.spring.suite.boot.ConfigurationProperties.PROPERTIES_ROOT;

@org.springframework.boot.context.properties.ConfigurationProperties(PROPERTIES_ROOT)
public class ConfigurationProperties {

    public static final String PROPERTIES_ROOT = "sharping";
    
    public static final String WEB_ROOT = PROPERTIES_ROOT + ".web";
    public static final String WEB_RESOLVER_ROOT = WEB_ROOT + ".resolver";
    public static final String WEB_RESOLVER_SPECIFICATION = WEB_RESOLVER_ROOT + ".specification";
    public static final String WEB_RESOLVER_LIMIT_OFFSET = WEB_RESOLVER_ROOT + ".pageable";
    
    public static final String EXPRESSION_ROOT = PROPERTIES_ROOT + ".expression";
    public static final String EXPRESSION_DIALECT = EXPRESSION_ROOT + ".dialect";

    public static final String SELECTABLE_ROOT = PROPERTIES_ROOT + ".selectable";
    public static final String SELECTABLE_ENABLED = SELECTABLE_ROOT + ".enabled";
    public static final String SELECTABLE_IMPL_CLASS = SELECTABLE_ROOT + ".repositoryBaseClass";
    public static final String SELECTABLE_BASE_PACKAGES = SELECTABLE_ROOT + ".packages";

    private ExpressionDialect expressionDialect = ExpressionDialect.None;
    private boolean specificationHandlerEnabled = false;
    private boolean pageableHandlerEnabled = false;
    private boolean selectableRepositoryEnabled = true;
    
    public ExpressionDialect getExpressionDialect() {
        return expressionDialect;
    }
    public void setExpressionDialect(ExpressionDialect expressionDialect) {
        this.expressionDialect = expressionDialect;
    }
    public boolean isSpecificationHandlerEnabled() {
        return specificationHandlerEnabled;
    }
    public void setSpecificationHandlerEnabled(boolean specificationHandlerEnabled) {
        this.specificationHandlerEnabled = specificationHandlerEnabled;
    }
    public boolean isPageableHandlerEnabled() {
        return pageableHandlerEnabled;
    }
    public void setPageableHandlerEnabled(boolean pageableHandlerEnabled) {
        this.pageableHandlerEnabled = pageableHandlerEnabled;
    }
    public boolean isSelectableRepositoryEnabled() {
        return selectableRepositoryEnabled;
    }
    public void setSelectableEnabled(boolean selectableRepositoryEnabled) {
        this.selectableRepositoryEnabled = selectableRepositoryEnabled;
    }
}
