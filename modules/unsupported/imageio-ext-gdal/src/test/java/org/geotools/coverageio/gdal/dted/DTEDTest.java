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
package org.geotools.coverageio.gdal.dted;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.File;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverageio.gdal.BaseGDALGridCoverage2DReader;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.test.TestData;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

/**
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 *
 * Testing {@link DTEDReader}
 */
public final class DTEDTest extends AbstractDTEDTestCase {
    /**
     * file name of a valid DTED sample data to be used for tests. 
     */
    private final static String fileName = "n43.dt0";

    /**
     * Creates a new instance of {@code DTEDTest}
     *
     * @param name
     */
    public DTEDTest(String name) {
        super(name);
    }

    public static final void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(DTEDTest.class);
    }

    public void test() throws Exception {
        if (!testingEnabled()) {
            return;
        }

        // Preparing an useful layout in case the image is striped.
        final ImageLayout l = new ImageLayout();
        l.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(512).setTileWidth(512);

        Hints hints = new Hints();
        hints.add(new RenderingHints(JAI.KEY_IMAGE_LAYOUT, l));

        // get a reader
        final File file = TestData.file(this, fileName);
        final BaseGDALGridCoverage2DReader reader = new DTEDReader(file, hints);

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
                    + (oldEnvelope.getLength(1) / cropFactor)
                },
                new double[] {
                    oldEnvelope.getUpperCorner().getOrdinate(0),
                    oldEnvelope.getUpperCorner().getOrdinate(1)
                });
        cropEnvelope.setCoordinateReferenceSystem(reader.getCrs());

        final ParameterValue gg = (ParameterValue) ((AbstractGridFormat) reader.getFormat()).READ_GRIDGEOMETRY2D
            .createValue();
        gg.setValue(new GridGeometry2D(
                new GeneralGridRange(
                    new Rectangle(0, 0, (int) (range.width / 2.0 / cropFactor),
                        (int) (range.height / 2.0 / cropFactor))), cropEnvelope));
        gc = (GridCoverage2D) reader.read(new GeneralParameterValue[] { gg });
        assertNotNull(gc);
        if (TestData.isInteractiveTest()) {
            gc.show();
        } else {
            gc.getRenderedImage().getData();
        }
    }
}
