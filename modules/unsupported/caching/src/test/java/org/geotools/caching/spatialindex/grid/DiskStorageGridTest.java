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
package org.geotools.caching.spatialindex.grid;

import junit.framework.Test;
import junit.framework.TestSuite;
import java.io.IOException;
import java.util.Properties;
import org.geotools.caching.spatialindex.AbstractSpatialIndex;
import org.geotools.caching.spatialindex.AbstractSpatialIndexTest;
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.Storage;
import org.geotools.caching.spatialindex.store.BufferedDiskStorage;


//import org.geotools.caching.spatialindex.store.DiskStorage;
public class DiskStorageGridTest extends AbstractSpatialIndexTest {
    Grid index;

    public static Test suite() {
        return new TestSuite(DiskStorageGridTest.class);
    }

    @Override
    protected AbstractSpatialIndex createIndex() {
        Storage storage = BufferedDiskStorage.createInstance();
        //        Storage storage = DiskStorage.createInstance();
        index = new Grid(new Region(universe), 100, storage);

        return index;
    }

    public void testInsertion() {
        super.testInsertion();
        System.out.println("Root insertions = " + index.root_insertions);
    }

    public void testWarmStart() throws IOException {
        Properties pset = index.getIndexProperties();
        pset.store(System.out, "Grid property set");
        index.flush();
        index = (Grid) Grid.createInstance(pset);
        super.index = index;
        testIntersectionQuery();
    }
}
