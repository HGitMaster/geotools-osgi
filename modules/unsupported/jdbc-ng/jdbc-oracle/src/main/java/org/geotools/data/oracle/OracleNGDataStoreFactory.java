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

import java.io.IOException;
import java.util.Map;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.SQLDialect;

/**
 * Oracle data store factory.
 * 
 * @author Justin Deoliveira, OpenGEO
 * @author Andrea Aime, OpenGEO
 */
public class OracleNGDataStoreFactory extends JDBCDataStoreFactory {
    private static final String JDBC_PATH = "jdbc:oracle:thin:@";
    
    /** parameter for database port */
    public static final Param PORT = new Param("port", Integer.class, "Port", true, 1521);
    
    /** parameter for namespace of the datastore */
    public static final Param LOOSEBBOX = new Param("Loose bbox", Boolean.class, "Perform only primary filter on bbox", false, Boolean.TRUE);
    
    @Override
    protected SQLDialect createSQLDialect(JDBCDataStore dataStore) {
        return new OracleDialect(dataStore);
    }

    @Override
    protected String getDatabaseID() {
        return "Oracle";
    }
    
    @Override
    public String getDisplayName() {
        return "Oracle NG";
    }

    public String getDescription() {
        return "Oracle Database";
    }
    
    @Override
    protected String getDriverClassName() {
        return "oracle.jdbc.driver.OracleDriver";
    }
    
    protected JDBCDataStore createDataStoreInternal(JDBCDataStore dataStore, Map params)
        throws IOException {
        
        // setup loose bbox
        OracleDialect dialect = (OracleDialect) dataStore.getSQLDialect();
        Boolean loose = (Boolean) LOOSEBBOX.lookUp(params);
        dialect.setLooseBBOXEnabled(loose == null || Boolean.TRUE.equals(loose));
        
        // setup proper fetch size
        dataStore.setFetchSize(200);
        
        return dataStore;
    }
    
//    @Override
//    protected DataSource createDataSource(Map params) throws IOException {
//        BasicDataSource dataSource = new BasicDataSource();
//
//        //driver
//        dataSource.setDriverClassName(getDriverClassName());
//
//        //jdbc url
//        
//        dataSource.setUrl(dbUrl);
//
//        //username
//        String user = (String) USER.lookUp(params);
//        dataSource.setUsername(user);
//
//        //password
//        String passwd = (String) PASSWD.lookUp(params);
//
//        if (passwd != null) {
//            dataSource.setPassword(passwd);
//        }
//
//        // setup pooling
//        dataSource.setMinIdle(4);
//        dataSource.setMaxActive(20);
//        dataSource.setAccessToUnderlyingConnectionAllowed(true);
//        dataSource.setPoolPreparedStatements(true);
//        dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
//        
//        // check connections?
//        Boolean validate = (Boolean) VALIDATECONN.lookUp(params);
//        if(Boolean.TRUE.equals(validate))
//            dataSource.setValidationQuery("select sysdate from dual");
//        
//        return dataSource;
//    }
    
    @Override
    protected String getJDBCUrl(Map params) throws IOException {
        String host = (String) HOST.lookUp(params);
        String db = (String) DATABASE.lookUp(params);
        int port = (Integer) PORT.lookUp(params);
        if( db.startsWith("(") )
            return JDBC_PATH + db;
        else if( db.startsWith("/") )
            return JDBC_PATH + "//" + host + ":" + port + db;
        else
            return JDBC_PATH + host + ":" + port + ":" + db;
    }
    
    @Override
    protected void setupParameters(Map parameters) {
        super.setupParameters(parameters);
        parameters.put(LOOSEBBOX.key, LOOSEBBOX);
        parameters.put(PORT.key, PORT);
    }
    
    @Override
    protected String getValidationQuery() {
        return "select sysdate from dual";
    }
}
