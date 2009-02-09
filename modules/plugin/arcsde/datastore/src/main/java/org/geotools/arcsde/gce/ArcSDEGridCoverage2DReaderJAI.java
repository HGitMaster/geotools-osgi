package org.geotools.arcsde.gce;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.gce.RasterUtils.QueryInfo;
import org.geotools.arcsde.gce.imageio.ArcSDEPyramid;
import org.geotools.arcsde.gce.imageio.ArcSDEPyramidLevel;
import org.geotools.arcsde.gce.imageio.RasterCellType;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultServiceInfo;
import org.geotools.data.ServiceInfo;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.resources.image.ColorUtilities;
import org.geotools.resources.image.ComponentColorModelJAI;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterConstraint;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.sun.imageio.plugins.common.BogusColorSpace;
import com.sun.media.imageio.stream.RawImageInputStream;
import com.sun.media.imageioimpl.plugins.raw.RawImageReaderSpi;

@SuppressWarnings( { "deprecation", "nls" })
public class ArcSDEGridCoverage2DReaderJAI extends AbstractGridCoverage2DReader {

    private final static Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    private final ArcSDERasterFormat parent;

    private final ArcSDEConnectionPool connectionPool;

    private final RasterInfo rasterInfo;

    private DefaultServiceInfo serviceInfo;

    public ArcSDEGridCoverage2DReaderJAI(ArcSDERasterFormat parent,
            ArcSDEConnectionPool connectionPool, RasterInfo rasterInfo, Hints hints)
            throws IOException {
        this.parent = parent;
        this.connectionPool = connectionPool;
        this.rasterInfo = rasterInfo;

        final ArcSDEPyramid pyramidInfo = rasterInfo.getPyramidInfo();

        super.hints = hints;
        super.coverageFactory = CoverageFactoryFinder.getGridCoverageFactory(this.hints);
        super.crs = rasterInfo.getCoverageCrs();
        super.originalEnvelope = rasterInfo.getOriginalEnvelope();
        super.originalGridRange = rasterInfo.getOriginalGridRange();
        super.coverageName = rasterInfo.getRasterTable();
        final int numLevels = pyramidInfo.getNumLevels();

        // level 0 is not an overview, but the raster itself
        super.numOverviews = numLevels - 1;

        // ///
        // 
        // setting the higher resolution avalaible for this coverage
        //
        // ///
        // highestRes = new double[2];
        // highestRes[0] = pyramidInfo.getPyramidLevel(0).getXRes();
        // highestRes[1] = pyramidInfo.getPyramidLevel(0).getYRes();
        ArcSDEPyramidLevel levelZero = pyramidInfo.getPyramidLevel(0);
        highestRes = super
                .getResolution(new GeneralEnvelope(levelZero.getEnvelope()),
                        new Rectangle2D.Double(0, 0, levelZero.getSize().width,
                                levelZero.getSize().height), crs);
        // //
        //
        // get information for the successive images
        //
        // //
        if (numOverviews >= 1) {
            overViewResolutions = new double[numOverviews][2];
            for (int pyramidLevel = 1; pyramidLevel <= numOverviews; pyramidLevel++) {
                ArcSDEPyramidLevel level = pyramidInfo.getPyramidLevel(pyramidLevel);
                overViewResolutions[pyramidLevel - 1] = super.getResolution(new GeneralEnvelope(
                        level.getEnvelope()), new Rectangle2D.Double(0, 0, level.getSize().width,
                        level.getSize().height), crs);
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
            final ReadParameters opParams = parseReadParams(params);
            overviewPolicy = opParams.overviewPolicy;
            requestedEnvelope = opParams.requestedEnvelope;
            requestedDim = opParams.dim;
        }
        final ArcSDEPyramid pyramidInfo = rasterInfo.getPyramidInfo();

        /*
         * set params
         */

        // This is to choose the pyramid level as if it were the image index in a file with
        // overviews
        int pyramidLevelChoice = 0;
        final ImageReadParam readP = new ImageReadParam();
        try {
            pyramidLevelChoice = setReadParams(overviewPolicy, readP, requestedEnvelope,
                    requestedDim);
            LOGGER.info("Pyramid level chosen: " + pyramidInfo.getPyramidLevel(pyramidLevelChoice));
        } catch (TransformException e) {
            new IllegalArgumentException(e);
        }

        /*
         * Obtain the tiles, pixel range, and resulting envelope
         */
        final QueryInfo rasterQueryInfo = RasterUtils.fitRequestToRaster(requestedEnvelope,
                requestedDim, pyramidInfo, pyramidLevelChoice);

        LOGGER.info(rasterQueryInfo.toString());

        /*
         * Create the prepared query (not executed) stream to fetch the tiles from
         */
        final Rectangle matchingTiles = rasterQueryInfo.getMatchingTiles();
        final ArcSDEPooledConnection conn = connectionPool.getConnection();
        final SeQuery preparedQuery;
        try {
            preparedQuery = createSeQuery(conn);
        } catch (IOException e) {
            conn.close();
            throw e;
        }

        final RenderedImage coverageRaster;
        try {
            // covers an area of full tiles
            final RenderedImage fullTilesRaster;
            /*
             * Create the tiled raster covering the full area of the matching tiles
             */
            fullTilesRaster = createTiledRaster(preparedQuery, matchingTiles, pyramidLevelChoice);

            /*
             * now crop it to the desired dimensions
             */
            final Rectangle resultDimension = rasterQueryInfo.getResultDimension();
            coverageRaster = cropToRequiredDimension(fullTilesRaster, resultDimension);
            /*
             * REVISIT: This is odd, we need to force the data to be loaded so we're free to release
             * the stream, which gives away the streamed, tiled nature of this rasters, but I don't
             * see the GCE api having a very clear usage workflow that ensures close() is always
             * being called to the underlying ImageInputStream so we could let it close the SeQuery
             * when done.
             */
            coverageRaster.getData();
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
        final GeneralEnvelope finalEnvelope = rasterQueryInfo.getResultEnvelope();

        return coverageFactory.create(coverageName, coverageRaster, finalEnvelope, bands, null,
                null);
    }

    private RenderedOp cropToRequiredDimension(final RenderedImage fullTilesRaster,
            final Rectangle cropTo) {

        int width = fullTilesRaster.getWidth();
        int height = fullTilesRaster.getHeight();

        Rectangle origDim = new Rectangle(0, 0, width, height);
        if (!origDim.contains(cropTo)) {
            throw new IllegalArgumentException("Original image (" + origDim
                    + ") does not contain desired dimension (" + cropTo + ")");
        }

        ParameterBlock cropParams = new ParameterBlock();

        cropParams.addSource(fullTilesRaster);// Source
        cropParams.add(Float.valueOf(cropTo.x)); // x origin for each band
        cropParams.add(Float.valueOf(cropTo.y)); // y origin for each band
        cropParams.add(Float.valueOf(cropTo.width));// width for each band
        cropParams.add(Float.valueOf(cropTo.height));// height for each band

        final RenderingHints hints = null;
        RenderedOp image = JAI.create("Crop", cropParams, hints);
        return image;
    }

    private static class ReadParameters {
        GeneralEnvelope requestedEnvelope;

        Rectangle dim;

        OverviewPolicy overviewPolicy;
    }

    private ReadParameters parseReadParams(GeneralParameterValue[] params)
            throws IllegalArgumentException {
        if (params == null) {
            throw new IllegalArgumentException("No GeneralParameterValue given to read operation");
        }

        GeneralEnvelope reqEnvelope = null;
        Rectangle dim = null;
        OverviewPolicy overviewPolicy = null;

        // /////////////////////////////////////////////////////////////////////
        //
        // Checking params
        //
        // /////////////////////////////////////////////////////////////////////
        for (int i = 0; i < params.length; i++) {
            final ParameterValue<?> param = (ParameterValue<?>) params[i];
            final String name = param.getDescriptor().getName().getCode();
            if (name.equals(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName().toString())) {
                final GridGeometry2D gg = (GridGeometry2D) param.getValue();
                reqEnvelope = new GeneralEnvelope((Envelope) gg.getEnvelope2D());

                CoordinateReferenceSystem nativeCrs = rasterInfo.getCoverageCrs();
                CoordinateReferenceSystem requestCrs = reqEnvelope.getCoordinateReferenceSystem();
                if (!CRS.equalsIgnoreMetadata(nativeCrs, requestCrs)) {
                    LOGGER.info("Request CRS and native CRS differ, "
                            + "reprojecting request envelope to native CRS");
                    ReferencedEnvelope nativeCrsEnv;
                    nativeCrsEnv = RasterUtils.toNativeCrs(reqEnvelope, nativeCrs);
                    reqEnvelope = new GeneralEnvelope(nativeCrsEnv);
                }

                dim = gg.getGridRange2D().getBounds();
                continue;
            }
            if (name.equals(AbstractGridFormat.OVERVIEW_POLICY.getName().toString())) {
                overviewPolicy = (OverviewPolicy) param.getValue();
                continue;
            }
        }

        if (overviewPolicy == null) {
            LOGGER.finer("No overview policy requested, defaulting to QUALITY");
            overviewPolicy = OverviewPolicy.QUALITY;
        }
        LOGGER.fine("Overview policy is " + overviewPolicy);

        LOGGER.info("Reading raster for " + dim.getWidth() + "x" + dim.getHeight()
                + " requested dim and " + reqEnvelope.getMinimum(0) + ","
                + reqEnvelope.getMaximum(0) + " - " + reqEnvelope.getMinimum(1)
                + reqEnvelope.getMaximum(1) + " requested extent");

        ReadParameters parsedParams = new ReadParameters();
        parsedParams.requestedEnvelope = reqEnvelope;
        parsedParams.dim = dim;
        parsedParams.overviewPolicy = overviewPolicy;
        return parsedParams;
    }

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

    private RenderedOp createTiledRaster(final SeQuery preparedQuery,
            final Rectangle matchingTiles, final int pyramidLevel) throws IOException {
        final SeRow row;

        final int tileWidth;
        final int tileHeight;
        final int numberOfBands;
        final RasterCellType pixelType;
        try {
            final SeRasterAttr rAttr;
            preparedQuery.execute();
            row = preparedQuery.fetch();
            rAttr = row.getRaster(0);

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
            int maxTileX = minTileX + matchingTiles.width;
            int maxTileY = minTileY + matchingTiles.height;
            LOGGER.fine("Requesting tiles [" + minTileX + "," + minTileY + ":" + maxTileX + ","
                    + maxTileY + "]");

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

        // Prepare temporaray colorModel and sample model, needed to build the final
        // ArcSDEPyramidLevel level;
        final ColorModel colorModel;
        final SampleModel sampleModel;
        {
            final int bitsPerSample = pixelType.getBitsPerSample();
            final int dataType = pixelType.getDataBufferType();

            if (bitsPerSample == 1 || bitsPerSample == 4) {
                if (numberOfBands != 1) {
                    throw new DataSourceException(bitsPerSample
                            + "-Bit rasters are only supported for one band");
                }
                int[] argb = new int[(int) Math.pow(2, bitsPerSample)];
                ColorUtilities.expand(new Color[] { Color.WHITE, Color.BLACK }, argb, 0,
                        argb.length);
                colorModel = rasterInfo.getGridSampleDimensions()[0].getColorModel(0,
                        numberOfBands, dataType);
                sampleModel = colorModel.createCompatibleSampleModel(tiledImageWidth,
                        tiledImageHeight);
            } else {
                int[] numBits = new int[numberOfBands];
                for (int i = 0; i < numberOfBands; i++) {
                    numBits[i] = bitsPerSample;
                }

                final ColorSpace colorSpace;
                switch (numberOfBands) {
                case 1:
                    colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
                    break;
                case 3:
                    colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
                    break;
                default:
                    colorSpace = new BogusColorSpace(numberOfBands);
                }
                colorModel = new ComponentColorModelJAI(colorSpace, numBits, false, false,
                        Transparency.OPAQUE, dataType);

                int[] bankIndices = new int[numberOfBands];
                int[] bandOffsets = new int[numberOfBands];
                // int bandOffset = (tileWidth * tileHeight * pixelType.getBitsPerSample()) / 8;
                for (int i = 0; i < numberOfBands; i++) {
                    bankIndices[i] = i;
                    bandOffsets[i] = 0;// (i * bandOffset);
                }
                sampleModel = new BandedSampleModel(dataType, tiledImageWidth, tiledImageHeight,
                        tiledImageWidth, bankIndices, bandOffsets);
            }
        }
        System.out.println("ColorModel: " + colorModel);
        System.out.println("SampleModel: " + sampleModel);

        final long[] imageOffsets = new long[] { 0 };
        final Dimension[] imageDimensions = new Dimension[] { new Dimension(tiledImageWidth,
                tiledImageHeight) };

        final ImageTypeSpecifier its = new ImageTypeSpecifier(colorModel, sampleModel);
        final RawImageInputStream raw = new RawImageInputStream(tiledImageInputStream, its,
                imageOffsets, imageDimensions);

        // building the final image layout
        final ImageLayout imageLayout;
        {
            int minX = 0;
            int minY = 0;
            int width = tiledImageWidth;
            int height = tiledImageHeight;

            int tileGridXOffset = 0;
            int tileGridYOffset = 0;

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
}
