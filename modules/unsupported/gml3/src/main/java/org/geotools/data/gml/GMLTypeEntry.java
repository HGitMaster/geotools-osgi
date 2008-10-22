/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.gml;

import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.store.ActiveTypeEntry;
import org.opengis.feature.simple.SimpleFeatureType;

public class GMLTypeEntry extends ActiveTypeEntry {

	public GMLTypeEntry(DataStore parent, SimpleFeatureType schema, Map metadata) {
		super(parent, schema, metadata);
	}
	
	GMLDataStore parent() {
		return (GMLDataStore) parent;
	}
	
	public FeatureSource createFeatureSource() {
		return new GMLFeatureSource( this );
	}
	
}
