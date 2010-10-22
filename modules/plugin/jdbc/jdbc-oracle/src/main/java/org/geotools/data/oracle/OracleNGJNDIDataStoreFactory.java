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
package org.geotools.data.oracle;

import java.util.Map;

import org.geotools.jdbc.JDBCJNDIDataStoreFactory;

/**
 * JNDI DataStoreFactory for oracle database.
 * 
 * @author Christian Mueller
 * 
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/plugin/jdbc/jdbc-oracle/src/main/java/org/geotools/data/oracle/OracleNGJNDIDataStoreFactory.java $
 */
public class OracleNGJNDIDataStoreFactory extends JDBCJNDIDataStoreFactory {

    public OracleNGJNDIDataStoreFactory() {
        super(new OracleNGDataStoreFactory());
    }
    
    @Override
    protected void setupParameters(Map parameters) {
        super.setupParameters(parameters);
        
        parameters.put(OracleNGDataStoreFactory.LOOSEBBOX.key, OracleNGDataStoreFactory.LOOSEBBOX);
        parameters.put(MAX_OPEN_PREPARED_STATEMENTS.key, MAX_OPEN_PREPARED_STATEMENTS);
    }
}
