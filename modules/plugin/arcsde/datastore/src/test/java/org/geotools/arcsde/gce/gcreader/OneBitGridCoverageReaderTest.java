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
package org.geotools.arcsde.gce.gcreader;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.geotools.arcsde.ArcSDERasterFormatFactory;
import org.geotools.arcsde.gce.RasterTestData;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.coverage.grid.Format;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeRasterAttr;

public class OneBitGridCoverageReaderTest {

    static RasterTestData rasterTestData;

    static String sderasterurlbase;
    static String tableName;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        rasterTestData = new RasterTestData();
        rasterTestData.setUp();
        tableName = rasterTestData.load1bitRaster();

        ArcSDEConnectionConfig config = rasterTestData.getConnectionPool().getConfig();
        sderasterurlbase = "sde://" + config.getUserName() + ":" + config.getUserPassword() + "@"
                + config.getServerName() + ":" + config.getPortNumber() + "/"
                + config.getDatabaseName() + "#";
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        rasterTestData.tearDown();
    }

    @Test
    public void testRead1bitCoverageExact() throws Exception {

        GridCoverage2D gc;
        Format f = new ArcSDERasterFormatFactory().createFormat();
        AbstractGridCoverage2DReader r = (AbstractGridCoverage2DReader) ((AbstractGridFormat) f)
                .getReader(sderasterurlbase + tableName);

        SeRasterAttr ras = rasterTestData.getRasterAttributes(tableName, new Rectangle(0, 0,
                0, 0), 0, new int[] { 1 });
        int totalheight = ras.getImageHeightByLevel(0);
        int totalwidth = ras.getImageWidthByLevel(0);
        SeExtent ext = ras.getExtentByLevel(0);

        GeneralParameterValue[] requestParams = new Parameter[1];
        GridGeometry2D ggr2d = new GridGeometry2D(new GeneralGridRange(new Rectangle(totalwidth,
                totalheight)), new ReferencedEnvelope(ext.getMinX(), ext.getMaxX(), ext.getMinY(),
                ext.getMaxY(), r.getCrs()));

        requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D, ggr2d);
        gc = (GridCoverage2D) r.read(requestParams);
        Assert.assertNotNull(gc);
        // ImageIO.write(gc.geophysics(true).getRenderedImage(), "PNG", new File("/tmp/" +
        // Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));

        final String rasFileName = rasterTestData
                .getRasterTestDataProperty("sampledata.onebitraster");
        BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null,
                rasFileName));

        Assert.assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(gc
                .geophysics(true).getRenderedImage(), originalImage));
    }

    @Test
    public void testRead1bitCoverageReproject() throws Exception {

        GridCoverage2D gc;
        Format f = new ArcSDERasterFormatFactory().createFormat();
        AbstractGridCoverage2DReader r = (AbstractGridCoverage2DReader) ((AbstractGridFormat) f)
                .getReader(sderasterurlbase + tableName);

        SeRasterAttr ras = rasterTestData.getRasterAttributes(tableName, new Rectangle(0, 0,
                0, 0), 0, new int[] { 1 });
        int totalheight = ras.getImageHeightByLevel(0);
        int totalwidth = ras.getImageWidthByLevel(0);
        SeExtent ext = ras.getExtentByLevel(0);

        CoordinateReferenceSystem origCrs = r.getCrs();
        ReferencedEnvelope originalFullEnv = new ReferencedEnvelope(ext.getMinX(), ext.getMaxX(),
                ext.getMinY(), ext.getMaxY(), origCrs);
        ReferencedEnvelope wgs84FullEnv = originalFullEnv.transform(CRS.decode("EPSG:4326"), true);

        GeneralParameterValue[] requestParams = new Parameter[1];
        GridGeometry2D ggr2d = new GridGeometry2D(new GeneralGridRange(new Rectangle(totalwidth,
                totalheight)), wgs84FullEnv);

        requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D, ggr2d);
        gc = (GridCoverage2D) r.read(requestParams);
        Assert.assertNotNull(gc);
        // ImageIO.write(gc.geophysics(true).getRenderedImage(), "PNG", new File("/tmp/" +
        // Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));

        // err...there's really nothing to compare it to, I guess...
        // final String rasFileName =
        // rasterTestData.getRasterTestDataProperty("sampledata.onebitraster");
        // BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null,
        // rasFileName));

        // assertTrue("Image from SDE isn't what we expected.",
        // RasterTestData.imageEquals(gc.geophysics(true).getRenderedImage(), originalImage));
    }
}
