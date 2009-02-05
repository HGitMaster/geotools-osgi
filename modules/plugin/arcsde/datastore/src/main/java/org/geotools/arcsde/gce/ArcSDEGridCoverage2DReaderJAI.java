package org.geotools.arcsde.gce;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.gce.imageio.ArcSDEPyramid;
import org.geotools.arcsde.gce.imageio.ArcSDEPyramidLevel;
import org.geotools.arcsde.gce.imageio.RasterCellType;
import org.geotools.arcsde.gce.imageio.ArcSDEPyramid.RasterQueryInfo;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
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
import org.geotools.parameter.Parameter;
import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
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

        // super.raster2Model = getRasterToModel();
    }

    private MathTransform getRasterToModel() {
        final GridToEnvelopeMapper geMapper;
        geMapper = new GridToEnvelopeMapper(originalGridRange, originalEnvelope);
        geMapper.setPixelAnchor(PixelInCell.CELL_CENTER);
        final MathTransform rasterToModel = geMapper.createTransform();
        return rasterToModel;
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

    public GridCoverage read(GeneralParameterValue[] params) throws IOException {
        GeneralEnvelope requestedEnvelope = null;
        Rectangle dim = null;
        OverviewPolicy overviewPolicy = null;
        if (params != null) {
            // /////////////////////////////////////////////////////////////////////
            //
            // Checking params
            //
            // /////////////////////////////////////////////////////////////////////
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    final ParameterValue param = (ParameterValue) params[i];
                    final String name = param.getDescriptor().getName().getCode();
                    if (name.equals(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName().toString())) {
                        final GridGeometry2D gg = (GridGeometry2D) param.getValue();
                        requestedEnvelope = new GeneralEnvelope((Envelope) gg.getEnvelope2D());

                        ReferencedEnvelope nativeCrsEnv;
                        nativeCrsEnv = RasterUtils.toNativeCrs(requestedEnvelope, crs);
                        requestedEnvelope = new GeneralEnvelope(nativeCrsEnv);
                        dim = gg.getGridRange2D().getBounds();
                        continue;
                    }
                    if (name.equals(AbstractGridFormat.OVERVIEW_POLICY.getName().toString())) {
                        overviewPolicy = (OverviewPolicy) param.getValue();
                        continue;
                    }
                }
            }
        }
        LOGGER.info("Reading raster for " + dim.getWidth() + "x" + dim.getHeight()
                + " requested dim and " + requestedEnvelope.getMinimum(0) + ","
                + requestedEnvelope.getMaximum(0) + " - " + requestedEnvelope.getMinimum(1)
                + requestedEnvelope.getMaximum(1) + " requested extent");

        final ArcSDEPyramid pyramidInfo = rasterInfo.getPyramidInfo();

        // /////////////////////////////////////////////////////////////////////
        //
        // set params
        //
        // /////////////////////////////////////////////////////////////////////
        // This is to choose the pyramid level as if it were the image index in a file with
        // overviews
        int pyramidLevelChoice = 0;
        final ImageReadParam readP = new ImageReadParam();
        try {
            pyramidLevelChoice = setReadParams(overviewPolicy, readP, requestedEnvelope, dim);
            LOGGER.info("Pyramid level chosen: " + pyramidInfo.getPyramidLevel(pyramidLevelChoice));
        } catch (TransformException e) {
            new DataSourceException(e);
        }

        // /////////////////////////////////////////////////////////////////////
        //
        // IMAGE READ OPERATION
        //
        // /////////////////////////////////////////////////////////////////////

        final RasterQueryInfo queryInfo = pyramidInfo.fitExtentToRasterPixelGrid(
                new ReferencedEnvelope(requestedEnvelope), pyramidLevelChoice);

        final RenderedOp coverageRaster = createRaster(pyramidLevelChoice, queryInfo);

        // /////////////////////////////////////////////////////////////////////
        //
        // BUILDING COVERAGE
        //
        // /////////////////////////////////////////////////////////////////////
        // I need to calculate a new transformation (raster2Model)
        // between the cropped image and the required
        // adjustedRequestEnvelope
        final int ssWidth = coverageRaster.getWidth();
        final int ssHeight = coverageRaster.getHeight();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Coverage read: width = " + ssWidth + " height = " + ssHeight);
        }

        // //
        //
        // setting new coefficients to define a new affineTransformation
        // to be applied to the grid to world transformation
        // -----------------------------------------------------------------------------------
        //
        // With respect to the original envelope, the obtained planarImage
        // needs to be rescaled. The scaling factors are computed as the
        // ratio between the cropped source region sizes and the read
        // image sizes.
        //
        // //
        final double scaleX = originalGridRange.getLength(0) / (1.0 * ssWidth);
        final double scaleY = originalGridRange.getLength(1) / (1.0 * ssHeight);

        // final AffineTransform tempRaster2Model = new AffineTransform((AffineTransform)
        // raster2Model);
        // tempRaster2Model.concatenate(new AffineTransform(scaleX, 0, 0, scaleY, 0, 0));
        //        
        // return createImageCoverage(coverageRaster, ProjectiveTransform
        // .create((AffineTransform) tempRaster2Model));
        return createImageCoverage(coverageRaster);
    }

    private RenderedOp createRaster(final int pyramidLevel, final RasterQueryInfo queryInfo)
            throws IOException {

        final ArcSDEPooledConnection conn = connectionPool.getConnection();
        final SeRow row;

        final int tileWidth, tileHeight;
        /*
         * The image size resulting of fetching all the required tiles to comply with the requested
         * image subset
         */
        final Rectangle actualImageSize;
        final int numberOfBands;
        final RasterCellType pixelType;
        try {
            final SeRasterAttr rAttr;
            final String rasterColumnName = rasterInfo.getRasterColumns()[0];
            final String tableName = rasterInfo.getRasterTable();
            SeQuery seQuery = new SeQuery(conn, new String[] { rasterColumnName },
                    new SeSqlConstruct(tableName));
            seQuery.prepareQuery();
            seQuery.execute();
            row = seQuery.fetch();
            rAttr = row.getRaster(0);

            numberOfBands = rAttr.getNumBands();
            pixelType = RasterCellType.valueOf(rAttr.getPixelType());

            tileWidth = rAttr.getTileWidth();
            tileHeight = rAttr.getTileHeight();

            SeRasterConstraint rConstraint = new SeRasterConstraint();
            int[] bandsToQuery = new int[numberOfBands];
            for (int bandN = 1; bandN <= numberOfBands; bandN++) {
                bandsToQuery[bandN - 1] = bandN;
            }

            rConstraint.setBands(bandsToQuery);
            rConstraint.setLevel(pyramidLevel);

            final ArcSDEPyramid pyramidInfo = rasterInfo.getPyramidInfo();
            final ArcSDEPyramidLevel optimalLevel = pyramidInfo.getPyramidLevel(pyramidLevel);

            int minTileX = -1;
            int minTileY = -1;
            int maxTileX = -1;
            int maxTileY = -1;
            {
                final int numTilesWide = optimalLevel.getNumTilesWide();
                final int numTilesHigh = optimalLevel.getNumTilesHigh();
                final Rectangle pixels = queryInfo.image;
                final int imageMinX = pixels.x;
                final int imageMinY = pixels.y;
                final int imageMaxX = imageMinX + pixels.width;
                final int imageMaxY = imageMinY + pixels.height;

                for (int tileX = 0; tileX < numTilesWide; tileX++) {
                    final int tileMinx = tileX * tileWidth;
                    final int tileMaxx = tileMinx + tileWidth - 1;

                    if (minTileX == -1 && imageMinX <= tileMaxx) {
                        minTileX = tileX;
                    }

                    if (imageMaxX >= tileMinx) {
                        maxTileX = tileX;
                    }
                }
                for (int tileY = 0; tileY < numTilesHigh; tileY++) {
                    final int tileMiny = tileY * tileHeight;
                    final int tileMaxy = tileMiny + tileHeight - 1;
                    if (minTileY == -1 && imageMinY <= tileMaxy) {
                        minTileY = tileY;
                    }

                    if (imageMaxY >= tileMiny) {
                        maxTileY = tileY;
                    }
                }
                LOGGER.info("Requesting tiles " + minTileX + ":" + maxTileX + "," + minTileY + ":"
                        + maxTileY);
            }
            rConstraint.setEnvelope(minTileX, minTileY, maxTileX, maxTileY);

            final int actualX = tileWidth * minTileX;
            final int actualY = tileHeight * minTileY;
            final int actualImageWidth = tileWidth * (1 + maxTileX - minTileX);
            final int actualImageHeight = tileHeight * (1 + maxTileY - minTileY);
            actualImageSize = new Rectangle(actualX, actualY, actualImageWidth, actualImageHeight);

            final int interleaveType = SeRaster.SE_RASTER_INTERLEAVE_BIP;
            rConstraint.setInterleave(interleaveType);
            seQuery.queryRasterTile(rConstraint);

        } catch (SeException se) {
            conn.close();
            throw new ArcSdeException(se);
        }

        // Prepare temporaray colorModel and sample model, needed to build the final
        // ArcSDEPyramidLevel level;
        final ColorModel colorModel;
        final SampleModel sampleModel;
        {
            int[] bankIndices = new int[numberOfBands];
            int[] bandOffsets = new int[numberOfBands];

            int bandOffset = (tileWidth * tileHeight * pixelType.getBitsPerSample()) / 8;

            for (int i = 0; i < numberOfBands; i++) {
                bankIndices[i] = i;
                bandOffsets[i] = 0;// (i * bandOffset);
            }
            sampleModel = new BandedSampleModel(pixelType.getDataBufferType(),
                    actualImageSize.width, actualImageSize.height, actualImageSize.width,
                    bankIndices, bandOffsets);

            int[] numBits = new int[numberOfBands];
            int bits = pixelType.getBitsPerSample();// DataBuffer.getDataTypeSize(dataType);
            for (int i = 0; i < numberOfBands; i++) {
                numBits[i] = bits;
            }

            final int dataType = pixelType.getDataBufferType();
            ColorSpace colorSpace = new BogusColorSpace(numberOfBands);
            colorModel = new ComponentColorModel(colorSpace, numBits, false, false,
                    Transparency.OPAQUE, dataType);
        }

        // Finally, build the image input stream
        final ImageInputStream in;

        try {
            TileReader reader = TileReader.getInstance(conn, pixelType, row, numberOfBands,
                    tileWidth, tileHeight);
            in = new ArcSDETiledImageInputStream(reader);
        } catch (IOException e) {
            conn.close();
            throw e;
        }

        final RawImageInputStream raw = new RawImageInputStream(in, sampleModel, new long[] { 0 },
                new Dimension[] { new Dimension(actualImageSize.width, actualImageSize.height) });

        // building the final image layout
        final ImageLayout imageLayout;
        {
            int minX = actualImageSize.x;// queryInfo.image.x;
            int minY = actualImageSize.y;// queryInfo.image.y;
            int width = actualImageSize.width;
            int height = actualImageSize.height;

            int tileGridXOffset = 0;// level.getXOffset();
            int tileGridYOffset = 0;// level.getYOffset();

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
        pb.add(Boolean.FALSE); // Read metadata
        pb.add(Boolean.FALSE);// Read thumbnails
        pb.add(Boolean.FALSE);// Verify input
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
     * @see GridCoverageReader#read(GeneralParameterValue[])
     */
    public GridCoverage2D _read(GeneralParameterValue[] params) throws IllegalArgumentException,
            IOException {
        GeneralEnvelope readEnvelope = null;
        Rectangle requestedDim = null;
        if (params != null) {
            final int length = params.length;
            Parameter<?> param;
            String paramName;
            for (int i = 0; i < length; i++) {
                param = (Parameter<?>) params[i];
                paramName = param.getDescriptor().getName().getCode();
                if (paramName.equals(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName().toString())) {
                    final GridGeometry2D gg = (GridGeometry2D) param.getValue();
                    readEnvelope = new GeneralEnvelope((Envelope) gg.getEnvelope2D());
                    requestedDim = gg.getGridRange2D().getBounds();
                } else {
                    LOGGER.warning("discarding parameter with name " + paramName);
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
            coverage = createCoverage(readEnvelope, requestedDim);
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
            throw e;
        }

        return coverage;
    }

    private GridCoverage2D createCoverage(GeneralEnvelope requestedEnvelope, Rectangle requestedDim)
            throws IOException {

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Creating coverage out of request: imagesize -- " + requestedDim
                    + "  envelope -- " + requestedEnvelope);
        }

        final ArcSDEPyramid pyramidInfo = rasterInfo.getPyramidInfo();
        final ReferencedEnvelope reqEnv;
        reqEnv = RasterUtils.toNativeCrs(requestedEnvelope, rasterInfo.getCoverageCrs());

        final ArcSDEPyramidLevel optimalLevel;
        optimalLevel = getOptimalPyramidLevel(requestedDim, reqEnv, pyramidInfo);

        final GeneralEnvelope outputImageEnvelope;

        final RenderedImage outputImage;

        if (false && !optimalLevel.getEnvelope().intersects((BoundingBox) reqEnv)) {
            // this is a blank raster. I guess we should create a completely
            // blank image with the
            // correct size and return that. Transparency?
            int width = requestedDim.width;
            int height = requestedDim.height;
            // outputImage = RasterUtils.createInitialBufferedImage(sampleImage, width, height);
            // outputImageEnvelope = new GeneralEnvelope(reqEnv);
        } else {
            final int pyramidLevel = optimalLevel.getLevel();
            final List<RasterBandInfo> bands = rasterInfo.getBands();
            final ArcSDEPooledConnection conn = connectionPool.getConnection();
            final SeRow row;

            final int tileW, tileH;
            final Rectangle imageSize;
            final int numberOfBands;
            final RasterCellType pixelType;
            try {
                final SeRasterAttr rAttr;
                final String rasterColumnName = rasterInfo.getRasterColumns()[0];
                final String tableName = rasterInfo.getRasterTable();
                SeQuery seQuery = new SeQuery(conn, new String[] { rasterColumnName },
                        new SeSqlConstruct(tableName));
                seQuery.prepareQuery();
                seQuery.execute();
                row = seQuery.fetch();
                rAttr = row.getRaster(0);

                numberOfBands = rAttr.getNumBands();
                pixelType = RasterCellType.valueOf(rAttr.getPixelType());

                tileW = rAttr.getTileWidth();
                tileH = rAttr.getTileHeight();

                SeRasterConstraint rConstraint = new SeRasterConstraint();
                int[] bandsToQuery = new int[numberOfBands];
                for (int bandN = 1; bandN <= numberOfBands; bandN++) {
                    bandsToQuery[bandN - 1] = bandN;
                }

                rConstraint.setBands(bandsToQuery);
                rConstraint.setLevel(pyramidLevel);

                final int numTilesWide = optimalLevel.getNumTilesWide();
                final int numTilesHigh = optimalLevel.getNumTilesHigh();
                rConstraint.setEnvelope(0, 0, numTilesWide - 1, numTilesHigh - 1);

                imageSize = new Rectangle(pyramidInfo.getTileWidth() * numTilesWide, pyramidInfo
                        .getTileHeight()
                        * numTilesHigh);

                final int interleaveType = SeRaster.SE_RASTER_INTERLEAVE_BIP;
                rConstraint.setInterleave(interleaveType);
                seQuery.queryRasterTile(rConstraint);

            } catch (SeException se) {
                conn.close();
                throw new ArcSdeException(se);
            }

            // Prepare temporaray colorModel and sample model, needed to build the final
            // ArcSDEPyramidLevel level;
            final ColorModel colorModel;
            final SampleModel sampleModel;
            {
                int[] bankIndices = new int[numberOfBands];
                int[] bandOffsets = new int[numberOfBands];

                int bandOffset = (tileW * tileH * pixelType.getBitsPerSample()) / 8;

                for (int i = 0; i < numberOfBands; i++) {
                    bankIndices[i] = i;
                    bandOffsets[i] = (i * bandOffset);
                }
                sampleModel = new BandedSampleModel(pixelType.getDataBufferType(), imageSize.width,
                        imageSize.height, imageSize.width, bankIndices, bandOffsets);

                int[] numBits = new int[numberOfBands];
                int dataType = pixelType.getDataBufferType();
                int bits = pixelType.getBitsPerSample();// DataBuffer.getDataTypeSize(dataType);
                for (int i = 0; i < numberOfBands; i++) {
                    numBits[i] = bits;
                }

                ColorSpace colorSpace = new BogusColorSpace(numberOfBands);
                colorModel = new ComponentColorModel(colorSpace, numBits, false, false,
                        Transparency.OPAQUE, pixelType.getDataBufferType());
            }

            // Finally, build the image input stream
            final ImageInputStream in;

            try {
                TileReader reader = TileReader.getInstance(conn, pixelType, row, numberOfBands,
                        tileW, tileH);
                in = new ArcSDETiledImageInputStream(reader);
            } catch (IOException e) {
                conn.close();
                throw e;
            }

            final RawImageInputStream raw = new RawImageInputStream(in, sampleModel,
                    new long[] { 0 }, new Dimension[] { new Dimension(imageSize.width,
                            imageSize.height) });

            // building the final image layout
            final ImageLayout imageLayout;
            {
                int minX = 0;
                int minY = 0;
                int width = imageSize.width;
                int height = imageSize.height;

                int tileGridXOffset = 0;// level.getXOffset();
                int tileGridYOffset = 0;// level.getYOffset();

                imageLayout = new ImageLayout(minX, minY, width, height, tileGridXOffset,
                        tileGridYOffset, tileW, tileH, sampleModel, colorModel);
            }

            // First operator: read the image
            final RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, imageLayout);

            ParameterBlock pb = new ParameterBlock();
            pb.add(raw);
            /*
             * image index, always 0 since we're already fetching the required pyramid level
             */
            pb.add(Integer.valueOf(0));
            pb.add(Boolean.FALSE);
            pb.add(Boolean.FALSE);
            pb.add(Boolean.FALSE);
            pb.add(null);
            pb.add(null);
            final ImageReadParam rParam = new ImageReadParam();
            pb.add(rParam);
            RawImageReaderSpi imageIOSPI = new RawImageReaderSpi();
            ImageReader readerInstance = imageIOSPI.createReaderInstance();
            pb.add(readerInstance);

            RenderedOp image = JAI.create("ImageRead", pb, hints);
            outputImage = image;
            outputImageEnvelope = new GeneralEnvelope(optimalLevel.getEnvelope());
        }

        // Create the coverage
        final GridSampleDimension[] gridBands = rasterInfo.getGridSampleDimensions();
        return coverageFactory.create(coverageName, outputImage, outputImageEnvelope, gridBands,
                null, null);

    }

    private ArcSDEPyramidLevel getOptimalPyramidLevel(Rectangle requestedDim,
            final ReferencedEnvelope reqEnv, final ArcSDEPyramid pyramidInfo)
            throws DataSourceException {
        ArcSDEPyramidLevel optimalLevel;
        {
            int level = pyramidInfo.pickOptimalRasterLevel(reqEnv, requestedDim);

            optimalLevel = pyramidInfo.getPyramidLevel(level);
        }
        return optimalLevel;
    }

}
