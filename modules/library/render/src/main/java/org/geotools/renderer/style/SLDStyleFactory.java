/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.renderer.style;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.renderer.lite.CustomGlyphRenderer;
import org.geotools.renderer.lite.GlyphRenderer;
import org.geotools.renderer.lite.SVGGlyphRenderer;
import org.geotools.styling.ExternalGraphic;
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
import org.geotools.styling.StyleAttributeExtractorTruncated;
import org.geotools.styling.StyleFactoryFinder;
import org.geotools.styling.Symbol;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextMark;
import org.geotools.styling.TextSymbolizer;
import org.geotools.styling.TextSymbolizer2;
import org.geotools.util.Range;
import org.geotools.util.SoftValueHashMap;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.style.GraphicalSymbol;

import com.vividsolutions.jts.geom.Geometry;


/**
 * Factory object that converts SLD style into rendered styles.
 *
 * DJB:  I've made a few changes to this.
 *       The old behavior was for this class to convert <LinePlacement> tags to <PointPlacement> tags.
 *       (ie. there never was a LinePlacement option)
 *       This is *certainly* not the correct place to do this, and it was doing a very poor job of it too,
 *       and the renderer was not expecting it to be doing it!
 *
 *       I added support in TextStyle3D for this and had this class correctly set Line/Point placement selection.
 *       NOTE: PointPlacement is the default if not present.
 *
 * @author aaime
 * @author dblasby
 */

/*
 *  orginal message on the subject:
 *
 * I was attempting to write documentation for label placement (plus fix
all the inconsistencies with the spec), and I noticed some problems
with the SLDStyleFactory and TextStyle2D.

It turns out the SLDStyleFactory is actually trying to do [poor] label
placement (see around line 570)! This also results in a loss of
information if you're using a <LinePlacement> element in your SLD.


1. remove the placement code from SLDStyleFactory!
2. get rid of the "AbsoluteLineDisplacement" stuff and replace it with
something that represents <PointPlacement>/<LinePlacement> elements in
the TextSymbolizer.

The current implementation seems to try to convert a <LinePlacement> and
an actual line into a <PointPlacement> (and setting the
AbsoluteLineDisplacement flag)!! This should be done by the real
labeling code.

This change could affect the j2d renderer as it appears to use the
"AbsoluteLineDisplacement" flag.
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/render/src/main/java/org/geotools/renderer/style/SLDStyleFactory.java $
*/

public class SLDStyleFactory {
    /** The logger for the rendering module. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.rendering");

    /** Holds a lookup bewteen SLD names and java constants. */
    private static final java.util.Map joinLookup = new java.util.HashMap();

    /** Holds a lookup bewteen SLD names and java constants. */
    private static final java.util.Map capLookup = new java.util.HashMap();

    /** Holds a lookup bewteen SLD names and java constants. */
    private static final java.util.Map fontStyleLookup = new java.util.HashMap();

    private static final FilterFactory ff = CommonFactoryFinder.getFilterFactory( null );
    
    /** This one is used as the observer object in image tracks */
    private static final Canvas obs = new Canvas();

    /** This one holds the list of glyphRenderers that can convert glyphs into an image */
    private static List glyphRenderers = new ArrayList();

    static { //static block to populate the lookups
        joinLookup.put("miter", new Integer(BasicStroke.JOIN_MITER));
        joinLookup.put("bevel", new Integer(BasicStroke.JOIN_BEVEL));
        joinLookup.put("round", new Integer(BasicStroke.JOIN_ROUND));

        capLookup.put("butt", new Integer(BasicStroke.CAP_BUTT));
        capLookup.put("round", new Integer(BasicStroke.CAP_ROUND));
        capLookup.put("square", new Integer(BasicStroke.CAP_SQUARE));

        fontStyleLookup.put("normal", new Integer(java.awt.Font.PLAIN));
        fontStyleLookup.put("italic", new Integer(java.awt.Font.ITALIC));
        fontStyleLookup.put("oblique", new Integer(java.awt.Font.ITALIC));
        fontStyleLookup.put("bold", new Integer(java.awt.Font.BOLD));

        /**
         * Initialize the gliph renderers array with the default ones
         */
        glyphRenderers.add(new CustomGlyphRenderer());

        try {
            glyphRenderers.add(new SVGGlyphRenderer());
        } catch (Exception e) {
            LOGGER.warning("Will not support SVG External Graphics " + e);
        }
    }

    /** Symbolizers that depend on attributes */
    Map dynamicSymbolizers = new SoftValueHashMap();

    /** Symbolizers that do not depend on attributes */
    Map staticSymbolizers = new SoftValueHashMap(); 

    

    private long hits;

    private long requests;

    /**
     * Holds value of property mapScaleDenominator.
     */
    private double mapScaleDenominator = Double.NaN;;


    public double getHitRatio() {
        return (double) hits/ (double) requests;
    }

    public long getHits() {
        return hits;
    }

    public long getRequests() {
        return requests;
    }


    /**
     * <p>
     * Creates a rendered style
     * </p>
     *
     * <p>
     * Makes use of a symbolizer cache based on identity to avoid recomputing over and over the
     * same style object and to reduce memory usage. The same Style2D object will be returned by
     * subsequent calls using the same feature independent symbolizer with the same scaleRange.
     * </p>
     *
     * @param drawMe The feature
     * @param symbolizer The SLD symbolizer
     * @param scaleRange The scale range in which the feature should be painted according to the
     *        symbolizer
     *
     * @return A rendered style equivalent to the symbolizer
     */
    public Style2D createStyle(Object drawMe, Symbolizer symbolizer, Range scaleRange) {
        Style2D style = null;

        SymbolizerKey key = new SymbolizerKey(symbolizer, scaleRange);
        style = (Style2D) staticSymbolizers.get(key);

        requests++;

        if (style != null) {
            hits++;
        } else {
            style = createStyleInternal(drawMe, symbolizer, scaleRange);

            // if known dynamic symbolizer return the style
            if (dynamicSymbolizers.containsKey(key)) {
                return style;
            } else {
                // lets see if it's static or dynamic
                StyleAttributeExtractorTruncated sae = new StyleAttributeExtractorTruncated();
                sae.visit(symbolizer);

                Set nameSet = sae.getAttributeNameSet();

                if ((nameSet == null) || (nameSet.size() == 0)) {
                    staticSymbolizers.put(key, style);
                } else {
                    dynamicSymbolizers.put(key, Boolean.TRUE);
                }
            }
        }
        return style;
    }

    /**
     * Really creates the symbolizer
     *
     * @param drawMe DOCUMENT ME!
     * @param symbolizer DOCUMENT ME!
     * @param scaleRange DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private Style2D createStyleInternal(Object drawMe, Symbolizer symbolizer, Range scaleRange) {
        Style2D style = null;

        if (symbolizer instanceof PolygonSymbolizer) {
            style = createPolygonStyle(drawMe, (PolygonSymbolizer) symbolizer, scaleRange);
        } else if (symbolizer instanceof LineSymbolizer) {
            style = createLineStyle(drawMe, (LineSymbolizer) symbolizer, scaleRange);
        } else if (symbolizer instanceof PointSymbolizer) {
            style = createPointStyle(drawMe, (PointSymbolizer) symbolizer, scaleRange);
        } else if (symbolizer instanceof TextSymbolizer) {
            style = createTextStyle(drawMe, (TextSymbolizer) symbolizer, scaleRange);
        }

        return style;
    }

    /**
     * Creates a rendered style
     *
     * @param f The feature
     * @param symbolizer The SLD symbolizer
     * @param scaleRange The scale range in which the feature should be painted according to the
     *        symbolizer
     *
     * @return A rendered style equivalent to the symbolizer
     *
     * @throws UnsupportedOperationException if an unknown symbolizer is passed to this method
     */
    public Style2D createDynamicStyle(SimpleFeature f, Symbolizer symbolizer, Range scaleRange) {
        Style2D style = null;

        if (symbolizer instanceof PolygonSymbolizer) {
            style = createDynamicPolygonStyle(f, (PolygonSymbolizer) symbolizer, scaleRange);
        } else if (symbolizer instanceof LineSymbolizer) {
            style = createDynamicLineStyle(f, (LineSymbolizer) symbolizer, scaleRange);
        } else {
            throw new UnsupportedOperationException("This kind of symbolizer is not yet supported");
        }

        return style;
    }

    PolygonStyle2D createPolygonStyle(Object feature, PolygonSymbolizer symbolizer, Range scaleRange) {
        PolygonStyle2D style = new PolygonStyle2D();

        setScaleRange(style, scaleRange);
        style.setStroke(getStroke(symbolizer.getStroke(), feature));
        style.setGraphicStroke(getGraphicStroke(symbolizer.getStroke(), feature));
        style.setContour(getStrokePaint(symbolizer.getStroke(), feature));
        style.setContourComposite(getStrokeComposite(symbolizer.getStroke(), feature));
        style.setFill(getPaint(symbolizer.getFill(), feature));
        style.setFillComposite(getComposite(symbolizer.getFill(), feature));

        return style;
    }

    Style2D createDynamicPolygonStyle(SimpleFeature feature, PolygonSymbolizer symbolizer,
        Range scaleRange) {
        PolygonStyle2D style = new DynamicPolygonStyle2D(feature, symbolizer);

        setScaleRange(style, scaleRange);

        //setStroke(style, symbolizer.getStroke(), feature);
        //setFill(style, symbolizer.getFill(), feature);
        return style;
    }

    Style2D createLineStyle(Object feature, LineSymbolizer symbolizer, Range scaleRange) {
        LineStyle2D style = new LineStyle2D();
        setScaleRange(style, scaleRange);
        style.setStroke(getStroke(symbolizer.getStroke(), feature));
        style.setGraphicStroke(getGraphicStroke(symbolizer.getStroke(), feature));
        style.setContour(getStrokePaint(symbolizer.getStroke(), feature));
        style.setContourComposite(getStrokeComposite(symbolizer.getStroke(), feature));

        return style;
    }

    Style2D createDynamicLineStyle(SimpleFeature feature, LineSymbolizer symbolizer, Range scaleRange) {
        LineStyle2D style = new DynamicLineStyle2D(feature, symbolizer);
        setScaleRange(style, scaleRange);

        //setStroke(style, symbolizer.getStroke(), feature);
        return style;
    }
    /**
     * Style used to render the provided feature as a point.
     * <p>
     * Depending on the symbolizers used:
     * <ul>
     * <li>MarkStyle2D
     * <li>GraphicStyle2D - used to render a glymph
     * </ul>
     * @param feature
     * @param symbolizer
     * @param scaleRange
     * @return
     */
    Style2D createPointStyle(Object feature, PointSymbolizer symbolizer, Range scaleRange) {
        Style2D retval = null;

        // extract base properties
        Graphic sldGraphic = symbolizer.getGraphic();
        float opacity = evalOpacity(sldGraphic.getOpacity(), feature);
        int size = 0;

        // by spec size is optional, and the default value is context dependend,
        // the natural size of the image for an external graphic is the size of the raster,
        // while:
        // - for a external graphic the default size shall be 16x16
        // - for a mark such as star or square the default size shall be 6x6 
        try {
            if(sldGraphic.getSize() != null && !Expression.NIL.equals(sldGraphic.getSize()))
                size = (int) evalToDouble(sldGraphic.getSize(),feature,0);
        } catch (NumberFormatException nfe) {
            // nothing to do
        }

        float rotation = (float)((evalToFloat(sldGraphic.getRotation(),feature, 0) * Math.PI) / 180);

        // Extract the sequence of external graphics and symbols and process them in order
        // to recognize which one will be used for rendering
        List<GraphicalSymbol> symbols = sldGraphic.graphicalSymbols();
        if( symbols.isEmpty()){
            symbols = new ArrayList<GraphicalSymbol>();
            Mark square = StyleFactoryFinder.createStyleFactory().createMark();
            symbols.add( square );
        }
		final int length = symbols.size();
		ExternalGraphic eg;
		GlyphRenderer r;
		BufferedImage img = null;
		double dsize;
		AffineTransform scaleTx;
		AffineTransformOp ato;
		BufferedImage scaledImage;
		Mark mark;
		Shape shape;
		MarkStyle2D ms2d;
		for( GraphicalSymbol symbol : symbols ){
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("trying to render symbol " + symbol);
            }
            // try loading external graphic and creating a GraphicsStyle2D
            if (symbol instanceof ExternalGraphic) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("rendering External graphic");
                }

                eg = (ExternalGraphic) symbol;
                img = externalGraphicToImage(feature, sldGraphic, size, eg);

                if (img == null) {
                    continue;
                } else {
                    retval = new GraphicStyle2D(img, rotation, opacity);
                    break;
                }
            }
            if (symbol instanceof Mark) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("rendering mark @ PointRenderer " + symbol.toString());
                }

				mark = (Mark) symbol;
				shape = getShape(mark, feature);
				
				if(shape == null)
				    throw new IllegalArgumentException("The specified mark " + mark.getWellKnownName() 
				            + " was not found!");

                ms2d = new MarkStyle2D();
                ms2d.setShape(shape);
                ms2d.setFill(getPaint(mark.getFill(), feature));
                ms2d.setFillComposite(getComposite(mark.getFill(), feature));
                ms2d.setStroke(getStroke(mark.getStroke(), feature));
                ms2d.setContour(getStrokePaint(mark.getStroke(), feature));
                ms2d.setContourComposite(getStrokeComposite(mark.getStroke(), feature));
                // in case of Mark we don't have a natural size, so we default to 16
                if(size <= 0)
                    size = 16;
                ms2d.setSize(size);
                ms2d.setRotation(rotation);
                retval = ms2d;

                break;
            }

            if (symbol instanceof TextMark) {
                // for the moment don't support TextMarks since they are not part
                // of the SLD specification
                continue;

                /**
                 * if (LOGGER.isLoggable(Level.FINER)) {     LOGGER.finer("rendering text symbol");
                 * } flag = renderTextSymbol(geom, sldgraphic, feature, (TextMark) symbols[i]); if
                 * (flag) {     return; }
                 */
            }
        }

        if (retval != null) {
            setScaleRange(retval, scaleRange);
        }

        return retval;
    }

    private BufferedImage externalGraphicToImage(Object feature, Graphic sldGraphic, int size,
            ExternalGraphic eg) {
        BufferedImage img = null;
        GlyphRenderer r;
        // first see if any glyph renderers can handle this, for backwards compatibility
        for(Iterator it = glyphRenderers.iterator(); it.hasNext() && (img == null); ) {
            r = (GlyphRenderer) it.next();

            if (r.canRender(eg.getFormat())) {
                img = r.render(sldGraphic, eg, feature,size);
                break; // dont render twice
            }
        }

        // if no-one of the glyph renderers can handle the eg, use the dynamic symbol factoreis
        if(img == null) {
            img = getImage(eg, (Feature) feature, size); //size is only a hint
        }
        return img;
    }



    Style2D createTextStyle(Object feature, TextSymbolizer symbolizer, Range scaleRange) {
        TextStyle2D ts2d = new TextStyle2D();
        setScaleRange(ts2d, scaleRange);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("creating text style");
        }

        String geomName = symbolizer.getGeometryPropertyName();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("geomName = " + geomName);
        }

        // extract label (from ows5 extensions, we could have the label element empty)
        String label = "";
        if(symbolizer.getLabel() != null) {
            Object obj = symbolizer.getLabel().evaluate(feature);
            if(obj != null)
                label = obj.toString();
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("label is " + label);
        }

        ts2d.setLabel(label);

        // get the sequence of fonts to be used and set the first one available
        Font[] fonts = symbolizer.getFonts();
        java.awt.Font javaFont = getFont(feature, fonts);
        ts2d.setFont(javaFont);

        // compute label position, anchor, rotation and displacement
        LabelPlacement placement = symbolizer.getLabelPlacement();
        double anchorX = 0;
        double anchorY = 0;
        double rotation = 0;
        double dispX = 0;
        double dispY = 0;

        if (placement instanceof PointPlacement)
        {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("setting pointPlacement");
            }

            // compute anchor point and displacement
            PointPlacement p = (PointPlacement) placement;
            anchorX = ((Number) p.getAnchorPoint().getAnchorPointX().evaluate(feature)).doubleValue();
            anchorY = ((Number) p.getAnchorPoint().getAnchorPointY().evaluate(feature)).doubleValue();

            dispX = ((Number) p.getDisplacement().getDisplacementX().evaluate(feature)).doubleValue();
            dispY = ((Number) p.getDisplacement().getDisplacementY().evaluate(feature)).doubleValue();

            // rotation
            if  ( (symbolizer instanceof TextSymbolizer2)  && (((TextSymbolizer2)symbolizer).getGraphic() != null) )
            {
                // don't rotate labels that are being placed on shields.
                rotation = 0.0;
            } else {
                rotation = ((Number) p.getRotation().evaluate(feature)).doubleValue();
                rotation *= (Math.PI / 180.0);
            }

            ts2d.setPointPlacement(true);
        }
        else if (placement instanceof LinePlacement)
        {
              // this code used to really really really really suck, so I removed it!
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("setting pointPlacement");
            }
            ts2d.setPointPlacement(false);
            LinePlacement p = (LinePlacement) placement;
            int displace =  ((Number) p.getPerpendicularOffset().evaluate(feature)).intValue();
            ts2d.setPerpendicularOffset( displace );
        }

        ts2d.setAnchorX(anchorX);
        ts2d.setAnchorY(anchorY);
        ts2d.setRotation((float) rotation);
        ts2d.setDisplacementX(dispX);
        ts2d.setDisplacementY(dispY);

        // setup fill and composite
        ts2d.setFill(getPaint(symbolizer.getFill(), feature));
        ts2d.setComposite(getComposite(symbolizer.getFill(), feature));

        // compute halo parameters
        Halo halo = symbolizer.getHalo();

        if (halo != null) {
            ts2d.setHaloFill(getPaint(halo.getFill(), feature));
            ts2d.setHaloComposite(getComposite(halo.getFill(), feature));
            ts2d.setHaloRadius(((Number) halo.getRadius().evaluate(feature)).floatValue());
        }

        Graphic graphicShield = null;
        if  (symbolizer instanceof TextSymbolizer2)
        {
                graphicShield = ( (TextSymbolizer2) symbolizer).getGraphic();
                if (graphicShield != null)
                {
                    PointSymbolizer p = StyleFactoryFinder.createStyleFactory().createPointSymbolizer();
                    p.setGraphic(graphicShield);

                    Style2D shieldStyle = createPointStyle(feature, p, scaleRange);
                    ts2d.setGraphic(shieldStyle);
                }
        }


        return ts2d;
    }

    /**
     * Extracts the named geometry from feature. If geomName is null then the feature's default
     * geometry is used. If geomName cannot be found in feature then null is returned.
     *
     * @param feature The feature to find the geometry in
     * @param geomName The name of the geometry to find: null if the default geometry should be
     *        used.
     *
     * @return The geometry extracted from feature or null if this proved impossible.
     */
    private Geometry findGeometry(final Object feature, String geomName) {
        Geometry geom = null;

        if( geomName == null ){
            geomName = ""; // ie default geometry
        }
        PropertyName property = ff.property( geomName );
        return (Geometry) property.evaluate( feature, Geometry.class );
    }

    /**
     * Returns the first font associated to the feature that can be found on the current machine
     *
     * @param feature The feature whose font is to be found
     * @param fonts An array of fonts dependent of the feature, the first that is found on the
     *        current machine is returned
     *
     * @return The first of the specified fonts found on this machine or null if none found
     */
    private java.awt.Font getFont(Object feature, Font[] fonts) {
        for (int k = 0; k < fonts.length; k++) {
            String requestedFont = fonts[k].getFontFamily().evaluate(feature).toString();
            java.awt.Font javaFont = FontCache.getDefaultInsance().getFont(requestedFont);
            
            if(javaFont != null) {
                String reqStyle = (String) fonts[k].getFontStyle().evaluate(feature);

                int styleCode;
                if (fontStyleLookup.containsKey(reqStyle)) {
                    styleCode = ((Integer) fontStyleLookup.get(reqStyle)).intValue();
                } else {
                    styleCode = java.awt.Font.PLAIN;
                }

                String reqWeight = (String) fonts[k].getFontWeight().evaluate(feature);

                if (reqWeight.equalsIgnoreCase("Bold")) {
                    styleCode = styleCode | java.awt.Font.BOLD;
                }

                int size = ((Number) fonts[k].getFontSize().evaluate(feature)).intValue();

                return javaFont.deriveFont(styleCode, size);
            }
        }

        // if everything else fails fall back on a default font distributed
        // along with the jdk
        return new java.awt.Font("Serif",java.awt.Font.PLAIN,12);
    }

    void setScaleRange(Style style, Range scaleRange) {
        double min = ((Number) scaleRange.getMinValue()).doubleValue();
        double max = ((Number) scaleRange.getMaxValue()).doubleValue();
        style.setMinMaxScale(min, max);
    }

    // Builds an image version of the graphics with the proper size, no further scaling will
    // be needed during rendering
    private BufferedImage getGraphicStroke(org.geotools.styling.Stroke stroke, Object feature) {
        if ((stroke == null) || (stroke.getGraphicStroke() == null)) {
            return null;
        }

        Graphic graphicStroke = stroke.getGraphicStroke();
        int size = ((Number) graphicStroke.getSize().evaluate(feature)).intValue();

        // lets see if an external image is to be used
        BufferedImage image = getImage(graphicStroke, (Feature) feature, size);

        if (image == null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("going for the mark from graphic fill");
            }

            Mark mark = getMark(graphicStroke, feature);
            image = new BufferedImage((int) size, (int) size, BufferedImage.TYPE_INT_ARGB);

            Graphics2D ig2d = image.createGraphics();
            double rotation = 0.0;
            rotation = ((Number) graphicStroke.getRotation().evaluate(feature)).doubleValue();
            rotation *= (Math.PI / 180.0);
            fillDrawMark(ig2d, size / 2, size / 2, mark, (int) size, rotation, feature);

            MediaTracker track = new MediaTracker(obs);
            track.addImage(image, 1);

            try {
                track.waitForID(1);
            } catch (InterruptedException e) {
                LOGGER.warning(e.toString());
            }
        }

        return image;
    }

    private Stroke getStroke(org.geotools.styling.Stroke stroke, Object feature) {
        if (stroke == null) {
            return null;
        }

        // resolve join type into a join code
        String joinType;
        int joinCode;

        joinType = evaluateExpression(stroke.getLineJoin(), feature, "miter");

        if (joinLookup.containsKey(joinType)) {
            joinCode = ((Integer) joinLookup.get(joinType)).intValue();
        } else {
            joinCode = java.awt.BasicStroke.JOIN_MITER;
        }

        // resolve cap type into a cap code
        String capType;
        int capCode;

        capType = evaluateExpression(stroke.getLineCap(), feature, "square");

        if (capLookup.containsKey(capType)) {
            capCode = ((Integer) capLookup.get(capType)).intValue();
        } else {
            capCode = java.awt.BasicStroke.CAP_SQUARE;
        }

        // get the other properties needed for the stroke
        float[] dashes = stroke.getDashArray();
        float width = evalToFloat(stroke.getWidth(), feature, 1);
        float dashOffset = evalToFloat(stroke.getDashOffset(), feature, 0);

        // Simple optimization: let java2d use the fast drawing path if the line width
        // is small enough...
        if (width < 1.5) {
            width = 0;
        }

        // now set up the stroke
        BasicStroke stroke2d;

        if ((dashes != null) && (dashes.length > 0)) {
            stroke2d = new BasicStroke(width, capCode, joinCode, 1, dashes, dashOffset);
        } else {
            stroke2d = new BasicStroke(width, capCode, joinCode, 1);
        }

        return stroke2d;
    }

    private Paint getStrokePaint(org.geotools.styling.Stroke stroke, Object feature) {
        if (stroke == null) {
            return null;
        }

        // the foreground color
        Paint contourPaint = evalToColor(stroke.getColor(),feature,Color.BLACK);

        // if a graphic fill is to be used, prepare the paint accordingly....
        org.geotools.styling.Graphic gr = stroke.getGraphicFill();

        if (gr != null) {
            contourPaint = getTexturePaint(gr, feature);
        }

        return contourPaint;
    }

    private Composite getStrokeComposite(org.geotools.styling.Stroke stroke, Object feature) {
        if (stroke == null) {
            return null;
        }

        // get the opacity and prepare the composite
        float opacity = evalOpacity(stroke.getOpacity(),feature);
        Composite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);

        return composite;
    }

    protected Paint getPaint(Fill fill, Object feature) {
        if (fill == null) {
            return null;
        }
        
        // get fill color
        Paint fillPaint = null;
        if (fill.getColor() != null) {
            Color color = fill.getColor().evaluate( feature,Color.class );
            fillPaint = color;
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Setting fill: " + fillPaint.toString());
            }
        }

        // if a graphic fill is to be used, prepare the paint accordingly....
        org.geotools.styling.Graphic gr = fill.getGraphicFill();

        if (gr != null) {
            fillPaint = getTexturePaint(gr, feature);
        }

        return fillPaint;
    }

    /**
     * Computes the Composite equivalent to the opacity in the SLD Fill
     *
     * @param fill
     * @param feature
     *
     */
    protected Composite getComposite(Fill fill, Object feature) {
        if (fill == null) {
            return null;
        }

        // get the opacity and prepare the composite
        float opacity = evalOpacity(fill.getOpacity(),feature);
        Composite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);

        return composite;
    }

    /**
     * DOCUMENT ME!
     *
     * @param gr DOCUMENT ME!
     * @param feature DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public TexturePaint getTexturePaint(org.geotools.styling.Graphic gr, Object feature) {
        BufferedImage image = getImage(gr, feature, -1);
        boolean isImage = false;

        if (image != null) {
            isImage = true;
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("got an image in graphic fill");
            }
        } else {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("going for the mark from graphic fill");
            }

            org.geotools.styling.Mark mark = getMark(gr, feature);

            if (mark == null) {
                return null;
            }

            int size = 200;

            image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = image.createGraphics();
            double rotation = 0.0;

            rotation = ((Number) gr.getRotation().evaluate(feature)).doubleValue();
            rotation *= (Math.PI / 180.0);

            fillDrawMark(g2d, 100, 100, mark, (int) (size * .9), rotation, feature);

            java.awt.MediaTracker track = new java.awt.MediaTracker(obs);
            track.addImage(image, 1);

            try {
                track.waitForID(1);
            } catch (InterruptedException e) {
                // TODO: what should we do with this?
                LOGGER.warning("An unterupptedException occurred while drawing a local image..."
                    + e);
            }
        }

        int size;
        if(gr.getSize() == null || gr.getSize().evaluate(feature) == null) {
            if(isImage)
                size = image.getWidth();
            else
                size = 16;
        } else {
            size = ((Number) gr.getSize().evaluate(feature)).intValue();
        }
        double width = image.getWidth();
        double height = image.getHeight();

        double unitSize = Math.max(width, height);
        double drawSize = (double) size / unitSize;

        width *= drawSize;
        height *= -drawSize;

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("size = " + size + " unitsize " + unitSize + " drawSize " + drawSize);
        }

        Rectangle2D.Double rect = new Rectangle2D.Double(0.0, 0.0, width, height);
        TexturePaint imagePaint = new TexturePaint(image, rect);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("applied TexturePaint " + imagePaint);
        }

        return imagePaint;
    }

    /**
     * Scans the dynamic external graphic factories and returns the image representing
     * the first external graphic that could be parsed successfully 
     * @param graphic
     * @param feature
     * @param size
     * @return
     */
    private BufferedImage getImage(Graphic graphic, Object feature, int size) {
        ExternalGraphic[] extgraphics = graphic.getExternalGraphics();

        if (extgraphics != null) {
            for (int i = 0; i < extgraphics.length; i++) {
                BufferedImage image = externalGraphicToImage(feature, graphic, size, extgraphics[i]);
                if(image != null)
                    return image;
            }
        }

        return null;
    }

    /**
     * Tries to parse the provided external graphic into a BufferedImage. 
     * @param eg
     * @param feature
     * @param size
     * @return the image, or null if the external graphics could not be interpreted
     */
    private BufferedImage getImage(ExternalGraphic eg, Object feature, int size) {
        // extract the url
        String strLocation;
        try {
            strLocation = eg.getLocation().toExternalForm();
        } catch(MalformedURLException e) {
            LOGGER.log(Level.INFO, "Malformed URL processing external graphic", e);
            return null;
        }
        // parse the eventual ${cqlExpression} embedded in the URL
        Expression location;
        try {
            location = ExpressionExtractor.extractCqlExpressions(strLocation);
        } catch(IllegalArgumentException e) {
            // in the unlikely event that a URL is using one of the chars reserved for ${cqlExpression}
            // let's try and use the location as a literal
            if(LOGGER.isLoggable(Level.FINE))
                LOGGER.log(Level.FINE, "Could not parse cql expressions out of " + strLocation, e);
            location = ff.literal(strLocation);
        }
        
        // scan the external graphic factories and see which one can be used
        Iterator<ExternalGraphicFactory> it  = DynamicSymbolFactoryFinder.getExternalGraphicFactories();
        while(it.hasNext()) {
            try {
                Icon icon = it.next().getIcon((Feature) feature, location, eg.getFormat(), size);
                if(icon != null) {
                    return rasterizeIcon(icon);
                }
            } catch(Exception e) {
                LOGGER.log(Level.FINE, "Error occurred evaluating external graphic", e);
            }
        }
        return null;
    }
    
    
    /**
     * Turns an icon into a BufferedImage
     * @param icon
     * @return
     */
    private BufferedImage rasterizeIcon(Icon icon) {
        // optimization, if this is an IconImage based on a BufferedImage, just return the
        // wrapped one
        if(icon instanceof ImageIcon) {
            ImageIcon img = (ImageIcon) icon;
            if(img.getImage() instanceof BufferedImage)
                return (BufferedImage) img.getImage();
        }
        
        // otherwise have the icon draw itself on a BufferedImage
        BufferedImage result = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), 
                BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = result.getGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        return result;
    }

    /**
     * Looks ups the marks included in the graphics and returns the one that can be drawn
     * by at least one mark factory
     * @param graphic
     * @param feature
     * @return
     */
    private Mark getMark(Graphic graphic, Object feature) {
        Mark[] marks = graphic.getMarks();
        for (int i = 0; i < marks.length; i++) {
            final Mark mark = marks[i];
            Shape shape = getShape(mark, feature);
            if(shape != null)
                return mark;
            
        }
        // if nothing worked, we return a square
        return null;
    }
    
    /**
     * Given a mark and a feature, returns the Shape provided by the first {@link MarkFactory} 
     * that was able to handle the Mark
     * @param mark
     * @param feature
     * @return
     */
    private Shape getShape(Mark mark, Object feature) {
        Expression name = mark.getWellKnownName();
        // expand eventual cql expressions embedded in the name
        if(name instanceof Literal) {
            String expression = name.evaluate(null, String.class);
            if(expression != null)
                name = ExpressionExtractor.extractCqlExpressions(expression);
        }
        
        Iterator<MarkFactory> it = DynamicSymbolFactoryFinder.getMarkFactories();
        while(it.hasNext()) {
            MarkFactory factory = it.next();
            try {
                Shape shape = factory.getShape(null, name, (Feature) feature);
                if(shape != null)
                    return shape;
            } catch(Exception e) {
                LOGGER.log(Level.FINE, "Exception while scanning for " +
                        "the appropriate mark factory", e);
            }
            
        } 
        return null;
    }

    private void fillDrawMark(Graphics2D g2d, double tx, double ty, Mark mark, int size,
        double rotation, Object feature) {
        AffineTransform temp = g2d.getTransform();
        AffineTransform markAT = new AffineTransform();
        Shape shape = getShape(mark, feature);

        Point2D mapCentre = new Point2D.Double(tx, ty);
        Point2D graphicCentre = new Point2D.Double();
        temp.transform(mapCentre, graphicCentre);
        markAT.translate(graphicCentre.getX(), graphicCentre.getY());

        double shearY = temp.getShearY();
        double scaleY = temp.getScaleY();

        double originalRotation = Math.atan(shearY / scaleY);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("originalRotation " + originalRotation);
        }

        markAT.rotate(rotation - originalRotation);

        double unitSize = 1.0; // getbounds is broken !!!
        double drawSize = (double) size / unitSize;
        markAT.scale(drawSize, -drawSize);

        g2d.setTransform(markAT);

        if (mark.getFill() != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("applying fill to mark");
            }

            g2d.setPaint(getPaint(mark.getFill(), feature));
            g2d.setComposite(getComposite(mark.getFill(), feature));
            g2d.fill(shape);
        }

        if (mark.getStroke() != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("applying stroke to mark");
            }

            g2d.setPaint(getStrokePaint(mark.getStroke(), feature));
            g2d.setComposite(getStrokeComposite(mark.getStroke(), feature));
            g2d.setStroke(getStroke(mark.getStroke(), feature));
            g2d.draw(shape);
        }

        g2d.setTransform(temp);

        if (mark.getFill() != null) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        return;
    }

    /**
     * Evaluates an expression over the passed feature, if the expression or the result is null,
     * the default value will be returned
     *
     * @param e
     * @param drawMe
     * @param defaultValue
     *
     */
    private String evaluateExpression(org.opengis.filter.expression.Expression e, Object drawMe, String defaultValue) {
        String result = defaultValue;

        if (e != null) {
            result = (String) e.evaluate(drawMe);

            if (result == null) {
                result = defaultValue;
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param joinType DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static int lookUpJoin(String joinType) {
        if (SLDStyleFactory.joinLookup.containsKey(joinType)) {
            return ((Integer) joinLookup.get(joinType)).intValue();
        } else {
            return java.awt.BasicStroke.JOIN_MITER;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param capType DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static int lookUpCap(String capType) {
        if (SLDStyleFactory.capLookup.containsKey(capType)) {
            return ((Integer) capLookup.get(capType)).intValue();
        } else {
            return java.awt.BasicStroke.CAP_SQUARE;
        }
    }

    /**
     * Getter for property mapScaleDenominator.
     * @return Value of property mapScaleDenominator.
     */
    public double getMapScaleDenominator() {

        return this.mapScaleDenominator;
    }

    /**
     * Setter for property mapScaleDenominator.
     * @param mapScaleDenominator New value of property mapScaleDenominator.
     */
    public void setMapScaleDenominator(double mapScaleDenominator) {

        this.mapScaleDenominator = mapScaleDenominator;
    }

    /**
     * Simple key used to cache Style2D objects based on the originating symbolizer and scale
     * range. Will compare symbolizers by identity, avoiding a possibly very long comparison
     *
     * @author aaime
     */
    static class SymbolizerKey {
        private Symbolizer symbolizer;
        private double minScale;
        private double maxScale;

        public SymbolizerKey(Symbolizer symbolizer, Range scaleRange) {
            this.symbolizer = symbolizer;
            minScale = ((Number) scaleRange.getMinValue()).doubleValue();
            maxScale = ((Number) scaleRange.getMaxValue()).doubleValue();
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            if (!(obj instanceof SymbolizerKey)) {
                return false;
            }

            SymbolizerKey other = (SymbolizerKey) obj;

            return (other.symbolizer == symbolizer) && (other.minScale == minScale)
            && (other.maxScale == maxScale);
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return ((((17 + System.identityHashCode(symbolizer)) * 37) + doubleHash(minScale)) * 37)
            + doubleHash(maxScale);
        }

        private int doubleHash(double value) {
            long bits = Double.doubleToLongBits(value);

            return (int) (bits ^ (bits >>> 32));
        }
    }

     private float evalToFloat(Expression exp, Object f, float fallback){
        if(exp == null){
            return fallback;
        }
        Float fo = (Float) exp.evaluate( f, Float.class );
        if( fo != null ){
            return fo.floatValue();
        }
        return fallback;  
    }

    private double evalToDouble(Expression exp, Object f, double fallback){
        if(exp == null){
            return fallback;
        }
        Object o = exp.evaluate(f);
        if(o instanceof Number)
            return ((Number) o).doubleValue();
        Double d = (Double) exp.evaluate( f, Double.class );
        if( d != null ){
            return d.doubleValue();
        }
        return fallback;        
    }

    private Color evalToColor(Expression exp, Object f, Color fallback){
        if(exp == null){
            return fallback;
        }
        Color color  = exp.evaluate( f, Color.class );
        if( color != null ){
            return color;
        }
        return fallback;
    }

    private float evalOpacity(Expression e, Object f){
        return evalToFloat(e,f,1);
    }

}
