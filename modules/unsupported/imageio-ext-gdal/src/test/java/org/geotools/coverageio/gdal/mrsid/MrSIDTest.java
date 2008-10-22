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
 *
 */
package org.geotools.coverageio.gdal.mrsid;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.coverageio.BaseGridCoverage2DReader;
import org.geotools.coverageio.gdal.BaseGDALGridFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.test.TestData;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

/**
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 * 
 * Testing {@link MrSIDReader}
 */
public final class MrSIDTest extends AbstractMrSIDTestCase {
    /**
     * file name of a valid MrSID sample data to be used for tests.
     * 
     * We suggest to download a valid MrSID sample file from this site:
     * https://zulu.ssc.nasa.gov/mrsid/
     * 
     * For each .SID file, a .MET file exists. Use the last one to build a valid
     * .PRJ for the sample. If you are only interested in reading/rendering
     * capabilities, or displaying coverages as simple rasters, the .PRJ is not
     * necessary. However, a valid .PRJ file is required anytime you need to use
     * the sample data as a coherently GeoReferenced coverage, by means of, as
     * an instance, uDIG.
     */
    private final static String fileName = "n13250i.sid";

    /**
     * Creates a new instance of {@link MrSIDTest}
     * 
     * @param name
     */
    public MrSIDTest(String name) {
        super(name);
    }

    /**
     * Test for reading a grid coverage from a MrSID source
     * 
     * @throws Exception
     */
    public void testVisualization() throws Exception {
        if (!testingEnabled()) {
            return;
        }
        // read in the grid coverage
        if (fileName.equalsIgnoreCase("")) {
            LOGGER
                    .info("==================================================================\n"
                            + " Warning! No valid test File has been specified.\n"
                            + " Please provide a valid sample in the source code and repeat this test!\n"
                            + "========================================================================");

            return;
        }

        // get a reader
        final BaseGridCoverage2DReader reader = new MrSIDReader(TestData.file(
                this, fileName));

        // /////////////////////////////////////////////////////////////////////
        //
        // read once
        //
        // /////////////////////////////////////////////////////////////////////
        GridCoverage2D gc = (GridCoverage2D) reader.read(null);
        assertNotNull(gc);

        if (TestData.isInteractiveTest()) {
            gc.show();
        } else {
            gc.getRenderedImage().getData();
        }

        // /////////////////////////////////////////////////////////////////////
        //
        // read again with subsampling and crop
        //
        // /////////////////////////////////////////////////////////////////////
        final double cropFactor = 2.0;
        final int oldW = gc.getRenderedImage().getWidth();
        final int oldH = gc.getRenderedImage().getHeight();
        final Rectangle range = reader.getOriginalGridRange().toRectangle();
        final GeneralEnvelope oldEnvelope = reader.getOriginalEnvelope();
        final GeneralEnvelope cropEnvelope = new GeneralEnvelope(new double[] {
                oldEnvelope.getLowerCorner().getOrdinate(0)
                        + (oldEnvelope.getLength(0) / cropFactor),

                oldEnvelope.getLowerCorner().getOrdinate(1)
                        + (oldEnvelope.getLength(1) / cropFactor) },
                new double[] { oldEnvelope.getUpperCorner().getOrdinate(0),
                        oldEnvelope.getUpperCorner().getOrdinate(1) });
        cropEnvelope.setCoordinateReferenceSystem(reader.getCrs());

        final ParameterValue gg = (ParameterValue) ((AbstractGridFormat) reader
                .getFormat()).READ_GRIDGEOMETRY2D.createValue();
        gg.setValue(new GridGeometry2D(new GeneralGridRange(new Rectangle(0, 0,
                (int) (range.width / 2.0 / cropFactor),
                (int) (range.height / 2.0 / cropFactor))), cropEnvelope));
        gc = (GridCoverage2D) reader.read(new GeneralParameterValue[] { gg });
        assertNotNull(gc);
        // NOTE: in some cases might be too restrictive
        assertTrue(cropEnvelope.equals(gc.getEnvelope(), XAffineTransform
                .getScale(((AffineTransform) ((GridGeometry2D) gc
                        .getGridGeometry()).getGridToCRS2D())) / 2, true));
        // this should be fine since we give 1 pixel tolerance
        assertEquals(oldW / 2.0 / (cropFactor), gc.getRenderedImage()
                .getWidth(), 1);
        assertEquals(oldH / 2.0 / (cropFactor), gc.getRenderedImage()
                .getHeight(), 1);

        if (TestData.isInteractiveTest()) {
            gc.show();
        } else {
            gc.getRenderedImage().getData();
        }

        // /////////////////////////////////////////////////////////////////////
        //
        // read ignoring overviews with subsampling and crop, using Jai,
        // multithreading and customized tilesize
        //
        // /////////////////////////////////////////////////////////////////////
        final ParameterValue policy = (ParameterValue) ((AbstractGridFormat) reader
                .getFormat()).OVERVIEW_POLICY.createValue();
        policy.setValue(OverviewPolicy.IGNORE);
        final ParameterValue mt = (ParameterValue) ((BaseGDALGridFormat) reader
                .getFormat()).USE_MULTITHREADING.createValue();
        mt.setValue(true);
        final ParameterValue tilesize = (ParameterValue) ((BaseGDALGridFormat) reader
                .getFormat()).SUGGESTED_TILE_SIZE.createValue();
        tilesize.setValue("128,128");
        final ParameterValue useJaiRead = (ParameterValue) ((BaseGDALGridFormat) reader
                .getFormat()).USE_JAI_IMAGEREAD.createValue();
        useJaiRead.setValue(true);

        gc = (GridCoverage2D) reader.read(new GeneralParameterValue[] { gg,
                policy, mt, tilesize, useJaiRead });
        assertNotNull(gc);
        // NOTE: in some cases might be too restrictive
        assertTrue(cropEnvelope.equals(gc.getEnvelope(), XAffineTransform
                .getScale(((AffineTransform) ((GridGeometry2D) gc
                        .getGridGeometry()).getGridToCRS2D())) / 2, true));
        // this should be fine since we give 1 pixel tolerance
        assertEquals(oldW / cropFactor, gc.getRenderedImage().getWidth(), 1);
        assertEquals(oldH / cropFactor, gc.getRenderedImage().getHeight(), 1);

        if (TestData.isInteractiveTest()) {
            gc.show();
        } else {
            gc.getRenderedImage().getData();
        }

        if (TestData.isInteractiveTest()) {
            // printing CRS information
            LOGGER.info(gc.getCoordinateReferenceSystem().toWKT());
            LOGGER.info(gc.getEnvelope().toString());
        }
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        // Test read exploiting common JAI operations (Crop-Translate-Rotate)
        suite.addTest(new MrSIDTest("testVisualization"));

        return suite;
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
