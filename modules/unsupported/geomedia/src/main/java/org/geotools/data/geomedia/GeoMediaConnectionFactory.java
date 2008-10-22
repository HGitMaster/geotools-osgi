/*
 *    GeoLBS - OpenSource Location Based Servces toolkit
 *    (C) 2004, Julian J. Ray, All Rights Reserved
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
 *
 */

package org.geotools.data.geomedia;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.ConnectionPoolDataSource;

import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.ConnectionPoolManager;


/**
 * <p>
 * Title: GeoTools2 Development
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * Provides javax.sql.DataSource wrapper around a JDBC object. User passes a JDBC driver string as part of the
 * properties. Driver is loaded using Class.forname() from the class path. User also passes a connection string URL as
 * part of the properties.
 *
 * @author Julian J. Ray
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/geomedia/src/main/java/org/geotools/data/geomedia/GeoMediaConnectionFactory.java $
 * @version 1.0
 */
public class GeoMediaConnectionFactory {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.data.geomedia");

    /** Map that contains Connection Pool Data Sources */
    private static Map mDataSources = new HashMap();

    // The class name for the JDBC driver

    /** DOCUMENT ME! */
    private String mDriverClassName;

    // The url to the DB

    /** DOCUMENT ME! */
    private String mPoolKey;

    // Connection Parameters

    /** DOCUMENT ME! */
    private GeoMediaConnectionParam[] mParams;

    // The username to login with

    /** DOCUMENT ME! */
    private String mUsername = "";

    /// The password to login with

    /** DOCUMENT ME! */
    private String mPasswd = "";

    /**
     * Creates a new Connection object.
     *
     * @param classname The host name of IP address to connect to.
     * @param poolKey The port number on the host. Usually 1433
     * @param params The database name on the host
     */
    public GeoMediaConnectionFactory(String classname, String poolKey, GeoMediaConnectionParam[] params) {
        mDriverClassName = classname;
        mPoolKey = poolKey;
        mParams = params;
    }

    /**
     * Creates the real Server Connection. Logs in to the Database and creates the Connection object. If the connection
     * pool is not established, a new one is created.
     *
     * @param user The user name.
     * @param pass The password
     *
     * @return The ConnectionPool object.
     *
     * @throws SQLException If an error occurs connecting to the DB.
     */
    public ConnectionPool getConnectionPool(String user, String pass)
        throws SQLException {
        String                   poolKey = mPoolKey + ":" + user + ":" + pass;
        ConnectionPoolDataSource poolDataSource = (ConnectionPoolDataSource) mDataSources.get(poolKey);

        if (poolDataSource == null) {
            try {
                // Attempt to load the driver
                Class driver = Class.forName(mDriverClassName);

                // Get the constructor
                Constructor constr = driver.getConstructor(new Class[] {  });
                poolDataSource = (ConnectionPoolDataSource) constr.newInstance(new Object[] {  });

                // Process the connection parameters
                for (int i = 0; i < mParams.length; i++) {
                    Method method = driver.getMethod(mParams[i].getMethodName(),
                            new Class[] { mParams[i].getClassType() });
                    Object ret = method.invoke(poolDataSource, new Object[] { mParams[i].getParam() });
                }

                // Cache the data source
                mDataSources.put(poolKey, poolDataSource);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver " + mDriverClassName + " not found!");
            } catch (NoSuchMethodException e) {
                throw new SQLException("Driver does not support method" + e.getMessage());
            } catch (InstantiationException e) {
                throw new SQLException("Cannot create instance of " + mDriverClassName);
            } catch (IllegalAccessException e) {
                throw new SQLException("IllegalAccessException while instantiating connection pool for driver "
                    + mDriverClassName);
            } catch (InvocationTargetException e) {
                throw new SQLException("IllegalAccessException while instantiating connection pool for driver "
                    + mDriverClassName);
            }
        }

        ConnectionPoolManager manager = ConnectionPoolManager.getInstance();
        ConnectionPool        connectionPool = manager.getConnectionPool(poolDataSource);

        return connectionPool;
    }

    /**
     * Returns a connection from the connection pool.
     *
     * @return The connection to the data base.
     *
     * @throws SQLException If an error occurs.
     */
    public ConnectionPool getConnectionPool() throws SQLException {
        return getConnectionPool(mUsername, mPasswd);
    }

    /**
     * Sets the login credentials.
     *
     * @param user The username
     * @param pass The password
     */
    public void setLogin(String user, String pass) {
        this.mUsername = user;
        this.mPasswd = pass;
    }
}
