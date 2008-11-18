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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.geotools.data.AbstractDataStoreFactory;
import org.geotools.data.DataStore;
import org.geotools.data.Parameter;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.type.FeatureTypeFactoryImpl;
import org.geotools.util.SimpleInternationalString;

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
    public static final Param DBTYPE = new Param("dbtype", String.class, "Type", true);

    /** parameter for database host */
    public static final Param HOST = new Param("host", String.class, "Host", true, "localhost");

    /** parameter for database port */
    public static final Param PORT = new Param("port", Integer.class, "Port", true);

    /** parameter for database instance */
    public static final Param DATABASE = new Param("database", String.class, "Database", false );

    /** parameter for database schema */
    public static final Param SCHEMA = new Param("schema", String.class, "Schema", false);

    /** parameter for database user */
    public static final Param USER = new Param("user", String.class,
    "user name to login as");

    /** parameter for database password */
    public static final Param PASSWD = new Param("passwd", String.class,
            new SimpleInternationalString("password used to login"), false, null, Collections
                    .singletonMap(Parameter.IS_PASSWORD, Boolean.TRUE));

    /** parameter for namespace of the datastore */
    public static final Param NAMESPACE = new Param("namespace", String.class, "Namespace prefix", false);

    /** parameter for data source */
    public static final Param DATASOURCE = new Param( "Data Source", DataSource.class, "Data Source", false );
    
    /** Maximum number of connections in the connection pool */
    public static final Param MAXCONN = new Param("max connections", Integer.class,
            "maximum number of open connections", false, new Integer(10));
    
    /** Minimum number of connections in the connection pool */
    public static final Param MINCONN = new Param("min connections", Integer.class,
            "minimum number of pooled connection", false, new Integer(1));
    
    /** If connections should be validated before using them */
    public static final Param VALIDATECONN = new Param("validate connections", Boolean .class,
            "check connection is alive before using it", false, Boolean.FALSE);
    
    @Override
    public String getDisplayName() {
        return getDescription();
    }
    
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
        
        // dialect
        final SQLDialect dialect = createSQLDialect(dataStore);
        dataStore.setSQLDialect(dialect);

        // datasource 
        // check if the DATASOURCE parameter was supplied, it takes precendence
        DataSource ds = (DataSource) DATASOURCE.lookUp( params );
        if ( ds != null ) {
            dataStore.setDataSource(ds);
        }
        else {
            dataStore.setDataSource(createDataSource(params, dialect));
        }

        // namespace
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
        parameters.put(NAMESPACE.key, NAMESPACE);
        parameters.put(MAXCONN.key, MAXCONN);
        parameters.put(MINCONN.key, MINCONN);
        if(getValidationQuery() != null)
            parameters.put(VALIDATECONN.key, VALIDATECONN);
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
    protected DataSource createDataSource(Map params, SQLDialect dialect) throws IOException {
        //create a datasource
        BasicDataSource dataSource = new BasicDataSource();

        // some default data source behaviour
        dataSource.setPoolPreparedStatements(dialect instanceof PreparedStatementSQLDialect);

        // driver
        dataSource.setDriverClassName(getDriverClassName());

        // url
        dataSource.setUrl(getJDBCUrl(params));

        // username
        String user = (String) USER.lookUp(params);
        dataSource.setUsername(user);

        // password
        String passwd = (String) PASSWD.lookUp(params);
        if (passwd != null) {
            dataSource.setPassword(passwd);
        }
        
        // connection pooling options
        dataSource.setMinIdle((Integer) MINCONN.lookUp(params));
        dataSource.setMaxActive((Integer) MAXCONN.lookUp(params));
        boolean validate = (Boolean) VALIDATECONN.lookUp(params);
        if(validate && getValidationQuery() != null) {
            dataSource.setValidationQuery(getValidationQuery());
        }

        return dataSource;
    }

    /**
     * Override this to return a good validation query (a very quick one, such as one that
     * asks the database what time is it) or return null if the factory does not support
     * validation. 
     * @return
     */
    protected abstract String getValidationQuery();

    /**
     * Builds up the JDBC url in a jdbc:<database>://<host>:<port>/<dbname>
     * Override if you need a different setup
     * @param params
     * @return
     * @throws IOException
     */
    protected String getJDBCUrl(Map params) throws IOException {
        // jdbc url
        String host = (String) HOST.lookUp(params);
        Integer port = (Integer) PORT.lookUp(params);
        String db = (String) DATABASE.lookUp(params);
        
        String url = "jdbc:" + getDatabaseID() + "://" + host;
        if ( port != null ) {
            url += ":" + port;
        }
        
        if ( db != null ) {
            url += "/" + db; 
        }
        return url;
    }
}
