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
package org.geotools.styling;

import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.IllegalFilterException;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

import com.vividsolutions.jts.geom.LineString;

/**
 * Tests style cloning
 * 
 * @author Sean Geoghegan
 * @source $URL:
 *         http://svn.geotools.org/trunk/modules/library/main/src/test/java
 *         /org/geotools/styling/StyleAttributeExtractorTest.java $
 * @deprecated This one has been replaced by the same test in the renderer
 *             module
 */
public class StyleAttributeExtractorTest extends TestCase {
    private StyleFactory styleFactory;

    private FilterFactory filterFactory;

    private SimpleFeatureType testSchema = null;

    /**
     * Constructor for StyleCloneTest.
     * 
     * @param arg0
     */
    public StyleAttributeExtractorTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        styleFactory = StyleFactoryFinder.createStyleFactory();
        filterFactory = CommonFactoryFinder.getFilterFactory(null);

        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.add("testGeometry", LineString.class);
        ftb.add("testBoolean", Boolean.class);
        ftb.add("testCharacter", Character.class);
        ftb.add("testByte", Byte.class);
        ftb.add("testShort", Short.class);
        ftb.add("testInteger", Integer.class);
        ftb.add("testLong", Long.class);
        ftb.add("testFloat", Float.class);
        ftb.add("testDouble", Double.class);
        ftb.add("testString", String.class);
        ftb.add("testZeroDouble", Double.class);
        ftb.setName("testSchema");
        testSchema = ftb.buildFeatureType();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        styleFactory = null;
    }

    private void assertAttributeName(Style s, String name) {
        assertAttributeName(s, new String[] { name });
    }

    private void assertAttributeName(Style style, String[] names) {
        StyleAttributeExtractor sae = new StyleAttributeExtractor();
        style.accept(sae);

        Set attNames = sae.getAttributeNameSet();

        assertNotNull(attNames);
        assertEquals(names.length, attNames.size());

        for (int i = 0; i < names.length; i++) {
            assertTrue(attNames.contains(names[i]));
        }
    }

    private Style createStyle() {
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle();
        Rule rule1 = styleFactory.createRule();
        fts.addRule(rule1);

        Rule rule2 = styleFactory.createRule();
        fts.addRule(rule2);
        fts.setFeatureTypeName("feature-type-1");

        FeatureTypeStyle fts2 = styleFactory.createFeatureTypeStyle();
        fts2.setFeatureTypeName("feature-type-2");

        Style style = styleFactory.getDefaultStyle();
        style.addFeatureTypeStyle(fts);
        style.addFeatureTypeStyle(fts2);

        return style;
    }

    public void testStyle() throws Exception {
        Style s = createStyle();
        assertAttributeName(s, new String[0]);
    }

    public void testRule() throws Exception {
        Symbolizer symb1 = styleFactory.createLineSymbolizer(styleFactory.getDefaultStroke(),
                "geometry");
        Symbolizer symb2 = styleFactory.createPolygonSymbolizer(styleFactory.getDefaultStroke(),
                styleFactory.getDefaultFill(), "shape");
        Rule rule = styleFactory.createRule();
        rule.setSymbolizers(new Symbolizer[] { symb1, symb2 });

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].addRule(rule);
        assertAttributeName(s, new String[] { "geometry", "shape" });

        Filter f = filterFactory.equals(filterFactory.property("testLong"), filterFactory
                .literal(10.0));
        rule.setFilter(f);

        assertAttributeName(s, new String[] { "geometry", "shape", "testLong" });
    }

    public void testPointSymbolizer() throws Exception {
        PointSymbolizer pointSymb = styleFactory.createPointSymbolizer();
        ExternalGraphic eg = styleFactory.createExternalGraphic("http://www.test.com", "image/png");
        Mark mark = styleFactory.createMark();
        Stroke stroke = styleFactory.getDefaultStroke();
        stroke.setWidth(filterFactory.property("testInteger"));
        mark.setStroke(stroke);
        mark.setWellKnownName(filterFactory.property("testString"));

        Expression opacity = filterFactory.property("testLong");
        Expression rotation = filterFactory.property("testDouble");
        Expression size = filterFactory.property("testFloat");
        Graphic g = styleFactory.createGraphic(new ExternalGraphic[] { eg }, new Mark[] { mark },
                null, opacity, rotation, size);
        pointSymb.setGraphic(g);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { pointSymb });
        assertAttributeName(s, new String[] { "testInteger", "testLong", "testDouble", "testFloat",
                "testString" });

        pointSymb.setGeometryPropertyName("testGeometry");
        assertAttributeName(s, new String[] { "testInteger", "testLong", "testDouble", "testFloat",
                "testString", "testGeometry" });
    }

    public void testTextSymbolizer() throws Exception {
        TextSymbolizer textSymb = styleFactory.createTextSymbolizer();
        Expression offset = filterFactory.property("testInteger");
        Expression label = filterFactory.property("testString");
        textSymb.setLabelPlacement(styleFactory.createLinePlacement(offset));
        textSymb.setLabel(label);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { textSymb });
        assertAttributeName(s, new String[] { "testInteger", "testString" });

        Expression ancX = filterFactory.property("testFloat");
        Expression ancY = filterFactory.property("testDouble");
        AnchorPoint ancPoint = styleFactory.createAnchorPoint(ancX, ancY);
        LabelPlacement placement = styleFactory.createPointPlacement(ancPoint, null, null);
        textSymb.setLabelPlacement(placement);

        assertAttributeName(s, new String[] { "testFloat", "testDouble", "testString" });
    }

    public void testFont() throws Exception {
        Font font = styleFactory.createFont(filterFactory.property("testString"), filterFactory
                .property("testString2"), filterFactory.property("testLong"), filterFactory
                .property("testBoolean"));

        TextSymbolizer textSymb = styleFactory.createTextSymbolizer();
        Expression offset = filterFactory.property("testFloat");
        Expression label = filterFactory.property("testByte");
        textSymb.setLabelPlacement(styleFactory.createLinePlacement(offset));
        textSymb.setLabel(label);
        textSymb.setFonts(new Font[] { font });

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { textSymb });
        assertAttributeName(s, new String[] { "testString", "testString2", "testLong",
                "testBoolean", "testFloat", "testByte" });
    }

    public void testHalo() throws Exception {
        Fill fill = styleFactory.getDefaultFill();
        fill.setColor(filterFactory.property("testString"));

        Expression radius = filterFactory.property("testLong");
        Halo halo = styleFactory.createHalo(fill, radius);
        TextSymbolizer textSymb = styleFactory.createTextSymbolizer();
        textSymb.setHalo(halo);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { textSymb });
        assertAttributeName(s, new String[] { "testString", "testLong" });
    }

    public void testLinePlacement() throws Exception {
        LinePlacement linePlacement = styleFactory.createLinePlacement(filterFactory
                .property("testLong"));
        TextSymbolizer textSymb = styleFactory.createTextSymbolizer();
        textSymb.setLabelPlacement(linePlacement);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { textSymb });
        assertAttributeName(s, new String[] { "testLong" });
    }

    public void testPointPlacement() throws Exception {
        PointPlacement pp = styleFactory.getDefaultPointPlacement();

        Expression x = filterFactory.property("testLong");
        Expression y = filterFactory.property("testInteger");
        AnchorPoint ap = styleFactory.createAnchorPoint(x, y);

        Expression dx = filterFactory.property("testFloat");
        Expression dy = filterFactory.property("testDouble");
        Displacement displacement = styleFactory.createDisplacement(dx, dy);

        pp.setAnchorPoint(ap);
        pp.setDisplacement(displacement);
        pp.setRotation(filterFactory.property("testFloat"));

        TextSymbolizer textSymb = styleFactory.createTextSymbolizer();
        textSymb.setLabelPlacement(pp);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { textSymb });
        assertAttributeName(s,
                new String[] { "testLong", "testInteger", "testFloat", "testDouble" });
    }

    public void testPolygonSymbolizer() throws Exception {
        PolygonSymbolizer ps = styleFactory.createPolygonSymbolizer();
        Stroke stroke = styleFactory.getDefaultStroke();
        stroke.setColor(filterFactory.property("testString"));

        Fill fill = styleFactory.getDefaultFill();
        fill.setOpacity(filterFactory.property("testDouble"));
        ps.setStroke(stroke);
        ps.setFill(fill);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { ps });
        assertAttributeName(s, new String[] { "testString", "testDouble" });
    }

    public void testLineSymbolizer() throws IllegalFilterException {
        LineSymbolizer ls = styleFactory.createLineSymbolizer();
        Stroke stroke = styleFactory.getDefaultStroke();
        stroke.setColor(filterFactory.property("testString"));
        ls.setStroke(stroke);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { ls });
        assertAttributeName(s, new String[] { "testString" });
    }

    public void testFill() throws IllegalFilterException {
        Fill fill = styleFactory.getDefaultFill();
        fill.setBackgroundColor(filterFactory.property("testString"));
        fill.setColor(filterFactory.property("testString2"));

        Mark mark = styleFactory.createMark();
        Expression le = filterFactory.literal(1);
        Expression rot = filterFactory.property("testFloat");
        Graphic graphic = styleFactory.createGraphic(null, new Mark[] { mark }, null, le, le, rot);
        fill.setGraphicFill(graphic);

        PolygonSymbolizer ps = styleFactory.getDefaultPolygonSymbolizer();
        ps.setFill(fill);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { ps });
        assertAttributeName(s, new String[] { "testString", "testString2", "testFloat" });
    }

    public void testStroke() throws IllegalFilterException {
        Stroke stroke = styleFactory.getDefaultStroke();
        stroke.setColor(filterFactory.property("testString2"));
        stroke.setDashOffset(filterFactory.property("testString"));

        Mark mark = styleFactory.createMark();
        Expression le = filterFactory.literal(1);
        Expression rot = filterFactory.property("testFloat");
        Graphic graphic = styleFactory.createGraphic(null, new Mark[] { mark }, null, le, le, rot);
        stroke.setGraphicFill(graphic);

        LineSymbolizer ls = styleFactory.getDefaultLineSymbolizer();
        ls.setStroke(stroke);

        Style s = createStyle();
        s.getFeatureTypeStyles()[0].getRules()[0].setSymbolizers(new Symbolizer[] { ls });
        assertAttributeName(s, new String[] { "testString", "testString2", "testFloat" });
    }

    /**
     * Main for test runner.
     * 
     * @param args
     *            DOCUMENT ME!
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Required suite builder.
     * 
     * @return A test suite for this unit test.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(StyleAttributeExtractorTest.class);

        return suite;
    }
}
