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
package org.geotools.filter;

import java.util.Iterator;
import java.util.List;

import org.opengis.feature.simple.SimpleFeature;

/**
 * Defines a logic filter (the only filter type that contains other filters).
 * This filter holds one or more filters together and relates them logically
 * with an internally defined type (AND, OR, NOT).
 *
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/library/api/src/main/java/org/geotools/filter/LogicFilter.java $
 * @version $Id: LogicFilter.java 37277 2011-05-24 06:48:54Z mbedward $
 *
 * @deprecated use {@link org.opengis.filter.BinaryLogicOperator}
 */
public interface LogicFilter extends Filter {
    /**
     * Determines whether the feature matches the appropriate logic
     * relationships.
     *
     * @param feature Specified feature to examine.
     *
     * @return Flag confirming whether or not this feature is inside the
     *         filter.
     *
     * @deprecated use {@link Filter#evaluate(Feature)}.
     */
    boolean contains(SimpleFeature feature);

    /**
     * Returns a list containing all of the child filters of this object.
     * <p>
     * This list will contain at least two elements, and each element will be an
     * instance of {@code Filter}.
     * </p>
     */
    @SuppressWarnings("rawtypes")
    List getChildren();
        
    /**
     * Gets an iterator for the filters held by this logic filter.
     *
     * @return the iterator of the filters.
     */
    @SuppressWarnings("rawtypes")
    Iterator getFilterIterator();

    /**
     * Adds a sub filter to this filter.
     *
     * @param filter Specified filter to add to the sub filter list.
     *
     * @throws IllegalFilterException Does not conform to logic filter
     *         structure
     *
     * @task REVISIT: make all filters immutable.  This should return a new
     *       filter.
     */
    void addFilter(org.opengis.filter.Filter filter) throws IllegalFilterException;
}
