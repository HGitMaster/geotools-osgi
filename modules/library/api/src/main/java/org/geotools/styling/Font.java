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

import java.util.List;

import org.opengis.filter.expression.Expression;


/**
 * A system-independent object for holding SLD font information. This holds
 * information on the text font to use in text processing. Font-family,
 * font-style, font-weight and font-size.
 *
 * @author Ian Turton, CCG
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/api/src/main/java/org/geotools/styling/Font.java $
 * @version $Id: Font.java 31133 2008-08-05 15:20:33Z johann.sorel $
 */
public interface Font extends org.opengis.style.Font{
    /** default font-size value **/
    static final int DEFAULT_FONTSIZE = 10;

    /**
     * @deprecated use getFamilly().get(0) for the preferred font
     */
    @Deprecated
    Expression getFontFamily();
    
    /**
     * SVG font-family parameters in preferred order.
     * @return live list of font-family parameters in preferred order
     */
    List<Expression> getFamily();

    /**
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setFontFamily(Expression family);

    /**
     * @deprecated this method is replaced by getStyle
     */
    @Deprecated
    Expression getFontStyle();

    /**
     * The "font-style" SVG parameter should be "normal", "italic", or "oblique".
     * @return Expression or null
     */
    Expression getStyle();
    
    /**
     * @deprecated symbolizers and underneath classes will be immutable in 2.6.x
     */
    @Deprecated
    void setFontStyle(Expression style);

    /**
     * @deprecated use getWeight
     */
    @Deprecated
    Expression getFontWeight();

    /**
     * The "font-weight" SVG parameter should be "normal" or "bold".
     * @return font-weight SVG parameter
     */
    Expression getWeight();
    
    /**
     * @deprecated symbolizers and underneath classes will be immutable in 2.6.x
     */
    @Deprecated
    void setFontWeight(Expression weight);

    /**
     * @deprecated use getSize
     */
    @Deprecated
    Expression getFontSize();

    /**
     * Font size.
     * @return font size
     */
    Expression getSize();
    
    /**
     * @deprecated symbolizers and underneath classes will be immutable in 2.6.x
     */
    @Deprecated
    void setFontSize(Expression size);

    /**
     * Enumeration of allow font-style values.
     */
    interface Style {
        static final String NORMAL = "normal";
        static final String ITALIC = "italic";
        static final String OBLIQUE = "oblique";
    }

    /**
     * Enumeration of allow font-weight values.
     */
    interface Weight {
        static final String NORMAL = "normal";
        static final String BOLD = "bold";
    }
}
