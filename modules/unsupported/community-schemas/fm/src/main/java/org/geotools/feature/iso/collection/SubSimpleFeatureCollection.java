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

import org.opengis.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

/**
 * Used as a reasonable default implementation for a subCollection of
 * simple features.
 * <p>
 * Note: to implementors, this is not optimal, please do your own thing - your
 * users will thank you.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research, Inc.
 * @author Justin Deoliveira, The Open Planning Project
 */
public class SubSimpleFeatureCollection extends SubFeatureCollection implements
		SimpleFeatureCollection {

	public SubSimpleFeatureCollection(SimpleFeatureCollection collection,
			Filter filter, FilterFactory factory) {
		super(collection, filter, factory);
	}

	public FeatureCollection subCollection(org.opengis.filter.Filter filter) {
		return new SubSimpleFeatureCollection(this, filter, factory);
	}

	public FeatureType getMemberType() {
		return ((SimpleFeatureCollection) collection).getMemberType();
	}

	public SimpleFeatureCollectionType getFeatureCollectionType() {
		return ((SimpleFeatureCollection)collection).getFeatureCollectionType();
	}

}
