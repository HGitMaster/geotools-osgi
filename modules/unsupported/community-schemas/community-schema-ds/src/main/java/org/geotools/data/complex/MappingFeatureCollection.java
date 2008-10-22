package org.geotools.data.complex;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.geotools.data.Query;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
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

import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.util.ProgressListener;

public class MappingFeatureCollection implements FeatureCollection<FeatureType, Feature> {

    private final ComplexDataStore store;

    private final FeatureTypeMapping mapping;

    private final Query query;

    public MappingFeatureCollection(ComplexDataStore store, FeatureTypeMapping mapping, Query query) {
        this.store = store;
        this.mapping = mapping;
        this.query = query;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#accepts(org.opengis.feature.FeatureVisitor,
     *      org.opengis.util.ProgressListener)
     */
    public void accepts(FeatureVisitor visitor, ProgressListener progress) throws IOException {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#add(org.opengis.feature.Feature)
     */
    public boolean add(Feature obj) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends Feature> collection) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#addAll(org.geotools.feature.FeatureCollection)
     */
    public boolean addAll(FeatureCollection<? extends FeatureType, ? extends Feature> resource) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#addListener(org.geotools.feature.CollectionListener)
     */
    public void addListener(CollectionListener listener) throws NullPointerException {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#clear()
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#close(org.geotools.feature.FeatureIterator)
     */
    public void close(FeatureIterator<Feature> close) {
        close.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#close(java.util.Iterator)
     */
    public void close(Iterator<Feature> close) {
        ((MappingFeatureIterator) close).close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> o) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#features()
     */
    public FeatureIterator<Feature> features() {
        try {
            return new MappingFeatureIterator(store, mapping, query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#getBounds()
     */
    public ReferencedEnvelope getBounds() {
        throw new UnsupportedOperationException();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#getID()
     */
    public String getID() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#getSchema()
     */
    public FeatureType getSchema() {
        return (FeatureType) mapping.getTargetFeature().getType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#isEmpty()
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#iterator()
     */
    public Iterator<Feature> iterator() {
        try {
            return new MappingFeatureIterator(store, mapping, query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#purge()
     */
    public void purge() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#removeListener(org.geotools.feature.CollectionListener)
     */
    public void removeListener(CollectionListener listener) throws NullPointerException {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#size()
     */
    public int size() {
        try {
            return store.getCount(query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#sort(org.opengis.filter.sort.SortBy)
     */
    public FeatureCollection<FeatureType, Feature> sort(SortBy order) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#subCollection(org.opengis.filter.Filter)
     */
    public FeatureCollection<FeatureType, Feature> subCollection(Filter filter) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#toArray()
     */
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.geotools.feature.FeatureCollection#toArray(O[])
     */
    public <O> O[] toArray(O[] a) {
        throw new UnsupportedOperationException();
    }

}
