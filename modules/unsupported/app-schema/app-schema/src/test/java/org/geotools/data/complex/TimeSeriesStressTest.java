/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.data.complex;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.complex.config.EmfAppSchemaReader;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.feature.Types;
import org.geotools.filter.FilterFactoryImplNamespaceAware;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.test.OnlineTestCase;
import org.geotools.xlink.XLINK;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.PropertyName;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.NamespaceSupport;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * FIXME: This class has never worked. It has been ported to OnlineTestCase so it fails silently in
 * Eclipse. Port is incomplete. Fix it if you care about it.
 * 
 * @author Rob Atkinson
 * @version $Id: TimeSeriesStressTest.java 32935 2009-05-04 04:35:04Z bencaradocdavies $
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/2.4.x/modules/unsupported/community-schemas
 *         /community-schema-ds/src/test/java/org/geotools/data/complex/TimeSeriesTest.java $
 * @since 2.4
 */
public class TimeSeriesStressTest extends OnlineTestCase {
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(TimeSeriesStressTest.class.getPackage().getName());

    private static final String AWNS = "http://www.water.gov.au/awdip";

    private static final String CVNS = "http://www.opengis.net/cv/0.2.1";

    private static final String SANS = "http://www.opengis.net/sampling/1.0";

    private static final String OMNS = "http://www.opengis.net/om/1.0";

    private static final String SWENS = "http://www.opengis.net/swe/1.0.1";

    private static final String GMLNS = "http://www.opengis.net/gml";

    // private static final String GEONS = "http://www.seegrid.csiro.au/xml/geometry";

    final String schemaBase = "/test-data/";

    EmfAppSchemaReader reader;

    private FeatureSource<FeatureType, Feature> source;

    @Override
    protected String getFixtureId() {
        return "app-schema.TimeSeriesStressTest";
    }
    
    /**
     * DOCUMENT ME!
     * 
     * @throws Exception
     *                 DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        reader = EmfAppSchemaReader.newInstance();

        // Logging.GEOTOOLS.forceMonolineConsoleOutput(Level.FINEST);
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception
     *                 DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param location
     *                schema location path discoverable through getClass().getResource()
     * 
     * @throws IOException
     *                 DOCUMENT ME!
     */
    private void loadSchema(String location) throws IOException {
        // load needed GML types directly from the gml schemas
        URL schemaLocation = getClass().getResource(location);
        if (schemaLocation == null) {
            schemaLocation = new URL(location);
        }
        assertNotNull(location, schemaLocation);
        reader.parse(schemaLocation);
    }

    private Name name(String ns, String localName) {
        return Types.typeName(ns, localName);
    }

    /**
     * A rough stress test for timeseries mapping
     * 
     * @throws Exception
     */
    public void testStressDataStore() throws Exception {
        final Map dsParams = new HashMap();
        String configLocation = schemaBase + "TimeSeriesTest_properties2.xml";
        final URL url = getClass().getResource(configLocation);

        dsParams.put("dbtype", "complex");
        dsParams.put("url", url.toExternalForm());

        final Name typeName = new NameImpl(AWNS, "SiteSinglePhenomTimeSeries");

        DataAccess<FeatureType, Feature> mappingDataStore = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(mappingDataStore);

        FeatureType fType = mappingDataStore.getSchema(typeName);
        assertNotNull(fType);

        FeatureSource<FeatureType, Feature> fSource = mappingDataStore.getFeatureSource(typeName);

        Filter filter = CQL.toFilter("gml:name = 'stat_id_3000'");
        FeatureCollection<FeatureType, Feature> features = fSource.getFeatures(filter);
        final int expectedResults = 3000;
        final int EXPECTED_RESULT_COUNT = 1;
        final int numberOfRuns = 5;

        // LOGGER.info("stressing getCount()...");
        final double countTime = stressCount(features, EXPECTED_RESULT_COUNT, numberOfRuns);
        LOGGER.info("getCount() agv time: " + countTime + "ms");

        LOGGER.info("Stressing getFeatures()...");
        final double fetchTime = stressGetFeatures(features, EXPECTED_RESULT_COUNT, numberOfRuns,
                expectedResults);
        LOGGER.info("getFeatures() agv time: " + fetchTime + "ms");
    }

    private double stressGetFeatures(final FeatureCollection features,
            final int EXPECTED_RESULT_COUNT, final int numberOfRuns, final int expectedResults) {
        double cumulativeTime = 0;
        StopWatch timer = new StopWatch();

        NamespaceSupport namespaces = new NamespaceSupport();
        namespaces.declarePrefix("aw", AWNS);
        namespaces.declarePrefix("om", OMNS);
        namespaces.declarePrefix("swe", SWENS);
        namespaces.declarePrefix("gml", GMLNS);
        namespaces.declarePrefix("sa", SANS);
        // TODO: use commonfactoryfinder or the mechanism choosed
        // to pass namespace context to filter factory
        FilterFactory ffac = new FilterFactoryImplNamespaceAware(namespaces);

        Feature feature = null;
        int count = 0;
        final String phenomNamePath = "aw:relatedObservation/aw:PhenomenonTimeSeries/om:observedProperty/swe:Phenomenon/gml:name";

        for (int run = 0; run < numberOfRuns; run++) {
            Iterator it = features.iterator();
            count = 0;
            timer.start();
            for (; it.hasNext();) {
                feature = (Feature) it.next();
                count++;
            }
            timer.stop();
            cumulativeTime += timer.time();
            features.close(it);
        }

        PropertyName gmlName = ffac.property("gml:name");
        PropertyName phenomName = ffac.property(phenomNamePath);

        Object nameVal = gmlName.evaluate(feature, String.class);
        assertNotNull("gml:name evaluated to null", nameVal);

        Object phenomNameVal = phenomName.evaluate(feature, String.class);
        assertNotNull(phenomNamePath + " evaluated to null", phenomNameVal);

        PropertyName sampledFeatureName = ffac.property("sa:sampledFeature");
        Attribute sampledFeatureVal = (Attribute) sampledFeatureName.evaluate(feature);
        assertNotNull("sa:sampledFeature evaluated to null", sampledFeatureVal);
        assertNull(sampledFeatureVal.getValue());
        Map attributes = (Map) sampledFeatureVal.getUserData().get(Attributes.class);
        assertNotNull(attributes);
        Name xlinkTitle = name(XLINK.NAMESPACE, "title");
        assertTrue(attributes.containsKey(xlinkTitle));
        assertNotNull(attributes.get(xlinkTitle));

        Name xlinkHref = name(XLINK.NAMESPACE, "href");
        assertTrue(attributes.containsKey(xlinkHref));
        assertNotNull(attributes.get(xlinkHref));

        assertEquals(EXPECTED_RESULT_COUNT, count);

        // /
        PropertyName elementName = ffac
                .property("aw:relatedObservation/aw:PhenomenonTimeSeries/om:result/cv:CompactDiscreteTimeCoverage");
        Object timeCovVal = elementName.evaluate(feature);
        assertNotNull(
                "aw:relatedObservation/aw:PhenomenonTimeSeries/om:result/cv:CompactDiscreteTimeCoverage",
                timeCovVal);
        assertTrue(timeCovVal instanceof Feature);
        final List elements = (List) ((Feature) timeCovVal).getValue();
        assertEquals(expectedResults, elements.size());

        ComplexAttribute element = (ComplexAttribute) elements.get(10);
        assertNotNull(element);
        Name compactTimeValuePairName = Types.typeName(CVNS, "CompactTimeValuePair");
        Name geometryName = Types.typeName(CVNS, "geometry");
        Name valueName = Types.typeName(CVNS, "value");

        Collection<Property> compactTimes = element.getProperties(compactTimeValuePairName);
        assertNotNull(compactTimes);
        assertEquals(1, compactTimes.size());

        ComplexAttribute compatTimeValuePair = (ComplexAttribute) compactTimes.iterator().next();
        Collection<Property> geometries = compatTimeValuePair.getProperties(geometryName);
        Collection<Property> values = compatTimeValuePair.getProperties(valueName);

        assertNotNull(geometries);
        assertNotNull(values);
        assertEquals(1, geometries.size());
        assertEquals(1, values.size());

        Attribute geom = (Attribute) geometries.iterator().next();
        Attribute value = (Attribute) values.iterator().next();

        assertNotNull(geom.getValue());
        assertNotNull(value.getValue());
        assertNotNull(value.getUserData().get(Attributes.class));

        return cumulativeTime / numberOfRuns;
    }

    /**
     * Runs numberOfCycles + 1, the first run does not count, returns the avg time it took to count
     * the features.
     * 
     * @param features
     * @param expectedFeatureCount
     * @param numberOfCycles
     * @return
     */
    private double stressCount(final FeatureCollection features, final int expectedFeatureCount,
            final int numberOfCycles) {
        double cumulativeTime = 0;

        StopWatch timer = new StopWatch();

        for (int i = 0; i < numberOfCycles; i++) {
            timer.start();
            // int size = ((Collection)features).size();
            int size = features.size();
            timer.stop();
            assertEquals(expectedFeatureCount, size);

            // int resultCount = getCount(features);
            // cumulativeTime += timer.time();
            //
            String msg = "be sure difference in result count is not due to different dataset.";
            assertEquals(msg, expectedFeatureCount, size);
        }

        return cumulativeTime / numberOfCycles;
    }

    private int getCount(FeatureCollection features) {
        Iterator iterator = features.iterator();
        int count = 0;
        try {
            while (iterator.hasNext()) {
                iterator.next();
                count++;
            }
        } finally {
            features.close(iterator);
        }
        return count;
    }

    private static class StopWatch {
        private long start;

        private long end = Long.MIN_VALUE;

        public void start() {
            start = System.currentTimeMillis();
            end = Long.MIN_VALUE;
        }

        public void stop() {
            end = System.currentTimeMillis();
        }

        public long time() {
            if (Long.MIN_VALUE == end) {
                throw new IllegalStateException("call stop() before time()");
            }
            return end - start;
        }
    }

    private static void populateTable() throws Exception {
        Map params = new HashMap();
        params.put(PostgisDataStoreFactory.DBTYPE.key, "postgis");
        params.put(PostgisDataStoreFactory.DATABASE.key, "postgis");
        params.put(PostgisDataStoreFactory.HOST.key, "localhost");
        params.put(PostgisDataStoreFactory.PORT.key, "5432");
        params.put(PostgisDataStoreFactory.USER.key, "postgres");
        params.put(PostgisDataStoreFactory.PASSWD.key, "postgres");

        final DataStore ds = DataStoreFinder.getDataStore(params);
        final String typeSpec = "station_id:String,POSITION:Point,station_name:String,"
                + "determinand_code:String,determinand_description:String,"
                + "sample_time_position:java.util.Date,result:Double,units:String";
        final SimpleFeatureType schema = DataUtilities.createType("TimeSeriesTest",
                typeSpec);
        LOGGER.info("Creating schema " + schema);
        ds.createSchema(schema);

        // the two fields grouped by
        String station_id;
        String determinand_code;
        // put 1000, 2000, 3000, and 4000 groups of records
        station_id = "stat_id_1000";
        determinand_code = "det_code_1000";
        populate(ds, schema, station_id, determinand_code, 1000);

        station_id = "stat_id_2000";
        determinand_code = "det_code_2000";
        populate(ds, schema, station_id, determinand_code, 2000);

        station_id = "stat_id_3000";
        determinand_code = "det_code_3000";
        populate(ds, schema, station_id, determinand_code, 3000);

        station_id = "stat_id_4000";
        determinand_code = "det_code_4000";
        populate(ds, schema, station_id, determinand_code, 4000);

        station_id = "stat_id_12000";
        determinand_code = "det_code_12000";
        populate(ds, schema, station_id, determinand_code, 12000);
    }

    private static void populate(final DataStore ds, final SimpleFeatureType schema,
            final String station_id, final String determinand_code, final int featureCount)
            throws Exception {
        SimpleFeature feature;
        GeometryFactory gf = new GeometryFactory();
        LOGGER.info("Creating " + featureCount + " features for station_id " + station_id);

        Transaction transaction = new DefaultTransaction();
        FeatureWriter<SimpleFeatureType, SimpleFeature> fw = ds.getFeatureWriterAppend(schema.getTypeName(), transaction);
        for (double i = 0; i < featureCount; i++) {
            fw.hasNext();
            feature = fw.next();
            feature.setAttribute("station_id", station_id);
            feature.setAttribute("determinand_code", determinand_code);
            feature.setAttribute("POSITION", gf.createPoint(new Coordinate(i / 1000D, i / 1000D)));
            feature.setAttribute("station_name", "stat_name_" + i);
            feature.setAttribute("determinand_description", "determinand description " + i
                    + " for " + station_id);
            feature.setAttribute("sample_time_position", new java.util.Date());
            feature.setAttribute("result", new Double(i / 1000));
            fw.write();
            if (i % 100 == 0) {
                transaction.commit();
            }
        }
        transaction.commit();
        fw.close();
        LOGGER.info(featureCount + " features added for " + station_id);
    }

    public static void main(String[] argv) {
        try {
            populateTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
