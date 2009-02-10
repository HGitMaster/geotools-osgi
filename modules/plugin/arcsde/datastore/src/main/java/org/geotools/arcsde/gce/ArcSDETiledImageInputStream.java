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

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

/**
 * An {@link ImageInputStream} that reads ArcSDE raster tiles in a band interleaved order.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.5.4
 * @version $Id: ArcSDETiledImageInputStream.java 32460 2009-02-10 05:23:31Z groldan $
 * @source $URL$
 */
class ArcSDETiledImageInputStream extends ImageInputStreamImpl implements ImageInputStream {

    private final TileReader tileReader;

    private byte[] currTileData;

    private int currTileDataIndex;

    public ArcSDETiledImageInputStream(final TileReader tileReader) throws IOException {
        super();
        this.tileReader = tileReader;
        final int bytesPerTile = tileReader.getBytesPerTile();
        this.currTileData = new byte[bytesPerTile];
        // force load at the first read invocation
        this.currTileDataIndex = bytesPerTile;
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
        final int bitsPerSample = tileReader.getBitsPerSample();

        int length = bytesPerTile * tilesWide * tilesHigh * numberOfBands;
        // if (1 == bitsPerSample) {
        // length *= 8;
        // }

        return length;
    }

    @Override
    public int read() throws IOException {
        final byte[] data = getTileData();
        if (data == null) {
            return -1;
        }
        byte b = data[currTileDataIndex];
        currTileDataIndex++;
        return b;
    }

    @Override
    public int read(byte[] buff, int off, int len) throws IOException {
        final byte[] data = getTileData();
        if (data == null) {
            return -1;
        }
        final int available = data.length - currTileDataIndex;
        final int count = Math.min(available, len);
        System.arraycopy(data, currTileDataIndex, buff, off, count);
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
    private byte[] getTileData() throws IOException {
        if (currTileDataIndex == currTileData.length) {
            if (tileReader.hasNext()) {
                currTileData = tileReader.next(currTileData);
                // if (tileReader.getBitsPerSample() == 1) {
                // currTileData = expandOneBitData(tileData);
                // } else {
                // }
                currTileDataIndex = 0;
            } else {
                return null;
            }
        }
        return currTileData;
    }

    // private byte[] expandOneBitData(final byte[] tileData) {
    // byte[] byteData = new byte[8 * tileData.length];
    // for (int i = 0; i < tileData.length; i++) {
    // byte packed = tileData[i];
    // int base = 8 * i;
    // byteData[base] = (byte) ((packed >>> 1) & 0x1);
    // byteData[base + 1] = (byte) ((packed >>> 2) & 0x1);
    // byteData[base + 2] = (byte) ((packed >>> 3) & 0x1);
    // byteData[base + 3] = (byte) ((packed >>> 4) & 0x1);
    // byteData[base + 4] = (byte) ((packed >>> 5) & 0x1);
    // byteData[base + 5] = (byte) ((packed >>> 6) & 0x1);
    // byteData[base + 6] = (byte) ((packed >>> 7) & 0x1);
    // byteData[base + 7] = (byte) ((packed >>> 8) & 0x1);
    // }
    // return byteData;
    // }
    //
    
    @Override
    public void close() throws IOException {
        super.close();
    }
}