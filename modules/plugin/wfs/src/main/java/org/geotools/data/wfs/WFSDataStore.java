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

import java.net.URL;
import java.util.Set;

import org.geotools.data.DataAccess;
import org.geotools.data.DataStore;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * {@link DataStore} extension interface to provide WFS specific extra information.
 * 
 * @author Gabriel Roldan
 * @version $Id: WFSDataStore.java 31752 2008-11-01 15:04:56Z groldan $
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

    public String getFeatureTypeTitle(String typeName);

    public String getFeatureTypeAbstract(String typeName);

    public ReferencedEnvelope getFeatureTypeWGS84Bounds(String typeName);

    public ReferencedEnvelope getFeatureTypeBounds(String typeName);

    public CoordinateReferenceSystem getFeatureTypeCRS(String typeName);

    public Set<String> getFeatureTypeKeywords(String typeName);

    public URL getDescribeFeatureTypeURL(String typeName);

}
