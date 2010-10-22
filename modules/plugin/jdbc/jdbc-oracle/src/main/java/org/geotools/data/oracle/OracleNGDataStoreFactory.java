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
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/plugin/jdbc/jdbc-oracle/src/main/java/org/geotools/data/oracle/OracleNGDataStoreFactory.java $
 */
public class OracleNGDataStoreFactory extends JDBCDataStoreFactory {
    private static final String JDBC_PATH = "jdbc:oracle:thin:@";
    
    /** parameter for database type */
    public static final Param DBTYPE = new Param("dbtype", String.class, "Type", true, "oracle");

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
        return (String) DBTYPE.sample;
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
    
    @Override
    protected boolean checkDBType(Map params) {
        if (super.checkDBType(params)) {
            try {
                //check for old factory
                Class.forName("org.geotools.data.oracle.OracleDataStoreFactory");
                
                //it is around, let it handle this connection
                return false;
            } 
            catch(ClassNotFoundException e) {
                //old factory not around, pick up the connection
                return true;
            }
        }
        else {
            //check for the old id
            return checkDBType(params, "Oracle");
        }
    }
    
    protected JDBCDataStore createDataStoreInternal(JDBCDataStore dataStore, Map params)
        throws IOException {
        
        // make the schema uppercase if it's not already
        if(dataStore.getDatabaseSchema() != null)
            dataStore.setDatabaseSchema(dataStore.getDatabaseSchema().toUpperCase());
        
        // setup loose bbox
        OracleDialect dialect = (OracleDialect) dataStore.getSQLDialect();
        Boolean loose = (Boolean) LOOSEBBOX.lookUp(params);
        dialect.setLooseBBOXEnabled(loose == null || Boolean.TRUE.equals(loose));
        
        // setup proper fetch size
        dataStore.setFetchSize(200);
        
        return dataStore;
    }
    
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
        // NOTE: when adding parameters here remember to add them to OracleNGOCIDataStoreFactory and
        // OracleNGJNDIDataStoreFactory
        
        super.setupParameters(parameters);
        parameters.put(LOOSEBBOX.key, LOOSEBBOX);
        parameters.put(MAX_OPEN_PREPARED_STATEMENTS.key, MAX_OPEN_PREPARED_STATEMENTS);
        parameters.put(PORT.key, PORT);
        parameters.put(DBTYPE.key, DBTYPE);
    }
    
    @Override
    protected String getValidationQuery() {
        return "select sysdate from dual";
    }
}
