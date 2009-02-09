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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.tools.ResolvingXMLReader;
import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.complex.config.AppSchemaDataAccessConfigurator;
import org.geotools.data.complex.config.AppSchemaDataAccessDTO;
import org.geotools.data.complex.config.EmfAppSchemaReader;
import org.geotools.data.complex.config.XMLConfigDigester;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.Types;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

/**
 * This is to ensure we have a working GeologicUnit configuration test.
 * 
 * @author Rini Angreani, Curtin University of Technology
 */
public class GeologicUnitTest extends TestCase {

    private static final String GSMLNS = "http://www.cgi-iugs.org/xml/GeoSciML/2";

    final String schemaBase = "/test-data/";

    EmfAppSchemaReader reader;

    /**
     * Set up the reader
     * 
     * @throws Exception
     *             If any exception occurs
     */
    protected void setUp() throws Exception {
        super.setUp();
        reader = EmfAppSchemaReader.newInstance();
    }

    /**
     * Release resources
     * 
     * @throws Exception
     *             If any exception occurs
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Load schema
     * 
     * @param location
     *            schema location path that can be found through getClass().getResource()
     */
    private void loadSchema(final String location) throws IOException {
        final URL catalogLocation = getClass().getResource(schemaBase + "mappedPolygons.oasis.xml");
        final Catalog catalog = new ResolvingXMLReader().getCatalog();
        catalog.getCatalogManager().setVerbosity(9);
        catalog.parseCatalog(catalogLocation);

        reader.setCatalog(catalog);

        reader.parse(new URL(location));
    }

    /**
     * Tests if the schema-to-FM parsing code developed for complex data store configuration loading
     * can parse the GeoSciML types
     * 
     * @throws Exception
     */
    public void testParseSchema() throws Exception {
        loadSchema("http://schemas.opengis.net/GeoSciML/Gsml.xsd");

        Map typeRegistry = reader.getTypeRegistry();

        Name typeName = Types.typeName(GSMLNS, "GeologicUnitType");
        ComplexType mf = (ComplexType) typeRegistry.get(typeName);
        assertNotNull(mf);
        assertTrue(mf instanceof FeatureType);

        AttributeType superType = mf.getSuper();
        assertNotNull(superType);
        Name superTypeName = Types.typeName(GSMLNS, "GeologicFeatureType");
        assertEquals(superTypeName, superType.getName());
        assertTrue(superType instanceof FeatureType);
    }

    /**
     * Test that mappings are loaded OK.
     * 
     * @throws Exception
     */
    public void testLoadMappingsConfig() throws Exception {
        XMLConfigDigester reader = new XMLConfigDigester();
        final URL url = getClass().getResource(schemaBase + "GeologicUnit.xml");

        AppSchemaDataAccessDTO config = reader.parse(url);

        Set mappings = AppSchemaDataAccessConfigurator.buildMappings(config);

        assertNotNull(mappings);
        assertEquals(1, mappings.size());
    }

    /**
     * Test that geologic unit features are returned correctly.
     * 
     * @throws Exception
     */
    public void testGetFeatures() throws Exception {
        /*
         * Initiate data accesses and make sure they have the mappings
         */
        final Map dsParams = new HashMap();
        URL url = getClass().getResource(schemaBase + "GeologicUnit.xml");
        assertNotNull(url);
        dsParams.put("dbtype", "app-schema");
        dsParams.put("url", url.toExternalForm());

        DataAccess guDataStore = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(guDataStore);
        FeatureType geologicUnitType = guDataStore.getSchema(FeatureChainingTest.GEOLOGIC_UNIT);
        assertNotNull(geologicUnitType);

        url = getClass().getResource(schemaBase + "CompositionPart.xml");
        assertNotNull(url);
        dsParams.put("url", url.toExternalForm());
        DataAccess cpDataStore = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(cpDataStore);
        FeatureType cpType = cpDataStore.getSchema(FeatureChainingTest.COMPOSITION_PART);
        assertNotNull(cpType);

        url = getClass().getResource(schemaBase + "CGITermValue.xml");
        assertNotNull(url);
        dsParams.put("url", url.toExternalForm());
        DataAccess cgiDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(cgiDataAccess);
        FeatureType cgiType = cgiDataAccess.getSchema(FeatureChainingTest.CGI_TERM_VALUE);
        assertNotNull(cgiType);

        /*
         * Make sure there are 3 geological unit features
         */
        FeatureSource guSource = (FeatureSource) guDataStore
                .getFeatureSource(FeatureChainingTest.GEOLOGIC_UNIT);

        int EXPECTED_RESULT_COUNT = 3;

        FeatureCollection guFeatures = (FeatureCollection) guSource.getFeatures();

        int resultCount = getCount(guFeatures);
        assertEquals(EXPECTED_RESULT_COUNT, resultCount);

        /*
         * Make sure there are 3 compositional part features
         */
        FeatureSource cpSource = (FeatureSource) cpDataStore
                .getFeatureSource(FeatureChainingTest.COMPOSITION_PART);

        FeatureCollection cpFeatures = (FeatureCollection) cpSource.getFeatures();

        resultCount = getCount(cpFeatures);

        assertEquals(EXPECTED_RESULT_COUNT, resultCount);

        /*
         * Make sure there are 8 cgi term values
         */
        EXPECTED_RESULT_COUNT = 8;

        FeatureSource cgiSource = (FeatureSource) cgiDataAccess
                .getFeatureSource(FeatureChainingTest.CGI_TERM_VALUE);
        FeatureCollection cgiFeatures = (FeatureCollection) cgiSource.getFeatures();

        resultCount = getCount(cgiFeatures);

        assertEquals(EXPECTED_RESULT_COUNT, resultCount);

        // Dispose data stores
        guDataStore.dispose();
        cpDataStore.dispose();
        cgiDataAccess.dispose();
    }

    /**
     * Return number of built features.
     * 
     * @param features
     * @return number of features in the collection
     */
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
}
