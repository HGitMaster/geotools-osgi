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

package org.geotools.feature.iso;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.geotools.factory.Factory;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.geometry.BoundingBox;

/**
 * A utility class for working with FeatureCollections. Provides a mechanism for
 * obtaining a FeatureCollection instance.
 * 
 * @author Ian Schneider
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/fm/module/main/src/org/geotools/feature/FeatureCollections.java $
 */
public abstract class FeatureCollections implements Factory {

    /**
     * Holds a reference to a FeatureCollections implementation once one has
     * been requested for the first time using instance().
     */
    private static FeatureCollections instance = null;

    private static FeatureCollections instance() {
        if (instance == null) {
            /*
             * instance = (FeatureCollections) FactoryFinder.findFactory(
             * "org.geotools.feature.FeatureCollections",
             * "org.geotools.feature.DefaultFeatureCollections");
             */
        }
        return instance;
    }

    /**
     * create a new FeatureCollection using the current default factory.
     * 
     * @return A FeatureCollection instance.
     */
    public static FeatureCollection newCollection() {
        return instance().createCollection();
    }

    /**
     * Subclasses must implement this to return a new FeatureCollection object.
     * 
     * @return A new FeatureCollection
     */
    protected abstract FeatureCollection createCollection();

    /**
     * Returns the implementation hints. The default implementation returns en
     * empty map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }

    /**
     * Calculates the bounds of a feature iterator. Obtains crs information from
     * the first feature in the iteration.
     */
    public static ReferencedEnvelope getBounds(Iterator/* <Feature> */iterator) {

        ReferencedEnvelope bounds = null;
        while (iterator.hasNext()) {
            Feature f = (Feature) iterator.next();
            BoundingBox e = f.getBounds();

            if (bounds == null) {
                bounds = new ReferencedEnvelope(e);
                // bounds.init(e);
            } else {
                bounds.include(e);
            }
        }

        return bounds;
    }

    /**
     * Returns the <code>collection</code>'s element count. May need to
     * traverse the collection.
     * 
     * @param collection
     * @return
     */
    public static int getSize(FeatureCollection collection) {
        int size = -1;
        if (collection instanceof Collection) {
            size = ((Collection) collection).size();
        } else {
            int count = 0;
            Iterator features = collection.iterator();
            try {
                while (features.hasNext()) {
                    features.next();
                    count++;
                }
            } finally {
                collection.close(features);
            }
            size = count;
        }
        return size;
    }

}
