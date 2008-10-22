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

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Add;

public class GeoApiSqlBuilderTest extends SQLFilterTestSupport {

    GeoAPISQLBuilder builder;

    FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
    
    public GeoApiSqlBuilderTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        final FilterToSQL filterToSQL = new FilterToSQL();
        filterToSQL.setSqlNameEscape("\"");
        builder = new GeoAPISQLBuilder(filterToSQL, testSchema, null);
    }

    public void testExpression() throws Exception {
        Add a = ff.add(ff.property("testInteger"), ff.literal(5));
        StringBuffer sb = new StringBuffer();
        builder.encode(sb, a);
        assertEquals("\"testInteger\" + 5", sb.toString());
    }
    
    public void testFilter() throws Exception {
        PropertyIsEqualTo equal = ff.equal(ff.property("testInteger"), ff.literal(5), false);
        StringBuffer sb = new StringBuffer();
        builder.encode(sb, equal);
        assertEquals("\"testInteger\" = 5", sb.toString());
    }
}
