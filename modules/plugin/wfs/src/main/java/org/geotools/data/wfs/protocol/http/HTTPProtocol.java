package org.geotools.data.wfs.protocol.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public interface HTTPProtocol {

    public void setTryGzip( boolean tryGzip );

    public boolean isTryGzip();

    public void setAuth( String username, String password );

    public void setTimeoutMillis( int milliseconds );

    public int getTimeoutMillis();

    /**
     * Issues an HTTP request over the {@code baseUrl} with a query string defined by the {@code
     * kvp} key/value pair of parameters.
     * <p>
     * If the base url query is not empty and already contains a parameter named as one of the
     * parameters in {@code kvp}, the original parameter value in the baseUrl query is overriden by
     * the one in the {@code kvp} map. For this purpose, the parameter name matching comparison is
     * made case insensitively.
     * </p>
     * 
     * @param baseUrl the URL where to fetch the contents from
     * @param kvp the set of key/value pairs to create the actual URL query string, may be empty
     * @return the server response of issuing the HTTP request through GET method
     * @throws IOException if a communication error of some sort occurs
     * @see #createUrl(URL, Map)
     */
    public HTTPResponse issueGet( URL baseUrl, Map<String, String> kvp ) throws IOException;

    /**
     * Creates an URL with {@code baseUrl} and a query string defined by the {@code kvp} key/value
     * pair of parameters.
     * <p>
     * If the base url query is not empty and already contains a parameter named as one of the
     * parameters in {@code kvp}, the original parameter value in the baseUrl query is overriden by
     * the one in the {@code kvp} map. For this purpose, the parameter name matching comparison is
     * made case insensitively.
     * </p>
     * 
     * @param baseUrl the original URL to create the new one from
     * @param kvp the set of key/value pairs to create the actual URL query string, may be empty
     * @return the new URL with {@code baseUrl} and the query string from {@code queryStringKvp}
     * @throws MalformedURLException if the resulting URL is not valid
     */
    public URL createUrl( URL baseUrl, Map<String, String> queryStringKvp )
            throws MalformedURLException;

}
