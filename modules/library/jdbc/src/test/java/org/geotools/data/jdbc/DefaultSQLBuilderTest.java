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
package org.geotools.data.jdbc;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.SQLEncoder;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Add;

public class DefaultSQLBuilderTest extends TestCase {

    DefaultSQLBuilder builder;

    FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

    protected void setUp() throws Exception {
        final SQLEncoder encoder = new SQLEncoder();
        encoder.setSqlNameEscape("\"");
        builder = new DefaultSQLBuilder(encoder);
    }

    public void testExpression() throws Exception {
        Add a = ff.add(ff.property("col"), ff.literal(5));
        StringBuffer sb = new StringBuffer();
        builder.encode(sb, a);
        assertEquals("\"col\" + 5", sb.toString());
    }
    
    public void testFilter() throws Exception {
        PropertyIsEqualTo equal = ff.equal(ff.property("col"), ff.literal(5), false);
        StringBuffer sb = new StringBuffer();
        builder.encode(sb, equal);
        assertEquals("\"col\" = 5", sb.toString());
    }
}
