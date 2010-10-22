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

import org.geotools.coverage.grid.io.UnknownFormat;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 * 
 * Test class invoking BaseGridFormatFactorySpi methods.
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/plugin/imageio-ext-gdal/src/test/java/org/geotools/coverageio/GDALGridFormatFactorySpiTest.java $
 */
public class GDALGridFormatFactorySpiTest {

    protected final static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(GDALGridFormatFactorySpiTest.class);

    @Test
    public void test() throws Exception {
        final BaseGridFormatFactorySPI spi = new BaseGridFormatFactorySPI();
        Assert.assertEquals(true, spi.createFormat() instanceof UnknownFormat);
        Assert.assertEquals(false, spi.isAvailable());
        Assert.assertEquals(true, spi.getImplementationHints().isEmpty());
    }

}
