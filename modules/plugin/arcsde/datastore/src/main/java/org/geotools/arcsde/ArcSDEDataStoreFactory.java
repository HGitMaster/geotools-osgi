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
 *
 */
package org.geotools.arcsde;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.arcsde.data.ArcSDEDataStore;
import org.geotools.arcsde.data.ViewRegisteringFactoryHelper;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.arcsde.pool.ArcSDEConnectionReference;
import org.geotools.arcsde.pool.ISession;
import org.geotools.arcsde.pool.SessionPool;
import org.geotools.arcsde.pool.SessionPoolFactory;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.Parameter;
import org.geotools.data.Transaction;
import org.geotools.util.SimpleInternationalString;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeRelease;
import com.esri.sde.sdk.pe.PeCoordinateSystem;
import com.esri.sde.sdk.pe.PeFactory;

/**
 * Factory to create DataStores over a live ArcSDE instance.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/ArcSDEDataStoreFactory.java $
 * @version $Id: ArcSDEDataStoreFactory.java 30772 2008-06-20 11:36:13Z groldan $
 */
public class ArcSDEDataStoreFactory implements DataStoreFactorySpi {
    /** package's logger */
    protected static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(ArcSDEDataStoreFactory.class.getPackage().getName());

    /** friendly factory description */
    private static final String FACTORY_DESCRIPTION = "ESRI(tm) ArcSDE 8.x and 9.x";

    /** DOCUMENT ME! */
    private static List<Param> paramMetadata = new ArrayList<Param>(10);

    public static final int JSDE_VERSION_DUMMY = -1;

    public static final int JSDE_VERSION_90 = 0;

    public static final int JSDE_VERSION_91 = 1;

    public static final int JSDE_VERSION_92 = 2;

    private static int JSDE_CLIENT_VERSION;

    static {
        String description = "namespace associated to this data store";
        paramMetadata.add(new Param("namespace", String.class, description, false));

        description = "fixed value. Must be \"arcsde\"";
        paramMetadata.add(new Param("dbtype", String.class, description, true, "arcsde"));

        description = "sever name where the ArcSDE gateway is running";
        paramMetadata.add(new Param("server", String.class, description, true));

        description = "port number in wich the ArcSDE server is listening for connections.Generally it's 5151";
        Integer DEFAULT_PORT = Integer.valueOf(5151);
        paramMetadata.add(new Param("port", Integer.class, description, true, DEFAULT_PORT));

        description = "the specific database to connect to. Only applicable to "
                + "certain databases. Value ignored if not applicable.";
        paramMetadata.add(new Param("instance", String.class, description, false));

        description = "name of a valid database user account.";
        paramMetadata.add(new Param("user", String.class, description, true));

        description = "the database user's password.";
        paramMetadata.add(new Param("password", String.class, new SimpleInternationalString(description),
                false, null, Collections.singletonMap(Parameter.IS_PASSWORD, Boolean.TRUE)));

        // optional parameters:
        description = "Minimun number of open connections";
        paramMetadata.add(new Param("pool.minConnections", Integer.class, description, false,
                Integer.valueOf(SessionPool.DEFAULT_CONNECTIONS)));

        description = "Maximun number of open connections (will not work if < 2)";
        Integer MAX_CONNECTIONS = Integer.valueOf(SessionPool.DEFAULT_MAX_CONNECTIONS);
        paramMetadata.add(new Param("pool.maxConnections", Integer.class, description, false,
                MAX_CONNECTIONS));

        description = "Milliseconds to wait for an available connection before failing to connect";
        Integer TIMEOUT = Integer.valueOf(SessionPool.DEFAULT_MAX_WAIT_TIME);
        paramMetadata.add(new Param("pool.timeOut", Integer.class, description, false, TIMEOUT));

        // determine which JSDE api we're running against
        determineJsdeVersion();
    }

    private static void determineJsdeVersion() {
        try {
            // this class only exists in the dummy api...
            Class.forName("com.esri.sde.sdk.GeoToolsDummyAPI");
            JSDE_CLIENT_VERSION = JSDE_VERSION_DUMMY;
        } catch (Throwable t) {
            // good, we're not using the Dummy API placeholder.
            try {
                // SeDBTune only exists in 9.2
                Class.forName("com.esri.sde.sdk.client.SeDBTune");
                JSDE_CLIENT_VERSION = JSDE_VERSION_92;
                LOGGER.fine("Using ArcSDE API version 9.2 (or higher)");
            } catch (Throwable t2) {
                // we're using 9.1 or 9.0.
                try {
                    int[] projcss = PeFactory.projcsCodelist();
                    if (projcss.length == 16380) {
                        // perhaps I am the hack-master.
                        JSDE_CLIENT_VERSION = JSDE_VERSION_91;
                        LOGGER.fine("Using ArcSDE API version 9.1");
                    } else {
                        JSDE_CLIENT_VERSION = JSDE_VERSION_90;
                        LOGGER.fine("Using ArcSDE API version 9.0 (or an earlier 8.x version)");
                    }
                } catch (Throwable crap) {
                    // not sure what happened here... This next line is
                    // un-intelligent.
                    JSDE_CLIENT_VERSION = JSDE_VERSION_90;
                }
            }
        }
    }

    /** factory of connection pools to different SDE databases */
    private static final SessionPoolFactory poolFactory = SessionPoolFactory.getInstance();

    /**
     * empty constructor
     */
    public ArcSDEDataStoreFactory() {
        if (!isAvailable()) {
            LOGGER.warning("The ESRI ArcSDE Java API seems to not be on your classpath. Please"
                    + " verify that all needed jars are. ArcSDE data stores"
                    + " will not be available.");
        }
    }

    /**
     * @throws UnsupportedOperationException always as the operation is not supported
     * @see DataStoreFactorySpi#createNewDataStore(Map)
     */
    public DataStore createNewDataStore(java.util.Map map) {
        throw new UnsupportedOperationException(
                "ArcSDE DataStore does not supports the creation of new databases. "
                        + "This should be done through database's specific tools");
    }

    /**
     * crates an SdeDataSource based on connection parameters held in <code>params</code>.
     * <p>
     * Expected parameters are:
     * <ul>
     * <li><b>dbtype </b>: MUST be <code>"arcsde"</code></li>
     * <li><b>server </b>: machine name where ArcSDE is running</li>
     * <li><b>port </b>: port number where ArcSDE listens for connections on server</li>
     * <li><b>instance </b>: database instance name to connect to</li>
     * <li><b>user </b>: database user name with at least reading privileges over SDE instance</li>
     * <li><b>password </b>: database user password</li>
     * </ul>
     * </p>
     * 
     * @param params connection parameters
     * @return a new <code>SdeDataStore</code> pointing to the database defined by
     *         <code>params</code>
     * @throws java.io.IOException if something goes wrong creating the datastore.
     */
    public DataStore createDataStore(Map params) throws java.io.IOException {
        if (JSDE_CLIENT_VERSION == JSDE_VERSION_DUMMY) {
            throw new DataSourceException("Can't connect to ArcSDE with the dummy jar.");
        }

        ArcSDEDataStore sdeDStore = null;
        ArcSDEConnectionConfig config = new ArcSDEConnectionConfig(params);
        //create a new session pool to be used only by this datastore
        final SessionPool connPool = poolFactory.createPool(config);

        // check to see if our sdk is compatible with this arcsde instance
        ISession session = null;
        try {
            session = connPool.getSession(Transaction.AUTO_COMMIT);
            SeRelease releaseInfo = session.getRelease();
            int majVer = releaseInfo.getMajor();
            int minVer = releaseInfo.getMinor();

            if (majVer == 9 && minVer > 1 && JSDE_CLIENT_VERSION < JSDE_VERSION_91) {
                // we can't connect to ArcSDE 9.2 with the arcsde 9.0 jars. It
                // just won't
                // work when trying to draw maps. Oh well, at least we'll warn
                // people.
                LOGGER
                        .severe("\n\n**************************\n"
                                + "DANGER DANGER DANGER!!!  You're using the ArcSDE 9.0 (or earlier) jars with "
                                + "ArcSDE "
                                + majVer
                                + "."
                                + minVer
                                + " on host '"
                                + config.getServerName()
                                + "' .  "
                                + "This PROBABLY WON'T WORK.  If you have issues "
                                + "or unexplained exceptions when rendering maps, upgrade your ArcSDE jars to version "
                                + "9.2 or higher.  See http://docs.codehaus.org/display/GEOTOOLS/ArcSDE+Plugin\n"
                                + "**************************\n\n");
            }
        } finally {
            if (session != null)
                session.dispose();
        }

        String namespaceUri = config.getNamespaceUri();
        if (connPool instanceof ArcSDEConnectionReference) {
            // notice we check the pool rather than the config? that is because
            // another user may of been in ahead of us and create connection pool
            //
            sdeDStore = new ArcSDEDataStore(connPool, namespaceUri);
        } else {
            sdeDStore = new ArcSDEDataStore(connPool, namespaceUri);
        }

        ViewRegisteringFactoryHelper.registerSqlViews(sdeDStore, params);

        return sdeDStore;
    }

    /**
     * Display name for this DataStore Factory
     * 
     * @return <code>"ArcSDE"</code>
     */
    public String getDisplayName() {
        return "ArcSDE";
    }

    // /** Interpret connection params as a metadata entity */
    // public DataSourceMetadataEnity createMetadata( Map params )
    // throws IOException {
    //
    // ArcSDEConnectionConfig config;
    // try {
    // config = new ArcSDEConnectionConfig(params);
    // } catch (NullPointerException ex) {
    // throw new IOException( "Cannot use provided params to connect" );
    // } catch (IllegalArgumentException ex) {
    // throw new DataSourceException( "Cannot use provided params to
    // connect:"+ex.getMessage(), ex );
    // }
    // String description =
    // "Connection to "+config.getDatabaseName()+ " at
    // "+config.getServerName()+":"+config.getPortNumber()+ " as "+
    // config.getUserName();
    // return new DataSourceMetadataEnity( config.getServerName(),
    // config.getDatabaseName(), description );
    // }

    /**
     * A human friendly name for this data source factory
     * 
     * @return this factory's description
     */
    public String getDescription() {
        return FACTORY_DESCRIPTION;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param params
     */
    public boolean canProcess(Map params) {
        if (JSDE_CLIENT_VERSION == JSDE_VERSION_DUMMY) {
            return false;
        }
        boolean canProcess = true;

        try {
            new ArcSDEConnectionConfig(params);
        } catch (NullPointerException ex) {
            canProcess = false;
        } catch (IllegalArgumentException ex) {
            canProcess = false;
        }

        return canProcess;
    }

    /**
     * Test to see if this datastore is available, if it has all the appropriate libraries to
     * construct a datastore.
     * 
     * @return <tt>true</tt> if and only if this factory is available to create DataStores.
     */
    public boolean isAvailable() {
        if (JSDE_CLIENT_VERSION == JSDE_VERSION_DUMMY) {
            LOGGER.warning("You must download and install the *real* ArcSDE JSDE jar files. "
                    + "Currently the GeoTools ArcSDE 'dummy jar' is on your classpath. "
                    + "ArcSDE connectivity is DISABLED. "
                    + "See http://docs.codehaus.org/display/GEOTOOLS/ArcSDE+Plugin");
            return false;
        }
        try {
            LOGGER.finer(SeConnection.class.getName() + " is in place.");
            LOGGER.finer(PeCoordinateSystem.class.getName() + " is in place.");
        } catch (Throwable t) {
            return false;
        }

        return true;
    }

    /**
     * @see DataStoreFactorySpi#getParametersInfo()
     */
    public DataStoreFactorySpi.Param[] getParametersInfo() {
        return paramMetadata.toArray(new Param[paramMetadata.size()]);
    }

    /**
     * Returns the implementation hints. The default implementation returns en empty map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }

    public static int getSdeClientVersion() {
        return JSDE_CLIENT_VERSION;
    }
}
