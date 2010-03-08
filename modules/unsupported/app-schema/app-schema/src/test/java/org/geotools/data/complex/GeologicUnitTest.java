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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.complex.config.AppSchemaDataAccessConfigurator;
import org.geotools.data.complex.config.AppSchemaDataAccessDTO;
import org.geotools.data.complex.config.CatalogUtilities;
import org.geotools.data.complex.config.EmfAppSchemaReader;
import org.geotools.data.complex.config.FeatureTypeRegistry;
import org.geotools.data.complex.config.XMLConfigDigester;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.Types;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.geotools.xml.SchemaIndex;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

/**
 * This is to ensure we have a working GeologicUnit configuration test.
 * 
 * @author Rini Angreani, Curtin University of Technology
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/app-schema/app-schema/src/test/java/org/geotools/data/complex/GeologicUnitTest.java $
 */
public class GeologicUnitTest {

    private static final String GSMLNS = "http://www.cgi-iugs.org/xml/GeoSciML/2";

    private static final String schemaBase = "/test-data/";

    private static EmfAppSchemaReader reader;

    /**
     * Set up the reader
     * 
     * @throws Exception
     *             If any exception occurs
     */
    @BeforeClass
    public static void setUp() throws Exception {
        reader = EmfAppSchemaReader.newInstance();
    }

    /**
     * Release resources
     * 
     * @throws Exception
     *             If any exception occurs
     */
    @AfterClass
    public static void tearDown() throws Exception {
        DataAccessRegistry.unregisterAll();
    }

    /**
     * Load schema
     * 
     * @param location
     *            schema location path that can be found through getClass().getResource()
     * @return 
     */
    private SchemaIndex loadSchema(final String location) throws IOException {
        final URL catalogLocation = getClass().getResource(schemaBase + "mappedPolygons.oasis.xml");
        reader.setCatalog(CatalogUtilities.buildPrivateCatalog(catalogLocation));
        return reader.parse(new URL(location), null);
    }

    /**
     * Tests if the schema-to-FM parsing code developed for complex data store configuration loading
     * can parse the GeoSciML types
     * 
     * @throws Exception
     */
    @Test
    public void testParseSchema() throws Exception {
        SchemaIndex schemaIndex = loadSchema("http://schemas.opengis.net/GeoSciML/Gsml.xsd");

        FeatureTypeRegistry typeRegistry = new FeatureTypeRegistry();
        typeRegistry.addSchemas(schemaIndex);

        Name typeName = Types.typeName(GSMLNS, "GeologicUnitType");
        ComplexType mf = (ComplexType) typeRegistry.getAttributeType(typeName);
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
    @Test
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
    @Test
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

        url = getClass().getResource(schemaBase + "MappedFeaturePropertyfile.xml");
        assertNotNull(url);

        dsParams.put("dbtype", "app-schema");
        dsParams.put("url", url.toExternalForm());
        DataAccess mfDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(mfDataAccess);

        /*
         * Make sure there are 3 geological unit features
         */
        FeatureSource guSource = (FeatureSource) guDataStore
                .getFeatureSource(FeatureChainingTest.GEOLOGIC_UNIT);

        int EXPECTED_RESULT_COUNT = 3;

        FeatureCollection guFeatures = (FeatureCollection) guSource.getFeatures();

        int resultCount = guFeatures.size();
        assertEquals(EXPECTED_RESULT_COUNT, resultCount);

        /*
         * Make sure there are 3 compositional part features
         */
        FeatureSource cpSource = DataAccessRegistry
                .getFeatureSource(FeatureChainingTest.COMPOSITION_PART);

        FeatureCollection cpFeatures = (FeatureCollection) cpSource.getFeatures();

        resultCount = cpFeatures.size();

        assertEquals(EXPECTED_RESULT_COUNT, resultCount);

        /*
         * Make sure there are 8 cgi term values
         */
        EXPECTED_RESULT_COUNT = 8;

        FeatureSource cgiSource = DataAccessRegistry
                .getFeatureSource(FeatureChainingTest.CGI_TERM_VALUE);
        FeatureCollection cgiFeatures = (FeatureCollection) cgiSource.getFeatures();

        resultCount = cgiFeatures.size();

        assertEquals(EXPECTED_RESULT_COUNT, resultCount);
    }
}
