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
package org.geotools.data.shapefile.indexed;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.geotools.TestData;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShpFileType;
import org.geotools.data.shapefile.TestCaseSupport;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.IllegalFilterException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/shpLazyLoadingIndex/ext/shape/test/org/geotools/data/shapefile/indexed/ShapefileDataStoreTest.java $
 * @version $Id: IndexedShapefileDataStoreTest.java 31597 2008-09-25 14:02:39Z groldan $
 * @author Ian Schneider
 */
public class IndexedShapefileDataStoreTest extends TestCaseSupport {
    final static String STATE_POP = "shapes/statepop.shp";

    final static String STREAM = "shapes/stream.shp";

    final static String DANISH = "shapes/danish_point.shp";

    final static String CHINESE = "shapes/chinese_poly.shp";

    public IndexedShapefileDataStoreTest(String testName) throws IOException {
        super(testName);
    }

    protected FeatureCollection<SimpleFeatureType, SimpleFeature> loadFeatures(String resource, Query q)
            throws Exception {
        if (q == null) {
            q = new DefaultQuery();
        }

        URL url = TestData.url(resource);
        IndexedShapefileDataStore s = new IndexedShapefileDataStore(url);
        FeatureSource<SimpleFeatureType, SimpleFeature> fs = s.getFeatureSource(s.getTypeNames()[0]);

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = fs.getFeatures(q);

        s.dispose();
        
        return features;
    }

    protected FeatureCollection<SimpleFeatureType, SimpleFeature> loadFeatures(String resource, Charset charset,
            Query q) throws Exception {
        if (q == null)
            q = new DefaultQuery();
        URL url = TestData.url(resource);
        ShapefileDataStore s = new IndexedShapefileDataStore(url, null, false,
                true, IndexType.QIX, charset);
        FeatureSource<SimpleFeatureType, SimpleFeature> fs = s.getFeatureSource(s.getTypeNames()[0]);
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = fs.getFeatures(q);
        s.dispose();
        return features;
    }

    protected FeatureCollection<SimpleFeatureType, SimpleFeature> loadFeatures(IndexedShapefileDataStore s)
            throws Exception {
        return s.getFeatureSource(s.getTypeNames()[0]).getFeatures();
    }

    public void testLoad() throws Exception {
        loadFeatures(STATE_POP, null);
    }

    public void testLoadDanishChars() throws Exception {
        FeatureCollection<SimpleFeatureType, SimpleFeature> fc = loadFeatures(DANISH, null);
        SimpleFeature first = firstFeature(fc);
        // Charlï¿½tte, if you can read it with your OS charset
        assertEquals("Charl\u00F8tte", first.getAttribute("TEKST1"));
    }

    public void testLoadChineseChars() throws Exception {
        try {
            FeatureCollection<SimpleFeatureType, SimpleFeature> fc = loadFeatures(CHINESE, Charset
                    .forName("GB18030"), null);
            SimpleFeature first = firstFeature(fc);
            String name = (String) first.getAttribute("NAME");
            assertEquals("\u9ed1\u9f99\u6c5f\u7701", name);
        } catch (UnsupportedCharsetException no) {
            // this JDK has not been installed with the required
            // lanaguage
        }
    }

    public void testSchema() throws Exception {
        URL url = TestData.url(STATE_POP);
        IndexedShapefileDataStore s = new IndexedShapefileDataStore(url);
        SimpleFeatureType schema = s.getSchema(s.getTypeNames()[0]);
        List<AttributeDescriptor> types = schema.getAttributeDescriptors();
        assertEquals("Number of Attributes", 253, types.size());
        assertNotNull(schema.getCoordinateReferenceSystem());
    }

    public void testSpacesInPath() throws Exception {
        URL u = TestData.url(TestCaseSupport.class, "folder with spaces/pointtest.shp");
        File f = new File(URLDecoder.decode(u.getFile(), "UTF-8"));
        assertTrue(f.exists());

        IndexedShapefileDataStore s = new IndexedShapefileDataStore(u);
        loadFeatures(s);
        s.dispose();
    }

    /**
     * Test envelope versus old DataSource
     */
    public void testEnvelope() throws Exception {
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = loadFeatures(STATE_POP, null);
        testEnvelope(features, IndexType.QIX);
        testEnvelope(features, IndexType.NONE);
    }

    private void testEnvelope(FeatureCollection<SimpleFeatureType, SimpleFeature> features, IndexType treeType)
            throws MalformedURLException, IOException {
        IndexedShapefileDataStore s = new IndexedShapefileDataStore(TestData
                .url(STATE_POP), null, true, true, treeType);
        String typeName = s.getTypeNames()[0];
        FeatureCollection<SimpleFeatureType, SimpleFeature> all = s.getFeatureSource(typeName).getFeatures();

        assertEquals(features.getBounds(), all.getBounds());
        s.dispose();
    }

    public void testCreateAndReadQIX() throws Exception {
        File shpFile = copyShapefiles(STATE_POP);
        URL url = shpFile.toURL();
        String filename = url.getFile();
        filename = filename.substring(0, filename.lastIndexOf("."));

        File file = new File(filename + ".qix");

        if (file.exists()) {
            file.delete();
        }
        file.deleteOnExit();

        IndexedShapefileDataStore ds = new IndexedShapefileDataStore(url, null,
                true, true, IndexType.QIX);
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = ds.getFeatureSource().getFeatures();
        FeatureIterator<SimpleFeature> indexIter = features.features();

        GeometryFactory factory = new GeometryFactory();
        double area = Double.MAX_VALUE;
        SimpleFeature smallestFeature = null;
        while (indexIter.hasNext()) {
            SimpleFeature newFeature = indexIter.next();

            BoundingBox bounds = newFeature.getBounds();
            Geometry geometry = factory.toGeometry(new ReferencedEnvelope(
                    bounds));
            double newArea = geometry.getArea();

            if (smallestFeature == null || newArea < area) {
                smallestFeature = newFeature;
                area = newArea;
            }
        }
        indexIter.close();

        IndexedShapefileDataStore ds2 = new IndexedShapefileDataStore(url,
                null, false, false, IndexType.NONE);

        Envelope newBounds = ds.getBounds(Query.ALL);
        double dx = newBounds.getWidth() / 4;
        double dy = newBounds.getHeight() / 4;
        newBounds = new Envelope(newBounds.getMinX() + dx, newBounds.getMaxX()
                - dx, newBounds.getMinY() + dy, newBounds.getMaxY() - dy);

        CoordinateReferenceSystem crs = features.getSchema().getCoordinateReferenceSystem();

        performQueryComparison(ds, ds2, new ReferencedEnvelope(newBounds, crs));
        performQueryComparison(ds, ds2, new ReferencedEnvelope(smallestFeature
                .getBounds()));

        assertTrue(file.exists());
        ds.dispose();
        ds2.dispose();
    }

    public void testFidFilter() throws Exception {
        File shpFile = copyShapefiles(STATE_POP);
        URL url = shpFile.toURL();
        IndexedShapefileDataStore ds = new IndexedShapefileDataStore(url, null, true, true,
                IndexType.NONE);
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = ds.getFeatureSource();
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = featureSource.getFeatures();
        FeatureIterator<SimpleFeature> indexIter = features.features();

        Set<String> expectedFids = new HashSet<String>();
        final Filter fidFilter;
        try {
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
            Set<FeatureId> fids = new HashSet<FeatureId>();
            while (indexIter.hasNext()) {
                SimpleFeature newFeature = indexIter.next();
                String id = newFeature.getID();
                expectedFids.add(id);
                fids.add(ff.featureId(id));
            }
            fidFilter = ff.id(fids);
        } finally {
            indexIter.close();
        }

        Set<String> actualFids = new HashSet<String>();
        {
            features = featureSource.getFeatures(fidFilter);
            indexIter = features.features();
            while (indexIter.hasNext()) {
                SimpleFeature next = indexIter.next();
                String id = next.getID();
                actualFids.add(id);
            }
        }

        TreeSet<String> lackingFids = new TreeSet<String>(expectedFids);
        lackingFids.removeAll(actualFids);

        TreeSet<String> unexpectedFids = new TreeSet<String>(actualFids);
        unexpectedFids.removeAll(expectedFids);

        String lacking = String.valueOf(lackingFids);
        String unexpected = String.valueOf(unexpectedFids);
        String failureMsg = "lacking fids: " + lacking + ". Unexpected ones: " + unexpected;
        assertEquals(failureMsg, expectedFids.size(), actualFids.size());
        assertEquals(failureMsg, expectedFids, actualFids);
    }

    private ArrayList performQueryComparison(
            IndexedShapefileDataStore indexedDS,
            IndexedShapefileDataStore baselineDS, ReferencedEnvelope newBounds)
            throws FactoryRegistryException, IllegalFilterException,
            IOException {
        FeatureCollection<SimpleFeatureType, SimpleFeature> features;
        FeatureIterator<SimpleFeature> indexIter;
        FilterFactory2 fac = CommonFactoryFinder.getFilterFactory2(null);
        String geometryName = indexedDS.getSchema().getGeometryDescriptor()
                .getLocalName();

        Filter filter = fac.bbox(fac.property(geometryName), newBounds);

        features = indexedDS.getFeatureSource().getFeatures(filter);
        FeatureCollection<SimpleFeatureType, SimpleFeature> features2 = baselineDS.getFeatureSource()
                .getFeatures(filter);

        FeatureIterator<SimpleFeature> baselineIter = features2.features();
        indexIter = features.features();

        ArrayList baselineFeatures = new ArrayList();
        ArrayList indexedFeatures = new ArrayList();

        try {
            while (baselineIter.hasNext()) {
                baselineFeatures.add(baselineIter.next());
            }
            while (indexIter.hasNext()) {
                indexedFeatures.add(indexIter.next());
            }
            assertFalse(indexIter.hasNext());
            assertFalse(baselineIter.hasNext());
            assertEquals(baselineFeatures, indexedFeatures);
        } finally {
            indexIter.close();
            baselineIter.close();
        }
        return indexedFeatures;
    }
//
//    public void testCreateAndReadGRX() throws Exception {
//        URL url = TestData.url(STATE_POP);
//        String filename = url.getFile();
//        filename = filename.substring(0, filename.lastIndexOf("."));
//
//        File file = new File(filename + ".grx");
//
//        if (file.exists()) {
//            file.delete();
//        }
//
//        IndexedShapefileDataStore ds = new IndexedShapefileDataStore(url, null,
//                true, true, IndexType.EXPERIMENTAL_UNSUPPORTED_GRX);
//        FeatureCollection<SimpleFeatureType, SimpleFeature> features = ds.getFeatureSource().getFeatures();
//        Iterator iter = features.iterator();
//
//        while (iter.hasNext()) {
//            iter.next();
//        }
//
//        // TODO: The following assertion fails
//        // assertTrue(file.exists());
//    }

    public void testLoadAndVerify() throws Exception {
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = loadFeatures(STATE_POP, null);

        int count = features.size();
        assertTrue("Got Features", count > 0);
        // assertEquals("Number of Features loaded", 49, count); // FILE CORRECT
        // assertEquals("Number of Features loaded", 3, count); // JAR WRONG

        SimpleFeatureType schema = firstFeature(features).getFeatureType();
        assertNotNull(schema.getGeometryDescriptor());
        assertEquals("Number of Attributes", 253, schema.getAttributeCount());
        assertEquals("Value of statename is wrong", firstFeature(features)
                .getAttribute("STATE_NAME"), "Illinois");
        assertEquals("Value of land area is wrong", ((Double) firstFeature(
                features).getAttribute("LAND_KM")).doubleValue(), 143986.61,
                0.001);
    }

    private IndexedShapefileDataStore createDataStore(File f) throws Exception {
        FeatureCollection<SimpleFeatureType, SimpleFeature> fc = createFeatureCollection();
        f.createNewFile();

        IndexedShapefileDataStore sds = new IndexedShapefileDataStore(f.toURL());
        writeFeatures(sds, fc);

        return sds;
    }

    private IndexedShapefileDataStore createDataStore() throws Exception {
        return createDataStore(getTempFile());
    }

    /**
     * Create a set of features, then remove every other one, updating the
     * remaining. Test for removal and proper update after reloading...
     */
    public void testUpdating() throws Throwable {
        try {
            IndexedShapefileDataStore sds = createDataStore();
            loadFeatures(sds);

            FeatureWriter<SimpleFeatureType, SimpleFeature> writer = null;

            try {
                writer = sds.getFeatureWriter(sds.getTypeNames()[0],
                        Filter.INCLUDE, Transaction.AUTO_COMMIT);

                while (writer.hasNext()) {
                    SimpleFeature feat = writer.next();
                    Byte b = (Byte) feat.getAttribute(1);

                    if ((b.byteValue() % 2) == 0) {
                        writer.remove();
                    } else {
                        feat.setAttribute(1, new Byte((byte) -1));
                    }
                }
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }

            FeatureCollection<SimpleFeatureType, SimpleFeature> fc = loadFeatures(sds);

            assertEquals(10, fc.size());

            for (FeatureIterator<SimpleFeature> i = fc.features(); i.hasNext();) {
                assertEquals(-1, ((Byte) i.next().getAttribute(1)).byteValue());
            }
            sds.dispose();
        } catch (Throwable t) {
            if (System.getProperty("os.name").startsWith("Windows")) {
                System.out.println("Ignore " + t
                        + " because you are on windows");

                return;
            } else {
                throw t;
            }
        }
        
    }

    /**
     * Create a test file, then continue removing the first entry until there
     * are no features left.
     */
    public void testRemoveFromFrontAndClose() throws Throwable {
        try {
            IndexedShapefileDataStore sds = createDataStore();

            int idx = loadFeatures(sds).size();

            while (idx > 0) {
                FeatureWriter<SimpleFeatureType, SimpleFeature> writer = null;

                try {
                    writer = sds.getFeatureWriter(sds.getTypeNames()[0],
                            Filter.INCLUDE, Transaction.AUTO_COMMIT);
                    writer.next();
                    writer.remove();
                } finally {
                    if (writer != null) {
                        writer.close();
                        writer = null;
                    }
                }

                assertEquals(--idx, loadFeatures(sds).size());
            }
            sds.dispose();
        } catch (Throwable t) {
            if (System.getProperty("os.name").startsWith("Windows")) {
                System.out.println("Ignore " + t
                        + " because you are on windows");

                return;
            } else {
                throw t;
            }
        }
    }

    /**
     * Create a test file, then continue removing the last entry until there are
     * no features left.
     */
    public void testRemoveFromBackAndClose() throws Throwable {
        try {
            IndexedShapefileDataStore sds = createDataStore();

            int idx = loadFeatures(sds).size();

            while (idx > 0) {
                FeatureWriter<SimpleFeatureType, SimpleFeature> writer = null;

                try {
                    writer = sds.getFeatureWriter(sds.getTypeNames()[0],
                            Filter.INCLUDE, Transaction.AUTO_COMMIT);

                    while (writer.hasNext()) {
                        writer.next();
                    }

                    writer.remove();
                } finally {
                    if (writer != null) {
                        writer.close();
                        writer = null;
                    }
                }

                assertEquals(--idx, loadFeatures(sds).size());
            }
            sds.dispose();
        } catch (Throwable t) {
            if (System.getProperty("os.name").startsWith("Windows")) {
                System.out.println("Ignore " + t
                        + " because you are on windows");

                return;
            } else {
                throw t;
            }
        }
    }

    public void testTestTransaction() throws Exception {
        IndexedShapefileDataStore sds = createDataStore();

        int idx = sds.getCount(Query.ALL);

        FeatureStore<SimpleFeatureType, SimpleFeature> store = (FeatureStore<SimpleFeatureType, SimpleFeature>) sds.getFeatureSource(sds
                .getTypeNames()[0]);

        Transaction transaction = new DefaultTransaction();
        store.setTransaction(transaction);
        SimpleFeature[] newFeatures1 = new SimpleFeature[1];
        SimpleFeature[] newFeatures2 = new SimpleFeature[2];
        GeometryFactory fac = new GeometryFactory();
        newFeatures1[0] = DataUtilities.template(sds.getSchema());
        newFeatures1[0].setDefaultGeometry(fac
                .createPoint(new Coordinate(0, 0)));
        newFeatures2[0] = DataUtilities.template(sds.getSchema());
        newFeatures2[0].setDefaultGeometry(fac
                .createPoint(new Coordinate(0, 0)));
        newFeatures2[1] = DataUtilities.template(sds.getSchema());
        newFeatures2[1].setDefaultGeometry(fac
                .createPoint(new Coordinate(0, 0)));

        store.addFeatures(DataUtilities.collection(newFeatures1));
        store.addFeatures(DataUtilities.collection(newFeatures2));
        transaction.commit();
        transaction.close();
        assertEquals(idx + 3, sds.getCount(Query.ALL));
        sds.dispose();

    }

    private SimpleFeatureType createExampleSchema() {
        SimpleFeatureTypeBuilder build = new SimpleFeatureTypeBuilder();
        build.setName("junk");
        build.add("a", Point.class);
        build.add("b", Byte.class);
        build.add("c", Short.class);
        build.add("d", Double.class);
        build.add("e", Float.class);
        build.add("f", String.class);
        build.add("g", Date.class);
        build.add("h", Boolean.class);
        build.add("i", Number.class);
        build.add("j", Long.class);
        build.add("k", BigDecimal.class);
        build.add("l", BigInteger.class);

        return build.buildFeatureType();
    }

    private FeatureCollection<SimpleFeatureType, SimpleFeature> createFeatureCollection() throws Exception {
        SimpleFeatureType featureType = createExampleSchema();
        SimpleFeatureBuilder build = new SimpleFeatureBuilder(featureType);

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = FeatureCollections.newCollection();
        for (int i = 0, ii = 20; i < ii; i++) {

            build.add(new GeometryFactory().createPoint(new Coordinate(1, -1)));
            build.add(new Byte((byte) i));
            build.add(new Short((short) i));
            build.add(new Double(i));
            build.add(new Float(i));
            build.add(new String(i + " "));
            build.add(new Date(i));
            build.add(new Boolean(true));
            build.add(new Integer(22));
            build.add(new Long(1234567890123456789L));
            build.add(new BigDecimal(new BigInteger(
                    "12345678901234567890123456789"), 2));
            build.add(new BigInteger("12345678901234567890123456789"));

            SimpleFeature feature = build.buildFeature(null);
            features.add(feature);
        }
        return features;
    }

    public void testAttributesWriting() throws Exception {
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = createFeatureCollection();
        File tmpFile = getTempFile();
        tmpFile.createNewFile();

        IndexedShapefileDataStore s = new IndexedShapefileDataStore(tmpFile
                .toURL());
        writeFeatures(s, features);
        s.dispose();
    }

    public void testGeometriesWriting() throws Exception {
        String[] wktResources = new String[] { "point", "multipoint", "line",
                "multiline", "polygon", "multipolygon" };

        for (int i = 0; i < wktResources.length; i++) {
            Geometry geom = readGeometry(wktResources[i]);
            String testName = wktResources[i];

            try {
                runWriteReadTest(geom, false);
                make3D(geom);
                testName += "3d";
                runWriteReadTest(geom, true);
            } catch (Throwable e) {
                throw new Exception("Error in " + testName, e);
            }
        }
    }

    private void make3D(Geometry g) {
        Coordinate[] c = g.getCoordinates();

        for (int i = 0, ii = c.length; i < ii; i++) {
            c[i].z = 42 + i;
        }
    }

    private void writeFeatures(IndexedShapefileDataStore s, FeatureCollection<SimpleFeatureType, SimpleFeature> fc)
            throws Exception {
        s.createSchema(fc.features().next().getFeatureType());

        FeatureWriter<SimpleFeatureType, SimpleFeature> fw = s.getFeatureWriter(s.getTypeNames()[0],
                Transaction.AUTO_COMMIT);
        FeatureIterator<SimpleFeature> it = fc.features();

        while (it.hasNext()) {
            SimpleFeature feature = it.next();
            SimpleFeature newFeature = fw.next();

            newFeature.setAttributes(feature.getAttributes());
            fw.write();
        }

        fw.close();
        assertEquals(20,  s.getFeatureSource().getFeatures().size());
    }

    private void runWriteReadTest(Geometry geom, boolean d3) throws Exception {
        // make features
        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.setName("Junk");
        ftb.add("a", geom.getClass());
        SimpleFeatureType type = ftb.buildFeatureType();

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = FeatureCollections.newCollection();

        for (int i = 0, ii = 20; i < ii; i++) {
            SimpleFeature feature = SimpleFeatureBuilder.build(type,
                    new Object[] { geom.clone() }, null);
            features.add(feature);
        }

        // set up file
        File tmpFile = getTempFile();
        tmpFile.delete();

        // write features
        IndexedShapefileDataStore s = new IndexedShapefileDataStore(tmpFile
                .toURL());
        s.createSchema(type);
        writeFeatures(s, features);

        s.dispose();
        
        // read features
        s = new IndexedShapefileDataStore(tmpFile.toURL());

        FeatureCollection<SimpleFeatureType, SimpleFeature> fc = loadFeatures(s);
        FeatureIterator<SimpleFeature> fci = fc.features();

        // verify
        while (fci.hasNext()) {
            SimpleFeature f = fci.next();
            Geometry fromShape = (Geometry) f.getDefaultGeometry();

            if (fromShape instanceof GeometryCollection) {
                if (!(geom instanceof GeometryCollection)) {
                    fromShape = ((GeometryCollection) fromShape)
                            .getGeometryN(0);
                }
            }

            try {
                Coordinate[] c1 = geom.getCoordinates();
                Coordinate[] c2 = fromShape.getCoordinates();

                for (int cc = 0, ccc = c1.length; cc < ccc; cc++) {
                    if (d3) {
                        assertTrue(c1[cc].equals3D(c2[cc]));
                    } else {
                        assertTrue(c1[cc].equals2D(c2[cc]));
                    }
                }
            } catch (Throwable t) {
                fail("Bogus : " + Arrays.asList(geom.getCoordinates()) + " : "
                        + Arrays.asList(fromShape.getCoordinates()));
            }
        }
        s.dispose();
        tmpFile.delete();
    }

    public void testIndexOutOfDate() throws Exception {
        File shpFile = copyShapefiles(STATE_POP);
        ShpFileType fix = ShpFileType.FIX;
        File fixFile = sibling(shpFile, fix.extension);
        fixFile.delete();
        IndexedShapefileDataStore ds = new IndexedShapefileDataStore(shpFile.toURI().toURL());
        
        assertFalse(ds.needsGeneration(fix));
        long fixMod = fixFile.lastModified();
        shpFile.setLastModified(fixMod+1000);
        assertTrue(ds.needsGeneration(fix));
        fixFile.setLastModified(shpFile.lastModified());
        assertFalse(ds.needsGeneration(fix));
        assertTrue(fixFile.delete());
        assertTrue(ds.needsGeneration(fix));
        ds.dispose();
    }
    
    public static void main(java.lang.String[] args) throws Exception {
        junit.textui.TestRunner.run(suite(IndexedShapefileDataStoreTest.class));
    }
}
