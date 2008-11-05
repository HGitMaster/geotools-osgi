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
package org.geotools.jdbc;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.geotools.data.AbstractDataStoreFactory;
import org.geotools.data.DataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.AbstractFeatureFactoryImpl;
import org.geotools.feature.LenientFeatureFactoryImpl;
import org.geotools.feature.type.FeatureTypeFactoryImpl;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.FilterFactoryImpl;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.Subtract;

import com.vividsolutions.jts.geom.GeometryFactory;


/**
 * Abstract implementation of DataStoreFactory for jdbc datastores.
 * <p>
 *
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public abstract class JDBCDataStoreFactory extends AbstractDataStoreFactory {
    /** parameter for database type */
    public static final Param DBTYPE = new Param("Type", String.class, "Type", true);

    /** parameter for database host */
    public static final Param HOST = new Param("Host", String.class, "Host", true, "localhost");

    /** parameter for database port */
    public static final Param PORT = new Param("Port", Integer.class, "Port", true);

    /** parameter for database instance */
    public static final Param DATABASE = new Param("Database", String.class, "Database");

    /** parameter for database schema */
    public static final Param SCHEMA = new Param("Schema", String.class, "Schema", false);

    /** parameter for database user */
    public static final Param USER = new Param("Username", String.class, "Username");

    /** parameter for database password */
    public static final Param PASSWD = new Param("Password", String.class, "Password", false);

    /** parameter for namespace of the datastore */
    public static final Param NAMESPACE = new Param("namespace", String.class);

    /** parameter for data source */
    public static final Param DATASOURCE = new Param( "Data Source", DataSource.class, "Data Source", false );
    
    public boolean canProcess(Map params) {
        if (!super.canProcess(params)) {
            return false; // was not in agreement with getParametersInfo
        }

        String type;

        try {
            type = (String) DBTYPE.lookUp(params);

            if (getDatabaseID().equals(type)) {
                return true;
            }

            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public final JDBCDataStore createDataStore(Map params)
        throws IOException {
        JDBCDataStore dataStore = new JDBCDataStore();

        //datasource 
        //check if the DATASOURCE parameter was supplied, it takes precendence
        DataSource ds = (DataSource) DATASOURCE.lookUp( params );
        if ( ds != null ) {
            dataStore.setDataSource(ds);
        }
        else {
            dataStore.setDataSource(createDataSource(params));
        }

        //dialect
        dataStore.setSQLDialect(createSQLDialect(dataStore));

        //namespace
        String namespace = (String) NAMESPACE.lookUp(params);

        if (namespace != null) {
            dataStore.setNamespaceURI(namespace);
        }

        //database schema
        String schema = (String) SCHEMA.lookUp(params);

        if (schema != null) {
            dataStore.setDatabaseSchema(schema);
        }

        //factories
        dataStore.setFilterFactory(CommonFactoryFinder.getFilterFactory(null));
        dataStore.setGeometryFactory(new GeometryFactory());
        dataStore.setFeatureTypeFactory(new FeatureTypeFactoryImpl());
        dataStore.setFeatureFactory(CommonFactoryFinder.getFeatureFactory(null));

        //call subclass hook and return
        return createDataStoreInternal(dataStore, params);
    }

    /**
     * Subclass hook to do additional initialization of a newly created datastore.
     * <p>
     * Typically subclasses will want to override this method in the case where
     * they provide additional datastore parameters, those should be processed
     * here.
     * </p>
     * <p>
     * This method is provided with an instance of the datastore. In some cases
     * subclasses may wish to create a new instance of the datastore, for instance
     * in order to wrap the original instance. This is supported but the new
     * datastore must be returned from this method. If not is such the case this
     * method should still return the original passed in.
     *
     * </p>
     * @param dataStore The newly created datastore.
     * @param params THe datastore parameters.
     *
     */
    protected JDBCDataStore createDataStoreInternal(JDBCDataStore dataStore, Map params)
        throws IOException {
        return dataStore;
    }

    public DataStore createNewDataStore(Map params) throws IOException {
        throw new UnsupportedOperationException();
    }

    public final Param[] getParametersInfo() {
        LinkedHashMap map = new LinkedHashMap();
        setupParameters(map);

        return (Param[]) map.values().toArray(new Param[map.size()]);
    }

    /**
     * Sets up the database connection parameters.
     * <p>
     * Subclasses may extend, but should not override. This implementation
     * registers the following parameters.
     * <ul>
     *   <li>{@link #HOST}
     *   <li>{@link #PORT}
     *   <li>{@link #DATABASE}
     *   <li>{@link #SCHEMA}
     *   <li>{@link #USER}
     *   <li>{@link #PASSWD}
     * </ul>
     * Subclass implementation may remove any parameters from the map, or may
     * overrwrite any parameters in the map.
     * </p>
     *
     * @param parameters Map of {@link Param} objects.
     */
    protected void setupParameters(Map parameters) {
        parameters.put(DBTYPE.key,
            new Param(DBTYPE.key, DBTYPE.type, DBTYPE.description, DBTYPE.required, getDatabaseID()));

        parameters.put(HOST.key, HOST);
        parameters.put(PORT.key, PORT);
        parameters.put(DATABASE.key, DATABASE);
        parameters.put(SCHEMA.key, SCHEMA);
        parameters.put(USER.key, USER);
        parameters.put(PASSWD.key, PASSWD);
    }

    /**
     * Determines if the datastore is available.
     * <p>
     * Subclasses may with to override or extend this method. This implementation
     * checks whether the jdbc driver class (provided by {@link #getDriverClassName()}
     * can be loaded.
     * </p>
     */
    public boolean isAvailable() {
        try {
            Class.forName(getDriverClassName());

            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Returns the implementation hints for the datastore.
     * <p>
     * Subclasses may override, this implementation returns <code>null</code>.
     * </p>
     */
    public Map getImplementationHints() {
        return null;
    }

    /**
     * Returns a string to identify the type of the database.
     * <p>
     * Example: 'postgis'.
     * </p>
     */
    protected abstract String getDatabaseID();

    /**
     * Returns the fully qualified class name of the jdbc driver.
     * <p>
     * For example: org.postgresql.Driver
     * </p>
     */
    protected abstract String getDriverClassName();

    /**
     * Creates the dialect that the datastore uses for communication with the
     * underlying database.
     * 
     * @param dataStore The datastore.
     */
    protected abstract SQLDialect createSQLDialect(JDBCDataStore dataStore);

    /**
     * Creates the datasource for the data store.
     * <p>
     * This method creates a {@link BasicDataSource} instance and populates it
     * as follows:
     * <ul>
     *  <li>poolPreparedStatements -> false
     *  <li>driverClassName -> {@link #getDriverClassName()}
     *  <li>url -> 'jdbc:&lt;{@link #getDatabaseID()}>://&lt;{@link #HOST}>/&lt;{@link #DATABASE}>'
     *  <li>username -> &lt;{@link #USER}>
     *  <li>password -> &lt;{@link #PASSWD}>
     * </ul>
     * If different behaviour is needed, this method should be extended or
     * overridden.
     * </p>
     */
    protected DataSource createDataSource(Map params) throws IOException {
        //create a datasource
        BasicDataSource dataSource = new BasicDataSource();

        //some defualt data source behaviour
        dataSource.setPoolPreparedStatements(false);

        //driver
        dataSource.setDriverClassName(getDriverClassName());

        //jdbc url
        String host = (String) HOST.lookUp(params);
        String db = (String) DATABASE.lookUp(params);
        dataSource.setUrl("jdbc:" + getDatabaseID() + "://" + host + "/" + db);

        //username
        String user = (String) USER.lookUp(params);
        dataSource.setUsername(user);

        //password
        String passwd = (String) PASSWD.lookUp(params);

        if (passwd != null) {
            dataSource.setPassword(passwd);
        }

        return dataSource;
    }
}
