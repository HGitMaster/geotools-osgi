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
package org.geotools.filter.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.capability.FunctionNameImpl;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

/**
 * This is an implemenation of the Recode function as defined by
 * the OGC Symbology Encoding (SE) 1.1 specification.
 * <p>
 * The Recode function provides a lookup table facility (think HashTable)
 * where both keys and values can be any {@code Expression}. The first
 * parameter to the function specifies the source of the value to lookup,
 * e.g. the name of a feature property as a {@code Literal}. The remaining
 * parameters define the lookup table as key:value pairs. Thus there should
 * be an odd number of parameters in total: the lookup value parameter plus
 * the set of key value pairs.
 * <p>
 * Where the lookup involves {@code String} values, comparisons are done
 * case-insensitively.
 * <p>
 * If the lookup value does not match any of the keys defined this function
 * returns {@code null}.
 *
 * @author Johann Sorel (Geomatys)
 * @author Michael Bedward
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/library/main/src/main/java/org/geotools/filter/function/RecodeFunction.java $
 * @version $Id: RecodeFunction.java 37414 2011-06-12 17:31:24Z aaime $
 */
public class RecodeFunction implements Function {

    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

    private final List<Expression> parameters;

    private final Literal fallback;

    /**
     * Make the instance of FunctionName available in a consistent spot.
     */
    public static final FunctionName NAME = new FunctionNameImpl("Recode", "LookupValue", "Data 1", 
        "Value 1", "Data 2", "Value 2");

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
    public FunctionName getFunctionName() {
        return NAME;
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

    public <T> T evaluate(Object object, Class<T> context) {
        if (parameters.size() % 2 != 1) {
            throw new IllegalArgumentException(
                    "There must be an equal number of lookup data and return values");
        }

        final Expression lookupExp = parameters.get(0);
        final List<Expression> pairList = parameters.subList(1, parameters.size());

        for (int i = 0; i < pairList.size(); i += 2) {
            Expression keyExpr = pairList.get(i);
            Expression valueExpr = pairList.get(i + 1);
            // we are going to test our propertyNameExpression against the keyExpression
            // if they are equal we will return the valueExpression
            //
            org.opengis.filter.Filter compareFilter = ff.equal(lookupExp, keyExpr, false);

            if (compareFilter.evaluate(object)) {
                return valueExpr.evaluate(object, context);
            }
        }
        
        return null;
    }

    public Literal getFallbackValue() {
        return fallback;
    }

}
