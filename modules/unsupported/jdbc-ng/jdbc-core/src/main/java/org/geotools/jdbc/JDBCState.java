/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.jdbc;

import java.sql.Connection;
import java.util.logging.Logger;

import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentState;


/**
 * State for jdbc datastore providing additional cached values such as primary
 * key and database connection.
 *
 * @author Jody Garnett, Refractions Research Inc.
 * @author Justin Deoliveira, The Open Planning Project
 */
public final class JDBCState extends ContentState {
    /**
     * cached database connection
     */
    private Connection connection;

    /**
     * cached primary key
     */
    private PrimaryKey primaryKey;

    /**
     * Creates the state from an existing one.
     */
    public JDBCState(JDBCState state) {
        super(state);

        //copy the primary key
        primaryKey = state.getPrimaryKey();

        //do not copy the connection
        //connection = state.getConnection();
    }

    /**
     * Creates a new state object.
     */
    public JDBCState(ContentEntry entry) {
        super(entry);
    }

    /**
     * The cached database connection.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Sets the cached database connection.
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * The cached primary key.
     */
    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    /**
     * Sets the cached primary key.
     * @param primaryKey
     */
    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * Flushes all cached state.
     */
    public void flush() {
        connection = null;
        primaryKey = null;
        super.flush();
    }

    /**
     * Copies the state.
     */
    public ContentState copy() {
        return new JDBCState(this);
    }

    /**
     * Closes the database connection with a call to
     * {@link JDBCDataStore#closeSafe(Connection)}.
     */
    public void close() {
        ((JDBCDataStore)entry.getDataStore()).closeSafe(connection);
        super.close();
    }
    
    @Override
    protected void finalize() throws Throwable {
        if ( connection != null && !connection.isClosed()) {
            Logger.getLogger( "org.geotools.jdbc").severe("State finalized with open connection.");
        }
    }
}
