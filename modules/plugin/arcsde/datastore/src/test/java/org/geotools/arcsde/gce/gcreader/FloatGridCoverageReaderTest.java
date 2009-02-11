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
import java.awt.image.RenderedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.geotools.arcsde.ArcSDERasterFormatFactory;
import org.geotools.arcsde.gce.ArcSDERasterFormat;
import org.geotools.arcsde.gce.RasterCellType;
import org.geotools.arcsde.gce.RasterTestData;
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
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class FloatGridCoverageReaderTest {

    static RasterTestData rasterTestData;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        rasterTestData = new RasterTestData();
        rasterTestData.setUp();
        rasterTestData.loadFloatRaster();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // rasterTestData.tearDown();
    }

    @Test
    public void testReadFloatRaster() throws Exception {
        final String floatUrl = rasterTestData.createCoverageUrl(RasterCellType.TYPE_32BIT_REAL, 1);

        ArcSDERasterFormat format = new ArcSDERasterFormatFactory().createFormat();
        // a fix

        // SeExtent imgExtent = new SeExtent(245900, 899600, 246300, 900000);
        // SeCoordinateReference crs =
        // getSeCRSFromPeProjectedCSId(PePCSDefs.PE_PCS_NAD_1983_HARN_MA_M);

        CoordinateReferenceSystem crs = CRS.decode("EPSG:2805");

        GridGeometry2D gg2d = new GridGeometry2D(new GeneralGridRange(new Rectangle(201, 201)),
                new ReferencedEnvelope(245900, 246300, 899600, 900000, crs));

        GeneralParameterValue[] requestParams = new Parameter[1];
        requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D, gg2d);

        AbstractGridCoverage2DReader reader = format.getReader(floatUrl);

        Assert.assertNotNull(reader);

        GridCoverage2D coverage = (GridCoverage2D) reader.read(requestParams);

        Assert.assertNotNull(coverage);

        RenderedImage actualImage = coverage.view(ViewType.GEOPHYSICS).getRenderedImage();
        ImageIO.write(actualImage, "TIFF", new File("/tmp/testReadFloatRaster.tiff"));

        final String sampleFileName = rasterTestData
                .getRasterTestDataProperty("sampledata.floatraster");
        BufferedImage expected = ImageIO.read(TestData.file(null, sampleFileName));

        ImageIO.write(expected, "TIFF", new File("/tmp/testReadFloatRaster-original.tiff"));

        Assert.assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
                actualImage, expected));

    }
}
