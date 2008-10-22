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

import java.util.AbstractList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import org.geotools.data.collection.ResourceCollection;
import org.geotools.data.collection.ResourceList;

	/**
     * Starter for resource based list implementations.
     * <p>
     * Same deal as ResouceCollections - iterators that need to be closed. This implementation is
     * set up for random access happy content, like an array list.
     * </p>
     * <p>
     * Read-only:
     * <ul>
     * <li><code>get(int index)</code>
     * <li><code>size()</code>
     * </ul>
     * </p>
     * <p>
     * For read/write:
     * <ul>
     * <li><code>set(index, element)</code> - for fixed length
     * <li><code>add(index, element)</tt> and <code>remove(index)</code> for dynamic length
	 * </ul>
	 * As usual override anything if you have a faster implementation, say based
	 * on a shapefile index.
	 * </p>
	 * 
	 * @author Jody Garnett, Refractions Research, Inc.
	 * @see AbstractList
	 * @since GeoTools 2.2
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/community-schemas/fm/src/main/java/org/geotools/feature/iso/collection/AbstractResourceList.java $
     */

public abstract class AbstractResourceList extends AbstractResourceCollection implements ResourceList {

	    protected AbstractResourceList() {
	    }

	    /**
         * Appends element.
         * <p>
         * This implementation calls <tt>add(size(), o)</tt>.
         * <p>
         * Note that this implementation throws an <tt>UnsupportedOperationException</tt> unless
         * <tt>add(int, Object)</tt> is overridden.
         * 
         * @param o element to be appended to this list.
         * @return <tt>true</tt> (as per the general contract of <tt>Collection.add</tt>).
         * @throws UnsupportedOperationException if the <tt>add</tt> method is not supported by
         *         this Set.
         * @throws ClassCastException if the class of the specified element prevents it from being
         *         added to this set.
         * @throws IllegalArgumentException some aspect of this element prevents it from being added
         *         to this collection.
         */
	    public boolean add(Object item) {
	    	add( size(), item );
	    	return true;
	    }

	    /**
         * item at the specified index.
         * 
         * @param index index of item
         * @return the item at the specified index.
         * @throws IndexOutOfBoundsException if index is not between 0 and size
         */
	    abstract public Object get(int index);

	    /**
         * Replaces item in position index (optional operation).
         * <p>
         * This implementation always throws an <tt>UnsupportedOperationException</tt>.
         * 
         * @param index index of element to replace.
         * @param element element to be stored at the specified position.
         * @return the element previously at the specified position.
         * @throws UnsupportedOperationException if the <tt>set</tt> method is not supported by
         *         this List.
         * @throws ClassCastException if the class of the specified element prevents it from being
         *         added to this list.
         * @throws IllegalArgumentException if some aspect of the specified element prevents it from
         *         being added to this list.
         * @throws IndexOutOfBoundsException if the specified index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>).
         */
	    
	    public Object set(int index, Object item) {
	    	throw new UnsupportedOperationException();
	    }

	    /**
         * Inserts the specified element at the specified position in this list (optional
         * operation). Shifts the element currently at that position (if any) and any subsequent
         * elements to the right (adds one to their indices).
         * <p>
         * This implementation always throws an UnsupportedOperationException.
         * 
         * @param index index at which the specified element is to be inserted.
         * @param element element to be inserted.
         * @throws UnsupportedOperationException if the <tt>add</tt> method is not supported by
         *         this list.
         * @throws ClassCastException if the class of the specified element prevents it from being
         *         added to this list.
         * @throws IllegalArgumentException if some aspect of the specified element prevents it from
         *         being added to this list.
         * @throws IndexOutOfBoundsException index is out of range (<tt>index &lt;
	     *		  0 || index &gt; size()</tt>).
         */
	    public void add(int index, Object element) {
	    	throw new UnsupportedOperationException();
	    }

	    /**
         * Removes the element at the specified position in this list (optional operation). Shifts
         * any subsequent elements to the left (subtracts one from their indices). Returns the
         * element that was removed from the list.
         * <p>
         * This implementation always throws an <tt>UnsupportedOperationException</tt>.
         * 
         * @param index the index of the element to remove.
         * @return the element previously at the specified position.
         * @throws UnsupportedOperationException if the <tt>remove</tt> method is not supported by
         *         this list.
         * @throws IndexOutOfBoundsException if the specified index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>).
         */
	    public Object remove(int index) {
	    	throw new UnsupportedOperationException();
	    }


	    // Search Operations

	    /**
         * Returns the index in this list of the first occurence of the specified element, or -1 if
         * the list does not contain this element. More formally, returns the lowest index
         * <tt>i</tt> such that <tt>(o==null ?
	     * get(i)==null : o.equals(get(i)))</tt>, or -1 if
         * there is no such index.
         * <p>
         * This implementation first gets a list iterator (with <tt>listIterator()</tt>). Then,
         * it iterates over the list until the specified element is found or the end of the list is
         * reached.
         * 
         * @param o element to search for.
         * @return the index in this List of the first occurence of the specified element, or -1 if
         *         the List does not contain this element.
         */
	    public int indexOf(Object o) {
	    	ListIterator e = listIterator();	    	
	    	try {
		
				if (o==null) {
				    while (e.hasNext())
				    	if (e.next()==null)
				    		return e.previousIndex();
				} else {
				    while (e.hasNext())
				    	if (o.equals(e.next()))
				    		return e.previousIndex();
				}
				return -1;
	    	}
	    	finally {
	    		close( e );
	    	}
	    }

	    /**
         * Returns the index in this list of the last occurence of the specified element, or -1 if
         * the list does not contain this element. More formally, returns the highest index
         * <tt>i</tt> such that <tt>(o==null ?
	     * get(i)==null : o.equals(get(i)))</tt>, or -1 if
         * there is no such index.
         * <p>
         * This implementation first gets a list iterator that points to the end of the list (with
         * listIterator(size())). Then, it iterates backwards over the list until the specified
         * element is found, or the beginning of the list is reached.
         * 
         * @param o element to search for.
         * @return the index in this list of the last occurence of the specified element, or -1 if
         *         the list does not contain this element.
         */
	    public int lastIndexOf(Object o) {
	    	ListIterator e = listIterator(size());	    	
	    	try {		
				if (o==null) {
				    while (e.hasPrevious())
					if (e.previous()==null)
					    return e.nextIndex();
				} else {
				    while (e.hasPrevious())
					if (o.equals(e.previous()))
					    return e.nextIndex();
				}
				return -1;
	    	}
	    	finally {
	    		close( e );
	    	}
	    }


	    // Bulk Operations
	    /**
         * Removes all of the elements from this collection (optional operation).
         * <p>
         * This implementation calls <tt>removeRange(0, size())</tt>.
         * <p>
         * Note that this implementation throws an <tt>UnsupportedOperationException</tt> unless
         * <tt>remove(int
	     * index)</tt> or <tt>removeRange(int fromIndex, int toIndex)</tt> is
         * overridden.
         * 
         * @throws UnsupportedOperationException if the <tt>clear</tt> method is not supported by
         *         this Collection.
         */
	    public void clear() {
	        removeRange(0, size());
	    }

	    /**
         * Inserts all of the elements in the specified collection into this list at the specified
         * position (optional operation).
         * <p>
         * Note that this implementation throws an <tt>UnsupportedOperationException</tt> unless
         * <tt>add(int, Object)</tt> is overridden.
         * 
         * @return <tt>true</tt> if this list changed as a result of the call.
         * @param index index at which to insert the first element from the specified collection.
         * @param c elements to be inserted into this List.
         * @throws UnsupportedOperationException if the <tt>addAll</tt> method is not supported by
         *         this list.
         * @throws ClassCastException if the class of an element of the specified collection
         *         prevents it from being added to this List.
         * @throws IllegalArgumentException some aspect an element of the specified collection
         *         prevents it from being added to this List.
         * @throws IndexOutOfBoundsException index out of range (<tt>index &lt; 0
	     *            || index &gt; size()</tt>).
         * @throws NullPointerException if the specified collection is null.
         */
	    public boolean addAll(int index, Collection c) {
			boolean modified = false;
			Iterator e = c.iterator();
			try {
				while (e.hasNext()) {
				    add(index++, e.next());
				    modified = true;
				}
				return modified;
			}
			finally {
				if( c instanceof ResourceCollection ){
					((ResourceCollection)c).close( e );
				}
			}
	    }


	    // Iterators
        /**
         * Returns <tt>listIterator(0)</tt>.
         * 
         * @return listIterator(0)
         * @see #listIterator(int)
         */
        public ListIterator listIterator() {
            return listIterator(0);
        }

        
        /**
         * Returns a list iterator of the elements in this list from index on.
         * 
         * @param index
         * @return a list iterator from index *
         * @throws IndexOutOfBoundsException if the specified index is out of range
         * @see #modCount
         */
        public ListIterator listIterator(final int index) {
            if (index<0 || index>size())
                throw new IndexOutOfBoundsException("Index: "+index);
            ListIterator iterator = openIterator( index );
            open.add( iterator );
            return iterator;                        
        }
        public ListIterator openIterator( final int index ){           
           return new ListItr(index);
        }        
        
	    /**
         * Returns a quick iterator that uses get and size methods.
         * <p>
         * As with all resource collections it is assumed that the iterator will
         * be closed after use.
         * </p>
         * 
         * @return an iterator over the elements in this list in proper sequence.
         * @see #modCount
         */
        protected Iterator openIterator() {
	    	return new ListItr(0);
	    }

        protected void closeIterator( Iterator close ) {
            // no resources used by default I
        }

	    private class ListItr implements ListIterator {
            int index = 0;
            int lastRet = -1;
            /**
             * detecte concurrent modification.
             */
            int expectedModCount = modCount;
            
			ListItr(int index) {
			    this.index = index;
			}

            final void checkForComodification() {
                if (modCount != expectedModCount)
                    throw new ConcurrentModificationException();                
            }

            public boolean hasNext() {
                return index != size();
            }

            public Object next() {
                checkForComodification();
                try {
                    Object next = get(index);
                    lastRet = index++;
                    return next;
                } catch(IndexOutOfBoundsException e) {
                    checkForComodification();
                    throw new NoSuchElementException();
                }
            }
    
            public void remove() {
                if (lastRet == -1)
                throw new IllegalStateException();
                    checkForComodification();
    
                try {
                    AbstractResourceList.this.remove(lastRet);
                    if (lastRet < index)
                        index--;
                    lastRet = -1;
                    expectedModCount = modCount;
                } catch(IndexOutOfBoundsException e) {
                    throw new ConcurrentModificationException();
                }
            }
    		public boolean hasPrevious() {
    		    return index != 0;
    		}

	        public Object previous() {
	            checkForComodification();
	            try {
	                int i = index - 1;
	                Object previous = get(i);
	                lastRet = index = i;
	                return previous;
	            } catch(IndexOutOfBoundsException e) {
	                checkForComodification();
	                throw new NoSuchElementException();
	            }
	        }

			public int nextIndex() {
			    return index;
			}
	
			public int previousIndex() {
			    return index-1;
			}
	
			public void set(Object o) {
			    if (lastRet == -1){
			    	throw new IllegalStateException();
			    }
		        checkForComodification();
	
			    try {
			    	AbstractResourceList.this.set(lastRet, o);
			    	expectedModCount = modCount;
			    } catch(IndexOutOfBoundsException e) {
			    	throw new ConcurrentModificationException();
			    }
			}
	
			public void add(Object o) {
		        checkForComodification();
	
			    try {
			    	AbstractResourceList.this.add(index++, o);
			    	lastRet = -1;
			    	expectedModCount = modCount;
			    } catch(IndexOutOfBoundsException e) {
			    	throw new ConcurrentModificationException();
			    }
			}
	    }

	    /**
         * view of the portion of this list between fromIndex up to toIndex.
         * 
         * @param fromIndex
         * @param toIndex
         * @return a view of the specified range within this list.
         * @throws IndexOutOfBoundsException
         * @throws IllegalArgumentException endpoint indices out of order
         */
	    public List subList(int fromIndex, int toIndex) {
	        return new SubList(this, fromIndex, toIndex);
	    }

	    // Comparison and hashing

	    /**
         * Compares the specified object with this list for equality. Returns <tt>true</tt> if and
         * only if the specified object is also a list, both lists have the same size, and all
         * corresponding pairs of elements in the two lists are <i>equal</i>. (Two elements
         * <tt>e1</tt> and <tt>e2</tt> are <i>equal</i> if <tt>(e1==null ? e2==null :
	     * e1.equals(e2))</tt>.)
         * In other words, two lists are defined to be equal if they contain the same elements in
         * the same order.
         * <p>
         * This implementation first checks if the specified object is this list. If so, it returns
         * <tt>true</tt>; if not, it checks if the specified object is a list. If not, it returns
         * <tt>false</tt>; if so, it iterates over both lists, comparing corresponding pairs of
         * elements. If any comparison returns <tt>false</tt>, this method returns <tt>false</tt>.
         * If either iterator runs out of elements before the other it returns <tt>false</tt> (as
         * the lists are of unequal length); otherwise it returns <tt>true</tt> when the
         * iterations complete.
         * 
         * @param o the object to be compared for equality with this list.
         * @return <tt>true</tt> if the specified object is equal to this list.
         */
	    public boolean equals(Object o) {
    		if (o == this)
    		    return true;
    		if (!(o instanceof List))
    		    return false;
    
    		ListIterator e1 = listIterator();
    		ListIterator e2 = ((List) o).listIterator();
            try {
        		while(e1.hasNext() && e2.hasNext()) {
        		    Object o1 = e1.next();
        		    Object o2 = e2.next();
        		    if (!(o1==null ? o2==null : o1.equals(o2))){
        		        return false;
                    }
        		}
        		return !(e1.hasNext() || e2.hasNext());
            }
            finally {
                close( e1 );
                if( o instanceof ResourceCollection ){
                    ((ResourceCollection)o).close( e2 );
                }                
            }
	    }

	    /**
         * Returns the hash code value for this list.
         * <p>
         * This implementation uses exactly the code that is used to define the list hash function
         * in the documentation for the <tt>List.hashCode</tt> method.
         * 
         * @return the hash code value for this list.
         */
	    public int hashCode() {
    		int hashCode = 1;
    		Iterator i = iterator();
            try {
    	     	while (i.hasNext()) {
    	     	    Object obj = i.next();
    	     	    hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
    	     	}
    	     	return hashCode;
            }
            finally {
                close( i );
            }
	    }

	    /**
         * Removes from this list all of the elements whose index is between fromIndex upto toIndex
         * 
         * @param fromIndex index of first element to be removed.
         * @param toIndex index after last element to be removed.
         */
	    public void removeRange(int fromIndex, int toIndex) {
	        ListIterator it = listIterator(fromIndex);
            try {
    	        for (int i=0, n=toIndex-fromIndex; i<n; i++) {
    	            it.next();
    	            it.remove();
    	        }
            }
            finally {
                close( it );
            }
	    }

	    /**
         * The number of times this list has been modified.
         */
	    protected transient int modCount = 0;

	}

	class SubList extends AbstractResourceList implements ResourceList {
        
	    private final class SubListIterator implements ListIterator {
            private ListIterator i;
            
            private SubListIterator( int index ) {
                super();
                i = l.listIterator(index+offset);                
                i = l.listIterator(index+offset);
            }
            public boolean hasNext() {
                return nextIndex() < size;
            }
            public Object next() {
                if (hasNext())
                    return i.next();
                else
                    throw new NoSuchElementException();
            }
            public boolean hasPrevious() {
                return previousIndex() >= 0;
            }
            public Object previous() {
                if (hasPrevious())
                    return i.previous();
                else
                    throw new NoSuchElementException();
            }
            public int nextIndex() {
                return i.nextIndex() - offset;
            }
            public int previousIndex() {
                return i.previousIndex() - offset;
            }
            public void remove() {
                i.remove();
                expectedModCount = l.modCount;
                size--;
                modCount++;
            }
            public void set(Object o) {
                i.set(o);
            }
            public void add(Object o) {
                i.add(o);
                expectedModCount = l.modCount;
                size++;
                modCount++;
            }
        }

        private AbstractResourceList l;
	    private int offset;
	    private int size;
	    private int expectedModCount;

	    SubList(AbstractResourceList list, int fromIndex, int toIndex) {
	        if (fromIndex < 0)
	            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
	        if (toIndex > list.size())
	            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
	        if (fromIndex > toIndex)
	            throw new IllegalArgumentException("fromIndex(" + fromIndex +
	                                               ") > toIndex(" + toIndex + ")");
	        l = list;
	        offset = fromIndex;
	        size = toIndex - fromIndex;
	        expectedModCount = l.modCount;
	    }

	    public Object set(int index, Object element) {
	        rangeCheck(index);
	        checkForComodification();
	        return l.set(index+offset, element);
	    }

	    public Object get(int index) {
	        rangeCheck(index);
	        checkForComodification();
	        return l.get(index+offset);
	    }

	    public int size() {
	        checkForComodification();
	        return size;
	    }

	    public void add(int index, Object element) {
	        if (index<0 || index>size)
	            throw new IndexOutOfBoundsException();
	        checkForComodification();
	        l.add(index+offset, element);
	        expectedModCount = l.modCount;
	        size++;
	        modCount++;
	    }

	    public Object remove(int index) {
	        rangeCheck(index);
	        checkForComodification();
	        Object result = l.remove(index+offset);
	        expectedModCount = l.modCount;
	        size--;
	        modCount++;
	        return result;
	    }

	    public void removeRange(int fromIndex, int toIndex) {
	        checkForComodification();
	        l.removeRange(fromIndex+offset, toIndex+offset);
	        expectedModCount = l.modCount;
	        size -= (toIndex-fromIndex);
	        modCount++;
	    }

	    public boolean addAll(Collection c) {
	        return addAll(size, c);
	    }

	    public boolean addAll(int index, Collection c) {
	        if (index<0 || index>size)
	            throw new IndexOutOfBoundsException(
	                "Index: "+index+", Size: "+size);
	        int cSize = c.size();
	        if (cSize==0)
	            return false;

	        checkForComodification();
	        l.addAll(offset+index, c);
	        expectedModCount = l.modCount;
	        size += cSize;
	        modCount++;
	        return true;
	    }
        
        public void closeIterator( Iterator close ) {
            SubListIterator it = (SubListIterator) close;
            l.close( it.i );                        
        }
        
        public void purge() {
            for( Iterator i = open.iterator(); i.hasNext(); ){
                SubListIterator it = (SubListIterator) i.next();
                l.close( it.i );
                i.remove();
            }
        }
        
	    public ListIterator listIterator(final int index) {
	        checkForComodification();
	        if (index<0 || index>size)
	            throw new IndexOutOfBoundsException(
	                "Index: "+index+", Size: "+size);

            ListIterator listIterator = new SubListIterator(index);
            open.add( listIterator );
            return listIterator;
	    }

	    public List subList(int fromIndex, int toIndex) {
	        return new SubList(this, fromIndex, toIndex);
	    }

	    private void rangeCheck(int index) {
	        if (index<0 || index>=size)
	            throw new IndexOutOfBoundsException("Index: "+index+
	                                                ",Size: "+size);
	    }

	    private void checkForComodification() {
	        if (l.modCount != expectedModCount)
	            throw new ConcurrentModificationException();
	    }
	}

	class RandomAccessSubList extends SubList implements RandomAccess {
	    RandomAccessSubList(AbstractResourceList list, int fromIndex, int toIndex) {
	        super(list, fromIndex, toIndex);
	    }

	    public List subList(int fromIndex, int toIndex) {
	        return new RandomAccessSubList(this, fromIndex, toIndex);
	    }
	}
