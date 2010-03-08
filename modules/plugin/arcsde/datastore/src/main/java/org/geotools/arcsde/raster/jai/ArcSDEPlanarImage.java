package org.geotools.arcsde.raster.jai;

import java.awt.Point;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.TileCache;
import javax.media.jai.TileFactory;

import org.geotools.arcsde.raster.io.TileReader;
import org.geotools.util.Utilities;
import org.geotools.util.logging.Logging;

import com.sun.media.jai.codecimpl.util.DataBufferDouble;
import com.sun.media.jai.util.ImageUtil;

@SuppressWarnings("unchecked")
public class ArcSDEPlanarImage extends PlanarImage {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.raster.jai");

    private TileReader tileReader;

    private final SampleModel tileSampleModel;

    private final BigInteger UID;

    private final int hashCode;

    public ArcSDEPlanarImage(TileReader tileReader, int minX, int minY, int width, int height,
            int tileGridXOffset, int tileGridYOffset, SampleModel tileSampleModel,
            ColorModel colorModel) {

        this.tileReader = tileReader;
        this.tileSampleModel = tileSampleModel;

        super.minX = minX;
        super.minY = minY;
        super.width = width;
        super.height = height;
        super.tileGridXOffset = tileGridXOffset;
        super.tileGridYOffset = tileGridYOffset;
        super.tileWidth = tileReader.getTileWidth();
        super.tileHeight = tileReader.getTileHeight();

        super.colorModel = colorModel;
        super.sampleModel = tileSampleModel;

        {
            int result = 17;
            // collect the contributions of various fields
            result = Utilities.hash(tileReader.getServerName(), result);
            result = Utilities.hash(tileReader.getRasterTableName(), result);
            result = Utilities.hash(tileReader.getRasterId(), result);
            result = Utilities.hash(tileReader.getPyramidLevel(), result);
            this.hashCode = result;
        }
        this.UID = (BigInteger) ImageUtil.generateID(this);
    }

    // @Override
    // public boolean equals(Object o) {
    // return super.equals(o);
    // }

    @Override
    public SampleModel getSampleModel() {
        return sampleModel;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public BigInteger getImageID() {
        return UID;
    }

    private int lastTileX, lastTileY;

    private WritableRaster currentTile;

    /**
     * @see java.awt.image.RenderedImage#getTile(int, int)
     */
    @Override
    public synchronized Raster getTile(final int tileX, final int tileY) {
        if (tileX == lastTileX && tileY == lastTileY && currentTile != null) {
            return currentTile;
        }

        // System.err.printf("getTile(%d, %d) %s\n", tileX, tileY, this.toString());
        final boolean useCache = false;
        final JAI jai = JAI.getDefaultInstance();
        final TileCache jaiCache = jai.getTileCache();

        if (useCache && jaiCache != null) {
            Raster tile = jaiCache.getTile(this, tileX, tileY);
            if (tile != null) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("! GOT TILE FROM TileCache " + tileX + ", " + tileY + ", plevel "
                            + tileReader.getPyramidLevel());
                }
                return tile;
            }
        }
        if (super.tileFactory == null) {
            TileFactory tileFactory = (TileFactory) jai.getRenderingHint(JAI.KEY_TILE_FACTORY);
            if (tileFactory != null) {
                super.tileFactory = tileFactory;
            }
        }

        final int xOrigin = tileXToX(tileX);
        final int yOrigin = tileYToY(tileY);

        if (currentTile == null) {
            currentTile = Raster.createWritableRaster(tileSampleModel, new Point(xOrigin, yOrigin));
        } else {
            DataBuffer db = currentTile.getDataBuffer();
            currentTile = Raster.createWritableRaster(tileSampleModel, db, new Point(xOrigin,
                    yOrigin));
        }

        if (shallIgnoreTile(tileX, tileY)) {
            // not a requested tile
            return currentTile;
        }

        final int readerTileX = tileX - tileReader.getMinTileX();
        final int readerTileY = tileY - tileReader.getMinTileY();

        try {
            switch (tileSampleModel.getDataType()) {
            case DataBuffer.TYPE_BYTE: {
                DataBufferByte dataBuffer = (DataBufferByte) currentTile.getDataBuffer();
                byte[][] bankData = dataBuffer.getBankData();
                tileReader.getTile(readerTileX, readerTileY, bankData);
            }
                break;
            case DataBuffer.TYPE_USHORT: {
                DataBufferUShort dataBuffer = (DataBufferUShort) currentTile.getDataBuffer();
                short[][] bankData = dataBuffer.getBankData();
                tileReader.getTile(readerTileX, readerTileY, bankData);
            }
                break;
            case DataBuffer.TYPE_SHORT: {
                DataBufferShort dataBuffer = (DataBufferShort) currentTile.getDataBuffer();
                short[][] bankData = dataBuffer.getBankData();
                tileReader.getTile(readerTileX, readerTileY, bankData);
            }
                break;
            case DataBuffer.TYPE_INT: {
                DataBufferInt dataBuffer = (DataBufferInt) currentTile.getDataBuffer();
                int[][] bankData = dataBuffer.getBankData();
                tileReader.getTile(readerTileX, readerTileY, bankData);
            }
                break;
            case DataBuffer.TYPE_FLOAT: {
                DataBufferFloat dataBuffer = (DataBufferFloat) currentTile.getDataBuffer();
                float[][] bankData = dataBuffer.getBankData();
                tileReader.getTile(readerTileX, readerTileY, bankData);
            }
                break;
            case DataBuffer.TYPE_DOUBLE: {
                DataBufferDouble dataBuffer = (DataBufferDouble) currentTile.getDataBuffer();
                double[][] bankData = dataBuffer.getBankData();
                tileReader.getTile(readerTileX, readerTileY, bankData);
            }
                break;
            default:
                throw new IllegalStateException("Unrecognized DataBuffer type: "
                        + tileSampleModel.getDataType());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        if (useCache && jaiCache != null) {
            jaiCache.add(this, tileX, tileY, currentTile);
        }

        lastTileX = tileX;
        lastTileY = tileY;

        return currentTile;
    }

    private boolean shallIgnoreTile(int tx, int ty) {
        int minTileX = tileReader.getMinTileX();
        int minTileY = tileReader.getMinTileY();
        int tilesWide = tileReader.getTilesWide();
        int tilesHigh = tileReader.getTilesHigh();

        return tx < minTileX || ty < minTileY || tx > minTileX + tilesWide
                || ty > minTileY + tilesHigh;
    }

}
