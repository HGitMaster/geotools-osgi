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
package org.geotools.jdbc;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.CollectionFeatureReader;
import org.geotools.data.FeatureReader;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;


public abstract class JDBCFeatureStoreTest extends JDBCTestSupport {
    JDBCFeatureStore featureStore;

    protected void setUp() throws Exception {
        super.setUp();

        featureStore = (JDBCFeatureStore) dataStore.getFeatureSource(tname("ft1"));
    }

    public void testAddFeatures() throws IOException {
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(featureStore.getSchema());
        DefaultFeatureCollection collection = new DefaultFeatureCollection(null,
                featureStore.getSchema());

        for (int i = 3; i < 6; i++) {
            b.set(aname("intProperty"), new Integer(i));
            b.set(aname("geometry"), new GeometryFactory().createPoint(new Coordinate(i, i)));
            collection.add(b.buildFeature(null));
        }

        List<FeatureId> fids = featureStore.addFeatures(collection);
        assertEquals(3, fids.size());

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureStore.getFeatures();
        assertEquals(6, features.size());

        FilterFactory ff = dataStore.getFilterFactory();

        for (Iterator f = fids.iterator(); f.hasNext();) {
            FeatureId identifier = (FeatureId) f.next();
            String fid = identifier.getID();
            Id filter = ff.id(Collections.singleton(identifier));

            features = featureStore.getFeatures(filter);
            assertEquals(1, features.size());

            Iterator iterator = features.iterator();
            assertTrue(iterator.hasNext());

            SimpleFeature feature = (SimpleFeature) iterator.next();
            assertEquals(fid, feature.getID());
            assertFalse(iterator.hasNext());

            features.close(iterator);
        }
    }
    
    /**
     * Check null encoding is working properly
     * @throws IOException
     */
    public void testAddNullAttributes() throws IOException {
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(featureStore.getSchema());
        SimpleFeature nullFeature = b.buildFeature("testId");
        featureStore.addFeatures(Arrays.asList(nullFeature));
    }

    public void testSetFeatures() throws IOException {
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(featureStore.getSchema());
        DefaultFeatureCollection collection = new DefaultFeatureCollection(null,
                featureStore.getSchema());

        for (int i = 3; i < 6; i++) {
            b.set(aname("intProperty"), new Integer(i));
            b.set(aname("geometry"), new GeometryFactory().createPoint(new Coordinate(i, i)));
            collection.add(b.buildFeature(null));
        }

         FeatureReader<SimpleFeatureType, SimpleFeature> reader = new CollectionFeatureReader(collection, collection.getSchema());
        featureStore.setFeatures(reader);

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureStore.getFeatures();
        assertEquals(3, features.size());

        Iterator iterator = features.iterator();
        HashSet numbers = new HashSet();
        numbers.add(new Integer(3));
        numbers.add(new Integer(4));
        numbers.add(new Integer(5));

        for (int i = 3; iterator.hasNext(); i++) {
            SimpleFeature feature = (SimpleFeature) iterator.next();
            assertTrue(numbers.contains(((Number)feature.getAttribute(aname("intProperty"))).intValue()));
            numbers.remove(feature.getAttribute(aname("intProperty")));
        }

        features.close(iterator);
    }

    public void testModifyFeatures() throws IOException {
        SimpleFeatureType t = featureStore.getSchema();
        featureStore.modifyFeatures(new AttributeDescriptor[] { t.getDescriptor(aname("stringProperty")) },
            new Object[] { "foo" }, Filter.INCLUDE);

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureStore.getFeatures();
        Iterator i = features.iterator();

        assertTrue(i.hasNext());

        while (i.hasNext()) {
            SimpleFeature feature = (SimpleFeature) i.next();
            assertEquals("foo", feature.getAttribute(aname("stringProperty")));
        }

        features.close(i);
    }

    public void testRemoveFeatures() throws IOException {
        FilterFactory ff = dataStore.getFilterFactory();
        Filter filter = ff.equals(ff.property(aname("intProperty")), ff.literal(1));

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureStore.getFeatures();
        assertEquals(3, features.size());

        featureStore.removeFeatures(filter);
        assertEquals(2, features.size());

        featureStore.removeFeatures(Filter.INCLUDE);
        assertEquals(0, features.size());
    }
}
