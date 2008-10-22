/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.hsql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.geotools.data.DataSourceException;
import org.geotools.data.Diff;
import org.geotools.data.DiffFeatureReader;
import org.geotools.data.DiffFeatureWriter;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.TransactionStateDiff;
import org.geotools.data.Transaction.State;
import org.geotools.data.jdbc.JDBCTransactionState;
import org.geotools.feature.IllegalAttributeException;

import org.geotools.filter.Filter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Envelope;


/**
 * A Transaction.State that keeps a difference table for use with
 * HsqlDataStore. This is a rip-off of TransactionStateDiff which 
 * can't be used here due to its dependence on AbstractDataStore.
 * 
 * One example of a difference is needing to extend JDBCTransactionState
 * so that the JDBC1DataStore is happy.
 *
 * @author Jody Garnett, Refractions Research
 * @author Amr Alam, Refractions Research
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/hsql/src/main/java/org/geotools/data/hsql/HsqlTransactionStateDiff.java $
 */
public class HsqlTransactionStateDiff extends JDBCTransactionState implements State {
    /**
     * DataStore used to commit() results of this transaction.
     *
     * @see TransactionStateDiff.commit();
     */
    HsqlDataStore store;

    /** Tranasction this State is opperating against. */
    Transaction transaction;

    /**
     * Map of differences by typeName.
     * 
     * <p>
     * Differences are stored as a Map of Feature by fid, and are reset during
     * a commit() or rollback().
     * </p>
     */
    Map typeNameDiff = new HashMap();

    public HsqlTransactionStateDiff(HsqlDataStore dataStore, 
    		Connection connection) throws IOException {
    	super(connection);
        store = dataStore;
    }

    public synchronized void setTransaction(Transaction transaction) {
        if (transaction != null) {
            // configure
            this.transaction = transaction;
        } else {
            this.transaction = null;

            if (typeNameDiff != null) {
                for (Iterator i = typeNameDiff.values().iterator();
                        i.hasNext();) {
                	Diff diff = (Diff) i.next();
                    diff.clear();
                }

                typeNameDiff.clear();
            }

            store = null;
        }
    }

    public synchronized Diff diff(String typeName) throws IOException {
        if (!exists(typeName)) {
            throw new IOException(typeName + " not defined");
        }

        if (typeNameDiff.containsKey(typeName)) {
            return (Diff) typeNameDiff.get(typeName);
        } else {
        	Diff diff = new Diff();
            typeNameDiff.put(typeName, diff);

            return diff;
        }
    }
    
    boolean exists(String typeName) {
        String[] types;
        try {
            types = store.getTypeNames();
        } catch (IOException e) {
            return false;
        }
        Arrays.sort(types);

        return Arrays.binarySearch(types, typeName) != -1;
    }

    /**
     * @see org.geotools.data.Transaction.State#addAuthorization(java.lang.String)
     */
    public synchronized void addAuthorization(String AuthID)
        throws IOException {
        // not required for TransactionStateDiff
    }

    /**
     * Will apply differences to store.
     *
     * @see org.geotools.data.Transaction.State#commit()
     */
    public synchronized void commit() throws IOException {
        Map.Entry entry;

        for (Iterator i = typeNameDiff.entrySet().iterator(); i.hasNext();) {
            entry = (Entry) i.next();

            String typeName = (String) entry.getKey();
            Diff diff = (Diff) entry.getValue();
            applyDiff(typeName, diff);
        }
    }

    /**
     * Called by commit() to apply one set of diff
     * 
     * <p>
     * diff will be modified as the differneces are applied, If the opperations
     * is successful diff will be empty at the end of this process.
     * </p>
     * 
     * <p>
     * diff can be used to represent the following operations:
     * </p>
     * 
     * <ul>
     * <li>
     * fid|null: represents a fid being removed
     * </li>
     * <li>
     * fid|feature: where fid exists, represents feature modification
     * </li>
     * <li>
     * fid|feature: where fid does not exist, represents feature being modified
     * </li>
     * </ul>
     * 
     *
     * @param typeName typeName being updated
     * @param diff differences to apply to FeatureWriter
     *
     * @throws IOException If the entire diff cannot be writen out
     * @throws DataSourceException If the entire diff cannot be writen out
     */
    void applyDiff(String typeName, Diff diff) throws IOException {
        if (diff.isEmpty()) {
            return;
        }
        FeatureWriter writer;
		try{
        	writer = store.getFeatureWriter(typeName);
        }catch (UnsupportedOperationException e) {
			// backwards compatibility
        	writer = store.getFeatureWriter(typeName);
		}
        SimpleFeature feature;
        SimpleFeature update;
        String fid;

        try {
            while (writer.hasNext()) {
                feature = (SimpleFeature)writer.next();
                fid = feature.getID();

                if (diff.modified2.containsKey(fid)) {
                    update = (SimpleFeature) diff.modified2.get(fid);

                    if (update == TransactionStateDiff.NULL) {
                        writer.remove();

                        // notify
                        store.listenerManager.fireFeaturesRemoved(typeName,
                            transaction, ReferencedEnvelope.reference(feature.getBounds()), true);
                    } else {
                        try {
                            feature.setAttributes(update.getAttributes());
                            writer.write();

                            // notify                        
                            ReferencedEnvelope bounds = new ReferencedEnvelope();
                            bounds.include(feature.getBounds());
                            bounds.include(update.getBounds());
                            store.listenerManager.fireFeaturesChanged(typeName,
                                transaction, bounds, true);
                        } catch (IllegalAttributeException e) {
                            throw new DataSourceException("Could update " + fid,
                                e);
                        }
                    }
                }
            }

            SimpleFeature addedFeature;
            SimpleFeature nextFeature;

            for (Iterator i = diff.added.values().iterator(); i.hasNext();) {
                addedFeature = (SimpleFeature) i.next();

                fid = addedFeature.getID();

                nextFeature = (SimpleFeature)writer.next();

                if (nextFeature == null) {
                    throw new DataSourceException("Could not add " + fid);
                } else {
                    try {
                        nextFeature.setAttributes(addedFeature
                            .getAttributes());
                        writer.write();

                        // notify                        
                        store.listenerManager.fireFeaturesAdded(typeName,
                            transaction, ReferencedEnvelope.reference(nextFeature.getBounds()), true);
                    } catch (IllegalAttributeException e) {
                        throw new DataSourceException("Could update " + fid,
                            e);
                    }
                }
            }
        } finally {
            writer.close();
            store.listenerManager.fireChanged(typeName, transaction, true);
            diff.clear();
        }
    }

    /**
     * @see org.geotools.data.Transaction.State#rollback()
     */
    public synchronized void rollback() throws IOException {
        Map.Entry entry;

        for (Iterator i = typeNameDiff.entrySet().iterator(); i.hasNext();) {
            entry = (Entry) i.next();

            String typeName = (String) entry.getKey();
            Diff diff = (Diff) entry.getValue();

            diff.clear(); // rollback differences
            store.listenerManager.fireChanged(typeName, transaction, false);
        }
    }

    /**
     * Convience Method for a Transaction based FeatureReader.
     * 
     * <p>
     * Constructs a DiffFeatureReader that works against this Transaction.
     * </p>
     *
     * @param typeName TypeName to aquire a Reader on
     *
     * @return FeatureReader the mask orgional contents with against the
     *         current Differences recorded by the Tansasction State
     *
     * @throws IOException If typeName is not Manged by this Tansaction State
     */
    public synchronized FeatureReader reader(String typeName)
        throws IOException {
    	Diff diff = diff(typeName);
        FeatureReader reader = store.getFeatureReader(typeName);

        return new DiffFeatureReader(reader, diff);
    }

    /**
     * Convience Method for a Transaction based FeatureWriter
     * 
     * <p>
     * Constructs a DiffFeatureWriter that works against this Transaction.
     * </p>
     *
     * @param typeName Type Name to record differences against
     *
     * @return A FeatureWriter that records Differences against a FeatureReader
     *
     * @throws IOException If a FeatureRader could not be constucted to record
     *         differences against
     */
    public synchronized FeatureWriter writer(final String typeName, Filter filter)
        throws IOException {
    	Diff diff = diff(typeName);
        SimpleFeatureType schema = store.getSchema(typeName);
        
        FeatureReader reader = store.getFeatureReader(schema, filter, transaction);

        return new DiffFeatureWriter(reader, diff) {
                public void fireNotification(int eventType, ReferencedEnvelope bounds) {
                    switch (eventType) {
                    case FeatureEvent.FEATURES_ADDED:
                        store.listenerManager.fireFeaturesAdded(typeName,
                            transaction, bounds, false);

                        break;

                    case FeatureEvent.FEATURES_CHANGED:
                        store.listenerManager.fireFeaturesChanged(typeName,
                            transaction, bounds, false);

                        break;

                    case FeatureEvent.FEATURES_REMOVED:
                        store.listenerManager.fireFeaturesRemoved(typeName,
                            transaction, bounds, false);

                        break;
                    }
                }
            };
    }
    
    public Connection getConnection() {
    	try {
			return store.createConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		}
    }
}
