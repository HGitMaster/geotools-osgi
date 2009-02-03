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

import org.geotools.arcsde.gce.RasterUtils;
import org.geotools.arcsde.gce.imageio.RasterCellType;
import org.geotools.data.DataSourceException;
import org.junit.Test;

/**
 * 
 * @author Gabriel Roldan (OpenGeo)
 */
public class ShortBandCopierTest extends AbstractArcsdeBandCopierOnlineTest {
    
    private static final RasterCellType pixelType = RasterCellType.TYPE_16BIT_S;

    @Override
    protected BufferedImage getTargetImage(final int width, final int height, final int numBands) {
        BufferedImage compatibleImage;
        try {
            compatibleImage = RasterUtils.createCompatibleBufferedImage(width, height, numBands,
                    pixelType, null);
        } catch (DataSourceException e) {
            throw new RuntimeException(e);
        }
        return compatibleImage;
    }

    @Test
    public void testSingleBand() throws Exception {
        final int numBands = 1;
        final RasterCellType pixelType = RasterCellType.TYPE_16BIT_S;
        final IndexColorModel colorModel = null;
        testArcSDEBandCopier(numBands, pixelType, colorModel);
    }
}
