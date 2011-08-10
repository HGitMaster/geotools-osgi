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
import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.complex.config.CatalogUtilities;
import org.geotools.data.complex.config.EmfAppSchemaReader;
import org.geotools.data.complex.config.FeatureTypeRegistry;
import org.geotools.data.property.PropertyDataStore;
import org.geotools.factory.Hints;
import org.geotools.feature.AttributeImpl;
import org.geotools.feature.ComplexAttributeImpl;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureImpl;
import org.geotools.feature.NameImpl;
import org.geotools.feature.Types;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.FeatureTypeImpl;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.expression.FeaturePropertyAccessorFactory;
import org.geotools.gml3.GMLSchema;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.geotools.xml.SchemaIndex;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.identity.FeatureId;

/**
 * This is to test the integration of a data access (which does not necessarily have to be an
 * app-schema data access) that produces complex features of a certain XML form as an input to an
 * app-schema data access of a different XML form. A new app-schema data access would be created to
 * remap the non-app-schema data access into the output XML form. Then the features can chain or be
 * chained as per normal. See FeatureChainingTest.java to see feature chaining in action.
 * 
 * @author Rini Angreani, Curtin University of Technology
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/app-schema/app-schema/src/test/java/org/geotools/data/complex/AppSchemaDataAccessIntegrationTest.java $
 */
public class AppSchemaDataAccessIntegrationTest extends DataAccessIntegrationTest {

    private static final String MO_URI = "urn:cgi:xmlns:GGIC:MineralOccurrence:1.0";

    private static final Name EARTH_RESOURCE = Types.typeName(MO_URI, "EarthResource");

    private static final Name EARTH_RESOURCE_TYPE = Types.typeName(MO_URI, "EarthResourceType");

    private static final Name MINERAL_DEPOSIT_TYPE = Types.typeName(MO_URI,
            "MineralDepositModelType");

    private static final Name MINERAL_DEPOSIT_PROPERTY_TYPE = Types.typeName(MO_URI,
            "MineralDepositModelPropertyType");

    /**
     * Remapped Geologic Unit data access in GSML form
     */
    private static DataAccess<FeatureType, Feature> newGuDataAccess;

    /**
     * Create the input data access containing complex features of MO form.
     */
    @BeforeClass
    public static void setUp() throws Exception {
        Map<String, Serializable> moParams = new HashMap<String, Serializable>();
        moParams.put("dbtype", "mo-data-access");
        moParams.put("directory", AppSchemaDataAccessIntegrationTest.class.getResource(schemaBase));
        // get original non-app-schema data access
        DataAccessFinder.getDataStore(moParams);
        DataAccessIntegrationTest.setFilterFactory();
        // load app-schema data access instances
        loadGeologicUnitDataAccess();
        loadDataAccesses("MappedFeatureAsOccurrence.xml");
    }

    /**
     * Dispose all the data accesses so that there is no mapping conflicts for other tests
     */
    @AfterClass
    public static void tearDown() {
        DataAccessRegistry.unregisterAll();
    }

    private static void loadGeologicUnitDataAccess() throws IOException {
        URL url = AppSchemaDataAccessIntegrationTest.class.getResource(schemaBase
                + "EarthResourceToGeologicUnit.xml");
        assertNotNull(url);

        Map<String, Serializable> dsParams = new HashMap<String, Serializable>();
        dsParams.put("dbtype", "app-schema");
        dsParams.put("url", url.toExternalForm());

        // create new app-schema data access using the existing non-app-schema inputDataAccess
        // and EarthResourceToGeologicUnit mapping file
        newGuDataAccess = DataAccessFinder.getDataStore(dsParams);
        assertNotNull(newGuDataAccess);

        guFeatureSource = newGuDataAccess.getFeatureSource(GEOLOGIC_UNIT);

        assertNotNull(guFeatureSource);
    }

    /**
     * Create complex features of type MO:EarthResource
     * 
     * @param fCollection
     *            Simple features collection
     * @param earthResourceType
     *            Earth Resource schema
     * @return MO:EarthResource features
     * @throws IOException
     */
    private static ArrayList<Feature> getInputFeatures(
            FeatureCollection<SimpleFeatureType, SimpleFeature> fCollection, ComplexType complexType)
            throws IOException {

        ArrayList<Feature> features = new ArrayList<Feature>();

        FeatureType earthResourceType = new FeatureTypeImpl(complexType.getName(), complexType
                .getDescriptors(), null, true, complexType.getRestrictions(),
                GMLSchema.ABSTRACTFEATURETYPE_TYPE, null);

        AttributeDescriptor featureDesc = new AttributeDescriptorImpl(earthResourceType,
                EARTH_RESOURCE, 0, -1, false, null);
        ComplexType mineralDepositType = (ComplexType) typeRegistry
                .getAttributeType(MINERAL_DEPOSIT_TYPE);
        ComplexType mineralDepositPropertyType = (ComplexType) typeRegistry
                .getAttributeType(MINERAL_DEPOSIT_PROPERTY_TYPE);
        // for simple string properties
        Name name = new NameImpl(null, "simpleContent");
        AttributeType simpleContentType = typeRegistry.getAttributeType(Types.typeName(
                "http://www.w3.org/2001/XMLSchema", "string"));
        AttributeDescriptor stringDescriptor = new AttributeDescriptorImpl(simpleContentType, name,
                1, 1, true, (Object) null);
        Iterator<SimpleFeature> simpleFeatures = fCollection.iterator();
        // gml:name descriptor
        AttributeDescriptor nameDescriptor = (AttributeDescriptor) GMLSchema.ABSTRACTGMLTYPE_TYPE
                .getDescriptor(Types.typeName(GMLNS, "name"));

        while (simpleFeatures.hasNext()) {
            SimpleFeature next = simpleFeatures.next();
            Collection<Property> properties = new ArrayList<Property>();
            ArrayList<Property> value = new ArrayList<Property>();
            
            // mo:form
            String propertyName = "FORM";
            value.add(new AttributeImpl(next.getProperty(propertyName).getValue(),
                    stringDescriptor, null));
            properties.add(new ComplexAttributeImpl(value, (AttributeDescriptor) earthResourceType
                    .getDescriptor(Types.typeName(MO_URI, "form")), null));

            // gml:name[1]
            value = new ArrayList<Property>();
            value.add(new AttributeImpl("gu." + next.getID(), stringDescriptor, null));
            ComplexAttributeImpl name1 = new ComplexAttributeImpl(value, nameDescriptor, null);
            properties.add(name1);

            // gml:name[2]
            value = new ArrayList<Property>();
            value.add(new AttributeImpl("er." + next.getID(), stringDescriptor, null));
            ComplexAttributeImpl name2 = new ComplexAttributeImpl(value, nameDescriptor, null);
            properties.add(name2);

            // mo:classification
            propertyName = "CLASSIFICATION";
            ComplexAttributeImpl classification = new ComplexAttributeImpl(
                    new ArrayList<Property>(), (AttributeDescriptor) earthResourceType
                            .getDescriptor(Types.typeName(MO_URI, "classification")), null);

            // mo:classification/mo:MineralDepositModel/mo:mineralDepositGroup
            value = new ArrayList<Property>();
            value.add(new AttributeImpl(next.getProperty(propertyName).getValue(),
                    stringDescriptor, null));

            Name leafAttribute = Types.typeName(MO_URI, "mineralDepositGroup");
            AttributeImpl mineralDepositGroup = new AttributeImpl(value,
                    (AttributeDescriptor) mineralDepositType.getDescriptor(leafAttribute), null);
            value = new ArrayList<Property>();
            value.add(mineralDepositGroup);

            // mo:classification/mo:MineralDepositModel
            ComplexAttributeImpl mineralDepositModel = new ComplexAttributeImpl(value,
                    (AttributeDescriptor) mineralDepositPropertyType.getDescriptor(Types.typeName(
                            MO_URI, "MineralDepositModel")), null);
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
                        (AttributeDescriptor) earthResourceType.getDescriptor(Types.typeName(
                                MO_URI, "composition")), null));
            }

            // mo:commodityDescription
            propertyName = "COMMODITYDESCRIPTION";

            String[] mfIds = next.getProperty(propertyName).getValue().toString().split(",");
            for (String mfId : mfIds) {
                ArrayList<Property> mfProperties = new ArrayList<Property>();
                mfProperties.add(new AttributeImpl(mfId, stringDescriptor, null));
                properties.add(new AttributeImpl(mfProperties,
                        (AttributeDescriptor) earthResourceType.getDescriptor(Types.typeName(
                                MO_URI, "commodityDescription")), null));
            }

            features.add(new FeatureImpl(properties, featureDesc, next.getIdentifier()));
        }
        fCollection.close(simpleFeatures);

        return features;
    }

    /**
     * Test that the app-schema data access with MO data access input loads successfully.
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
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
    @Override
    @Test
    public void testMappings() throws IOException {
        FeatureCollection<FeatureType, Feature> guCollection = (FeatureCollection<FeatureType, Feature>) guFeatureSource
                .getFeatures();
        // mo:EarthResource -> gsml:GeologicUnit output iterator
        AbstractMappingFeatureIterator iterator = (AbstractMappingFeatureIterator) guCollection.iterator();
        FeatureTypeMapping guSchema = AppSchemaDataAccessRegistry.getMapping(GEOLOGIC_UNIT);
        Hints hints = new Hints(FeaturePropertyAccessorFactory.NAMESPACE_CONTEXT, guSchema
                .getNamespaces());
        // find attribute mappings for chained features
        final String composition = "composition";
        final String occurrence = "occurence";
        final String commodity = "commodityDescription";
        List<AttributeMapping> otherMappings = new ArrayList<AttributeMapping>();
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
                otherMappings.add(attMapping);
            }
        }
        // make sure all the mappings are there
        assertNotNull(occurrenceMapping);
        assertNotNull(compositionMapping);
        assertEquals(otherMappings.size(), guSchema.getAttributeMappings().size() - 2);

        int guCount = 0;
        ArrayList<Feature> guFeatures = new ArrayList<Feature>();
        while (iterator.hasNext()) {
            Feature next = (Feature) iterator.next();
            FeatureId fId = next.getIdentifier();
            Feature moFeature = null;
            // find matching input MO feature to compare the values with
            for (Feature inputFeature : inputFeatures) {
                if (iterator.featureFidMapping.evaluate(inputFeature).equals(fId.toString())) {
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

            // check multi-valued properties are all mapped
            // there should be 2 gml:name attributes, although only mapped once
            // <AttributeMapping>
            // <!-- All instances of gml:name should be mapped, how many is not known -->
            // <targetAttribute>gml:name</targetAttribute>
            // <sourceExpression>
            // <inputAttribute>gml:name</inputAttribute>
            // </sourceExpression>
            // <isMultiple>true</isMultiple>
            // </AttributeMapping>
            assertEquals(next.getProperties("name").size(), 2);

            /**
             * Check normal in-line attribute mappings
             */
            for (AttributeMapping attMapping : otherMappings) {
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
        assertEquals(guCount, inputFeatures.size());

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
    @Override
    public void testFilters() throws IOException {
        // Filtering on re-mapped geologic unit features
        // Composition is a multi-valued property chained inside geologic unit.
        // We're testing that we can get a geologic unit which has a composition part with a
        // significant proportion value
        Expression property = ff
                .property("gsml:composition/gsml:CompositionPart/gsml:proportion/gsml:CGI_TermValue/gsml:value");
        Filter filter = ff.like(property, "significant");
        FeatureCollection<FeatureType, Feature> filteredResults = guFeatureSource
                .getFeatures(filter);
        // see CompositionPart.properties:
        // cp.167775491936278812=interbedded component|significant
        // cp.167775491936278844=interbedded component|significant
        // EarthResource.properties:
        // _=FORM:String,COMPOSITION:String
        // 25699=strataform|cp.167775491936278844,cp.167775491936278812,cp.167775491936278856
        // 25682=cross-cutting|cp.167775491936278812
        assertEquals(filteredResults.size(), 2);

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
        filter = ff.like(property, "vein");
        filteredResults = mfSource.getFeatures(filter);

        // see EarthResource.properties file:
        // _=FORM:String,COMPOSITION:String,CLASSIFICATION:String,COMMODITYDESCRIPTION:String
        // 25678=vein|cp.167775491936278856|urn:cgi:classifierScheme:GSV:GeologicalUnitType|mf2,mf3
        // There are 2 mapped features: mf2 and mf3.
        // You can verify by looking at MappedFeaturePropertiesFile.properties as well
        assertEquals(filteredResults.size(), 2);
    }

    /**
     * Non app-schema data access factory producing min-occ XML output.
     */
    public static class MinOccDataAccessFactory extends InputDataAccessFactory {
        public DataAccess<? extends FeatureType, ? extends Feature> createDataStore(
                Map<String, Serializable> params) throws IOException {
            String schemaLocation = "/commonSchemas_new/mineralOccurrence/mineralOccurrence.xsd";
            String typeName = EARTH_RESOURCE.getLocalPart();
            URL schemaDir = (URL) params.get("directory");
            File fileDir;
            try {
                fileDir = new File(schemaDir.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            PropertyDataStore dataStore = new PropertyDataStore(fileDir);

            FeatureSource<SimpleFeatureType, SimpleFeature> simpleFeatureSource = dataStore
                    .getFeatureSource(typeName);

            // get the simple features from EarthResource.properties file
            FeatureCollection<SimpleFeatureType, SimpleFeature> fCollection = simpleFeatureSource
                    .getFeatures();
            reader = EmfAppSchemaReader.newInstance();
            typeRegistry = new FeatureTypeRegistry();
            // set catalog
            URL catalogLocation = getClass().getResource(schemaBase + "mappedPolygons.oasis.xml");
            reader.setCatalog(CatalogUtilities.buildPrivateCatalog(catalogLocation));   
            
            SchemaIndex schemaIndex = reader.parse(new URL(schemaDir.toString() + schemaLocation),
                    null);
            typeRegistry.addSchemas(schemaIndex);

            ComplexType complexType = (ComplexType) typeRegistry
                    .getAttributeType(EARTH_RESOURCE_TYPE);

            inputFeatures = getInputFeatures(fCollection, complexType);

            return new InputDataAccess(inputFeatures, simpleFeatureSource.getSchema());
        }

        public boolean canProcess(Map<String, Serializable> params) {
            Object dbType = params.get("dbtype");
            return dbType == null ? false : dbType.equals("mo-data-access");
        }
    }
}
