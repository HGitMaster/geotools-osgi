/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter.visitor;
import java.util.logging.Logger;

import org.geotools.filter.Expression;
import org.geotools.filter.ExpressionBuilder;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.MapScaleDenominator;

import org.geotools.filter.parser.ParseException;


/**
 * Finds instances of specific environment variable expressions within filters and
 * composite expressions and replaces them with simple literals.
 *
 * @author James Macgill, Penn State
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/main/java/org/geotools/filter/visitor/EnvironmentVariableResolver.java $
 */
public class EnvironmentVariableResolver {
 
    /** Standard java logger */
    private static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.filter");
    private boolean found = false;
    
    /**
     * Empty constructor
     */
    public EnvironmentVariableResolver() {
    }
    
    /**
     * Find all instances of MapScaleDenominator and replace them with
     * the a literal expression for the provided map scale.
     * The passed in filter is NOT modifed by calls to this method.
     * @param filter The Filter to check for MapScaleDenominator Environment Variables
     * @return A Filter with the map scale env variable replaced by literals
     */
    public Filter resolve(Filter filter, double mapScale) throws ParseException {
        String input = filter.toString();
        input = input.replaceAll("sld:MapScaleDenominator", ""+mapScale);
        Filter output = (Filter)ExpressionBuilder.parse(input);
        return output;
    }
      
    /**
     * Find all instances of MapScaleDenominator and replace them with
     * the a literal expression for the provided map scale.
     * The passed in filter is NOT modifed by calls to this method.
     * @param exp The Expression to check for MapScaleDenominator Environment Variables
     * @return An Epression with the map scale env variable replaced by literals
     */
    public Expression resolve(Expression exp, double mapScale) throws ParseException {
        
        String input = exp.toString();
        input = input.replaceAll("sld:MapScaleDenominator", ""+mapScale);
        Expression output = (Expression)ExpressionBuilder.parse(input);
        return output;
    }
    /**
     * Test supplied filter to see if it contains any EnvironmentVariable expressions
     * @todo supply implementation, currently always returns true!
     */
    public boolean needsResolving(Filter f){
        final java.util.List parts = new java.util.ArrayList();
        new AbstractFilterVisitor(){
            public void visit(Expression expression) {
                if(expression instanceof MapScaleDenominator){
                    parts.add(expression);
                }
            }
        }.visit(f);
        return parts.size() > 0;
    }
    
}
