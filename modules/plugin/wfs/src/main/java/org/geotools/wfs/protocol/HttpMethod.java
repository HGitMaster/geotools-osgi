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
package org.geotools.wfs.protocol;

/**
 * Enumeration to specify the preferred HTTP method a WFS datastore to use then
 * accessing WFS operations.
 * 
 * @author Gabriel Roldan
 * @version $Id: HttpMethod.java 30666 2008-06-12 23:11:43Z acuster $
 * @since 2.5.x
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/plugin/wfs/src/main/java/org/geotools/wfs/protocol/HttpMethod.java $
 */
public enum HttpMethod {
    GET, POST;
}
