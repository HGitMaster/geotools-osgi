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
import java.util.Iterator;
import java.util.Set;

import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * 
 * Base implementation of FeatureType.
 * 
 * @author gabriel
 *
 */
public class FeatureTypeImpl extends ComplexTypeImpl implements FeatureType {
	
	protected AttributeDescriptor DEFAULT;

	protected CoordinateReferenceSystem CRS;
	
	public FeatureTypeImpl(
            Name name, Collection schema, AttributeDescriptor defaultGeom, 
		CoordinateReferenceSystem crs, boolean isAbstract, 
		Set/*<Filter>*/ restrictions, AttributeType superType, InternationalString description
	) {
		super(name, schema, true, isAbstract, restrictions, superType, description);
		DEFAULT = defaultGeom;
        CRS = crs;
	}

	public CoordinateReferenceSystem getCRS() {
		return CRS;
	}
	
	public AttributeDescriptor getDefaultGeometry() {
		if (DEFAULT == null) {
			for (Iterator itr = attributes().iterator(); itr.hasNext();) {
				AttributeDescriptor desc = (AttributeDescriptor) itr.next();
				if (desc.type() instanceof GeometryType) {
					DEFAULT = desc; 
					break;
				}
			}
		}
		return DEFAULT;
	}
	
//	public GeometryType getDefaultGeometry() {
//		
//		if (DEFAULT == null) {
//			for (Iterator itr = getAttributes().iterator(); itr.hasNext();) {
//				AttributeDescriptor desc = (AttributeDescriptor) itr.next();
//				if (desc.getType() instanceof GeometryType) {
//					DEFAULT = (GeometryType) desc.getType(); 
//					break;
//				}
//			}
//		}
//		return DEFAULT;
//	}
}
