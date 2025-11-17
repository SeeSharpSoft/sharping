package net.seesharpsoft.spring.data.web;

import net.seesharpsoft.spring.data.jpa.MultiInput2SpecificationConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpecificationHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String DEFAULT_FILTER_PARAMETER = "filter";
    private static final String DEFAULT_PREFIX = "";
    private static final String DEFAULT_QUALIFIER_DELIMITER = "_";

    private String filterParameterName = DEFAULT_FILTER_PARAMETER;
    private String prefix = DEFAULT_PREFIX;
    private String qualifierDelimiter = DEFAULT_QUALIFIER_DELIMITER;
    private Converter<String, Specification<?>> stringToSpecificationConverter;
    private Converter<String[], Specification<?>> stringsToSpecificationConverter;
    private static final Logger LOGGER = LoggerFactory.getLogger(SpecificationHandlerMethodArgumentResolver.class);

    public SpecificationHandlerMethodArgumentResolver(Converter<String, Specification<?>> stringToSpecificationConverter) {
        Assert.notNull(stringToSpecificationConverter, "String to Specification converter must be provided!");
        this.stringToSpecificationConverter = stringToSpecificationConverter;
        this.stringsToSpecificationConverter = new MultiInput2SpecificationConverter(this.stringToSpecificationConverter);
        LOGGER.debug("Created SpecificationHandlerMethodArgumentResolver with converter={}", stringToSpecificationConverter);
    }

    /**
     * Configures the parameter name to be used to find the filter in the request. Defaults to {@code filter}.
     *
     * @param filterParameterName the parameter name to be used, must not be {@literal null} or empty.
     */
    public void setFilterParameterName(String filterParameterName) {

        Assert.hasText(filterParameterName, "Filter parameter name must not be null or empty!");
        LOGGER.debug("Setting filterParameterName from '{}' to '{}'", this.filterParameterName, filterParameterName);
        this.filterParameterName = filterParameterName;
    }

    /**
     * Retrieves the parameter name to be used to find the filter in the request. Defaults to {@code filter}.
     *
     * @return the parameter name to be used, never {@literal null} or empty.
     */
    public String getFilterParameterName() {
        return this.filterParameterName;
    }

    /**
     * Configures a general prefix to be prepended to filter parameters. Useful to namespace the
     * property names used in case they are clashing with ones used by your application. By default, no prefix is used.
     *
     * @param prefix the prefix to be used or {@literal null} to reset to the default.
     */
    public void setPrefix(String prefix) {
        LOGGER.debug("Setting prefix from '{}' to '{}'", this.prefix, prefix);
        this.prefix = prefix == null ? DEFAULT_PREFIX : prefix;
    }

    /**
     * The delimiter to be used between the qualifier and the actual filter properties. Defaults to
     * {@code _}. So a qualifier of {@code foo} will result in a filter parameter of {@code foo_filter}.
     *
     * @param qualifierDelimiter the delimiter to be used or {@literal null} to reset to the default.
     */
    public void setQualifierDelimiter(String qualifierDelimiter) {
        LOGGER.debug("Setting qualifierDelimiter from '{}' to '{}'", this.qualifierDelimiter, qualifierDelimiter);
        this.qualifierDelimiter = qualifierDelimiter == null ? DEFAULT_QUALIFIER_DELIMITER : qualifierDelimiter;
    }

    /**
     * Returns the name of the request parameter to find the {@link Specification} information in. Inspects the given
     * {@link MethodParameter} for {@link Qualifier} present and prefixes the given source parameter name with it.
     *
     * @param source          the basic parameter name.
     * @param methodParameter the {@link MethodParameter} potentially qualified.
     * @return the name of the request parameter.
     */
    protected String getParameterNameToUse(String source, MethodParameter methodParameter) {
        StringBuilder builder = new StringBuilder(prefix);
        if (methodParameter != null && methodParameter.hasParameterAnnotation(Qualifier.class)) {
            builder.append(methodParameter.getParameterAnnotation(Qualifier.class).value());
            builder.append(qualifierDelimiter);
        }
        String parameterName = builder.append(source).toString();
        LOGGER.debug("Resolved filter parameter name '{}' for method parameter '{}'",
                parameterName,
                methodParameter == null ? "null" : methodParameter.getParameterName());
        return parameterName;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        boolean supported = Specification.class.equals(methodParameter.getParameterType());
        LOGGER.debug("supportsParameter for '{}': {}", methodParameter.getParameterName(), supported);
        return supported;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        String parameterName = getParameterNameToUse(getFilterParameterName(), methodParameter);
        String[] filterValues = nativeWebRequest.getParameterValues(parameterName);

        LOGGER.debug("Resolving Specification argument for parameter '{}' using request parameter '{}', values={}",
                methodParameter.getParameterName(), parameterName, filterValues);

        Object result = filterValues == null ?
                stringToSpecificationConverter.convert(null) :
                filterValues.length == 1 ?
                        stringToSpecificationConverter.convert(filterValues[0]) :
                        stringsToSpecificationConverter.convert(filterValues);

        LOGGER.debug("Resolved Specification argument for parameter '{}' -> {}",
                methodParameter.getParameterName(), result);

        return result;
    }

}
