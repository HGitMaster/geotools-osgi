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

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.geotools.data.collection.ResourceCollection;
import org.geotools.data.collection.ResourceList;


/**
 * Simple SubList based on from, to index.
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/community-schemas/fm/src/main/java/org/geotools/feature/iso/collection/SubResourceList.java $
 */
public class SubResourceList extends AbstractResourceList implements ResourceCollection, List {
	ResourceList collection;	
	int fromIndex;
	int toIndex;
	
	public SubResourceList( ResourceList collection, int from, int to ){
		this.collection = collection;
		this.fromIndex = from;
		this.toIndex = to;
	}

	public Object get(int index) {        
		return collection.get( index - fromIndex );
	}

	public int size() {
		return toIndex - fromIndex;
	}

	protected Iterator openIterator() {
		return openIterator( 0 ); 
	}
    
    public ListIterator openIterator( int index ) {
        return new SubResourceListIterator( index );
    }
    
	protected void closeIterator(Iterator close) {
		if( close == null )
            return;
        if( close instanceof SubResourceListIterator ){
            SubResourceListIterator it = (SubResourceListIterator) close;
            collection.close( it.delegate );
        }
        open.remove( close );
	}    
	
    private class SubResourceListIterator implements ListIterator {
        ListIterator delegate;
        
        SubResourceListIterator(int index) {
            delegate = collection.listIterator( index+fromIndex );
        }
        public boolean hasNext() {
            return delegate.nextIndex() < toIndex;
        }

        public Object next() {
            if( delegate.nextIndex() >= toIndex )
                throw new NoSuchElementException();
            return delegate.next();
        }

        public void remove() {
            delegate.remove();
            toIndex--;
        }
        public boolean hasPrevious() {
            return delegate.previousIndex() > fromIndex;
        }

        public Object previous() {
            if( delegate.nextIndex() < fromIndex )
                throw new NoSuchElementException();
            return delegate.next();
        }

        public int nextIndex() {
            return delegate.nextIndex() + fromIndex;
        }

        public int previousIndex() {
            return delegate.previousIndex() + fromIndex;
        }

        public void set(Object o) {
            delegate.set( o );
        }

        public void add(Object o) {
            delegate.add( o );
            toIndex++;
        }
    }
    // Overrides for speed
    public void clear() {
        collection.removeRange( fromIndex, toIndex );
    }
}
