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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

public class FeatureCollectionTypeImpl extends FeatureTypeImpl implements
		FeatureCollectionType {

	final protected Set MEMBERS;

	public FeatureCollectionTypeImpl(Name name, Collection schema,
			Collection members, AttributeDescriptor defaultGeom,
			CoordinateReferenceSystem crs, boolean isAbstract,
			Set/* <Filter> */restrictions, AttributeType superType,
			InternationalString description

	) {
		super(name, schema, defaultGeom, crs, isAbstract, restrictions,
				superType, description);
		this.MEMBERS = new HashSet(members);
	}

	public Set getMembers() {
		return MEMBERS;
	}

}
