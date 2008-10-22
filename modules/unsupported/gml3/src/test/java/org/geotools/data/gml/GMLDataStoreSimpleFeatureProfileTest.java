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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.feature.FeatureCollection;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class GMLDataStoreSimpleFeatureProfileTest extends TestCase {

	GMLDataStore dataStore;
	
	protected void setUp() throws Exception {
		
		String location = getClass().getResource( "dataset-sf0a.xml" ).toString();
		
		String namespace = "http://cite.opengeospatial.org/gmlsf";
		String schemaLocation = getClass().getResource( "cite-gmlsf0a.xsd" ).toString();
		
		dataStore = new GMLDataStore( 
			location, new ApplicationSchemaConfiguration( namespace, schemaLocation ) 
		);
	}
	
	public void testGetTypeNames() throws Exception {
		List names = Arrays.asList( dataStore.getTypeNames() );
		
		assertTrue( names.contains( "PrimitiveGeoFeature") );
		assertTrue( names.contains( "AggregateGeoFeature") );
		assertTrue( names.contains( "Entit\u00e9G\u00e9n\u00e9rique") );
	}
	
	public void testGetSchema1() throws IOException {
		SimpleFeatureType featureType = 
			dataStore.getSchema( "PrimitiveGeoFeature" );
		assertNotNull( featureType );
		
		assertNotNull( featureType.getAttribute( "pointProperty" ) );
		assertNotNull( featureType.getAttribute( "curveProperty" ) );
		assertNotNull( featureType.getAttribute( "surfaceProperty" ) );
	}
	
	public void testGetSchema2() throws IOException {
	    SimpleFeatureType featureType = 
			dataStore.getSchema( "AggregateGeoFeature" );
		assertNotNull( featureType );
		
		assertNotNull( featureType.getAttribute( "multiPointProperty" ) );
		assertNotNull( featureType.getAttribute( "multiCurveProperty" ) );
		assertNotNull( featureType.getAttribute( "multiSurfaceProperty" ) );
	}
	
	public void testGetSchema3() throws Exception {
	    SimpleFeatureType featureType = 
			dataStore.getSchema( "Entit\u00e9G\u00e9n\u00e9rique" );
		assertNotNull( featureType );
		assertNotNull( featureType.getAttribute( "attribut.G\u00e9om\u00e9trie" ) );
		
	}
	
	public void testGetFeatures1() throws Exception {
		FeatureCollection features = dataStore.getFeatureSource( "PrimitiveGeoFeature" ).getFeatures();
		assertEquals( 5, features.size() );
		
		Iterator iterator = features.iterator();
		assertTrue( iterator.hasNext() );
		try {
			SimpleFeature f = (SimpleFeature) iterator.next();
			assertEquals( "f001", f.getID() );
		}
		finally {
			features.close( iterator );
		}
	}
	
	public void testGetFeatures2() throws Exception {
		FeatureCollection features = dataStore.getFeatureSource( "AggregateGeoFeature" ).getFeatures();
		assertEquals( 4, features.size() );
		
		Iterator iterator = features.iterator();
		assertTrue( iterator.hasNext() );
		try {
		    SimpleFeature f = (SimpleFeature) iterator.next();
			assertEquals( "f005", f.getID() );
		}
		finally {
			features.close( iterator );
		}
	}
}
