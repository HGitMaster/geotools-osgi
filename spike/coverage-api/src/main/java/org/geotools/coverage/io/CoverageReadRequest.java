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
package org.geotools.coverage.io;

import java.awt.Rectangle;
import java.util.Set;
import java.util.SortedSet;

import org.geotools.coverage.io.range.RangeType;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.temporal.TemporalGeometricPrimitive;

/**
 * Captures element of a request to read data from an underlying coverage.
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public interface CoverageReadRequest extends CoverageRequest {

	/**
	 * @return
	 * @uml.property  name="rangeSubset"
	 */
	public RangeType getRangeSubset();

	public void setDomainSubset(final Rectangle rasterArea,
			final MathTransform2D gridToWorldTrasform,
			final CoordinateReferenceSystem crs)
			throws MismatchedDimensionException, TransformException;

	/**
	 * Set the domain subset elements.
	 * @param anchor  
	 * @param  value
	 * @uml.property  name="domainSubset"
	 */
	public void setDomainSubset(final Rectangle rasterArea,
			final BoundingBox worldArea, PixelInCell anchor);

	/**
	 * Set the domain subset elements.
	 * 
	 * <p>
	 * Note that this is a convenience method for requesting a subset of the original coverage at full resolution.
	 * @param rasterArea
	 * @param worldArea
	 */
	public void setDomainSubset(final BoundingBox worldArea);

	/**
	 * Set the domain subset elements. <p> Note that this is a convenience method for requesting a subset of the original coverage at full resolution.
	 * @param  value
	 * @uml.property  name="domainSubset"
	 */
	public void setDomainSubset(final Envelope worldArea);

	/**
	 * Set the domain subset elements.
	 * 
	 * @param rasterArea
	 * @param worldArea
	 */
	public void setDomainSubset(final Rectangle rasterArea,
			final BoundingBox worldArea);

	/**
	 * Set the domain subset elements.
	 * @param  value
	 * @uml.property  name="domainSubset"
	 */
	public void setDomainSubset(final Rectangle rasterArea,
			final Envelope worldArea);

	/**
	 * Set the domain subset elements.
	 * @param  value
	 * @uml.property  name="domainSubset"
	 */
	public void setDomainSubset(final Rectangle rasterArea,
			final Envelope worldArea, PixelInCell anchor);

	/**
	 * Set the range subset we are requesting. <p> Note that a null  {@link RangeType}  means get everything.
	 * @param  value
	 * @uml.property  name="rangeSubset"
	 */
	public void setRangeSubset(final RangeType value);

	/**
	 * @return
	 * @uml.property  name="verticalSubset"
	 */
	public Set<Envelope> getVerticalSubset();

	/**
	 * @param  verticalSubset
	 * @uml.property  name="verticalSubset"
	 */
	public void setVerticalSubset(
	        Set<Envelope> verticalSubset);

	/**
	 * @return
	 * @uml.property  name="temporalSubset"
	 */
	public SortedSet<TemporalGeometricPrimitive> getTemporalSubset();

	/**
	 * @param  temporalSubset
	 * @uml.property  name="temporalSubset"
	 */
	public void setTemporalSubset(
			SortedSet<TemporalGeometricPrimitive> temporalSubset);

	/**
	 * @return
	 * @uml.property  name="rasterArea"
	 */
	public Rectangle getRasterArea();

	/**
	 * @return
	 * @uml.property  name="geographicArea"
	 */
	public BoundingBox getGeographicArea();

	/**
	 * @return
	 * @uml.property  name="gridToWorldTransform"
	 */
	public MathTransform2D getGridToWorldTransform();

}