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

package org.geotools.feature.iso.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

/**
 * Pure implementation of SimpleFeatureCollection.
 * <p>
 * Because a SimpleFeatureCollection cannot contain attributes many methods are
 * stubbed to return appropriate null values, or throw
 * IllegalArgumentExceptions.
 * </p>
 * To make use of this class pelase extend with the following:
 * <ul>
 * <li>openIterator()
 * <li>closeIterator()
 * <li>size()
 * </ul>
 * 
 * @author Jody Garnett (Refractions Research)
 */
public abstract class AbstractSimpleFeatureCollection extends
        AbstractResourceCollection implements SimpleFeatureCollection {

    SimpleFeatureCollectionType type;

    String id;

    BoundingBox bbox;

    Map userData;

    public AbstractSimpleFeatureCollection(SimpleFeatureCollectionType type,
            String id) {
        this.type = type;
        this.id = id;
        this.bbox = null;
    }

    public FeatureType getMemberType() {
        return type.getMemberType();
    }

    public Object getUserData(Object key) {
        if (userData != null) {
            return userData.get(key);
        }
        return null;
    }

    public void putUserData(Object key, Object value) {
        if (userData == null) {
            userData = new HashMap();
        }
        userData.put(key, value);
    }

    public FeatureCollection subCollection(org.opengis.filter.Filter filter) {
        return new SubFeatureCollection(this, filter, null);
    }

    public FeatureCollection sort(org.opengis.filter.sort.SortBy order) {
        throw new UnsupportedOperationException();
    }

    public Collection memberTypes() {
        return Collections.singleton(type);
    }

    public void putClientProperty(Object key, Object value) {
    }

    public Object getClientProperty(Object key) {
        return null;
    }

    public SimpleFeatureCollectionType getFeatureCollectionType() {
        return type;
    }
    
    public AttributeType getType(){
    	return getFeatureCollectionType();
    }

    public boolean nillable() {
        return false;
    }

    public String getID() {
        return id;
    }

    public CoordinateReferenceSystem getCRS() {
        return type.getCRS();
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the bounding box for the features in this feature collection.
     * 
     * @return the envelope of the geometries contained by this feature
     *         collection.
     */
    public BoundingBox getBounds() {
        /*
        if (bbox == null) {
            BoundsVisitor bounds = new BoundsVisitor();

            accepts(bounds, null);
            bbox = bounds.getBounds();
        }
        */
        return bbox;
    }

    public GeometryAttribute getDefaultGeometry() {
        return null;
    }

    public void setDefaultGeometry(GeometryAttribute geom) {
    }

    public void setValue(List newValue) {
        throw new IllegalArgumentException(
                "SimpleFeatureCollection does not support properties");
    }

    public List getValue(Name name) {
        return Collections.EMPTY_LIST;
    }

    public AttributeDescriptor getDescriptor() {
        return null;
    }

    public PropertyDescriptor descriptor() {
        return null;
    }

    public Name name() {
        return type.getName();
    }

    public Object getValue() {
        return Collections.EMPTY_LIST;
    }

    public Collection attributes() {
        return Collections.EMPTY_LIST;
    }

    public Collection associations() {
        return Collections.EMPTY_LIST;
    }

    public void setValue(Object newValue) throws IllegalArgumentException {
        throw new IllegalArgumentException(
                "SimpleFeatureCollection does not support properties");
    }

    /**
     * Accepts a visitor, which then visits each feature in the collection.
     */
    public void accepts(FeatureVisitor visitor, ProgressListener progress) {
        Iterator iterator = null;
        // if( progress == null ) progress = new NullProgressListener();
        try {
            float size = size();
            float position = 0;
            progress.started();
            visitor.init(type);
            for (iterator = iterator(); !progress.isCanceled()
                    && iterator.hasNext(); progress.progress(position++ / size)) {
                try {
                    Feature feature = (Feature) iterator.next();
                    visitor.visit(feature);
                } catch (Exception erp) {
                    progress.exceptionOccurred(erp);
                }
            }
        } finally {
            progress.complete();
            close(iterator);
        }
    }
}
