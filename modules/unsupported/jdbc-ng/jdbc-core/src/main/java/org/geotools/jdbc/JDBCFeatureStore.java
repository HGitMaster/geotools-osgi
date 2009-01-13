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
package org.geotools.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.Transaction;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureStore;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;


/**
 * FeatureStore implementation for jdbc based relational database tables.
 * <p>
 * All read only methods are delegated to {@link JDBCFeatureSource}.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 */
public final class JDBCFeatureStore extends ContentFeatureStore {
    
    /**
     * jdbc feature source to delegate to, we do this b/c we can't inherit from
     * both ContentFeatureStore and JDBCFeatureSource at the same time
     */
    JDBCFeatureSource delegate;
    
    /**
     * Creates the new feature store.
     * @param entry The datastore entry.
     * @param query The defining query.
     */
    public JDBCFeatureStore(ContentEntry entry,Query query) throws IOException {
        super(entry,query);
        
        delegate = new JDBCFeatureSource( entry, query ) {
            @Override
            public void setTransaction(Transaction transaction) {
                super.setTransaction(transaction);
                
                //keep this feature store in sync
                JDBCFeatureStore.this.setTransaction(transaction);
            }
        };
    }

    @Override
    public JDBCDataStore getDataStore() {
        return delegate.getDataStore();
    }

    @Override
    public ContentEntry getEntry() {
        return delegate.getEntry();
    }

    @Override
    public ResourceInfo getInfo() {
        return delegate.getInfo();
    }

    @Override
    public Name getName() {
        return delegate.getName();
    }

    @Override
    public QueryCapabilities getQueryCapabilities() {
        return delegate.getQueryCapabilities();
    }

    @Override
    public JDBCState getState() {
        return delegate.getState();
    }

    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public void setTransaction(Transaction transaction) {
        //JD: note, we need to set both super and delegate transactions.
        super.setTransaction(transaction);

        //JD: this guard ensures that a recursive loop will not form
        if ( delegate.getTransaction() != transaction ) {
            delegate.setTransaction(transaction);    
        }
    }
    
    public PrimaryKey getPrimaryKey() {
        return delegate.getPrimaryKey();
    }

    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        return delegate.buildFeatureType();
    }
    
    @Override
    protected int getCountInternal(Query query) throws IOException {
        return delegate.getCount(query);
    }
    
    @Override
    protected ReferencedEnvelope getBoundsInternal(Query query)
            throws IOException {
        return delegate.getBoundsInternal(query);
    }
    
    @Override
    protected boolean canFilter() {
        return delegate.canFilter();
    }
    
    @Override
    protected boolean canSort() {
        return delegate.canSort();
    }
    
    @Override
    protected boolean canRetype() {
        return delegate.canRetype();
    }
    
    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(
            Query query) throws IOException {
        return delegate.getReaderInternal(query);
    }
    
//  /**
//  * This method operates by delegating to the
//  * {@link JDBCFeatureCollection#update(AttributeDescriptor[], Object[])}
//  * method provided by the feature collection resulting from
//  * {@link #filtered(ContentState, Filter)}.
//  *
//  * @see FeatureStore#modifyFeatures(AttributeDescriptor[], Object[], Filter)
//  */
// public void modifyFeatures(AttributeDescriptor[] type, Object[] value, Filter filter)
//     throws IOException {
//     if (filter == null) {
//         String msg = "Must specify a filter, must not be null.";
//         throw new IllegalArgumentException(msg);
//     }
//
//     JDBCFeatureCollection features = (JDBCFeatureCollection) filtered(getState(), filter);
//     features.update(type, value);
// }
    
    protected FeatureWriter<SimpleFeatureType, SimpleFeature> getWriterInternal(Query query, int flags)
            throws IOException {
        
        if ( flags == 0 ) {
            throw new IllegalArgumentException( "no write flags set" );
        }
        
        //get connection from current state
        Connection cx = getDataStore().getConnection(getState());
        
        Filter postFilter;
        //check for update only case
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer;
        try {
            //check for insert only
            if ( (flags | WRITER_ADD) == WRITER_ADD ) {
                if ( getDataStore().getSQLDialect() instanceof PreparedStatementSQLDialect ) {
                    PreparedStatement ps = getDataStore().selectSQLPS(getSchema(), Filter.EXCLUDE, query.getSortBy(), cx);
                    return new JDBCInsertFeatureWriter( ps, cx, delegate, query.getHints() );
                }
                else {
                    //build up a statement for the content, inserting only so we dont want
                    // the query to return any data ==> Filter.EXCLUDE
                    String sql = getDataStore().selectSQL(getSchema(), Filter.EXCLUDE, query.getSortBy());
                    getDataStore().getLogger().fine(sql);
    
                    return new JDBCInsertFeatureWriter( sql, cx, delegate, query.getHints() );
                }
            }
            
            //split the filter
            Filter[] split = delegate.splitFilter(query.getFilter());
            Filter preFilter = split[0];
            postFilter = split[1];
            
            // build up a statement for the content
            if(getDataStore().getSQLDialect() instanceof PreparedStatementSQLDialect) {
                PreparedStatement ps = getDataStore().selectSQLPS(getSchema(), preFilter, query.getSortBy(), cx);
                if ( (flags | WRITER_UPDATE) == WRITER_UPDATE ) {
                    writer = new JDBCUpdateFeatureWriter(ps, cx, delegate, query.getHints() );
                } else {
                    //update insert case
                    writer = new JDBCUpdateInsertFeatureWriter(ps, cx, delegate, query.getPropertyNames(), query.getHints() );
                }
            } else {
                String sql = getDataStore().selectSQL(getSchema(), preFilter, query.getSortBy());
                getDataStore().getLogger().fine(sql);
                
                if ( (flags | WRITER_UPDATE) == WRITER_UPDATE ) {
                    writer = new JDBCUpdateFeatureWriter( sql, cx, delegate, query.getHints() );
                } else {
                    //update insert case
                    writer = new JDBCUpdateInsertFeatureWriter( sql, cx, delegate, query.getHints() );
                }
            }
            
        } 
        catch (Exception e) {
            // close the connection
            getDataStore().closeSafe(cx);
            // now we can safely rethrow the exception
            throw (IOException) new IOException( ).initCause(e);
        }
        
        //check for post filter and wrap accordingly
        if ( postFilter != null && postFilter != Filter.INCLUDE ) {
            writer = new FilteringFeatureWriter( writer, postFilter );
        }
        return writer;
    }
}
