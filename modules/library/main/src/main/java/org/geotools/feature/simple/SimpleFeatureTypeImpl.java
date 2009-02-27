/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.feature.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.feature.type.ComplexTypeImpl;
import org.geotools.feature.type.FeatureTypeImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.util.InternationalString;

/**
 * Implementation fo SimpleFeatureType, subtypes must be atomic and are stored
 * in a list.
 * 
 * @author Justin
 * @author Ben Caradoc-Davies, CSIRO Exploration and Mining
 */
public class SimpleFeatureTypeImpl extends FeatureTypeImpl implements
        SimpleFeatureType {

    // list of types
    List<AttributeType> types = null;

    // the property descriptors for this type (never null)
    final List<AttributeDescriptor> descriptors;

    Map<String, Integer> index;

    public SimpleFeatureTypeImpl(Name name, List<AttributeDescriptor> schema,
            GeometryDescriptor defaultGeometry, boolean isAbstract,
            List<Filter> restrictions, AttributeType superType,
            InternationalString description) {
        super(name, (List) schema, defaultGeometry, isAbstract, restrictions,
                superType, description);
        // ensure immutability by making unmodifiable private copy
        descriptors = Collections.unmodifiableList(new ArrayList<AttributeDescriptor>(schema));
        index = buildIndex(this);
    }

    public final List<AttributeDescriptor> getAttributeDescriptors() {
        /*
         * This method and getDescriptors() are final to ensure that the returned properties have
         * consistent iteration order.
         */
        return descriptors;
    }

    public List<AttributeType> getTypes() {
        if (types == null) {
            synchronized (this) {
                if (types == null) {
                    types = new ArrayList<AttributeType>();
                    for (Iterator<AttributeDescriptor> itr = descriptors
                            .iterator(); itr.hasNext();) {
                        AttributeDescriptor ad = itr.next();
                        types.add(ad.getType());
                    }
                }
            }
        }

        return types;
    }

    public AttributeType getType(Name name) {
        AttributeDescriptor attribute = (AttributeDescriptor) getDescriptor(name);
        if (attribute != null) {
            return attribute.getType();
        }

        return null;
    }

    public AttributeType getType(String name) {
        AttributeDescriptor attribute = (AttributeDescriptor) getDescriptor(name);
        if (attribute != null) {
            return attribute.getType();
        }

        return null;
    }

    public AttributeType getType(int index) {
        return getTypes().get(index);
    }

    public AttributeDescriptor getDescriptor(Name name) {
        return (AttributeDescriptor) super.getDescriptor(name);
    }

    public AttributeDescriptor getDescriptor(String name) {
        return (AttributeDescriptor) super.getDescriptor(name);
    }

    public AttributeDescriptor getDescriptor(int index) {
        return descriptors.get(index);
    }

    public int indexOf(Name name) {
        if(name.getNamespaceURI() == null)
            return indexOf(name.getLocalPart());
        
        // otherwise do a full scan
        int index = 0;
        for (Iterator<AttributeDescriptor> itr = getAttributeDescriptors().iterator(); itr.hasNext(); index++) {
            AttributeDescriptor descriptor = (AttributeDescriptor) itr.next();
            if (descriptor.getName().equals(name)) {
                return index;
            }
        }
        return -1;
    }

    public int indexOf(String name) {
        Integer idx = index.get(name);
        if(idx != null)
            return idx.intValue();
        else
            return -1;
    }

    public int getAttributeCount() {
        return descriptors.size();
    }

    public String getTypeName() {
        return getName().getLocalPart();
    }

    /**
     * Builds the name -> position index used by simple features for fast attribute lookup
     * @param featureType
     * @return
     */
    static Map<String, Integer> buildIndex(SimpleFeatureType featureType) {
        // build an index of attribute name to index
        Map<String, Integer> index = new HashMap<String, Integer>();
        int i = 0;
        for (AttributeDescriptor ad : featureType.getAttributeDescriptors()) {
            index.put(ad.getLocalName(), i++);
        }
        if (featureType.getGeometryDescriptor() != null) {
            index.put(null, index.get(featureType.getGeometryDescriptor()
                    .getLocalName()));
        }
        return index;
    }

    /**
     * @see org.geotools.feature.type.ComplexTypeImpl#getDescriptors()
     */
    @SuppressWarnings("unchecked")
    @Override
    public final Collection<PropertyDescriptor> getDescriptors() {
        /*
         * This method and getAttributeDescriptors() are final to ensure that the returned
         * properties have consistent iteration order. ComplexTypeImpl.getDescriptors() must not be
         * used because iteration order is different.
         * 
         * In this method, we circumvent the generics type system with a double cast. In general, as
         * discussed in the Sun Java Tutorials
         * (http://java.sun.com/docs/books/tutorial/java/generics/subtyping.html),
         * Collection<PropertyDescriptor> is not the superclass of List<AttributeDescriptor>,
         * because we could cast an instance of the latter to the former type and add a
         * PropertyDescriptor that is not an AttributeDescriptor, breaking the contract of the
         * subclass. This is why casting from List<AttributeDescriptor> to
         * Collection<PropertyDescriptor> is prohibited by the generics type system. However, if the
         * collection is immutable (as is the "descriptors" member), the contract-breaking cast/add
         * cannot occur, List<AttributeDescriptor> is formally substitutable for
         * Collection<PropertyDescriptor>, and thus can be treated as a subtype without breaking
         * generics.
         */
        return (Collection) descriptors;
    }

    /**
     * Hash code based on super and list of properties. Needed because
     * {@link ComplexTypeImpl#hashCode()} does not observe property order (it uses
     * {@link Map#equals(Object)} semantics).
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 47;
        int result = super.hashCode();
        // need more because super.hashCode() does not respect order
        result = prime * result + descriptors.hashCode();
        return result;
    }

    /**
     * Equality based on super and list of properties. Needed because
     * {@link ComplexTypeImpl#equals()} does not observe property order (it uses
     * {@link Map#equals(Object)} semantics).
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleFeatureTypeImpl other = (SimpleFeatureTypeImpl) obj;
        if (!descriptors.equals(other.descriptors))
            return false;
        return true;
    }

}
