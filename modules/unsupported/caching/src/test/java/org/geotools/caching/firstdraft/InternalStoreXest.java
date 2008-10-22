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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.geotools.caching.firstdraft.InternalStore;
import org.geotools.caching.firstdraft.impl.HashMapInternalStore;
import org.geotools.caching.firstdraft.impl.SimpleHashMapInternalStore;
import org.geotools.caching.firstdraft.util.Generator;
import org.geotools.feature.Feature;


public class InternalStoreXest extends TestCase {
    public static Test suite() {
        return new TestSuite(InternalStoreXest.class);
    }

    public void testSimpleHashMapInternalStore() {
        SimpleHashMapInternalStore simple = new SimpleHashMapInternalStore();
        testStore(simple);
    }

    public void testHashMapInternalStore() {
        SimpleHashMapInternalStore simple = new SimpleHashMapInternalStore();
        HashMapInternalStore hash = new HashMapInternalStore(100, simple);
        testStore(hash);
    }

    protected void testStore(InternalStore tested) {
        Random rand = new Random();
        List features = new ArrayList();
        Generator gen = new Generator(1000, 1000);

        for (int i = 0; i < 1000; i++) {
            Feature f = gen.createFeature(i);
            features.add(f);
            tested.put(f);
        }

        for (int i = 0; i < 10000; i++) {
            int e = rand.nextInt(1000);
            Feature f = (Feature) features.get(e);
            Feature retrieved = tested.get(f.getID());
            assertTrue(f.equals(retrieved));
        }
    }
}
