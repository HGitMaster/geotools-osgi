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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEConnectionPoolFactory;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.data.DataSourceException;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SDEPoint;
import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterBand;
import com.esri.sde.sdk.client.SeRasterColumn;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeTable;
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
                /*
                 * We're not using any extra param anymore. Yet, be cautious cause a client may
                 * still be using urls with some old extra param, so jus strip it
                 */
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
            final List<SeRasterAttr> rasterAttributes;
            rasterAttributes = getSeRasterAttr(scon, rasterTable, rasterColumns);

            if (rasterAttributes.size() == 0) {
                throw new IllegalArgumentException("Table " + rasterTable
                        + " contains no raster datasets");
            }

            final CoordinateReferenceSystem coverageCrs;

            /*
             * by bandId map of colormaps. The dataset may be composed of more than one raster so we
             * gather all the colormaps once and held them here by rasterband id
             */
            final Map<Long, IndexColorModel> rastersColorMaps;
            {
                final SeRasterColumn rasterColumn;
                final SeRasterBand sampleBand;
                final long rasterColumnId;
                final int bitsPerSample;
                try {
                    SeRasterAttr ratt = rasterAttributes.get(0);
                    rasterColumn = new SeRasterColumn(scon, ratt.getRasterColumnId());
                    rasterColumnId = rasterColumn.getID().longValue();
                    sampleBand = ratt.getBands()[0];
                    bitsPerSample = RasterCellType.valueOf(ratt.getPixelType()).getBitsPerSample();
                } catch (SeException e) {
                    throw new ArcSdeException(e);
                }
                final SeCoordinateReference seCoordRef = rasterColumn.getCoordRef();
                LOGGER.finer("Looking CRS for raster column " + rasterTable);
                coverageCrs = RasterUtils.findCompatibleCRS(seCoordRef);
                if (DefaultEngineeringCRS.CARTESIAN_2D == coverageCrs) {
                    LOGGER.warning("Raster " + rasterTable
                            + " has not CRS set, using DefaultEngineeringCRS.CARTESIAN_2D");
                }
                if (sampleBand.hasColorMap()) {
                    rastersColorMaps = loadColorMaps(rasterColumnId, bitsPerSample, scon);
                } else {
                    rastersColorMaps = Collections.emptyMap();
                }

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
                    final List<RasterBandInfo> bands;
                    bands = setUpBandInfo(scon, rAtt, rastersColorMaps);
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

        rasterColumns = fetchColumns.toArray(new String[fetchColumns.size()]);
        return rasterColumns;
    }

    private List<SeRasterAttr> getSeRasterAttr(ArcSDEPooledConnection scon, String rasterTable,
            String[] rasterColumns) throws IOException {

        LOGGER.fine("Gathering raster attributes for " + rasterTable);
        SeRasterAttr rasterAttributes;
        LinkedList<SeRasterAttr> rasterAttList = new LinkedList<SeRasterAttr>();
        SeQuery query = null;
        try {
            query = new SeQuery(scon, rasterColumns, new SeSqlConstruct(rasterTable));
            query.prepareQuery();
            query.execute();

            SeRow row = query.fetch();
            while (row != null) {
                rasterAttributes = row.getRaster(0);
                rasterAttList.addFirst(rasterAttributes);
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

    private List<RasterBandInfo> setUpBandInfo(ArcSDEPooledConnection scon,
            SeRasterAttr rasterAttributes, Map<Long, IndexColorModel> rastersColorMaps)
            throws IOException {
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
            final int bitsPerSample = cellType.getBitsPerSample();
            setBandInfo(bandInfo, band, scon, bitsPerSample, rastersColorMaps);
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
            final ArcSDEPooledConnection scon, int bitsPerSample,
            final Map<Long, IndexColorModel> colorMaps) throws IOException {

        bandInfo.bandId = band.getId().longValue();
        bandInfo.bandNumber = band.getBandNumber();
        bandInfo.bandName = "Band " + bandInfo.bandNumber;

        bandInfo.rasterId = band.getRasterId().longValue();
        bandInfo.rasterColumnId = band.getRasterColumnId().longValue();

        bandInfo.bandHeight = band.getBandHeight();
        bandInfo.bandWidth = band.getBandWidth();
        bandInfo.hasColorMap = band.hasColorMap();
        if (bandInfo.hasColorMap) {
            IndexColorModel colorMap = colorMaps.get(Long.valueOf(bandInfo.bandId));
            LOGGER.finest("Setting band's color map: " + colorMap);
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
            // double noDataValue = 0;
            // bandInfo.noDataValue = noDataValue;
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

    /**
     * 
     * @param band
     * @param scon
     * @return
     * @throws ArcSdeException
     */
    private Map<Long, IndexColorModel> loadColorMaps(final long rasterColumnId,
            final int bitsPerSample, ArcSDEPooledConnection scon) throws IOException {
        LOGGER.fine("Reading colormap for raster column " + rasterColumnId);

        final String auxTableName = getAuxTableName(rasterColumnId, scon);
        LOGGER.fine("Quering auxiliary table " + auxTableName + " for color map data");

        Map<Long, IndexColorModel> colorMaps = new HashMap<Long, IndexColorModel>();
        SeQuery query = null;
        try {
            SeSqlConstruct sqlConstruct = new SeSqlConstruct();
            sqlConstruct.setTables(new String[] { auxTableName });
            String whereClause = "TYPE = 3";
            sqlConstruct.setWhere(whereClause);

            query = new SeQuery(scon, new String[] { "RASTERBAND_ID", "OBJECT" }, sqlConstruct);
            query.prepareQuery();
            query.execute();

            long bandId;
            ByteArrayInputStream colorMapIS;
            DataBuffer colorMapData;
            IndexColorModel colorModel;

            SeRow row = query.fetch();
            while (row != null) {
                bandId = ((Number) row.getObject(0)).longValue();
                colorMapIS = row.getBlob(1);

                colorMapData = readColorMap(colorMapIS);
                colorModel = RasterUtils.sdeColorMapToJavaColorModel(colorMapData, bitsPerSample);

                colorMaps.put(Long.valueOf(bandId), colorModel);

                row = query.fetch();
            }
        } catch (SeException e) {
            throw new ArcSdeException("Error fetching colormap data for column " + rasterColumnId
                    + " from table " + auxTableName, e);
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
        LOGGER.fine("Read color map data for " + colorMaps.size() + " rasters");
        return colorMaps;
    }

    private String getAuxTableName(long rasterColumnId, ArcSDEPooledConnection scon)
            throws IOException {

        final String owner;
        SeQuery query = null;
        try {
            final String dbaName = scon.getSdeDbaName();
            String rastersColumnsTable = dbaName + ".SDE_RASTER_COLUMNS";

            SeSqlConstruct sqlCons = new SeSqlConstruct(rastersColumnsTable);
            sqlCons.setWhere("RASTERCOLUMN_ID = " + rasterColumnId);

            try {
                query = new SeQuery(scon, new String[] { "OWNER" }, sqlCons);
                query.prepareQuery();
            } catch (SeException e) {
                // sde 9.3 calls it raster_columns, not sde_raster_columns...
                rastersColumnsTable = dbaName + ".RASTER_COLUMNS";
                sqlCons = new SeSqlConstruct(rastersColumnsTable);
                sqlCons.setWhere("RASTERCOLUMN_ID = " + rasterColumnId);
                query = new SeQuery(scon, new String[] { "OWNER" }, sqlCons);
                query.prepareQuery();
            }
            query.execute();

            SeRow row = query.fetch();
            if (row == null) {
                throw new IllegalArgumentException("No raster column registered with id "
                        + rasterColumnId);
            }
            owner = row.getString(0);
            query.close();
        } catch (SeException e) {
            throw new ArcSdeException("Error getting auxiliary table for raster column "
                    + rasterColumnId, e);
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

        final String auxTableName = owner + ".SDE_AUX_" + rasterColumnId;

        return auxTableName;
    }

    private DataBuffer readColorMap(final ByteArrayInputStream colorMapIS) throws IOException {

        final DataInputStream dataIn = new DataInputStream(colorMapIS);
        // discard unneeded data
        int discardedData = dataIn.readInt();

        final int colorSpaceType = dataIn.readInt();
        final int numBanks;
        if (colorSpaceType == SeRaster.SE_COLORMAP_RGB) {
            numBanks = 3;
        } else if (colorSpaceType == SeRaster.SE_COLORMAP_RGBA) {
            numBanks = 4;
        } else {
            throw new IllegalStateException("Got unknown colormap type: " + colorSpaceType);
        }
        LOGGER.finest("Colormap has " + numBanks + " color components");

        final int buffType = dataIn.readInt();
        final int numElems = dataIn.readInt();
        LOGGER.finest("ColorMap length: " + numElems);

        final DataBuffer buff;
        if (buffType == SeRaster.SE_COLORMAP_DATA_BYTE) {
            LOGGER.finest("Creating Byte data buffer for " + numBanks + " banks and " + numElems
                    + " elements per bank");
            buff = new DataBufferByte(numElems, numBanks);
            for (int elem = 0; elem < numElems; elem++) {
                for (int bank = 0; bank < numBanks; bank++) {
                    int val = dataIn.readUnsignedByte();
                    buff.setElem(bank, elem, val);
                }
            }
        } else if (buffType == SeRaster.SE_COLORMAP_DATA_SHORT) {
            LOGGER.finest("Creating Short data buffer for " + numBanks + " banks and " + numElems
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

        assert dataIn.read() == -1 : "color map data should have been exausted";
        return buff;
    }
}
