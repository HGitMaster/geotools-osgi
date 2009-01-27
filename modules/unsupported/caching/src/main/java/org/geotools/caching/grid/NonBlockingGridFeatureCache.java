/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.caching.grid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.geotools.caching.CacheOversizedException;
import org.geotools.caching.FeatureCacheException;
import org.geotools.caching.spatialindex.Storage;
import org.geotools.data.FeatureSource;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;

/**
 * This is a implementation of a feature cache that does not block while getting data to write to
 * the cache.
 * <p>
 * The result is that multiple request may request the same data and the same data may be placed in
 * the cache multiple times. However the performance is better as other threads can still read the
 * cache.
 * </p>
 * <p>
 * An improvement might be to try to track which regions have already been requested and wait for
 * those regions as opposed to just request them again.
 * </p>
 * 
 * @author Emily Gouge, Refractions Research
 */
public class NonBlockingGridFeatureCache extends GridFeatureCache {

    /**
     * @param FeatureStore from which to cache features
     * @param indexcapacity = number of tiles in index
     * @param capacity = max number of features to cache
     * @throws FeatureCacheException
     * @throws IOException
     */
    public NonBlockingGridFeatureCache( FeatureSource fs, int indexcapacity, int capacity,
            Storage store ) throws FeatureCacheException {
        this(fs, getFeatureBounds(fs), indexcapacity, capacity, store);
    }

    /**
     * Creates a new grid feature cache.
     * 
     * @param fs FeatureStore from which to cache features
     * @param env The size of the feature cache; once defined features outside this bounds cannot be
     *        added to the featurestore/cache
     * @param indexcapactiy number of tiles in the index
     * @param capcity maximum number of features to cache
     * @param store the cache storage
     */
    public NonBlockingGridFeatureCache( FeatureSource fs, ReferencedEnvelope env,
            int indexcapactiy, int capcity, Storage store ) {
        super(fs, env, indexcapactiy, capcity, store);
    }

    public FeatureCollection get( Envelope e ) throws IOException { // TODO: read lock
        FeatureCollection fromCache;
        FeatureCollection fromSource;
        List<Envelope> notcached = null;

        String geometryname = getSchema().getGeometryDescriptor().getLocalName();
        String srs = getSchema().getGeometryDescriptor().getCoordinateReferenceSystem().toString();

        // acquire R-lock
        writeLog(Thread.currentThread().getName() + " : Asking R lock, matching filter");
        lock.readLock().lock();
        try {
            writeLog(Thread.currentThread().getName() + " : Got R lock, matching filter");

            notcached = match(e);
            fromCache = peek(e);

            if (notcached.isEmpty()) { // everything in cache
                // return result from cache
                return fromCache;
            }
        } finally {
            // release R-lock
            writeLog(Thread.currentThread().getName() + " : Released R lock, missing data");
            lock.readLock().unlock();
        }

        // here we have what's in the cache
        // so lets get what's not in the cache
        // get what data we are missing from the cache based on the not cached array.
        Filter filter = null;
        if (notcached.size() == 1) {
            Envelope env = notcached.get(0);
            filter = ff.bbox(geometryname, env.getMinX(), env.getMinY(), env.getMaxX(), env
                    .getMaxY(), srs);
        } else {
            // or the envelopes together into a single or filter
            ArrayList<Filter> filters = new ArrayList<Filter>(notcached.size());
            for( Iterator<Envelope> it = notcached.iterator(); it.hasNext(); ) {
                Envelope next = (Envelope) it.next();
                Filter bbox = ff.bbox(geometryname, next.getMinX(), next.getMinY(), next.getMaxX(),
                        next.getMaxY(), srs);
                filters.add(bbox);
            }
            filter = ff.or(filters);
        }

        // got a miss from cache, need to get more data
        // cache these features in a local feature collection while we deal with them
        FeatureCollection localSource = new MemoryFeatureCollection(getSchema());
        try {
            // get the data from the source
            fromSource = this.fs.getFeatures(filter);
            localSource.addAll(fromSource);
            fromSource = localSource;
        } catch (Exception ex) {
            // some issue getting features from source;
            // lets return what we have; we don't want to register anything (so it will get re-requested next time)
            logger.log(Level.INFO, "Error getting data for cache from source feature store.", ex);
            return fromCache;

        }
        
        // acquire W-lock
        writeLog(Thread.currentThread().getName() + " : Asking W lock, getting data");
        lock.writeLock().lock();
        writeLog(Thread.currentThread().getName() + " : Got W lock, getting data");
        try {
            // theoretically other threads may have already added the data
            // here; for now we will just blindly re-add it to the cache
            source_hits++;
            source_feature_reads += fromSource.size();
            try {
                isOversized(fromSource);
                try {
                    register(filter); // get notice we discovered some new part of the universe
                    put(fromSource);

                } catch (Exception ex) {
                    // something happened here so we better unregister this area
                    // so if we try again next time we'll try getting data again
                    unregister(filter);
                }
            } catch (CacheOversizedException e1) {
                logger.log(Level.INFO, "Adding data to cache : " + e1.toString());
            }

        } finally {
            // release W-lock
            writeLog(Thread.currentThread().getName() + " : Released W lock, inserted new data");
            lock.writeLock().unlock();
        }

        fromCache.addAll(fromSource);
        return fromCache;
    }
}
