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

import java.util.Iterator;
import java.util.List;

import org.geotools.feature.iso.FeatureImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;

/**
 * An implementation of the SimpleFeature convience methods ontop of
 * FeatureImpl.
 * 
 * @author Justin
 */
public class SimpleFeatureImpl extends FeatureImpl implements SimpleFeature {

    public SimpleFeatureImpl(List values, AttributeDescriptor desc, String id) {
        super(values, desc, id);
    }

    public SimpleFeatureImpl(List values, SimpleFeatureType type, String id) {
        super(values, type, id);
    }

    /**
     * Create a Feature with the following content.
     * 
     * @param values
     *            Values in agreement with provided type
     * @param type
     *            Type of feature to be created
     * @param id
     *            Feature ID
     */
    public SimpleFeatureImpl(SimpleFeatureType type, String id, Object[] values) {
        this(SimpleFeatureFactoryImpl.attributes(type, values), type, id);
    }

    /**
     * Retrive value by attribute name.
     * 
     * @param name
     * @return Attribute Value associated with name
     */
    public Object getValue(String name) {
        for (Iterator itr = super.properties.iterator(); itr.hasNext();) {
            Attribute att = (Attribute) itr.next();
            AttributeType type = att.getType();
            String attName = type.getName().getLocalPart();
            if (attName.equals(name)) {
                return att.getValue();
            }
        }
        return null;
    }

    public Object getValue(AttributeType type) {
        if (!super.types().contains(type)) {
            throw new IllegalArgumentException("this feature content model has no type " + type);
        }
        for (Iterator itr = super.properties.iterator(); itr.hasNext();) {
            Attribute att = (Attribute) itr.next();
            if (att.getType().equals(type)) {
                return att.getValue();
            }
        }
        throw new Error();
    }

    /**
     * Access attribute by "index" indicated by SimpleFeatureType.
     * 
     * @param index
     * @return
     */
    public Object getValue(int index) {
        Attribute att = (Attribute) super.properties.get(index);
        return att == null ? null : att.getValue();
        // return values().get(index);
    }

    /**
     * Modify attribute with "name" indicated by SimpleFeatureType.
     * 
     * @param name
     * @param value
     */
    public void setValue(String name, Object value) {
        AttributeType type = ((SimpleFeatureType) getType()).getType(name);
        List/* <AttributeType> */types = ((SimpleFeatureType)getType()).getTypes();
        int idx = types.indexOf(type);
        if (idx == -1) {
            throw new IllegalArgumentException(name + " is not a feature attribute");
        }
        setValue(idx, value);
    }

    /**
     * Modify attribute at the "index" indicated by SimpleFeatureType.
     * 
     * @param index
     * @param value
     */
    public void setValue(int index, Object value) {
        List/* <Attribute> */contents = (List) super.getValue();
        Attribute attribute = (Attribute) contents.get(index);
        attribute.setValue(value);
        this.setValue(contents);
    }

    public List getAttributes() {
        return (List) getValue();
    }

    public int getNumberOfAttributes() {
        return types().size();
    }

    public List getTypes() {
        return super.types();
    }

    public Object getDefaultGeometryValue() {
        return getDefaultGeometry() != null ? getDefaultGeometry().getValue() : null;
    }

    public void defaultGeometry(Object geometry) {
        if (getDefaultGeometry() != null) {
            getDefaultGeometry().setValue(geometry);
        }
    }

    public Object operation(String arg0, Object arg1) {
        throw new UnsupportedOperationException("operation not supported yet");
    }

    public void setDefaultGeometryValue(Object geometry) {
        GeometryAttribute defaultGeometry = getDefaultGeometry();
        defaultGeometry.setValue(geometry);
    }

    public void setValue(List /* <Attribute> */values) {
        super.setValue(values);
    }

    public void setValues(List values) {
        super.setValue(values);
    }

    public void setValues(Object[] values) {
        if(values == null){
            super.setValue(null);
            return;
        }
        List properties = super.properties;
        for(int i = 0; i < values.length; i++){
            Attribute att = (Attribute) properties.get(i);
            att.setValue(values[i]);
        }
    }
}
