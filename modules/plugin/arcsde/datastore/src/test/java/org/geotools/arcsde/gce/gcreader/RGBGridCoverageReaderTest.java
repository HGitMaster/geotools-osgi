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
package org.geotools.arcsde.gce.gcreader;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.geotools.arcsde.ArcSDERasterFormatFactory;
import org.geotools.arcsde.gce.RasterTestData;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.ViewType;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;
import org.geotools.test.TestData;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.coverage.grid.Format;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class RGBGridCoverageReaderTest {

    static RasterTestData rasterTestData;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        rasterTestData = new RasterTestData();
        rasterTestData.setUp();
        rasterTestData.loadRGBRaster();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        rasterTestData.tearDown();
    }

    @Test
    public void testReadRGBRaster() throws Exception {
        Properties p = rasterTestData.getTestData().getConProps();
        ArcSDEConnectionConfig config = new ArcSDEConnectionConfig(p);

        String rgbUrl = "sde://" + config.getUserName() + ":" + config.getUserPassword() + "@"
                + config.getServerName() + ":" + config.getPortNumber() + "/"
                + config.getDatabaseName() + "#" + rasterTestData.getRGBRasterTableName();

        GridCoverage2D gc;
        Format f = new ArcSDERasterFormatFactory().createFormat();
        // a fix

        GeneralParameterValue[] requestParams = new Parameter[1];

        CoordinateReferenceSystem crs = CRS.decode("EPSG:2805");

        GridGeometry2D gg2d = new GridGeometry2D(new GeneralGridRange(new Rectangle(256, 128)),
                new ReferencedEnvelope(231000, 231000 + 128, 898000, 898000 + 64, crs));

        requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D, gg2d);

        AbstractGridCoverage2DReader r = (AbstractGridCoverage2DReader) ((AbstractGridFormat) f)
                .getReader(rgbUrl);
        gc = (GridCoverage2D) r.read(requestParams);
        Assert.assertNotNull(gc);
        ImageIO.write(gc.view(ViewType.PHOTOGRAPHIC).getRenderedImage(), "PNG", new File("/tmp/"
                + Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));

        BufferedImage expected = ImageIO.read(TestData.file(null, rasterTestData
                .getRasterTestDataProperty("sampledata.rgbraster")));
        expected = expected.getSubimage(0, expected.getHeight() - 128, 256, 128);

        ImageIO.write(expected, "PNG", new File("/tmp/"
                + Thread.currentThread().getStackTrace()[1].getMethodName() + "-original.png"));

        Assert.assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(gc
                .view(ViewType.PHOTOGRAPHIC).getRenderedImage(), expected));

    }
}
