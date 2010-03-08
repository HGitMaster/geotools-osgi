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

import java.util.Map;

import org.opengis.filter.expression.Expression;


/**
 * A symbolizer describes how a feature should appear on a map.
 *
 * <p>
 * A symbolizer is obtained by specifying one of a small number of different
 * types of symbolizer and then supplying parameters to override its default
 * behaviour.
 * </p>
 *
 * <p>
 * The text symbolizer describes how to display text labels and the like.
 * </p>
 *
 * <p>
 * The details of this object are taken from the <a
 * href="https://portal.opengeospatial.org/files/?artifact_id=1188"> OGC
 * Styled-Layer Descriptor Report (OGC 02-070) version 1.0.0.</a>:
 * <pre><code>
 * &lt;xsd:element name="TextSymbolizer" substitutionGroup="sld:Symbolizer">
 *   &lt;xsd:annotation>
 *     &lt;xsd:documentation>
 *       A "TextSymbolizer" is used to render text labels according to
 *       various graphical parameters.
 *     &lt;/xsd:documentation>
 *   &lt;/xsd:annotation>
 *   &lt;xsd:complexType>
 *     &lt;xsd:complexContent>
 *       &lt;xsd:extension base="sld:SymbolizerType">
 *         &lt;xsd:sequence>
 *           &lt;xsd:element ref="sld:Geometry" minOccurs="0"/>
 *           &lt;xsd:element ref="sld:Label" minOccurs="0"/>
 *           &lt;xsd:element ref="sld:Font" minOccurs="0"/>
 *           &lt;xsd:element ref="sld:LabelPlacement" minOccurs="0"/>
 *           &lt;xsd:element ref="sld:Halo" minOccurs="0"/>
 *           &lt;xsd:element ref="sld:Fill" minOccurs="0"/>
 *         &lt;/xsd:sequence>
 *       &lt;/xsd:extension>
 *     &lt;/xsd:complexContent>
 *   &lt;/xsd:complexType>
 * &lt;/xsd:element>
 * </code></pre>
 * </p>
 *
 * <p>
 * Renderers can use this information when displaying styled features, though
 * it must be remembered that not all renderers will be able to fully
 * represent strokes as set out by this interface.  For example, opacity may
 * not be supported.
 * </p>
 *
 * <p>
 * Notes:
 *
 * <ul>
 * <li>
 * The graphical parameters and their values are derived from SVG/CSS2
 * standards with names and semantics which are as close as possible.
 * </li>
 * </ul>
 * </p>
 * $Id: TextSymbolizer.java 34564 2009-11-30 16:08:45Z aaime $
 *
 * @author Ian Turton, CCG
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/library/api/src/main/java/org/geotools/styling/TextSymbolizer.java $
 */
public interface TextSymbolizer extends org.opengis.style.TextSymbolizer,Symbolizer {
    /**
     * Returns the expression that will be evaluated to determine what text is
     * displayed.
     *
     * @return Expression that will be evaulated as a String to display on screen
     */
    Expression getLabel();

    /**
     * Sets the expression that will be evaluated to determine what text is
     * displayed. See {@link #getLabel} for details.
     */
    void setLabel(Expression label);

    /**
     * Returns a device independent Font object that is to be used to render
     * the label.
     *
     * @deprecated use getFont()
     */
    Font[] getFonts();
    
    /**
     * Font to use when rendering this symbolizer.
     * @return Font to use when rendering this symbolizer
     */
    Font getFont();
    
    /**
     * Font used when rendering this symbolizer.
     * @param font
     */
    public void setFont( org.opengis.style.Font font );
    
    /**
     * sets a list of device independent Font objects to be used to render the
     * label.
     *
     * @deprecated use getFont() setters to modify the set of font faces used
     */
    void setFonts(Font[] fonts);

    /**
     * A LabelPlacement specifies how a text element should be rendered
     * relative to its geometric point or line.
     */
    LabelPlacement getLabelPlacement();

    /**
     * A LabelPlacement specifies how a text element should be rendered
     * relative to its geometric point or line.
     */
    void setLabelPlacement(org.opengis.style.LabelPlacement labelPlacement);

    /**
     * A LabelPlacement specifies how a text element should be rendered
     * relative to its geometric point or line.
     * @deprecated Please use setLabelPlacement
     */
    void setPlacement(LabelPlacement labelPlacement);

    /**
     * A LabelPlacement specifies how a text element should be rendered
     * relative to its geometric point or line.
     *
     * @deprecated Please use getLabelPlacement()     
     */
    LabelPlacement getPlacement();

    /**
     * A halo fills an extended area outside the glyphs of a rendered text
     * label to make the label easier to read over a background.
     *
     */
    Halo getHalo();

    /**
     * A halo fills an extended area outside the glyphs of a rendered text
     * label to make the label easier to read over a background.
     */
    void setHalo(org.opengis.style.Halo halo);

    /**
     * Returns the object that indicates how the text will be filled.
     *
     */
    Fill getFill();

    /**
     * Sets the object that indicates how the text will be filled. See {@link
     * #getFill} for details.
     */
    void setFill(org.opengis.style.Fill fill);

    /**
     * Priority -- null = use the default labeling priority Expression =
     * an expression that evaluates to a number (ie. Integer, Long, Double...)
     * Larger = more likely to be rendered
     */
    void setPriority(Expression e);

    /**
     * Priority -- null       = use the default labeling priority Expression =
     * an expression that evaluates to a number (ie. Integer, Long, Double...)
     * Larger = more likely to be rendered
     *
     */
    Expression getPriority();

    /**
     * Adds a parameter value to the options map
     * @deprecated Please use getOptions().put( key, value )
     */
    void addToOptions(String key, String value);

    /**
     * Find the value of a key in the map (may return null)
     *
     * @param key
     * @deprecated Please use getOptions.get( key )
     */
    String getOption(String key);

    /**
     * return the map of option
     *
     * @return null - no options set
     */
    Map<String,String> getOptions();
}