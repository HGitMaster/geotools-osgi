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
package org.geotools.caching.firstdraft.quadtree;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.caching.firstdraft.quadtree.Node;
import org.geotools.caching.firstdraft.quadtree.QuadTree;
import org.geotools.caching.firstdraft.quadtree.QueryStrategy;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IData;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.INode;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IVisitor;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.Region;
import org.geotools.caching.firstdraft.util.Generator;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;


public class QuadTreeXest extends TestCase {
    protected FeatureType type;
    protected List data;
    protected QuadTree tree;

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

    protected void setUp() {
        data = createDataSet(2000);
        tree = new QuadTree(new Region(new double[] { 0d, 0d }, new double[] { 1000d, 1000d }));
    }

    public static Test suite() {
        return new TestSuite(QuadTreeXest.class);
    }

    public void testInsertData() {
        for (Iterator it = data.iterator(); it.hasNext();) {
            Feature f = (Feature) it.next();
            tree.insertData(f.getID().getBytes(), toRegion(f.getBounds()), f.hashCode());
        }
    }

    public void testCountQuery() {
        testInsertData();

        CountingVisitor v1 = new CountingVisitor();
        tree.intersectionQuery(new Region(new double[] { 0d, 0d }, new double[] { 1000d, 1000d }),
            v1);
        //System.out.println("Nodes = " + v1.nodes + " ; Data = " + v1.data) ;
        // some data overlap in the tree, so we may count more than actual
        assertTrue(v1.data >= 2000);

        CountingVisitor v2 = new CountingVisitor();
        tree.intersectionQuery(new Region(new double[] { 0d, 0d }, new double[] { 1000d, 1000d }),
            v2);
        //System.out.println("Nodes = " + v2.nodes + " ; Data = " + v2.data) ;
        assertEquals(v1.data, v2.data);
        assertEquals(v2.nodes, v2.nodes);
    }

    public void testIntersectionQuery() {
        testInsertData();

        YieldingVisitor v = new YieldingVisitor();
        long start = System.currentTimeMillis();
        tree.intersectionQuery(new Region(new double[] { 0d, 0d }, new double[] { 1000d, 1000d }), v);

        long q1 = System.currentTimeMillis() - start;
        assertEquals(2000, v.yields.size());
        v = new YieldingVisitor();
        start = System.currentTimeMillis();
        tree.intersectionQuery(new Region(new double[] { 250d, 250d }, new double[] { 500d, 500d }),
            v);

        long q2 = System.currentTimeMillis() - start;
        assertTrue(v.yields.size() < 2000);

        /* Runtime context may cause this to fail ...
           but this is what we expect of an index */

        // assertTrue(q2 < q1) ;
        if (q2 >= q1) {
            org.geotools.util.logging.Logging.getLogger("org.geotools.caching.quadtree")
                  .log(Level.SEVERE, "Index not fast as expected.");
        }
    }

    public void testContainementQuery() {
        Region r = new Region(new double[] { 10, 15 }, new double[] { 15, 20 });
        tree.insertData(null, r, 0);

        NodeEnvelopeVisitor v = new NodeEnvelopeVisitor();
        tree.containmentQuery(r, v);
        assertTrue(v.lastNode.getShape().contains(r));
    }

    public void testMaximumDepth() {
        Region r = new Region(new double[] { 0d, 0d }, new double[] { 1000d, 1000d });
        int height = 5;
        tree = new QuadTree(r, height);
        testInsertData();

        LevelCountVisitor v = new LevelCountVisitor();
        tree.intersectionQuery(r, v);
        assertTrue((height + 1) >= v.getNumLevels());
        assertEquals(height, v.getMaxLevel());
        assertTrue(0 <= v.getMinLevel());

        //printTree(tree, System.out) ;
    }

    public void testRootExpansion() {
        // expanding in all possible directions
        expandTree(410d, 510d);
        expandTree(250d, 300d);
        expandTree(410d, 300d);
        expandTree(250d, 510d);
    }

    protected void expandTree(double x, double y) {
        assertTrue((x >= 0) && (x <= 1000));
        assertTrue((y >= 0) && (y <= 1000));

        double xmin = 300d;
        double ymin = 400d;
        double xmax = 350d;
        double ymax = 450d;
        Region r = new Region(new double[] { xmin, ymin }, new double[] { xmax, ymax });
        tree = new QuadTree(r);

        double nxmin = (x < xmin) ? x : xmin;
        double nxmax = (x > xmax) ? x : xmax;
        double nymin = (y < ymin) ? y : ymin;
        double nymax = (y > ymax) ? y : ymax;
        tree.insertData(null, new Region(new double[] { xmin, ymin }, new double[] { xmax, ymax }),
            1);
        tree.insertData(null,
            new Region(new double[] { nxmin, nymin }, new double[] { nxmax, nymax }), 2);

        CountingVisitor v = new CountingVisitor();
        tree.intersectionQuery(new Region(new double[] { 0d, 0d }, new double[] { 1000d, 1000d }), v);
        //printTree(tree, System.out) ;
        assertEquals(2, v.data);
    }

    // Test Utilities

    /** Transform a JTS Envelope to a Region
     *
     * @param e JTS Envelope
     * @return
     */
    protected static Region toRegion(final Envelope e) {
        Region r = new Region(new double[] { e.getMinX(), e.getMinY() },
                new double[] { e.getMaxX(), e.getMaxY() });

        return r;
    }

    protected void printTree(final QuadTree t, final PrintStream out) {
        t.queryStrategy(new QueryStrategy() {
                Stack nodes = new Stack();

                public Node getNextNode(Node current, boolean[] hasNext) {
                    out.println("@level " + current.getLevel() + " : " + current);

                    for (int i = 0; i < current.getChildrenCount(); i++) {
                        nodes.add(0, current.getSubNode(i));
                    }

                    if (!nodes.isEmpty()) {
                        hasNext[0] = true;

                        return (Node) nodes.pop();
                    } else {
                        hasNext[0] = false;

                        return null;
                    }
                }
            });
    }

    class CountingVisitor implements IVisitor {
        int data = 0;
        int nodes = 0;

        public void visitData(IData d) {
            data++;

            //System.out.println(new String(d.getData())) ;
        }

        public void visitNode(INode n) {
            nodes++;
        }
    }

    class YieldingVisitor implements IVisitor {
        HashMap yields = new HashMap();

        public void visitData(IData d) {
            yields.put(new String(d.getData()), null);
        }

        public void visitNode(INode n) {
            // do nothing
        }
    }

    class NodeEnvelopeVisitor implements IVisitor {
        INode lastNode = null;

        public void visitData(IData d) {
            // TODO Auto-generated method stub
        }

        public void visitNode(INode n) {
            lastNode = n;
        }
    }

    class LevelCountVisitor implements IVisitor {
        int minLevel = -1;
        int maxLevel = -1;

        public void visitData(IData d) {
            // TODO Auto-generated method stub
        }

        public void visitNode(INode n) {
            if (maxLevel == -1) {
                minLevel = n.getLevel();
                maxLevel = minLevel;
            } else if (minLevel > n.getLevel()) {
                minLevel = n.getLevel();
            }
        }

        public int getNumLevels() {
            return maxLevel - minLevel + 1;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        public int getMinLevel() {
            return minLevel;
        }
    }
}
