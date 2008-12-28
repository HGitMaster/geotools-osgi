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
package org.geotools.data.postgis;

import java.io.IOException;
import java.util.Map;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.SQLDialect;

public class PostgisNGDataStoreFactory extends JDBCDataStoreFactory {
    private static final String JDBC_PATH = "jdbc:oracle:thin:@";
    
    /** parameter for namespace of the datastore */
    public static final Param LOOSEBBOX = new Param("Loose bbox", Boolean.class, "Perform only primary filter on bbox", false, Boolean.TRUE);
    
    /** parameter for database port */
    public static final Param PORT = new Param("port", Integer.class, "Port", true, 5432);
    
    @Override
    protected SQLDialect createSQLDialect(JDBCDataStore dataStore) {
        return new PostGISDialect(dataStore);
    }

    @Override
    protected String getDatabaseID() {
        return "PostGIS";
    }
    
    @Override
    public String getDisplayName() {
        return "PostGIS NG";
    }

    public String getDescription() {
        return "PostGIS Database";
    }
    
    @Override
    protected String getDriverClassName() {
        return "org.postgresql.Driver";
    }
    
    protected JDBCDataStore createDataStoreInternal(JDBCDataStore dataStore, Map params)
        throws IOException {
        
        // setup loose bbox
        PostGISDialect dialect = (PostGISDialect) dataStore.getSQLDialect();
        Boolean loose = (Boolean) LOOSEBBOX.lookUp(params);
        dialect.setLooseBBOXEnabled(loose == null || Boolean.TRUE.equals(loose));
        
        // setup proper fetch size
        dataStore.setFetchSize(200);
        
        return dataStore;
    }
    
    @Override
    protected void setupParameters(Map parameters) {
        super.setupParameters(parameters);
        parameters.put(LOOSEBBOX.key, LOOSEBBOX);
        parameters.put(PORT.key, PORT);
    }
    
    @Override
    protected String getValidationQuery() {
        return "select now()";
    }
    
    @Override
    protected String getJDBCUrl(Map params) throws IOException {
        String host = (String) HOST.lookUp(params);
        String db = (String) DATABASE.lookUp(params);
        int port = (Integer) PORT.lookUp(params);
        return "jdbc:postgresql" + "://" + host + ":" + port + "/" + db;
    }

}
