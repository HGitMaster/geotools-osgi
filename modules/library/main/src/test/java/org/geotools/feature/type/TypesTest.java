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
package org.geotools.feature.type;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.AttributeImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.Attribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;

public class TypesTest extends TestCase {
    public void testWithoutRestriction(){
        // used to prevent warning
        FilterFactory fac = CommonFactoryFinder.getFilterFactory(GeoTools
                .getDefaultHints());

        String attributeName = "string";
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder(); //$NON-NLS-1$
        builder.setName("test");
        builder.add(attributeName, String.class);
        SimpleFeatureType featureType = builder.buildFeatureType();
        
        SimpleFeature feature = SimpleFeatureBuilder.build(featureType, new Object[]{"Value"},
                null);
        
        assertNotNull( feature );        
    }
    /**
     * This utility class is used by Types to prevent attribute modification.
     */
    public void testRestrictionCheck() {
        FilterFactory fac = CommonFactoryFinder.getFilterFactory(GeoTools
                .getDefaultHints());

        String attributeName = "string";
        PropertyIsEqualTo filter = fac.equals(fac.property("."), fac
                .literal("Value"));

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder(); //$NON-NLS-1$
        builder.setName("test");
        builder.restriction(filter).add(attributeName, String.class);
        SimpleFeatureType featureType = builder.buildFeatureType();
        
        SimpleFeature feature = SimpleFeatureBuilder.build(featureType, new Object[]{"Value"},
                null);
        
        assertNotNull( feature );

    }
}
