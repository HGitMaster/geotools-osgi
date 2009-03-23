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
 *
 */
package org.geotools.arcsde.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.geotools.arcsde.pool.SessionPool;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.Not;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.spatial.BBOX;

import com.esri.sde.sdk.client.SeException;
import com.vividsolutions.jts.geom.Envelope;

/**
 * {@link ArcSdeFeatureSource} test cases
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/test/java
 *         /org/geotools/arcsde/data/ArcSDEDataStoreTest.java $
 * @version $Id: ArcSDEFeatureSourceTest.java 32668 2009-03-23 14:47:37Z groldan $
 */
public class ArcSDEFeatureSourceTest {
    /** package logger */
    private static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(ArcSDEFeatureSourceTest.class.getPackage().getName());

    /** DOCUMENT ME! */
    private static TestData testData;

    /** an ArcSDEDataStore created on setUp() to run tests against */
    private DataStore store;

    /** a filter factory for testing */
    FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        testData = new TestData();
        testData.setUp();
        final boolean insertTestData = true;
        testData.createTempTable(insertTestData);
    }

    @AfterClass
    public static void oneTimeTearDown() {
        boolean cleanTestTable = false;
        boolean cleanPool = true;
        testData.tearDown(cleanTestTable, cleanPool);
    }

    /**
     * loads {@code testData/testparams.properties} into a Properties object, wich is used to obtain
     * test tables names and is used as parameter to find the DataStore
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    @Before
    public void setUp() throws Exception {
        // facilitates running a single test at a time (eclipse lets you do this
        // and it's very useful)
        if (testData == null) {
            oneTimeSetUp();
        }
        this.store = testData.getDataStore();
    }

    @After
    public void tearDown() throws Exception {
        this.store = null;
    }

    /**
     * This method tests the feature reader by opening various simultaneous FeatureReaders using the
     * 3 test tables.
     * <p>
     * I found experimentally that until 24 simultaneous streams can be opened by a single
     * connection. Each featurereader has an ArcSDE stream opened until its <code>close()</code>
     * method is called or hasNext() returns flase, wich automatically closes the stream. If more
     * than 24 simultaneous streams are tryied to be opened upon a single SeConnection, an exception
     * is thrown by de Java ArcSDE API saying that a "NETWORK I/O OPERATION FAILED"
     * </p>
     * 
     * @throws IOException
     *             DOCUMENT ME!
     * @throws IllegalAttributeException
     *             DOCUMENT ME!
     * @throws SeException
     */
    @Test
    public void testGetFeatureReader() throws IOException {
        final String typeName = testData.getTempTableName();

        FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReader(typeName);

        assertNotNull(reader);
        int count = 0;
        try {
            while (testNext(reader)) {
                ++count;
            }
        } finally {
            reader.close();
        }

        assertEquals(8, count);
    }

    /**
     * Checks that a query returns only the specified attributes.
     * 
     * @throws IOException
     * @throws IllegalAttributeException
     * @throws SeException
     */
    @Test
    public void testRestrictsAttributes() throws IOException, IllegalAttributeException,
            SeException {
        final String typeName = testData.getTempTableName();
        final DataStore ds = testData.getDataStore();
        final SimpleFeatureType schema = ds.getSchema(typeName);
        final int queriedAttributeCount = schema.getAttributeCount() - 3;
        final String[] queryAtts = new String[queriedAttributeCount];

        for (int i = 0; i < queryAtts.length; i++) {
            queryAtts[i] = schema.getDescriptor(i).getLocalName();
        }

        // build the query asking for a subset of attributes
        final Query query = new DefaultQuery(typeName, Filter.INCLUDE, queryAtts);

        FeatureReader<SimpleFeatureType, SimpleFeature> reader;
        reader = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);
        SimpleFeatureType resultSchema;
        try {
            resultSchema = reader.getFeatureType();
        } finally {
            reader.close();
        }

        assertTrue(queriedAttributeCount == resultSchema.getAttributeCount());

        for (int i = 0; i < queriedAttributeCount; i++) {
            assertEquals(queryAtts[i], resultSchema.getDescriptor(i).getLocalName());
        }
    }

    /**
     * Checks that arcsde datastore returns featuretypes whose attributes are exactly in the
     * requested order.
     * 
     * @throws IOException
     *             DOCUMENT ME!
     * @throws IllegalAttributeException
     *             DOCUMENT ME!
     * @throws SeException
     */
    @Test
    public void testRespectsAttributeOrder() throws IOException, IllegalAttributeException,
            SeException {
        final String typeName = testData.getTempTableName();
        final DataStore ds = testData.getDataStore();
        final SimpleFeatureType schema = ds.getSchema(typeName);
        final int queriedAttributeCount = schema.getAttributeCount();
        final String[] queryAtts = new String[queriedAttributeCount];

        // build the attnames in inverse order
        for (int i = queryAtts.length, j = 0; i > 0; j++) {
            --i;
            queryAtts[j] = schema.getDescriptor(i).getLocalName();
        }

        // build the query asking for a subset of attributes
        final Query query = new DefaultQuery(typeName, Filter.INCLUDE, queryAtts);

        FeatureReader<SimpleFeatureType, SimpleFeature> reader;
        reader = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);
        try {

            SimpleFeatureType resultSchema = reader.getFeatureType();
            assertEquals(queriedAttributeCount, resultSchema.getAttributeCount());

            for (int i = 0; i < queriedAttributeCount; i++) {
                assertEquals(queryAtts[i], resultSchema.getDescriptor(i).getLocalName());
            }
        } finally {
            reader.close();
        }
    }

    /**
     * Say the query contains a set of propertynames to retrieve and the query filter others, the
     * returned feature type should still match the ones in Query.propertyNames
     * 
     * @throws IOException
     * @throws IllegalAttributeException
     * @throws SeException
     * @throws CQLException
     */
    @Test
    public void testRespectsQueryAttributes() throws IOException, IllegalAttributeException,
            SeException, CQLException {
        final String typeName = testData.getTempTableName();
        final DataStore ds = testData.getDataStore();
        final FeatureSource<SimpleFeatureType, SimpleFeature> fs = ds.getFeatureSource(typeName);

        final String[] queryAtts = { "SHAPE" };
        final Filter filter = CQL.toFilter("INT32_COL = 1");

        // build the query asking for a subset of attributes
        final Query query = new DefaultQuery(typeName, filter, queryAtts);

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = fs.getFeatures(query);
        SimpleFeatureType resultSchema = features.getSchema();

        assertEquals(1, resultSchema.getAttributeCount());
        assertEquals("SHAPE", resultSchema.getDescriptor(0).getLocalName());

        Feature feature = null;
        FeatureIterator<SimpleFeature> iterator = null;
        try {
            iterator = features.features();
            feature = iterator.next();
        } finally {
            if (iterator != null) {
                features.close(iterator);
            }
        }

        assertEquals(resultSchema, feature.getType());
    }

    private boolean testNext(FeatureReader<SimpleFeatureType, SimpleFeature> r) throws IOException,
            IllegalAttributeException {
        if (r.hasNext()) {
            SimpleFeature f = r.next();
            assertNotNull(f);
            assertNotNull(f.getFeatureType());
            assertNotNull(f.getBounds());

            GeometryAttribute defaultGeom = f.getDefaultGeometryProperty();
            assertNotNull(defaultGeom);

            return true;
        }

        return false;
    }

    private FeatureReader<SimpleFeatureType, SimpleFeature> getReader(String typeName)
            throws IOException {
        Query q = new DefaultQuery(typeName, Filter.INCLUDE);
        FeatureReader<SimpleFeatureType, SimpleFeature> reader = store.getFeatureReader(q,
                Transaction.AUTO_COMMIT);
        SimpleFeatureType retType = reader.getFeatureType();
        assertNotNull(retType.getGeometryDescriptor());
        assertTrue(reader.hasNext());

        return reader;
    }

    /**
     * tests the datastore behavior when fetching data based on mixed queries.
     * <p>
     * "Mixed queries" refers to mixing alphanumeric and geometry based filters, since that is the
     * natural separation of things in the Esri Java API for ArcSDE. This is necessary since mixed
     * queries sometimes are problematic. So this test ensures that:
     * <ul>
     * <li>A mixed query respects all filters</li>
     * <li>A mixed query does not fails when getBounds() is performed</li>
     * <li>A mixed query does not fails when size() is performed</li>
     * </ul>
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testMixedQueries() throws Exception {
        final int EXPECTED_RESULT_COUNT = 1;
        FeatureSource<SimpleFeatureType, SimpleFeature> fs = store.getFeatureSource(testData
                .getTempTableName());
        Filter bboxFilter = getBBoxfilter(fs);
        Filter sqlFilter = CQL.toFilter("INT32_COL < 5");
        LOGGER.fine("Geometry filter: " + bboxFilter);
        LOGGER.fine("SQL filter: " + sqlFilter);

        And mixedFilter = ff.and(sqlFilter, bboxFilter);

        Not not = ff.not(ff.id(Collections.singleton(ff.featureId(testData.getTempTableName()
                + ".90000"))));

        mixedFilter = ff.and(mixedFilter, not);

        LOGGER.fine("Mixed filter: " + mixedFilter);

        // verify both filter constraints are met
        try {
            testFilter(mixedFilter, fs, EXPECTED_RESULT_COUNT);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        // check that getBounds and size do function
        FeatureIterator<SimpleFeature> reader = null;
        FeatureCollection<SimpleFeatureType, SimpleFeature> results = fs.getFeatures(mixedFilter);
        Envelope bounds = results.getBounds();
        assertNotNull(bounds);
        LOGGER.fine("results bounds: " + bounds);

        reader = results.features();
        try {
            /*
             * verify that when features are already being fetched, getBounds and size still work
             */
            reader.next();
            bounds = results.getBounds();
            assertNotNull(bounds);
            LOGGER.fine("results bounds when reading: " + bounds);

            int count = results.size();
            assertEquals(EXPECTED_RESULT_COUNT, count);
            LOGGER.fine("wooohoooo...");

        } finally {
            reader.close();
        }
    }

    /**
     * to expose GEOT-408, tests that queries in which only non spatial attributes are requested
     * does not fails due to the datastore trying to parse the geometry attribute.
     * 
     * @throws Exception
     */
    @Test
    public void testAttributeOnlyQuery() throws Exception {
        DataStore ds = testData.getDataStore();
        FeatureSource<SimpleFeatureType, SimpleFeature> fSource = ds.getFeatureSource(testData
                .getTempTableName());
        SimpleFeatureType type = fSource.getSchema();
        DefaultQuery attOnlyQuery = new DefaultQuery(type.getTypeName());
        List propNames = new ArrayList(type.getAttributeCount() - 1);

        for (int i = 0; i < type.getAttributeCount(); i++) {
            if (type.getDescriptor(i) instanceof GeometryDescriptor) {
                continue;
            }

            propNames.add(type.getDescriptor(i).getLocalName());
        }

        attOnlyQuery.setPropertyNames(propNames);

        FeatureCollection<SimpleFeatureType, SimpleFeature> results = fSource
                .getFeatures(attOnlyQuery);
        SimpleFeatureType resultSchema = results.getSchema();
        assertEquals(propNames.size(), resultSchema.getAttributeCount());

        for (int i = 0; i < propNames.size(); i++) {
            assertEquals(propNames.get(i), resultSchema.getDescriptor(i).getLocalName());
        }

        // the problem described in GEOT-408 arises in attribute reader, so
        // we must to try fetching features
        FeatureIterator<SimpleFeature> iterator = results.features();
        SimpleFeature feature = iterator.next();
        iterator.close();
        assertNotNull(feature);

        // the id must be grabed correctly.
        // this exercises the fact that although the geometry is not included
        // in the request, it must be fecthed anyway to obtain the
        // SeShape.getFeatureId()
        // getID() should throw an exception if the feature is was not grabed
        // (see
        // ArcSDEAttributeReader.readFID().
        String id = feature.getID();
        assertNotNull(id);
        assertFalse(id.endsWith(".-1"));
        assertFalse(id.endsWith(".0"));
    }

    /**
     * Test that FID filters are correctly handled
     * 
     * @throws Exception
     */
    @Test
    public void testFidFilters() throws Exception {
        final DataStore ds = testData.getDataStore();
        final String typeName = testData.getTempTableName();

        // grab some fids
        FeatureReader<SimpleFeatureType, SimpleFeature> reader = ds.getFeatureReader(
                new DefaultQuery(typeName), Transaction.AUTO_COMMIT);
        List fids = new ArrayList();

        while (reader.hasNext()) {
            fids.add(ff.featureId(reader.next().getID()));

            // skip one
            if (reader.hasNext()) {
                reader.next();
            }
        }

        reader.close();

        Id filter = ff.id(new HashSet(fids));

        FeatureSource<SimpleFeatureType, SimpleFeature> source = ds.getFeatureSource(typeName);
        Query query = new DefaultQuery(typeName, filter);
        FeatureCollection<SimpleFeatureType, SimpleFeature> results = source.getFeatures(query);

        assertEquals(fids.size(), results.size());
        FeatureIterator<SimpleFeature> iterator = results.features();

        while (iterator.hasNext()) {
            String fid = iterator.next().getID();
            assertTrue("a fid not included in query was returned: " + fid, fids.contains(ff
                    .featureId(fid)));
        }
        results.close(iterator);
    }

    @Test
    public void testMoreThan1000FidFilters() throws Exception {
        final DataStore ds = testData.getDataStore();
        final String typeName = testData.getTempTableName();

        // grab some fids
        FeatureReader<SimpleFeatureType, SimpleFeature> reader = ds.getFeatureReader(
                new DefaultQuery(typeName), Transaction.AUTO_COMMIT);
        List fids = new ArrayList();

        if (reader.hasNext()) {
            fids.add(ff.featureId(reader.next().getID()));
        }

        reader.close();

        String idTemplate = ((FeatureId) fids.get(0)).getID();
        idTemplate = idTemplate.substring(0, idTemplate.length() - 1);

        for (int x = 100; x < 2000; x++) {
            fids.add(ff.featureId(idTemplate + x));
        }

        Id filter = ff.id(new HashSet(fids));

        FeatureSource<SimpleFeatureType, SimpleFeature> source = ds.getFeatureSource(typeName);
        Query query = new DefaultQuery(typeName, filter);
        FeatureCollection<SimpleFeatureType, SimpleFeature> results = source.getFeatures(query);

        assertEquals(1, results.size());
        FeatureIterator<SimpleFeature> iterator = results.features();

        while (iterator.hasNext()) {
            String fid = iterator.next().getID();
            assertTrue("a fid not included in query was returned: " + fid, fids.contains(ff
                    .featureId(fid)));
        }
        results.close(iterator);
    }

    /**
     * test that getFeatureSource over an sde layer works
     * 
     * @throws IOException
     * @throws SeException
     */
    @Test
    public void testGetFeatureSourcePoint() throws IOException, SeException {
        testGetFeatureSource(store.getFeatureSource(testData.getTempTableName()));
    }

    @Test
    public void testGetFeatures() throws Exception {
        final String table = testData.getTempTableName();
        LOGGER.fine("getting all features from " + table);

        FeatureSource<SimpleFeatureType, SimpleFeature> source = store.getFeatureSource(table);
        int expectedCount = 8;
        int fCount = source.getCount(Query.ALL);
        String failMsg = "Expected and returned result count does not match";
        assertEquals(failMsg, expectedCount, fCount);

        FeatureCollection<SimpleFeatureType, SimpleFeature> fresults = source.getFeatures();
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = fresults;
        failMsg = "FeatureResults.size and .collection().size thoes not match";
        assertEquals(failMsg, fCount, features.size());
        LOGGER.fine("fetched " + fCount + " features for " + table + " layer, OK");
    }

    @Test
    public void testSQLFilter() throws Exception {
        int expected = 4;
        Filter filter = CQL.toFilter("INT32_COL < 5");
        FeatureSource<SimpleFeatureType, SimpleFeature> fsource = store.getFeatureSource(testData
                .getTempTableName());
        testFilter(filter, fsource, expected);
    }

    @Test
    public void testBBoxFilter() throws Exception {
        int expected = 7;
        testBBox(testData.getTempTableName(), expected);
    }

    /**
     * A bbox filter with an empty attribute name should work against the default geometry attribute
     */
    @SuppressWarnings("nls")
    @Test
    public void testBboxFilterWithEmptyAttributeName() throws Exception {
        BBOX emptyAttNameFilter = ff.bbox("", -10, -10, 10, 10, "EPSG:4326");
        String typeName = testData.getTempTableName();
        SimpleFeatureType schema = store.getSchema(typeName);

        FeatureSource<SimpleFeatureType, SimpleFeature> source;
        source = store.getFeatureSource(typeName);
        FeatureCollection<SimpleFeatureType, SimpleFeature> features;
        features = source.getFeatures(emptyAttNameFilter);

        FeatureIterator<SimpleFeature> iterator = features.features();
        try {
            assertTrue(iterator.hasNext());
        } finally {
            iterator.close();
        }
    }

    @Test
    public void testStress() throws Exception {

        ArcSDEDataStore ds = testData.getDataStore();

        SessionPool pool = testData.getConnectionPool();
        final int initialAvailableCount = pool.getAvailableCount();
        final int initialPoolSize = pool.getPoolSize();

        String typeName = testData.getTempTableName();

        FeatureSource<SimpleFeatureType, SimpleFeature> source;
        source = ds.getFeatureSource(typeName);

        assertEquals(initialAvailableCount, pool.getAvailableCount());
        assertEquals(initialPoolSize, pool.getPoolSize());

        SimpleFeatureType schema = source.getSchema();

        assertEquals("After getSchema()", initialAvailableCount, pool.getAvailableCount());
        assertEquals("After getSchema()", initialPoolSize, pool.getPoolSize());

        final Envelope layerBounds = source.getBounds();

        assertEquals("After getBounds()", initialAvailableCount, pool.getAvailableCount());
        assertEquals("After getBounds()", initialPoolSize, pool.getPoolSize());

        source.getCount(Query.ALL);

        assertEquals("After size()", initialAvailableCount, pool.getAvailableCount());
        assertEquals("After size()", initialPoolSize, pool.getPoolSize());

        BBOX bbox = ff.bbox(schema.getGeometryDescriptor().getLocalName(),
                layerBounds.getMinX() + 10, layerBounds.getMinY() + 10, layerBounds.getMaxX() - 10,
                layerBounds.getMaxY() - 10, schema.getCoordinateReferenceSystem().getName()
                        .getCode());

        for (int i = 0; i < 20; i++) {
            LOGGER.fine("Running iteration #" + i);

            FeatureCollection<SimpleFeatureType, SimpleFeature> res;
            res = source.getFeatures(bbox);
            FeatureIterator<SimpleFeature> reader = res.features();
            try {
                assertNotNull(reader.next());

                assertTrue(0 < res.size());
                assertNotNull(res.getBounds());

                assertNotNull(reader.next());

                assertTrue(0 < res.size());
                assertNotNull(res.getBounds());

                assertNotNull(reader.next());
            } finally {
                reader.close();
            }
        }
    }

    // ///////////////// HELPER FUNCTIONS ////////////////////////

    /**
     * for a given FeatureSource, makes the following assertions:
     * <ul>
     * <li>it's not null</li>
     * <li>.getDataStore() != null</li>
     * <li>.getDataStore() == the datastore obtained in setUp()</li>
     * <li>.getSchema() != null</li>
     * <li>.getBounds() != null</li>
     * <li>.getBounds().isNull() == false</li>
     * <li>.getFeatures().getCounr() > 0</li>
     * <li>.getFeatures().reader().hasNex() == true</li>
     * <li>.getFeatures().reader().next() != null</li>
     * </ul>
     * 
     * @param fsource
     *            DOCUMENT ME!
     * @throws IOException
     *             DOCUMENT ME!
     */
    private void testGetFeatureSource(FeatureSource<SimpleFeatureType, SimpleFeature> fsource)
            throws IOException {
        assertNotNull(fsource);
        assertNotNull(fsource.getDataStore());
        assertEquals(fsource.getDataStore(), store);
        assertNotNull(fsource.getSchema());

        FeatureCollection<SimpleFeatureType, SimpleFeature> results = fsource.getFeatures();
        int count = results.size();
        assertTrue("size returns " + count, count > 0);
        LOGGER.fine("feature count: " + count);

        Envelope env1;
        Envelope env2;
        env1 = fsource.getBounds();
        assertNotNull(env1);
        assertFalse(env1.isNull());
        env2 = fsource.getBounds(Query.ALL);
        assertNotNull(env2);
        assertFalse(env2.isNull());
        env1 = results.getBounds();
        assertNotNull(env1);
        assertFalse(env1.isNull());

        FeatureIterator<SimpleFeature> reader = results.features();
        assertTrue(reader.hasNext());

        try {
            assertNotNull(reader.next());
        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        }

        reader.close();
    }

    private void testFilter(Filter filter, FeatureSource<SimpleFeatureType, SimpleFeature> fsource,
            int expected) throws IOException {
        FeatureCollection<SimpleFeatureType, SimpleFeature> fc = fsource.getFeatures(filter);

        FeatureIterator<SimpleFeature> fi = fc.features();
        try {
            int numFeat = 0;
            while (fi.hasNext()) {
                fi.next();
                numFeat++;
            }

            String failMsg = "Fully fetched features size and estimated num features count does not match";
            assertEquals(failMsg, expected, numFeat);
        } finally {
            fc.close(fi);
        }
    }

    private void testBBox(String table, int expected) throws Exception {
        FeatureSource<SimpleFeatureType, SimpleFeature> fs = store.getFeatureSource(table);
        Filter bboxFilter = getBBoxfilter(fs);
        testFilter(bboxFilter, fs, expected);
    }

    private Filter getBBoxfilter(FeatureSource<SimpleFeatureType, SimpleFeature> fs)
            throws Exception {
        SimpleFeatureType schema = fs.getSchema();
        BBOX bbe = ff.bbox(schema.getGeometryDescriptor().getLocalName(), -60, -55, -40, -20,
                schema.getCoordinateReferenceSystem().getName().getCode());
        return bbe;
    }

}
