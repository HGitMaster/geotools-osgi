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

import java.net.URL;

import org.geotools.factory.Factory;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;


/**
 * Abstract base class for implementing style factories.
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/api/src/main/java/org/geotools/styling/StyleFactory.java $
 */
public interface StyleFactory extends Factory {
    public TextSymbolizer createTextSymbolizer(Fill fill, Font[] fonts, Halo halo,
        Expression label, LabelPlacement labelPlacement, String geometryPropertyName);

    public ExternalGraphic createExternalGraphic(URL url, String format);

    public ExternalGraphic createExternalGraphic(String uri, String format);

    public AnchorPoint createAnchorPoint(Expression x, Expression y);

    public Displacement createDisplacement(Expression x, Expression y);

    //    public  LinePlacement createLinePlacement();
    public PointSymbolizer createPointSymbolizer();

    //    public  PointPlacement createPointPlacement();
    public Mark createMark(Expression wellKnownName, Stroke stroke, Fill fill, Expression size,
        Expression rotation);

    /**
     * Convinence method for obtaining a mark of a fixed shape
     *
     * @return a Mark that matches the name in this method.
     */
    public Mark getCircleMark();

    /**
     * Convinence method for obtaining a mark of a fixed shape
     *
     * @return a Mark that matches the name in this method.
     */
    public Mark getXMark();

    /**
     * Convinence method for obtaining a mark of a fixed shape
     *
     * @return a Mark that matches the name in this method.
     */
    public Mark getStarMark();

    /**
     * Convinence method for obtaining a mark of a fixed shape
     *
     * @return a Mark that matches the name in this method.
     */
    public Mark getSquareMark();

    /**
     * Convinence method for obtaining a mark of a fixed shape
     *
     * @return a Mark that matches the name in this method.
     */
    public Mark getCrossMark();

    /**
     * Convinence method for obtaining a mark of a fixed shape
     *
     * @return a Mark that matches the name in this method.
     */
    public Mark getTriangleMark();

    /**
     * Creates a new extent.
     *
     * @param name The name of the extent.
     * @param value The value of the extent.
     *
     * @return The new extent.
     */
    public Extent createExtent(String name, String value);

    /**
     * Creates a new feature type constraint.
     *
     * @param featureTypeName The feature type name.
     * @param filter The filter.
     * @param extents The extents.
     *
     * @return The new feature type constaint.
     */
    public FeatureTypeConstraint createFeatureTypeConstraint(String featureTypeName, Filter filter,
        Extent[] extents);

    public LayerFeatureConstraints createLayerFeatureConstraints(
        FeatureTypeConstraint[] featureTypeConstraints);

    public FeatureTypeStyle createFeatureTypeStyle(Rule[] rules);

    /**
     * Creates a new ImageOutline.
     *
     * @param symbolizer A line or polygon symbolizer.
     *
     * @return The new image outline.
     */
    public ImageOutline createImageOutline(Symbolizer symbolizer);

    public LinePlacement createLinePlacement(Expression offset);

    public PolygonSymbolizer createPolygonSymbolizer();

    public Halo createHalo(Fill fill, Expression radius);

    public Fill createFill(Expression color, Expression backgroundColor, Expression opacity,
        Graphic graphicFill);
    
    /**
     * Create default line symbolizer 
     * @return
     */
    public LineSymbolizer createLineSymbolizer();

    public PointSymbolizer createPointSymbolizer(Graphic graphic, String geometryPropertyName);

    public Style createStyle();

    public NamedStyle createNamedStyle();

    public Fill createFill(Expression color, Expression opacity);

    public Fill createFill(Expression color);

    public TextSymbolizer createTextSymbolizer();

    public PointPlacement createPointPlacement(AnchorPoint anchorPoint, Displacement displacement,
        Expression rotation);

    /**
     * A convienice method to make a simple stroke
     *
     * @param color the color of the line
     * @param width the width of the line
     *
     * @return the stroke object
     *
     * @see org.geotools.stroke
     */
    public Stroke createStroke(Expression color, Expression width);

    /**
     * A convienice method to make a simple stroke
     *
     * @param color the color of the line
     * @param width The width of the line
     * @param opacity The opacity of the line
     *
     * @return The stroke
     *
     * @see org.geotools.stroke
     */
    public Stroke createStroke(Expression color, Expression width, Expression opacity);

    /**
     * creates a stroke
     *
     * @param color The color of the line
     * @param width The width of the line
     * @param opacity The opacity of the line
     * @param lineJoin - the type of Line joint
     * @param lineCap - the type of line cap
     * @param dashArray - an array of floats describing the dashes in the line
     * @param dashOffset - where in the dash array to start drawing from
     * @param graphicFill - a graphic object to fill the line with
     * @param graphicStroke - a graphic object to draw the line with
     *
     * @return The completed stroke.
     *
     * @see org.geotools.stroke
     */
    public Stroke createStroke(Expression color, Expression width, Expression opacity,
        Expression lineJoin, Expression lineCap, float[] dashArray, Expression dashOffset,
        Graphic graphicFill, Graphic graphicStroke);

    public Rule createRule();

    public LineSymbolizer createLineSymbolizer(Stroke stroke, String geometryPropertyName);

    public FeatureTypeStyle createFeatureTypeStyle();

    public Graphic createGraphic(ExternalGraphic[] externalGraphics, Mark[] marks,
        Symbol[] symbols, Expression opacity, Expression size, Expression rotation);

    public Font createFont(Expression fontFamily, Expression fontStyle, Expression fontWeight,
        Expression fontSize);

    public Mark createMark();

    public PolygonSymbolizer createPolygonSymbolizer(Stroke stroke, Fill fill,
        String geometryPropertyName);

    public RasterSymbolizer createRasterSymbolizer();

    public RasterSymbolizer createRasterSymbolizer(String geometryPropertyName, Expression opacity,
        ChannelSelection channel, Expression overlap, ColorMap colorMap, ContrastEnhancement ce,
        ShadedRelief relief, Symbolizer outline);

    public RasterSymbolizer getDefaultRasterSymbolizer();

    public ChannelSelection createChannelSelection(SelectedChannelType[] channels);

    public ContrastEnhancement createContrastEnhancement();

    public ContrastEnhancement createContrastEnhancement(Expression gammaValue);

    public SelectedChannelType createSelectedChannelType(String name,
        ContrastEnhancement enhancement);

    /**
     * @deprecated Use {@link #createSelectedChannelType(String, ContrastEnhancement)}
     */
    public SelectedChannelType createSelectedChannelType(String name, Expression gammaValue);

    public ColorMap createColorMap();

    public ColorMapEntry createColorMapEntry();

    public Style getDefaultStyle();

    public Stroke getDefaultStroke();

    public Fill getDefaultFill();

    public Mark getDefaultMark();

    public PointSymbolizer getDefaultPointSymbolizer();

    public PolygonSymbolizer getDefaultPolygonSymbolizer();

    public LineSymbolizer getDefaultLineSymbolizer();

    /**
     * Creates a default Text Symbolizer, using the defaultFill, defaultFont
     * and defaultPointPlacement,  Sets the geometry attribute name to be
     * geometry:text. No Halo is set. <b>The label is not set</b>
     *
     * @return A default TextSymbolizer
     */
    public TextSymbolizer getDefaultTextSymbolizer();

    public Graphic createDefaultGraphic();

    public Graphic getDefaultGraphic();

    public Font getDefaultFont();

    public PointPlacement getDefaultPointPlacement();

    public StyledLayerDescriptor createStyledLayerDescriptor();

    public UserLayer createUserLayer();

    public NamedLayer createNamedLayer();

    public RemoteOWS createRemoteOWS(String service, String onlineResource);

    public ShadedRelief createShadedRelief(Expression reliefFactor);
}
