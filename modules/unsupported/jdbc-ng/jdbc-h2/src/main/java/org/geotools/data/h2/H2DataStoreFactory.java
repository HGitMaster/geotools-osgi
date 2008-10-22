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
package org.geotools.data.h2;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.SQLDialect;


/**
 * DataStoreFacotry for H2 database.
 *
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class H2DataStoreFactory extends JDBCDataStoreFactory {
    /** parameter for how to handle associations */
    public static final Param ASSOCIATIONS = new Param("Associations", Boolean.class,
            "Associations", false, Boolean.FALSE);

    /**
     * base location to store h2 database files
     */
    File baseDirectory = null;

    /**
     * Sets the base location to store h2 database files.
     *
     * @param baseDirectory A directory.
     */
    public void setBaseDirectory(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * The base location to store h2 database files.
     */
    public File getBaseDirectory() {
        return baseDirectory;
    }
    
    protected void setupParameters(Map parameters) {
        super.setupParameters(parameters);

        //remove unneccessary parameters
        parameters.remove(HOST.key);
        parameters.remove(PORT.key);
        parameters.remove(SCHEMA.key);
        parameters.remove(USER.key);
        parameters.remove(PASSWD.key);

        //add additional parameters
        parameters.put(ASSOCIATIONS.key, ASSOCIATIONS);
    }

    public String getDisplayName() {
        return "H2";
    }

    public String getDescription() {
        return "H2 Embedded Database";
    }

    protected String getDatabaseID() {
        return "h2";
    }

    protected String getDriverClassName() {
        return "org.h2.Driver";
    }

    protected SQLDialect createSQLDialect(JDBCDataStore dataStore) {
        return new H2Dialect(dataStore);
    }

    protected DataSource createDataSource(Map params) throws IOException {
        String database = (String) DATABASE.lookUp(params);
        BasicDataSource dataSource = new BasicDataSource();

        if (baseDirectory == null) {
            //use current working directory
            dataSource.setUrl("jdbc:h2:" + database);
        } else {
            //use directory specified
            String location = new File(baseDirectory, database).getAbsolutePath();
            dataSource.setUrl("jdbc:h2:file:" + location);
        }

        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setPoolPreparedStatements(false);

        return dataSource;
    }

    protected JDBCDataStore createDataStoreInternal(JDBCDataStore dataStore, Map params)
        throws IOException {
        //check the foreign keys parameter
        Boolean foreignKeys = (Boolean) ASSOCIATIONS.lookUp(params);

        if (foreignKeys != null) {
            dataStore.setAssociations(foreignKeys.booleanValue());
        }

        return dataStore;
    }
}
