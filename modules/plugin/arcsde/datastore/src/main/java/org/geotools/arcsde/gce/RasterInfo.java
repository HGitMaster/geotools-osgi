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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geotools.arcsde.gce.imageio.ArcSDEPyramid;
import org.geotools.arcsde.gce.imageio.RasterCellType;
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
 * @version $Id: RasterInfo.java 32461 2009-02-10 21:16:29Z groldan $
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
    private ArcSDEPyramid pyramidInfo;

    private List<RasterBandInfo> bands;

    private CoordinateReferenceSystem coverageCrs;

    private GeneralEnvelope originalEnvelope;

    private GeneralGridRange originalGridRange;

    private List<GridSampleDimension> gridSampleDimensions;

    private int imageWidth;

    private int imageHeight;

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
    void setPyramidInfo(ArcSDEPyramid pyramidInfo) {
        this.pyramidInfo = pyramidInfo;
    }

    /**
     * @return the pyramidInfo
     */
    public ArcSDEPyramid getPyramidInfo() {
        return pyramidInfo;
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
        return getBands().size();
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    /**
     * @param bands
     *            the bands to set
     */
    void setBands(List<RasterBandInfo> bands) {
        this.bands = bands;
    }

    /**
     * @return the bands
     */
    public List<RasterBandInfo> getBands() {
        return new ArrayList<RasterBandInfo>(bands);
    }

    public RasterBandInfo getBand(int index) {
        return bands.get(index);
    }

    /**
     * @param coverageCrs
     *            the coverageCrs to set
     */
    public void setCoverageCrs(CoordinateReferenceSystem coverageCrs) {
        this.coverageCrs = coverageCrs;
    }

    /**
     * @return the coverageCrs
     */
    public CoordinateReferenceSystem getCoverageCrs() {
        return coverageCrs;
    }

    /**
     * @param originalGridRange
     *            the originalGridRange to set
     */
    public void setOriginalGridRange(GeneralGridRange originalGridRange) {
        this.originalGridRange = originalGridRange;
    }

    /**
     * @return the originalGridRange
     */
    public GeneralGridRange getOriginalGridRange() {
        return originalGridRange;
    }

    /**
     * @param originalEnvelope
     *            the originalEnvelope to set
     */
    public void setOriginalEnvelope(GeneralEnvelope originalEnvelope) {
        this.originalEnvelope = originalEnvelope;
    }

    /**
     * @return the originalEnvelope
     */
    public GeneralEnvelope getOriginalEnvelope() {
        return originalEnvelope;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ArcSDE Raster: " + getRasterTable());
        sb.append(", raster columns: ").append(Arrays.asList(getRasterColumns()));
        sb.append(", Num bands: ").append(getNumBands());
        sb.append(", Dimension: ").append(getImageWidth()).append("x").append(getImageHeight());
        for (RasterBandInfo band : getBands()) {
            sb.append("\n\t").append(band.toString());
        }
        return sb.toString();
    }

}