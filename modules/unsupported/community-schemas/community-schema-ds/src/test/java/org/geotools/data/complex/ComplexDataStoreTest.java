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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.geotools.data.Source;
import org.geotools.data.complex.config.ComplexDataStoreConfigurator;
import org.geotools.data.complex.config.ComplexDataStoreDTO;
import org.geotools.data.complex.config.XMLConfigDigester;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.data.feature.memory.MemoryDataAccess;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.iso.Types;
import org.geotools.feature.iso.simple.SimpleFeatureBuilder;
import org.geotools.feature.iso.simple.SimpleFeatureFactoryImpl;
import org.geotools.feature.iso.simple.SimpleTypeBuilder;
import org.geotools.feature.iso.simple.SimpleTypeFactoryImpl;
import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.TypeFactory;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.geometry.BoundingBox;
import org.xml.sax.helpers.NamespaceSupport;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: ComplexDataStoreTest.java 31374 2008-09-03 07:26:50Z bencd $
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/community-schemas/community-schema-ds/src/test/java/org/geotools/data/complex/ComplexDataStoreTest.java $
 * @since 2.4
 */
public class ComplexDataStoreTest extends TestCase {

    private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(ComplexDataStoreTest.class.getPackage()
            .getName());

    Name targetName;

    FeatureType targetType;

    private ComplexDataStore dataStore;

    FeatureTypeMapping mapping;

    protected void setUp() throws Exception {
        super.setUp();
        MemoryDataAccess ds = createWaterSampleTestFeatures();
        targetType = TestData.createComplexWaterSampleType();
        TypeFactory tf = new TypeFactoryImpl();
        AttributeDescriptor targetFeature = tf.createAttributeDescriptor(targetType, targetType
                .getName(), 0, Integer.MAX_VALUE, true, null);
        targetName = targetFeature.getName();
        List mappings = TestData.createMappingsColumnsAndValues(targetFeature);

        Name sourceName = TestData.WATERSAMPLE_TYPENAME;
        FeatureSource2 source = (FeatureSource2) ds.access(sourceName);

        // empty nssupport as the sample types have no namespace defined
        NamespaceSupport namespaces = new NamespaceSupport();
        mapping = new FeatureTypeMapping(source, targetFeature, mappings, namespaces);

        dataStore = new ComplexDataStore(Collections.singleton(mapping));

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for
     * 'org.geotools.data.complex.ComplexDataStore.getTypeNames()'
     */
    public void testGetTypeNames() throws IOException {
        String[] typeNames = dataStore.getTypeNames();
        assertNotNull(typeNames);
        assertEquals(1, typeNames.length);
        assertEquals(targetName.getLocalPart(), typeNames[0]);

        // DataAccess interface:
        List names = dataStore.getNames();
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals(targetName, names.get(0));
    }

    /*
     * Test method for
     * 'org.geotools.data.complex.ComplexDataStore.getSchema(String)'
     */
    public void testDescribeType() throws IOException {
        AttributeDescriptor descriptor = (AttributeDescriptor) dataStore.describe(targetName);
        assertNotNull(descriptor);
        assertEquals(targetType, descriptor.type());
    }

    public void testGetBounds() throws IOException {
        final String namespaceUri = "http://online.socialchange.net.au";
        final String localName = "RoadSegment";
        final Name typeName = Types.typeName(namespaceUri, localName);

        URL configUrl = getClass().getResource("/test-data/roadsegments.xml");

        ComplexDataStoreDTO config = new XMLConfigDigester().parse(configUrl);

        Set/* <FeatureTypeMapping> */mappings = ComplexDataStoreConfigurator.buildMappings(config);

        dataStore = new ComplexDataStore(mappings);
        FeatureSource2 source = (FeatureSource2) dataStore.access(typeName);

        AttributeDescriptor describe = (AttributeDescriptor) source.describe();
        FeatureType mappedType = (FeatureType) describe.type();
        assertNotNull(mappedType.getDefaultGeometry());

        FeatureTypeMapping mapping = (FeatureTypeMapping) mappings.iterator().next();

        FeatureSource2 mappedSource = mapping.getSource();
        ReferencedEnvelope expected = getBounds(mappedSource);
        Envelope actual = getBounds(source);

        assertEquals(expected, actual);

    }

    private ReferencedEnvelope getBounds(FeatureSource2 source) {
        ReferencedEnvelope boundingBox = new ReferencedEnvelope(DefaultGeographicCRS.WGS84);
        FeatureCollection features = (FeatureCollection) source.content();
        Iterator iterator = features.iterator();
        try {
            while (iterator.hasNext()) {
                Feature f = (Feature) iterator.next();
                BoundingBox bounds = f.getBounds();
                boundingBox.include(bounds);
            }
        } finally {
            features.close(iterator);
        }
        
        return boundingBox;
    }

    /*
     * Test method for
     * 'org.geotools.data.complex.ComplexDataStore.getFeatureReader(String)'
     */
    public void testGetFeatureReader() throws IOException {
        Source access = dataStore.access(targetName);
        Object describe = access.describe();
        assertTrue(describe instanceof AttributeDescriptor);
        assertEquals(targetType, ((AttributeDescriptor) describe).type());

        FeatureCollection reader = (FeatureCollection) access.content();
        assertNotNull(reader);

        Iterator features = reader.iterator();
        assertTrue(features.hasNext());

        Feature complexFeature = (Feature) features.next();
        assertNotNull(complexFeature);
        assertEquals(targetType, complexFeature.getType());
        
        reader.close(features);

        org.opengis.filter.FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        PropertyName expr;
        Object value;

        expr = ff.property("measurement[1]");
        value = expr.evaluate(complexFeature);
        assertNotNull(value);

        expr = ff.property("measurement[1]/parameter");
        value = expr.evaluate(complexFeature);
        assertNotNull(value);

        expr = ff.property("measurement[1]/value");
        value = expr.evaluate(complexFeature);
        assertNotNull(value);

        expr = ff.property("measurement[2]/parameter");
        value = expr.evaluate(complexFeature);
        assertNotNull(value);

        expr = ff.property("measurement[2]/value");
        value = expr.evaluate(complexFeature);
        assertNotNull(value);

        expr = ff.property("measurement[3]/parameter");
        value = expr.evaluate(complexFeature);
        assertNotNull(value);

        expr = ff.property("measurement[3]/value");
        value = expr.evaluate(complexFeature);
        assertNotNull(value);

    }

    /*
     * Test method for
     * 'org.geotools.data.AbstractDataStore.getFeatureSource(String)'
     */
    public void testGetFeatureSource() throws IOException {
        Source complexSource = dataStore.access(targetName);
        assertNotNull(complexSource);
        Object describe = complexSource.describe();
        assertTrue(describe instanceof AttributeDescriptor);
        assertEquals(targetType, ((AttributeDescriptor) describe).type());
    }

    /*
     * Test method for
     * 'org.geotools.data.AbstractDataStore.getFeatureReader(Query,
     * Transaction)'
     */
    public void testGetFeatureReaderQuery() throws Exception {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

        PropertyName property = ff.property("sample/measurement[1]/parameter");
        Literal literal = ff.literal("ph");
        Filter filterParameter = ff.equals(property, literal);

        property = ff.property("sample/measurement[1]/value");
        literal = ff.literal(new Integer(3));
        Filter filterValue = ff.equals(property, literal);

        Filter filter = ff.and(filterParameter, filterValue);

        Source complexSource = dataStore.access(targetName);
        FeatureCollection features = (FeatureCollection) complexSource.content(filter);

        Iterator reader = features.iterator();

        PropertyIsEqualTo equivalentSourceFilter = ff.equals(ff.property("ph"), ff
                .literal(new Integer(3)));
        Collection collection = mapping.getSource().content(equivalentSourceFilter);

        int count = 0;
        int expectedCount = collection.size();

        Filter badFilter = ff.greater(ff.property("sample/measurement[1]/value"), ff
                .literal(new Integer(3)));

        while (reader.hasNext()) {
            Feature f = (Feature) reader.next();
            assertNotNull(f);
            assertTrue(filter.evaluate(f));
            assertFalse(badFilter.evaluate(f));
            count++;
        }
        features.close(reader);
        assertEquals(expectedCount, count);
    }

    public void testGroupByFeatureReader() throws Exception {

        LOGGER.info("DATA TEST: testGroupByFeatureReader");

        // dataStore with denormalized wq_ir_results type
        MemoryDataAccess dataStore = TestData.createDenormalizedWaterQualityResults();
        // mapping definitions from simple wq_ir_results type to complex wq_plus
        // type
        FeatureTypeMapping mapper = TestData.createMappingsGroupByStation(dataStore);

        // for(Iterator it = mapper.getSource().content().iterator();
        // it.hasNext();){
        // SimpleFeature f = (SimpleFeature) it.next();
        // for(int i = 0; i < f.getNumberOfAttributes(); i++){
        // Object o = f.get(i);
        // System.out.print(o + ",\t");
        // }
        // System.out.println("");
        // }

        targetName = mapper.getTargetFeature().getName();

        Set/* <FeatureTypeMapping> */mappings = Collections.singleton(mapper);

        ComplexDataStore complexDataStore = new ComplexDataStore(mappings);

        Source complexSource = complexDataStore.access(targetName);
        assertNotNull(complexSource);

        AttributeDescriptor sourceDescriptor;
        sourceDescriptor = (AttributeDescriptor) complexSource.describe();
        targetType = (FeatureType) sourceDescriptor.type();
        assertNotNull(targetType);

        FeatureCollection complexFeatures = (FeatureCollection) complexSource.content();
        assertNotNull(complexFeatures);

        final int EXPECTED_FEATURE_COUNT = 10;// as results from applying the
        // mappings to the simple
        // FeatureSource

        int featureCount = 0;
        Iterator it = complexFeatures.iterator();
        Name measurementName = Types.typeName("measurement");
        while (it.hasNext()) {

            Feature currFeature = (Feature) it.next();
            featureCount++;

            assertNotNull(currFeature);

            // currFeature must have as many "measurement" complex attribute
            // instances as the current iteration number
            // This check relies on MemoryDataStore returning Features in the
            // same order they was inserted

            int expectedMeasurementInstances = featureCount;

            List/* <Attribute> */measurements = currFeature.get(measurementName);

            assertNotNull(measurements);

            try {
                for (Iterator itr = measurements.iterator(); itr.hasNext();) {
                    Attribute attribute = (Attribute) itr.next();
                    String measurementId = attribute.getID();
                    assertNotNull("expected not null id", measurementId);
                }
                assertEquals(expectedMeasurementInstances, measurements.size());
            } catch (AssertionFailedError e) {
                LOGGER.warning(currFeature.toString());
                throw e;
            }

        }
        complexFeatures.close(it);
        assertEquals(EXPECTED_FEATURE_COUNT, featureCount);
    }

    public void testGroupingFeatureIterator() throws Exception {
        // dataStore with denormalized wq_ir_results type
        MemoryDataAccess dataStore = TestData.createDenormalizedWaterQualityResults();
        // mapping definitions from simple wq_ir_results type to complex wq_plus
        // type
        FeatureTypeMapping mapper = TestData.createMappingsGroupByStation(dataStore);

        targetName = mapper.getTargetFeature().getName();

        Set/* <FeatureTypeMapping> */mappings = Collections.singleton(mapper);

        ComplexDataStore complexDataStore = new ComplexDataStore(mappings);

        Source complexSource = complexDataStore.access(targetName);
        assertNotNull(complexSource);

        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        Filter filter = ff.equals(ff.property("anzlic_no"), ff.literal("anzlic_no1"));
        FeatureCollection complexFeatures = (FeatureCollection) complexSource.content(filter);
        assertNotNull(complexFeatures);

        Iterator it = complexFeatures.iterator();

        while (it.hasNext()) {
            assertTrue(it.hasNext());
            Feature currFeature = (Feature) it.next();
            assertNotNull(currFeature);
        }
        complexFeatures.close(it);
    }


    /**
     * Loads config from an xml config file which uses a property datastore as
     * source of features.
     * 
     * @throws IOException
     */
    public void testWithConfig() throws Exception {
        final String nsUri = "http://online.socialchange.net.au";
        final String localName = "RoadSegment";
        final Name typeName = new org.geotools.feature.type.TypeName(nsUri, localName);

        final URL configUrl = getClass().getResource("/test-data/roadsegments.xml");

        ComplexDataStoreDTO config = new XMLConfigDigester().parse(configUrl);

        Set/* <FeatureTypeMapping> */mappings = ComplexDataStoreConfigurator.buildMappings(config);

        dataStore = new ComplexDataStore(mappings);
        Source source = dataStore.access(typeName);

        AttributeDescriptor sdesc = (AttributeDescriptor) source.describe();
        FeatureType type = (FeatureType) sdesc.type();

        AttributeDescriptor node;
        node = (AttributeDescriptor) Types.descriptor(type, Types.typeName(nsUri, "the_geom"));
        assertNotNull(node);
        assertEquals("LineStringPropertyType", node.type().getName().getLocalPart());

        assertNotNull(type.getDefaultGeometry());
        assertEquals(node.type(), type.getDefaultGeometry().type());

        assertNotNull(Types.descriptor(type, Types.typeName(nsUri, "name")));

        Name ftNodeName = Types.typeName(nsUri, "fromToNodes");
        assertNotNull(Types.descriptor(type, ftNodeName));

        AttributeDescriptor descriptor = (AttributeDescriptor) Types.descriptor(type, ftNodeName);

        ComplexType fromToNodes = (ComplexType) descriptor.type();

        assertFalse(descriptor.isNillable());
        assertTrue(fromToNodes.isIdentified());

        Name fromNodeName = Types.typeName(nsUri, "fromNode");
        AttributeDescriptor fromNode = (AttributeDescriptor) Types.descriptor(fromToNodes,
                fromNodeName);
        assertNotNull(fromNode);

        Name toNodeName = Types.typeName(nsUri, "toNode");
        AttributeDescriptor toNode = (AttributeDescriptor) Types
                .descriptor(fromToNodes, toNodeName);
        assertNotNull(fromNode);

        assertEquals(Point.class, ((AttributeType) fromNode.type()).getBinding());
        assertEquals(Point.class, ((AttributeType) toNode.type()).getBinding());

        FeatureCollection content = (FeatureCollection) source.content();
        Iterator features = content.iterator();
        int count = 0;
        final int expectedCount = 5;
        try {
            while (features.hasNext()) {
                Feature f = (Feature) features.next();
                LOGGER.finest(String.valueOf(f));
                ++count;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally{
            content.close(features);
        }
        assertEquals("feature count", expectedCount, count);

        // Test DefaultMappingFeatureIterator MaxFeatures support [GEOS-1930]
        final int expectedCount2 = 3;
        FeatureCollection content2 = (FeatureCollection) source.content(Filter.INCLUDE, expectedCount2);
        Iterator features2 = content2.iterator();
        int count2 = 0;
        try {
            while (features2.hasNext()) {
                Feature f = (Feature) features2.next();
                LOGGER.finest(String.valueOf(f));
                ++count2;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            content.close(features2);
        }
        assertEquals("feature count", expectedCount2, count2);
        
    }

    /**
     * Creates a MemoryDataStore contaning a simple FeatureType with test data
     * for the "Multiple columns could be mapped to a multi-value property"
     * mapping case.
     * <p>
     * The structure of the "WaterSample" FeatureType is as follows: <table>
     * <tr>
     * <th>watersampleid</th>
     * <th>ph</th>
     * <th>temp</th>
     * <th>turbidity</th>
     * </tr>
     * <tr>
     * <td>watersample.1</td>
     * <td>7</td>
     * <td>21</td>
     * <td>0.6</td>
     * </tr>
     * </table>
     * </p>
     */
    public static MemoryDataAccess createWaterSampleTestFeatures() throws Exception {
        MemoryDataAccess dataStore = new MemoryDataAccess();
        SimpleTypeFactory tf = new SimpleTypeFactoryImpl();
        SimpleTypeBuilder tb = new SimpleTypeBuilder(tf);

        tb.setName(TestData.WATERSAMPLE_TYPENAME.getLocalPart());
        tb.addAttribute("watersampleid", String.class);
        tb.addAttribute("ph", Integer.class);
        tb.addAttribute("temp", Integer.class);
        tb.addAttribute("turbidity", Float.class);

        SimpleFeatureType type = tb.feature();

        dataStore.createSchemaInternal(type);

        final int NUM_FEATURES = 10;
        SimpleFeatureFactory af = new SimpleFeatureFactoryImpl();

        SimpleFeatureBuilder fbuilder = new SimpleFeatureBuilder(new SimpleFeatureFactoryImpl());
        for (int i = 0; i < NUM_FEATURES; i++) {
            String fid = type.getName().getLocalPart() + "." + i;
            
            fbuilder.init();
            fbuilder.setType(type);
            fbuilder.add("watersample." + i);
            fbuilder.add(new Integer(i));
            fbuilder.add(new Integer(10 + i));
            fbuilder.add(new Float(i));

            SimpleFeature f = fbuilder.feature(fid);
            dataStore.addFeatureInternal(f);
        }
        return dataStore;
    }
}
