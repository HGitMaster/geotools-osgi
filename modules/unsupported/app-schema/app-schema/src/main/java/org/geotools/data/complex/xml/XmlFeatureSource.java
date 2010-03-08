/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2009, Open Source Geospatial Foundation (OSGeo)
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

import org.geotools.data.FeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * @author Russell Petty, GSV
 * @version $Id: XmlFeatureSource.java 34061 2009-10-05 06:31:55Z bencaradocdavies $
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/app-schema/app-schema/src/main/java/org/geotools/data/complex/xml/XmlFeatureSource.java $
 */
public interface XmlFeatureSource extends FeatureSource<SimpleFeatureType, SimpleFeature> {

    void setNamespaces(NamespaceSupport namespaces);

    void setItemXpath(String itemXpath);
}
