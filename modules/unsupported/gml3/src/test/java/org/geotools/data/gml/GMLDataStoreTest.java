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

import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeatureType;

public class GMLDataStoreTest extends GMLDataStoreTestSupport {

	
	public void testGetSchema() throws Exception {
		SimpleFeatureType featureType = dataStore.getSchema( "TestFeature" );
		assertNotNull( featureType );
	
		assertEquals( "http://www.geotools.org/test", featureType.getName().getNamespaceURI().toString() );
		assertTrue( featureType.getAttribute( "geom" ) != null );
		assertTrue( featureType.getAttribute( "count" ) != null );
	}
	
	public void testGetTypeNames() throws Exception {
		String[] typeNames = dataStore.getTypeNames();
		assertEquals( 1, typeNames.length );
		assertEquals( "TestFeature", typeNames[ 0 ] );
	}
	
	public void testGetFeatureSource() throws Exception {
		FeatureSource featureSource = dataStore.getFeatureSource( "TestFeature" );
		assertNotNull( featureSource );
	}
	
	public void testGetFeatures() throws Exception {
		FeatureSource featureSource = dataStore.getFeatureSource( "TestFeature" ); 
		FeatureCollection features = featureSource.getFeatures();
		assertEquals( 3, features.size() );
		
	}
	
	
}
