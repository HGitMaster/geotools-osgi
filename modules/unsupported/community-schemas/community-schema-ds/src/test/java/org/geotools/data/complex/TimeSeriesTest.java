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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.tools.ResolvingXMLReader;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.complex.config.ComplexDataStoreConfigurator;
import org.geotools.data.complex.config.ComplexDataStoreDTO;
import org.geotools.data.complex.config.EmfAppSchemaReader;
import org.geotools.data.complex.config.XMLConfigDigester;
import org.geotools.data.feature.FeatureAccess;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.feature.iso.Types;
import org.geotools.feature.iso.UserData;
import org.geotools.filter.FilterFactoryImplNamespaceAware;
import org.geotools.gml3.bindings.GML;
import org.geotools.xlink.bindings.XLINK;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * DOCUMENT ME!
 * 
 * @author Rob Atkinson
 * @version $Id: TimeSeriesTest.java 31374 2008-09-03 07:26:50Z bencd $
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/2.4.x/modules/unsupported/community-schemas/community-schema-ds/src/test/java/org/geotools/data/complex/TimeSeriesTest.java $
 * @since 2.4
 */
public class TimeSeriesTest extends TestCase {
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(TimeSeriesTest.class.getPackage().getName());

    private static final String AWNS = "http://www.water.gov.au/awdip";

    private static final String CVNS = "http://www.opengis.net/cv/0.2.1";

    private static final String SANS = "http://www.opengis.net/sampling/1.0";

    private static final String OMNS = "http://www.opengis.net/om/1.0";

    private static final String SWENS = "http://www.opengis.net/swe/1.0.1";

    private static final String GMLNS = "http://www.opengis.net/gml";

    // private static final String GEONS =
    // "http://www.seegrid.csiro.au/xml/geometry";

    final String schemaBase = "/test-data/";

    EmfAppSchemaReader reader;

    private FeatureSource2 source;

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception
     *             DOCUMENT ME!
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
     *             DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param location
     *            schema location path discoverable through
     *            getClass().getResource()
     * 
     * @throws IOException
     *             DOCUMENT ME!
     */
    private void loadSchema(URL location) throws IOException {
        URL catalogLocation = getClass().getResource(schemaBase + "observations.oasis.xml");
        Catalog catalog = new ResolvingXMLReader().getCatalog();
        catalog.getCatalogManager().setVerbosity(9);
        catalog.parseCatalog(catalogLocation);

        reader.setCatalog(catalog);

        reader.parse(location);
    }

    /**
     * Tests if the schema-to-FM parsing code developed for complex datastore
     * configuration loading can parse the GeoSciML types
     * 
     * @throws Exception
     */
    public void testParseSchema() throws Exception {
        try {
            String schemaLocation = schemaBase + "commonSchemas_new/awdip.xsd";
            URL location = getClass().getResource(schemaLocation);
            assertNotNull(location);
            loadSchema(location);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        final Map typeRegistry = reader.getTypeRegistry();

        final Name typeName = Types.typeName(AWNS, "SiteSinglePhenomTimeSeriesType");
        final ComplexType testType = (ComplexType) typeRegistry.get(typeName);

        List names = new ArrayList(typeRegistry.keySet());
        Collections.sort(names, new Comparator() {
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        assertNotNull(testType);
        assertTrue(testType instanceof FeatureType);

        AttributeType superType = testType.getSuper();
        assertNotNull(superType);

        Name superTypeName = Types.typeName(AWNS, "SamplingSitePurposeType");
        assertEquals(superTypeName, superType.getName());
        // assertTrue(superType instanceof FeatureType);

        // ensure all needed types were parsed and aren't just empty proxies
        Map samplingProperties = new HashMap();

        // from gml:AbstractFeatureType
        samplingProperties.put(name(GMLNS, "metaDataProperty"), typeName(GMLNS,
                "MetaDataPropertyType"));
        samplingProperties.put(name(GMLNS, "description"), typeName(GMLNS, "StringOrRefType"));
        samplingProperties.put(name(GMLNS, "name"), typeName(GMLNS, "CodeType"));
        samplingProperties.put(name(GMLNS, "boundedBy"), typeName(GMLNS, "BoundingShapeType"));
        samplingProperties.put(name(GMLNS, "location"), typeName(GMLNS, "LocationPropertyType"));

        // aw:SamplingSiteType
        samplingProperties.put(name(AWNS, "samplingRegimeType"), Types.toTypeName(GML.CodeType));
        samplingProperties.put(name(AWNS, "waterBodyType"), Types.toTypeName(GML.CodeType));
        samplingProperties.put(name(AWNS, "accessTypeCode"), Types.toTypeName(GML.CodeType));

        // sa:SamplingPointType
        samplingProperties.put(name(SANS, "position"), typeName(GMLNS, "PointPropertyType"));

        // sa:SamplingFeatureType
        samplingProperties.put(name(SANS, "relatedObservation"), typeName(OMNS,
                "ObservationPropertyType"));
        samplingProperties.put(name(SANS, "relatedSamplingFeature"), typeName(SANS,
                "SamplingFeatureRelationPropertyType"));
        samplingProperties
                .put(name(SANS, "sampledFeature"), typeName(GMLNS, "FeaturePropertyType"));
        samplingProperties.put(name(SANS, "surveyDetails"), typeName(SANS,
                "SurveyProcedurePropertyType"));

        // sa:SiteSinglePhenomTimeSeriesType
        samplingProperties.put(name(AWNS, "relatedObservation"), typeName(AWNS,
                "PhenomenonTimeSeriesPropertyType"));

        assertPropertyNamesAndTypeNames(testType, samplingProperties);

        AttributeDescriptor relatedObservation = (AttributeDescriptor) Types.descriptor(testType,
                name(AWNS, "relatedObservation"));
        Map relatedObsProps = new HashMap();
        relatedObsProps.put(name(AWNS, "PhenomenonTimeSeries"), typeName(AWNS,
                "PhenomenonTimeSeriesType"));
        ComplexType phenomenonTimeSeriesPropertyType = (ComplexType) relatedObservation.type();

        assertPropertyNamesAndTypeNames(phenomenonTimeSeriesPropertyType, relatedObsProps);

        AttributeDescriptor phenomenonTimeSeries = (AttributeDescriptor) Types.descriptor(
                phenomenonTimeSeriesPropertyType, name(AWNS, "PhenomenonTimeSeries"));
        ComplexType phenomenonTimeSeriesType = (ComplexType) phenomenonTimeSeries.type();
        Map phenomenonTimeSeriesProps = new HashMap();
        // from
        // aw:WaterObservationType/om:TimeSeriesObsType/om:AbstractObservationType
        // phenomenonTimeSeriesProps.put(name(OMNS, "procedure"), typeName(OMNS,
        // "ObservationProcedurePropertyType"));
        // phenomenonTimeSeriesProps.put(name(OMNS, "countParameter"),
        // typeName(SWENS,
        // "TypedCountType"));
        // phenomenonTimeSeriesProps.put(name(OMNS, "measureParameter"),
        // typeName(SWENS,
        // "TypedMeasureType"));
        // phenomenonTimeSeriesProps.put(name(OMNS, "termParameter"),
        // typeName(SWENS,
        // "TypedCategoryType"));
        // phenomenonTimeSeriesProps.put(name(OMNS, "observedProperty"),
        // typeName(SWENS,
        // "PhenomenonPropertyType"));
        //
        // from PhenomenonTimeSeriesType
        phenomenonTimeSeriesProps.put(name(AWNS, "result"), typeName(CVNS,
                "CompactDiscreteTimeCoveragePropertyType"));

        assertPropertyNamesAndTypeNames(phenomenonTimeSeriesType, phenomenonTimeSeriesProps);

        AttributeDescriptor observedProperty = (AttributeDescriptor) Types.descriptor(
                phenomenonTimeSeriesType, name(OMNS, "observedProperty"));

        ComplexType phenomenonPropertyType = (ComplexType) observedProperty.type();

        assertPropertyNamesAndTypeNames(phenomenonPropertyType, Collections.singletonMap(name(
                SWENS, "Phenomenon"), typeName(SWENS, "PhenomenonType")));

        AttributeDescriptor phenomenon = (AttributeDescriptor) Types.descriptor(
                phenomenonPropertyType, name(SWENS, "Phenomenon"));
        ComplexType phenomenonType = (ComplexType) phenomenon.type();
        assertNotNull(phenomenonType.getSuper());
        assertEquals(typeName(GMLNS, "DefinitionType"), phenomenonType.getSuper().getName());

        Map phenomenonProps = new HashMap();
        // from gml:DefinitionType
        phenomenonProps.put(name(GMLNS, "metaDataProperty"), null);
        phenomenonProps.put(name(GMLNS, "description"), null);
        phenomenonProps.put(name(GMLNS, "name"), null);

        assertPropertyNamesAndTypeNames(phenomenonType, phenomenonProps);
    }

    private void assertPropertyNamesAndTypeNames(ComplexType parentType,
            Map expectedPropertiesAndTypes) throws Exception {

        for (Iterator it = expectedPropertiesAndTypes.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Entry) it.next();
            Name dName = (Name) entry.getKey();
            Name expectedDescriptorTypeName = (Name) entry.getValue();

            AttributeDescriptor d = (AttributeDescriptor) Types.descriptor(parentType, dName);
            assertNotNull("Descriptor " + dName + " not found for type " + parentType.getName(), d);
            AttributeType type;
            try {
                type = (AttributeType) d.type();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "type not parsed for "
                        + ((AttributeDescriptor) d).getName(), e);
                throw e;
            }
            assertNotNull(type);
            Name actualTypeName = type.getName();
            assertNotNull(actualTypeName);
            assertNotNull(type.getBinding());
            if (expectedDescriptorTypeName != null) {
                assertEquals("type mismatch for property " + dName, expectedDescriptorTypeName,
                        actualTypeName);
            }
        }
    }

    private Name typeName(String ns, String localName) {
        return Types.typeName(ns, localName);
    }

    private Name name(String ns, String localName) {
        return Types.typeName(ns, localName);
    }

    public void testLoadMappingsConfig() throws Exception {
        XMLConfigDigester reader = new XMLConfigDigester();
        String configLocation = schemaBase + "TimeSeriesTest_properties.xml";
        URL url = getClass().getResource(configLocation);

        // configLocation =
        // "file:/home/gabriel/svn/geoserver/trunk/configuration/community-schema-timeseries2/TimeSeriesTest_properties.xml";
        // URL url = new URL(configLocation);

        ComplexDataStoreDTO config = reader.parse(url);

        Set mappings = ComplexDataStoreConfigurator.buildMappings(config);

        assertNotNull(mappings);
        assertEquals(1, mappings.size());

        FeatureTypeMapping mapping = (FeatureTypeMapping) mappings.iterator().next();

        AttributeDescriptor targetFeature = mapping.getTargetFeature();
        assertNotNull(targetFeature);
        assertNotNull(targetFeature.type());
        assertEquals(AWNS, targetFeature.getName().getNamespaceURI());
        assertEquals("SiteSinglePhenomTimeSeries", targetFeature.getName().getLocalPart());

        List attributeMappings = mapping.getAttributeMappings();
        AttributeMapping attMapping = (AttributeMapping) attributeMappings.get(0);
        assertNotNull(attMapping);
        assertEquals("aw:SiteSinglePhenomTimeSeries", attMapping.getTargetXPath().toString());

        attMapping = (AttributeMapping) attributeMappings.get(1);
        assertNotNull(attMapping);
        // note the mapping says SiteSinglePhenomTimeSeries/gml:name[1] but
        // attMapping.getTargetXPath().toString() results in a simplyfied form
        assertEquals("gml:name", attMapping.getTargetXPath().toString());

        attMapping = (AttributeMapping) attributeMappings.get(2);
        assertNotNull(attMapping);
        assertEquals("sa:sampledFeature", attMapping.getTargetXPath().toString());
        // this mapping has no source expression, just client properties
        assertSame(Expression.NIL, attMapping.getSourceExpression());
        assertSame(Expression.NIL, attMapping.getIdentifierExpression());
        Map clientProperties = attMapping.getClientProperties();
        assertEquals(2, clientProperties.size());

        Name clientPropName = name(XLINK.NAMESPACE, "title");
        assertTrue("client property " + clientPropName + " not found", clientProperties
                .containsKey(clientPropName));
        clientPropName = name(XLINK.NAMESPACE, "href");
        assertTrue("client property " + clientPropName + " not found", clientProperties
                .containsKey(clientPropName));

        // now test the use of specific subtype overriding a general node type
        attMapping = (AttributeMapping) attributeMappings.get(3);
        assertNotNull(attMapping);
        String expected = "aw:relatedObservation/aw:PhenomenonTimeSeries/om:observedProperty/swe:Phenomenon/gml:name";
        String actual = attMapping.getTargetXPath().toString();
        assertEquals(expected, actual);
    }

    public void testDataStore() throws Exception {
        FeatureAccess mappingDataStore;
        final Name typeName = new org.geotools.feature.Name(AWNS, "SiteSinglePhenomTimeSeries");
        {
            final Map dsParams = new HashMap();

            String configLocation = schemaBase + "TimeSeriesTest_properties.xml";
            final URL url = getClass().getResource(configLocation);
            // configLocation =
            // "file:/home/gabriel/svn/geoserver/trunk/configuration/community-schema-timeseries2/TimeSeriesTest_properties.xml";
            // URL url = new URL(configLocation);

            dsParams.put("dbtype", "complex");
            dsParams.put("url", url.toExternalForm());

            mappingDataStore = (FeatureAccess) DataAccessFinder.createAccess(dsParams);
        }
        assertNotNull(mappingDataStore);
        FeatureSource2 fSource;
        {
            AttributeDescriptor attDesc = (AttributeDescriptor) mappingDataStore.describe(typeName);
            assertNotNull(attDesc);
            assertTrue(attDesc.type() instanceof FeatureType);

            FeatureType fType = (FeatureType) attDesc.type();

            fSource = (FeatureSource2) mappingDataStore.access(typeName);
        }
        FeatureCollection features;
        // make a getFeatures request with a nested properties filter.
        // note that the expected result count is 6 - 3 sites x 2 phenomena
        final int EXPECTED_RESULT_COUNT = 6;
        {
            features = (FeatureCollection) fSource.content();

            int resultCount = getCount(features);
            String msg = "be sure difference in result count is not due to different dataset.";
            assertEquals(msg, EXPECTED_RESULT_COUNT, resultCount);
        }

        Feature feature;
        int count = 0;

        FilterFactory ffac;
        {
            NamespaceSupport namespaces = new NamespaceSupport();

            namespaces.declarePrefix("aw", AWNS);
            namespaces.declarePrefix("om", OMNS);
            namespaces.declarePrefix("swe", SWENS);
            namespaces.declarePrefix("gml", GMLNS);
            namespaces.declarePrefix("sa", SANS);
            // TODO: use commonfactoryfinder or the mechanism choosed
            // to pass namespace context to filter factory
            ffac = new FilterFactoryImplNamespaceAware(namespaces);
        }

        final String phenomNamePath = "aw:relatedObservation/aw:PhenomenonTimeSeries/om:observedProperty/swe:Phenomenon/gml:name";
        Iterator it = features.iterator();
        for (; it.hasNext();) {
            feature = (Feature) it.next();
            count++;
            {
                PropertyName gmlName = ffac.property("gml:name");
                PropertyName phenomName = ffac.property(phenomNamePath);

                Object nameVal = gmlName.evaluate(feature, String.class);
                assertNotNull("gml:name evaluated to null", nameVal);

                Object phenomNameVal = phenomName.evaluate(feature, String.class);
                assertNotNull(phenomNamePath + " evaluated to null", phenomNameVal);
            }
            {
                PropertyName sampledFeatureName = ffac.property("sa:sampledFeature");
                Attribute sampledFeatureVal = (Attribute) sampledFeatureName.evaluate(feature);
                assertNotNull("sa:sampledFeature evaluated to null", sampledFeatureVal);
                assertNull(sampledFeatureVal.getValue());
                Map attributes = (Map) ((UserData) sampledFeatureVal).getUserData(
                        Attributes.class);
                assertNotNull(attributes);
                Name xlinkTitle = name(XLINK.NAMESPACE, "title");
                assertTrue(attributes.containsKey(xlinkTitle));
                assertNotNull(attributes.get(xlinkTitle));

                Name xlinkHref = name(XLINK.NAMESPACE, "href");
                assertTrue(attributes.containsKey(xlinkHref));
                assertNotNull(attributes.get(xlinkHref));
            }

            {
                final String elementPath = "aw:relatedObservation/aw:PhenomenonTimeSeries/om:result/cv:CompactDiscreteTimeCoverage";
                PropertyName elementName = ffac.property(elementPath);
                Object timeCovVal = elementName.evaluate(feature);
                assertNotNull(elementPath, timeCovVal);
                assertTrue(timeCovVal instanceof Feature);
                final List elements = (List) ((Feature) timeCovVal).getValue();
                if (count == 1)
                    assertEquals(1, elements.size());
                else if (count == 2) {
                    assertEquals(31, elements.size());

                    Name compactTimeValuePairName = Types.typeName(CVNS, "CompactTimeValuePair");
                    Name geometryName = Types.typeName(CVNS, "geometry");
                    Name valueName = Types.typeName(CVNS, "value");

                    ComplexAttribute element = (ComplexAttribute) elements.get(21);
                    assertNotNull(element);

                    List compactTimes = element.get(compactTimeValuePairName);
                    assertNotNull(compactTimes);
                    assertEquals(1, compactTimes.size());

                    ComplexAttribute compatTimeValuePair = (ComplexAttribute) compactTimes.get(0);
                    List geometries = compatTimeValuePair.get(geometryName);
                    List values = compatTimeValuePair.get(valueName);

                    assertNotNull(geometries);
                    assertNotNull(values);
                    assertEquals(1, geometries.size());
                    assertEquals(1, values.size());

                    Attribute geom = (Attribute) geometries.get(0);
                    Attribute value = (Attribute) values.get(0);

                    assertNotNull(geom.getValue());
                    assertNotNull(value.getValue());

                    Object valueContent = geom.getValue();
                    Date sampleTimePosition = (Date) valueContent;// 0=2007-01-01,
                    // 21=2007-01-22
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(sampleTimePosition);
                    assertEquals(2007, cal.get(Calendar.YEAR));
                    assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
                    assertEquals(22, cal.get(Calendar.DAY_OF_MONTH));
                }
            }

        }
        features.close(it);

        assertEquals(EXPECTED_RESULT_COUNT, count);

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
}
