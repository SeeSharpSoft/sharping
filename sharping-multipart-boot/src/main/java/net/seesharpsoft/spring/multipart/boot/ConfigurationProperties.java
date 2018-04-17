package net.seesharpsoft.spring.multipart.boot;

import net.seesharpsoft.spring.multipart.batch.services.BatchRequestProperties;

import static net.seesharpsoft.spring.multipart.boot.ConfigurationProperties.PROPERTIES_ROOT;

@org.springframework.boot.context.properties.ConfigurationProperties(PROPERTIES_ROOT)
public class ConfigurationProperties {

    public static final String PROPERTIES_ROOT = "net.seesharpsoft.spring.multipart";
    public static final String PROPERTIES_ENDPOINT_PATH = PROPERTIES_ROOT + ".endpoint";
    public static final String PROPERTIES_ENDPOINT_DEFAULT = "/batch";

    private String endpoint = PROPERTIES_ENDPOINT_DEFAULT;
    private RequestProcessingMode mode = RequestProcessingMode.None;
    private Boolean wrapMultiPartResolver = true;
    private BatchRequestProperties properties = new BatchRequestProperties();

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public RequestProcessingMode getMode() {
        return mode;
    }

    public void setMode(RequestProcessingMode mode) {
        this.mode = mode;
    }

    public Boolean getWrapMultiPartResolver() {
        return wrapMultiPartResolver;
    }

    public void setWrapMultiPartResolver(Boolean wrapMultiPartResolver) {
        this.wrapMultiPartResolver = wrapMultiPartResolver;
    }

    public BatchRequestProperties getProperties() {
        return properties;
    }

    public void setProperties(BatchRequestProperties properties) {
        this.properties = properties;
    }
}
