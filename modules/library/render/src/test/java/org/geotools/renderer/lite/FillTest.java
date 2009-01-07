package org.geotools.renderer.lite;

import java.awt.RenderingHints;
import java.io.File;

import junit.framework.TestCase;

import org.geotools.data.FeatureSource;
import org.geotools.data.property.PropertyDataStore;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.Style;
import org.geotools.test.TestData;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import static java.awt.RenderingHints.*;

public class FillTest extends TestCase {
    private static final long TIME = 2000;
    FeatureSource<SimpleFeatureType, SimpleFeature> fs;
    ReferencedEnvelope bounds;

    @Override
    protected void setUp() throws Exception {
        File property = new File(TestData.getResource(this, "square.properties").toURI());
        PropertyDataStore ds = new PropertyDataStore(property.getParentFile());
        fs = ds.getFeatureSource("square");
        bounds = fs.getBounds();
        bounds.expandBy(0.2, 0.2);
        
        System.setProperty("org.geotools.test.interactive", "true");
        
    }
    
    public void testSolidFill() throws Exception {
        Style style = RendererBaseTest.loadStyle(this, "fillSolid.sld");
        
        DefaultMapContext mc = new DefaultMapContext(DefaultGeographicCRS.WGS84);
        mc.addLayer(fs, style);
        
        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setContext(mc);
        
        RendererBaseTest.showRender("SolidFill", renderer, TIME, bounds);
    }

    public void testCrossFill() throws Exception {
        Style style = RendererBaseTest.loadStyle(this, "fillCross.sld");
        
        DefaultMapContext mc = new DefaultMapContext(DefaultGeographicCRS.WGS84);
        mc.addLayer(fs, style);
        
        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setContext(mc);
        renderer.setJava2DHints(new RenderingHints(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON));
        
        RendererBaseTest.showRender("CrossFill", renderer, TIME, bounds);
    }
    
    public void testCircleFill() throws Exception {
        Style style = RendererBaseTest.loadStyle(this, "fillCircle.sld");
        
        DefaultMapContext mc = new DefaultMapContext(DefaultGeographicCRS.WGS84);
        mc.addLayer(fs, style);
        
        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setContext(mc);
        renderer.setJava2DHints(new RenderingHints(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON));
        
        RendererBaseTest.showRender("CircleFill", renderer, TIME, bounds);
    }
    
    public void testSlash() throws Exception {
        Style style = RendererBaseTest.loadStyle(this, "fillSlash.sld");
        
        DefaultMapContext mc = new DefaultMapContext(DefaultGeographicCRS.WGS84);
        mc.addLayer(fs, style);
        
        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setContext(mc);
        renderer.setJava2DHints(new RenderingHints(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON));
        
        RendererBaseTest.showRender("SlashFill", renderer, TIME, bounds);
    }
    
    public void testImageFill() throws Exception {
        Style style = RendererBaseTest.loadStyle(this, "fillImage.sld");
        
        DefaultMapContext mc = new DefaultMapContext(DefaultGeographicCRS.WGS84);
        mc.addLayer(fs, style);
        
        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setContext(mc);
        
        RendererBaseTest.showRender("ImageFill", renderer, TIME, bounds);
    }
}
