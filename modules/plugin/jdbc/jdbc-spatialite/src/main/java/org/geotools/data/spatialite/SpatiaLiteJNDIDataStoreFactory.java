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
package org.geotools.data.spatialite;

import org.geotools.jdbc.JDBCJNDIDataStoreFactory;

/**
 * JNDI DataStoreFactory for SpatiaLite database. 
 * 
 * @author Christian Mueller
 * 
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/plugin/jdbc/jdbc-spatialite/src/main/java/org/geotools/data/spatialite/SpatiaLiteJNDIDataStoreFactory.java $
 */
public class SpatiaLiteJNDIDataStoreFactory extends JDBCJNDIDataStoreFactory {

    public SpatiaLiteJNDIDataStoreFactory() {
        super(new SpatiaLiteDataStoreFactory());
    }
}
