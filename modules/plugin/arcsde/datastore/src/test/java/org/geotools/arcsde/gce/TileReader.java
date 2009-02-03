package org.geotools.arcsde.gce;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.gce.imageio.RasterCellType;
import org.geotools.data.DataSourceException;
import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeRasterTile;
import com.esri.sde.sdk.client.SeRow;

@SuppressWarnings("nls")
public abstract class TileReader {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    protected final SeConnection conn;

    protected final SeRow row;

    private SeRasterTile lastFetchedTile;

    protected int tileDataLength;

    private byte[] currTileData;

    protected int currTileDataIndex;

    private final int numberOfBands;

    private int tileWidth;

    private int tileHeight;

    private int bitsPerSample;

    public static TileReader getInstance(final SeConnection conn, final RasterCellType cellType,
            final SeRow row, final int numberOfBands, int tileW, int tileH) {
        final int bitsPerSample = cellType.getBitsPerSample();
        switch (cellType) {
        case TYPE_1BIT:
            return new OneBitTileReader(conn, row, numberOfBands, tileW, tileH, bitsPerSample);
        case TYPE_4BIT:
            // return new FourBitTileReader(conn, row);
        default:
            return new DefaultTileReader(conn, row, numberOfBands, tileW, tileH, bitsPerSample);
        }
    }

    public abstract int read(byte[] buff, int off, int len) throws IOException;

    public abstract int read() throws IOException;

    private TileReader(final SeConnection conn, final SeRow row, final int numberOfBands,
            int tileW, int tileH, int bitsPerSample) {
        this.conn = conn;
        this.row = row;
        this.numberOfBands = numberOfBands;
        this.tileWidth = tileW;
        this.tileHeight = tileH;
        this.bitsPerSample = bitsPerSample;
    }

    public final void close() throws IOException {
        try {
            row.reset();
            conn.close();
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    protected final void markRead(int actualByteReadCount) throws IOException {
        currTileDataIndex += actualByteReadCount;
    }

    private byte[] fetchTile() throws IOException {
        try {
            lastFetchedTile = row.getRasterTile();
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }

        if (lastFetchedTile == null) {
            // no more tiles to fetch (aka, EOF)
            return null;
        }
        
        final int tileDataLength = (tileWidth * tileHeight * bitsPerSample) / 8;
        final byte[] tileData = new byte[tileDataLength];
        Arrays.fill(tileData, (byte) 0x00);
        System.out.println("Fetching " + lastFetchedTile + " - bitmask: "
                + lastFetchedTile.getBitMaskData().length);
        final int numPixels = lastFetchedTile.getNumPixels();
        if (numPixels == 0) {
            LOGGER.fine("ignoring tile since it contains no pixel data: " + lastFetchedTile);
            return tileData;
        }

        // get pixel data AND bitmask data appended, if exist. Pixel data packed into
        // byte[] according to pixel type. This is the only generic way of getting at
        // the pixel data regardless of its data type
        byte[] rawTileData = lastFetchedTile.getPixelData();
        final byte[] bitMaskData = lastFetchedTile.getBitMaskData();
        final int bitMaskDataLength = bitMaskData.length;
        final int dataAndBitMaskLength = rawTileData.length;
        final int pureDataLength = dataAndBitMaskLength - bitMaskDataLength;
        if (pureDataLength != tileDataLength) {
            throw new IllegalStateException("expected data length of " + tileDataLength + ", got "
                    + pureDataLength);
        }
        System.arraycopy(rawTileData, 0, tileData, 0, tileDataLength);

        this.tileDataLength = tileDataLength;
        return tileData;
    }

    // private byte[] fetchTile() throws IOException {
    // byte[] pixelInterleavedData = null;
    // SeRasterTile[] bandsTiles = new SeRasterTile[numberOfBands];
    // try {
    // for (int band = 0; band < numberOfBands; band++) {
    // bandsTiles[band] = row.getRasterTile();
    // }
    // lastFetchedTile = bandsTiles[numberOfBands - 1];
    //
    // } catch (SeException e) {
    // throw new ArcSdeException(e);
    // }
    // if (lastFetchedTile != null) {
    // // wasn't it the last tile in the stream?
    // // get pixel data AND bitmask data appended, if exist. Pixel data packed into
    // // byte[] according to pixel type. This is the only generic way of getting at
    // // the pixel data regardless of its data type
    // final RasterCellType cellType = RasterCellType.valueOf(lastFetchedTile.getPixelType());
    // final int bitsPerSample = cellType.getBitsPerSample();
    // final int bandSize = (bitsPerSample * tileWidth * tileHeight) / 8;
    // tileDataLength = numberOfBands * bandSize;
    // pixelInterleavedData = new byte[tileDataLength];
    //
    // for (int band = 0; band < numberOfBands; band++) {
    // SeRasterTile currTile = bandsTiles[band];
    // System.err.println("Reading from tile " + currTile + ". Bitmask: "
    // + currTile.getBitMaskData().length);
    // byte[] thisTileData = currTile.getPixelData();
    // final int bitMaskDataLength = currTile.getBitMaskData().length;
    // final int dataAndBitMaskLength = thisTileData.length;
    // // cut off the bitmask data from the pixel data length
    // final int thisTileDataLength = dataAndBitMaskLength - bitMaskDataLength;
    //
    // if (bandSize != thisTileDataLength) {
    // throw new IllegalStateException("tile data expected to be " + bandSize
    // + " but is " + thisTileDataLength + ": " + lastFetchedTile);
    // }
    // final int bytesPerSample = bitsPerSample / 8;
    // final int shift = bytesPerSample * numberOfBands;
    //
    // for (int tilePixel = 0, interleaved = 0; tilePixel < bandSize; tilePixel++, interleaved +=
    // shift) {
    // for (int i = 0; i < bytesPerSample; i++) {
    // pixelInterleavedData[interleaved + i] = thisTileData[tilePixel];
    // }
    // }
    // }
    // }
    // return pixelInterleavedData;
    // }

    protected final byte[] getTileData() throws IOException {
        if (currTileData == null) {
            currTileData = fetchTile();
            if (currTileData == null) {
                return null;
            }
            markRead(0);
        }
        if (currTileDataIndex == tileDataLength) {
            currTileData = fetchTile();
            if (currTileData == null) {
                return null;
            }
            currTileDataIndex = 0;
        }
        return currTileData;
    }

    /**
     * 
     * @author groldan
     * 
     */
    private static class DefaultTileReader extends TileReader {

        public DefaultTileReader(final SeConnection conn, final SeRow row, final int numberOfBands,
                int tileW, int tileH, int bitsPerSample) {
            super(conn, row, numberOfBands, tileW, tileH, bitsPerSample);
        }

        @Override
        public int read() throws IOException {
            byte[] data = getTileData();
            if (data == null) {
                return -1;
            }
            int currByte = data[currTileDataIndex] & 0xFF;
            markRead(1);
            return currByte;
        }

        @Override
        public int read(byte[] buff, int off, int len) throws IOException {
            int readCount = 0;
            byte[] data = getTileData();
            if (data == null) {
                return -1;
            }
            int remaining = len;
            while (remaining > 0 && data != null) {
                int available = tileDataLength - currTileDataIndex;
                if (available >= remaining) {
                    System.arraycopy(data, currTileDataIndex, buff, off + readCount, remaining);
                    readCount += remaining;
                    markRead(remaining);
                    remaining = 0;
                } else {
                    System.arraycopy(data, currTileDataIndex, buff, off + readCount, available);
                    remaining -= available;
                    readCount += available;
                    markRead(available);
                    data = getTileData();
                }
            }
            return readCount;
        }

    }

    private static class OneBitTileReader extends TileReader {

        /**
         * Current byte actually read holding up to 8 samples
         */
        private int currByte;

        /**
         * how many bits of the current byte have been alread read
         */
        private int currByteReadCount;

        public OneBitTileReader(final SeConnection conn, final SeRow row, final int numberOfBands,
                int tileW, int tileH, int bitsPerSample) {
            super(conn, row, numberOfBands, tileW, tileH, bitsPerSample);
            currByteReadCount = 8;// need to fetch more
        }

        @Override
        public int read(byte[] buff, int off, int len) throws IOException {
            int readCount = 0;
            int b;
            for (int i = 0; i < len; i++) {
                b = read();
                if (b == -1) {
                    break;
                }
                try {
                    buff[off + readCount] = (byte) (b & 0xFF);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    throw e;
                }
                readCount++;
            }
            return readCount;
        }

        private static int[] bitmasks = {

        };

        @Override
        public int read() throws IOException {
            if (currByteReadCount == 8) {
                byte[] data = getTileData();
                if (data == null) {
                    return -1;
                }
                currByte = data[currTileDataIndex] & 0xFF;
                currByteReadCount = 0;
                markRead(1);
            }
            int bit = currByte & 0xFFFFFF;
            currByteReadCount++;
            return bit;
        }
    }
}
