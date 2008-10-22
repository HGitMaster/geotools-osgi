/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.caching.firstdraft;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.geotools.caching.firstdraft.impl.InMemoryFeatureCache;
import org.geotools.caching.firstdraft.util.Generator;
import org.geotools.data.DataUtilities;
import org.geotools.data.Query;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.spatial.BBOXImpl;


public class InMemoryFeatureCacheXest extends TestCase {
    protected InMemoryFeatureCache cache;
    protected FeatureType type;
    protected List data;

    protected List createDataSet(int numberOfData) {
        //System.out.println("=== Creating Data Set");
        Generator gen = new Generator(1000, 1000);
        type = gen.getFeatureType();

        List ret = new ArrayList();

        for (int i = 0; i < numberOfData; i++) {
            ret.add(gen.createFeature(i));
        }

        return ret;
    }

    public static Test suite() {
        return new TestSuite(InMemoryFeatureCacheXest.class);
    }

    /*public void setName(String name) {
       super.setName("InMemoryFeatureCache Test") ;
       }*/
    protected void setUp() throws Exception {
        super.setUp();
        data = createDataSet(100);

        MemoryDataStore ds = new MemoryDataStore();
        ds.createSchema(type);
        ds.addFeatures(data);
        cache = new InMemoryFeatureCache(ds, type, 150);
    }

    public void testInvalidTypeException() throws SchemaException {
        try {
            FeatureType t = DataUtilities.createType("test.notype",
                    "id:0,*geom:Geometry,dummy:java.lang.String");
            cache = new InMemoryFeatureCache(cache.getDataStore(), t, 150);
        } catch (FeatureCacheException e) {
            return;
        }

        fail("FeatureCacheException not thrown");
    }

    public void testBasicReadOperations() throws FeatureCacheException {
        Feature f = (Feature) data.get(0);
        Feature c = cache.peek(f.getID());
        assertNull(c);
        c = cache.get(f.getID());
        assertTrue(f.equals(c));
        c = cache.peek(f.getID());
        assertTrue(f.equals(c));
        c = cache.get("noexist");
        assertNull(c);
    }

    public void testReadOperations() throws IOException, FeatureCacheException {
        FeatureCollection fc = cache.getFeatures();
        assertEquals(data.size(), fc.size());
    }

    public void testPut() throws IllegalAttributeException, FeatureCacheException {
        Feature f = DataUtilities.template(type, "put");
        Feature c = cache.peek("put");
        assertNull(c);
        cache.put(f);
        c = cache.peek("put");
        assertTrue(f.equals(c));
    }

    public void testPutAll() {
    }

    public void testRemove() throws FeatureCacheException {
        Feature f = (Feature) data.get(0);
        Feature c = cache.get(f.getID());
        c = cache.remove(f.getID());
        assertTrue(f.equals(c));
        c = cache.peek(f.getID());
        assertNull(c);
        c = cache.get(f.getID());
        assertTrue(f.equals(c));
    }

    public void testSplitFilter() throws IOException, FeatureCacheException {
        Feature f = (Feature) data.get(0);
        FilterFactory ff = new FilterFactoryImpl();
        Envelope env = f.getBounds();
        Filter bb = ff.bbox(type.getPrimaryGeometry().getLocalName(), env.getMinX(), env.getMinY(),
                env.getMaxX(), env.getMaxY(), type.getPrimaryGeometry().getLocalName());
        Filter globalbb = ff.bbox(type.getPrimaryGeometry().getLocalName(), 0, env.getMinY(),
                env.getMinX() + ((env.getMaxX() - env.getMinX()) / 2),
                env.getMinY() + ((env.getMaxY() - env.getMinY()) / 2),
                type.getPrimaryGeometry().getLocalName());

        Filter attfilter = ff.like(ff.property("dummydata"), "Id: 1*");
        Filter filter = ff.and(globalbb, attfilter);
        cache.getFeatures(bb);

        Filter[] split = cache.splitFilter(filter);

        /*System.out.println(split[0]);
           System.out.println(split[1]);
           System.out.println(split[2]);*/
        Filter newbb = ff.bbox(type.getPrimaryGeometry().getLocalName(), 0, env.getMinY(),
                env.getMinX(), env.getMinY() + ((env.getMaxY() - env.getMinY()) / 2),
                type.getPrimaryGeometry().getLocalName());
        assertEquals(split[0], globalbb);
        assertEquals(split[1], newbb);
        assertEquals(split[2], attfilter);
        cache.getFeatures(filter);
        split = cache.splitFilter(filter);
        assertEquals(split[0], globalbb);
        assertEquals(split[1], Filter.EXCLUDE);
        assertEquals(split[2], attfilter);
    }

    public void testCountAndBounds() throws IOException {
        MemoryDataStore ds = new MemoryDataStore();
        ds.createSchema(type);
        ds.addFeatures(data);
        assertEquals(data.size(), cache.getCount(Query.ALL));
        assertEquals(ds.getFeatureSource(type.getTypeName()).getBounds(), cache.getBounds());
        assertEquals(ds.getFeatureSource(type.getTypeName()).getBounds(Query.ALL),
            cache.getBounds(Query.ALL));
    }

    public void testEviction() throws IOException {
        FilterFactory ff = new FilterFactoryImpl();
        Filter all = ff.bbox(type.getPrimaryGeometry().getLocalName(), 0, 0, 1000, 1000, "srs");
        FeatureCollection fc = cache.getFeatures(all);
        assertEquals(data.size(), fc.size());
        fc = cache.getFeatures(all);
        assertEquals(data.size(), fc.size());
        cache.evict();
        fc = cache.getFeatures(all);
        assertEquals(data.size(), fc.size());
        cache.evict();
        cache.evict();
        fc = cache.getFeatures(all);
        assertEquals(data.size(), fc.size());
    }
}
