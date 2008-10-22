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


/**
 * A symbolizer describes how a polygon feature should appear on a map.
 *
 * <p>
 * The symbolizer describes not just the shape that should appear but also
 * such graphical properties as color and opacity.
 * </p>
 *
 * <p>
 * A symbolizer is obtained by specifying one of a small number of different
 * types of symbolizer and then supplying parameters to overide its default
 * behaviour.
 * </p>
 *
 * <p>
 * The details of this object are taken from the <a
 * href="https://portal.opengeospatial.org/files/?artifact_id=1188"> OGC
 * Styled-Layer Descriptor Report (OGC 02-070) version 1.0.0.</a>:
 * <pre><code>
 * &lt;xsd:element name="PolygonSymbolizer" substitutionGroup="sld:Symbolizer">
 *    &lt;xsd:annotation>
 *      &lt;xsd:documentation>
 *        A "PolygonSymbolizer" specifies the rendering of a polygon or
 *        area geometry, including its interior fill and border stroke.
 *      &lt;/xsd:documentation>
 *    &lt;/xsd:annotation>
 *    &lt;xsd:complexType>
 *      &lt;xsd:complexContent>
 *       &lt;xsd:extension base="sld:SymbolizerType">
 *         &lt;xsd:sequence>
 *           &lt;xsd:element ref="sld:Geometry" minOccurs="0"/>
 *           &lt;xsd:element ref="sld:Fill" minOccurs="0"/>
 *           &lt;xsd:element ref="sld:Stroke" minOccurs="0"/>
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
 *
 * @author James Macgill
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/api/src/main/java/org/geotools/styling/PolygonSymbolizer.java $
 * @version $Id: PolygonSymbolizer.java 31133 2008-08-05 15:20:33Z johann.sorel $
 */
public interface PolygonSymbolizer extends org.opengis.style.PolygonSymbolizer,Symbolizer {
    /**
     * Provides the graphical-symbolization parameter to use to fill the area
     * of the geometry. Note that the area should be filled first before the
     * outline  is rendered.
     *
     * @return The Fill style to use when rendering the area.
     */
    Fill getFill();

    /**
     * Provides the graphical-symbolization parameter to use to fill the area
     * of the geometry. Note that the area should be filled first before the
     * outline  is rendered.
     *
     * @param fill The Fill style to use when rendering the area.
     * 
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setFill(Fill fill);

    /**
     * Provides the graphical-symbolization parameter to use for the outline of
     * the Polygon.
     *
     * @return The Stroke style to use when rendering lines.
     */
    Stroke getStroke();

    /**
     * Provides the graphical-symbolization parameter to use for the outline of
     * the Polygon.
     *
     * @param stroke The Stroke style to use when rendering lines.
     * 
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setStroke(Stroke stroke);

    /**
     * This property defines the geometry to be used for styling.<br>
     * The property is optional and if it is absent (null) then the "default"
     * geometry property of the feature should be used.  Geometry types other
     * than inherently area types can be used.   If a line is used then the
     * line string is closed for filling (only) by connecting its end point to
     * its start point. The geometryPropertyName is the name of a geometry
     * property in the Feature being styled.  Typically, features only have
     * one geometry so, in general, the need to select one is not required.
     * Note: this moves a little away from the SLD spec which provides an
     * XPath reference to a Geometry object, but does follow it  in spirit.
     *
     * @param geometryPropertyName The name of the attribute in the feature
     *        being styled  that should be used.  If null then the default
     *        geometry should be used.
     * 
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setGeometryPropertyName(String geometryPropertyName);
}
