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
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Equals;

/**
 * Builds an {@link Equals}
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.6
 */
class SpatialEqualsBuilder extends SpatialOperationBuilder{

    public SpatialEqualsBuilder(BuildResultStack resultStack,
            FilterFactory filterFactory) {
        super(resultStack, filterFactory);
    }

    @Override
    protected BinarySpatialOperator buildFilter(
            final Expression expr1,
            final Expression expr2) {

        BinarySpatialOperator filter = getFilterFactory().equal(expr1, expr2);

        return filter;
    }

}
