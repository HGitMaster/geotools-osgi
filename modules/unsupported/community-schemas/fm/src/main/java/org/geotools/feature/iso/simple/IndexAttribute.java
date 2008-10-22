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

import java.util.List;

import org.opengis.feature.Attribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;

/**
 * Attribute that delegates to its parent array features based on index.
 * 
 * @author Jody
 */
class IndexAttribute implements Attribute {
	/**
	 * 
	 */
	ArraySimpleFeature feature;

	int index;

	IndexDescriptor descriptor;

	public IndexAttribute(ArraySimpleFeature feature, int index) {
		this.feature = feature;
		this.index = index;
		descriptor = new IndexDescriptor(this.feature, index);
	}

	public AttributeDescriptor getDescriptor() {
		return descriptor;
	}

	public boolean nillable() {
		return true;
	}

	public AttributeType getType() {
		return this.feature.type.getType(index);
	}

	public String getID() {
		return null;
	}

	public Object getValue() {
		return this.feature.values[index];
	}

	public void setValue(Object value) throws IllegalArgumentException {
		this.feature.values[index] = value;
	}

	public PropertyDescriptor descriptor() {
		return descriptor;
	}

	public Name name() {
		return descriptor.getName();
	}

    public Object operation(Name arg0, List arg1) {
        throw new UnsupportedOperationException("operation not supported yet");
    }

}