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

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.util.logging.Logger;

import org.geotools.geometry.GeneralEnvelope;
import org.geotools.util.logging.Logging;

/**
 * Captures information about a query for a single raster in a raster dataset.
 * 
 * @author Gabriel Roldan
 * @version $Id: RasterQueryInfo.java 34789 2010-01-13 16:44:32Z groldan $
 * @since 2.5.6
 * @see RasterUtils#findMatchingRasters
 * @see RasterUtils#fitRequestToRaster
 */
public final class RasterQueryInfo {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    private GeneralEnvelope requestedEnvelope;

    private Rectangle requestedDim;

    private int pyramidLevel;

    /**
     * The two-dimensional range of tile indices whose envelope intersect the requested extent. Will
     * have negative width and height if none of the tiles do.
     */
    private Rectangle matchingTiles;

    private GeneralEnvelope resultEnvelope;

    private Rectangle resultDimension;

    private Long rasterId;

    private Rectangle mosaicLocation;

    private RenderedImage resultImage;

    private Rectangle tiledImageSize;

    private double[] resolution;

    private int rasterIndex;

    /**
     * The full tile range for the matching pyramid level
     */
    private Rectangle levelTileRange;

    public RasterQueryInfo() {
        setResultDimensionInsideTiledImage(new Rectangle(0, 0, 0, 0));
        setMatchingTiles(new Rectangle(0, 0, 0, 0));
        setResultEnvelope(null);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[Raster query info:");
        s.append("\n\tRaster ID            : ").append(getRasterId());
        s.append("\n\tPyramid level        : ").append(getPyramidLevel());
        s.append("\n\tResolution           : ").append(
                getResolution()[0] + "," + getResolution()[1]);
        s.append("\n\tRequested envelope   : ").append(getRequestedEnvelope());
        s.append("\n\tRequested dimension  : ").append(getRequestedDim());
        Rectangle mt = getMatchingTiles();
        Rectangle ltr = getLevelTileRange();
        String matching = "x=" + mt.x + "-" + (mt.x + mt.width - 1) + ", y=" + mt.y + "-"
                + (mt.y + mt.height - 1);
        String level = "x=" + ltr.x + "-" + (ltr.width - 1) + ", y=" + ltr.y + "-"
                + (ltr.height - 1);
        s.append("\n\tMatching tiles       : ").append(matching).append(" out of ").append(level);
        s.append("\n\tTiled image size     : ").append(getTiledImageSize());
        s.append("\n\tResult dimension     : ").append(getResultDimensionInsideTiledImage());
        s.append("\n\tMosaiced dimension   : ").append(getMosaicLocation());
        s.append("\n\tResult envelope      : ").append(getResultEnvelope());
        s.append("\n]");
        return s.toString();
    }

    /**
     * @return the rasterId (as in SeRaster.getId()) for the raster in the raster dataset this query
     *         works upon
     */
    public Long getRasterId() {
        return rasterId;
    }

    public GeneralEnvelope getRequestedEnvelope() {
        return requestedEnvelope;
    }

    public Rectangle getRequestedDim() {
        return requestedDim;
    }

    public int getPyramidLevel() {
        return pyramidLevel;
    }

    public Rectangle getMatchingTiles() {
        return matchingTiles;
    }

    public GeneralEnvelope getResultEnvelope() {
        return resultEnvelope;
    }

    public Rectangle getResultDimensionInsideTiledImage() {
        return resultDimension;
    }

    void setRasterId(Long rasterId) {
        this.rasterId = rasterId;
    }

    void setPyramidLevel(int pyramidLevel) {
        this.pyramidLevel = pyramidLevel;
    }

    void setRequestedEnvelope(GeneralEnvelope requestedEnvelope) {
        this.requestedEnvelope = requestedEnvelope;
    }

    void setRequestedDim(Rectangle requestedDim) {
        this.requestedDim = requestedDim;
    }

    void setResultEnvelope(GeneralEnvelope resultEnvelope) {
        this.resultEnvelope = resultEnvelope;
    }

    void setMatchingTiles(Rectangle matchingTiles) {
        this.matchingTiles = matchingTiles;
    }

    void setResultDimensionInsideTiledImage(Rectangle resultDimension) {
        this.resultDimension = resultDimension;
    }

    void setMosaicLocation(Rectangle rasterMosaicLocation) {
        this.mosaicLocation = rasterMosaicLocation;
    }

    public Rectangle getMosaicLocation() {
        return mosaicLocation;
    }

    public void setResultImage(RenderedImage rasterImage) {
        this.resultImage = rasterImage;
//        if (rasterImage.getWidth() != tiledImageSize.width
//                || rasterImage.getHeight() != tiledImageSize.height) {
//            LOGGER.warning("Result image and expected dimensions don't match: image="
//                    + resultImage.getWidth() + "x" + resultImage.getHeight() + ", expected="
//                    + tiledImageSize.width + "x" + tiledImageSize.height);
//        }
    }

    public RenderedImage getResultImage() {
        return resultImage;
    }

    void setTiledImageSize(Rectangle tiledImageSize) {
        this.tiledImageSize = tiledImageSize;
    }

    public Rectangle getTiledImageSize() {
        return tiledImageSize;
    }

    void setResolution(double[] resolution) {
        this.resolution = resolution;
    }

    public double[] getResolution() {
        return resolution == null ? new double[] { -1, -1 } : resolution;
    }

    void setRasterIndex(int rasterN) {
        this.rasterIndex = rasterN;
    }

    public int getRasterIndex() {
        return rasterIndex;
    }

    void setLevelTileRange(Rectangle levelTileRange) {
        this.levelTileRange = levelTileRange;
    }

    public Rectangle getLevelTileRange() {
        return levelTileRange;
    }
}