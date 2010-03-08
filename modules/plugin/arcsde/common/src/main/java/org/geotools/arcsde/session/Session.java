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
package org.geotools.arcsde.session;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.ArcSdeException;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeDBMSInfo;
import com.esri.sde.sdk.client.SeDelete;
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
import com.esri.sde.sdk.geom.GeometryFactory;

/**
 * Provides thread safe access to an SeConnection.
 * <p>
 * This class has become more and more magic over time! It no longer represents a Connection but
 * provides "safe" access to a connection.
 * <p>
 * 
 * @author Gabriel Roldan
 * @author Jody Garnett
 * @version $Id: Session.java 34789 2010-01-13 16:44:32Z groldan $
 * @since 2.3.x
 */
class Session implements ISession {

    public static final Logger LOGGER;

    static {
        /*
         * This Jar may be used withoug geotools' gt-metadata being in the class path, so try to use
         * the org.geotools.util.logging.Logging.getLogger method reflectively and fall back to
         * plain java.util.logger if that's the case
         */
        Logger logger = null;
        try {
            Class<?> clazz = Class.forName("org.geotools.util.logging.Logging");
            Method method = clazz.getMethod("getLogger", String.class);
            logger = (Logger) method.invoke(null, "org.geotools.arcsde.session");
        } catch (Exception e) {
            logger = Logger.getLogger("org.geotools.arcsde.session");
            logger.info("org.geotools.util.logging.Logging seems not to be in the classpath, "
                    + "acquired Logger through java.util.Logger");
        }
        LOGGER = logger;
    }

    /**
     * How many seconds must have elapsed since the last connection round trip to the server for
     * {@link #testServer()} to actually check the connection's validity
     */
    protected static final long TEST_SERVER_ROUNDTRIP_INTERVAL_SECONDS = 5;

    /** Actual SeConnection being protected */
    private final SeConnection connection;

    /**
     * SessionPool used to manage open connections (shared).
     */
    private final SessionPool pool;

    private final ArcSDEConnectionConfig config;

    /**
     * Used to assign unique ids to each new session
     */
    private static final AtomicInteger sessionCounter = new AtomicInteger();

    /**
     * Global unique id for this session
     */
    private final int sessionId;

    private boolean transactionInProgress;

    private boolean isPassivated;

    private Map<String, SeTable> cachedTables = new WeakHashMap<String, SeTable>();

    private Map<String, SeLayer> cachedLayers = new WeakHashMap<String, SeLayer>();

    /**
     * The SeConnection bound task executor, ensures all operations against a given connection are
     * performed in the same thread regardless of the thread the {@link #issue(Command)} is being
     * called from.
     */
    //private final ExecutorService taskExecutor;

    /**
     * Thread used by the taskExecutor; so we can detect recursion.
     */
    private Thread commandThread;

    /**
     * Keeps track of the number of references to this session (ie, how many times it has been
     * {@link #markActive() activated} so it's only actually {@link #dispose() disposed} when the
     * reference count gets down to zero.
     */
    private final AtomicInteger referenceCounter = new AtomicInteger();

    /**
     * Executes a {@link Command} inside the Session's worker thread
     */
    private final class SessionTask<T> implements Callable<T> {
        private final Command<T> command;

        private SessionTask(Command<T> command) {
            this.command = command;
        }

        /**
         * Executes a {@link Command} inside the Session's worker thread
         * 
         * @see java.util.concurrent.Callable#call()
         * @see Session#issue(Command)
         */
        public T call() throws Exception {
            final Thread currentThread = Thread.currentThread();

            if (commandThread != currentThread) {
                LOGGER.fine("updating command thread from " + commandThread + " to "
                        + currentThread);
                commandThread = currentThread;

            }
            if (currentThread != commandThread) {
                throw new IllegalStateException("currentThread != commandThread");
            }
            try {
                return command.execute(Session.this, connection);
            } catch (Exception e) {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, "Command execution failed for Session "
                            + Session.this.sessionId + " in thread " + currentThread.getId(), e);
                } else if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Command execution failed for Session " + Session.this.sessionId
                            + " in thread " + currentThread.getId());
                }

                if (e instanceof SeException) {
                    throw new ArcSdeException((SeException) e);
                } else if (e instanceof IOException) {
                    throw e;
                }
                throw new RuntimeException("Command execution failed for Session "
                        + Session.this.sessionId + " in thread " + currentThread.getId(), e);
            }
        }
    }

    /**
     * A custom {@link ThreadFactory} for the Session's {@link ExecutorService} for the sole reason
     * of giving threads a significative name (unvaluable when debugging/profiling)
     */
    private static class SessionThreadFactory implements ThreadFactory {

        private final int sessionId;

        private static final ThreadGroup group = new ThreadGroup("ArcSDE Session threads");

        public SessionThreadFactory(final int sessionId) {
            this.sessionId = sessionId;
        }

        /**
         * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
         */
        public Thread newThread(final Runnable r) {
            Thread t = new Thread(group, r, "ArcSDE Session " + sessionId);
            t.setDaemon(true);
            return t;
        }
    }

    /**
     * Provides safe access to an SeConnection.
     * 
     * @param pool
     *            SessionPool used to manage SeConnection
     * @param config
     *            Used to set up a SeConnection
     * @throws SeException
     *             If we cannot connect
     */
    Session(final SessionPool pool, final ArcSDEConnectionConfig config) throws IOException {
        this.sessionId = sessionCounter.incrementAndGet();
        this.config = config;
        this.pool = pool;
        //this.taskExecutor = Executors.newSingleThreadExecutor(new SessionThreadFactory(sessionId));

        // grab command thread, held by taskExecutor
        updateCommandThread();

        /*
         * This ensures the connection runs always on the same thread. Will fail if its accessed by
         * different threads
         */
        final CreateSeConnectionCommand connectionCommand;
        connectionCommand = new CreateSeConnectionCommand(config, sessionId);
        try {
            this.connection = issue(connectionCommand);
        } catch (IOException e) {
            // make sure a connection creation failure does not leave a stale thread
            //this.taskExecutor.shutdownNow();
            throw e;
        } catch (RuntimeException shouldntHappen) {
            //this.taskExecutor.shutdownNow();
            throw shouldntHappen;
        }
    }

    /**
     * @see ISession#issue(org.geotools.arcsde.session.Command)
     */
    public synchronized <T> T issue(final Command<T> command) throws IOException {
        try {
            if (connection == null) {
                return command.execute(this, null);
            } else {
                return command.execute(this, connection);
            }
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
//        final Thread callingThread = Thread.currentThread();
//        if (callingThread == commandThread) {
//            // Called command inside command
//            try {
//                return command.execute(this, connection);
//            } catch (SeException e) {
//                Throwable cause = e.getCause();
//                if (cause instanceof IOException) {
//                    throw (IOException) cause;
//                }
//                throw new ArcSdeException(e);
//            }
//        } else {
//            final SessionTask<T> sessionTask = new SessionTask<T>(command);
//            final Future<T> task = taskExecutor.submit(sessionTask);
//            T result;
//            try {
//                result = task.get();
//            } catch (InterruptedException e) {
//                updateCommandThread();
//                throw new RuntimeException("Command execution abruptly interrupted", e);
//            } catch (ExecutionException e) {
//                updateCommandThread();
//                Throwable cause = e.getCause();
//                if (cause instanceof IOException) {
//                    throw (IOException) cause;
//                } else if (cause instanceof SeException) {
//                    throw new ArcSdeException((SeException) cause);
//                }
//                throw (IOException) new IOException().initCause(cause);
//            }
//            return result;
//        }
    }

    private void updateCommandThread() {
        final Callable<Object> task = new Callable<Object>() {
            public Object call() throws Exception {
                final Thread currentThread = Thread.currentThread();
                if (currentThread != commandThread) {
                    LOGGER.fine("updating command thread from " + commandThread + " to "
                            + currentThread);
                    commandThread = currentThread;
                }
                return null;
            }
        };
        // used to detect when thread has been
        // restarted after error
        // and block until task is executed
//        try {
//            taskExecutor.submit(task).get();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        }
    }

    /**
     * @see ISession#testServer()
     */
    public final void testServer() throws IOException {
        /*
         * This method is called often (every time a session is to be returned from the pool) to
         * check if it's still valid. We can call getTimeSinceLastRT safely since it does not
         * require a server roundtrip and hence there's no risk of violating thread safety. So we do
         * it before issuing the command to avoid the perf penalty imposed by running the command if
         * not needed.
         */
        final long secondsSinceLastServerRoundTrip = this.connection.getTimeSinceLastRT();

        if (TEST_SERVER_ROUNDTRIP_INTERVAL_SECONDS < secondsSinceLastServerRoundTrip) {
            issue(Commands.TEST_SERVER_COMMAND);
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
     * @see #isPassivated
     * @see #checkActive()
     */
    void markActive() {
        referenceCounter.incrementAndGet();
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
        if (referenceCounter.get() != 0) {
            throw new IllegalStateException("referenceCount = " + referenceCounter);
        }
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
        if (!cachedLayers.containsKey(layerName)) {
            issue(new Command<Void>() {
                @Override
                public Void execute(final ISession session, final SeConnection connection)
                        throws SeException, IOException {
                    synchronized (cachedLayers) {
                        if (!cachedLayers.containsKey(layerName)) {
                            SeTable table = getTable(layerName);
                            String shapeColumn = getShapeColumn(table);
                            if (shapeColumn == null) {
                                return null;
                            }
                            SeLayer layer = new SeLayer(connection, layerName, shapeColumn);
                            cachedLayers.put(layerName, layer);
                        }
                    }
                    return null;
                }
            });
        }

        SeLayer seLayer = cachedLayers.get(layerName);
        if (seLayer == null) {
            throw new NoSuchElementException("Layer '" + layerName + "' not found");
        }
        return seLayer;

    }

    private String getShapeColumn(SeTable table) throws ArcSdeException {
        try {
            for (SeColumnDefinition aDef : table.describe()) {
                if (aDef.getType() == SeColumnDefinition.TYPE_SHAPE) {
                    return aDef.getName();
                }
            }
        } catch (SeException e) {
            throw new ArcSdeException("Exception describing table " + table.getName(), e);
        }
        return null;
    }

    /**
     * @see ISession#getRasterColumn(java.lang.String)
     */
    public synchronized SeRasterColumn getRasterColumn(final String rasterName) throws IOException {
        throw new UnsupportedOperationException("Waiting for a proper implementation");
    }

    /**
     * @see org.geotools.arcsde.session.ISession#getRasterColumns()
     */
    public List<String> getRasterColumns() throws IOException {
        checkActive();
        List<String> rasterNames = issue(Commands.GetRasterColumnNamesCommand);
        return rasterNames;
    }

    /**
     * @see ISession#getTable(java.lang.String)
     */
    public SeTable getTable(final String tableName) throws IOException {
        checkActive();
        if (!cachedTables.containsKey(tableName)) {
            issue(new Command<Void>() {
                @Override
                public Void execute(final ISession session, final SeConnection connection)
                        throws SeException, IOException {
                    synchronized (cachedTables) {
                        if (!cachedTables.containsKey(tableName)) {
                            SeTable table = new SeTable(connection, tableName);
                            try {
                                table.describe();
                            } catch (SeException e) {
                                throw new NoSuchElementException("Table '" + tableName
                                        + "' not found");
                            }
                            cachedTables.put(tableName, table);
                        }
                    }
                    return null;
                }
            });
        }

        SeTable seTable = (SeTable) cachedTables.get(tableName);
        if (seTable == null) {
            throw new NoSuchElementException("Table '" + tableName + "' not found");
        }
        return seTable;
    }

    /**
     * @see ISession#startTransaction()
     */
    public void startTransaction() throws IOException {
        checkActive();
        issue(Commands.StartTransactionCommand);
        transactionInProgress = true;
    }

    /**
     * @see ISession#commitTransaction()
     */
    public void commitTransaction() throws IOException {
        checkActive();
        issue(Commands.CommitTransactionCommand);
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
            issue(Commands.RollbackTransactionCommand);
        } finally {
            transactionInProgress = false;
        }
    }

    /**
     * @see ISession#dispose()
     */
    public void dispose() throws IllegalStateException {
        checkActive();
        final int refCount = referenceCounter.decrementAndGet();

        if (refCount > 0) {
            // ignore
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("---------> Ignoring disposal, ref count is still " + refCount
                        + " for " + this);
            }

            // System.err.println("---------> Ignoring disposal, ref count is still " + refCount
            // + " for " + this);
            return;
        }

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("  -> RefCount is " + refCount + ". Disposing " + this);
        }
        if (transactionInProgress) {
            throw new IllegalStateException(
                    "Transaction is in progress, should commit or rollback before closing");
        }
        try {
            // System.err.println("---------> Disposing " + this + " on thread " +
            // Thread.currentThread().getName());
            this.pool.returnObject(this);
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
            issue(Commands.CloseConnectionCommand);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "closing connection " + toString(), e);
        } finally {
            //taskExecutor.shutdown();
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
    public List<SeLayer> getLayers() throws IOException {
        return issue(Commands.GetLayersCommand);
    }

    /**
     * @see ISession#getUser()
     */
    public String getUser() throws IOException {
        return issue(Commands.GetUserCommand);
    }

    /**
     * @see ISession#getRelease()
     */
    public SeRelease getRelease() throws IOException {
        return issue(Commands.GetReleaseCommand);
    }

    /**
     * @see ISession#getDatabaseName()
     */
    public String getDatabaseName() throws IOException {
        return issue(Commands.GetDatabaseNameCommand);
    }

    /**
     * @see ISession#getDBMSInfo()
     */
    public SeDBMSInfo getDBMSInfo() throws IOException {
        return issue(Commands.getDBMSInfoCommand);
    }

    /**
     * @see ISession#createSeRegistration(java.lang.String)
     */
    public SeRegistration createSeRegistration(final String typeName) throws IOException {
        return issue(new Commands.CreateSeRegistrationCommand(typeName));
    }

    /**
     * @see ISession#createSeTable(java.lang.String)
     */
    public SeTable createSeTable(final String qualifiedName) throws IOException {
        return issue(new Commands.CreateSeTableCommand(qualifiedName));
    }

    /**
     * @see ISession#createSeInsert()
     */
    public SeInsert createSeInsert() throws IOException {
        return issue(Commands.CreateSeInsertCommand);
    }

    /**
     * @see ISession#createSeUpdate()
     */
    public SeUpdate createSeUpdate() throws IOException {
        return issue(Commands.CreateSeUpdateCommand);
    }

    /**
     * @see ISession#createSeDelete()
     */
    public SeDelete createSeDelete() throws IOException {
        return issue(Commands.CreateSeDeleteCommand);
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
        return issue(new Commands.DescribeTableCommand(table));
    }

    /**
     * @see ISession#fetch(com.esri.sde.sdk.client.SeQuery)
     */
    public SdeRow fetch(final SeQuery query) throws IOException {
        return fetch(query, new SdeRow((GeometryFactory) null));
    }

    public SdeRow fetch(final SeQuery query, final SdeRow currentRow) throws IOException {
        return issue(new Command<SdeRow>() {
            @Override
            public SdeRow execute(final ISession session, final SeConnection connection)
                    throws SeException, IOException {
                SeRow row = query.fetch();
                if (row == null) {
                    return null;
                } else {
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
        issue(new Commands.CloseStateCommand(state));
    }

    /**
     * @see ISession#close(com.esri.sde.sdk.client.SeStreamOp)
     */
    public void close(final SeStreamOp stream) throws IOException {
        issue(new Commands.CloseStreamCommand(stream));
    }

    /**
     * @see ISession#createState(com.esri.sde.sdk.client.SeObjectId)
     */
    public SeState createState(final SeObjectId stateId) throws IOException {
        return issue(new Commands.CreateSeStateCommand(stateId));
    }

    /**
     * @see ISession#createAndExecuteQuery(java.lang.String[],
     *      com.esri.sde.sdk.client.SeSqlConstruct)
     */
    public SeQuery createAndExecuteQuery(final String[] propertyNames, final SeSqlConstruct sql)
            throws IOException {
        return issue(new Commands.CreateAndExecuteQueryCommand(propertyNames, sql));
    }

    /**
     * Creates either a direct child state of parentStateId, or a sibling being an exact copy of
     * parentStatId if either the state can't be closed because its in use or parentStateId does not
     * belong to the current user.
     */
    public SeState createChildState(final long parentStateId) throws IOException {
        return issue(new Commands.CreateVersionStateCommand(parentStateId));
    }

    private static final class CreateSeConnectionCommand extends Command<SeConnection> {
        private final ArcSDEConnectionConfig config;

        private final int sessionId;

        /**
         * 
         * @param config
         * @param sessionId
         *            the session id the connection is to be created for. For exception reporting
         *            purposes only
         */
        private CreateSeConnectionCommand(final ArcSDEConnectionConfig config, final int sessionId) {
            this.config = config;
            this.sessionId = sessionId;
        }

        @Override
        public SeConnection execute(final ISession session, final SeConnection connection)
                throws SeException, IOException {
            final String serverName = config.getServerName();
            final int portNumber = config.getPortNumber();
            final String databaseName = config.getDatabaseName();
            final String userName = config.getUserName();
            final String userPassword = config.getPassword();

            NegativeArraySizeException cause = null;
            SeConnection conn = null;
            try {
                for (int i = 0; i < 3; i++) {
                    try {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine("Creating connection for session #" + sessionId + "(try "
                                    + (i + 1) + " of 3)");
                        }
                        conn = new SeConnection(serverName, portNumber, databaseName, userName,
                                userPassword);
                        //conn.setConcurrency(SeConnection.SE_ONE_THREAD_POLICY);

                        // SeStreamSpec streamSpec = new SeStreamSpec();
                        // streamSpec.setRasterBufSize(2*128*128);
                        // conn.setStreamSpec(streamSpec);
                        break;
                    } catch (NegativeArraySizeException nase) {
                        LOGGER.warning("Strange failed ArcSDE connection error.  "
                                + "Trying again (try " + (i + 1) + " of 3). SessionId: "
                                + sessionId);
                        cause = nase;
                    }
                }
            } catch (SeException e) {
                throw new ArcSdeException("Can't create connection to " + serverName
                        + " for Session #" + sessionId, e);
            } catch (RuntimeException e) {
                throw (IOException) new IOException("Can't create connection to " + serverName
                        + " for Session #" + sessionId).initCause(e);
            }

            if (cause != null) {
                throw (IOException) new IOException("Couldn't create ArcSDE connection to "
                        + serverName + " for Session #" + sessionId
                        + " because of strange SDE internal exception. "
                        + " Tried 3 times, giving up.").initCause(cause);
            }
            return conn;
        }
    }
}
