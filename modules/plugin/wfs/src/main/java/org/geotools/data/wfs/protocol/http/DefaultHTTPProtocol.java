/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.wfs.protocol.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.geotools.util.logging.Logging;

/**
 * Default implementation of {@link HTTPProtocol} based on apache's common-http-client
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id: DefaultHTTPProtocol.java 31823 2008-11-11 16:11:49Z groldan $
 * @since 2.6
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/plugin/wfs/src/main/java/org/geotools/data/wfs/protocol/http/DefaultHTTPProtocol.java $
 */
@SuppressWarnings("nls")
public class DefaultHTTPProtocol implements HTTPProtocol {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs.protocol.http");

    /**
     * An {@link HTTPResponse} wrapping an executed {@link GetMethod} or {@link PostMethod} from the
     * apache commons-httpclient package
     * 
     * @author Gabriel Roldan (OpenGeo)
     */
    public class HTTPClientResponse implements HTTPResponse {

        private HttpMethodBase method;

        public HTTPClientResponse(HttpMethodBase method) {
            this.method = method;
        }

        /**
         * @see HTTPResponse#getResponseStream()
         */
        public InputStream getResponseStream() throws IOException {
            InputStream responseStream = method.getResponseBodyAsStream();

            String encoding = getResponseHeader("Content-Encoding");
            if (encoding != null) {
                if (encoding.toLowerCase().indexOf("gzip") > -1) {
                    LOGGER.finest("Response is GZIP encoded, wrapping with gzip input stream");
                    responseStream = new GZIPInputStream(responseStream);
                }
            } else {
                LOGGER.finest("HTTP response is not gzip encoded");
            }
            return responseStream;
        }

        public String getContentType() {
            return getResponseHeader("Content-Type");
        }

        public String getResponseCharset() {
            String responseCharSet = method.getResponseCharSet();
            return responseCharSet;
        }

        /**
         * @see HTTPResponse#getResponseHeader()
         */
        public String getResponseHeader(String headerName) {
            Header header = method.getResponseHeader(headerName);
            String headerValue = null;
            if (header != null) {
                headerValue = header.getValue();
            }
            return headerValue;
        }

        /**
         * @see HTTPResponse#getTargetUrl()
         */
        public String getTargetUrl() {
            try {
                URI uri = method.getURI();
                return uri.toString();
            } catch (URIException e) {
                LOGGER.log(Level.FINE, "can't get HTTP request URI", e);
            }
            return null;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(getClass().getSimpleName());
            sb.append("[Method=").append(method.getName());
            sb.append("\n\tHTTP version=").append(method.getEffectiveVersion());
            sb.append("\n\tstatus code=").append(method.getStatusCode());
            sb.append("\n\tpath=").append(method.getPath());
            sb.append("\n\tquery string=").append(method.getQueryString());
            sb.append("\n\tresponse charset=").append(getResponseCharset());
            Header[] responseHeaders = method.getResponseHeaders();
            sb.append("\n\tresponse headers=");
            for (Header header : responseHeaders) {
                sb.append(header.toExternalForm());
            }
            sb.append("]");
            return sb.toString();
        }
    }

    private boolean tryGzip;

    private String authUsername;

    private String authPassword;

    private int timeoutMillis;

    /**
     * @see HTTPProtocol#isTryGzip()
     */
    public boolean isTryGzip() {
        return this.tryGzip;
    }

    /**
     * @see HTTPProtocol#setTryGzip(boolean)
     */
    public void setTryGzip(boolean tryGzip) {
        this.tryGzip = tryGzip;
    }

    /**
     * @see HTTPProtocol#setAuth(String, String)
     */
    public void setAuth(String username, String password) {
        this.authUsername = username;
        this.authPassword = password;
    }

    /**
     * @see HTTPProtocol#
     */
    public int getTimeoutMillis() {
        return this.timeoutMillis;
    }

    /**
     * @see HTTPProtocol#setTimeoutMillis(int)
     */
    public void setTimeoutMillis(int milliseconds) {
        this.timeoutMillis = milliseconds;
    }

    /**
     * @see HTTPProtocol#createUrl(URL, Map)
     */
    public URL createUrl(final URL baseUrl, final Map<String, String> queryStringKvp)
            throws MalformedURLException {
        final String finalUrlString = createUri(baseUrl, queryStringKvp);
        URL queryUrl = new URL(finalUrlString);
        return queryUrl;
    }

    private String createUri(final URL baseUrl, final Map<String, String> queryStringKvp) {
        final String query = baseUrl.getQuery();
        Map<String, String> finalKvpMap = new HashMap<String, String>(queryStringKvp);
        if (query != null) {
            Map<String, String> userParams = new CaseInsensitiveMap(queryStringKvp);
            String[] rawUrlKvpSet = query.split("&");
            for (String rawUrlKvp : rawUrlKvpSet) {
                int eqIdx = rawUrlKvp.indexOf('=');
                String key, value;
                if (eqIdx > 0) {
                    key = rawUrlKvp.substring(0, eqIdx);
                    value = rawUrlKvp.substring(eqIdx + 1);
                } else {
                    key = rawUrlKvp;
                    value = null;
                }
                if (userParams.containsKey(key)) {
                    LOGGER.fine("user supplied value for query string argument " + key
                            + " overrides the one in the base url");
                } else {
                    finalKvpMap.put(key, value);
                }
            }
        }

        String protocol = baseUrl.getProtocol();
        String host = baseUrl.getHost();
        int port = baseUrl.getPort();
        String path = baseUrl.getPath();

        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append("://").append(host);
        if (port != -1 && port != baseUrl.getDefaultPort()) {
            sb.append(':');
            sb.append(port);
        }
        if (!"".equals(path) && !path.startsWith("/")) {
            sb.append('/');
        }
        sb.append(path).append('?');

        String key, value;
        try {
            for (Map.Entry<String, String> kvp : finalKvpMap.entrySet()) {
                key = kvp.getKey();
                value = kvp.getValue();
                if (value == null) {
                    value = "";
                } else {
                    value = URLEncoder.encode(value, "UTF-8");
                }
                sb.append(key);
                sb.append('=');
                sb.append(value);
                sb.append('&');
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        final String finalUrlString = sb.toString();
        return finalUrlString;
    }

    public HTTPResponse issueGet(final URL baseUrl, final Map<String, String> kvp)
            throws IOException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("About to issue GET request to " + baseUrl.toExternalForm()
                    + " with query parameters: " + kvp);
        }

        final String uri = createUri(baseUrl, kvp);
        HttpClient client = new HttpClient();
        GetMethod request = new GetMethod(uri);

        if (isTryGzip()) {
            LOGGER.finest("Adding 'Accept-Encoding=gzip' header to request");
            request.addRequestHeader("Accept-Encoding", "gzip");
        }

        int statusCode;
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Executing HTTP GET request: " + uri);
        }
        // TODO: remove this
        System.err.println("Executing HTTP GET request: " + uri);
        try {
            statusCode = client.executeMethod(request);
        } catch (IOException e) {
            request.releaseConnection();
            throw e;
        }
        if (statusCode != HttpStatus.SC_OK) {
            request.releaseConnection();
            String statusText = HttpStatus.getStatusText(statusCode);
            throw new IOException("Request failed with status code " + statusCode + "("
                    + statusText + "): " + uri);
        }

        HTTPResponse httpResponse = new HTTPClientResponse(request);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Got " + httpResponse);
        }
        return httpResponse;
    }
}
