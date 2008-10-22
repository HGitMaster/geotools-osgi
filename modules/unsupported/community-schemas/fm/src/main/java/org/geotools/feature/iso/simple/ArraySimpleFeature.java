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

package org.geotools.feature.iso.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.feature.Attribute;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * This class is designed to be fast and lighweight, making use of the
 * assumptions of SimpleFeature to do everything in the easiest manner possible.
 * <p>
 * This code operates under the assumption that the most common use will be
 * through the SimpleFeatureAPI and that we should lazyly create any wrapers
 * (such as AttributeDescriptor)as needed.
 * </p>
 * <p>
 * In the interests of going quick we will not perform any tests here and allow
 * a subclass classed StrickSimpleFeature to provide a decent implementation as
 * required.
 * </p>
 * 
 * @author Jody Garnett
 */
public class ArraySimpleFeature implements SimpleFeature {
	SimpleFeatureType type;

	private String id;

	Object values[];

	private List list; // list of values

	private List attributes; // attribute descriptors (wrapers)

	private int defaultIndex;

	private Map userData;

	public ArraySimpleFeature(SimpleFeatureType type, String id) {
		this(type, id, new Object[type.getProperties().size()]);
	}

	public ArraySimpleFeature(SimpleFeatureType type, String id, Object[] values) {
		this.type = type;
		this.id = id;
		this.values = values;
		this.list = Arrays.asList(values);
		this.defaultIndex = type.indexOf(type.getDefaultGeometry().getName()
				.getLocalPart());
	}

	public List getTypes() {
		return type.getTypes();
	}

	public Object getUserData(Object key) {
		if( userData != null ){
			return userData.get( key );
		}
		return null;
	}
	public void putUserData(Object key, Object value) {
		if( userData == null ){
			userData = new HashMap();
		}
		userData.put( key, value );
	}
	
	public List getValues() {
		return list;
	}

	public AttributeType getType() {
		return type;
	}

	public Object getValue(String name) {
		return values[type.indexOf(name)];
	}

	public Object getValue(int index) {
		return values[index];
	}

	public void setValue(String name, Object value) {
		values[type.indexOf(name)] = value;
	}

	public void setValue(int index, Object value) {
		values[index] = value;
	}

	public int getNumberOfAttributes() {
		return values.length;
	}

	public Object getDefaultGeometryValue() {
		int index = type.indexOf(type.getDefaultGeometry().getName()
				.getLocalPart());
		return getValue(index);
	}

	/**
	 * List of AttributeDescriptors in the order indicated by SimpleFeatureType.
	 * 
	 * This method will mostly be called by generic feature code that has not
	 * been optimized for the helper methods contained in this class.
	 * 
	 * AttributeDescriptors here will be wrappers around two core real objects:
	 * <ul>
	 * <li>value[index]
	 * <li>type.get( index )
	 * </ul>
	 */
	public synchronized Object getValue() {
		if (attributes == null) {
			attributes = new ArrayList(values.length);
			final int LENGTH = values.length;
			for (int i = 0; i < LENGTH; i++) {
				if( i == defaultIndex ){
					list.add( new IndexGeometryAttribute(this, i));
				}
				else {
					list.add(new IndexAttribute(this, i));
				}
			}
		}
		return attributes;
	}

	/**
	 * Always will return null because.
	 * <p>
	 * To be used with a descriptor this SimpleFeature would be being used as an
	 * attirbute in another Feature, not something we are interested in please
	 * just go use a normal implementation, we are optimized and limited.
	 */
	public AttributeDescriptor getDescriptor() {
		return null;
	}

	/**
	 * We are expecting an List of Attributes with type and values compattible
	 * with our contents.
	 */
	public void setValue(Object list) throws IllegalArgumentException {
		List attributes = (List) list;
		final int LENGTH = values.length;
		for (int i = 0; i < LENGTH; i++) {
			Attribute attribute = (Attribute) attributes.get(i);
			values[i] = attribute.getValue();
		}
	}

	public Collection attributes() {
		return getAttributes();
	}

	public Collection associations() {
		return Collections.EMPTY_LIST;
	}

	public List getValue(Name name) {
		return Collections.singletonList( getValue(name.getLocalPart()));
	}

	public boolean nillable() {
		return false;
	}

	public String getID() {
		return id; // Feature ID
	}

	public PropertyDescriptor descriptor() {
		return null; // lack descriptor, not suitable use for SimpleFeature
	}

	public Name name() {
		return null; // lack name
	}

	public CoordinateReferenceSystem getCRS() {
		return type.getCRS();
	}

	public void setCRS(CoordinateReferenceSystem crs) {
		if (!type.getCRS().equals(crs)) {
			throw new IllegalArgumentException("Provided crs does not match");
		}
	}

	public BoundingBox getBounds() {
		return getDefaultGeometry().getBounds();
	}

	public GeometryAttribute getDefaultGeometry() {
		return (GeometryAttribute) attributes.get(defaultIndex);
	}

	public void setDefaultGeometryValue(GeometryAttribute geom) {
		values[defaultIndex] = geom.getValue();
	}

    public Object operation(String arg0, Object arg1) {
        throw new UnsupportedOperationException("operation not supported yet");
    }

    public Object operation(Name arg0, List arg1) {
        throw new UnsupportedOperationException("operation not supported yet");
    }

	public List getAttributes() {
		return attributes;
	}

	public void setDefaultGeometryValue(Object geometry) {
	}

	public void setValue(List /*<Property>*/values) {
		setValues(values);
	}

	public void setValues(List /*<Attribute>*/values) {
		// assume values are in the right order
		for(int i = 0; i < this.values.length; i++){
			this.values[i] = values.get(i);
		}
	}

	public void setValues(Object[] values) {
		System.arraycopy(values, 0, this.values, 0, this.values.length);
	}

	public void setDefaultGeometry(GeometryAttribute geometryAttribute) {
		setDefaultGeometryValue(geometryAttribute == null? null : geometryAttribute.getValue());
	}

	public List get(Name name) {
		int index = type.indexOf(name.getLocalPart());
		Object value = getValue(index);
		Attribute att;
		if( index == defaultIndex ){
			att = new IndexGeometryAttribute(this, index);
		}else {
			att = new IndexAttribute(this, index);
		}
		return Collections.singletonList(att);
	}

}
