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
package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.data.versioning.ArcSdeVersionHandler;
import org.geotools.arcsde.pool.ISession;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.DataFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureReaderIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * FeatureCollection implementation that works over an {@link ArcSDEFeatureReader} or one of the
 * decorators over it returned by {@link ArcSDEDataStore#getFeatureReader(Query, Session, boolean)}.
 * <p>
 * Note this class and the iterators it returns are thread safe.
 * </p>
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id: ArcSdeFeatureCollection.java 30921 2008-07-05 07:51:23Z jgarnett $
 * @since 2.5
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/ArcSdeFeatureCollection.java $
 * @see FeatureCollection
 */
public class ArcSdeFeatureCollection extends DataFeatureCollection {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.data");

    private final ArcSdeFeatureSource featureSource;

    private final Query query;

    private final Set<ArcSdeFeatureReaderIterator> openIterators;

    private final SimpleFeatureType childrenSchema;

    // private Session session;

    public ArcSdeFeatureCollection(final ArcSdeFeatureSource featureSource, final Query namedQuery) throws IOException {
        this.featureSource = featureSource;
        this.query = namedQuery;
        //this.childrenSchema = ArcSDEQuery.getQuerySchema(namedQuery, featureSource.getSchema());

        FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReader();
        try{
            this.childrenSchema = reader.getFeatureType();
        }finally{
            reader.close();
        }

        final Set<ArcSdeFeatureReaderIterator> iterators;
        iterators = new HashSet<ArcSdeFeatureReaderIterator>();
        this.openIterators = Collections.synchronizedSet(iterators);
    }

    /**
     * @see FeatureCollection#getSchema()
     */
    @Override
    public final synchronized SimpleFeatureType getSchema() {
        return childrenSchema;
    }

    /**
     * @see FeatureCollection#getBounds()
     */
    @Override
    public final ReferencedEnvelope getBounds() {
        ReferencedEnvelope bounds;

        LOGGER.info("Getting collection bounds");
        try {
            bounds = featureSource.getBounds(query);
            if (bounds == null) {
                LOGGER.info("FeatureSource returned null bounds, going to return an empty one");
                bounds = new ReferencedEnvelope(getCRS());
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Error getting collection bounts", e);
            bounds = new ReferencedEnvelope(getCRS());
        }
        return bounds;
    }

    private CoordinateReferenceSystem getCRS() {
        GeometryDescriptor defaultGeometry = this.featureSource.getSchema().getGeometryDescriptor();
        return defaultGeometry == null ? null : defaultGeometry.getCoordinateReferenceSystem();
    }

    @Override
    public final int getCount() throws IOException {
        return featureSource.getCount(query);
    }

    /**
     * @param openIterator an {@link ArcSdeFeatureReaderIterator}
     */
    @Override
    protected final void closeIterator(Iterator<SimpleFeature> openIterator) throws IOException {
        ArcSdeFeatureReaderIterator iterator = (ArcSdeFeatureReaderIterator) openIterator;
        iterator.close();
    }

    private void releaseIterator(ArcSdeFeatureReaderIterator iterator) {
        this.openIterators.remove(iterator);
    }

    /**
     * Extends FeatureReaderIterator to instruct the parent collection to close the session at this
     * iterator's close method if its the last open iterator in the collection.
     * 
     * @author Gabriel Roldan (TOPP)
     * @see ArcSdeFeatureCollection#closeConnectionIfNeedBe()
     */
    private static class ArcSdeFeatureReaderIterator extends FeatureReaderIterator<SimpleFeature> {

        private final ArcSdeFeatureCollection parent;

        public ArcSdeFeatureReaderIterator(final FeatureReader<SimpleFeatureType, SimpleFeature> reader,
                                           final ArcSdeFeatureCollection parent) {
            super(reader);
            this.parent = parent;
        }

        @Override
        public void close() {
            try {
                // close the underlying feature reader
                super.close();
            } finally {
                parent.releaseIterator(this);
            }
        }
    }

    /**
     * Returns
     */
    @Override
    protected synchronized final Iterator<SimpleFeature> openIterator() throws IOException {
        final FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReader();
        final ArcSdeFeatureReaderIterator iterator;
        iterator = new ArcSdeFeatureReaderIterator(reader, this);
        this.openIterators.add(iterator);
        return iterator;
    }

    private FeatureReader<SimpleFeatureType, SimpleFeature> getReader() throws IOException {
        final FeatureReader<SimpleFeatureType, SimpleFeature> reader;

        final ArcSDEDataStore dataStore = featureSource.getDataStore();
        final ArcSdeVersionHandler versionHandler = featureSource.getVersionHandler();

        final ISession session = getSession();
        try {
            reader = dataStore.getFeatureReader(query, session, versionHandler);
        } catch (IOException ioe) {
            session.dispose();
            throw ioe;
        } catch (RuntimeException re) {
            session.dispose();
            throw re;
        }
        return reader;
    }

    /**
     * Returns the underlying feature source connection priorly locking it for thread safety. Relies
     * on the feature source to return an appropriate connection depending on whether it is under a
     * transaction or not.
     * 
     * @return
     * @throws IOException
     * @throws RuntimeException if the connection can't be acquired
     */
    private synchronized ISession getSession() throws IOException {
        return featureSource.getSession();
    }
}
