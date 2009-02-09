/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.awt.geom.Point2D.Double;
import java.awt.image.IndexColorModel;
import java.io.IOException;

import org.geotools.arcsde.gce.imageio.CompressionType;
import org.geotools.arcsde.gce.imageio.InterleaveType;
import org.geotools.arcsde.gce.imageio.InterpolationType;
import org.geotools.arcsde.gce.imageio.RasterCellType;

import com.vividsolutions.jts.geom.Envelope;

/**
 * 
 * @author Gabriel Roldan
 */
public class RasterBandInfo {

    long bandId;

    int bandHeight;

    int bandWidth;

    String bandName;

    int bandNumber;

    boolean hasColorMap;

    IndexColorModel colorMap;

    CompressionType compressionType;

    Envelope bandExtent;

    RasterCellType cellType;

    long rasterColumnId;

    InterleaveType interleaveType;

    InterpolationType interpolationType;

    int maxPyramidLevel;

    boolean isSkipPyramidLevelOne;

    long rasterId;

    boolean hasStats;

    int tileWidth;

    int tileHeight;

    Double tileOrigin;

    double statsMin;

    double statsMax;

    double statsMean;

    double statsStdDev;

    public RasterBandInfo() throws IOException {
        // do nothing
    }

    /**
     * @return the ArcSDE identifier for the band
     */
    public long getBandId() {
        return bandId;
    }

    public int getBandHeight() {
        return bandHeight;
    }

    public int getBandWidth() {
        return bandWidth;
    }

    public String getBandName() {
        return bandName;
    }

    public int getBandNumber() {
        return bandNumber;
    }

    public boolean isColorMapped() {
        return hasColorMap;
    }

    public CompressionType getCompressionType() {
        return compressionType;
    }

    public Envelope getBandExtent() {
        return bandExtent;
    }

    public RasterCellType getCellType() {
        return cellType;
    }

    public long getRasterColumnId() {
        return rasterColumnId;
    }

    public InterleaveType getInterleaveType() {
        return interleaveType;
    }

    public InterpolationType getInterpolationType() {
        return interpolationType;
    }

    public int getMaxPyramidLevel() {
        return maxPyramidLevel;
    }

    public boolean isSkipPyramidLevelOne() {
        return isSkipPyramidLevelOne;
    }

    public long getRasterId() {
        return rasterId;
    }

    public boolean isHasStats() {
        return hasStats;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public Double getTileOrigin() {
        return tileOrigin;
    }

    public IndexColorModel getColorMap() {
        return colorMap;
    }

    public double getStatsMin() {
        return statsMin;
    }

    public double getStatsMax() {
        return statsMax;
    }

    public double getStatsMean() {
        return statsMean;
    }

    public double getStatsStdDev() {
        return statsStdDev;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getBandName());
        sb.append(": ").append(getBandWidth()).append("x").append(getBandHeight()).append(" ");
        sb.append(getCellType()).append(".");
        sb.append(" Tiles: ").append(getTileWidth()).append("x").append(getTileHeight());
        sb.append(", Tile origin: ").append((int) getTileOrigin().x).append(",").append(
                (int) getTileOrigin().y);
        sb.append(", ").append(getCompressionType());
        sb.append(", ").append(getInterpolationType());
        sb.append(", Color Map: ").append(isColorMapped() ? "YES" : "NO");
        sb.append(", Max pyramid level: " + getMaxPyramidLevel()).append(
                isSkipPyramidLevelOne() ? " (Skips level one)" : "");
        if (hasStats) {
            sb.append(", Statistics: min=").append(getStatsMin()).append(" max=").append(
                    getStatsMax()).append(" mean=").append(getStatsMean()).append(" stddev=")
                    .append(getStatsStdDev());
        }
        return sb.toString();
    }

}