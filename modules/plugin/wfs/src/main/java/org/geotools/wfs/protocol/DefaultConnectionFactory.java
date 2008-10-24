/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.wfs.protocol;

import static org.geotools.data.wfs.HttpMethod.POST;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.geotools.data.wfs.HttpMethod;
import org.geotools.util.logging.Logging;

/**
 * Handles setting up connections to a WFS based on a WFS capabilities document,
 * taking care of GZIP and authentication.
 * 
 * @author Gabriel Roldan
 * @version $Id: DefaultConnectionFactory.java 29055 2008-02-02 17:38:44Z
 *          groldan $
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/main/java/org/geotools/wfs/io/WFSConnectionFactory.java $
 */
public class DefaultConnectionFactory implements ConnectionFactory {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private final boolean tryGzip;

    private final Authenticator auth;

    private Charset encoding;

    private String authUser;

    private String authPass;

    /**
     * A simple user/password authenticator
     * 
     * @author Gabriel Roldan
     * @version $Id: DefaultConnectionFactory.java 29055 2008-02-02 17:38:44Z
     *          groldan $
     * @since 2.5.x
     * @source $URL:
     *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/main/java/org/geotools/data/wfs/WFSDataStoreFactory.java $
     */
    private static class WFSAuthenticator extends Authenticator {
        private java.net.PasswordAuthentication pa;

        /**
         * 
         * @param user
         * @param pass
         */
        public WFSAuthenticator(String user, String pass) {
            pa = new java.net.PasswordAuthentication(user, pass.toCharArray());
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            return pa;
        }
    }

    public DefaultConnectionFactory() {
        this(false, null, null, DEFAULT_CHARSET);
    }

    /**
     * Creates a connection factory set up for the given tryGzip flag, HTTP
     * authentication if needed, and default character encoding.
     * 
     * @param tryGzip
     * @param user
     * @param pass
     * @param encoding
     */
    public DefaultConnectionFactory(final boolean tryGzip, final String user, final String pass,
            final Charset encoding) {
        this.tryGzip = tryGzip;
        this.authUser = user;
        this.authPass = pass;

        if (user != null && pass != null) {
            auth = new WFSAuthenticator(user, pass);
        } else {
            auth = null;
        }

        this.encoding = encoding;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.wfs.protocol.ConnectionFactory#getEncoding()
     */
    public Charset getEncoding() {
        return encoding;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.wfs.protocol.ConnectionFactory#getConnection(java.net.URL,
     *      org.geotools.wfs.protocol.HttpMethod)
     */
    public HttpURLConnection getConnection(URL query, HttpMethod method) throws IOException {
        return getConnection(query, tryGzip, method, auth);
    }

    private static HttpURLConnection getConnection(final URL url, final boolean tryGzip,
            final HttpMethod method, final Authenticator auth) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        if (POST == method) {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-type", "text/xml, application/xml");
        } else {
            connection.setRequestMethod("GET");
        }
        connection.setDoInput(true);
        /*
         * FIXME this could breaks uDig. Not quite sure what to do otherwise.
         * Maybe have a mechanism that would allow an authenticator to ask the
         * datastore itself for a previously supplied user/pass.
         */
        if (auth != null) {
            synchronized (Authenticator.class) {
                Authenticator.setDefault(auth);
                connection.connect();
                Authenticator.setDefault(null);
            }
        }

        if (tryGzip) {
            connection.addRequestProperty("Accept-Encoding", "gzip");
        }

        return connection;
    }

    public InputStream getInputStream(HttpURLConnection hc) throws IOException {
        return getInputStream(hc, tryGzip);
    }

    public InputStream getInputStream(URL query, HttpMethod method) throws IOException {
        HttpURLConnection connection = getConnection(query, method);
        InputStream inputStream = getInputStream(connection);
        return inputStream;
    }

    /**
     * If the connection content-encoding contains the {@code gzip} flag creates
     * a gzip inputstream, otherwise returns a normal buffered input stream by
     * opening the http connection.
     * 
     * @param hc
     *            the connection to use to create the stream
     * @return an input steam from the provided connection
     */
    private static InputStream getInputStream(final HttpURLConnection hc, final boolean tryGZIP)
            throws IOException {
        InputStream is = hc.getInputStream();

        if (tryGZIP) {
            if (hc.getContentEncoding() != null && hc.getContentEncoding().indexOf("gzip") != -1) {
                is = new GZIPInputStream(is);
            }
        }
        is = new BufferedInputStream(is);
        // special logger for communication information only.
        Logger logger = Logging.getLogger("org.geotools.data.communication");
        return is;
    }

    public String getAuthPassword() {
        return authPass;
    }

    public String getAuthUsername() {
        return authUser;
    }

    public boolean isTryGzip() {
        return tryGzip;
    }
}
