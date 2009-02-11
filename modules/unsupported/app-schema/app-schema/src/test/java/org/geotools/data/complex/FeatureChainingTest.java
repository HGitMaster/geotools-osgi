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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import junit.framework.TestCase;
import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.complex.config.EmfAppSchemaReader;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.Types;
import org.geotools.filter.RegfuncFilterFactoryImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

/**
 * This is the tests for feature chaining; nesting complex attributes (feature and non-feature)
 * inside another complex attribute.
 * 
 * @author Rini Angreani, Curtin University of Technology
 */
public class FeatureChainingTest extends TestCase {
    static final String GSMLNS = "http://www.cgi-iugs.org/xml/GeoSciML/2";

    static final String GMLNS = "http://www.opengis.net/gml";

    static final Name MAPPED_FEATURE_TYPE = Types.typeName(GSMLNS, "MappedFeatureType");

    static final Name MAPPED_FEATURE = Types.typeName(GSMLNS, "MappedFeature");

    static final Name GEOLOGIC_UNIT_TYPE = Types.typeName(GSMLNS, "GeologicUnitType");

    static final Name GEOLOGIC_UNIT = Types.typeName(GSMLNS, "GeologicUnit");

    static final Name COMPOSITION_PART_TYPE = Types.typeName(GSMLNS, "CompositionPartType");

    static final Name COMPOSITION_PART = Types.typeName(GSMLNS, "CompositionPart");

    static final Name CGI_TERM_VALUE = Types.typeName(GSMLNS, "CGI_TermValue");

    static final Name CGI_TERM_VALUE_TYPE = Types.typeName(GSMLNS, "CGI_TermValueType");

    static final Name CONTROLLED_CONCEPT = Types.typeName(GSMLNS, "ControlledConcept");

    static FilterFactory ff = new RegfuncFilterFactoryImpl(null);

    /**
     * Map of geological unit values to mapped feature objects based on
     * mappedFeaturePropertyFile.properties
     */
    final Map<String, String> mfToGuMap = new HashMap<String, String>() {
        {
            put("mf1", "gu.25699");
            put("mf2", "gu.25678");
        }
    };

    /**
     * Map of compositional part values to geological unit objects based on geologicUnit.properties
     */
    final Map<String, String> guToCpMap = new HashMap<String, String>() {
        {
            put("gu.25699", "cp.167775491936278844");
            put("gu.25678", "cp.167775491936278856;cp.167775491936278844");
            put("gu.25682", "cp.167775491936278812");
        }
    };

    /**
     * Map of exposure colour values to geological unit objects based on geologicUnit.properties
     */
    final Map<String, String> guToExposureColorMap = new HashMap<String, String>() {
        {
            put("gu.25699", "Blue");
            put("gu.25678", "Yellow;Blue");
            put("gu.25682", "Red");
        }
    };

    /**
     * Map of out crop character values to geological unit objects based on geologicUnit.properties
     */
    private Map<String, String> guToOutcropCharacterMap = new HashMap<String, String>() {
        {
            put("gu.25699", "x");
            put("gu.25678", "x;y");
            put("gu.25682", "z");

        }
    };

    final String schemaBase = "/test-data/";

    EmfAppSchemaReader reader;

    private FeatureSource mfSource;

    /**
     * Generated mapped features
     */
    private FeatureCollection<FeatureType, Feature> mfFeatures;

    /**
     * Generated geological unit features
     */
    private FeatureCollection<FeatureType, Feature> guFeatures;

    /**
     * Generated compositional part fake "features"
     */
    private FeatureCollection<FeatureType, Feature> cpFeatures;

    /**
     * Geological unit data access
     */
    private DataAccess guDataAccess;

    /**
     * Compositional part data access
     */
    private DataAccess cpDataAccess;

    /**
     * Mapped feature data access
     */
    private DataAccess mfDataAccess;

    /**
     * CGI Term Value data access
     */
    private DataAccess cgiDataAccess;

    /**
     * Set up the reader
     * 
     * @throws Exception
     */
    protected void setUp() throws Exception {
        super.setUp();
        reader = EmfAppSchemaReader.newInstance();
    }

    /**
     * Release resources
     * 
     * @throws Exception
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test that chaining works
     * 
     * @throws Exception
     */
    public void testFeatureChaining() throws Exception {
        this.loadDataAccesses();

        Iterator mfIterator = mfFeatures.iterator();

        Iterator guIterator = guFeatures.iterator();

        // Extract all geological unit features into a map by id
        Map<String, Feature> guMap = new HashMap<String, Feature>();
        Feature guFeature;
        while (guIterator.hasNext()) {
            guFeature = (Feature) guIterator.next();
            String guId = guFeature.getIdentifier().getID();
            if (!guMap.containsKey(guId)) {
                guMap.put(guId, guFeature);
            }
        }

        // Extract all compositional part "features" into a map by id
        Iterator cpIterator = cpFeatures.iterator();
        Map<String, Feature> cpMap = new HashMap<String, Feature>();
        Feature cpFeature;
        while (cpIterator.hasNext()) {
            cpFeature = (Feature) cpIterator.next();
            String cpId = cpFeature.getIdentifier().getID();
            if (!cpMap.containsKey(cpId)) {
                cpMap.put(cpId, cpFeature);
            }
        }

        Feature mfFeature;
        Collection nestedGuFeatures;
        String guId;
        final String NESTED_LINK = "specification";
        Collection nestedCpFeatures;
        String cpId;
        while (mfIterator.hasNext()) {
            mfFeature = (Feature) mfIterator.next();
            String mfId = mfFeature.getIdentifier().toString();
            String[] guIds = this.mfToGuMap.get(mfId).split(";");

            // make sure we have the right number of nested features
            nestedGuFeatures = (Collection) mfFeature.getProperty(NESTED_LINK).getValue();
            assertEquals(guIds.length, nestedGuFeatures.size());

            ArrayList<String> nestedGuIds = new ArrayList<String>();

            for (Feature nestedGuFeature : (Collection<Feature>) nestedGuFeatures) {
                /**
                 * Test geological unit
                 */
                // make sure each of the nested geologic unit is valid
                guId = nestedGuFeature.getIdentifier().toString();
                assertEquals(true, guMap.containsKey(guId));

                nestedGuIds.add(guId);

                // make sure the nested geologic unit feature has the right properties
                guFeature = guMap.get(guId.toString());
                Collection<Property> guProperties = guFeature.getProperties();
                assertEquals(guProperties, nestedGuFeature.getProperties());

                /**
                 * Test compositional part
                 */
                // make sure the right number of nested features are there
                String[] cpIds = this.guToCpMap.get(guId).split(";");
                nestedCpFeatures = (Collection) guFeature.getProperty("composition").getValue();
                assertEquals(cpIds.length, nestedCpFeatures.size());

                ArrayList<String> nestedCpIds = new ArrayList<String>();
                for (Feature nestedCpFeature : (Collection<Feature>) nestedCpFeatures) {
                    // make sure each of the nested compositional part feature is valid
                    cpId = nestedCpFeature.getIdentifier().toString();
                    assertEquals(true, cpMap.containsKey(cpId));

                    nestedCpIds.add(cpId);

                    // make sure each of the nested compositional part has the right properties
                    cpFeature = cpMap.get(cpId.toString());
                    Collection<Property> cpProperties = cpFeature.getProperties();
                    assertEquals(cpProperties, nestedCpFeature.getProperties());

                }
                // make sure all the nested compositional part features are there
                assertEquals(nestedCpIds.containsAll(Arrays.asList(cpIds)), true);
            }
            // make sure all the nested geological unit features are there
            assertEquals(nestedGuIds.containsAll(Arrays.asList(guIds)), true);

        }
        mfFeatures.close(mfIterator);
        guFeatures.close(guIterator);
        cpFeatures.close(cpIterator);

        disposeDataAccesses();
    }

    /**
     * Test nesting multiple multi valued properties. Both exposure color and outcrop character are
     * multi valued. By making sure that both are nested inside geological unit feature, it's
     * verified that nesting multiple multi valued properties is possible.
     * 
     * @throws Exception
     */
    public void testMultipleMultiValuedProperties() throws Exception {
        this.loadDataAccesses();
        Iterator guIterator = guFeatures.iterator();

        Feature guFeature;
        final String EXPOSURE_COLOR = "exposureColor";
        final String OUTCROP_CHARACTER = "outcropCharacter";
        while (guIterator.hasNext()) {
            guFeature = (Feature) guIterator.next();
            String guId = guFeature.getIdentifier().toString();
            ArrayList realValues = new ArrayList();

            /**
             * Test exposure color
             */
            Collection<Feature> nestedTermValues = (Collection<Feature>) guFeature.getProperty(
                    EXPOSURE_COLOR).getValue();
            // get exposure color property values from geological unit feature
            for (Feature feature : nestedTermValues) {
                realValues.add(feature.getProperty("value").getValue());
            }

            // compares the values from the property file
            String[] values = this.guToExposureColorMap.get(guId).split(";");
            assertEquals(realValues.size(), values.length);
            assertEquals(realValues.containsAll(Arrays.asList(values)), true);

            /**
             * Test outcrop character
             */
            nestedTermValues = (Collection<Feature>) guFeature.getProperty(OUTCROP_CHARACTER)
                    .getValue();
            realValues.clear();
            // get nested outcrop character values from geological unit feature
            for (Feature feature : nestedTermValues) {
                realValues.add(feature.getProperty("value").getValue());
            }
            // compare with values from property file
            values = this.guToOutcropCharacterMap.get(guId).split(";");
            assertEquals(realValues.size(), values.length);
            assertEquals(realValues.containsAll(Arrays.asList(values)), true);
        }
        this.disposeDataAccesses();
        
        guFeatures.close(guIterator);
    }

    /**
     * Test mapping multi-valued simple properties still works.
     * 
     * @throws Exception
     */
    public void testMultiValuedSimpleProperties() throws Exception {
        // Controlled Concept can have many gml:name
        Map dsParams = new HashMap();
        URL url = getClass().getResource(schemaBase + "ControlledConcept.xml");
        assertNotNull(url);

        dsParams.put("dbtype", "app-schema");
        dsParams.put("url", url.toExternalForm());
        DataAccess dataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(dataAccess);

        FeatureType featureType = dataAccess.getSchema(CONTROLLED_CONCEPT);
        assertNotNull(featureType);

        FeatureSource fSource = (FeatureSource) dataAccess.getFeatureSource(CONTROLLED_CONCEPT);
        FeatureCollection features = (FeatureCollection) fSource.getFeatures();

        final int EXPECTED_RESULTS = 2;
        assertEquals(getCount(features), EXPECTED_RESULTS);

        Iterator<Feature> iterator = features.iterator();
        while (iterator.hasNext()) {
            Feature next = iterator.next();
            Property name = next.getProperty("name");
            Object value = name.getValue();
            assertEquals(value instanceof Collection, true);
            if (next.getIdentifier().toString().equals("1")) {
                // see ControlledConcept.properties where id = 1
                assertEquals(((Collection) value).size(), 3);
            } else {
                // see ControlledConcept.properties where id = 2
                assertEquals(((Collection) value).size(), 1);
            }
        }

        dataAccess.dispose();
    }

    /**
     * Test filtering attributes on nested features.
     * 
     * @throws Exception
     */
    public void testFilters() throws Exception {
        this.loadDataAccesses();
        // make sure filter query can be made on MappedFeature based on GU properties
        //
        // <ogc:Filter>
        // <ogc:PropertyIsEqualTo>
        // <ogc:Function name="contains_text">
        // <ogc:PropertyName>
        // gsml:specification/gsml:GeologicUnit/gml:description
        // </ogc:PropertyName>
        // <ogc:Literal>Olivine basalt, tuff, microgabbro, minor sedimentary rocks</ogc:Literal>
        // </ogc:Function>
        // <ogc:Literal>1</ogc:Literal>
        // </ogc:PropertyIsEqualTo>
        // </ogc:Filter>

        Expression property = ff.property("gsml:specification/gsml:GeologicUnit/gml:description");
        Expression string = ff
                .literal("Olivine basalt, tuff, microgabbro, minor sedimentary rocks");
        // <ogc:PropertyIsEqualTo>
        Filter filter = ff.equals(property, string);

        FeatureCollection filteredResults = mfSource.getFeatures(filter);

        assertEquals(getCount(filteredResults), 2);

        /**
         * Test filtering on multi valued properties
         */
        FeatureSource guSource = AppSchemaDataAccessRegistry.getMappingFeatureSource(GEOLOGIC_UNIT);
        // composition part is a multi valued property
        // we're testing that we can get a geologic unit which has a composition part with a
        // significant proportion value
        property = ff
                .property("gsml:composition/gsml:CompositionPart/gsml:proportion/gsml:CGI_TermValue/gsml:value");
        string = ff.literal("significant");
        filter = ff.equals(property, string);
        filteredResults = guSource.getFeatures(filter);
        assertEquals(getCount(filteredResults), 3);

        this.disposeDataAccesses();
    }

    /**
     * Load all the data accesses.
     * 
     * @return
     * @throws Exception
     */
    private void loadDataAccesses() throws Exception {
        /**
         * Load mapped feature data access
         */
        Map dsParams = new HashMap();
        URL url = getClass().getResource(schemaBase + "MappedFeaturePropertyfile.xml");
        assertNotNull(url);

        dsParams.put("dbtype", "app-schema");
        dsParams.put("url", url.toExternalForm());
        mfDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(mfDataAccess);

        FeatureType mappedFeatureType = mfDataAccess.getSchema(MAPPED_FEATURE);
        assertNotNull(mappedFeatureType);

        mfSource = (FeatureSource) mfDataAccess.getFeatureSource(MAPPED_FEATURE);
        mfFeatures = (FeatureCollection) mfSource.getFeatures();

        /**
         * Load geologic unit data access
         */
        url = getClass().getResource(schemaBase + "GeologicUnit.xml");
        assertNotNull(url);

        dsParams.put("url", url.toExternalForm());
        guDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(guDataAccess);

        FeatureType guType = guDataAccess.getSchema(GEOLOGIC_UNIT);
        assertNotNull(guType);

        FeatureSource guSource = (FeatureSource) guDataAccess.getFeatureSource(GEOLOGIC_UNIT);
        guFeatures = (FeatureCollection) guSource.getFeatures();

        /**
         * Load composition part data access
         */
        url = getClass().getResource(schemaBase + "CompositionPart.xml");
        assertNotNull(url);

        dsParams.put("url", url.toExternalForm());
        cpDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(cpDataAccess);

        FeatureType cpType = cpDataAccess.getSchema(COMPOSITION_PART);
        assertNotNull(cpType);

        FeatureSource cpSource = (FeatureSource) cpDataAccess.getFeatureSource(COMPOSITION_PART);
        cpFeatures = (FeatureCollection) cpSource.getFeatures();

        /**
         * Load CGI Term Value data access
         */
        url = getClass().getResource(schemaBase + "CGITermValue.xml");
        assertNotNull(url);

        dsParams.put("url", url.toExternalForm());
        cgiDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(cgiDataAccess);

        FeatureType cgiType = cgiDataAccess.getSchema(CGI_TERM_VALUE);
        assertNotNull(cgiType);

        FeatureSource cgiSource = (FeatureSource) cgiDataAccess.getFeatureSource(CGI_TERM_VALUE);
        FeatureCollection cgiFeatures = (FeatureCollection) cgiSource.getFeatures();

        int EXPECTED_RESULT_COUNT = 2;

        int resultCount = getCount(mfFeatures);
        assertEquals(EXPECTED_RESULT_COUNT, resultCount);

        EXPECTED_RESULT_COUNT = 3;
        resultCount = getCount(guFeatures);
        assertEquals(EXPECTED_RESULT_COUNT, resultCount);

        resultCount = getCount(cpFeatures);
        assertEquals(EXPECTED_RESULT_COUNT, resultCount);

        EXPECTED_RESULT_COUNT = 8;
        resultCount = getCount(cgiFeatures);
        assertEquals(EXPECTED_RESULT_COUNT, resultCount);
    }

    /**
     * Dispose all data accesses
     */
    private void disposeDataAccesses() {
        if (mfDataAccess == null || guDataAccess == null || cpDataAccess == null
                || cgiDataAccess == null) {
            throw new UnsupportedOperationException(
                    "This is to be called after data accesses are created!");
        }
        mfDataAccess.dispose();
        guDataAccess.dispose();
        cpDataAccess.dispose();
        cgiDataAccess.dispose();
    }

    private int getCount(FeatureCollection features) {
        MappingFeatureIterator iterator = (MappingFeatureIterator) features.iterator();
        int count = 0;
        try {
            while (iterator.hasNext()) {
                iterator.next();
                count++;
            }
        } finally {
            features.close((Iterator<Feature>) iterator);
        }
        return count;
    }
}
