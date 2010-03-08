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
package org.geotools.data;

import java.io.IOException;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

/**
 * Versioning feature store, provides rollback facilities not included in
 * standard feature stores, plus the extra methods inherited from
 * {@link VersioningFeatureSource}
 * 
 * @author Andrea Aime, TOPP
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/postgis-versioned/src/main/java/org/geotools/data/VersioningFeatureStore.java $
 */
public interface VersioningFeatureStore extends VersioningFeatureSource, FeatureStore<SimpleFeatureType, SimpleFeature> {
    /**
     * Rolls back features matching the filter to the state they had on the
     * specified version.
     * <p>
     * For a feature to be included into the rollback it's sufficient that one
     * of its states between <code>toVersion</code> and current matches the
     * filter.
     * 
     * @param toVersion
     *            target of the rollback
     * @param filter
     *            limits the feature whose history will be rolled back by an OGC
     *            filter
     * @param users
     *            limits the feaeature whose history will be rolled back, by
     *            catching only those that have been modified by at least one of
     *            the specified users. May be null to avoi user filtering.
     * @throws IOException
     */
    public void rollback(String toVersion, Filter filter, String[] users) throws IOException;

}
