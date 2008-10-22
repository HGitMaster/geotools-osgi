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

package org.geotools.data.complex;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.geotools.data.DataAccess;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.collection.AbstractFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;

/**
 * A FeatureSource2 that uses a {@linkplain org.geotools.data.complex.FeatureTypeMapping} to perform
 * Feature fetching.
 * 
 * <p>
 * Note that the number of Features available from a MappingFeatureReader may not match the number
 * of features that resulted of executing the incoming query over the surrogate FeatureSource. This
 * will be the case when grouping attributes has configured on the FeatureTypeMapping this reader is
 * based on.
 * </p>
 * <p>
 * When a MappingFeatureReader is created, a delegated FeatureIterator will be created based on the
 * information provided by the FeatureTypeMapping object. That delegate reader will be specialized
 * in applying the appropiate mapping stratagy based on wether grouping has to be performed or not.
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: MappingFeatureSource.java 31611 2008-10-02 08:16:20Z bencd $
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/2.4.x/modules/unsupported/community-schemas/community-schema-ds/src/main/java/org/geotools/data/complex/MappingFeatureSource.java $
 * @since 2.4
 */
class MappingFeatureSource implements FeatureSource<FeatureType, Feature> {

    private ComplexDataStore store;

    private FeatureTypeMapping mapping;

    public MappingFeatureSource(ComplexDataStore store, FeatureTypeMapping mapping) {
        this.store = store;
        this.mapping = mapping;
    }

    public void addFeatureListener(FeatureListener listener) {
        throw new UnsupportedOperationException();
    }

    public ReferencedEnvelope getBounds() throws IOException {
        return store.getBounds(namedQuery(Filter.INCLUDE, Integer.MAX_VALUE));
    }

    private DefaultQuery namedQuery(Filter filter, int countLimit) {
        DefaultQuery query = new DefaultQuery();
        query.setTypeName(getName().getLocalPart());
        query.setFilter(filter);
        query.setMaxFeatures(countLimit);
        return query;
    }

    private DefaultQuery namedQuery(Query query) {
        DefaultQuery namedQuery = namedQuery(query.getFilter(), query.getMaxFeatures());
        namedQuery.setPropertyNames(query.getPropertyNames());
        namedQuery.setCoordinateSystem(query.getCoordinateSystem());
        namedQuery.setHandle(query.getHandle());
        namedQuery.setMaxFeatures(query.getMaxFeatures());
        namedQuery.setSortBy(query.getSortBy());
        return namedQuery;
    }

    public ReferencedEnvelope getBounds(Query query) throws IOException {
        DefaultQuery namedQuery = namedQuery(query);
        return store.getBounds(namedQuery);
    }

    public int getCount(Query query) throws IOException {
        DefaultQuery namedQuery = namedQuery(query);
        int count = store.getCount(namedQuery);
        return count;
    }

    public DataAccess<FeatureType, Feature> getDataStore() {
        return store;
    }

    public FeatureType getSchema() {
        return (FeatureType) mapping.getTargetFeature().getType();
    }

    public FeatureCollection<FeatureType, Feature> getFeatures(Query query) throws IOException {
        return new MappingFeatureCollection(store, mapping, namedQuery(query));
    }

    public FeatureCollection<FeatureType, Feature> getFeatures(Filter filter) throws IOException {
        return new MappingFeatureCollection(store, mapping, namedQuery(filter, Integer.MAX_VALUE));
    }

    public FeatureCollection<FeatureType, Feature> getFeatures() throws IOException {
        return new MappingFeatureCollection(store, mapping, namedQuery(Filter.INCLUDE,
                Integer.MAX_VALUE));
    }

    public void removeFeatureListener(FeatureListener listener) {
        throw new UnsupportedOperationException("this is a read only feature source");
    }

    public Object describe() {
        return mapping.getTargetFeature();
    }

    public void dispose() {
        store.dispose();
    }

    public FilterCapabilities getFilterCapabilities() {
        throw new UnsupportedOperationException();
    }

    public ResourceInfo getInfo() {
        throw new UnsupportedOperationException();
    }

    public Name getName() {
        Name name = mapping.getTargetFeature().getName();
        return name;
    }

    public void setTransaction(Transaction t) {
        throw new UnsupportedOperationException();
    }

    public Set getSupportedHints() {
        return Collections.EMPTY_SET;
    }

    public QueryCapabilities getQueryCapabilities() {
        return new QueryCapabilities();
    }

}
