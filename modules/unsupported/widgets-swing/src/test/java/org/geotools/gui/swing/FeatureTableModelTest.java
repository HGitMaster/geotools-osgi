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
 */
package org.geotools.gui.swing;

import java.util.logging.Logger;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.TopologyException;

import org.geotools.util.logging.Logging;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.gui.swing.table.FeatureTableModel;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


/**
 * Tests {@link FeatureTableModel}. The table will be shown only if the test is run from the
 * main method. Otherwise (i.e. if run from Maven), widgets are invisibles.
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing/src/test/java/org/geotools/gui/swing/FeatureTableModelTest.java $
 * @version $Id: FeatureTableModelTest.java 30655 2008-06-12 20:24:25Z acuster $
 * @author James Macgill, CCG
 */
public class FeatureTableModelTest extends TestBase {
    /**
     * Standard logging instance.
     */
    protected static final Logger LOGGER = Logging.getLogger("org.geotools.filter");

    /**
     * Feature on which to perform tests.
     */
    private static SimpleFeature testFeatures[] = null;

    /**
     * Feature type on which to perform tests.
     */
    private static SimpleFeatureType testSchema;

    /**
     * Creates a test case.
     */
    public FeatureTableModelTest(final String testName) {
        super(testName);
    }

    /**
     * Run the test case from the command line.
     */
    public static void main(final String[] args) {
        main(args, suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(FeatureTableModelTest.class);
        return suite;
    }

    /**
     * Display the table.
     */
    public void testDisplay() throws Exception {
        MemoryDataStore datastore = new MemoryDataStore();
        datastore.addFeature(testFeatures[0]);
        datastore.addFeature(testFeatures[1]);

        String typeName = datastore.getTypeNames()[0];
        FeatureCollection<SimpleFeatureType, SimpleFeature> table = datastore.getFeatureSource(typeName).getFeatures();

        FeatureTableModel ftm = new FeatureTableModel();
        ftm.setFeatureCollection(table);
        JTable jtable = new JTable();
        jtable.setModel(ftm);
        JScrollPane scroll = new JScrollPane(jtable);
        show(scroll, "FeatureTableModel");
    }

    /**
     * Sets up a schema and a test feature.
     *
     * @throws SchemaException If there is a problem setting up the schema.
     * @throws IllegalAttributeException If problem setting up the feature.
     */
    @Override
    protected void setUp() throws SchemaException, IllegalAttributeException, TopologyException {

        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName("testSchema");
        b.add("testGeometry", Geometry.class);
        b.add("testBoolean", Boolean.class);
        b.add("testCharacter", Character.class);
        b.add("testByte",      Byte.class);
        b.add("testShort",     Short.class);
        b.add("testInteger",   Integer.class);
        b.add("testLong",      Long.class);
        b.add("testFloat",     Float.class);
        b.add("testDouble",    Double.class);
        b.add("testString",    String.class);


        // Builds the schema
        testSchema = b.buildFeatureType();



        // Creates coordinates for a linestring
        Coordinate[] lineCoords = new Coordinate[3];
        lineCoords[0] = new Coordinate(1,2);
        lineCoords[1] = new Coordinate(3,4);
        lineCoords[2] = new Coordinate(5,6);

        // Creates coordinates for a polygon
        Coordinate[] polyCoords = new Coordinate[5];
        polyCoords[0] = new Coordinate(1,1);
        polyCoords[1] = new Coordinate(2,4);
        polyCoords[2] = new Coordinate(4,4);
        polyCoords[3] = new Coordinate(8,2);
        polyCoords[4] = new Coordinate(1,1);

        GeometryFactory fac = new GeometryFactory();

        // Builds the test feature
        Object[] attributesA = new Object[10];
        attributesA[0] = fac.createLineString(lineCoords);
        attributesA[1] = new Boolean(true);
        attributesA[2] = new Character('t');
        attributesA[3] = new Byte("10");
        attributesA[4] = new Short("101");
        attributesA[5] = new Integer(1002);
        attributesA[6] = new Long(10003);
        attributesA[7] = new Float(10000.4);
        attributesA[8] = new Double(100000.5);
        attributesA[9] = "feature A";

        Object[] attributesB = new Object[10];
        LinearRing ring = fac.createLinearRing(polyCoords);
        attributesB[0] = fac.createPolygon(ring,null);
        attributesB[1] = new Boolean(false);
        attributesB[2] = new Character('t');
        attributesB[3] = new Byte("20");
        attributesB[4] = new Short("201");
        attributesB[5] = new Integer(2002);
        attributesB[6] = new Long(20003);
        attributesB[7] = new Float(20000.4);
        attributesB[8] = new Double(200000.5);
        attributesB[9] = "feature B";


        // Creates the feature itself
        testFeatures = new SimpleFeature[2];
        testFeatures[0] = SimpleFeatureBuilder.build(testSchema, attributesA, "fid.1");
        testFeatures[1] = SimpleFeatureBuilder.build(testSchema, attributesB, "fid.2");
        //_log.debug("...flat features created");
    }
}
