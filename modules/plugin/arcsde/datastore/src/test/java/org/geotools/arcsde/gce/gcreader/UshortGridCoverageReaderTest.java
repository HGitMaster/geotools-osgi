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
import java.awt.image.RenderedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.geotools.arcsde.ArcSDERasterFormatFactory;
import org.geotools.arcsde.gce.ArcSDERasterFormat;
import org.geotools.arcsde.gce.RasterTestData;
import org.geotools.arcsde.gce.imageio.RasterCellType;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.ViewType;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class UshortGridCoverageReaderTest {

    static RasterTestData rasterTestData;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        rasterTestData = new RasterTestData();
        rasterTestData.setUp();
        rasterTestData.loadUshortRaster();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // rasterTestData.tearDown();
    }

    @Test
    public void testReadUshortRaster_SingleBand_No_CM() throws Exception {
        final String connectionUrl = rasterTestData.createCoverageUrl(RasterCellType.TYPE_16BIT_U,
                1);

        ArcSDERasterFormat format = new ArcSDERasterFormatFactory().createFormat();

        CoordinateReferenceSystem crs = CRS.decode("EPSG:2805");

        GeneralGridRange querySize = new GeneralGridRange(new Rectangle(256, 256));
        ReferencedEnvelope queryEnvelope = new ReferencedEnvelope(0, 512, 0, 512, crs);
        GridGeometry2D queryGeom = new GridGeometry2D(querySize, queryEnvelope);

        GeneralParameterValue[] requestParams = new Parameter[1];
        requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D, queryGeom);

        AbstractGridCoverage2DReader reader = format.getReader(connectionUrl);

        Assert.assertNotNull(reader);

        GridCoverage2D coverage = (GridCoverage2D) reader.read(requestParams);

        Assert.assertNotNull(coverage);

        // final RenderedImage expectedImage = createExpectedImage();
        final RenderedImage actualImage = coverage.view(ViewType.GEOPHYSICS).getRenderedImage();
        ImageIO.write(actualImage, "TIFF", new File(
                "/tmp/testReadUshortRaster_SingleBand_No_CM.tiff"));

    }

}
