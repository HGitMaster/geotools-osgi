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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;

/**
 * Singleton factory that maintains a single {@link SessionPool connection pool} per set of
 * {@link ArcSDEConnectionConfig connection parameters}.
 * 
 * @author Gabriel Roldan
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/unsupported/arcsde/datastore/src/main/java/org/geotools/arcsde/pool/SessionPoolFactory.java $
 * @version $Id: SessionPoolFactory.java 30722 2008-06-13 18:15:42Z acuster $
 */
public class SessionPoolFactory {
    /** package logger */
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.arcsde.pool");

    /** singleton pool factory */
    private static final SessionPoolFactory singleton = new SessionPoolFactory();

    /**
     * Map{ArcSDEConnectionConfig,SessionPool} with per config connection pool
     */
    private final Map currentPools = new HashMap();

    /**
     * Creates a new SdeConnectionPoolFactory object.
     */
    private SessionPoolFactory() {
        // intentionally blank
    }

    /**
     * Returns a connection pool factory instance
     * 
     * @return the connection pool factory singleton
     */
    public synchronized static SessionPoolFactory getInstance() {
        return singleton;
    }

    /**
     * Creates a connection pool factory for the given connection parameters, or returns the
     * existing one if there already exists one for that set of connection params.
     * 
     * @param config  contains the connection parameters and pool preferences
     * @return a pool for the given connection parameters, wether it already existed or had to be
     *         created.
     * @throws DataSourceException if the pool needs but can't be created
     */
    public synchronized SessionPool createSharedPool(ArcSDEConnectionConfig config)
            throws DataSourceException {
        SessionPool pool = (SessionPool) this.currentPools.get(config);

        if (pool == null) {
            // the new pool will be populated with config.minConnections
            // connections
            if (config.getMaxConnections() != null && config.getMaxConnections() == 1) {
                // engage experimental single connection mode!
                pool = new ArcSDEConnectionReference(config);
            } else {
                pool = new SessionPool(config);
            }
            this.currentPools.put(config, pool);
        }

        return pool;
    }

    /**
     * Creates a _new_ session pool.
     * 
     * @param config
     * @return
     * @throws DataSourceException
     */
    public SessionPool createPool(ArcSDEConnectionConfig config) throws DataSourceException {
        return new SessionPool(config);
    }

    /**
     * Closes and removes all the existing connection pools
     */
    public void clear() {
        closeAll();
        this.currentPools.clear();
        LOGGER.fine("sde connection pools creared");
    }

    /**
     * loses all the available connection pools
     */
    private void closeAll() {
        for (Iterator it = this.currentPools.values().iterator(); it.hasNext();) {
            ((SessionPool) it.next()).close();
        }
    }

    /**
     * Ensures proper closure of connection pools at this object's finalization stage.
     */
    @Override
    protected void finalize() {
        closeAll();
    }
}
