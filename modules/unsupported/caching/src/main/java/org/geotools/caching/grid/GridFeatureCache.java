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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.opengis.feature.Feature;
import org.geotools.caching.AbstractFeatureCache;
import org.geotools.caching.CacheOversizedException;
import org.geotools.caching.FeatureCacheException;
import org.geotools.caching.FeatureCollectingVisitor;
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.Storage;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.spatial.BBOXImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;


public class GridFeatureCache extends AbstractFeatureCache {
    GridTracker tracker;
    int max_tiles = 10;
    int capacity;

    //int evictions = 0 ;
    //int puts = 0 ;

    /**
     * @param FeatureStore from which to cache features
     * @param indexcapacity = number of tiles in index
     * @param capacity = max number of features to cache
     * @throws FeatureCacheException
     * @throws IOException 
     */
    public GridFeatureCache(FeatureSource fs, int indexcapacity, int capacity, Storage store)
        throws FeatureCacheException {
    	
    	this(fs, getFeatureBounds(fs), indexcapacity, capacity, store);   
    }
    
    /**
     * Creates a new grid feature cache.
     * @param fs			FeatureStore from which to cache features
     * @param env			The size of the feature cache; once defined features outside this bounds cannot be added to the featurestore/cache
     * @param indexcapactiy	number of tiles in the index
     * @param capcity		maximum number of features to cache
     * @param store			the cache storage
     */
    public GridFeatureCache(FeatureSource fs, ReferencedEnvelope env, int indexcapactiy, int capcity, Storage store){
    	super(fs);

    	tracker = new GridTracker(convert(env), indexcapactiy, store);
    	this.capacity = capcity;
    }
    
    /**
     * Private function used to get the bounds of a feature collection and convert the IOException
     * to a FeatureCacheException
     * 
     * @param fs
     * @return
     * @throws FeatureCacheException
     */
    private static ReferencedEnvelope getFeatureBounds(FeatureSource fs) throws FeatureCacheException{
    	try{
    		return fs.getBounds();
    	}catch(IOException ex){
    		throw new FeatureCacheException(ex);
    	}
    }

    protected Filter match(BBOXImpl sr) {
        Region search = convert(extractEnvelope(sr));
        Stack missing = tracker.searchMissingTiles(search);
        FilterFactoryImpl ff = new FilterFactoryImpl();

        // TODO: group regions
        if (missing.size() > max_tiles) {
            return sr;
        } else if (missing.size() > 1) {
            ArrayList<Filter> filters = new ArrayList<Filter>(missing.size());

            while (!missing.isEmpty()) {
                Region rg = (Region) missing.pop();
                Filter bbf = ff.bbox(sr.getPropertyName(), rg.getLow(0), rg.getLow(1),
                        rg.getHigh(0), rg.getHigh(1), sr.getSRS());
                filters.add(bbf);
            }

            return ff.or(filters);
        } else if (missing.size() == 1) {
            Region rg = (Region) missing.pop();

            return ff.bbox(sr.getPropertyName(), rg.getLow(0), rg.getLow(1), rg.getHigh(0),
                rg.getHigh(1), sr.getSRS());
        } else {
            return Filter.EXCLUDE;
        }
    }

    /**
     * @param e
     * @return list of envelopes not in cache
     */
    protected List<Envelope> match(Envelope e) {
        Region search = convert(e);
        ArrayList<Envelope> missing = new ArrayList<Envelope>();

        if (!this.tracker.getRoot().getShape().contains(search)) { // query is outside of root mbr
                                                                   // we limit our search to the inside of the root mbr

            Envelope r = convert((Region) this.tracker.getRoot().getShape());
            r = r.intersection(e);
            search = convert(r);
        }

        List missing_tiles = tracker.searchMissingTiles(search);
        
        // TODO: groug regions
        if (missing_tiles.size() > max_tiles) {
            missing.add(e);
        } else {
            for (Iterator it = missing_tiles.iterator(); it.hasNext();) {
                Region next = (Region) it.next();
                missing.add(convert(next));
            }
        }

        return missing;
    }

    public void clear() {
        lock.writeLock().lock();

        try {
            tracker.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public FeatureCollection peek(Envelope e) {
        FeatureCollectingVisitor v = new FeatureCollectingVisitor(this.getSchema());
        lock.readLock().lock();

        try {
            this.tracker.intersectionQuery(convert(e), v);
        } finally {
            lock.readLock().unlock();
        }

        return v.getCollection();
    }

    public void put(FeatureCollection fc, Envelope e) throws CacheOversizedException {
        isOversized(fc);
        writeLog(Thread.currentThread().getName() + " : Asking W lock, putting data");
        lock.writeLock().lock();
        writeLog(Thread.currentThread().getName() + " : Got W lock, putting data");

        try {
            //put then register put fails
            register(e);
            put(fc);
        }catch (Exception ex){
        	unregister(e);
        } finally {
            writeLog(Thread.currentThread().getName() + " : Released W lock, data inserted (put)");
            lock.writeLock().unlock();
        }
    }

    protected void isOversized(FeatureCollection fc) throws CacheOversizedException {
        if (fc.size() > this.capacity) {
            throw new CacheOversizedException("Cannot cache collection of size " + fc.size()
                + " (capacity = " + capacity + " )");
        }
    }

    public void remove(Envelope e) {
        InvalidatingVisitor v = new InvalidatingVisitor();
        writeLog(Thread.currentThread().getName() + " : Asking W lock, removing data");
        lock.writeLock().lock();
        writeLog(Thread.currentThread().getName() + " : Got W lock, removing data");

        try {
        	if (e == null){
        		//no envelope specified so assume everything
        		e = getBounds();
        	}
        	this.tracker.intersectionQuery(convert(e), v);
        }catch (IOException ex) {
        	//TODO: do something about this error
        } finally {
            writeLog(Thread.currentThread().getName() + " : Released W lock, data removed");
            lock.writeLock().unlock();
        }
    }

    public ReferencedEnvelope getBounds() throws IOException {
    	CoordinateReferenceSystem crs = this.fs.getSchema().getCoordinateReferenceSystem(); 
        return new ReferencedEnvelope ( convert((Region) this.tracker.getRoot().getShape()), crs);
    }

    public ReferencedEnvelope getBounds(Query query) throws IOException {
        return this.fs.getBounds(query);
    }

    public int getCount(Query query) throws IOException {
        return this.fs.getCount(query);
    }

    public void put(FeatureCollection fc) throws CacheOversizedException {
        int size = fc.size();

        isOversized(fc);
        //puts++ ;
        writeLog(Thread.currentThread().getName() + " : Asking W lock, putting data");
        lock.writeLock().lock();
        writeLog(Thread.currentThread().getName() + " : Got W lock, putting data");

        try {
            while (tracker.getStatistics().getNumberOfData() > (capacity - size)) { // was capacity - fc.size()
                writeLog(Thread.currentThread().getName() + " : evicting");
                tracker.policy.evict();

                //evictions++ ;
                //System.out.println("Put #" + puts + " > number of evictions = " + evictions) ;
            }

            FeatureIterator<SimpleFeature> it = fc.features();
            try{
                while (it.hasNext()) {
                    SimpleFeature f = it.next();
                    this.tracker.insertData(f, convert((Envelope)f.getBounds()), f.hashCode());
                }
            }finally{
                fc.close(it);
            }
        } finally {
            writeLog(Thread.currentThread().getName() + " : Released W lock, data inserted (put)");
            lock.writeLock().unlock();
        }
    }

    protected void register(BBOXImpl f) {
        register(extractEnvelope(f));
    }

    protected void register(Envelope e) {
        Region r = convert(e);
        ValidatingVisitor v = new ValidatingVisitor(r);

        writeLog(Thread.currentThread().getName() + " : Asking W lock, registering");
        lock.writeLock().lock();
        writeLog(Thread.currentThread().getName() + " : Got W lock, registering");
        try {
            this.tracker.containmentQuery(r, v);
        } finally {
            writeLog(Thread.currentThread().getName() + " : Released W lock, registered envelope");
            lock.writeLock().unlock();
        }
    }
    
    protected void unregister(Envelope e){
    	Region r = convert(e);
    	InvalidatingVisitor v = new InvalidatingVisitor(r);
    	
    	writeLog(Thread.currentThread().getName() + " : Asking W lock, registering");
        lock.writeLock().lock();
        writeLog(Thread.currentThread().getName() + " : Got W lock, registering");
        try {
            this.tracker.containmentQuery(r, v);
        } finally {
            writeLog(Thread.currentThread().getName() + " : Released W lock, registered envelope");
            lock.writeLock().unlock();
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("GridFeatureCache [");
        sb.append(" Source = " + this.fs);
        sb.append(" Capacity = " + this.capacity);
        sb.append(" Nodes = " + this.tracker.stats.getNumberOfNodes());
        sb.append(" ]");
        sb.append("\n" + tracker.getIndexProperties());

        return sb.toString();
    }

    public Set getSupportedHints() {
        return new HashSet();
    }

    public String getStats() {
        StringBuffer sb = new StringBuffer();
        sb.append(tracker.getStatistics().toString());
        sb.append("\n" + sourceAccessStats());

        return sb.toString();
    }
}
