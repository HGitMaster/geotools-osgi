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
package org.geotools.arcsde.data.versioning;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.pool.Command;
import org.geotools.arcsde.pool.ISession;
import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeState;
import com.esri.sde.sdk.client.SeStreamOp;
import com.esri.sde.sdk.client.SeVersion;

/**
 * Handles a versioned table when in transaction mode
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id: TransactionDefaultVersionHandler.java 32195 2009-01-09 19:00:35Z groldan $
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java
 *         /org/geotools/arcsde/data/versioning/TransactionDefaultVersionHandler.java $
 */
public class TransactionDefaultVersionHandler implements ArcSdeVersionHandler {

    private static final Logger LOGGER = Logging.getLogger(TransactionDefaultVersionHandler.class
            .getName());

    private final ISession session;

    private final SeVersion defaultVersion;

    // private SeObjectId initialStateId;

    // private SeVersion thisTransactionVersion;

    /**
     * The state used for the transaction, its a sibling of the current state
     */
    private SeState transactionState;

    public TransactionDefaultVersionHandler(final ISession session) throws IOException {
        this.session = session;
        LOGGER.finest("Getting default version");
        defaultVersion = session.getDefaultVersion();
    }

    /**
     * Called by ArcSdeFeatureWriter.createStream
     * 
     * @see ArcSdeVersionHandler#
     */
    public void setUpStream(final ISession session, final SeStreamOp streamOperation)
            throws IOException {

        session.issue(new Command<Void>() {
            @Override
            public Void execute(ISession session, SeConnection connection) throws SeException,
                    IOException {

                LOGGER.finest("setting up stream for transaction on a versioned table");

                if (transactionState == null) {
                    LOGGER.finer("no transaction state created yet, about "
                            + "to create a new state for the transaction");
                    try {
                        LOGGER.finest("Refreshing default version info");
                        defaultVersion.getInfo();

                        LOGGER.finest("Getting default version state");
                        final SeState currentState = new SeState(connection, defaultVersion
                                .getStateId());
                        final long currentStateId = currentState.getId().longValue();

                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.finer("Default version state: " + currentStateId + ", open: "
                                    + currentState.isOpen() + ", owner: " + currentState.getOwner()
                                    + ", current user: " + connection.getUser());
                        }

                        LOGGER.finer("Creating new state for the transaction...");
                        transactionState = session.createChildState(currentStateId);

                        LOGGER.finer("New transaction state: " + transactionState.getId()
                                + ", parent: " + transactionState.getParentId().longValue());

                        LOGGER.finer("Locking the transaction state");
                        transactionState.lock();
                        LOGGER.finer("Transaction state locked");
                    } catch (SeException e) {
                        throw new ArcSdeException(e);
                    }
                }
                final SeObjectId differencesId = new SeObjectId(SeState.SE_NULL_STATE_ID);
                final SeObjectId currentStateId = transactionState.getId();
                streamOperation.setState(currentStateId, differencesId,
                        SeState.SE_STATE_DIFF_NOCHECK);
                return null;
            }
        });
    }

    /**
     * Not called at all
     * 
     * @see ArcSdeVersionHandler#editOperationWritten(SeStreamOp)
     */
    public void editOperationWritten(SeStreamOp editOperation) throws IOException {
        // intentionally blank
    }

    /**
     * Not called at all
     * 
     * @see ArcSdeVersionHandler#editOperationFailed(SeStreamOp)
     */
    public void editOperationFailed(SeStreamOp editOperation) throws IOException {
        // intentionally blank
    }

    /**
     * Called by ArcTransactionState.commit()
     * 
     * @see ArcSdeVersionHandler#commitEditState()
     */
    public void commitEditState() throws IOException {
        LOGGER.fine("Commiting versioned state");
        if (transactionState == null) {
            LOGGER.fine("Already commited, ignoring operation");
            return;
        }

        session.issue(new Command<Void>() {
            @Override
            public Void execute(ISession session, SeConnection connection) throws SeException,
                    IOException {
                SeObjectId transactionStateId = transactionState.getId();
                LOGGER.finer("Refreshing default version info");
                defaultVersion.getInfo();
                LOGGER.finer("Chaning default version state to point to transaction state "
                        + transactionStateId);
                defaultVersion.changeState(transactionStateId);

                // SeObjectId parentStateId = transactionState.getId();
                // transactionState.trimTree(parentStateId, transactionStateId);
                LOGGER.finer("Freeing transaction state lock");
                transactionState.freeLock();

                transactionState = null;
                return null;
            }
        });
    }

    /**
     * Called by ArcTransactionState.rollback()
     * 
     * @see ArcSdeVersionHandler#rollbackEditState()
     */
    public void rollbackEditState() throws IOException {
        LOGGER.finer("Rolling back versioned transaction state");
        if (transactionState == null) {
            LOGGER.finer("Already rolled back, ignoring operation");
            return;
        }
        session.issue(new Command<Void>() {

            @Override
            public Void execute(ISession session, SeConnection connection) throws SeException,
                    IOException {
                try {
                    LOGGER.finer("Releasing lock on transaction state "
                            + transactionState.getId().longValue());
                    transactionState.freeLock();
                } catch (SeException e) {
                    // no locked any more, ignore
                }
                LOGGER.finer("Deleting transaction state " + transactionState.getId().longValue());
                transactionState.delete();
                transactionState = null;

                // parent state is closed, create a new one for it
                // defaultVersion.getInfo();
                // final SeObjectId defaultVersionStateId =
                // defaultVersion.getStateId();
                // final SeState defaultVersionState = new SeState(connection,
                // defaultVersionStateId);
                // if (!defaultVersionState.isOpen()) {
                // // create a new open state as child of the current version
                // closed state
                // SeState newOpenState = new SeState(connection);
                // newOpenState.create(defaultVersionStateId);
                // final SeObjectId newStateId = newOpenState.getId();
                // defaultVersion.changeState(newStateId);
                // }
                return null;
            }
        });
    }

}
