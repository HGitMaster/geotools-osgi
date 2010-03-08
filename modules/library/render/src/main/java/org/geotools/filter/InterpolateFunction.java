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

import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

/**
 * 
 * Implementation of "Interpolation" as a normal function.
 * <p>
 * This implementation is compatible with the Function
 * interface; the parameter list can be used to set the
 * threshold values etc...
 * <p>
 *
 * This function expects:
 * <ol>
 * <li>PropertyName; use "Rasterdata" to indicate this is a colour map
 * <li>Literal: lookup value
 * <li>Literal: InterpolationPoint : data 1
 * <li>Literal: InterpolationPoint : value 1
 * <li>Literal: InterpolationPoint : data 2
 * <li>Literal: InterpolationPoint : value 2
 * <li>Literal: Mode
 * <li>Literal: Method
 * </ol>
 * In reality any expression will do.
 * 
 * @author Johann Sorel (Geomatys)
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/library/render/src/main/java/org/geotools/filter/InterpolateFunction.java $
 */
public class InterpolateFunction implements Function {

    
    /** Use as a literal value to indicate interpolation mode */
    public static final String MODE_LINEAR = "linear";
    
    /** Use as a literal value to indicate interpolation mode */
    public static final String MODE_COSINE = "cosine";
     
    /** Use as a literal value to indicate interpolation mode */ 
    public static final String MODE_CUBIC = "cubic";
    
    /** Use as a literal value to indicate interpolation method */ 
    public static final String METHOD_NUMERIC = "numeric";
    
    /** Use as a literal value to indicate interpolation method */ 
    public static final String METHOD_COLOR = "color";
    
    
    /**
     * Use as a PropertyName when defining a color map.
     * The "Raterdata" is expected to apply to only a single band;
     */
    public static final String RASTER_DATA = "Rasterdata";
    
    private final List<Expression> parameters;
    private final Literal fallback;
    
    
    /**
     * Make the instance of FunctionName available in
     * a consistent spot.
     */
    public static final FunctionName NAME = new Name();

    /**
     * Describe how this function works.
     * (should be available via FactoryFinder lookup...)
     */
    public static class Name implements FunctionName {

        public int getArgumentCount() {
            return -2; // indicating unbounded, 2 minimum
        }

        public List<String> getArgumentNames() {
            return Arrays.asList(new String[]{
                        "LookupValue",
                        "Data 1", "Value 1",
                        "Data 2", "Value 2",
                        "linear, cosine or cubic",
                        "numeric or color"
                    });
        }

        public String getName() {
            return "Interpolate";
        }
    };

    public InterpolateFunction() {
        this( new ArrayList<Expression>(), null);
    }

    public InterpolateFunction(List<Expression> parameters, Literal fallback) {
        this.parameters = parameters;
        this.fallback = fallback;
    }

    public String getName() {
        return "Interpolate";
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
        final Expression lookupExp = parameters.get(0);
        Expression currentExp = parameters.get(1);

//        final List<Expression> splits;
//        if (parameters.size() == 2) {
//            return currentExp.evaluate(object, context);
//        } else if (parameters.size() % 2 == 0) {
//            splits = parameters.subList(2, parameters.size());
//        } else {
//            splits = parameters.subList(2, parameters.size() - 1);
//        }
//        
//        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
//        for (int i = 0; i < splits.size(); i += 2) {
//            Expression threshholdExp = splits.get(i);
//            Expression rangedExp = splits.get(i + 1);
//            
//            
//            String lookupValue = lookupExp.evaluate(object, String.class);
//            
//            //we deal with a raster data
//            if(lookupValue.equalsIgnoreCase(RASTER_DATA)){
//                Double bandValue = new Double(object.toString());
//                Double threshholdValue = threshholdExp.evaluate(object, Double.class);
//                
//                if (PRECEDING.equals(belongsTo)) {
//                    if(bandValue > threshholdValue){
//                        currentExp = rangedExp;
//                    }else{
//                        break;
//                    }
//                } else {
//                    if(bandValue >= threshholdValue){
//                        currentExp = rangedExp;
//                    }else{
//                        break;
//                    }
//                }
//            }
//            //we deal with something else, a mistake ? can it happen ?
//            else{
//                Filter isIncludedInThreshold;
//                if (PRECEDING.equals(belongsTo)) {
//                    isIncludedInThreshold = ff.greater(lookupExp, threshholdExp);
//                } else {
//                    isIncludedInThreshold = ff.greaterOrEqual(lookupExp, threshholdExp);
//                }
//                if (isIncludedInThreshold.evaluate(object)) {
//                    currentExp = rangedExp;
//                } else {
//                    break;
//                }
//            }
//            
//        }
        return currentExp.evaluate(object, context);
    }


    public Literal getFallbackValue() {
        return fallback;
    }
    
}
