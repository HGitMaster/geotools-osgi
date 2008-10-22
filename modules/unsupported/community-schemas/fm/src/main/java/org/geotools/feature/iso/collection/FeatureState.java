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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.data.collection.ResourceCollection;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.iso.FeatureImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.simple.BoundingBoxAttribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This is *not* a Feature - it is a Delegate used by FeatureCollection
 * implementations as "mix-in", provides implementation of featureCollection
 * events, featureType, and attribute access.
 * <p>
 * To use cut&paste the following code exactly:<pre>
 * <code>
 * 
 * </code>
 * </p>
 * <p>
 * On the bright side this means we can "fix" all the FeatureCollection implementations
 * in one fell-swoop.
 * </p>
 * 
 * @author Jody Garnett, Refractions Reserach, Inc.
 * @since GeoTools 2.2
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/community-schemas/fm/src/main/java/org/geotools/feature/iso/collection/FeatureState.java $
 */
public class FeatureState extends FeatureImpl {
	Map userData = null;
	
	/**
	 * The data.
	 */
    protected FeatureCollection data;
    /** 
     * Internal listener storage list 
     */
    protected List listeners = new ArrayList(2);
    
    protected FeatureState(
		Collection values, AttributeDescriptor desc, String id, FeatureCollection data
	) {
		super(values, desc, id);
		this.data = data;
	}

    protected FeatureState(
		Collection values, FeatureType type, String id, FeatureCollection data
	) {
		super(values, type, id);
		this.data = data;
	}
	
    //
	// FeatureCollection Event Support
	//

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
    
    /**
     * To let listeners know that something has changed.
     */
    protected void fireChange(Feature[] features, int type) {
        /*
    	boundsAttribute().set(null); // must recalculate bounds
    	
        CollectionEvent cEvent = new CollectionEvent( 
    		(FeatureCollection) data, features, type
		);
        
        for (int i = 0, ii = listeners.size(); i < ii; i++) {
            ((CollectionListener) listeners.get(i)).collectionChanged(cEvent);
        }
        */
    }
        
    protected void fireChange(Feature feature, int type) {
        fireChange(new Feature[] {feature}, type);
    }
    
    protected void fireChange(Collection coll, int type) {
        Feature[] features = new Feature[coll.size()];
        features = (Feature[]) coll.toArray(features);
        fireChange(features, type);
    }
    
    /**
     * Accessor for getting bouding box attribute.
     */
    protected BoundingBoxAttribute boundsAttribute() {
        	for (Iterator itr = attributes().iterator(); itr.hasNext();) {
        		Attribute att = (Attribute)itr.next();
        		//JD: check for GML namespace
        		if ("bounds".equals(att.name().getLocalPart())) {
        			return (BoundingBoxAttribute) att;
        		}
        	}
        	
        	return null;
    }
    
    //
	// Feature Methods
    //    
    /**
     * Gets the bounding box for the features in this feature collection.
     * 
     * @return the envelope of the geometries contained by this feature
     *         collection.
     */
    public BoundingBox getBounds() {
    	BoundingBoxAttribute bbox = boundsAttribute();
		if (bbox.getValue() == null) {
    		BoundingBox bounds = 
    			new ReferencedEnvelope((CoordinateReferenceSystem)null);
    			 
            Iterator i = data.iterator();
            try {            	
	            while(i.hasNext()) {
	                BoundingBox geomBounds = ((Feature) i.next()).getBounds();                
	                if ( ! geomBounds.isEmpty() ) {
	                    bounds.include(geomBounds);
	                }
	            }
            }
            finally {
            	data.close( i );
            }
        }
        return (BoundingBox)bbox.getValue();
    }
    
    /** Test if collection is all features! */
    public static boolean isFeatures( Collection stuff ){
        if( stuff instanceof FeatureCollection ) return true;
        
        Iterator i = stuff.iterator();
        try {
	        while( i.hasNext() ){
	            if(!(i.next() instanceof Feature))
	                return false;
	        }
        }
        finally {
            if( stuff instanceof ResourceCollection){
                ((ResourceCollection) stuff).close( i );
            }
        }
        return true;
    }

	public synchronized void putUserData(Object key, Object value) {
		if( userData == null ){
			userData = new HashMap();
		}
		userData.put(key, value);
	}

	public synchronized Object getUserData(Object key) {
		if( userData != null ){
			return userData.get(key);
		}
		return null;
	}
	
}
