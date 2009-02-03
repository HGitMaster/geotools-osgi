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
 *
 */
package org.geotools.arcsde.gce;

import java.awt.Rectangle;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterBand;

public class ArcSDERasterProducerTest {

    private static RasterTestData testData;

    @BeforeClass
    public static void oneTimeSetup() throws IOException {
        testData = new RasterTestData();
        testData.setUp();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        testData.tearDown();
    }

    @Test
    public void testCreateRGBColorMappedRaster() throws Exception {
        final String tableName = testData.loadRGBColorMappedRaster();

        SeRasterAttr attr = testData.getRasterAttributes(tableName, new Rectangle(0, 0, 0, 0), 0,
                new int[] { 1 });
        SeRasterBand[] bands = attr.getBands();

        Assert.assertTrue(bands.length == 1);
        Assert.assertTrue(bands[0].hasColorMap());
        // SeRasterBandColorMap colorMap = bands[0].getColorMap();
        // Assert.assertTrue(colorMap != null);
    }

    @Test
    public void testCreateGrayscaleByteRaster() throws Exception {
        final String tableName = testData.loadOneByteGrayScaleRaster();

        SeRasterAttr attr = testData.getRasterAttributes(tableName, new Rectangle(0, 0, 0, 0), 0,
                new int[] { 1 });
        Assert.assertTrue(attr.getPixelType() == SeRaster.SE_PIXEL_TYPE_8BIT_U);
        Assert.assertTrue(attr.getNumBands() == 1);
        Assert.assertTrue(attr.getBandInfo(1).hasColorMap() == false);

    }

    @Test
    public void testCreateFloatRaster() throws Exception {
        final String tableName = testData.loadFloatRaster();

        SeRasterAttr attr = testData.getRasterAttributes(tableName, new Rectangle(0, 0, 0, 0), 0,
                new int[] { 1 });
        Assert.assertTrue(attr.getPixelType() == SeRaster.SE_PIXEL_TYPE_32BIT_REAL);
        Assert.assertTrue(attr.getNumBands() == 1);
        Assert.assertTrue(attr.getBandInfo(1).hasColorMap() == false);
        Assert.assertEquals(201, attr.getImageWidth());
        Assert.assertEquals(201, attr.getImageHeight());
    }
}
