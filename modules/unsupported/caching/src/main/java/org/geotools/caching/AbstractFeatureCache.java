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
package org.geotools.caching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.util.BBoxFilterSplitter;
import org.geotools.data.DataAccess;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.Transaction;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.store.EmptyFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.OrImpl;
import org.geotools.filter.spatial.BBOXImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import com.vividsolutions.jts.geom.Envelope;


public abstract class AbstractFeatureCache implements FeatureCache, FeatureListener {

    protected static Logger logger;
    static FilterFactory ff;

    static {
        ff = new FilterFactoryImpl();
        logger = org.geotools.util.logging.Logging.getLogger("org.geotools.caching");
    }

    protected FeatureSource fs;
    protected int source_hits = 0;
    protected int source_feature_reads = 0;
    protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public AbstractFeatureCache(FeatureSource fs) {
        this.fs = fs;
        fs.addFeatureListener(this);
    }

    /*
     * Interface FeatureStore : not any more needed in this read-only version
     *
     *
    
                   public Set addFeatures(FeatureCollection collection)
                       throws IOException {
                       return this.fs.addFeatures(collection);
                   }
                   public Transaction getTransaction() {
                       return this.fs.getTransaction();
                   }
                   public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter)
                       throws IOException {
                       this.fs.modifyFeatures(type, value, filter);
                   }
                   public void modifyFeatures(AttributeType type, Object value, Filter filter)
                       throws IOException {
                       this.fs.modifyFeatures(type, value, filter);
                   }
                   public void removeFeatures(Filter filter) throws IOException {
                       this.fs.removeFeatures(filter);
                   }
                   public void setFeatures(FeatureReader reader) throws IOException {
                       this.fs.setFeatures(reader);
                   }
                   public void setTransaction(Transaction transaction) {
                       this.fs.setTransaction(transaction);
                   } */
    public void addFeatureListener(FeatureListener listener) {
        this.fs.addFeatureListener(listener);
    }

    public DataAccess getDataStore() {
        return this.fs.getDataStore();
    }

    public FeatureCollection getFeatures() throws IOException {
        return this.getFeatures(Filter.INCLUDE);
    }


    @SuppressWarnings("unchecked")
    public FeatureCollection getFeatures(Query query) throws IOException {
    	String typename = query.getTypeName();
    	String schemaname = this.getSchema().getTypeName();
        if ((query.getTypeName() != null)
                && (typename != schemaname)) {
            return new EmptyFeatureCollection(this.getSchema());
        } else {
            FeatureCollection fc = getFeatures(query.getFilter());
            if (fc.size() == 0) {
                return new EmptyFeatureCollection(this.getSchema());
            }

            //filter already applied so we really don't need to re-apply it
            //just include all
            query = new DefaultQuery(query.getTypeName(), query.getNamespace(), Filter.INCLUDE, query.getMaxFeatures(), query.getPropertyNames(), query.getHandle());

            //below is probably not the best way to apply a query
            //to a feature collection; but I don't know a better way.
            
            // now that we have a feature collection we need to wrap it
            // in a datastore so we can apply the query to it.
            // in this case we'll use a memory data store
            MemoryDataStore md = new MemoryDataStore();

            //add the features
            //we are using an array because 
            //making a memory data store from a feature collection results
            //in a null pointer exception
            ArrayList<SimpleFeature> features = new ArrayList<SimpleFeature>();
            FeatureIterator<SimpleFeature> it = fc.features();
            try{
                for( ; it.hasNext(); ) {
                    SimpleFeature type = (SimpleFeature) it.next();
                    features.add(type);
                }
            }finally{
                it.close();
            }
            md.addFeatures(features.toArray(new SimpleFeature[features.size()]));

            //convert back to a feature collection with the query applied
            FeatureReader<SimpleFeatureType, SimpleFeature> fr = md.getFeatureReader(query, Transaction.AUTO_COMMIT);
            FeatureCollection fc1 = new DefaultFeatureCollection("cachedfeaturecollection", (SimpleFeatureType) fr.getFeatureType());
            while( fr.hasNext() ) {
                fc1.add(fr.next());
            }
            fr.close();

            return fc1;

            // return getFeatures(query.getFilter());
            // return fs.getFeatures(query);
        }
    }

    public FeatureCollection getFeatures(Filter filter)
        throws IOException {
        System.out.println("GET FEATURES FOR: " + filter);
        /* PostPreProcessFilterSplittingVisitor may return
           a mixture of logical filters (or, and, not) and bbox filters,
           and for now I do not know how to handle this */

        //PostPreProcessFilterSplittingVisitor splitter = new PostPreProcessFilterSplittingVisitor(caps, this.fs.getSchema(), null) ;
        /* so we use this splitter which will return a single BBOX */
        BBoxFilterSplitter splitter = new BBoxFilterSplitter();
        filter.accept(splitter, null);

        Filter spatial_restrictions = splitter.getFilterPre();
        Filter other_restrictions = splitter.getFilterPost();

        if (spatial_restrictions == Filter.EXCLUDE){
            //nothing to get
            return new EmptyFeatureCollection(this.getSchema());
        }else if (spatial_restrictions == Filter.INCLUDE ) {
            // we could not isolate any spatial restriction
            // delegate to source
            System.out.println("get requesting features from source for include filter: " + filter);
            return this.fs.getFeatures(filter);
        } else {
            FeatureCollection fc;

            try {
                // first pre-process query from cache
                fc = _getFeatures(spatial_restrictions);
            } catch (UnsupportedOperationException e) {
                logger.log(Level.WARNING, "Querying cache : " + e.toString());
                System.out.println("ERROR: get requesting features from source for filter: " + filter);
                return this.fs.getFeatures(filter);
            }

            // refine result set before returning
            return fc.subCollection(other_restrictions);
        }
    }

    protected FeatureCollection _getFeatures(Filter filter)
        throws IOException {
        if (filter instanceof BBOXImpl) {
            //return _getFeatures((BBOXImpl) filter) ;
            return get(extractEnvelope((BBOXImpl) filter)).subCollection(filter);
        } else {
            throw new UnsupportedOperationException("Cannot handle given filter :" + filter);

            /*FeatureCollection fc = new DefaultFeatureCollection("", this.getSchema()) ;
               for (Iterator it = processBBox(filter).iterator() ; it.hasNext() ;) {
                       BBOXImpl next = (BBOXImpl) it.next() ;
                       fc.addAll(_getFeatures(next)) ;
               }
               return fc ;*/
        }
    }

    /*protected List processBBox(Filter filter) {
       return null ;
       }*/

    /**@deprecated
     * @param spatial_restrictions
     * @return
     * @throws IOException
     */
    protected FeatureCollection _getFeatures(BBOXImpl spatial_restrictions)
        throws IOException {
        Filter notcached = match(spatial_restrictions);
        Envelope sr = extractEnvelope(spatial_restrictions);

        if (notcached == spatial_restrictions) { // nothing in cache
                                                 // get all stuff from source

            FeatureCollection fc = this.fs.getFeatures(notcached);

            try {
                put(fc, sr); // will raise an exception if cache is oversized 
            } catch (CacheOversizedException e) {
                logger.log(Level.INFO, "Adding data to cache : " + e);
            }

            return fc;
        } else if (notcached == Filter.EXCLUDE) { // everything in cache

            return peek(sr);
        } else { // we need missing data from source
                 // get from cache what we have

            FeatureCollection fromCache = peek(sr);

            // get from source what we are missing
            FeatureCollection fromSource = this.fs.getFeatures(notcached);

            try {
            	//get notice we discovered some new part of the universe
                register(notcached);
                // add new data to cache - will raise an exception if cache is oversized 
                put(fromCache);
            } catch (CacheOversizedException e) {
                logger.log(Level.INFO, "Adding data to cache : " + e.toString());
                remove(extractEnvelope((BBOXImpl) notcached));
            } catch (Exception ex){
            	unregister(notcached);
            }

            // merge result sets
            fromCache.addAll(fromSource);

            return fromCache;
        }
    }

    public FeatureCollection get(Envelope e) throws IOException { // TODO: read lock

        FeatureCollection fromCache;
        FeatureCollection fromSource;
        List<Envelope> notcached = null;
        
        String geometryname = fs.getSchema().getGeometryDescriptor().getLocalName();
        String srs = fs.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem().toString();

        //      acquire R-lock
        writeLog(Thread.currentThread().getName() + " : Asking R lock, matching filter");
        lock.readLock().lock();
        try {
            writeLog(Thread.currentThread().getName() + " : Got R lock, matching filter");
            notcached = match(e);
            if (notcached.isEmpty()) { // everything in cache
                // return result from cache
                fromCache = peek(e);
                return fromCache;
            }
        } finally {
            // release R-lock
            writeLog(Thread.currentThread().getName() + " : Released R lock, missing data");
            lock.readLock().unlock();
        }

        // got a miss from cache, need to get more data
        // acquire W-lock
        writeLog(Thread.currentThread().getName() + " : Asking W lock, getting data");
        lock.writeLock().lock();
        writeLog(Thread.currentThread().getName() + " : Got W lock, getting data");
        try {
			notcached = match(e); // check again because another thread may have inserted data in between
			if (notcached.isEmpty()) {
				fromCache = peek(e);
				return fromCache;
			}
			
			//get items from cache
			fromCache = peek(e);
			 
			// get what data we are missing from the cache based on the not cached array.
			Filter filter = null;
			if (notcached.size() == 1){
			    Envelope env = notcached.get(0);
			    filter = ff.bbox(geometryname, env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY(), srs);
			}else{
			    //or the envelopes together into a single or filter
			    ArrayList<Filter> filters = new ArrayList<Filter>(notcached.size());
	            for (Iterator<Envelope> it = notcached.iterator(); it.hasNext();) {
	                Envelope next = (Envelope) it.next();
	                Filter bbox = ff.bbox(geometryname, next.getMinX(), next.getMinY(), next.getMaxX(), next.getMaxY(), srs);
	                filters.add(bbox);
	            }
	            filter = ff.or(filters);
			}
			
			//get the data from the source
			fromSource = this.fs.getFeatures(filter);
			
			//update stats
			source_hits++;
			source_feature_reads += fromSource.size();

			fromCache.addAll(fromSource);
			
			//add it to the cache; 
			try {
				isOversized(fromSource);
				try{
					register(filter); // get notice we discovered some new part of the universe
					//put(fromSource);  // add new data to cache - will raise an exception if cache is over sized
					// add new data to cache - will raise an exception if cache is over sized
					//here we are adding the everything (include the stuff that's already in the cache
					//which is done to prevent multiple wfs calls 
					put(fromCache);  
					
				}catch (Exception ex){
					//something happened here so we better unregister this area
					//so if we try again next time we'll try getting data again
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
		
        return fromCache;
    }

    /**@deprecated
     * @param sr
     * @return
     */
    protected abstract Filter match(BBOXImpl sr);

    protected abstract List<Envelope> match(Envelope e);

    protected void register(BBOXImpl f){
        register(extractEnvelope(f));
    }

    protected abstract void register(Envelope e);

    protected abstract void unregister(Envelope e);
    
    protected void unregister(BBOXImpl f){
        unregister(extractEnvelope(f));
    }
    
    protected void unregister(Filter f){
        if (f instanceof OrImpl) {
            for (Iterator<Filter> it = ((OrImpl)f).getChildren().iterator(); it.hasNext();) {
                Filter child = (Filter) it.next();
                unregister(child);
            }
        } else if (f instanceof BBOXImpl) {
            unregister((BBOXImpl) f);
        } else {
            throw new UnsupportedOperationException("Do not know how to handle this filter" + f);
        }
    }
    
    protected abstract void isOversized(FeatureCollection fc)
        throws CacheOversizedException;

    public static Envelope extractEnvelope(BBOXImpl filter) {
        return new Envelope(filter.getMinX(), filter.getMaxX(), filter.getMinY(), filter.getMaxY());
    }

    public SimpleFeatureType getSchema() {
        return (SimpleFeatureType)this.fs.getSchema();
    }

    public void removeFeatureListener(FeatureListener listener) {
        this.fs.removeFeatureListener(listener);
    }

    public void changed(FeatureEvent event) {
        // TODO: what to do if change is outside of root mbr ?
        // implementations should handle this case
        // add an abstract checkForOutOfBoundsEvent(event) ?
        remove(event.getBounds());
    }

    public static Region convert(Envelope e) {
        return new Region(new double[] { e.getMinX(), e.getMinY() },
            new double[] { e.getMaxX(), e.getMaxY() });
    }

    public static Envelope convert(Region r) {
        return new Envelope(r.getLow(0), r.getHigh(0), r.getLow(1), r.getHigh(1));
    }

    protected void register(Filter f) {
        if (f instanceof OrImpl) {
            for (Iterator<Filter> it = ((OrImpl)f).getChildren().iterator(); it.hasNext();) {
                Filter child = (Filter) it.next();
                register(child);
            }
        } else if (f instanceof BBOXImpl) {
            register((BBOXImpl) f);
        } else {
            throw new UnsupportedOperationException("Do not know how to handle this filter" + f);
        }
    }

    public String sourceAccessStats() {
        StringBuffer sb = new StringBuffer();
        sb.append("Source hits = " + source_hits);
        sb.append(" ; Feature reads = " + source_feature_reads);

        return sb.toString();
    }

    public abstract String getStats();

    public void readLock() {
        lock.readLock().lock();
    }

    public void readUnLock() {
        lock.readLock().unlock();
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnLock() {
        lock.writeLock().unlock();
    }

    public void writeLog(String msg) {
//    	System.out.println(msg);
//        try {
//            FileWriter log = new FileWriter("log/threads.log", true);
//            log.write(msg + "\n");
//            log.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    
    
    /**
	 * @return the resource info from the main feature source being cached
	 */
	public ResourceInfo getInfo() {
		return fs.getInfo();
	}

	/**
	 * @return the name from the main feature source being cached
	 */
	public Name getName() {
		//pass along to existing feature source
		return fs.getName();
	}

	/**
	 * @return the query capabilities from the main feature source being cached
	 */
	public QueryCapabilities getQueryCapabilities() {
		// pass along to feature source
		return fs.getQueryCapabilities();
	}
}
