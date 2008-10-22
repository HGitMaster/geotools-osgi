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

import org.junit.Assert;
import org.junit.Test;

import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterBand;

public class ArcSDERasterProducerTest {

    @Test
    public void testCreateRGBColorMappedRaster() throws Exception {
        RasterTestData rtd = new RasterTestData();
        rtd.setUp();
        rtd.loadRGBColorMappedRaster();

        SeRasterAttr attr = rtd.getRasterAttributes(rtd.getRGBColorMappedRasterTableName(),
                new Rectangle(0, 0, 0, 0), 0, new int[] { 1 });
        SeRasterBand[] bands = attr.getBands();

        Assert.assertTrue(bands.length == 1);
        // Assert.assertTrue(bands[0].getColorMap() != null);

        rtd.tearDown();
    }

    @Test
    public void testCreateGrayscaleByteRaster() throws Exception {
        RasterTestData rtd = new RasterTestData();
        rtd.setUp();
        rtd.loadOneByteGrayScaleRaster();

        SeRasterAttr attr = rtd.getRasterAttributes(rtd.getGrayScaleOneByteRasterTableName(),
                new Rectangle(0, 0, 0, 0), 0, new int[] { 1 });
        Assert.assertTrue(attr.getPixelType() == SeRaster.SE_PIXEL_TYPE_8BIT_U);
        Assert.assertTrue(attr.getNumBands() == 1);
        Assert.assertTrue(attr.getBandInfo(1).hasColorMap() == false);

        rtd.tearDown();
    }

    @Test
    public void testCreateFloatRaster() throws Exception {
        RasterTestData rtd = new RasterTestData();
        rtd.setUp();
        rtd.loadFloatRaster();

        SeRasterAttr attr = rtd.getRasterAttributes(rtd.getFloatRasterTableName(), new Rectangle(0,
                0, 0, 0), 0, new int[] { 1 });
        Assert.assertTrue(attr.getPixelType() == SeRaster.SE_PIXEL_TYPE_32BIT_REAL);
        Assert.assertTrue(attr.getNumBands() == 1);
        Assert.assertTrue(attr.getBandInfo(1).hasColorMap() == false);

        rtd.tearDown();
    }
}
