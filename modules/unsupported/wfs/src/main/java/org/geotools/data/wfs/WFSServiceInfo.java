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
package org.geotools.data.wfs;

import org.geotools.data.ServiceInfo;

/**
 * Extends the standard {@link ServiceInfo} interface to provide WFS specific metadata.
 * 
 * @author Gabriel Roldan
 * @version $Id: WFSServiceInfo.java 37306 2011-05-25 06:13:21Z mbedward $
 * @since 2.5.x
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/unsupported/wfs/src/main/java/org/geotools/data/wfs/WFSServiceInfo.java $
 *         http://gtsvn.refractions.net/trunk/modules/plugin/wfs/src/main/java/org/geotools/data
 *         /wfs/WFSServiceInfo.java $
 * @see WFSDataStore#getInfo()
 */
public interface WFSServiceInfo extends ServiceInfo {

    /**
     * @return the WFS protocol version
     */
    public String getVersion();
}
