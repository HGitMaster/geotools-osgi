/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.util.Converters;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

/**
 * Implementation of "Recode" as a normal function.
 * <p>
 * This implementation is compatible with the Function interface; the parameter list can be used to
 * set the threshold values etc...
 * <p>
 * This function expects:
 * <ol>
 * <li>PropertyName; use "Rasterdata" to indicate this is a colour map
 * <li>Literal: data 1
 * <li>Literal: value 1
 * <li>Literal: data 2
 * <li>Literal: value 2
 * </ol>
 * In reality any expression will do.
 * 
 * @author Johann Sorel (Geomatys)
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/library/render/src/main/java/org/geotools/filter/RecodeFunction.java $
 */
public class RecodeFunction implements Function {

    private final List<Expression> parameters;

    private final Literal fallback;

    /**
     * Make the instance of FunctionName available in a consistent spot.
     */
    public static final FunctionName NAME = new Name();

    /**
     * Describe how this function works. (should be available via FactoryFinder lookup...)
     */
    public static class Name implements FunctionName {

        public int getArgumentCount() {
            return -2; // indicating unbounded, 2 minimum
        }

        public List<String> getArgumentNames() {
            return Arrays.asList(new String[] { "LookupValue", "Data 1", "Value 1", "Data 2",
                    "Value 2" });
        }

        public String getName() {
            return "Recode";
        }
    };

    public RecodeFunction() {
        this(new ArrayList<Expression>(), null);
    }

    public RecodeFunction(List<Expression> parameters, Literal fallback) {
        this.parameters = parameters;
        this.fallback = fallback;
    }

    public String getName() {
        return "Recode";
    }

    public List<Expression> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public Object accept(ExpressionVisitor visitor, Object extraData) {
        return visitor.visit(this, extraData);
    }

    public Object evaluate(Object object) {
        return evaluate(object, Object.class);
    }

    public <T> T evaluate(Object object, Class<T> targetClass) {
        if (parameters.size() == 2) {
            return parameters.get(1).evaluate(object, targetClass);
        }

        final Expression propertyNameExp = parameters.get(0);

        final List<Expression> pairList;

        if (parameters.size() % 2 == 1) {
            pairList = parameters.subList(1, parameters.size());
        } else {
            // this should not happen
            pairList = parameters.subList(1, parameters.size() - 1);
        }

        // we are going to use this to construct equals experssions
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

        for (int i = 0; i < pairList.size(); i += 2) {
            Expression keyExpr = pairList.get(i);
            Expression valueExpr = pairList.get(i + 1);

            // we are going to test our propertyNameExpression against the keyExpression
            // if they are equal we will return the valueExpression
            //
            PropertyIsEqualTo compareFilter = ff.equals(propertyNameExp, keyExpr);

            if (compareFilter.evaluate(object)) {
                return valueExpr.evaluate(object, targetClass); // yeah!
            }
        }
        return fallback == null ? null : Converters.convert(fallback, targetClass);
    }

    public Literal getFallbackValue() {
        return fallback;
    }

}
