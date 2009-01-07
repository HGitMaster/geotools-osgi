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
package org.geotools.caching;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.caching.grid.DataUtilities;
import org.geotools.caching.grid.GridFeatureCache;
import org.geotools.caching.spatialindex.store.BufferedDiskStorage;
import org.geotools.caching.spatialindex.store.DiskStorage;
import org.geotools.caching.spatialindex.store.MemoryStorage;
import org.geotools.caching.util.Generator;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.spatial.BBOXImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;


public class BenchMark {
    FeatureSource control;
    AbstractFeatureCache[] sample;
    FeatureCollection dataset;
    int numdata = 5000;
    List<Filter> filterset;
    int numfilters = 150;
    double[] windows = new double[] { .05, .025, .001 };
    double[] windows_real = new double[] {  };

    void createUnitsquareDataSet() {
        Generator gen = new Generator(1, 1, 1025);
        dataset = new DefaultFeatureCollection("Test", Generator.type);

        for (int i = 0; i < numdata; i++) {
            dataset.add(gen.createFeature(i));
        }
    }

    void createUnitsquareFilterSet() {
        filterset = new ArrayList<Filter>(numfilters);

        //Coordinate p = Generator.pickRandomPoint(new Coordinate(0.5, 0.5), .950, .950);
        Coordinate p = new Coordinate(0.5, 0.5);

        for (int i = 0; i < numfilters; i += windows.length) {
            for (int j = 0; j < windows.length; j++) {
                filterset.add(Generator.createBboxFilter(p, windows[j], windows[j]));
                p = Generator.pickRandomPoint(p, windows[j], windows[j]);
            }
        }
    }

    void createFilterSet(Envelope e) {
        filterset = new ArrayList<Filter>(numfilters);

        //Coordinate p = Generator.pickRandomPoint(new Coordinate(0.5, 0.5), .950, .950);
        Coordinate p = new Coordinate(e.centre());
        double width = e.getMaxX() - e.getMinX();
        double height = e.getMaxY() - e.getMinY();

        for (int i = 0; i < numfilters; i += windows.length) {
            for (int j = 0; j < windows.length; j++) {
                BBOXImpl f = (BBOXImpl) Generator.createBboxFilter(p, windows[j], windows[j]);
                f.setPropertyName("road");
                f.setSRS("");
                filterset.add(f);
                p = Generator.pickRandomPoint(p, windows[j], windows[j]);
            }
        }
    }

    void initLocalControl() throws IOException {
        MemoryDataStore ds = new MemoryDataStore();
        ds.createSchema((SimpleFeatureType)dataset.getSchema());
        ds.addFeatures(dataset);
        control = (FeatureStore) ds.getFeatureSource(dataset.getSchema().getName());
    }

    //    import org.geotools.data.wfs.WFSDataStore;
    //    import org.geotools.data.wfs.WFSDataStoreFactory;
    //    import java.net.MalformedURLException;
    //    import java.net.URL;
    //
    //    void initRemoteControl() {
    //        HashMap params = new HashMap();
    //        WFSDataStoreFactory fact = new WFSDataStoreFactory();
    //
    //        try {
    //            params.put(WFSDataStoreFactory.URL.key,
    //                new URL("http://www2.dmsolutions.ca/cgi-bin/mswfs_gmap?version=1.0.0&request=getcapabilities&service=wfs"));
    //
    //            WFSDataStore source = (WFSDataStore) fact.createNewDataStore(params);
    //            control = source.getFeatureSource("road");
    //        } catch (MalformedURLException e) {
    //            e.printStackTrace();
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //    }
    QueryStatistics[] runQueries(FeatureSource fs) {
        QueryStatistics[] stats = new QueryStatistics[filterset.size()];
        Iterator<Filter> iter = filterset.iterator();
        int i = 0;

        while (iter.hasNext()) {
            Filter f = iter.next();
            stats[i] = new QueryStatistics();

            double progress = (100d * i) / filterset.size();

            if ((10 * (int) (progress / 10)) == progress) {
                System.out.print((int) progress + "..");
            }

            try {
                long startTime = System.currentTimeMillis();

                //System.out.print(".") ;
                FeatureCollection resultSet = fs.getFeatures(f);
                FeatureIterator fiter = resultSet.features();

                while (fiter.hasNext()) {
                    fiter.next();
                }

                resultSet.close(fiter);

                long endTime = System.currentTimeMillis();
                stats[i].setNumberOfFeatures(resultSet.size());
                //                System.out.println(stats[i].getNumberOfFeatures()) ;
                stats[i].setExecutionTime(endTime - startTime);
            } catch (IOException e) {
                e.printStackTrace();
            }

            i++;
        }

        System.out.println("done.");

        return stats;
    }

    void printResults(QueryStatistics[] control_stats, QueryStatistics[] ds_stats, PrintStream out) {
        long meanDifference = 0;
        long meanOverhead = 0;
        int overheadCount = 0;
        long meanLeverage = 0;
        int leverageCount = 0;

        boolean conform = true;

        for (int i = 0; i < filterset.size(); i++) {
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

            List<Filter> errors = new ArrayList<Filter>();

            if (ds_stats[i].getNumberOfFeatures() != control_stats[i].getNumberOfFeatures()) {
                conform = false;

                errors.add(filterset.get(i));
                System.err.println("Query " + i + " : Got " + ds_stats[i].getNumberOfFeatures()
                    + " features, expected " + control_stats[i].getNumberOfFeatures());

                runErrorsAgain(errors);
            }

            /*System.out.println("Test: " + ds_stats[i].getNumberOfFeatures() + " features ; "
               + ds_stats[i].getExecutionTime() + " ms ; " + "Control: "
               + control_stats[i].getNumberOfFeatures() + " features ; "
               + control_stats[i].getExecutionTime() + " ms ; "); */
        }

        if (conform) {
            out.println("Query results seem to be ok.");
        } else {
            out.println("Sample did not yield same results as control.");
        }

        meanDifference = ((filterset.size() - 2) > 0) ? (meanDifference / (filterset.size() - 2)) : 0;
        meanOverhead = (overheadCount > 0) ? (meanOverhead / overheadCount) : 0;
        meanLeverage = (leverageCount > 0) ? (meanLeverage / leverageCount) : 0;
        out.println("Mean execution time difference = " + meanDifference + " ms.");
        out.println("Mean overhead = " + meanOverhead + " ms. ("
            + ((100 * overheadCount) / (overheadCount + leverageCount)) + " %)");
        out.println("Mean leverage = " + meanLeverage + " ms. ("
            + ((100 * leverageCount) / (overheadCount + leverageCount)) + " %)");
    }

    void runErrorsAgain(List<Filter> errors) {
        sample[0].clear();

        for (Iterator<Filter> it = errors.iterator(); it.hasNext();) {
            Filter next = it.next();

            try {
                GridInspector inspector = new GridInspector((GridFeatureCache) sample[0]);
                inspector.checkFilterIsCached((BBOXImpl) next);

                List<Envelope> list = inspector.getMatch((BBOXImpl) next);
                FeatureCollection fc = sample[0].getFeatures(next);
                FeatureCollection co = control.getFeatures(next);

                if (fc.size() != co.size()) {
                    List<Feature> missing = compare(fc, co);

                    for (Iterator<Feature> mit = missing.iterator(); mit.hasNext();) {
                        Feature f = mit.next();

                        for (Iterator<Envelope> eit = list.iterator(); eit.hasNext();) {
                            Envelope e = eit.next();
                            Envelope against = AbstractFeatureCache.extractEnvelope((BBOXImpl) next);
                            System.out.println("query enlarged : " + e.contains(against) + " = "
                                + e);

                            FilterFactoryImpl ff = new FilterFactoryImpl();
                            BBOXImpl tr = (BBOXImpl) ff.bbox("", e.getMinX(), e.getMinY(),
                                    e.getMaxX(), e.getMaxY(), "");
                            FeatureCollection dblco = control.getFeatures(tr);
                            compare(dblco, co);
                        }

                        //                        while (true) {
                        sample[0].clear();
                        inspector.findFeature(f);
                        sample[0].getFeatures(next);
                        inspector.findFeature(f);

                        //                        }
                    }

                    System.out.println(sample[0].sourceAccessStats());
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    List<Feature> compare(FeatureCollection fc, FeatureCollection co) {
        System.out.println("Got " + fc.size() + " features, expected " + co.size());

        List<Feature> ret = new ArrayList<Feature>();
        FeatureIterator itco = co.features();

        while (itco.hasNext()) {
            Feature next = itco.next();
            FeatureIterator itfc = fc.features();
            boolean found = false;

            while (itfc.hasNext()) {
                Feature nextfc = itfc.next();

                if (nextfc.equals(next)) {
                    found = true;

                    break;
                }
            }

            if (!found) {
                System.out.println("Not found : " + next.getIdentifier() + "," + next.getBounds()
                    + " (size " + (next.getBounds().getWidth() * next.getBounds().getHeight()));
                ret.add(next);
            }

            fc.close(itfc);
        }

        co.close(itco);

        return ret;
    }

    public void localSetup() throws Exception {
        System.out.print("DataSet : ");
        createUnitsquareDataSet();
        System.out.println("OK");
        System.out.print("Control (init) : ");
        initLocalControl();
        System.out.println("OK");
        System.out.print("FilterSet : ");

        File f = new File("benchfilters.data");

        if (f.exists()) {
            filterset = DataUtilities.loadFilters(f);
        } else {
            createUnitsquareFilterSet();
            DataUtilities.saveFilters(filterset, f);
        }

        System.out.println("OK");
    }

    //    public void remoteSetup() throws IOException {
    //        System.out.print("Control (init) : ");
    //        initRemoteControl();
    //        System.out.println("OK");
    //        System.out.print("FilterSet : ");
    //        createFilterSet(control.getBounds());
    //        System.out.println("OK");
    //    }
    public static void main(String[] args) {
        BenchMark thisClass = new BenchMark();

        try {
            thisClass.localSetup();

            final int nodes = 500;
            final int capacity = 500;
            System.out.print("Sample (init) : ");
            thisClass.sample = new AbstractFeatureCache[3];
            thisClass.sample[0] = new GridFeatureCache(thisClass.control, nodes, capacity,
                    MemoryStorage.createInstance());
            thisClass.sample[1] = new GridFeatureCache(thisClass.control, nodes, capacity,
                    DiskStorage.createInstance());
            thisClass.sample[2] = new GridFeatureCache(thisClass.control, nodes, capacity,
                    BufferedDiskStorage.createInstance());

            //            DiskStorage storage = new DiskStorage(File.createTempFile("cache", ".tmp"), 1000);
            //            thisClass.sample[2] = new GridFeatureCache(thisClass.control, 500, 1000, storage);
            //            storage = new DiskStorage(File.createTempFile("cache", ".tmp"), 1000);
            //            thisClass.sample[3] = new GridFeatureCache(thisClass.control, 500, 2500, storage);
            //			thisClass.sample[3] = new GridFeatureCache(thisClass.control, 60, 3000, new MemoryStorage(100) ) ;
            //			thisClass.sample[4] = new GridFeatureCache(thisClass.control, 60, 4000, new MemoryStorage(100) ) ;
            System.out.println("OK");
            System.out.print("Control (run) : ");

            QueryStatistics[] control_stats = thisClass.runQueries(thisClass.control);
            QueryStatistics[][] sample_stats = new QueryStatistics[thisClass.sample.length][];

            for (int i = 0; i < thisClass.sample.length; i++) {
                System.out.print("Sample " + i + " (run) : ");
                sample_stats[i] = thisClass.runQueries(thisClass.sample[i]);
                System.out.println(thisClass.sample[i]);
                thisClass.printResults(control_stats, sample_stats[i], System.out);
                System.out.println(thisClass.sample[i].getStats());
            }

            //            for (int i = thisClass.sample.length - 1; i >= 0; i--) {
            //                System.out.print("Sample " + i + " (run inverse order) : ");
            //                sample_stats[i] = thisClass.runQueries(thisClass.sample[i]);
            //                System.out.println(thisClass.sample[i]);
            //                thisClass.printResults(control_stats, sample_stats[i], System.out);
            //                System.out.println(thisClass.sample[i].sourceAccessStats());
            //            }
            for (int i = 0; i < thisClass.sample.length; i++) {
                // TODO: close storage
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FeatureCacheException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class QueryStatistics {
        private int numberOfFeatures;
        private long executionTime;

        /**
         * @return  the executionTime
         * @uml.property  name="executionTime"
         */
        public long getExecutionTime() {
            return executionTime;
        }

        /**
         * @param executionTime  the executionTime to set
         * @uml.property  name="executionTime"
         */
        public void setExecutionTime(long executionTime) {
            this.executionTime = executionTime;
        }

        /**
         * @return  the numberOfFeatures
         * @uml.property  name="numberOfFeatures"
         */
        public int getNumberOfFeatures() {
            return numberOfFeatures;
        }

        /**
         * @param numberOfFeatures  the numberOfFeatures to set
         * @uml.property  name="numberOfFeatures"
         */
        public void setNumberOfFeatures(int numberOfFeatures) {
            this.numberOfFeatures = numberOfFeatures;
        }
    }
}
