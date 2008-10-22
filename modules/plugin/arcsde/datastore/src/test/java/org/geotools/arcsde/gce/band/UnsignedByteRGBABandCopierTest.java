/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.arcsde.gce.band;

import java.util.logging.Logger;

import org.geotools.arcsde.gce.RasterTestData;
import org.geotools.util.logging.Logging;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class UnsignedByteRGBABandCopierTest {

    static RasterTestData rasterTestData;

    static Logger LOGGER;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        LOGGER = Logging.getLogger(UnsignedByteRGBABandCopierTest.class.getCanonicalName());
        if (rasterTestData == null) {
            rasterTestData = new RasterTestData();
            rasterTestData.setUp();
            rasterTestData.loadRGBRaster();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        rasterTestData.tearDown();
    }

}
