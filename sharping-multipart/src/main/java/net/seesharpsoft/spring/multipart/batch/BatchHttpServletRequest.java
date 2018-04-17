package net.seesharpsoft.spring.multipart.batch;

import net.seesharpsoft.spring.util.ByteArrayServletInputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MimeType;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * HttpServletRequest implementation to be used for a single request from a batch.
 */
public class BatchHttpServletRequest extends HttpServletRequestWrapper {

    private byte[] content;

    private URI uri;

    private HttpMethod method;

    private HttpHeaders headers;

    private Map<String, String[]> parameters;

    private Map<String, Object> attributes;

    private MimeType mimeType;

    private Map<String, String[]> resolveParameters(URI uri) {
        Map<String, String[]> result = new HashMap<>();
        String query = uri.getQuery();
        if (query != null && !query.isEmpty()) {
            String[] parameters = query.split("&");
            for (String parameter : parameters) {
                String[] keyValue = parameter.split("=");
                String key = keyValue[0];
                String[] values = result.get(key);
                if (values == null) {
                    values = new String[1];
                } else {
                    values = Arrays.copyOf(values, values.length + 1);
                }
                values[values.length - 1] = keyValue.length > 1 ? keyValue[1] : "";
                result.put(key, values);
            }
        }
        return result;
    }

    private void initAdditionalRequestInformation(URI uri, HttpServletRequest request, HttpHeaders headers) {
        // resolve uri query parameters
        this.parameters = resolveParameters(uri);
        // copy attributes
        this.attributes = new HashMap<>();
//        Enumeration<String> attributeNames = request.getAttributeNames();
//        while(attributeNames.hasMoreElements()) {
//            String attribute = attributeNames.nextElement();
//            attributes.put(attribute, request.getAttribute(attribute));
//        }
        // copy headers
        this.headers = new HttpHeaders();
        this.headers.putAll(headers);
    }

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public BatchHttpServletRequest(HttpServletRequest request, URI uri, HttpMethod method, HttpHeaders headers, byte[] body, MimeType mimeType) {
        super(request);
        initAdditionalRequestInformation(uri, request, headers);
        this.uri = uri;
        this.method = method;
        this.content = body;
        this.mimeType = mimeType;
    }

    @Override
    public String getMethod() {
        return this.method.toString();
    }

    /************ Headers ************/

    @Override
    public String getHeader(String name) {
        return this.headers.getFirst(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return this.headers.get(name) == null ? Collections.emptyEnumeration() : Collections.enumeration(this.headers.get(name));
    }

    @Override
    public long getDateHeader(String name) {
        return this.headers.getFirstDate(name);
    }

    @Override
    public int getIntHeader(String name) {
        String header = this.getHeader(name);
        if (header == null) {
            return -1;
        }

        return Integer.parseInt(header);
    }

    @Override
    public Enumeration getHeaderNames() {
        return Collections.enumeration(this.headers.keySet());
    }

    /************* Request URI parts ************/

    @Override
    public String getScheme() {
        return this.uri.getScheme();
    }

    @Override
    public String getServerName() {
        return this.uri.getHost();
    }

    @Override
    public int getServerPort() {
        return uri.getPort();
    }

    @Override
    public String getContextPath() {
        return "";
    }

    @Override
    public String getServletPath() {
        return "";
    }

    @Override
    public String getPathInfo() {
        return this.uri.getPath();
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getQueryString() {
        return this.uri.getQuery();
    }

    @Override
    public String getRequestURI() {
        return this.getContextPath() + this.getServletPath()
                + this.getPathInfo();
    }

    @Override
    public StringBuffer getRequestURL() {
        //protocol, server name, port number, and server path
        return new StringBuffer(this.getScheme())
                .append("://")
                .append(this.getServerName())
                .append(':')
                .append(this.getServerPort())
                .append(this.getRequestURI());
    }

    /********* Request parameters *************/

    @Override
    public String getParameter(String name) {
        String[] values = this.getParameterValues(name);
        return values != null && values.length > 0 ? values[0] : null;
    }

    @Override
    public String[] getParameterValues(String name) {
        return this.parameters.get(name);
    }

    @Override
    public Enumeration getParameterNames() {
        return Collections.enumeration(this.parameters.keySet());
    }

    @Override
    public Map getParameterMap() {
        return Collections.unmodifiableMap(this.parameters);
    }

    /********* Request attributes *********/

    @Override
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public Enumeration getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (value != null) {
            this.attributes.put(name, value);
        } else {
            this.removeAttribute(name);
        }
    }

    @Override
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    /********* Content *********/

    @Override
    public ServletInputStream getInputStream() {
        return new ByteArrayServletInputStream(this.content);
    }

    @Override
    public int getContentLength() {
        return this.content == null ? 0 : this.content.length;
    }

    @Override
    public String getContentType() {
        return this.mimeType.toString();
    }

}
