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
package org.geotools.arcsde.gce.imageio;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.geotools.data.DataSourceException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SDEPoint;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeRasterAttr;

/**
 * This class represents an ArcSDE Raster Pyramid. Basically, it wraps the SeRasterAttr object and
 * implements some convenience methods for doing calculations with it.
 * 
 * @author Saul Farber
 * 
 */
public class ArcSDEPyramid {

    /**
     * Orders pyramid levels by their level index
     */
    private static final Comparator<ArcSDEPyramidLevel> levelComparator = new Comparator<ArcSDEPyramidLevel>() {
        public int compare(ArcSDEPyramidLevel p0, ArcSDEPyramidLevel p1) {
            return (p0.getLevel() - p1.getLevel());
        }
    };

    ArrayList<ArcSDEPyramidLevel> pyramidList;

    private int tileWidth;

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    private int tileHeight;

    /**
     * Creates an in-memory representation of an ArcSDE Raster Pyramid. Basically it wraps the
     * supplide SeRasterAttr object and implements some convenience logic for extracting
     * information/ doing calculations with it.
     * 
     * @param rasterAttributes
     *            the SeRasterAttr object for the raster of interest.
     * @param crs
     * @throws DataSourceException
     */
    public ArcSDEPyramid(final SeRasterAttr rasterAttributes, final CoordinateReferenceSystem crs)
            throws DataSourceException {
        try {
            // levels goes from 0 to N, maxLevel is the zero-based max index of levels
            final int numLevels = rasterAttributes.getMaxLevel() + 1;
            pyramidList = new ArrayList<ArcSDEPyramidLevel>(numLevels);

            tileWidth = rasterAttributes.getTileWidth();
            tileHeight = rasterAttributes.getTileHeight();

            for (int level = 0; level < numLevels; level++) {
                if (level == 1 && rasterAttributes.skipLevelOne()) {
                    continue;
                }

                ReferencedEnvelope levelExtent = new ReferencedEnvelope(crs);
                SeExtent slExtent = rasterAttributes.getExtentByLevel(level);
                levelExtent.expandToInclude(slExtent.getMinX(), slExtent.getMinY());
                levelExtent.expandToInclude(slExtent.getMaxX(), slExtent.getMaxY());

                final int levelWidth = rasterAttributes.getImageWidthByLevel(level);
                final int levelHeight = rasterAttributes.getImageHeightByLevel(level);

                Dimension size = new Dimension(levelWidth, levelHeight);

                SDEPoint imageOffset = rasterAttributes.getImageOffsetByLevel(level);
                int xOffset = (int) (imageOffset == null ? 0 : imageOffset.getX());
                int yOffset = (int) (imageOffset == null ? 0 : imageOffset.getY());

                int tilesPerRow = rasterAttributes.getTilesPerRowByLevel(level);
                int tilesPerCol = rasterAttributes.getTilesPerColByLevel(level);
                addPyramidLevel(level, levelExtent, xOffset, yOffset, tilesPerRow, tilesPerCol,
                        size);
            }

        } catch (SeException se) {
            throw new DataSourceException(se);
        }
    }

    /**
     * Don't use this constructor. It only exists for unit testing purposes.
     * 
     * @param tileWidth
     *            DON'T USE
     * @param tileHeight
     *            DON'T USE
     */
    public ArcSDEPyramid(int tileWidth, int tileHeight) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        pyramidList = new ArrayList<ArcSDEPyramidLevel>(4);
    }

    public Dimension getTileDimension() {
        return new Dimension(tileWidth, tileHeight);
    }

    public ArcSDEPyramidLevel getPyramidLevel(int level) {
        return pyramidList.get(level);
    }

    public int getNumLevels() {
        return pyramidList.size();
    }

    /**
     * Given this raster's pyramid info this method picks the optimal pyramid level for rendering
     * this request.
     * 
     * @param requestEnvelope
     *            The requested geographical extent
     * @param pixelDimensions
     *            The request pixel size of the image
     * @return the integer number of the raster level most appropriate for this request.
     * @deprecated
     */
    public int pickOptimalRasterLevel(final ReferencedEnvelope requestEnvelope,
            final Rectangle pixelDimensions) throws DataSourceException {

        double reqXRes = requestEnvelope.getWidth() / pixelDimensions.width;
        double reqYRes = requestEnvelope.getHeight() / pixelDimensions.height;

        ArcSDEPyramidLevel[] pyramidInfo = pyramidList.toArray(new ArcSDEPyramidLevel[pyramidList
                .size()]);

        int targetLevel = 0;
        for (int i = 0; i < pyramidInfo.length; i++) {
            if (reqXRes >= pyramidInfo[i].getXRes() && reqYRes >= pyramidInfo[i].getYRes()) {
                targetLevel = i;
            } else {
                break;
            }
        }

        return targetLevel;
    }

    /**
     * Given a requested envelope and a chosen raster level, figure out and return the actual SDE
     * raster tiles, image size and the exact envelope of that image.
     * 
     * @param reqEnv
     *            The original requested envelope.
     * @param rasterLvl
     *            The chosen pyramid level at which to best-fit the requsted envelope.
     * @return
     */
    public RasterQueryInfo fitExtentToRasterPixelGrid(ReferencedEnvelope reqEnv, int rasterLvl) {
        final RasterQueryInfo ret = new RasterQueryInfo();
        final ArcSDEPyramidLevel pLevel = getPyramidLevel(rasterLvl);

        final ReferencedEnvelope levelEnvelope = pLevel.getEnvelope();
        double delta = reqEnv.getMinX() - levelEnvelope.getMinX();

        final double resX = pLevel.getXRes();
        final double resY = pLevel.getYRes();

        final int xMinPixel = (int) Math.floor(delta / resX);

        delta = reqEnv.getMaxX() - levelEnvelope.getMinX();
        final int xMaxPixel = (int) Math.ceil(delta / resX);

        delta = levelEnvelope.getMaxY() - reqEnv.getMaxY();
        // Distance in pixels from the top of the whole pyramid image to the top
        // of our AOI.
        // If we're off the top, this number will be negative.
        final int yMinPixel = (int) Math.floor(delta / resY);

        delta = levelEnvelope.getMaxY() - reqEnv.getMinY();
        final int yMaxPixel = (int) Math.ceil(delta / resY);

        final int widthPixel = xMaxPixel - xMinPixel;
        final int heightPixel = yMaxPixel - yMinPixel;

        final double xMinGeo = levelEnvelope.getMinX() + resX * xMinPixel;
        final double yMinGeo = levelEnvelope.getMaxY() - resY * (yMinPixel + heightPixel);
        final double widthGeo = resX * widthPixel;
        final double heightGeo = resY * heightPixel;

        ret.requestedEnvelope = new ReferencedEnvelope(xMinGeo, xMinGeo + widthGeo, yMinGeo,
                yMinGeo + heightGeo, reqEnv.getCoordinateReferenceSystem());
        ret.requestedPixels = new Rectangle(xMinPixel, yMinPixel, widthPixel, heightPixel);

        // /*
        // * figure out which tiles to query, in tile space
        // */
        // final int minTileX = sourceRegion.x / tileWidth;
        // final int minTileY = sourceRegion.y / tileHeight;
        // if (LOGGER.isLoggable(Level.FINER))
        // LOGGER
        // .finer("figured minTiles: " + minTileX + "," + minTileY + ".  Image is "
        // + curLevel.getNumTilesWide() + "x" + curLevel.getNumTilesHigh()
        // + " tiles wxh.");
        // int maxTileX = (sourceRegion.x + sourceRegion.width + tileWidth - 1) / tileWidth - 1;
        // int maxTileY = (sourceRegion.y + sourceRegion.height + tileHeight - 1) / tileHeight - 1;
        // if (maxTileX >= curLevel.getNumTilesWide())
        // maxTileX = curLevel.getNumTilesWide() - 1;
        // if (maxTileY >= curLevel.getNumTilesHigh())
        // maxTileY = curLevel.getNumTilesHigh() - 1;
        //
        // // figure out what our offset into the tile grid is
        // final int tilegridOffsetX = sourceRegion.x % tileWidth;
        // final int tilegridOffsetY = sourceRegion.y % tileHeight;
        //
        // if (LOGGER.isLoggable(Level.INFO)) {
        // LOGGER.info("Reading " + param.getSourceRegion() + " offset by "
        // + param.getDestinationOffset() + " (tiles " + minTileX + "," + minTileY
        // + " to " + maxTileX + "," + maxTileY + " in level " + imageIndex + ")");
        // }
        return ret;
    }

    /**
     * Don't use this method. It's only public for unit testing purposes.
     * 
     * @param level
     *            the zero-based level index for the new level
     * @param extent
     *            the geographical extent the level covers
     * @param crs
     *            DON'T USE
     * @param offset
     *            DON'T USE
     * @param numTilesWide
     *            DON'T USE
     * @param numTilesHigh
     *            DON'T USE
     * @param imageSize
     *            DON'T USE
     */
    public void addPyramidLevel(int level, ReferencedEnvelope extent, int xOffset, int yOffset,
            int numTilesWide, int numTilesHigh, Dimension imageSize) {

        ArcSDEPyramidLevel pyramidLevel;
        pyramidLevel = new ArcSDEPyramidLevel(level, extent, xOffset, yOffset, numTilesWide,
                numTilesHigh, imageSize);

        pyramidList.add(pyramidLevel);

        Collections.sort(pyramidList, levelComparator);
    }

    public static class RasterQueryInfo {

        public Rectangle requestedPixels;

        public ReferencedEnvelope requestedEnvelope;

        public Rectangle actualPixels;

        public ReferencedEnvelope actualEnvelope;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("ArcSDEPyramid[");
        b.append("Nº levels: ").append(getNumLevels()).append(", tile size: ").append(
                getTileWidth()).append("x").append(getTileHeight()).append("\n\tLevels:");
        for (int l = 0; l < getNumLevels(); l++) {
            b.append("\n\t").append(getPyramidLevel(l));
        }
        b.append("\n]");
        return b.toString();
    }

    // public ReferencedEnvelope getTileExtent(int pyramidLevel, int tileX, int tileY) {
    // final ArcSDEPyramidLevel level = getPyramidLevel(pyramidLevel);
    // final ReferencedEnvelope levelExtent = level.getEnvelope();
    // ReferencedEnvelope tileExtent = new ReferencedEnvelope(levelExtent
    // .getCoordinateReferenceSystem());
    // double xres = level.getXRes();
    // double yres = level.getYRes();
    //
    // double tileWidth = getTileWidth();
    // double tileHeight = getTileHeight();
    //
    // double tileSpanXGeo = xres * tileWidth;
    // double tileSpanYGeo = yres * tileHeight;
    //
    // double minx = levelExtent.getMinX() + (tileX * tileSpanXGeo);
    // double miny = levelExtent.getMinY() + (tileY * tileSpanYGeo);
    //
    // tileExtent.expandToInclude(minx, miny);
    // tileExtent.expandToInclude(minx + tileSpanXGeo, miny + tileSpanYGeo);
    // return tileExtent;
    // }
}
