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

import java.util.Collections;

import org.geotools.feature.iso.FeatureCollectionImpl;
import org.opengis.feature.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;

public class SimpleFeatureCollectionImpl extends FeatureCollectionImpl
	implements SimpleFeatureCollection {

	public SimpleFeatureCollectionImpl(AttributeDescriptor descriptor, String id) {
		super(Collections.EMPTY_LIST, descriptor, id);
	}
	
	public SimpleFeatureCollectionImpl(SimpleFeatureCollectionType type, String id) {
		super(Collections.EMPTY_LIST, type, id);
	}

	public FeatureType getMemberType() {
		return (FeatureType) memberTypes().iterator().next();
	}

    public SimpleFeatureCollectionType getFeatureCollectionType() {
        throw new UnsupportedOperationException("not implemented yet");
    }
	
}
