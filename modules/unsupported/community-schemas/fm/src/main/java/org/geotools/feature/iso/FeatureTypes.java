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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.LengthFunction;
import org.geotools.geometry.jts.JTS;
import org.geotools.resources.Utilities;
import org.geotools.util.GTContainer;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.expression.Literal;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.picocontainer.defaults.DefaultPicoContainer;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Utility methods for working against the FeatureType interface.
 * <p>
 * Many methods from DataUtilities should be refractored here.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 * <li>Schema construction from String spec
 * <li>Schema Force CRS
 * </ul>
 * 
 * @author Jody Garnett, Refractions Research
 * @since 2.1.M3
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/2.4.x/modules/unsupported/community-schemas/fm/src/main/java/org/geotools/feature/iso/FeatureTypes.java $
 */
public class FeatureTypes {

	/** represent an unbounded field length */
	final public static int ANY_LENGTH = -1;

	/** factory for building types */
	final private static TypeFactory factory;

	/** empty types */
	final private static FeatureType emptyFeatureType;

	final private static FeatureCollectionType emptyFeatureCollectionType;

	//
	// The following code makes use of a GTContainer placing the implementation
	// of emptyFeatureType etc beyond the reach of users.
	//
	// If this is what is intended lets use the implementations directly,
	// if not these should be normal non static fields
	//
	static {
		DefaultPicoContainer container = GTContainer.normal();
		factory = (TypeFactory) container
				.getComponentInstanceOfType(TypeFactory.class);

		emptyFeatureType = factory.createFeatureType(Types
				.typeName("EmptyFeatureType"), Collections.EMPTY_LIST, null,
				null, false, Collections.EMPTY_SET, null, null);

		emptyFeatureCollectionType = factory.createFeatureCollectionType(Types
				.typeName("EmptyFeatureCollectionType"), Collections.EMPTY_SET,
				Collections.EMPTY_SET, null, null, false,
				Collections.EMPTY_SET, null, null);
	}

	/**
	 * Creates a non-static instance of this class statisfying necessary
	 * injected dependencies.
	 */
	private static FeatureTypes instance() {
		// TODO: use factory finders
		return new FeatureTypes(null, null);
	}

	// /**
	// * Returns an empty feature type.
	// */
	// public static FeatureType emptyFeatureType() {
	// return emptyFeatureType;
	// }
	//    
	// /**
	// * Returns an empty simple feature type.
	// */
	// public static SimpleFeatureType emptySimpleFeatureType() {
	// return emptySimpleFeatureType;
	// }
	//    
	// /**
	// * Returns an empty feature collection type.
	// */
	// public static FeatureCollectionType emptyFeatureCollectionType() {
	// return emptyFeatureCollectionType;
	// }
	//    
	// /**
	// * Returns an empty feature collection type.
	// */
	// public static SimpleFeatureCollectionType
	// emptySimpleFeatureCollectionType() {
	// return emptySimpleFeatureCollectionType;
	// }

	/**
	 * Returns an array of the attribute names contained in the feature type.
	 */
	public static Name[] attributeNames(FeatureType type) {
		Collection atts = type.attributes();
		Name[] names = new Name[atts.size()];

		int i = 0;
		for (Iterator itr = atts.iterator(); itr.hasNext(); i++) {
			AttributeDescriptor ad = (AttributeDescriptor) itr.next();
			names[i] = ad.getName();
		}

		return names;
	}

	/**
	 * This is a 'suitable replacement for extracting the expected field length
	 * of an attribute absed on its "facets" (ie Filter describing type
	 * restrictions);
	 * <p>
	 * This code is copied from the ShapefileDataStore where it was written
	 * (probably by dzwiers). Cholmes is providing documentation.
	 * </p>
	 * 
	 * @param attributeType
	 * @return max length of field in characters, or ANY_LENGTH
	 */
	public static int getFieldLength(AttributeType type) {

		Class colType = type.getBinding();
		// String colName = type.getName();

		int fieldLen = -1;

		Set restrictions = type.getRestrictions();
		if (restrictions == null) {
			return ANY_LENGTH;
		}

		for (Iterator itr = restrictions.iterator(); itr.hasNext();) {
			Filter f = (Filter) itr.next();
			if (f instanceof PropertyIsLessThan
					|| f instanceof PropertyIsGreaterThanOrEqualTo) {
				try {
					BinaryComparisonOperator cf = (BinaryComparisonOperator) f;
					if (cf.getExpression1() instanceof LengthFunction) {
						return Integer.parseInt(((Literal) cf.getExpression2())
								.getValue().toString());
					} else if (cf.getExpression2() instanceof LengthFunction) {
						return Integer.parseInt(((Literal) cf.getExpression1())
								.getValue().toString());
					} else {
						return ANY_LENGTH;
					}
				} catch (NumberFormatException e) {
					// continue
				}
			}
		}

		return ANY_LENGTH;
	}

	/** builders * */
	TypeBuilder typeBuilder;

	AttributeBuilder builder;

	public FeatureTypes(TypeBuilder typeBuilder, AttributeBuilder builder) {
		this.typeBuilder = typeBuilder;
		this.builder = builder;
	}

	/**
	 * Transforms a simple feature type from one schema to another.
	 */
	public FeatureType transformFeatureType(FeatureType schema,
			CoordinateReferenceSystem crs) {

		TypeFactory tf = typeBuilder.getTypeFactory();
		FeatureFactory af = builder.getFeatureFactory();

		// initialize with state from old type
		typeBuilder.init(schema);

		AttributeDescriptor defaultGeometry = null;
		for (Iterator itr = schema.attributes().iterator(); itr.hasNext();) {
			AttributeDescriptor ad = (AttributeDescriptor) itr.next();
			AttributeType attributeType = (AttributeType) ad.type();
			if (attributeType instanceof GeometryType) {
				GeometryType geometryType = (GeometryType) attributeType;
				GeometryType geometry;

				// duplicate old type with new crs
				geometry = tf.createGeometryType(geometryType.getName(),
						geometryType.getBinding(), crs, geometryType
								.isIdentified(), geometryType.isAbstract(),
						geometryType.getRestrictions(),
						(GeometryType) geometryType.getSuper(), geometryType
								.getDescription());
				attributeType = geometry;

				if (defaultGeometry == null
						|| geometryType == schema.getDefaultGeometry()
								.type()) {
					
					//TODO: handle default value
					Object defaultValue = null;
					defaultGeometry = factory.createAttributeDescriptor(
							attributeType, ad.getName(), ad.getMinOccurs(), ad
									.getMaxOccurs(), ad.isNillable(), defaultValue);

				}
			}

			typeBuilder.attribute(ad.getName(), attributeType);
		}

		if (defaultGeometry != null) {
			typeBuilder.setDefaultGeometry(defaultGeometry.getName());
		}

		return typeBuilder.feature();
	}

	/**
	 * @deprecated use
	 *             {@link #transformFeatureType(FeatureType, CoordinateReferenceSystem)}
	 */
	public static FeatureType transform(FeatureType schema,
			CoordinateReferenceSystem crs) throws SchemaException {

		return instance().transformFeatureType(schema, crs);
	}

	/**
	 * Applies transform to all geometry attribute.
	 * 
	 * @param feature
	 *            Feature to be transformed
	 * @param schema
	 *            Schema for target transformation - transform( schema, crs )
	 * @param transform
	 *            MathTransform used to transform coordinates - reproject( crs,
	 *            crs )
	 * @return transformed Feature of type schema
	 * @throws TransformException
	 * @throws MismatchedDimensionException
	 * @throws IllegalAttributeException
	 */
	public Feature transformFeature(Feature feature, FeatureType schema,
			MathTransform transform) throws MismatchedDimensionException,
			TransformException, IllegalAttributeException {

		builder.init();
		builder.setType(schema);
		for (Iterator itr = ((Collection) feature).iterator(); itr.hasNext();) {
			Attribute att = (Attribute) itr.next();
			builder.add(att.getID(), att.getValue(), att.name());
		}

		feature = (Feature) builder.build(feature.getID());

		GeometryAttribute geomAtt = (GeometryAttribute) feature
				.getDefaultGeometry();
		Geometry geom = (Geometry) geomAtt;

		if (geom != null) {
			geom = JTS.transform(geom, transform);
			geomAtt.setValue(geom);
		}

		return feature;
	}

	/**
	 * @deprecated use
	 *             {@link #transformFeature(Feature, FeatureType, MathTransform)
	 */
	public static Feature transform(Feature feature, FeatureType schema,
			MathTransform transform) throws MismatchedDimensionException,
			TransformException, IllegalAttributeException {

		assert feature instanceof SimpleFeature;
		assert schema instanceof SimpleFeatureType;

		return instance().transform((SimpleFeature) feature,
				(SimpleFeatureType) schema, transform);
	}

	/**
	 * The most specific way to create a new FeatureType.
	 * 
	 * @param types
	 *            The AttributeTypes to create the FeatureType with.
	 * @param name
	 *            The typeName of the FeatureType. Required, may not be null.
	 * @param ns
	 *            The namespace of the FeatureType. Optional, may be null.
	 * @param isAbstract
	 *            True if this created type should be abstract.
	 * @param superTypes
	 *            A Collection of types the FeatureType will inherit from.
	 *            Currently, all types inherit from feature in the opengis
	 *            namespace.
	 * @return A new FeatureType created from the given arguments.
	 * @throws FactoryRegistryException
	 *             If there are problems creating a factory.
	 * @throws SchemaException
	 *             If the AttributeTypes provided are invalid in some way.
	 */
	public static FeatureType newFeatureType(AttributeType[] types,
			String name, URI ns, boolean isAbstract, FeatureType[] superTypes)
			throws FactoryRegistryException, SchemaException {
		return newFeatureType(types, name, ns, isAbstract, superTypes, null);
	}

	/**
	 * @deprecated use
	 *             {@link #createNewFeatureType(AttributeType[], String, URI, boolean, FeatureType[], GeometryType)}
	 */
	public static FeatureType newFeatureType(AttributeType[] types,
			String name, URI ns, boolean isAbstract, FeatureType[] superTypes,
			AttributeType defaultGeometry) throws FactoryRegistryException,
			SchemaException {

		assert defaultGeometry instanceof GeometryType;
		assert superTypes == null || superTypes.length == 0
				|| superTypes.length == 0;
		return instance().createNewFeatureType(types, name, ns, isAbstract,
				superTypes, (GeometryType) defaultGeometry);
	}

	/**
	 * The most specific way to create a new FeatureType.
	 * 
	 * @param types
	 *            The AttributeTypes to create the FeatureType with.
	 * @param name
	 *            The typeName of the FeatureType. Required, may not be null.
	 * @param ns
	 *            The namespace of the FeatureType. Optional, may be null.
	 * @param isAbstract
	 *            True if this created type should be abstract.
	 * @param superTypes
	 *            A Collection of types the FeatureType will inherit from.
	 *            Currently, all types inherit from feature in the opengis
	 *            namespace.
	 * @return A new FeatureType created from the given arguments.
	 * @throws FactoryRegistryException
	 *             If there are problems creating a factory.
	 * @throws SchemaException
	 *             If the AttributeTypes provided are invalid in some way.
	 * 
	 * @deprecated use {@link FeatureTypeBuilder}
	 */
	public FeatureType createNewFeatureType(AttributeType[] types, String name,
			URI ns, boolean isAbstract, FeatureType[] superTypes,
			GeometryType defaultGeometry) throws FactoryRegistryException,
			SchemaException {

		typeBuilder.init();
		typeBuilder.setNamespaceURI(ns.toString());
		typeBuilder.setName(name);
		typeBuilder.setAbstract(isAbstract);

		// assume name of attribute = name of type
		typeBuilder.setDefaultGeometry(Types.typeName(defaultGeometry
				.getName()));

		if (superTypes != null && superTypes.length > 0) {
			typeBuilder.setSuper(superTypes[0]);
		}

		// name attribute same as its type
		for (int i = 0; i < types.length; i++) {
			typeBuilder.attribute(Types.typeName(types[i].getName()),
					types[i]);
		}

		return typeBuilder.feature();

	}

	/**
	 * @deprecated use
	 *             {@link #newFeatureType(AttributeType[], String, URI, boolean, SimpleFeatureType[], GeometryType)
	 */
	public static FeatureType newFeatureType(AttributeType[] types,
			String name, URI ns, boolean isAbstract, FeatureType[] superTypes,
			GeometryType defaultGeometry) throws FactoryRegistryException,
			SchemaException {

		if (superTypes == null)
			superTypes = new SimpleFeatureType[] {};

		assert defaultGeometry == null
				|| defaultGeometry instanceof GeometryType;
		assert superTypes.length == 0
				|| (superTypes.length == 1 && superTypes[0] instanceof SimpleFeatureType);
		superTypes = (SimpleFeatureType[]) Arrays.asList(superTypes).toArray(
				new SimpleFeatureType[superTypes.length]);

		return instance().newFeatureType(types, name, ns, isAbstract,
				(SimpleFeatureType[]) superTypes,
				(GeometryType) defaultGeometry);
	}

	/**
	 * Create a new FeatureType with the given AttributeTypes. A short cut for
	 * calling <code>newFeatureType(types,name,ns,isAbstract,null)</code>.
	 * 
	 * @param types
	 *            The AttributeTypes to create the FeatureType with.
	 * @param name
	 *            The typeName of the FeatureType. Required, may not be null.
	 * @param ns
	 *            The namespace of the FeatureType. Optional, may be null.
	 * @param isAbstract
	 *            True if this created type should be abstract.
	 * @return A new FeatureType created from the given arguments.
	 * @throws FactoryRegistryException
	 *             If there are problems creating a factory.
	 * @throws SchemaException
	 *             If the AttributeTypes provided are invalid in some way.
	 * 
	 * @deprecated use
	 *             {@link #newFeatureType(AttributeType[], String, URI, boolean, SimpleFeatureType[], GeometryType)}
	 */
	public static FeatureType newFeatureType(AttributeType[] types,
			String name, URI ns, boolean isAbstract)
			throws FactoryRegistryException, SchemaException {
		return newFeatureType(types, name, ns, isAbstract, null);
	}

	/**
	 * Create a new FeatureType with the given AttributeTypes. A short cut for
	 * calling <code>newFeatureType(types,name,ns,false,null)</code>.
	 * 
	 * @param types
	 *            The AttributeTypes to create the FeatureType with.
	 * @param name
	 *            The typeName of the FeatureType. Required, may not be null.
	 * @param ns
	 *            The namespace of the FeatureType. Optional, may be null.
	 * @return A new FeatureType created from the given arguments.
	 * @throws FactoryRegistryException
	 *             If there are problems creating a factory.
	 * @throws SchemaException
	 *             If the AttributeTypes provided are invalid in some way.
	 * 
	 * @deprecated use
	 *             {@link #newFeatureType(AttributeType[], String, URI, boolean, SimpleFeatureType[], GeometryType)}
	 */
	public static FeatureType newFeatureType(AttributeType[] types,
			String name, URI ns) throws FactoryRegistryException,
			SchemaException {
		return newFeatureType(types, name, ns, false);
	}

	/**
	 * Create a new FeatureType with the given AttributeTypes. A short cut for
	 * calling <code>newFeatureType(types,name,null,false,null)</code>.
	 * Useful for test cases or datasources which may not allow a namespace.
	 * 
	 * @param types
	 *            The AttributeTypes to create the FeatureType with.
	 * @param name
	 *            The typeName of the FeatureType. Required, may not be null.
	 * @return A new FeatureType created from the given arguments.
	 * @throws FactoryRegistryException
	 *             If there are problems creating a factory.
	 * @throws SchemaException
	 *             If the AttributeTypes provided are invalid in some way.
	 * 
	 * @deprecated use
	 *             {@link #newFeatureType(AttributeType[], String, URI, boolean, SimpleFeatureType[], GeometryType)}
	 */
	public static FeatureType newFeatureType(AttributeType[] types, String name)
			throws FactoryRegistryException, SchemaException {

		URI namespace = null;
		try {
			namespace = new URI("http://www.opengis.net/gml");
		} catch (URISyntaxException e) {
			namespace = null;
		}

		return newFeatureType(types, name, namespace, false);
	}

	/**
	 * A query of the the types ancestor information.
	 * <p>
	 * This utility method may be used as common implementation for
	 * <code>FeatureType.isDecendedFrom( namespace, typeName )</code>,
	 * however for specific uses, such as GML, an implementor may be able to
	 * provide a more efficient implemenation based on prior knolwege.
	 * </p>
	 * <p>
	 * This is a proper check, if the provided FeatureType matches the given
	 * namespace and typename it is <b>not </b> considered to be decended from
	 * itself.
	 * </p>
	 * 
	 * @param featureType
	 *            typeName with parentage in question
	 * @param namespace
	 *            namespace to match against, or null for a "wildcard"
	 * @param typeName
	 *            typename to match against, or null for a "wildcard"
	 * @return true if featureType is a decendent of the indicated namespace &
	 *         typeName
	 */
	public static boolean isDecendedFrom(FeatureType featureType,
			URI namespace, String typeName) {
		if (featureType == null)
			return false;

		FeatureType superType = (FeatureType) featureType.getSuper();
		if (superType == null) {
			return false;

		}
		Name name = superType.getName();

		boolean match = true;

		if (namespace != null) {
			match = namespace.toString().equals(name.getNamespaceURI());
		}

		if (match && typeName != null) {
			match = typeName.equals(name.getLocalPart());
		}

		if (match) {
			return true;
		}

		if (superType.getSuper() != null) {
			return isDecendedFrom((FeatureType) superType.getSuper(),
					namespace, typeName);
		}

		return false;
	}

	public static boolean isDecendedFrom(FeatureType featureType,
			FeatureType isParentType) {
		if (featureType == null || isParentType == null)
			return false;

		FeatureType superType = (FeatureType) featureType.getSuper();
		if (superType == null) {
			return false;
		}

		if (superType.equals(isParentType)) {
			return true;
		}

		if (Utilities.equals(superType.getName(), isParentType.getName())) {
			return true;
		}

		if (superType.getSuper() != null) {
			return isDecendedFrom((FeatureType) superType.getSuper(),
					isParentType);
		}

		return false;
	}

	/** Exact equality based on typeNames, namespace, attributes and ancestors */
	public static boolean equals(FeatureType typeA, FeatureType typeB) {
		if (typeA == typeB)
			return true;

		if (typeA == null || typeB == null) {
			return false;
		}

		return equalsId(typeA, typeB)
				&& equals(typeA.attributes(), typeB.attributes())
				&& equalsAncestors(typeA, typeB);
	}

	public static boolean equals(Collection/* <AttributeType> */a,
			Collection/* <AttributeType> */b) {
		if (a.size() != b.size())
			return false;

		Iterator ia = a.iterator();
		Iterator ib = b.iterator();

		while (ia.hasNext()) {
			AttributeType ata = (AttributeType) ia.next();
			AttributeType atb = (AttributeType) ib.next();

			if (!equals(ata, atb))
				return false;
		}

		return true;
	}

	public static boolean equals(AttributeType attributesA[],
			AttributeType attributesB[]) {
		return equals(Arrays.asList(attributesA), Arrays.asList(attributesB));
	}

	/**
	 * This method depends on the correct implementation of FeatureType equals
	 * <p>
	 * We may need to write an implementation that can detect cycles,
	 * </p>
	 * 
	 * @param typeA
	 * @param typeB
	 * @return
	 */
	public static boolean equalsAncestors(FeatureType typeA, FeatureType typeB) {
		return ancestors(typeA).equals(typeB);
	}

	public static Set ancestors(FeatureType featureType) {
		if (featureType == null || featureType.getSuper() == null)
			return Collections.EMPTY_SET;

		HashSet set = new HashSet();
		set.add(featureType.getSuper());

		return set;
	}

	public static boolean equals(AttributeType a, AttributeType b) {
		return a == b || (a != null && a.equals(b));
	}

	/** Quick check of namespace and typename */
	public static boolean equalsId(FeatureType typeA, FeatureType typeB) {
		if (typeA == typeB)
			return true;

		if (typeA == null || typeB == null) {
			return false;
		}

		return Utilities.equals(typeA.getName(), typeB.getName());
	}

	/**
	 * 
	 * @deprecated use {@link #createView(FeatureType, Name[])
	 */
	public static FeatureType view(FeatureType type, Name[] names) {

		return instance().createView((SimpleFeatureType) type, names);
	}

	/**
	 * Creates a view of the original feature type based on the set of attribute
	 * names provided.
	 * 
	 * @return
	 */
	public FeatureType createView(FeatureType type, Name[] names) {
		if (names == null) {
			return type;
		}

		boolean same = type.attributes().size() == names.length;
		int i = 0;
		for (Iterator itr = type.attributes().iterator(); itr.hasNext(); i++) {
			AttributeDescriptor ad = (AttributeDescriptor) itr.next();
			AttributeType at = (AttributeType) ad.type();

			same = Utilities.equals(at.getName(), names[i]);
		}

		if (same) {
			return type;
		}

		typeBuilder.init(type);
		typeBuilder.getProperties().clear();

		for (i = 0; i < names.length; i++) {
			PropertyDescriptor ad = Types.descriptor(type, names[i]);
			if (ad == null) {
				throw new IllegalArgumentException(
						"Original type does not contain attribute" + names[i]);
			}
			if (!(ad instanceof AttributeDescriptor)) {
				throw new IllegalArgumentException(names[i]
						+ " references non attribute");
			}

			typeBuilder.attribute(ad.getName(), (AttributeType) ad.type());
		}

		return typeBuilder.feature();
	}
}
