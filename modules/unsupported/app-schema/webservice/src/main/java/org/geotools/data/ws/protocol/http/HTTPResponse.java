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
package org.geotools.data.ws.protocol.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * A handle to an HTTP response headers and contents.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id: HTTPResponse.java 37306 2011-05-25 06:13:21Z mbedward $
 * @since 2.6
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/unsupported/app-schema/webservice/src/main/java/org/geotools/data/ws/protocol/http/HTTPResponse.java $
 */
public interface HTTPResponse {

    /**
     * Returns the stream to the response contents ready to be consumed, whether gzip encoding is
     * being used or not.
     * 
     * @throws IOException
     *             the plain stream to the response contents
     */
    public InputStream getResponseStream() throws IOException;
}
