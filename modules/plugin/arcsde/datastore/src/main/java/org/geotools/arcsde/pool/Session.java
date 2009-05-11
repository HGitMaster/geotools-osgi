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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.pool.ObjectPool;
import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.data.SdeRow;
import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeDBMSInfo;
import com.esri.sde.sdk.client.SeDelete;
import com.esri.sde.sdk.client.SeError;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRasterColumn;
import com.esri.sde.sdk.client.SeRegistration;
import com.esri.sde.sdk.client.SeRelease;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeState;
import com.esri.sde.sdk.client.SeStreamOp;
import com.esri.sde.sdk.client.SeTable;
import com.esri.sde.sdk.client.SeUpdate;
import com.esri.sde.sdk.client.SeVersion;
import com.esri.sde.sdk.geom.GeometryFactory;

/**
 * Provides thread safe access to an SeConnection.
 * <p>
 * This class has become more and more magic over time! It no longer represents a Connection but
 * provides "safe" access to a connection.
 * <p>
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id: Session.java 32322 2009-01-24 20:11:52Z groldan $
 * @since 2.3.x
 */
class Session implements ISession {

    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.arcsde.pool");

    /** Actual SeConnection being protected */
    SeConnection connection;

    /**
     * ObjectPool used to manage open connections (shared).
     */
    private ObjectPool pool;

    private ArcSDEConnectionConfig config;

    private static int sessionCounter;

    private int sessionId;

    private boolean transactionInProgress;

    private boolean isPassivated;

    private Map<String, SeTable> cachedTables = new WeakHashMap<String, SeTable>();

    private Map<String, SeLayer> cachedLayers = new WeakHashMap<String, SeLayer>();

    private Map<String, SeRasterColumn> cachedRasters = new HashMap<String, SeRasterColumn>();

    /**
     * The SeConnection bound task executor, ensures all operations against a given connection are
     * performed in the same thread regardless of the thread the {@link #issue(Command)} is being
     * called from.
     */
    private final ExecutorService taskExecutor;

    /**
     * Thread used by the taskExecutor; so we can detect recursion.
     */
    private Thread commandThread;

    /**
     * Provides safe access to an SeConnection.
     * 
     * @param pool
     *            ObjectPool used to manage SeConnection
     * @param config
     *            Used to set up a SeConnection
     * @throws SeException
     *             If we cannot connect
     */
    Session(final ObjectPool pool, final ArcSDEConnectionConfig config) throws IOException {
        this.config = config;
        this.pool = pool;
        this.taskExecutor = Executors.newSingleThreadExecutor();

        // grab command thread
        updateCommandThread();

        // This ensures the connection runs always on the same thread. Will fail
        // if its
        // accessed by different threads
        this.connection = issue(new Command<SeConnection>() {
            @Override
            public SeConnection execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                String serverName = config.getServerName();
                int intValue = config.getPortNumber().intValue();
                String databaseName = config.getDatabaseName();
                String userName = config.getUserName();
                String userPassword = config.getUserPassword();

                SeConnection conn = new SeConnection(serverName, intValue, databaseName, userName,
                        userPassword);

                conn.setConcurrency(SeConnection.SE_ONE_THREAD_POLICY);
                return conn;
            }
        });

        synchronized (Session.class) {
            sessionCounter++;
            sessionId = sessionCounter;
        }
    }

    /**
     * @see ISession#issue(org.geotools.arcsde.pool.Command)
     */
    public <T> T issue(final Command<T> command) throws IOException {
        final Thread callingThread = Thread.currentThread();
        if (callingThread == commandThread) {
            // Called command inside command
            try {
                return command.execute(this, connection);
            } catch (SeException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                throw new ArcSdeException(e);
            }
        } else {
            StackTraceElement ste = callingThread.getStackTrace()[3];
            // System.err.println("executing command " + ste.getClassName() +
            // "."
            // + ste.getMethodName() + ":" + ste.getLineNumber() + " ("
            // + callingThread.getName() + ")");

            final FutureTask<T> task = new FutureTask<T>(new Callable<T>() {
                public T call() throws Exception {
                    final Thread currentThread = Thread.currentThread();

                    if (commandThread != currentThread) {
                        LOGGER.fine("updating command thread from " + commandThread + " to "
                                + currentThread);
                        commandThread = currentThread;

                    }

                    // System.err.println(" -executing command for Session "
                    // + Session.this.connectionId + " in thread " +
                    // currentThread.getId());

                    if (currentThread != commandThread) {
                        throw new IllegalStateException("currentThread != commandThread");
                    }
                    try {
                        return command.execute(Session.this, connection);
                    } catch (Exception e) {
                        if (e instanceof SeException) {
                            e = new ArcSdeException((SeException) e);
                        }
                        LOGGER
                                .log(Level.SEVERE, "Command execution failed for Session "
                                        + Session.this.sessionId + " in thread "
                                        + currentThread.getId(), e);
                        throw e;
                    }
                }
            });

            T result;
            synchronized (config) {
                taskExecutor.execute(task);
                try {
                    result = task.get();
                } catch (InterruptedException e) {
                    updateCommandThread();
                    throw new RuntimeException("Command execution abruptly interrupted", e);
                } catch (ExecutionException e) {
                    updateCommandThread();
                    Throwable cause = e.getCause();
                    if (cause instanceof IOException) {
                        throw (IOException) cause;
                    } else if (cause instanceof SeException) {
                        throw new ArcSdeException((SeException) cause);
                    }
                    throw new DataSourceException(cause);
                }

            }
            return result;
        }
    }

    private void updateCommandThread() {
        final FutureTask<?> task = new FutureTask<Object>(new Callable<Object>() {
            public Object call() throws Exception {
                final Thread currentThread = Thread.currentThread();
                if (currentThread != commandThread) {
                    LOGGER.fine("updating command thread from " + commandThread + " to "
                            + currentThread);
                    commandThread = currentThread;
                }
                return null;
            }
        });
        // used to detect when thread has been
        // restarted after error
        taskExecutor.execute(task);
        // block until task is executed
        try {
            task.get();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * @see ISession#isClosed()
     */
    public final boolean isClosed() {
        return this.connection.isClosed();
    }

    /**
     * Marks the connection as being active (i.e. its out of the pool and ready to be used).
     * <p>
     * Shall be called just before being returned from the connection pool
     * </p>
     * 
     * @see #markInactive()
     * @see #isPassivated
     * @see #checkActive()
     */
    void markActive() {
        this.isPassivated = false;
    }

    /**
     * Marks the connection as being inactive (i.e. laying on the connection pool)
     * <p>
     * Shall be callled just before sending it back to the pool
     * </p>
     * 
     * @see #markActive()
     * @see #isPassivated
     * @see #checkActive()
     */
    void markInactive() {
        this.isPassivated = true;
    }

    /**
     * @see ISession#isPassivated()
     */
    public boolean isDisposed() {
        return isPassivated;
    }

    /**
     * Sanity check method called before every public operation delegates to the superclass.
     * 
     * @throws IllegalStateException
     *             if {@link #isDisposed() isPassivated() == true} as this is a serious workflow
     *             breackage.
     */
    private void checkActive() {
        if (isDisposed()) {
            throw new IllegalStateException("Unrecoverable error: " + toString()
                    + " is passivated, shall not be used!");
        }
    }

    /**
     * @see ISession#getLayer(java.lang.String)
     */
    public SeLayer getLayer(final String layerName) throws IOException {
        checkActive();
        return issue(new Command<SeLayer>() {
            @Override
            public SeLayer execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                if (!cachedLayers.containsKey(layerName)) {
                    synchronized (Session.this) {
                        if (!cachedLayers.containsKey(layerName)) {
                            cacheLayers();
                        }
                    }
                }
                SeLayer seLayer = cachedLayers.get(layerName);
                if (seLayer == null) {
                    throw new NoSuchElementException("Layer '" + layerName + "' not found");
                }
                return seLayer;
            }
        });
    }

    /**
     * @see ISession#getRasterColumn(java.lang.String)
     */
    public synchronized SeRasterColumn getRasterColumn(final String rasterName) throws IOException {
        checkActive();
        if (!cachedRasters.containsKey(rasterName)) {
            try {
                cacheRasters();
            } catch (SeException e) {
                throw new DataSourceException("Can't obtain raster " + rasterName, e);
            }
        }
        SeRasterColumn raster = cachedRasters.get(rasterName);
        if (raster == null) {
            throw new NoSuchElementException("Raster '" + rasterName + "' not found");
        }
        return raster;
    }

    /**
     * @see ISession#getTable(java.lang.String)
     */
    public SeTable getTable(final String tableName) throws IOException {
        checkActive();
        return issue(new Command<SeTable>() {
            @Override
            public SeTable execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                if (!cachedTables.containsKey(tableName)) {
                    synchronized (Session.this) {
                        if (!cachedTables.containsKey(tableName)) {
                            cacheLayers();
                        }
                    }
                }
                SeTable seTable = (SeTable) cachedTables.get(tableName);
                if (seTable == null) {
                    throw new NoSuchElementException("Table '" + tableName + "' not found");
                }
                return seTable;
            }
        });
    }

    /**
     * Caches both tables and layers
     * 
     * @throws SeException
     */
    @SuppressWarnings("unchecked")
    private void cacheLayers() throws IOException {
        try {
            Vector/* <SeLayer> */layers = connection.getLayers();
            String qualifiedName;
            SeLayer layer;
            SeTable table;
            cachedTables.clear();
            cachedLayers.clear();
            for (Iterator it = layers.iterator(); it.hasNext();) {
                layer = (SeLayer) it.next();
                qualifiedName = layer.getQualifiedName();
                table = new SeTable(connection, qualifiedName);
                cachedLayers.put(qualifiedName, layer);
                cachedTables.put(qualifiedName, table);
            }
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void cacheRasters() throws SeException {
        Vector<SeRasterColumn> rasters = this.connection.getRasterColumns();
        cachedRasters.clear();
        for (SeRasterColumn raster : rasters) {
            cachedRasters.put(raster.getQualifiedTableName(), raster);
        }
    }

    /**
     * @see ISession#startTransaction()
     */
    public void startTransaction() throws IOException {
        checkActive();
        issue(new Command<Void>() {
            @Override
            public Void execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                connection.setTransactionAutoCommit(0);
                connection.startTransaction();
                transactionInProgress = true;
                return null;
            }
        });
    }

    /**
     * @see ISession#commitTransaction()
     */
    public void commitTransaction() throws IOException {
        checkActive();
        issue(new Command<Void>() {
            @Override
            public Void execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                connection.commitTransaction();
                return null;
            }
        });
        transactionInProgress = false;
    }

    /**
     * @see ISession#isTransactionActive()
     */
    public boolean isTransactionActive() {
        checkActive();
        return transactionInProgress;
    }

    /**
     * @see ISession#rollbackTransaction()
     */
    public void rollbackTransaction() throws IOException {
        checkActive();
        try {
            issue(new Command<Void>() {
                @Override
                public Void execute(final ISession session, final SeConnection connection)
                        throws SeException, IOException {
                    connection.rollbackTransaction();
                    return null;
                }
            });
        } finally {
            transactionInProgress = false;
        }
    }

    /**
     * @see ISession#close()
     */
    public void dispose() throws IllegalStateException {
        checkActive();
        if (transactionInProgress) {
            throw new IllegalStateException(
                    "Transaction is in progress, should commit or rollback before closing");
        }

        try {
            this.pool.returnObject(this);
            if (LOGGER.isLoggable(Level.FINER)) {
                // StackTraceElement[] stackTrace =
                // Thread.currentThread().getStackTrace();
                // String caller = stackTrace[3].getClassName() + "." +
                // stackTrace[3].getMethodName();
                // System.err.println("<- " + caller + " returning " +
                // toString() + " to pool");
                LOGGER.finer("<-- " + toString() + " retured to pool. Active: "
                        + pool.getNumActive() + ", idle: " + pool.getNumIdle());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "Session[" + sessionId + "]";
    }

    /**
     * Actually closes the connection, called when the session is discarded from the pool
     */
    void destroy() {
        LOGGER.fine("Destroying connection " + toString());
        try {
            issue(new Command<Void>() {
                @Override
                public Void execute(final ISession session, final SeConnection connection)
                        throws SeException, IOException {
                    connection.close();
                    return null;
                }
            });
            LOGGER.fine(toString() + " successfully closed");
        } catch (Exception e) {
            LOGGER
                    .log(Level.WARNING, "closing connection " + toString() + ": " + e.getMessage(),
                            e);
        }
    }

    /**
     * @see ISession#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        return other == this;
    }

    /**
     * @see ISession#hashCode()
     */
    @Override
    public int hashCode() {
        return 17 ^ this.config.hashCode();
    }

    /**
     * @see ISession#getLayers()
     */
    @SuppressWarnings("unchecked")
    public List<SeLayer> getLayers() throws IOException {
        return issue(new Command<List<SeLayer>>() {
            @Override
            public List<SeLayer> execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                return connection.getLayers();
            }
        });
    }

    /**
     * @see ISession#getUser()
     */
    public String getUser() throws IOException {
        return issue(new Command<String>() {
            @Override
            public String execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                return connection.getUser();
            }
        });
    }

    /**
     * @see ISession#getRelease()
     */
    public SeRelease getRelease() throws IOException {
        return issue(new Command<SeRelease>() {
            @Override
            public SeRelease execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                return connection.getRelease();
            }
        });
    }

    /**
     * @see ISession#getDatabaseName()
     */
    public String getDatabaseName() throws IOException {
        return issue(new Command<String>() {
            @Override
            public String execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                return connection.getDatabaseName();
            }
        });
    }

    public SeDBMSInfo getDBMSInfo() throws IOException {
        return issue(new Command<SeDBMSInfo>() {
            @Override
            public SeDBMSInfo execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                return connection.getDBMSInfo();
            }
        });
    }

    //
    // Factory methods that make use of internal connection
    // Q: How "long" are these objects good for? until the connection closes -
    // or longer...
    //
    /**
     * @see ISession#createSeLayer()
     */
    public SeLayer createSeLayer() throws IOException {
        return issue(new Command<SeLayer>() {
            @Override
            public SeLayer execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                return new SeLayer(connection);
            }
        });
    }

    /**
     * @see ISession#createSeRegistration(java.lang.String)
     */
    public SeRegistration createSeRegistration(final String typeName) throws IOException {
        return issue(new Command<SeRegistration>() {
            @Override
            public SeRegistration execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                return new SeRegistration(connection, typeName);
            }
        });
    }

    /**
     * @see ISession#createSeTable(java.lang.String)
     */
    public SeTable createSeTable(final String qualifiedName) throws IOException {
        return issue(new Command<SeTable>() {
            @Override
            public SeTable execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                return new SeTable(connection, qualifiedName);
            }
        });
    }

    /**
     * @see ISession#createSeInsert()
     */
    public SeInsert createSeInsert() throws IOException {
        return issue(new Command<SeInsert>() {
            @Override
            public SeInsert execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                return new SeInsert(connection);
            }
        });
    }

    /**
     * @see ISession#createSeUpdate()
     */
    public SeUpdate createSeUpdate() throws IOException {
        return issue(new Command<SeUpdate>() {
            @Override
            public SeUpdate execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                return new SeUpdate(connection);
            }
        });
    }

    /**
     * @see ISession#createSeDelete()
     */
    public SeDelete createSeDelete() throws IOException {
        return issue(new Command<SeDelete>() {
            @Override
            public SeDelete execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                return new SeDelete(connection);
            }
        });
    }

    /**
     * @see ISession#createSeRasterColumn()
     */
    public SeRasterColumn createSeRasterColumn() throws IOException {
        return issue(new Command<SeRasterColumn>() {
            @Override
            public SeRasterColumn execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                return new SeRasterColumn(connection);
            }
        });
    }

    /**
     * @see ISession#createSeRasterColumn(com.esri.sde.sdk.client.SeObjectId)
     */
    public SeRasterColumn createSeRasterColumn(final SeObjectId rasterColumnId) throws IOException {
        return issue(new Command<SeRasterColumn>() {
            @Override
            public SeRasterColumn execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                return new SeRasterColumn(connection, rasterColumnId);
            }
        });
    }

    /**
     * @see ISession#describe(java.lang.String)
     */
    public SeColumnDefinition[] describe(final String tableName) throws IOException {
        final SeTable table = getTable(tableName);
        return describe(table);
    }

    /**
     * @see ISession#describe(com.esri.sde.sdk.client.SeTable)
     */
    public SeColumnDefinition[] describe(final SeTable table) throws IOException {
        return issue(new Command<SeColumnDefinition[]>() {
            @Override
            public SeColumnDefinition[] execute(final ISession session,
                    final SeConnection connection) throws SeException, IOException {
                return table.describe();
            }
        });
    }

    /**
     * @see ISession#fetch(com.esri.sde.sdk.client.SeQuery)
     */
    public SdeRow fetch(final SeQuery query) throws IOException {
        return fetch(query, new SdeRow((GeometryFactory)null));
    }

    public SdeRow fetch(final SeQuery query, final SdeRow currentRow) throws IOException{
        return issue(new Command<SdeRow>() {
            @Override
            public SdeRow execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                SeRow row = query.fetch();
                if (row == null) {
                    return null;
                }else{
                    currentRow.setRow(row);
                }
                return currentRow;
            }
        });
    }
    
    /**
     * @see ISession#close(com.esri.sde.sdk.client.SeState)
     */
    public void close(final SeState state) throws IOException {
        issue(new Command<Void>() {
            @Override
            public Void execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                state.close();
                return null;
            }
        });
    }

    /**
     * @see ISession#close(com.esri.sde.sdk.client.SeStreamOp)
     */
    public void close(final SeStreamOp stream) throws IOException {
        issue(new Command<Void>() {
            @Override
            public Void execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                stream.close();
                return null;
            }
        });
    }

    /**
     * @see ISession#createState(com.esri.sde.sdk.client.SeObjectId)
     */
    public SeState createState(final SeObjectId stateId) throws IOException {
        return issue(new Command<SeState>() {
            @Override
            public SeState execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                return new SeState(connection, stateId);
            }
        });
    }

    /**
     * @see ISession#createSeQuery()
     */
    public SeQuery createSeQuery() throws IOException {
        return issue(new Command<SeQuery>() {
            @Override
            public SeQuery execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                return new SeQuery(connection);
            }
        });
    }

    /**
     * @see ISession#createSeQuery(java.lang.String[], com.esri.sde.sdk.client.SeSqlConstruct)
     */
    public SeQuery createSeQuery(final String[] propertyNames, final SeSqlConstruct sql)
            throws IOException {

        return issue(new Command<SeQuery>() {
            @Override
            public SeQuery execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                return new SeQuery(connection, propertyNames, sql);
            }
        });
    }

    /**
     * @see ISession#createAndExecuteQuery(java.lang.String[],
     *      com.esri.sde.sdk.client.SeSqlConstruct)
     */
    public SeQuery createAndExecuteQuery(final String[] propertyNames, final SeSqlConstruct sql)
            throws IOException {
        return issue(new Command<SeQuery>() {
            @Override
            public SeQuery execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                SeQuery query = new SeQuery(connection, propertyNames, sql);
                query.prepareQuery();
                query.execute();
                return query;
            }
        });
    }

    /**
     * @see ISession#getDefaultVersion()
     */
    public SeVersion getDefaultVersion() throws IOException {
        return issue(new Command<SeVersion>() {
            @Override
            public SeVersion execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                SeVersion defaultVersion = new SeVersion(connection,
                        SeVersion.SE_QUALIFIED_DEFAULT_VERSION_NAME);
                defaultVersion.getInfo();
                return defaultVersion;
            }
        });
    }

    /**
     * Creates either a direct child state of parentStateId, or a sibling being an exact copy of
     * parentStatId if either the state can't be closed because its in use or parentStateId does not
     * belong to the current user.
     */
    public SeState createChildState(final long parentStateId) throws IOException {
        return issue(new Command<SeState>() {
            @Override
            public SeState execute(ISession session, SeConnection connection) throws SeException,
                    IOException {
                SeState parentState = new SeState(connection, new SeObjectId(parentStateId));

                SeState realParent = null;

                boolean mergeParentToRealParent = false;

                if (parentState.isOpen()) {
                    // only closed states can have child states
                    try {
                        parentState.close();
                        realParent = parentState;
                    } catch (SeException e) {
                        final int errorCode = e.getSeError().getSdeError();
                        if (SeError.SE_STATE_INUSE == errorCode
                                || SeError.SE_NO_PERMISSIONS == errorCode) {
                            // it's not our state or somebody's editing it so we
                            // need to clone the parent,
                            // starting from the parent of the parent
                            realParent = new SeState(connection, parentState.getParentId());
                            mergeParentToRealParent = true;
                        } else {
                            throw e;
                        }
                    }
                } else {
                    realParent = parentState;
                }

                // create the new state
                SeState newState = new SeState(connection);
                newState.create(realParent.getId());

                if (mergeParentToRealParent) {
                    // a sibling of parentStateId was created instead of a
                    // child, we need to merge the changes
                    // in parentStateId to the new state so they refer to the
                    // same content.
                    // SE_state_merge applies changes to a parent state to
                    // create a new merged state.
                    // The new state is the child of the parent state with the
                    // changes of the second state.
                    // Both input states must have the same parent state.
                    // When a row has been changed in both parent and second
                    // states, the row from the changes state is used.
                    // The parent and changes states must be open or owned by
                    // the current user unless the current user is the ArcSDE
                    // DBA.
                    newState.merge(realParent.getId(), parentState.getId());
                }

                return newState;
            }
        });
    }

}
