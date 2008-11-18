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
package org.geotools.data.db2;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.SQLDialect;



/**
 * DataStoreFactory for DB2 database.
 *
 * @author Christian Mueller
 *
 */
public class DB2NGDataStoreFactory extends JDBCDataStoreFactory {
	
	public final static String DriverClassName = "com.ibm.db2.jcc.DB2Driver"; 
	
    protected SQLDialect createSQLDialect(JDBCDataStore dataStore) {
        return new DB2SQLDialectPrepared(dataStore);
    }

    public String getDisplayName() {
        return "DB2";
    }
    
    protected String getDriverClassName() {
        return DriverClassName;
    }

    protected String getDatabaseID() {
        return "db2";
    }

    public String getDescription() {
        return "DB2 Database";
    }
    
    @Override
    protected String getValidationQuery() {
        return "select current date from sysibm.sysdummy1";
    }
}
