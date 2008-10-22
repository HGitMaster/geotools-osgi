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
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.caching.firstdraft.impl.InMemoryDataCache;
import org.geotools.caching.firstdraft.impl.SpatialQueryTracker;
import org.geotools.caching.firstdraft.util.Generator;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureLocking;
import org.geotools.data.Query;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.geotools.filter.spatial.BBOXImpl;


public class CacheXest extends TestCase {
    private final static short UNCHANGED = 0;
    private final static short OPTIMIZED = 1;
    private final static short EMPTYQUERY = 2;
    private final List querySet = new ArrayList();
    private Collection data = null;
    private FeatureType type = null;

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

    private void createRandomQuerySet(int numberOfQueries) {
        System.out.println("=== Creating Query Set");

        for (int i = 0; i < numberOfQueries; i++) {
            Coordinate p = Generator.pickRandomPoint(new Coordinate(500, 500), 950, 950);
            querySet.add(Generator.createBboxQuery(p, 10, 10));
        }
    }

    private List createDataSet(int numberOfData) {
        System.out.println("=== Creating Data Set");

        Generator gen = new Generator(1000, 1000);
        type = gen.getFeatureType();

        List ret = new ArrayList();

        for (int i = 0; i < numberOfData; i++) {
            ret.add(gen.createFeature(i));
        }

        return ret;
    }

    protected void setUp() throws Exception {
        super.setUp();
        data = createDataSet(2000);
    }

    public static Test suite() {
        return new TestSuite(CacheXest.class);
    }

    public void ztestShapefileStore() throws IOException, MalformedURLException {
        File f = new File("target/test.shp");
        ShapefileDataStore sds = new ShapefileDataStore(f.toURL(), URI.create("testStore"));

        if (!f.exists()) {
            sds.createSchema(type);

            if (sds.getFeatureSource() instanceof FeatureLocking) {
                FeatureLocking fl = (FeatureLocking) sds.getFeatureSource();
                FeatureCollection col = new DefaultFeatureCollection("testStore", type);
                col.addAll(data);
                fl.addFeatures(col);
            }
        } else {
            if (sds.getSchema().equals(type)) {
                System.out.println("Schema ok");
            } else {
                System.out.println(sds.getSchema());
                System.out.println(type);
            }
        }
    }

    public void testTracker() {
        createQuerySet(50);

        SpatialQueryTracker tracker = new SpatialQueryTracker();
        int unchanged = 0;
        int optimized = 0;
        int empty = 0;

        for (Iterator i = querySet.iterator(); i.hasNext();) {
            Query q = (Query) i.next();
            Query m = tracker.match(q);
            short comp = compareQuery(q, m);

            //String msg = "" ;
            if (comp == UNCHANGED) {
                //msg = "Unchanged" ;
                unchanged++;
            } else if (comp == OPTIMIZED) {
                //msg = "Optimized" ;
                optimized++;
            } else if (comp == EMPTYQUERY) {
                //msg = "" ;
                empty++;
            }

            //System.out.println(msg) ;
            tracker.register(m);
        }

        System.out.println("Unchanged=" + unchanged + ", Optimized=" + optimized + ", Empty="
            + empty);
        assertTrue(unchanged > 0);
        assertTrue(optimized > 0);
        assertTrue(empty > ((2 / 3) * querySet.size()));

        Query control = (Query) querySet.get(0);
        Query m = tracker.match(control);
        assertTrue(compareQuery(control, m) == EMPTYQUERY);
        //Query q = Generator.createBboxQuery(new Coordinate(500,500), 10000, 10000) ;
        tracker.unregister(control);
        m = tracker.match(control);
        assertTrue(compareQuery(control, m) == UNCHANGED);
        //System.out.println("Clear index");
        tracker.register(m);
        m = tracker.match(control);
        assertTrue(compareQuery(control, m) == EMPTYQUERY);
        tracker.clear();
        m = tracker.match(control);
        assertTrue(compareQuery(control, m) == UNCHANGED);
    }

    public void testDataCacheConformance() throws IOException, SchemaException {
        createRandomQuerySet(50);

        MemoryDataStore control = new MemoryDataStore();
        control.createSchema(type);
        control.addFeatures(data);

        MemoryDataStore ds = new MemoryDataStore();
        ds.createSchema(type);
        ds.addFeatures(data);

        InMemoryDataCache tested = new InMemoryDataCache(ds);
        Iterator iter = querySet.iterator();

        while (iter.hasNext()) {
            Query q = (Query) iter.next();
            FeatureCollection controlSet = control.getView(q).getFeatures();
            FeatureCollection testedSet = tested.getView(q).getFeatures();
            assertTrue(compareFeatureCollectionByHash(controlSet, testedSet));
        }
    }

    public void testDataCachePerformance() throws IOException {
        createQuerySet(25);

        MemoryDataStore control = new MemoryDataStore();
        control.createSchema(type);
        control.addFeatures(data);

        MemoryDataStore ds = new MemoryDataStore();
        ds.createSchema(type);
        ds.addFeatures(data);

        InMemoryDataCache tested = new InMemoryDataCache(ds);
        long diff = compareDataStores(tested, control, querySet);
        assertTrue(diff < 0);
    }

    public void testDataCachePerformanceRandomQueries()
        throws IOException {
        createRandomQuerySet(50);

        MemoryDataStore control = new MemoryDataStore();
        control.createSchema(type);
        control.addFeatures(data);

        MemoryDataStore ds = new MemoryDataStore();
        ds.createSchema(type);
        ds.addFeatures(data);

        InMemoryDataCache tested = new InMemoryDataCache(ds);
        long diff = compareDataStores(tested, control, querySet);
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
            } else {
                return OPTIMIZED;
            }
        } else {
            return EMPTYQUERY;
        }
    }

    private static QueryStatistics[] runQueries(DataStore ds, List querySet) {
        QueryStatistics[] stats = new QueryStatistics[querySet.size()];
        Iterator iter = querySet.iterator();
        int i = 0;

        while (iter.hasNext()) {
            Query q = (Query) iter.next();
            stats[i] = new QueryStatistics();

            try {
                long startTime = System.currentTimeMillis();

                //System.out.print(".") ;
                FeatureCollection resultSet = ds.getView(q).getFeatures();
                FeatureIterator fiter = resultSet.features();

                while (fiter.hasNext()) {
                    fiter.next();
                }

                resultSet.close(fiter);

                long endTime = System.currentTimeMillis();
                stats[i].setNumberOfFeatures(resultSet.size());
                stats[i].setExecutionTime(endTime - startTime);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SchemaException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            i++;
        }

        return stats;
    }

    protected static long compareDataStores(DataStore ds, DataStore control, List querySet) {
        QueryStatistics[] ds_stats = runQueries(ds, querySet);
        QueryStatistics[] control_stats = runQueries(control, querySet);
        long meanDifference = 0;
        long meanOverhead = 0;
        int overheadCount = 0;
        long meanLeverage = 0;
        int leverageCount = 0;

        for (int i = 0; i < querySet.size(); i++) {
            if (i > 1) {
                long diff = ds_stats[i].getExecutionTime() - control_stats[i].getExecutionTime();
                meanDifference += diff;

                if (diff > 0) {
                    overheadCount++;
                    meanOverhead += diff;
                } else {
                    leverageCount++;
                    meanLeverage += diff;
                }
            }

            //System.out.println("Test: " + ds_stats[i].getNumberOfFeatures() + " features ; " + ds_stats[i].getExecutionTime() + " ms ; " +
            //		"Control: " + control_stats[i].getNumberOfFeatures() + " features ; " + control_stats[i].getExecutionTime() + " ms ; ");
        }

        meanDifference = ((querySet.size() - 2) > 0) ? (meanDifference / (querySet.size() - 2)) : 0;
        meanOverhead = (overheadCount > 0) ? (meanOverhead / overheadCount) : 0;
        meanLeverage = (leverageCount > 0) ? (meanLeverage / leverageCount) : 0;
        System.out.println("Mean execution time difference = " + meanDifference + " ms.");
        System.out.println("Mean overhead = " + meanOverhead + " ms. ("
            + ((100 * overheadCount) / (overheadCount + leverageCount)) + " %)");
        System.out.println("Mean leverage = " + meanLeverage + " ms. ("
            + ((100 * leverageCount) / (overheadCount + leverageCount)) + " %)");

        return meanDifference;
    }

    protected static boolean compareFeatureCollectionByID(FeatureCollection set1,
        FeatureCollection set2) {
        if (set1.size() == set2.size()) {
            FeatureIterator iter = set1.features();
            TreeSet ids1 = new TreeSet();

            while (iter.hasNext()) {
                ids1.add(iter.next().getID());
            }

            set1.close(iter);
            iter = set2.features();

            TreeSet ids2 = new TreeSet();

            while (iter.hasNext()) {
                ids2.add(iter.next().getID());
            }

            set2.close(iter);

            Iterator i2 = ids2.iterator();

            for (Iterator i1 = ids1.iterator(); i1.hasNext();) {
                String id1 = (String) i1.next();
                String id2 = (String) i2.next();

                if (!id1.equals(id2)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    protected static boolean compareFeatureCollectionByHash(FeatureCollection set1,
        FeatureCollection set2) {
        if (set1.size() == set2.size()) {
            FeatureIterator iter = set1.features();
            TreeSet ids1 = new TreeSet();

            while (iter.hasNext()) {
                ids1.add(new Integer(iter.next().hashCode()));
            }

            set1.close(iter);
            iter = set2.features();

            TreeSet ids2 = new TreeSet();

            while (iter.hasNext()) {
                ids2.add(new Integer(iter.next().hashCode()));
            }

            set2.close(iter);

            Iterator i2 = ids2.iterator();

            for (Iterator i1 = ids1.iterator(); i1.hasNext();) {
                Integer id1 = (Integer) i1.next();
                Integer id2 = (Integer) i2.next();

                if (!id1.equals(id2)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }
}
