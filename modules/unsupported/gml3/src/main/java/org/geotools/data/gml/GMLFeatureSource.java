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

import java.io.IOException;

import org.geotools.data.store.AbstractFeatureSource2;
import org.geotools.feature.FeatureCollection;

public class GMLFeatureSource extends AbstractFeatureSource2 {

	public GMLFeatureSource(GMLTypeEntry entry) {
		super(entry);
	}

	public FeatureCollection getFeatures() throws IOException {
		return new GMLFeatureCollection( (GMLTypeEntry) entry );
	}
}
