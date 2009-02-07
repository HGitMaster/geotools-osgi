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
        if (tileReader.hasNext()) {
            this.currTileData = tileReader.next();
            this.currTileDataIndex = 0;
        } else {
            this.currTileData = new byte[] { 0 };
            this.currTileDataIndex = 0;
        }
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

        final int length = bytesPerTile * tilesWide * tilesHigh * numberOfBands;

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
     * Fetchs a tile from the {@code tileReader} if necessary and returns the current tile data.
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
                currTileData = tileReader.next();
                currTileDataIndex = 0;
            } else {
                return null;
            }
        }
        return currTileData;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}