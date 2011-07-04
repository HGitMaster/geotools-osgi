/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

/**
 * Enumeration to specify the preferred HTTP method a WFS datastore to use then
 * accessing WFS operations.
 * 
 * @author Gabriel Roldan
 * @version $Id: HttpMethod.java 37306 2011-05-25 06:13:21Z mbedward $
 * @since 2.5.x
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/unsupported/wfs/src/main/java/org/geotools/data/wfs/protocol/http/HttpMethod.java $
 */
public enum HttpMethod {
    GET, POST;
}
