package net.seesharpsoft.spring.multipart.batch;

import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * Extends the StandardServletMultipartResolver to exclude batch media type request from the usual multipart handling.
 */
public class BatchMultipartResolver implements MultipartResolver {

    private MultipartResolver multipartResolver;

    private static final String EXCLUDED_MEDIA_TYPE_VALUE = BatchMediaType.MULTIPART_BATCH.getType().toLowerCase() +
            '/' + BatchMediaType.MULTIPART_BATCH.getSubtype().toLowerCase();

    public BatchMultipartResolver(MultipartResolver resolver) {
        this.multipartResolver = resolver;
    }

    public BatchMultipartResolver() {
        this(new StandardServletMultipartResolver());
    }

    @Override
    public boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return multipartResolver.isMultipart (request) && contentType != null
                && !contentType.toLowerCase().startsWith(EXCLUDED_MEDIA_TYPE_VALUE);
    }

    @Override
    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest httpServletRequest) throws MultipartException {
        return multipartResolver.resolveMultipart(httpServletRequest);
    }

    @Override
    public void cleanupMultipart(MultipartHttpServletRequest multipartHttpServletRequest) {
        multipartResolver.cleanupMultipart(multipartHttpServletRequest);
    }
}
