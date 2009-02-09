/**
 * 
 */
package org.geotools.arcsde.gce;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

public class ArcSDETiledImageInputStream extends ImageInputStreamImpl implements ImageInputStream {

    private final TileReader tileReader;

    private byte[] currTileData;

    private int currTileDataIndex;

    public ArcSDETiledImageInputStream(TileReader tileReader) throws IOException {
        super();
        this.tileReader = tileReader;
        this.currTileData = new byte[0];
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
                byte[] tileData = tileReader.next();
                // if (tileReader.getBitsPerSample() == 1) {
                // currTileData = expandOneBitData(tileData);
                // } else {
                currTileData = tileData;
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