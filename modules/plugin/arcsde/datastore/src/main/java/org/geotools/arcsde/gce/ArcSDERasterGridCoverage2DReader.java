/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.gce.imageio.ArcSDEPyramid;
import org.geotools.arcsde.gce.imageio.ArcSDEPyramidLevel;
import org.geotools.arcsde.gce.imageio.ArcSDERasterImageReadParam;
import org.geotools.arcsde.gce.imageio.ArcSDERasterReader;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultServiceInfo;
import org.geotools.data.ServiceInfo;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;

/**
 * This class can read an ArcSDE Raster datasource and create a {@link GridCoverage2D} from the
 * data.
 * 
 * @author Saul Farber (based on ArcGridReader)
 * @since 2.3.x
 */
@SuppressWarnings("deprecation")
final class ArcSDERasterGridCoverage2DReader extends AbstractGridCoverage2DReader implements
        GridCoverageReader {

    private static final boolean DEBUG = false;

    /** Logger. */
    private final static Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    /**
     * Holds all the needed coverage metadata
     */
    private final RasterInfo rasterInfo;

    private DefaultServiceInfo serviceInfo;

    /**
     * The connectionpool we're using to fetch images from this ArcSDE raster layer
     */
    private ArcSDEConnectionPool connectionPool = null;

    /**
     * Creates a new instance of an ArcSDERasterReader
     * 
     * @param input
     *            Source object (probably a connection-type URL) for which we want to build the
     *            ArcSDERasterReader
     * @param hints
     *            Hints to be used by this reader throughout his life.
     * @throws IOException
     */
    public ArcSDERasterGridCoverage2DReader(final ArcSDEConnectionPool connectionPool,
            final RasterInfo rasterInfo, final Hints hints) throws IOException {
        if (hints != null) {
            this.hints.add(hints);
        }

        this.connectionPool = connectionPool;
        this.rasterInfo = rasterInfo;
        this.coverageFactory = CoverageFactoryFinder.getGridCoverageFactory(this.hints);

        super.crs = rasterInfo.getCoverageCrs();
        super.originalEnvelope = rasterInfo.getOriginalEnvelope();
        super.originalGridRange = rasterInfo.getOriginalGridRange();
        super.coverageName = rasterInfo.getRasterTable();

        LOGGER.info("ArcSDE raster has been configured: " + rasterInfo);
    }

    /**
     * @see GridCoverageReader#getFormat()
     */
    public Format getFormat() {
        return new ArcSDERasterFormat();
    }

    @Override
    public ServiceInfo getInfo() {
        if (serviceInfo == null) {
            serviceInfo = new DefaultServiceInfo();
            serviceInfo.setTitle(rasterInfo.getRasterTable() + " is an ArcSDE Raster");
            serviceInfo.setDescription(rasterInfo.toString());
            Set<String> keywords = new HashSet<String>();
            keywords.add("ArcSDE");
            serviceInfo.setKeywords(keywords);
        }
        return serviceInfo;
    }

    /**
     * Reads a {@link GridCoverage2D} possibly matching as close as possible the resolution computed
     * by using the input params provided by using the parameters for this
     * {@link #read(GeneralParameterValue[])}.
     * <p>
     * To have an idea about the possible read parameters take a look at {@link AbstractGridFormat}
     * class and {@link ArcSDERasterFormat} class.
     * 
     * @param params
     *            an array of {@link GeneralParameterValue} containing the parameters to control
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
            final ArcSDEPyramid pyramidInfo = rasterInfo.getPyramidInfo();
            readEnvelope = new GeneralEnvelope(pyramidInfo.getPyramidLevel(
                    pyramidInfo.getNumLevels() - 1).getEnvelope());
        }
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("ArcSDE raster image requested: [" + readEnvelope + ", " + requestedDim
                    + "]");

        GridCoverage2D coverage;
        try {
            coverage = createCoverage(readEnvelope, requestedDim, null);
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
            throw e;
        }

        return coverage;
    }

    /**
     * This method creates the GridCoverage2D from the underlying SDE Raster Table. Note: Because of
     * pyramiding, this coverage will not usually be exactly the same size/extent as the coverage
     * requested originally by read(). This is because the ArcSDERasterReader will choose to supply
     * you with best data available, and let the eventually renderer (generally the
     * GridCoverageRenderer) downsample/generalize the returned coverage.
     * 
     * @param requestedDim
     *            The requested image dimensions in pixels
     * @param readEnvelope
     *            The request envelope, in CRS units
     * @param forcedLevel
     *            If this parameter is non-null, it contains the level of the pyramid at which to
     *            render this request. Note that this parameter should be used with care, as forcing
     *            a rendering at too low a level could cause significant memory overload!
     * @return a GridCoverage
     * @throws IOException
     * @throws java.io.IOException
     */
    private GridCoverage2D createCoverage(GeneralEnvelope requestedEnvelope,
            Rectangle requestedDim, Integer forcedLevel) throws IOException {

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Creating coverage out of request: imagesize -- " + requestedDim
                    + "  envelope -- " + requestedEnvelope);
        }

        final ArcSDEPyramid pyramidInfo = rasterInfo.getPyramidInfo();
        final BufferedImage sampleImage = rasterInfo.getSampleImage();

        final ReferencedEnvelope reqEnv;
        reqEnv = RasterUtils.toNativeCrs(requestedEnvelope, rasterInfo.getCoverageCrs());

        final ArcSDEPyramidLevel optimalLevel;
        optimalLevel = getOptimalPyramidLevel(requestedDim, forcedLevel, reqEnv, pyramidInfo);

        final BufferedImage outputImage;
        final GeneralEnvelope outputImageEnvelope;

        if (!optimalLevel.getEnvelope().intersects((BoundingBox) reqEnv)) {
            // this is a blank raster. I guess we should create a completely
            // blank image with the
            // correct size and return that. Transparency?
            int width = requestedDim.width;
            int height = requestedDim.height;
            outputImage = RasterUtils.createInitialBufferedImage(sampleImage, width, height);
            outputImageEnvelope = new GeneralEnvelope(reqEnv);
        } else {
            final int pyramidLevel = optimalLevel.getLevel();
            final List<RasterBandInfo> bands = rasterInfo.getBands();
            final Point levelZeroPRP = rasterInfo.getLevelZeroPRP();
            final ArcSDERasterReader imageIOReader = rasterInfo.getImageIOReader();
            final ArcSDEPooledConnection scon = connectionPool.getConnection();

            final ArcSDERasterImageReadParam rParam;
            try {
                rParam = RasterUtils.createImageReadParam(pyramidLevel, pyramidInfo, reqEnv, scon, bands,
                        levelZeroPRP, imageIOReader, sampleImage);
            } catch (NegativelyIndexedTileException e) {
                scon.close();
                LOGGER.warning("Using pyramid level 1 to render this "
                        + "request, as the data is unavailable at " + "a negatively indexed tile.");
                return createCoverage(requestedEnvelope, requestedDim, Integer.valueOf(1));
            }

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
            try {
                imageIOReader.read(pyramidLevel, rParam);
                outputImage = rParam.getActualDestination();
                outputImageEnvelope = rParam.getOutputImageEnvelope();
            } finally {
                scon.close();
            }
            if (DEBUG) {
                Graphics2D g = outputImage.createGraphics();
                g.setColor(Color.orange);
                g.setFont(new Font("Sans-serif", Font.BOLD, 12));
                Rectangle sourceRegion = rParam.getSourceRegion();
                g.drawString(sourceRegion.getMinX() + "," + sourceRegion.getMinY(), 30, 40);
            }

        }

        // Create the coverage
        final GridSampleDimension[] gridBands = rasterInfo.getGridSampleDimensions();
        return coverageFactory.create(coverageName, outputImage, outputImageEnvelope, gridBands,
                null, null);

    }

    private ArcSDEPyramidLevel getOptimalPyramidLevel(Rectangle requestedDim, Integer forcedLevel,
            final ReferencedEnvelope reqEnv, final ArcSDEPyramid pyramidInfo)
            throws DataSourceException {
        ArcSDEPyramidLevel optimalLevel;
        {
            int level = 0;
            if (forcedLevel != null) {
                level = forcedLevel.intValue();
            } else {
                level = pyramidInfo.pickOptimalRasterLevel(reqEnv, requestedDim);
            }

            optimalLevel = pyramidInfo.getPyramidLevel(level);
        }
        return optimalLevel;
    }
}
