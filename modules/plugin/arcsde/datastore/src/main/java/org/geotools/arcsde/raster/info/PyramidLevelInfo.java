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
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Represents one level in an ArcSDE pyramid. Holds information about a given pyramid level, like
 * resolution, x/y offsets, number of tiles high/wide, total pixel size and total envelope covered
 * by this level.
 * 
 * @author sfarber
 * 
 */
final class PyramidLevelInfo {
    private int pyramidLevel, xTiles, yTiles;

    Point2D extentOffset;

    Point imageOffset;

    private double xRes, yRes;

    private ReferencedEnvelope envelope;

    public Dimension size;

    /**
     * 
     * @param level
     *            the level index
     * @param extent
     *            the geographical extent the level covers
     * @param imgOffset
     *            the offset of the image at this level
     * @param extOffset
     *            the offset of the image extent at this level
     * @param numTilesWide
     * @param numTilesHigh
     * @param levelSize
     *            the size of the actual image inside the tiled pixel range
     */
    PyramidLevelInfo(int level, ReferencedEnvelope extent, Point imgOffset, Point2D extOffset,
            int numTilesWide, int numTilesHigh, Dimension levelSize) {
        this.pyramidLevel = level;
        this.xRes = extent.getWidth() / levelSize.width;
        this.yRes = extent.getHeight() / levelSize.height;
        this.envelope = extent;
        this.imageOffset = imgOffset;
        this.extentOffset = extOffset;
        this.xTiles = numTilesWide;
        this.yTiles = numTilesHigh;
        this.size = levelSize;
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
        return imageOffset.x;
    }

    /**
     * @return DOCUMENT ME!!!
     */
    public int getYOffset() {
        return imageOffset.y;
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
     * The envelope covering the image grid range inside fully tiled image at this pyramid level
     * 
     * @return The geographical area covered by the {@link #getImageRange() grid range} of the
     *         raster at this pyramid level
     */
    public ReferencedEnvelope getImageEnvelope() {
        final double deltaX = extentOffset.getX();
        final double deltaY = extentOffset.getY();
        double minx = this.envelope.getMinX() - deltaX;
        double miny = this.envelope.getMinY() - deltaY;
        double maxx = minx + this.envelope.getWidth();
        double maxy = miny + this.envelope.getHeight();

        CoordinateReferenceSystem crs = this.envelope.getCoordinateReferenceSystem();
        ReferencedEnvelope imageExtent = new ReferencedEnvelope(minx, maxx, miny, maxy, crs);

        return imageExtent;
    }

    /**
     * @return The total number of pixels in the image at this level as whole tiles
     */
    public Dimension getSize() {
        return size;
    }

    /**
     * The rectangle covering the actual raster data inside the tiled space
     * 
     * @return
     */
    public Rectangle getImageRange() {
        final int offsetX = getXOffset();
        final int offsetY = getYOffset();

        /*
         * get the range of actual data pixels in this pyramid level in pixel space, offset and
         * trailing no data pixels to fill up the tile space do not count
         */
        final Rectangle levelRange = new Rectangle(offsetX, offsetY, size.width, size.height);
        return levelRange;
    }

    @Override
    public String toString() {
        return "[level: " + pyramidLevel + " size: " + size.width + "x" + size.height + "  xRes: "
                + xRes + "  yRes: " + yRes + "  xOffset: " + getXOffset() + "  yOffset: "
                + getYOffset() + "  extent: " + envelope.getMinX() + "," + envelope.getMinY() + " "
                + envelope.getMaxX() + "," + envelope.getMaxY() + "  tilesWide: " + xTiles
                + "  tilesHigh: " + yTiles + "]";
    }
}