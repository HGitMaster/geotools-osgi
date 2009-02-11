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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.geotools.data.DataSourceException;
import org.geotools.geometry.GeneralEnvelope;
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

    private int tileHeight;

    private GeneralEnvelope originalEnvelope;

    private ArrayList<RasterBandInfo> bands;

    private CoordinateReferenceSystem crs;

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
        this.crs = crs;
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

                Dimension levelImageSize = new Dimension(levelWidth, levelHeight);

                SDEPoint imageOffset = rasterAttributes.getImageOffsetByLevel(level);
                int xOffset = (int) (imageOffset == null ? 0 : imageOffset.getX());
                int yOffset = (int) (imageOffset == null ? 0 : imageOffset.getY());

                int tilesPerRow = rasterAttributes.getTilesPerRowByLevel(level);
                int tilesPerCol = rasterAttributes.getTilesPerColByLevel(level);
                addPyramidLevel(level, levelExtent, xOffset, yOffset, tilesPerRow, tilesPerCol,
                        levelImageSize);
            }

        } catch (SeException se) {
            throw new DataSourceException(se);
        }
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
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
     * @param xOffset
     *            the offset on the X axis of the actual image inside the tile space for this level
     * @param yOffset
     *            the offset on the Y axis of the actual image inside the tile space for this level
     * @param numTilesWide
     *            the number of tiles that make up the level on the X axis
     * @param numTilesHigh
     *            the number of tiles that make up the level on the Y axis
     * @param imageSize
     *            the size of the actual image in pixels
     */
    void addPyramidLevel(int level, ReferencedEnvelope extent, int xOffset, int yOffset,
            int numTilesWide, int numTilesHigh, Dimension imageSize) {

        PyramidLevelInfo pyramidLevel;
        pyramidLevel = new PyramidLevelInfo(level, extent, xOffset, yOffset, numTilesWide,
                numTilesHigh, imageSize);

        pyramidList.add(pyramidLevel);

        Collections.sort(pyramidList, levelComparator);
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

    void setOriginalEnvelope(GeneralEnvelope originalEnvelope) {
        this.originalEnvelope = originalEnvelope;
    }

    public GeneralEnvelope getOriginalEnvelope() {
        return originalEnvelope;
    }

    void setBands(List<RasterBandInfo> bands) {
        this.bands = new ArrayList<RasterBandInfo>(bands);
    }

    public List<RasterBandInfo> getBands() {
        return new ArrayList<RasterBandInfo>(bands);
    }

    public int getNumBands() {
        return bands.size();
    }

    public RasterBandInfo getBand(final int index) {
        return bands.get(0);
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }
}
