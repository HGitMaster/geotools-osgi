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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * This implementation is capable of creating a good default choice for
 * capturing SimpleFeatureType, the focus is on corretness rather then
 * efficiency or even strict error messages.
 * 
 * @author Jody Garnett
 */
public class SimpleTypeFactoryImpl extends TypeFactoryImpl implements
		SimpleTypeFactory {
	
	public SimpleTypeFactoryImpl() {
		super();
	}
	public SimpleTypeFactoryImpl(CRSFactory crsFactory, FilterFactory filterFactory) {
		super( crsFactory, filterFactory );
	}
	
	public FeatureType createFeatureType(Name name, Collection schema,
			AttributeDescriptor defaultGeometry, CoordinateReferenceSystem crs,
			boolean isAbstract, Set restrictions, AttributeType superType,
			InternationalString description) {
		return new SimpleFeatureTypeImpl(name, schema, defaultGeometry, crs,
				restrictions, description);
	}

	public FeatureCollectionType createFeatureCollectionType(Name name,
			Collection properties, Collection members,
			AttributeDescriptor defaultGeom, CoordinateReferenceSystem crs,
			boolean isAbstract, Set restrictions, AttributeType superType,
			InternationalString description) {
		return new SimpleFeatureCollectionTypeImpl(name,
				(AssociationDescriptor) members.iterator().next(),
				restrictions, description);
	}

    public SimpleFeatureType createSimpleFeatureType(Name name, 
            List types, AttributeDescriptor defaultGeometry, 
            CoordinateReferenceSystem crs, Set restrictions, 
            InternationalString description) {
        
        return new SimpleFeatureTypeImpl(name, types, defaultGeometry, crs,
                restrictions, description);
    }

	public SimpleFeatureCollectionType createSimpleFeatureCollectionType(
			Name name, SimpleFeatureType member, InternationalString description) {
		return new SimpleFeatureCollectionTypeImpl(name, member, description);
	}

}
