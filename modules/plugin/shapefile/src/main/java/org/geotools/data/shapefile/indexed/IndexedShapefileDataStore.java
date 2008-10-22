/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.shapefile.indexed;

import static org.geotools.data.shapefile.ShpFileType.DBF;
import static org.geotools.data.shapefile.ShpFileType.FIX;
import static org.geotools.data.shapefile.ShpFileType.QIX;
import static org.geotools.data.shapefile.ShpFileType.SHP;
import static org.geotools.data.shapefile.ShpFileType.SHX;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FIDReader;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.InProcessLockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.TransactionStateDiff;
import org.geotools.data.shapefile.FileWriter;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.ShpFileType;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.dbf.IndexedDbaseFileReader;
import org.geotools.data.shapefile.shp.IndexFile;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileReader.Record;
import org.geotools.feature.SchemaException;
import org.geotools.feature.visitor.IdCollectorFilterVisitor;
import org.geotools.filter.FilterAttributeExtractor;
import org.geotools.filter.visitor.ExtractBoundsFilterVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.index.CloseableCollection;
import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.LockTimeoutException;
import org.geotools.index.TreeException;
import org.geotools.index.quadtree.QuadTree;
import org.geotools.index.quadtree.StoreException;
import org.geotools.index.quadtree.fs.FileSystemIndexStore;
import org.geotools.index.rtree.RTree;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A DataStore implementation which allows reading and writing from Shapefiles.
 * 
 * @author Ian Schneider
 * @author Tommaso Nolli
 * @author jesse eichar
 * 
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/constantTimeFid/src/org/geotools/data/shapefile/indexed/IndexedShapefileDataStore.java $
 */
public class IndexedShapefileDataStore extends ShapefileDataStore implements
        FileWriter {
    IndexType treeType;

    final boolean useIndex;

    private RTree rtree;

    int maxDepth;

    /**
     * Creates a new instance of ShapefileDataStore.
     * 
     * @param url
     *                The URL of the shp file to use for this DataSource.
     */
    public IndexedShapefileDataStore(URL url)
            throws java.net.MalformedURLException {
        this(url, null, false, true, IndexType.QIX);
    }

    /**
     * Creates a new instance of ShapefileDataStore.
     * 
     * @param url
     *                The URL of the shp file to use for this DataSource.
     * @param namespace
     *                DOCUMENT ME!
     */
    public IndexedShapefileDataStore(URL url, URI namespace)
            throws java.net.MalformedURLException {
        this(url, namespace, false, true, IndexType.QIX);
    }

    /**
     * Creates a new instance of ShapefileDataStore.
     * 
     * @param url
     *                The URL of the shp file to use for this DataSource.
     * @param useMemoryMappedBuffer
     *                enable/disable memory mapping of files
     * @param createIndex
     *                enable/disable automatic index creation if needed
     */
    public IndexedShapefileDataStore(URL url, boolean useMemoryMappedBuffer,
            boolean createIndex) throws java.net.MalformedURLException {
        this(url, null, useMemoryMappedBuffer, createIndex, IndexType.QIX);
    }

    /**
     * Creates a new instance of ShapefileDataStore.
     * 
     * @param url
     *                The URL of the shp file to use for this DataSource.
     * @param namespace
     *                DOCUMENT ME!
     * @param useMemoryMappedBuffer
     *                enable/disable memory mapping of files
     * @param createIndex
     *                enable/disable automatic index creation if needed
     * @param treeType
     *                The type of index to use
     * 
     */
    public IndexedShapefileDataStore(URL url, URI namespace,
            boolean useMemoryMappedBuffer, boolean createIndex,
            IndexType treeType) throws MalformedURLException {
        this(url, namespace, useMemoryMappedBuffer, createIndex, treeType,
                DEFAULT_STRING_CHARSET);
    }

    /**
     * Creates a new instance of ShapefileDataStore.
     * 
     * @param url
     *                The URL of the shp file to use for this DataSource.
     * @param namespace
     *                DOCUMENT ME!
     * @param useMemoryMappedBuffer
     *                enable/disable memory mapping of files
     * @param createIndex
     *                enable/disable automatic index creation if needed
     * @param treeType
     *                The type of index used
     * @param dbfCharset
     *                {@link Charset} used to decode strings from the DBF
     * 
     * @throws NullPointerException
     *                 DOCUMENT ME!
     * @throws .
     */
    public IndexedShapefileDataStore(URL url, URI namespace,
            boolean useMemoryMappedBuffer, boolean createIndex,
            IndexType treeType, Charset dbfCharset)
            throws java.net.MalformedURLException {
        super(url, namespace, useMemoryMappedBuffer, dbfCharset);

        this.treeType = treeType;
        this.useIndex = treeType != IndexType.NONE;
        maxDepth = -1;
        try {
            if (shpFiles.isLocal() && createIndex
                    && needsGeneration(treeType.shpFileType)) {
                createSpatialIndex();
            }
        } catch (IOException e) {
            this.treeType = IndexType.NONE;
            ShapefileDataStoreFactory.LOGGER.log(Level.SEVERE, e
                    .getLocalizedMessage());
        }
        try {
            if (shpFiles.isLocal() && needsGeneration(FIX)) {
                generateFidIndex();
            }
        } catch (IOException e) {
            ShapefileDataStoreFactory.LOGGER.log(Level.SEVERE, e
                    .getLocalizedMessage());
        }

    }

    /**
     * Forces the spatial index to be created
     */
    public void createSpatialIndex() throws IOException {
        buildQuadTree(maxDepth);
    }

    protected void finalize() throws Throwable {
        if (rtree != null) {
            try {
                rtree.close();
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER
                        .severe("org.geotools.data.shapefile.indexed.IndexedShapeFileDataStore#finalize(): Error closing rtree. "
                                + e.getLocalizedMessage());
            }
        }
    }

    protected Filter getUnsupportedFilter(String typeName, Filter filter) {

        if (filter instanceof Id && isLocal() && shpFiles.exists(FIX))
            return Filter.INCLUDE;

        return filter;
    }

    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriterAppend(String typeName,
            Transaction transaction) throws IOException {
        if (transaction == null) {
            throw new NullPointerException(
                    "getFeatureWriter requires Transaction: "
                            + "did you mean to use Transaction.AUTO_COMMIT?");
        }

        FeatureWriter<SimpleFeatureType, SimpleFeature> writer;

        if (transaction == Transaction.AUTO_COMMIT) {
            return super.getFeatureWriterAppend(typeName, transaction);
        } else {
            writer = state(transaction).writer(typeName, Filter.EXCLUDE);
        }

        if (getLockingManager() != null) {
            // subclass has not provided locking so we will
            // fake it with InProcess locks
            writer = ((InProcessLockingManager) getLockingManager())
                    .checkedWriter(writer, transaction);
        }

        while (writer.hasNext())
            writer.next();
        return writer;
    }

    /**
     * This method is identical to the super class WHY?
     */
    protected TransactionStateDiff state(Transaction transaction) {
        synchronized (transaction) {
            TransactionStateDiff state = (TransactionStateDiff) transaction
                    .getState(this);

            if (state == null) {
                state = new TransactionStateDiff(this);
                transaction.putState(this, state);
            }

            return state;
        }
    }

    /**
     * Use the spatial index if available and adds a small optimization: if no
     * attributes are going to be read, don't uselessly open and read the dbf
     * file.
     * 
     * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String,
     *      org.geotools.data.Query)
     */
    protected  FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(String typeName, Query query)
            throws IOException {
        if (query.getFilter() == Filter.EXCLUDE)
            return new EmptyFeatureReader<SimpleFeatureType, SimpleFeature>(getSchema());

        String[] propertyNames = query.getPropertyNames() == null ? new String[0]
                : query.getPropertyNames();
        String defaultGeomName = schema.getGeometryDescriptor().getLocalName();

        FilterAttributeExtractor fae = new FilterAttributeExtractor();
        query.getFilter().accept(fae, null);

        Set attributes = new HashSet(Arrays.asList(propertyNames));
        attributes.addAll(fae.getAttributeNameSet());

        SimpleFeatureType newSchema = schema;
        boolean readDbf = true;
        boolean readGeometry = true;

        propertyNames = (String[]) attributes.toArray(new String[attributes
                .size()]);

        try {
            if (((query.getPropertyNames() != null)
                    && (propertyNames.length == 1) && propertyNames[0]
                    .equals(defaultGeomName))) {
                readDbf = false;
                newSchema = DataUtilities.createSubType(schema, propertyNames);
            } else if ((query.getPropertyNames() != null)
                    && (propertyNames.length == 0)) {
                readDbf = false;
                readGeometry = false;
                newSchema = DataUtilities.createSubType(schema, propertyNames);
            }

            return createFeatureReader(typeName, getAttributesReader(readDbf,
                    readGeometry, query.getFilter()), newSchema);
        } catch (SchemaException se) {
            throw new DataSourceException("Error creating schema", se);
        }
    }

    protected  FeatureReader<SimpleFeatureType, SimpleFeature> createFeatureReader(String typeName,
            IndexedShapefileAttributeReader r, SimpleFeatureType readerSchema)
            throws SchemaException, IOException {

        FIDReader fidReader;
        if (!indexUseable(FIX)) {
            fidReader = new ShapeFIDReader(getCurrentTypeName(), r);
        } else {
            fidReader = new IndexedFidReader(shpFiles, r);
        }
        return new org.geotools.data.FIDFeatureReader(r, fidReader,
                readerSchema);
    }

    /**
     * Forces the FID index to be regenerated
     * 
     * @throws IOException
     */
    public void generateFidIndex() throws IOException {
        FidIndexer.generate(shpFiles);
    }

    /**
     * Returns the attribute reader, allowing for a pure shape reader, or a
     * combined dbf/shp reader.
     * 
     * @param readDbf -
     *                if true, the dbf fill will be opened and read
     * @param readGeometry
     *                DOCUMENT ME!
     * @param filter -
     *                a Filter to use
     * 
     * 
     * @throws IOException
     */
    protected IndexedShapefileAttributeReader getAttributesReader(
            boolean readDbf, boolean readGeometry, Filter filter)
            throws IOException {
        Envelope bbox = new ReferencedEnvelope(); // will be bbox.isNull() to
        // start

        CloseableCollection<Data> goodRecs = null;
        if (filter instanceof Id && shpFiles.isLocal() && shpFiles.exists(FIX)) {
            Id fidFilter = (Id) filter;
            Set<?> fids = (Set<?>) fidFilter.getIDs();

            goodRecs = queryFidIndex((Set<String>) fids);
        } else {
            if (filter != null) {
                // Add additional bounds from the filter
                // will be null for Filter.EXCLUDES
                bbox = (Envelope) filter.accept(
                        ExtractBoundsFilterVisitor.BOUNDS_VISITOR, bbox);
                if (bbox == null) {
                    bbox = new ReferencedEnvelope();
                    // we hit Filter.EXCLUDES consider returning an empty
                    // reader?
                    // (however should simplify the filter to detect ff.not(
                    // fitler.EXCLUDE )
                }
            }

            if (!bbox.isNull() && this.useIndex) {
                try {
                    goodRecs = this.queryQuadTree(bbox);
                } catch (TreeException e) {
                    throw new IOException("Error querying index: "
                            + e.getMessage());
                }
            }
        }
        List<AttributeDescriptor> atts = (schema == null) ? readAttributes()
                : schema.getAttributeDescriptors();

        IndexedDbaseFileReader dbfR = null;

        if (!readDbf) {
            LOGGER.fine("The DBF file won't be opened since no attributes "
                    + "will be read from it");
            atts = new ArrayList<AttributeDescriptor>(1);
            atts.add(schema.getGeometryDescriptor());

            if (!readGeometry) {
                atts = new ArrayList<AttributeDescriptor>(1);
            }
        } else {
            dbfR = (IndexedDbaseFileReader) openDbfReader();
        }

        return new IndexedShapefileAttributeReader(atts, openShapeReader(),
                dbfR, goodRecs);
    }

    /**
     * Uses the Fid index to quickly lookup the shp offset and the record number
     * for the list of fids
     * 
     * @param fids
     *                the fids of the features to find.
     * @return a list of Data objects
     * @throws IOException
     * @throws TreeException
     */
    private CloseableCollection<Data> queryFidIndex(Set<String> idsSet) throws IOException {

        if (!indexUseable(FIX)) {
            return null;
        }

        String fids[] = (String[]) idsSet.toArray(new String[idsSet.size()]);
        Arrays.sort(fids);

        IndexedFidReader reader = new IndexedFidReader(shpFiles);

        CloseableCollection<Data> records = new CloseableArrayList(fids.length);
        try {
            IndexFile shx = openIndexFile();
            try {

                DataDefinition def = new DataDefinition("US-ASCII");
                def.addField(Integer.class);
                def.addField(Long.class);
                for (int i = 0; i < fids.length; i++) {
                    long recno = reader.findFid(fids[i]);
                    if (recno == -1){
                        if(LOGGER.isLoggable(Level.FINEST)){
                            LOGGER.finest("fid " + fids[i] + " not found in index, continuing with next queried fid...");
                        }
                        continue;
                    }
                    try {
                        Data data = new Data(def);
                        data.addValue(new Integer((int) recno + 1));
                        data.addValue(new Long(shx
                                .getOffsetInBytes((int) recno)));
                        if(LOGGER.isLoggable(Level.FINEST)){
                            LOGGER.finest("fid " + fids[i] + " found for record #"
                                    + data.getValue(0) + " at index file offset "
                                    + data.getValue(1));
                        }
                        records.add(data);
                    } catch (Exception e) {
                        IOException exception = new IOException();
                        exception.initCause(e);
                        throw exception;
                    }
                }
            } finally {
                shx.close();
            }
        } finally {
            reader.close();
        }

        return records;
    }

    /**
     * Returns true if the index for the given type exists and is useable.
     * 
     * @param indexType
     *                the type of index to check
     * 
     * @return true if the index for the given type exists and is useable.
     */
    public boolean indexUseable(ShpFileType indexType) {
        if (isLocal()) {
            if (needsGeneration(indexType) || !shpFiles.exists(indexType)) {
                return false;
            }
        } else {

            ReadableByteChannel read = null;
            try {
                read = shpFiles.getReadChannel(indexType, this);
            } catch (IOException e) {
                return false;
            } finally {
                if (read != null) {
                    try {
                        read.close();
                    } catch (IOException e) {
                        ShapefileDataStoreFactory.LOGGER.log(Level.WARNING,
                                "could not close stream", e);
                    }
                }
            }
        }

        return true;
    }

    boolean needsGeneration(ShpFileType indexType) {
        if (!isLocal())
            throw new IllegalStateException(
                    "This method only applies if the files are local and the file can be created");

        URL indexURL = shpFiles.acquireRead(indexType, this);
        URL shpURL = shpFiles.acquireRead(SHP, this);
        try {

            if (indexURL == null) {
                return true;
            }
            // indexes require both the SHP and SHX so if either or missing then
            // you don't need to
            // index
            if (!shpFiles.exists(SHX) || !shpFiles.exists(SHP)) {
                return false;
            }

            File indexFile = DataUtilities.urlToFile(indexURL);
            File shpFile = DataUtilities.urlToFile(shpURL);
            long indexLastModified = indexFile.lastModified();
            long shpLastModified = shpFile.lastModified();
            boolean shpChangedMoreRecently = indexLastModified < shpLastModified;
            return !indexFile.exists() || shpChangedMoreRecently;
        } finally {
            if (shpURL != null) {
                shpFiles.unlockRead(shpURL, this);
            }
            if (indexURL != null) {
                shpFiles.unlockRead(indexURL, this);
            }
        }
    }

    /**
     * Returns true if the indices already exist and do not need to be
     * regenerated or cannot be generated (IE isn't local).
     * 
     * @return true if the indices already exist and do not need to be
     *         regenerated.
     */
    public boolean isIndexed() {
        if (shpFiles.isLocal()) {
            return true;
        }
        return !needsGeneration(FIX) && !needsGeneration(treeType.shpFileType);
    }

    // /**
    // * RTree query
    // *
    // * @param bbox
    // *
    // *
    // * @throws DataSourceException
    // * @throws IOException
    // */
    // private List queryRTree(Envelope bbox) throws DataSourceException,
    // IOException {
    // List goodRecs = null;
    // RTree rtree = this.openRTree();
    //
    // try {
    // if ((rtree != null) && (rtree.getBounds() != null)
    // && !bbox.contains(rtree.getBounds())) {
    // goodRecs = rtree.search(bbox);
    // }
    // } catch (LockTimeoutException le) {
    // throw new DataSourceException("Error querying RTree", le);
    // } catch (TreeException re) {
    // throw new DataSourceException("Error querying RTree", re);
    // }
    //
    // return goodRecs;
    // }

    /**
     * QuadTree Query
     * 
     * @param bbox
     * 
     * 
     * @throws DataSourceException
     * @throws IOException
     * @throws TreeException
     *                 DOCUMENT ME!
     */
    private CloseableCollection<Data> queryQuadTree(Envelope bbox)
            throws DataSourceException, IOException, TreeException {
        CloseableCollection<Data> tmp = null;

        try {
            QuadTree quadTree = openQuadTree();
            if ((quadTree != null)
                    && !bbox.contains(quadTree.getRoot().getBounds())) {
                tmp = quadTree.search(bbox);

                if (tmp == null || !tmp.isEmpty())
                    return tmp;
            }
            if (quadTree != null) {
                quadTree.close();
            }
        } catch (Exception e) {
            throw new DataSourceException("Error querying QuadTree", e);
        }

        return null;
    }

    /**
     * Convenience method for opening a DbaseFileReader.
     * 
     * @return A new DbaseFileReader
     * 
     * @throws IOException
     *                 If an error occurs during creation.
     */
    protected DbaseFileReader openDbfReader() throws IOException {
        if (shpFiles.get(DBF) == null) {
            return null;
        }

        if (isLocal() && !shpFiles.exists(DBF)) {
            return null;
        }

        return new IndexedDbaseFileReader(shpFiles, false, dbfCharset);
    }

    //
    // /**
    // * Convenience method for opening an RTree index.
    // *
    // * @return A new RTree.
    // *
    // * @throws IOException
    // * If an error occurs during creation.
    // * @throws DataSourceException
    // * DOCUMENT ME!
    // */
    // protected RTree openRTree() throws IOException {
    // if (!isLocal()) {
    // return null;
    // }
    // URL treeURL = shpFiles.acquireRead(GRX, this);
    // try {
    // File treeFile = DataUtilities.urlToFile(treeURL);
    //
    // if (!treeFile.exists() || (treeFile.length() == 0)) {
    // treeType = IndexType.NONE;
    // return null;
    // }
    //
    // try {
    // FileSystemPageStore fps = new FileSystemPageStore(treeFile);
    // rtree = new RTree(fps);
    // } catch (TreeException re) {
    // throw new DataSourceException("Error opening RTree", re);
    // }
    //
    // return rtree;
    // } finally {
    // shpFiles.unlockRead(treeURL, this);
    // }
    // }

    /**
     * Convenience method for opening a QuadTree index.
     * 
     * @return A new QuadTree
     * 
     * @throws StoreException
     */
    protected QuadTree openQuadTree() throws StoreException {
        if (!isLocal()) {
            return null;
        }
        URL treeURL = shpFiles.acquireRead(QIX, this);
        try {
            File treeFile = DataUtilities.urlToFile(treeURL);

            if (!treeFile.exists() || (treeFile.length() == 0)) {
                treeType = IndexType.NONE;
                return null;
            }

            try {
                FileSystemIndexStore store = new FileSystemIndexStore(treeFile);
                return store.load(openIndexFile());
            } catch (IOException e) {
                throw new StoreException(e);
            }
        } finally {
            shpFiles.unlockRead(treeURL, this);
        }

    }

    /**
     * Create a FeatureWriter for the given type name.
     * 
     * @param typeName
     *                The typeName of the FeatureType to write
     * @param transaction
     *                DOCUMENT ME!
     * 
     * @return A new FeatureWriter.
     * 
     * @throws IOException
     *                 If the typeName is not available or some other error
     *                 occurs.
     */
    protected FeatureWriter<SimpleFeatureType, SimpleFeature> createFeatureWriter(String typeName,
            Transaction transaction) throws IOException {
        typeCheck(typeName);

         FeatureReader<SimpleFeatureType, SimpleFeature> featureReader;
        IndexedShapefileAttributeReader attReader = getAttributesReader(true,
                true, null);
        try {
            SimpleFeatureType schema = getSchema();
            if (schema == null) {
                throw new IOException(
                        "To create a shapefile, you must first call createSchema()");
            }
            featureReader = createFeatureReader(typeName, attReader, schema);

        } catch (Exception e) {

            featureReader = new EmptyFeatureReader<SimpleFeatureType, SimpleFeature>(schema);
        }

        return new IndexedShapefileFeatureWriter(typeName, shpFiles, attReader,
                featureReader, this);
    }

    /**
     * @see org.geotools.data.AbstractDataStore#getBounds(org.geotools.data.Query)
     */
    protected ReferencedEnvelope getBounds(Query query) throws IOException {
        ReferencedEnvelope ret = null;

        Set records = new HashSet();
        Filter filter = query.getFilter();
        if (filter == Filter.INCLUDE || query == Query.ALL) {
            return getBounds();
        }
        // else if (this.useIndex) {
        // if (treeType == IndexType.GRX) {
        // return getBoundsRTree(query);
        // }
        // }

        Set<String> fids = (Set<String>) filter.accept(
                IdCollectorFilterVisitor.ID_COLLECTOR, new HashSet());

        if (!fids.isEmpty()) {
            Collection<Data> recordsFound = queryFidIndex(fids);
            if (recordsFound != null) {
                records.addAll(recordsFound);
            }
        }

        if (records.isEmpty())
            return null;

        ShapefileReader reader = new ShapefileReader(shpFiles, false, false);
        try {
            ret = new ReferencedEnvelope(getSchema().getCoordinateReferenceSystem());
            for (Iterator iter = records.iterator(); iter.hasNext();) {
                Data data = (Data) iter.next();
                reader.goTo(((Long) data.getValue(1)).intValue());
                Record record = reader.nextRecord();
                ret.expandToInclude(new Envelope(record.minX, record.maxX,
                        record.minY, record.maxY));
            }
            return ret;
        } finally {
            reader.close();
        }
    }

    // private ReferencedEnvelope getBoundsRTree(Query query) throws IOException
    // {
    // ReferencedEnvelope ret = null;
    //
    // RTree rtree = this.openRTree();
    //
    // if (rtree != null) {
    // try {
    // Envelope envelopeFromIndex = rtree.getBounds(query.getFilter());
    // ret = new ReferencedEnvelope(envelopeFromIndex, schema.getCRS());
    // } catch (TreeException e) {
    // LOGGER.log(Level.SEVERE, e.getMessage(), e);
    // } catch (UnsupportedFilterException e) {
    // // Ignoring...
    // } finally {
    // try {
    // rtree.close();
    // } catch (Exception ee) {
    // }
    // }
    // }
    // return ret;
    // }

    /**
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     *
    public FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource(final String typeName)
            throws IOException {
        final SimpleFeatureType featureType = getSchema(typeName);

        if (isWriteable) {
            if (getLockingManager() != null) {
                return new AbstractFeatureLocking() {
                    public DataStore getDataStore() {
                        return IndexedShapefileDataStore.this;
                    }

                    public void addFeatureListener(FeatureListener listener) {
                        listenerManager.addFeatureListener(this, listener);
                    }

                    public void removeFeatureListener(FeatureListener listener) {
                        listenerManager.removeFeatureListener(this, listener);
                    }

                    public SimpleFeatureType getSchema() {
                        return featureType;
                    }

                    public ReferencedEnvelope getBounds(Query query)
                            throws IOException {
                        return IndexedShapefileDataStore.this.getBounds(query);
                    }
                };
            } else {
                return new AbstractFeatureStore() {
                    public DataStore getDataStore() {
                        return IndexedShapefileDataStore.this;
                    }

                    public void addFeatureListener(FeatureListener listener) {
                        listenerManager.addFeatureListener(this, listener);
                    }

                    public void removeFeatureListener(FeatureListener listener) {
                        listenerManager.removeFeatureListener(this, listener);
                    }

                    public SimpleFeatureType getSchema() {
                        return featureType;
                    }

                    public ReferencedEnvelope getBounds(Query query)
                            throws IOException {
                        return IndexedShapefileDataStore.this.getBounds(query);
                    }
                };
            }
        } else {
            return new AbstractFeatureSource() {
                public DataStore getDataStore() {
                    return IndexedShapefileDataStore.this;
                }

                public void addFeatureListener(FeatureListener listener) {
                    listenerManager.addFeatureListener(this, listener);
                }

                public void removeFeatureListener(FeatureListener listener) {
                    listenerManager.removeFeatureListener(this, listener);
                }

                public SimpleFeatureType getSchema() {
                    return featureType;
                }

                public ReferencedEnvelope getBounds(Query query)
                        throws IOException {
                    return IndexedShapefileDataStore.this.getBounds(query);
                }
            };
        }
    }
    */

    //
    // /**
    // * Builds the RTree index
    // *
    // * @throws TreeException
    // * DOCUMENT ME!
    // */
    // void buildRTree() throws TreeException {
    // if (isLocal()) {
    // LOGGER.fine("Creating spatial index for " + shpFiles.get(SHP));
    //
    // synchronized (this) {
    // if (rtree != null) {
    // rtree.close();
    // }
    //
    // rtree = null;
    // }
    //
    // ShapeFileIndexer indexer = new ShapeFileIndexer();
    // indexer.setIdxType(IndexType.GRX);
    // indexer.setShapeFileName(shpFiles);
    //
    // try {
    // indexer.index(false, new NullProgressListener());
    // } catch (MalformedURLException e) {
    // throw new TreeException(e);
    // } catch (LockTimeoutException e) {
    // throw new TreeException(e);
    // } catch (Exception e) {
    // if (e instanceof TreeException) {
    // throw (TreeException) e;
    // } else {
    // throw new TreeException(e);
    // }
    // }
    // }
    // }

    /**
     * Builds the QuadTree index. Usually not necessary since reading features
     * will index when required
     * 
     * @param maxDepth
     *                depth of the tree. if < 0 then a best guess is made.
     * @throws TreeException
     */
    public void buildQuadTree(int maxDepth) throws TreeException {
        if (isLocal()) {
            LOGGER.fine("Creating spatial index for " + shpFiles.get(SHP));

            ShapeFileIndexer indexer = new ShapeFileIndexer();
            indexer.setIdxType(IndexType.QIX);
            indexer.setShapeFileName(shpFiles);
            indexer.setMax(maxDepth);

            try {
                indexer.index(false, new NullProgressListener());
            } catch (MalformedURLException e) {
                throw new TreeException(e);
            } catch (LockTimeoutException e) {
                throw new TreeException(e);
            } catch (Exception e) {
                if (e instanceof TreeException) {
                    throw (TreeException) e;
                } else {
                    throw new TreeException(e);
                }
            }
        }
    }

    public boolean isMemoryMapped() {
        return useMemoryMappedBuffer;
    }

    public String id() {
        return getClass().getName() + ": " + getCurrentTypeName();
    }
    
}
