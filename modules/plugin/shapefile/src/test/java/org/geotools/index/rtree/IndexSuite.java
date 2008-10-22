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
package org.geotools.index.rtree;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.geotools.index.rtree.memory.MemoryPageStoreTest;

/**
 * DOCUMENT ME!
 * 
 * @author Tommaso Nolli
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/shapefile/src/test/java/org/geotools/index/rtree/IndexSuite.java $
 */
public class IndexSuite extends TestCase {
    public IndexSuite(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("All Index Tests");

        suite
                .addTestSuite(org.geotools.index.rtree.fs.FileSystemPageStoreTest.class);
        suite
                .addTestSuite(org.geotools.index.rtree.cachefs.FileSystemPageStoreTest.class);
        suite.addTestSuite(MemoryPageStoreTest.class);

        suite.addTestSuite(RTreeTest.class);

        return suite;
    }
}
