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

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import org.geotools.arcsde.gce.imageio.RasterCellType;
import org.junit.Test;

/**
 * 
 * @author Gabriel Roldan (OpenGeo)
 */
public class UnsignedShortGrayscaleBandCopierTest extends AbstractArcsdeBandCopierOnlineTest {

    @Override
    protected BufferedImage getTargetImage(final int width, final int height, final int numBands) {
        final BufferedImage fromSdeImage;
        if (numBands != 1) {
            throw new IllegalArgumentException("onle single-band images supported");
        }
        final int pixelStride = 1;
        final int scanLineStride = width;
        final int[] bandOffsets = new int[] { 0 };

        SampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_USHORT, width, height,
                pixelStride, scanLineStride, bandOffsets);
        DataBuffer db = new DataBufferUShort(width * height);
        WritableRaster wr = Raster.createWritableRaster(sm, db, null);
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorModel cm = new ComponentColorModel(cs, false, true, Transparency.OPAQUE,
                DataBuffer.TYPE_USHORT);
        fromSdeImage = new BufferedImage(cm, wr, false, null);

        return fromSdeImage;
    }

    @Test
    public void testLiveSingleBand() throws Exception {
        final int numBands = 1;
        final RasterCellType pixelType = RasterCellType.TYPE_16BIT_U;
        final IndexColorModel colorModel = null;
        testArcSDEBandCopier(numBands, pixelType, colorModel);
    }
}
