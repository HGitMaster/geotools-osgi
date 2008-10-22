/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.feature.iso;

import java.util.Set;

import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

/**
 * Helper methods for dealing with Type restrictions represented by Filters
 * 
 * @author Gabriel Roldan, Axios Engineering
 */
public class Restrictions {

	/**
	 * Creates a Filter that enforces the length of value to be equal to the
	 * declared length.
	 * <p>
	 * <code>length</code> is the number of units of length, where units of
	 * length varies depending on the type that is being ?derived? from (#of
	 * chars for a string type, #of octets for a binary type, etc)
	 * </p>
	 * @param attributeName
	 * @param binding
	 * @param length
	 * @return
	 */
	public static Filter createLength(Name attributeName, Class binding,
			int length) {
		throw new UnsupportedOperationException("Not yet implemented");
	}


	/**
	 * Creates a Filter that 
	 */
	public static Filter createMinLength(Name attributeName, Class binding,
			int length) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Creates a Filter that 
	 */
	public static Filter createMaxLength(Name attributeName, Class binding,
			int length) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Creates a Filter that 
	 */
	public static Filter createPattern(Name attributeName, Class binding,
			String regExp) {
		throw new UnsupportedOperationException("Not yet implemented");
	}


	/**
	 * Creates a Filter that 
	 */
	public static Filter createEnumeration(Name attributeName, Class binding,
			Set values) {
		throw new UnsupportedOperationException("Not yet implemented");
	}


	/**
	 * Creates a Filter that
	 * @parameter constraintName one of <code>"preserve"</code>, <code>"replace"</code>, 
	 * <code>"collapse"</code> 
	 */
	public static Filter createWhiteSpace(Name attributeName, Class binding,
			String constraintName) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Creates a Filter that 
	 */
	public static Filter createMaxInclusive(Name attributeName, Class binding,
			Object maxValue) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Creates a Filter that 
	 */
	public static Filter createMaxExclusive(Name attributeName, Class binding,
			Object maxValue) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Creates a Filter that 
	 */
	public static Filter createMinInclusive(Name attributeName, Class binding,
			Object minValue) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Creates a Filter that 
	 */
	public static Filter createMinExclusive(Name attributeName, Class binding,
			Object minValue) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Creates a Filter that 
	 */
	public static Filter createTotalDigits(Name attributeName, Class binding,
			int totalDigits) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Creates a Filter that 
	 */
	public static Filter createFractionDigits(Name attributeName, Class binding,
			int fractionDigits) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
