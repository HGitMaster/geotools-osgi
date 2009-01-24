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

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import org.geotools.arcsde.gce.imageio.ArcSDEPyramid;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * 
 * @author Gabriel Roldan
 */
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

    private BufferedImage sampleImage;

    private Point levelZeroPRP;

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
    String getRasterTable() {
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
    String[] getRasterColumns() {
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
    ArcSDEPyramid getPyramidInfo() {
        return pyramidInfo;
    }

    public GridSampleDimension[] getGridSampleDimensions() {
        return gridSampleDimensions.toArray(new GridSampleDimension[gridSampleDimensions.size()]);
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

    public void setGridSampleDimensions(List<GridSampleDimension> gridSampleDimensions) {
        this.gridSampleDimensions = gridSampleDimensions;
    }

    /**
     * @param sampleImage
     *            the sampleImage to set
     */
    void setSampleImage(BufferedImage sampleImage) {
        this.sampleImage = sampleImage;
    }

    /**
     * @return the sampleImage
     */
    BufferedImage getSampleImage() {
        return sampleImage;
    }

    /**
     * @param levelZeroPRP
     *            the levelZeroPRP to set
     */
    void setLevelZeroPRP(Point levelZeroPRP) {
        this.levelZeroPRP = levelZeroPRP;
    }

    /**
     * @return the levelZeroPRP
     */
    Point getLevelZeroPRP() {
        return levelZeroPRP;
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
    List<RasterBandInfo> getBands() {
        return bands;
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