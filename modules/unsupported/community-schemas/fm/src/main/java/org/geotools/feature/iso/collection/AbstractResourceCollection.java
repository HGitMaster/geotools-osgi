/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.feature.iso.collection;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.geotools.data.collection.ResourceCollection;

/**
 * Collection supporting close( Iterator ).
 * <p>
 * This implementation is a port of java.util.Collection with support for
 * the use of close( Iterator ). This will allow subclasses that make use of
 * resources during iterator() to be uses safely.
 * </p>
 * <p>
 * Subclasses are reminded that they should construct their Iterator to
 * return system resources once content has been exhuasted. While this class
 * is safe, and we remind users, not all libraries that accept collections
 * can be hacked.
 * </p>
 * <h2>How to Collectionify Resource Access</h2>
 * <p>
 * We need to do the same things as for use of AbstractCollection - namely:
 * <ul>
 * <li><b>Read-Only</b>:
 *     provide implementations for <code>size</code> and <tt>openIterator</tt> ( w/ <code>hasNext()</code> and <code>next()</code>.)
 *     and finally closeIteartor( iterator )<p>
 * </li>
 * <li><b>Modifiable collection</b>
 *     Do all of the above and supply <code>add( Object )</code> 
 *     let that <code>Iterator</code> do <tt>remove()</tt>
 * </li>
 * </ul>
 * And of course subclass, we are after all feelign <b>abstract</b> today :-)
 * </p>
 * <p>
 * Why not play with <code>iterator()</code>? Because we are keeping track of them for
 * later <code>purge()</code>...
 * </p>
 * 
 * @author Jody Garnett, Refractions Research, Inc.
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/community-schemas/fm/src/main/java/org/geotools/feature/iso/collection/AbstractResourceCollection.java $
 */
public abstract class AbstractResourceCollection extends AbstractCollection implements ResourceCollection {

	protected AbstractResourceCollection() {
    }

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
     *		  supported by this collection.
     * 
     * @throws NullPointerException if this collection does not permit
     * 		  <tt>null</tt> elements, and the specified element is
     * 		  <tt>null</tt>.
     * 
     * @throws ClassCastException if the class of the specified element
     * 		  prevents it from being added to this collection.
     * 
     * @throws IllegalArgumentException if some aspect of this element
     *         prevents it from being added to this collection.
     */
    public boolean add(Object o) {
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
     * 		  not supported by this collection.
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
     * 	       in the specified collection.
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
		Iterator e = c.iterator();
		try {
			while (e.hasNext()) {
			    if (add(e.next()))
				modified = true;
			}
		}
		finally {
			if( c instanceof ResourceCollection){
				ResourceCollection other = (ResourceCollection) c;
				other.close( e );
			}
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
     * 	       is not supported by this collection.
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
     * 	       is not supported by this Collection.
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
     * 		  not supported by this collection.
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
     * Please implement!
     * <p>
     * Note: If you return a ResourceIterator, the default implemntation of close( Iterator )
     * will know what to do.
     * 
     * @return
     */
    final public Iterator iterator(){    	
    	Iterator iterator = null;
    	try {
    		iterator = openIterator();
    	}
    	catch(IOException io) {
    		throw new RuntimeException(io);
    	}
    	
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
     * Open a resource based Iterator, we will call close( iterator ).
     * <p>
     * Please subclass to provide your own iterator for the the ResourceCollection,
     * note <code>iterator()</code> is implemented to call <code>open()</code>
     * and track the results in for later <code>purge()</code>.
     * 
     * @param close
     * @return Iterator based on resource use
     */
    abstract protected Iterator openIterator() throws IOException;
    
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
    abstract protected void closeIterator( Iterator close ) throws IOException;
    
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
    	}
    }
}
