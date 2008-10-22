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
package org.geotools.arcsde.gce.band;

import java.awt.image.WritableRaster;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SeRasterTile;

public class FloatBandCopier extends ArcSDERasterBandCopier {

    Logger LOGGER = org.geotools.util.logging.Logging.getLogger(this.getClass().toString());

    @Override
    public void copyPixelData(SeRasterTile tile,
            WritableRaster raster,
            int copyOffX,
            int copyOffY,
            int targetBand) throws DataSourceException {

        if (LOGGER.isLoggable(Level.FINER))
            LOGGER.finer("copying raster data band " + tile.getBandId().longValue()
                    + " into image band " + targetBand);

        final int numPixels = tile.getNumPixels();
        if (numPixels == 0) {
            // no pixels to copy, skip this one.
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("no pixels to copy in raster tile " + tile.getColumnIndex() + ","
                        + tile.getRowIndex());
            return;
        }
        float[] pixelData = new float[numPixels];
        byte[] bitmaskData;
        try {
            pixelData = tile.getPixels(pixelData);
            // This is a virtually undocumented function. I figured out what it
            // was supposed to be by looking here:
            // http://edndoc.esri.com/arcsde/9.2/api/japi/docs/com/esri/sde/sdk/client/SeRasterData.html#setScanLine(int,%20byte[],%20int,%20byte[],%20int)
            // Basically, it's an array of 1-bit values, fully packed (8 1-bit
            // indicators for 8 pixels packed into each byte).
            // If there's a '0' at the nth position, it means that the n'th
            // pixel is a no-data pixel.
            bitmaskData = tile.getBitMaskData();
        } catch (Exception e) {
            throw new DataSourceException(e);
        }
        int x, y;
        final boolean haveBMData;
        if (bitmaskData.length > 0)
            haveBMData = true;
        else
            haveBMData = false;

        final int imgWidth = raster.getWidth() + copyOffX > tileWidth ? tileWidth - copyOffX
                : raster.getWidth();
        final int imgHeight = raster.getHeight() + copyOffY > tileHeight ? tileHeight - copyOffY
                : raster.getHeight();

        for (x = 0; x < imgWidth; x++) {
            // final float[] imageDataRow = new float[imgHeight];
            for (y = 0; y < imgHeight; y++) {
                final int pixArrayOffset = (y + copyOffY) * tileWidth + (x + copyOffX);
                if (haveBMData) {
                    if (((bitmaskData[pixArrayOffset / 8] >> (7 - (pixArrayOffset % 8))) & 0x01) == 0x00) {
                        // it's a no-data pixel. Make it transparent/no-data
                        // TODO: support nodata values here
                        raster.setSample(x, y, targetBand, 0.0f);
                        continue;
                    }
                }
                try {
                    final float sdePixelData = pixelData[pixArrayOffset];
                    raster.setSample(x, y, targetBand, sdePixelData);
                } catch (RuntimeException e) {
                    LOGGER.severe("at data " + (x + copyOffX) + "," + (y + copyOffY)
                            + "(img pixel " + x + "," + y + ")");
                    LOGGER.severe("number of pixels reported in tile was " + numPixels);
                    e.printStackTrace();
                    throw e;
                }
            }
        }

    }

}
