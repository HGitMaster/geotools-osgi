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
package org.geotools.renderer.lite;

import static org.easymock.EasyMock.*;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Iterator;

import junit.framework.TestCase;

import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.RenderListener;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * Test the inner workings of StreamingRenderer.
 * <p>
 * Rendering is a pretty high level concept
 * @author Jody
 */
public class StreamingRendererTest extends TestCase {

    private SimpleFeatureType testFeatureType;
    private GeometryFactory gf = new GeometryFactory();
    protected int errors;
    protected int features;
    protected void setUp() throws Exception {
        super.setUp();

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Lines");
        builder.add("geom", LineString.class, DefaultGeographicCRS.WGS84);
        testFeatureType = builder.buildFeatureType();
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> createLineCollection() throws Exception {
        FeatureCollection<SimpleFeatureType, SimpleFeature> fc = FeatureCollections.newCollection();
        fc.add(createLine(-177, 0, -177, 10));
        fc.add(createLine(-177, 0, -200, 0));
        fc.add(createLine(-177, 0, -177, 100));
        return fc;
    }

    private SimpleFeature createLine(double x1, double y1, double x2, double y2) {
        Coordinate[] coords = new Coordinate[] { new Coordinate(x1, y1), new Coordinate(x2, y2) };
        return SimpleFeatureBuilder.build(testFeatureType, new Object[] { gf.createLineString(coords) }, null);
    }

    private Style createLineStyle() {
        StyleBuilder sb = new StyleBuilder();
        return sb.createStyle(sb.createLineSymbolizer());
    }

    public void testRenderStuff() throws Exception {
        // build map context
        MapContext mapContext = new DefaultMapContext(DefaultGeographicCRS.WGS84);
        mapContext.addLayer(createLineCollection(), createLineStyle());

        // build projected envelope to work with (small one around the area of
        // validity of utm zone 1, which being a Gauss projection is a vertical 
        // slice parallel to the central meridian, -177°)
        ReferencedEnvelope reWgs = new ReferencedEnvelope(new Envelope(-180,
                -170, 20, 40), DefaultGeographicCRS.WGS84);
        CoordinateReferenceSystem utm1N = CRS.decode("EPSG:32601");
        System.out.println(CRS.getGeographicBoundingBox(utm1N));
        ReferencedEnvelope reUtm = reWgs.transform(utm1N, true);

        BufferedImage image = new BufferedImage(200, 200,BufferedImage.TYPE_4BYTE_ABGR);

        // setup the renderer and listen for errors
        StreamingRenderer sr = new StreamingRenderer();
        sr.setContext(mapContext);
        sr.addRenderListener(new RenderListener() {
            public void featureRenderer(SimpleFeature feature) {
                features++;
            }
            public void errorOccurred(Exception e) {
                errors++;
            }
        });
        errors = 0;
        features = 0;
        sr.paint((Graphics2D) image.getGraphics(), new Rectangle(200, 200),reUtm);
        
        // we should get two errors since there are two features that cannot be
        // projected but the renderer itself should not throw exceptions
        assertTrue( features > 0 );
    }

    public void testInfiniteLoopAvoidance() throws Exception {
        final Exception sentinel = new RuntimeException("This is the one that should be thrown in hasNext()");
        
        // setup the mock necessary to have the renderer hit into the exception in hasNext()
        Iterator it2 = createNiceMock(Iterator.class);
        expect(it2.hasNext()).andThrow(sentinel).anyTimes();
        replay(it2);
        
        FeatureCollection fc = createNiceMock(FeatureCollection.class);
        expect(fc.iterator()).andReturn(it2);
        expect(fc.size()).andReturn(200);
        expect(fc.getSchema()).andReturn(testFeatureType).anyTimes();
        replay(fc);
        
        FeatureSource fs = createNiceMock(FeatureSource.class);
        expect(fs.getFeatures((Query) anyObject())).andReturn(fc);
        expect(fs.getSchema()).andReturn(testFeatureType).anyTimes();
        expect(fs.getSupportedHints()).andReturn(new HashSet());
        replay(fs);
        
        // build map context
        MapContext mapContext = new DefaultMapContext(DefaultGeographicCRS.WGS84);
        mapContext.addLayer(fs, createLineStyle());
        
        // setup the renderer and listen for errors
        final StreamingRenderer sr = new StreamingRenderer();
        sr.setContext(mapContext);
        sr.addRenderListener(new RenderListener() {
            public void featureRenderer(SimpleFeature feature) {
                features++;
            }
            public void errorOccurred(Exception e) {
                errors++;
                
                if(errors > 2) {
                    // we dont' want to block the loop in case of regression on this bug
                    sr.stopRendering();
                }

                // but we want to make sure we're getting
                Throwable t = e;
                while(t != sentinel && t.getCause() != null)
                    t = t.getCause();
                assertSame(sentinel, t);
            }
        });
        errors = 0;
        features = 0;
        BufferedImage image = new BufferedImage(200, 200,BufferedImage.TYPE_4BYTE_ABGR);
        ReferencedEnvelope reWgs = new ReferencedEnvelope(new Envelope(-180,
                -170, 20, 40), DefaultGeographicCRS.WGS84);
        sr.paint((Graphics2D) image.getGraphics(), new Rectangle(200, 200),reWgs);
        
        // we should get two errors since there are two features that cannot be
        // projected but the renderer itself should not throw exceptions
        assertEquals(0, features);
        assertEquals(1, errors);
    }
}
