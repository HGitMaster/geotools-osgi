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

/**
 * Describes the capabilities of this {@link CoverageSource}
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public class CoverageSourceCapabilities {
	private final boolean readSubsamplingSupported;
	
	private final boolean readRangeSubsettingSupported;

	private final boolean readHorizontalDomainSubsettingSupported;
	
	private final boolean reprojectionSupported;

	public CoverageSourceCapabilities(boolean readSubsamplingSupported,
			boolean readRangeSubsettingSupported,
			boolean readHorizontalDomainSubsettingSupported,
			boolean reprojectionSupported) {
		this.readSubsamplingSupported = readSubsamplingSupported;
		this.readRangeSubsettingSupported = readRangeSubsettingSupported;
		this.readHorizontalDomainSubsettingSupported = readHorizontalDomainSubsettingSupported;
		this.reprojectionSupported = reprojectionSupported;
	}

	public boolean isReadSubsamplingSupported() {
		return readSubsamplingSupported;
	}

	public boolean isReadRangeSubsettingSupported() {
		return readRangeSubsettingSupported;
	}

	public boolean isReadHorizontalDomainSubsettingSupported() {
		return readHorizontalDomainSubsettingSupported;
	}

	public boolean isReprojectionSupported() {
		return reprojectionSupported;
	}
	
	
}
