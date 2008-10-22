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
package org.geotools.feature.collection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.util.ProgressListener;

/**
 * Implement a feature collection just based on provision of iterator.
 * 
 * @author Jody Garnett (Refractions Research Inc)
 */
public abstract class AdaptorFeatureCollection implements FeatureCollection<SimpleFeatureType, SimpleFeature> {
 
    public AdaptorFeatureCollection( String id, SimpleFeatureType memberType ) {
        this.id = id == null ? "featureCollection" : id;
        this.schema = memberType;
    }
    
    //
    // FeatureCollection<SimpleFeatureType, SimpleFeature> - Feature Access
    // 
    public FeatureIterator<SimpleFeature> features() {
        FeatureIterator<SimpleFeature> iter = new DelegateFeatureIterator<SimpleFeature>( this, openIterator() );
        open.add( iter );
        return iter; 
    }
    public void close( FeatureIterator<SimpleFeature> close ) {     
        closeIterator( close );
        open.remove( close );
    }
    public void closeIterator( FeatureIterator<SimpleFeature> close ) {
        DelegateFeatureIterator<SimpleFeature> iter = (DelegateFeatureIterator<SimpleFeature>) close;
        closeIterator( iter.delegate );
        iter.close(); 
    }

    /**
     * Accepts a visitor, which then visits each feature in the collection.
     * @throws IOException 
     */
    public void accepts(FeatureVisitor visitor, ProgressListener progress ) throws IOException {
        Iterator<SimpleFeature> iterator = null;
        if( progress == null ) progress = new NullProgressListener();
        try{
            float size = size();
            float position = 0;            
            progress.started();
            for( iterator = iterator(); !progress.isCanceled() && iterator.hasNext();){
                if (size > 0) progress.progress( position++/size );
                try {
                    SimpleFeature feature = iterator.next();
                    visitor.visit(feature);
                }
                catch( Exception erp ){
                    progress.exceptionOccurred( erp );
                }
            }            
        }
        finally {
            progress.complete();            
            close( iterator );
        }
    }
        
    //
    // Feature Collections API
    //
    public FeatureCollection<SimpleFeatureType, SimpleFeature> subList( Filter filter ) {
        return new SubFeatureList(this, filter );
    }
    
    public FeatureCollection<SimpleFeatureType, SimpleFeature> subCollection( Filter filter ) {
        if( filter == Filter.INCLUDE ){
            return this;
        }        
        return new SubFeatureCollection( this, filter );
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> sort( SortBy order ) {
        return new SubFeatureList(this, order );
    }

    //
    // Resource Collection management
    //
    /**
     * @return <tt>true</tt> if this collection contains no elements.
     */
    public boolean isEmpty() {
        return size() == 0;
    }
    
    /**
     * Returns <tt>true</tt> if this collection contains the specified
     * element.
     * <tt></tt>.<p>
     *
     * This implementation iterates over the elements in the collection,
     * checking each element in turn for equality with the specified element.
     *
     * @param o object to be checked for containment in this collection.
     * @return <tt>true</tt> if this collection contains the specified element.
     */
    public boolean contains(Object o) {
        Iterator e = null;
        try {
            e = iterator();
            if (o==null) {
                while (e.hasNext())
                if (e.next()==null)
                    return true;
            } else {
                while (e.hasNext())
                if (o.equals(e.next()))
                    return true;
            }
            return false;
        }
        finally {
            close( e );
        }
    }
    

    /**
     * Array of all the elements.
     * 
     * @return an array containing all of the elements in this collection.
     */
    public Object[] toArray() {
        Object[] result = new Object[size()];
        Iterator e = null;
        try {
            e = iterator();
            for (int i=0; e.hasNext(); i++)
                result[i] = e.next();
            return result;
        } finally {
            close( e );
        }
    }

    public Object[] toArray(Object[] a) {
        int size = size();
        if (a.length < size)
            a = (Object[])java.lang.reflect.Array
        .newInstance(a.getClass().getComponentType(), size);

        Iterator it = iterator();
        try {
            
            Object[] result = a;
            for (int i=0; i<size; i++)
                result[i] = it.next();
            if (a.length > size)
            a[size] = null;
            return a;
        }
        finally {
            close( it );
        }
    }

    // Modification Operations

    /**
     * Implement to support modification.
     * 
     * @param o element whose presence in this collection is to be ensured.
     * @return <tt>true</tt> if the collection changed as a result of the call.
     * 
     * @throws UnsupportedOperationException if the <tt>add</tt> method is not
     *        supported by this collection.
     * 
     * @throws NullPointerException if this collection does not permit
     *        <tt>null</tt> elements, and the specified element is
     *        <tt>null</tt>.
     * 
     * @throws ClassCastException if the class of the specified element
     *        prevents it from being added to this collection.
     * 
     * @throws IllegalArgumentException if some aspect of this element
     *         prevents it from being added to this collection.
     */
    public boolean add(SimpleFeature o) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes a single instance of the specified element from this
     * collection, if it is present (optional operation). 
     * 
     * @param o element to be removed from this collection, if present.
     * @return <tt>true</tt> if the collection contained the specified
     *         element.
     * @throws UnsupportedOperationException if the <tt>remove</tt> method is
     *        not supported by this collection.
     */
    public boolean remove(Object o) {
        Iterator e = iterator();
        try {
            if (o==null) {
                while (e.hasNext()) {
                if (e.next()==null) {
                    e.remove();
                    return true;
                }
                }
            } else {
                while (e.hasNext()) {
                if (o.equals(e.next())) {
                    e.remove();
                    return true;
                }
            }
        }
        return false;
        }
        finally {
            close( e );
        }
    }


    // Bulk Operations

    /**
     * Returns <tt>true</tt> if this collection contains all of the elements
     * in the specified collection. <p>
     * 
     * @param c collection to be checked for containment in this collection.
     * @return <tt>true</tt> if this collection contains all of the elements
     *         in the specified collection.
     * @throws NullPointerException if the specified collection is null.
     * 
     * @see #contains(Object)
     */
    public boolean containsAll(Collection c) {
        Iterator e = c.iterator();
        try {
            while (e.hasNext())
                if(!contains(e.next()))
                return false;
            return true;
        } finally {
            close( e );
        }
    }

    /**
     * Adds all of the elements in the specified collection to this collection
     * (optional operation).
     *
     * @param c collection whose elements are to be added to this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call.
     * @throws UnsupportedOperationException if this collection does not
     *         support the <tt>addAll</tt> method.
     * @throws NullPointerException if the specified collection is null.
     * 
     * @see #add(Object)
     */
    public boolean addAll(Collection c) {
        boolean modified = false;
        Iterator<SimpleFeature> e = c.iterator();
        try {
            while (e.hasNext()) {
                if (add(e.next()))
                modified = true;
            }
        }
        finally {
            if( c instanceof FeatureCollection){
                FeatureCollection other = (FeatureCollection) c;
                other.close( e );
            }
        }
        return modified;
    }
    public boolean addAll(FeatureCollection c) {
        boolean modified = false;
        Iterator<SimpleFeature> e = c.iterator();
        try {
            while (e.hasNext()) {
                if (add(e.next()))
                modified = true;
            }
        }
        finally {
            c.close( e );            
        }
        return modified;
    }
    /**
     * Removes from this collection all of its elements that are contained in
     * the specified collection (optional operation). <p>
     *
     * @param c elements to be removed from this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call.
     * @throws UnsupportedOperationException if the <tt>removeAll</tt> method
     *         is not supported by this collection.
     * @throws NullPointerException if the specified collection is null.
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection c) {
        boolean modified = false;
        Iterator e = iterator();
        try {
            while (e.hasNext()) {
                if (c.contains(e.next())) {
                e.remove();
                modified = true;
                }
            }
            return modified;
        }
        finally {
            close( e );
        }
    }

    /**
     * Retains only the elements in this collection that are contained in the
     * specified collection (optional operation).
     *
     * @param c elements to be retained in this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call.
     * @throws UnsupportedOperationException if the <tt>retainAll</tt> method
     *         is not supported by this Collection.
     * @throws NullPointerException if the specified collection is null.
     *
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean retainAll(Collection c) {
        boolean modified = false;
        Iterator e = iterator();
        try {
            while (e.hasNext()) {
                if (!c.contains(e.next())) {
                e.remove();
                modified = true;
                }
            }
            return modified;
        }
        finally {
            close( e );
        }
    }

    /**
     * Removes all of the elements from this collection (optional operation).
     * 
     * @throws UnsupportedOperationException if the <tt>clear</tt> method is
     *        not supported by this collection.
     */
    public void clear() {
        Iterator e = iterator();
        try {
            while (e.hasNext()) {
                e.next();
                e.remove();
            }
        }finally {
            close(e);
        }
    }


    //  String conversion

    /**
     * Returns a string representation of this collection. 
     * 
     * @return a string representation of this collection.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        Iterator i = iterator();
        try {
            boolean hasNext = i.hasNext();
            while (hasNext) {
                Object o = i.next();
                buf.append(o == this ? "(this Collection)" : String.valueOf(o));
                hasNext = i.hasNext();
                if (hasNext)
                    buf.append(", ");
            }
            buf.append("]");
            return buf.toString();
        } finally {
            close( i );
        }
    }
    
    //
    // Contents
    //
    //
    /** Set of open resource iterators */
    protected final Set open = new HashSet();
    /**
     * listeners
     */
    protected List listeners = new ArrayList();
    /** 
     * id used when serialized to gml
     */
    protected String id;
    protected SimpleFeatureType schema;

    /**
     * Returns the set of open iterators.
     * 
     */
    final public Set getOpenIterators() {
        return open;
    }
    
    /**
     * Please implement!
     * <p>
     * Note: If you return a ResourceIterator, the default implemntation of close( Iterator )
     * will know what to do.
     * 
     */
    final public Iterator<SimpleFeature> iterator(){       
        Iterator<SimpleFeature> iterator = openIterator();
        open.add( iterator );
        return iterator;
    }

    /**
     * Returns the number of elements in this collection.
     * 
     * @return Number of items, or Interger.MAX_VALUE
     */
    public abstract int size();
    
    /**
     * Clean up after any resources assocaited with this iteartor in a manner similar to JDO collections.
     * </p>
     * Example (safe) use:<pre><code>
     * Iterator iterator = collection.iterator();
     * try {
     *     for( Iterator i=collection.iterator(); i.hasNext();){
     *          Feature feature = (Feature) i.hasNext();
     *          System.out.println( feature.getID() );
     *     }
     * }
     * finally {
     *     collection.close( iterator );
     * }
     * </code></pre>
     * </p>
     * @param close
     */
    final public void close( Iterator close ){
        if( close == null ) return;
        try {
            closeIterator( close );
        }
        catch ( Throwable e ){
            // TODO Log e = ln
        }
        finally {
            open.remove( close );
        }
        
    }
    /**
     * Open an Iterator, we will call close( iterator ).
     * <p>
     * Please subclass to provide your own iterator - note <code>iterator()</code> is
     * implemented to call <code>open()</code> and track the results in for
     * later <code>purge()</code>.
     * 
     * @return Iterator based on resource use
     */
    abstract protected Iterator<SimpleFeature> openIterator();
    
    /**
     * Please override to cleanup after your own iterators, and
     * any used resources.
     * <p>
     * As an example if the iterator was working off a File then
     * the inputstream should be closed.
     * </p>
     * <p>
     * Subclass must call super.close( close ) to allow the list
     * of open iterators to be adjusted.
     * </p>
     * 
     * @param close Iterator, will not be <code>null</code>
     */
    abstract protected void closeIterator( Iterator<SimpleFeature> close );
    
    /**
     * Close any outstanding resources released by this resources.
     * <p>
     * This method should be used with great caution, it is however available
     * to allow the use of the ResourceCollection with algorthims that are
     * unaware of the need to close iterators after use.
     * </p>
     * <p>
     * Example of using a normal Collections utility method:<pre><code>
     * Collections.sort( collection );
     * collection.purge(); 
     * </code></pre>
     */
    public void purge(){        
        for( Iterator i = open.iterator(); i.hasNext(); ){
            Object resource = i.next();
            if( resource instanceof Iterator ){
                Iterator resourceIterator = (Iterator) resource;
                try {
                    closeIterator( resourceIterator );
                }
                catch( Throwable e){
                    // TODO: Log e = ln
                }
                finally {
                    i.remove();
                }
            }
            else if ( resource instanceof FeatureIterator ){
                FeatureIterator<SimpleFeature> resourceIterator = (FeatureIterator<SimpleFeature>) resource;
                try {
                    closeIterator( resourceIterator );
                }
                catch( Throwable e){
                    // TODO: Log e = ln
                }
                finally {
                    i.remove();
                }
            }
        }
    }

    public String getID() {
    	return id;
    }

    public final void addListener(CollectionListener listener) throws NullPointerException {
    	listeners.add(listener);
    }

    public final void removeListener(CollectionListener listener) throws NullPointerException {
    	listeners.remove(listener);
    }


    public SimpleFeatureType getSchema() {
    	return schema;
    }

    /**
     * Subclasses need to override this.
     */
    public ReferencedEnvelope getBounds() {
    	throw new UnsupportedOperationException("subclasses should override");
    }
}
