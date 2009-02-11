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

import org.geotools.geometry.jts.ReferencedEnvelope;

/**
 * Represents one level in an ArcSDE pyramid. Holds information about a given pyramid level, like
 * resolution, x/y offsets, number of tiles high/wide, total pixel size and total envelope covered
 * by this level.
 * 
 * @author sfarber
 * 
 */
class PyramidLevelInfo {
    private int pyramidLevel, xOffset, yOffset, xTiles, yTiles;

    private double xRes, yRes;

    private ReferencedEnvelope envelope;

    public Dimension size;

    /**
     * 
     * @param level
     *            the level index
     * @param extent
     *            the geographical extent the level covers
     * @param xOffset
     *            the offset of the image at this level on the x axis, >= 0
     * @param yOffset
     *            the offset of the image at this level on the y axis, >= 0
     * @param numTilesWide
     * @param numTilesHigh
     * @param size
     *            the dimensions of the level
     */
    PyramidLevelInfo(int level, ReferencedEnvelope extent, int xOffset, int yOffset,
            int numTilesWide, int numTilesHigh, Dimension size) {
        this.pyramidLevel = level;
        this.xRes = (extent.getMaxX() - extent.getMinX()) / size.width;
        this.yRes = (extent.getMaxY() - extent.getMinY()) / size.height;
        this.envelope = extent;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xTiles = numTilesWide;
        this.yTiles = numTilesHigh;
        this.size = size;
    }

    /**
     * @return Which level in the pyramid this object represents
     */
    public int getLevel() {
        return pyramidLevel;
    }

    /**
     * @return The X and Y resolution in units/pixel for pixels at this level
     */
    public double getXRes() {
        return xRes;
    }

    /**
     * @return The X and Y resolution in units/pixel for pixels at this level
     */
    public double getYRes() {
        return yRes;
    }

    /**
     * @return DOCUMENT ME!!!
     */
    public int getXOffset() {
        return xOffset;
    }

    /**
     * @return DOCUMENT ME!!!
     */
    public int getYOffset() {
        return yOffset;
    }

    /**
     * @return The total number of tiles covering the width of this level
     */
    public int getNumTilesWide() {
        return xTiles;
    }

    /**
     * @return The total number of tiles covering the height of this level
     */
    public int getNumTilesHigh() {
        return yTiles;
    }

    /**
     * @return The geographical area covered by this level of the pyramid
     */
    public ReferencedEnvelope getEnvelope() {
        return new ReferencedEnvelope(this.envelope);
    }

    /**
     * @return The total number of pixels in the image at this level
     */
    public Dimension getSize() {
        return size;
    }
    
    public Rectangle getRange(){
        final int offsetX = getXOffset();
        final int offsetY = getYOffset();

        /*
         * get the range of actual data pixels in this pyramid level in pixel space, offset and
         * trailing no data pixels to fill up the tile space do not count
         */
        final Rectangle levelRange = new Rectangle(offsetX, offsetY, size.width,
                size.height);
        return levelRange;
    }

    @Override
    public String toString() {
        return "[level: " + pyramidLevel + " size: " + size.width + "x" + size.height + "  xRes: "
                + xRes + "  yRes: " + yRes + "  xOffset: " + xOffset + "  yOffset: " + yOffset
                + "  extent: " + envelope + "  tilesWide: " + xTiles + "  tilesHigh: " + yTiles
                + "]";
    }
}