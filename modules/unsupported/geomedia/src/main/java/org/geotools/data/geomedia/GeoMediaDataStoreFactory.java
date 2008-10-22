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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Collections;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.jdbc.ConnectionPool;


/**
 * Creates a GeoMediaDataStoreFactoru based on the correct params.
 * 
 * <p>
 * This factory should be registered in the META-INF/ folder, under services/ in the DataStoreFactorySpi file.
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/geomedia/src/main/java/org/geotools/data/geomedia/GeoMediaDataStoreFactory.java $
 */
public class GeoMediaDataStoreFactory implements DataStoreFactorySpi {
    /** A logger for logging */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.data.geomedia");

    /** DOCUMENT ME! */
    private GeoMediaConnectionParam[] mConnectionParams = null;

    /**
     * Creates a new instance of GeoMediaDataStoreFactory
     */
    public GeoMediaDataStoreFactory() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param params DOCUMENT ME!
     */
    public void setConnectionParams(GeoMediaConnectionParam[] params) {
        mConnectionParams = params;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public GeoMediaConnectionParam[] getConnectionParams() {
        return mConnectionParams;
    }

    /**
     * Determines whether DataStore created by this factory can process the parameters.
     * 
     * <p>
     * Required Parameters are:
     * </p>
     * 
     * <ul>
     * <li>
     * <code>dbtype</code> - must equal "oracle"
     * </li>
     * <li>
     * <code>host</code>
     * </li>
     * <li>
     * <code>port</code>
     * </li>
     * <li>
     * <code>user</code>
     * </li>
     * <li>
     * <code>passwd</code>
     * </li>
     * <li>
     * <code>instance</code>
     * </li>
     * </ul>
     * 
     * <p>
     * There are no defaults since each parameter must be explicitly defined by the user, or another
     * DataSourceFactorySpi should be used. This behaviour is defined in the DataStoreFactorySpi contract.
     * </p>
     *
     * @param params The parameter to check.
     *
     * @return True if all the required parameters are supplied.
     */
    public boolean canProcess(Map params) {
        if (mConnectionParams == null) {
            return false;
        }
        return params.containsKey("dbtype") && params.get("dbtype").equals("geomedia") && params.containsKey("dbkey")
        && params.containsKey("dbdriver") && params.containsKey("user") && params.containsKey("passwd");

        //&& params.containsKey("parameters");
    }

    /**
     * Construct a postgis data store using the params.
     *
     * @param params The full set of information needed to construct a live data source.  Should have  dbtype equal to
     *        geomedia, as well as host, user, passwd, database, dbkey.
     *
     * @return The created DataSource, this may be null if the required resource was not found or if insufficent
     *         parameters were given. Note that canProcess() should have returned false if the problem is to do with
     *         insufficent parameters.
     *
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException Thrown if there were any problems creating or connecting the datasource.
     */
    public DataStore createDataStore(Map params) throws IOException {
        /* There are no defaults here. Calling canProcess verifies that
         * all these variables exist.
         */
        String databaseDriver = (String) DBDRIVER.lookUp( params );
        String databasePoolKey = (String) DBKEY.lookUp( params );
        
        String user = (String) USER.lookUp( params );
        String passwd = (String) PASSWD.lookUp( params );

        try {
            GeoMediaConnectionFactory factory = new GeoMediaConnectionFactory(databaseDriver, databasePoolKey,
                    mConnectionParams);

            factory.setLogin(user, passwd);

            ConnectionPool    pool = factory.getConnectionPool();
            GeoMediaDataStore dataStore = new GeoMediaDataStore(pool);

            return dataStore;
        } catch (SQLException ex) {
            throw new DataSourceException(ex.getMessage());
        }
    }

    /**
     * Oracle cannot create a new database.
     *
     * @param params
     *
     *
     * @throws IOException DOCUMENT ME!
     * @throws UnsupportedOperationException Cannot create new database
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        throw new UnsupportedOperationException("GeoTools cannot create a new GeoMedia Database");
    }

    public String getDisplayName() {
        return "GeoMedia";
    }
    /**
     * Describe the nature of the datastore constructed by this factory.
     *
     * @return A human readable description that is suitable for inclusion in a list of available datasources.
     */
    public String getDescription() {
        return "GeoMedia Spatial Database";
    }
//    // TODO: have a clue
//	public DataSourceMetadataEnity createMetadata( Map params ) throws IOException {
//	    String databaseDriver = (String) DBDRIVER.lookUp( params );
//        String databasePoolKey = (String) DBKEY.lookUp( params );
//        
//        String user = (String) USER.lookUp( params );        
//        
//	    return new DataSourceMetadataEnity( databasePoolKey, "GeoMedia "+user, "GeoMedia connection to "+databasePoolKey+" as "+user );
//	}
	/**
	 * Test to see if this datastore is available, if it has all the
	 * appropriate libraries to construct a datastore.  This datastore just
	 * returns true for now.  This method is used for gui apps, so as to
	 * not advertise data store capabilities they don't actually have.
	 *
	 * @return <tt>true</tt> if and only if this factory is available to create
	 *         DataStores.
	 *
	 * @task REVISIT: I'm just adding this method to compile, maintainer should
	 *       revisit to check for any libraries that may be necessary for
	 *       datastore creations. ch.
	 */
	public boolean isAvailable() {
		return true;
	}

	static final Param DBTYPE = new Param("dbtype", String.class, "This must be 'geomedia'.", true);
	static final Param DBDRIVER = new Param("dbdriver", String.class, "Class name of a Type4 Javax DataSource database driver.", true);
	static final Param USER = new Param("user", String.class, "The user name to log in with.", true);
	static final Param PASSWD = new Param("passwd", String.class, "The password.", true);
	static final Param DBKEY = new Param("databasePoolKey", String.class, "the port number on the host, usually 1433", true, "1433" );
	static final Param[] PARAM_INFO = new Param[] {
        DBTYPE,
        DBDRIVER,
        USER,
        PASSWD
    };
    /**
     * Describe parameters.
     *
     *
     * @see org.geolbs.data.DataStoreFactorySpi#getParametersInfo()
     */
    public Param[] getParametersInfo() {
        return PARAM_INFO;
    }

    /**
     * Returns the implementation hints. The default implementation returns en empty map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}
