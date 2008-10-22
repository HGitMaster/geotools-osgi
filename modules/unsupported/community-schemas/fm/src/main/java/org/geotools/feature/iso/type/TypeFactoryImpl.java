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
import java.util.Set;

import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.OperationDescriptor;
import org.opengis.feature.type.OperationType;
import org.opengis.feature.type.Schema;
import org.opengis.feature.type.TypeFactory;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * This implementation is capable of creating a good default implementation of
 * the Types used in the feature model.
 * <p>
 * The implementation focus here is on corretness rather then efficiency or even
 * strict error messages. The code serves as a good example, but is not
 * optimized for any particular use.
 * </p>
 * 
 * @author Jody Garnett
 */
public class TypeFactoryImpl implements TypeFactory {
	/** Used for spatial content */
	CRSFactory crsFactory;
	
	/** Used for type restricftions */
	FilterFactory filterFactory;

	/** Rely on setter injection */
	public TypeFactoryImpl() {
		this.crsFactory = null;
		this.filterFactory = null;
	}
	/** Constructor injection */
	public TypeFactoryImpl(CRSFactory crsFactory, FilterFactory filterFactory) {
		this.crsFactory = crsFactory;
		this.filterFactory = filterFactory;
	}

	public Schema createSchema(String uri) {
		return new SchemaImpl(uri);
	}

	public CRSFactory getCRSFactory() {
		return crsFactory;
	}

	public void setCRSFactory(CRSFactory crsFactory) {
		this.crsFactory = crsFactory;
	}

	public FilterFactory getFilterFactory() {
		return filterFactory;
	}

	public void setFilterFactory(FilterFactory filterFactory) {
		this.filterFactory = filterFactory;
	}

	public OperationDescriptor createOperationDescriptor(OperationType type,
			boolean isImplemented) {
		throw new UnsupportedOperationException("implement");
	}

	public AssociationDescriptor createAssociationDescriptor(
			AssociationType type, Name name, int minOccurs, int maxOccurs) {
		return new AssociationDescriptorImpl(type, name, minOccurs, maxOccurs);
	}

	public AttributeDescriptor createAttributeDescriptor(AttributeType type,
			Name name, int minOccurs, int maxOccurs, boolean isNillable, Object defaultValue) {
		return new AttributeDescriptorImpl(type, name, minOccurs, maxOccurs,
				isNillable, defaultValue);
	}

	public AssociationType createAssociationType(Name name,
			AttributeType referenceType, boolean isIdentifiable,
			boolean isAbstract, Set restrictions, AssociationType superType,
			InternationalString description) {
		return new AssociationTypeImpl(name, referenceType, isIdentifiable,
				isAbstract, restrictions, superType, description);
	}

	public AttributeType createAttributeType(Name name, Class binding,
			boolean isIdentifiable, boolean isAbstract, Set restrictions,
			AttributeType superType, InternationalString description) {

		return new AttributeTypeImpl(name, binding, isIdentifiable, isAbstract,
				restrictions, superType, description);
	}

	public ComplexType createComplexType(Name name, Collection schema,
			boolean isIdentifiable, boolean isAbstract, Set restrictions,
			AttributeType superType, InternationalString description) {

		return new ComplexTypeImpl(name, schema, isIdentifiable, isAbstract,
				restrictions, superType, description);
	}

	public GeometryType createGeometryType(Name name, Class binding,
			CoordinateReferenceSystem crs, boolean isIdentifiable,
			boolean isAbstract, Set restrictions, AttributeType superType,
			InternationalString description) {

		return new GeometryTypeImpl(name, binding, crs, isIdentifiable,
				isAbstract, restrictions, superType, description);
	}

	public FeatureType createFeatureType(Name name, Collection schema,
			AttributeDescriptor defaultGeometry, CoordinateReferenceSystem crs,
			boolean isAbstract, Set restrictions, AttributeType superType,
			InternationalString description) {

		return new FeatureTypeImpl(name, schema, defaultGeometry, crs,
				isAbstract, restrictions, superType, description);
	}

	public FeatureCollectionType createFeatureCollectionType(Name name,
			Collection properties, Collection members,
			AttributeDescriptor defaultGeom, CoordinateReferenceSystem crs,
			boolean isAbstract, Set restrictions, AttributeType superType,
			InternationalString description) {
		return new FeatureCollectionTypeImpl(name, properties, members,
				defaultGeom, crs, isAbstract, restrictions, superType,
				description);
	}
}
