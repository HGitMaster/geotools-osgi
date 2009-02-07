package org.geotools.arcsde.gce;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

    private final int bitsPerSample;

    private final TileIterator tileIterator;

    private final Rectangle imageDimension;

    private final Rectangle requestedTiles;

    private final Dimension tileSize;

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
            final SeRow row, final Rectangle imageSize, final Rectangle requestedTiles,
            final Dimension tileSize) {
        final int bitsPerSample = cellType.getBitsPerSample();
        switch (cellType) {
        case TYPE_1BIT:
            return new OneBitTileReader(conn, row, imageSize, bitsPerSample, requestedTiles,
                    tileSize);
        case TYPE_4BIT:
            // return new FourBitTileReader(conn, row);
        default:
            return new DefaultTileReader(conn, row, imageSize, bitsPerSample, requestedTiles,
                    tileSize);
        }
    }

    public abstract int read(byte[] buff, int off, int len) throws IOException;

    public abstract int read() throws IOException;

    /**
     * 
     * @param conn
     * @param row
     * @param imageDimensions
     *            the image size, x and y are the offsets, width and height the actual width and
     *            height, used to ignore incomming pixel data as appropriate to fit the image
     *            dimensions
     * @param bitsPerSample
     * @param requestedTiles
     */
    private TileReader(final SeConnection conn, final SeRow row, Rectangle imageDimensions,
            int bitsPerSample, final Rectangle requestedTiles, Dimension tileSize) {
        this.tileIterator = new TileIterator(conn, row);
        this.imageDimension = imageDimensions;
        this.bitsPerSample = bitsPerSample;
        this.requestedTiles = requestedTiles;
        this.tileSize = tileSize;
    }

    public final void close() throws IOException {
        LOGGER.finer("Close explicitly called, releasing connection if still in use");
        tileIterator.close();
    }

    protected final void markRead(int actualByteReadCount) throws IOException {
        currTileDataIndex += actualByteReadCount;
    }

    private static class BoundsCheck {
        private final int tileWidth;

        private final int tileHeight;

        private final int minImageX;

        private final int maxImageX;

        private final int minImageY;

        private final int maxImageY;

        public BoundsCheck(Rectangle imageSize, Dimension tileSize) {
            tileWidth = tileSize.width;
            tileHeight = tileSize.height;
            minImageX = imageSize.x;
            maxImageX = minImageX + imageSize.width;
            minImageY = imageSize.y;
            maxImageY = minImageY + imageSize.height;
        }

        public boolean imageContains(final int tileX, final int tileY, final int tileCol,
                final int tileRow) {

            int x = tileCol * tileWidth + tileX;
            int y = tileRow * tileHeight + tileY;

            if (x < minImageX) {
                return false;
            }
            if (x > maxImageX) {
                return false;
            }
            if (y < minImageY) {
                return false;
            }
            if (y > maxImageY) {
                return false;
            }
            return true;
        }
    }

    private BoundsCheck boundsCheck;

    private byte[] fetchTile() throws IOException {
        if (boundsCheck == null) {
            boundsCheck = new BoundsCheck(imageDimension, tileSize);
        }

        if (tileIterator.hasNext()) {
            lastFetchedTile = tileIterator.next();
        } else {
            return null;
        }
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.info(" >> Fetching " + lastFetchedTile + " - bitmask: "
                    + lastFetchedTile.getBitMaskData().length + " has more: " + tileIterator.hasNext());
        }

        final int numPixels = lastFetchedTile.getNumPixels();

        if (numPixels == 0) {
            LOGGER.finer("tile contains no pixel data, skipping: " + lastFetchedTile);
            return fetchTile();
        }

        // get pixel data AND bitmask data appended, if exist. Pixel data packed into
        // byte[] according to pixel type. This is the only generic way of getting at
        // the pixel data regardless of its data type
        final byte[] rawTileData = lastFetchedTile.getPixelData();
        final byte[] bitmaskData = lastFetchedTile.getBitMaskData();
        final int bitMaskDataLength = bitmaskData.length;
        final byte[] tileData;

        final Point shift = getTilePixelShift(lastFetchedTile);

        if (shift.x == 0 && shift.y == 0 && bitMaskDataLength == 0) {
            // it's a full tile, go the easy way
            tileDataLength = rawTileData.length;
            tileData = new byte[tileDataLength];
            System.arraycopy(rawTileData, 0, tileData, 0, tileDataLength);
        } else {
            // there are no data pixels (ie, ones that merely fill the tile dimension, nothing to do
            // with the no-data concept). Copy just the non blank ones.
            final int tileWidth = tileSize.width;
            final int tileHeight = tileSize.height;

            if (numPixels != tileWidth * tileHeight) {
                throw new IllegalStateException("numPixels != tileWidth * tileHeight");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream(rawTileData.length
                    - bitMaskDataLength);
            final int bytesPerSample = bitsPerSample / 8;

            final int columnIndex = lastFetchedTile.getColumnIndex();
            final int rowIndex = lastFetchedTile.getRowIndex();

            int pixArrayOffset;
            boolean isNoData;
            boolean include;
            int numSkippedPixels = 0;
            for (int x = 0; x < tileWidth; x++) {
                for (int y = 0; y < tileHeight; y++) {
                    pixArrayOffset = y * tileWidth + x;
                    isNoData = isNoData(bitmaskData, pixArrayOffset);
                    if (isNoData) {
                        numSkippedPixels++;
                        continue;
                    }
                    include = boundsCheck.imageContains(x, y, columnIndex, rowIndex);
                    // discard = isNoData
                    // || false
                    // && ((shift.x > 0 && x > shift.x)
                    // || (shift.x < 0 && x > (tileWidth + shift.x))
                    // || (shift.y > 0 && y > shift.y) || (shift.y < 0 && y > (tileHeight +
                    // shift.y)));
                    if (include) {
                        for (int byteN = 0; byteN < bytesPerSample; byteN++) {
                            out.write(rawTileData[pixArrayOffset + byteN]);
                        }
                    } else {
                        numSkippedPixels++;
                    }
                }
            }

            // for (int pixelN = 0; pixelN < numPixels; pixelN++) {
            // pixArrayOffset = pixelN * bytesPerSample;
            // isNoData = isNoData(bitmaskData, pixelN);
            // discard = isNoData ;
            // if (isNoData) {
            // // it's a no-data pixel, ignore it For now works for bitsPerSample >= 8
            // numSkippedPixels++;
            // } else {
            // for (int byteN = 0; byteN < bytesPerSample; byteN++) {
            // out.write(rawTileData[pixArrayOffset + byteN]);
            // }
            // }
            // }

            tileData = out.toByteArray();
            tileDataLength = tileData.length;
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("Tile " + lastFetchedTile.getColumnIndex() + ","
                        + lastFetchedTile.getRowIndex() + " contained " + numSkippedPixels
                        + " which where ignored. Only " + (numPixels - numSkippedPixels)
                        + " fetched.");
            }
        }

        return tileData;
    }

    private boolean isNoData(final byte[] bitmaskData, int pixelN) {
        boolean isNoData;
        isNoData = bitmaskData.length > 0
                && ((bitmaskData[pixelN / 8] >> (7 - (pixelN % 8))) & 0x01) == 0x00;
        return isNoData;
    }

    /**
     * Based on the actual image dimensions and the requested tiles, determines whether this tile
     * contains pixels that should be discarded
     * 
     * @param tile
     * @return the offset x and y of the pixels that shall be discarded
     */
    private Point getTilePixelShift(final SeRasterTile tile) {

        final int columnIndex = tile.getColumnIndex();
        final int rowIndex = tile.getRowIndex();

        /*
         * The pixel range covered by the tile for the pyramid level
         */
        final Rectangle tilePixels = new Rectangle(columnIndex * tileSize.width, rowIndex
                * tileSize.height, tileSize.width, tileSize.height);

        int xshift = 0;
        int yshift = 0;

        final int tileMinX = tilePixels.x;
        final int tileMinY = tilePixels.y;
        final int tileMaxX = tileMinX + tilePixels.width;
        final int tileMaxY = tileMinY + tilePixels.height;

        final int imageMinX = imageDimension.x;
        final int imageMinY = imageDimension.y;
        final int imageMaxX = imageMinX + imageDimension.width;
        final int imageMaxY = imageMinY + imageDimension.height;

        if (tileMinX < imageMinX) {
            xshift = imageMinX - tileMinX;
        } else if (tileMaxX > imageMaxX) {
            xshift = -1 * (tileMaxX - imageMaxX);
        }

        if (tileMinY < imageMinY) {
            yshift = tileMinY - imageMinY;
        } else if (tileMaxY > imageMaxY) {
            yshift = -1 * ((tileMaxY) - imageMaxY);
        }

        return new Point(xshift, yshift);
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

        public DefaultTileReader(final SeConnection conn, final SeRow row,
                final Rectangle imageSize, int bitsPerSample, Rectangle requestedTiles,
                Dimension tileSize) {
            super(conn, row, imageSize, bitsPerSample, requestedTiles, tileSize);
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

        public OneBitTileReader(final SeConnection conn, final SeRow row,
                final Rectangle imageSize, int bitsPerSample, final Rectangle requestedTiles,
                final Dimension tileSize) {
            super(conn, row, imageSize, bitsPerSample, requestedTiles, tileSize);
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
