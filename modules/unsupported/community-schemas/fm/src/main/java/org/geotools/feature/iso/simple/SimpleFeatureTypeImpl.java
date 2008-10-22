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

package org.geotools.feature.iso.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geotools.feature.iso.Descriptors;
import org.geotools.feature.iso.Types;
import org.geotools.feature.iso.type.FeatureTypeImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * Implementation fo SimpleFeatureType, subtypes must be atomic and are stored
 * in a list.
 * 
 * @author Justin
 */
public class SimpleFeatureTypeImpl extends FeatureTypeImpl implements
		SimpleFeatureType {

	// list of types
	List types = null;

	public SimpleFeatureTypeImpl(Name name, Collection schema,
			AttributeDescriptor defaultGeometry, CoordinateReferenceSystem crs,
			Set/* <Filter> */restrictions, InternationalString description) {
		super(name, new ArrayList(schema), defaultGeometry, crs, false, restrictions, null,
				description);
	}
	
	public SimpleFeatureTypeImpl(Name name, List typeList, AttributeType geometryType, CoordinateReferenceSystem crs, Set restrictions, InternationalString description) {
		this( name, SimpleFeatureFactoryImpl.descriptors( typeList ), SimpleFeatureFactoryImpl.geometryName( geometryType), crs, restrictions, description );
		types = typeList;
	}
	
	private SimpleFeatureTypeImpl(Name name, List list, Name geomName, CoordinateReferenceSystem crs, Set restrictions, InternationalString description) {
		this( name, list, SimpleFeatureFactoryImpl.find( list, geomName ), crs, restrictions, description );
	}
	
	public AttributeType get(Name qname) {
		return Descriptors.type(SCHEMA, qname);
	}

	public AttributeType getType(String name) {
		return get(Types.typeName(name));
	}

	public AttributeType getType(int index) {
		return (AttributeType) getTypes().get(index);
	}

	public int indexOf(String arg0) {
		int index = 0;
		for (Iterator itr = SCHEMA.iterator(); itr.hasNext(); index++) {
			AttributeDescriptor descriptor = (AttributeDescriptor) itr.next();
			if (name.equals(descriptor.getName().getLocalPart())) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * Number of available attributes
	 */
	public int getNumberOfAttribtues() {
		return SCHEMA.size();
	}

	/**
	 * Types are returned in the perscribed index order.
	 * 
	 * @return Types in prescribed order
	 */
	public List /* List<AttributeType> */getTypes() {
		if (types == null) {
			synchronized (this) {
				if (types == null) {
					types = new ArrayList();
					for (Iterator itr = SCHEMA.iterator(); itr.hasNext();) {
						AttributeDescriptor ad = (AttributeDescriptor) itr
								.next();
						types.add(ad.type());
					}
				}
			}
		}

		return types;
	}

	public GeometryType getDefaultGeometryType() {
		AttributeDescriptor desc = getDefaultGeometry();
		if (desc != null)
			return (GeometryType) desc.type();

		return null;
	}

    public AttributeDescriptor getAttribute(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public AttributeDescriptor getAttribute(int index) {
        return (AttributeDescriptor) ((List)SCHEMA).get(index);
    }

    public int getAttributeCount() {
        return SCHEMA.size();
    }

    public List getAttributes() {
        return (List)SCHEMA;
    }

}
