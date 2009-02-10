package org.geotools.arcsde.gce;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeRasterTile;
import com.esri.sde.sdk.client.SeRow;

/**
 * Offers an iterator like interface to fetch ArcSDE raster tiles.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.5.4
 * @version $Id: TileReader.java 32459 2009-02-10 05:18:30Z groldan $
 * @source $URL$
 */
@SuppressWarnings( { "nls" })
class TileReader {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    private final int bitsPerSample;

    private final Rectangle requestedTiles;

    private final Dimension tileSize;

    private final int tileDataLength;

    private final SeRow row;

    private final int pixelsPerTile;

    private final int numberOfBands;

    private SeRasterTile nextTile;

    private boolean started;

    public static TileReader getInstance(final SeRow row, final int bitsPerSample,
            final int numberOfBands, final Rectangle requestedTiles, final Dimension tileSize) {
        return new TileReader(row, bitsPerSample, numberOfBands, requestedTiles, tileSize);
    }

    /**
     * 
     * @param row
     * @param imageDimensions
     *            the image size, x and y are the offsets, width and height the actual width and
     *            height, used to ignore incomming pixel data as appropriate to fit the image
     *            dimensions
     * @param bitsPerSample
     * @param numberOfBands2
     * @param requestedTiles
     */
    private TileReader(final SeRow row, int bitsPerSample, int numberOfBands,
            final Rectangle requestedTiles, Dimension tileSize) {
        this.row = row;
        this.bitsPerSample = bitsPerSample;
        this.numberOfBands = numberOfBands;
        this.requestedTiles = requestedTiles;
        this.tileSize = tileSize;
        this.pixelsPerTile = tileSize.width * tileSize.height;
        this.tileDataLength = (int) Math
                .ceil(((double) pixelsPerTile * (double) bitsPerSample) / 8D);
    }

    /**
     * @return number of bits per sample
     */
    public int getBitsPerSample() {
        return bitsPerSample;
    }

    /**
     * @return numbre of bands being fetched
     */
    public int getNumberOfBands() {
        return numberOfBands;
    }

    /**
     * @return number of pixels per tile over the X axis
     */
    public int getTileWidth() {
        return tileSize.width;
    }

    /**
     * @return number of pixels per tile over the Y axis
     */
    public int getTileHeight() {
        return tileSize.height;
    }

    /**
     * @return number of tiles being fetched over the X axis
     */
    public int getTilesWide() {
        return 1 + requestedTiles.width;
    }

    /**
     * @return number of tiles being fetched over the Y axis
     */
    public int getTilesHigh() {
        return 1 + requestedTiles.height;
    }

    /**
     * @return number of bytes in the raw pixel content of a tile, not taking into account any
     *         trailing bitmask data.
     */
    public int getBytesPerTile() {
        return tileDataLength;
    }

    /**
     * @return whether there are more tiles to fetch
     * @throws IOException
     */
    public boolean hasNext() throws IOException {
        if (!started) {
            try {
                nextTile = row.getRasterTile();
                started = true;
                if (nextTile == null) {
                    LOGGER.fine("No tiles to fetch at all, releasing connection");
                }
            } catch (SeException e) {
                throw new ArcSdeException(e);
            }
        }
        return nextTile != null;
    }

    /**
     * Fetches a tile and returns its raw pixel data packaged as bytes according to the number of
     * bits per sample
     * 
     * @param tileData
     *            a possibly {@code null} array where to store the next tile data. If {@code null} a
     *            new byte[] of length {@link #getBytesPerTile()} will be allocated and filled up
     *            with the raw tile pixel data.
     * @return contents of the next tile, or {@code null} if there are no more tiles to fetch
     * @throws IOException
     * @throws {@link IllegalArgumentException} if tileData is not null and its size is less than
     *         {@link #getBytesPerTile()}
     */
    public byte[] next(byte[] tileData) throws IOException {
        if (tileData == null) {
            tileData = new byte[getBytesPerTile()];
        } else if (tileData.length < getBytesPerTile()) {
            throw new IllegalArgumentException("tileData shall have at least " + getBytesPerTile()
                    + " elements: " + tileData.length);
        }
        final SeRasterTile tile;

        if (hasNext()) {
            tile = nextTile();
        } else {
            return null;
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(" >> Fetching " + tile + " - bitmask: " + tile.getBitMaskData().length
                    + " has more: " + hasNext());
        }

        final byte NO_DATA_BYTE = (byte) 0xFF;

        final int numPixels = tile.getNumPixels();
        if (0 == numPixels) {
            Arrays.fill(tileData, NO_DATA_BYTE);
            LOGGER.finer("tile contains no pixel data, skipping: " + tile);
        } else if (pixelsPerTile == numPixels) {
            // get pixel data AND bitmask data appended, if exist. Pixel data packed into
            // byte[] according to pixel type. This is the only generic way of getting at
            // the pixel data regardless of its data type
            final byte[] rawTileData = tile.getPixelData();
            final byte[] bitmaskData = tile.getBitMaskData();
            final int bitMaskDataLength = bitmaskData.length;
            final int tileDataLength = rawTileData.length - bitMaskDataLength;

            if (this.tileDataLength != tileDataLength) {
                throw new IllegalStateException("Expected data lengh of " + this.tileDataLength
                        + " but got " + tileDataLength);
            }
            System.arraycopy(rawTileData, 0, tileData, 0, tileDataLength);

            /*
             * if (bitsPerSample >= 8 && bitMaskDataLength > 0) {
             * LOGGER.finer("Tile contains no data pixels, applying no-data mask"); int
             * pixArrayOffset; boolean isNoData; final int bytesPerSample = bitsPerSample / 8;// hey
             * this won't work for bits x // sample < 8 for (int pixelN = 0; pixelN < numPixels;
             * pixelN++) { pixArrayOffset = pixelN bytesPerSample; isNoData = (((bitmaskData[pixelN
             * / 8] >> (7 - (pixArrayOffset % 8))) & 0x01) == 0x00); if (isNoData) { for (int i = 0;
             * i < bytesPerSample; i++) { tileData[pixArrayOffset + i] = NO_DATA_BYTE; } } } }
             */

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("returning " + numPixels + " pixels data packaged into "
                        + tileDataLength + " bytes for tile [" + tile.getColumnIndex() + ","
                        + tile.getRowIndex() + "]");
            }
        } else {
            throw new IllegalStateException("Expected pixels per tile == " + pixelsPerTile
                    + " but got " + numPixels + ": " + tile);
        }

        return tileData;
    }

    private SeRasterTile nextTile() throws IOException {
        if (nextTile == null) {
            throw new EOFException("No more tiles to read");
        }
        SeRasterTile curr = nextTile;
        try {
            nextTile = row.getRasterTile();
            if (nextTile == null) {
                LOGGER.finer("There're no more tiles to fetch");
            }
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
        return curr;
    }

}
