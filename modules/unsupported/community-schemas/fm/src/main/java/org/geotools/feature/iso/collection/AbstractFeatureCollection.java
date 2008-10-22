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
import java.util.Iterator;
import java.util.List;

import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

/**
 * Abstract feature collection to be used as base for FeatureCollection
 * implementations. <br>
 * <p>
 * Subclasses must implement the following methods:
 * <ul>
 * <li>{@link Collection#size()}
 * <li>{@link org.geotools.feature.collection.AbstractResourceCollection#openIterator()}
 * <li>{@link org.geotools.feature.collection.AbstractResourceCollection#closeIterator(Iterator)}
 * </ul>
 * </p>
 * <br>
 * <p>
 * This implementation of FeatureCollection uses a delegate to satisfy the
 * methods of the {@link Feature} interface.
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 * 
 */
public abstract class AbstractFeatureCollection extends
        AbstractResourceCollection implements FeatureCollection {

    private FeatureState delegate;

    protected AbstractFeatureCollection(Collection values,
            AttributeDescriptor descriptor, String id) {
        this.delegate = new FeatureState(values, descriptor, id, this);
    }

    protected AbstractFeatureCollection(Collection values,
            FeatureCollectionType type, String id) {
        this.delegate = new FeatureState(values, type, id, this);
    }

    public FeatureCollection subCollection(org.opengis.filter.Filter filter) {
        // TODO: inject a filter factory
        return new SubFeatureCollection(this, filter, null);
    }

    public FeatureCollection sort(org.opengis.filter.sort.SortBy order) {
        throw new UnsupportedOperationException();
    }

    public Collection memberTypes() {
        return ((FeatureCollectionType) getType()).getMembers();
    }

    public void putUserData(Object key, Object value) {
        delegate.putUserData(key, value);
    }

    public Object getUserData(Object key) {
        return delegate.getUserData(key);
    }

    public AttributeType getType() {
        return delegate.getType();
    }

    public boolean nillable() {
        return delegate.nillable();
    }

    public String getID() {
        return delegate.getID();
    }

    public CoordinateReferenceSystem getCRS() {
        return delegate.getCRS();
    }

    public void setCRS(CoordinateReferenceSystem crs) {
        delegate.setCRS(crs);
    }

    public BoundingBox getBounds() {
        return delegate.getBounds();
    }

    public GeometryAttribute getDefaultGeometry() {
        return delegate.getDefaultGeometry();
    }

    public void setDefaultGeometry(GeometryAttribute geom) {
        delegate.setDefaultGeometry(geom);
    }

    public void setValue(List newValue) throws IllegalArgumentException {
        delegate.setValue(newValue);
    }

    public List get(Name name) {
        return delegate.get(name);
    }

    public AttributeDescriptor getDescriptor() {
        return delegate.getDescriptor();
    }

    public PropertyDescriptor descriptor() {
        return delegate.descriptor();
    }

    public Name name() {
        return delegate.name();
    }

    public Object getValue() {
        return delegate.getValue();
    }

    public Collection attributes() {
        return delegate.attributes();
    }

    public Collection associations() {
        return delegate.associations();
    }

    public void setValue(Object newValue) throws IllegalArgumentException {
        delegate.setValue(newValue);
    }

    /**
     * Accepts a visitor, which then visits each feature in the collection.
     */
    public void accepts(FeatureVisitor visitor, ProgressListener progress) {
        Iterator iterator = null;
        if (progress == null)
            progress = new NullProgressListener();
        try {
            float size = size();
            float position = 0;
            progress.started();
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

    public Object operation(Name arg0, List arg1) {
        throw new UnsupportedOperationException("operation not supported yet");
    }
}