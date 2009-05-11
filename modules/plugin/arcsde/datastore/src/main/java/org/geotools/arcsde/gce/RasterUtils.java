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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageTypeSpecifier;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DataSourceException;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
import org.geotools.resources.image.ColorUtilities;
import org.geotools.resources.image.ComponentColorModelJAI;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;

import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.pe.PeCoordinateSystem;
import com.esri.sde.sdk.pe.PeFactory;
import com.esri.sde.sdk.pe.PeGeographicCS;
import com.esri.sde.sdk.pe.PeProjectedCS;
import com.esri.sde.sdk.pe.PeProjectionException;
import com.sun.imageio.plugins.common.BogusColorSpace;

/**
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.5.4
 * @version $Id: RasterUtils.java 32773 2009-04-10 17:38:40Z groldan $
 * @source $URL$
 */
@SuppressWarnings( { "nls", "deprecation" })
class RasterUtils {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    private RasterUtils() {
        // do nothing
    }

    public static ReferencedEnvelope toReferencedEnvelope(GeneralEnvelope envelope) {
        double minx = envelope.getMinimum(0);
        double maxx = envelope.getMaximum(0);
        double miny = envelope.getMinimum(1);
        double maxy = envelope.getMaximum(1);
        CoordinateReferenceSystem crs = envelope.getCoordinateReferenceSystem();

        ReferencedEnvelope refEnv = new ReferencedEnvelope(minx, maxx, miny, maxy, crs);
        return refEnv;
    }

    public static ReferencedEnvelope toNativeCrs(final GeneralEnvelope requestedEnvelope,
            final CoordinateReferenceSystem nativeCRS) throws IllegalArgumentException {

        ReferencedEnvelope reqEnv = toReferencedEnvelope(requestedEnvelope);

        if (!CRS.equalsIgnoreMetadata(nativeCRS, reqEnv.getCoordinateReferenceSystem())) {
            // we're being reprojected. We'll need to reproject reqEnv into
            // our native coordsys
            try {
                // ReferencedEnvelope origReqEnv = reqEnv;
                reqEnv = reqEnv.transform(nativeCRS, true);
            } catch (FactoryException fe) {
                // unable to reproject?
                throw new IllegalArgumentException("Unable to find a reprojection from requested "
                        + "coordsys to native coordsys for this request", fe);
            } catch (TransformException te) {
                throw new IllegalArgumentException("Unable to perform reprojection from requested "
                        + "coordsys to native coordsys for this request", te);
            }
        }
        return reqEnv;
    }

    /**
     * Gets the coordinate system that will be associated to the {@link GridCoverage}.
     * 
     * @param rasterAttributes
     * @return if {@code seCoordRef.getcoordSys()} is {@code null} returns
     *         {@link DefaultEngineeringCRS#CARTESIAN_2D}, otherwise an equivalent CRS from the EPSG
     *         database if found, or a CRS built from the seCoordRef WKT otherwise.
     */
    public static CoordinateReferenceSystem findCompatibleCRS(final SeCoordinateReference seCoordRef)
            throws DataSourceException {

        if (seCoordRef == null) {
            LOGGER.fine("SeCoordinateReference is null, "
                    + "using DefaultEngineeringCRS.CARTESIAN_2D");
            return DefaultEngineeringCRS.CARTESIAN_2D;
        }

        final PeCoordinateSystem coordSys = seCoordRef.getCoordSys();

        if (coordSys == null) {
            LOGGER.fine("SeCoordinateReference.getCoordSys() is null, "
                    + "using DefaultEngineeringCRS.CARTESIAN_2D");
            return DefaultEngineeringCRS.CARTESIAN_2D;
        }

        try {
            int epsgCode = -1;
            final int[] seEpsgCodes;
            if (coordSys instanceof PeGeographicCS) {
                seEpsgCodes = PeFactory.geogcsCodelist();
            } else if (coordSys instanceof PeProjectedCS) {
                seEpsgCodes = PeFactory.projcsCodelist();
            } else {
                throw new RuntimeException("Shouldnt happen!: Unnkown SeCoordSys type: "
                        + coordSys.getClass().getName());
            }
            int seEpsgCode;
            PeCoordinateSystem candidate;
            for (int i = 0; i < seEpsgCodes.length; i++) {
                try {
                    seEpsgCode = seEpsgCodes[i];
                    candidate = (PeCoordinateSystem) PeFactory.factory(seEpsgCode);
                    // in ArcSDE 9.2, if the PeFactory doesn't support a projection it claimed to
                    // support, it returns 'null'. So check for it.
                    if (candidate != null && candidate.getName().trim().equals(coordSys.getName())) {
                        epsgCode = seEpsgCode;
                        break;
                    }
                } catch (PeProjectionException pe) {
                    // Strangely SDE includes codes in the projcsCodeList() that
                    // it doesn't actually support.
                    // Catch the exception and skip them here.
                }
            }

            CoordinateReferenceSystem crs;
            if (epsgCode == -1) {
                ArcSDERasterFormat.LOGGER.warning("Couldn't determine EPSG code for this raster."
                        + "  Using SDE's WKT-like coordSysDescription() instead.");
                crs = CRS.parseWKT(seCoordRef.getCoordSysDescription());
            } else {
                crs = CRS.decode("EPSG:" + epsgCode);
            }
            return crs;
        } catch (FactoryException e) {
            ArcSDERasterFormat.LOGGER.log(Level.SEVERE, "", e);
            throw new DataSourceException(e);
        } catch (PeProjectionException e) {
            ArcSDERasterFormat.LOGGER.log(Level.SEVERE, "", e);
            throw new DataSourceException(e);
        }
    }

    public static class QueryInfo {

        private GeneralEnvelope requestedEnvelope;

        private Rectangle requestedDim;

        private int pyramidLevel;

        /**
         * The two-dimensional range of tile indices whose envelope intersect the requested extent.
         * Will have negative width and height if none of the tiles do.
         */
        private Rectangle matchingTiles;

        private GeneralEnvelope resultEnvelope;

        private Rectangle resultDimension;

        private Long rasterId;

        private Rectangle mosaicLocation;

        private RenderedImage resultImage;

        private Rectangle tiledImageSize;

        private double[] resolution;

        private int rasterIndex;

        /**
         * The full tile range for the matching pyramid level
         */
        private Rectangle levelTileRange;

        public QueryInfo() {
            setResultDimensionInsideTiledImage(new Rectangle(0, 0, 0, 0));
            setMatchingTiles(new Rectangle(0, 0, 0, 0));
            setResultEnvelope(null);
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder("[Raster query info:");
            s.append("\n\tRaster ID            : ").append(getRasterId());
            s.append("\n\tPyramid level        : ").append(getPyramidLevel());
            s.append("\n\tResolution           : ").append(
                    getResolution()[0] + "," + getResolution()[1]);
            s.append("\n\tRequested envelope   : ").append(getRequestedEnvelope());
            s.append("\n\tRequested dimension  : ").append(getRequestedDim());
            Rectangle mt = getMatchingTiles();
            Rectangle ltr = getLevelTileRange();
            String matching = "x=" + mt.x + "-" + (mt.width - 1) + ", y=" + mt.y + "-"
                    + (mt.height - 1);
            String level = "x=" + ltr.x + "-" + (ltr.width - 1) + ", y=" + ltr.y + "-"
                    + (ltr.height - 1);
            s.append("\n\tMatching tiles       : ").append(matching).append(" out of ").append(
                    level);
            s.append("\n\tTiled image size     : ").append(getTiledImageSize());
            s.append("\n\tResult dimension     : ").append(getResultDimensionInsideTiledImage());
            s.append("\n\tMosaiced dimension   : ").append(getMosaicLocation());
            s.append("\n\tResult envelope      : ").append(getResultEnvelope());
            s.append("\n]");
            return s.toString();
        }

        /**
         * @return the rasterId (as in SeRaster.getId()) for the raster in the raster dataset this
         *         query works upon
         */
        public Long getRasterId() {
            return rasterId;
        }

        public GeneralEnvelope getRequestedEnvelope() {
            return requestedEnvelope;
        }

        public Rectangle getRequestedDim() {
            return requestedDim;
        }

        public int getPyramidLevel() {
            return pyramidLevel;
        }

        public Rectangle getMatchingTiles() {
            return matchingTiles;
        }

        public GeneralEnvelope getResultEnvelope() {
            return resultEnvelope;
        }

        public Rectangle getResultDimensionInsideTiledImage() {
            return resultDimension;
        }

        void setRasterId(Long rasterId) {
            this.rasterId = rasterId;
        }

        void setPyramidLevel(int pyramidLevel) {
            this.pyramidLevel = pyramidLevel;
        }

        void setRequestedEnvelope(GeneralEnvelope requestedEnvelope) {
            this.requestedEnvelope = requestedEnvelope;
        }

        void setRequestedDim(Rectangle requestedDim) {
            this.requestedDim = requestedDim;
        }

        void setResultEnvelope(GeneralEnvelope resultEnvelope) {
            this.resultEnvelope = resultEnvelope;
        }

        void setMatchingTiles(Rectangle matchingTiles) {
            this.matchingTiles = matchingTiles;
        }

        void setResultDimensionInsideTiledImage(Rectangle resultDimension) {
            this.resultDimension = resultDimension;
        }

        void setMosaicLocation(Rectangle rasterMosaicLocation) {
            this.mosaicLocation = rasterMosaicLocation;
        }

        public Rectangle getMosaicLocation() {
            return mosaicLocation;
        }

        public void setResultImage(RenderedImage rasterImage) {
            this.resultImage = rasterImage;
            if (rasterImage.getWidth() != tiledImageSize.width
                    || rasterImage.getHeight() != tiledImageSize.height) {
                LOGGER.warning("Result image and expected dimensions don't match: image="
                        + resultImage.getWidth() + "x" + resultImage.getHeight() + ", expected="
                        + tiledImageSize.width + "x" + tiledImageSize.height);
            }
        }

        public RenderedImage getResultImage() {
            return resultImage;
        }

        void setTiledImageSize(Rectangle tiledImageSize) {
            this.tiledImageSize = tiledImageSize;
        }

        public Rectangle getTiledImageSize() {
            return tiledImageSize;
        }

        void setResolution(double[] resolution) {
            this.resolution = resolution;
        }

        public double[] getResolution() {
            return resolution == null ? new double[] { -1, -1 } : resolution;
        }

        void setRasterIndex(int rasterN) {
            this.rasterIndex = rasterN;
        }

        public int getRasterIndex() {
            return rasterIndex;
        }

        void setLevelTileRange(Rectangle levelTileRange) {
            this.levelTileRange = levelTileRange;
        }

        public Rectangle getLevelTileRange() {
            return levelTileRange;
        }
    }

    public static MathTransform createRasterToModel(final Rectangle levelGridRange,
            final GeneralEnvelope levelEnvelope) {
        /*
         * GeneralGridRange range's is exclusive for the higher coords, so we contract it by one in
         * both dimensions for the transform to produce matching mosaics
         */
        Rectangle reducedRange = new Rectangle(levelGridRange.x, levelGridRange.y,
                levelGridRange.width - 1, levelGridRange.height - 1);

        // create a raster to model transform, from this tile pixel space to the tile's geographic
        // extent
        GeneralGridRange gridRange = new GeneralGridRange(reducedRange, 2);
        GridToEnvelopeMapper geMapper = new GridToEnvelopeMapper(gridRange, levelEnvelope);
        geMapper.setPixelAnchor(PixelInCell.CELL_CORNER);

        final MathTransform rasterToModel = geMapper.createTransform();
        return rasterToModel;
    }

    private static Rectangle getResultDimensionForTileRange(final Rectangle tiledImageGridRange,
            final Rectangle matchingLevelRange) {

        int minx = Math.max(tiledImageGridRange.x, matchingLevelRange.x);
        int miny = Math.max(tiledImageGridRange.y, matchingLevelRange.y);
        int maxx = (int) Math.min(tiledImageGridRange.getMaxX(), matchingLevelRange.getMaxX());
        int maxy = (int) Math.min(tiledImageGridRange.getMaxY(), matchingLevelRange.getMaxY());

        return new Rectangle(minx, miny, maxx - minx, maxy - miny);
    }

    /**
     * Returns the rectangle specifying the matching tiles for a given pyramid level and rectangle
     * specifying the overlapping area to request in the level's pixel space.
     * 
     * @param pixelRange
     * @param tilesHigh
     * @param tilesWide
     * @param tileSize
     * @param numTilesHigh
     * @param numTilesWide
     * 
     * @param pixelRange
     * @param level
     * 
     * @return a rectangle holding the coordinates in tile space that fully covers the requested
     *         pixel range for the given pyramid level, or a negative area rectangle
     */
    private static Rectangle findMatchingTiles(final Dimension tileSize, int numTilesWide,
            int numTilesHigh, final Rectangle pixelRange) {

        final int minPixelX = pixelRange.x;
        final int minPixelY = pixelRange.y;

        // TODO: WARNING, we're not considering the possible x/y offsets on the level range for the
        // given pyramid level here!

        int minTileX = (int) Math.floor(minPixelX / tileSize.getWidth());
        int minTileY = (int) Math.floor(minPixelY / tileSize.getHeight());

        int numTilesX = (int) Math.ceil(pixelRange.getWidth() / tileSize.getWidth());
        int numTilesY = (int) Math.ceil(pixelRange.getHeight() / tileSize.getHeight());

        int maxTiledX = (minTileX + numTilesX) * tileSize.width;
        int maxTiledY = (minTileY + numTilesY) * tileSize.height;

        if (maxTiledX < pixelRange.getMaxX() && (minTileX + numTilesX) < numTilesWide) {
            numTilesX++;
        }

        if (maxTiledY < pixelRange.getMaxY() && (minTileY + numTilesY) < numTilesHigh) {
            numTilesY++;
        }

        Rectangle matchingTiles = new Rectangle(minTileX, minTileY, numTilesX, numTilesY);
        return matchingTiles;
    }

    private static Rectangle getTargetGridRange(final MathTransform modelToRaster,
            final Envelope requestedEnvelope) {
        Rectangle levelOverlappingPixels;
        int levelMinPixelX;
        int levelMaxPixelX;
        int levelMinPixelY;
        int levelMaxPixelY;
        {
            // use a model to raster transform to find out which pixel range at the specified level
            // better match the requested extent
            GeneralEnvelope requestedPixels;
            try {
                requestedPixels = CRS.transform(modelToRaster, requestedEnvelope);
            } catch (NoninvertibleTransformException e) {
                throw new IllegalArgumentException(e);
            } catch (TransformException e) {
                throw new IllegalArgumentException(e);
            }

            levelMinPixelX = (int) Math.floor(requestedPixels.getMinimum(0));
            levelMaxPixelX = (int) Math.floor(requestedPixels.getMaximum(0));

            levelMinPixelY = (int) Math.ceil(requestedPixels.getMinimum(1));
            levelMaxPixelY = (int) Math.ceil(requestedPixels.getMaximum(1));

            final int width = levelMaxPixelX - levelMinPixelX;
            final int height = levelMaxPixelY - levelMinPixelY;
            levelOverlappingPixels = new Rectangle(levelMinPixelX, levelMinPixelY, width, height);
        }
        return levelOverlappingPixels;
    }

    /**
     * Creates an IndexColorModel out of a DataBuffer obtained from an ArcSDE's raster color map.
     * <p>
     * The resulting IndexColorModel has always four components, whether the original color map has
     * alpha channel or not. In case the original color map has no alpha channel, the fourth
     * component will be all opaque.
     * </p>
     * <p>
     * The no-data value to be used for this color model will always be the last element in the
     * resulting map. If needed, the map's gonna be extended in capacity to make room for the
     * no-data value, as well as the color model's transfer type may be promoted to the next higher
     * one (ie, from 8bit to 16bit). Further processing may take into account this possible
     * difference between the actual data pixel depth and the colormap's one to perform the
     * necessary conversion.
     * </p>
     * 
     * @param colorMapData
     * @return
     */
    public static IndexColorModel sdeColorMapToJavaColorModel(final DataBuffer colorMapData,
            final int bitsPerSample) {
        if (colorMapData == null) {
            throw new NullPointerException("colorMapData");
        }

        if (colorMapData.getNumBanks() < 3 || colorMapData.getNumBanks() > 4) {
            throw new IllegalArgumentException("colorMapData shall have 3 or 4 banks: "
                    + colorMapData.getNumBanks());
        }

        int[] ARGB = null;
        final int numBanks = colorMapData.getNumBanks();
        {
            final int mapSize = colorMapData.getSize();
            ARGB = new int[mapSize];
            int r;
            int g;
            int b;
            int a;
            for (int i = 0; i < mapSize; i++) {
                r = colorMapData.getElem(0, i);
                g = colorMapData.getElem(1, i);
                b = colorMapData.getElem(2, i);
                a = numBanks == 4 ? colorMapData.getElem(3, i) : 255;
                int rgba = ColorUtilities.getIntFromColor(r, g, b, a);
                ARGB[i] = rgba;
            }
        }
        /*
         * Now check if the map need to be expanded and/or the transfer type promoted to the next
         * higher level in order to make room for the no-data value
         */
        int transferType = colorMapData.getDataType();
        int finalBitsPerSample = bitsPerSample;
        {
            int mapSize = ARGB.length;
            switch (bitsPerSample) {
            case 8:
                if (mapSize >= 256) {
                    LOGGER.finer("Promoting transfer type from 8 to 16 bits per sample");
                    transferType = DataBuffer.TYPE_USHORT;
                    finalBitsPerSample = 16;
                    int[] tmp = new int[65536];
                    System.arraycopy(ARGB, 0, tmp, 0, ARGB.length);
                    ARGB = tmp;
                    // HACK set the largest value as the no data value. I need to get rid of this
                    // hack by properly setting a no-data value being just one more index than the
                    // maximum one instead of 65535, but for that we need proper format promotion
                    // with parameterizable no-data value for TileReader
                    int nodataValue = ColorUtilities.getIntFromColor(0, 0, 0, 0);
                    ARGB[ARGB.length - 1] = nodataValue;
                } else {
                    finalBitsPerSample = 8;
                }
                break;
            case 16:
                transferType = DataBuffer.TYPE_USHORT;
                finalBitsPerSample = 16;
                break;
            default:
                throw new IllegalArgumentException("Unknown pixel depth to compute color map: "
                        + bitsPerSample);
            }
        }

        final boolean hasAlpha = true;
        final int transparency = Transparency.TRANSLUCENT;

        IndexColorModel colorModel = new IndexColorModel(finalBitsPerSample, ARGB.length, ARGB, 0,
                hasAlpha, transparency, transferType);

        return colorModel;
    }

    public static ImageTypeSpecifier createFullImageTypeSpecifier(final RasterInfo rasterInfo,
            final int rasterIndex) {
        final int numberOfBands = rasterInfo.getNumBands();
        final RasterCellType pixelType = rasterInfo.getCellType();
        // Prepare temporary colorModel and sample model, needed to build the final
        // ArcSDEPyramidLevel level;
        final ColorModel colorModel;
        final SampleModel sampleModel;
        int sampleImageWidth = 1;// rasterInfo.getImageWidth();
        int sampleImageHeight = 1;// rasterInfo.getImageHeight();
        {
            final int bitsPerSample = pixelType.getBitsPerSample();
            final int dataType = pixelType.getDataBufferType();
            final boolean hasColorMap = rasterInfo.isColorMapped();
            if (hasColorMap) {
                LOGGER.fine("Found single-band colormapped raster, using its index color model");
                colorModel = rasterInfo.getColorMap(rasterIndex);
                sampleModel = colorModel.createCompatibleSampleModel(sampleImageWidth,
                        sampleImageHeight);
            } else if (bitsPerSample == 1 || bitsPerSample == 4) {
                if (numberOfBands != 1) {
                    throw new IllegalArgumentException(bitsPerSample
                            + "-Bit rasters are only supported for one band");
                }
                int[] argb = new int[(int) Math.pow(2, bitsPerSample)];
                ColorUtilities.expand(new Color[] { Color.WHITE, Color.BLACK }, argb, 0,
                        argb.length);
                GridSampleDimension gridSampleDimension = rasterInfo.getGridSampleDimensions()[0];
                colorModel = gridSampleDimension.getColorModel(0, numberOfBands, dataType);
                sampleModel = colorModel.createCompatibleSampleModel(sampleImageWidth,
                        sampleImageHeight);
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
                sampleModel = new BandedSampleModel(dataType, sampleImageWidth, sampleImageHeight,
                        sampleImageWidth, bankIndices, bandOffsets);
            }
        }

        final ImageTypeSpecifier its = new ImageTypeSpecifier(colorModel, sampleModel);
        return its;
    }

    public static ArcSDEGridCoverage2DReaderJAI.ReadParameters parseReadParams(
            final GeneralEnvelope coverageEnvelope, final GeneralParameterValue[] params)
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

                CoordinateReferenceSystem nativeCrs = coverageEnvelope
                        .getCoordinateReferenceSystem();
                CoordinateReferenceSystem requestCrs = reqEnvelope.getCoordinateReferenceSystem();
                if (!CRS.equalsIgnoreMetadata(nativeCrs, requestCrs)) {
                    LOGGER.info("Request CRS and native CRS differ, "
                            + "reprojecting request envelope to native CRS");
                    ReferencedEnvelope nativeCrsEnv;
                    nativeCrsEnv = toNativeCrs(reqEnvelope, nativeCrs);
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

        if (dim == null && reqEnvelope == null) {
            throw new ParameterNotFoundException("Parameter is mandatory and shall provide "
                    + "the extent and dimension to request", AbstractGridFormat.READ_GRIDGEOMETRY2D
                    .getName().toString());
        }

        if (!reqEnvelope.intersects(coverageEnvelope, true)) {
            throw new IllegalArgumentException(
                    "The requested extend does not overlap the coverage extent: "
                            + coverageEnvelope);
        }

        if (dim.width <= 0 || dim.height <= 0) {
            throw new IllegalArgumentException("The requested coverage dimension can't be null: "
                    + dim);
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

        ArcSDEGridCoverage2DReaderJAI.ReadParameters parsedParams = new ArcSDEGridCoverage2DReaderJAI.ReadParameters();
        parsedParams.requestedEnvelope = reqEnvelope;
        parsedParams.dim = dim;
        parsedParams.overviewPolicy = overviewPolicy;
        return parsedParams;
    }

    /**
     * Given a collection of {@link QueryInfo} instances holding information about how a request
     * fits for each individual raster composing a catalog, figure out where their resulting images
     * fit into the overall mosaic that's gonna be the result of the request.
     * 
     * @param rasterInfo
     * @param resultEnvelope
     * @param results
     * @return
     */
    public static Rectangle setMosaicLocations(final RasterInfo rasterInfo,
            final GeneralEnvelope resultEnvelope, final List<QueryInfo> results) {
        final Rectangle mosaicDimension;
        final MathTransform modelToRaster;
        final MathTransform rasterToModel;
        {
            /*
             * Of all the rasters that match the requested envelope, chose the one with the lowest
             * resolution as the base to compute the final mosaic layout, so we avoid JAI upsamples,
             * which are buggy and produce repeated patterns over the x axis instead of just scaling
             * up the image.
             */
            QueryInfo dimensionChoice = findLowestResolution(results);
            Long rasterId = dimensionChoice.getRasterId();
            int pyramidLevel = dimensionChoice.getPyramidLevel();
            int rasterIndex = rasterInfo.getRasterIndex(rasterId);
            Rectangle levelRange = rasterInfo.getGridRange(rasterIndex, pyramidLevel);
            GeneralEnvelope levelEnvelope = rasterInfo.getGridEnvelope(rasterIndex, pyramidLevel);
            rasterToModel = createRasterToModel(levelRange, levelEnvelope);
            try {
                modelToRaster = rasterToModel.inverse();
            } catch (NoninvertibleTransformException e) {
                throw new RuntimeException(e);
            }
            mosaicDimension = getTargetGridRange(modelToRaster, resultEnvelope);
        }

        for (QueryInfo rasterResultInfo : results) {
            final GeneralEnvelope rasterResultEnvelope = rasterResultInfo.getResultEnvelope();

            final Rectangle targetRasterGridRange;
            targetRasterGridRange = getTargetGridRange(modelToRaster, rasterResultEnvelope);

            rasterResultInfo.setMosaicLocation(targetRasterGridRange);
        }

        return mosaicDimension;
    }

    private static QueryInfo findLowestResolution(List<QueryInfo> results) {
        double[] prev = { Double.MIN_VALUE, Double.MIN_VALUE };
        QueryInfo lowestResQuery = null;

        double[] curr;
        for (QueryInfo query : results) {
            curr = query.getResolution();
            if (curr[0] > prev[0]) {
                prev = curr;
                lowestResQuery = query;
            }
        }
        return lowestResQuery;
    }

    /**
     * Find out the raster ids and their pyramid levels in the raster dataset for the rasters whose
     * envelope overlaps the requested one
     * 
     * @param rasterInfo
     * @param requestedEnvelope
     * @param requestedDim
     * @param overviewPolicy
     * @return
     */
    public static List<QueryInfo> findMatchingRasters(final RasterInfo rasterInfo,
            final GeneralEnvelope requestedEnvelope, final Rectangle requestedDim,
            final OverviewPolicy overviewPolicy) {

        final int numRasters = rasterInfo.getNumRasters();
        List<QueryInfo> matchingRasters = new ArrayList<QueryInfo>(numRasters);

        int optimalPyramidLevel;
        GeneralEnvelope gridEnvelope;
        for (int rasterN = 0; rasterN < numRasters; rasterN++) {
            optimalPyramidLevel = rasterInfo.getOptimalPyramidLevel(rasterN, overviewPolicy,
                    requestedEnvelope, requestedDim);
            gridEnvelope = rasterInfo.getGridEnvelope(rasterN, optimalPyramidLevel);
            final boolean edgesInclusive = true;
            if (requestedEnvelope.intersects(gridEnvelope, edgesInclusive)) {
                QueryInfo match = new QueryInfo();
                match.setRequestedEnvelope(requestedEnvelope);
                match.setRequestedDim(requestedDim);

                match.setRasterId(rasterInfo.getRasterId(rasterN));
                match.setRasterIndex(rasterN);
                match.setPyramidLevel(optimalPyramidLevel);
                match.setResolution(rasterInfo.getResolution(rasterN, optimalPyramidLevel));
                matchingRasters.add(match);
            }
        }
        return matchingRasters;
    }

    public static void fitRequestToRaster(final GeneralEnvelope requestedEnvelope,
            final RasterInfo rasterInfo, final QueryInfo query) {

        final int rasterIndex = query.getRasterIndex();
        final int pyramidLevel = query.getPyramidLevel();
        final Rectangle rasterGridRange = rasterInfo.getGridRange(rasterIndex, pyramidLevel);
        final GeneralEnvelope rasterEnvelope = rasterInfo
                .getGridEnvelope(rasterIndex, pyramidLevel);
        final MathTransform rasterToModel = createRasterToModel(rasterGridRange, rasterEnvelope);
        final MathTransform modelToRaster;
        try {
            modelToRaster = rasterToModel.inverse();
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e);
        }
        final Rectangle resultGridRange;
        final GeneralEnvelope resultEnvelope;
        try {
            int minx;
            int miny;
            int maxx;
            int maxy;
            {
                GeneralEnvelope requestedGridRange = CRS
                        .transform(modelToRaster, requestedEnvelope);
                minx = (int) Math.floor(requestedGridRange.getMinimum(0));
                miny = (int) Math.floor(requestedGridRange.getMinimum(1));
                maxx = (int) Math.ceil(requestedGridRange.getMaximum(0));
                maxy = (int) Math.ceil(requestedGridRange.getMaximum(1));
                /*
                 * expand requested grid range by two pixels to the four directions to give the
                 * mosaic more chances to overlap
                 */
                minx -= 2;
                miny -= 2;
                maxx += 2;
                maxy += 2;
            }

            minx = (int) Math.max(Math.floor(rasterGridRange.getMinX()), minx);
            miny = (int) Math.max(Math.floor(rasterGridRange.getMinY()), miny);
            maxx = (int) Math.min(Math.ceil(rasterGridRange.getMaxX()), maxx);
            maxy = (int) Math.min(Math.ceil(rasterGridRange.getMaxY()), maxy);

            resultGridRange = new Rectangle(minx, miny, maxx - minx, maxy - miny);
            resultEnvelope = CRS.transform(rasterToModel, new GeneralEnvelope(resultGridRange));
            resultEnvelope.setCoordinateReferenceSystem(requestedEnvelope
                    .getCoordinateReferenceSystem());
        } catch (TransformException e) {
            throw new RuntimeException(e);
        }

        final Rectangle matchingTiles;
        final Rectangle levelTileRange;
        final Rectangle tiledImageGridRange;
        {
            final Dimension tileSize = rasterInfo.getTileDimension(rasterIndex);
            final int numTilesWide = rasterInfo.getNumTilesWide(rasterIndex, pyramidLevel);
            final int numTilesHigh = rasterInfo.getNumTilesHigh(rasterIndex, pyramidLevel);
            final Point tileOffset = rasterInfo.getTileOffset(rasterIndex, pyramidLevel);
            levelTileRange = new Rectangle(0, 0, numTilesWide, numTilesHigh);
            matchingTiles = findMatchingTiles(tileSize, numTilesWide, numTilesHigh, resultGridRange);

            int tiledImageMinX = (matchingTiles.x * tileSize.width);
            int tiledImageMinY = (matchingTiles.y * tileSize.height);

            int tiledWidth = (matchingTiles.width * tileSize.width);
            int tiledHeight = (matchingTiles.height * tileSize.height);

            tiledImageGridRange = new Rectangle(tiledImageMinX, tiledImageMinY, tiledWidth,
                    tiledHeight);
        }

        /*
         * What is the grid range inside the whole level grid range that fits into the matching
         * tiles
         */
        Rectangle resultDimensionInsideTiledImage;
        resultDimensionInsideTiledImage = getResultDimensionForTileRange(tiledImageGridRange,
                resultGridRange);

        query.setResultEnvelope(resultEnvelope);
        query.setResultDimensionInsideTiledImage(resultDimensionInsideTiledImage);
        query.setTiledImageSize(tiledImageGridRange);
        query.setLevelTileRange(levelTileRange);
        query.setMatchingTiles(matchingTiles);
    }
}
