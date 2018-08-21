package net.seesharpsoft.spring.data.web;

import net.seesharpsoft.spring.data.domain.OffsetLimitRequest;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public class PageableHandlerMethodArgumentResolver extends org.springframework.data.web.PageableHandlerMethodArgumentResolver {

    private static final SortHandlerMethodArgumentResolver DEFAULT_SORT_RESOLVER = new SortHandlerMethodArgumentResolver();
    
    private static final String DEFAULT_OFFSET_PARAMETER = "offset";
    private static final String DEFAULT_LIMIT_PARAMETER = "limit";

    private String offsetParameterName = DEFAULT_OFFSET_PARAMETER;
    private String limitParameterName = DEFAULT_LIMIT_PARAMETER;
    
    private final SortArgumentResolver sortArgumentResolver;

    /**
     * Constructs an instance of this resolved with a default {@link SortHandlerMethodArgumentResolver}.
     */
    public PageableHandlerMethodArgumentResolver() {
        this((SortArgumentResolver) null);
    }

    /**
     * Constructs an instance of this resolver with the specified {@link SortHandlerMethodArgumentResolver}.
     *
     * @param sortArgumentResolver the sort resolver to use
     */
    public PageableHandlerMethodArgumentResolver(SortHandlerMethodArgumentResolver sortArgumentResolver) {
        this((SortArgumentResolver) sortArgumentResolver);
    }

    /**
     * Constructs an instance of this resolver with the specified {@link SortArgumentResolver}.
     *
     * @param sortArgumentResolver the sort resolver to use
     */
    public PageableHandlerMethodArgumentResolver(SortArgumentResolver sortArgumentResolver) {
        this.sortArgumentResolver = sortArgumentResolver == null ? DEFAULT_SORT_RESOLVER : sortArgumentResolver;
    }
    
    /**
     * Configures the parameter name to be used to find the offset in the request. Defaults to {@code offset}.
     *
     * @param offsetParameterName the parameter name to be used, must not be {@literal null} or empty.
     */
    public void setOffsetParameterName(String offsetParameterName) {

        Assert.hasText(offsetParameterName, "Offset parameter name must not be null or empty!");
        this.offsetParameterName = offsetParameterName;
    }

    /**
     * Retrieves the parameter name to be used to find the offset in the request. Defaults to {@code offset}.
     *
     * @return the parameter name to be used, never {@literal null} or empty.
     */
    public String getOffsetParameterName() {
        return this.offsetParameterName;
    }

    /**
     * Configures the parameter name to be used to find the limit in the request. Defaults to {@code limit}.
     *
     * @param limitParameterName the parameter name to be used, must not be {@literal null} or empty.
     */
    public void setLimitParameterName(String limitParameterName) {

        Assert.hasText(limitParameterName, "Limit parameter name must not be null or empty!");
        this.limitParameterName = limitParameterName;
    }

    /**
     * Retrieves the parameter name to be used to find the limit in the request. Defaults to {@code limit}.
     *
     * @return the parameter name to be used, never {@literal null} or empty.
     */
    public String getLimitParameterName() {
        return this.limitParameterName;
    }

    @Override
    public Pageable resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer,
                                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        String offsetString = webRequest.getParameter(getParameterNameToUse(offsetParameterName, methodParameter));
        String limitString = webRequest.getParameter(getParameterNameToUse(limitParameterName, methodParameter));

        boolean offsetAndLimitGiven = StringUtils.hasText(offsetString) && StringUtils.hasText(limitString);

        if (!offsetAndLimitGiven) {
            return super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
        }

        int offset = tryParseInteger(offsetString, 0);
        int limit = tryParseInteger(limitString, getMaxPageSize());

        return new OffsetLimitRequest(offset, limit, sortArgumentResolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory));
    }
    
    private int tryParseInteger(String intString, int defaultValue) {
        try {
            return Integer.parseInt(intString);
        } catch(NumberFormatException exc) {
            return defaultValue;
        }
    }
}
