/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;

import org.geotools.arcsde.gce.band.ArcSDERasterBandCopier;
import org.geotools.arcsde.gce.imageio.ArcSDEPyramid;
import org.geotools.arcsde.gce.imageio.ArcSDEPyramidLevel;
import org.geotools.arcsde.gce.imageio.ArcSDERasterImageReadParam;
import org.geotools.arcsde.gce.imageio.RasterCellType;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.data.DataSourceException;
import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterConstraint;
import com.esri.sde.sdk.client.SeRasterTile;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

/**
 * 
 * <p>
 * Support matrix:
 * 
 * <pre>
 * &lt;code&gt;
 *         Pixel Type                      1 Band     1 Band colormapped    3-4 Bands
 * SeRaster#SE_PIXEL_TYPE_16BIT_S
 * SeRaster#SE_PIXEL_TYPE_16BIT_U
 * SeRaster#SE_PIXEL_TYPE_1BIT                Y
 * SeRaster#SE_PIXEL_TYPE_32BIT_REAL
 * SeRaster#SE_PIXEL_TYPE_32BIT_S
 * SeRaster#SE_PIXEL_TYPE_32BIT_U
 * SeRaster#SE_PIXEL_TYPE_4BIT
 * SeRaster#SE_PIXEL_TYPE_64BIT_REAL
 * SeRaster#SE_PIXEL_TYPE_8BIT_S              
 * SeRaster#SE_PIXEL_TYPE_8BIT_U              Y                                 Y 
 * &lt;/code&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author Saul Farber
 * @author Gabriel Roldan
 * @deprecated leaving in test code by now until making sure we're not loosing test coverage
 */
@Deprecated
public class ArcSDERasterReader extends ImageReader {

    private static final boolean DEBUG = false;

    private static final ArrayList<ImageTypeSpecifier> supportedImageTypes;
    static {
        supportedImageTypes = new ArrayList<ImageTypeSpecifier>();
        supportedImageTypes.add(ImageTypeSpecifier
                .createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB));
    }

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    private final ArcSDEPyramid pyramidInfo;

    private final String rasterTable;

    private final String rasterColumn;

    private BufferedImage sampleImage;

    public ArcSDERasterReader(final ArcSDERasterReaderSpi parent, final ArcSDEPyramid pyramidInfo,
            final String rasterTable, final String rasterColumn, final BufferedImage sampleImage) {
        super(parent);
        this.pyramidInfo = pyramidInfo;
        this.rasterTable = rasterTable;
        this.rasterColumn = rasterColumn;
        this.sampleImage = sampleImage;
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        return pyramidInfo.getPyramidLevel(imageIndex).size.height;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        return pyramidInfo.getPyramidLevel(imageIndex).size.width;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        return supportedImageTypes.iterator();
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        return pyramidInfo.getNumLevels();
    }

    /**
     * This method is THREADSAFE. Reads from its configured ArcSDE Raster "row" (one row in a raster
     * table corresponds to one ArcSDERasterReader) in the manner described by the supplied
     * ImageReadParam.
     * 
     * @param param
     *            This must be an ArcSDERasterImageReadParam, containing a valid, live SeConnection
     *            through which this reader will "suck" its raster data from SDE.
     * @param imageIndex
     *            This parameter specifies which image pyramid level (if there's more than one) to
     *            read from. If there's only one pyramid level, this value should be 0.
     * @throws IOException
     * 
     * @see ImageReader#read(int, ImageReadParam)
     */
    @Override
    public BufferedImage read(final int imageIndex, final ImageReadParam param) throws IOException {

        // we only read from ArcSDERasterImageReadParams.
        if (!(param instanceof ArcSDERasterImageReadParam)) {
            throw new IllegalArgumentException(
                    "read() must be called with an ArcSDERasterReadImageParam, not a "
                            + param.getClass());
        }
        final ArcSDERasterImageReadParam sdeirp = (ArcSDERasterImageReadParam) param;

        // double-check that all the required info is present.
        if (sdeirp.getSourceBands() == null) {
            throw new IllegalArgumentException(
                    "You must provide source bands to the ArcSDERasterReader via param.setSourceBands()");
        }
        if (sdeirp.getConnection() == null) {
            throw new IllegalArgumentException(
                    "You must provide a connection to the ArcSDERasterReader via the param.setConnection() method.");
        }
        if (sdeirp.getBandMapper() == null) {
            throw new IllegalArgumentException(
                    "You must provide a hashmap bandmapper to the ArcSDERasterReader via the param.setBandMapper() method");
        }

        return read(imageIndex, sdeirp);
    }

    private BufferedImage read(final int imageIndex, final ArcSDERasterImageReadParam param)
            throws IOException {

        // start collecting our background information for doing the read.

        // the source region is the actual pixel extent of this particar pyramid layer
        final Rectangle sourceRegion = param.getSourceRegion();

        final int tileWidth = pyramidInfo.getTileWidth();
        final int tileHeight = pyramidInfo.getTileHeight();
        final ArcSDEPyramidLevel curLevel = pyramidInfo.getPyramidLevel(imageIndex);

        // if we're reading "off the top" or "off the left" side of the image,
        // then there's some "blank" part of the returned image that we need
        // to leave blank. This offset tells us where to start writing into
        // the destination image in that case. (Note, if we're reading "off
        // the bottom" or "off the right" side of the image, this doesn't
        // affect us as we just stop writing at the end of the tile and the
        // rest is left blank automatically).
        final Point intoDestinationImageOffset = param.getDestinationOffset() == null ? new Point(
                0, 0) : param.getDestinationOffset();

        if (curLevel.getXOffset() != 0) {
            sourceRegion.x += curLevel.getXOffset();
        }
        if (curLevel.getYOffset() != 0) {
            sourceRegion.y += curLevel.getYOffset();
        }

        // figure out which tiles exactly, we'll be fetching
        final int minTileX = sourceRegion.x / tileWidth;
        final int minTileY = sourceRegion.y / tileHeight;
        if (LOGGER.isLoggable(Level.FINER))
            LOGGER
                    .finer("figured minTiles: " + minTileX + "," + minTileY + ".  Image is "
                            + curLevel.getNumTilesWide() + "x" + curLevel.getNumTilesHigh()
                            + " tiles wxh.");
        int maxTileX = (sourceRegion.x + sourceRegion.width + tileWidth - 1) / tileWidth - 1;
        int maxTileY = (sourceRegion.y + sourceRegion.height + tileHeight - 1) / tileHeight - 1;
        if (maxTileX >= curLevel.getNumTilesWide())
            maxTileX = curLevel.getNumTilesWide() - 1;
        if (maxTileY >= curLevel.getNumTilesHigh())
            maxTileY = curLevel.getNumTilesHigh() - 1;

        // figure out what our offset into the tile grid is
        final int tilegridOffsetX = sourceRegion.x % tileWidth;
        final int tilegridOffsetY = sourceRegion.y % tileHeight;

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Reading " + param.getSourceRegion() + " offset by "
                    + param.getDestinationOffset() + " (tiles " + minTileX + "," + minTileY
                    + " to " + maxTileX + "," + maxTileY + " in level " + imageIndex + ")");
        }
        // Now we do the actual reading from SDE.
        ArcSDEPooledConnection scon = param.getConnection();
        SeQuery query = null;
        BufferedImage destination;

        try {
            // This rather strange set of query operations is apparently the way
            // one gets SDE Raster output. First, query the
            // database for the single row in the raster business table.
            // FIXME: Raster catalogs need to specify what their row number is.
            query = new SeQuery(scon, new String[] { rasterColumn },
                    new SeSqlConstruct(rasterTable));
            query.prepareQuery();
            query.execute();
            // Next, fetch the single row back.
            final SeRow r = query.fetch();

            // Now build a SeRasterConstraint object which queries the db for
            // the right tiles/bands/pyramid level
            SeRasterConstraint rConstraint = new SeRasterConstraint();
            rConstraint.setEnvelope(minTileX, minTileY, maxTileX, maxTileY);
            rConstraint.setLevel(imageIndex);
            rConstraint.setBands(param.getSourceBands());

            // Finally, execute the raster query aganist the already-opened
            // SeQuery object which already has an SeRow fetched against it.
            query.queryRasterTile(rConstraint);

            final SeRasterAttr rattr = r.getRaster(0);
            final RasterCellType pixelType = RasterCellType.valueOf(rattr.getPixelType());

            // the destination image for our eventual raster read. Can be provided
            // (if the given one is non-null) or we can be expected to generate
            // it (if the given one is null)
            destination = param.getDestination();
            if (destination == null) {
                final int imageWidth = intoDestinationImageOffset.x + sourceRegion.width;
                final int imageHeight = intoDestinationImageOffset.y + sourceRegion.height;
                destination = RasterTestData.createInitialBufferedImage(sampleImage, imageWidth,
                        imageHeight);
            } else if (!(intoDestinationImageOffset.x == 0 && intoDestinationImageOffset.y == 0)) {
                int destWidth = sourceRegion.width, destHeight = sourceRegion.height;
                if (intoDestinationImageOffset.x + sourceRegion.width > destination.getWidth())
                    destWidth = destination.getWidth() - intoDestinationImageOffset.x;
                if (intoDestinationImageOffset.y + sourceRegion.height > destination.getHeight())
                    destHeight = destination.getHeight() - intoDestinationImageOffset.y;
                destination = destination.getSubimage(intoDestinationImageOffset.x,
                        intoDestinationImageOffset.y, destWidth, destHeight);
            } else {
                // we've got a non-null destination image and there's no offset.
                // Nothing to do!
            }

            // the bit of our destination image that overlaps this tile
            WritableRaster destinationSubTile;

            // the offsets into the CURRENT TILE of where we should start
            // copying from the tile to the destination image. Will only be
            // non-zero if we're at the "leading edge" or "top edge" of the
            // grid of tiles.
            int curTileOffsetX, curTileOffsetY;

            // When we copy from the current SeRasterTile into the destination
            // image, we'll create a BufferedImage.getSubImage() of the destitation
            // that perfectly overlaps the current SeRasterTile, and copy the
            // data from the current SeRasterTile to that sub-image. These vars
            // hold the proper offsets, width and height into the DESTINATION IMAGE.
            int destImageOffsetX, destImageOffsetY;
            int destImageTileWidth, destImageTileHeight;

            // Copying from an ArcSDE Raster to the specified image format, we
            // can optionally have a band mapper which specifies which data band
            // goes to which image band.
            Map<Integer, Integer> bandMapper = param.getBandMapper();

            // And we need to create a bandcopier for this raster type.
            final ArcSDERasterBandCopier bandCopier = ArcSDERasterBandCopier.getInstance(pixelType,
                    tileWidth, tileHeight);

            // Now, magically, calls to r.getRasterTile() will fetch our list
            // of tiles.
            SeRasterTile curTile = r.getRasterTile();
            while (curTile != null) {
                // LOGGER.info("tile at " + curTile.getColumnIndex() + "," +
                // curTile.getRowIndex() + " has " + curTile.getNumPixels() + "
                // pixels");
                if (curTile.getNumPixels() == 0 && false) {
                    curTile = r.getRasterTile();
                    continue;
                }

                // Does our image start at the exact tile boundary? If we're in
                // the middle of a range of tiles, it does, otherwise it's probably
                // going to start slightly in from the edge of the tile.
                if (curTile.getColumnIndex() == minTileX) {
                    curTileOffsetX = tilegridOffsetX;
                } else {
                    curTileOffsetX = 0;
                }

                if (curTile.getRowIndex() == minTileY) {
                    curTileOffsetY = tilegridOffsetY;
                } else {
                    curTileOffsetY = 0;
                }

                // Now we figure out how far into the destination image we go to
                // create a mini sub-tile that overlaps this current tile.
                // If we're at the first tile, we start at zero.
                final int curTileX = curTile.getColumnIndex() - minTileX;
                if (curTileX == 0) {
                    destImageOffsetX = 0;
                } else {
                    destImageOffsetX = (tileWidth - tilegridOffsetX) + ((curTileX - 1) * tileWidth);
                }

                final int curTileY = curTile.getRowIndex() - minTileY;
                if (curTileY == 0) {
                    destImageOffsetY = 0;
                } else {
                    destImageOffsetY = (tileHeight - tilegridOffsetY)
                            + ((curTileY - 1) * tileHeight);
                }

                if (curTile.getColumnIndex() == maxTileX
                        && (destImageOffsetX + tileWidth > destination.getWidth())) {
                    // if we're in the final tile, and the image doesn't cover the whole tile,
                    // be sure to grab just the bit of the image that DOES grab the tile...so
                    // that the bufferedImage.getSubimage() call below isn't asked for too "much"
                    // image.
                    destImageTileWidth = destination.getWidth() - destImageOffsetX;
                } else if (curTileX == 0) {
                    // if we're at the beginning of a row or column, we don't
                    // need to grab the entire sub-image. We need to grab
                    // just the bit that overlaps the current tile.
                    destImageTileWidth = (tileWidth - curTileOffsetX);
                } else {
                    destImageTileWidth = tileWidth;
                }

                if (curTile.getColumnIndex() == curLevel.getNumTilesWide() - 1) {
                    // it's possible we don't actually want to read the whole tile, if the tile
                    // has "padding" on the edge
                    if (curLevel.getSize().getWidth() % tileWidth != 0)
                        destImageTileWidth = Math.min(destImageTileWidth,
                                (curLevel.getSize().width % tileWidth) - curTileOffsetX);
                }

                if (curTile.getRowIndex() == maxTileY
                        && (destImageOffsetY + tileHeight > destination.getHeight())) {
                    destImageTileHeight = destination.getHeight() - destImageOffsetY;
                } else if (curTileY == 0) {
                    destImageTileHeight = (tileHeight - curTileOffsetY);
                } else {
                    destImageTileHeight = tileHeight;
                }

                if (curTile.getRowIndex() == curLevel.getNumTilesHigh() - 1) {
                    if (curLevel.getSize().getHeight() % tileHeight != 0)
                        destImageTileHeight = Math.min(destImageTileHeight,
                                (curLevel.getSize().height % tileHeight) - curTileOffsetY);
                }

                // LOGGER.info("creating subtile at " + new
                // Rectangle(destImageOffsetX, destImageOffsetY,
                // destImageTileWidth, destImageTileHeight));
                if (destImageTileWidth == 0 || destImageTileHeight == 0) {
                    if (LOGGER.isLoggable(Level.FINER))
                        LOGGER.finer("Skipping tile " + curTileX + "," + curTileY
                                + " because it has imagetile height " + destImageTileHeight
                                + " and width " + destImageTileWidth);
                    curTile = r.getRasterTile();
                    continue;
                }
                BufferedImage subtile = destination.getSubimage(destImageOffsetX, destImageOffsetY,
                        destImageTileWidth, destImageTileHeight);
                destinationSubTile = subtile.getRaster();

                final Integer curBandId = Integer.valueOf((int) curTile.getBandId().longValue());
                final int targetBand = ((Integer) bandMapper.get(curBandId)).intValue();
                bandCopier.copyPixelData(curTile, destinationSubTile, curTileOffsetX,
                        curTileOffsetY, targetBand);
                // ImageIO.write(subtile, "PNG", new File("/tmp/tile" + curTileX + "-" + curTileY +
                // ".png"));

                if (DEBUG) {
                    int[] blackpixel = new int[] { 0x00, 0x00, 0x00, 0xff };
                    for (int x = 0; x < destinationSubTile.getWidth(); x++) {
                        destinationSubTile.setPixel(x, 0, blackpixel);
                        destinationSubTile.setPixel(x, destinationSubTile.getHeight() - 1,
                                blackpixel);
                    }
                    for (int y = 0; y < destinationSubTile.getHeight(); y++) {
                        destinationSubTile.setPixel(0, y, blackpixel);
                        destinationSubTile.setPixel(destinationSubTile.getWidth() - 1, y,
                                blackpixel);
                    }

                    final Graphics2D graphics = subtile.createGraphics();
                    graphics.setFont(new Font("Sans-serif", Font.BOLD, 10));
                    graphics.setColor(Color.yellow);
                    graphics.drawString(curTile.getRowIndex() + "," + curTile.getColumnIndex(), 10,
                            10);

                    graphics.drawString(destImageOffsetX + "," + destImageOffsetY + " -- "
                            + destImageTileWidth + "x" + destImageTileHeight, 10, 25);

                    graphics.dispose();
                }

                // fetch the next tile
                curTile = r.getRasterTile();
            }
            // always null at this point
            // curTile = null;
            destinationSubTile = null;

            // don't need to close connections, cause that's done in the
            // 'finally' block
        } catch (SeException se) {
            LOGGER.log(Level.SEVERE, se.getSeError().getErrDesc(), se);
            throw new DataSourceException(se);
        } finally {
            try {
                if (query != null)
                    query.close();
                /*
                 * if (scon != null && !scon.isClosed()) scon.close();
                 */
            } catch (SeException se) {
                LOGGER.log(Level.SEVERE, se.getSeError().getErrDesc(), se);
                throw new DataSourceException(
                        "Unable to clean up connections to database.  May have left one hanging.",
                        se);
            }
        }

        return destination;
    }

    /**
     * Not implemented
     */
    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }

    /**
     * Not implemented
     */
    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

}
