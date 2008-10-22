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
import java.util.Iterator;
import java.util.List;

import org.geotools.feature.iso.AttributeFactoryImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;

/**
 * This builder will help you put together a SimpleFeature.
 * <p>
 * Since the simple feature is well simple, this class is not very complicated
 * either! You are required to provide a SimpleFeatureFactory in order to use
 * this builder.
 * </p>
 * 
 * @author Justin
 */
public class SimpleFeatureBuilder {

    List attributes = new ArrayList();

    private SimpleFeatureFactory factory;
    
    private FeatureFactory typeFactory;

    private SimpleFeatureType featureType;

    private SimpleFeatureCollectionType collectionType;

    public SimpleFeatureBuilder(SimpleFeatureFactory factory) {
        this.factory = factory;
        this.typeFactory = new AttributeFactoryImpl();
    }

    /**
     * Setter injection for SimpleFeatureFactory. XXX Review? If you do not mean
     * for Setter injection please factory final
     * 
     * @param factory
     */
    public void setSimpleFeatureFactory(SimpleFeatureFactory factory) {
        this.factory = factory;
    }

    public void init() {
        attributes.clear();
        featureType = null;
        collectionType = null;
    }

    public void setType(SimpleFeatureType featureType) {
        this.featureType = featureType;
    }

    public void setType(SimpleFeatureCollectionType collectionType) {
        this.collectionType = collectionType;
    }

    /** Call to add the next attribute to the builder. */
    public void add(Object value) {
        attributes.add(value);
    }

    public Object build(String id) {
        if (featureType != null) {
            return feature(id);
        }
        if (collectionType != null) {
            return collection(id);
        }
        return null;
    }

    public SimpleFeature feature(String id) {
        return factory.createSimpleFeature(properties(), featureType, id);
    }

    private List properties() {
        List properties = new ArrayList(attributes.size());
        List descriptors = featureType.getAttributes();
        int attCount = featureType.getAttributeCount();
        for(int i = 0; i < attCount; i++){
            Object value = attributes.get(i);
            AttributeDescriptor descriptor = (AttributeDescriptor) descriptors.get(i);
            Attribute attribute = create(value, descriptor);
            properties.add(attribute);
        }
        return properties;
    }

    public SimpleFeatureCollection collection(String id) {
        return factory.createSimpleFeatureCollection(collectionType, id);
    }

    protected Attribute create(Object value, AttributeDescriptor descriptor) {
//        if (descriptor != null) {
//            type = descriptor.getType();
//        }
        AttributeType type = (AttributeType) descriptor.type();

        Attribute attribute = null;
        if (type instanceof GeometryType) {
            attribute = factory.createGeometryAttribute(value, descriptor, null, null);
        } else {
            attribute = factory.createAttribute(value, descriptor, null);
        }

        return attribute;
    }

    /**
     * Initialize the builder with the provided feature.
     * <p>
     * This is used to quickly create a "clone", can be used to change between
     * one SimpleFeatureImplementation and another.
     * </p>
     * 
     * @param feature
     */
    public void init(SimpleFeature feature) {
        init();
        this.featureType = (SimpleFeatureType) feature.getType();
        for (Iterator i = feature.attributes().iterator(); i.hasNext();) {
            this.attributes.add(i.next()); // TODO: copy
        }
    }
}
