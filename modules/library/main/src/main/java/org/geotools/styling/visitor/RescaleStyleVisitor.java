/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.Displacement;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Font;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Stroke;
import org.geotools.styling.Symbol;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;

/**
 * This is a style visitor that will produce a copy of the provided
 * style rescaled by a provided factor.
 * <p>
 * The provided scale will be use to modify all line widths, font sizes and
 * so forth. We may need to go the extra distance and play with the min/max
 * scale on rules, and if there is any DPI specific madness going on we are
 * going to cry.
 * <p>
 * According to the specification we are supposed to use environmental variables
 * to make our styles render in a resolution independent manner. The current
 * GeoTools environment variable visitor only does processing for <b>mapscale</b>
 * but does not have a dpi substitution. On the plus side this visitor accepts
 * a general Expression and you are free to use an environmental variable expression
 * in order to make sure a normal base style is tweaked in all the right spots.
 * <p>
 * 
 * @author Jody Garnett (Refractions Research)
 */
public class RescaleStyleVisitor extends DuplicatingStyleVisitor {
    
    /**
     * This is the scale used as a multiplication factory for everything that
     * has a size.
     */
    private Expression scale;

    public RescaleStyleVisitor( double scale ){
        this( CommonFactoryFinder.getFilterFactory2(null), scale );
    }
    
    public RescaleStyleVisitor( Expression scale){
        this( CommonFactoryFinder.getFilterFactory2(null), scale );
    }
    
    public RescaleStyleVisitor(FilterFactory2 filterFactory, double scale) {
        this( filterFactory, filterFactory.literal(scale));
    }

    public RescaleStyleVisitor(FilterFactory2 filterFactory, Expression scale) {
        super( CommonFactoryFinder.getStyleFactory( null ), filterFactory );
        this.scale = scale;
    }
    /**
     * Used to rescale the provided expr.
     * <p>
     * We do optimize the case where the provided expression is a literal; no
     * sense doing a calculation each time if we don't have to.
     * 
     * @param expr
     * @return expr multiplied by the provided scale
     */
    protected Expression rescale( Expression expr ){
        Expression rescale = ff.multiply( scale, expr );
        if( expr instanceof Literal && scale instanceof Literal){
            double constant = (double) rescale.evaluate(null, Double.class);
            return ff.literal(constant);
        }
        return rescale;
    }
    
    /**
     * Increase stroke width.
     * <p>
     * Based on feedback we may need to change the dash array as well.
     * <p>
     */
    public void visit(org.geotools.styling.Stroke stroke) {
        Stroke copy = sf.getDefaultStroke();
        copy.setColor( copy(stroke.getColor()));
        copy.setDashArray( copy(stroke.getDashArray()));
        copy.setDashOffset( copy( stroke.getDashOffset()));
        copy.setGraphicFill( copy(stroke.getGraphicFill()));
        copy.setGraphicStroke( copy( stroke.getGraphicStroke()));
        copy.setLineCap(copy(stroke.getLineCap()));
        copy.setLineJoin( copy(stroke.getLineJoin()));
        copy.setOpacity( copy(stroke.getOpacity()));
        copy.setWidth( rescale(stroke.getWidth()));
        pages.push(copy);
    }   
    
    /** Increase font size */
    protected Font copy(Font font) {
        if( font == null) return font;
        
        Expression fontFamily = copy( font.getFontFamily() );
        Expression fontStyle = copy( font.getFontStyle() );
        Expression fontWeight = copy( font.getFontWeight() );
        Expression fontSize = rescale( font.getFontSize() );
        Font copy = sf.createFont(fontFamily, fontStyle, fontWeight, fontSize);
        return copy;
    }
    
    /** Make graphics (such as used with PointSymbolizer) bigger */
    public void visit(Graphic gr) {
        Graphic copy = null;

        Displacement displacementCopy = null;

        if (gr.getDisplacement() != null) {
            gr.getDisplacement().accept(this);
            displacementCopy = (Displacement) pages.pop();
        }

        ExternalGraphic[] externalGraphics = gr.getExternalGraphics();
        ExternalGraphic[] externalGraphicsCopy = new ExternalGraphic[externalGraphics.length];

        int length=externalGraphics.length;
        for (int i = 0; i < length; i++) {
            externalGraphicsCopy[i] = copy( externalGraphics[i]);
        }

        Mark[] marks = gr.getMarks();
        Mark[] marksCopy = new Mark[marks.length];
        length=marks.length;
        for (int i = 0; i < length; i++) {
            marksCopy[i] = copy( marks[i]);
        }

        Expression opacityCopy = copy( gr.getOpacity() );
        Expression rotationCopy = copy( gr.getRotation() );
        Expression sizeCopy = rescale( gr.getSize() );
        
        Symbol[] symbols = gr.getSymbols();
        length=symbols.length;
        Symbol[] symbolCopys = new Symbol[length];

        for (int i = 0; i < length; i++) {
            symbolCopys[i] = copy( symbols[i] );
        }

        copy = sf.createDefaultGraphic();
        copy.setGeometryPropertyName(gr.getGeometryPropertyName());
        copy.setDisplacement(displacementCopy);
        copy.setExternalGraphics(externalGraphicsCopy);
        copy.setMarks(marksCopy);
        copy.setOpacity((Expression) opacityCopy);
        copy.setRotation((Expression) rotationCopy);
        copy.setSize((Expression) sizeCopy);
        copy.setSymbols(symbolCopys);

        pages.push(copy);
    }
        
}
