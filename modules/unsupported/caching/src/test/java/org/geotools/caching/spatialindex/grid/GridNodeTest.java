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
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.Iterator;
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.grid.GridData;


public class GridNodeTest extends TestCase {
    GridNode node;
    Region mbr;
    String data = "Sample data : ";
    String data2 = "Sample data 2 : ";

    public static Test suite() {
        return new TestSuite(GridNodeTest.class);
    }

    public void setUp() {
        mbr = new Region(new double[] { 0, 1 }, new double[] { 2, 3 });
        node = new GridNode(new Grid(), mbr);
    }

    public void testConstructor() {
        assertEquals(mbr, node.mbr);
        assertEquals(new Region(mbr), node.mbr);

        GridNode child = new GridNode(new Grid(), mbr);
        assertEquals(0, child.getLevel());
        assertEquals(mbr, node.mbr);
        assertEquals(new Region(mbr), node.mbr);
    }

    void populate() {
        for (int i = 0; i < 10; i++) {
            node.insertData(new GridData(8, mbr, data + i));
            node.insertData(new GridData(16, mbr, data2 + i));
        }
    }

    public void testInsert() {
        populate();
        assertEquals(20, node.num_data);
        assertEquals(8, getId(node, 0));
        assertEquals(data2 + 0, getData(node, 1));
        assertEquals(8, getId(node, 18));
        assertEquals(data2 + 9, getData(node, 19));
    }

    public void testDelete() {
        populate();
        node.deleteData(14);
        node.deleteData(15);
        assertEquals(18, node.num_data);
        assertEquals(8, getId(node, 0));
        assertEquals(data2 + 0, getData(node, 1));
        assertEquals(8, getId(node, 14));
        assertEquals(data + 9, getData(node, 16));
        assertEquals(null, getData(node, 19));

        try {
            node.deleteData(18);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    Object getData(GridNode n, int index) {
        if ((index < 0) || (index > (n.num_data - 1))) {
            return null;
        }

        Iterator<GridData> it = n.data.iterator();

        for (int i = 0; i < index; i++) {
            it.next();
        }

        return it.next().getData();
    }

    int getId(GridNode n, int index) {
        if ((index < 0) || (index > (n.num_data - 1))) {
            return -1;
        }

        Iterator<GridData> it = n.data.iterator();

        for (int i = 0; i < index; i++) {
            it.next();
        }

        return it.next().id;
    }
}
