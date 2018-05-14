package net.seesharpsoft.spring.multipart.batch;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * HttpServletResponse implementation to be used as response entity for single request from a batch.
 */
public class BatchHttpServletResponse implements HttpServletResponse {

    private PrintWriter writer;

    private ByteArrayServletOutputStream outputStream;

    private HttpHeaders headers;

    private String contentType;

    private int status;

    private String charset;

    private Locale locale;

    private void initAdditionalRequestInformation(HttpServletResponse response) {
        // copy headers
        this.headers = new HttpHeaders();
        Collection<String> headerNames = response.getHeaderNames();
        for(String header : headerNames) {
            this.headers.put(header, new ArrayList<>(response.getHeaders(header)));
        }
        this.charset = response.getCharacterEncoding();
        this.locale = response.getLocale();
    }

    /**
     * Constructs a response object wrapping the given response.
     *
     * @param response The response to wrap
     * @throws IllegalArgumentException if the response is null
     */
    public BatchHttpServletResponse(HttpServletResponse response) {
        initAdditionalRequestInformation(response);
        this.status = SC_OK;
    }

    public void addCookie(Cookie cookie) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatus(int statusCode) {
        this.status = statusCode;
    }

    @Override
    public void setStatus(int sc, String sm) {
        throw new UnsupportedOperationException("deprecated");
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    /************ Headers ************/

    public HttpHeaders getHeaderObject() {
        return this.headers;
    }

    @Override
    public String getHeader(String name) {
        return this.headers.getFirst(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return this.headers.get(name);
    }

    @Override
    public void setHeader(String name, String value) {
        this.headers.set(name, value);
    }

    @Override
    public void setDateHeader(String name, long date) {
        this.headers.setDate(name, date);
    }

    @Override
    public void setIntHeader(String name, int value) {
        this.setHeader(name, String.valueOf(value));
    }

    @Override
    public void addHeader(String name, String value) {
        this.headers.add(name, value);
    }

    @Override
    public void addDateHeader(String name, long date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.addHeader(name, dateFormat.format(new Date(date)));
    }

    @Override
    public void addIntHeader(String name, int value) {
        this.addHeader(name, String.valueOf(value));
    }

    @Override
    public Collection getHeaderNames() {
        return this.headers.keySet();
    }

    @Override
    public boolean containsHeader(String name) {
        return this.headers.containsKey(name);
    }

    @Override
    public String encodeURL(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeRedirectURL(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeUrl(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeRedirectUrl(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.status = sc;
        throw new HttpServerErrorException(HttpStatus.valueOf(this.status), msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        this.sendError(sc, HttpStatus.valueOf(sc).getReasonPhrase());
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        throw new UnsupportedOperationException();
    }

    /********* Content *********/

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.outputStream == null) {
            this.outputStream = new ByteArrayServletOutputStream();
        }
        return this.outputStream;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public void setContentType(String type) {
        this.contentType = type;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.charset = charset;
    }

    @Override
    public String getCharacterEncoding() {
        return this.charset;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.writer == null) {
            this.writer = new PrintWriter(this.getOutputStream());
        }
        return this.writer;
    }

    @Override
    public void setContentLength(int length) {
        this.headers.setContentLength(length);
    }

    @Override
    public void setContentLengthLong(long length) {
        this.headers.setContentLength(length);
    }

    @Override
    public void resetBuffer() {
        this.writer.close();
        this.writer = null;
        try {
            this.outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.outputStream = null;
    }

    @Override
    public void setBufferSize(int size) {
    }

    @Override
    public int getBufferSize() {
        return 0;
    }


    @Override
    public void flushBuffer() throws IOException {
        this.getOutputStream().flush();
    }

    @Override
    public boolean isCommitted() {
        return status != 0;
    }

    @Override
    public void reset() {
        this.headers.clear();
        this.resetBuffer();
    }

    @Override
    public void setLocale(Locale loc) {
        this.locale = loc;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    public byte[] getContent() throws IOException {
        return ((ByteArrayServletOutputStream)this.getOutputStream()).toByteArray();
    }

    private static class ByteArrayServletOutputStream extends ServletOutputStream {

        private ByteArrayOutputStream buffer;

        public ByteArrayServletOutputStream() {
            this.buffer = new ByteArrayOutputStream();
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener listener) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public void write(int b) throws IOException {
            this.buffer.write(b);
        }

        public byte[] toByteArray() {
            return buffer.toByteArray();
        }
    }
}
