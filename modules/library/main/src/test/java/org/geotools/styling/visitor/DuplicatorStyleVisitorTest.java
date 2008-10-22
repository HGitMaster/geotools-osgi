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
package org.geotools.styling.visitor;

import java.util.Collections;

import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.IllegalFilterException;
import org.geotools.styling.AnchorPoint;
import org.geotools.styling.Displacement;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeConstraint;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.Graphic;
import org.geotools.styling.Halo;
import org.geotools.styling.LabelPlacement;
import org.geotools.styling.LinePlacement;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.styling.UserLayer;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.util.Cloneable;


/**
 * Unit test for DuplicatorStyleVisitor.
 *
 * @author Cory Horner, Refractions Research Inc.
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/test/java/org/geotools/styling/visitor/DuplicatorStyleVisitorTest.java $
 */
public class DuplicatorStyleVisitorTest extends TestCase {
    StyleBuilder sb;
    StyleFactory sf;
    FilterFactory2 ff;
    DuplicatingStyleVisitor visitor;
    
    public DuplicatorStyleVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    	sf = CommonFactoryFinder.getStyleFactory(null);
        ff = CommonFactoryFinder.getFilterFactory2(null);
        sb = new StyleBuilder(sf, ff);
        visitor = new DuplicatingStyleVisitor( sf, ff );
    }
    
    public void testStyleDuplication() throws IllegalFilterException {
    	//create a style
    	Style oldStyle = sb.createStyle("FTSName", sf.createPolygonSymbolizer());
    	oldStyle.getFeatureTypeStyles()[0].setSemanticTypeIdentifiers(new String[] {"simple", "generic:geometry"});
    	//duplicate it
    	oldStyle.accept(visitor);
    	Style newStyle = (Style) visitor.getCopy();
    	
    	//compare it
    	assertNotNull(newStyle);
    	assertEquals(2, newStyle.getFeatureTypeStyles()[0].getSemanticTypeIdentifiers().length);

    	//TODO: actually compare it
    	assertTrue(areStylesEqualByXml(oldStyle, newStyle));
    }
    
    /**
     * Produces an XML representation of a Style.
     * @param style
     * @return
     * @throws TransformerException
     */
    private String styleToXML(final Style style) throws TransformerException
    {
        StyledLayerDescriptor sld = sf.createStyledLayerDescriptor();
        UserLayer layer = sf.createUserLayer();
        layer.setLayerFeatureConstraints(new FeatureTypeConstraint[]{null});
        sld.addStyledLayer(layer);
        layer.addUserStyle(style);

        SLDTransformer styleTransform = new SLDTransformer();
        String xml = styleTransform.transform(sld);

        return xml;
    }
    
    /**
     * Returns whether two Styles have equal XML representations.
     * @param s1
     * @param s2
     * @return
     */
    private boolean areStylesEqualByXml(final Style s1, final Style s2)
    {
        try
        {
            String xmlS1 = styleToXML(s1);
            String xmlS2 = styleToXML(s2);
            
            return xmlS1.equals(xmlS2);
        }
        catch (TransformerException te)
        {
            return false;
        }
    }


    public void testStyle() throws Exception {
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.setFeatureTypeName("feature-type-1");

        FeatureTypeStyle fts2 = fts2();

        Style style = sf.getDefaultStyle();
        style.addFeatureTypeStyle(fts);
        style.addFeatureTypeStyle(fts2);

        style.accept( visitor );        
        Style copy = (Style) visitor.getCopy();
        
        //assertClone(style, clone);
        assertEqualsContract( style, copy );
        
        Style notEq = sf.getDefaultStyle();

        fts2 = fts2();
        notEq.addFeatureTypeStyle(fts2);
        
        assertEqualsContract(copy, notEq, style);
    }

    private FeatureTypeStyle fts2() {
        FeatureTypeStyle fts2 = sf.createFeatureTypeStyle();
        Rule rule = sf.createRule();
        fts2.addRule(rule);
        fts2.setFeatureTypeName("feature-type-2");

        return fts2;
    }

    public void testFeatureTypeStyle() throws Exception {
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.setFeatureTypeName("feature-type");

        Rule rule1;

        rule1 = sf.createRule();
        rule1.setName("rule1");
        rule1.setFilter(ff.id(Collections.singleton(ff.featureId("FID"))));

        Rule rule2 = sf.createRule();
        rule2.setIsElseFilter(true);
        rule2.setName("rule2");
        fts.addRule(rule1);
        fts.addRule(rule2);

        fts.accept( visitor );
        FeatureTypeStyle clone = (FeatureTypeStyle) visitor.getCopy();
        //assertClone(fts, clone);
        assertEqualsContract( fts, clone );
        
        rule1 = sf.createRule();
        rule1.setName("rule1");
        rule1.setFilter(ff.id(Collections.singleton(ff.featureId("FID"))));

        FeatureTypeStyle notEq = sf.createFeatureTypeStyle();
        notEq.setName("fts-not-equal");
        notEq.addRule(rule1);
        assertEqualsContract(clone, notEq, fts);
    }

    public void testRule() throws Exception {
        Symbolizer symb1 = sf.createLineSymbolizer(sf
                .getDefaultStroke(), "geometry");

        Symbolizer symb2 = sf.createPolygonSymbolizer(sf
                .getDefaultStroke(), sf.getDefaultFill(), "shape");

        Rule rule = sf.createRule();
        rule.setSymbolizers(new Symbolizer[] { symb1, symb2 });

        rule.accept(visitor);
        Rule clone = (Rule) visitor.getCopy();
        assertCopy(rule, clone);
        assertEqualsContract(rule, clone);
        
        symb2 = sf.createPolygonSymbolizer(sf
                .getDefaultStroke(), sf.getDefaultFill(), "shape");

        Rule notEq = sf.createRule();
        notEq.setSymbolizers(new Symbolizer[] { symb2 });
        assertEqualsContract(clone, notEq, rule);

        symb1 = sf.createLineSymbolizer(sf.getDefaultStroke(),
                "geometry");
        clone.setSymbolizers(new Symbolizer[] { symb1 });
        assertTrue(!rule.equals(clone));
    }

    public void testPointSymbolizer() throws Exception {
        PointSymbolizer pointSymb = sf.createPointSymbolizer();
        pointSymb.accept(visitor);
        PointSymbolizer clone = (PointSymbolizer) visitor.getCopy();

        assertCopy(pointSymb, clone);
        assertEqualsContract(pointSymb, clone);
        
        PointSymbolizer notEq = sf.getDefaultPointSymbolizer();
        notEq.setGeometryPropertyName("something_else");
        assertEqualsContract(clone, notEq, pointSymb);
    }

    public void testTextSymbolizer() {
        TextSymbolizer textSymb = sf.createTextSymbolizer();
        Expression offset = ff.literal(10);
        textSymb.setLabelPlacement(sf.createLinePlacement(offset));

        textSymb.accept(visitor);
        TextSymbolizer clone = (TextSymbolizer) visitor.getCopy();
        assertCopy(textSymb, clone);
        assertEqualsContract(textSymb, clone);
        
        TextSymbolizer notEq = sf.getDefaultTextSymbolizer();
        Expression ancX = ff.literal(10);
        Expression ancY = ff.literal(10);
        AnchorPoint ancPoint = sf.createAnchorPoint(ancX, ancY);
        LabelPlacement placement = sf.createPointPlacement(ancPoint,
                null, null);
        notEq.setLabelPlacement(placement);
        assertEqualsContract(clone, notEq, textSymb);
    }

    public void testFont() {
        Font font = sf.getDefaultFont();
        Font clone = visitor.copy(font);
        assertCopy(font, clone);
        assertEqualsContract(font, clone);

        Font other = sf.createFont(ff.literal("other"),
                ff.literal("normal"),
                ff.literal("BOLD"),
                ff.literal(12));

        assertEqualsContract(clone, other, font);
    }

    public void testHalo() {
        Halo halo = sf.createHalo(sf.getDefaultFill(),
                ff.literal(10));
        
        halo.accept(visitor);
        Halo clone = (Halo) visitor.getCopy();
        
        assertCopy(halo, clone);

        Halo other = sf.createHalo(sf.getDefaultFill(),
                ff.literal(12));
        assertEqualsContract(clone, other, halo);
    }

    public void testLinePlacement() throws Exception {
        LinePlacement linePlacement = sf.createLinePlacement(ff.literal(12));
        
        linePlacement.accept(visitor);
        LinePlacement clone = (LinePlacement) visitor.getCopy();;
        
        assertCopy(linePlacement, clone);

        LinePlacement other = sf.createLinePlacement(ff.property("NAME"));
        assertEqualsContract(clone, other, linePlacement);
    }

    public void testAnchorPoint() {
        AnchorPoint anchorPoint = sf.createAnchorPoint(ff.literal(1),
                ff.literal(2));
        anchorPoint.accept(visitor);
        
        AnchorPoint clone = (AnchorPoint) visitor.getCopy();
        assertCopy(anchorPoint, clone);

        AnchorPoint other = sf.createAnchorPoint(ff.literal(3), ff
                .literal(4));
        assertEqualsContract(clone, other, anchorPoint);
    }

    public void testDisplacement() {
        Displacement displacement = sf.createDisplacement(ff.literal(1),
                ff.literal(2));
        
        displacement.accept(visitor);
        Displacement clone = (Displacement) visitor.getCopy();
        assertCopy(displacement, clone);

        Displacement other = sf.createDisplacement(ff.literal(3),
                ff.literal(4));
        assertEqualsContract(clone, other, displacement);
    }

    public void testPointPlacement() {
        PointPlacement pointPl = sf.getDefaultPointPlacement();
        
        PointPlacement clone = (PointPlacement) visitor.copy( pointPl );        
        assertCopy(pointPl, clone);

        PointPlacement other = (PointPlacement) ((Cloneable) pointPl).clone();
        other.setRotation(ff.literal(274.0));
        assertEqualsContract(clone, other, pointPl);
    }

    public void testPolygonSymbolizer() {
        try {
            //visitor.setStrict(true);
            PolygonSymbolizer polygonSymb = sf.createPolygonSymbolizer();
            PolygonSymbolizer clone = (PolygonSymbolizer) visitor
                    .copy(polygonSymb);
            assertCopy(polygonSymb, clone);

            PolygonSymbolizer notEq = sf.getDefaultPolygonSymbolizer();
            notEq.setGeometryPropertyName("something_else");

            assertEqualsContract(clone, notEq, polygonSymb);
        } finally {
            visitor.setStrict(false);
        }
    }
    
    public void testLineSymbolizer() {
        LineSymbolizer lineSymb = sf.createLineSymbolizer();
        LineSymbolizer clone = (LineSymbolizer) visitor.copy( lineSymb);
        assertCopy(lineSymb, clone);

        LineSymbolizer notEq = sf.getDefaultLineSymbolizer();
        notEq.setGeometryPropertyName("something_else");
        assertEqualsContract(clone, notEq, lineSymb);
    }

    public void testGraphic() {
        Graphic graphic = sf.getDefaultGraphic();
        graphic.addMark(sf.getDefaultMark());

        Graphic clone = (Graphic) visitor.copy( graphic);
        assertCopy(graphic, clone);
        assertEqualsContract(clone, graphic);
        assertEquals(clone.getSymbols().length, graphic.getSymbols().length);

        Graphic notEq = sf.getDefaultGraphic();
        notEq.setGeometryPropertyName("geomprop");
        assertEqualsContract(clone, notEq, graphic);
    }

    public void testExternalGraphic() {
        ExternalGraphic exGraphic = sf.createExternalGraphic("http://somewhere",
                "image/png");
        ExternalGraphic clone = visitor.copy( exGraphic);
        assertCopy(exGraphic, clone);

        ExternalGraphic notEq = sf.createExternalGraphic("http://somewhereelse",
                "image/jpeg");
        assertEqualsContract(clone, notEq, exGraphic);

        // make sure it works for different format, same url
        ExternalGraphic notEq2 = visitor.copy( clone);
        notEq2.setFormat("image/jpeg");
        assertEqualsContract(clone, notEq2, exGraphic);
    }

    public void testMark() {
        Mark mark = sf.getCircleMark();
        Mark clone = visitor.copy( mark);
        assertCopy(mark, clone);

        Mark notEq = sf.getStarMark();
        assertEqualsContract(clone, notEq, mark);
    }

    public void testFill() {
        Fill fill = sf.getDefaultFill();
        Fill clone = visitor.copy( fill);
        assertCopy(fill, clone);

        Fill notEq = sf.createFill(ff.literal("#FF0000"));
        assertEqualsContract(clone, notEq, fill);
    }

    public void testStroke() {
        Stroke stroke = sf.getDefaultStroke();
        Stroke clone = visitor.copy( stroke );
        assertCopy(stroke, clone);

        Stroke notEq = sf.createStroke(ff.literal("#FF0000"), ff
                .literal(10));
        assertEqualsContract(clone, notEq, stroke);

        // a stroke is a complex object with lots of properties,
        // need more extensive tests here.
        Stroke dashArray = sf.getDefaultStroke();
        dashArray.setDashArray(new float[] { 1.0f, 2.0f, 3.0f });

        Stroke dashArray2 = (Stroke) ((Cloneable)dashArray).clone();
        assertEqualsContract(dashArray, dashArray2);
    }

    private static void assertCopy(Object real, Object clone) {
        assertNotNull("Real was null", real);
        assertNotNull("Clone was null", clone);
        assertTrue("" + real.getClass().getName() + " was not cloned",
            real != clone);
    }

    private static void assertEqualsContract(Object controlEqual,
        Object controlNe, Object test) {
        assertNotNull(controlEqual);
        assertNotNull(controlNe);
        assertNotNull(test);

        // check reflexivity
        assertTrue("Reflexivity test failed", test.equals(test));

        // check symmetric
        assertTrue("Symmetry test failed", controlEqual.equals(test));
        assertTrue("Symmetry test failed", test.equals(controlEqual));
        assertTrue("Symmetry test failed", !test.equals(controlNe));
        assertTrue("Symmetry test failed", !controlNe.equals(test));

        // check transitivity
        assertTrue("Transitivity test failed", !controlEqual.equals(controlNe));
        assertTrue("Transitivity test failed", !test.equals(controlNe));
        assertTrue("Transitivity test failed", !controlNe.equals(controlEqual));
        assertTrue("Transitivity test failed", !controlNe.equals(test));

        // check non-null
        assertTrue("Non-null test failed", !test.equals(null));

        // assertHashcode equality
        int controlEqHash = controlEqual.hashCode();
        int testHash = test.hashCode();
        if( controlEqHash == testHash ){
            System.out.println( "Warning  - Equal objects should return equal hashcodes");
        }
    }

    private static void assertEqualsContract(Object controlEqual, Object test) {
        assertNotNull(controlEqual);
        assertNotNull(test);

        // check reflexivity
        assertTrue("Reflexivity test failed", test.equals(test));

        // check symmetric
        assertTrue("Symmetry test failed", controlEqual.equals(test));
        assertTrue("Symmetry test failed", test.equals(controlEqual));

        // check non-null
        assertTrue("Non-null test failed", !test.equals(null));

        // assertHashcode equality
        int controlEqHash = controlEqual.hashCode();
        int testHash = test.hashCode();
        assertTrue("Equal objects should return equal hashcodes",controlEqHash == testHash);
    }
    
}
