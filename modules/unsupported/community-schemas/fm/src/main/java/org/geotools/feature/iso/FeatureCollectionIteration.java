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
import java.util.Iterator;

import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;


/**
 * The FeatureCollectionIteration provides a depth first traversal of a
 * FeatureCollection which will call the provided call-back Handler. Because
 * of the complex nature of Features, which may have other Features (or even a
 * collection of Features) as attributes, the handler is repsonsible for
 * maintaining its own state as to where in the traversal it is recieving
 * events from. Many handlers will not need to worry about state.
 * 
 * <p>
 * <b>Implementation Notes:</b> The depth first visitation is implemented
 * through recursion. The limits to recursion depending on the settings in the
 * JVM, but some tests show a 2 argument recursive having a limit of ~50000
 * method calls with a stack size of 512k (the standard setting).
 * </p>
 *
 * @author Ian Schneider, USDA-ARS
 * @author Chris Holmes, TOPP
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/community-schemas/fm/src/main/java/org/geotools/feature/iso/FeatureCollectionIteration.java $
 */
public class FeatureCollectionIteration {
    /**
     * A callback handler for the iteration of the contents of a
     * FeatureCollection.
     */
    protected final Handler handler;

    /** The collection being iterated */
    private final FeatureCollection collection;

    /**
     * Create a new FeatureCollectionIteration with the given handler and
     * collection.
     *
     * @param handler The handler to perform operations on this iteration.
     * @param collection The collection to iterate over.
     *
     * @throws NullPointerException If handler or collection are null.
     */
    public FeatureCollectionIteration(Handler handler,
        FeatureCollection collection) throws NullPointerException {
        if (handler == null) {
            throw new NullPointerException("handler");
        }

        if (collection == null) {
            throw new NullPointerException("collection");
        }

        this.handler = handler;
        this.collection = collection;
    }

    /**
     * A convienience method for obtaining a new iteration and calling iterate.
     *
     * @param handler The handler to perform operations on this iteration.
     * @param collection The collection to iterate over.
     */
    public static void iteration(Handler handler, FeatureCollection collection) {
        FeatureCollectionIteration iteration = new FeatureCollectionIteration(handler,
                collection);
        iteration.iterate();
    }

    /**
     * Start the iteration.
     */
    public void iterate() {
        walker(collection);
    }

    /**
     * Perform the iterative behavior on the given collection. This will alert
     * the handler with a <code>handleFeatureCollection</code> call, followed
     * by an <code> iterate()</code>, followed by a
     * <code>handler.endFeatureCollection()</code> call.
     *
     * @param collection The collection to iterate upon.
     */
    protected void walker(FeatureCollection collection) {
        handler.handleFeatureCollection(collection);

        iterate(collection.iterator());

        handler.endFeatureCollection(collection);
    }

    /**
     * Perform the actual iteration on the Iterator which is provided.
     *
     * @param iterator The Iterator to iterate upon.
     */
    protected void iterate(Iterator iterator) {
        while (iterator.hasNext()) {
            walker((Feature) iterator.next());
        }
    }

    /**
     * Perform the visitation of an individual Feature.
     *
     * @param feature The Feature to explore.
     */
    protected void walker(Feature feature) {
       
        handler.handleFeature(feature);
        Collection attributes = (Collection)feature.getValue();
        
        for (Iterator itr = attributes.iterator(); itr.hasNext();) {
            Attribute att = (Attribute) itr.next();
            AttributeType type = att.getType();
            
            // recurse if attribute type is another collection
            if (type instanceof FeatureCollectionType) {
                walker((FeatureCollection) att);
            } 
            else if (type instanceof FeatureType) {
                // recurse if attribute type is another feature
                walker((Feature) att);
            } 
            else {
                // normal handling
                handler.handleAttribute(type, att);
            }
        }

        handler.endFeature(feature);
    }

    /**
     * A callback handler for the iteration of the contents of a
     * FeatureCollection.
     */
    public interface Handler {
        /**
         * The handler is visiting a FeatureCollection.
         *
         * @param fc The currently visited FeatureCollection.
         */
        void handleFeatureCollection(FeatureCollection fc);

        /**
         * The handler is done visiting a FeatureCollection.
         *
         * @param fc The FeatureCollection which was visited.
         */
        void endFeatureCollection(FeatureCollection fc);

        /**
         * The handler is visiting a Feature.
         *
         * @param f The Feature the handler is visiting.
         */
        void handleFeature(Feature f);

        /**
         * The handler is ending its visit with a Feature.
         *
         * @param f The Feature that was visited.
         */
        void endFeature(Feature f);

        /**
         * The handler is visiting an Attribute of a Feature.
         *
         * @param type The meta-data of the given attribute value.
         * @param value The attribute value, may be null.
         */
        void handleAttribute(AttributeType type, Object value);
    }
}
