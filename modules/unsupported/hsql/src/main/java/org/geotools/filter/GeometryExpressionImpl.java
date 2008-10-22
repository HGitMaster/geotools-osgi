/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter;

import org.opengis.feature.simple.SimpleFeature;

/**
 * Defines a complex filter (could also be called logical filter). This filter
 * holds one or more filters together and relates them logically in an
 * internally defined manner.
 *
 * @author Rob Hranac, TOPP
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/hsql/src/main/java/org/geotools/filter/GeometryExpressionImpl.java $
 * @version $Id: GeometryExpressionImpl.java 30700 2008-06-13 14:40:23Z acuster $
 */
public class GeometryExpressionImpl extends DefaultExpression {

    /** Holds all sub filters of this filter. */
    protected String attPath;

    /** Holds all sub filters of this filter. */
    protected String colName = null;

    /**
     * Constructor with the schema for this attribute.
     *
     * @param colName a String with the schema for this attribute.
     */
    protected GeometryExpressionImpl(String colName) {
        this.colName = colName;
        this.expressionType = ATTRIBUTE;
    }

    /**
     * Gets the value of this attribute from the passed feature.
     *
     * @param feature Feature from which to extract attribute value.
     */
    public Object evaluate(SimpleFeature feature) {
        return feature.getAttribute(attPath);
    }

     /**
     * Return this expression as a string.
     *
     * @return String representation of this attribute expression.
     */
    public String toString() {
        return attPath;
    }

    /**
     * Used by FilterVisitors to perform some action on this filter instance.
     * Typicaly used by Filter decoders, but may also be used by any thing
     * which needs infomration from filter structure. Implementations should
     * always call: visitor.visit(this); It is importatant that this is not
     * left to a parent class unless the  parents API is identical.
     *
     * @param visitor The visitor which requires access to this filter, the
     *        method must call visitor.visit(this);
     *
     * @todo This method can't be overriden anymore as of Geotools 2.3. Replaced by
     * {@link org.opengis.filter.expression.Expression#accept(ExpressionVisitor, Object)}.
     */
//    public void accept(FilterVisitor visitor) {
//        visitor.visit(this);
//    }
}
