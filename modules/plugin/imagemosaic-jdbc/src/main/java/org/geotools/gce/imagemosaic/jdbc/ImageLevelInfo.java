/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.gce.imagemosaic.jdbc;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Java Bean for pyramid level information. For each pyramid and the original
 * image, there is one ImageLevelInfo object
 * 
 * @author mcr
 * 
 */
class ImageLevelInfo implements Comparable<ImageLevelInfo> {

	/**
	 * Flag if ImageIO.read(InputStream in) does not return a null pointer
	 */
	private boolean canImageIOReadFromInputStream;

	/**
	 * The Coordinate Reference System stored in the sql database (if supported)
	 */
	private CoordinateReferenceSystem crs;

	/**
	 * The Spatial Reference System Id if the used database supports it
	 */
	private Integer srsId;

	/**
	 * The name of the coverage, stored in the master table
	 */
	private String coverageName;

	/**
	 * minimum X value of the covered extent
	 */
	private Double extentMinX;

	/**
	 * minimum Y value of the covered extent
	 */
	private Double extentMinY;

	/**
	 * maximum X value of the covered extent
	 */
	private Double extentMaxX;

	/**
	 * maximu Y value of the covered extent
	 */
	private Double extentMaxY;

	/**
	 * resolution of the x axis
	 */
	private Double resX;

	/**
	 * resolution of the y axis
	 */
	private Double resY;

	/**
	 * table name where to find the images
	 */
	private String tileTableName;

	/**
	 * table name where to find georeferencing information
	 */
	private String spatialTableName;

	/**
	 * the number of entries in the spatial table
	 */
	private Integer countFeature;

	/**
	 * the number of entries in the tiles table
	 */
	private Integer countTiles;

	/**
	 * storing resolutionX and resolution Y as array, for convinience
	 */
	private double[] resolution = null;

	/**
	 * storing the extent as envelope, for convinience
	 */
	private Envelope envelope = null;

	String getCoverageName() {
		return coverageName;
	}

	void setCoverageName(String coverageName) {
		this.coverageName = coverageName;
	}

	Double getExtentMaxX() {
		return extentMaxX;
	}

	void setExtentMaxX(Double extentMaxX) {
		this.extentMaxX = extentMaxX;
		envelope = null;
	}

	Double getExtentMaxY() {
		return extentMaxY;
	}

	void setExtentMaxY(Double extentMaxY) {
		this.extentMaxY = extentMaxY;
		envelope = null;
	}

	Double getExtentMinX() {
		return extentMinX;
	}

	void setExtentMinX(Double extentMinX) {
		this.extentMinX = extentMinX;
		envelope = null;
	}

	Double getExtentMinY() {
		return extentMinY;
	}

	void setExtentMinY(Double extentMinY) {
		this.extentMinY = extentMinY;
		envelope = null;
	}

	Double getResX() {
		return resX;
	}

	void setResX(Double resX) {
		this.resX = resX;
		resolution = null;
	}

	Double getResY() {
		return resY;
	}

	void setResY(Double resY) {
		this.resY = resY;
		resolution = null;
	}

	String getSpatialTableName() {
		return spatialTableName;
	}

	void setSpatialTableName(String spatialTableName) {
		this.spatialTableName = spatialTableName;
	}

	String getTileTableName() {
		return tileTableName;
	}

	void setTileTableName(String tileTableName) {
		this.tileTableName = tileTableName;
	}

	@Override
	public String toString() {
		return "Coverage: " + getCoverageName() + ":" + getSpatialTableName()
				+ ":" + getTileTableName();
	}

	public int compareTo(ImageLevelInfo other) {
		int res = 0;

		if ((res = getCoverageName().compareTo(other.getCoverageName())) != 0) {
			return res;
		}

		if ((res = getResX().compareTo(other.getResX())) != 0) {
			return res;
		}

		if ((res = getResY().compareTo(other.getResY())) != 0) {
			return res;
		}

		return 0;
	}

	double[] getResolution() {
		if (resolution != null) {
			return resolution;
		}

		resolution = new double[2];

		if (getResX() != null) {
			resolution[0] = getResX().doubleValue();
		}

		if (getResY() != null) {
			resolution[1] = getResY().doubleValue();
		}

		return resolution;
	}

	Envelope getEnvelope() {
		if (envelope != null) {
			return envelope;
		}

		if ((getExtentMaxX() == null) || (getExtentMaxY() == null)
				|| (getExtentMinX() == null) || (getExtentMinY() == null)) {
			return null;
		}

		envelope = new Envelope(getExtentMinX().doubleValue(), getExtentMaxX()
				.doubleValue(), getExtentMinY().doubleValue(), getExtentMaxY()
				.doubleValue());

		return envelope;
	}

	Integer getCountFeature() {
		return countFeature;
	}

	void setCountFeature(Integer countFeature) {
		this.countFeature = countFeature;
	}

	Integer getCountTiles() {
		return countTiles;
	}

	void setCountTiles(Integer countTiles) {
		this.countTiles = countTiles;
	}

	CoordinateReferenceSystem getCrs() {
		return crs;
	}

	void setCrs(CoordinateReferenceSystem crs) {
		this.crs = crs;
	}

	boolean calculateResolutionNeeded() {
		return (getResX() == null) || (getResY() == null);
	}

	boolean calculateExtentsNeeded() {
		return (getExtentMaxX() == null) || (getExtentMaxY() == null)
				|| (getExtentMinX() == null) || (getExtentMinY() == null);
	}

	String infoString() {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintWriter w = new PrintWriter(bout);
		w.print("Coveragename: ");
		w.println(getCoverageName());

		if (getCrs() != null) {
			w.print("CoordinateRefernceSystem: ");
			w.println(getCrs().getName());
		}

		if (getSrsId() != null) {
			w.print("SRS_ID: ");
			w.println(getSrsId());
		}

		w.print("Envelope: ");
		w.println(getEnvelope());

		w.print("Resolution X: ");
		w.println(getResX());

		w.print("Resolution Y: ");
		w.println(getResY());

		w.print("Tiletable: ");
		w.print(getTileTableName());

		if (getCountTiles() != null) {
			w.print(" #tiles: ");
			w.println(getCountTiles());
		}

		w.print("Spatialtable: ");
		w.print(getSpatialTableName());

		if (getCountFeature() != null) {
			w.print(" #geometries: ");
			w.println(getCountFeature());
		}

		w.close();

		return bout.toString();
	}

	Integer getSrsId() {
		return srsId;
	}

	void setSrsId(Integer srsId) {
		this.srsId = srsId;
	}

	boolean isImplementedAsTableSplit() {
		return getSpatialTableName().equals(getTileTableName()) == false;
	}

	public boolean getCanImageIOReadFromInputStream() {
		return canImageIOReadFromInputStream;
	}

	public void setCanImageIOReadFromInputStream(
			boolean canImageIOReadFromInputStream) {
		this.canImageIOReadFromInputStream = canImageIOReadFromInputStream;
	}
}
