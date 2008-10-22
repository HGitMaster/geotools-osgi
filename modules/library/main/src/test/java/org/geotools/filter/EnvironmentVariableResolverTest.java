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
package org.geotools.filter;

import java.io.StringWriter;

import org.geotools.filter.visitor.EnvironmentVariableResolver;


/**
 * Unit test for sql encoding of filters into where statements.
 *
 * @author Chris Holmes, TOPP
 *
 * @task REVISIT: validate these so we know if they break.
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/test/java/org/geotools/filter/EnvironmentVariableResolverTest.java $
 */
public class EnvironmentVariableResolverTest extends FilterTestSupport {
    private FilterFactory filterFac = FilterFactoryFinder.createFilterFactory();
//
//    /** Test suite for this test case */
//    TestSuite suite = null;
//
//    /** Constructor with test name. */
    String dataFolder = "";
    boolean setup = false;
//
    public EnvironmentVariableResolverTest(String testName) {
        super(testName);
        LOGGER.finer("running SQLEncoderTests");

        dataFolder = System.getProperty("dataFolder");

        if (dataFolder == null) {
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder = "file:////" + dataFolder + "/tests/unit/testData";
            LOGGER.finer("data folder is " + dataFolder);
        }
    }

    public void testConstructor() throws Exception {
        
        EnvironmentVariable mapScale = filterFac.createEnvironmentVariable("MapScaleDenominator");
        
        
        LiteralExpression testInt = filterFac.createLiteralExpression(5);
 
        MathExpression add = filterFac.createMathExpression(MathExpression.MATH_ADD);
        add.addLeftValue(testInt);
        add.addRightValue(mapScale);
        String in = add.toString();
        StringWriter output = new StringWriter();
        EnvironmentVariableResolver resolver = new EnvironmentVariableResolver();
        Expression resolved = resolver.resolve(add, 10);
        assertEquals(15,((Number)resolved.getValue(testFeature)).intValue());
    }
    
  

}
