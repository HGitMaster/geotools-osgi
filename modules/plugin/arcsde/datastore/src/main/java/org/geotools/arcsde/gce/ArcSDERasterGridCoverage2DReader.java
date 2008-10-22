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
package org.geotools.arcsde.gce;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.ImageLayout;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.gce.band.ArcSDERasterBandCopier;
import org.geotools.arcsde.gce.imageio.ArcSDERasterImageReadParam;
import org.geotools.arcsde.gce.imageio.ArcSDERasterReader;
import org.geotools.arcsde.gce.imageio.ArcSDERasterReaderSpi;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.arcsde.pool.ISession;
import org.geotools.arcsde.pool.SessionPool;
import org.geotools.arcsde.pool.SessionPoolFactory;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.coverage.Category;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.LinearTransform1D;
import org.geotools.util.NumberRange;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.esri.sde.sdk.client.SeColumnDefinition;
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
import com.esri.sde.sdk.pe.PeFactory;
import com.esri.sde.sdk.pe.PeProjectedCS;
import com.esri.sde.sdk.pe.PeProjectionException;

/**
 * This class can read an ArcSDE Raster datasource and create a {@link GridCoverage2D} from the
 * data.
 * 
 * @author Saul Farber (based on ArcGridReader)
 * @since 2.3.x
 */
public final class ArcSDERasterGridCoverage2DReader extends AbstractGridCoverage2DReader implements
        GridCoverageReader {

    private static final boolean DEBUG = false;

    /** Logger. */
    private final static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(ArcSDERasterGridCoverage2DReader.class.getPackage().getName());

    /**
     * The connectionpool we're using to fetch images from this ArcSDE raster layer
     */
    private SessionPool connectionPool = null;

    /** The name of the raster table we're pulling images from in this reader * */
    private String rasterTable = null;

    /**
     * raster column names on this raster. If there's more than one raster column (is this
     * possible?) then we just use the first one.
     */
    private String[] rasterColumns;

    /** An SDE API object which holds lots of metadata about the raster layer * */
    private SeRasterAttr rasterAttributes = null;

    /** The epsg code for the native projection of this raster * */
    private int epsgCode = -1;

    /** Array holding information on each level of the pyramid in this raster. * */
    private ArcSDEPyramid pyramidInfo;

    /**
     * Local copy of the javax.imageio.ImageReader subclass for reading from this ArcSDE Raster
     * Source *
     */
    private ArcSDERasterReader imageIOReader;

    /**
     * hashmap storing ArcSDERasterBand data-typed objects keyed to their SeRasterBand.getId()s *
     */
    private HashMap<Long, ArcSDERasterBandCopier> bandInfo;

    private GridSampleDimension[] gridBands;

    private BufferedImage sampleImage;

    private Point _levelZeroPRP;

    /**
     * Creates a new instance of an ArcSDERasterReader
     * 
     * @param input Source object (probably a connection-type URL) for which we want to build the
     *            ArcSDERasterReader
     * @throws IOException
     */
    public ArcSDERasterGridCoverage2DReader(Object input) throws IOException {
        this(input, null);
    }

    /**
     * Creates a new instance of an ArcSDERasterReader
     * 
     * @param input Source object (probably a connection-type URL) for which we want to build the
     *            ArcSDERasterReader
     * @param hints Hints to be used by this reader throughout his life.
     * @throws IOException
     */
    public ArcSDERasterGridCoverage2DReader(Object input, final Hints hints) throws IOException {

        if (hints != null)
            this.hints.add(hints);

        setupConnectionPool(input);
        calculateCoordinateReferenceSystem();
        pyramidInfo = new ArcSDEPyramid(rasterAttributes, crs);
        if (_levelZeroPRP != null) {
            _levelZeroPRP = new Point(_levelZeroPRP.x * pyramidInfo.tileWidth, _levelZeroPRP.y
                    * pyramidInfo.tileHeight);
        }
        calculateBandDependentInfo();
        setupCoverageMetadata();
        setupImageIOReader();

        this.coverageFactory = CoverageFactoryFinder.getGridCoverageFactory(this.hints);

        LOGGER.info("ArcSDE raster " + coverageName + " based on table " + rasterTable
                + " has been configured.");
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageReader#getFormat()
     */
    public Format getFormat() {
        return new ArcSDERasterFormat();
    }

    /**
     * Reads a {@link GridCoverage2D} possibly matching as close as possible the resolution computed
     * by using the input params provided by using the parameters for this
     * {@link #read(GeneralParameterValue[])}.
     * <p>
     * To have an idea about the possible read parameters take a look at {@link AbstractGridFormat}
     * class and {@link ArcSDERasterFormat} class.
     * 
     * @param params an array of {@link GeneralParameterValue} containing the parameters to control
     *            this read process.
     * @return a {@link GridCoverage2D}.
     * @see AbstractGridFormat
     * @see ArcSDERasterFormat
     * @see org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
     */
    public GridCoverage read(GeneralParameterValue[] params) throws IllegalArgumentException,
            IOException {

        GeneralEnvelope readEnvelope = null;
        Rectangle requestedDim = null;
        if (params != null) {
            final int length = params.length;
            Parameter param;
            String name;
            for (int i = 0; i < length; i++) {
                param = (Parameter) params[i];
                name = param.getDescriptor().getName().getCode();
                if (name.equals(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName().toString())) {
                    final GridGeometry2D gg = (GridGeometry2D) param.getValue();
                    readEnvelope = new GeneralEnvelope((Envelope) gg.getEnvelope2D());
                    requestedDim = gg.getGridRange2D().getBounds();
                } else {
                    LOGGER.warning("discarding parameter with name " + name);
                }

            }
        }
        if (requestedDim == null) {
            throw new IllegalArgumentException("You must call ArcSDERasterReader.read() with a "
                    + "GPV[] including a Parameter for READ_GRIDGEOMETRY2D.");
        }
        if (readEnvelope == null) {
            readEnvelope = new GeneralEnvelope(pyramidInfo.getPyramidLevel(
                    pyramidInfo.getNumLevels() - 1).getEnvelope());
        }
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("ArcSDE raster image requested: [" + readEnvelope + ", " + requestedDim
                    + "]");
        return createCoverage(readEnvelope, requestedDim, null);
    }

    /**
     * This method creates the GridCoverage2D from the underlying SDE Raster Table. Note: Because of
     * pyramiding, this coverage will not usually be exactly the same size/extent as the coverage
     * requested originally by read(). This is because the ArcSDERasterReader will choose to supply
     * you with best data available, and let the eventually renderer (generally the
     * GridCoverageRenderer) downsample/generalize the returned coverage.
     * 
     * @param requestedDim The requested image dimensions in pixels
     * @param readEnvelope The request envelope, in CRS units
     * @param forcedLevel If this parameter is non-null, it contains the level of the pyramid at
     *            which to render this request. Note that this parameter should be used with care,
     *            as forcing a rendering at too low a level could cause significant memory overload!
     * @return a GridCoverage
     * @throws IOException
     * @throws java.io.IOException
     */
    private GridCoverage createCoverage(GeneralEnvelope requestedEnvelope,
            Rectangle requestedDim,
            Integer forcedLevel) throws IOException {

        ISession session = null;
        try {

            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Creating coverage out of request: imagesize -- " + requestedDim
                        + "  envelope -- " + requestedEnvelope);

            ReferencedEnvelope reqEnv = new ReferencedEnvelope(requestedEnvelope.getMinimum(0),
                    requestedEnvelope.getMaximum(0), requestedEnvelope.getMinimum(1),
                    requestedEnvelope.getMaximum(1), requestedEnvelope
                            .getCoordinateReferenceSystem());

            final CoordinateReferenceSystem nativeCRS = pyramidInfo.getPyramidLevel(0)
                    .getEnvelope().getCoordinateReferenceSystem();
            if (!CRS.equalsIgnoreMetadata(nativeCRS, reqEnv.getCoordinateReferenceSystem())) {
                // we're being reprojected. We'll need to reproject reqEnv into
                // our native coordsys
                try {
                    // ReferencedEnvelope origReqEnv = reqEnv;
                    reqEnv = reqEnv.transform(nativeCRS, true);
                } catch (FactoryException fe) {
                    // unable to reproject?
                    throw new DataSourceException("Unable to find a reprojection from requested "
                            + "coordsys to native coordsys for this request", fe);
                } catch (TransformException te) {
                    throw new DataSourceException("Unable to perform reprojection from requested "
                            + "coordsys to native coordsys for this request", te);
                }
            }

            int level = 0;
            if (forcedLevel != null) {
                level = forcedLevel.intValue();
            } else {
                level = pyramidInfo.pickOptimalRasterLevel(reqEnv, requestedDim);
            }

            ArcSDEPyramidLevel optimalLevel = pyramidInfo.getPyramidLevel(level);

            BufferedImage outputImage = null;
            ReferencedEnvelope outputImageEnvelope = null;

            if (!optimalLevel.getEnvelope().intersects((BoundingBox) reqEnv)) {
                // this is a blank raster. I guess we should create a completely
                // blank image with the
                // correct size and return that. Transparency?
                outputImage = createInitialBufferedImage(requestedDim.width, requestedDim.height);
                outputImageEnvelope = new ReferencedEnvelope(reqEnv);

            } else {
                // ok, there's actually something to render. Render it.
                RasterQueryInfo rasterGridInfo = pyramidInfo.fitExtentToRasterPixelGrid(reqEnv,
                        level);

                session = connectionPool.getSession();

                ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
                rParam.setConnection(session);

                outputImage = createInitialBufferedImage(rasterGridInfo.image.width,
                        rasterGridInfo.image.height);
                rParam.setDestination(outputImage);

                final int minImageX = Math.max(rasterGridInfo.image.x, 0);
                final int maxImageX = Math.min(rasterGridInfo.image.x + rasterGridInfo.image.width,
                        pyramidInfo.getPyramidLevel(level).size.width);
                int minImageY = Math.max(rasterGridInfo.image.y, 0);
                int maxImageY = Math.min(rasterGridInfo.image.y + rasterGridInfo.image.height,
                        pyramidInfo.getPyramidLevel(level).size.height);

                Rectangle sourceRegion = new Rectangle(minImageX, minImageY, maxImageX - minImageX,
                        maxImageY - minImageY);
                // check for inaccessible negative-indexed level-zero tiles.
                // Shift to level 1 if necessary.
                if (level == 0 && _levelZeroPRP != null) {
                    if ((maxImageY > _levelZeroPRP.y && minImageY < _levelZeroPRP.y)
                            || (maxImageX > _levelZeroPRP.x && minImageX < _levelZeroPRP.x)) {
                        LOGGER.warning("Using pyramid level 1 to render this "
                                + "request, as the data is unavailable at "
                                + "a negatively indexed tile.");
                        return createCoverage(requestedEnvelope, requestedDim, Integer.valueOf(1));
                    } else if (maxImageY > _levelZeroPRP.y && maxImageX > _levelZeroPRP.x) {
                        // we're on the south side of the PRP...need to shift
                        // everything up
                        sourceRegion.translate(_levelZeroPRP.x * -1, _levelZeroPRP.y * -1);
                    } else {
                        // all the data we want is negatively indexed on one axis or another. Since
                        // we can't get at it, we'll have to shift up to level 1;
                        LOGGER.warning("Using pyramid level 1 to render this "
                                + "request, as the data is unavailable at a negatively "
                                + "indexed tile.");
                        return createCoverage(requestedEnvelope, requestedDim, new Integer(1));
                    }
                }

                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Expanded request to cover source region [" + sourceRegion
                            + "] in level " + level + ".  Spatial extent of this source region is "
                            + rasterGridInfo.envelope);

                rParam.setSourceRegion(sourceRegion);

                if (rasterGridInfo.image.x < 0 || rasterGridInfo.image.y < 0) {
                    Point destOffset = new Point(0, 0);
                    if (rasterGridInfo.image.x < 0)
                        destOffset.x = rasterGridInfo.image.x * -1;
                    if (rasterGridInfo.image.y < 0)
                        destOffset.y = rasterGridInfo.image.y * -1;
                    rParam.setDestination(outputImage.getSubimage(destOffset.x, destOffset.y,
                            outputImage.getWidth() - destOffset.x, outputImage.getHeight()
                                    - destOffset.y));
                    if (LOGGER.isLoggable(Level.FINER))
                        LOGGER.finer("source region is offset by " + destOffset + " into the "
                                + outputImage.getWidth() + "x" + outputImage.getHeight()
                                + " output image.");
                }

                outputImageEnvelope = new ReferencedEnvelope(rasterGridInfo.envelope);

                // not quite sure how, but I figure one could request a subset
                // of all available bands...
                // for now we'll just grab the first three, and assume they're
                // RGB in order.
                SeRasterBand[] seBands = rasterAttributes.getBands();
                int[] bands = new int[Math.min(3, seBands.length)];
                HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();
                for (int i = 0; i < bands.length; i++) {
                    bands[i] = i + 1;
                    bandMapper.put(new Integer((int) seBands[i].getId().longValue()),
                            new Integer(i));
                }
                rParam.setSourceBands(bands);
                rParam.setBandMapper(bandMapper);

                // if we don't provide an ImageLayout to the JAI ImageRead
                // operation, it'll try to read the entire raster layer!
                // It's only a slight abuse of the semantics of the word "tile"
                // when we tell JAI that it can tile our image at exactly the
                // size of the section of the raster layer we're looking to render.
                final ImageLayout layout = new ImageLayout();
                layout.setTileWidth(sourceRegion.width);
                layout.setTileHeight(sourceRegion.height);

                ParameterBlock pb = new ParameterBlock();
                pb.add(new Object());
                pb.add(new Integer(level));
                pb.add(Boolean.FALSE);
                pb.add(Boolean.FALSE);
                pb.add(Boolean.FALSE);
                pb.add(null);
                pb.add(null);
                pb.add(rParam);
                pb.add(imageIOReader);

                // We're not really interested in this renderedRaster, because I
                // can't figure out how to get JAI
                // to render the source area to a translated offset in the
                // destination image. Note that this
                // is really a throwaway operation. The ".getData()" call at the
                // end forces the created RenderedImage
                // to load its internal data, which causes the
                // rParam.getDestination() bufferedImage to get loaded
                // We use that at the end, essentially discarding this
                // RenderedImage (it's not actually what we want,
                // anyway).

                // Doesn't work with 1-bit images?
                // JAI.create("ImageRead", pb, new RenderingHints(JAI.KEY_IMAGE_LAYOUT,
                // layout)).getData();

                // non-jai way, but at least it works!
                imageIOReader.read(level, rParam);

                if (DEBUG) {
                    Graphics2D g = outputImage.createGraphics();
                    g.setColor(Color.orange);
                    g.setFont(new Font("Sans-serif", Font.BOLD, 12));
                    g.drawString(sourceRegion.getMinX() + "," + sourceRegion.getMinY(), 30, 40);
                }

            }

            // Create the coverage
            return coverageFactory.create(coverageName, outputImage, new GeneralEnvelope(
                    outputImageEnvelope), gridBands, null, null);

        } catch (DataSourceException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            throw new DataSourceException(e);
        } catch (SeException se) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, se.getSeError().getErrDesc(), se);
            throw new DataSourceException(se);
        } catch (UnavailableArcSDEConnectionException uce) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, uce.getLocalizedMessage(), uce);
            throw new DataSourceException(uce);
        } finally {
            if (session != null)
                session.dispose();
        }
    }

    /**
     * @param sdeUrl - A StringBuffer containing a string of form
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

    /**
     * Gets the coordinate system that will be associated to the {@link GridCoverage}. The WGS84
     * coordinate system is used by default.
     */
    private void calculateCoordinateReferenceSystem() throws IOException {

        if (rasterAttributes == null) {
            throw new DataSourceException("Raster Attributes are null, can't calculated CRS info.");
        }

        ISession session = null;
        try {
            session = connectionPool.getSession();
            SeRasterColumn rCol = session
                    .createSeRasterColumn(rasterAttributes.getRasterColumnId());

            PeProjectedCS pcs = new PeProjectedCS(rCol.getCoordRef().getProjectionDescription());
            epsgCode = -1;
            int[] projcs = PeFactory.projcsCodelist();
            for (int i = 0; i < projcs.length; i++) {
                try {
                    PeProjectedCS candidate = PeFactory.projcs(projcs[i]);
                    // in ArcSDE 9.2, if the PeFactory doesn't support a
                    // projection it claimed
                    // to support, it returns 'null'. So check for it.
                    if (candidate != null && candidate.getName().trim().equals(pcs.getName()))
                        epsgCode = projcs[i];
                } catch (PeProjectionException pe) {
                    // Strangely SDE includes codes in the projcsCodeList() that
                    // it doesn't actually support.
                    // Catch the exception and skip them here.
                }
            }

            if (epsgCode == -1) {
                LOGGER.warning("Couldn't determine EPSG code for this raster."
                        + "  Using SDE's WKT-like coordSysDescription() instead.");
                crs = CRS.parseWKT(rCol.getCoordRef().getCoordSysDescription());
            } else {
                crs = CRS.decode("EPSG:" + epsgCode);
            }

            SeExtent sdeExtent = rasterAttributes.getExtent();
            originalEnvelope = new GeneralEnvelope(crs);
            originalEnvelope.setRange(0, sdeExtent.getMinX(), sdeExtent.getMaxX());
            originalEnvelope.setRange(1, sdeExtent.getMinY(), sdeExtent.getMaxY());
        } catch (UnavailableArcSDEConnectionException e) {
            LOGGER.log(Level.SEVERE, "", e);
            throw new DataSourceException(e);
        } catch (SeException e) {
            LOGGER.log(Level.SEVERE, "", e);
            throw new ArcSdeException(e);
        } catch (FactoryException e) {
            LOGGER.log(Level.SEVERE, "", e);
            throw new DataSourceException(e);
        } catch (PeProjectionException e) {
            LOGGER.log(Level.SEVERE, "", e);
            throw new DataSourceException(e);
        } finally {
            if (session != null && !session.isClosed())
                session.dispose();
        }
    }

    /**
     * Checks the input prvided to this {@link ArcSDERasterGridCoverage2DReader} and sets all the
     * other objects and flags accordingly.
     * 
     * @param input provied to this {@link ArcSDERasterGridCoverage2DReader}.
     * @throws DataSourceException
     * @throws IOException
     */
    private void setupConnectionPool(Object input) throws IOException {
        if (input == null) {
            final DataSourceException ex = new DataSourceException(
                    "No source set to read this coverage.");
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            throw ex;
        }

        // this will be our connection string
        String sdeUrl = null;

        if (input instanceof String) {
            sdeUrl = (String) input;
            LOGGER.warning("connecting to ArcSDE Raster: " + sdeUrl);
        } else if (input instanceof File) {
            sdeUrl = ((File) input).getPath();
            LOGGER.warning("connectiong via file-hack to ArcSDE Raster: " + sdeUrl);
        } else {
            throw new IllegalArgumentException("Unsupported input type: " + input.getClass());
        }

        ArcSDEConnectionConfig sdeConfig = sdeURLToConnectionConfig(new StringBuffer(sdeUrl));
        if (sdeUrl.indexOf(";") != -1) {
            final String extraParams = sdeUrl.substring(sdeUrl.indexOf(";") + 1, sdeUrl.length());
            sdeUrl = sdeUrl.substring(0, sdeUrl.indexOf(";"));

            // Right now we only support one kind of extra parameter, so we'll
            // pull it out here.
            if (extraParams.indexOf("LZERO_ORIGIN_TILE=") != -1) {
                String offsetTile = extraParams
                        .substring(extraParams.indexOf("LZERO_ORIGIN_TILE=") + 18);
                int xOffsetTile = Integer
                        .parseInt(offsetTile.substring(0, offsetTile.indexOf(",")));
                int yOffsetTile = Integer.parseInt(offsetTile
                        .substring(offsetTile.indexOf(",") + 1));
                _levelZeroPRP = new Point(xOffsetTile, yOffsetTile);
            }

        }
        rasterTable = sdeUrl.substring(sdeUrl.indexOf("#") + 1);

        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("Building ArcSDEGridCoverageReader2D for " + sdeConfig
                    + ", with raster table " + rasterTable);

        connectionPool = SessionPoolFactory.getInstance().createSharedPool(sdeConfig);

        try {
            ISession session = connectionPool.getSession();

            SeTable sTable = session.getTable(rasterTable);
            SeQuery q = null;
            try {
                SeColumnDefinition[] cols = session.describe(sTable);
                ArrayList fetchColumns = new ArrayList();
                for (int i = 0; i < cols.length; i++) {
                    if (cols[i].getType() == SeColumnDefinition.TYPE_RASTER)
                        fetchColumns.add(cols[i].getName());
                }
                if (fetchColumns.size() == 0)
                    throw new DataSourceException(
                            "Couldn't find any TYPE_RASTER columns in ArcSDE table " + rasterTable);

                rasterColumns = (String[]) fetchColumns.toArray(new String[fetchColumns.size()]);
                q = session.createAndExecuteQuery(rasterColumns, new SeSqlConstruct(rasterTable));

                SeRow r = q.fetch();
                rasterAttributes = r.getRaster(0);

                q.close();
            } catch (SeException se) {
                throw new DataSourceException("Error fetching raster connection data from "
                        + rasterTable + ": " + se.getSeError().getErrDesc(), se);
            } finally {
                if (!session.isClosed())
                    session.dispose();
            }

        } catch (UnavailableArcSDEConnectionException uce) {
            throw new DataSourceException("Unable to fetch a connection to ArcSDE server at "
                    + connectionPool.getConfig().getServerName() + ".", uce);
        }

    }

    /**
     * Inspects the band layout of this raster layer to determine whether this reader can actually
     * support this raster layer, what sort of BufferedImage to create when rendering this layer and
     * how to describe each band in rendered layers.
     * 
     * @throws DataSourceException if there's an error communicating with SDE about this raster
     *             layer.
     */
    private void calculateBandDependentInfo() throws DataSourceException {
        try {

            sampleImage = ArcSDERasterReader.createCompatibleBufferedImage(1, 1, rasterAttributes);

            SeRasterBand[] sdeBands = rasterAttributes.getBands();
            bandInfo = new HashMap<Long, ArcSDERasterBandCopier>();
            for (int i = 0; i < sdeBands.length; i++) {
                bandInfo.put(Long.valueOf(sdeBands[i].getId().longValue()), ArcSDERasterBandCopier
                        .getInstance(rasterAttributes.getPixelType(), pyramidInfo.tileHeight,
                                pyramidInfo.tileWidth));
            }

            if (rasterAttributes.getNumBands() == 1) {
                if (rasterAttributes.getPixelType() == SeRaster.SE_PIXEL_TYPE_1BIT) {
                    NumberRange sampleValueRange = new NumberRange(0, 1);
                    Category bitBandCat = new Category(this.coverageName + ": Band One (1-bit)",
                            new Color[] { Color.BLACK, Color.WHITE }, sampleValueRange,
                            LinearTransform1D.IDENTITY);
                    gridBands = new GridSampleDimension[1];
                    gridBands[0] = new GridSampleDimension(bitBandCat.getName(),
                            new Category[] { bitBandCat }, null).geophysics(true);

                } else if (rasterAttributes.getBands()[0].hasColorMap()) {
                    // we support 1-band with colormap now
                    gridBands = new GridSampleDimension[1];
                    Category cmCat = null;// buildCategory(rasterAttributes.getBands()[0].getColorMap());
                    gridBands[0] = new GridSampleDimension(cmCat.getName(),
                            new Category[] { cmCat }, null).geophysics(true);
                } else if (rasterAttributes.getPixelType() == SeRaster.SE_PIXEL_TYPE_8BIT_S
                        || rasterAttributes.getPixelType() == SeRaster.SE_PIXEL_TYPE_8BIT_U) {
                    LOGGER
                            .warning("Discovered 8-bit single-band raster.  Using return image type: TYPE_BYTE_GRAY");
                    NumberRange sampleValueRange = new NumberRange(0, 255);
                    Category greyscaleBandCat = new Category(this.coverageName
                            + ": Band One (grayscale)", new Color[] { Color.BLACK, Color.WHITE },
                            sampleValueRange, LinearTransform1D.IDENTITY);
                    gridBands = new GridSampleDimension[1];
                    gridBands[0] = new GridSampleDimension(greyscaleBandCat.getName(),
                            new Category[] { greyscaleBandCat }, null).geophysics(true);
                } else {
                    throw new IllegalArgumentException(
                            "One-band, non-colormapped raster layers with type "
                                    + rasterAttributes.getPixelType() + " are not supported.");
                }

            } else if (rasterAttributes.getNumBands() == 3 || rasterAttributes.getNumBands() == 4) {
                if (rasterAttributes.getPixelType() != SeRaster.SE_PIXEL_TYPE_8BIT_U) {
                    throw new IllegalArgumentException(
                            "3 or 4 band rasters are only supported if they have pixel type 8-bit unsigned pixels.");
                }
                NumberRange sampleValueRange = new NumberRange(0, 255);
                Category nan = new Category("no-data", new Color[] { new Color(0x00000000) },
                        new NumberRange(0, 0), LinearTransform1D.IDENTITY);
                Category white = new Category("valid-data", new Color[] { new Color(0xff000000) },
                        new NumberRange(255, 255), LinearTransform1D.IDENTITY);
                Category redBandCat = new Category("red", new Color[] { Color.BLACK, Color.RED },
                        sampleValueRange, LinearTransform1D.IDENTITY);
                Category blueBandCat = new Category("blue",
                        new Color[] { Color.BLACK, Color.BLUE }, sampleValueRange,
                        LinearTransform1D.IDENTITY);
                Category greenBandCat = new Category("green", new Color[] { Color.BLACK,
                        Color.GREEN }, sampleValueRange, LinearTransform1D.IDENTITY);

                gridBands = new GridSampleDimension[4];
                gridBands[0] = new GridSampleDimension("Red band", new Category[] { redBandCat },
                        null);
                gridBands[1] = new GridSampleDimension("Green band",
                        new Category[] { blueBandCat }, null);
                gridBands[2] = new GridSampleDimension("Blue band",
                        new Category[] { greenBandCat }, null);
                gridBands[3] = new GridSampleDimension("NODATA Mask Band", new Category[] { nan,
                        white }, null);

            }
        } catch (SeException se) {
            LOGGER.log(Level.SEVERE, se.getSeError().getErrDesc(), se);
            throw new DataSourceException(se);
        }
    }

    private void setupCoverageMetadata() {

        ArcSDEPyramidLevel highestRes = pyramidInfo.getPyramidLevel(pyramidInfo.getNumLevels() - 1);
        Rectangle actualDim = new Rectangle(0, 0, highestRes.getNumTilesWide()
                * pyramidInfo.tileWidth, highestRes.getNumTilesHigh() * pyramidInfo.tileHeight);
        originalGridRange = new GeneralGridRange(actualDim);

        coverageName = rasterTable;
    }

    private void setupImageIOReader() throws DataSourceException {
        HashMap readerMap = new HashMap();
        readerMap.put(ArcSDERasterReaderSpi.PYRAMID, pyramidInfo);
        readerMap.put(ArcSDERasterReaderSpi.RASTER_TABLE, rasterTable);
        readerMap.put(ArcSDERasterReaderSpi.RASTER_COLUMN, rasterColumns[0]);

        try {
            imageIOReader = (ArcSDERasterReader) new ArcSDERasterReaderSpi()
                    .createReaderInstance(readerMap);
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE,
                    "Error creating ImageIOReader in ArcSDERasterGridCoverage2DReader", ioe);
            throw new DataSourceException(ioe);
        }
    }

    private BufferedImage createInitialBufferedImage(final int width, final int height)
            throws DataSourceException {

        final WritableRaster newras = sampleImage.getRaster().createCompatibleWritableRaster(width,
                height);
        final BufferedImage ret = new BufferedImage(sampleImage.getColorModel(), newras,
                sampleImage.isAlphaPremultiplied(), null);
        // By default BufferedImages are created with all banks set to zero.
        // That's an all-black, transparent image.
        // Transparency is handled in the ArcSDERasterBandCopier. Blackness
        // isn't. Let's fix that and set
        // the image to white.
        int[] pixels = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            pixels[i] = 0x00ffffff;
        }
        ret.setRGB(0, 0, width, height, pixels, 0, 1);

        return ret;
    }
}
