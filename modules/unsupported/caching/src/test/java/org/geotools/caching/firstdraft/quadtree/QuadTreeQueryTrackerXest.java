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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.filter.Filter;
import org.geotools.caching.firstdraft.quadtree.Node;
import org.geotools.caching.firstdraft.quadtree.QuadTreeQueryTracker;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IData;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.INode;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IVisitor;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.Region;
import org.geotools.caching.firstdraft.util.Generator;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.feature.FeatureType;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.spatial.BBOXImpl;


public class QuadTreeQueryTrackerXest extends TestCase {
    private final static short ERROR = -1;
    private final static short UNCHANGED = 0;
    private final static short OPTIMIZED = 1;
    private final static short EMPTYQUERY = 2;
    private final static short EXPANDED = 3;
    private final List querySet = new ArrayList();
    private FeatureType type;
    private QuadTreeQueryTracker tracker;

    protected void setUp() {
        Generator gen = new Generator(1000, 1000);
        type = gen.getFeatureType();
        tracker = new QuadTreeQueryTracker(new Region(new double[] { 0, 0 },
                    new double[] { 1000, 1000 }), type);
    }

    public static Test suite() {
        return new TestSuite(QuadTreeQueryTrackerXest.class);
    }

    public void testTracker() {
        createQuerySet(50);

        int unchanged = 0;
        int optimized = 0;
        int empty = 0;
        int expanded = 0;

        for (Iterator i = querySet.iterator(); i.hasNext();) {
            Query q = (Query) i.next();
            Query m = tracker.match(q);
            short comp = compareQuery(q, m);

            //String msg = "" ;
            if (comp == UNCHANGED) {
                //msg = "Unchanged" ;
                unchanged++;
                tracker.register(q);
            } else if (comp == OPTIMIZED) {
                //msg = "Optimized" ;
                optimized++;
                tracker.register(q);
            } else if (comp == EMPTYQUERY) {
                //msg = "" ;
                empty++;
            } else if (comp == EXPANDED) {
                expanded++;
                tracker.register(q);
            }
        }

        System.out.println("Unchanged=" + unchanged + ", Optimized=" + optimized + ", Empty="
            + empty + ", Expanded=" + expanded);

        //assertTrue(unchanged > 0);
        //assertTrue(optimized > 0);
        //assertTrue(empty > ((2 / 3) * querySet.size()));
    }

    public void testRegister() {
        createQuerySet(1);

        Query control = (Query) querySet.get(0);
        Query m = tracker.match(control);
        short comp = compareQuery(control, m);
        assertTrue((comp == UNCHANGED) || (comp == EXPANDED));
        tracker.register(m);
        m = tracker.match(control);
        comp = compareQuery(control, m);
        assertEquals(EMPTYQUERY, comp);
        // test for repetibility
        m = tracker.match(control);
        comp = compareQuery(control, m);
        assertEquals(EMPTYQUERY, comp);
    }

    public void testUnregister() {
        /*FilterFactoryImpl ff = new FilterFactoryImpl() ;
           Filter f = ff.bbox(type.getTypeName(), 10, 20, 15, 25, "") ;
           Query q = new DefaultQuery(type.getTypeName(), f) ; */
        createQuerySet(1);

        Query q = (Query) querySet.get(0);
        Query m = tracker.match(q);
        short comp = compareQuery(q, m);
        assertTrue((comp == UNCHANGED) || (comp == EXPANDED));
        tracker.register(q);
        m = tracker.match(q);
        assertEquals(EMPTYQUERY, compareQuery(q, m));
        tracker.unregister(q);
        m = tracker.match(q);
        comp = compareQuery(q, m);
        assertTrue((comp == UNCHANGED) || (comp == EXPANDED));
        // test for repetibility
        tracker.unregister(q);
        m = tracker.match(q);
        comp = compareQuery(q, m);
        assertTrue((comp == UNCHANGED) || (comp == EXPANDED));
    }

    public void testHitting() {
        createQuerySet(50);

        for (Iterator it = querySet.iterator(); it.hasNext();) {
            Query q = (Query) it.next();
            Query m = tracker.match(q);

            if (!m.getFilter().equals(Filter.EXCLUDE)) {
                tracker.register(q);
            }
        }

        tracker.tree.root.entry.getOldestChildAccess();
        tracker.tree.intersectionQuery(tracker.tree.root.getShape(),
            new IVisitor() {
                public void visitData(IData d) {
                    // do nothing
                }

                public void visitNode(INode n) {
                    Node node = (Node) n;
                    System.out.println(node.entry);
                }
            });
    }

    //
    // Utilities //
    //
    private void createQuerySet(int numberOfQueries) {
        System.out.println("=== Creating Query Set");

        Coordinate p = Generator.pickRandomPoint(new Coordinate(500, 500), 950, 950);
        Coordinate last = p;

        for (int i = 0; i < numberOfQueries; i++) {
            querySet.add(Generator.createBboxQuery(p, 100, 100));
            p = Generator.pickRandomPoint(p, 50, 50);
            querySet.add(Generator.createBboxQuery(p, 50, 50));
            p = Generator.pickRandomPoint(p, 20, 20);
            querySet.add(Generator.createBboxQuery(p, 20, 20));

            Coordinate temp = p;
            p = last;
            last = temp;
        }
    }

    private short compareQuery(Query q1, Query q2) {
        if (q1.equals(q2)) {
            return UNCHANGED;
        }

        if ((q1.getFilter() instanceof BBOXImpl) && (q2.getFilter() instanceof BBOXImpl)) {
            BBOXImpl bb = (BBOXImpl) q1.getFilter();
            Envelope env1 = new Envelope(bb.getMinX(), bb.getMaxX(), bb.getMinY(), bb.getMaxY());
            bb = (BBOXImpl) q2.getFilter();

            Envelope env2 = new Envelope(bb.getMinX(), bb.getMaxX(), bb.getMinY(), bb.getMaxY());

            if (env1.equals(env2)) {
                return UNCHANGED;
            } else if (env1.contains(env2)) {
                return OPTIMIZED;
            } else if (env2.contains(env1)) {
                return EXPANDED;
            } else {
                return ERROR;
            }
        } else {
            return EMPTYQUERY;
        }
    }
}
