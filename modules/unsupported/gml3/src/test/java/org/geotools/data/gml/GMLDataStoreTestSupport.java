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

import org.geotools.gml3.ApplicationSchemaConfiguration;

import junit.framework.TestCase;

public class GMLDataStoreTestSupport extends TestCase {

	GMLDataStore dataStore;
	
	protected void setUp() throws Exception {
		String location = getClass().getResource( "test.xml" ).toString();
		String schemaLocation = getClass().getResource( "test.xsd" ).toString();
		
		dataStore = new GMLDataStore( location, new ApplicationSchemaConfiguration( "http://www.geotools.org/test", schemaLocation ) );
	}
	
}
