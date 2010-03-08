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
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureSource;
import org.geotools.data.complex.config.NonFeatureTypeProxy;
import org.geotools.feature.Types;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

/**
 * This tests AppSchemaDataAccessRegistry class. When an appschema data access is created, it would
 * be registered in the registry. Once it's in the registry, its feature type mapping and feature
 * source (simple or mapped) would be accessible globally.
 * 
 * @author Rini Angreani, Curtin Universtiy of Technology
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/app-schema/app-schema/src/test/java/org/geotools/data/complex/AppSchemaDataAccessRegistryTest.java $
 */
public class AppSchemaDataAccessRegistryTest {

    public static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.data.complex");

    private static final String GSMLNS = "http://www.cgi-iugs.org/xml/GeoSciML/2";

    private static final Name MAPPED_FEATURE = Types.typeName(GSMLNS, "MappedFeature");

    private static final Name GEOLOGIC_UNIT = Types.typeName(GSMLNS, "GeologicUnit");

    private static final Name COMPOSITION_PART = Types.typeName(GSMLNS, "CompositionPart");

    private static final Name CGI_TERM_VALUE = Types.typeName(GSMLNS, "CGI_TermValue");

    private static final Name CONTROLLED_CONCEPT = Types.typeName(GSMLNS, "ControlledConcept");

    private static final String schemaBase = "/test-data/";

    /**
     * Geological unit data access
     */
    private static AppSchemaDataAccess guDataAccess;

    /**
     * Compositional part data access
     */
    private static AppSchemaDataAccess cpDataAccess;

    /**
     * Mapped feature data access
     */
    private static AppSchemaDataAccess mfDataAccess;

    /**
     * CGI Term Value data access
     */
    private static AppSchemaDataAccess cgiDataAccess;

    /**
     * Controlled Concept data access
     */
    private static AppSchemaDataAccess ccDataAccess;

    /**
     * Test registering and unregistering all data accesses works.
     * 
     * @throws Exception
     */
    @Test
    public void testRegisterAndUnregisterDataAccess() throws Exception {
        /**
         * Check that data access are registered
         */
        this.checkRegisteredDataAccess(mfDataAccess, MAPPED_FEATURE, false);
        this.checkRegisteredDataAccess(guDataAccess, GEOLOGIC_UNIT, false);
        this.checkRegisteredDataAccess(cpDataAccess, COMPOSITION_PART, true);
        this.checkRegisteredDataAccess(cgiDataAccess, CGI_TERM_VALUE, true);
        this.checkRegisteredDataAccess(ccDataAccess, CONTROLLED_CONCEPT, true);

        /**
         * Now unregister, and see if they're successful
         */
        unregister(mfDataAccess, MAPPED_FEATURE);
        unregister(guDataAccess, GEOLOGIC_UNIT);
        unregister(cpDataAccess, COMPOSITION_PART);
        unregister(cgiDataAccess, CGI_TERM_VALUE);
        unregister(ccDataAccess, CONTROLLED_CONCEPT);
    }

    /**
     * Test that asking for a nonexistent type causes an excception to be thrown with the correct
     * number of type names in the detail message.
     * 
     * @throws Exception
     */
    @Test
    public void testThrowDataSourceException() throws Exception {
        Name typeName = Types.typeName(GSMLNS, "DoesNotExist");
        boolean handledException = false;
        try {
            AppSchemaDataAccessRegistry.getMapping(typeName);
        } catch (DataSourceException e) {
            LOGGER.info(e.toString());
            handledException = true;
        }
        assertTrue("Expected a DataSourceException to have been thrown and handled",
                handledException);
    }

    /**
     * Load all data accesses
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        /**
         * Load Mapped Feature data access
         */
        Map dsParams = new HashMap();
        URL url = AppSchemaDataAccessRegistryTest.class.getResource(schemaBase + "MappedFeaturePropertyfile.xml");
        assertNotNull(url);
        dsParams.put("dbtype", "app-schema");
        dsParams.put("url", url.toExternalForm());
        mfDataAccess = (AppSchemaDataAccess) DataAccessFinder.getDataStore(dsParams);
        assertNotNull(mfDataAccess);

        /**
         * Load Geological Unit data access
         */
        url = AppSchemaDataAccessRegistryTest.class.getResource(schemaBase + "GeologicUnit.xml");
        assertNotNull(url);
        dsParams.put("url", url.toExternalForm());
        guDataAccess = (AppSchemaDataAccess) DataAccessFinder.getDataStore(dsParams);
        assertNotNull(guDataAccess);

        /**
         * Find Compositional Part data access
         */
        cpDataAccess = (AppSchemaDataAccess) DataAccessRegistry.getDataAccess(COMPOSITION_PART);
        assertNotNull(cpDataAccess);

        /**
         * Find CGI Term Value data access
         */
        cgiDataAccess = (AppSchemaDataAccess) DataAccessRegistry.getDataAccess(CGI_TERM_VALUE);
        assertNotNull(cgiDataAccess);

        /**
         * Find ControlledConcept data access
         */
        ccDataAccess = (AppSchemaDataAccess) DataAccessRegistry.getDataAccess(CONTROLLED_CONCEPT);
        assertNotNull(ccDataAccess);
    }
    
    /**
     * Clean the registry so it doesn't affect other tests
     */
    @AfterClass
    public static void tearDown() {
        DataAccessRegistry.unregisterAll();
    }

    /**
     * Tests that registry works.
     * 
     * @param dataAccess
     *            The app schema data access to check
     * @param typeName
     *            Feature type
     * @param isNonFeature
     *            true if the type is non feature
     * @throws IOException
     */
    private void checkRegisteredDataAccess(AppSchemaDataAccess dataAccess, Name typeName,
            boolean isNonFeature) throws IOException {
        FeatureTypeMapping mapping = AppSchemaDataAccessRegistry.getMapping(typeName);
        assertNotNull(mapping);
        // compare with the supplied data access
        assertEquals(dataAccess.getMapping(typeName).equals(mapping), true);
        if (isNonFeature) {
            assertEquals(mapping.getTargetFeature().getType() instanceof NonFeatureTypeProxy, true);
        }

        // should return a simple feature source
        FeatureSource<FeatureType, Feature> source = AppSchemaDataAccessRegistry
                .getSimpleFeatureSource(typeName);
        assertNotNull(source);
        assertEquals(mapping.getSource(), source);

        // should return a mapping feature source
        FeatureSource<FeatureType, Feature> mappedSource = DataAccessRegistry
                .getFeatureSource(typeName);
        assertNotNull(mappedSource);
        // compare with the supplied data access
        assertEquals(mappedSource.getDataStore().equals(dataAccess), true);
    }

    /**
     * Tests that unregistering data access works
     * 
     * @param dataAccess
     *            The data access
     * @param typeName
     *            The feature type name
     * @throws IOException
     */
    private void unregister(DataAccess dataAccess, Name typeName) throws IOException {
        DataAccessRegistry.unregister(dataAccess);
        boolean notFound = false;
        try {
            FeatureTypeMapping mapping = AppSchemaDataAccessRegistry.getMapping(typeName);
        } catch (DataSourceException e) {
            notFound = true;
        }
        if (!notFound) {
            fail("Expecting DataSourceException but didn't occur. Deregistering data access fails.");
        }
        notFound = false;
        try {
            FeatureSource source = AppSchemaDataAccessRegistry.getSimpleFeatureSource(typeName);
        } catch (DataSourceException e) {
            notFound = true;
        }
        if (!notFound) {
            fail("Expecting DataSourceException but didn't occur. Deregistering data access fails.");
        }
    }
}
