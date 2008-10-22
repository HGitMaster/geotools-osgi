/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.caching.firstdraft;

import java.io.IOException;
import org.opengis.filter.Filter;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;


/**
 * @author  crousson
 */
public class DelayedDataStore implements DataStore {
    private final DataStore sourceDataStore;
    private long msResponseDelay;

    public DelayedDataStore(DataStore sourceDataStore) {
        this.sourceDataStore = sourceDataStore;
        this.msResponseDelay = 0;
    }

    public DelayedDataStore(DataStore sourceDataStore, long msResponseDelay) {
        this.sourceDataStore = sourceDataStore;
        this.msResponseDelay = msResponseDelay;
    }

    /**
     * @param msResponseDelay  the msResponseDelay to set
     * @uml.property  name="msResponseDelay"
     */
    public void setMsResponseDelay(long ms) {
        this.msResponseDelay = ms;
    }

    public long getMsReponseDelay() {
        return this.msResponseDelay;
    }

    private synchronized void idle() {
        try {
            this.wait(this.msResponseDelay);
        } catch (InterruptedException e) {
            // do nothing
            e.printStackTrace();
        }
    }

    public void createSchema(FeatureType arg0) throws IOException {
        this.sourceDataStore.createSchema(arg0);
    }

    public FeatureReader getFeatureReader(Query arg0, Transaction arg1)
        throws IOException {
        idle();

        return this.sourceDataStore.getFeatureReader(arg0, arg1);
    }

    public FeatureSource getFeatureSource(String arg0)
        throws IOException {
        idle();

        return this.sourceDataStore.getFeatureSource(arg0);
    }

    public FeatureWriter getFeatureWriter(String arg0, Transaction arg1)
        throws IOException {
        idle();

        return this.sourceDataStore.getFeatureWriter(arg0, arg1);
    }

    public FeatureWriter getFeatureWriter(String arg0, Filter arg1, Transaction arg2)
        throws IOException {
        idle();

        return this.sourceDataStore.getFeatureWriter(arg0, arg1, arg2);
    }

    public FeatureWriter getFeatureWriterAppend(String arg0, Transaction arg1)
        throws IOException {
        idle();

        return this.sourceDataStore.getFeatureWriterAppend(arg0, arg1);
    }

    public LockingManager getLockingManager() {
        return this.sourceDataStore.getLockingManager();
    }

    public FeatureType getSchema(String arg0) throws IOException {
        return this.sourceDataStore.getSchema(arg0);
    }

    public String[] getTypeNames() throws IOException {
        return this.sourceDataStore.getTypeNames();
    }

    public FeatureSource getView(Query arg0) throws IOException, SchemaException {
        idle();

        return this.sourceDataStore.getView(arg0);
    }

    public void updateSchema(String arg0, FeatureType arg1)
        throws IOException {
        this.sourceDataStore.updateSchema(arg0, arg1);
    }
}
