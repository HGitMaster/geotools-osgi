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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageTypeSpecifier;

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.operation.transform.LinearTransform1D;
import org.geotools.util.NumberRange;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.5.4
 * @version $Id: RasterInfo.java 32473 2009-02-11 21:28:44Z groldan $
 * @source $URL$
 */
@SuppressWarnings( { "nls", "deprecation" })
class RasterInfo {

    /** The name of the raster table we're pulling images from in this reader * */
    private String rasterTable = null;

    /**
     * raster column names on this raster. If there's more than one raster column (is this
     * possible?) then we just use the first one.
     */
    private String[] rasterColumns;

    /** Array holding information on each level of the pyramid in this raster. * */
    private List<PyramidInfo> subRasterInfo;

    /**
     * The original (ie, pyramid level zero) envelope for the whole raster dataset
     */
    private GeneralEnvelope originalEnvelope;

    private GeneralGridRange originalGridRange;

    private List<GridSampleDimension> gridSampleDimensions;

    private ImageTypeSpecifier renderedImageSpec;

    /**
     * @param rasterTable
     *            the rasterTable to set
     */
    void setRasterTable(String rasterTable) {
        this.rasterTable = rasterTable;
    }

    /**
     * @return the raster table name
     */
    public String getRasterTable() {
        return rasterTable;
    }

    /**
     * @param rasterColumns
     *            the rasterColumns to set
     */
    void setRasterColumns(String[] rasterColumns) {
        this.rasterColumns = rasterColumns;
    }

    /**
     * @return the raster column names
     */
    public String[] getRasterColumns() {
        return rasterColumns;
    }

    /**
     * @param pyramidInfo
     *            the pyramidInfo to set
     */
    void setPyramidInfo(List<PyramidInfo> pyramidInfo) {
        this.subRasterInfo = pyramidInfo;
    }

    public GridSampleDimension[] getGridSampleDimensions() {
        if (gridSampleDimensions == null) {
            synchronized (this) {
                if (gridSampleDimensions == null) {
                    gridSampleDimensions = buildSampleDimensions();
                }
            }
        }
        return gridSampleDimensions.toArray(new GridSampleDimension[getNumBands()]);
    }

    private List<GridSampleDimension> buildSampleDimensions() {
        final int numBands = getNumBands();
        List<GridSampleDimension> dimensions = new ArrayList<GridSampleDimension>(numBands);

        final Color[] RGB = { Color.RED, Color.GREEN, Color.BLUE };
        final String[] RGBCatNames = { "red", "green", "blue" };

        List<RasterBandInfo> bands = subRasterInfo.get(0).getBands();

        for (RasterBandInfo band : bands) {
            final int bandNumber = band.getBandNumber();
            final RasterCellType cellType = band.getCellType();
            String bandName = band.getBandName();

            final NumberRange<?> sampleValueRange = cellType.getSampleValueRange();

            final Color minColor = Color.BLACK;
            String catName;
            final Color maxColor;
            switch (numBands) {
            case 3:
                maxColor = RGB[bandNumber - 1];
                catName = RGBCatNames[bandNumber - 1];
                break;
            default:
                maxColor = Color.WHITE;
                catName = bandName;
            }
            final Color[] colorRange = { minColor, maxColor };
            Category bandCat = new Category(catName, colorRange, sampleValueRange,
                    LinearTransform1D.IDENTITY).geophysics(true);
            Category[] categories = { bandCat };
            // if (band.isHasStats()) {
            // Category catMin = new Category("Min", null, band.getStatsMin()).geophysics(true);
            // Category catMax = new Category("Max", null, band.getStatsMin()).geophysics(true);
            // Category catMean = new Category("Mean", null, band.getStatsMin()).geophysics(true);
            // Category catStdDev = new Category("StdDev", null, band.getStatsMin())
            // .geophysics(true);
            // categories = new Category[] { bandCat, catMin, catMax, catMean, catStdDev };
            // }
            GridSampleDimension sampleDim = new GridSampleDimension(bandName, categories, null)
                    .geophysics(true);

            dimensions.add(sampleDim);
        }
        return dimensions;
    }

    public int getNumBands() {
        return subRasterInfo.get(0).getNumBands();
    }

    public int getImageWidth() {
        final GeneralGridRange originalGridRange = getOriginalGridRange();
        final int width = originalGridRange.getSpan(0);
        return width;
    }

    public int getImageHeight() {
        final GeneralGridRange originalGridRange = getOriginalGridRange();
        final int height = originalGridRange.getSpan(1);
        return height;
    }

    /**
     * @return the coverageCrs
     */
    public CoordinateReferenceSystem getCoverageCrs() {
        return subRasterInfo.get(0).getCoordinateReferenceSystem();
    }

    /**
     * @return the originalGridRange for the whole raster dataset
     */
    public GeneralGridRange getOriginalGridRange() {
        if (originalGridRange == null) {
            Rectangle range = null;
            for (PyramidInfo pyramid : subRasterInfo) {
                Rectangle levelZeroRange = pyramid.getPyramidLevel(0).getImageRange();
                if (range == null) {
                    range = levelZeroRange;
                } else {
                    range.add(levelZeroRange);
                }
            }
            originalGridRange = new GeneralGridRange(range);
        }
        return originalGridRange;
    }

    /**
     * @return the originalEnvelope
     */
    public GeneralEnvelope getOriginalEnvelope() {
        if (originalEnvelope == null) {
            GeneralEnvelope env = null;
            for (PyramidInfo raster : subRasterInfo) {
                GeneralEnvelope rasterEnvelope = raster.getOriginalEnvelope();
                if (env == null) {
                    env = new GeneralEnvelope(rasterEnvelope);
                } else {
                    env.add(rasterEnvelope);
                }
            }
            originalEnvelope = env;
        }
        return originalEnvelope;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ArcSDE Raster: " + getRasterTable());
        sb.append(", raster columns: ").append(Arrays.asList(getRasterColumns()));
        sb.append(", Num bands: ").append(getNumBands());
        sb.append(", Dimension: ").append(getImageWidth()).append("x").append(getImageHeight());
        for (PyramidInfo pyramid : subRasterInfo) {
            sb.append("\n\t").append(pyramid.toString());
        }
        return sb.toString();
    }

    public int getRasterCount() {
        return subRasterInfo.size();
    }

    public RasterBandInfo getBand(final int rasterIndex, final int bandIndex) {
        PyramidInfo rasterInfo = getRasterInfo(rasterIndex);
        return rasterInfo.getBand(bandIndex);
    }

    public int getNumPyramidLevels(final int rasterIndex) {
        PyramidInfo rasterInfo = getRasterInfo(rasterIndex);
        return rasterInfo.getNumLevels();
    }

    public GeneralEnvelope getEnvelope(final int rasterIndex, final int pyramidLevel) {
        PyramidLevelInfo level = getLevel(rasterIndex, pyramidLevel);
        return new GeneralEnvelope(level.getEnvelope());
    }

    public Rectangle getGridRange(final int rasterIndex, final int pyramidLevel) {
        PyramidLevelInfo level = getLevel(rasterIndex, pyramidLevel);
        Rectangle levelRange = level.getImageRange();
        return levelRange;
    }

    public int getNumTilesWide(int rasterIndex, int pyramidLevel) {
        PyramidLevelInfo level = getLevel(rasterIndex, pyramidLevel);
        return level.getNumTilesWide();
    }

    public int getNumTilesHigh(int rasterIndex, int pyramidLevel) {
        PyramidLevelInfo level = getLevel(rasterIndex, pyramidLevel);
        return level.getNumTilesHigh();
    }

    public Dimension getTileDimension(int rasterIndex) {
        PyramidInfo rasterInfo = getRasterInfo(rasterIndex);
        return rasterInfo.getTileDimension();
    }

    private PyramidLevelInfo getLevel(int rasterIndex, int pyramidLevel) {
        PyramidInfo rasterInfo = getRasterInfo(rasterIndex);
        PyramidLevelInfo level = rasterInfo.getPyramidLevel(pyramidLevel);
        return level;
    }

    private PyramidInfo getRasterInfo(int rasterIndex) {
        PyramidInfo rasterInfo = subRasterInfo.get(rasterIndex);
        return rasterInfo;
    }

    public ImageTypeSpecifier getRenderedImageSpec() {
        if (this.renderedImageSpec == null) {
            synchronized (this) {
                if (this.renderedImageSpec == null) {
                    this.renderedImageSpec = RasterUtils.createFullImageTypeSpecifier(this);
                }
            }
        }
        return this.renderedImageSpec;
    }

    public IndexColorModel getColorMap() {
        final RasterBandInfo bandOne = getBand(0, 0);
        return bandOne.getColorMap();
    }

    boolean isColorMapped() {
        final RasterBandInfo bandOne = getBand(0, 0);
        return bandOne.isColorMapped();
    }

    public RasterCellType getCellType() {
        return getBand(0, 0).getCellType();
    }
}
