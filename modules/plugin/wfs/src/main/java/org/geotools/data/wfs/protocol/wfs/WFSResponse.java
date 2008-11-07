package org.geotools.data.wfs.protocol.wfs;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.opengis.wfs.BaseRequestType;

import org.geotools.util.logging.Logging;

@SuppressWarnings("nls")
public class WFSResponse {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs.protocol.wfs");

    private Charset charset;
    private String contentType;
    private InputStream inputStream;

    private BaseRequestType request;

    private String targetUrl;

    /**
     * @param charset the response charset, {@code null} if unknown, utf-8 will be assumed then
     * @param contentType the response content type
     * @param in the response input stream ready to be consumed
     */
    public WFSResponse( String targetUrl, BaseRequestType originatingRequest, Charset charset,
            String contentType, InputStream in ) {
        this.targetUrl = targetUrl;
        this.request = originatingRequest;
        if (charset == null) {
            this.charset = Charset.forName("UTF-8");
        } else {
            this.charset = charset;
        }
        this.contentType = contentType;
        this.inputStream = in;
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("WFS response: charset=" + charset + ", contentType=" + contentType);
        }
    }

    /**
     * Returns the character encoding if set by the server as an http header, if unknown assumes
     * {@code UTF-8}
     * 
     * @return the character set for the response if set, or {@code null}
     */
    public Charset getCharacterEncoding() {
        return charset;
    }

    /**
     * Returns the WFS response declared content type
     * 
     * @return the content type of the response
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * The open input stream for the response contents
     * 
     * @return the input stream for the response
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Allows to replace the input stream
     * 
     * @param in
     */
    public void setInputStream( InputStream in ) {
        this.inputStream = in;
    }

    public BaseRequestType getOriginatingRequest() {
        return request;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    @Override
    public String toString() {
        return new StringBuilder("WFSResponse[charset=").append(charset).append(", contentType=")
                .append(contentType).append("]").toString();
    }
}
