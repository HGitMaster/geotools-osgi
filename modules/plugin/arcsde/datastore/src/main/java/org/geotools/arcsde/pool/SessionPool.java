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
package org.geotools.arcsde.pool;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.geotools.data.DataSourceException;
import org.geotools.data.Transaction;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeRelease;

/**
 * Maintains <code>SeConnection</code>'s for a single set of connection
 * properties (for instance: by server, port, user and password) in a pool to
 * recycle used connections.
 * <p>
 * We are making use of an Apache Commons ObjectPool to maintain connections.
 * This connection pool is configurable in the sense that some parameters can be
 * passed to establish the pooling policy. To pass parameters to the connection
 * pool, you should set properties in the parameters Map passed to
 * SdeDataStoreFactory.createDataStore, which will invoke
 * SdeConnectionPoolFactory to get the SDE instance's pool singleton. That
 * instance singleton will be created with the preferences passed the first time
 * createDataStore is called for a given SDE instance/user, if subsequent calls
 * change that preferences, they will be ignored.
 * </p>
 * <p>
 * The expected optional parameters that you can set up in the argument Map for
 * createDataStore are:
 * <ul>
 * <li> pool.minConnections Integer, tells the minimum number of open
 * connections the pool will maintain opened </li>
 * <li> pool.maxConnections Integer, tells the maximum number of open
 * connections the pool will create and maintain opened </li>
 * <li> pool.timeOut Integer, tells how many milliseconds a calling thread is
 * guaranteed to wait before getConnection() throws an
 * UnavailableArcSDEConnectionException </li>
 * </ul>
 * </p>
 * 
 * @author Gabriel Roldan
 * @version $Id: SessionPool.java 31925 2008-11-28 18:44:55Z groldan $
 */
public class SessionPool {
    /** package's logger */
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.arcsde.pool");

    /** DOCUMENT ME! */
    protected static final Level INFO_LOG_LEVEL = Level.WARNING;

    /** default number of connections a pool creates at first population */
    public static final int DEFAULT_CONNECTIONS = 2;

    /** default number of maximun allowable connections a pool can hold */
    public static final int DEFAULT_MAX_CONNECTIONS = 6;

    public static final int DEFAULT_MAX_WAIT_TIME = 500;

    /** DOCUMENT ME! */
    private SeConnectionFactory seConnectionFactory;

    /** this connection pool connection's parameters */
    protected ArcSDEConnectionConfig config;

    /** Apache commons-pool used to pool arcsde connections */
    protected ObjectPool pool;

    /**
     * Creates a new SdeConnectionPool object with the connection parameters
     * holded by <code>config</code>
     * 
     * @param config
     *            holds connection options such as server, user and password, as
     *            well as tuning options as maximum number of connections
     *            allowed
     * @throws DataSourceException
     *             If connection could not be established
     * @throws NullPointerException
     *             If config is null
     */
    protected SessionPool(ArcSDEConnectionConfig config) throws DataSourceException {
        if (config == null) {
            throw new NullPointerException("parameter config can't be null");
        }

        this.config = config;
        LOGGER.fine("populating ArcSDE connection pool");

        this.seConnectionFactory = createConnectionFactory();

        int minConnections = config.getMinConnections().intValue();
        int maxConnections = config.getMaxConnections().intValue();
        // byte exhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;
        byte exhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
        long maxWait = config.getConnTimeOut().longValue();

        this.pool = new GenericObjectPool(seConnectionFactory, maxConnections, exhaustedAction,
                maxWait, true, true);
        LOGGER.fine("Created ArcSDE connection pool for " + config);

        ISession[] preload = new ISession[minConnections];

        try {
            for (int i = 0; i < minConnections; i++) {
                preload[i] = (ISession) this.pool.borrowObject();
                if (i == 0) {
                    SeRelease seRelease = preload[i].getRelease();
                    String sdeDesc = seRelease.getDesc();
                    int major = seRelease.getMajor();
                    int minor = seRelease.getMinor();
                    int bugFix = seRelease.getBugFix();
                    String desc = "ArcSDE " + major + "." + minor + "." + bugFix + " " + sdeDesc;
                    LOGGER.fine("Connected to " + desc);
                }
            }

            for (int i = 0; i < minConnections; i++) {
                this.pool.returnObject(preload[i]);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "can't connect to " + config, e);
            throw new DataSourceException(e);
        }
    }

    /**
     * SeConnectionFactory used to create {@link ISession} instances for the
     * pool.
     * <p>
     * Subclass may overide to customize this behaviour.
     * </p>
     * 
     * @return SeConnectionFactory.
     */
    protected SeConnectionFactory createConnectionFactory() {
        return new SeConnectionFactory(this.config);
    }

    /**
     * returns the number of actual connections held by this connection pool.
     * In other words, the sum of used and available connections, regardless
     * 
     * @return DOCUMENT ME!
     */
    public int getPoolSize() {
        checkOpen();
        synchronized (this.pool) {
            return this.pool.getNumActive() + this.pool.getNumIdle();
        }
    }

    /**
     * closes all connections in this pool. The first call closes all
     * SeConnections, further calls have no effect.
     */
    public void close() {
        if (pool != null) {
            try {
                this.pool.close();
                pool = null;
                LOGGER.fine("SDE connection pool closed. ");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Closing pool: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Returns whether this pool is closed
     * 
     * @return
     */
    public boolean isClosed() {
        return pool == null;
    }

    private void checkOpen() throws IllegalStateException {
        if (isClosed()) {
            throw new IllegalStateException("This session pool is closed");
        }
    }

    /**
     * Ensures proper closure of connection pool at this object's finalization
     * stage.
     */
    @Override
    protected void finalize() {
        close();
    }

    /**
     * TODO: Document this method!
     * 
     * @return DOCUMENT ME!
     */
    public synchronized int getAvailableCount() {
        checkOpen();
        return this.pool.getNumIdle();
    }

    /**
     * Number of active sessions.
     * 
     * @return Number of active session; used to monitor the live pool.
     */
    public synchronized int getInUseCount() {
        checkOpen();
        return this.pool.getNumActive();
    }

    /**
     * Retrieve the connection for the provided transaction.
     * <p>
     * The connection is held open until while the transaction is underway. A a
     * Transaction.State is registered for this SessionPool in order to hold the
     * session.
     * </p>
     * 
     * @param transaction
     * @return the session associated with the transaction
     */
    public ISession getSession(Transaction transaction) throws IOException {
        final ISession session;
        checkOpen();
        if (Transaction.AUTO_COMMIT.equals(transaction)) {
            session = getSession();
        } else {
            SessionTransactionState state;
            state = SessionTransactionState.getState(transaction, this);
            session = state.getConnection();
        }
        return session;
    }

    /**
     * Grab a session from the pool, this session is the responsibility of the
     * calling code and must be closed after use.
     * 
     * @return A Session, when close() is called it will be recycled into the
     *         pool
     * @throws DataSourceException
     *             If we could not get a connection
     * @throws UnavailableArcSDEConnectionException
     *             If we are out of connections
     * @throws IllegalStateException
     *             If pool has been closed.
     */
    public ISession getSession() throws DataSourceException, UnavailableArcSDEConnectionException {
        checkOpen();
        try {
            // String caller = null;
            // if (LOGGER.isLoggable(Level.FINER)) {
            // StackTraceElement[] stackTrace =
            // Thread.currentThread().getStackTrace();
            // caller = stackTrace[3].getClassName() + "." +
            // stackTrace[3].getMethodName();
            // }
            Session connection = (Session) this.pool.borrowObject();

            if (LOGGER.isLoggable(Level.FINER)) {
                // System.err.println("-> " + caller + " got " + connection);
                LOGGER.finer("-->" + connection + " out of connection pool. Active: "
                        + pool.getNumActive() + ", idle: " + pool.getNumIdle());
            }

            connection.markActive();
            return connection;
        } catch (NoSuchElementException e) {
            LOGGER.log(Level.WARNING, "Out of connections: " + e.getMessage(), e);
            throw new UnavailableArcSDEConnectionException(this.pool.getNumActive(), this.config);
        } catch (SeException se) {
            LOGGER.log(Level.WARNING, "ArcSDE error getting connection: "
                    + se.getSeError().getErrDesc(), se);
            throw new DataSourceException("ArcSDE Error Message: " + se.getSeError().getErrDesc(),
                    se);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unknown problem getting connection: " + e.getMessage(), e);
            throw new DataSourceException(
                    "Unknown problem fetching connection from connection pool", e);
        }
    }

    /**
     * Gets the list of available layer names on the database
     * 
     * @return a <code>List&lt;String&gt;</code> with the registered
     *         featureclasses on the ArcSDE database
     * @throws DataSourceException
     */
    @SuppressWarnings("unchecked")
    public List<String> getAvailableLayerNames() throws IOException {
        checkOpen();
        final ISession session;

        final List<String> layerNames;
        try {
            session = getSession();
        } catch (UnavailableArcSDEConnectionException ex) {
            throw new DataSourceException("No free connection found to query the layers list", ex);
        }

        try {
            layerNames = session.issue(new Command<List<String>>() {
                @Override
                public List<String> execute(ISession session, SeConnection connection)
                        throws SeException, IOException {
                    final List<String> layerNames = new LinkedList<String>();
                    final List<SeLayer> layers = session.getLayers();
                    SeLayer layer;
                    for (Iterator<SeLayer> it = layers.iterator(); it.hasNext();) {
                        layer = it.next();
                        layerNames.add(layer.getQualifiedName());
                    }
                    return layerNames;
                }
            });
        } finally {
            session.dispose();
        }

        return layerNames;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public ArcSDEConnectionConfig getConfig() {
        return this.config;
    }

    /**
     * PoolableObjectFactory intended to be used by a Jakarta's commons-pool
     * objects pool, that provides ArcSDE's SeConnections.
     * 
     * @author Gabriel Roldan, Axios Engineering
     * @version $Id: SessionPool.java 31925 2008-11-28 18:44:55Z groldan $
     */
    protected class SeConnectionFactory extends BasePoolableObjectFactory {
        /** DOCUMENT ME! */
        private ArcSDEConnectionConfig config;

        /**
         * Creates a new SeConnectionFactory object.
         * 
         * @param config
         *            DOCUMENT ME!
         */
        public SeConnectionFactory(ArcSDEConnectionConfig config) {
            super();
            this.config = config;
        }

        /**
         * Called whenever a new instance is needed.
         * 
         * @return a newly created <code>SeConnection</code>
         * @throws SeException
         *             if the connection can't be created
         */
        @Override
        public Object makeObject() throws IOException {
            NegativeArraySizeException cause = null;
            for (int i = 0; i < 3; i++) {
                try {
                    ISession seConn = new Session(SessionPool.this.pool, config);
                    return seConn;
                } catch (NegativeArraySizeException nase) {
                    LOGGER.warning("Strange failed ArcSDE connection error.  Trying again (try "
                            + (i + 1) + " of 3)");
                    cause = nase;
                }
            }
            throw new DataSourceException(
                    "Couldn't create ArcSDE Session because of strange SDE internal exception.  Tried 3 times, giving up.",
                    cause);
        }

        /**
         * is invoked on every instance before it is returned from the pool.
         * 
         * @param obj
         */
        @Override
        public void activateObject(Object obj) {
            final Session conn = (Session) obj;
            conn.markActive();
            LOGGER.finest("    activating connection " + obj);
        }

        @Override
        public void passivateObject(Object obj) {
            LOGGER.finest("    passivating connection " + obj);
            final Session conn = (Session) obj;
            conn.markInactive();
        }

        /**
         * is invoked in an implementation-specific fashion to determine if an
         * instance is still valid to be returned by the pool. It will only be
         * invoked on an "activated" instance.
         * 
         * @param an
         *            instance of {@link Session} maintained by this pool.
         * @return <code>true</code> if the connection is still alive and
         *         operative (checked by asking its user name),
         *         <code>false</code> otherwise.
         */
        @Override
        public boolean validateObject(Object obj) {
            ISession session = (ISession) obj;
            boolean valid = !session.isClosed();
            // MAKE PROPER VALIDITY CHECK HERE as for GEOT-1273
            if (valid) {
                try {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("    Validating SDE Connection " + session);
                    }
                    String user = session.getUser();
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("    Connection validated, user: " + user);
                    }
                } catch (IOException e) {
                    LOGGER.info("Can't validate SeConnection, discarding it: " + session);
                    valid = false;
                }
            }
            return valid;
        }

        /**
         * is invoked on every instance when it is being "dropped" from the pool
         * (whether due to the response from validateObject, or for reasons
         * specific to the pool implementation.)
         * 
         * @param obj
         *            an instance of {@link Session} maintained by this pool.
         */
        @Override
        public void destroyObject(Object obj) {
            Session conn = (Session) obj;
            conn.destroy();
        }
    }

    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer();
        if (pool == null) {
            ret.append("[Session pool is disposed]");
        } else {
            ret.append("[ACTIVE: ");
            ret.append(pool.getNumActive() + "/" + ((GenericObjectPool) pool).getMaxActive());
            ret.append("  INACTIVE: ");
            ret.append(pool.getNumIdle() + "/" + ((GenericObjectPool) pool).getMaxIdle() + "]");
        }
        return ret.toString();
    }

}
