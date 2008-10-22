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

import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.simple.SimpleFeature;

public class GMLIteratorTest extends GMLDataStoreTestSupport {

	public void test() throws Exception {
		GMLIterator iterator = new GMLIterator( (GMLTypeEntry) dataStore.entry( "TestFeature" ) );
		
		List features = new ArrayList();
		while( iterator.hasNext() ) {
			features.add( iterator.next() );
		}
		
		assertEquals( 3, features.size() );
		for ( int i = 0; i < features.size(); i++ ) {
			SimpleFeature feature = (SimpleFeature) features.get( i );
			assertNotNull( feature );
			assertEquals( "" + i, feature.getID() );
		}
	}
}
