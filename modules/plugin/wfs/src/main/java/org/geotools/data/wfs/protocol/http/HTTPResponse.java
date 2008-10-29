package org.geotools.data.wfs.protocol.http;

import java.io.IOException;
import java.io.InputStream;

public interface HTTPResponse {

    public InputStream getResponseStream() throws IOException;
    
    public String getResponseCharset();
    
    public String getContentType();
}
