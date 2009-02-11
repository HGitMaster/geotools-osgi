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
package org.geotools.arcsde.gce.band;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

import org.geotools.arcsde.gce.RasterCellType;
import org.geotools.arcsde.gce.RasterTestData;
import org.geotools.data.DataSourceException;
import org.junit.Ignore;
import org.junit.Test;

public class UnsignedByteRGBABandCopierTest extends AbstractArcsdeBandCopierOnlineTest {

    private static final RasterCellType pixelType = RasterCellType.TYPE_8BIT_U;

    @Override
    protected BufferedImage getTargetImage(final int width, final int height, final int numBands) {
        BufferedImage compatibleImage;
        try {
            compatibleImage = RasterTestData.createCompatibleBufferedImage(width, height, numBands,
                    pixelType, null);
        } catch (DataSourceException e) {
            throw new RuntimeException(e);
        }
        return compatibleImage;
    }

    @Test
    public void testSingleBand() throws Exception {
        final int numBands = 1;
        final IndexColorModel colorModel = null;
        testArcSDEBandCopier(numBands, pixelType, colorModel);
    }

    @Test
    @Ignore
    public void testThreeBands() throws Exception {
        final int numBands = 3;
        final IndexColorModel colorModel = null;
        testArcSDEBandCopier(numBands, pixelType, colorModel);
    }
}
