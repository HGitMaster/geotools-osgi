/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2009-2011, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.data.complex.xml;

import org.geotools.data.simple.SimpleFeatureCollection;

/**
 * @author Russell Petty (GeoScience Victoria)
 * @version $Id: XmlFeatureCollection.java 37417 2011-06-13 04:40:41Z bencaradocdavies $
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/extension/app-schema/app-schema/src/main/java/org/geotools/data/complex/xml/XmlFeatureCollection.java $
 */
public interface XmlFeatureCollection extends SimpleFeatureCollection {

    XmlResponse xmlResponse();
    
    XmlResponse xmlResponse(String xpath, String value);
}
