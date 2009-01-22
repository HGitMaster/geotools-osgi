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
package org.geotools.caching.grid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.geotools.caching.AbstractFeatureCache;
import org.geotools.caching.AbstractFeatureCacheTest;
import org.geotools.caching.CacheOversizedException;
import org.geotools.caching.FeatureCacheException;
import org.geotools.caching.FeatureCollectingVisitor;
import org.geotools.caching.spatialindex.store.MemoryStorage;
import org.geotools.caching.util.Generator;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.type.AttributeTypeImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;


public class NonBlockingGridFeatureCacheTest extends AbstractFeatureCacheTest {
    static boolean testEviction_holistic = false;
    NonBlockingGridFeatureCache cache;

    public static Test suite() {
        return new TestSuite(NonBlockingGridFeatureCacheTest.class);
    }

    @Override
    protected AbstractFeatureCache createInstance(int capacity)
        throws FeatureCacheException, IOException {
        this.cache = new NonBlockingGridFeatureCache((FeatureStore) ds.getFeatureSource(
                    dataset.getSchema().getName()), 100, capacity,
                MemoryStorage.createInstance());

        return this.cache;
    }

    @Override
    public void testEviction() throws IOException, FeatureCacheException {
        super.cache = createInstance(numdata / 2);

        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
                Filter f = Generator.createBboxFilter(new Coordinate(i * 0.1, j * 0.1), 0.1, 0.1);
                FeatureCollection<SimpleFeatureType, SimpleFeature> control = ds.getFeatureSource(dataset.getSchema().getName()).getFeatures(f);
                FeatureCollection<FeatureType, org.opengis.feature.Feature> c = cache.getFeatures(f);
                
                assertEquals(control.size(), c.size());

                if (!testEviction_holistic && (cache.tracker.getEvictions() > 10)) { // wait to generate a fair amount of evictions,
                                                                                     // and see everything is still working

                    return;
                }
            }
        }

        System.out.println(cache.tracker.getStatistics());
        System.out.println(cache.sourceAccessStats());

        if (!testEviction_holistic) {
            fail("Did not got enough evictions : " + cache.tracker.getEvictions());
        }
    }

    @Override
    public void testPut() throws CacheOversizedException {
        cache.put(dataset);

        FeatureCollectingVisitor v = new FeatureCollectingVisitor((SimpleFeatureType)dataset.getSchema());
        cache.tracker.intersectionQuery(AbstractFeatureCache.convert(unitsquare), v);

        //assertEquals(0, v.getCollection().size());
        List<Envelope> matches = cache.match(unitsquare);

        for (Iterator<Envelope> it = matches.iterator(); it.hasNext();) {
            cache.register(it.next());
        }

        cache.put(dataset);

        v = new FeatureCollectingVisitor((SimpleFeatureType)dataset.getSchema());
        cache.tracker.intersectionQuery(AbstractFeatureCache.convert(unitsquare), v);

        assertEquals(dataset.size(), v.getCollection().size());
    }
    
    /**
     * A test that queries the dataset for a given set of attributes and a maximum number
     * of features (Skips the geometry attribute)
     */
    public void testQuery () throws IOException{
    	int maxfeatures = 10;
    	//build up query
    	SimpleFeatureType schema = dataset.getSchema();
    	ArrayList<String> attributes = new ArrayList<String>();
    	 for( int i = 0; i < schema.getAttributeCount(); i++ ) {
             AttributeDescriptor attr = schema.getDescriptor(i);
             if( !(attr instanceof GeometryDescriptor) ){
            	 attributes.add(attr.getName().getLocalPart());
             }
         }
    	DefaultQuery query = new DefaultQuery(schema.getTypeName(),Filter.INCLUDE, maxfeatures, attributes.toArray(new String[0]), null);
    	    	
    	FeatureCollection features = cache.getFeatures(query);
    	
    	//ensure feature count is < maximum
    	assertTrue(features.size() <= maxfeatures);
    	
    	//ensure attribute count correct
    	SimpleFeatureType ftype = (SimpleFeatureType)features.getSchema();
    	assertEquals(attributes.size(), ftype.getAttributeCount());
    }
    
}
