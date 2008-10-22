/*
 *    GeoTools - The Open Source Java GIS Tookit
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

package org.geotools.filter.text.txt;

import org.geotools.filter.text.commons.BuildResultStack;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.BBOX;

/**
 * Builds a bbox filter using the elements pushed by the parsing process.
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.6
 */
class SpatialBBoxBuilder extends SpatialOperationBuilder {

    public SpatialBBoxBuilder(BuildResultStack resultStack,
            FilterFactory filterFactory) {
        super(resultStack, filterFactory);
        
    }

    public BBOX buildWithCRS() throws CQLException{
        
        String crs = getResultStack().popStringValue();
        assert crs != null;

        double maxY = getResultStack().popDoubleValue();
        double maxX = getResultStack().popDoubleValue();
        double minY = getResultStack().popDoubleValue();
        double minX = getResultStack().popDoubleValue();

        Expression expr = getResultStack().popExpression();

        FilterFactory2 ff = (FilterFactory2)getFilterFactory();
        
        BBOX bbox = ff.bbox(
                    expr, minX, minY, maxX, maxY, crs);
        return bbox;
    }
    
    public  BBOX build() throws CQLException {
        
        double maxY = getResultStack().popDoubleValue();
        double maxX = getResultStack().popDoubleValue();
        double minY = getResultStack().popDoubleValue();
        double minX = getResultStack().popDoubleValue();

        Expression expr = getResultStack().popExpression();

        FilterFactory2 ff = (FilterFactory2)getFilterFactory();
        
        BBOX bbox = ff.bbox(
                    expr, minX, minY, maxX, maxY, null);
        return bbox;
    }

}
