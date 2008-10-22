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
package org.geotools.data.mysql;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.SQLDialect;


/**
 * DataStoreFactory for MySQL database.
 *
 * @author David Winslow, The Open Planning Project
 *
 */
public class MySQLDataStoreFactory extends JDBCDataStoreFactory {
    protected SQLDialect createSQLDialect(JDBCDataStore dataStore) {
        return new MySQLDialect(dataStore);
    }

    public String getDisplayName() {
        return "MySQL";
    }
    
    protected String getDriverClassName() {
        return "com.mysql.jdbc.Driver";
    }

    protected String getDatabaseID() {
        return "mysql";
    }

    public String getDescription() {
        return "MySQL Database";
    }
}
