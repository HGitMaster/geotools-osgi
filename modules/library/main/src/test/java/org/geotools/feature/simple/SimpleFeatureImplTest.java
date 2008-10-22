/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.feature.simple;

import junit.framework.TestCase;

import org.geotools.data.DataUtilities;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class SimpleFeatureImplTest extends TestCase {
    
    SimpleFeatureType schema;
    SimpleFeature feature;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        schema = DataUtilities.createType("buildings", "_=the_geom:MultiPolygon,name:String,ADDRESS:String");
        feature = SimpleFeatureBuilder.build(schema, new Object[] {null, "ABC", "Random Road, 12"}, "building.1");
    }
    
    // see GEOT-2061
    public void testGetProperty() {
        assertEquals("ABC", feature.getProperty("name").getValue());
        assertNull(feature.getProperty("NOWHERE"));
        assertEquals(0, feature.getProperties("NOWHERE").size());
    }
}
