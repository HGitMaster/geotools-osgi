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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.type.Name;

/**
 * Implement a SimpleFeatureCollection by burning memory!
 * <p>
 * Contents are maintained in a sorted TreeMap by FID, this serves as a
 * reference implementation when exploring the FeatureCollection api.
 * </p>
 *   
 * @author Jody Garnett, Refractions Research
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/community-schemas/fm/src/main/java/org/geotools/feature/iso/collection/MemorySimpleFeatureCollection.java $
 */
public class MemorySimpleFeatureCollection extends AbstractSimpleFeatureCollection implements SimpleFeatureCollection,RandomFeatureAccess {
	
	public MemorySimpleFeatureCollection(SimpleFeatureCollectionType type, String id) {
		super(type, id);
	}

    //use LinkedHashMap to preserve iteration order
	private Map contents = new LinkedHashMap();
    
    public int size() {
        return contents.size();
    }

    protected Iterator openIterator() {
        return new MemoryIterator( contents.values().iterator() );
    }

    protected void closeIterator( Iterator close ) {
        if( close == null ) return;
        
        MemoryIterator it = (MemoryIterator) close;
        it.close();
    }
    
    public boolean add( Object o ) {
        Feature feature = (Feature) o;
        contents.put( feature.getID(), feature );
        return true;
    }
    
    class MemoryIterator implements Iterator {
        Iterator it;
        MemoryIterator( Iterator iterator ){
            it = iterator;
        }
        public void close(){
            it = null;
        }
        public boolean hasNext() {
            if( it == null ){
                throw new IllegalStateException();
            }            
            return it.hasNext();
        }
        public Object next() {
            if( it == null ){
                throw new IllegalStateException();
            }
            return it.next(); 
        }
        public void remove() {
            it.remove();
        }        
    }

    //
    // RandomFeatureAccess 
    //
    public Feature getFeatureMember( String id ) throws NoSuchElementException {
        if( contents.containsKey( id ) ){
            return (Feature) contents.get( id );
        }
        throw new NoSuchElementException( id );
    }

    public Feature removeFeatureMember( String id ) {
        if( contents.containsKey( id ) ){
            Feature old = (Feature) contents.get( id );
            contents.remove( id );
            return old;
        }
        return null;
    }

    public Object operation(Name arg0, List arg1) {
        throw new UnsupportedOperationException();
    }

    /**
     * TODO: implement
     */
    public List get(Name name) {
        throw new UnsupportedOperationException();
	}
   
}
