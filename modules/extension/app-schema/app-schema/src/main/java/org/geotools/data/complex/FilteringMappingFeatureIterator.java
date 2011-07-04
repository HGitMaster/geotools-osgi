/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2010-2011, Open Source Geospatial Foundation (OSGeo)
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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.IteratorUtils;
import org.geotools.data.Query;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

/**
 * An extension to {@linkplain org.geotools.data.complex.DataAccessMappingFeatureIterator} where
 * filter is present. Since join query between 2 or more tables isn't supported, the only way we can
 * query nested features is by applying the filter per simple feature (database row). This is done
 * in hasNext().
 *
 * @author Rini Angreani (CSIRO Earth Science and Resource Engineering)
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/extension/app-schema/app-schema/src/main/java/org/geotools/data/complex/FilteringMappingFeatureIterator.java $
 */
public class FilteringMappingFeatureIterator extends DataAccessMappingFeatureIterator {

    protected ListIterator<SimpleFeature> listFeatureIterator;

    private Filter filter;

    public FilteringMappingFeatureIterator(AppSchemaDataAccess store, FeatureTypeMapping mapping,
            Query query, Query unrolledQuery, Filter filter) throws IOException {
        super(store, mapping, query, false, unrolledQuery);
        this.filter = filter;
    }

    @Override
    protected void initialiseSourceFeatures(FeatureTypeMapping mapping, Query query)
            throws IOException {
        super.initialiseSourceFeatures(mapping, query);
        listFeatureIterator = IteratorUtils.toListIterator(super.getSourceFeatureIterator());
    }

    @Override
    protected void closeSourceFeatures() {
        super.closeSourceFeatures();
        listFeatureIterator = null;
    }

    @Override
    protected ListIterator<SimpleFeature> getSourceFeatureIterator() {
        return listFeatureIterator;
    }

    @Override
    public boolean hasNext() {
        // check that the feature exists
        while (super.hasNext()) {
            // apply filter
            if (filter.evaluate(curSrcFeature)) {
               return true;
            }

            setHasNextCalled(false);
        }
        return false;
    }

    @Override
    protected void setNextFeature(String fId, List<Object> foreignIds, ArrayList<Feature> features) throws IOException {
        int prevCount = 0;
        while (listFeatureIterator.hasPrevious()) {
            Feature prev = listFeatureIterator.previous();
            prevCount++;
            // include other rows that don't match the filter, but matches the id of the
            // matching feature.. for denormalised view
            if (extractIdForFeature(prev).equals(fId)) {
                features.add(prev);
            } else {
                break;
            }
        }
        // get back to the original position
        for (int i = 0; i < prevCount; i++) {
            listFeatureIterator.next();
        }
        // then add next features to same id
        super.setNextFeature(fId, foreignIds, features);
    }
}
