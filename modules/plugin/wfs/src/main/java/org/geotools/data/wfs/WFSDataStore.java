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
package org.geotools.data.wfs;

import org.geotools.data.DataAccess;
import org.geotools.data.DataStore;

/**
 * {@link DataStore} extension interface to provide WFS specific extra information.
 * 
 * @author Gabriel Roldan
 * @version $Id: WFSDataStore.java 31731 2008-10-29 13:51:20Z groldan $
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/main/java/org/geotools
 *         /data/wfs/WFSDataStore.java $
 */
public interface WFSDataStore extends DataStore {
    /**
     * Overrides {@link DataAccess#getInfo()} so it type narrows to a {@link WFSServiceInfo}
     * 
     * @return service information
     * @see DataAccess#getInfo()
     */
    WFSServiceInfo getInfo();

}
