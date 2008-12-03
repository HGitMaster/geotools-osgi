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

import java.util.Iterator;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.And;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.opengis.filter.spatial.BBOX;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


public abstract class JDBCFeatureSourceTest extends JDBCTestSupport {
    ContentFeatureSource featureSource;

    protected void setUp() throws Exception {
        super.setUp();

        featureSource = (JDBCFeatureStore) dataStore.getFeatureSource(tname("ft1"));
    }

    public void testSchema() throws Exception {
        SimpleFeatureType schema = featureSource.getSchema();
        assertEquals(tname("ft1"), schema.getTypeName());
        assertEquals(dataStore.getNamespaceURI(), schema.getName().getNamespaceURI());
        assertTrue(areCRSEqual(CRS.decode("EPSG:4326"), schema.getCoordinateReferenceSystem()));

        assertEquals(4, schema.getAttributeCount());
        assertNotNull(schema.getDescriptor(aname("geometry")));
        assertNotNull(schema.getDescriptor(aname("intProperty")));
        assertNotNull(schema.getDescriptor(aname("stringProperty")));
        assertNotNull(schema.getDescriptor(aname("doubleProperty")));
    }

    public void testBounds() throws Exception {
        ReferencedEnvelope bounds = featureSource.getBounds();
        assertEquals(0l, Math.round(bounds.getMinX()));
        assertEquals(0l, Math.round(bounds.getMinY()));
        assertEquals(2l, Math.round(bounds.getMaxX()));
        assertEquals(2l, Math.round(bounds.getMaxY()));

        assertTrue(areCRSEqual(CRS.decode("EPSG:4326"), bounds.getCoordinateReferenceSystem()));
    }

    public void testBoundsWithQuery() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        PropertyIsEqualTo filter = ff.equals(ff.property(aname("stringProperty")), ff.literal("one"));

        DefaultQuery query = new DefaultQuery();
        query.setFilter(filter);

        ReferencedEnvelope bounds = featureSource.getBounds(query);
        assertEquals(1l, Math.round(bounds.getMinX()));
        assertEquals(1l, Math.round(bounds.getMinY()));
        assertEquals(1l, Math.round(bounds.getMaxX()));
        assertEquals(1l, Math.round(bounds.getMaxY()));

        assertTrue(areCRSEqual(CRS.decode("EPSG:4326"), bounds.getCoordinateReferenceSystem()));
    }

    public void testCount() throws Exception {
        assertEquals(3, featureSource.getCount(Query.ALL));
    }

    public void testCountWithFilter() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        PropertyIsEqualTo filter = ff.equals(ff.property(aname("stringProperty")), ff.literal("one"));

        DefaultQuery query = new DefaultQuery();
        query.setFilter(filter);
        assertEquals(1, featureSource.getCount(query));
    }

    public void testGetFeatures() throws Exception {
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureSource.getFeatures();
        assertEquals(3, features.size());
    }

    public void testGetFeaturesWithFilter() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        PropertyIsEqualTo filter = ff.equals(ff.property(aname("stringProperty")), ff.literal("one"));

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureSource.getFeatures(filter);
        assertEquals(1, features.size());

        Iterator<SimpleFeature> iterator = features.iterator();
        assertTrue(iterator.hasNext());

        SimpleFeature feature = (SimpleFeature) iterator.next();
        assertEquals("one", feature.getAttribute(aname("stringProperty")));
        assertEquals( new Double(1.1), feature.getAttribute( aname("doubleProperty")) );
        features.close(iterator);
    }
    
    public void testGetFeaturesWithInvalidFilter() throws Exception {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        PropertyIsEqualTo f = ff.equals(ff.property("invalidAttribute"), ff.literal(5));

        // make sure a complaint related to the invalid filter is thrown here
        try { 
            FeatureIterator<SimpleFeature> fi = featureSource.getFeatures(f).features();
            fi.close();
            fail("This query should have failed, it contains an invalid filter");
        } catch(Exception e) {
            e.printStackTrace();
            // fine
        }
    }
    
    public void testGetFeaturesWithLogicFilter() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        PropertyIsEqualTo property = ff.equals(ff.property(aname("stringProperty")), ff.literal("one"));
        BBOX bbox = ff.bbox(aname("geometry"), -20, -20, 20, 20, "EPSG:4326");
        And filter = ff.and(property, bbox);

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureSource.getFeatures(filter);
        assertEquals(1, features.size());

        Iterator<SimpleFeature> iterator = features.iterator();
        assertTrue(iterator.hasNext());

        SimpleFeature feature = (SimpleFeature) iterator.next();
        assertEquals("one", feature.getAttribute(aname("stringProperty")));
        assertEquals( new Double(1.1), feature.getAttribute( aname("doubleProperty")) );
        features.close(iterator);
        
    }
    
    public void testCaseInsensitiveFilter() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        PropertyIsEqualTo sensitive = ff.equal(ff.property(aname("stringProperty")), ff.literal("OnE"), true);
        PropertyIsEqualTo insensitive = ff.equal(ff.property(aname("stringProperty")), ff.literal("OnE"), false);
        assertEquals(0, featureSource.getCount(new DefaultQuery(null, sensitive)));
        assertEquals(1, featureSource.getCount(new DefaultQuery(null, insensitive)));
    }

    public void testGetFeaturesWithQuery() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        PropertyIsEqualTo filter = ff.equals(ff.property(aname("stringProperty")), ff.literal("one"));

        DefaultQuery query = new DefaultQuery();
        query.setPropertyNames(new String[] { aname("doubleProperty"), aname("intProperty") });
        query.setFilter(filter);

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureSource.getFeatures(query);
        assertEquals(1, features.size());

        Iterator<SimpleFeature> iterator = features.iterator();
        assertTrue(iterator.hasNext());

        SimpleFeature feature = (SimpleFeature) iterator.next();
        assertEquals(2, feature.getAttributeCount());

        assertEquals(new Double(1.1), feature.getAttribute(0));
        assertNotNull( feature.getAttribute(1));
        features.close(iterator);
    }
    
    public void testGetFeaturesWithInvalidQuery() {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        PropertyIsEqualTo f = ff.equals(ff.property("invalidAttribute"), ff.literal(5));

        // make sure a complaint related to the invalid filter is thrown here
        try { 
            FeatureIterator<SimpleFeature> fi = featureSource.getFeatures(new DefaultQuery("ft1", f)).features();
            fi.close();
            fail("This query should have failed, it contains an invalid filter");
        } catch(Exception e) {
            e.printStackTrace();
            // fine
        }
    }

    public void testGetFeaturesWithSort() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        SortBy sort = ff.sort(aname("stringProperty"), SortOrder.ASCENDING);
        DefaultQuery query = new DefaultQuery();
        query.setSortBy(new SortBy[] { sort });

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureSource.getFeatures(query);
        assertEquals(3, features.size());

        Iterator<SimpleFeature> iterator = features.iterator();
        assertTrue(iterator.hasNext());

        SimpleFeature f = (SimpleFeature) iterator.next();
        assertEquals("one", f.getAttribute(aname("stringProperty")));

        assertTrue(iterator.hasNext());
        f = (SimpleFeature) iterator.next();
        assertEquals("two", f.getAttribute(aname("stringProperty")));

        assertTrue(iterator.hasNext());
        f = (SimpleFeature) iterator.next();
        assertEquals("zero", f.getAttribute(aname("stringProperty")));

        features.close(iterator);

        sort = ff.sort(aname("stringProperty"), SortOrder.DESCENDING);
        query.setSortBy(new SortBy[] { sort });
        features = featureSource.getFeatures(query);

        iterator = features.iterator();
        assertTrue(iterator.hasNext());

        f = (SimpleFeature) iterator.next();
        assertEquals("zero", f.getAttribute(aname("stringProperty")));

        assertTrue(iterator.hasNext());
        f = (SimpleFeature) iterator.next();
        assertEquals("two", f.getAttribute(aname("stringProperty")));

        assertTrue(iterator.hasNext());
        f = (SimpleFeature) iterator.next();
        assertEquals("one", f.getAttribute(aname("stringProperty")));
        features.close(iterator);
    }
    
    public void testGetFeaturesWithMax() throws Exception {
        DefaultQuery q = new DefaultQuery(featureSource.getSchema().getTypeName());
        q.setMaxFeatures(2);
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureSource.getFeatures(q);
        
        // check size
        assertEquals(2, features.size());
        
        // check actual iteration
        Iterator<SimpleFeature> it = features.iterator();
        int count = 0;
        while(it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(2, count);
        features.close(it);
    }
    
}
