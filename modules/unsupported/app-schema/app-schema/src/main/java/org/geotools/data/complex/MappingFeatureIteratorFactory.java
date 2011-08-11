/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2009, Open Source Geospatial Foundation (OSGeo)
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

import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.data.complex.filter.MultiValuedOrImpl;
import org.geotools.filter.FidFilterImpl;
import org.opengis.filter.Filter;

/**
 * @author Russell Petty, GSV
 * @author Rini Angreani, CSIRO Earth Science and Resource Engineering
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/app-schema/app-schema/src/main/java/org/geotools/data/complex/MappingFeatureIteratorFactory.java $
 */
public class MappingFeatureIteratorFactory {

    public static IMappingFeatureIterator getInstance(AppSchemaDataAccess store,
            FeatureTypeMapping mapping, Query query) throws IOException {

        if (mapping instanceof XmlFeatureTypeMapping) {
            return new XmlMappingFeatureIterator(store, mapping, query);
        }

        boolean isFiltered = false;
        if (query.getFilter() != null) {
            Query unrolledQuery = store.unrollQuery(query, mapping);
            Filter filter = unrolledQuery.getFilter();
            if (filter instanceof MultiValuedOrImpl) {
            	if (!(unrolledQuery instanceof DefaultQuery)) {
                    unrolledQuery = new DefaultQuery(unrolledQuery);
            	}
                ((DefaultQuery) unrolledQuery).setFilter(Filter.INCLUDE);
                return new FilteringMappingFeatureIterator(store, mapping, unrolledQuery, filter);
            } else if (!filter.equals(Filter.INCLUDE) && !filter.equals(Filter.EXCLUDE)
                    && !(filter instanceof FidFilterImpl)) {
                isFiltered = true;
            }
        }

        return new DataAccessMappingFeatureIterator(store, mapping, query, isFiltered,
                isDenormalised(mapping));
    }

    /**
     * Determine if at least one attribute mapping is multi-valued, which means the data comes from
     * denormalised view.
     * 
     * @param mapping
     *            The feature type mapping
     * @return
     */
    private static boolean isDenormalised(FeatureTypeMapping mapping) {
        for (AttributeMapping att : mapping.getAttributeMappings()) {
            if (att.isMultiValued()) {
                return true;
            }
        }
        return false;
    }
}
