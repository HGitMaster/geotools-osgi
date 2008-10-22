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

import java.io.IOException;
import java.sql.Date;
import java.util.HashMap;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.Filter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;


/**
 * DOCUMENT ME!
 *
 * @author Luca S. Percich, AMA-MI
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/mif/src/test/java/org/geotools/data/mif/MIFDataStoreTest.java $
 */
public class MIFDataStoreTest extends TestCase {
    private MIFDataStore ds;

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void main(java.lang.String[] args) throws Exception {
        junit.textui.TestRunner.run(new TestSuite(MIFDataStoreTest.class));
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
     * Utility method for instantiating a MIFDataStore
     *
     * @param initPath DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected boolean initDS(String initPath) {
        try {
            initPath = MIFTestUtils.fileName(initPath);

            HashMap params = MIFTestUtils.getParams("mif", initPath, null);
            ds = new MIFDataStore(initPath, params);
            assertNotNull(ds);

            return true;
        } catch (Exception e) {
            fail(e.getMessage());

            return false;
        }
    }

    /**
     * See if all the MIF in data dir are recognized
     */
    public void testOpenDir() {
        initDS("");

        try {
            assertNotNull(ds.getSchema("grafo"));
            assertNotNull(ds.getSchema("nodi"));
            assertNotNull(ds.getSchema("mixed"));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
    
    /**
     */
    public void testCreateSchema() {
        initDS("");

        try {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName("newschema");
            
            AttributeTypeBuilder atb = new AttributeTypeBuilder();
            atb.setName("obj");
            atb.setBinding(LineString.class);
            atb.setNillable(true);
            
            builder.add(atb.buildDescriptor("obj"));
            
            atb.setName("charfield");
            atb.setBinding(String.class);
            atb.setNillable(false);
            atb.setLength(25);
            atb.setDefaultValue("");
            
            builder.add(atb.buildDescriptor("charfield"));
            
            atb.setName("intfield");
            atb.setBinding(Integer.class);
            atb.setLength(0);
            atb.setDefaultValue(0);
            
            builder.add(atb.buildDescriptor("intfield"));

            atb.setName("datefield");
            atb.setBinding(Date.class);
            atb.setDefaultValue(null);
            
            builder.add(atb.buildDescriptor("datefield"));

            atb.setName("doublefield");
            atb.setBinding(Double.class);
            atb.setDefaultValue(0d);
            
            builder.add(atb.buildDescriptor("doublefield"));

            atb.setName("floatfield");
            atb.setBinding(Float.class);
            atb.setDefaultValue(0f);
            
            builder.add(atb.buildDescriptor("floatfield"));
            
            atb.setName("boolfield");
            atb.setBinding(Boolean.class);
            atb.setDefaultValue(false);
            
            builder.add(atb.buildDescriptor("boolfield"));

            SimpleFeatureType newFT = builder.buildFeatureType();

            ds.createSchema(newFT);

            SimpleFeatureType builtFT = ds.getSchema("newschema");

            assertEquals(builtFT, newFT);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     */
    public void testCreateSchemaBadGeometry() {
        initDS("");

        try {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName("newschema");
            
            AttributeTypeBuilder atb = new AttributeTypeBuilder();
            atb.setName("charfield");
            atb.setBinding(String.class);
            atb.setNillable(false);
            atb.setLength(25);
            atb.setDefaultValue("");
            
            builder.add(atb.buildDescriptor("charfield"));
            
            atb.setName("intfield");
            atb.setBinding(Integer.class);
            atb.setLength(0);
            atb.setDefaultValue(0);
            
            builder.add(atb.buildDescriptor("intfield"));
            
            atb.setName("obj");
            atb.setBinding(LineString.class);
            atb.setNillable(true);
            
            builder.add(atb.buildDescriptor("obj"));

            SimpleFeatureType newFT = builder.buildFeatureType();

            ds.createSchema(newFT);
            fail("SchemaException expected"); // Geometry must be the first field
        } catch (Exception e) {
        }
    }

    /**
     */
    public void testCreateSchemaTwoGeometry() {
        initDS("");

        try {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName("newschema");
            
            AttributeTypeBuilder atb = new AttributeTypeBuilder();
            
            atb.setName("obj");
            atb.setBinding(LineString.class);
            atb.setNillable(true);
            
            builder.add(atb.buildDescriptor("obj"));
            
            atb.setName("charfield");
            atb.setBinding(String.class);
            atb.setNillable(false);
            atb.setLength(25);
            atb.setDefaultValue("");
            
            builder.add(atb.buildDescriptor("charfield"));
            
            atb.setName("obj2");
            atb.setBinding(LineString.class);
            atb.setNillable(false);
            atb.setDefaultValue(null);
            
            builder.add(atb.buildDescriptor("obj2"));
            
            atb.setName("intfield");
            atb.setBinding(Integer.class);
            atb.setLength(0);
            atb.setDefaultValue(0);
            
            builder.add(atb.buildDescriptor("intfield"));
            
            SimpleFeatureType newFT = builder.buildFeatureType();

            ds.createSchema(newFT);
            fail("SchemaException expected"); // Only one geometry
        } catch (Exception e) {
        }
    }

    /**
     */
    public void testFeatureReaderFilter() {
        initDS("grafo"); // .mif

        try {
             FeatureReader<SimpleFeatureType, SimpleFeature> fr = getFeatureReader("grafo", "ID = 33755");
            SimpleFeature arc = null;
            Integer id = new Integer(0);

            if (fr.hasNext()) {
                arc = fr.next();
                id = (Integer) arc.getAttribute("ID");
            }

            assertNotNull(arc);
            assertEquals(id.intValue(), 33755);
            fr.close();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests createSchema & FeatureWriter
     */
    public void testFeatureWriter() {
        initDS("");

        String outmif = "grafo_new";

        try {
            SimpleFeatureType newFT = MIFTestUtils.duplicateSchema(ds.getSchema(
                        "grafo"), outmif);
            ds.createSchema(newFT);

            int maxAttr = newFT.getAttributeCount() - 1;

            FeatureWriter<SimpleFeatureType, SimpleFeature> fw = ds.getFeatureWriterAppend(outmif,
                    Transaction.AUTO_COMMIT);
            SimpleFeature f;
             FeatureReader<SimpleFeatureType, SimpleFeature> fr = getFeatureReader("grafo",
                    "ID == 73690 || ID == 71045");

            int counter = 0;

            while (fr.hasNext()) {
                ++counter;

                SimpleFeature fin = fr.next();
                f = fw.next();

                for (int i = 0; i <= maxAttr; i++) {
                    f.setAttribute(i, fin.getAttribute(i));
                }

                fw.write();
            }

            fr.close();
            fw.close();

            assertEquals(counter, 2);

            fw = ds.getFeatureWriter(outmif,
                    MIFTestUtils.parseFilter("ID == 71045"),
                    Transaction.AUTO_COMMIT);

            assertEquals(true, fw.hasNext());
            f = fw.next();
            fw.remove();

            fw.close();

            fw = ds.getFeatureWriterAppend(outmif, Transaction.AUTO_COMMIT);

            f = fw.next();
            f.setAttribute("ID", "99998");
            f.setAttribute("NOMECOMUNE", "foobar");
            fw.write();

            fw.close();

            fr = getFeatureReader(outmif);

            counter = 0;

            while (fr.hasNext()) {
                f = fr.next();
                counter++;
            }

            fr.close();
            assertEquals(counter, 2);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests createSchema & FeatureWriter
     */
    public void testFeatureWriterAppendTransaction() {
        try {
            String outmif = "grafo_append";
            MIFTestUtils.copyMif("grafo", outmif);
            initDS(outmif);

            SimpleFeature f;
            Transaction transaction = new DefaultTransaction("mif");

            try {
                FeatureWriter<SimpleFeatureType, SimpleFeature> fw = ds.getFeatureWriterAppend(outmif, transaction);

                f = fw.next();
                f = fw.next();
                f.setAttribute("ID", "80001");
                f.setAttribute("NOMECOMUNE", "foo");
                fw.write();

                f = fw.next();
                f.setAttribute("ID", "80002");
                f.setAttribute("NOMECOMUNE", "bar");
                fw.write();

                fw.close();

                transaction.commit();
            } catch (Exception e) {
            	e.printStackTrace();
                transaction.rollback();
                fail(e.getMessage());
            } finally {
                transaction.close();
            }

             FeatureReader<SimpleFeatureType, SimpleFeature> fr = getFeatureReader(outmif,
                    "ID > 80000 && ID <80003");

            int counter = 0;

            while (fr.hasNext()) {
                f = fr.next();
                counter++;
            }

            fr.close();

            assertEquals(counter, 2);
        } catch (Exception e) {
        	e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testFeatureSource() {
        String outmif = "mixed_fs";

        try {
            MIFTestUtils.copyMif("mixed", outmif);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        initDS(outmif);

        FeatureSource<SimpleFeatureType, SimpleFeature> fs = null;
        SimpleFeatureType featureType = null;

        try {
            featureType = ds.getSchema(outmif);
            assertNotNull("Cannot get FeatureType", featureType);
        } catch (Exception e) {
            fail("Cannot get FeatureType: " + e.getMessage());
        }

        try {
            fs = ds.getFeatureSource(outmif);
            assertNotNull("Cannot get FeatureSource.", fs);
        } catch (IOException e) {
            fail("Cannot get FeatureSource: " + e.getMessage());
        }

        try {
            ((FeatureStore<SimpleFeatureType, SimpleFeature>) fs).modifyFeatures(featureType.getDescriptor(
                    "DESCRIPTION"), "FOO", Filter.INCLUDE);
        } catch (Exception e) {
            fail("Cannot update Features: " + e.getMessage());
        }

        try {
            ((FeatureStore<SimpleFeatureType, SimpleFeature>) fs).removeFeatures(MIFTestUtils.parseFilter(
                    "GEOMTYPE != 'NULL'"));
        } catch (IOException e) {
            fail("Cannot delete Features: " + e.getMessage());
        }

        try {
             FeatureReader<SimpleFeatureType, SimpleFeature> fr = getFeatureReader(outmif);

            assertEquals(true, fr.hasNext());

            SimpleFeature f = fr.next();
            assertEquals("FOO", f.getAttribute("DESCRIPTION"));
            assertEquals(false, fr.hasNext());

            fr.close();
        } catch (Exception e) {
            fail("Cannot check feature: " + e.getMessage());
        }
    }

    /**
     * Test that feature get the correct SRID
     */
    public void testSRID() {
        initDS("");

         FeatureReader<SimpleFeatureType, SimpleFeature> fr;

        try {
            fr = getFeatureReader("grafo");

            SimpleFeature f = fr.next();
            assertEquals(((Geometry) f.getDefaultGeometry()).getFactory().getSRID(),
                MIFTestUtils.SRID);

            fr.close();
        } catch (Exception e) {
            fail("Cannot check SRID: " + e.getMessage());
        }
    }

    /**
     * Obtain a feature reader for the given featureType / filter
     *
     * @param featureTypeName
     * @param filter
     *
     *
     * @throws Exception
     */
    protected  FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(String featureTypeName,
        String filter) throws Exception {
        DefaultQuery q = new DefaultQuery(featureTypeName,
                MIFTestUtils.parseFilter(filter));

        return ds.getFeatureReader(q, Transaction.AUTO_COMMIT);
    }

    /**
     * Obtain a feature reader for all the features of the given featureType
     *
     * @param featureTypeName
     *
     *
     * @throws Exception
     */
    protected  FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(String featureTypeName)
        throws Exception {
        return getFeatureReader(featureTypeName, "1=1");
    }
}
