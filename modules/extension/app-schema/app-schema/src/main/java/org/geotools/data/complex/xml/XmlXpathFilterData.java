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

import org.jdom.Document;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * A DTO used to pass data to the XmlXPathPropertyAccessorFactory, to filter 
 * responses from the backend on the Geoserver side.
 * 
 * @author Russell Petty (GeoScience Victoria)
 * @version $Id: XmlXpathFilterData.java 37417 2011-06-13 04:40:41Z bencaradocdavies $
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/extension/app-schema/app-schema/src/main/java/org/geotools/data/complex/xml/XmlXpathFilterData.java $
 */
public class XmlXpathFilterData {
    private NamespaceSupport namespaces;

    private Document doc;

    private int count;

    private String itemXpath;

    public XmlXpathFilterData(NamespaceSupport namespaces, Document doc, int count, String itemXpath) {
        this.namespaces = namespaces;
        this.doc = doc;
        this.count = count;
        this.itemXpath = itemXpath;
    }

    public NamespaceSupport getNamespaces() {
        return namespaces;
    }

    public Document getDoc() {
        return doc;
    }

    public int getCount() {
        return count;
    }

    public String getItemXpath() {
        return itemXpath;
    }
}
