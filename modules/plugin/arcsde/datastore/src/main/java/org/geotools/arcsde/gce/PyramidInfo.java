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

import org.geotools.coverage.grid.io.OverviewPolicy;
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

    private Long rasterId;

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
            this.rasterId = Long.valueOf(rasterAttributes.getRasterId().longValue());
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

    public Long getRasterId() {
        return rasterId;
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
     * @param pyramidLevel
     * @return resx, resy, scalefactor
     */
    double[] getResolution(final int pyramidLevel) {
        final double highestRes = getPyramidLevel(0).getXRes();
        PyramidLevelInfo level = getPyramidLevel(pyramidLevel);
        double[] resolution = new double[3];
        resolution[0] = level.getXRes();
        resolution[1] = level.getYRes();
        resolution[2] = level.getXRes() / highestRes;
        return resolution;
    }

    /**
     * <p>
     * NOTE: logic stolen and adapted from {@code AbstractGridCoverage2DReader#getOverviewImage()}
     * </p>
     * 
     * @param policy
     * @return
     */
    public int getOptimalPyramidLevel(final OverviewPolicy policy, final double[] requestedRes) {

        int pyramidLevelChoice = 0;

        // sort resolutions from smallest pixels (higher res) to biggest pixels (higher res)
        // keeping a reference to the original image choice
        final double[] highestRes = getResolution(0);

        // Now search for the best matching resolution.
        // Check also for the "perfect match"... unlikely in practice unless someone
        // tunes the clients to request exactly the resolution embedded in
        // the overviews, something a perf sensitive person might do in fact

        // the requested resolutions
        final double reqx = requestedRes[0];
        final double reqy = requestedRes[1];

        // requested scale factor for least reduced axis
        final double requestedScaleFactorX = reqx / highestRes[0];
        final double requestedScaleFactorY = reqy / highestRes[1];
        final int leastReduceAxis = requestedScaleFactorX <= requestedScaleFactorY ? 0 : 1;
        final double requestedScaleFactor = leastReduceAxis == 0 ? requestedScaleFactorX
                : requestedScaleFactorY;

        final int numLevels = getNumLevels();

        // no pyramiding or are we looking for a resolution even higher than the native one?
        if (0 == numLevels || requestedScaleFactor <= 1) {
            pyramidLevelChoice = 0;
        } else {
            // are we looking for a resolution even lower than the smallest overview?
            final double[] min = getResolution(numLevels - 1);
            if (requestedScaleFactor >= min[2]) {
                pyramidLevelChoice = numLevels - 1;
            } else {
                // Ok, so we know the overview is between min and max, skip the first
                // and search for an overview with a resolution lower than the one requested,
                // that one and the one from the previous step will bound the searched resolution
                double[] prev = highestRes;
                for (int levelN = 1; levelN < numLevels; levelN++) {
                    final double[] curr = getResolution(levelN);
                    // perfect match check
                    if (curr[2] == requestedScaleFactor) {
                        pyramidLevelChoice = levelN;
                    } else {
                        /*
                         * middle check. The first part of the condition should be sufficient, but
                         * there are cases where the x resolution is satisfied by the lowest
                         * resolution, the y by the one before the lowest (so the aspect ratio of
                         * the request is different than the one of the overviews), and we would end
                         * up going out of the loop since not even the lowest can "top" the request
                         * for one axis
                         */
                        if (curr[2] > requestedScaleFactor || levelN == numLevels - 1) {
                            if (policy == OverviewPolicy.QUALITY) {
                                pyramidLevelChoice = levelN - 1;
                            } else if (policy == OverviewPolicy.SPEED) {
                                return levelN;
                            } else if (requestedScaleFactor - prev[2] < curr[2]
                                    - requestedScaleFactor) {
                                pyramidLevelChoice = levelN - 1;
                            } else {
                                pyramidLevelChoice = levelN;
                            }
                            break;
                        }
                        prev = curr;
                    }
                }
            }
        }
        // fallback
        return pyramidLevelChoice;
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
