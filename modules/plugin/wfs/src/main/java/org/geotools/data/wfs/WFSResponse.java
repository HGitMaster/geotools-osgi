package org.geotools.data.wfs;

import java.io.InputStream;
import java.nio.charset.Charset;

public class WFSResponse {

    private Charset charset;
    private String contentType;
    private InputStream inputStream;

    /**
     * @param charset the response charset, {@code null} if unknown, utf-8 will be assumed then
     * @param contentType the response content type
     * @param in the response inpnut stream ready to be consumed
     */
    public WFSResponse( Charset charset, String contentType, InputStream in ) {
        if (charset == null) {
            this.charset = Charset.forName("UTF-8");
        } else {
            this.charset = charset;
        }
        this.contentType = contentType;
        this.inputStream = in;
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

    @Override
    public String toString() {
        return new StringBuilder("WFSResponse[charset=").append(charset).append(", contentType=")
                .append(contentType).append("]").toString();
    }
}
