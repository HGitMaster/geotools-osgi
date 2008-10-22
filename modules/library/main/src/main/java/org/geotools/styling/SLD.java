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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geotools.filter.Filters;
import org.geotools.styling.visitor.DuplicatingStyleVisitor;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;


/**
 * Utility class for working with GeoTools SLD objects.
 * <p>
 * This class assumes a subset of the SLD specification:
 * <ul>
 * <li>
 * Single Rule - matching Filter.INCLUDE
 * </li>
 * <li>
 * Symbolizer lookup by name
 * </li>
 * </ul>
 * </p>
 * <p>
 * When you start to branch out to SLD information that contains multiple rules
 * you will need to modify this class.
 * </p>
 *
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/main/java/org/geotools/styling/SLD.java $
 */
public class SLD {
    /** <code>NOTFOUND</code> indicates int value was unavailable */
    public static final int NOTFOUND = Filters.NOTFOUND;
    public static final StyleBuilder builder = new StyleBuilder();
    public static final double ALIGN_LEFT = 1.0;
    public static final double ALIGN_CENTER = 0.5;
    public static final double ALIGN_RIGHT = 0.0;
    public static final double ALIGN_BOTTOM = 1.0;
    public static final double ALIGN_MIDDLE = 0.5;
    public static final double ALIGN_TOP = 0.0;

    /**
     * Retrieve linestring color from linesymbolizer if available.
     *
     * @param symbolizer Line symbolizer information.
     *
     * @return Color of linestring, or null if unavailable.
     */
    public static Color lineColor(LineSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Stroke stroke = symbolizer.getStroke();

        return strokeColor(stroke);
    }

    /**
     * @deprecated please use color( stroke )
     * 
     * @param stroke
     * @return SLD.color( stroke )
     */
    public static Color strokeColor(Stroke stroke) {
        return color( stroke );
    }

    public static Color color(Stroke stroke) {
        if (stroke == null) {
            return null;
        }
        return color(stroke.getColor());
    }
    
    public static Color color(Fill fill) {
        if (fill == null) {
            return null;
        }

        return color(fill.getColor());
    }

    /**
     * Updates the color for line symbolizers in the current style.
     * <p>
     * This method will update the Style in place; some of the symbolizers
     * will be replaced with modified copies.
     * </p>
     *
     * @param style Will update the style in place
     * @param colour Color to to use
     */
    public static void setLineColour(Style style, final Color colour) {
        if (style == null) {
            return;
        }
        for( FeatureTypeStyle featureTypeStyle : style.getFeatureTypeStyles() ){
        	for( int i =0; i<featureTypeStyle.rules().size(); i++){
        		Rule rule = featureTypeStyle.rules().get( i );
        		DuplicatingStyleVisitor update = new DuplicatingStyleVisitor(){
        			public void visit(LineSymbolizer line) {
        				String name = line.getGeometryPropertyName();
        				Stroke stroke = update( line.getStroke());
        				LineSymbolizer copy = sf.createLineSymbolizer( stroke, name );
        		        pages.push(copy);
        			}
        			Stroke update( Stroke stroke ){
        				Expression color = ff.literal( colour );
						Expression width = copy(stroke.getWidth());
						Expression opacity = copy( stroke.getOpacity() );
						Expression lineJoin = copy( stroke.getLineJoin() );
						Expression lineCap = copy( stroke.getLineCap() );
						float[] dashArray = copy( stroke.getDashArray() );
						Expression dashOffset = copy( stroke.getDashOffset() );
						Graphic graphicStroke = copy( stroke.getGraphicStroke() );
						Graphic graphicFill = copy( stroke.getGraphicFill() );
						return sf.createStroke(color, width, opacity, lineJoin, lineCap, dashArray, dashOffset, graphicFill, graphicStroke);        			    
        			}
        		};
        		rule.accept( update );
        		Rule updatedRule = (Rule) update.getCopy();
        		featureTypeStyle.rules().set(i, updatedRule);
        	}
        }
    }

    /**
     * Sets the Colour for the given Line symbolizer
     *
     * @param symbolizer
     * @param colour
     */
    public static void setLineColour(LineSymbolizer symbolizer, Color colour) {
        if (symbolizer == null) {
            return;
        }

        Stroke stroke = symbolizer.getStroke();

        if (stroke == null) {
            stroke = builder.createStroke(colour);
            symbolizer.setStroke(stroke);
        }

        if (colour != null) {
            stroke.setColor(builder.colorExpression(colour));
        }
    }

    /**
     * Retrieve color from linesymbolizer if available.
     *
     * @param symbolizer Line symbolizer information.
     *
     * @return Color of linestring, or null if unavailable.
     */
    public static Color color(LineSymbolizer symbolizer) {
        return lineColor(symbolizer);
    }

    /**
     * Retrieve linestring width from symbolizer if available.
     *
     * @param symbolizer Line symbolizer information.
     *
     * @return width of linestring, or NOTFOUND
     */
    public static int lineWidth(LineSymbolizer symbolizer) {
        if (symbolizer == null) {
            return NOTFOUND;
        }

        Stroke stroke = symbolizer.getStroke();

        return width(stroke);
    }

    public static int width(Stroke stroke) {
        if (stroke == null) {
            return NOTFOUND;
        }

        return intValue(stroke.getWidth());
    }

    public static int size(Mark mark) {
        if (mark == null) {
            return NOTFOUND;
        }

        return intValue(mark.getSize());
    }

    /**
     * Retrieve linestring width from symbolizer if available.
     *
     * @param symbolizer Line symbolizer information.
     *
     * @return width of linestring, or NOTFOUND
     */
    public static int width(LineSymbolizer symbolizer) {
        return lineWidth(symbolizer);
    }

    /**
     * Grabs the opacity from the first LineSymbolizer.
     *
     * @param symbolizer Line symbolizer information.
     *
     * @return double of the line stroke's opacity, or NaN if unavailable.
     */
    public static double lineOpacity(LineSymbolizer symbolizer) {
        if (symbolizer == null) {
            return Double.NaN;
        }

        Stroke stroke = symbolizer.getStroke();

        return opacity(stroke);
    }
    public static double opacity(Stroke stroke) {
        if (stroke == null) {
            return Double.NaN;
        }
        return opacity( stroke.getOpacity() );
    }
    public static double opacity(RasterSymbolizer rasterSymbolizer ){
        if( rasterSymbolizer == null ){
            return 1.0;
        }
        return opacity( rasterSymbolizer.getOpacity() );
    }

    private static double opacity( Expression opacity ) {
        if( opacity == null ){
            return 1.0;
        }
        Double numeric = (Double) opacity.evaluate( null, Double.class);        
        return numeric.doubleValue();
    }

    /**
     * Grabs the linejoin from the first LineSymbolizer.
     *
     * @param symbolizer Line symbolizer information.
     *
     * @return String of the line stroke's linejoin, or null if unavailable.
     */
    public static String lineLinejoin(LineSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Stroke stroke = symbolizer.getStroke();

        if (stroke == null) {
            return null;
        }

        Expression linejoinExp = stroke.getLineJoin();

        return linejoinExp.toString();
    }

    /**
     * Grabs the linecap from the first LineSymbolizer.
     *
     * @param symbolizer Line symbolizer information.
     *
     * @return String of the line stroke's linecap, or null if unavailable.
     */
    public static String lineLinecap(LineSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Stroke stroke = symbolizer.getStroke();

        if (stroke == null) {
            return null;
        }

        Expression linecapExp = stroke.getLineCap();

        return linecapExp.toString();
    }

    /**
     * Grabs the dashes array from the first LineSymbolizer.
     *
     * @param symbolizer Line symbolizer information.
     *
     * @return float[] of the line dashes array, or null if unavailable.
     */
    public static float[] lineDash(LineSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Stroke stroke = symbolizer.getStroke();

        if (stroke == null) {
            return null;
        }

        float[] linedash = stroke.getDashArray();

        return linedash;
    }

    /**
     * Grabs the location of the first external graphic.
     *
     * @param style SLD style information.
     *
     * @return Location of the first external graphic, or null
     */
    public static URL pointGraphic(Style style) {
        PointSymbolizer point = pointSymbolizer(style);

        if (point == null) {
            return null;
        }

        Graphic graphic = point.getGraphic();

        if (graphic == null) {
            return null;
        }

        ExternalGraphic[] graphicList = graphic.getExternalGraphics();

        for (int i = 0; i < graphicList.length; i++) {
            ExternalGraphic externalGraphic = graphicList[i];

            if (externalGraphic == null) {
                continue;
            }

            URL location;

            try {
                location = externalGraphic.getLocation(); // Should check format is supported by SWT

                if (location != null) {
                    return location;
                }
            } catch (MalformedURLException e) {
                // ignore, try the next one
            }
        }

        return null;
    }

    public static Mark pointMark(Style style) {
        if (style == null) {
            return null;
        }

        return mark(pointSymbolizer(style));
    }

    public static Mark mark(PointSymbolizer sym) {
        return mark(graphic(sym));
    }

    public static Mark mark(Graphic graphic) {
        if (graphic == null) {
            return null;
        }

        return ((graphic.getMarks() != null) && (graphic.getMarks().length > 0))
        ? graphic.getMarks()[0] : null;
    }

    public static Graphic graphic(PointSymbolizer sym) {
        if (sym == null) {
            return null;
        }

        return sym.getGraphic();
    }

    /**
     * Grabs the size of the points graphic, if found.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Point symbolizer information.
     *
     * @return size of the graphic
     */
    public static int pointSize(PointSymbolizer symbolizer) {
        if (symbolizer == null) {
            return NOTFOUND;
        }

        Graphic g = symbolizer.getGraphic();

        if (g == null) {
            return NOTFOUND;
        }

        Expression exp = g.getSize();
        int size = intValue(exp);

        return size;
    }

    /**
     * Grabs the well known name of the first Mark that has one.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Point symbolizer information.
     *
     * @return well known name of the first Mark
     */
    public static String pointWellKnownName(PointSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Graphic g = symbolizer.getGraphic();

        if (g == null) {
            return null;
        }

        Mark[] markList = g.getMarks();

        for (int i = 0; i < markList.length; i++) {
            Mark mark = markList[i];

            if (mark == null) {
                continue;
            }

            String string = wellKnownName(mark);

            if (string == null) {
                continue;
            }

            return string;
        }

        return null;
    }

    public static String wellKnownName(Mark mark) {
        if (mark == null) {
            return null;
        }

        Expression exp = mark.getWellKnownName();

        if (exp == null) {
            return null;
        }

        String string = stringValue(exp);

        return string;
    }

    /**
     * Grabs the color from the first Mark.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Point symbolizer information.
     *
     * @return Color of the point's mark, or null if unavailable.
     */
    public static Color pointColor(PointSymbolizer symbolizer) {
        return color(symbolizer);
    }

    /**
     * Sets the Colour for the point symbolizer
     *
     * @param style
     * @param colour
     */
    public static void setPointColour(Style style, Color colour) {
        if (style == null) {
            return;
        }

        setPointColour(pointSymbolizer(style), colour);
    }

    /**
     * Sets the Colour for the given point symbolizer
     *
     * @param symbolizer
     * @param colour
     */
    public static void setPointColour(PointSymbolizer symbolizer, Color colour) {
        if (symbolizer == null) {
            return;
        }

        Graphic graphic = symbolizer.getGraphic();

        if (graphic == null) {
            graphic = builder.createGraphic();
        }

        Mark[] markList = graphic.getMarks();

        for (int i = 0; i < markList.length; i++) {
            Mark mark = markList[i];

            if (mark == null) {
                continue;
            }

            Stroke stroke = mark.getStroke();

            if (stroke == null) {
                stroke = builder.createStroke(Color.BLACK); //pretty black outline
                mark.setStroke(stroke);
            }

            if (colour != null) {
                Fill fill = mark.getFill();

                if (fill == null) {
                    continue;
                }

                fill.setColor(builder.colorExpression(colour));
            }
        }
    }

    /**
     * Grabs the color from the first Mark.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Point symbolizer information.
     *
     * @return Color of the point's mark, or null if unavailable.
     */
    public static Color color(PointSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Graphic graphic = symbolizer.getGraphic();

        if (graphic == null) {
            return null;
        }

        Mark[] markList = graphic.getMarks();

        for (int i = 0; i < markList.length; i++) {
            Mark mark = markList[i];

            if (mark == null) {
                continue;
            }

            Stroke stroke = mark.getStroke();

            if (stroke == null) {
                continue;
            }

            Color colour = color(stroke.getColor());

            if (colour != null) {
                return colour;
            }
        }

        return null;
    }

    /**
     * Grabs the width of the first Mark with a Stroke that has a non-null
     * width.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Point symbolizer information.
     *
     * @return width of the points border
     */
    public static int pointWidth(PointSymbolizer symbolizer) {
        if (symbolizer == null) {
            return NOTFOUND;
        }

        Graphic g = symbolizer.getGraphic();

        if (g == null) {
            return NOTFOUND;
        }

        Mark[] markList = g.getMarks();

        for (int i = 0; i < markList.length; i++) {
            Mark mark = markList[i];

            if (mark == null) {
                continue;
            }

            Stroke stroke = mark.getStroke();

            if (stroke == null) {
                continue;
            }

            Expression exp = stroke.getWidth();

            if (exp == null) {
                continue;
            }

            int width = intValue(exp);

            if (width == NOTFOUND) {
                continue;
            }

            return width;
        }

        return NOTFOUND;
    }

    /**
     * Grabs the point border opacity from the first PointSymbolizer.
     * 
     * <p>
     * If you are using something fun like rules you  will need to do your own
     * thing.
     * </p>
     *
     * @param symbolizer Point symbolizer information.
     *
     * @return double of the point's border opacity, or NaN if unavailable.
     */
    public static double pointBorderOpacity(PointSymbolizer symbolizer) {
        if (symbolizer == null) {
            return Double.NaN;
        }

        Graphic graphic = symbolizer.getGraphic();

        if (graphic == null) {
            return Double.NaN;
        }

        Mark[] markList = graphic.getMarks();

        for (int i = 0; i < markList.length; i++) {
            Mark mark = markList[i];

            if (mark == null) {
                continue;
            }

            Stroke stroke = mark.getStroke();

            if (stroke == null) {
                continue;
            }

            Expression opacityExp = stroke.getOpacity();

            return Double.parseDouble(opacityExp.toString());
        }

        return Double.NaN;
    }

    /**
     * Grabs the point opacity from the first PointSymbolizer.
     * <p>
     * If you are using something fun like rules you  will need to do your own
     * thing.
     * </p>
     *
     * @param symbolizer Point symbolizer information.
     *
     * @return double of the point's opacity, or NaN if unavailable.
     */
    public static double pointOpacity(PointSymbolizer symbolizer) {
        if (symbolizer == null) {
            return Double.NaN;
        }

        Graphic graphic = symbolizer.getGraphic();

        if (graphic == null) {
            return Double.NaN;
        }

        Mark[] markList = graphic.getMarks();

        for (int i = 0; i < markList.length; i++) {
            Mark mark = markList[i];

            if (mark == null) {
                continue;
            }

            Fill fill = mark.getFill();

            if (fill == null) {
                continue;
            }
            Expression expr = fill.getOpacity();
            if( expr == null ){
                continue;
            }
            return SLD.opacity( expr );
        }

        return Double.NaN;
    }

    /**
     * Grabs the fill from the first Mark.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Point symbolizer information.
     *
     * @return Color of the point's fill, or null if unavailable.
     */
    public static Color pointFill(PointSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Graphic graphic = symbolizer.getGraphic();

        if (graphic == null) {
            return null;
        }

        Mark[] markList = graphic.getMarks();

        for (int i = 0; i < markList.length; i++) {
            Mark mark = markList[i];

            if (mark == null) {
                continue;
            }

            Fill fill = mark.getFill();

            if (fill == null) {
                continue;
            }

            Color colour = color(fill.getColor());

            if (colour != null) {
                return colour;
            }
        }

        return null;
    }

    /**
     * Grabs the color from the first PolygonSymbolizer.
     * 
     * <p>
     * If you are using something fun like rules you  will need to do your own
     * thing.
     * </p>
     *
     * @param symbolizer Polygon symbolizer information.
     *
     * @return Color of the polygon's stroke, or null if unavailable.
     */
    public static int polyWidth(PolygonSymbolizer symbolizer) {
        if (symbolizer == null) {
            return NOTFOUND;
        }

        Stroke stroke = symbolizer.getStroke();

        if (stroke == null) {
            return NOTFOUND;
        }

        int width = intValue(stroke.getWidth());

        return width;
    }

    /**
     * Grabs the color from the first PolygonSymbolizer.
     * 
     * <p>
     * If you are using something fun like rules you  will need to do your own
     * thing.
     * </p>
     *
     * @param symbolizer Polygon symbolizer information.
     *
     * @return Color of the polygon's stroke, or null if unavailable.
     */
    public static Color polyColor(PolygonSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Stroke stroke = symbolizer.getStroke();

        if (stroke == null) {
            return null;
        }

        Color colour = color(stroke.getColor());

        if (colour != null) {
            return colour;
        }

        return null;
    }

    /**
     * Updates the raster opacity in the current style
     * 
     * <p>
     * This method will update the Style in place; some of the rules &
     * symbolizers will be replace with modified copies.
     * 
     *  All symbolizers associated with all rules are modified.
     * </p>
     * 
     * @param style
     * @param opacity - new opacity value between 0 and 1
     */
    public static void setRasterOpacity(Style style, final double opacity){
    	if (style == null){
    		return;
    	}
    	for (FeatureTypeStyle featureTypeStyle : style.getFeatureTypeStyles()) {
			for (int i = 0; i < featureTypeStyle.rules().size(); i++) {
				Rule rule = featureTypeStyle.rules().get(i);
				
				DuplicatingStyleVisitor update = new DuplicatingStyleVisitor() {
					public void visit(RasterSymbolizer raster) {
						
						ChannelSelection channelSelection = copy(raster.getChannelSelection());
						ColorMap colorMap = copy(raster.getColorMap());
						ContrastEnhancement ce = copy(raster.getContrastEnhancement());
						String geometryProperty = raster.getGeometryPropertyName();
						Symbolizer outline = copy(raster.getImageOutline());			
						Expression overlap = copy(raster.getOverlap());
						ShadedRelief shadedRelief = copy(raster.getShadedRelief());
						
						Expression newOpacity = ff.literal(opacity);
						
						RasterSymbolizer copy = sf.createRasterSymbolizer(geometryProperty, newOpacity, channelSelection, 
								overlap, colorMap, ce, shadedRelief, outline);
						
				        if( STRICT && !copy.equals( raster )){
				            throw new IllegalStateException("Was unable to duplicate provided raster:"+raster );
				        }
				        pages.push(copy);
					}
				};
				
				rule.accept(update);
				Rule updatedRule = (Rule) update.getCopy();
				featureTypeStyle.rules().set(i, updatedRule);
			}
		} 
    }
    
    /**
     * Updates the raster channel selection in the current style
     * 
     * <p>
     * This method will update the Style in place; some of the rules &
     * symbolizers will be replace with modified copies.
     * 
     *  All symbolizes associated with all rules are updated.
     * </p>
     * 
     * @param rasterSymbolizer
     * @param rgb - an array of the new red, green, blue channels
     * @param gray - the new gray channel
     * 
     * Only one of rgb or gray should be provided.
     */
    public static void setChannelSelection(Style style, final SelectedChannelType[] rgb, final SelectedChannelType gray){
    	if (style == null){
    		return;
    	}
    	for (FeatureTypeStyle featureTypeStyle : style.getFeatureTypeStyles()) {
			for (int i = 0; i < featureTypeStyle.rules().size(); i++) {
				Rule rule = featureTypeStyle.rules().get(i);

				DuplicatingStyleVisitor update = new DuplicatingStyleVisitor() {
					public void visit(RasterSymbolizer raster) {

						ChannelSelection channelSelection = createChannelSelection();
						
						ColorMap colorMap = copy(raster.getColorMap());
						ContrastEnhancement ce = copy(raster
								.getContrastEnhancement());
						String geometryProperty = raster
								.getGeometryPropertyName();
						Symbolizer outline = copy(raster.getImageOutline());
						Expression overlap = copy(raster.getOverlap());
						ShadedRelief shadedRelief = copy(raster
								.getShadedRelief());

						Expression opacity = copy(raster.getOpacity());

						RasterSymbolizer copy = sf.createRasterSymbolizer(geometryProperty, opacity,
								channelSelection, overlap, colorMap, ce,
								shadedRelief, outline);
				        if( STRICT && !copy.equals( raster )){
				            throw new IllegalStateException("Was unable to duplicate provided raster:"+raster );
				        }
				        pages.push(copy);
					}
					
					private ChannelSelection createChannelSelection(){
						if (rgb == null){
							return sf.createChannelSelection(new SelectedChannelType[] {gray});
						}else{
						  	return sf.createChannelSelection(rgb);
						}
					}
				};

				rule.accept(update);
				Rule updatedRule = (Rule) update.getCopy();
				featureTypeStyle.rules().set(i, updatedRule);
			}
		}
    }
    
    /**
     * Sets the colour for a polygon symbolizer
     *
     * @param style
     * @param colour
     */
    public static void setPolyColour(Style style, Color colour) {
        if (style == null) {
            return;
        }

        setPolyColour(polySymbolizer(style), colour);
    }

    /**
     * Sets the Colour for the given polygon symbolizer
     *
     * @param symbolizer
     * @param colour
     */
    public static void setPolyColour(PolygonSymbolizer symbolizer, Color colour) {
        if (symbolizer == null) {
            return;
        }

        Stroke stroke = symbolizer.getStroke();

        if (stroke == null) {
            stroke = builder.createStroke(colour);
            symbolizer.setStroke(stroke);
        }

        if (colour != null) {
            stroke.setColor(builder.colorExpression(colour));

            Fill fill = symbolizer.getFill();

            if (fill != null) {
                fill.setColor(builder.colorExpression(colour));
            }
        }
    }

    /**
     * Grabs the fill from the first PolygonSymbolizer.
     * 
     * <p>
     * If you are using something fun like rules you  will need to do your own
     * thing.
     * </p>
     *
     * @param symbolizer Polygon symbolizer information.
     *
     * @return Color of the polygon's fill, or null if unavailable.
     */
    public static Color polyFill(PolygonSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Fill fill = symbolizer.getFill();

        if (fill == null) {
            return null;
        }

        Color colour = color(fill.getColor());

        if (colour != null) {
            return colour;
        }

        return null;
    }

    /**
     * Grabs the border opacity from the first PolygonSymbolizer.
     * 
     * <p>
     * If you are using something fun like rules you  will need to do your own
     * thing.
     * </p>
     *
     * @param symbolizer Polygon symbolizer information.
     *
     * @return double of the polygon's border opacity, or NaN if unavailable.
     */
    public static double polyBorderOpacity(PolygonSymbolizer symbolizer) {
        if (symbolizer == null) {
            return Double.NaN;
        }

        Stroke stroke = symbolizer.getStroke();

        if (stroke == null) {
            return Double.NaN;
        }

        Expression opacityExp = stroke.getOpacity();
        double opacity = Double.parseDouble(opacityExp.toString());

        return opacity;
    }

    /**
     * Grabs the fill opacity from the first PolygonSymbolizer.
     * 
     * <p>
     * If you are using something fun like rules you  will need to do your own
     * thing.
     * </p>
     *
     * @param symbolizer Polygon symbolizer information.
     *
     * @return double of the polygon's fill opacity, or NaN if unavailable.
     */
    public static double polyFillOpacity(PolygonSymbolizer symbolizer) {
        if (symbolizer == null) {
            return Double.NaN;
        }

        Fill fill = symbolizer.getFill();

        return opacity(fill);
    }
    /**
     * Retrieve the opacity from the provided fill; or return the default.
     * @param fill
     * @return opacity from the above fill; or return the Fill.DEFAULT value
     */
    public static double opacity(Fill fill) {
        if (fill == null) {
            fill = Fill.DEFAULT;
        }

        Expression opacityExp = fill.getOpacity();
        if( opacityExp == null ){
            opacityExp = Fill.DEFAULT.getOpacity();
        }
        double opacity = Filters.asDouble(opacityExp);
        
        return opacity;
    }

    /**
     * Grabs the opacity from the first RasterSymbolizer.
     * 
     * <p>
     * If you are using something fun like rules you  will need to do your own
     * thing.
     * </p>
     *
     * @param symbolizer Raster symbolizer information.
     *
     * @return opacity of the first RasterSymbolizer
     */
    public static double rasterOpacity(RasterSymbolizer symbolizer) {
        if (symbolizer == null) {
            return Double.NaN;
        }

        return doubleValue(symbolizer.getOpacity());
    }

    public static double rasterOpacity(Style style) {
        return rasterOpacity(rasterSymbolizer(style));
    }

    /**
     * Retrieve the first TextSymbolizer from the provided Style.
     *
     * @param fts SLD featureTypeStyle information.
     *
     * @return TextSymbolizer, or null if not found.
     */
    public static TextSymbolizer textSymbolizer(FeatureTypeStyle fts) {
        return (TextSymbolizer) symbolizer(fts, TextSymbolizer.class);
    }

    /**
     * Retrieve the first TextSymbolizer from the provided Style.
     *
     * @param style SLD style information.
     *
     * @return TextSymbolizer, or null if not found.
     */
    public static TextSymbolizer textSymbolizer(Style style) {
        return (TextSymbolizer) symbolizer(style, TextSymbolizer.class);
    }

    /**
     * Grabs the label from the first TextSymbolizer.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Text symbolizer information.
     *
     * @return Expression of the label's text, or null if unavailable.
     */
    public static Expression textLabel(TextSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Expression exp = symbolizer.getLabel();

        if (exp == null) {
            return null;
        }

        return exp;
    }

    public static String textLabelString(TextSymbolizer sym) {
        Expression exp = textLabel(sym);

        return (exp == null) ? null : exp.toString();
    }

    /**
     * Grabs the fontFill from the first TextSymbolizer.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Text symbolizer information.
     *
     * @return Color of the font's fill, or null if unavailable.
     */
    public static Color textFontFill(TextSymbolizer symbolizer) {
        if (symbolizer == null) {
            return null;
        }

        Fill fill = symbolizer.getFill();

        if (fill == null) {
            return null;
        }

        Color colour = color(fill.getColor());

        if (colour != null) {
            return colour;
        }

        return null;
    }

    /**
     * Grabs the haloFill from the first TextSymbolizer.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Text symbolizer information.
     *
     * @return Color of the halo's fill, or null if unavailable.
     */
    public static Color textHaloFill(TextSymbolizer symbolizer) {
        Halo halo = symbolizer.getHalo();

        if (halo == null) {
            return null;
        }

        Fill fill = halo.getFill();

        if (fill == null) {
            return null;
        }

        Color colour = color(fill.getColor());

        if (colour != null) {
            return colour;
        }

        return null;
    }

    /**
     * Grabs the halo width from the first TextSymbolizer.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Text symbolizer information.
     *
     * @return float of the halo's width, or null if unavailable.
     */
    public static int textHaloWidth(TextSymbolizer symbolizer) {
        Halo halo = symbolizer.getHalo();

        if (halo == null) {
            return 0;
        }

        Expression exp = halo.getRadius();

        if (exp == null) {
            return 0;
        }

        int width = (int) Float.parseFloat(exp.toString());

        if (width != 0) {
            return width;
        }

        return 0;
    }

    /**
     * Grabs the halo opacity from the first TextSymbolizer.
     * 
     * <p>
     * If you are using something fun like symbols you  will need to do your
     * own thing.
     * </p>
     *
     * @param symbolizer Text symbolizer information.
     *
     * @return double of the halo's opacity, or NaN if unavailable.
     */
    public static double textHaloOpacity(TextSymbolizer symbolizer) {
        if (symbolizer == null) {
            return Double.NaN;
        }

        Halo halo = symbolizer.getHalo();

        if (halo == null) {
            return Double.NaN;
        }

        Fill fill = halo.getFill();

        if (fill == null) {
            return Double.NaN;
        }

        Expression expr = fill.getOpacity();
        if( expr == null ){
            return Double.NaN;
        }
        Double numeric = (Double) expr.evaluate( null, Double.class);
        if( numeric == null ){
            return Double.NaN;
        }
        return numeric.doubleValue();
    }

    /**
     * Navigate through the expression finding the first mentioned Color.
     * 
     * <p>
     * If you have a specific Feature in mind please use:
     * <pre><code>
     * Object value = expr.getValue( feature );
     * return value instanceof Color ? (Color) value : null;
     * </code></pre>
     * </p>
     *
     * @param expr
     *
     * @return First available color, or null.
     */
    public static Color color(Expression expr) {
        if (expr == null) {
            return null;
        }
        return expr.evaluate(null, Color.class );
        /*
        Color color = (Color) value(expr, Color.class);

        if (color != null) {
            return color;
        }

        String rgba = (String) value(expr, String.class);

        try {
            color = Color.decode(rgba);

            if (color != null) {
                return color;
            }
        } catch (NumberFormatException badRGB) {
            // unavailable
        }
        return null;
        */
    }

    /**
     * This method is here for backward compatability.
     *
     * @param expr DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @see Filters#intValue(Expression)
     * @deprecated Use expr.evaulate( null, Integer.class )
     */
    public static int intValue(Expression expr) {
        return Filters.asInt(expr);
    }

    /**
     * This method is here for backward compatability.
     *
     * @param expr DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @see Filters#stringValue(Expression)
     * @deprecated
     */
    public static String stringValue(Expression expr) {
        return Filters.asString(expr);
    }

    /**
     * This method is here for backward compatability.
     *
     * @param expr DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @see Filters#doubleValue(Expression)
     * @deprecated
     */
    public static double doubleValue(Expression expr) {
        return Filters.asDouble(expr);
    }

    /**
     * Retrieve the first RasterSymbolizer from the provided Style.
     *
     * @param fts SLD featureTypeStyle information.
     *
     * @return RasterSymbolizer, or null if not found.
     */
    public static RasterSymbolizer rasterSymbolizer(FeatureTypeStyle fts) {
        return (RasterSymbolizer) symbolizer(fts, RasterSymbolizer.class);
    }

    /**
     * Retrieve the first RasterSymbolizer from the provided Style.
     *
     * @param style SLD style information.
     *
     * @return RasterSymbolizer, or null if not found.
     */
    public static RasterSymbolizer rasterSymbolizer(Style style) {
        return (RasterSymbolizer) symbolizer(style, RasterSymbolizer.class);
    }

    /**
     * Retrieve the first LineSymbolizer from the provided Style.
     *
     * @param fts SLD featureTypeStyle information.
     *
     * @return LineSymbolizer, or null if not found.
     */
    public static LineSymbolizer lineSymbolizer(FeatureTypeStyle fts) {
        return (LineSymbolizer) symbolizer(fts, LineSymbolizer.class);
    }

    /**
     * Retrieve the first LineSymbolizer from the provided Style.
     *
     * @param style SLD style information.
     *
     * @return LineSymbolizer, or null if not found.
     */
    public static LineSymbolizer lineSymbolizer(Style style) {
        return (LineSymbolizer) symbolizer(style, LineSymbolizer.class);
    }

    public static Stroke stroke(LineSymbolizer sym) {
        if (sym == null) {
            return null;
        }

        return sym.getStroke();
    }

    public static Stroke stroke(PolygonSymbolizer sym) {
        if (sym == null) {
            return null;
        }

        return sym.getStroke();
    }

    public static Stroke stroke(PointSymbolizer sym) {
        Mark mark = mark(sym);

        return (mark == null) ? null : mark.getStroke();
    }

    public static Fill fill(PolygonSymbolizer sym) {
        if (sym == null) {
            return null;
        }

        return sym.getFill();
    }

    public static Fill fill(PointSymbolizer sym) {
        Mark mark = mark(sym);

        return (mark == null) ? null : mark.getFill();
    }

    /**
     * Retrieve the first PointSymbolizer from the provided FeatureTypeStyle.
     *
     * @param fts SLD featureTypeStyle information.
     *
     * @return PointSymbolizer, or null if not found.
     */
    public static PointSymbolizer pointSymbolizer(FeatureTypeStyle fts) {
        return (PointSymbolizer) symbolizer(fts, PointSymbolizer.class);
    }

    /**
     * Retrieve the first PointSymbolizer from the provided Style.
     *
     * @param style SLD style information.
     *
     * @return PointSymbolizer, or null if not found.
     */
    public static PointSymbolizer pointSymbolizer(Style style) {
        return (PointSymbolizer) symbolizer(style, PointSymbolizer.class);
    }

    /**
     * Retrieve the first PolygonSymbolizer from the provided Style.
     *
     * @param fts SLD featureTypeStyle information.
     *
     * @return PolygonSymbolizer, or null if not found.
     */
    public static PolygonSymbolizer polySymbolizer(FeatureTypeStyle fts) {
        return (PolygonSymbolizer) symbolizer(fts, PolygonSymbolizer.class);
    }

    /**
     * Retrieve the first PolygonSymbolizer from the provided Style.
     *
     * @param style SLD style information.
     *
     * @return PolygonSymbolizer, or null if not found.
     */
    public static PolygonSymbolizer polySymbolizer(Style style) {
        return (PolygonSymbolizer) symbolizer(style, PolygonSymbolizer.class);
    }

    /**
     * Returns the feature type style in the style which matched a particular
     * name.
     *
     * @param style The style in question.
     * @param type The feature type must be non-null.
     *
     * @return Teh FeatureTypeStyle object if it exists, otherwise false.
     */
    public static FeatureTypeStyle featureTypeStyle(Style style,
        SimpleFeatureType type) {
        if (style == null) {
            return null;
        }

        if ((type == null) || (type.getTypeName() == null)) {
            return null;
        }

        FeatureTypeStyle[] styles = style.getFeatureTypeStyles();

        if (styles == null) {
            return null;
        }

        for (int i = 0; i < styles.length; i++) {
            FeatureTypeStyle ftStyle = styles[i];

            if (type.getTypeName().equals(ftStyle.getName())) {
                return ftStyle;
            }
        }

        return null;
    }

    /**
     * Returns the first style object which matches a given schema.
     *
     * @param styles Array of style objects.
     * @param schema Feature schema.
     *
     * @return The first object to match the feature type, otherwise null if no
     *         match.
     */
    public static Style matchingStyle(Style[] styles, SimpleFeatureType schema) {
        if ((styles == null) || (styles.length == 0)) {
            return null;
        }

        for (int i = 0; i < styles.length; i++) {
            Style style = styles[i];

            if (featureTypeStyle(style, schema) != null) {
                return style;
            }
        }

        return null;
    }

    /**
     * Retrieve the first SYMBOLIZER from the provided Style.
     *
     * @param style SLD style information.
     * @param SYMBOLIZER LineSymbolizer.class, PointSymbolizer.class, 
     *        PolygonSymbolizer.class, RasterSymbolizer.class, or TextSymbolizer.class
     *
     * @return symbolizer instance from style, or null if not found.
     */
    protected static Symbolizer symbolizer(Style style, final Class SYMBOLIZER) {
        if (style == null) {
            return null;
        }

        FeatureTypeStyle[] ftStyleList = style.getFeatureTypeStyles();

        if (ftStyleList == null) {
            return null;
        }

        for (int i = 0; i < ftStyleList.length; i++) {
            FeatureTypeStyle ftStyle = ftStyleList[i];
            Symbolizer result = symbolizer(ftStyle, SYMBOLIZER);
            if (result != null) return result;
        }
        return null;
    }

    /**
     * Retrieve the first SYMBOLIZER from the provided FeatureTypeStyle.
     *
     * @param fts        the FeatureTypeStyle SLD style information.
     * @param SYMBOLIZER LineSymbolizer.class, PointSymbolizer.class, 
     *                   PolygonSymbolizer.class, RasterSymbolizer.class, 
     *                   or TextSymbolizer.class
     *        
     * @return symbolizer instance from fts, or null if not found.
     */
    protected static Symbolizer symbolizer(FeatureTypeStyle fts, final Class SYMBOLIZER) {
        if (fts == null) {
            return null;
        }

        Rule[] ruleList = fts.getRules();
        if (ruleList == null) {
            return null;
        }

RULE: 
        for (int j = 0; j < ruleList.length; j++) {
            Rule rule = ruleList[j];
            Symbolizer[] symbolizerList = rule.getSymbolizers();

            if (symbolizerList == null) {
                continue RULE;
            }

SYMBOLIZER: 
            for (int k = 0; k < symbolizerList.length; k++) {
                Symbolizer symbolizer = symbolizerList[k];

                if (symbolizer == null) {
                    continue SYMBOLIZER;
                }

                if (SYMBOLIZER.isInstance(symbolizer)) {
                    return symbolizer;
                }
            }
        }
        return null;
    }
    
    public static String colorToHex(Color c) {
    	return "#" + Integer.toHexString(c.getRGB() & 0x00ffffff);
    }
    
    public static Style[] styles(StyledLayerDescriptor sld) {
        StyledLayer[] layers = sld.getStyledLayers();
        List styles = new ArrayList();
        for (int i = 0; i < layers.length; i++) {
            if (layers[i] instanceof UserLayer) {
                UserLayer layer = (UserLayer) layers[i];
                styles.addAll(toList(layer.getUserStyles()));
            } else if (layers[i] instanceof NamedLayer) {
                NamedLayer layer = (NamedLayer) layers[i];
                styles.addAll(toList(layer.getStyles()));
            }
        }
        return (Style[]) styles.toArray(new Style[styles.size()]);
    }
    
    public static FeatureTypeStyle[] featureTypeStyles(StyledLayerDescriptor sld) {
        Style[] style = styles(sld);
        List fts = new ArrayList();
        for (int i = 0; i < style.length; i++) {
            fts.addAll(toList(style[i].getFeatureTypeStyles()));
        }
        return (FeatureTypeStyle[]) fts.toArray(new FeatureTypeStyle[fts.size()]);
    }
    
    public static FeatureTypeStyle featureTypeStyle(StyledLayerDescriptor sld, SimpleFeatureType type) {
        //alternatively, we could use a StyleVisitor here
        Style[] styles = styles(sld);
        for (int i = 0; i < styles.length; i++) {
            FeatureTypeStyle[] fts = styles[i].getFeatureTypeStyles();
            for (int j = 0; j < fts.length; j++) {
                if (type.getTypeName().equals(fts[j].getName())) {
                    return fts[j];
                }
            }
        }
        return null;
    }
    
    private static List toList(Object[] array) {
        List list = new ArrayList();
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }
    
    public static Style defaultStyle(StyledLayerDescriptor sld) {
        Style[] style = styles(sld);
        for (int i = 0; i < style.length; i++) {
            if (style[i].isDefault()) {
                return style[i];
            }
        }
        //no default, so just grab the first one
        if( style.length == 0 ){
        	return null;
        }
        return style[0];
    }
    
	public static Filter[] filters(Rule[] rule) {
		Filter[] filter = new Filter[rule.length];
		for (int i = 0; i < rule.length; i++) {
			filter[i] = rule[0].getFilter();
		}
		return filter;
	}
    
	public static Filter[] filters(Style style) {
		Rule[] rule = rules(style);
		return filters(rule);
	}
    
	public static Rule[] rules(Style style) {
		Set<Rule> ruleSet = new HashSet<Rule>();
		FeatureTypeStyle[] fts = style.getFeatureTypeStyles();
		for (int i = 0; i < fts.length; i++) {
			Rule[] ftsRules = fts[i].getRules();
			for (int j = 0; j < ftsRules.length; j++) {
				ruleSet.add(ftsRules[j]);
			}
		}
		if (ruleSet.size() > 0) {
			return toRuleArray(ruleSet.toArray());
		} else {
			return new Rule[0];
		}
	}

	public static Symbolizer[] symbolizers(Style style) {
		Set symbolizers = new HashSet();
		Rule[] rule = rules(style);
		for (int i = 0; i < rule.length; i++) {
			Symbolizer[] symb = rule[i].getSymbolizers();
			for (int j = 0; j < symb.length; j++) {
				symbolizers.add(symb[j]);
			}
		}
		if (symbolizers.size() > 0) {
			return toSymbolizerArray(symbolizers.toArray());
		} else {
			return new Symbolizer[0];
		}
	}
	
	public static Symbolizer[] symbolizers(Rule rule) {
		Set symbolizers = new HashSet();
		Symbolizer[] symb = rule.getSymbolizers();
		for (int j = 0; j < symb.length; j++) {
			symbolizers.add(symb[j]);
		}
		if (symbolizers.size() > 0) {
			return toSymbolizerArray(symbolizers.toArray());
		} else {
			return new Symbolizer[0];
		}
	}
	
	public static String[] colors(Style style) {
		Set colorSet = new HashSet();
		Rule[] rule = rules(style);
		for (int i = 0; i < rule.length; i++) {
			String[] color = colors(rule[i]);
			for (int j = 0; j < color.length; j++) {
				colorSet.add(color[j]);
			}
		}
		if (colorSet.size() > 0) {
			return toStringArray(colorSet.toArray());
		} else {
			return new String[0];
		}
	}
    
	public static String[] colors(Rule rule) {
		Set colorSet = new HashSet();
		Symbolizer[] symbolizer = rule.getSymbolizers();
		for (int i = 0; i < symbolizer.length; i++) {
			if (symbolizer[i] instanceof PolygonSymbolizer) {
				PolygonSymbolizer symb = (PolygonSymbolizer) symbolizer[i];
				colorSet.add(symb.getFill().getColor().toString());
			} else if (symbolizer[i] instanceof LineSymbolizer) {
				LineSymbolizer symb = (LineSymbolizer) symbolizer[i];
				colorSet.add(symb.getStroke().getColor().toString());		
			} else if (symbolizer[i] instanceof PointSymbolizer) {
				PointSymbolizer symb = (PointSymbolizer) symbolizer[i];
				colorSet.add(symb.getGraphic().getMarks()[0].getFill().getColor().toString());	
			}
		}
		if (colorSet.size() > 0) {
			return toStringArray(colorSet.toArray());
		} else {
			return new String[0];
		}
	}

	private static String[] toStringArray(Object[] object) {
		String[] result = new String[object.length];
		for (int i = 0; i < object.length; i++) {
			result[i] = (String) object[i];
		}
		return result;
	}

	private static Rule[] toRuleArray(Object[] object) {
		Rule[] result = new Rule[object.length];
		for (int i = 0; i < object.length; i++) {
			result[i] = (Rule) object[i];
		}
		return result;
	}

	private static Symbolizer[] toSymbolizerArray(Object[] object) {
		Symbolizer[] result = new Symbolizer[object.length];
		for (int i = 0; i < object.length; i++) {
			result[i] = (Symbolizer) object[i];
		}
		return result;
	}
	
	/**
	 * Converts a java.awt.Color into an HTML Colour
	 * 
	 * @param color
	 * @return HTML Color (fill) in hex #RRGGBB
	 */
	public static String toHTMLColor(Color color) {
		String red = "0" + Integer.toHexString(color.getRed());
		red = red.substring(red.length() - 2);
		String grn = "0" + Integer.toHexString(color.getGreen());
		grn = grn.substring(grn.length() - 2);
		String blu = "0" + Integer.toHexString(color.getBlue());
		blu = blu.substring(blu.length() - 2);
		return ("#" + red + grn + blu).toUpperCase();
	}

	public static Color toColor(String htmlColor) {
		return new Color(Integer.parseInt(htmlColor.substring(1), 16));
	}

    /**
     * Grabs the font from the first TextSymbolizer.
     * <p>
     * If you are using something fun like symbols you 
     * will need to do your own thing.
     * </p>
     * @param symbolizer Text symbolizer information.
     * @return FontData[] of the font's fill, or null if unavailable.
     */
    public static Font font( TextSymbolizer symbolizer ) {
        if(symbolizer == null) return null;
        Font[] font = symbolizer.getFonts();
        if(font == null || font[0] == null ) return null;
        return font[0];
    }

    public static Style getDefaultStyle( StyledLayerDescriptor sld ) {
        Style[] styles = styles(sld);
        for (int i = 0; i < styles.length; i++) {
            if (styles[i].isDefault()) {
                return styles[i];
            }
        }
        //no default, so just grab the first one
        return styles[0];
    }

    public static boolean isSemanticTypeMatch( FeatureTypeStyle fts, String regex ) {
        String[] identifiers = fts.getSemanticTypeIdentifiers();
        for (int i = 0; i < identifiers.length; i++) {
            if (identifiers[i].matches(regex)) return true;
        }
        return false;
    }

    /**
     * Returns the min scale of the default rule, or 0 if none is set 
     */
    public static double minScale( FeatureTypeStyle fts ) {
        if(fts == null || fts.getRules().length == 0)
            return 0.0;
    
        Rule r = fts.getRules()[0]; 
        return r.getMinScaleDenominator();
    }

    /**
     * Returns the max scale of the default rule, or {@linkplain Double#NaN} if none is set 
     */
    public static double maxScale( FeatureTypeStyle fts ) {
        if(fts == null || fts.getRules().length == 0)
            return Double.NaN;
    
        Rule r = fts.getRules()[0]; 
        return r.getMaxScaleDenominator();
    }

    public static PointPlacement getPlacement( double horizAlign, double vertAlign, double rotation ) {
        return builder.createPointPlacement(horizAlign, vertAlign, rotation);
    }

}
