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


/**
 * Defines the sort order, based on a property and assending/desending.
 *
 * @deprecated Please use org.opengis.filter.sort.SortBy
 * @since GeoTools 2.2, Filter 1.1
 * @author Jody Garnett, Refractions Research, Inc.
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/library/api/src/main/java/org/geotools/filter/SortBy.java $
 */
public interface SortBy extends org.opengis.filter.sort.SortBy {
    /**
     * @deprecated use {@link org.opengis.filter.sort.SortBy#UNSORTED}
     */
    public static final SortBy[] UNSORTED = new SortBy[] {  };
}
