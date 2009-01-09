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

import org.geotools.arcsde.pool.Command;
import org.geotools.arcsde.pool.ISession;
import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeState;
import com.esri.sde.sdk.client.SeStreamOp;
import com.esri.sde.sdk.client.SeVersion;

/**
 * Handles a versioned table when in auto commit mode, meaning it sets up streams to edit directly
 * the default version.
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id: AutoCommitDefaultVersionHandler.java 32195 2009-01-09 19:00:35Z groldan $
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/trunk/modules/plugin/arcsde/datastore/src/main/java/org/geotools
 *         /arcsde/data/versioning/AutoCommitDefaultVersionHandler.java $
 */
public class AutoCommitDefaultVersionHandler implements ArcSdeVersionHandler {

    private static final Logger LOGGER = Logging.getLogger(AutoCommitDefaultVersionHandler.class
            .getName());

    private SeVersion defaultVersion;

    public AutoCommitDefaultVersionHandler() throws IOException {
        //
    }

    public void setUpStream(final ISession session, final SeStreamOp streamOperation)
            throws IOException {

        session.issue(new Command<Void>() {
            @Override
            public Void execute(ISession session, SeConnection connection) throws SeException,
                    IOException {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("setting up stream for default version in "
                            + streamOperation.getClass().getName());
                }

                if (defaultVersion == null) {
                    LOGGER.finer("Acquiring default version");
                    defaultVersion = new SeVersion(connection,
                            SeVersion.SE_QUALIFIED_DEFAULT_VERSION_NAME);
                }

                LOGGER.finest("Refreshing default version info");
                defaultVersion.getInfo();

                if (!(streamOperation instanceof SeQuery)) {
                    LOGGER.finer("StreamOp is not query, verifying an SeState can be used");

                    // create a new state for the operation only if its not a
                    // simple query
                    final SeState currentState;
                    final SeObjectId currStateId = defaultVersion.getStateId();
                    currentState = new SeState(connection, currStateId);

                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.finer("Default Version state: " + currStateId.longValue()
                                + ", parent: " + currentState.getParentId().longValue()
                                + ", open: " + currentState.isOpen() + ", owner: "
                                + currentState.getOwner() + ", current user: "
                                + connection.getUser());
                    }

                    final String currUser = connection.getUser();
                    final String stateOwner = currentState.getOwner();

                    if (currentState.isOpen() && currUser.equals(stateOwner)) {
                        LOGGER.finer("Default version state is open and belongs "
                                + "to the current user, using it as is");
                    } else {
                        LOGGER.finer("Creating new state for the operation");
                        SeState newState = session.createChildState(currStateId.longValue());
                        LOGGER.finer("Setting default version to new state "
                                + newState.getId().longValue());
                        defaultVersion.changeState(newState.getId());
                    }
                }

                SeObjectId differencesId = new SeObjectId(SeState.SE_NULL_STATE_ID);
                // defaultVersion.getInfo();
                SeObjectId currentStateId = defaultVersion.getStateId();
                streamOperation.setState(currentStateId, differencesId,
                        SeState.SE_STATE_DIFF_NOCHECK);
                return null;
            }
        });
    }

    public void editOperationWritten(SeStreamOp editOperation) throws IOException {
        //
    }

    public void editOperationFailed(SeStreamOp editOperation) throws IOException {
        //
    }

    /**
     * This method should not be called, but {@link #editOperationFailed(SeStreamOp)} instead, as
     * this is a handler for auto commit mode
     * 
     * @throws UnsupportedOperationException
     * @see {@link ArcSdeVersionHandler#rollbackEditState()}
     */
    public void commitEditState() throws IOException {
        throw new UnsupportedOperationException("commit shouldn't be called for"
                + " an autocommit versioning handler ");
    }

    /**
     * This method should not be called, but {@link #editOperationWritten(SeStreamOp)} instead, as
     * this is a handler for auto commit mode
     * 
     * @throws UnsupportedOperationException
     * @see {@link ArcSdeVersionHandler#rollbackEditState()}
     */
    public void rollbackEditState() throws IOException {
        throw new UnsupportedOperationException("rollback shouldn't be called for"
                + " an autocommit versioning handler ");
    }

}
