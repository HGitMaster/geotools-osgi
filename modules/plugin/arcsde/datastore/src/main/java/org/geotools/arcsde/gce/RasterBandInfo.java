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

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.util.logging.Logger;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.gce.band.ArcSDERasterBandCopier;
import org.geotools.arcsde.gce.imageio.CompressionType;
import org.geotools.arcsde.gce.imageio.InterleaveType;
import org.geotools.arcsde.gce.imageio.InterpolationType;
import org.geotools.arcsde.gce.imageio.RasterCellType;

import com.esri.sde.sdk.client.SDEPoint;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeRasterBand;
import com.esri.sde.sdk.client.SeRasterBand.SeRasterBandColorMap;
import com.vividsolutions.jts.geom.Envelope;

/**
 * 
 * @author Gabriel Roldan
 */
public class RasterBandInfo {

    private final long bandId;

    private final int bandHeight;

    private final int bandWidth;

    private final String bandName;

    private final int bandNumber;

    private final boolean hasColorMap;

    private final IndexColorModel colorMap;

    private final CompressionType compressionType;

    private final Envelope bandExtent;

    private final RasterCellType cellType;

    private final long rasterColumnId;

    private final InterleaveType interleaveType;

    private final InterpolationType interpolationType;

    private final int maxPyramidLevel;

    private final boolean isSkipPyramidLevelOne;

    private final long rasterId;

    private final boolean hasStats;

    private final int tileWidth;

    private final int tileHeight;

    private final Double tileOrigin;

    private final ArcSDERasterBandCopier rasterBandCopier;

    private final double statsMin;

    private final double statsMax;

    public RasterBandInfo(SeRasterBand band) throws IOException {

        bandId = band.getId().longValue();
        bandNumber = band.getBandNumber();
        bandName = "Band " + bandNumber;

        rasterId = band.getRasterId().longValue();
        rasterColumnId = band.getRasterColumnId().longValue();

        bandHeight = band.getBandHeight();
        bandWidth = band.getBandWidth();
        hasColorMap = band.hasColorMap();
        if (hasColorMap) {
            // TODO: hold on on getting the color map until the blocking issue is resolved
            Logger.getLogger("org.geotools.arcsde.gce").warning(
                    "Skipping getting the color map for band " + band);
            colorMap = null;
            // SeRasterBandColorMap sdeColorMap;
            // try {
            // sdeColorMap = band.getColorMap();
            // } catch (SeException e) {
            // throw new ArcSdeException("Getting band's color map", e);
            // }
            // colorMap = RasterUtils.sdeColorMapToJavaColorModel(sdeColorMap);
        } else {
            colorMap = null;
        }
        compressionType = CompressionType.valueOf(band.getCompressionType());
        SeExtent extent = band.getExtent();
        bandExtent = new Envelope(extent.getMinX(), extent.getMaxX(), extent.getMinY(), extent
                .getMaxY());
        cellType = RasterCellType.valueOf(band.getPixelType());
        interleaveType = InterleaveType.valueOf(band.getInterleave());
        interpolationType = InterpolationType.valueOf(band.getInterpolation());
        maxPyramidLevel = band.getMaxLevel();
        isSkipPyramidLevelOne = band.skipLevelOne();
        hasStats = band.hasStats();
        if (hasStats) {
            try {
                statsMin = band.getStatsMin();
                statsMax = band.getStatsMax();
            } catch (SeException e) {
                throw new ArcSdeException(e);
            }
        } else {
            statsMin = java.lang.Double.NaN;
            statsMax = java.lang.Double.NaN;
        }
        tileWidth = band.getTileWidth();
        tileHeight = band.getTileHeight();
        SDEPoint tOrigin;
        try {
            tOrigin = band.getTileOrigin();
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
        tileOrigin = new Point2D.Double(tOrigin.getX(), tOrigin.getY());

        rasterBandCopier = ArcSDERasterBandCopier.getInstance(cellType, tileWidth, tileHeight);
    }

    public ArcSDERasterBandCopier getRasterBandCopier() {
        return rasterBandCopier;
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

    public boolean isHasColorMap() {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getBandName());
        sb.append(": ").append(getBandWidth()).append("x").append(getBandHeight()).append(" ");
        sb.append(getCellType()).append(".");
        sb.append(" Tiles: ").append(getTileWidth()).append("x").append(getTileHeight());
        sb.append(", ").append(getCompressionType());
        sb.append(", ").append(getInterpolationType());
        sb.append(", Color Map: ").append(isHasColorMap() ? "YES" : "NO");
        sb.append(", Max pyramid level: " + getMaxPyramidLevel()).append(
                isSkipPyramidLevelOne() ? " (Skips level one)" : "");
        return sb.toString();
    }

}