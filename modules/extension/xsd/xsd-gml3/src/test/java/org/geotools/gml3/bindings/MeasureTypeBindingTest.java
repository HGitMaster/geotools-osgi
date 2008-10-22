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

import javax.measure.unit.BaseUnit;
import org.geotools.gml3.GML;
import org.geotools.gml3.GML3TestSupport;
import org.geotools.measure.Measure;


public class MeasureTypeBindingTest extends GML3TestSupport {
    public void testParser() throws Exception {
        GML3MockData.element(GML.measure, document, document);
        document.getDocumentElement().setAttribute("uom", "http://someuri");
        document.getDocumentElement().appendChild(document.createTextNode("1234"));

        Measure measure = (Measure) parse();
        assertNotNull(measure);
        assertEquals(1234, measure.doubleValue(), 0.1);
        assertEquals("http://someuri", ((BaseUnit) measure.getUnit()).getSymbol());
    }
}
