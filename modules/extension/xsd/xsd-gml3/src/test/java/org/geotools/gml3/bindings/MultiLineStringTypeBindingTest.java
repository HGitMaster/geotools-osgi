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
package org.geotools.gml3.bindings;

import org.geotools.gml3.GML;
import org.geotools.gml3.GML3TestSupport;
import org.w3c.dom.Document;

import com.vividsolutions.jts.geom.MultiLineString;


public class MultiLineStringTypeBindingTest extends GML3TestSupport {
    public void test() throws Exception {
        GML3MockData.multiLineString(document, document);

        MultiLineString multiLineString = (MultiLineString) parse();
        assertNotNull(multiLineString);

        assertEquals(2, multiLineString.getNumGeometries());
    }

    public void testEncode() throws Exception {
        Document dom = encode(GML3MockData.multiLineString(), GML.MultiLineString);
        assertEquals(2,
            dom.getElementsByTagNameNS(GML.NAMESPACE, GML.lineStringMember.getLocalPart())
               .getLength());
    }
}
