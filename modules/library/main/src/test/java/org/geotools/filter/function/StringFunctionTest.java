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
package org.geotools.filter.function;

import static junit.framework.Assert.assertEquals;

import org.geotools.factory.CommonFactoryFinder;
import org.junit.Test;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

public class StringFunctionTest  {
    
    FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

    @Test
    public void testStrReplace() {
        Literal foo = ff.literal("foo");
        Literal o = ff.literal("o");
        Literal bar = ff.literal("bar");
        
        Function f = ff.function("strReplace", new Expression[]{foo,o,bar,ff.literal(true)});
        String s = (String) f.evaluate(null,String.class);
        assertEquals( "fbarbar", s );
        
        f = ff.function("strReplace", new Expression[]{foo,o,bar,ff.literal(false)});
        s = (String) f.evaluate(null,String.class);
        assertEquals( "fbaro", s );
    }
    
    @Test
    public void testParseLong() {
        assertEquals(Long.MAX_VALUE , ff.function("parseLong", ff.literal(Long.MAX_VALUE + "")).evaluate(null));
        assertEquals(5l , ff.function("parseLong", ff.literal("5.0")).evaluate(null));
    }
    
    @Test
    public void testCapitalize() {
        assertEquals("United Kingdom", ff.function("strCapitalize", ff.literal("UNITED KINGDOM")).evaluate(null));
        assertEquals("United Kingdom", ff.function("strCapitalize", ff.literal("UnItEd kInGdOm")).evaluate(null));
    }
    
    
}
