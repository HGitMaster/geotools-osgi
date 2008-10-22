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
package org.geotools.renderer.shape;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.TestData;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileRendererUtil;
import org.geotools.data.shapefile.dbf.IndexedDbaseFileReader;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;

import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.operation.transform.IdentityTransform;
import org.geotools.renderer.RenderListener;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * Tests ShapeRenderer class
 * 
 * @author jeichar
 * @since 2.1.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/2.2.x/ext/shaperenderer/test/org/geotools/renderer/shape/ShapeRendererTest.java $
 */
public class ShapeRendererTest extends TestCase {
    private static final boolean INTERACTIVE = false;

    private static final MathTransform IDENTITY = IdentityTransform.create(2);

    private File shp2;

    private File shx2;

    private File prj2;

    private File dbf2;

    private String typename;

    private File directory;
    
    private SimpleFeature sf;

    protected void setUp() throws Exception {
        org.geotools.util.logging.Logging.getLogger("org.geotools").setLevel(Level.FINE);
        File shp = new File(TestData.url(Rendering2DTest.class, "theme1.shp")
                .getFile());
        File shx = new File(TestData.url(Rendering2DTest.class, "theme1.shx")
                .getFile());
        File prj = new File(TestData.url(Rendering2DTest.class, "theme1.prj")
                .getFile());
        File dbf = new File(TestData.url(Rendering2DTest.class, "theme1.dbf")
                .getFile());

        directory = TestData.file(Rendering2DTest.class, ".");
        
        shp2 = File.createTempFile("theme2", ".shp", directory);
        typename = shp2.getName().substring(0, shp2.getName().lastIndexOf("."));
        shx2 = new File(directory, typename + ".shx");
        prj2 = new File(directory, typename + ".prj");
        dbf2 = new File(directory, typename + ".dbf");

        copy(shp, shp2);
        copy(shx, shx2);
        copy(prj, prj2);
        copy(dbf, dbf2);
        
        // setup a sample feature
        ShapefileDataStore ds = TestUtilites.getDataStore(shp2.getName());
        SimpleFeatureType type = ds.getSchema();
        GeometryFactory gf = new GeometryFactory();
        LineString ls = gf.createLineString(new Coordinate[] {new Coordinate(0,0), new Coordinate(10,10)});
        MultiLineString mls = gf.createMultiLineString(new LineString[] {ls});
        sf = SimpleFeatureBuilder.build( type, new Object[] {mls, new Integer(0), "Hi"}, "newFeature");
    }

    protected void tearDown() throws Exception {
        dbf2.deleteOnExit();
        shx2.deleteOnExit();
        shp2.deleteOnExit();
        prj2.deleteOnExit();
        File fix=new File( directory, typename+".fix");
        File qix=new File( directory, typename+".qix");
        
        if( shp2.exists() && !shp2.delete() )
            System.out.println("failed to delete: "+shp2.getAbsolutePath());
        if( shx2.exists() && !shx2.delete() )
            System.out.println("failed to delete: "+shx2.getAbsolutePath());

        if( prj2.exists() && !prj2.delete()) 
            System.out.println("failed to delete: "+prj2.getAbsolutePath());

        if( dbf2.exists() && !dbf2.delete() )
            System.out.println("failed to delete: "+dbf2.getAbsolutePath());
        
        if( fix.exists() && !fix.delete() ){
            fix.deleteOnExit();
            System.out.println("failed to delete: "+fix.getAbsolutePath());
        }
        if( qix.exists() && !qix.delete() ){
            qix.deleteOnExit();
            System.out.println("failed to delete: "+qix.getAbsolutePath());
        }
    }

    void copy(File src, File dst) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst, false);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }
        }
    }

    public void testCreateFeature() throws Exception {
        ShapefileRenderer renderer = new ShapefileRenderer(null);
        Style style = LabelingTest.loadStyle("LineStyle.sld");
        ShapefileDataStore ds = TestUtilites.getDataStore(shp2.getName());
        IndexedDbaseFileReader reader = ShapefileRendererUtil
                        .getDBFReader(ds);
        renderer.dbfheader = reader.getHeader();
        SimpleFeatureType type = renderer.createFeatureType(null, style, ds);
        assertEquals(2, type.getAttributeCount());
        assertEquals("NAME", type.getDescriptor(0).getLocalName());
        Envelope bounds = ds.getFeatureSource().getBounds();
        ShapefileReader shpReader = ShapefileRendererUtil
                        .getShpReader(ds, bounds, 
                                new Rectangle(0,0,(int)bounds.getWidth(), (int)bounds.getHeight()),
                                IDENTITY, false, false);
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        SimpleFeature feature = renderer.createFeature(builder, shpReader.nextRecord(), reader, "id");
        shpReader.close();
        reader.close();
        
        assertEquals("id", feature.getID());
        assertEquals("dave street", feature.getAttribute(0));
    }

    public void testRemoveTransaction() throws Exception {
        ShapefileDataStore ds = TestUtilites.getDataStore(shp2.getName());
        System.out.println("Count: " + ds.getFeatureSource().getCount(Query.ALL));
        Style st = TestUtilites.createTestStyle(null, typename);
        final FeatureStore<SimpleFeatureType, SimpleFeature> store;
        store = (FeatureStore<SimpleFeatureType, SimpleFeature>) ds.getFeatureSource();
        Transaction t = new DefaultTransaction();
        store.setTransaction(t);
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = store.getFeatures();
        FeatureIterator<SimpleFeature> iter = collection.features();
        FidFilter createFidFilter = TestUtilites.filterFactory.createFidFilter(iter
                        .next().getID());
        collection.close(iter);
        store.removeFeatures(createFidFilter);

        MapContext context = new DefaultMapContext();
        context.addLayer(store, st);
        ShapefileRenderer renderer = new ShapefileRenderer(context);
        TestUtilites.CountingRenderListener listener = new TestUtilites.CountingRenderListener();
        renderer.addRenderListener(listener);
        Envelope env = context.getLayerBounds();
        int boundary = 7;
        TestUtilites.INTERACTIVE = INTERACTIVE;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary,
                env.getMinY() - boundary, env.getMaxY() + boundary);
        TestUtilites.showRender("testTransaction", renderer, 2000, env);
        assertEquals(2, listener.count);
        t.commit();

        collection = store.getFeatures();
        iter = collection.features();
        final SimpleFeature feature = iter.next();
        collection.close(iter);

        // now add a new feature new fid should be theme2.4 remove it and assure
        // that it is not rendered
        SimpleFeatureType type = store.getSchema();
        store.addFeatures(DataUtilities.collection(new SimpleFeature[] { sf } )); //$NON-NLS-1$
        t.commit();
        System.out.println("Count: " + ds.getFeatureSource().getCount(Query.ALL));
        listener.count = 0;
        TestUtilites.showRender("testTransaction", renderer, 2000, env);
        assertEquals(3, listener.count);

        iter = store.getFeatures().features();
        SimpleFeature last = null;
        while (iter.hasNext()) {
            last = iter.next();
        }
        iter.close();

        store.removeFeatures(TestUtilites.filterFactory.createFidFilter(last
                .getID()));

        listener.count = 0;
        TestUtilites.showRender("testTransaction", renderer, 2000, env);
        assertEquals(2, listener.count);

    }

    public void testAddTransaction() throws Exception {
        final ShapefileDataStore ds = TestUtilites.getDataStore(shp2.getName());
        Style st = TestUtilites.createTestStyle(null, typename);
        FeatureStore<SimpleFeatureType, SimpleFeature> store = (FeatureStore<SimpleFeatureType, SimpleFeature>) ds.getFeatureSource();
        Transaction t = new DefaultTransaction();
        store.setTransaction(t);
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = store.getFeatures();
        FeatureIterator<SimpleFeature> iter = collection.features();
        final SimpleFeature feature = iter.next();
        collection.close(iter);

        SimpleFeatureType type = ds.getSchema();
        store.addFeatures(DataUtilities.collection(sf));

        MapContext context = new DefaultMapContext();
        context.addLayer(store, st);
        ShapefileRenderer renderer = new ShapefileRenderer(context);
        TestUtilites.CountingRenderListener listener = new TestUtilites.CountingRenderListener();
        renderer.addRenderListener(listener);
        Envelope env = context.getLayerBounds();
        int boundary = 7;
        TestUtilites.INTERACTIVE = INTERACTIVE;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary,
                env.getMinY() - boundary, env.getMaxY() + boundary);
        TestUtilites.showRender("testTransaction", renderer, 2000, env);

        assertEquals(4, listener.count);
    }

    public void testModifyTransaction() throws Exception {
        ShapefileDataStore ds = TestUtilites.getDataStore(shp2.getName());
        Style st = TestUtilites.createTestStyle(null, typename);
        FeatureStore<SimpleFeatureType, SimpleFeature> store = (FeatureStore<SimpleFeatureType, SimpleFeature>) ds.getFeatureSource();
        Transaction t = new DefaultTransaction();
        store.setTransaction(t);
        store.modifyFeatures(ds.getSchema().getDescriptor("NAME"), "bleep",
                Filter.NONE);

        MapContext context = new DefaultMapContext();
        context.addLayer(store, st);
        ShapefileRenderer renderer = new ShapefileRenderer(context);
        TestUtilites.CountingRenderListener listener = new TestUtilites.CountingRenderListener();
        renderer.addRenderListener(listener);
        renderer.addRenderListener(new RenderListener() {

            public void featureRenderer(SimpleFeature feature) {
                assertEquals("bleep", feature.getAttribute("NAME"));
            }

            public void errorOccurred(Exception e) {
                assertFalse(true);
            }

        });
        Envelope env = context.getLayerBounds();
        int boundary = 7;
        TestUtilites.INTERACTIVE = INTERACTIVE;
        env = new Envelope(env.getMinX() - boundary, env.getMaxX() + boundary,
                env.getMinY() - boundary, env.getMaxY() + boundary);
        TestUtilites.showRender("testTransaction", renderer, 2000, env);

        assertEquals(3, listener.count);
    }

}
