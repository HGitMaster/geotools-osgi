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
 * Singleton factory that maintains a single {@link ArcSDEConnectionPool connection pool} per set of
 * {@link ArcSDEConnectionConfig connection parameters}.
 * 
 * @author Gabriel Roldan
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/unsupported/arcsde/datastore/src/main
 *         /java/org/geotools/arcsde/pool/ArcSDEConnectionPoolFactory.java $
 * @version $Id: ArcSDEConnectionPoolFactory.java 32195 2009-01-09 19:00:35Z groldan $
 * @deprecated sill here waiting for gce to switch to {@link SessionPoolFactory}
 */
public class ArcSDEConnectionPoolFactory {
    /** package logger */
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.arcsde.pool");

    /** singleton pool factory */
    private static final ArcSDEConnectionPoolFactory singleton = new ArcSDEConnectionPoolFactory();

    /**
     * Map{ArcSDEConnectionConfig,ArcSDEConnectionPool} with per config connection pool
     */
    private final Map currentPools = new HashMap();

    /**
     * Creates a new SdeConnectionPoolFactory object.
     */
    private ArcSDEConnectionPoolFactory() {
        // intentionally blank
    }

    /**
     * Returns a connection pool factory instance
     * 
     * @return the connection pool factory singleton
     */
    public synchronized static ArcSDEConnectionPoolFactory getInstance() {
        return singleton;
    }

    /**
     * Creates a connection pool factory for the given connection parameters, or returns the
     * existing one if there already exists one for that set of connection params.
     * 
     * @param config
     *             contains the connection parameters and pool preferences
     * 
     * @return a pool for the given connection parameters, wether it already existed or had to be
     *         created.
     * 
     * @throws DataSourceException
     *             if the pool needs but can't be created
     */
    public synchronized ArcSDEConnectionPool createPool(ArcSDEConnectionConfig config)
            throws DataSourceException {
        ArcSDEConnectionPool pool = (ArcSDEConnectionPool) this.currentPools.get(config);

        if (pool == null) {
            // the new pool will be populated with config.minConnections
            // connections
            pool = new ArcSDEConnectionPool(config);
            this.currentPools.put(config, pool);
        }

        return pool;
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
            ((ArcSDEConnectionPool) it.next()).close();
        }
    }

    /**
     * Ensures proper closure of connection pools at this object's finalization stage.
     */
    // //@Override
    protected void finalize() {
        closeAll();
    }
}
