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
import java.util.NoSuchElementException;

import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.filter.Filter;

/**
 * Provides an implementation of Iterator that will filter
 * contents using the provided filter.
 * <p>
 * This is a *Generic* iterator not limited to Feature, this
 * will become more interesting as Filter is able to evaulate
 * itself with more things then just Features.
 * </p>
 * <p>
 * This also explains the use of Collection (where you may
 * have expected a FeatureCollection). However
 * <code>FeatureCollectoin.close( iterator )</code> will be
 * called on the internal delgate.
 * </p>
 *  
 * @author Jody Garnett, Refractions Research, Inc.
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/community-schemas/fm/src/main/java/org/geotools/feature/iso/collection/FilteredIterator.java $
 */
public class FilteredIterator implements Iterator {
	/** Used to close the delgate, or null */
	FeatureCollection collection;
	Iterator delegate;
	Filter filter;

	private Object next;
	
	public FilteredIterator(Iterator iterator, Filter filter) {
		this.collection = null;
		this.delegate = iterator;
		this.filter = filter;
	}
	public FilteredIterator(FeatureCollection collection, Filter filter) {
		this.collection = collection;
		this.delegate = collection.iterator();
		this.filter = filter;
		next = getNext();
	}
	
	/** Package protected, please use SubFeatureCollection.close( iterator ) */
	void close(){
		if( collection != null ){
			collection.close( delegate );
		}
		collection = null;
		delegate = null;
		filter = null;
		next = null;
	}
	
	private Object getNext() {
		Object item = null;
		while (delegate.hasNext()) {
			item = (Feature) delegate.next();
			if (filter.evaluate( (Feature) item )){ 
				return item;
			}
		}
		return null;
	}

	public boolean hasNext() {
		return next != null;
	}

	public Object next() {
		if(next == null){
			throw new NoSuchElementException();
		}
		Object current = next;
		next = getNext();
		return current;
	}

	public void remove() {
		if( delegate == null ) throw new IllegalStateException();
		
	    delegate.remove();
	}
}
