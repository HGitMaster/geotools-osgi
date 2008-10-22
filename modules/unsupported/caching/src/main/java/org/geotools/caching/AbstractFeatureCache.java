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

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vividsolutions.jts.geom.Envelope;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.spatial.BBOX;
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.util.BBoxFilterSplitter;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.store.EmptyFeatureCollection;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.OrImpl;
import org.geotools.filter.spatial.BBOXImpl;


public abstract class AbstractFeatureCache implements FeatureCache, FeatureListener {
    public static FilterCapabilities caps;
    protected static Logger logger;
    static FilterFactory ff;

    static {
        ff = new FilterFactoryImpl();
        logger = org.geotools.util.logging.Logging.getLogger("org.geotools.caching");
        caps = new FilterCapabilities();
        caps.addType(Or.class);
        caps.addType(And.class);
        caps.addType(Not.class);
        caps.addType(BBOX.class);
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

    public DataStore getDataStore() {
        return this.fs.getDataStore();
    }

    public FeatureCollection getFeatures() throws IOException {
        return this.getFeatures(Filter.INCLUDE);
    }

    public FeatureCollection getFeatures(Query query) throws IOException {
        if ((query.getTypeName() != null)
                && (query.getTypeName() != this.getSchema().getTypeName())) {
            return new EmptyFeatureCollection(this.getSchema());
        } else {
            return getFeatures(query.getFilter());
        }
    }

    public FeatureCollection getFeatures(Filter filter)
        throws IOException {
        /* PostPreProcessFilterSplittingVisitor may return
           a mixture of logical filters (or, and, not) and bbox filters,
           and for now I do not know how to handle this */

        //PostPreProcessFilterSplittingVisitor splitter = new PostPreProcessFilterSplittingVisitor(caps, this.fs.getSchema(), null) ;
        /* so we use this splitter which will return a single BBOX */
        BBoxFilterSplitter splitter = new BBoxFilterSplitter();
        filter.accept(splitter, null);

        Filter spatial_restrictions = splitter.getFilterPre();
        Filter other_restrictions = splitter.getFilterPost();

        if (spatial_restrictions == Filter.INCLUDE) {
            // we could not isolate any spatial restriction
            // delegate to source
            return this.fs.getFeatures(filter);
        } else {
            FeatureCollection fc;

            try {
                // first pre-process query from cache
                fc = _getFeatures(spatial_restrictions);
            } catch (UnsupportedOperationException e) {
                logger.log(Level.WARNING, "Querying cache : " + e.toString());

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
                //            	 get notice we discovered some new part of the universe
                register(notcached);
                // add new data to cache - will raise an exception if cache is oversized 
                put(fromCache);
            } catch (UnsupportedOperationException e) {
                logger.log(Level.WARNING, "Adding data to cache : " + e.toString());
            } catch (CacheOversizedException e) {
                logger.log(Level.INFO, "Adding data to cache : " + e.toString());
                remove(extractEnvelope((BBOXImpl) notcached));
            }

            // merge result sets
            fromCache.addAll(fromSource);

            return fromCache;
        }
    }

    public FeatureCollection get(Envelope e) throws IOException { // TODO: read lock

        FeatureCollection fromCache;
        FeatureCollection fromSource;
        String geometryname = fs.getSchema().getPrimaryGeometry().getLocalName();
        String srs = fs.getSchema().getPrimaryGeometry().getCoordinateSystem().toString();

        //      acquire R-lock
        writeLog(Thread.currentThread().getName() + " : Asking R lock, matching filter");
        lock.readLock().lock();
        writeLog(Thread.currentThread().getName() + " : Got R lock, matching filter");

        List notcached = match(e);

        if (notcached.isEmpty()) { // everything in cache
                                   // return result from cache
            fromCache = peek(e);
            // release R-lock
            writeLog(Thread.currentThread().getName() + " : Released R lock, got answer");
            lock.readLock().unlock();

            return fromCache;
        }

        // release R-lock
        writeLog(Thread.currentThread().getName() + " : Released R lock, missing data");
        lock.readLock().unlock();
        // got a miss from cache, need to get more data
        // acquire W-lock
        writeLog(Thread.currentThread().getName() + " : Asking W lock, getting data");
        lock.writeLock().lock();
        writeLog(Thread.currentThread().getName() + " : Got W lock, getting data");
        notcached = match(e); // check again because another thread may have inserted data in between

        if (notcached.isEmpty()) {
            // acquire R-lock
            writeLog(Thread.currentThread().getName()
                + " : Asking R lock, data inserted in between");
            lock.readLock().lock();
            writeLog(Thread.currentThread().getName() + " : Got R lock, data inserted in between");
            // release W-lock
            writeLog(Thread.currentThread().getName()
                + " : Released W lock, data inserted in between");
            lock.writeLock().unlock();
            fromCache = peek(e);
            // release R-lock
            writeLog(Thread.currentThread().getName() + " : Released R lock, got answer from cache");
            lock.readLock().unlock();

            return fromCache;
        }

        if (notcached.size() == 1) {
            Envelope m = (Envelope) notcached.get(0);

            if (m == e) { // nothing in cache
                          // get all stuff from source

                Filter filter = ff.bbox(geometryname, m.getMinX(), m.getMinY(), m.getMaxX(),
                        m.getMaxY(), srs);
                fromSource = this.fs.getFeatures(filter);
                source_hits++;
                source_feature_reads += fromSource.size();

                try {
                    put(fromSource, e);
                } catch (CacheOversizedException e1) {
                    logger.log(Level.INFO, "Adding data to cache : " + e1);
                }

                // release W-lock
                writeLog(Thread.currentThread().getName() + " : Released W lock, inserted new data");
                lock.writeLock().unlock();

                return fromSource;
            }
        }

        // we need missing data from source
        // first, get from cache what we have
        fromCache = peek(e);

        // then, get from source what we are missing
        ArrayList filters = new ArrayList(notcached.size());

        for (Iterator it = notcached.iterator(); it.hasNext();) {
            Envelope next = (Envelope) it.next();
            Filter bbox = ff.bbox(geometryname, next.getMinX(), next.getMinY(), next.getMaxX(),
                    next.getMaxY(), srs);
            filters.add(bbox);
        }

        Filter filter = ff.or(filters);
        fromSource = this.fs.getFeatures(filter);
        source_hits++;
        source_feature_reads += fromSource.size();

        try {
            isOversized(fromSource);
            register(filter); // get notice we discovered some new part of the universe
            put(fromSource); // add new data to cache - will raise an exception if cache is oversized 
        } catch (UnsupportedOperationException e1) {
            logger.log(Level.WARNING, "Adding data to cache : " + e1.toString());
        } catch (CacheOversizedException e1) {
            logger.log(Level.INFO, "Adding data to cache : " + e1.toString());
        }

        // release W-lock
        writeLog(Thread.currentThread().getName() + " : Released W lock, inserted new data");
        lock.writeLock().unlock();
        // merge result sets before returning
        fromCache.addAll(fromSource);

        return fromCache;
    }

    /**@deprecated
     * @param sr
     * @return
     */
    protected abstract Filter match(BBOXImpl sr);

    protected abstract List<Envelope> match(Envelope e);

    protected abstract void register(BBOXImpl f);

    protected abstract void register(Envelope e);

    protected abstract void isOversized(FeatureCollection fc)
        throws CacheOversizedException;

    public static Envelope extractEnvelope(BBOXImpl filter) {
        return new Envelope(filter.getMinX(), filter.getMaxX(), filter.getMinY(), filter.getMaxY());
    }

    public FeatureType getSchema() {
        return this.fs.getSchema();
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
            for (Iterator it = ((OrImpl) f).getChildren().iterator(); it.hasNext();) {
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
}
