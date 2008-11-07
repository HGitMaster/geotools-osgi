package org.geotools.data.wfs.protocol.http;

import java.io.IOException;
import java.io.InputStream;

public interface HTTPResponse {

    public String getTargetUrl();

    public InputStream getResponseStream() throws IOException;

    public String getResponseHeader( String headerName );

    public String getResponseCharset();

    /**
     * Shortcut method to get the response content-type header
     */
    public String getContentType();
}
