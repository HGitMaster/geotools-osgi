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
package org.geotools.arcsde.raster.info;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DataSourceException;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SDEPoint;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeRasterAttr;

/**
 * A RasterInfo gathers the metadata for a single raster in a raster dataset
 * <p>
 * Basically, it wraps the SeRasterAttr object and implements some convenience methods for doing
 * calculations with it.
 * </p>
 * 
 * @author Saul Farber
 * @author Gabriel Roldan
 */
public final class RasterInfo {

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
    RasterInfo(final SeRasterAttr rasterAttributes, final CoordinateReferenceSystem crs)
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

                /*
                 * this extent corresponds to the actual image size inside the tiled grid. That is,
                 * to getImageWidth/Height by level. The dimensions of this extent are correct, but
                 * it needs to be shifted by SeRasterAttr.getExtentOffsetByLevel(level) the same way
                 * the grid envelope does by SeRasterAttr.getImageOffsetByLevel(level)
                 */
                final SeExtent slExtent = rasterAttributes.getExtentByLevel(level);

                final int levelWidth = rasterAttributes.getImageWidthByLevel(level);
                final int levelHeight = rasterAttributes.getImageHeightByLevel(level);

                Dimension levelImageSize = new Dimension(levelWidth, levelHeight);

                Point imgOffset = new Point();
                // extent offset equals imgOffset * pixel resolution (ie, if resx == 2 and offsetX =
                // 10, extent offset x == 20)
                Point2D extOffset = new Point2D.Double();
                {
                    SDEPoint imageOffset = rasterAttributes.getImageOffsetByLevel(level);
                    int xOffset = (int) (imageOffset == null ? 0 : imageOffset.getX());
                    int yOffset = (int) (imageOffset == null ? 0 : imageOffset.getY());
                    imgOffset.setLocation(xOffset, yOffset);

                    SDEPoint extentOffset = rasterAttributes.getExtentOffsetByLevel(level);
                    double xOffsetExtent = extentOffset == null ? 0D : extentOffset.getX();
                    double yOffsetExtent = extentOffset == null ? 0D : extentOffset.getY();
                    extOffset.setLocation(xOffsetExtent, yOffsetExtent);
                }

                final int numTilesWide = rasterAttributes.getTilesPerRowByLevel(level);
                final int numTileHigh = rasterAttributes.getTilesPerColByLevel(level);

                ReferencedEnvelope levelExtent = new ReferencedEnvelope(slExtent.getMinX(),
                        slExtent.getMaxX(), slExtent.getMinY(), slExtent.getMaxY(), crs);

                addPyramidLevel(level, levelExtent, imgOffset, extOffset, numTilesWide,
                        numTileHigh, levelImageSize);
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
    public RasterInfo(Long rasterId, int tileWidth, int tileHeight) {
        this.rasterId = rasterId;
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
     *            the geographical extent the level covers, may need to be offsetted by {@code
     *            extOffset}
     * @param imgOffset
     *            the offset on the X and Y axes of the actual image inside the tile space for this
     *            level
     * @param extOffset
     *            the offset on the X and Y axes of the actual image inside the tile space for this
     *            level
     * @param numTilesWide
     *            the number of tiles that make up the level on the X axis
     * @param numTilesHigh
     *            the number of tiles that make up the level on the Y axis
     * @param imageSize
     *            the size of the actual image in pixels
     */
    public void addPyramidLevel(int level, ReferencedEnvelope extent, Point imgOffset,
            Point2D extOffset, int numTilesWide, int numTilesHigh, Dimension imageSize) {

        PyramidLevelInfo pyramidLevel;
        pyramidLevel = new PyramidLevelInfo(level, extent, imgOffset, extOffset, numTilesWide,
                numTilesHigh, imageSize);

        pyramidList.add(pyramidLevel);

        Collections.sort(pyramidList, levelComparator);
    }

    void setOriginalEnvelope(GeneralEnvelope originalEnvelope) {
        this.originalEnvelope = originalEnvelope;
    }

    public GeneralEnvelope getOriginalEnvelope() {
        return originalEnvelope;
    }

    public void setBands(List<RasterBandInfo> bands) {
        this.bands = new ArrayList<RasterBandInfo>(bands);
    }

    public List<RasterBandInfo> getBands() {
        return new ArrayList<RasterBandInfo>(bands);
    }

    public int getNumBands() {
        return bands.size();
    }

    public RasterBandInfo getBand(final int index) {
        return bands.get(index);
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    public RasterCellType getTargetCellType() {
        // if (isColorMapped()) {
        // // color map is already promoted if needed
        // return getNativeCellType();
        // }
        List<Number> noDataValues = getNoDataValues();
        RasterCellType nativeCellType = getNativeCellType();
        RasterCellType targetCellType = RasterUtils.determineTargetCellType(nativeCellType,
                noDataValues);
        return targetCellType;
    }

    public boolean isColorMapped() {
        return getBand(0).isColorMapped();
    }

    public RasterCellType getNativeCellType() {
        return getBand(0).getCellType();
    }

    public List<Number> getNoDataValues() {
        final List<Number> noDataValues = new ArrayList<Number>();
        for (RasterBandInfo band : getBands()) {
            Number noDataValue = band.getNoDataValue();
            noDataValues.add(noDataValue);
        }
        return noDataValues;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append("[Id: ").append(getRasterId());
        String srs = null;
        try {
            srs = CRS.lookupIdentifier(getCoordinateReferenceSystem(), false);
        } catch (FactoryException e) {
            e.printStackTrace();
        }
        sb.append(", bands: ").append(getNumBands());
        sb.append(", levels: ").append(getNumLevels());
        sb.append(", tile size: ").append(getTileWidth()).append("x").append(getTileHeight());
        sb.append(", crs: ").append(srs == null ? getCoordinateReferenceSystem().toWKT() : srs);
        GeneralEnvelope env = getOriginalEnvelope();
        sb.append(", Envelope: ").append(env.getMinimum(0)).append(",").append(env.getMinimum(1))
                .append(" ").append(env.getMaximum(0)).append(",").append(env.getMaximum(1));

        sb.append("]\n Bands[");
        for (RasterBandInfo band : getBands()) {
            sb.append("\n\t");
            sb.append(band.toString());
        }
        sb.append("\n ]");
        sb.append("\n Pyramid[");
        for (int l = 0; l < getNumLevels(); l++) {
            sb.append("\n\t").append(getPyramidLevel(l).toString());
        }
        sb.append("\n ]");
        return sb.toString();
    }

}
