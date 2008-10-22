/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.hsql;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;


/**
 * Creates a HsqlDataStoreFactory based on the correct params.
 * 
 * <p>
 * This factory should be registered in the META-INF/ folder, under services/
 * in the DataStoreFactorySpi file.
 * </p>
 *
 * @author Amr Alam, Refractions Research
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/hsql/src/main/java/org/geotools/data/hsql/HsqlDataStoreFactory.java $
 */
public class HsqlDataStoreFactory  implements DataStoreFactorySpi{
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(HsqlDataStoreFactory.class
            .getName());

    /** Creates Hsql JDBC driver class. */
    private static final String DRIVER_CLASS = "org.hsqldb.jdbcDriver";

    /** Param, package visibiity for JUnit tests */
    public static final Param DBTYPE = new Param("dbtype", String.class,
            "must be 'hsql'", true, "hsql");

    /** Param, package visibiity for JUnit tests */
    public static final Param HOST = new Param("host", String.class,
            "hsql host machine", true, "localhost");

    /** Param, package visibiity for JUnit tests */
    public static final Param PORT = new Param("port", String.class,
            "hsql connection port", true, "9001");

    /** Param, package visibiity for JUnit tests */
    public static final Param DATABASE = new Param("database", String.class,
            "hsql database");

    /** Param, package visibiity for JUnit tests */
    public static final Param DBFILENAME = new Param("hsqlfilename", String.class,
            "hsql database filename");

    /** Param, package visibiity for JUnit tests */
    public static final Param USER = new Param("user", String.class,
            "user name to login as", false);

    /** Param, package visibiity for JUnit tests */
    public static final Param PASSWD = new Param("passwd", String.class,
            "password used to login", false);

    /** Param, package visibiity for JUnit tests */
    public static final Param NAMESPACE = new Param("namespace", String.class,
            "namespace prefix used", false);

    /** Array with all of the params */
    public static final Param[] arrayParameters = {
            DBTYPE, DBFILENAME, USER, PASSWD, NAMESPACE
        };

    private static Map datastores=Collections.synchronizedMap(new HashMap());

    /**
     * Creates a new instance of HsqlDataStoreFactory
     */
    public HsqlDataStoreFactory() {
    }

    /**
     * Checks to see if all the hsql params are there.
     * 
     * <p>
     * Should have:
     * </p>
     * 
     * <ul>
     * <li>
     * dbtype: equal to hsql
     * </li>
     * <li>
     * user
     * </li>
     * <li>
     * passwd
     * </li>
     * <li>
     * dbfilename
     * </li>
     * </ul>
     * 
     *
     * @param params Set of parameters needed for a hsql data store.
     *
     * @return <code>true</code> if dbtype equals hsql, and contains keys for
     *         host, user, passwd, and database.
     */
    public boolean canProcess(Map params) {
        Object value;

        if (params != null) {
            for (int i = 0; i < arrayParameters.length; i++) {
                if (!(((value = params.get(arrayParameters[i].key)) != null)
                        && (arrayParameters[i].type.isInstance(value)))) {
                    if (arrayParameters[i].required) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.warning("Failed on : "
                                + arrayParameters[i].key);
                            LOGGER.fine(params.toString());
                        }

                        return (false);
                    }
                }
            }
        } else {
            return (false);
        }

        if ((((String) params.get("dbtype")).equalsIgnoreCase("hsql"))) {
            return (true);
        } else {
            return (false);
        }
    }

    /**
     * Construct a hsql data store using the params.
     *
     * @param params The full set of information needed to construct a live
     *        data source.  Should have  dbtype equal to 'hsql', as well as
     *        dbfilename, user, passwd, and namespace (optional).
     *
     * @return The created DataSource, this may be null if the required
     *         resource was not found or if insufficent parameters were given.
     *         Note that canProcess() should have returned false if the
     *         problem is to do with insuficent parameters.
     *
     * @throws IOException See DataSourceException
     */
    public DataStore createDataStore(Map params) throws IOException {
        if (datastores.containsKey(params))
            return (DataStore) datastores.get(params);
        
        return createNewDataStore(params);
    }

    /**
     * DOCUMENT ME!
     *
     * @param params
     *
     *
     * @throws IOException See UnsupportedOperationException
     */
    public DataStore createNewDataStore(Map params) throws IOException {
//      lookup will throw nice exceptions back to the client code
        //        String host = (String) HOST.lookUp(params);
        String filename = (String) DBFILENAME.lookUp(params);
        String user = (String) USER.lookUp(params);
        String passwd = (String) PASSWD.lookUp(params);

        //        String port = (String) PORT.lookUp(params);
        //        String database = (String) DATABASE.lookUp(params);
        String namespace = (String) NAMESPACE.lookUp(params);

        if (!canProcess(params)) {
            // Do this as a last sanity check.
            LOGGER.warning("Can not process : " + params);
            throw new IOException("The parameteres map isn't correct!!");
        }

        HsqlConnectionFactory connFact = new HsqlConnectionFactory(filename,
                user, passwd);

        HsqlDataStore ds;
        if (namespace != null) {
            ds=new HsqlDataStore(connFact, namespace);
        } else {
            ds=new HsqlDataStore(connFact);
        }
        datastores.put(params,ds);
        return ds;
    }

    /**
     * DOCUMENT ME!
     *
     * @return "HSQL"
     */
    public String getDisplayName() {
        return "HSQL";
    }

    /**
     * Describe the nature of the datasource constructed by this factory.
     *
     * @return A human readable description that is suitable for inclusion in a
     *         list of available datasources.  Currently uses the string "HSQL
     *         Database"
     */
    public String getDescription() {
        return "HSQL Database";
    }

    //    /**
    //     *
    //     */
    //    public DataSourceMetadataEnity createMetadata( Map params ) throws IOException {
    //        String host = (String) HOST.lookUp(params);
    //        String user = (String) USER.lookUp(params);
    //        String port = (String) PORT.lookUp(params);
    //        String database = (String) DATABASE.lookUp(params);
    //        return new DataSourceMetadataEnity( host+"port", database, "HSQL connection to "+host+" as "+user );
    //    }

    /**
     * Test to see if this datastore is available, if it has all the
     * appropriate libraries to construct a datastore.  This datastore just
     * returns true for now.  This method is used for gui apps, so as to not
     * advertise data store capabilities they don't actually have.
     *
     * @return <tt>true</tt> if and only if this factory is available to create
     *         DataStores.
     */
    public boolean isAvailable() {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException cnfe) {
            LOGGER.warning("HSQL data sources are not available: "
                + cnfe.getMessage());

            return false;
        }

        return true;
    }

    /**
     * Describe parameters.
     *
     *
     * @see org.geotools.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        return new Param[] { DBTYPE, DBFILENAME, USER, PASSWD, NAMESPACE };
    }

    /**
     * @return Returns the implementation hints. The default implementation returns en
     * empty map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }

    
}
