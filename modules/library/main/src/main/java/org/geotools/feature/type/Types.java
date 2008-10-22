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
package org.geotools.feature.type;

import java.util.Collections;
import java.util.Iterator;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.NameImpl;
import org.geotools.util.Converters;
import org.opengis.feature.Attribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.filter.Filter;


/**
 * This is a set of utility methods used when <b>implementing</b> types.
 * <p>
 * This set of classes captures the all important how does it work questions,
 * particularly with respect to super types.
 * </p>
 * FIXME: These methods need a Q&A check to confirm correct use of Super TODO:
 * Cannot tell the difference in intent from FeatureTypes
 * 
 * @author Jody Garnett, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 */
public class Types {

//    /**
//     * Returns The name of attributes defined in the type.
//     * 
//     * @param type
//     *            The type.
//     * 
//     */
//    public static Name[] names(ComplexType type) {
//        ArrayList names = new ArrayList();
//        for (Iterator itr = type.attributes().iterator(); itr.hasNext();) {
//            AttributeDescriptor ad = (AttributeDescriptor) itr.next();
//            names.add(ad.getName());
//        }
//
//        return (Name[]) names.toArray(new Name[names.size()]);
//    }
//
//    /**
//     * Creates an attribute name from a single non-qualified string.
//     * 
//     * @param name
//     *            The name, may be null
//     * 
//     * @return The name in which getLocalPart() == name and getNamespaceURI() ==
//     *         null. Or null if name == null.
//     */
//    public static Name attributeName(String name) {
//        if (name == null) {
//            return null;
//        }
//        return new org.geotools.feature.Name(name);
//    }
//
//    /**
//     * Creates an attribute name from a single non-qualified string.
//     * 
//     * @param name
//     *            The name, may be null
//     * @param namespace
//     *            The scope or namespace, may be null.
//     * 
//     * @return The name in which getLocalPart() == name and getNamespaceURI() ==
//     *         namespace.
//     */
//    public static Name attributeName(String namespace, String name) {
//        return new org.geotools.feature.Name(namespace, name);
//    }
//
//    /**
//     * Creates an attribute name from another name.
//     * 
//     * @param name
//     *            The other name.
//     */
//    public static Name attributeName(Name name) {
//        return new org.geotools.feature.Name(name.getNamespaceURI(), name.getLocalPart());
//    }
//
//    /**
//     * Creates a type name from a single non-qualified string.
//     * 
//     * @param name
//     *            The name, may be null
//     * 
//     * @return The name in which getLocalPart() == name and getNamespaceURI() ==
//     *         null. Or null if name == null.
//     */
//    public static TypeName typeName(String name) {
//        if (name == null) {
//            return null;
//        }
//        return new org.geotools.feature.type.TypeName(name);
//    }
//
//    /**
//     * Creates an attribute name from a single non-qualified string.
//     * 
//     * @param name
//     *            The name, may be null
//     * @param namespace
//     *            The scope or namespace, may be null.
//     * 
//     * @return The name in which getLocalPart() == name and getNamespaceURI() ==
//     *         namespace.
//     */
//    public static TypeName typeName(String namespace, String name) {
//        return new org.geotools.feature.type.TypeName(namespace, name);
//    }
//
//    /**
//     * Creates a type name from another name.
//     * 
//     * @param name
//     *            The other name.
//     */
//    public static TypeName typeName(Name name) {
//        return new org.geotools.feature.type.TypeName(name.getNamespaceURI(), name.getLocalPart());
//    }
//
//    /**
//     * Creates a set of attribute names from a set of strings.
//     * <p>
//     * This method returns null if names == null.
//     * </p>
//     * <p>
//     * The ith name has getLocalPart() == names[i] and getNamespaceURI() == null
//     * </p>
//     */
//    public static Name[] toNames(String[] names) {
//        if (names == null) {
//            return null;
//        }
//        Name[] attributeNames = new Name[names.length];
//
//        for (int i = 0; i < names.length; i++) {
//            attributeNames[i] = attributeName(names[i]);
//        }
//
//        return attributeNames;
//    }
//
//    /**
//     * Creates a set of type names from a set of strings.
//     * <p>
//     * This method returns null if names == null.
//     * </p>
//     * <p>
//     * The ith name has getLocalPart() == names[i] and getNamespaceURI() == null
//     * </p>
//     */
//    public static TypeName[] toTypeNames(String[] names) {
//        if (names == null) {
//            return null;
//        }
//
//        TypeName[] typeNames = new TypeName[names.length];
//
//        for (int i = 0; i < names.length; i++) {
//            typeNames[i] = typeName(names[i]);
//        }
//
//        return typeNames;
//    }
//
//    /**
//     * Convenience method for turning an array of qualified names into a list of
//     * non qualified names.
//     * 
//     */
//    public static String[] fromNames(Name[] attributeNames) {
//        if (attributeNames == null) {
//            return null;
//        }
//
//        String[] names = new String[attributeNames.length];
//        for (int i = 0; i < attributeNames.length; i++) {
//            names[i] = attributeNames[i].getLocalPart();
//        }
//
//        return names;
//    }
//
//    /**
//     * Convenience method for turning an array of qualified names into a list of
//     * non qualified names.
//     * 
//     */
//    public static String[] fromTypeNames(TypeName[] typeNames) {
//        if (typeNames == null)
//            return null;
//
//        String[] names = new String[typeNames.length];
//        for (int i = 0; i < typeNames.length; i++) {
//            names[i] = typeNames[i].getLocalPart();
//        }
//
//        return names;
//    }
//
//    /**
//     * Returns the first descriptor matching the given local name within the
//     * given type.
//     * 
//     * @param type
//     *            The type, non null.
//     * @param name
//     *            The name, non null.
//     * 
//     * @return The first descriptor, or null if no match.
//     */
//    public static PropertyDescriptor descriptor(ComplexType type, String name) {
//        List match = descriptors(type, name);
//
//        if (match.isEmpty())
//            return null;
//
//        return (PropertyDescriptor) match.get(0);
//    }
//
//    /**
//     * Returns the first descriptor matching the given local name within the
//     * given type.
//     * 
//     * @param type
//     *            The type, non null.
//     * @param name
//     *            The name, non null.
//     * 
//     * @return The first descriptor, or null if no match.
//     */
//    public static PropertyDescriptor descriptor(ComplexType type, String name,
//            AttributeType actualType) {
//        List match = descriptors(type, name);
//
//        if (match.isEmpty()) {
//            Collection properties = type.getProperties();
//            for (Iterator it = properties.iterator(); it.hasNext();) {
//                PropertyDescriptor desc = (PropertyDescriptor) it.next();
//                if (!(desc instanceof AttributeDescriptor)) {
//                    continue;
//                }
//                AttributeDescriptor attDesc = (AttributeDescriptor) desc;
//                AttributeType attType = attDesc.getType();
//                if (isSuperType(actualType, attType)) {
//                    return attDesc;
//                }
//            }
//            return null;
//        }
//
//        return (PropertyDescriptor) match.get(0);
//    }
//
//    public static PropertyDescriptor descriptor(ComplexType type, Name name,
//            AttributeType actualType) {
//        List match = descriptors(type, name);
//
//        if (match.isEmpty()) {
//            Collection properties = type.getProperties();
//            for (Iterator it = properties.iterator(); it.hasNext();) {
//                PropertyDescriptor desc = (PropertyDescriptor) it.next();
//                if (!(desc instanceof AttributeDescriptor)) {
//                    continue;
//                }
//                AttributeDescriptor attDesc = (AttributeDescriptor) desc;
//                AttributeType attType = attDesc.getType();
//                if (isSuperType(actualType, attType)) {
//                    return attDesc;
//                }
//            }
//            return null;
//        }
//
//        return (PropertyDescriptor) match.get(0);
//    }
//
//    /**
//     * Returns the first descriptor matching the given name + namespace within
//     * the given type.
//     * 
//     * @param type
//     *            The type, non null.
//     * @param name
//     *            The name, non null.
//     * @param namespace
//     *            The namespace, non null.
//     * 
//     * @return The first descriptor, or null if no match.
//     */
//    public static PropertyDescriptor descriptor(ComplexType type, String name, String namespace) {
//        return descriptor(type, new org.geotools.feature.Name(namespace, name));
//    }
//
//    /**
//     * Returns the first descriptor matching the given name within the given
//     * type.
//     * 
//     * 
//     * @param type
//     *            The type, non null.
//     * @param name
//     *            The name, non null.
//     * 
//     * @return The first descriptor, or null if no match.
//     */
//    public static PropertyDescriptor descriptor(ComplexType type, Name name) {
//        List match = descriptors(type, name);
//
//        if (match.isEmpty())
//            return null;
//
//        return (PropertyDescriptor) match.get(0);
//    }
//
//    /**
//     * Returns the set of descriptors matching the given local name within the
//     * given type.
//     * 
//     * @param type
//     *            The type, non null.
//     * @param name
//     *            The name, non null.
//     * 
//     * @return The list of descriptors named 'name', or an empty list if none
//     *         such match.
//     */
//    public static List/* <PropertyDescriptor> */descriptors(ComplexType type, String name) {
//        if (name == null)
//            return Collections.EMPTY_LIST;
//
//        List match = new ArrayList();
//
//        for (Iterator itr = type.getProperties().iterator(); itr.hasNext();) {
//            PropertyDescriptor descriptor = (PropertyDescriptor) itr.next();
//            String localPart = descriptor.getName().getLocalPart();
//            if (name.equals(localPart)) {
//                match.add(descriptor);
//            }
//        }
//
//        // only look up in the super type if the descriptor is not found
//        // as a direct child definition
//        if (match.size() == 0) {
//            AttributeType superType = type.getSuper();
//            if (superType instanceof ComplexType) {
//                List superDescriptors = descriptors((ComplexType) superType, name);
//                match.addAll(superDescriptors);
//            }
//        }
//        return match;
//    }
//
//    /**
//     * Returns the set of descriptors matching the given name.
//     * 
//     * @param type
//     *            The type, non null.
//     * @param name
//     *            The name, non null.
//     * 
//     * @return The list of descriptors named 'name', or an empty list if none
//     *         such match.
//     */
//    public static List/* <PropertyDescriptor> */descriptors(ComplexType type, Name name) {
//        if (name == null)
//            return Collections.EMPTY_LIST;
//
//        List match = new ArrayList();
//
//        for (Iterator itr = type.getProperties().iterator(); itr.hasNext();) {
//            PropertyDescriptor descriptor = (PropertyDescriptor) itr.next();
//            Name descriptorName = descriptor.getName();
//            if (name.equals(descriptorName)) {
//                match.add(descriptor);
//            }
//        }
//
//        // only look up in the super type if the descriptor is not found
//        // as a direct child definition
//        if (match.size() == 0) {
//            AttributeType superType = type.getSuper();
//            if (superType instanceof ComplexType) {
//                List superDescriptors = descriptors((ComplexType) superType, name);
//                match.addAll(superDescriptors);
//            }
//        }
//        return match;
//    }
//
//    /**
//     * Determines if <code>parent</code> is a super type of <code>type</code>
//     * 
//     * @param type
//     *            The type in question.
//     * @param parent
//     *            The possible parent type.
//     * 
//     */
//    public static boolean isSuperType(AttributeType type, AttributeType parent) {
//        while (type.getSuper() != null) {
//            type = type.getSuper();
//            if (type.equals(parent))
//                return true;
//        }
//
//        return false;
//    }
//
//    /**
//     * Converts content into a format which is used to store it internally
//     * within an attribute of a specific type.
//     * 
//     * @param value
//     *            the object to attempt parsing of.
//     * 
//     * @throws IllegalArgumentException
//     *             if parsing is attempted and is unsuccessful.
//     */
//    public static Object parse(AttributeType type, Object content) throws IllegalArgumentException {
//
//        // JD: TODO: this is pretty lame
//        if (type instanceof AttributeTypeImpl) {
//            AttributeTypeImpl hack = (AttributeTypeImpl) type;
//            Object parsed = hack.parse(content);
//
//            if (parsed != null) {
//                return parsed;
//            }
//        }
//
//        return content;
//    }
//
//    /**
//     * Validates anattribute. <br>
//     * <p>
//     * Same result as calling:
//     * 
//     * <pre>
//     * 	<code>
//     * validate(attribute.type(), attribute)
//     * </code>
//     * </pre>
//     * 
//     * </p>
//     * 
//     * @param attribute
//     *            The attribute.
//     * 
//     * @throws IllegalAttributeException
//     *             In the event that content violates any restrictions specified
//     *             by the attribute.
//     */
//    public static void validate(Attribute attribute) throws IllegalAttributeException {
//
//        validate(attribute, attribute.getValue());
//    }
//
    public static boolean isValid( Attribute attribute ){
        try {
            validate(attribute.getType(), attribute, attribute.getValue(), false );
            return true;
        }
        catch (IllegalAttributeException invalid ){
            return false;
        }
    }
    /**
     * Validates content against an attribute.
     * 
     * @param attribute
     *            The attribute.
     * @param attributeContent
     *            Content of attribute.
     * 
     * @throws IllegalAttributeException
     *             In the event that content violates any restrictions specified
     *             by the attribute.
     */
    public static void validate(Attribute attribute, Object attributeContent)
            throws IllegalAttributeException {

        validate(attribute.getType(), attribute, attributeContent, false);
    }

    public static void validate(AttributeType type, Attribute attribute, Object attributeContent)
            throws IllegalAttributeException {

        validate(type, attribute, attributeContent, false);
    }

    
    protected static void validate(AttributeType type, Attribute attribute,
            Object attributeContent, boolean isSuper) throws IllegalAttributeException {

        if (type == null) {
            throw new IllegalAttributeException("null type");
        }

        if (attributeContent == null) {
            if (!attribute.isNillable()) {
                throw new IllegalAttributeException(type.getName() + " not nillable");
            }
            return;
        }

        if (type.isIdentified() && attribute.getIdentifier() == null) {
            throw new NullPointerException(type.getName() + " is identified, null id not accepted");
        }

        if (!isSuper) {

            // JD: This is an issue with how the xml simpel type hierarchy
            // maps to our current Java Type hiearchy, the two are inconsitent.
            // For instance, xs:integer, and xs:int, the later extend the
            // former, but their associated java bindings, (BigDecimal, and
            // Integer)
            // dont.
            Class clazz = attributeContent.getClass();
            Class binding = type.getBinding();
            if (binding != null && binding != clazz && !binding.isAssignableFrom(clazz)) {
                throw new IllegalAttributeException(clazz.getName()
                        + " is not an acceptable class for " + type.getName()
                        + " as it is not assignable from " + binding);
            }
        }

        if (type.getRestrictions() != null) {
            for (Filter f : type.getRestrictions()) {
                if (!f.evaluate(attribute)) {
                    throw new IllegalAttributeException("Attribute instance (" + attribute.getIdentifier() + ")"
                            + "fails to pass filter: " + f);
                }
            }
        }

        // move up the chain,
        if (type.getSuper() != null) {
            validate(type.getSuper(), attribute, attributeContent, true);
        }
    }
//
//    public static void validate(ComplexAttribute attribute) throws IllegalArgumentException {
//
//    }
//
//    public static void validate(ComplexAttribute attribute, Collection content)
//            throws IllegalArgumentException {
//
//    }
//
//    protected static void validate(ComplexType type, ComplexAttribute attribute, Collection content)
//            throws IllegalAttributeException {
//
//        // do normal validation
//        validate((AttributeType) type, (Attribute) attribute, (Object) content, false);
//
//        if (content == null) {
//            // not really much else we can do
//            return;
//        }
//
//        Collection schema = type.attributes();
//
//        int index = 0;
//        for (Iterator itr = content.iterator(); itr.hasNext();) {
//            Attribute att = (Attribute) itr.next();
//
//            // att shall not be null
//            if (att == null) {
//                throw new NullPointerException("Attribute at index " + index
//                        + " is null. Attributes "
//                        + "can't be null. Do you mean Attribute.get() == null?");
//            }
//
//            // and has to be of one of the allowed types
//            AttributeType attType = att.getType();
//            boolean contains = false;
//            for (Iterator sitr = schema.iterator(); sitr.hasNext();) {
//                AttributeDescriptor ad = (AttributeDescriptor) sitr.next();
//                if (ad.getType().equals(attType)) {
//                    contains = true;
//                    break;
//                }
//            }
//
//            if (!contains) {
//                throw new IllegalArgumentException("Attribute of type " + attType.getName()
//                        + " found at index " + index
//                        + " but this type is not allowed by this descriptor");
//            }
//
//            index++;
//        }
//
//        // empty is allows, in such a case, content should be empty
//        if (type.attributes().isEmpty()) {
//            if (!content.isEmpty()) {
//                throw new IllegalAttributeException(
//                        "Type indicates empty attribute collection, content does not");
//            }
//
//            // we are done
//            return;
//        }
//
//        if (type instanceof SequenceType) {
//            validateSequence((SequenceType) type, attribute, content);
//        } else if (type instanceof ChoiceType) {
//            validateChoice((ChoiceType) type, attribute, content);
//        } else {
//            validateAll(type, attribute, content);
//        }
//
//        if (type.getSuper() != null) {
//            validate((ComplexType) type.getSuper(), attribute, content);
//        }
//    }
//
//    private static void validateSequence(SequenceType type, ComplexAttribute att, Collection content)
//            throws IllegalAttributeException {
//
//        if (!(att instanceof Sequence)) {
//            throw new IllegalAttributeException("Attribute must be instance of: "
//                    + Sequence.class.getName() + " for type instance of: "
//                    + SequenceType.class.getName());
//        }
//
//        // sequence must be instance of list
//        if (!(content instanceof List)) {
//            throw new IllegalAttributeException("Content must be instance of "
//                    + List.class.getName() + " for Sequence");
//        }
//
//        processSequence((List) type.attributes(), (List) content);
//    }
//
//    private static void processSequence(List/* <AttributeDescriptor> */sequence,
//            List/* <Attribute> */content) throws IllegalAttributeException {
//
//        Iterator ditr = sequence.iterator();
//        ArrayList remaining = new ArrayList(content);
//        // seed with first descriptor
//
//        do {
//            AttributeDescriptor ad = (AttributeDescriptor) ditr.next();
//
//            // march through content until name mismatch
//            int occurences = 0;
//            for (Iterator itr = remaining.iterator(); itr.hasNext();) {
//                Attribute a = (Attribute) itr.next();
//                if (ad.getName().equals(a.name())) {
//                    occurences++;
//                    itr.remove();
//                } else {
//                    break;
//                }
//            }
//
//            int min = ad.getMinOccurs();
//            int max = ad.getMaxOccurs();
//            if (occurences < min || occurences > max) {
//                throw new IllegalAttributeException("Found " + occurences + " occurences of "
//                        + ad.getName() + " when between " + min + " and " + max + " expected");
//            }
//
//        } while (ditr.hasNext());
//
//        if (!remaining.isEmpty()) {
//            throw new IllegalAttributeException(
//                    "Extra content found beyond the specified in the schema: " + remaining);
//        }
//    }
//
//    private static void validateChoice(ChoiceType type, ComplexAttribute att, Collection content)
//            throws IllegalAttributeException {
//
//        if (!(att instanceof Choice)) {
//            throw new IllegalAttributeException("Attribute must be instance of: "
//                    + Choice.class.getName() + " for type instance of: "
//                    + ChoiceType.class.getName());
//        }
//
//        processChoice((Set) type.attributes(), content);
//    }
//
//    private static void processChoice(Set/* <AttributeDescriptor> */choice,
//            Collection/* <Attribute> */content) throws IllegalAttributeException {
//
//        // TODO: implement
//    }
//
//    private static void validateAll(ComplexType type, ComplexAttribute att, Collection content)
//            throws IllegalAttributeException {
//        processAll(type.attributes(), content);
//    }
//
//    private static void processAll(Collection/* <AttributeDescriptor> */all,
//            Collection/* <Attribute> */content) throws IllegalAttributeException {
//
//        // TODO: JD: this can be definitley be optimzed, as written its O(n^2)
//
//        // for each descriptor, count occurences of each matching attribute
//        ArrayList remaining = new ArrayList(content);
//        for (Iterator itr = all.iterator(); itr.hasNext();) {
//            AttributeDescriptor ad = (AttributeDescriptor) itr.next();
//
//            int min = ad.getMinOccurs();
//            int max = ad.getMaxOccurs();
//            int occurences = 0;
//
//            for (Iterator citr = remaining.iterator(); citr.hasNext();) {
//                Attribute a = (Attribute) citr.next();
//                if (a.name().equals(ad.getName())) {
//                    occurences++;
//                    citr.remove();
//                }
//            }
//
//            if (occurences < ad.getMinOccurs() || occurences > ad.getMaxOccurs()) {
//                throw new IllegalAttributeException("Found " + occurences + " of " + ad.getName()
//                        + " when type" + "specifies between " + min + " and " + max);
//            }
//        }
//
//        if (!remaining.isEmpty()) {
//            throw new IllegalAttributeException(
//                    "Extra content found beyond the specified in the schema: " + remaining);
//        }
//
//    }

    /**
     * Ensure that attributeContent is a good value for descriptor.
     */
    public static void validate(AttributeDescriptor descriptor,
            Object value) throws IllegalAttributeException {

        if (descriptor == null) {
            throw new NullPointerException("Attribute descriptor required for validation");
        }
        
        if (value == null) {
            if (!descriptor.isNillable()) {
                throw new IllegalArgumentException(descriptor.getName() + " requires a non null value");
            }           
        } else {
            validate( descriptor.getType(), value, false );
        }
    }
    
    /**
     * Do our best to make the provided value line up with the needs of descriptor.
     * <p>
     * This helper method uses the Coverters api to convert the provided
     * value into the required class. If the value is null (and the attribute
     * is not nillable) a default value will be returned.
     * @param descriptor Attribute descriptor we need to supply a value for.
     * @param value The provided value
     * @return Our best attempt to make a valid value
     * @throws IllegalArgumentException if we really could not do it.
     */
    public static Object parse(AttributeDescriptor descriptor, Object value) throws IllegalArgumentException {
        if (value == null){
            if( descriptor.isNillable()){
                return descriptor.getDefaultValue();
            }
        }
        else {
            Class target = descriptor.getType().getBinding(); 
            if ( !target.isAssignableFrom( value.getClass() ) ) {
                // attempt to convert
                Object converted = Converters.convert(value,target);
                if ( converted != null ) {
                    return converted;
                }
//                else {
//                    throw new IllegalArgumentException( descriptor.getLocalName()+ " could not convert "+value+" into "+target);
//                }
            }
        }        
        return value;
    }
    
    protected static void validate(final AttributeType type, final Object value, boolean isSuper) throws IllegalAttributeException {
        if (!isSuper) {
            // JD: This is an issue with how the xml simpel type hierarchy
            // maps to our current Java Type hiearchy, the two are inconsitent.
            // For instance, xs:integer, and xs:int, the later extend the
            // former, but their associated java bindings, (BigDecimal, and
            // Integer)
            // dont.
            Class clazz = value.getClass();
            Class binding = type.getBinding();
            if (binding != null && !binding.isAssignableFrom(clazz)) {
                throw new IllegalAttributeException(clazz.getName()
                        + " is not an acceptable class for " + type.getName()
                        + " as it is not assignable from " + binding);
            }
        }

        if (type.getRestrictions() != null && type.getRestrictions().size() > 0) {
            for (Filter filter : type.getRestrictions()) {
                if (!filter.evaluate(value)) {
                    throw new IllegalAttributeException( type.getName() + " restriction "+ filter + " not met by: " + value);
                }
            }
        }

        // move up the chain,
        if (type.getSuper() != null) {
            validate(type.getSuper(), value, true );
        }
    }
}
