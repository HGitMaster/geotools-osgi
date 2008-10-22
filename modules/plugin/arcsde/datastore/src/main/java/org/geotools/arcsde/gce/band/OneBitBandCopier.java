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

import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SeRasterTile;

public class OneBitBandCopier extends ArcSDERasterBandCopier {

    Logger LOGGER = org.geotools.util.logging.Logging.getLogger(this.getClass().toString());

    /**
     * @see org.geotools.arcsde.gce.band.ArcSDERasterBandCopier#copyPixelData(com.esri.sde.sdk.client.SeRasterTile,
     *      java.awt.image.WritableRaster, int, int, int) Note that this method is missing two
     *      pieces of functionality: - it ignores the arcsde bitmask (it's a one-bit image...how are
     *      you going to represent nodata?) - for non byte-aligned ending images (images that aren't
     *      a power-of-two pixels wide) it doesn't blit out the final pixels, instead leaving them
     *      in there to be ignored by the WritableRaster implementation. Both of these issues could
     *      be considered bugs.
     */
    @Override
    public void copyPixelData(SeRasterTile tile,
            WritableRaster raster,
            int copyOffX,
            int copyOffY,
            int targetBand) throws DataSourceException {
        final int numPixels = tile.getNumPixels();
        if (numPixels == 0) {
            // no pixels to copy, skip this one.
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("no pixels to copy in raster tile " + tile.getColumnIndex() + ","
                        + tile.getRowIndex());
            return;
        }
        byte[] pixelData;
        // byte[] bitmaskData;
        try {
            pixelData = tile.getPixelData();
            // bitmaskData = tile.getBitMaskData();
        } catch (Exception e) {
            throw new DataSourceException(e);
        }

        final byte[] imageDataBuf = ((DataBufferByte) raster.getDataBuffer()).getData(targetBand);
        // base byte index where we should start writing into the imageDataBuf
        final int imageDataBufTranslateX = (raster.getSampleModelTranslateX() * -1 + 7) / 8;
        final int imageDataBufOffset = (8 - ((raster.getSampleModelTranslateX() * -1) % 8)) % 8;
        final boolean isImageDataBufOffsetByteAligned = imageDataBufOffset == 0;
        final int imageDataBufTranslateY = (raster.getSampleModelTranslateY() * -1);

        // let's figure out the offsets into the byte-array that represent the start and end row for
        // our copying. I.e. the "y" values
        final int bytesPerImageRow = (raster.getSampleModel().getWidth() + 7) / 8; // the total
        // number of
        // imageDataBuf
        // bytes that
        // make up one
        // line of the
        // ENTIRE image
        final int bytesPerTileRow = (raster.getWidth() - imageDataBufOffset + 7) / 8; // the total
        // number of
        // bytes
        // that make
        // up one
        // row in
        // this
        // sub-image
        // (tile)
        final int bytesPerDataRow = (tileWidth + 7) / 8; // the total number of bytes that make
        // up one row in this data tile
        if ((copyOffY + raster.getHeight()) > tileHeight)
            throw new IllegalArgumentException(
                    "Won't copy raster tile data into an image that extends beyond the tile.  The image you've given this bandcopier is too big!");

        // now let's figure out where we should start and stop the copying for each row (the "x"
        // values)
        final int dataStartColumn = copyOffX / 8;
        final boolean isDataStartOffsetByteAligned = (copyOffX % 8) == 0;
        final int dataStartOffset = copyOffX % 8;

        for (int sdeDataRow = copyOffY; sdeDataRow < raster.getHeight() + copyOffY; sdeDataRow++) {
            final int imageRow = sdeDataRow - copyOffY;

            if (isDataStartOffsetByteAligned && isImageDataBufOffsetByteAligned) {
                // we're byte-aligned both on the source data array and the output-image data
                // array...much easier!
                System.arraycopy(pixelData, sdeDataRow * bytesPerDataRow + dataStartColumn,
                        imageDataBuf, (imageRow + imageDataBufTranslateY) * bytesPerImageRow
                                + imageDataBufTranslateX, bytesPerTileRow);
            } else {
                // blech. We're not byte-aligned.
                copyArrayWithBitShift(pixelData, sdeDataRow * bytesPerDataRow + dataStartColumn,
                        dataStartOffset + imageDataBufOffset, imageDataBuf,
                        (imageRow + imageDataBufTranslateY) * bytesPerImageRow
                                + imageDataBufTranslateX, 0, bytesPerTileRow,
                        (bytesPerTileRow + dataStartColumn) == bytesPerDataRow);
                // mask in the value from the previous tile to the left of this tile, if there is
                // one
                if (raster.getSampleModelTranslateX() != 0) {
                    copyArrayWithBitShift(pixelData,
                            sdeDataRow * bytesPerDataRow + dataStartColumn, 0, imageDataBuf,
                            (imageRow + imageDataBufTranslateY) * bytesPerImageRow
                                    + imageDataBufTranslateX - 1, 8 - imageDataBufOffset, 1, true);
                }
            }
        }
    }

    private byte[] copyArrayWithBitShift(byte[] src,
            int sstartindex,
            int sstartoffset,
            byte[] dest,
            int dstartindex,
            int dstartoffset,
            int length,
            boolean clipSource) {
        if (sstartindex + length > src.length)
            throw new ArrayIndexOutOfBoundsException("source array isn't " + (sstartindex + length)
                    + " bytes long.");

        if (dstartindex + length > dest.length)
            throw new ArrayIndexOutOfBoundsException("destination array isn't at least "
                    + (dstartindex + length) + " bytes long.");

        byte low;
        for (int i = 0; i < length; i++) {
            if (i == length - 1 && clipSource)
                low = (byte) 0x00;
            else {
                try {
                    low = src[i + sstartindex + 1];
                } catch (Exception e) {
                    low = 0x00;
                }
            }

            short transfer = (short) (0xFF00 | src[i + sstartindex]);
            transfer = (short) (transfer << 8);
            transfer = (short) (transfer | (0x00FF & low));

            if (sstartoffset > dstartoffset) {
                transfer = (short) (transfer << (sstartoffset - dstartoffset));
            } else if (dstartoffset > sstartoffset) {
                transfer = (short) (transfer >> (dstartoffset - sstartoffset));
            }

            if (dstartoffset == 0) {
                // slight optimization
                dest[i + dstartindex] = (byte) (transfer >> 8);
            } else if (i == length - 1) {
                // final pass through...don't worry about the "next" byte
                final int mso = 8 - dstartoffset;
                final int l = Math.max(mso - (8 - sstartoffset), 0);
                final byte mask = (byte) (Math.pow(2, 8 - dstartoffset) - Math.pow(2, l));
                // apply the mask to the src and dest
                // mask has zeros where we should use the dest, and ones where we should "inject"
                // the src
                // do the (boolean) math to convince yourself
                dest[i + dstartindex] = (byte) ((dest[i + dstartindex] & ~mask) | (transfer >> 8 & mask));
            } else {
                final byte highmask = (byte) (Math.pow(2, 8 - dstartoffset) - 1);
                final byte lowmask = (byte) (Math.pow(2, 8 - dstartoffset) * -1);
                dest[i + dstartindex] = (byte) ((dest[i + dstartindex] & ~highmask) | (transfer >> 8 & highmask));
                dest[i + 1 + dstartindex] = (byte) ((dest[i + dstartindex + 1] & ~lowmask) | (transfer & lowmask));
            }
        }
        return dest;
    }
}
