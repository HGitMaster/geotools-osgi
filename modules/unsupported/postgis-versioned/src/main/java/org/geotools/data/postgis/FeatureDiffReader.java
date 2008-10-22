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
package org.geotools.data.postgis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Transaction;
import org.geotools.data.postgis.fidmapper.VersionedFIDMapper;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

/**
 * Provides forward only access to the feature differences
 * 
 * @author aaime
 * @since 2.4
 * 
 */
public class FeatureDiffReader {
	/** The logger for the postgis module. */
    protected static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.data.postgis");

    private  FeatureReader<SimpleFeatureType, SimpleFeature> fvReader;

    private  FeatureReader<SimpleFeatureType, SimpleFeature> tvReader;

    private RevisionInfo fromVersion;

    private RevisionInfo toVersion;

    private VersionedFIDMapper mapper;

    private Transaction transaction;

    private VersionedPostgisDataStore store;

    private  FeatureReader<SimpleFeatureType, SimpleFeature> deletedReader;

    private  FeatureReader<SimpleFeatureType, SimpleFeature> createdReader;

    private SimpleFeatureType externalFeatureType;

    private FeatureDiff lastDiff;

    private ModifiedFeatureIds modifiedIds;

    public FeatureDiffReader(VersionedPostgisDataStore store, Transaction transaction,
            SimpleFeatureType externalFeatureType, RevisionInfo fromVersion, RevisionInfo toVersion,
            VersionedFIDMapper mapper, ModifiedFeatureIds modifiedIds) throws IOException {
        this.store = store;
        this.transaction = transaction;
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.externalFeatureType = externalFeatureType;
        this.mapper = mapper;
        this.modifiedIds = modifiedIds;
        initReaders();
    }

    /**
     * Allows to clone a diff reader, this makes it possible to scroll over the same diffs with
     * multiple readers at the same time (reset allows only for multiple isolated scans)
     * 
     * @param other
     * @throws IOException
     */
    public FeatureDiffReader(FeatureDiffReader other) throws IOException {
        this.store = other.store;
        this.transaction = other.transaction;
        this.fromVersion = other.fromVersion;
        this.toVersion = other.toVersion;
        this.externalFeatureType = other.externalFeatureType;
        this.mapper = other.mapper;
        this.modifiedIds = other.modifiedIds;
        initReaders();
    }

    void initReaders() throws IOException {
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

        // TODO: extract only pk attributes for the delete reader, no need for the others
        if (fromVersion.revision > toVersion.revision) {
            createdReader = readerFromIdsRevision(ff, null, modifiedIds.deleted, modifiedIds.fromRevision);
            deletedReader = readerFromIdsRevision(ff, null, modifiedIds.created, modifiedIds.toRevision);
            fvReader = readerFromIdsRevision(ff, mapper, modifiedIds.modified, modifiedIds.toRevision);
            tvReader = readerFromIdsRevision(ff, mapper, modifiedIds.modified, modifiedIds.fromRevision);
        } else {
            createdReader = readerFromIdsRevision(ff, null, modifiedIds.created, modifiedIds.toRevision);
            deletedReader = readerFromIdsRevision(ff, null, modifiedIds.deleted, modifiedIds.fromRevision);
            fvReader = readerFromIdsRevision(ff, mapper, modifiedIds.modified, modifiedIds.fromRevision);
            tvReader = readerFromIdsRevision(ff, mapper, modifiedIds.modified, modifiedIds.toRevision);
        }
        
    }

    /**
     * Returns a feature reader for the specified fids and revision, or null if the fid set is empty
     * 
     * @param ff
     * @param fids
     * @param ri
     * @return
     * @throws IOException
     */
     FeatureReader<SimpleFeatureType, SimpleFeature> readerFromIdsRevision(FilterFactory ff, VersionedFIDMapper mapper, Set fids,
            RevisionInfo ri) throws IOException {
        if (fids != null && !fids.isEmpty()) {
            Filter fidFilter = store.buildFidFilter(fids);
            Filter versionFilter = store.buildVersionedFilter(externalFeatureType.getTypeName(),
                    fidFilter, ri);
            DefaultQuery query = new DefaultQuery(externalFeatureType.getTypeName(), versionFilter);
            if (mapper != null) {
                List sort = new ArrayList(mapper.getColumnCount() - 1);
                for (int i = 0; i < mapper.getColumnCount(); i++) {
                    String colName = mapper.getColumnName(i);
                    if (!"revision".equals(colName))
                        sort.add(ff.sort(colName, SortOrder.DESCENDING));
                }
                query.setSortBy((SortBy[]) sort.toArray(new SortBy[sort.size()]));
            }
            return store.wrapped.getFeatureReader(query, transaction);
        } else {
            return null;
        }
    }

    /**
     * The first version used to compute the difference
     * 
     * @return
     */
    public String getFromVersion() {
        return fromVersion.getVersion();
    }

    /**
     * The second version used to computed the difference
     * 
     * @return
     */
    public String getToVersion() {
        return toVersion.getVersion();
    }

    /**
     * Returns the feature type whose features are diffed with this reader
     * 
     * @return
     */
    public SimpleFeatureType getSchema() {
        return externalFeatureType;
    }

    /**
     * Reads the next FeatureDifference
     * 
     * @return The next FeatureDifference
     * 
     * @throws IOException
     *             If an error occurs reading the FeatureDifference.
     * @throws NoSuchElementException
     *             If there are no more Features in the Reader.
     */
    public FeatureDiff next() throws IOException, NoSuchElementException {
        // check we have something, and force reader mantainance as well, so that
        // we make sure finished ones are nullified
        if (!hasNext())
            throw new NoSuchElementException("No more diffs in this reader");
        if (createdReader != null) {
            return new FeatureDiff(null, gatherNextUnversionedFeature(createdReader));
        } else if (deletedReader != null) {
            return new FeatureDiff(gatherNextUnversionedFeature(deletedReader), null);
        } else {
            FeatureDiff diff = lastDiff;
            lastDiff = null;
            return diff;
        }

    }

    /**
     * Turns a versioned feature into the extenal equivalent, with modified fid and without the
     * versioning columns
     * 
     * @param f
     * @return
     * @throws IllegalAttributeException
     */
    private SimpleFeature gatherNextUnversionedFeature(final  FeatureReader<SimpleFeatureType, SimpleFeature> fr) throws IOException {
        try {
            final SimpleFeature f = fr.next();
            final Object[] attributes = new Object[externalFeatureType.getAttributeCount()];
            for (int i = 0; i < externalFeatureType.getAttributeCount(); i++) {
                attributes[i] = f.getAttribute(externalFeatureType.getDescriptor(i).getLocalName());
            }
            String id = mapper.getUnversionedFid(f.getID());
            return SimpleFeatureBuilder.build(externalFeatureType, attributes, id);
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("Could not properly load the fetures to diff: " + e);
        }

    }

    /**
     * Query whether this FeatureDiffReader has another FeatureDiff.
     * 
     * @return True if there are more differences to be read. In other words, true if calls to next
     *         would return a feature rather than throwing an exception.
     * 
     * @throws IOException
     *             If an error occurs determining if there are more Features.
     */
    public boolean hasNext() throws IOException {
        // we first scan created, then removed, then the two that need to be diffed (which are
        // guaranteed to be parallel, so check just one)
        if (createdReader != null) {
            if (createdReader.hasNext()) {
                return true;
            } else {
                createdReader.close();
                createdReader = null;
            }
        }
        if (deletedReader != null) {
            if (deletedReader.hasNext()) {
                return true;
            } else {
                deletedReader.close();
                deletedReader = null;
            }
        }
        // this is harder... we may have features that have changed between fromVersion and
        // toVersion, but which are equal in those two (typical case, rollback). So we really
        // need to compute the diff and move forward if there's no difference at all
        if (lastDiff != null)
            return true;
        if (fvReader != null && tvReader != null) {
            while (true) {
                if (!fvReader.hasNext()) {
                    lastDiff = null;
                    fvReader.close();
                    tvReader.close();
                    return false;
                }
                // compute field by field difference
                SimpleFeature from = gatherNextUnversionedFeature(fvReader);
                SimpleFeature to = gatherNextUnversionedFeature(tvReader);
                FeatureDiff diff = new FeatureDiff(from, to);
                if (diff.getChangedAttributes().size() != 0) {
                    lastDiff = diff;
                    return true;
                }
            }
        } else {
            return false; // closed;
        }
    }

    /**
     * Resets the reader to the initial position
     * 
     * @throws IOException
     */
    public void reset() throws IOException {
        close();
        initReaders();
    }

    /**
     * Release the underlying resources associated with this stream.
     * 
     * @throws IOException
     *             DOCUMENT ME!
     */
    public void close() throws IOException {
        if (createdReader != null) {
            createdReader.close();
            createdReader = null;
        }
        if (deletedReader != null) {
            deletedReader.close();
            deletedReader = null;
        }
        if (fvReader != null) {
            fvReader.close();
            fvReader = null;
        }
        if (tvReader != null) {
            tvReader.close();
            tvReader = null;
        }
    }

    protected void finalize() throws Throwable {
        if(createdReader != null || deletedReader != null || fvReader != null || tvReader != null) {
            LOGGER.warning("There's code leaaving the feature diff readers open!");
            close();
        }
    }

}
