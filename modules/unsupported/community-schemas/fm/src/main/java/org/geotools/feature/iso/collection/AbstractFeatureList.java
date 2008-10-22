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

import java.util.Collection;
import java.util.List;

import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Abstract feature list to be used as base for FeatureList implementations.
 * <br>
 * <p>
 * Subclasses must implement the following methods:
 
 * <ul>
 *	<li>{@link Collection#size()}
 *	<li>{@link org.opengis.feature.FeatureList#subList(Filter)}
 *	<li>{@link org.geotools.feature.collection.AbstractResourceList#get(int)}
 * </ul>
 * </p>
 * <br>
 * <p>
 * This implementation of FeatureList uses a delegate to satisfy the 
 * methods of the {@link Feature} interface. 
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public abstract class AbstractFeatureList extends AbstractResourceList implements FeatureCollection {

	private FeatureState delegate;
	
	protected AbstractFeatureList(
		Collection values,  AttributeDescriptor descriptor, String id 
	) {
		this.delegate = new FeatureState(values,descriptor,id,this);
	}
	
	protected AbstractFeatureList(
		Collection values,  FeatureCollectionType type, String id
	) {
		this.delegate = new FeatureState(values,type,id,this);
	}

  	public FeatureCollection subCollection(org.opengis.filter.Filter filter) {
  		//TODO: inject a filter factory
		return new SubFeatureCollection(this,filter,null);
	}

	public FeatureCollection sort(org.opengis.filter.sort.SortBy order) {
		throw new UnsupportedOperationException();
	}
	public Collection memberTypes() {
		return ((FeatureCollectionType)getType()).getMembers();
	}

	public void putUserData(Object key, Object value) {
		delegate.putUserData(key,value);
	}

	public Object getUserData(Object key) {
		return delegate.getUserData(key);
	}

	public AttributeType getType() {
		return delegate.getType();
	}
	
	public boolean nillable() {
		return delegate.nillable();
	}
	
	public String getID() {
		return delegate.getID();
	}

	public CoordinateReferenceSystem getCRS() {
		return delegate.getCRS();
	}
    
    public void setCRS(CoordinateReferenceSystem crs) {
        delegate.setCRS(crs);
    }

	public BoundingBox getBounds() {
		return delegate.getBounds();
	}

	public GeometryAttribute getDefaultGeometry() {
		return delegate.getDefaultGeometry();
	}

	public void setDefaultGeometry(GeometryAttribute geom) {
		delegate.setDefaultGeometry(geom);
	}

	public void setValue(List newValue) throws IllegalArgumentException {
		delegate.setValue(newValue);
	}

	public List getValue(Name name) {
		return delegate.get(name);
	}

	public AttributeDescriptor getDescriptor() {
		return delegate.getDescriptor();
	}

	public Name name() {
		return delegate.name();
	}

	public Object getValue() {
		return delegate.getValue();
	}

	public void setValue(Object newValue) throws IllegalArgumentException {
		delegate.setValue(newValue);
	}

}