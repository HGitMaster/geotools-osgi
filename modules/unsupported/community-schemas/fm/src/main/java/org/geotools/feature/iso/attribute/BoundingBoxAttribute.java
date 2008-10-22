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

package org.geotools.feature.iso.attribute;

import org.geotools.feature.iso.AttributeImpl;
import org.opengis.feature.type.AttributeType;
import org.opengis.geometry.BoundingBox;

public class BoundingBoxAttribute extends AttributeImpl implements
		org.opengis.feature.simple.BoundingBoxAttribute {

	public BoundingBoxAttribute(BoundingBox content, AttributeType type) {
		super(content,type,null);
	}

	public BoundingBox getBoundingBox() {
		return (BoundingBox)getValue();
	}

	public void setBoundingBox(BoundingBox newValue) {
		setValue(newValue);
	}
}
