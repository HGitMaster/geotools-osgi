/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.coverageio;

import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.coverage.grid.io.UnknownFormat;

/**
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 * 
 * Base testing class invoking BaseGridFormatFactorySpi methods.
 */
public class BaseGridFormatFactorySpiTest extends TestCase {

    protected final static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.coverageio");

    public BaseGridFormatFactorySpiTest(String name) {
        super(name);
    }

    public static final void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(BaseGridFormatFactorySpiTest.class);
    }

    public void test() throws Exception {
        final BaseGridFormatFactorySPI spi = new BaseGridFormatFactorySPI();
        assertEquals(true, spi.createFormat() instanceof UnknownFormat);
        assertEquals(false, spi.isAvailable());
        assertEquals(true, spi.getImplementationHints().isEmpty());
    }

}
