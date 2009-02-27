/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.arcsde.gce;

import java.io.IOException;
import java.util.Arrays;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

/**
 * An {@link ImageInputStream} that reads ArcSDE raster tiles in a band interleaved order.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.5.4
 * @version $Id: ArcSDETiledImageInputStream.java 32555 2009-02-27 19:52:41Z groldan $
 * @source $URL$
 */
final class ArcSDETiledImageInputStream extends ImageInputStreamImpl implements ImageInputStream {

    private final TileReader tileReader;

    private final int tileDataLength;

    private final byte[] currTileData;

    private byte[] actualTileData;

    private int currTileDataIndex;

    private final byte[] currBitmaskData;

    private final boolean promote;

    public ArcSDETiledImageInputStream(final TileReader tileReader,
            final boolean promoteByteToUshort) {
        super();
        this.tileReader = tileReader;
        this.promote = promoteByteToUshort;
        final int bytesPerTile = tileReader.getBytesPerTile();
        final int bitmaskDataLength = (int) Math.ceil(bytesPerTile / 8D);
        this.tileDataLength = (promoteByteToUshort ? 2 : 1) * bytesPerTile;
        this.currTileData = new byte[bytesPerTile];
        this.actualTileData = new byte[tileDataLength];
        this.currBitmaskData = new byte[bitmaskDataLength];
        // force load at the first read invocation
        this.currTileDataIndex = tileDataLength;
    }

    /**
     * Returns the computed lenght of the stream based on the tile dimensions, number of tiles,
     * number of bands, and bits per sample
     */
    @Override
    public long length() {
        final int bytesPerTile = tileReader.getBytesPerTile();
        final int tilesWide = tileReader.getTilesWide();
        final int tilesHigh = tileReader.getTilesHigh();
        final int numberOfBands = tileReader.getNumberOfBands();
        // final int bitsPerSample = tileReader.getBitsPerSample();

        int length = bytesPerTile * tilesWide * tilesHigh * numberOfBands;
        if (promote) {
            length = 2 * length;
        }
        return length;
    }

    @Override
    public int read() throws IOException {
        final byte[][] data = getTileData();
        if (data == null) {
            return -1;
        }
        byte b = data[0][currTileDataIndex];
        currTileDataIndex++;
        return b;
    }

    @Override
    public int read(byte[] buff, int off, int len) throws IOException {
        final byte[][] data = getTileData();
        if (data == null) {
            return -1;
        }
        final int available = data[0].length - currTileDataIndex;
        final int count = Math.min(available, len);
        System.arraycopy(data[0], currTileDataIndex, buff, off, count);
        currTileDataIndex += count;
        return count;
    }

    /**
     * Fetches a tile from the {@code tileReader} if necessary and returns the current tile data.
     * <p>
     * It is needed to fetch a new tile if {@link #currTileDataIndex} indicates all the current tile
     * data has been already read. If so, {@code currTileDataIndex} is reset to 0. The {@code read}
     * operations are responsible of incrementing {@code currTileDataIndex} depending on how many
     * bytes have been consumed from the tile data returned by this method.
     * </p>
     * 
     * @return {@code null} if there's no more tiles to fetch, the current tile data otherwise
     * @throws IOException
     */
    private byte[][] getTileData() throws IOException {
        if (currTileDataIndex == tileDataLength) {
            if (!tileReader.hasNext()) {
                return null;
            }

            currTileDataIndex = 0;
            if (promote) {
                tileReader.next(currTileData, currBitmaskData);
                final int numSamples = currTileData.length;
                Arrays.fill(actualTileData, (byte) 0x0);
                int pixArrayOffset;
                boolean isNoData;

                for (int sampleN = 0; sampleN < numSamples; sampleN++) {
                    isNoData = (((currBitmaskData[sampleN / 8] >> (7 - (sampleN % 8))) & 0x01) == 0x00);
                    pixArrayOffset = 2 * sampleN;
                    if (isNoData) {
                        /*
                         * The promoted index color model has the last entry set as transparent, so
                         * set the sample value to match the entry
                         */
                        actualTileData[pixArrayOffset] = (byte) ((65535 >>> 8) & 0xFF);
                        actualTileData[pixArrayOffset + 1] = (byte) ((65535 >>> 0) & 0xFF);
                    } else {
                        actualTileData[pixArrayOffset + 1] = (byte) ((currTileData[sampleN] >>> 0) & 0xFF);
                    }
                }
            } else {
                tileReader.next(actualTileData, currBitmaskData);
            }
        }
        return new byte[][] { actualTileData, currBitmaskData };
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}