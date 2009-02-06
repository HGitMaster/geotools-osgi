package org.geotools.arcsde.gce;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.gce.imageio.RasterCellType;
import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeRasterTile;
import com.esri.sde.sdk.client.SeRow;

@SuppressWarnings("nls")
public abstract class TileReader {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    private final int tileWidth;

    private final int tileHeight;

    private final int bitsPerSample;

    private final TileIterator tileIterator;

    private SeRasterTile lastFetchedTile;

    protected int tileDataLength;

    private byte[] currTileData;

    protected int currTileDataIndex;

    private static class TileIterator {
        private final SeRow row;

        private SeConnection conn;

        private SeRasterTile nextTile;

        private boolean started;

        public TileIterator(SeConnection conn, SeRow row) {
            this.conn = conn;
            this.row = row;
            started = false;
        }

        public boolean hasNext() throws IOException {
            if (!started) {
                try {
                    nextTile = row.getRasterTile();
                    started = true;
                    if (nextTile == null) {
                        LOGGER.fine("No tiles to fetch at all, releasing connection");
                        close();
                    }
                } catch (SeException e) {
                    throw new ArcSdeException(e);
                }
            }
            return nextTile != null;
        }

        public SeRasterTile next() throws IOException {
            if (nextTile == null) {
                throw new EOFException("No more tiles to read");
            }
            SeRasterTile curr = nextTile;
            try {
                nextTile = row.getRasterTile();
                if (nextTile == null) {
                    LOGGER.finer("Releasing the connection since there're no mor tiles to fetch");
                    close();
                }
            } catch (SeException e) {
                throw new ArcSdeException(e);
            }
            return curr;
        }

        public void close() throws IOException {
            if (conn != null) {
                try {
                    conn.close();
                    conn = null;
                } catch (SeException e) {
                    throw new ArcSdeException(e);
                }
            }
        }
    }

    public static TileReader getInstance(final SeConnection conn, final RasterCellType cellType,
            final SeRow row, int tileW, int tileH) {
        final int bitsPerSample = cellType.getBitsPerSample();
        switch (cellType) {
        case TYPE_1BIT:
            return new OneBitTileReader(conn, row, tileW, tileH, bitsPerSample);
        case TYPE_4BIT:
            // return new FourBitTileReader(conn, row);
        default:
            return new DefaultTileReader(conn, row, tileW, tileH, bitsPerSample);
        }
    }

    public abstract int read(byte[] buff, int off, int len) throws IOException;

    public abstract int read() throws IOException;

    private TileReader(final SeConnection conn, final SeRow row, int tileW, int tileH,
            int bitsPerSample) {
        this.tileIterator = new TileIterator(conn, row);
        this.tileWidth = tileW;
        this.tileHeight = tileH;
        this.bitsPerSample = bitsPerSample;
    }

    public final void close() throws IOException {
        LOGGER.finer("Close explicitly called, releasing connection if still in use");
        tileIterator.close();
    }

    protected final void markRead(int actualByteReadCount) throws IOException {
        currTileDataIndex += actualByteReadCount;
    }

    private byte[] fetchTile() throws IOException {
        if (tileIterator.hasNext()) {
            lastFetchedTile = tileIterator.next();
        }

        tileDataLength = (tileWidth * tileHeight * bitsPerSample) / 8;
        final byte[] tileData = new byte[tileDataLength];
        final byte NO_DATA_BYTE = (byte) 0x00;

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(" >> Fetching " + lastFetchedTile + " - bitmask: "
                    + lastFetchedTile.getBitMaskData().length);
        }
        final int numPixels = lastFetchedTile.getNumPixels();
        if (numPixels == 0) {
            LOGGER.finer("tile contains no pixel data: " + lastFetchedTile);
            Arrays.fill(tileData, NO_DATA_BYTE);
            return tileData;
        }

        // get pixel data AND bitmask data appended, if exist. Pixel data packed into
        // byte[] according to pixel type. This is the only generic way of getting at
        // the pixel data regardless of its data type
        final byte[] rawTileData = lastFetchedTile.getPixelData();
        final byte[] bitmaskData = lastFetchedTile.getBitMaskData();
        final int bitMaskDataLength = bitmaskData.length;
        final int dataAndBitMaskLength = rawTileData.length;
        final int pureDataLength = dataAndBitMaskLength - bitMaskDataLength;
        if (pureDataLength != tileDataLength) {
            throw new IllegalStateException("expected data length of " + tileDataLength + ", got "
                    + pureDataLength);
        }
        System.arraycopy(rawTileData, 0, tileData, 0, tileDataLength);

        // now set the nodata pixels to no-data
        if (bitMaskDataLength > 0) {
            LOGGER.finer("Applying no-data bitmask to tile pixels, bits per sample: "
                    + bitsPerSample);
            final int bytesPerSample = bitsPerSample / 8;
            for (int pixelN = 0; pixelN < numPixels; pixelN++) {
                if (((bitmaskData[pixelN / 8] >> (7 - (pixelN % 8))) & 0x01) == 0x00) {
                    // it's a no-data pixel. For now works for bitsPerSample >= 8
                    if (bitsPerSample >= 8) {
                        for (int pixByteN = 0; pixByteN < bytesPerSample; pixByteN++) {
                            tileData[(pixelN * bytesPerSample) + pixByteN] = NO_DATA_BYTE;
                        }
                    } else if (bitsPerSample == 1) {
                        // TODO
                    } else if (bitsPerSample == 4) {
                        // TODO
                    } else {
                        throw new IllegalStateException("bitsPerSample " + bitsPerSample);
                    }
                }
            }
        }

        return tileData;
    }

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

        public DefaultTileReader(final SeConnection conn, final SeRow row, int tileW, int tileH,
                int bitsPerSample) {
            super(conn, row, tileW, tileH, bitsPerSample);
        }

        @Override
        public int read() throws IOException {
            byte[] data = getTileData();
            if (data == null) {
                return -1;
            }
            byte currByte = data[currTileDataIndex];
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
        private byte currByte;

        /**
         * how many bits of the current byte have been alread read
         */
        private int currByteReadCount;

        public OneBitTileReader(final SeConnection conn, final SeRow row, int tileW, int tileH,
                int bitsPerSample) {
            super(conn, row, tileW, tileH, bitsPerSample);
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
                currByte = data[currTileDataIndex];
                currByteReadCount = 0;
                markRead(1);
            }
            int bit = currByte & 0xFFFFFF;
            currByteReadCount++;
            return bit;
        }
    }
}
