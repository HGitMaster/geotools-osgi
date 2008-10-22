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

import org.opengis.filter.Filter;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.style.Description;

/**
 * A rule is used to attach a condition to, and group, the individual
 * symbolizers used for rendering.  The Title and Abstract describe the rule
 * and may be used to generate a legend, as may the LegendGraphic. The Filter,
 * ElseFilter, MinScale and MaxScale elements allow the selection of features
 * and rendering scales for a rule.  The scale selection works as follows.
 * When a map is to be rendered, the scale denominator is computed and all
 * rules in all UserStyles that have a scale outside of the request range are
 * dropped.  (This also includes Rules that have an ElseFilter.)  An
 * ElseFilter is simply an ELSE condition to the conditions (Filters) of all
 * other rules in the same UserStyle. The exact meaning of the ElseFilter is
 * determined after Rules have been eliminated for not fitting the rendering
 * scale.  This definition of the behaviour of ElseFilters may seem a little
 * strange, but it allows for scale-dependent and scale-independent ELSE
 * conditions.  For the Filter, only SqlExpression is available for
 * specification, but this is a hack and should be replaced with Filter as
 * defined in WFS. A missing Filter element means "always true".  If a set of
 * Rules has no ElseFilters, then some features may not be rendered (which is
 * presumably the desired behavior).  The Scales are actually scale
 * denominators (as double floats), so "10e6" would be interpreted as 1:10M. A
 * missing MinScale means there is no lower bound to the scale-denominator
 * range (lim[x->0+](x)), and a missing MaxScale means there is no upper bound
 * (infinity).  0.28mm
 *
 * <p>
 * The details of this object are taken from the <a
 * href="https://portal.opengeospatial.org/files/?artifact_id=1188"> OGC
 * Styled-Layer Descriptor Report (OGC 02-070) version 1.0.0.</a>:
 * <pre><code>
 * &lt;xsd:element name="Rule"&gt;
 *   &lt;xsd:annotation&gt;
 *     &lt;xsd:documentation&gt;
 *       A Rule is used to attach property/scale conditions to and group
 *       the individual symbolizers used for rendering.
 *     &lt;/xsd:documentation&gt;
 *   &lt;/xsd:annotation&gt;
 *   &lt;xsd:complexType&gt;
 *     &lt;xsd:sequence&gt;
 *       &lt;xsd:element ref="sld:Name" minOccurs="0"/&gt;
 *       &lt;xsd:element ref="sld:Title" minOccurs="0"/&gt;
 *       &lt;xsd:element ref="sld:Abstract" minOccurs="0"/&gt;
 *       &lt;xsd:element ref="sld:LegendGraphic" minOccurs="0"/&gt;
 *       &lt;xsd:choice minOccurs="0"&gt;
 *         &lt;xsd:element ref="ogc:Filter"/&gt;
 *         &lt;xsd:element ref="sld:ElseFilter"/&gt;
 *       &lt;/xsd:choice&gt;
 *       &lt;xsd:element ref="sld:MinScaleDenominator" minOccurs="0"/&gt;
 *       &lt;xsd:element ref="sld:MaxScaleDenominator" minOccurs="0"/&gt;
 *       &lt;xsd:element ref="sld:Symbolizer" maxOccurs="unbounded"/&gt;
 *     &lt;/xsd:sequence&gt;
 *   &lt;/xsd:complexType&gt;
 * &lt;/xsd:element&gt;
 * </code></pre>
 * </p>
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/api/src/main/java/org/geotools/styling/Rule.java $
 */
public interface Rule extends org.opengis.style.Rule{

    /**
     * Sets the name of the rule.
     *
     * @param name The name of the rule.  This provides a way to identify a
     *        rule.
     */
    void setName(String name);

    /**
     * Description for this style.
     * @return Human readable description for use in user interfaces
     * @since 2.5.x
     */
    Description getDescription();
    
    /**
     * Gets the title.
     *
     * @return The title of the rule.  This is a brief, human readable,
     *         description of the rule.
     * @deprecated use getDescription().getTitle().getString()
     */
    @Deprecated
    String getTitle();

    /**
     * Sets the title.
     *
     * @param title The title of the rule.  This is a brief, human readable,
     *        description of the rule.
     */
    @Deprecated
    void setTitle(String title);

    /**
     * Gets the abstract text for the rule.
     *
     * @return The abstract text, a more detailed description of the rule.
     * @deprecated use getDescription().getAbstract().getString()
     */
    @Deprecated
    String getAbstract();

    /**
     * Sets the abstract text for the rule.
     *
     * @param abstractStr The abstract text, a more detailed description of the
     *        rule.
     */
    @Deprecated
    void setAbstract(String abstractStr);

    /**
     * The smallest value for scale denominator at which symbolizers contained
     * by this rule should be applied.
     *
     * @param scale The smallest (inclusive) denominator value that this rule
     *        will be active for.
     */
    void setMinScaleDenominator(double scale);

    /**
     * The largest value for scale denominator at which symbolizers contained
     * by this rule should be applied.
     *
     * @param scale The largest (exclusive) denominator value that this rule
     *        will be active for.
     */
    void setMaxScaleDenominator(double scale);

    void setFilter(Filter filter);

    /**
     * @deprecated renamed in isElseFilter
     */
    @Deprecated
    boolean hasElseFilter();

    void setIsElseFilter(boolean defaultb);

    /**
     * A set of equivalent Graphics in different formats which can be used as a
     * legend against features stylized by the symbolizers in this rule.
     *
     * @return An array of Graphic objects, any of which can be used as the
     *         legend.
     * 
     * @deprecated there is only one legend graphic
     */
    @Deprecated
    Graphic[] getLegendGraphic();

    /**
     * A set of equivalent Graphics in different formats which can be used as a
     * legend against features stylized by the symbolizers in this rule.
     *
     * @param graphics An array of Graphic objects, any of which can be used as
     *        the legend.
     * 
     * @deprecated there wis only one graphic legend
     */
    @Deprecated
    void setLegendGraphic(Graphic[] graphics);

    /**
     * The symbolizers contain the actual styling information for different
     * geometry types.  A single feature may be rendered by more than one of
     * the symbolizers returned by this method.  It is important that the
     * symbolizers be applied in the order in which they are returned if the
     * end result is to be as intended. All symbolizers should be applied to
     * all features which make it through the filters in this rule regardless
     * of the features' geometry. For example, a polygon symbolizer should be
     * applied to line geometries and even points.  If this is not the desired
     * beaviour, ensure that either the filters block inappropriate features
     * or that the FeatureTypeStyler which contains this rule has its
     * FeatureTypeName or SemanticTypeIdentifier set appropriately.
     *
     * @return An array of symbolizers to be applied, in sequence, to all of
     *         the features addressed by the FeatureTypeStyler which contains
     *         this rule.
     * 
     * @deprecated replaced by a live list symbolizers()
     */
    @Deprecated    
    Symbolizer[] getSymbolizers();
    
    List<org.geotools.styling.Symbolizer> symbolizers();

    /**
     * The symbolizers contain the actual styling information for different
     * geometry types.  A single feature may be rendered by more than one of
     * the symbolizers returned by this method.  It is important that the
     * symbolizers be applied in the order in which they are returned if the
     * end result is to be as intended. All symbolizers should be applied to
     * all features which make it through the filters in this rule regardless
     * of the features' geometry. For example, a polygon symbolizer should be
     * applied to line geometries and even points.  If this is not the desired
     * beaviour, ensure that either the filters block inappropriate features
     * or that the FeatureTypeStyler which contains this rule has its
     * FeatureTypeName or SemanticTypeIdentifier set appropriately.
     *
     * @param symbolizers An array of symbolizers to be applied, in sequence,
     *        to all of the features addressed by the FeatureTypeStyler which
     *        contains this rule.
     * 
     * @deprecated replaced by a live list
     */
    @Deprecated
    void setSymbolizers(Symbolizer[] symbolizers);

    void setOnlineResource(OnLineResource online);

    void accept(org.geotools.styling.StyleVisitor visitor);
}
