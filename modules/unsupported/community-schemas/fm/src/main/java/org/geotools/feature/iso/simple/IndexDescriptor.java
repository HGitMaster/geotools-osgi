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

import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyType;

class IndexDescriptor implements AttributeDescriptor {
	/**
	 * 
	 */
	ArraySimpleFeature feature;
	int index;

	protected IndexDescriptor(ArraySimpleFeature feature, int index) {
		this.feature = feature;
		this.index = index;
	}

	public boolean isNillable() {
		return true;
	}

	public AttributeType getType() {
		return this.feature.type.getType(index);
	}

	public int getMinOccurs() {
		return 1;
	}

	public int getMaxOccurs() {
		return 1;
	}

	public void putUserData(Object arg0, Object arg1) {
	}

	public Object getUserData(Object arg0) {
		return null;
	}

	public Name getName() {
		return this.feature.type.getType(index).getName();
	}

	public PropertyType type() {
		return this.feature.type.getType(index);
	}

	public Object getDefaultValue() {
		return null;
	}
}