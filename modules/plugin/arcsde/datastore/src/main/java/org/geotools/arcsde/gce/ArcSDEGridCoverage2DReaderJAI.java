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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.MosaicDescriptor;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.gce.RasterUtils.QueryInfo;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DefaultServiceInfo;
import org.geotools.data.ServiceInfo;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterConstraint;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.sun.media.imageio.stream.RawImageInputStream;
import com.sun.media.imageioimpl.plugins.raw.RawImageReaderSpi;

/**
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.5.4
 * @version $Id: ArcSDEGridCoverage2DReaderJAI.java 32498 2009-02-17 17:20:15Z groldan $
 * @source $URL$
 */
@SuppressWarnings( { "deprecation", "nls" })
class ArcSDEGridCoverage2DReaderJAI extends AbstractGridCoverage2DReader {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    private final ArcSDERasterFormat parent;

    private final ArcSDEConnectionPool connectionPool;

    private final RasterInfo rasterInfo;

    private DefaultServiceInfo serviceInfo;

    private LoggingHelper geomLog = new LoggingHelper();

    public ArcSDEGridCoverage2DReaderJAI(final ArcSDERasterFormat parent,
            final ArcSDEConnectionPool connectionPool, final RasterInfo rasterInfo,
            final Hints hints) throws IOException {
        // check it's a supported format
        {
            final int bitsPerSample = rasterInfo.getBand(0, 0).getCellType().getBitsPerSample();
            if (rasterInfo.getNumBands() > 1 && (bitsPerSample == 1 || bitsPerSample == 4)) {
                throw new IllegalArgumentException(bitsPerSample
                        + "-bit rasters with more than one band are not supported");
            }
        }
        this.parent = parent;
        this.connectionPool = connectionPool;
        this.rasterInfo = rasterInfo;

        super.hints = hints;
        super.coverageFactory = CoverageFactoryFinder.getGridCoverageFactory(this.hints);
        super.crs = rasterInfo.getCoverageCrs();
        super.originalEnvelope = rasterInfo.getOriginalEnvelope();

        GeneralGridEnvelope gridRange = rasterInfo.getOriginalGridRange();
        super.originalGridRange = new GeneralGridRange(gridRange.toRectangle());

        super.coverageName = rasterInfo.getRasterTable();
        final int numLevels = rasterInfo.getNumPyramidLevels(0);

        // level 0 is not an overview, but the raster itself
        super.numOverviews = numLevels - 1;

        // ///
        // 
        // setting the higher resolution avalaible for this coverage
        //
        // ///
        highestRes = super.getResolution(originalEnvelope, originalGridRange.toRectangle(), crs);
        // //
        //
        // get information for the successive images
        //
        // //
        // REVISIT may the different rasters in the raster dataset have different pyramid levels? I
        // guess so
        if (numOverviews > 0) {
            overViewResolutions = new double[numOverviews][2];
            for (int pyramidLevel = 1; pyramidLevel <= numOverviews; pyramidLevel++) {
                Rectangle levelGridRange = rasterInfo.getGridRange(0, pyramidLevel);
                GeneralEnvelope levelEnvelope = rasterInfo.getGridEnvelope(0, pyramidLevel);
                overViewResolutions[pyramidLevel - 1] = super.getResolution(levelEnvelope,
                        levelGridRange, crs);
            }
        } else {
            overViewResolutions = null;
        }
    }

    /**
     * @see GridCoverageReader#getFormat()
     */
    public Format getFormat() {
        return parent;
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
     * @see GridCoverageReader#read(GeneralParameterValue[])
     */
    public GridCoverage read(GeneralParameterValue[] params) throws IOException {

        final GeneralEnvelope requestedEnvelope;
        final Rectangle requestedDim;
        final OverviewPolicy overviewPolicy;
        {
            final ReadParameters opParams = RasterUtils.parseReadParams(getOriginalEnvelope(),
                    params);
            overviewPolicy = opParams.overviewPolicy;
            requestedEnvelope = opParams.requestedEnvelope;
            requestedDim = opParams.dim;
        }

        /*
         * For each raster in the raster dataset, obtain the tiles, pixel range, and resulting
         * envelope
         */
        final List<QueryInfo> queries;
        queries = findMatchingRasters(requestedEnvelope, requestedDim, overviewPolicy);
        if (queries.isEmpty()) {
            /*
             * none of the rasters match the requested envelope. This may happen by the tiled nature
             * of the raster dataset
             */
            return createFakeCoverage(requestedEnvelope, requestedDim);
        }

        final GeneralEnvelope resultEnvelope = getResultEnvelope(queries);

        geomLog.appendLoggingGeometries(LoggingHelper.REQ_ENV, requestedEnvelope);
        geomLog.appendLoggingGeometries(LoggingHelper.RES_ENV, resultEnvelope);

        /*
         * Once we collected the matching rasters and their image subsets, find out where in the
         * overall resulting mosaic they fit. If the rasters does not share the spatial resolution,
         * the QueryInfo.resultDimension and QueryInfo.mosaicLocation width or height won't match
         */
        RasterUtils.setMosaicLocations(rasterInfo, resultEnvelope, queries);

        /*
         * Gather the rendered images for each of the rasters that match the requested envelope
         */
        final ArcSDEPooledConnection conn = connectionPool.getConnection();
        final SeQuery preparedQuery = createSeQuery(conn);
        try {
            preparedQuery.execute();

            final Map<Long, QueryInfo> byRasterdIdQueries = new HashMap<Long, QueryInfo>();
            for (QueryInfo q : queries) {
                byRasterdIdQueries.put(q.getRasterId(), q);
            }

            SeRow row = preparedQuery.fetch();

            while (row != null) {
                final SeRasterAttr rAttr = row.getRaster(0);
                final Long rasterId = Long.valueOf(rAttr.getRasterId().longValue());
                final RenderedImage rasterImage;

                if (byRasterdIdQueries.containsKey(rasterId)) {

                    final QueryInfo rasterQueryInfo = byRasterdIdQueries.get(rasterId);

                    LOGGER.finer(rasterQueryInfo.toString());
                    geomLog.appendLoggingGeometries(LoggingHelper.MOSAIC_EXPECTED, rasterQueryInfo
                            .getMosaicLocation());
                    geomLog.appendLoggingGeometries(LoggingHelper.MOSAIC_ENV, rasterQueryInfo
                            .getResultEnvelope());

                    final int pyramidLevelChoice = rasterQueryInfo.getPyramidLevel();

                    rasterImage = getRasterMatchingTileRange(pyramidLevelChoice, preparedQuery,
                            row, rAttr, rasterQueryInfo);

                    {
                        final Rectangle tiledImageSize = rasterQueryInfo.getTiledImageSize();
                        int width = rasterImage.getWidth();
                        int height = rasterImage.getHeight();
                        if (tiledImageSize.width != width || tiledImageSize.height != height) {
                            throw new IllegalStateException(
                                    "Read image is not of the expected size. Image=" + width + "x"
                                            + height + ", expected: " + tiledImageSize.width + "x"
                                            + tiledImageSize.height);
                        }
                        if (tiledImageSize.x != rasterImage.getMinX()
                                || tiledImageSize.y != rasterImage.getMinY()) {
                            throw new IllegalStateException(
                                    "Read image is not at the expected location "
                                            + tiledImageSize.x + "," + tiledImageSize.y + ": "
                                            + rasterImage.getMinX() + "," + rasterImage.getMinY());
                        }
                    }

                    rasterQueryInfo.setResultImage(rasterImage);
                }
                // advance to the next raster in the dataset
                row = preparedQuery.fetch();
            }
        } catch (SeException e) {
            throw new ArcSdeException(e);
        } finally {
            try {
                preparedQuery.close();
            } catch (SeException e) {
                throw new ArcSdeException(e);
            } finally {
                conn.close();
            }
        }

        /*
         * BUILDING COVERAGE
         */
        final GridSampleDimension[] bands = rasterInfo.getGridSampleDimensions();

        final RenderedImage coverageRaster = createMosaic(queries);

        geomLog.log(LoggingHelper.REQ_ENV);
        geomLog.log(LoggingHelper.RES_ENV);
        geomLog.log(LoggingHelper.MOSAIC_ENV);
        geomLog.log(LoggingHelper.MOSAIC_EXPECTED);
        geomLog.log(LoggingHelper.MOSAIC_RESULT);

        return coverageFactory.create(coverageName, coverageRaster, resultEnvelope, bands, null,
                null);
    }

    /**
     * Called when the requested envelope do overlap the coverage envelope but none of the rasters
     * in the dataset do
     * 
     * @param requestedEnvelope
     * @param requestedDim
     * @return
     */
    private GridCoverage createFakeCoverage(GeneralEnvelope requestedEnvelope,
            Rectangle requestedDim) {

        ImageTypeSpecifier its = rasterInfo.getRenderedImageSpec();
        SampleModel sampleModel = its.getSampleModel(requestedDim.width, requestedDim.height);
        ColorModel colorModel = its.getColorModel();

        WritableRaster raster = Raster.createWritableRaster(sampleModel, null);
        BufferedImage image = new BufferedImage(colorModel, raster, false, null);
        return coverageFactory.create(coverageName, image, requestedEnvelope);
    }

    private List<QueryInfo> findMatchingRasters(final GeneralEnvelope requestedEnvelope,
            final Rectangle requestedDim, final OverviewPolicy overviewPolicy) {

        final List<QueryInfo> matchingQueries;
        matchingQueries = RasterUtils.findMatchingRasters(rasterInfo, requestedEnvelope,
                requestedDim, overviewPolicy);

        if (matchingQueries.isEmpty()) {
            return matchingQueries;
        }

        for (QueryInfo match : matchingQueries) {
            RasterUtils.fitRequestToRaster(requestedEnvelope, rasterInfo, match);
        }
        return matchingQueries;
    }

    private GeneralEnvelope getResultEnvelope(final List<QueryInfo> queryInfos) {

        GeneralEnvelope finalEnvelope = null;

        for (QueryInfo rasterQueryInfo : queryInfos) {
            // gather resulting envelope
            if (finalEnvelope == null) {
                finalEnvelope = new GeneralEnvelope(rasterQueryInfo.getResultEnvelope());
            } else {
                finalEnvelope.add(rasterQueryInfo.getResultEnvelope());
            }
        }
        if (finalEnvelope == null) {
            throw new IllegalStateException("Restult envelope is null, this shouldn't happen!! "
                    + "we checked the request overlaps the coverage envelope before!");
        }
        return finalEnvelope;
    }

    /**
     * For each raster: crop->scale->translate->add to mosaic
     * 
     * @param queries
     * @return
     */
    private RenderedImage createMosaic(final List<QueryInfo> queries) {
        // if (queries.size() == 1) {
        // // no need to mosaic at all
        // return queries.get(0).getResultImage();
        // }

        List<RenderedImage> transformed = new ArrayList<RenderedImage>(queries.size());

        for (QueryInfo query : queries) {
            RenderedImage image = query.getResultImage();
            image = cropToRequiredDimension(image, query.getResultDimensionInsideTiledImage());

            final Rectangle mosaicLocation = query.getMosaicLocation();
            // scale
            Float scaleX = Float.valueOf((float) (mosaicLocation.getWidth() / image.getWidth()));
            Float scaleY = Float.valueOf((float) (mosaicLocation.getHeight() / image.getHeight()));
            Float translateX = Float.valueOf(-image.getMinX());
            Float translateY = Float.valueOf(-image.getMinY());

            ParameterBlock pb = new ParameterBlock();
            pb.addSource(image);
            pb.add(scaleX);
            pb.add(scaleY);
            pb.add(translateX);
            pb.add(translateY);
            pb.add(new InterpolationNearest());

            image = JAI.create("scale", pb);

            int minx = image.getMinX();
            int miny = image.getMinY();
            int width = image.getWidth();
            int height = image.getHeight();

            assert mosaicLocation.width == width;
            assert mosaicLocation.height == height;

            // translate
            pb = new ParameterBlock();
            pb.addSource(image);
            pb.add(Float.valueOf(mosaicLocation.x - minx));
            pb.add(Float.valueOf(mosaicLocation.y - miny));
            pb.add(new InterpolationNearest());

            image = JAI.create("translate", pb);

            assert image.getMinX() == mosaicLocation.x : image.getMinX() + " != "
                    + mosaicLocation.x;
            assert image.getMinY() == mosaicLocation.y : image.getMinY() + " != "
                    + mosaicLocation.y;
            assert image.getWidth() == mosaicLocation.width : image.getWidth() + " != "
                    + mosaicLocation.width;
            assert image.getHeight() == mosaicLocation.height : image.getHeight() + " != "
                    + mosaicLocation.height;

            transformed.add(image);
        }

        ParameterBlock mosaicParams = new ParameterBlock();

        for (RenderedImage img : transformed) {
            mosaicParams.addSource(img);
            geomLog.appendLoggingGeometries(LoggingHelper.MOSAIC_RESULT, img);
        }

        mosaicParams.add(MosaicDescriptor.MOSAIC_TYPE_BLEND); // mosaic type
        mosaicParams.add(null); // alpha mask
        mosaicParams.add(null); // source ROI mask
        mosaicParams.add(null); // source threshold
        mosaicParams.add(null); // destination background value

        RenderedImage mosaic = JAI.create("Mosaic", mosaicParams, null);
        //
        // {
        // BufferedImage img = new BufferedImage(mosaic.getColorModel(), Raster
        // .createWritableRaster(mosaic.getSampleModel().createCompatibleSampleModel(
        // mosaic.getWidth(), mosaic.getHeight()), null), false, null);
        // Graphics2D graphics = img.createGraphics();
        // graphics.drawRenderedImage(mosaic, at);
        // graphics.setColor(Color.CYAN);
        // graphics.setStroke(new BasicStroke(2));
        // for (QueryInfo query : queries) {
        // Rectangle m = query.getMosaicLocation();
        // graphics
        // .drawRect(m.x - mosaic.getMinX(), m.y - mosaic.getMinY(), m.width, m.height);
        // }
        // mosaic = img;
        // }

        return mosaic;
    }

    /**
     * Creates the mosaiced image that results from querying all the rasters in the raster dataset
     * for the requested envelope
     * 
     * @param mosaicDimension
     *            the dimensions of the resulting image
     * @param queries
     *            the images to mosaic together
     * @return the mosaiced image
     */
    private RenderedImage _createMosaic(final Rectangle mosaicDimension,
            final List<QueryInfo> queries) {
        if (queries.size() == 1) {
            // no need to mosaic at all
            return queries.get(0).getResultImage();
        }

        List<RenderedImage> translated = new ArrayList<RenderedImage>(queries.size());

        for (QueryInfo query : queries) {
            RenderedImage image = query.getResultImage();
            Rectangle mosaicLocation = query.getMosaicLocation();
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(image);
            pb.add(Float.valueOf((float) mosaicLocation.getX()));
            pb.add(Float.valueOf((float) mosaicLocation.getY()));
            pb.add(new InterpolationNearest());
            RenderedImage trans = JAI.create("translate", pb, null);

            translated.add(trans);
        }

        ParameterBlock mosaicParams = new ParameterBlock();
        for (RenderedImage img : translated) {
            System.err.println("--> adding to mosaic: " + img.getMinX() + "," + img.getMinY() + ","
                    + img.getWidth() + "," + img.getHeight());
            mosaicParams.addSource(img);
        }
        mosaicParams.add(MosaicDescriptor.MOSAIC_TYPE_OVERLAY); // mosaic type
        mosaicParams.add(null); // alpha mask
        mosaicParams.add(null); // source ROI mask
        mosaicParams.add(null); // source threshold
        mosaicParams.add(null); // destination background value

        RenderedImage mosaic = JAI.create("Mosaic", mosaicParams, null);

        {
            BufferedImage img = new BufferedImage(mosaic.getColorModel(), Raster
                    .createWritableRaster(mosaic.getSampleModel().createCompatibleSampleModel(
                            mosaic.getWidth(), mosaic.getHeight()), null), false, null);
            Graphics2D graphics = img.createGraphics();
            AffineTransform at = new AffineTransform();
            at.translate(-(int) mosaic.getMinX(), -(int) mosaic.getMinY());
            graphics.drawRenderedImage(mosaic, at);
            graphics.setColor(Color.CYAN);
            graphics.setStroke(new BasicStroke(2));
            for (QueryInfo query : queries) {
                Rectangle m = query.getMosaicLocation();
                graphics
                        .drawRect(m.x - mosaic.getMinX(), m.y - mosaic.getMinY(), m.width, m.height);
            }
            mosaic = img;
        }

        return mosaic;
    }

    private RenderedImage getRasterMatchingTileRange(int pyramidLevelChoice,
            final SeQuery preparedQuery, SeRow row, final SeRasterAttr rAttr,
            final QueryInfo rasterQueryInfo) throws IOException {

        /*
         * Create the prepared query (not executed) stream to fetch the tiles from
         */
        final Rectangle matchingTiles = rasterQueryInfo.getMatchingTiles();

        // covers an area of full tiles
        final RenderedImage fullTilesRaster;

        /*
         * Create the tiled raster covering the full area of the matching tiles
         */
        fullTilesRaster = createTiledRaster(preparedQuery, row, rAttr, matchingTiles,
                pyramidLevelChoice, rasterQueryInfo.getTiledImageSize().getLocation());

        /*
         * REVISIT: This is odd, we need to force the data to be loaded so we're free to release the
         * stream, which gives away the streamed, tiled nature of this rasters, but I don't see the
         * GCE api having a very clear usage workflow that ensures close() is always being called to
         * the underlying ImageInputStream so we could let it close the SeQuery when done.
         */
        try {
            fullTilesRaster.getData();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fetching data for " + rasterQueryInfo.toString(), e);
        }

        return fullTilesRaster;
    }

    private RenderedImage cropToRequiredDimension(final RenderedImage fullTilesRaster,
            final Rectangle cropTo) {

        int minX = fullTilesRaster.getMinX();
        int minY = fullTilesRaster.getMinY();
        int width = fullTilesRaster.getWidth();
        int height = fullTilesRaster.getHeight();

        Rectangle origDim = new Rectangle(minX, minY, width, height);
        if (!origDim.contains(cropTo)) {
            throw new IllegalArgumentException("Original image (" + origDim
                    + ") does not contain desired dimension (" + cropTo + ")");
        } else if (origDim.equals(cropTo)) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("No need to crop image, full tiled dimension and target one "
                        + "do match: original: " + width + "x" + height + ", target: "
                        + cropTo.width + "x" + cropTo.height);
            }
            return fullTilesRaster;
        }

        ParameterBlock cropParams = new ParameterBlock();

        cropParams.addSource(fullTilesRaster);// Source
        cropParams.add(Float.valueOf(cropTo.x)); // x origin for each band
        cropParams.add(Float.valueOf(cropTo.y)); // y origin for each band
        cropParams.add(Float.valueOf(cropTo.width));// width for each band
        cropParams.add(Float.valueOf(cropTo.height));// height for each band

        final RenderingHints hints = null;
        RenderedImage image = JAI.create("Crop", cropParams, hints);

        assert cropTo.x == image.getMinX();
        assert cropTo.y == image.getMinY();
        assert cropTo.width == image.getWidth();
        assert cropTo.height == image.getHeight();
        return image;
    }

    static class ReadParameters {
        GeneralEnvelope requestedEnvelope;

        Rectangle dim;

        OverviewPolicy overviewPolicy;
    }

    /**
     * Creates a prepared query for the coverage's table, does not set any constraint nor executes
     * it.
     */
    private SeQuery createSeQuery(final ArcSDEPooledConnection conn) throws IOException {
        final SeQuery seQuery;
        final String[] rasterColumns = rasterInfo.getRasterColumns();
        final String tableName = rasterInfo.getRasterTable();
        try {
            seQuery = new SeQuery(conn, rasterColumns, new SeSqlConstruct(tableName));
            seQuery.prepareQuery();
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
        return seQuery;
    }

    private RenderedOp createTiledRaster(final SeQuery preparedQuery, final SeRow row,
            final SeRasterAttr rAttr, final Rectangle matchingTiles, final int pyramidLevel,
            final Point imageLocation) throws IOException {

        final int tileWidth;
        final int tileHeight;
        final int numberOfBands;
        final RasterCellType pixelType;
        try {
            numberOfBands = rAttr.getNumBands();
            pixelType = RasterCellType.valueOf(rAttr.getPixelType());
            tileWidth = rAttr.getTileWidth();
            tileHeight = rAttr.getTileHeight();

            int[] bandsToQuery = new int[numberOfBands];
            for (int bandN = 1; bandN <= numberOfBands; bandN++) {
                bandsToQuery[bandN - 1] = bandN;
            }

            int minTileX = matchingTiles.x;
            int minTileY = matchingTiles.y;
            int maxTileX = minTileX + matchingTiles.width - 1;
            int maxTileY = minTileY + matchingTiles.height - 1;
            LOGGER.fine("Requesting tiles [" + minTileX + "," + minTileY + ":" + maxTileX + ","
                    + maxTileY + "]");

            Rectangle tiledImageSize = new Rectangle(0, 0, tileWidth * matchingTiles.width,
                    tileHeight * matchingTiles.height);

            LOGGER.fine("Tiled image size: " + tiledImageSize);

            final int interleaveType = SeRaster.SE_RASTER_INTERLEAVE_BIP;

            SeRasterConstraint rConstraint = new SeRasterConstraint();
            rConstraint.setBands(bandsToQuery);
            rConstraint.setLevel(pyramidLevel);
            rConstraint.setEnvelope(minTileX, minTileY, maxTileX, maxTileY);
            rConstraint.setInterleave(interleaveType);

            preparedQuery.queryRasterTile(rConstraint);

        } catch (SeException se) {
            throw new ArcSdeException(se);
        }

        // Finally, build the image input stream
        final ImageInputStream tiledImageInputStream;

        final TileReader tileReader = TileReader.getInstance(row, pixelType.getBitsPerSample(),
                numberOfBands, matchingTiles, new Dimension(tileWidth, tileHeight));
        tiledImageInputStream = new ArcSDETiledImageInputStream(tileReader);

        final int tiledImageWidth = tileReader.getTilesWide() * tileReader.getTileWidth();
        final int tiledImageHeight = tileReader.getTilesHigh() * tileReader.getTileHeight();

        // Prepare temporary colorModel and sample model, needed to build the final
        // ArcSDEPyramidLevel level;
        final ColorModel colorModel;
        final SampleModel sampleModel;
        {
            final ImageTypeSpecifier fullImageSpec = rasterInfo.getRenderedImageSpec();
            colorModel = fullImageSpec.getColorModel();
            sampleModel = fullImageSpec.getSampleModel(tiledImageWidth, tiledImageHeight);
        }

        final long[] imageOffsets = new long[] { 0 };
        final Dimension[] imageDimensions = new Dimension[] { new Dimension(tiledImageWidth,
                tiledImageHeight) };

        final ImageTypeSpecifier its = new ImageTypeSpecifier(colorModel, sampleModel);
        final RawImageInputStream raw = new RawImageInputStream(tiledImageInputStream, its,
                imageOffsets, imageDimensions);

        // building the final image layout
        final ImageLayout imageLayout;
        {
            int minX = imageLocation.x;
            int minY = imageLocation.y;
            int width = tiledImageWidth;
            int height = tiledImageHeight;

            int tileGridXOffset = imageLocation.x;
            int tileGridYOffset = imageLocation.y;

            imageLayout = new ImageLayout(minX, minY, width, height, tileGridXOffset,
                    tileGridYOffset, tileWidth, tileHeight, sampleModel, colorModel);
        }

        // First operator: read the image
        final RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, imageLayout);

        ParameterBlock pb = new ParameterBlock();
        pb.add(raw);// Input
        /*
         * image index, always 0 since we're already fetching the required pyramid level
         */
        pb.add(Integer.valueOf(0)); // Image index
        pb.add(Boolean.TRUE); // Read metadata
        pb.add(Boolean.TRUE);// Read thumbnails
        pb.add(Boolean.TRUE);// Verify input
        pb.add(null);// Listeners
        pb.add(null);// Locale
        final ImageReadParam rParam = new ImageReadParam();
        pb.add(rParam);// ReadParam
        RawImageReaderSpi imageIOSPI = new RawImageReaderSpi();
        ImageReader readerInstance = imageIOSPI.createReaderInstance();
        pb.add(readerInstance);// Reader

        RenderedOp image = JAI.create("ImageRead", pb, hints);
        return image;
    }

    /**
     * A simple helper class to guard and easy logging the mosaic geometries in both geographical
     * and pixel ranges
     */
    private static class LoggingHelper {

        public Level GEOM_LEVEL = Level.FINER;

        public static String REQ_ENV = "Requested envelope";

        public static String RES_ENV = "Resulting envelope";

        public static String MOSAIC_ENV = "Resulting mosaiced envelopes";

        public static String MOSAIC_EXPECTED = "Expected mosaic layout (in pixels)";

        public static String MOSAIC_RESULT = "Resulting image mosaic layout (in pixels)";

        private Map<String, StringBuilder> geoms = null;

        LoggingHelper() {
            // not much to to
        }

        private StringBuilder getGeom(String geomName) {
            if (geoms == null) {
                geoms = new HashMap<String, StringBuilder>();
            }
            StringBuilder sb = geoms.get(geomName);
            if (sb == null) {
                sb = new StringBuilder("MULTIPOLYGON(\n");
                geoms.put(geomName, sb);
            }
            return sb;
        }

        public void appendLoggingGeometries(String geomName, RenderedImage img) {
            if (LOGGER.isLoggable(GEOM_LEVEL)) {
                appendLoggingGeometries(geomName, new Rectangle(img.getMinX(), img.getMinY(), img
                        .getWidth(), img.getHeight()));
            }
        }

        public void appendLoggingGeometries(String geomName, Rectangle env) {
            if (LOGGER.isLoggable(GEOM_LEVEL)) {
                appendLoggingGeometries(geomName, new GeneralEnvelope(env));
            }
        }

        public void appendLoggingGeometries(String geomName, GeneralEnvelope env) {
            if (LOGGER.isLoggable(GEOM_LEVEL)) {
                StringBuilder sb = getGeom(geomName);
                sb.append("\n  ((" + env.getMinimum(0) + " " + env.getMinimum(1) + ", "
                        + env.getMaximum(0) + " " + env.getMinimum(1) + ", " + env.getMaximum(0)
                        + " " + env.getMaximum(1) + ", " + env.getMinimum(0) + " "
                        + env.getMaximum(1) + ", " + env.getMinimum(0) + " " + env.getMinimum(1)
                        + ")),");
            }
        }

        public void log(String geomName) {
            if (LOGGER.isLoggable(GEOM_LEVEL)) {
                StringBuilder sb = getGeom(geomName);
                sb.setLength(sb.length() - 1);
                sb.append("\n)");
                LOGGER.log(GEOM_LEVEL, geomName + ":\n" + sb.toString());
            }
        }
    }
}
