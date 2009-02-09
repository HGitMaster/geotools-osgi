/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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

import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeRasterTile;

/**
 * 
 * @author Gabriel Roldan (OpenGeo)
 * 
 * @deprecated leaving in test code by now until making sure we're not loosing test coverage
 */
@Deprecated
class UnsignedShortBandCopier extends ArcSDERasterBandCopier {

    Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    @Override
    public void copyPixelData(final SeRasterTile tile, final WritableRaster raster,
            final int copyOffX, final int copyOffY, final int targetBand)
            throws DataSourceException {
        if (raster.getTransferType() != DataBuffer.TYPE_USHORT) {
            // this can't happen, it'd have been caught earlier when we created the transfer object.
            throw new IllegalArgumentException(
                    "Can't copy ArcSDE Raster data from an SE_PIXEL_TYPE_16BIT_U raster to a java.awt.Raster with a transferType of "
                            + raster.getTransferType());
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("copying raster data band " + tile.getBandId().longValue()
                    + " into image band " + targetBand);
        }

        final int numPixels = tile.getNumPixels();
        if (numPixels == 0) {
            // no pixels to copy, skip this one.
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("no pixels to copy in raster tile " + tile.getColumnIndex() + ","
                        + tile.getRowIndex());
            }
            return;
        }
        final int[] pixelData = new int[numPixels];
        final byte[] bitmaskData;
        try {
            tile.getPixels(pixelData);
            // This is a virtually undocumented function. I figured out what it
            // was supposed to be by looking here:
            // http://edndoc.esri.com/arcsde/9.2/api/japi/docs/com/esri/sde/sdk/client/SeRasterData.
            // html#setScanLine(int,%20byte[],%20int,%20byte[],%20int)
            // Basically, it's an array of 1-bit values, fully packed (8 1-bit
            // indicators for 8 pixels packed into each byte).
            // If there's a '0' at the nth position, it means that the n'th
            // pixel is a no-data pixel.
            bitmaskData = tile.getBitMaskData();
        } catch (Exception e) {
            throw new DataSourceException(e);
        }
        final int imgWidth = raster.getWidth() + copyOffX > tileWidth ? tileWidth - copyOffX
                : raster.getWidth();
        final int imgHeight = raster.getHeight() + copyOffY > tileHeight ? tileHeight - copyOffY
                : raster.getHeight();

        final int numBands = raster.getNumBands();
        final boolean checkAlpha = numBands == 4 && bitmaskData != null && bitmaskData.length > 0;
        boolean sampleIsNoData;
        int pixArrayOffset;
        int bandSample;
        for (int x = 0; x < imgWidth; x++) {
            for (int y = 0; y < imgHeight; y++) {
                pixArrayOffset = (y + copyOffY) * tileWidth + (x + copyOffX);
                bandSample = pixelData[pixArrayOffset] & 0xFFFF;
                raster.setSample(x, y, targetBand, bandSample);
                if (numBands == 4) {
                    if (checkAlpha) {
                        sampleIsNoData = ((bitmaskData[pixArrayOffset / 8] >> (7 - (pixArrayOffset % 8))) & 0x01) == 0x00;
                        if (sampleIsNoData) {
                            // it's a no-data pixel. Make it transparent if there's
                            // a 4th band, and also make it white.
                            raster.setSample(x, y, 3, 0);
                            raster.setSample(x, y, targetBand, 255);
                            continue;
                        }
                    } else {
                        // verify we have an opaque pixel
                        raster.setSample(x, y, 3, 255);
                    }
                }
            }
        }
    }
}
