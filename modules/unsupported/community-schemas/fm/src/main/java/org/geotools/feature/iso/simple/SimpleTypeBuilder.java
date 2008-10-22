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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.feature.iso.Types;
import org.geotools.feature.iso.type.AttributeDescriptorImpl;
import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyType;
import org.opengis.feature.type.Schema;
import org.opengis.feature.type.TypeFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * Builder to ease creation of simple types.
 * <p>
 * For reference these are the limitations of a "Simple Feature" model:
 * <ol>
 * <li>Properties limited to attributes only (no associations)
 * <li>Properties is a List - order of attributes matters
 * <li>Attribute "index" is as good as a Name
 * <li>Attribute "name" (ie String) is as good as Name
 * <ul>
 * <li>No name conflict, so lookup with simple string is okay
 * </ul>
 * <li>getSuper() is <code>null</code>, required for safe use of index and
 * name
 * </ol>
 * </p>
 * <p>
 * There are four methods to manage builder state:
 * <ul>
 * <li>{@link init()} - completly replace settings with builder defaul
 * <li>{@link init( PropertyType )} - completly replace settings from type
 * <li>{@link reset()} - called after type creation to reset common type
 * settings
 * </ul>
 * For examples of use please review the two type creation methods:
 * <ul>
 * <li>{@link feature()}
 * <li>{@link collection()}
 * </ul>
 * Several methods for adding attribute make use of a Class directly, this class
 * is used to lookup an AttributeType "binding" to be used as a prototype. In
 * addition to providing binding one at a time, you can load a {@link Schema} in
 * one fell swoop. You may find SimpleSchema a useful starting place.
 * </p>
 * 
 * @author Justin Deolivera
 * @author Jody Garnett
 */
public class SimpleTypeBuilder {
    /**
     * Used for content creation (contains a crs and filter factory)
     */
    private SimpleTypeFactory factory;

    private TypeFactory typeFactory;

    /**
     * Naming: local name
     */
    private String local;

    /**
     * Naming: uri indicating scope
     */
    private String uri;

    /**
     * Description of type.
     */
    private InternationalString description;

    /**
     * MemberType for collection.
     * <p>
     * A simple feature collection can only represent one association.
     */
    private SimpleFeatureType memberType;

    /**
     * List of attributes.
     */
    private List attributes;

    /**
     * Additional restrictions on the type.
     */
    private Set restrictions;

    /** Name of the default geometry to use */
    private String defaultGeom;

    protected CoordinateReferenceSystem crs;

    /**
     * Map of java class bound to properties types.
     */
    protected Map/* <Class,AttributeType> */bindings;

    public SimpleTypeBuilder(SimpleTypeFactory factory) {
        this.factory = factory;
        this.typeFactory = new TypeFactoryImpl();
    }

    // Dependency Injection
    //
    public void setSimpleTypeFactory(SimpleTypeFactory factory) {
        this.factory = factory;
    }

    public SimpleTypeFactory getSimpleTypeFactory() {
        return factory;
    }

    // Creation Methods
    //
    /**
     * Creation of simple feature.
     * 
     * @return SimpleFeatureType created
     */
    public SimpleFeatureType feature() {
        AttributeType geom = lookUp(getGeometryName());
        Name name = typeName();
        List descriptors = getDescriptors();
        AttributeDescriptor defaultGeometry = null;
        CoordinateReferenceSystem crs2 = getCRS();
        Set restrictions = restrictions();
        InternationalString description = getDescription();
        SimpleFeatureType type = factory.createSimpleFeatureType(name, descriptors,
                defaultGeometry, crs2, restrictions, description);
        reset();
        return type;
    }

    /**
     * Creation of simple feature collection.
     * 
     * @return SimpleFeatureCollectionType created
     */
    public SimpleFeatureCollectionType collection() {
        SimpleFeatureCollectionType type = getSimpleTypeFactory()
                .createSimpleFeatureCollectionType(typeName(), memberType, getDescription());
        reset();
        return type;
    }

    // Builder State
    //
    /** Reset the builder for new conent */
    public void init() {
        this.description = null;
        this.defaultGeom = null;
        this.local = null;
        this.memberType = null;
        this.uri = null;
        this.crs = null;
        this.attributes = null;

    }

    public void init(PropertyType type) {
        init();
        if (type == null)
            return;

        uri = type.getName().getNamespaceURI();
        local = type.getName().getLocalPart();
        description = type.getDescription();
        restrictions = null;
        restrictions().addAll(type.getRestrictions());

        if (type instanceof SimpleFeatureType) {
            SimpleFeatureType feature = (SimpleFeatureType) type;
            attributes = newList((List) feature.attributes());
        }
        if (type instanceof SimpleFeatureCollectionType) {
            SimpleFeatureCollectionType collection = (SimpleFeatureCollectionType) type;
            attributes = Collections.EMPTY_LIST; // will prevent any addition
            // of attributes
            this.memberType = collection.getMemberType();
        }
    }

    /**
     * Reset is called after creation a "new" type.
     * <p>
     * The following informatoin is reset:
     * <ul>
     * <li>local = local part of name
     * <li>attributes (aka structural properties)
     * <li>default geometry
     * </ul>
     */
    public void reset() {
        this.local = null;
        this.attributes = newList(attributes);
        this.defaultGeom = null;
    }

    // Naming
    //
    public void setNamespaceURI(String namespace) {
        this.uri = namespace;
    }

    public String getNamespaceURI() {
        return uri;
    }

    public void setName(String name) {
        this.local = name;
    }

    public SimpleTypeBuilder name(String name) {
        setName(name);
        return this;
    }

    public String getName() {
        return local;
    }

    public void setDescription(InternationalString description) {
        this.description = description;
    }

    public InternationalString getDescription() {
        return description;
    }

    /**
     * Used to lookup AttributeType for provided binding.
     * 
     * @param binding
     * @return AttributeType
     */
    public AttributeType getBinding(Class binding) {
        return (AttributeType) bindings().get(binding);
    }

    /**
     * Used to provide a specific type for provided binding.
     * <p>
     * You can use this method to map the AttributeType used when addAttribute(
     * String name, Class binding ) is called.
     * 
     * @param binding
     * @param type
     */
    public void addBinding(Class binding, AttributeType type) {
        bindings().put(binding, type);
    }

    /**
     * Load the indicated schema to map Java class to your Type System. (please
     * us a profile to prevent binding conflicts).
     * 
     * @param schema
     */
    public void load(Schema schema) {
        for (Iterator itr = schema.values().iterator(); itr.hasNext();) {
            AttributeType type = (AttributeType) itr.next();
            addBinding(type.getBinding(), type);
        }
    }

    // Attributes
    //
    /**
     * Access to attributes used by builder.
     * <p>
     * You can use this method to perform collection opperations before
     * construction. This is most useful when initializing the builder with a
     * known type, performing modifications, and then creating a derrived type.
     * </p>
     * 
     * @return List of attributes used for creation
     */
    public List getAttributes() {
        if (attributes == null) {
            attributes = newList();
        }
        return attributes;
    }

    private List getDescriptors() {
        List types = getAttributes();
        List descriptors = new ArrayList(types.size());
        AttributeType type;
        AttributeDescriptor descriptor;
        for (Iterator it = types.iterator(); it.hasNext();) {
            type = (AttributeType) it.next();
            descriptor = new AttributeDescriptorImpl(type, Types.typeName(type.getName()), 0,
                    1, true, null);
            descriptors.add(descriptor);
        }
        return descriptors;
    }

    /**
     * Allow for user supplied list implementaion used for attributes.
     * <p>
     * Examples of useful attribute lists:
     * <ul>
     * <li>ArrayList - fixed length, use new ArrayList( size ) when known
     * <li>LinkedList etc...
     * </ul>
     * The list class used here should also be used for feature contents.
     * </p>
     * 
     * @param attributes
     *            List implementation used to organize attributes
     */
    public void setAttributes(List attributes) {
        this.attributes = attributes;
    }

    public SimpleTypeBuilder attribute(AttributeType type) {
        addAttribute(type);
        return this;
    }

    /**
     * Adds a new AttributeType w/ provided name and binding.
     * <p>
     * The binding will be used to locate an AttributeType to use as a
     * prototype, or will be used directly if a binding cannot be found.
     * 
     * @param name
     * @param bind
     * @return SimpleTypeBuilder for use with chaining
     */
    public SimpleTypeBuilder attribute(String name, Class bind) {
        addAttribute(name, bind);
        return this;
    }

    public SimpleTypeBuilder geometry(String name, Class bind) {
        addGeometry(name, bind);
        return this;
    }

    public void addAttribute(AttributeType type) {
        // simple feature type => attribute name == type name, so create a new
        // type with the same name as the attribute, which extends the old
        // type
        attributes().add(type);
    }

    /**
     * Adds a new AttributeType w/ provided name and binding.
     * <p>
     * The binding will be used to locate an AttributeType to use as a
     * prototype, or will be used directly if a binding cannot be found.
     * 
     * @param name
     * @param bind
     * @return SimpleTypeBuilder for use with chaining
     */
    public void addAttribute(String name, Class bind) {
        AttributeType prototype = getBinding(bind);
        AttributeType type;
        Name typeName = Types.typeName(name);
        if (prototype != null) {
            if (prototype instanceof GeometryType) {
                type = createPrototype(typeName, prototype, crs);
            } else {
                type = createPrototype(typeName, prototype);
            }
        } else {
            type = createType(typeName, bind);
        }
        addAttribute(type);
    }

    /**
     * Add a new GeometryAttributeType w/ provided name and binding.
     * <p>
     * A GeometryAttribute will be created in the same manner as for
     * addAttribute with the addition of the CRS.
     * 
     * @param name
     * @param binding
     */
    public void addGeometry(String name, Class bind) {
        AttributeType prototype = getBinding(bind);
        AttributeType type;
        Name typeName = Types.typeName(name);
        if (prototype != null) {
            type = createPrototype(typeName, prototype, crs);
        } else {
            type = createType(typeName, bind, crs);
        }
        addAttribute(type);
    }

    /** Indicate "default" geometry by attributeName */
    public SimpleTypeBuilder geometry(String attributeName) {
        setGeometryName(attributeName);
        return this;
    }

    public void setGeometryName(String type) {
        defaultGeom = type;
    }

    /**
     * Return the current defaultGeometry.
     * <p>
     * This is the name of the AttributeType that will be used for the
     * FeatureType to be created. You make explicitly set a string, or let the
     * first GeometryType found be used as the default.
     * </p>
     * 
     * @return name of the default GeometryAttributeType
     */
    public String getGeometryName() {
        if (defaultGeom == null) {
            for (Iterator i = attributes.iterator(); i.hasNext();) {
                AttributeType attribute = (AttributeType) i.next();
                if (attribute instanceof GeometryType) {
                    return attribute.getName().getLocalPart();
                }
            }
            return null;
        }
        return defaultGeom;
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    public SimpleTypeBuilder crs(CoordinateReferenceSystem crs) {
        setCRS(crs);
        return this;
    }

    /**
     * Uses CRS utility class with buildres TypeFactory.getCRSFactory to look up
     * a CoordinateReferenceSystem based on the provied srs.
     * <p>
     * A SpatialReferenceSystem can be one of the following:
     * <ul>
     * <li>"AUTHORITY:CODE"
     * <li>Well Known Text
     * </ul>
     * 
     * @param srs
     * @return TypeBuilder ready for chaining
     * @throws IllegalArgumentException
     *             When SRS not understood
     */
    public SimpleTypeBuilder crs(String SRS) {
        try {
            setCRS(CRS.decode(SRS));
        } catch (Exception e) {
            throw new IllegalArgumentException("SRS '" + SRS + "' unknown:" + e);
        }
        return this;
    }

    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    public SimpleTypeBuilder member(SimpleFeatureType memberType) {
        setMember(memberType);
        return this;
    }

    public void setMember(SimpleFeatureType memberType) {
        this.memberType = memberType;
    }

    // Bindings
    //

    public void load(SimpleSchema schema) {
        for (Iterator itr = schema.values().iterator(); itr.hasNext();) {
            AttributeType type = (AttributeType) itr.next();
            addBinding(type.getBinding(), type);
        }
    }

    // Factory method argument preparation
    //
    /**
     * Naming: Accessor which returns type name as follows:
     * <ol>
     * <li>If <code>typeName</code> has been set, its value is returned.
     * <li>If <code>name</code> has been set, it + <code>namespaceURI</code>
     * are returned.
     * </ol>
     * 
     */
    protected Name typeName() {
        if (local == null)
            return null;
        return Types.typeName(uri, local);
    }

    /**
     * Grab property collection as an argument to factory method.
     * <p>
     * This may return a copy as needed, since most calls to a factory method
     * end up with a reset this seems not be needed at present.
     * </p>
     */
    protected Collection attributes() {
        if (attributes == null) {
            attributes = newList();
        }
        return attributes;
    }

    protected Set restrictions() {
        if (restrictions == null) {
            restrictions = newSet();
        }
        return restrictions;
    }

    /**
     * Accessor for bindings.
     */
    protected Map bindings() {
        if (bindings == null) {
            bindings = new HashMap();
        }
        return bindings;
    }

    // protected AssociationDescriptor contentsDescriptor() {
    // AssociationType assocType = factory.createAssociationType(memberType
    // .getName(), memberType, false, false, Collections.EMPTY_SET,
    // null, null);
    // // Q: not sure if we should be creating null names here?
    // // A: just use "memberOf" unless they say different...
    // return factory.createAssociationDescriptor(assocType, Types
    // .typeName("memberType"), 0, Integer.MAX_VALUE);
    // }

    // Utility Methods
    // (Subclass may customize)
    //
    protected Set newSet() {
        return new HashSet();
    }

    /**
     * Template method to enable subclasses to customize the list implementation
     * used by "default".
     * 
     * @return List (subclass may override)
     */
    protected List newList() {
        return new ArrayList();
    }

    /**
     * Provides an empty copy of the provided origional list.
     * <p>
     * This method is used by reset for the following goals:
     * <ul>
     * <li>use the user supplied collection directly by the TypeFactory,
     * <li>remember the user supplied collection type for subsequent builder
     * use
     * </ul>
     * This allows a user to indicate that attributes are stored in a
     * "LinkedList" once.
     * 
     * @param origional
     *            Origional collection
     * @return New instance of the originoal Collection
     */
    protected List newList(List origional) {
        if (origional == null) {
            return newList();
        }
        if (origional == Collections.EMPTY_LIST) {
            return newList();
        }
        try {
            return (List) origional.getClass().newInstance();
        } catch (InstantiationException e) {
            return newList();
        } catch (IllegalAccessException e) {
            return newList();
        }
    }

    private AttributeType lookUp(String name) {
        if (name == null)
            return null;
        for (Iterator i = attributes.iterator(); i.hasNext();) {
            AttributeType attributeType = (AttributeType) i.next();
            if (name.equals(attributeType.getName().getLocalPart())) {
                return attributeType;
            }
        }
        return null;
    }

    protected AttributeType createPrototype(Name typeName, AttributeType proto) {
        return typeFactory.createAttributeType(typeName, proto.getBinding(), false, false,
                Collections.EMPTY_SET, null, null);
    }

    protected GeometryType createPrototype(Name typeName, AttributeType proto,
            CoordinateReferenceSystem crs) {
        return typeFactory.createGeometryType(typeName, proto.getBinding(), crs, false, false,
                Collections.EMPTY_SET, null, null);
    }

    /**
     * Create an AttributeType bound to this Java class. Attribute Type created
     * with:
     * <ul>
     * <li>name: typeName
     * <li>binding: bind
     * <li>
     * </ul>
     * Subclass may override.
     * 
     * @param typeName
     *            Name of attribute type to create
     * @param bind
     * @return
     */
    protected AttributeType createType(Name typeName, Class bind) {
        return typeFactory.createAttributeType(typeName, bind, false, false, Collections.EMPTY_SET,
                null, null);
    }

    protected GeometryType createType(Name typeName, Class bind, CoordinateReferenceSystem crs) {
        return typeFactory.createGeometryType(typeName, bind, crs, false, false,
                Collections.EMPTY_SET, null, null);
    }
}
