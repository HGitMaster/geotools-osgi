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

import org.xml.sax.ContentHandler;


/**
 * Interface to recieve filters from the filter sax parsing classes.  Should be
 * implemented by classes that want the sax parsing classes to pass on their
 * filter information.
 *
 * @author Rob Hranac, Vision for New York
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/library/main/src/main/java/org/geotools/filter/FilterHandler.java $
 * @version $Id: FilterHandler.java 37292 2011-05-25 03:24:35Z mbedward $
 */
public interface FilterHandler extends ContentHandler {
    /**
     * Method to recieve the filters from the sax processing.
     *
     * @param filter The filter constructed by the factories.
     */
    void filter(org.opengis.filter.Filter filter);
}
