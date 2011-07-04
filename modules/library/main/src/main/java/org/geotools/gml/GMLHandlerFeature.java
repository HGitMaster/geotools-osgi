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
package org.geotools.gml;

import org.opengis.feature.simple.SimpleFeature;
import org.xml.sax.ContentHandler;


/**
 * LEVEL4 saxGML4j GML handler: Gets features.
 * 
 * <p>
 * This handler must be implemented by the parent of a GMLFilterFeature filter
 * in order to handle the features passed to it from the child.
 * </p>
 *
 * @author Rob Hranac, Vision for New York
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/library/main/src/main/java/org/geotools/gml/GMLHandlerFeature.java $
 * @version $Id: GMLHandlerFeature.java 37292 2011-05-25 03:24:35Z mbedward $
 */
public interface GMLHandlerFeature extends ContentHandler {
    /**
     * Receives OGC simple feature from parent.
     */
    void feature(SimpleFeature feature);
}
