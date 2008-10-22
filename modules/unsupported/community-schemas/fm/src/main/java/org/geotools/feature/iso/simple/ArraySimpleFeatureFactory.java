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
import java.util.Date;
import java.util.List;

import org.geotools.feature.iso.collection.MemorySimpleFeatureCollection;
import org.opengis.feature.Association;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.BooleanAttribute;
import org.opengis.feature.simple.NumericAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.TemporalAttribute;
import org.opengis.feature.simple.TextAttribute;
import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyType;
import org.opengis.geometry.coordinate.GeometryFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * A SimpleFeatureFactory that produces an implementation that directly wraps an
 * array of object values.
 * <p>
 * This implementation is designed to be as close to the previous geotools
 * DefaultFeature implementation as possible while still meeting the
 * requirements of the GeoAPI feature model.
 * <p>
 * This implementation is marked as a SimpleFeature (as it is only capable of
 * storing directly bound Types with no multiplicity.
 * </p>
 * <p>
 * Although the use of SimpleFeatureFactory is straight forward and direct we
 * have chosen to implement FeatureFactory as well, this allows any and all code
 * to work with SimpleFeatures (as a SimpleFeatureType by definition will only
 * describe content that can be created via this factory).
 * </p>
 * 
 * @author Jody
 */
public class ArraySimpleFeatureFactory implements SimpleFeatureFactory {
	/**
	 * Q: Why do I need this? I am not making content? only holding it.
	 * A: Anyone producing may need this.
	 */
	CRSFactory crsFactory;

	/**
	 * Q: Why do I need this? I am not making content? only holding it.
	 * A: Anyone producing may need this.
	 */
	GeometryFactory gf;

	public CRSFactory getCRSFactory() {
		return crsFactory;
	}

	public void setCRSFactory(CRSFactory factory) {
		crsFactory = factory;
	}

	public GeometryFactory getGeometryFactory() {
		return gf;
	}

	public void setGeometryFactory(GeometryFactory geometryFactory) {
		this.gf = geometryFactory;
	}

	/**
	 * Direct creation of a SimpleFeature from a provided SimpleFeatureType, it
	 * is understood that the provided array of values maps directly to the
	 * attribute descriptors in the provided type.
	 * 
	 * @param type
	 *            SimpleFeatureType of the created Feature, non null
	 * @param id
	 *            To be used as the Feature ID for the created Feature, non null
	 * @param values
	 *            Attribute values for the created feature, if <code>null</code>
	 *            the created feature will be empty
	 */
	public SimpleFeature createSimpleFeature(SimpleFeatureType type,
			String fid, Object[] values) {
		return new ArraySimpleFeature(type, fid, values);
	}

	/**
	 * Constructs a MemorySimpleFeatureCollection, not often used as datastores
	 * will provide a custom collection (for an example see PostGIS).
	 * 
	 * @param type
	 *            Type of the collection being created, non null
	 * @param id
	 *            Feature ID of the collection being created, non null
	 * @param contents
	 *            Initial contents (ie Collection&lt;SimpleFeatures&gt;) or
	 *            <code>null</code>
	 * @return
	 */

	public SimpleFeatureCollection createSimpleFeatureCollection(
			SimpleFeatureCollectionType type, String fid) {
		return new MemorySimpleFeatureCollection(type, fid);
	}

	/**
	 * Create a stub associations descriptor, will be used as a parameter to
	 * by createFeatureCollectio
	 * <p>
	 * This is only used by generic feature model code following a
	 * SimpleFeatureCollectionType to the letter, we will unwrap the
	 * create association and make use of the reference type directly.
	 * </p>
	 */
	public AssociationDescriptor createAssociationDescriptor(
			final AssociationType associationType, final Name name, int min, int max) {
		return new AssociationDescriptor(){
			public AssociationType getType() {
				return associationType;
			}
			public int getMinOccurs() {
				return 1;
			}
			public int getMaxOccurs() {
				return 1;
			}
			public void putUserData(Object arg0, Object arg1) {				
			}
			public Object getUserData(Object arg0) {
				return null;
			}
			public Name getName() {
				return name;
			}
			public PropertyType type() {
				return associationType.getReferenceType();
			}			
		};
	}

	public Association createAssociation(Attribute arg0,
			AssociationDescriptor arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public AttributeDescriptor createAttributeDescriptor(AttributeType arg0,
			Name arg1, int arg2, int arg3, boolean arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	public Attribute createAttribute(Object arg0, AttributeDescriptor arg1,
			String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public BooleanAttribute createBooleanAttribute(Boolean arg0,
			AttributeDescriptor arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public NumericAttribute createNumericAttribute(Number arg0,
			AttributeDescriptor arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public TextAttribute createTextAttribute(CharSequence arg0,
			AttributeDescriptor arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public TemporalAttribute createTemporalAttribute(Date arg0,
			AttributeDescriptor arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public GeometryAttribute createGeometryAttribute(Object arg0,
			AttributeDescriptor arg1, String arg2,
			CoordinateReferenceSystem arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	public ComplexAttribute createComplexAttribute(Collection arg0,
			AttributeDescriptor arg1, String arg2) {
		throw new UnsupportedOperationException(
		"Simple Feature Collection cannot be used with complex attribute");
	}

	public ComplexAttribute createComplexAttribute(Collection arg0,
			ComplexType arg1, String arg2) {
		throw new UnsupportedOperationException(
		"Simple Feature Collection cannot be used with complex attribute");
	}

	public Feature createFeature(Collection arg0, AttributeDescriptor arg1,
			String arg2) {
		throw new UnsupportedOperationException(
		"Simple Feature Collection cannot be used as a complex attribute");
	}

	public Feature createFeature(Collection values, FeatureType type, String id) {
		return new ArraySimpleFeature( (SimpleFeatureType) type, id, values.toArray() );
	}



	public FeatureCollection createFeatureCollection(Collection arg0,
			AttributeDescriptor arg1, String arg2) {
		throw new UnsupportedOperationException(
		"Simple Feature Collection cannot be used as a complex attribute");
	}

	public FeatureCollection createFeatureCollection(Collection properties,
			FeatureCollectionType type, String id) {
		return new MemorySimpleFeatureCollection((SimpleFeatureCollectionType) type, id );
	}


	//
	// Will not implement
	//
	public SimpleFeatureCollection createSimpleFeatureCollection(
			AttributeDescriptor descriptor, String arg1) {
		throw new UnsupportedOperationException(
				"Simple Feature Collection cannot be used as a complex attribute");
	}

	public SimpleFeature createSimpleFeature(List attributes, SimpleFeatureType type, String id) {
		// TODO Auto-generated method stub
		return null;
	}

}
