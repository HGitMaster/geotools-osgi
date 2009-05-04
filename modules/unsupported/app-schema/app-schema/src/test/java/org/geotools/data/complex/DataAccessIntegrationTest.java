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

import java.awt.RenderingHints.Key;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.ServiceInfo;
import org.geotools.data.complex.config.EmfAppSchemaReader;
import org.geotools.data.property.PropertyDataStore;
import org.geotools.factory.Hints;
import org.geotools.feature.AttributeImpl;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.ComplexAttributeImpl;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureImpl;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.feature.Types;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.FeatureTypeImpl;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.RegfuncFilterFactoryImpl;
import org.geotools.filter.expression.FeaturePropertyAccessorFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gml3.GMLSchema;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.sort.SortBy;
import org.opengis.util.ProgressListener;

import junit.framework.TestCase;

/**
 * This is to test the integration of a data access (which does not necessarily have to be an
 * app-schema data access) as an input to an app-schema data access. The main purpose of such
 * configuration is so that we could use a data access that produces complex features in an XML
 * form, and re-map them to produce complex features of another XML form. Additionally, once the
 * features have been re-mapped, they could also chain/be chained by other features of the mapped
 * XML form. See FeatureChainingTest.java for details.
 * 
 * @author Rini Angreani, Curtin University of Technology
 */
public class DataAccessIntegrationTest extends TestCase {
    static final String GSMLNS = "http://www.cgi-iugs.org/xml/GeoSciML/2";

    static final String GMLNS = "http://www.opengis.net/gml";

    static final String MO = "urn:cgi:xmlns:GGIC:MineralOccurrence:1.0";

    static final Name MAPPED_FEATURE_TYPE = Types.typeName(GSMLNS, "MappedFeatureType");

    static final Name MAPPED_FEATURE = Types.typeName(GSMLNS, "MappedFeature");

    static final Name GEOLOGIC_UNIT_TYPE = Types.typeName(GSMLNS, "GeologicUnitType");

    static final Name GEOLOGIC_UNIT = Types.typeName(GSMLNS, "GeologicUnit");

    static final Name COMPOSITION_PART_TYPE = Types.typeName(GSMLNS, "CompositionPartType");

    static final Name COMPOSITION_PART = Types.typeName(GSMLNS, "CompositionPart");

    static final Name EARTH_RESOURCE = Types.typeName(MO, "EarthResource");

    static final Name EARTH_RESOURCE_TYPE = Types.typeName(MO, "EarthResourceType");

    static final Name MINERAL_DEPOSIT_TYPE = Types.typeName(MO, "MineralDepositModelType");

    static final Name MINERAL_DEPOSIT_PROPERTY_TYPE = Types.typeName(MO,
            "MineralDepositModelPropertyType");

    static final String schemaBase = "/test-data/";

    /**
     * Filter factory instance
     */
    static FilterFactory ff = new RegfuncFilterFactoryImpl(null);

    /**
     * The input data access in MO form
     */
    private static DataAccess<FeatureType, Feature> minOccDataAccess;

    /**
     * The converted Geologic Unit data access in GSML form
     */
    private DataAccess<FeatureType, Feature> mappedGUDataAccess;

    /**
     * Mapped Feature data access in GSML form
     */
    private DataAccess<FeatureType, Feature> mfDataAccess;

    /**
     * Composition Part data access in GSML form
     */
    private DataAccess<FeatureType, Feature> cpDataAccess;

    /**
     * CGI Value data access in GSML Form
     */
    private DataAccess<FeatureType, Feature> cgiDataAccess;

    /**
     * App schema config reader
     */
    private EmfAppSchemaReader reader;

    /**
     * GSML:geologicUnit feature source coming from the mapped data access
     */
    private FeatureSource<FeatureType, Feature> guFeatureSource;

    /**
     * Collection of MO:earthResource complex features
     */
    private ArrayList<Feature> minOccFeatures;

    /**
     * Collection of GSML:compositionPart complex features
     */
    private ArrayList<Feature> cpFeatures;

    /**
     * Collection of GSML:mappedFeature complex features
     */
    private ArrayList<Feature> mfFeatures;

    /**
     * Create the input data access containing complex features of MO form.
     */
    protected void setUp() throws Exception {
        File dir = new File(getClass().getResource(schemaBase).toURI());
        PropertyDataStore dataStore = new PropertyDataStore(dir);
        FeatureSource<SimpleFeatureType, SimpleFeature> simpleFeatureSource = dataStore
                .getFeatureSource(EARTH_RESOURCE);

        // get the simple features from EarthResource.properties file
        FeatureCollection<SimpleFeatureType, SimpleFeature> fCollection = simpleFeatureSource
                .getFeatures();
        reader = EmfAppSchemaReader.newInstance();
        reader.parse(getClass().getResource(
                schemaBase + "commonSchemas_new/mineralOccurrence/mineralOccurrence.xsd"));

        Map typeRegistry = reader.getTypeRegistry();
        ComplexType complexType = (ComplexType) typeRegistry.get(EARTH_RESOURCE_TYPE);

        // extend from abstract feature type
        FeatureType earthResourceType = new FeatureTypeImpl(complexType.getName(), complexType
                .getDescriptors(), null, true, complexType.getRestrictions(),
                GMLSchema.ABSTRACTFEATURETYPE_TYPE, null);

        minOccFeatures = getEarthResourceFeatures(fCollection, earthResourceType);

        minOccDataAccess = new MinOccDataAccess(minOccFeatures, simpleFeatureSource.getSchema());

        this.loadDataAccesses();
    }

    /**
     * Create complex features of type MO:EarthResource
     * 
     * @param fCollection
     *            Simple features collection
     * @param earthResourceType
     *            Earth Resource schema
     * @return MO:EarthResource features
     */
    private ArrayList<Feature> getEarthResourceFeatures(
            FeatureCollection<SimpleFeatureType, SimpleFeature> fCollection,
            FeatureType earthResourceType) {
        ArrayList<Feature> features = new ArrayList<Feature>();

        AttributeDescriptor featureDesc = new AttributeDescriptorImpl(earthResourceType,
                EARTH_RESOURCE, 0, -1, false, null);
        ComplexType mineralDepositType = (ComplexType) reader.getTypeRegistry().get(
                MINERAL_DEPOSIT_TYPE);
        ComplexType mineralDepositPropertyType = (ComplexType) reader.getTypeRegistry().get(
                MINERAL_DEPOSIT_PROPERTY_TYPE);
        // for simple string properties
        Name name = new NameImpl(null, "simpleContent");
        AttributeType simpleContentType = (AttributeType) reader.getTypeRegistry().get(
                Types.typeName("http://www.w3.org/2001/XMLSchema", "string"));
        AttributeDescriptor stringDescriptor = new AttributeDescriptorImpl(simpleContentType, name,
                1, 1, true, (Object) null);
        Iterator<SimpleFeature> simpleFeatures = fCollection.iterator();
        while (simpleFeatures.hasNext()) {
            SimpleFeature next = simpleFeatures.next();
            Collection<Property> properties = new ArrayList<Property>();

            // mo:form
            String propertyName = "FORM";
            properties.add(new AttributeImpl(next.getProperty(propertyName).getValue(),
                    (AttributeDescriptor) earthResourceType.getDescriptor(Types
                            .typeName(MO, "form")), null));

            // mo:classification
            propertyName = "CLASSIFICATION";
            ComplexAttributeImpl classification = new ComplexAttributeImpl(
                    new ArrayList<Property>(), (AttributeDescriptor) earthResourceType
                            .getDescriptor(Types.typeName(MO, "classification")), null);

            // mo:classification/mo:MineralDepositModel/mo:mineralDepositGroup
            Name leafAttribute = Types.typeName(MO, "mineralDepositGroup");
            AttributeImpl mineralDepositGroup = new AttributeImpl(next.getProperty(propertyName)
                    .getValue(), (AttributeDescriptor) mineralDepositType
                    .getDescriptor(leafAttribute), null);
            ArrayList<Property> value = new ArrayList<Property>();
            value.add(mineralDepositGroup);

            // mo:classification/mo:MineralDepositModel
            ComplexAttributeImpl mineralDepositModel = new ComplexAttributeImpl(value,
                    (AttributeDescriptor) mineralDepositPropertyType.getDescriptor(Types.typeName(
                            MO, "MineralDepositModel")), null);
            value = new ArrayList<Property>();
            value.add(mineralDepositModel);
            classification.setValue(value);
            properties.add(classification);

            // mo:composition
            propertyName = "COMPOSITION";
            String[] cpIds = next.getProperty(propertyName).getValue().toString().split(",");
            for (String cpId : cpIds) {
                Collection<Property> cpProperties = new ArrayList<Property>(cpIds.length);
                cpProperties.add(new AttributeImpl(cpId, stringDescriptor, null));
                properties.add(new AttributeImpl(cpProperties,
                        (AttributeDescriptor) earthResourceType.getDescriptor(Types.typeName(MO,
                                "composition")), null));
            }

            // mo:commodityDescription
            propertyName = "COMMODITYDESCRIPTION";

            String[] mfIds = next.getProperty(propertyName).getValue().toString().split(",");
            for (String mfId : mfIds) {
                ArrayList<Property> mfProperties = new ArrayList<Property>();
                mfProperties.add(new AttributeImpl(mfId, stringDescriptor, null));
                properties.add(new AttributeImpl(mfProperties,
                        (AttributeDescriptor) earthResourceType.getDescriptor(Types.typeName(MO,
                                "commodityDescription")), null));
            }

            features.add(new FeatureImpl(properties, featureDesc, next.getIdentifier()));
        }
        fCollection.close(simpleFeatures);

        return features;
    }

    /**
     * Dispose all the data accesses so that there is no mapping conflicts for other tests
     */
    protected void tearDown() {
        minOccDataAccess.dispose();
        mappedGUDataAccess.dispose();
        mfDataAccess.dispose();
        cpDataAccess.dispose();
        cgiDataAccess.dispose();
    }

    private void loadDataAccesses() throws IOException {
        /**
         * Load mapped feature as occurrence data access
         */
        Map<String, Serializable> dsParams = new HashMap();
        URL url = getClass().getResource(schemaBase + "MappedFeatureAsOccurrence.xml");
        assertNotNull(url);

        dsParams.put("dbtype", "app-schema");
        dsParams.put("url", url.toExternalForm());

        mfDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(mfDataAccess);

        FeatureType mappedFeatureType = mfDataAccess.getSchema(MAPPED_FEATURE);
        assertNotNull(mappedFeatureType);
        FeatureSource<FeatureType, Feature> mfSource = mfDataAccess
                .getFeatureSource(MAPPED_FEATURE);
        FeatureCollection<FeatureType, Feature> mfCollection = mfSource.getFeatures();
        Iterator<Feature> mfIterator = mfCollection.iterator();
        mfFeatures = new ArrayList<Feature>();
        while (mfIterator.hasNext()) {
            mfFeatures.add(mfIterator.next());
        }
        mfCollection.close(mfIterator);

        /**
         * Load geologic unit data access mapped from the input MO data access
         */

        url = getClass().getResource(schemaBase + "EarthResourceToGeologicUnit.xml");
        assertNotNull(url);

        dsParams.put("url", url.toExternalForm());

        mappedGUDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(mappedGUDataAccess);

        guFeatureSource = mappedGUDataAccess.getFeatureSource(GEOLOGIC_UNIT);

        assertNotNull(guFeatureSource);

        /**
         * Load composition part data access
         */
        url = getClass().getResource(schemaBase + "CompositionPart.xml");
        assertNotNull(url);

        dsParams.put("dbtype", "app-schema");
        dsParams.put("url", url.toExternalForm());
        cpDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(cpDataAccess);
        FeatureSource<FeatureType, Feature> cpSource = cpDataAccess
                .getFeatureSource(COMPOSITION_PART);
        FeatureCollection<FeatureType, Feature> cpCollection = cpSource.getFeatures();
        Iterator<Feature> cpIterator = cpCollection.iterator();

        /**
         * Load CGI Term Value data access
         */
        url = getClass().getResource(schemaBase + "CGITermValue.xml");
        assertNotNull(url);

        dsParams.put("url", url.toExternalForm());
        cgiDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(cgiDataAccess);

        /**
         * Load Controlled Concept data access
         */
        url = getClass().getResource(schemaBase + "ControlledConcept.xml");
        assertNotNull(url);

        dsParams.put("url", url.toExternalForm());
        DataAccess<FeatureType, Feature> ccDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(ccDataAccess);

        cpFeatures = new ArrayList<Feature>();
        while (cpIterator.hasNext()) {
            cpFeatures.add(cpIterator.next());
        }
        cpCollection.close(cpIterator);

        ccDataAccess.dispose();
    }

    /**
     * Test that the app-schema data access with MO data access input loads successfully.
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    public void testLoadDataAccess() throws IOException, URISyntaxException {
        // get the re-mapped geologic unit features
        FeatureCollection<FeatureType, Feature> guFeatures = guFeatureSource.getFeatures();
        Iterator<Feature> guIterator = guFeatures.iterator();
        ArrayList<String> guIds = new ArrayList<String>();
        while (guIterator.hasNext()) {
            guIds.add(guIterator.next().getIdentifier().toString());
        }
        guFeatures.close(guIterator);

        // get the simple earth resource features
        File dir = new File(getClass().getResource(schemaBase).toURI());
        PropertyDataStore dataStore = new PropertyDataStore(dir);
        FeatureSource<SimpleFeatureType, SimpleFeature> simpleFeatureSource = dataStore
                .getFeatureSource(EARTH_RESOURCE);
        FeatureCollection<SimpleFeatureType, SimpleFeature> moFeatures = simpleFeatureSource
                .getFeatures();
        Iterator<SimpleFeature> moIterator = moFeatures.iterator();
        ArrayList<String> moIds = new ArrayList<String>();
        while (moIterator.hasNext()) {
            moIds.add(moIterator.next().getIdentifier().toString());
        }
        moFeatures.close(moIterator);

        // compare the feature ids and make sure that the features are all there
        assertEquals(guIds.size(), moIds.size());
        assertEquals(guIds.containsAll(moIds), true);
    }

    /**
     * Test that the re-mapping from MO:EarthResource to GSML:GeologicUnit is successful. This also
     * tests feature chaining for the mapped GU features.
     * 
     * @throws IOException
     */
    public void testMappings() throws IOException {
        FeatureCollection<FeatureType, Feature> guCollection = (FeatureCollection<FeatureType, Feature>) guFeatureSource
                .getFeatures();
        // mo:EarthResource -> gsml:GeologicUnit output iterator
        MappingFeatureIterator iterator = (MappingFeatureIterator) guCollection.iterator();
        FeatureTypeMapping guSchema = AppSchemaDataAccessRegistry.getMapping(GEOLOGIC_UNIT);
        Hints hints = new Hints(FeaturePropertyAccessorFactory.NAMESPACE_CONTEXT, guSchema
                .getNamespaces());
        // find attribute mappings for chained features
        final String composition = "composition";
        final String occurrence = "occurence";
        final String commodity = "commodityDescription";
        List<AttributeMapping> nonFeatureMappings = new ArrayList<AttributeMapping>();
        AttributeMapping compositionMapping = null;
        AttributeMapping occurrenceMapping = null;
        for (AttributeMapping attMapping : guSchema.getAttributeMappings()) {
            String attName = attMapping.getTargetXPath().toString();
            if (attName.equals("gsml:" + composition)) {
                compositionMapping = attMapping;
            } else if (attName.equals("gsml:" + occurrence)) {
                occurrenceMapping = attMapping;
            } else {
                // normal inline attribute mappings (not chained)
                nonFeatureMappings.add(attMapping);
            }
        }
        // make sure all the mappings are there
        assertNotNull(occurrenceMapping);
        assertNotNull(compositionMapping);
        assertEquals(nonFeatureMappings.size(), guSchema.getAttributeMappings().size() - 2);

        int guCount = 0;
        ArrayList<Feature> guFeatures = new ArrayList<Feature>();
        while (iterator.hasNext()) {
            Feature next = (Feature) iterator.next();
            FeatureId fId = next.getIdentifier();
            Feature moFeature = null;
            // find matching input MO feature to compare the values with
            for (Feature inputFeature : minOccFeatures) {
                if (iterator.featureFidMapping.evaluate(inputFeature).equals(fId)) {
                    moFeature = inputFeature;
                }
            }
            assertNotNull(moFeature);

            /**
             * Check Feature Chaining : Composition Part as composition
             */
            Collection<Property> gsmlCompositions = (Collection<Property>) next
                    .getProperties(composition);
            Collection<Property> moCompositions = (Collection<Property>) moFeature
                    .getProperties(composition);
            Collection<String> cpIds = new ArrayList<String>();
            for (Property inputProperty : moCompositions) {
                Collection<Attribute> values = (Collection<Attribute>) inputProperty.getValue();
                for (Attribute attrib : values) {
                    cpIds.add(attrib.getValue().toString());
                }
            }
            assertEquals(cpIds.size() > 0, true);
            assertEquals(gsmlCompositions.size(), cpIds.size());
            ArrayList<String> nestedCpIds = new ArrayList<String>(cpIds.size());
            for (Property outputProperty : gsmlCompositions) {
                Collection<Feature> values = (Collection<Feature>) outputProperty.getValue();
                Feature compositionPart = values.iterator().next();
                // check the values
                assertEquals(cpFeatures.contains(compositionPart), true);
                nestedCpIds.add(compositionPart.getIdentifier().toString());
            }

            // check the feature has the correct id
            assertEquals(cpIds.containsAll(nestedCpIds), true);

            /**
             * Check Feature Chaining : Mapped Feature as occurrence
             */
            Collection<Property> occurrences = (Collection<Property>) next
                    .getProperties(occurrence);
            Collection<Property> commodities = (Collection<Property>) moFeature
                    .getProperties(commodity);
            Collection<String> mfIds = new ArrayList<String>();
            for (Property property : commodities) {
                Collection<Attribute> values = (Collection<Attribute>) property.getValue();
                for (Attribute attrib : values) {
                    mfIds.add(attrib.getValue().toString());
                }
            }
            assertEquals(mfIds.size() > 0, true);
            assertEquals(occurrences.size(), mfIds.size());
            ArrayList<String> nestedMfIds = new ArrayList<String>(mfIds.size());
            for (Property mf : occurrences) {
                Collection<Feature> values = (Collection<Feature>) mf.getValue();
                Feature mfFeature = values.iterator().next();
                // check the values
                assertEquals(mfFeatures.contains(mfFeature), true);
                nestedMfIds.add(mfFeature.getIdentifier().toString());
            }

            // check the feature has the correct id
            assertEquals(mfIds.containsAll(nestedMfIds), true);

            /**
             * Check normal in-line attribute mappings
             */
            for (AttributeMapping attMapping : nonFeatureMappings) {
                Expression sourceExpr = attMapping.getSourceExpression();
                // make sure the mapping has the right values
                if (!(sourceExpr instanceof AttributeExpressionImpl)) {
                    // ignore attributes that aren't mapped from the input features, such as id
                    continue;
                }
                AttributeExpressionImpl outputExpr = new AttributeExpressionImpl(attMapping
                        .getTargetXPath().toString(), hints);
                Object inputValue = sourceExpr.evaluate(moFeature);
                while (inputValue instanceof Attribute) {
                    inputValue = ((Attribute) inputValue).getValue();
                }
                Object outputValue = outputExpr.evaluate(next);
                while (outputValue instanceof Attribute) {
                    outputValue = ((Attribute) outputValue).getValue();
                }
                assertEquals(inputValue, outputValue);
            }
            guFeatures.add(next);
            guCount++;
        }
        // make sure number of re-mapped features is consistent with input complex features
        assertEquals(guCount, minOccFeatures.size());

        /**
         * Feature chaining : Make sure the features can be chained as well. The re-mapped Geologic
         * Unit features are chained inside Mapped Features as specification.
         */
        mfDataAccess.dispose();
        // recreate mapped features from another mapping file to avoid circular reference
        Map<String, Serializable> dsParams = new HashMap<String, Serializable>();
        URL url = getClass().getResource(schemaBase + "MappedFeaturePropertyfile.xml");
        assertNotNull(url);
        dsParams.put("dbtype", "app-schema");
        dsParams.put("url", url.toExternalForm());
        mfDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(mfDataAccess);
        FeatureType mappedFeatureType = mfDataAccess.getSchema(MAPPED_FEATURE);
        assertNotNull(mappedFeatureType);
        FeatureSource<FeatureType, Feature> mfSource = mfDataAccess
                .getFeatureSource(MAPPED_FEATURE);
        FeatureCollection<FeatureType, Feature> mfCollection = mfSource.getFeatures();
        Iterator<Feature> mfIterator = mfCollection.iterator();

        while (mfIterator.hasNext()) {
            Feature mf = mfIterator.next();
            Property spec = mf.getProperty("specification");
            assertNotNull(spec);
            Object guObject = spec.getValue();
            assertNotNull(guObject);
            assertEquals(guObject instanceof Collection, true);
            assertEquals(((Collection<Feature>) guObject).size(), 1);
            guObject = ((Collection<Feature>) guObject).iterator().next();
            assertEquals(guObject instanceof Feature, true);
            Feature guFeature = (Feature) guObject;
            // make sure this is the re-mapped geologic unit feature
            assertEquals(guFeatures.contains(guFeature), true);
            String propertyGuId = FeatureChainingTest.mfToGuMap.get(mf.getIdentifier().toString())
                    .split("gu.")[1];
            assertEquals(((Feature) guObject).getIdentifier().toString(), propertyGuId);
        }
        mfCollection.close(mfIterator);

        mfDataAccess.dispose();
    }

    /**
     * Test filters on the re-mapped geologic unit features, as well as the features that chain
     * them.
     * 
     * @throws IOException
     */
    public void testFilters() throws IOException {
        // Filtering on re-mapped geologic unit features
        // Composition is a multi-valued property chained inside geologic unit.
        // We're testing that we can get a geologic unit which has a composition part with a
        // significant proportion value
        Expression property = ff
                .property("gsml:composition/gsml:CompositionPart/gsml:proportion/gsml:CGI_TermValue/gsml:value");
        Expression string = ff.literal("significant");
        Expression function = ff.function(FeatureChainingTest.CONTAINS_TEXT, property, string);
        Filter filter = ff.equals(function, ff.literal(1));
        FeatureCollection<FeatureType, Feature> filteredResults = guFeatureSource
                .getFeatures(filter);
        // see CompositionPart.properties:
        // cp.167775491936278812=interbedded component|significant
        // cp.167775491936278844=interbedded component|significant
        // EarthResource.properties:
        // _=FORM:String,COMPOSITION:String
        // 25699=strataform|cp.167775491936278844,cp.167775491936278812,cp.167775491936278856
        // 25682=cross-cutting|cp.167775491936278812
        assertEquals(FeatureChainingTest.getCount(filteredResults), 2);

        // Filtering on mapped feature features that chain the re-mapped geologic unit features
        // First we need to recreate the mapping with a mapping file where gsml:specification exists
        mfDataAccess.dispose();
        Map<String, Serializable> dsParams = new HashMap<String, Serializable>();
        URL url = getClass().getResource(schemaBase + "MappedFeaturePropertyfile.xml");
        assertNotNull(url);
        dsParams.put("dbtype", "app-schema");
        dsParams.put("url", url.toExternalForm());
        mfDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(mfDataAccess);
        FeatureSource<FeatureType, Feature> mfSource = mfDataAccess
                .getFeatureSource(MAPPED_FEATURE);
        property = ff
                .property("gsml:specification/gsml:GeologicUnit/gsml:bodyMorphology/gsml:CGI_TermValue/gsml:value");
        string = ff.literal("vein");
        function = ff.function(FeatureChainingTest.CONTAINS_TEXT, property, string);
        filter = ff.equals(function, ff.literal(1));
        filteredResults = mfSource.getFeatures(filter);

        // see EarthResource.properties file:
        // _=FORM:String,COMPOSITION:String,CLASSIFICATION:String,COMMODITYDESCRIPTION:String
        // 25678=vein|cp.167775491936278856|urn:cgi:classifierScheme:GSV:GeologicalUnitType|mf2,mf3
        // There are 2 mapped features: mf2 and mf3.
        // You can verify by looking at MappedFeaturePropertiesFile.properties as well
        assertEquals(FeatureChainingTest.getCount(filteredResults), 2);
    }

    /**
     * This is a test data access factory to create non-app-schema MO data access as an input for
     * the tests above.
     * 
     * @author ang05a
     */
    public static class MinOccDataAccessFactory implements DataAccessFactory {
        public MinOccDataAccessFactory() {
        }

        public boolean canProcess(Map<String, Serializable> params) {
            Object url = params.get("url");
            return url == null ? false : url.equals(getClass().getResource(
                    schemaBase + "EarthResourceToGeologicUnit.xml"));
        }

        public DataAccess<? extends FeatureType, ? extends Feature> createDataStore(
                Map<String, Serializable> params) throws IOException {
            return minOccDataAccess;
        }

        public String getDescription() {
            return null;
        }

        public String getDisplayName() {
            return null;
        }

        public Param[] getParametersInfo() {
            return null;
        }

        public boolean isAvailable() {
            return true;
        }

        public Map<Key, ?> getImplementationHints() {
            return null;
        }
    }

    /**
     * This is a test non app-schema MO:data access
     * 
     * @author ang05a
     */
    private class MinOccDataAccess implements DataAccess<FeatureType, Feature> {
        private FeatureSource<FeatureType, Feature> fSource;

        private ArrayList<Name> names = new ArrayList<Name>();

        public MinOccDataAccess(Collection<Feature> features, FeatureType schema) {
            MinOccFeatureCollection fCollection = new MinOccFeatureCollection(schema, features);
            fSource = new MinOccFeatureSource(fCollection, this);
            names.add(fSource.getName());
            DataAccessRegistry.register(this);
        }

        public void createSchema(FeatureType featureType) throws IOException {
            throw new UnsupportedOperationException();
        }

        public void dispose() {
            this.fSource = null;
            this.names.clear();
            DataAccessRegistry.unregister(this);
        }

        public FeatureSource<FeatureType, Feature> getFeatureSource(Name typeName)
                throws IOException {
            return fSource;
        }

        public ServiceInfo getInfo() {
            throw new UnsupportedOperationException();
        }

        public List<Name> getNames() throws IOException {
            return names;
        }

        public FeatureType getSchema(Name name) throws IOException {
            return fSource.getFeatures().getSchema();
        }

        public void updateSchema(Name typeName, FeatureType featureType) throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * This is a test feature source for non-app-schema complex features.
     * 
     * @author ang05a
     */
    private class MinOccFeatureSource implements FeatureSource<FeatureType, Feature> {
        private FeatureCollection<FeatureType, Feature> fCollection;

        private DataAccess<FeatureType, Feature> dataAccess;

        public MinOccFeatureSource(FeatureCollection<FeatureType, Feature> fCollection,
                DataAccess<FeatureType, Feature> dataAccess) {
            this.fCollection = fCollection;
            this.dataAccess = dataAccess;
        }

        public void addFeatureListener(FeatureListener listener) {
            throw new UnsupportedOperationException();
        }

        public ReferencedEnvelope getBounds() throws IOException {
            throw new UnsupportedOperationException();
        }

        public ReferencedEnvelope getBounds(Query query) throws IOException {
            throw new UnsupportedOperationException();
        }

        public int getCount(Query query) throws IOException {
            return fCollection.size();
        }

        public DataAccess<FeatureType, Feature> getDataStore() {
            return dataAccess;
        }

        public FeatureCollection<FeatureType, Feature> getFeatures(Query query) throws IOException {
            return fCollection;
        }

        public FeatureCollection<FeatureType, Feature> getFeatures(Filter filter)
                throws IOException {
            return fCollection.subCollection(filter);
        }

        public FeatureCollection<FeatureType, Feature> getFeatures() throws IOException {
            return fCollection;
        }

        public ResourceInfo getInfo() {
            throw new UnsupportedOperationException();
        }

        public Name getName() {
            return fCollection.getSchema().getName();
        }

        public QueryCapabilities getQueryCapabilities() {
            throw new UnsupportedOperationException();
        }

        public FeatureType getSchema() {
            return fCollection.getSchema();
        }

        public Set<Key> getSupportedHints() {
            throw new UnsupportedOperationException();
        }

        public void removeFeatureListener(FeatureListener listener) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * This is a test feature collection of non-app-schema complex features
     * 
     * @author ang05a
     */
    private class MinOccFeatureCollection implements FeatureCollection<FeatureType, Feature> {
        private ArrayList<Feature> fList = new ArrayList<Feature>();

        private FeatureType schema;

        public MinOccFeatureCollection(FeatureType schema, Collection<Feature> features) {
            this.schema = schema;
            this.addAll(features);
        }

        public void accepts(FeatureVisitor visitor, ProgressListener progress) throws IOException {
            throw new UnsupportedOperationException();
        }

        public boolean add(Feature obj) {
            return fList.add(obj);
        }

        public boolean addAll(Collection<? extends Feature> collection) {
            return fList.addAll(collection);
        }

        public boolean addAll(FeatureCollection<? extends FeatureType, ? extends Feature> resource) {
            throw new UnsupportedOperationException();
        }

        public void addListener(CollectionListener listener) throws NullPointerException {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            fList.clear();
        }

        public void close(FeatureIterator<Feature> close) {
            close.close();
        }

        public void close(Iterator<Feature> close) {
            ((MinOccFeatureIterator) close).close();
        }

        public boolean contains(Object o) {
            return fList.contains(o);
        }

        public boolean containsAll(Collection<?> o) {
            return fList.containsAll(o);
        }

        public FeatureIterator<Feature> features() {
            return new MinOccFeatureIterator(fList);
        }

        public ReferencedEnvelope getBounds() {
            throw new UnsupportedOperationException();
        }

        public String getID() {
            return null;
        }

        public FeatureType getSchema() {
            return schema;
        }

        public boolean isEmpty() {
            return this.fList.isEmpty();
        }

        public Iterator<Feature> iterator() {
            return (Iterator<Feature>) features();
        }

        public void purge() {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o) {
            return this.fList.remove(o);
        }

        public boolean removeAll(Collection<?> c) {
            return this.fList.removeAll(c);
        }

        public void removeListener(CollectionListener listener) throws NullPointerException {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection<?> c) {
            return this.fList.retainAll(c);
        }

        public int size() {
            return this.fList.size();
        }

        public FeatureCollection<FeatureType, Feature> sort(SortBy order) {
            throw new UnsupportedOperationException();
        }

        public FeatureCollection<FeatureType, Feature> subCollection(Filter filter) {
            if (filter == Filter.INCLUDE) {
                return this;
            }
            FeatureCollection<FeatureType, Feature> fCollection = new MinOccFeatureCollection(
                    this.schema, new ArrayList<Feature>());

            for (Feature feature : this.fList) {
                if (filter.evaluate(feature)) {
                    fCollection.add(feature);
                }
            }
            return fCollection;
        }

        public Object[] toArray() {
            return fList.toArray();
        }

        public <O> O[] toArray(O[] a) {
            return fList.toArray(a);
        }
    }

    /**
     * This is a test feature iterator for non-app-schema complex features
     * 
     * @author ang05a
     */
    private class MinOccFeatureIterator implements Iterator<Feature>, FeatureIterator<Feature> {
        Iterator<Feature> iterator;

        public MinOccFeatureIterator(ArrayList<Feature> features) {
            iterator = features.iterator();
        }

        public void close() {
            iterator = null;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Feature next() throws NoSuchElementException {
            return iterator.next();
        }

        public void remove() {
            iterator.remove();
        }
    }
}
