/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.feature.type.FeatureTypeImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
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
    List<AttributeType> types = null;

    List<AttributeDescriptor> descriptors;

    Map<String, Integer> index;

    public SimpleFeatureTypeImpl(Name name, List<AttributeDescriptor> schema,
            GeometryDescriptor defaultGeometry, boolean isAbstract,
            List<Filter> restrictions, AttributeType superType,
            InternationalString description) {
        super(name, (List) schema, defaultGeometry, isAbstract, restrictions,
                superType, description);
        descriptors = schema;
        index = buildIndex(this);
    }

    public List<AttributeDescriptor> getAttributeDescriptors() {
        return Collections.unmodifiableList(descriptors);
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

}
