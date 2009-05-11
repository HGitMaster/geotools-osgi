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
 *
 */
package org.geotools.arcsde.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jsqlparser.statement.select.PlainSelect;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.data.versioning.ArcSdeVersionHandler;
import org.geotools.arcsde.data.versioning.AutoCommitDefaultVersionHandler;
import org.geotools.arcsde.filter.FilterToSQLSDE;
import org.geotools.arcsde.filter.GeometryEncoderException;
import org.geotools.arcsde.filter.GeometryEncoderSDE;
import org.geotools.arcsde.pool.Command;
import org.geotools.arcsde.pool.ISession;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.data.jdbc.FilterToSQLException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.FilterAttributeExtractor;
import org.geotools.filter.visitor.PostPreProcessFilterSplittingVisitor;
import org.geotools.filter.visitor.SimplifyingFilterVisitor;
import org.geotools.filter.visitor.SimplifyingFilterVisitor.FIDValidator;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeFilter;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeQueryInfo;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeState;
import com.esri.sde.sdk.client.SeTable;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Wrapper class for SeQuery to hold a SeConnection until close() is called and provide utility
 * methods.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java
 *         /org/geotools/arcsde/data/ArcSDEQuery.java $
 * @version $Id: ArcSDEQuery.java 32709 2009-03-26 16:08:09Z groldan $
 */
class ArcSDEQuery {
    /** Shared package's logger */
    private static final Logger LOGGER = Logging.getLogger(ArcSDEQuery.class.getName());

    /**
     * The connection to the ArcSDE server obtained when first created the SeQuery in
     * <code>getSeQuery</code>. It is retained until <code>close()</code> is called. Do not use it
     * directly, but through <code>getConnection()</code>.
     * <p>
     * NOTE: this member is package visible only for unit test pourposes
     * </p>
     */
    final ISession session;

    /**
     * The exact feature type this query is about to request from the arcsde server. It could have
     * less attributes than the ones of the actual table schema, in which case only those attributes
     * will be requested.
     */
    private SimpleFeatureType schema;

    /**
     * The query built using the constraints given by the geotools Query. It must not be accessed
     * directly, but through <code>getSeQuery()</code>, since it is lazyly created
     */
    private SeQuery query;

    /**
     * Holds the geotools Filter that originated this query from which can parse the sql where
     * clause and the set of spatial filters for the ArcSDE Java API
     */
    private ArcSDEQuery.FilterSet filters;

    /** The lazyly calculated result count */
    private int resultCount = -1;

    /** DOCUMENT ME! */
    private FIDReader fidReader;

    private Object[] previousRowValues;

    private ArcSdeVersionHandler versioningHandler;

    /**
     * Creates a new SDEQuery object.
     * 
     * @param session
     *            the session attached to the life cycle of this query
     * @param schema
     *            the schema with all the attributes as expected.
     * @param filterSet
     *            DOCUMENT ME!
     * @param versioningHandler
     *            used to transparently set up SeQuery streams pointing to the propper version edit
     *            state when appropriate
     * @throws DataSourceException
     *             DOCUMENT ME!
     * @see prepareQuery
     */
    private ArcSDEQuery(final ISession session, final SimpleFeatureType schema,
            final FilterSet filterSet, final FIDReader fidReader,
            ArcSdeVersionHandler versioningHandler) throws DataSourceException {
        this.session = session;
        this.schema = schema;
        this.filters = filterSet;
        this.fidReader = fidReader;
        this.versioningHandler = versioningHandler;
    }

    /**
     * Creates a Query to be executed over a registered ArcSDE layer (whether it is from a table or
     * a spatial view).
     * 
     * @param session
     *            the session the query works over. As its managed by the calling code its the
     *            calling code responsibility to close it when done.
     * @param fullSchema
     * @param query
     * @param isMultiversioned
     *            whether the table is versioned, if so, the default version and current state will
     *            be used for the SeQuery
     * @return
     * @throws IOException
     */
    public static ArcSDEQuery createQuery(final ISession session,
            final SimpleFeatureType fullSchema, final Query query, final FIDReader fidReader,
            final ArcSdeVersionHandler versioningHandler) throws IOException {

        Filter filter = query.getFilter();

        LOGGER.fine("Creating new ArcSDEQuery");

        final String typeName = fullSchema.getTypeName();
        final SeLayer sdeLayer = session.getLayer(typeName);
        final SimpleFeatureType querySchema = getQuerySchema(query, fullSchema);
        // create the set of filters to work over
        final ArcSDEQuery.FilterSet filters = new ArcSDEQuery.FilterSet(sdeLayer, filter,
                querySchema, null, null, fidReader);

        final ArcSDEQuery sdeQuery;
        sdeQuery = new ArcSDEQuery(session, querySchema, filters, fidReader, versioningHandler);
        return sdeQuery;
    }

    /**
     * Creates a query to be executed over an inprocess view (a view defined by a SQL SELECT
     * statement at the datastore configuration)
     * 
     * @return the newly created ArcSDEQuery.
     * @throws IOException
     *             see <i>throws DataSourceException</i> bellow.
     * @see ArcSDEDataStore#registerView(String, PlainSelect)
     */
    public static ArcSDEQuery createInprocessViewQuery(final ISession session,
            final SimpleFeatureType fullSchema, final Query query,
            final SeQueryInfo definitionQuery, final PlainSelect viewSelectStatement)
            throws IOException {

        final Filter filter = query.getFilter();
        final FIDReader fidReader = FIDReader.NULL_READER;
        final SeLayer sdeLayer;

        // the first table has to be the main layer
        final SeSqlConstruct construct;
        try {
            construct = definitionQuery.getConstruct();
        } catch (SeException e) {
            throw new ArcSdeException("shouldn't happen: " + e.getMessage(), e);
        }
        final String[] tables = construct.getTables();
        String layerName = tables[0];
        // @REVISIT: HACK HERE!, look how to get rid of alias in
        // query info, or
        // better stop using queryinfo as definition query and use
        // the PlainSelect,
        // then construct the query info dynamically when needed?
        if (layerName.indexOf(" AS") > 0) {
            layerName = layerName.substring(0, layerName.indexOf(" AS"));
        }
        sdeLayer = session.getLayer(layerName);

        final SimpleFeatureType querySchema = getQuerySchema(query, fullSchema);
        // create the set of filters to work over
        final ArcSDEQuery.FilterSet filters = new ArcSDEQuery.FilterSet(sdeLayer, filter,
                querySchema, definitionQuery, viewSelectStatement, fidReader);

        final ArcSDEQuery sdeQuery;
        sdeQuery = new ArcSDEQuery(session, querySchema, filters, fidReader,
                ArcSdeVersionHandler.NONVERSIONED_HANDLER);
        return sdeQuery;
    }

    /**
     * Returns a {@link SimpleFeatureType} whichs a "view" of the <code>fullSchema</code> adapted as
     * per the required query property names.
     * 
     * @param query
     *            the query containing the list of property names required by the output schema and
     *            the {@link Filter query predicate} from which to fetch required properties to be
     *            used at runtime filter evaluation.
     * @param fullSchema
     *            a feature type representing an ArcSDE layer full schema.
     * @return a FeatureType derived from <code>fullSchema</code> which contains the property names
     *         required by the <code>query</code> and the ones referenced in the query filter.
     * @throws DataSourceException
     */
    public static SimpleFeatureType getQuerySchema(final Query query,
            final SimpleFeatureType fullSchema) throws DataSourceException {
        // guess which properties need to actually be retrieved.
        final List<String> queryColumns = getQueryColumns(query, fullSchema);
        final String[] attNames = queryColumns.toArray(new String[queryColumns.size()]);

        try {
            // create the resulting feature type for the real attributes to
            // retrieve
            SimpleFeatureType querySchema = DataUtilities.createSubType(fullSchema, attNames);
            return querySchema;
        } catch (SchemaException ex) {
            throw new DataSourceException(
                    "Some requested attributes do not match the table schema: " + ex.getMessage(),
                    ex);
        }
    }

    private static List<String> getQueryColumns(Query query, final SimpleFeatureType fullSchema)
            throws DataSourceException {
        final List<String> columnNames = new ArrayList<String>();

        final String[] queryColumns = query.getPropertyNames();

        if ((queryColumns == null) || (queryColumns.length == 0)) {
            final List<AttributeDescriptor> attNames = fullSchema.getAttributeDescriptors();
            for (Iterator<AttributeDescriptor> it = attNames.iterator(); it.hasNext();) {
                AttributeDescriptor att = it.next();
                String attName = att.getLocalName();
                // de namespace-ify the names
                // REVISIT: this shouldnt be needed!
                if (attName.indexOf(":") != -1) {
                    attName = attName.substring(attName.indexOf(":") + 1);
                }
                columnNames.add(attName);
            }
        } else {
            columnNames.addAll(Arrays.asList(queryColumns));
        }

        // Ok, say we don't support the full filter natively and it references
        // some properties, then they have to be retrieved in order to evaluate
        // the filter at runtime
        Filter f = query.getFilter();
        if (f != null) {
            final FilterAttributeExtractor attExtractor = new FilterAttributeExtractor(fullSchema);
            f.accept(attExtractor, null);
            final String[] filterAtts = attExtractor.getAttributeNames();
            for (String attName : filterAtts) {
                if (!columnNames.contains(attName)) {
                    columnNames.add(attName);
                }
            }
        }

        return columnNames;
    }

    /**
     * Returns the FID strategy used
     * 
     * @return DOCUMENT ME!
     */
    public FIDReader getFidReader() {
        return this.fidReader;
    }

    public static ArcSDEQuery.FilterSet createFilters(SeLayer layer, SimpleFeatureType schema,
            Filter filter, SeQueryInfo qInfo, PlainSelect viewSelect, FIDReader fidReader)
            throws NoSuchElementException, IOException {

        ArcSDEQuery.FilterSet filters = new ArcSDEQuery.FilterSet(layer, filter, schema, qInfo,
                viewSelect, fidReader);

        return filters;
    }

    /**
     * Returns the stream used to fetch rows, creating it if it was not yet created.
     * 
     * @throws SeException
     * @throws IOException
     */
    private SeQuery getSeQuery() throws IOException {
        if (this.query == null) {
            try {
                String[] propsToQuery = fidReader.getPropertiesToFetch(this.schema);
                this.query = createSeQueryForFetch(propsToQuery);
            } catch (SeException e) {
                throw new ArcSdeException(e);
            }
        }
        return this.query;
    }

    /**
     * creates an SeQuery with the filters provided to the constructor and returns it. Queries
     * created with this method can be used to execute and fetch results. They cannot be used for
     * other operations, such as calculating layer extents, or result count.
     * <p>
     * Difference with {@link #createSeQueryForFetch(Session, String[])} is tha this function tells
     * <code>SeQuery.setSpatialConstraints</code> to NOT return geometry based bitmasks, which are
     * needed for calculating the query extent and result count, but not for fetching SeRows.
     * </p>
     * 
     * @param propertyNames
     *            names of attributes to build the query for, respecting order
     * @return DOCUMENT ME!
     * @throws SeException
     *             if the ArcSDE Java API throws it while creating the SeQuery or setting it the
     *             spatial constraints.
     * @throws IOException
     *             DOCUMENT ME!
     */
    private SeQuery createSeQueryForFetch(String[] propertyNames) throws SeException, IOException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("constructing new sql query with connection: " + session
                    + ", propnames: " + java.util.Arrays.asList(propertyNames)
                    + " sqlConstruct where clause: '" + this.filters.getSeSqlConstruct().getWhere()
                    + "'");
        }

        final SeQuery seQuery = session.createSeQuery();
        setQueryVersionState(seQuery);

        final SeQueryInfo qInfo = filters.getQueryInfo(propertyNames);
        final SeFilter[] spatialConstraints = this.filters.getSpatialFilters();
        if (LOGGER.isLoggable(Level.FINER)) {
            String msg = "ArcSDE query is: " + toString(qInfo);
            LOGGER.finer(msg);
        }
        // try {
        session.issue(new Command<Void>() {
            @Override
            public Void execute(ISession session, SeConnection connection) throws SeException,
                    IOException {
                seQuery.prepareQueryInfo(qInfo);

                if (spatialConstraints.length > 0) {
                    final boolean setReturnGeometryMasks = false;
                    seQuery.setSpatialConstraints(SeQuery.SE_OPTIMIZE, setReturnGeometryMasks,
                            spatialConstraints);
                }

                return null;
            }
        });
        // } catch (SeException e) {
        // // HACK: a DATABASE LEVEL ERROR (code -51) occurs when using
        // // prepareQueryInfo but the geometry att is not required in the list
        // // of properties to retrieve, and thus propertyNames contains
        // // SHAPE.fid as a last resort to get a fid
        // if (-51 == e.getSeError().getSdeError()) {
        // seQuery.close();
        // seQuery = session.createSeQuery(propertyNames,
        // filters.getSeSqlConstruct());
        // setQueryVersionState(seQuery);
        // seQuery.prepareQuery();
        // } else {
        // throw new ArcSdeException(e);
        // }
        // }

        // if (spatialConstraints.length > 0) {
        // final boolean setReturnGeometryMasks = false;
        // seQuery.setSpatialConstraints(SeQuery.SE_OPTIMIZE,
        // setReturnGeometryMasks,
        // spatialConstraints);
        // }
        //
        return seQuery;
    }

    private String toString(SeQueryInfo qInfo) {
        StringBuffer sb = new StringBuffer("SeQueryInfo[\n\tcolumns=");
        try {
            SeSqlConstruct sql = qInfo.getConstruct();
            String[] tables = sql.getTables();
            String[] cols = qInfo.getColumns();
            String by = null;
            try {
                by = qInfo.getByClause();
            } catch (NullPointerException npe) {
                // no-op
            }
            String where = sql.getWhere();
            for (int i = 0; cols != null && i < cols.length; i++) {
                sb.append(cols[i]);
                if (i < cols.length - 1)
                    sb.append(", ");
            }
            sb.append("\n\tTables=");
            for (int i = 0; i < tables.length; i++) {
                sb.append(tables[i]);
                if (i < tables.length - 1)
                    sb.append(", ");
            }
            sb.append("\n\tWhere=");
            sb.append(where);
            sb.append("\n\tOrderBy=");
            sb.append(by);
        } catch (SeException e) {
            sb.append("Exception retrieving query info properties: " + e.getMessage());
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * If the table being queried is multi versioned (we have a flag indicating it), retrieves the
     * default version and its current version state to use for the query object
     * 
     * @param seQuery
     * @throws IOException
     */
    private void setQueryVersionState(SeQuery seQuery) throws IOException {
        versioningHandler.setUpStream(session, seQuery);
    }

    /**
     * Returns the schema of the originating Query
     * 
     * @return the schema of the originating Query
     */
    public SimpleFeatureType getSchema() {
        return this.schema;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public ArcSDEQuery.FilterSet getFilters() {
        return this.filters;
    }

    /**
     * Convenient method to just calculate the result count of a given query.
     */
    public static int calculateResultCount(final ISession session, final FeatureTypeInfo typeInfo,
            final Query query, final ArcSdeVersionHandler versioningHandler) throws IOException {

        ArcSDEQuery countQuery = null;
        final int count;
        try {
            final SimpleFeatureType fullSchema = typeInfo.getFeatureType();
            if (typeInfo.isInProcessView()) {
                final SeQueryInfo definitionQuery = typeInfo.getSdeDefinitionQuery();
                final PlainSelect viewSelectStatement = typeInfo.getDefinitionQuery();
                countQuery = createInprocessViewQuery(session, fullSchema, query, definitionQuery,
                        viewSelectStatement);
            } else {
                final FIDReader fidStrategy = typeInfo.getFidStrategy();
                countQuery = createQuery(session, fullSchema, query, fidStrategy, versioningHandler);
            }
            count = countQuery.calculateResultCount();
        } finally {
            if (countQuery != null) {
                countQuery.close();
            }
        }
        return count;
    }

    /**
     * Convenient method to just calculate the resulting bound box of a given query.
     */
    public static Envelope calculateQueryExtent(final ISession session,
            final FeatureTypeInfo typeInfo, final Query query,
            final ArcSdeVersionHandler versioningHandler) throws IOException {

        final SimpleFeatureType fullSchema = typeInfo.getFeatureType();
        final String defaultGeomAttName = fullSchema.getGeometryDescriptor().getLocalName();

        // we're calculating the bounds, so we'd better be sure and add the
        // spatial column to the query's propertynames
        final DefaultQuery realQuery = new DefaultQuery(query);
        realQuery.setPropertyNames(new String[] { defaultGeomAttName });

        final ArcSDEQuery boundsQuery;

        if (typeInfo.isInProcessView()) {
            final SeQueryInfo definitionQuery = typeInfo.getSdeDefinitionQuery();
            final PlainSelect viewSelectStatement = typeInfo.getDefinitionQuery();
            boundsQuery = createInprocessViewQuery(session, fullSchema, realQuery, definitionQuery,
                    viewSelectStatement);
        } else {
            boundsQuery = createQuery(session, fullSchema, realQuery, FIDReader.NULL_READER,
                    versioningHandler);
        }

        Envelope queryExtent = null;
        try {
            Filter unsupportedFilter = boundsQuery.getFilters().getUnsupportedFilter();
            if (unsupportedFilter == Filter.INCLUDE) {
                // we can only use an optimized bounds calculation if the
                // query is fully supported by sde
                queryExtent = boundsQuery.calculateQueryExtent();
            }
        } finally {
            boundsQuery.close();
        }
        return queryExtent;
    }

    /**
     * if the query has been parsed as just a where clause filter, or has no filter at all, the
     * result count calculation is optimized by selecting a <code>count()</code> single row. If the
     * filter involves any kind of spatial filter, such as BBOX, the calculation can't be optimized
     * by this way, because the ArcSDE Java API throws a <code>"DATABASE LEVEL
     * ERROR OCURRED"</code> exception. So, in this case, a query over the shape field is made and the result is
     * traversed counting the number of rows inside a while loop
     * 
     * @return DOCUMENT ME!
     * @throws IOException
     *             DOCUMENT ME!
     * @throws DataSourceException
     *             DOCUMENT ME!
     */
    public int calculateResultCount() throws IOException {

        final Command<Integer> countCmd = new Command<Integer>() {
            @Override
            public Integer execute(ISession session, SeConnection connection) throws SeException,
                    IOException {
                final String colName = ArcSDEQuery.this.schema.getGeometryDescriptor().getName()
                        .getLocalPart();
                final SeQueryInfo qInfo = filters.getQueryInfo(new String[] { colName });

                final SeFilter[] spatialFilters = filters.getSpatialFilters();

                SeQuery query = new SeQuery(connection);
                try {
                    setQueryVersionState(query);

                    if (spatialFilters != null && spatialFilters.length > 0) {
                        query.setSpatialConstraints(SeQuery.SE_OPTIMIZE, true, spatialFilters);
                    }

                    SeTable.SeTableStats tableStats = query.calculateTableStatistics("*",
                            SeTable.SeTableStats.SE_COUNT_STATS, qInfo, 0);

                    int actualCount = tableStats.getCount();
                    return new Integer(actualCount);
                } finally {
                    query.close();
                }
            }
        };

        final Integer count = session.issue(countCmd);
        return count.intValue();
    }

    public int _calculateResultCount() throws IOException {

        final Command<Integer> countCmd = new Command<Integer>() {
            @Override
            public Integer execute(ISession session, SeConnection connection) throws SeException,
                    IOException {
                final String colName = ArcSDEQuery.this.schema.getGeometryDescriptor().getName()
                        .getLocalPart();
                final SeQueryInfo queryInfo = filters.getQueryInfo(new String[] { colName });

                final String[] columns = { "*" };
                final SeFilter[] spatialFilters = filters.getSpatialFilters();

                SeSqlConstruct sql = new SeSqlConstruct();
                String[] tables = filters.getSeSqlConstruct().getTables();
                sql.setTables(tables);
                String whereClause = filters.getSeSqlConstruct().getWhere();
                if (whereClause != null) {
                    sql.setWhere(whereClause);
                }
                SeQuery query = new SeQuery(connection, columns, sql);
                setQueryVersionState(query);

                SeQueryInfo qInfo = new SeQueryInfo();
                qInfo.setConstruct(sql);

                if (spatialFilters != null && spatialFilters.length > 0) {
                    query.setSpatialConstraints(SeQuery.SE_OPTIMIZE, true, spatialFilters);
                }

                SeTable.SeTableStats tableStats = query.calculateTableStatistics("*",
                        SeTable.SeTableStats.SE_COUNT_STATS, qInfo, 0);

                int actualCount = tableStats.getCount();
                query.close();
                return new Integer(actualCount);
            }
        };

        final Integer count = session.issue(countCmd);
        return count.intValue();
    }

    /**
     * Returns the envelope for all features within the layer that pass any SQL construct, state, or
     * spatial constraints for the stream.
     * 
     * @return DOCUMENT ME!
     * @throws IOException
     *             DOCUMENT ME!
     * @throws DataSourceException
     *             DOCUMENT ME!
     */
    public Envelope calculateQueryExtent() throws IOException {
        Envelope envelope = null;

        LOGGER.fine("Building a new SeQuery to consult it's resulting envelope");

        final SeQuery extentQuery = session.createSeQuery();
        setQueryVersionState(extentQuery);

        final String[] spatialCol = { schema.getGeometryDescriptor().getLocalName() };
        final SeQueryInfo sdeQueryInfo = filters.getQueryInfo(spatialCol);
        final SeFilter[] spatialConstraints = this.filters.getSpatialFilters();

        if (LOGGER.isLoggable(Level.FINER)) {
            String msg = "ArcSDE query is: " + toString(sdeQueryInfo);
            LOGGER.finer(msg);
        }
        try {
            envelope = session.issue(new Command<Envelope>() {
                @Override
                public Envelope execute(ISession session, SeConnection connection)
                        throws SeException, IOException {

                    // extentQuery.prepareQueryInfo(sdeQueryInfo);
                    if (spatialConstraints.length > 0) {
                        extentQuery.setSpatialConstraints(SeQuery.SE_OPTIMIZE, false,
                                spatialConstraints);
                    }

                    SeExtent extent = extentQuery.calculateLayerExtent(sdeQueryInfo);

                    Envelope envelope = new Envelope(extent.getMinX(), extent.getMaxX(), extent
                            .getMinY(), extent.getMaxY());
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("got extent: " + extent + ", built envelope: " + envelope);
                    }
                    return envelope;
                }
            });
        } catch (IOException ex) {
            SeSqlConstruct sqlCons = this.filters.getSeSqlConstruct();
            String sql = (sqlCons == null) ? null : sqlCons.getWhere();
            String tables = (sqlCons == null) ? null : Arrays.asList(sqlCons.getTables())
                    .toString();
            if (ex.getCause() instanceof SeException) {
                SeException sdeEx = (SeException) ex.getCause();
                if (sdeEx.getSeError().getSdeError() == -288) {
                    // gah, the dreaded 'LOGFILE SYSTEM TABLES DO NOT EXIST'
                    // error.
                    // this error is worthless. Make it quiet, at least.
                    LOGGER.severe("ArcSDE is complaining that your 'LOGFILE SYSTEM "
                            + "TABLES DO NOT EXIST'.  This is an ignorable error.");
                }
            }
            LOGGER.log(Level.SEVERE, "***********************\ntables: " + tables + "\nfilter: "
                    + this.filters.getGeometryFilter() + "\nSQL: " + sql, ex);
            throw ex;
        } finally {
            close(extentQuery, session);
        }

        return envelope;
    }

    /**
     * Silently closes this query.
     * 
     * @param query
     * @throws IOException
     */
    private static void close(final SeQuery query, final ISession session) throws IOException {
        if (query == null) {
            return;
        }
        session.close(query);
    }

    // //////////////////////////////////////////////////////////////////////
    // //////////// RELEVANT METHODS WRAPPED FROM SeStreamOp ////////////////
    // //////////////////////////////////////////////////////////////////////

    /**
     * Closes the query.
     * <p>
     * The {@link Session connection} used by the query is not closed by this operation as it was
     * provided by the calling code and thus it is its responsibility to handle the connection life
     * cycle.
     * </p>
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        close(this.query, session);
        this.query = null;
    }

    /**
     * Tells the server to execute a stream operation.
     * 
     * @throws IOException
     *             DOCUMENT ME!
     * @throws DataSourceException
     *             DOCUMENT ME!
     */
    public void execute() throws IOException {
        final SeQuery seQuery = getSeQuery();
        session.issue(new Command<Void>() {
            @Override
            public Void execute(ISession session, SeConnection connection) throws SeException,
                    IOException {
                seQuery.execute();
                return null;
            }
        });
    }

    private SdeRow currentRow;

    /**
     * Fetches an SeRow of data.
     * 
     * @return DOCUMENT ME!
     * @throws IOException
     *             (DataSourceException) if the fetching fails
     * @throws IllegalStateException
     *             if the query was already closed or {@link #execute()} hastn't been called yet
     */
    public SdeRow fetch() throws IOException, IllegalStateException {
        if (this.query == null) {
            throw new IllegalStateException("query closed or not yet executed");
        }

        final SeQuery seQuery = getSeQuery();
        // commented out while SeToJTSGeometryFactory is in development
        // if(currentRow == null){
        // GeometryFactory geomFac = new SeToJTSGeometryFactory();
        // currentRow = new SdeRow(geomFac);
        // int geometryIndex = -1;
        // for(int i = 0; i < schema.getAttributeCount(); i++){
        // if(schema.getDescriptor(i) instanceof GeometryDescriptor){
        // geometryIndex = i;
        // break;
        // }
        // }
        // currentRow.setGeometryIndex(geometryIndex);
        // }
        // try {
        // currentRow = session.fetch(seQuery, currentRow);
        try {
            currentRow = session.fetch(seQuery);
        } catch (IOException e) {
            close();
            String msg = "Error fetching row for " + this.schema.getTypeName() + "[";
            msg += "\nFilter: " + filters.sourceFilter;
            msg += "\n where clause sent: " + filters.sdeSqlConstruct.getWhere();
            msg += "\ngeometry filter:" + filters.geometryFilter;
            LOGGER.log(Level.WARNING, msg, e);
            throw e;
        } catch (Exception e) {
            close();
            LOGGER.log(Level.SEVERE, "fetching row: " + e.getMessage(), e);
            throw new DataSourceException("fetching row: " + e.getMessage(), e);
        }

        if (currentRow != null) {
            currentRow.setPreviousValues(this.previousRowValues);
            previousRowValues = currentRow.getAll();
        }
        return currentRow;
    }

    /**
     * Sets the spatial filters on the query using SE_OPTIMIZE as the policy for spatial index
     * search
     * 
     * @param filters
     *            a set of spatial constraints to filter upon
     * @throws IOException
     *             DOCUMENT ME!
     * @throws DataSourceException
     *             DOCUMENT ME!
     */
    public void setSpatialConstraints(SeFilter[] filters) throws IOException {
        try {
            getSeQuery().setSpatialConstraints(SeQuery.SE_OPTIMIZE, false, filters);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public String toString() {
        return "Schema: " + this.schema.getTypeName() + ", query: " + this.query;
    }

    /**
     * DOCUMENT ME!
     * 
     * @author $author$
     * @version $Revision: 1.9 $
     */
    public static class FilterSet {
        /** DOCUMENT ME! */
        private SeQueryInfo definitionQuery;

        private PlainSelect layerSelectStatement;

        private FIDReader fidReader;

        /** DOCUMENT ME! */
        private final SeLayer sdeLayer;

        /** DOCUMENT ME! */
        private final Filter sourceFilter;

        /** DOCUMENT ME! */
        private Filter _sqlFilter;

        /** DOCUMENT ME! */
        private Filter geometryFilter;

        /** DOCUMENT ME! */
        private Filter unsupportedFilter;

        private FilterToSQLSDE _sqlEncoder;

        /**
         * Holds the ArcSDE Java API definition of the geometry related filters this datastore
         * implementation supports natively.
         */
        private SeFilter[] sdeSpatialFilters;

        /**
         * Holds the ArcSDE Java API definition of the <strong>non</strong> geometry related filters
         * this datastore implementation supports natively.
         */
        private SeSqlConstruct sdeSqlConstruct;

        private SimpleFeatureType featureType;

        /**
         * Creates a new FilterSet object.
         * 
         * @param sdeLayer
         *            DOCUMENT ME!
         * @param sourceFilter
         *            DOCUMENT ME!
         */
        public FilterSet(SeLayer sdeLayer, Filter sourceFilter, SimpleFeatureType ft,
                SeQueryInfo definitionQuery, PlainSelect layerSelectStatement, FIDReader fidReader) {
            assert sdeLayer != null;
            assert sourceFilter != null;
            assert ft != null;

            this.sdeLayer = sdeLayer;
            this.sourceFilter = sourceFilter;
            this.featureType = ft;
            this.definitionQuery = definitionQuery;
            this.layerSelectStatement = layerSelectStatement;
            this.fidReader = fidReader;
            createGeotoolsFilters();
        }

        /**
         * Given the <code>Filter</code> passed to the constructor, unpacks it to three different
         * filters, one for the supported SQL based filter, another for the supported Geometry based
         * filter, and the last one for the unsupported filter. All of them can be retrieved from
         * its corresponding getter.
         */
        private void createGeotoolsFilters() {
            FilterToSQLSDE sqlEncoder = getSqlEncoder();

            PostPreProcessFilterSplittingVisitor unpacker = new PostPreProcessFilterSplittingVisitor(
                    sqlEncoder.getCapabilities(), featureType, null);
            sourceFilter.accept(unpacker, null);

            SimplifyingFilterVisitor filterSimplifier = new SimplifyingFilterVisitor();
            final String typeName = this.featureType.getTypeName();
            FIDValidator validator = new SimplifyingFilterVisitor.TypeNameDotNumberFidValidator(
                    typeName);
            filterSimplifier.setFIDValidator(validator);

            this._sqlFilter = unpacker.getFilterPre();
            this._sqlFilter = (Filter) this._sqlFilter.accept(filterSimplifier, null);

            if (LOGGER.isLoggable(Level.FINE) && _sqlFilter != null)
                LOGGER.fine("SQL portion of SDE Query: '" + _sqlFilter + "'");

            Filter remainingFilter = unpacker.getFilterPost();

            unpacker = new PostPreProcessFilterSplittingVisitor(GeometryEncoderSDE
                    .getCapabilities(), featureType, null);
            remainingFilter.accept(unpacker, null);

            this.geometryFilter = unpacker.getFilterPre();
            this.geometryFilter = (Filter) this.geometryFilter.accept(filterSimplifier, null);
            if (LOGGER.isLoggable(Level.FINE) && geometryFilter != null)
                LOGGER.fine("Spatial-Filter portion of SDE Query: '" + geometryFilter + "'");

            this.unsupportedFilter = unpacker.getFilterPost();
            this.unsupportedFilter = (Filter) this.unsupportedFilter.accept(filterSimplifier, null);
            if (LOGGER.isLoggable(Level.FINE) && unsupportedFilter != null)
                LOGGER.fine("Unsupported (and therefore ignored) portion of SDE Query: '"
                        + unsupportedFilter + "'");
        }

        /**
         * Returns an SeQueryInfo that can be used to retrieve a set of SeRows from an ArcSDE layer
         * or a layer with joins. The SeQueryInfo object lacks the set of column names to fetch. It
         * is the responsibility of the calling code to call setColumns(String []) on the returned
         * object to specify which properties to fetch.
         * 
         * @param unqualifiedPropertyNames
         *            non null, possibly empty, list of property names to fetch
         * @return
         * @throws IOException
         */
        public SeQueryInfo getQueryInfo(final String[] unqualifiedPropertyNames) throws IOException {
            assert unqualifiedPropertyNames != null;
            String[] tables;
            String byClause = null;

            final SeSqlConstruct plainSqlConstruct = getSeSqlConstruct();

            String where = plainSqlConstruct.getWhere();

            try {
                if (definitionQuery == null) {
                    tables = new String[] { this.sdeLayer.getQualifiedName() };
                } else {
                    tables = definitionQuery.getConstruct().getTables();
                    String joinWhere = definitionQuery.getConstruct().getWhere();
                    if (where == null) {
                        where = joinWhere;
                    } else {
                        where = joinWhere == null ? where : (joinWhere + " AND " + where);
                    }
                    try {
                        byClause = definitionQuery.getByClause();
                    } catch (NullPointerException e) {
                        // no-op
                    }
                }

                final SeQueryInfo qInfo = new SeQueryInfo();
                final SeSqlConstruct sqlConstruct = new SeSqlConstruct();
                sqlConstruct.setTables(tables);
                if (where != null && where.length() > 0) {
                    sqlConstruct.setWhere(where);
                }

                final int queriedAttCount = unqualifiedPropertyNames.length;

                if (queriedAttCount > 0) {
                    String[] sdeAttNames = new String[queriedAttCount];
                    FilterToSQLSDE sqlEncoder = getSqlEncoder();

                    for (int i = 0; i < queriedAttCount; i++) {
                        String attName = unqualifiedPropertyNames[i];
                        String coldef = sqlEncoder.getColumnDefinition(attName);
                        sdeAttNames[i] = coldef;
                    }
                    qInfo.setColumns(sdeAttNames);
                }

                qInfo.setConstruct(sqlConstruct);
                if (byClause != null) {
                    qInfo.setByClause(byClause);
                }
                return qInfo;
            } catch (SeException e) {
                throw new ArcSdeException(e);
            }
        }

        /**
         * DOCUMENT ME!
         * 
         * @return the SeSqlConstruct corresponding to the given SeLayer and SQL based filter.
         *         Should never return null.
         * @throws DataSourceException
         *             if an error occurs encoding the sql filter to a SQL where clause, or creating
         *             the SeSqlConstruct for the given layer and where clause.
         */
        public SeSqlConstruct getSeSqlConstruct() throws DataSourceException {
            if (this.sdeSqlConstruct == null) {
                final String layerName;
                try {
                    layerName = this.sdeLayer.getQualifiedName();
                    this.sdeSqlConstruct = new SeSqlConstruct(layerName);
                } catch (SeException e) {
                    throw new ArcSdeException("Can't create SQL construct for "
                            + sdeLayer.getName(), e);
                }

                Filter sqlFilter = getSqlFilter();

                if (!Filter.INCLUDE.equals(sqlFilter)) {
                    String whereClause = null;
                    FilterToSQLSDE sqlEncoder = getSqlEncoder();

                    try {
                        whereClause = sqlEncoder.encodeToString(sqlFilter);
                    } catch (FilterToSQLException sqle) {
                        String message = "Geometry encoder error: " + sqle.getMessage();
                        throw new DataSourceException(message, sqle);
                    }
                    LOGGER.fine("ArcSDE where clause '" + whereClause + "'");

                    this.sdeSqlConstruct.setWhere(whereClause);
                }
            }

            return this.sdeSqlConstruct;
        }

        /**
         * Lazily creates the array of <code>SeShapeFilter</code> objects that map the corresponding
         * geometry related filters included in the original <code>org.geotools.data.Query</code>
         * passed to the constructor.
         * 
         * @return an array with the spatial filters to be applied to the SeQuery, or null if none.
         * @throws DataSourceException
         *             DOCUMENT ME!
         */
        public SeFilter[] getSpatialFilters() throws DataSourceException {
            if (this.sdeSpatialFilters == null) {
                GeometryEncoderSDE geometryEncoder = new GeometryEncoderSDE(this.sdeLayer,
                        featureType);

                try {
                    geometryEncoder.encode(getGeometryFilter());
                } catch (GeometryEncoderException e) {
                    throw new DataSourceException("Error parsing geometry filters: "
                            + e.getMessage(), e);
                }

                this.sdeSpatialFilters = geometryEncoder.getSpatialFilters();
            }

            return this.sdeSpatialFilters;
        }

        /**
         * DOCUMENT ME!
         * 
         * @return the subset, non geometry related, of the original filter this datastore
         *         implementation supports natively, or <code>Filter.INCLUDE</code> if the original
         *         Query does not contains non spatial filters that we can deal with at the ArcSDE
         *         Java API side.
         */
        public Filter getSqlFilter() {
            return (this._sqlFilter == null) ? Filter.INCLUDE : this._sqlFilter;
        }

        /**
         * DOCUMENT ME!
         * 
         * @return the geometry related subset of the original filter this datastore implementation
         *         supports natively, or <code>Filter.INCLUDE</code> if the original Query does not
         *         contains spatial filters that we can deal with at the ArcSDE Java API side.
         */
        public Filter getGeometryFilter() {
            return (this.geometryFilter == null) ? Filter.INCLUDE : this.geometryFilter;
        }

        /**
         * DOCUMENT ME!
         * 
         * @return the part of the original filter this datastore implementation does not supports
         *         natively, or <code>Filter.INCLUDE</code> if we support the whole Query filter.
         */
        public Filter getUnsupportedFilter() {
            return (this.unsupportedFilter == null) ? Filter.INCLUDE : this.unsupportedFilter;
        }

        private FilterToSQLSDE getSqlEncoder() {
            if (_sqlEncoder == null) {
                final String layerName;
                try {
                    layerName = sdeLayer.getQualifiedName();
                } catch (SeException e) {
                    throw (RuntimeException) new RuntimeException(
                            "error getting layer's qualified name").initCause(e);
                }
                String fidColumn = fidReader.getFidColumn();
                _sqlEncoder = new FilterToSQLSDE(layerName, fidColumn, featureType,
                        layerSelectStatement);
            }
            return _sqlEncoder;
        }
    }
}
