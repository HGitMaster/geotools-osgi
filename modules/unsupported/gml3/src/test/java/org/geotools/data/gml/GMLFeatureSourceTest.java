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

import java.util.HashSet;
import java.util.Iterator;

import org.geotools.data.DefaultQuery;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.identity.FeatureId;


public class GMLFeatureSourceTest extends GMLDataStoreTestSupport {

	GMLFeatureSource featureSource;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		featureSource = (GMLFeatureSource) dataStore.getFeatureSource( "TestFeature" );
	}
	
	public void testGetFeatures() throws Exception {
		FeatureCollection features = featureSource.getFeatures();
		assertTrue( features instanceof GMLFeatureCollection );
		
		assertEquals( 3, features.size() );
	}
	
	public void testGetFeaturesWithFilter() throws Exception {
		FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
		HashSet fids = new HashSet();
		FeatureId id = ff.featureId( "1" );
		fids.add( id );
		
		Filter idFilter = ff.id( fids );
		FeatureCollection features = featureSource.getFeatures( idFilter );
		assertEquals( 1, features.size() );
	}
	
	public void testGetFeaturesWithQuery() throws Exception {
		FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
		HashSet fids = new HashSet();
		FeatureId id = ff.featureId( "1" );
		fids.add( id );
		
		Filter idFilter = ff.id( fids );
		DefaultQuery query = new DefaultQuery( 
			"TestFeature", idFilter, new String[] { "count" } 
		);
		FeatureCollection features = featureSource.getFeatures( query );
		assertEquals( 1, features.size() );
		
		Iterator i = features.iterator();
		assertTrue( i.hasNext() );
		
		SimpleFeature f = (SimpleFeature) i.next();
		assertNotNull( f );
		
		assertEquals( 1, f.getFeatureType().getAttributeCount() );
		assertNull( f.getDefaultGeometry() );
		assertEquals( new Integer( 1 ), f.getAttribute( "count" ) );
		
		features.close( i );
	}
	
}
