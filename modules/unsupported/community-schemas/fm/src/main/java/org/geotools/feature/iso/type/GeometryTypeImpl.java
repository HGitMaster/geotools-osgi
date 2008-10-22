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

package org.geotools.feature.iso.type;

import java.util.Set;

import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

import com.vividsolutions.jts.geom.GeometryFactory;

public class GeometryTypeImpl extends AttributeTypeImpl implements GeometryType {

	protected GeometryFactory geometryFactory;

	/**
	 * CoordianteSystem used by this GeometryAttributeType NOT used yet, needs
	 * to incorporate the functionality from the old GeometricAttributeType
	 */
	private CoordinateReferenceSystem CRS;

	public GeometryTypeImpl(
            Name name, Class binding, CoordinateReferenceSystem crs, 
		boolean identified, boolean isAbstract, Set restrictions, 
		AttributeType superType, InternationalString description
	) {
		super(name, binding, identified, isAbstract, restrictions, superType, description);
		CRS = crs;
	}

	public CoordinateReferenceSystem getCRS() {
		return CRS;
	}

}
