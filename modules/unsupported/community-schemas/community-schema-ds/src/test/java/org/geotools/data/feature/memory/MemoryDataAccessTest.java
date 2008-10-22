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

package org.geotools.data.feature.memory;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.data.ComplexTestData;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.AttributeBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.TypeBuilder;
import org.geotools.feature.Types;
import org.geotools.filter.IllegalFilterException;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Expression;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: MemoryDataAccessTest.java 31607 2008-10-01 07:55:36Z bencd $
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/community-schemas/community-schema-ds/src/test/java/org/geotools/data/feature/memory/MemoryDataAccessTest.java $
 * @since 2.4
 */
public class MemoryDataAccessTest extends TestCase {

    private MemoryDataAccess dataStore;

    FeatureType wq_plusType;

    static List wq_plusFeatures;

    static final int NUM_FEATURES = 20;

    private TypeBuilder builder;

    private FilterFactory ff;

    protected void setUp() throws Exception {
        super.setUp();
        wq_plusType = ComplexTestData
                .createExample01MultiValuedComplexProperty(new TypeFactoryImpl());
        TypeFactory tf = new TypeFactoryImpl();
        builder = new TypeBuilder(tf);
        ff = CommonFactoryFinder.getFilterFactory(null);

        dataStore = complexFixture();

    }

    protected void tearDown() throws Exception {
        super.tearDown();
        wq_plusType = null;
        wq_plusFeatures = null;
    }

    public void testFilter() throws Exception {
        TypeFactory tf = new SimpleTypeFactoryImpl();
        builder = new TypeBuilder(tf);

        FeatureFactory af = new AttributeFactoryImpl();
        AttributeBuilder attBuilder = new AttributeBuilder(af);

        builder.setName("testType");
        builder.setBinding(Integer.class);
        AttributeType testType = builder.attribute();

        builder.addAttribute("test", testType);
        builder.setName("typeName");

        SimpleFeatureType t = (SimpleFeatureType) builder.feature();

        attBuilder.setType(t);
        attBuilder.add(null, "test");
        Attribute attribute = attBuilder.build();

        PropertyIsGreaterThan gtFilter = ff.greater(ff.property("test"), ff
                .literal(12));
        PropertyIsNull nullf = ff.isNull(ff.property("test"));

        Filter filter = ff.or(nullf, gtFilter);

        boolean contains = filter.evaluate(attribute);
        assertTrue(contains);
    }

    public void testDescribeType() throws IOException {
        AttributeDescriptor descriptor = (AttributeDescriptor) dataStore
                .describe(wq_plusType.getName());
        assertNotNull(descriptor);
        FeatureType schema = (FeatureType) descriptor.type();
        assertNotNull(schema);
        assertEquals(wq_plusType, schema);
    }

    public void testFeatureReader() throws IOException {
        Name typeName = wq_plusType.getName();
        FeatureSource source = (FeatureSource) dataStore.access(typeName);

        FeatureCollection features = (FeatureCollection) source.content();
        assertNotNull(features);

        int count = 0;
        Iterator reader = features.iterator();
        for (; reader.hasNext();) {
            Feature object = (Feature) reader.next();
            assertNotNull(object);
            count++;
        }
        features.close(reader);
        assertEquals(NUM_FEATURES, count);
    }

    public void testFeatureSource() throws IOException {
        Name name = wq_plusType.getName();
        FeatureSource fs = (FeatureSource) dataStore.access(name);
        assertNotNull(fs);
        AttributeDescriptor describe = (AttributeDescriptor) fs.describe();
        assertEquals(wq_plusType, describe.type());

        assertNotNull(fs.getBounds());
        Envelope expected = new Envelope();
        for (Iterator it = wq_plusFeatures.iterator(); it.hasNext();) {
            expected.expandToInclude((Envelope) ((Feature) it.next())
                    .getBounds());
        }
        assertTrue(expected.equals(fs.getBounds()));
        assertEquals(NUM_FEATURES, fs.getCount(Query.ALL));

        assertSame(dataStore, fs.getDataStore());
    }

    /**
     * Query:
     * 
     * <pre>
     *          (measurement/determinand_description = 'determinand_description_5_0')
     *           OR
     *          ( length(project_no) &gt; 12) //at least ending with two digits
     * </pre>
     */
    public void testComplexQuery() throws IllegalFilterException, IOException {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

        Filter determinand = ff.equals(ff
                .property("measurement/determinand_description"), ff
                .literal("determinand_description_5_0"));

        Expression length = ff.function("LengthFunction", ff
                .property("project_no"));
        Filter project_no = ff.greater(length, ff.literal(13));

        Filter filter = ff.or(determinand, project_no);

        FeatureSource source = dataStore.access(wq_plusType.getName());
        FeatureCollection result = (FeatureCollection) source.content(filter);

        assertNotNull(result);

        int expected = 1 + (NUM_FEATURES - 10);
        int actual = ((Collection)result).size();
        assertEquals(expected, actual);
    }

    /**
     * Creates an in memory datastore containing a complex FeatureType:
     * <p>
     * Schema:
     * 
     * <pre>
     *              wq_plus 
     *              	 sitename (1..1)
     *              	 anzlic_no (0..1)
     *              	 location (0..1)
     *                measurement (0..*)
     *                	determinand_description (1..1)
     *                	result (1..1)
     *                project_no (0..1)
     * </pre>
     * 
     * </p>
     * <p>
     * The features created has a variable number of measurement attribute
     * instances. {@link #NUM_FEATURES} features are created inside an iteration
     * from 0 to <code>NUM_FEATURES - 1</code>. The iteration number is the
     * number of measurement instances each feature has.
     * </p>
     * 
     * @return
     */
    private static MemoryDataAccess complexFixture() throws IOException {
        MemoryDataAccess md = new MemoryDataAccess();
        final TypeFactory tf = new TypeFactoryImpl();

        final FeatureFactory attf = new AttributeFactoryImpl();
        final GeometryFactory gf = new GeometryFactory();
        final AttributeBuilder builder = new AttributeBuilder(attf);

        FeatureType ftype = ComplexTestData
                .createExample01MultiValuedComplexProperty(tf);

        // md.createSchema(ftype);

        wq_plusFeatures = new LinkedList();
        final String namespaceURI = ftype.getName().getNamespaceURI();

        builder.setType(ftype);
        for (int i = 0; i < NUM_FEATURES; i++) {

            builder.add("sitename_" + i, Types.typeName(namespaceURI, "sitename"));

            builder.add("anzlic_no_" + i, Types.typeName(namespaceURI, "anzlic_no"));

            builder.add(gf.createPoint(new Coordinate(i, i)), Types.typeName(
                    namespaceURI, "location"));

            PropertyDescriptor measurementDescriptor = Types.descriptor(ftype,
                    Types.typeName(namespaceURI, "measurement"));

            ComplexType mtype = (ComplexType) measurementDescriptor.type();

            AttributeBuilder mbuilder = new AttributeBuilder(attf);

            //Collection measurements = new ArrayList();

            for (int mcount = 0; mcount < i; mcount++) {
                mbuilder.setType(mtype);

                mbuilder.add("determinand_description_" + i + "_" + mcount,
                        Types.typeName(namespaceURI, "determinand_description"));

                mbuilder.add("result_" + i + "_" + mcount, Types.typeName(
                        namespaceURI, "result"));

                ComplexAttribute measurement = (ComplexAttribute) mbuilder
                        .build();
                // measurements.add(measurement);

                builder.add(measurement.getValue(), Types.typeName(namespaceURI,
                        "measurement"));
            }
            /*
             * if (measurements.size() > 0) { builder .add(measurements, new
             * Name(namespaceURI, "measurement")); }
             */

            builder.add("project_no_ " + i,
                    Types.typeName(namespaceURI, "project_no"));

            String fid = ftype.getName().getLocalPart() + "." + i;
            Feature f = (Feature) builder.build(fid);

            wq_plusFeatures.add(f);
        }

        md.addFeatures(wq_plusFeatures);
        return md;
    }
}
