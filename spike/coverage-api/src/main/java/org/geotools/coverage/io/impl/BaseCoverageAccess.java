package org.geotools.coverage.io.impl;

import org.geotools.coverage.io.CoverageAccess;
import org.geotools.coverage.io.Driver;

/**
 * BaseCoverageAccess class that can be used as a starting point for
 * implementing your own coverage classes.
 * <p>
 * This implementation provides 
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 */
public abstract class BaseCoverageAccess implements CoverageAccess{
	/**
	 * Driver used to create this CoverageAccess.
	 */
	private final Driver driver;

	protected BaseCoverageAccess( Driver driver ){
		this.driver = driver;
	}
	
	public Driver getDriver() {
		return driver;
	}
	

}
