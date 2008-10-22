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
package org.geotools.filter.v1_0;

import junit.framework.TestCase;
import java.io.ByteArrayInputStream;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.geotools.xml.Parser.Properties;


public class OGCFilterTest extends TestCase {
    Parser parser;

    protected void setUp() throws Exception {
        super.setUp();

        Configuration configuration = new OGCConfiguration();
        parser = new Parser(configuration);
    }

    public void testRun() throws Exception {
        Object thing = parser.parse(getClass().getResourceAsStream("test1.xml"));
        assertNotNull(thing);
        assertTrue(thing instanceof PropertyIsEqualTo);

        PropertyIsEqualTo equal = (PropertyIsEqualTo) thing;
        assertTrue(equal.getExpression1() instanceof PropertyName);
        assertTrue(equal.getExpression2() instanceof Literal);

        PropertyName name = (PropertyName) equal.getExpression1();
        assertEquals("testString", name.getPropertyName());

        Literal literal = (Literal) equal.getExpression2();
        assertEquals("2", literal.toString());
    }

    public void testLax() throws Exception {
        String xml = "<Filter>" + "  <PropertyIsEqualTo>" + "    <PropertyName>foo</PropertyName>"
            + "    <Literal>bar</Literal>" + "  </PropertyIsEqualTo>" + "</Filter>";

        OGCConfiguration configuration = new OGCConfiguration();
        configuration.getProperties().add(Properties.IGNORE_SCHEMA_LOCATION);

        Parser parser = new Parser(configuration);
        Filter filter = (Filter) parser.parse(new ByteArrayInputStream(xml.getBytes()));
        assertNotNull(filter);
    }
}
