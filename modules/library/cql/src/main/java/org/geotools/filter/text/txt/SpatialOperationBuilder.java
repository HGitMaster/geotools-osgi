/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.filter.text.txt;

import org.geotools.filter.text.commons.BuildResultStack;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.BinarySpatialOperator;

/**
 * Builds an {@link BinarySpatialOperator}
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.6
 */
abstract class SpatialOperationBuilder {
    

    private final BuildResultStack resultStack;
    private final FilterFactory2 filterFactory;
    
    protected final BuildResultStack getResultStack() {
        return resultStack;
    }

    protected final FilterFactory2 getFilterFactory() {
        return filterFactory;
    }

    public SpatialOperationBuilder(BuildResultStack resultStack, FilterFactory filterFactory){
        assert resultStack != null;
        assert filterFactory != null;
        
        this.resultStack = resultStack;
        this.filterFactory = (FilterFactory2)filterFactory;
    }
    
    public BinarySpatialOperator build() throws CQLException{

        Expression expr2 = resultStack.popExpression();

        Expression expr1 = resultStack.popExpression();

        return buildFilter(expr1, expr2) ;
   }

    protected BinarySpatialOperator buildFilter(Expression expr1, Expression expr2) {
        throw new UnsupportedOperationException("must be implemented");
    };

}
