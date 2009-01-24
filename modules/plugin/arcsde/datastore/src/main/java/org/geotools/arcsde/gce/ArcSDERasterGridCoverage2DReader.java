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
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.ImageLayout;

import org.geotools.arcsde.gce.imageio.ArcSDEPyramid;
import org.geotools.arcsde.gce.imageio.ArcSDEPyramidLevel;
import org.geotools.arcsde.gce.imageio.ArcSDERasterImageReadParam;
import org.geotools.arcsde.gce.imageio.ArcSDERasterReader;
import org.geotools.arcsde.gce.imageio.ArcSDERasterReaderSpi;
import org.geotools.arcsde.gce.imageio.ArcSDEPyramid.RasterQueryInfo;
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
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

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
     * Local copy of the javax.imageio.ImageReader subclass for reading from this ArcSDE Raster
     * Source *
     */
    private ArcSDERasterReader imageIOReader;

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

        setupImageIOReader();

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

        GridCoverage2D coverage = createCoverage(readEnvelope, requestedDim, null);

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

        ArcSDEPooledConnection scon = null;

        try {

            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Creating coverage out of request: imagesize -- " + requestedDim
                        + "  envelope -- " + requestedEnvelope);

            ReferencedEnvelope reqEnv = new ReferencedEnvelope(requestedEnvelope.getMinimum(0),
                    requestedEnvelope.getMaximum(0), requestedEnvelope.getMinimum(1),
                    requestedEnvelope.getMaximum(1), requestedEnvelope
                            .getCoordinateReferenceSystem());

            final ArcSDEPyramid pyramidInfo = rasterInfo.getPyramidInfo();

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

                scon = connectionPool.getConnection();

                ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
                rParam.setConnection(scon);

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
                final Point levelZeroPRP = rasterInfo.getLevelZeroPRP();
                if (level == 0 && levelZeroPRP != null) {
                    if ((maxImageY > levelZeroPRP.y && minImageY < levelZeroPRP.y)
                            || (maxImageX > levelZeroPRP.x && minImageX < levelZeroPRP.x)) {
                        LOGGER.warning("Using pyramid level 1 to render this "
                                + "request, as the data is unavailable at "
                                + "a negatively indexed tile.");
                        return createCoverage(requestedEnvelope, requestedDim, Integer.valueOf(1));
                    } else if (maxImageY > levelZeroPRP.y && maxImageX > levelZeroPRP.x) {
                        // we're on the south side of the PRP...need to shift
                        // everything up
                        sourceRegion.translate(levelZeroPRP.x * -1, levelZeroPRP.y * -1);
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
                List<RasterBandInfo> seBands = rasterInfo.getBands();
                int[] bands = new int[Math.min(3, seBands.size())];
                Map<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();
                for (int bandIndex = 0; bandIndex < bands.length; bandIndex++) {
                    bands[bandIndex] = bandIndex + 1;
                    bandMapper.put(new Integer((int) seBands.get(bandIndex).getBandId()),
                            new Integer(bandIndex));
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
            final GridSampleDimension[] gridBands = rasterInfo.getGridSampleDimensions();
            return coverageFactory.create(coverageName, outputImage, new GeneralEnvelope(
                    outputImageEnvelope), gridBands, null, null);

        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
            throw e;
        } finally {
            if (scon != null) {
                scon.close();
            }
        }
    }

    private void setupImageIOReader() throws IOException {
        final ArcSDEPyramid pyramidInfo = rasterInfo.getPyramidInfo();
        final String rasterTable = rasterInfo.getRasterTable();
        final String[] rasterColumns = rasterInfo.getRasterColumns();

        Map<String, Object> readerMap = new HashMap<String, Object>();
        readerMap.put(ArcSDERasterReaderSpi.PYRAMID, pyramidInfo);
        readerMap.put(ArcSDERasterReaderSpi.RASTER_TABLE, rasterTable);
        readerMap.put(ArcSDERasterReaderSpi.RASTER_COLUMN, rasterColumns[0]);

        try {
            ArcSDERasterReaderSpi arcSDERasterReaderSpi = new ArcSDERasterReaderSpi();
            imageIOReader = arcSDERasterReaderSpi.createReaderInstance(readerMap);
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE,
                    "Error creating ImageIOReader in ArcSDERasterGridCoverage2DReader", ioe);
            throw ioe;
        }
    }

    private BufferedImage createInitialBufferedImage(final int width, final int height)
            throws DataSourceException {

        final BufferedImage sampleImage = rasterInfo.getSampleImage();
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
        final int transparentWhite = 0x00ffffff;
        for (int i = 0; i < width * height; i++) {
            pixels[i] = transparentWhite;
        }
        ret.setRGB(0, 0, width, height, pixels, 0, 1);

        return ret;
    }
}
