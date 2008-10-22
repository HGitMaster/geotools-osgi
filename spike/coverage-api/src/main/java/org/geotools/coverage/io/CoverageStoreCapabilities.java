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
 * Describes the capabilities of this {@link CoverageStore}
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public class CoverageStoreCapabilities extends CoverageSourceCapabilities {

	private final boolean writeHorizontalDomainSubsettingSupported;
	private final boolean writeRangeSubsettingSupported;
	private final boolean writeSubsamplingSupported;
	
	public CoverageStoreCapabilities(boolean readSubsamplingSupported,
			boolean readRangeSubsettingSupported,
			boolean readHorizontalDomainSubsettingSupported,
			boolean reprojectionSupported,
			boolean writeHorizontalDomainSubsettingSupported,
			boolean writeRangeSubsettingSupported,
			boolean writeSubsamplingSupported) {
		super(readSubsamplingSupported, readRangeSubsettingSupported,
				readHorizontalDomainSubsettingSupported, reprojectionSupported);
		this.writeHorizontalDomainSubsettingSupported = writeHorizontalDomainSubsettingSupported;
		this.writeRangeSubsettingSupported = writeRangeSubsettingSupported;
		this.writeSubsamplingSupported = writeSubsamplingSupported;
	}

	public boolean isWriteHorizontalDomainSubsettingSupported() {
		return writeHorizontalDomainSubsettingSupported;
	}

	public boolean isWriteRangeSubsettingSupported() {
		return writeRangeSubsettingSupported;
	}

	public boolean isWriteSubsamplingSupported() {
		return writeSubsamplingSupported;
	}
	
	

}
