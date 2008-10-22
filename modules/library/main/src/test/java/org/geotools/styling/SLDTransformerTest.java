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

import java.awt.Color;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.style.GraphicalSymbol;
import org.opengis.style.Rule;
import org.opengis.style.Symbolizer;

/**
 * This test case captures specific problems encountered with the SLDTransformer code.
 * <p>
 * Please note that SLDTransformer is specifically targeted at SLD 1.0; for new code you should be
 * using the SLD 1.0 (or SE 1.1) xml-xsd bindings.
 * </p>
 * 
 * @author Jody
 */
public class SLDTransformerTest extends TestCase {
    static StyleFactory2 sf = (StyleFactory2) CommonFactoryFinder.getStyleFactory(null);

    static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

    static SLDTransformer transformer;

    protected void setUp() throws Exception {
        transformer = new SLDTransformer();
        transformer.setIndentation(4);
    }

    /**
     * This problem is reported from uDig 1.2, we are trying to save a RasterSymbolizer (used to
     * record the opacity of a raster layer) out to an SLD file for safe keeping.
     */
    public void testEncodingRasterSymbolizer() throws Exception {
        RasterSymbolizer defaultRasterSymbolizer = sf.createRasterSymbolizer();
        String xmlFragment = transformer.transform(defaultRasterSymbolizer);
        assertNotNull(xmlFragment);

        RasterSymbolizer opacityRasterSymbolizer = sf.createRasterSymbolizer();
        opacityRasterSymbolizer.setOpacity(ff.literal(1.0));

        xmlFragment = transformer.transform(opacityRasterSymbolizer);
        assertNotNull(xmlFragment);

        SLDParser parser = new SLDParser(sf);
        parser.setInput(new StringReader(xmlFragment));
        Object out = parser.parseSLD();
        assertNotNull(out);
    }

    /**
     * Now that we have uDig 1.2 handling opacity we can start look at something more exciting - a
     * complete style object.
     */
    public void testEncodingStyle() throws Exception {

        // simple default raster symbolizer
        RasterSymbolizer defaultRasterSymbolizer = sf.createRasterSymbolizer();
        String xmlFragment = transformer.transform(defaultRasterSymbolizer);
        assertNotNull(xmlFragment);

        // more complex raster symbolizer
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(GeoTools.getDefaultHints());
        StyleBuilder styleBuilder = new StyleBuilder(styleFactory);

        RasterSymbolizer rasterSymbolizer = styleFactory.createRasterSymbolizer();

        // set opacity
        rasterSymbolizer.setOpacity((Expression) CommonFactoryFinder.getFilterFactory(
                GeoTools.getDefaultHints()).literal(0.25));

        // set channel selection
        ChannelSelectionImpl csi = new ChannelSelectionImpl();
        // red
        SelectedChannelTypeImpl redChannel = new SelectedChannelTypeImpl();
        redChannel.setChannelName("1");
        ContrastEnhancementImpl rcei = new ContrastEnhancementImpl();
        rcei.setHistogram();
        redChannel.setContrastEnhancement(rcei);

        // green
        SelectedChannelTypeImpl greenChannel = new SelectedChannelTypeImpl();
        greenChannel.setChannelName("4");
        ContrastEnhancementImpl gcei = new ContrastEnhancementImpl();
        gcei.setGammaValue(ff.literal(2.5));
        greenChannel.setContrastEnhancement(gcei);

        // blue
        SelectedChannelTypeImpl blueChannel = new SelectedChannelTypeImpl();
        blueChannel.setChannelName("2");
        ContrastEnhancementImpl bcei = new ContrastEnhancementImpl();
        bcei.setNormalize();
        blueChannel.setContrastEnhancement(bcei);

        csi.setRGBChannels(redChannel, greenChannel, blueChannel);
        rasterSymbolizer.setChannelSelection(csi);

        Style style = styleBuilder.createStyle(rasterSymbolizer);
        style.setName("simpleStyle");
        // style.setAbstract("Hello World");

        NamedLayer layer = styleFactory.createNamedLayer();
        layer.addStyle(style);

        StyledLayerDescriptor sld = styleFactory.createStyledLayerDescriptor();
        sld.addStyledLayer(layer);

        xmlFragment = transformer.transform(sld);
        // System.out.println(xmlFragment);

        assertNotNull(xmlFragment);
        SLDParser parser = new SLDParser(sf);
        parser.setInput(new StringReader(xmlFragment));
        Style[] stuff = parser.readXML();
        Style out = stuff[0];
        assertNotNull(out);
        assertEquals(0.25, SLD.rasterOpacity(out));
    }

    /**
     * This is a problem reported from uDig 1.2; we are trying to save a LineSymbolizer (and then
     * restore it) and the stroke is comming back black and with width 1 all the time.
     * 
     * @throws Exception
     */
    public void testStroke() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><sld:UserStyle xmlns=\"http://www.opengis.net/sld\" xmlns:sld=\"http://www.opengis.net/sld\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\"><sld:Name>Default Styler</sld:Name><sld:Title>Default Styler</sld:Title><sld:FeatureTypeStyle><sld:Name>simple</sld:Name><sld:Title>title</sld:Title><sld:Abstract>abstract</sld:Abstract><sld:FeatureTypeName>Feature</sld:FeatureTypeName><sld:SemanticTypeIdentifier>generic:geometry</sld:SemanticTypeIdentifier><sld:SemanticTypeIdentifier>simple</sld:SemanticTypeIdentifier><sld:Rule><sld:Title>title</sld:Title><sld:Abstract>abstract</sld:Abstract><sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator><sld:LineSymbolizer><sld:Stroke><sld:CssParameter name=\"stroke\"><ogc:Literal>#0000FF</ogc:Literal></sld:CssParameter><sld:CssParameter name=\"stroke-linecap\"><ogc:Literal>butt</ogc:Literal></sld:CssParameter><sld:CssParameter name=\"stroke-linejoin\"><ogc:Literal>miter</ogc:Literal></sld:CssParameter><sld:CssParameter name=\"stroke-opacity\"><ogc:Literal>1.0</ogc:Literal></sld:CssParameter><sld:CssParameter name=\"stroke-width\"><ogc:Literal>2.0</ogc:Literal></sld:CssParameter><sld:CssParameter name=\"stroke-dashoffset\"><ogc:Literal>0.0</ogc:Literal></sld:CssParameter></sld:Stroke></sld:LineSymbolizer></sld:Rule></sld:FeatureTypeStyle></sld:UserStyle>";
        StringReader reader = new StringReader(xml);
        SLDParser sldParser = new SLDParser(sf, reader);

        Style[] parsed = sldParser.readXML();
        assertNotNull("parsed xml", parsed);
        assertTrue("parsed xml into style", parsed.length > 0);

        Style style = parsed[0];
        assertNotNull(style);
        Rule rule = style.featureTypeStyles().get(0).rules().get(0);
        LineSymbolizer lineSymbolize = (LineSymbolizer) rule.symbolizers().get(0);
        Stroke stroke = lineSymbolize.getStroke();

        Expression color = stroke.getColor();
        Color value = color.evaluate(null, Color.class);
        assertNotNull("color", value);
        assertEquals("blue", Color.BLUE, value);
        assertEquals("expected width", 2, (int) stroke.getWidth().evaluate(null, Integer.class));
    }

    /**
     * SLD Fragment reported to produce error on user list - no related Jira.
     * @throws Exception
     */
    public void testTextSymbolizerLabelPalcement() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
            +"<StyledLayerDescriptor version=\"1.0.0\" "
            +"              xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd\" "
            +"              xmlns=\"http://www.opengis.net/sld\" "
            +"              xmlns:ogc=\"http://www.opengis.net/ogc\" "
            +"              xmlns:xlink=\"http://www.w3.org/1999/xlink\" "
            +"              xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
            +"      <NamedLayer>"
            +"              <Name>Default Line</Name>"
            +"              <UserStyle>"
            +"                      <Title>A boring default style</Title>"
            +"                      <Abstract>A sample style that just prints out a blue line</Abstract>"
            +"                              <FeatureTypeStyle>"
            +"                              <Rule>"
            +"                                      <Name>Rule 1</Name>"
            +"                                      <Title>Blue Line</Title>"
            +"                                      <Abstract>A blue line with a 1 pixel width</Abstract>"
            +"                                      <LineSymbolizer>"
            +"                                              <Stroke>"
            +"                                                      <CssParameter name=\"stroke\">#0000ff</CssParameter>"
            +"                                              </Stroke>"
            +"                                      </LineSymbolizer>"
            +"                              </Rule>"
            +"                              <Rule>"
            +"                              <TextSymbolizer>"
            +"                <Label><ogc:PropertyName>name</ogc:PropertyName></Label>"
            +"                <Font>"
            +"                    <CssParameter name=\"font-family\">Arial</CssParameter>"
            +"                    <CssParameter name=\"font-style\">normal</CssParameter>"
            +"                    <CssParameter name=\"font-size\">12</CssParameter>"
            +"                    <CssParameter name=\"font-weight\">normal</CssParameter>"
            +"                </Font>"
            +"                <LabelPlacement>"
            +"                      <LinePlacement>"
            +"                              <PerpendicularOffset>0</PerpendicularOffset>"
            +"                      </LinePlacement>"
            +"                </LabelPlacement>"
            +"                </TextSymbolizer>"
            +"                              </Rule>"
            +"                  </FeatureTypeStyle>"
            +"              </UserStyle>"
            +"      </NamedLayer>"
            +"</StyledLayerDescriptor>";

        StringReader reader = new StringReader(xml);
        SLDParser sldParser = new SLDParser(sf, reader);

        Style[] parsed = sldParser.readXML();
        assertNotNull("parsed xml", parsed);
        assertTrue("parsed xml into style", parsed.length > 0);

        Style style = parsed[0];
        assertNotNull(style);
        Rule rule = style.featureTypeStyles().get(0).rules().get(0);
        LineSymbolizer lineSymbolize = (LineSymbolizer) rule.symbolizers().get(0);
        Stroke stroke = lineSymbolize.getStroke();

        Expression color = stroke.getColor();
        Color value = color.evaluate(null, Color.class);
        assertNotNull("color", value);
        assertEquals("blue", Color.BLUE, value);
    }
    /**
     * Another bug reported from uDig 1.2; we are trying to save a LineSymbolizer (and then restore
     * it) and the stroke is comming back black and with width 1 all the time.
     * 
     * @throws Exception
     */
    public void testPointSymbolizer() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<sld:StyledLayerDescriptor xmlns:sld=\"http://www.opengis.net/sld\" "
                + "xmlns:ogc=\"http://www.opengis.net/ogc\""
                + " xmlns:gml=\"http://www.opengis.net/gml\" version=\"1.0.0\">"
                + "    <sld:UserLayer>"
                + "       <sld:LayerFeatureConstraints>"
                + "            <sld:FeatureTypeConstraint/>"
                + "        </sld:LayerFeatureConstraints>"
                + "        <sld:UserStyle>"
                + "            <sld:Name>Default Styler</sld:Name> "
                + "            <sld:Title>Default Styler</sld:Title>"
                + "            <sld:Abstract/>"
                + "            <sld:FeatureTypeStyle>"
                + "                <sld:Name>simple</sld:Name>"
                + "                <sld:Title>title</sld:Title>"
                + "                <sld:Abstract>abstract</sld:Abstract>"
                + "                <sld:FeatureTypeName>Feature</sld:FeatureTypeName>"
                + "                <sld:SemanticTypeIdentifier>generic:geometry</sld:SemanticTypeIdentifier>"
                + "                <sld:SemanticTypeIdentifier>simple</sld:SemanticTypeIdentifier>"
                + "                <sld:Rule>"
                + "                    <sld:Name>name</sld:Name>"
                + "                    <sld:Title>title</sld:Title>"
                + "                    <sld:Abstract>Abstract</sld:Abstract>"
                + "                    <sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator>"
                + "                    <sld:PointSymbolizer>"
                + "                        <sld:Graphic>"
                + "                            <sld:Mark>"
                + "                                <sld:WellKnownName>triangle</sld:WellKnownName>"
                + "                                <sld:Fill>"
                + "                                    <sld:CssParameter name=\"fill\">"
                + "                                        <ogc:Literal>#FFFF00</ogc:Literal>"
                + "                                    </sld:CssParameter>"
                + "                                    <sld:CssParameter name=\"fill-opacity\">"
                + "                                        <ogc:Literal>1.0</ogc:Literal>"
                + "                                    </sld:CssParameter>"
                + "                                </sld:Fill>"
                + "                                <sld:Stroke>"
                + "                                    <sld:CssParameter name=\"stroke\">"
                + "                                        <ogc:Literal>#008000</ogc:Literal>"
                + "                                    </sld:CssParameter>"
                + "                                    <sld:CssParameter name=\"stroke-linecap\">"
                + "                                        <ogc:Literal>butt</ogc:Literal>"
                + "                                    </sld:CssParameter>"
                + "                                    <sld:CssParameter name=\"stroke-linejoin\">"
                + "                                        <ogc:Literal>miter</ogc:Literal>"
                + "                                    </sld:CssParameter>"
                + "                                    <sld:CssParameter name=\"stroke-opacity\">"
                + "                                        <ogc:Literal>1.0</ogc:Literal>"
                + "                                    </sld:CssParameter>"
                + "                                    <sld:CssParameter name=\"stroke-width\">"
                + "                                        <ogc:Literal>1.0</ogc:Literal>"
                + "                                    </sld:CssParameter>"
                + "                                    <sld:CssParameter name=\"stroke-dashoffset\">"
                + "                                        <ogc:Literal>0.0</ogc:Literal>"
                + "                                    </sld:CssParameter>"
                + "                                </sld:Stroke>"
                + "                            </sld:Mark>"
                + "                            <sld:Opacity>"
                + "                                <ogc:Literal>1.0</ogc:Literal>"
                + "                            </sld:Opacity>"
                + "                            <sld:Size>"
                + "                                <ogc:Literal>10.0</ogc:Literal>"
                + "                            </sld:Size>"
                + "                            <sld:Rotation>"
                + "                                <ogc:Literal>0.0</ogc:Literal>"
                + "                            </sld:Rotation>"
                + "                        </sld:Graphic>"
                + "                    </sld:PointSymbolizer>" + "                </sld:Rule>"
                + "            </sld:FeatureTypeStyle>" + "        </sld:UserStyle>"
                + "    </sld:UserLayer>" + "</sld:StyledLayerDescriptor>";

        StringReader reader = new StringReader(xml);
        SLDParser sldParser = new SLDParser(sf, reader);

        Style[] parsed = sldParser.readXML();
        assertNotNull("parsed xml", parsed);
        assertTrue("parsed xml into style", parsed.length > 0);

        Style style = parsed[0];
        assertNotNull(style);
        Rule rule = style.featureTypeStyles().get(0).rules().get(0);
        List<? extends Symbolizer> symbolizers = rule.symbolizers();
        assertEquals( 1, symbolizers.size() );
        PointSymbolizer symbolize = (PointSymbolizer) symbolizers.get(0);
        Graphic graphic = symbolize.getGraphic();
        List<GraphicalSymbol> symbols = graphic.graphicalSymbols();
        assertEquals( 1, symbols.size() );
        Mark mark = (Mark) symbols.get(0);
        Expression color = mark.getFill().getColor();
        Color value = color.evaluate(null, Color.class);
        assertNotNull("color", value);
        assertEquals("blue", Color.YELLOW, value);
    }

    /**
     * We have a pretty serious issue with this class not behaving well when logging is turned on!
     * This is the same test as above but with logging enganged at the FINEST level.
     * 
     * @throws Exception
     */
    public void testStrokeWithLogging() throws Exception {
        Logger logger = Logger.getLogger("org.geotools.styling");
        Level before = logger.getLevel();
        try {
            logger.setLevel(Level.FINEST);
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><sld:UserStyle xmlns=\"http://www.opengis.net/sld\" xmlns:sld=\"http://www.opengis.net/sld\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\"><sld:Name>Default Styler</sld:Name><sld:Title>Default Styler</sld:Title><sld:FeatureTypeStyle><sld:Name>simple</sld:Name><sld:Title>title</sld:Title><sld:Abstract>abstract</sld:Abstract><sld:FeatureTypeName>Feature</sld:FeatureTypeName><sld:SemanticTypeIdentifier>generic:geometry</sld:SemanticTypeIdentifier><sld:SemanticTypeIdentifier>simple</sld:SemanticTypeIdentifier><sld:Rule><sld:Title>title</sld:Title><sld:Abstract>abstract</sld:Abstract><sld:MaxScaleDenominator>1.7976931348623157E308</sld:MaxScaleDenominator><sld:LineSymbolizer><sld:Stroke><sld:CssParameter name=\"stroke\"><ogc:Literal>#0000FF</ogc:Literal></sld:CssParameter><sld:CssParameter name=\"stroke-linecap\"><ogc:Literal>butt</ogc:Literal></sld:CssParameter><sld:CssParameter name=\"stroke-linejoin\"><ogc:Literal>miter</ogc:Literal></sld:CssParameter><sld:CssParameter name=\"stroke-opacity\"><ogc:Literal>1.0</ogc:Literal></sld:CssParameter><sld:CssParameter name=\"stroke-width\"><ogc:Literal>2.0</ogc:Literal></sld:CssParameter><sld:CssParameter name=\"stroke-dashoffset\"><ogc:Literal>0.0</ogc:Literal></sld:CssParameter></sld:Stroke></sld:LineSymbolizer></sld:Rule></sld:FeatureTypeStyle></sld:UserStyle>";
            StringReader reader = new StringReader(xml);
            SLDParser sldParser = new SLDParser(sf, reader);

            Style[] parsed = sldParser.readXML();
            assertNotNull("parsed xml", parsed);
            assertTrue("parsed xml into style", parsed.length > 0);

            Style style = parsed[0];
            assertNotNull(style);
            Rule rule = style.featureTypeStyles().get(0).rules().get(0);
            LineSymbolizer lineSymbolize = (LineSymbolizer) rule.symbolizers().get(0);
            Stroke stroke = lineSymbolize.getStroke();

            Expression color = stroke.getColor();
            Color value = color.evaluate(null, Color.class);
            assertNotNull("color", value);
            assertEquals("blue", Color.BLUE, value);
            assertEquals("expected width", 2, (int) stroke.getWidth().evaluate(null, Integer.class));
        } finally {
            logger.setLevel(before);
        }
    }
}
