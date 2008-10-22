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
 * $Id: TextSymbolizer.java 31133 2008-08-05 15:20:33Z johann.sorel $
 *
 * @author Ian Turton, CCG
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/api/src/main/java/org/geotools/styling/TextSymbolizer.java $
 */
public interface TextSymbolizer extends org.opengis.style.TextSymbolizer,Symbolizer {

    /**
     * Sets the expression that will be evaluated to determine what text is
     * displayed. See {@link #getLabel} for details.
     *
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setLabel(Expression label);

    /**
     * Returns a device independent Font object that is to be used to render
     * the label.
     *
     * @deprecated use getFont()
     */
    @Deprecated
    Font[] getFonts();
    
    /**
     * Font to use when rendering this symbolizer.
     * @return Font to use when rendering this symbolizer
     */
    Font getFont();
    
    /**
     * sets a list of device independent Font objects to be used to render the
     * label.
     *
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setFonts(Font[] fonts);

    /**
     * A LabelPlacement specifies how a text element should be rendered
     * relative to its geometric point or line.
     *
     * @since Geotools 2.2 (GeoAPI 2.0)
     */
    @Deprecated
    LabelPlacement getPlacement();

    /**
     * A LabelPlacement specifies how a text element should be rendered
     * relative to its geometric point or line.
     */
    LabelPlacement getLabelPlacement();

    /**
     * A LabelPlacement specifies how a text element should be rendered
     * relative to its geometric point or line.
     *
     * @deprecated use setPlacement(LabelPlacement)
     */
    @Deprecated
    void setLabelPlacement(LabelPlacement labelPlacement);

    /**
     * A LabelPlacement specifies how a text element should be rendered
     * relative to its geometric point or line.
     *
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setPlacement(LabelPlacement labelPlacement);

    /**
     * A halo fills an extended area outside the glyphs of a rendered text
     * label to make the label easier to read over a background.
     *
     */
    org.geotools.styling.Halo getHalo();

    /**
     * A halo fills an extended area outside the glyphs of a rendered text
     * label to make the label easier to read over a background.
     *
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setHalo(Halo halo);

    /**
     * Returns the object that indicates how the text will be filled.
     *
     */
    org.geotools.styling.Fill getFill();

    /**
     * Sets the object that indicates how the text will be filled. See {@link
     * #getFill} for details.
     *
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setFill(Fill fill);

    /**
     * This property defines the geometry to be used for styling.<br>
     * The property is optional and if it is absent (null) then the "default"
     * geometry property of the feature should be used.  Geometry types other
     * than inherently point types can be used.  The geometryPropertyName is
     * the name of a geometry property in the Feature being styled.
     * Typically, features only have one geometry so, in general, the need to
     * select one is not required. Note: this moves a little away from the SLD
     * spec which provides an XPath reference to a Geometry object, but does
     * follow it in spirit.
     *
     * @param name The name of the attribute in the feature being styled  that
     *        should be used.  If null then the default geometry should be
     *        used.
     * 
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setGeometryPropertyName(String name);

    /**
     * Priority -- null       = use the default labeling priority Expression =
     * an expression that evaluates to a number (ie. Integer, Long, Double...)
     * Larger = more likely to be rendered
     *
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setPriority(Expression e);

    /**
     * Priority -- null       = use the default labeling priority Expression =
     * an expression that evaluates to a number (ie. Integer, Long, Double...)
     * Larger = more likely to be rendered
     *
     */
    Expression getPriority();

    /**
     * adds a parameter value to the options map
     *
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void addToOptions(String key, String value);

    /**
     * Find the value of a key in the map (may return null)
     *
     * @param key
     *
     */
    String getOption(String key);

    /**
     * return the map of option
     *
     * @return null - no options set
     */
    Map<String,String> getOptions();
}
