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
package org.geotools.data.gpx.test;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.geotools.TestData;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.gpx.GpxDataStore;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


public class GpxTest extends TestCase {
    public void testGpxDataStore() throws IOException {
        URL u = TestData.url(this, "folder with spaces/test1.gpx");
        
        DataStore ds = FileDataStoreFinder.getDataStore(u);

        assertNotNull("Unable to load GPX file, DataStoreFactory failed.", ds);
        assertTrue("Opening gpx test file, opened by other DataStore", ds instanceof GpxDataStore);

        String[] types = ds.getTypeNames();
        Set typeSet = new TreeSet();
        Collections.addAll(typeSet, types);

        assertTrue("point type should be supported", typeSet.contains(GpxDataStore.TYPE_NAME_POINT));
        assertTrue("track type should be supported", typeSet.contains(GpxDataStore.TYPE_NAME_TRACK));
        assertTrue("track type should be supported", typeSet.contains(GpxDataStore.TYPE_NAME_ROUTE));
        
        FeatureSource<SimpleFeatureType, SimpleFeature> points = ds.getFeatureSource(GpxDataStore.TYPE_NAME_POINT);

        // exactly 1 "point" in thest file;
        FeatureIterator<SimpleFeature> it = points.getFeatures().features();

        int cnt = 0;

        while (it.hasNext()) {
            Feature f = it.next();

                System.out.println(f);

            cnt++;
        }

        assertTrue("incorrect feature count in point feature class", cnt == 1);
    }
}
