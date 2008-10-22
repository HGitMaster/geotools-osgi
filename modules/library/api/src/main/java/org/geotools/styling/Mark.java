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
 */
package org.geotools.styling;

import org.opengis.filter.expression.Expression;


/**
 * A Mark element defines a "shape" which has coloring applied to it.
 *
 * <p>
 * The details of this object are taken from the <a
 * href="https://portal.opengeospatial.org/files/?artifact_id=1188"> OGC
 * Styled-Layer Descriptor Report (OGC 02-070) version 1.0.0.</a>:
 * <pre><code>
 * &lt;xsd:element name="Mark"&gt;
 *   &lt;xsd:annotation&gt;
 *     &lt;xsd:documentation&gt;
 *       A "Mark" specifies a geometric shape and applies coloring to it.
 *     &lt;/xsd:documentation&gt;
 *   &lt;/xsd:annotation&gt;
 *   &lt;xsd:complexType&gt;
 *     &lt;xsd:sequence&gt;
 *       &lt;xsd:element ref="sld:WellKnownName" minOccurs="0"/&gt;
 *       &lt;xsd:element ref="sld:Fill" minOccurs="0"/&gt;
 *       &lt;xsd:element ref="sld:Stroke" minOccurs="0"/&gt;
 *     &lt;/xsd:sequence&gt;
 *   &lt;/xsd:complexType&gt;
 * &lt;/xsd:element&gt;
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
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/api/src/main/java/org/geotools/styling/Mark.java $
 * @version $Id: Mark.java 31133 2008-08-05 15:20:33Z johann.sorel $
 */
public interface Mark extends org.opengis.style.Mark,Symbol {
    public static final Mark[] MARKS_EMPTY = new Mark[0];

    /**
     * This parameter gives the well-known name of the shape of the mark.<br>
     * Allowed names include at least "square", "circle", "triangle", "star",
     * "cross" and "x" though renderers may draw a different symbol instead if
     * they don't have a shape for all of these.<br>
     *
     * @return The well-known name of a shape.  The default value is "square".
     */
    Expression getWellKnownName();

    /**
     * This parameter gives the well-known name of the shape of the mark.<br>
     * Allowed names include at least "square", "circle", "triangle", "star",
     * "cross" and "x" though renderers may draw a different symbol instead if
     * they don't have a shape for all of these.<br>
     *
     * @param wellKnownName The well-known name of a shape.  The default value
     *        is "square".
     * 
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setWellKnownName(Expression wellKnownName);

    /**
     * This paramterer defines which stroke style should be used when rendering
     * the Mark.
     *
     * @return The Stroke definition to use when rendering the Mark.
     */
    Stroke getStroke();

    /**
     * This paramterer defines which stroke style should be used when rendering
     * the Mark.
     *
     * @param stroke The Stroke definition to use when rendering the Mark.
     * 
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setStroke(Stroke stroke);

    /**
     * This parameter defines which fill style to use when rendering the Mark.
     *
     * @return the Fill definition to use when rendering the Mark.
     */
    Fill getFill();

    /**
     * This parameter defines which fill style to use when rendering the Mark.
     *
     * @param fill the Fill definition to use when rendering the Mark.
     * 
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setFill(Fill fill);

    /**
     * @deprecated this method is already defined in the parent class.
     */
    @Deprecated
    Expression getSize();

    /**
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setSize(Expression size);

    /**
     * @deprecated this method is already defined in the parent class.
     */
    @Deprecated
    Expression getRotation();

    /**
     * @deprecated symbolizers and underneath classes are immutable
     */
    @Deprecated
    void setRotation(Expression rotation);

    void accept(org.geotools.styling.StyleVisitor visitor);
}
