/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.mif;

import java.util.HashMap;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


/**
 * DOCUMENT ME!
 *
 * @author Luca S. Percich, AMA-MI
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/mif/src/test/java/org/geotools/data/mif/MIFFileTest.java $
 */
public class MIFFileTest extends TestCase {
    private MIFFile mif = null;

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void main(java.lang.String[] args) throws Exception {
        junit.textui.TestRunner.run(new TestSuite(MIFFileTest.class));
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        MIFTestUtils.cleanFiles();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        MIFTestUtils.cleanFiles();
        super.tearDown();
    }

    /**
     * Tests for schema and feature reading from an existing MIF file
     */
    public void testMIFFileOpen() {
        try {
            mif = new MIFFile(MIFTestUtils.fileName("mixed"), // .mif
                    MIFTestUtils.getParams("mif", "", null));
            assertEquals("450",
                mif.getHeaderClause(MIFDataStore.HCLAUSE_VERSION));

            SimpleFeatureType schema = mif.getSchema();
            assertNotNull(schema);
            assertEquals(11, schema.getAttributeCount());
            assertEquals("DESCRIPTION", schema.getDescriptor(1).getLocalName());
            assertEquals(Double.class,
                schema.getDescriptor("LENGTH").getType().getBinding());

             FeatureReader<SimpleFeatureType, SimpleFeature> fr = mif.getFeatureReader();
            int tot = 0;

            while (fr.hasNext()) {
                SimpleFeature f = fr.next();

                if (++tot == 4) {
                    assertEquals("POLYGON", (String) f.getAttribute("GEOMTYPE"));
                }
            }

            fr.close();

            assertEquals(tot, 9);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /*
     * Test a MIF file copy using input FeatureReader, createSchema and output FeatureWriter
     */
    public void testFileCopy() {
        if (!System.getProperty("os.name", "unknown").startsWith("Windows")) {
            // For an unknown reason, this test seems to fail on Linux box.
            return;
        }
        MIFFile in = null;
        MIFFile out = null;
        FeatureReader<SimpleFeatureType, SimpleFeature> inFR = null;
        FeatureReader<SimpleFeatureType, SimpleFeature> outFR = null;
        FeatureWriter<SimpleFeatureType, SimpleFeature> outFW = null;
        int maxAttr = 0;

        try {
            // Input file
            in = new MIFFile(MIFTestUtils.fileName("grafo"), null); // .mif

            SimpleFeatureType ft = in.getSchema();

            maxAttr = ft.getAttributeCount() - 1;

            // Params for output file
            HashMap params = new HashMap();

            // params.put(MIFFile.HCLAUSE_TRANSFORM, "100,100,0,0");
            params.put(MIFDataStore.HCLAUSE_UNIQUE,
                in.getHeaderClause(MIFDataStore.HCLAUSE_UNIQUE));
            params.put(MIFDataStore.HCLAUSE_INDEX,
                in.getHeaderClause(MIFDataStore.HCLAUSE_INDEX));
            params.put(MIFDataStore.HCLAUSE_VERSION,
                in.getHeaderClause(MIFDataStore.HCLAUSE_VERSION));
            params.put(MIFDataStore.HCLAUSE_COORDSYS,
                in.getHeaderClause(MIFDataStore.HCLAUSE_COORDSYS));

            // params.put(MIFDataStore.HCLAUSE_DELIMITER, in.getHeaderClause(MIFDataStore.HCLAUSE_DELIMITER));
            params.put(MIFDataStore.HCLAUSE_DELIMITER, ",");

            // Output file
            out = new MIFFile(MIFTestUtils.fileName("grafo_out"), ft, params); // .mif
        } catch (Exception e) {
            fail("Can't create grafo_out: " + e.getMessage());
        }

        try {
            inFR = in.getFeatureReader();
            outFW = out.getFeatureWriter();

            SimpleFeature inF;
            SimpleFeature outF;
            int counter = 0;

            while (inFR.hasNext()) {
                inF = inFR.next();
                outF = outFW.next();

                for (int i = 0; i < outF.getAttributeCount(); i++) {
                    outF.setAttribute(i, inF.getAttribute(i));
                }

                outFW.write();
                counter++;
            }

            inFR.close();
            outFW.close();
        } catch (Exception e) {
            fail("Can't copy features: " + e.getMessage());
        }

        try {
            inFR = in.getFeatureReader();

            outFR = out.getFeatureReader();

            int n = 0;

            while (inFR.hasNext()) {
                SimpleFeature fin = inFR.next();
                SimpleFeature fout = outFR.next();

                // Cycling attribute sampling
                assertEquals(fin.getAttribute(n).toString(),
                    fout.getAttribute(n).toString());

                if (++n > maxAttr) {
                    n = 0;
                }
            }

            inFR.close();
            outFR.close();
        } catch (Exception e) {
            fail("Can't compare features: " + e.getMessage());
        }
    }

    /**
     * Test writing / appending
     */
    public void testFeatureWriter() {
        try {
            MIFTestUtils.copyMif("mixed", "mixed_wri");

            MIFFile in = new MIFFile(MIFTestUtils.fileName("mixed_wri"), // .mif
                    MIFTestUtils.getParams("", "", null));
            FeatureWriter<SimpleFeatureType, SimpleFeature> fw = in.getFeatureWriter();

            SimpleFeature f;
            int counter = 0;

            while (fw.hasNext()) {
                f = fw.next();
                ++counter;

                if (counter == 5) {
                    fw.remove(); // removes multilinestring line
                } else if (counter == 7) {
                    f.setAttribute("DESCRIPTION", "fubar");
                    fw.write();
                } else {
                    f.setAttribute("DESCRIPTION", "foo"); // shouldn't affect data because I dont call write()
                }
            }

            // Appends a line
            SimpleFeature newf = fw.next();
            newf.setAttribute("DESCRIPTION", "newline");
            fw.write();

            fw.close();

            // Reopens a writer to modify feature # 3
            fw = in.getFeatureWriter();
            f = fw.next();
            f = fw.next();
            f = fw.next();
            f.setAttribute("NUM_OF_SEGMENTS", new Integer(179));
            fw.write();
            fw.close(); // should rewrite all other features

             FeatureReader<SimpleFeatureType, SimpleFeature> fr = in.getFeatureReader();
            counter = 0;

            while (fr.hasNext()) {
                f = fr.next();
                ++counter;

                String descr = (String) f.getAttribute("DESCRIPTION");
                assertEquals(false, descr.equals("foo"));

                if (counter == 3) {
                    assertEquals(179,
                        ((Integer) f.getAttribute("NUM_OF_SEGMENTS")).intValue());
                } else if (counter == 5) {
                    assertEquals("Single polygon with 2 holes", descr);
                } else if (counter == 6) {
                    assertEquals("fubar", descr);
                } else if (counter == 9) {
                    assertEquals("newline", descr);
                }
            }

            fr.close();
            assertEquals(9, counter);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test opening of two FeatureReaders on the same MIF file.
     */
    public void testConcurrentReader() {
        try {
            MIFFile mif1 = new MIFFile(MIFTestUtils.fileName("grafo"), // .mif
                    MIFTestUtils.getParams("", "", null));

            MIFFile mif2 = new MIFFile(MIFTestUtils.fileName("grafo"), // .mif
                    MIFTestUtils.getParams("", "", null));

             FeatureReader<SimpleFeatureType, SimpleFeature> fr1 = mif1.getFeatureReader();

            fr1.next();

            SimpleFeature f1 = fr1.next();

             FeatureReader<SimpleFeatureType, SimpleFeature> fr2 = mif2.getFeatureReader();

            fr2.next();

            SimpleFeature f2 = fr2.next();

            for (int i = 0; i < f1.getAttributeCount(); i++) {
                assertEquals("Features are different",
                    f1.getAttribute(i).toString(), f2.getAttribute(i).toString());
            }

            fr2.close();
            fr1.close();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testUntypedGeometryTypes() {
        try {
            mif = new MIFFile(MIFTestUtils.fileName("mixed"), // .mif
                    MIFTestUtils.getParams("mif", "", null, "untyped"));

            SimpleFeatureType ft = mif.getSchema();

            assertEquals(ft.getGeometryDescriptor().getType().getBinding(), Geometry.class);

             FeatureReader<SimpleFeatureType, SimpleFeature> fr = mif.getFeatureReader();

            while (fr.hasNext()) {
                SimpleFeature f = fr.next();
                String geomtype = (String) f.getAttribute("GEOMTYPE");

                if (geomtype.equals("LINE")) {
                    geomtype = "LINESTRING";
                }

                Geometry geom = (Geometry) f.getAttribute("the_geom");

                if (geom == null) {
                    assertEquals(geomtype, "NULL");
                } else {
                    String gtype = geom.getClass().getName();
                    gtype = gtype.substring(28).toUpperCase();

                    boolean compat = (geomtype.equals(gtype));

                    // compat = compat || geomtype.equals("MULTI" + gtype);
                    assertTrue("Uncompatible types: " + gtype + ", " + geomtype,
                        compat);

                    if (geomtype.equals("POLYGON")) {
                        assertEquals("Bad number of holes",
                            ((Integer) f.getAttribute("NUM_OF_HOLES")).intValue(),
                            ((Polygon) geom).getNumInteriorRing());
                    }
                }
            }

            fr.close();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testTypedGeometryUntyped() {
        doTestTyping("grafo", "untyped", Geometry.class, LineString.class, false);
    }

    /**
     * DOCUMENT ME!
     */
    public void testTypedGeometryTyped() {
        doTestTyping("grafo", "typed", LineString.class, false);
    }

    /**
     * DOCUMENT ME!
     */
    public void testTypedGeometryTypesMulti() {
        doTestTyping("grafo", "multi", MultiLineString.class, false);
    }

    /**
     * DOCUMENT ME!
     */
    public void testTypedGeometryTypesLineString() {
        doTestTyping("grafo", "LineString", LineString.class, false);
    }

    /**
     * DOCUMENT ME!
     */
    public void testTypedGeometryTypesMultiLineString() {
        doTestTyping("grafo", "MultiLineString", MultiLineString.class, false);
    }

    /**
     * DOCUMENT ME!
     */
    public void testTypedGeometryTypesPoint() {
        // If I force to MultiLineString, the features read must already be MultiLineString
        doTestTyping("nodi", "Point", Point.class, false);
    }

    public void testTypedGeometryText() {
        doTestTyping("text", "Text", Point.class, false);
    }

    public void testTypedGeometryTextAuto() {
        doTestTyping("text", "Typed", Point.class, false);
    }

    /**
     * DOCUMENT ME!
     */
    public void testTypedGeometryTypesMultiPoint() {
        // If I force to MultiLineString, the features read must already be MultiLineString
        doTestTyping("nodi", "multi", Point.class, false);
    }

    private void doTestTyping(String typeName, String geomType,
        Class geomClass, boolean errorExpected) {
        doTestTyping(typeName, geomType, geomClass, geomClass, errorExpected);
    }

    private void doTestTyping(String typeName, String geomType,
        Class geomClass, Class instanceGeomClass, boolean errorExpected) {
        try {
            mif = new MIFFile(MIFTestUtils.fileName(typeName), // .mif
                    MIFTestUtils.getParams("mif", "", null, geomType));

            SimpleFeatureType ft = mif.getSchema();

            assertEquals(geomClass, ft.getGeometryDescriptor().getType().getBinding());

             FeatureReader<SimpleFeatureType, SimpleFeature> fr = mif.getFeatureReader();

            try {
                SimpleFeature f = fr.next();
                Geometry geom = (Geometry) f.getAttribute("the_geom");

                if (errorExpected) {
                    fail("Expected error reading geometric attribute");
                } else {
                    assertEquals(instanceGeomClass, geom.getClass());
                }
            } catch (Exception e) {
                if (!errorExpected) {
                    fail(e.getMessage());
                }
            }

            fr.close();
        } catch (Exception e) {
            if (!errorExpected) {
                fail(e.getMessage());
            }
        }
    }
}
