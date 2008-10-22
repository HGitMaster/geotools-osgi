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
package org.geotools.data.store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.feature.CollectionEvent;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.visitor.FeatureVisitor;
import org.geotools.filter.SortBy;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.NullProgressListener;
import org.geotools.util.ProgressListener;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;

/**
 * A FeatureCollection that completly delegates to a backing FetaureSource.
 * 
 * @author Jody Garnett (Refractions Research, Inc.)
 */
public class ContentFeatureCollection implements FeatureCollection<SimpleFeatureType, SimpleFeature> {
    
    /**
     * feature store the collection originated from.
     */
    protected ContentFeatureSource featureSource;
    protected Query query;
    
    /**
     * feature (possibly retyped from feautre source original) type
     */
    protected SimpleFeatureType featureType;
    /**
     * state of the feature source 
     */
    protected ContentState state;
    
    /** Internal listener storage list */
    protected List listeners = new ArrayList(2);

    /** Set of open resource iterators */
    protected final Set open = new HashSet();
    
    /**
     * feature listener which listens to the feautre source and 
     * forwards events to its listeners.
     */
    FeatureListener listener = new FeatureListener(){
        public void changed(FeatureEvent featureEvent) {
            if( listeners.isEmpty() ) return;

            FeatureCollection<SimpleFeatureType, SimpleFeature> collection;
            collection = (FeatureCollection<SimpleFeatureType, SimpleFeature>) ContentFeatureCollection.this;
            CollectionEvent event = new CollectionEvent( collection, featureEvent );

            CollectionListener[] notify = (CollectionListener[]) listeners.toArray( new CollectionListener[ listeners.size() ]);
            for( int i=0; i<notify.length; i++ ){
                CollectionListener listener = notify[i];
                try {
                    listener.collectionChanged( event );
                }
                catch (Throwable t ){
                    //TODO: log this
                    //ContentDataStore.LOGGER.log( Level.WARNING, "Problem encountered during notification of "+event, t );
                }
            }
        }           
    };
    
    protected ContentFeatureCollection( ContentFeatureSource featureSource, Query query ) {
        this.featureSource = featureSource;
        this.query = query;
        
        //add the feautre source listener
        featureSource.addFeatureListener(listener);
        
        //retype feature type if necessary
        if ( query.getPropertyNames() != Query.ALL_NAMES ) {
            this.featureType = 
                SimpleFeatureTypeBuilder.retype(featureSource.getSchema(), query.getPropertyNames() );
        }
        else {
            this.featureType = featureSource.getSchema();
        }
    }
    
    public SimpleFeatureType getSchema() {
        return featureType;
    }
    
    //Visitors
    /**
     * Accepts a visitor, which then visits each feature in the collection.
     * @throws IOException 
     */
    public void accepts(FeatureVisitor visitor, ProgressListener progress ) throws IOException {
        accepts( (org.opengis.feature.FeatureVisitor) visitor, 
            (org.opengis.util.ProgressListener) progress );
    }

    public void accepts(org.opengis.feature.FeatureVisitor visitor,
            org.opengis.util.ProgressListener progress) throws IOException {
        if( progress == null ) progress = new NullProgressListener();
        
         FeatureReader<SimpleFeatureType, SimpleFeature> reader = featureSource.getReader(query);
        try{
            float size = size();
            float position = 0;            
            progress.started();
            while( reader.hasNext() ){
                if (size > 0) progress.progress( position++/size );
                try {
                    SimpleFeature feature = reader.next();
                    visitor.visit(feature);
                }
                catch( Exception erp ){
                    progress.exceptionOccurred( erp );
                }
            }            
        }
        finally {
            progress.complete();            
            reader.close();
        }
    }
    
    
    //Listeners
    /**
     * Adds a listener for collection events.
     *
     * @param listener The listener to add
     */
    public void addListener(CollectionListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener for collection events.
     *
     * @param listener The listener to remove
     */
    public void removeListener(CollectionListener listener) {
        listeners.remove(listener);
    }
    
    // Iterators
    public static class WrappingFeatureIterator implements FeatureIterator<SimpleFeature> {
       
         FeatureReader<SimpleFeatureType, SimpleFeature> delegate;
        
        public WrappingFeatureIterator(  FeatureReader<SimpleFeatureType, SimpleFeature> delegate ) {
            this.delegate = delegate;
        }
        
        public boolean hasNext() {
            try {
                return delegate.hasNext();    
            }
            catch( IOException e ) {
                throw new RuntimeException( e );
            }
            
        }

        public SimpleFeature next() throws java.util.NoSuchElementException {
            try {
                return delegate.next();    
            }
            catch( IOException e ) {
                throw new RuntimeException( e );
            }
        }

        public void close() {
            try {
                delegate.close();    
            }
            catch( IOException e ) {
                throw new RuntimeException( e );
            }
            
        }
    }
    
    public FeatureIterator<SimpleFeature> features(){
        try {
            return new WrappingFeatureIterator( featureSource.getReader(query) );    
        }
        catch( IOException e ) {
            throw new RuntimeException( e );
        }
    }
    
    public void close( FeatureIterator<SimpleFeature> iterator ) {
        iterator.close();
    }
    
    public static class WrappingIterator implements Iterator {
    
         FeatureReader<SimpleFeatureType, SimpleFeature> delegate;
        
        public  WrappingIterator(  FeatureReader<SimpleFeatureType, SimpleFeature> delegate ) {
            this.delegate = delegate;
        }
        
        public boolean hasNext() {
            try {
                return delegate.hasNext();    
            }
            catch( IOException e ) {
                throw new RuntimeException( e );
            }
            
        }

        public Object next() {
            try {
                return delegate.next();    
            }
            catch( IOException e ) {
                throw new RuntimeException( e );
            }
        }

    
       public void remove() {
           throw new UnsupportedOperationException();
       }
    }
    
    public Iterator iterator() {
        try {
            return new WrappingIterator( featureSource.getReader(query) );    
        }
        catch( IOException e ) {
            throw new RuntimeException( e );
        }
    }
    
    public void close(Iterator close) {
        try {
            ((WrappingIterator)close).delegate.close();    
        }
        catch( IOException e ) {
            throw new RuntimeException( e );
        }
    }
    
    public ReferencedEnvelope getBounds() {
        try {
            return featureSource.getBounds(query);    
        }
        catch( IOException e ) {
            throw new RuntimeException( e );
        }
    }
    
    public int size() {
        try {
           return featureSource.getCount(query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean add(SimpleFeature o) {
        return addAll(Collections.singletonList(o));
    }

    ContentFeatureStore ensureFeatureStore() {
        if ( featureSource instanceof ContentFeatureStore ) {
            return (ContentFeatureStore) featureSource;
        }
        
        throw new UnsupportedOperationException( "read only" );
    }
    public boolean addAll(Collection c) {
        ContentFeatureStore featureStore = ensureFeatureStore();
        
        try {
            List<FeatureId> ids = featureStore.addFeatures(c);
            return ids.size() == c.size();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean addAll(FeatureCollection c) {
        ContentFeatureStore featureStore = ensureFeatureStore();
        try {
            List<FeatureId> ids;
            ids = featureStore.addFeatures( c );
            return ids.size() == c.size();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void clear() {
        ContentFeatureStore featureStore = ensureFeatureStore();
        
        try { 
            featureStore.removeFeatures(query.getFilter());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void purge() {
        //do nothing
    }
    
    public FeatureCollection<SimpleFeatureType, SimpleFeature> sort(SortBy order) {
        return sort( (org.opengis.filter.sort.SortBy) order );
    }
    
    public FeatureCollection<SimpleFeatureType, SimpleFeature> sort(org.opengis.filter.sort.SortBy sort) {
        Query query = new DefaultQuery();
        ((DefaultQuery)query).setSortBy( new org.opengis.filter.sort.SortBy[]{sort});

        query = DataUtilities.mixQueries( this.query, query, null );
        return new ContentFeatureCollection( featureSource, query );    
    }
    
    public FeatureCollection<SimpleFeatureType, SimpleFeature> subCollection(Filter filter) {
        Query query = new DefaultQuery();
        ((DefaultQuery)query).setFilter( filter );
        
        query = DataUtilities.mixQueries(this.query, query, null);
        return new ContentFeatureCollection( featureSource, query );    
    }
    
    //Unsupported
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection collection) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection collection) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection collection) {
        throw new UnsupportedOperationException();
    }

    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    public Object[] toArray(Object[] array) {
        throw new UnsupportedOperationException();
    }

    public Object getAttribute(String name) {
        throw new UnsupportedOperationException();
    }

    public Object getAttribute(Name name) {
        throw new UnsupportedOperationException();
    }

    public Object getAttribute(int indedx) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }

    public int getAttributeCount() {
        throw new UnsupportedOperationException();
    }

    public List<Object> getAttributes() {
        throw new UnsupportedOperationException();
    }

    public Object getDefaultGeometry() {
        throw new UnsupportedOperationException();
    }

    public SimpleFeatureType getFeatureType() {
        throw new UnsupportedOperationException();
    }

    public SimpleFeatureType getType() {
        throw new UnsupportedOperationException();
    }

    public void setAttribute(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    public void setAttribute(Name name, Object value) {
        throw new UnsupportedOperationException();
    }

    public void setAttribute(int index, Object value)
            throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }

    public void setAttributes(List<Object> attributes) {
        throw new UnsupportedOperationException();
    }

    public void setAttributes(Object[] attributes) {
        throw new UnsupportedOperationException();
    }

    public void setDefaultGeometry(Object defaultGeometry) {
        throw new UnsupportedOperationException();
    }

    public GeometryAttribute getDefaultGeometryProperty() {
        throw new UnsupportedOperationException();
    }
	public FeatureId getIdentifier() {
		throw new UnsupportedOperationException();
	}
    public String getID() {
        throw new UnsupportedOperationException();
    }

    public void setDefaultGeometryProperty(GeometryAttribute defaultGeometryProperty) {
        throw new UnsupportedOperationException();
    }

    public Collection<Property> getProperties() {
        throw new UnsupportedOperationException();
    }

    public Collection<Property> getProperties(Name name) {
        throw new UnsupportedOperationException();
    }

    public Collection<Property> getProperties(String name) {
        throw new UnsupportedOperationException();
    }

    public Property getProperty(Name name) {
        throw new UnsupportedOperationException();
    }

    public Property getProperty(String name) {
        throw new UnsupportedOperationException();
    }

    public Collection<? extends Property> getValue() {
        throw new UnsupportedOperationException();
    }

    public void setValue(Collection<Property> value) {
        throw new UnsupportedOperationException();
    }

    public AttributeDescriptor getDescriptor() {
        throw new UnsupportedOperationException();
    }

    public Name getName() {
        throw new UnsupportedOperationException();
    }

    public Map<Object, Object> getUserData() {
        throw new UnsupportedOperationException();
    }

    public boolean isNillable() {
        throw new UnsupportedOperationException();
    }

    public void setValue(Object value) {
        throw new UnsupportedOperationException();
    }
    public void validate() {
    }
}
