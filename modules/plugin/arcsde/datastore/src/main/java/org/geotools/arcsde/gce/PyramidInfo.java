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
class PyramidInfo {

    /**
     * Orders pyramid levels by their level index
     */
    private static final Comparator<PyramidLevelInfo> levelComparator = new Comparator<PyramidLevelInfo>() {
        public int compare(PyramidLevelInfo p0, PyramidLevelInfo p1) {
            return (p0.getLevel() - p1.getLevel());
        }
    };

    ArrayList<PyramidLevelInfo> pyramidList;

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
    public PyramidInfo(final SeRasterAttr rasterAttributes, final CoordinateReferenceSystem crs)
            throws DataSourceException {
        try {
            // levels goes from 0 to N, maxLevel is the zero-based max index of levels
            final int numLevels = rasterAttributes.getMaxLevel() + 1;
            pyramidList = new ArrayList<PyramidLevelInfo>(numLevels);

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
    public PyramidInfo(int tileWidth, int tileHeight) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        pyramidList = new ArrayList<PyramidLevelInfo>(4);
    }

    public Dimension getTileDimension() {
        return new Dimension(tileWidth, tileHeight);
    }

    public PyramidLevelInfo getPyramidLevel(int level) {
        return pyramidList.get(level);
    }

    public int getNumLevels() {
        return pyramidList.size();
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
    void addPyramidLevel(int level, ReferencedEnvelope extent, int xOffset, int yOffset,
            int numTilesWide, int numTilesHigh, Dimension imageSize) {

        PyramidLevelInfo pyramidLevel;
        pyramidLevel = new PyramidLevelInfo(level, extent, xOffset, yOffset, numTilesWide,
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
