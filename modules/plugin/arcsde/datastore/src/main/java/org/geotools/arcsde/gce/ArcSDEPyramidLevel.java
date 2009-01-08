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

import java.awt.Dimension;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SDEPoint;
import com.esri.sde.sdk.client.SeExtent;

/**
 * Represents one level in an ArcSDE pyramid. Holds information about a given
 * pyramid level, like resolution, x/y offsets, number of tiles high/wide, total
 * pixel size and total envelope covered by this level.
 * 
 * @author sfarber
 * 
 */
public class ArcSDEPyramidLevel {
    private int pyramidLevel, xOffset, yOffset, xTiles, yTiles;

    private double xRes, yRes;

    private ReferencedEnvelope envelope;

    public Dimension size;

    public ArcSDEPyramidLevel(int level, SeExtent extent, CoordinateReferenceSystem crs,
            SDEPoint offset, int xTiles, int yTiles, Dimension size) {
        this.pyramidLevel = level;
        this.xRes = (extent.getMaxX() - extent.getMinX()) / size.width;
        this.yRes = (extent.getMaxY() - extent.getMinY()) / size.height;
        this.envelope = new ReferencedEnvelope(extent.getMinX(), extent.getMaxX(),
                extent.getMinY(), extent.getMaxY(), crs);
        if (offset != null) {
            this.xOffset = (int) offset.getX();
            this.yOffset = (int) offset.getY();
        }
        this.xTiles = xTiles;
        this.yTiles = yTiles;
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

    @Override
    public String toString() {
        return "[level: " + pyramidLevel + "  xRes: " + xRes + "  yRes: " + yRes + "  xOffset: "
                + xOffset + "  yOffset: " + yOffset + "  extent: " + envelope + "  tilesWide: "
                + xTiles + "  tilesHigh: " + yTiles + "]";
    }
}