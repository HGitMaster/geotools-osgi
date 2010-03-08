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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.geotools.data.DataAccessFactory.Param;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.SQLDialect;



/**
 * DataStoreFactory for DB2 database.
 *
 * @author Christian Mueller
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/plugin/jdbc/jdbc-db2/src/main/java/org/geotools/data/db2/DB2NGDataStoreFactory.java $
 */
public class DB2NGDataStoreFactory extends JDBCDataStoreFactory {

    public static int MajorVersion,MinorVersion;
    public static String ProductName,ProductVersion;
    public static String GetCurrentSchema = "select current sqlid from sysibm.sysdummy1";
    
    /** parameter for database type */
    public static final Param DBTYPE = new Param("dbtype", String.class, "Type", true, "db2");
    
	public final static String DriverClassName = "com.ibm.db2.jcc.DB2Driver"; 
	
    protected SQLDialect createSQLDialect(JDBCDataStore dataStore) {
        return new DB2SQLDialectPrepared(dataStore);
    }

    public String getDisplayName() {
        return "DB2 NG";
    }
    
    protected String getDriverClassName() {
        return DriverClassName;
    }

    protected String getDatabaseID() {
        return (String) DBTYPE.sample;
    }

    public String getDescription() {
        return "DB2 Database";
    }
    
    @Override
    protected String getValidationQuery() {
        return "select current date from sysibm.sysdummy1";
    }
    
    @Override
    protected boolean checkDBType(Map params) {
        if (super.checkDBType(params)) {
            return true;
        }
        
        //check also for "DB2" which is iold db type, but only when the old
        // factory is not on the classpath
        if (checkDBType(params, "DB2")) {
            try {
                Class.forName("org.geotools.data.db2.DB2DataStoreFactory");
                
                //old factory is around, let it handle the connection
                return false;
            } 
            catch (ClassNotFoundException e) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    protected String getJDBCUrl(Map params) throws IOException {
        // jdbc url
    	String host=null;
    	Integer port = null;
    	try {
    		host = (String) HOST.lookUp(params);
    		port = (Integer) PORT.lookUp(params);
    	} catch (IOException ex) {
    		// do nothing
    	}
    	
        String db = (String) DATABASE.lookUp(params);
        
        if (host==null && port== null && db !=null)
        	return "jdbc:"+getDatabaseID()+":"+db;

        return super.getJDBCUrl(params);
    }
    
    protected void setupParameters(Map parameters) {
        super.setupParameters(parameters);
        parameters.put(DBTYPE.key, DBTYPE);
    }

    @Override
    protected JDBCDataStore createDataStoreInternal(JDBCDataStore dataStore, Map params)
    throws IOException {
    Connection con = null;
    try {    
    con = dataStore.getDataSource().getConnection();    
    DatabaseMetaData md = con.getMetaData();
    MajorVersion=md.getDatabaseMajorVersion();
    MinorVersion=md.getDatabaseMinorVersion();
    ProductName=md.getDatabaseProductName();
    ProductVersion=md.getDatabaseProductVersion();
    if (dataStore.getDatabaseSchema()==null) {
        PreparedStatement ps = con.prepareStatement(GetCurrentSchema);
        ResultSet  rs = ps.executeQuery();
        rs.next();
        dataStore.setDatabaseSchema(rs.getString(1));
        rs.close();        
        ps.close();
    }
    } catch (SQLException e) {
        throw new IOException(e.getMessage());
    }
    dataStore.closeSafe(con);
    return dataStore;
}

    
}
