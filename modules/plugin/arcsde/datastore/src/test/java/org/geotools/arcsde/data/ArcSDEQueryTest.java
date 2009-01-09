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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geotools.arcsde.data.ArcSDEQuery.FilterSet;
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
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.spatial.BBOX;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Test suite for the {@link ArcSDEQuery} query wrapper
 * 
 * @author Gabriel Roldan
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/test/java
 *         /org/geotools/arcsde/data/ArcSDEQueryTest.java $
 * @version $Revision: 1.9 $
 */
public class ArcSDEQueryTest {

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
     * loads {@code test-data/testparams.properties} into a Properties object, wich is used to
     * obtain test tables names and is used as parameter to find the DataStore
     */
    @Before
    public void setUp() throws Exception {
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

    @After
    public void tearDown() throws Exception {
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

    /**
     * Filters are separated into backend supported and unsupported filters. Once split they should
     * be simplified to avoid silly filters like {@code 1 = 1 AND 1 = 1}
     * 
     * @throws IOException
     * @throws CQLException
     */
    @Test
    public void testSimplifiesFilters() throws IOException, CQLException {

        Filter filter = CQL
                .toFilter("STRING_COL = strConcat('string', STRING_COL) AND STRING_COL > 'String2' AND BBOX(SHAPE, 10.0,20.0,30.0,40.0)");
        filteringQuery = new DefaultQuery(typeName, filter);
        // filteringQuery based on the above filter...
        ArcSDEQuery sdeQuery = getQueryFiltered();

        FilterSet filters;
        try {
            filters = sdeQuery.getFilters();
        } finally {
            sdeQuery.session.dispose();
            sdeQuery.close();
        }
        Filter geometryFilter = filters.getGeometryFilter();
        Filter sqlFilter = filters.getSqlFilter();
        Filter unsupportedFilter = filters.getUnsupportedFilter();

        System.out.println("geom: " + geometryFilter + ", sql: " + sqlFilter + ", unsupp: "
                + unsupportedFilter);

        assertTrue(geometryFilter instanceof BBOX);
        assertTrue(sqlFilter instanceof PropertyIsGreaterThan);
        assertTrue(unsupportedFilter instanceof PropertyIsEqualTo);

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        // @id = 'DELETEME.1' AND STRING_COL = 'test'
        filter = ff.and(ff.id(Collections.singleton(ff.featureId(typeName + ".1"))), ff.equals(ff
                .property("STRING_COL"), ff.literal("test")));

        filteringQuery = new DefaultQuery(typeName, filter);
        // filteringQuery based on the above filter...
        sdeQuery = getQueryFiltered();

        try {
            filters = sdeQuery.getFilters();
        } finally {
            sdeQuery.session.dispose();
            sdeQuery.close();
        }
        geometryFilter = filters.getGeometryFilter();
        sqlFilter = filters.getSqlFilter();
        unsupportedFilter = filters.getUnsupportedFilter();

        System.out.println("geom: " + geometryFilter + ", sql: " + sqlFilter + ", unsupp: "
                + unsupportedFilter);

        Assert.assertEquals(Filter.INCLUDE, geometryFilter);
        assertTrue(String.valueOf(sqlFilter), sqlFilter instanceof And);
        Assert.assertEquals(Filter.INCLUDE, unsupportedFilter);

        // AND( @id = 'DELETEME.1' )
        List<Filter> singleAnded = Collections.singletonList((Filter) ff.id(Collections
                .singleton(ff.featureId(typeName + ".1"))));
        filter = ff.and(singleAnded);

        filteringQuery = new DefaultQuery(typeName, filter);
        // filteringQuery based on the above filter...
        sdeQuery = getQueryFiltered();

        try {
            filters = sdeQuery.getFilters();
        } finally {
            sdeQuery.session.dispose();
            sdeQuery.close();
        }
        geometryFilter = filters.getGeometryFilter();
        sqlFilter = filters.getSqlFilter();
        unsupportedFilter = filters.getUnsupportedFilter();

        System.out.println("geom: " + geometryFilter + ", sql: " + sqlFilter + ", unsupp: "
                + unsupportedFilter);

        // this one should have been simplified
        assertTrue(sqlFilter instanceof Id);
        Assert.assertEquals(Filter.INCLUDE, geometryFilter);
        Assert.assertEquals(Filter.INCLUDE, unsupportedFilter);
    }

    @Test
    public void testWipesOutInvalidFids() throws IOException {
        final String typeName = this.typeName;
        Set<Identifier> ids = new HashSet<Identifier>();
        ids.add(ff.featureId(typeName + ".1"));
        ids.add(ff.featureId(typeName + ".2"));
        // some non valid ones...
        ids.add(ff.featureId(typeName + ".a"));
        ids.add(ff.featureId("states_.1"));

        Filter filter = ff.id(ids);
        filteringQuery = new DefaultQuery(typeName, filter);
        // filteringQuery based on the above filter...
        ArcSDEQuery sdeQuery = getQueryFiltered();

        FilterSet filters;
        try {
            filters = sdeQuery.getFilters();
        } finally {
            sdeQuery.session.dispose();
            sdeQuery.close();
        }
        Filter sqlFilter = filters.getSqlFilter();
        assertTrue(sqlFilter instanceof Id);
        Id id = (Id) sqlFilter;
        Assert.assertEquals(2, id.getIDs().size());
        Set<Identifier> validFids = new HashSet<Identifier>();
        validFids.add(ff.featureId(typeName + ".1"));
        validFids.add(ff.featureId(typeName + ".2"));
        Id expected = ff.id(validFids);
        Assert.assertEquals(expected, id);
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

    @Test
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

    @Test
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

    @Test
    public void testCalculateResultCount() throws Exception {
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = dstore.getFeatureSource(
                typeName).getFeatures();
        FeatureIterator<SimpleFeature> reader = features.features();
        int read = 0;
        try {
            while (reader.hasNext()) {
                reader.next();
                read++;
            }
        } finally {
            reader.close();
        }
        ArcSDEQuery q = getQueryAll();
        int calculated;
        try {
            calculated = q.calculateResultCount();
        } finally {
            q.session.dispose();
            q.close();
        }
        Assert.assertEquals(read, calculated);

        q = getQueryFiltered();
        try {
            calculated = q.calculateResultCount();
        } finally {
            q.session.dispose();
            q.close();
        }
        Assert.assertEquals(FILTERING_COUNT, calculated);
    }

    @Test
    public void testCalculateQueryExtent() throws Exception {
        {
            FeatureCollection<SimpleFeatureType, SimpleFeature> features;
            features = dstore.getFeatureSource(typeName).getFeatures();
            FeatureIterator<SimpleFeature> reader = features.features();
            SimpleFeatureType featureType = features.getSchema();
            GeometryDescriptor defaultGeometry = featureType.getGeometryDescriptor();
            ReferencedEnvelope real = new ReferencedEnvelope(defaultGeometry
                    .getCoordinateReferenceSystem());
            try {
                while (reader.hasNext()) {
                    real.include(reader.next().getBounds());
                }
            } finally {
                reader.close();
            }

            // TODO: make calculateQueryExtent to return ReferencedEnvelope
            ArcSDEQuery queryAll = getQueryAll();
            Envelope actual;
            try {
                actual = queryAll.calculateQueryExtent();
            } finally {
                queryAll.close();
            }
            Envelope expected = new Envelope(real);
            assertNotNull(actual);
            assertEquals(expected, actual);

        }
        {
            FeatureReader<SimpleFeatureType, SimpleFeature> featureReader;
            featureReader = dstore.getFeatureReader(filteringQuery, Transaction.AUTO_COMMIT);
            ReferencedEnvelope real = new ReferencedEnvelope();
            try {
                while (featureReader.hasNext()) {
                    real.include(featureReader.next().getBounds());
                }
            } finally {
                featureReader.close();
            }

            ArcSDEQuery queryFiltered = getQueryFiltered();
            Envelope actual;
            try {
                actual = queryFiltered.calculateQueryExtent();
            } finally {
                queryFiltered.close();
            }
            assertNotNull(actual);
            Envelope expected = new Envelope(real);
            assertEquals(expected, actual);
        }
    }

    private void assertEquals(Envelope e1, Envelope e2) {
        final double tolerance = 1.0E-9;
        Assert.assertEquals(e1.getMinX(), e2.getMinX(), tolerance);
        Assert.assertEquals(e1.getMinY(), e2.getMinY(), tolerance);
        Assert.assertEquals(e1.getMaxX(), e2.getMaxX(), tolerance);
        Assert.assertEquals(e1.getMaxY(), e2.getMaxY(), tolerance);
    }
}
