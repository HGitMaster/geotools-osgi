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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.arcsde.ArcSDEDataStoreFactory;
import org.geotools.arcsde.data.versioning.ArcSdeVersionHandler;
import org.geotools.arcsde.data.versioning.AutoCommitDefaultVersionHandler;
import org.geotools.arcsde.pool.ISession;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Test suite for the {@link ArcSDEQuery} query wrapper
 * 
 * @author Gabriel Roldan
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/test/java/org/geotools/arcsde/data/ArcSDEQueryTest.java $
 * @version $Revision: 1.9 $
 */
public class ArcSDEQueryTest extends TestCase {

    private static TestData testData;

    /**
     * do not access it directly, use {@link #getQueryAll()}
     */
    private ArcSDEQuery _queryAll;

    /**
     * do not access it directly, use {@link #getQueryFiltered()}
     */
    private ArcSDEQuery queryFiltered;

    private ArcSDEDataStore dstore;

    private String typeName;

    private Query filteringQuery;

    private FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

    private SimpleFeatureType ftype;

    private static final int FILTERING_COUNT = 3;

    /**
     * Constructor for ArcSDEQueryTest.
     * 
     * @param arg0
     */
    public ArcSDEQueryTest(String name) {
        super(name);
    }

    /**
     * Builds a test suite for all this class' tests with per suite initialization directed to
     * {@link #oneTimeSetUp()} and per suite clean up directed to {@link #oneTimeTearDown()}
     * 
     * @return
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(ArcSDEQueryTest.class);

        TestSetup wrapper = new TestSetup(suite) {
            @Override
            protected void setUp() throws Exception {
                oneTimeSetUp();
            }

            @Override
            protected void tearDown() {
                oneTimeTearDown();
            }
        };
        return wrapper;
    }

    private static void oneTimeSetUp() throws Exception {
        testData = new TestData();
        testData.setUp();

        final boolean insertTestData = true;
        testData.createTempTable(insertTestData);
    }

    private static void oneTimeTearDown() {
        boolean cleanTestTable = false;
        boolean cleanPool = true;
        testData.tearDown(cleanTestTable, cleanPool);
    }

    /**
     * loads {@code test-data/testparams.properties} into a Properties object, wich is used to
     * obtain test tables names and is used as parameter to find the DataStore
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (testData == null) {
            oneTimeSetUp();
        }
        dstore = testData.getDataStore();
        typeName = testData.getTempTableName();
        this.ftype = dstore.getSchema(typeName);

        // grab some fids
        FeatureSource<SimpleFeatureType, SimpleFeature> source = dstore.getFeatureSource(typeName);
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures();
        FeatureIterator<SimpleFeature> iterator = features.features();
        List fids = new ArrayList();
        for (int i = 0; i < FILTERING_COUNT; i++) {
            fids.add(ff.featureId(iterator.next().getID()));
        }
        iterator.close();
        Id filter = ff.id(new HashSet(fids));
        filteringQuery = new DefaultQuery(typeName, filter);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            this._queryAll.close();
        } catch (Exception e) {
            // no-op
        }
        try {
            this.queryFiltered.close();
        } catch (Exception e) {
            // no-op
        }
        this._queryAll = null;
        this.queryFiltered = null;
    }

    private ArcSDEQuery getQueryAll() throws IOException {
        ISession session = dstore.getSession(Transaction.AUTO_COMMIT);
        this._queryAll = ArcSDEQuery.createQuery(session, ftype, Query.ALL, FIDReader.NULL_READER,
                ArcSdeVersionHandler.NONVERSIONED_HANDLER);
        return this._queryAll;
    }

    private ArcSDEQuery getQueryFiltered() throws IOException {
        ISession session = dstore.getSession(Transaction.AUTO_COMMIT);
        FeatureTypeInfo fti = ArcSDEAdapter.fetchSchema(typeName, null, session);
        this.queryFiltered = ArcSDEQuery.createQuery(session, ftype, filteringQuery, fti
                .getFidStrategy(), new AutoCommitDefaultVersionHandler());
        return this.queryFiltered;
    }

    /**
     * DOCUMENT ME!
     */
    public void testClose() throws IOException {
        ArcSDEQuery queryAll = getQueryAll();
        assertNotNull(queryAll.session);

        queryAll.execute();

        assertNotNull(queryAll.session);

        // should nevel do this, just to assert it is
        // not closed by returned to the pool
        ISession session = queryAll.session;

        queryAll.close();

        assertNotNull(queryAll.session);
        assertFalse(session.isClosed());

        session.dispose();
    }

    /**
     * DOCUMENT ME!
     */
    public void testFetch() throws IOException {
        ArcSDEQuery queryAll = getQueryAll();
        try {
            queryAll.fetch();
            fail("fetch without calling execute");
        } catch (IllegalStateException e) {
            // ok
        }

        queryAll.execute();
        assertNotNull(queryAll.fetch());

        queryAll.close();
        try {
            queryAll.fetch();
            fail("fetch after close!");
        } catch (IllegalStateException e) {
            // ok
        }

        queryAll.session.dispose();
    }

    /**
     * DOCUMENT ME!
     */
    public void testCalculateResultCount() throws Exception {
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = dstore.getFeatureSource(
                typeName).getFeatures();
        FeatureIterator<SimpleFeature> reader = features.features();
        int read = 0;
        while (reader.hasNext()) {
            reader.next();
            read++;
        }
        reader.close();

        ArcSDEQuery q = getQueryAll();
        int calculated = q.calculateResultCount();
        q.session.dispose();
        assertEquals(read, calculated);

        q = getQueryFiltered();
        calculated = q.calculateResultCount();
        q.session.dispose();
        assertEquals(FILTERING_COUNT, calculated);
    }

    /**
     * DOCUMENT ME!
     */
    public void testCalculateQueryExtent() throws Exception {
        {
            FeatureCollection<SimpleFeatureType, SimpleFeature> features = dstore.getFeatureSource(
                    typeName).getFeatures();
            FeatureIterator<SimpleFeature> reader = features.features();
            SimpleFeatureType featureType = features.getSchema();
            GeometryDescriptor defaultGeometry = featureType.getGeometryDescriptor();
            ReferencedEnvelope real = new ReferencedEnvelope(defaultGeometry.getCoordinateReferenceSystem());
            try {
                while (reader.hasNext()) {
                    real.include(reader.next().getBounds());
                }
            } finally {
                reader.close();
            }

            // TODO: make calculateQueryExtent to return ReferencedEnvelope
            ArcSDEQuery queryAll = getQueryAll();
            Envelope actual = queryAll.calculateQueryExtent();
            Envelope expected = new Envelope(real);
            assertNotNull(actual);
            assertEquals(expected, actual);

        }
        {
            FeatureReader<SimpleFeatureType, SimpleFeature> featureReader = dstore
                    .getFeatureReader(filteringQuery, Transaction.AUTO_COMMIT);
            ReferencedEnvelope real = new ReferencedEnvelope();
            try {
                while (featureReader.hasNext()) {
                    real.include(featureReader.next().getBounds());
                }
            } finally {
                featureReader.close();
            }

            Envelope actual = getQueryFiltered().calculateQueryExtent();
            assertNotNull(actual);
            Envelope expected = new Envelope(real);
            assertEquals(expected, actual);
        }
    }

    private void assertEquals(Envelope e1, Envelope e2) {
        final double tolerance = 1.0E-9;
        assertEquals(e1.getMinX(), e2.getMinX(), tolerance);
        assertEquals(e1.getMinY(), e2.getMinY(), tolerance);
        assertEquals(e1.getMaxX(), e2.getMaxX(), tolerance);
        assertEquals(e1.getMaxY(), e2.getMaxY(), tolerance);
    }
}
