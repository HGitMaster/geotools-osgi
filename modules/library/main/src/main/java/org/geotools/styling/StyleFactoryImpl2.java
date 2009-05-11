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
 *
 * Created on 14 October 2002, 15:50
 */
package org.geotools.styling;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.measure.unit.Unit;
import javax.swing.Icon;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.style.ColorReplacement;
import org.opengis.style.ContrastMethod;
import org.opengis.style.Description;
import org.opengis.style.ExtensionSymbolizer;
import org.opengis.style.ExternalMark;
import org.opengis.style.GraphicFill;
import org.opengis.style.GraphicLegend;
import org.opengis.style.GraphicStroke;
import org.opengis.style.GraphicalSymbol;
import org.opengis.style.OverlapBehavior;
import org.opengis.style.SemanticType;
import org.opengis.util.InternationalString;


/**
 * Factory for creating Styles; based on the GeoAPI StyleFactory interface.
 * <p>
 * This factory is simple; it just creates styles with no logic or magic default values.
 * For magic default values please read the SE or SLD specification; or use an appropriate
 * builder.
 * 
 * @author Jody Garnett
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/library/main/src/main/java/org/geotools/styling/StyleFactoryImpl.java $
 * @version $Id: StyleFactoryImpl2.java 32736 2009-04-04 06:51:02Z jive $
 */
public class StyleFactoryImpl2 implements org.opengis.style.StyleFactory {
    private FilterFactory2 filterFactory;
	    
    public StyleFactoryImpl2() {
        this( CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints()));
    }

    protected StyleFactoryImpl2(FilterFactory2 factory) {
        filterFactory = factory;
    }
    
    public org.opengis.style.AnchorPoint anchorPoint(Expression x, Expression y) {
        return new AnchorPointImpl( filterFactory, x, y );
    }

    public org.opengis.style.ChannelSelection channelSelection(
            org.opengis.style.SelectedChannelType gray) {
        ChannelSelectionImpl channelSelection = new ChannelSelectionImpl();
        channelSelection.setGrayChannel( gray );
        return channelSelection;
    }
    public org.opengis.style.ChannelSelection channelSelection(
            org.opengis.style.SelectedChannelType red, org.opengis.style.SelectedChannelType green,
            org.opengis.style.SelectedChannelType blue) {
        ChannelSelectionImpl channelSelection = new ChannelSelectionImpl();
        channelSelection.setRGBChannels(red, green,blue);
        return channelSelection;
    }

    public org.opengis.style.ColorMap colorMap(Expression propertyName, Expression... mapping) {
        Expression[] arguments = new Expression[mapping.length + 2];
        arguments[0] = propertyName;
        for( int i=0; i<mapping.length;i++){
            arguments[i+1] = mapping[i];
        }
        Function function = filterFactory.function("Categorize", arguments );
        ColorMapImpl colorMap = new ColorMapImpl( function );
        
        return colorMap;
    }

    public ColorReplacement colorReplacement(Expression propertyName, Expression... mapping) {
        Expression[] arguments = new Expression[mapping.length + 2];
        arguments[0] = propertyName;
        for( int i=0; i<mapping.length;i++){
            arguments[i+1] = mapping[i];
        }
        Function function = filterFactory.function("Recode", arguments );
        ColorReplacementImpl colorMap = new ColorReplacementImpl( function );
        
        return colorMap;
    }

    public org.opengis.style.ContrastEnhancement contrastEnhancement(Expression gamma,
            ContrastMethod method) {
        return new ContrastEnhancementImpl( filterFactory, gamma, method );
    }

    public Description description(InternationalString title, InternationalString description) {
        return new DescriptionImpl( title, description );
    }

    public org.opengis.style.Displacement displacement(Expression dx, Expression dy) {
        return new DisplacementImpl( dx, dy );
    }

    public org.opengis.style.ExternalGraphic externalGraphic(Icon inline,
            Collection<ColorReplacement> replacements) {
        ExternalGraphicImpl externalGraphic = new ExternalGraphicImpl( inline, replacements, null );
        return externalGraphic;
    }

    public org.opengis.style.ExternalGraphic externalGraphic(OnLineResource resource,
            String format, Collection<ColorReplacement> replacements) {
        ExternalGraphicImpl externalGraphic = new ExternalGraphicImpl( null, replacements, resource);
        externalGraphic.setFormat( format );
        return externalGraphic;
    }

    public ExternalMark externalMark(Icon inline) {
        return new ExternalMarkImpl( inline );
    }

    public ExternalMark externalMark(OnLineResource resource, String format, int markIndex) {
        return new ExternalMarkImpl( resource, format, markIndex );
    }

    public org.opengis.style.FeatureTypeStyle featureTypeStyle(String name,
            Description description, Id definedFor, Set<Name> featureTypeNames,
            Set<SemanticType> types, List<org.opengis.style.Rule> rules) {
        FeatureTypeStyleImpl featureTypeStyle = new FeatureTypeStyleImpl();
        featureTypeStyle.setName( name );
        
        if( description != null && description.getTitle() != null ){
            featureTypeStyle.setTitle(description.getTitle().toString());
        }
        if( description != null && description.getAbstract() != null ){
            featureTypeStyle.setAbstract( description.getAbstract().toString());
        }
        //featureTypeStyle.setFeatureInstanceIDs( defainedFor );
        featureTypeStyle.featureTypeNames().addAll( featureTypeNames );
        featureTypeStyle.semanticTypeIdentifiers().addAll( types );
        
        for( org.opengis.style.Rule rule : rules ){
            if( rule instanceof RuleImpl ){
                featureTypeStyle.rules().add( (RuleImpl) rule );
            }
            else {
                featureTypeStyle.rules().add( new RuleImpl( rule ));
            }
        }
        return featureTypeStyle;
    }

    public org.opengis.style.Fill fill(GraphicFill fill, Expression color, Expression opacity) {
        return null;
    }

    public org.opengis.style.Font font(List<Expression> family, Expression style,
            Expression weight, Expression size) {
        return null;
    }

    public org.opengis.style.Graphic graphic(List<GraphicalSymbol> symbols, Expression opacity,
            Expression size, Expression rotation, org.opengis.style.AnchorPoint anchor,
            org.opengis.style.Displacement disp) {
        return null;
    }
    public GraphicFill graphicFill(List<GraphicalSymbol> symbols, Expression opacity,
            Expression size, Expression rotation, org.opengis.style.AnchorPoint anchorPoint,
            org.opengis.style.Displacement displacement) {
        return null;
    }
    public GraphicLegend graphicLegend(List<GraphicalSymbol> symbols, Expression opacity,
            Expression size, Expression rotation, org.opengis.style.AnchorPoint anchorPoint,
            org.opengis.style.Displacement displacement) {
        return null;
    }
    public GraphicStroke graphicStroke(List<GraphicalSymbol> symbols, Expression opacity,
            Expression size, Expression rotation, org.opengis.style.AnchorPoint anchorPoint,
            org.opengis.style.Displacement displacement, Expression initialGap, Expression gap) {
        return null;
    }
    public org.opengis.style.Halo halo(org.opengis.style.Fill fill, Expression radius) {
        return null;
    }

    public org.opengis.style.LinePlacement linePlacement(Expression offset, Expression initialGap,
            Expression gap, boolean repeated, boolean aligned, boolean generalizedLine) {
        return null;
    }

    public org.opengis.style.LineSymbolizer lineSymbolizer(String name, Expression geometry,
            Description description, Unit<?> unit, org.opengis.style.Stroke stroke,
            Expression offset) {
        return null;
    }
    public org.opengis.style.Mark mark(Expression wellKnownName, org.opengis.style.Fill fill,
            org.opengis.style.Stroke stroke) {
        return null;
    }

    public org.opengis.style.Mark mark(ExternalMark externalMark, org.opengis.style.Fill fill,
            org.opengis.style.Stroke stroke) {
        return null;
    }

    public org.opengis.style.PointPlacement pointPlacement(org.opengis.style.AnchorPoint anchor,
            org.opengis.style.Displacement displacement, Expression rotation) {
        return null;
    }

    public org.opengis.style.PointSymbolizer pointSymbolizer(String name, Expression geometry,
            Description description, Unit<?> unit, org.opengis.style.Graphic graphic) {
        return null;
    }
    public org.opengis.style.PolygonSymbolizer polygonSymbolizer(String name, Expression geometry,
            Description description, Unit<?> unit, org.opengis.style.Stroke stroke,
            org.opengis.style.Fill fill, org.opengis.style.Displacement displacement,
            Expression offset) {
        return null;
    }
    
    public org.opengis.style.RasterSymbolizer rasterSymbolizer(String name, Expression geometry,
            Description description, Unit<?> unit, Expression opacity,
            org.opengis.style.ChannelSelection channelSelection, OverlapBehavior overlapsBehaviour,
            org.opengis.style.ColorMap colorMap, org.opengis.style.ContrastEnhancement contrast,
            org.opengis.style.ShadedRelief shaded, org.opengis.style.Symbolizer outline) {
        return null;
    }
    
    public ExtensionSymbolizer extensionSymbolizer(String name, String propertyName,
            Description description, Unit<?> unit, String extensionName,
            Map<String, Expression> parameters) {
        return null;
    }    
  
    public org.opengis.style.Rule rule(String name, Description description, GraphicLegend legend,
            double min, double max, List<org.opengis.style.Symbolizer> symbolizers, Filter filter) {
        return null;
    }
    
    public org.opengis.style.SelectedChannelType selectedChannelType(String channelName,
            org.opengis.style.ContrastEnhancement contrastEnhancement)
    {
        return null;
    }
    
    public org.opengis.style.ShadedRelief shadedRelief(Expression reliefFactor,
            boolean brightnessOnly) {
        return null;
    }

    public org.opengis.style.Stroke stroke(Expression color, Expression opacity, Expression width,
            Expression join, Expression cap, float[] dashes, Expression offset) {
        return null;
    }
    
    public org.opengis.style.Stroke stroke(GraphicFill fill, Expression color, Expression opacity,
            Expression width, Expression join, Expression cap, float[] dashes, Expression offset) {
        return null;
    }
    
    public org.opengis.style.Stroke stroke(GraphicStroke stroke, Expression color,
            Expression opacity, Expression width, Expression join, Expression cap, float[] dashes,
            Expression offset) {
        return null;
    }
    
    public org.opengis.style.Style style(String name, Description description, boolean isDefault,
            List<org.opengis.style.FeatureTypeStyle> featureTypeStyles,
            org.opengis.style.Symbolizer defaultSymbolizer) {
        return null;
    }
    
    public org.opengis.style.TextSymbolizer textSymbolizer(String name, Expression geometry,
            Description description, Unit<?> unit, Expression label, org.opengis.style.Font font,
            org.opengis.style.LabelPlacement placement, org.opengis.style.Halo halo,
            org.opengis.style.Fill fill) {
        return null;
    }
}
