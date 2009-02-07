/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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

import static org.geotools.arcsde.gce.imageio.RasterCellType.TYPE_1BIT;
import static org.geotools.arcsde.gce.imageio.RasterCellType.TYPE_8BIT_S;
import static org.geotools.arcsde.gce.imageio.RasterCellType.TYPE_8BIT_U;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
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
import org.geotools.arcsde.gce.imageio.ArcSDEPyramid;
import org.geotools.arcsde.gce.imageio.ArcSDEPyramidLevel;
import org.geotools.arcsde.gce.imageio.ArcSDERasterReader;
import org.geotools.arcsde.gce.imageio.ArcSDERasterReaderSpi;
import org.geotools.arcsde.gce.imageio.RasterCellType;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEConnectionPoolFactory;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
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

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeQueryInfo;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterBand;
import com.esri.sde.sdk.client.SeRasterColumn;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeTable;
import com.esri.sde.sdk.client.SeTable.SeTableStats;

/**
 * An implementation of the ArcSDE Raster Format. Based on the ArcGrid module.
 * 
 * @author Saul Farber (saul.farber)
 * @author jeichar
 * @author Simone Giannecchini (simboss)
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java
 *         /org/geotools/arcsde/gce/ArcSDERasterFormat.java $
 */
@SuppressWarnings("deprecation")
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

    /**
     * Creates an instance and sets the metadata.
     */
    public ArcSDERasterFormat() {
        setInfo();
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
                new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D }));
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
            return null;
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
        String rasterTable;
        Point levelZeroPRP = null;
        {
            String sdeUrl = coverageUrl;
            if (sdeUrl.indexOf(";") != -1) {
                final String extraParams = sdeUrl.substring(sdeUrl.indexOf(";") + 1, sdeUrl
                        .length());
                sdeUrl = sdeUrl.substring(0, sdeUrl.indexOf(";"));

                // Right now we only support one kind of extra parameter, so we'll
                // pull it out here.
                if (extraParams.indexOf("LZERO_ORIGIN_TILE=") != -1) {
                    String offsetTile = extraParams.substring(extraParams
                            .indexOf("LZERO_ORIGIN_TILE=") + 18);
                    int xOffsetTile = Integer.parseInt(offsetTile.substring(0, offsetTile
                            .indexOf(",")));
                    int yOffsetTile = Integer.parseInt(offsetTile
                            .substring(offsetTile.indexOf(",") + 1));
                    levelZeroPRP = new Point(xOffsetTile, yOffsetTile);
                }

            }
            rasterTable = sdeUrl.substring(sdeUrl.indexOf("#") + 1);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Building ArcSDEGridCoverageReader2D for " + rasterTable);
            }
        }

        final String[] rasterColumns;
        final List<RasterBandInfo> bands;
        final CoordinateReferenceSystem coverageCrs;
        final GeneralEnvelope originalEnvelope;
        final ArcSDEPyramid pyramidInfo;
        final BufferedImage sampleImage;
        final List<GridSampleDimension> gridSampleDimensions;
        final ArcSDERasterReader imageIOReader;

        rasterColumns = getRasterColumns(scon, rasterTable);
        final SeRasterAttr rasterAttributes = getSeRasterAttr(scon, rasterTable, rasterColumns);
        final RasterCellType cellType;
        try {
            cellType = RasterCellType.valueOf(rasterAttributes.getPixelType());
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
        {
            SeRasterColumn rCol;
            try {
                rCol = new SeRasterColumn(scon, rasterAttributes.getRasterColumnId());
            } catch (SeException e) {
                throw new ArcSdeException(e);
            }
            SeCoordinateReference seCoordRef = rCol.getCoordRef();
            coverageCrs = RasterUtils.findCompatibleCRS(seCoordRef);
        }
        pyramidInfo = new ArcSDEPyramid(rasterAttributes, coverageCrs);

        if (levelZeroPRP != null) {
            int tileWidth = pyramidInfo.getTileWidth();
            int tileHeight = pyramidInfo.getTileHeight();
            levelZeroPRP = new Point(levelZeroPRP.x * tileWidth, levelZeroPRP.y * tileHeight);
        }

        bands = setUpBandInfo(scon, rasterTable, rasterAttributes);

        // sampleImage = RasterUtils.createCompatibleBufferedImage(1, 1, bands.size(), cellType,
        // bands
        // .get(0).getColorMap());

        // gridSampleDimensions = buildGridSampleDimensions(scon, rasterTable, rasterAttributes);

        originalEnvelope = calculateOriginalEnvelope(rasterAttributes, coverageCrs);

        GeneralGridRange originalGridRange = calculateOriginalGridRange(pyramidInfo);

        // imageIOReader = createImageIOReader(rasterTable, rasterColumns, pyramidInfo,
        // sampleImage);

        RasterInfo rasterInfo = new RasterInfo();
        try {
            rasterInfo.setImageWidth(rasterAttributes.getImageWidth());
            rasterInfo.setImageHeight(rasterAttributes.getImageHeight());
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
        rasterInfo.setRasterTable(rasterTable);
        rasterInfo.setRasterColumns(rasterColumns);
        // rasterInfo.setGridSampleDimensions(gridSampleDimensions);
        rasterInfo.setLevelZeroPRP(levelZeroPRP);
        rasterInfo.setBands(bands);
        rasterInfo.setPyramidInfo(pyramidInfo);
        // rasterInfo.setSampleImage(sampleImage);
        rasterInfo.setCoverageCrs(coverageCrs);
        rasterInfo.setOriginalEnvelope(originalEnvelope);
        rasterInfo.setOriginalGridRange(originalGridRange);
        // rasterInfo.setImageIOReader(imageIOReader);

        return rasterInfo;
    }

    private ArcSDERasterReader createImageIOReader(final String rasterTable,
            final String[] rasterColumns, final ArcSDEPyramid pyramidInfo,
            final BufferedImage sampleImage) throws IOException {

        Map<String, Object> readerMap = new HashMap<String, Object>();
        readerMap.put(ArcSDERasterReaderSpi.PYRAMID, pyramidInfo);
        readerMap.put(ArcSDERasterReaderSpi.RASTER_TABLE, rasterTable);
        readerMap.put(ArcSDERasterReaderSpi.RASTER_COLUMN, rasterColumns[0]);
        readerMap.put(ArcSDERasterReaderSpi.SAMPLE_IMAGE, sampleImage);

        ArcSDERasterReader imageIOReader;
        try {
            ArcSDERasterReaderSpi arcSDERasterReaderSpi = new ArcSDERasterReaderSpi();
            imageIOReader = arcSDERasterReaderSpi.createReaderInstance(readerMap);
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE,
                    "Error creating ImageIOReader in ArcSDERasterGridCoverage2DReader", ioe);
            throw ioe;
        }
        return imageIOReader;
    }

    private GeneralGridRange calculateOriginalGridRange(ArcSDEPyramid pyramidInfo) {
        // final int numLevels = pyramidInfo.getNumLevels();
        final ArcSDEPyramidLevel highestRes = pyramidInfo.getPyramidLevel(0);

        final int width = highestRes.size.width;
        final int height = highestRes.size.height;

        Rectangle actualDim = new Rectangle(0, 0, width, height);
        GeneralGridRange originalGridRange = new GeneralGridRange(actualDim);
        return originalGridRange;
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

    private SeRasterAttr getSeRasterAttr(ArcSDEPooledConnection scon, String rasterTable,
            String[] rasterColumns) throws IOException {

        SeRasterAttr rasterAttributes;
        SeQuery query = null;
        try {

            query = new SeQuery(scon, rasterColumns, new SeSqlConstruct(rasterTable));
            query.prepareQuery();
            query.execute();

            SeRow r = query.fetch();
            rasterAttributes = r.getRaster(0);
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
        return rasterAttributes;
    }

    private List<RasterBandInfo> setUpBandInfo(ArcSDEPooledConnection scon, String rasterTable,
            SeRasterAttr rasterAttributes) throws IOException {
        int numBands;
        SeRasterBand[] seBands;
        try {
            numBands = rasterAttributes.getNumBands();
            seBands = rasterAttributes.getBands();
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }

        List<RasterBandInfo> detachedBandInfo = new ArrayList<RasterBandInfo>(numBands);

        RasterBandInfo bandInfo;
        SeRasterBand band;
        for (int bandN = 0; bandN < numBands; bandN++) {
            band = seBands[bandN];
            bandInfo = new RasterBandInfo(band);
            detachedBandInfo.add(bandInfo);
        }
        return detachedBandInfo;
    }

}
