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
package org.geotools.gml2;

import org.geotools.xml.XSDParserDelegate;

/**
 * Parser delegate for GML 2.
 * 
 * @author Justin Deoliveira, OpenGEO
 * @since 2.6
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/extension/xsd/xsd-gml2/src/main/java/org/geotools/gml2/GMLParserDelegate.java $
 */
public class GMLParserDelegate extends XSDParserDelegate {

    public GMLParserDelegate() {
        super(new GMLConfiguration());
    }

}
