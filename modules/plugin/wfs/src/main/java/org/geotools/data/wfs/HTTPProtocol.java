package org.geotools.data.wfs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public interface HTTPProtocol {

    public void setGzipResponses( boolean gzipResponses );

    public boolean isGzipResponses();

    public void setGzipPostRequests( boolean gzipPostRequests );

    public boolean isGzipPostRequests();

    public HTTPResponse issueGet( URL baseUrl, Map<String, String> kvp ) throws IOException;

    public URL createUrl( URL baseUrl, Map<String, String> queryStringKvp ) throws MalformedURLException;

}
