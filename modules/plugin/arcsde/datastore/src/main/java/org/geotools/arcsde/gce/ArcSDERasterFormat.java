/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.arcsde.gce;

import static org.geotools.arcsde.gce.RasterCellType.TYPE_1BIT;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_8BIT_S;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_8BIT_U;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.measure.unit.Unit;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEConnectionPoolFactory;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.data.DataSourceException;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.geotools.referencing.operation.transform.LinearTransform1D;
import org.geotools.util.NumberRange;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform1D;

import com.esri.sde.sdk.client.SDEPoint;
import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeQueryInfo;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterBand;
import com.esri.sde.sdk.client.SeRasterColumn;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeTable;
import com.esri.sde.sdk.client.SeTable.SeTableStats;
import com.vividsolutions.jts.geom.Envelope;

/**
 * An implementation of the ArcSDE Raster Format. Based on the ArcGrid module.
 * 
 * @author Saul Farber (saul.farber)
 * @author jeichar
 * @author Simone Giannecchini (simboss)
 * @author Gabriel Roldan (OpenGeo)
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java
 *         /org/geotools/arcsde/gce/ArcSDERasterFormat.java $
 */
@SuppressWarnings( { "nls", "deprecation" })
public class ArcSDERasterFormat extends AbstractGridFormat implements Format {

    protected static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    /**
     * Cache of raster metadata objects, where the keys are the URL's representing the full
     * connection properties to a given ArcSDE raster, and the value the
     * {@link ArcSDERasterGridCoverage2DReader}'s externalized state, so it is not needed to gather
     * the raster properties each time.
     */
    private static final Map<String, RasterInfo> rasterInfos = new WeakHashMap<String, RasterInfo>();

    private static final Map<String, ArcSDEConnectionConfig> connectionConfigs = new WeakHashMap<String, ArcSDEConnectionConfig>();

    private static final ArcSDERasterFormat instance = new ArcSDERasterFormat();

    /**
     * Creates an instance and sets the metadata.
     */
    private ArcSDERasterFormat() {
        setInfo();
    }

    public static ArcSDERasterFormat getInstance() {
        return instance;
    }

    /**
     * Sets the metadata information.
     */
    private void setInfo() {
        Map<String, String> info = new HashMap<String, String>();

        info.put("name", "ArcSDE Raster");
        info.put("description", "ArcSDE Raster Format");
        info.put("vendor", "Geotools");
        info.put("docURL", "");
        info.put("version", GeoTools.getVersion().toString());
        mInfo = info;

        readParameters = new ParameterGroup(new DefaultParameterDescriptorGroup(mInfo,
                new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D, OVERVIEW_POLICY }));
    }

    /**
     * @param source
     *            either a {@link String} or {@link File} instance representing the connection URL
     * @see AbstractGridFormat#getReader(Object source)
     */
    @Override
    public AbstractGridCoverage2DReader getReader(Object source) {
        return getReader(source, null);
    }

    /**
     * @param source
     *            either a {@link String} or {@link File} instance representing the connection URL
     * @see AbstractGridFormat#getReader(Object, Hints)
     */
    @Override
    public AbstractGridCoverage2DReader getReader(final Object source, final Hints hints) {
        try {
            if (source == null) {
                throw new DataSourceException("No source set to read this coverage.");
            }

            // this will be our connection string
            final String coverageUrl = parseCoverageUrl(source);

            final ArcSDEConnectionConfig connectionConfig = getConnectionConfig(coverageUrl);

            ArcSDEConnectionPool connectionPool = setupConnectionPool(connectionConfig);

            RasterInfo rasterInfo = getRasterInfo(coverageUrl, connectionPool);

            // return new ArcSDERasterGridCoverage2DReader(connectionPool, rasterInfo, hints);
            return new ArcSDEGridCoverage2DReaderJAI(this, connectionPool, rasterInfo, hints);
        } catch (IOException dse) {
            LOGGER
                    .log(Level.SEVERE, "Unable to creata ArcSDERasterReader for " + source + ".",
                            dse);
            throw new RuntimeException(dse);
        }
    }

    private RasterInfo getRasterInfo(final String coverageUrl, ArcSDEConnectionPool connectionPool)
            throws IOException {

        RasterInfo rasterInfo = rasterInfos.get(coverageUrl);
        if (rasterInfo == null) {
            synchronized (rasterInfos) {
                rasterInfo = rasterInfos.get(coverageUrl);
                if (rasterInfo == null) {
                    ArcSDEPooledConnection scon;
                    try {
                        scon = connectionPool.getConnection();
                    } catch (UnavailableArcSDEConnectionException e) {
                        throw new DataSourceException(e);
                    }
                    try {
                        rasterInfo = gatherCoverageMetadata(scon, coverageUrl);
                        rasterInfos.put(coverageUrl, rasterInfo);
                    } finally {
                        if (!scon.isPassivated())
                            scon.close();
                    }
                }
            }
        }
        return rasterInfo;
    }

    private ArcSDEConnectionConfig getConnectionConfig(final String coverageUrl) {
        ArcSDEConnectionConfig sdeConfig;
        sdeConfig = connectionConfigs.get(coverageUrl);
        if (sdeConfig == null) {
            synchronized (connectionConfigs) {
                sdeConfig = connectionConfigs.get(coverageUrl);
                if (sdeConfig == null) {
                    sdeConfig = sdeURLToConnectionConfig(new StringBuffer(coverageUrl));
                    connectionConfigs.put(coverageUrl, sdeConfig);
                }
            }
        }
        return sdeConfig;
    }

    /**
     * @see AbstractGridFormat#getWriter(Object)
     */
    @Override
    public GridCoverageWriter getWriter(Object destination) {
        // return new ArcGridWriter(destination);
        return null;
    }

    /**
     * @param source
     *            either a {@link String} or {@link File} instance representing the connection URL
     * @see AbstractGridFormat#accepts(Object input)
     */
    @Override
    public boolean accepts(Object input) {
        StringBuffer url;
        if (input instanceof File) {
            url = new StringBuffer(((File) input).getPath());
        } else if (input instanceof String) {
            url = new StringBuffer((String) input);
        } else {
            return false;
        }
        try {
            sdeURLToConnectionConfig(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @see Format#getName()
     */
    @Override
    public String getName() {
        return this.mInfo.get("name");
    }

    /**
     * @see Format#getDescription()
     */
    @Override
    public String getDescription() {
        return this.mInfo.get("description");
    }

    /**
     * @see Format#getVendor()
     */
    @Override
    public String getVendor() {
        return this.mInfo.get("vendor");
    }

    /**
     * @see Format#getDocURL()
     */
    @Override
    public String getDocURL() {
        return this.mInfo.get("docURL");
    }

    /**
     * @see Format#getVersion()
     */
    @Override
    public String getVersion() {
        return this.mInfo.get("version");
    }

    /**
     * Retrieves the default instance for the {@link ArcSDERasterFormat} of the
     * {@link GeoToolsWriteParams} to control the writing process.
     * 
     * @return a default instance for the {@link ArcSDERasterFormat} of the
     *         {@link GeoToolsWriteParams} to control the writing process.
     * @see AbstractGridFormat#getDefaultImageIOWriteParameters()
     */
    @Override
    public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
        throw new UnsupportedOperationException("ArcSDE Rasters are read only for now.");
    }

    // ////////////////

    /**
     * @param input
     *            either a {@link String} or a {@link File} instance representing the connection URL
     *            to a given coverage
     * @return the connection URL as a string
     */
    private String parseCoverageUrl(Object input) {
        String coverageUrl;
        if (input instanceof String) {
            coverageUrl = (String) input;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("connecting to ArcSDE Raster: " + coverageUrl);
            }
        } else if (input instanceof File) {
            coverageUrl = ((File) input).getPath();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("connectiong via file-hack to ArcSDE Raster: " + coverageUrl);
            }
        } else {
            throw new IllegalArgumentException("Unsupported input type: " + input.getClass());
        }
        return coverageUrl;
    }

    /**
     * Checks the input prvided to this {@link ArcSDERasterGridCoverage2DReader} and sets all the
     * other objects and flags accordingly.
     * 
     * @param sdeUrl
     *            a url representing the connection parameters to an arcsde server instance provied
     *            to this {@link ArcSDERasterGridCoverage2DReader}.
     * @throws IOException
     */
    private ArcSDEConnectionPool setupConnectionPool(ArcSDEConnectionConfig sdeConfig)
            throws IOException {

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Getting ArcSDE connection pool for " + sdeConfig);
        }

        ArcSDEConnectionPool connectionPool;
        connectionPool = ArcSDEConnectionPoolFactory.getInstance().createPool(sdeConfig);
        return connectionPool;
    }

    /**
     * @param sdeUrl
     *            - A StringBuffer containing a string of form
     *            'sde://user:pass@sdehost:[port]/[dbname]
     * @return a ConnectionConfig object representing these parameters
     */
    static ArcSDEConnectionConfig sdeURLToConnectionConfig(StringBuffer sdeUrl) {
        // annoyingly, geoserver currently stores the user-entered SDE string as
        // a File, and passes us the
        // File object. The File object strips the 'sde://user...' into a
        // 'sde:/user..'. So we need to check
        // for both forms of the url.
        String sdeHost, sdeUser, sdePass, sdeDBName;
        int sdePort;
        if (sdeUrl.indexOf("sde:/") == -1) {
            throw new IllegalArgumentException(
                    "ArcSDE Raster URL must be of the form sde://user:pass@sdehost:port/[dbname]#rasterTableName -- Got "
                            + sdeUrl);
        }
        if (sdeUrl.indexOf("sde://") == -1) {
            sdeUrl.delete(0, 5);
        } else {
            sdeUrl.delete(0, 6);
        }

        int idx = sdeUrl.indexOf(":");
        if (idx == -1) {
            throw new IllegalArgumentException(
                    "ArcSDE Raster URL must be of the form sde://user:pass@sdehost:port/[dbname]#rasterTableName");
        }
        sdeUser = sdeUrl.substring(0, idx);
        sdeUrl.delete(0, idx);

        idx = sdeUrl.indexOf("@");
        if (idx == -1) {
            throw new IllegalArgumentException(
                    "ArcSDE Raster URL must be of the form sde://user:pass@sdehost:port/[dbname]#rasterTableName");
        }
        sdePass = sdeUrl.substring(1, idx);
        sdeUrl.delete(0, idx);

        idx = sdeUrl.indexOf(":");
        if (idx == -1) {
            // there's no "port" specification. Assume 5151;
            sdePort = 5151;

            idx = sdeUrl.indexOf("/");
            if (idx == -1) {
                throw new IllegalArgumentException(
                        "ArcSDE Raster URL must be of the form sde://user:pass@sdehost:port/[dbname]#rasterTableName");
            }
            sdeHost = sdeUrl.substring(1, idx).toString();
            sdeUrl.delete(0, idx);
        } else {
            sdeHost = sdeUrl.substring(1, idx).toString();
            sdeUrl.delete(0, idx);

            idx = sdeUrl.indexOf("/");
            if (idx == -1) {
                throw new IllegalArgumentException(
                        "ArcSDE Raster URL must be of the form sde://user:pass@sdehost:port/[dbname]#rasterTableName");
            }
            sdePort = Integer.parseInt(sdeUrl.substring(1, idx).toString());
            sdeUrl.delete(0, idx);
        }

        idx = sdeUrl.indexOf("#");
        if (idx == -1) {
            throw new IllegalArgumentException(
                    "ArcSDE Raster URL must be of the form sde://user:pass@sdehost:port/[dbname]#rasterTableName");
        }
        sdeDBName = sdeUrl.substring(1, idx).toString();
        sdeUrl.delete(0, idx);

        return new ArcSDEConnectionConfig("arcsde", sdeHost, sdePort + "", sdeDBName, sdeUser,
                sdePass);
    }

    private RasterInfo gatherCoverageMetadata(final ArcSDEPooledConnection scon,
            final String coverageUrl) throws IOException {
        LOGGER.fine("Gathering raster dataset metadata for " + coverageUrl);
        String rasterTable;
        {
            String sdeUrl = coverageUrl;
            if (sdeUrl.indexOf(";") != -1) {
                final String extraParams = sdeUrl.substring(sdeUrl.indexOf(";") + 1, sdeUrl
                        .length());
                sdeUrl = sdeUrl.substring(0, sdeUrl.indexOf(";"));
            }
            rasterTable = sdeUrl.substring(sdeUrl.indexOf("#") + 1);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Building ArcSDEGridCoverageReader2D for " + rasterTable);
            }
        }

        final String[] rasterColumns = getRasterColumns(scon, rasterTable);
        final List<PyramidInfo> rastersLayoutInfo = new ArrayList<PyramidInfo>();
        {
            final List<RasterBandInfo> bands;

            final List<SeRasterAttr> rasterAttributes = getSeRasterAttr(scon, rasterTable,
                    rasterColumns);

            if (rasterAttributes.size() == 0) {
                throw new IllegalArgumentException("Table " + rasterTable
                        + " contains no raster datasets");
            }

            final CoordinateReferenceSystem coverageCrs;
            {
                SeRasterColumn rasterColumn;
                try {
                    SeRasterAttr ratt = rasterAttributes.get(0);
                    rasterColumn = new SeRasterColumn(scon, ratt.getRasterColumnId());
                    bands = setUpBandInfo(scon, rasterTable, ratt);
                } catch (SeException e) {
                    throw new ArcSdeException(e);
                }
                final SeCoordinateReference seCoordRef = rasterColumn.getCoordRef();
                coverageCrs = RasterUtils.findCompatibleCRS(seCoordRef);
            }
            try {
                for (SeRasterAttr rAtt : rasterAttributes) {
                    LOGGER.fine("Gathering raster metadata for " + rasterTable + " raster "
                            + rAtt.getRasterId().longValue());
                    PyramidInfo pyramidInfo = new PyramidInfo(rAtt, coverageCrs);
                    rastersLayoutInfo.add(pyramidInfo);

                    final GeneralEnvelope originalEnvelope;
                    originalEnvelope = calculateOriginalEnvelope(rAtt, coverageCrs);
                    pyramidInfo.setOriginalEnvelope(originalEnvelope);
                    pyramidInfo.setBands(bands);
                }
            } catch (SeException e) {
                throw new ArcSdeException("Gathering raster dataset information", e);
            }
        }

        RasterInfo rasterInfo = new RasterInfo();
        rasterInfo.setRasterTable(rasterTable);
        rasterInfo.setRasterColumns(rasterColumns);
        rasterInfo.setPyramidInfo(rastersLayoutInfo);

        return rasterInfo;
    }

    private GeneralEnvelope calculateOriginalEnvelope(final SeRasterAttr rasterAttributes,
            CoordinateReferenceSystem coverageCrs) throws IOException {
        SeExtent sdeExtent;
        try {
            sdeExtent = rasterAttributes.getExtent();
        } catch (SeException e) {
            throw new ArcSdeException("Exception getting the raster extent", e);
        }
        GeneralEnvelope originalEnvelope = new GeneralEnvelope(coverageCrs);
        originalEnvelope.setRange(0, sdeExtent.getMinX(), sdeExtent.getMaxX());
        originalEnvelope.setRange(1, sdeExtent.getMinY(), sdeExtent.getMaxY());
        return originalEnvelope;
    }

    private String[] getRasterColumns(final ArcSDEPooledConnection scon, final String rasterTable)
            throws IOException {

        String[] rasterColumns;
        SeTable sTable = scon.getTable(rasterTable);
        SeColumnDefinition[] cols;
        try {
            cols = sTable.describe();
        } catch (SeException e) {
            throw new ArcSdeException("Exception fetching the list of columns for table "
                    + rasterTable, e);
        }
        List<String> fetchColumns = new ArrayList<String>(cols.length / 2);
        for (int i = 0; i < cols.length; i++) {
            if (cols[i].getType() == SeColumnDefinition.TYPE_RASTER)
                fetchColumns.add(cols[i].getName());
        }
        if (fetchColumns.size() == 0) {
            throw new DataSourceException("Couldn't find any TYPE_RASTER columns in ArcSDE table "
                    + rasterTable);
        }

        rasterColumns = (String[]) fetchColumns.toArray(new String[fetchColumns.size()]);
        return rasterColumns;
    }

    private List<GridSampleDimension> buildGridSampleDimensions_Old(ArcSDEPooledConnection conn,
            String coverageName, SeRasterAttr rasterAttributes) throws IOException {

        List<GridSampleDimension> gridBands;

        try {
            final int numBands = rasterAttributes.getNumBands();
            gridBands = new ArrayList<GridSampleDimension>(numBands);

            final SeRasterBand[] sdeBands = rasterAttributes.getBands();
            final RasterCellType pixelType = RasterCellType
                    .valueOf(rasterAttributes.getPixelType());

            final LinearTransform1D identity = LinearTransform1D.IDENTITY;
            if (numBands == 1) {
                final SeRasterBand singleBand = sdeBands[0];
                if (pixelType == TYPE_1BIT) {
                    final NumberRange<Integer> sampleValueRange = NumberRange.create(0, 1);
                    final String bandName = coverageName + ": Band One (1-bit)";
                    final Color minColor = Color.BLACK;
                    final Color maxColor = Color.WHITE;
                    final Color[] colorRange = { minColor, maxColor };
                    Category bitBandCat = new Category(bandName, colorRange, sampleValueRange,
                            identity);
                    gridBands.add(new GridSampleDimension(bitBandCat.getName(),
                            new Category[] { bitBandCat }, null).geophysics(true));

                } else if (pixelType == RasterCellType.TYPE_32BIT_REAL) {
                    float minimum;
                    float maximum;
                    if (singleBand.hasStats()) {
                        minimum = (float) singleBand.getStatsMin();
                        maximum = (float) singleBand.getStatsMax();
                    } else {
                        // throw new IllegalStateException("Raster " + this.coverageName
                        // + " of type 32-bit float contains no statistics."
                        // + " Coverage sample range can't be requested.");
                        LOGGER.info(coverageName
                                + " has no statistics. Calulating min and max values...");
                        SeRasterColumn col = new SeRasterColumn(conn, rasterAttributes
                                .getRasterColumnId());
                        String tableName = col.getQualifiedTableName();
                        String rasterColName = col.getName();
                        SeQuery query = new SeQuery(conn);
                        try {
                            // let the server limit the number of values (from the giomgr.defs
                            // config file)
                            final int maxDistinctValues = 0;
                            // which stats to calculate
                            final int mask = SeTableStats.SE_ALL_STATS;// SeTableStats.SE_MIN_STATS
                            // |
                            // SeTableStats.SE_MAX_STATS;
                            SeQueryInfo queryInfo = new SeQueryInfo();
                            queryInfo.setConstruct(new SeSqlConstruct(tableName));
                            // queryInfo.setColumns(new String[] { rasterColName });
                            SeTableStats stats = query.calculateTableStatistics(rasterColName,
                                    mask, queryInfo, maxDistinctValues);
                            minimum = (float) stats.getMin();
                            maximum = (float) stats.getMax();
                        } finally {
                            query.close();
                        }
                    }
                    final String bandName = coverageName + ": Band One (32-bit floating point)";
                    Category floatBandCategory;
                    {
                        final NumberRange<Float> sampleValueRange;
                        sampleValueRange = NumberRange.create(minimum, maximum);
                        final Color minColor = Color.BLACK;
                        final Color maxColor = Color.WHITE;
                        final Color[] colorRange = { minColor, maxColor };
                        final MathTransform1D sampleToGeophysics = identity;
                        floatBandCategory = new Category(bandName, colorRange, sampleValueRange,
                                sampleToGeophysics);
                    }
                    final Unit<?> units = null;
                    Category[] categories = { floatBandCategory };
                    GridSampleDimension floatSampleDimension = new GridSampleDimension(bandName,
                            categories, units);
                    // The range of sample values in all categories maps directly the "real world"
                    // values without the need for any transformation
                    final boolean isGeophysicsView = true;
                    GridSampleDimension geophysics = floatSampleDimension
                            .geophysics(isGeophysicsView);
                    gridBands.add(geophysics);
                } else {
                    if (singleBand.hasColorMap()) {
                        // we support 1-band with colormap now
                        Category cmCat = null;// buildCategory(rasterAttributes.getBands()[0].
                        // getColorMap
                        // ());
                        gridBands.add(new GridSampleDimension(cmCat.getName(),
                                new Category[] { cmCat }, null).geophysics(true));

                    } else if (pixelType == TYPE_8BIT_S || pixelType == TYPE_8BIT_U) {
                        LOGGER.fine("Discovered 8-bit single-band raster.  "
                                + "Using return image type: TYPE_BYTE_GRAY");
                        // TODO: I guess if its TYPE_8BIT_S the range shouldn't be 0-255
                        NumberRange<Integer> sampleValueRange = NumberRange.create(0, 255);
                        Category greyscaleBandCat = new Category(coverageName
                                + ": Band One (grayscale)",
                                new Color[] { Color.BLACK, Color.WHITE }, sampleValueRange,
                                identity);
                        gridBands.add(new GridSampleDimension(greyscaleBandCat.getName(),
                                new Category[] { greyscaleBandCat }, null).geophysics(true));
                    } else if (pixelType == RasterCellType.TYPE_16BIT_U) {
                        final int minimum = 0;
                        final int maximum = 65535;
                        NumberRange<Integer> sampleValueRange = NumberRange
                                .create(minimum, maximum);
                        Category greyscaleBandCat = new Category("Band 1", new Color[] {
                                Color.BLACK, Color.WHITE }, sampleValueRange, identity);
                        gridBands.add(new GridSampleDimension(greyscaleBandCat.getName(),
                                new Category[] { greyscaleBandCat }, null).geophysics(true));
                    } else if (pixelType == RasterCellType.TYPE_16BIT_S) {
                        final short minimum = Short.MIN_VALUE;
                        final short maximum = Short.MAX_VALUE;
                        NumberRange<Short> sampleValueRange = NumberRange.create(minimum, maximum);
                        Category greyscaleBandCat = new Category("Band 1", new Color[] {
                                Color.BLACK, Color.WHITE }, sampleValueRange, identity);
                        gridBands.add(new GridSampleDimension(greyscaleBandCat.getName(),
                                new Category[] { greyscaleBandCat }, null).geophysics(true));
                    } else {
                        throw new IllegalArgumentException(
                                "One-band, non-colormapped raster layers with type " + pixelType
                                        + " are not supported.");
                    }
                }

            } else if (numBands == 3 || numBands == 4) {
                if (pixelType != TYPE_8BIT_U) {
                    throw new IllegalArgumentException(
                            "3 or 4 band rasters are only supported if they have pixel type 8-bit unsigned pixels.");
                }
                NumberRange<Integer> sampleValueRange = NumberRange.create(0, 255);
                Category nan = new Category("no-data", new Color[] { new Color(0x00000000) },
                        NumberRange.create(0, 0), identity);
                Category white = new Category("valid-data", new Color[] { new Color(0xff000000) },
                        NumberRange.create(255, 255), identity);
                Category redBandCat = new Category("red", new Color[] { Color.BLACK, Color.RED },
                        sampleValueRange, identity);
                Category blueBandCat = new Category("blue",
                        new Color[] { Color.BLACK, Color.BLUE }, sampleValueRange, identity);
                Category greenBandCat = new Category("green", new Color[] { Color.BLACK,
                        Color.GREEN }, sampleValueRange, identity);

                gridBands.add(new GridSampleDimension("Red band", new Category[] { redBandCat },
                        null));
                gridBands.add(new GridSampleDimension("Green band", new Category[] { blueBandCat },
                        null));
                gridBands.add(new GridSampleDimension("Blue band", new Category[] { greenBandCat },
                        null));
                if (numBands == 4) {
                    // temporary workaround
                    gridBands.add(new GridSampleDimension("NODATA Mask Band", new Category[] { nan,
                            white }, null));
                }

            } else {
                throw new DataSourceException("The coverage contains " + numBands
                        + " bands. We only support 1, 3 or 4 bands");
            }
        } catch (SeException e) {
            throw new ArcSdeException("Error creating the coverage's sample dimensions", e);
        }

        return gridBands;
    }

    private List<SeRasterAttr> getSeRasterAttr(ArcSDEPooledConnection scon, String rasterTable,
            String[] rasterColumns) throws IOException {

        LOGGER.fine("Gathering raster attributes for " + rasterTable);
        SeRasterAttr rasterAttributes;
        List<SeRasterAttr> rasterAttList = new ArrayList<SeRasterAttr>();
        SeQuery query = null;
        try {
            query = new SeQuery(scon, rasterColumns, new SeSqlConstruct(rasterTable));
            query.prepareQuery();
            query.execute();

            SeRow row = query.fetch();
            while (row != null) {
                rasterAttributes = row.getRaster(0);
                rasterAttList.add(rasterAttributes);
                row = query.fetch();
            }
        } catch (SeException se) {
            throw new ArcSdeException("Error fetching raster attributes for " + rasterTable, se);
        } finally {
            if (query != null) {
                try {
                    query.close();
                } catch (SeException e) {
                    throw new ArcSdeException(e);
                }
            }
        }
        LOGGER.fine("Found " + rasterAttList.size() + " raster attributes for " + rasterTable);
        return rasterAttList;
    }

    private List<RasterBandInfo> setUpBandInfo(ArcSDEPooledConnection scon, String rasterTable,
            SeRasterAttr rasterAttributes) throws IOException {
        final int numBands;
        final SeRasterBand[] seBands;
        final RasterCellType cellType;
        try {
            numBands = rasterAttributes.getNumBands();
            seBands = rasterAttributes.getBands();
            cellType = RasterCellType.valueOf(rasterAttributes.getPixelType());
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }

        List<RasterBandInfo> detachedBandInfo = new ArrayList<RasterBandInfo>(numBands);

        RasterBandInfo bandInfo;
        SeRasterBand band;
        for (int bandN = 0; bandN < numBands; bandN++) {
            band = seBands[bandN];
            bandInfo = new RasterBandInfo();
            setBandInfo(bandInfo, band, scon, cellType.getBitsPerSample());
            detachedBandInfo.add(bandInfo);
        }
        return detachedBandInfo;
    }

    /**
     * 
     * @param bandInfo
     * @param band
     * @param scon
     * @param bitsPerSample
     *            only used if the band is colormapped to create the IndexColorModel
     * @throws IOException
     */
    private void setBandInfo(RasterBandInfo bandInfo, final SeRasterBand band,
            final ArcSDEPooledConnection scon, int bitsPerSample) throws IOException {

        bandInfo.bandId = band.getId().longValue();
        bandInfo.bandNumber = band.getBandNumber();
        bandInfo.bandName = "Band " + bandInfo.bandNumber;

        bandInfo.rasterId = band.getRasterId().longValue();
        bandInfo.rasterColumnId = band.getRasterColumnId().longValue();

        bandInfo.bandHeight = band.getBandHeight();
        bandInfo.bandWidth = band.getBandWidth();
        bandInfo.hasColorMap = band.hasColorMap();
        if (bandInfo.hasColorMap) {
            IndexColorModel colorMap = getBandColorMap(band, scon, bitsPerSample);
            LOGGER.fine("Setting band's color map: " + colorMap);
            bandInfo.colorMap = colorMap;
        } else {
            bandInfo.colorMap = null;
        }
        bandInfo.compressionType = CompressionType.valueOf(band.getCompressionType());
        SeExtent extent = band.getExtent();
        bandInfo.bandExtent = new Envelope(extent.getMinX(), extent.getMaxX(), extent.getMinY(),
                extent.getMaxY());
        bandInfo.cellType = RasterCellType.valueOf(band.getPixelType());
        bandInfo.interleaveType = InterleaveType.valueOf(band.getInterleave());
        bandInfo.interpolationType = InterpolationType.valueOf(band.getInterpolation());
        bandInfo.maxPyramidLevel = band.getMaxLevel();
        bandInfo.isSkipPyramidLevelOne = band.skipLevelOne();
        bandInfo.hasStats = band.hasStats();
        if (bandInfo.hasStats) {
            try {
                bandInfo.statsMin = band.getStatsMin();
                bandInfo.statsMax = band.getStatsMax();
                bandInfo.statsMean = band.getStatsMean();
                bandInfo.statsStdDev = band.getStatsStdDev();
            } catch (SeException e) {
                throw new ArcSdeException(e);
            }
        } else {
            bandInfo.statsMin = java.lang.Double.NaN;
            bandInfo.statsMax = java.lang.Double.NaN;
            bandInfo.statsMean = java.lang.Double.NaN;
            bandInfo.statsStdDev = java.lang.Double.NaN;
        }
        bandInfo.tileWidth = band.getTileWidth();
        bandInfo.tileHeight = band.getTileHeight();
        SDEPoint tOrigin;
        try {
            tOrigin = band.getTileOrigin();
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
        bandInfo.tileOrigin = new Point2D.Double(tOrigin.getX(), tOrigin.getY());
    }

    private IndexColorModel getBandColorMap(SeRasterBand band, ArcSDEPooledConnection scon,
            int bitsPerPixel) throws IOException {
        final DataBuffer colorMapData = getColormapData(band, scon);

        IndexColorModel colorModel;
        colorModel = RasterUtils.sdeColorMapToJavaColorModel(bitsPerPixel, colorMapData);

        return colorModel;
    }

    /**
     * 
     * @param band
     * @param scon
     * @return
     * @throws ArcSdeException
     */
    private DataBuffer getColormapData(SeRasterBand band, ArcSDEPooledConnection scon)
            throws IOException {
        LOGGER.fine("Reading colormap for raster band " + band);

        final SeObjectId rasterColumnId = band.getRasterColumnId();

        final String auxTableName = "SDE_AUX_" + rasterColumnId.longValue();
        LOGGER.fine("Quering auxiliary table " + auxTableName + " for color map data");

        DataBuffer colorMap;
        SeQuery query = null;
        try {
            SeTable table = new SeTable(scon, auxTableName);

            SeSqlConstruct sqlConstruct = new SeSqlConstruct();
            sqlConstruct.setTables(new String[] { auxTableName });
            sqlConstruct.setWhere("TYPE = 3");

            query = new SeQuery(scon, new String[] { "OBJECT" }, sqlConstruct);
            query.prepareQuery();
            query.execute();

            SeRow row = query.fetch();

            ByteArrayInputStream colorMapIS = row.getBlob(0);

            colorMap = readColorMap(colorMapIS);

        } catch (SeException e) {
            throw new ArcSdeException("Error fetching colormap data for band " + band, e);
        } finally {
            if (query != null) {
                try {
                    query.close();
                } catch (SeException e) {
                    LOGGER.log(Level.INFO, "ignoring exception when closing query to "
                            + "fetch colormap data", e);
                }
            }
        }
        return colorMap;
    }

    private DataBuffer readColorMap(final ByteArrayInputStream colorMapIS) throws IOException {
        final int COLOR_MODEL_TYPE_INDEX = 4;// either RGB or RGBA

        final DataInputStream dataIn = new DataInputStream(colorMapIS);
        // discard unneeded data
        for (int i = 0; i < COLOR_MODEL_TYPE_INDEX; i++) {
            dataIn.readByte();
        }

        final int colorSpaceType = dataIn.readInt();
        final int numBanks;
        if (colorSpaceType == SeRaster.SE_COLORMAP_RGB) {
            numBanks = 3;
        } else if (colorSpaceType == SeRaster.SE_COLORMAP_RGBA) {
            numBanks = 4;
        } else {
            throw new IllegalStateException("Got unknown colormap type: " + colorSpaceType);
        }
        LOGGER.info("Colormap has " + numBanks + " color components");

        final int buffType = dataIn.readInt();
        final int numElems = dataIn.readInt();
        LOGGER.fine("ColorMap length: " + numElems);

        final DataBuffer buff;
        if (buffType == SeRaster.SE_COLORMAP_DATA_BYTE) {
            LOGGER.fine("Creating Byte data buffer for " + numBanks + " banks and " + numElems
                    + " elements per bank");
            buff = new DataBufferByte(numElems, numBanks);
            for (int elem = 0; elem < numElems; elem++) {
                for (int bank = 0; bank < numBanks; bank++) {
                    int val = dataIn.readByte();
                    buff.setElem(bank, elem, val);
                }
            }
        } else if (buffType == SeRaster.SE_COLORMAP_DATA_SHORT) {
            LOGGER.fine("Creating Short data buffer for " + numBanks + " banks and " + numElems
                    + " elements per bank");
            buff = new DataBufferShort(numElems, numBanks);
            for (int elem = 0; elem < numElems; elem++) {
                for (int bank = 0; bank < numBanks; bank++) {
                    int val = dataIn.readShort();
                    buff.setElem(bank, elem, val);
                }
            }
        } else {
            throw new IllegalStateException("Unknown databuffer type from colormap header: "
                    + buffType + " expected one of TYPE_BYTE, TYPE_SHORT");
        }

        return buff;
    }
}
