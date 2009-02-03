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

import static org.geotools.arcsde.gce.imageio.RasterCellType.TYPE_16BIT_S;
import static org.geotools.arcsde.gce.imageio.RasterCellType.TYPE_16BIT_U;
import static org.geotools.arcsde.gce.imageio.RasterCellType.TYPE_1BIT;
import static org.geotools.arcsde.gce.imageio.RasterCellType.TYPE_8BIT_S;
import static org.geotools.arcsde.gce.imageio.RasterCellType.TYPE_8BIT_U;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageTypeSpecifier;
import javax.media.jai.ImageLayout;

import org.geotools.arcsde.gce.imageio.ArcSDEPyramid;
import org.geotools.arcsde.gce.imageio.ArcSDERasterImageReadParam;
import org.geotools.arcsde.gce.imageio.ArcSDERasterReader;
import org.geotools.arcsde.gce.imageio.RasterCellType;
import org.geotools.arcsde.gce.imageio.ArcSDEPyramid.RasterQueryInfo;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.data.DataSourceException;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.resources.image.ComponentColorModelJAI;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeRasterBand.SeRasterBandColorMap;
import com.esri.sde.sdk.pe.PeCoordinateSystem;
import com.esri.sde.sdk.pe.PeFactory;
import com.esri.sde.sdk.pe.PeGeographicCS;
import com.esri.sde.sdk.pe.PeProjectedCS;
import com.esri.sde.sdk.pe.PeProjectionException;

/**
 * 
 * @author Gabriel Roldan
 */
public class RasterUtils {

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
            final CoordinateReferenceSystem nativeCRS) throws DataSourceException {

        ReferencedEnvelope reqEnv = toReferencedEnvelope(requestedEnvelope);

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
        return reqEnv;
    }

    public static BufferedImage createInitialBufferedImage(final BufferedImage prototype,
            final int width, final int height) throws DataSourceException {

        final WritableRaster rasterPrototype = prototype.getRaster();
        final WritableRaster newras = rasterPrototype.createCompatibleWritableRaster(width, height);
        final BufferedImage ret = new BufferedImage(prototype.getColorModel(), newras, prototype
                .isAlphaPremultiplied(), null);
        // By default BufferedImages are created with all banks set to zero.
        // That's an all-black, transparent image.
        // Transparency is handled in the ArcSDERasterBandCopier. Blackness
        // isn't. Let's fix that and set
        // the image to white.
        final int transparentWhite = 0x00ffffff;
        int[] pixels = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            pixels[i] = transparentWhite;
        }
        ret.setRGB(0, 0, width, height, pixels, 0, 1);

        return ret;
    }

    public static ArcSDERasterImageReadParam createImageReadParam(final int pyramidLevel,
            final ArcSDEPyramid pyramidInfo, final ReferencedEnvelope reqEnv,
            final ArcSDEPooledConnection scon, final List<RasterBandInfo> seBands,
            final Point levelZeroPRP, final ArcSDERasterReader imageIOReader,
            BufferedImage sampleImage) throws NegativelyIndexedTileException, DataSourceException {

        // ok, there's actually something to render. Render it.
        final RasterQueryInfo rasterGridInfo;
        rasterGridInfo = pyramidInfo.fitExtentToRasterPixelGrid(reqEnv, pyramidLevel);

        final BufferedImage actualOutputImage = createInitialBufferedImage(sampleImage,
                rasterGridInfo.image.width, rasterGridInfo.image.height);

        final Rectangle sourceRegion = calculateSourceRegion(pyramidLevel, pyramidInfo,
                levelZeroPRP, rasterGridInfo);

        final BufferedImage outputImage = getDrawingImage(rasterGridInfo, actualOutputImage);

        final GeneralEnvelope outputImageEnvelope = new GeneralEnvelope(rasterGridInfo.envelope);

        // not quite sure how, but I figure one could request a subset
        // of all available bands...
        // for now we'll just grab the first three, and assume they're
        // RGB in order.
        int[] bands = new int[Math.min(3, seBands.size())];
        Map<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();
        for (int bandIndex = 0; bandIndex < bands.length; bandIndex++) {
            bands[bandIndex] = bandIndex + 1;
            bandMapper.put(new Integer((int) seBands.get(bandIndex).getBandId()), new Integer(
                    bandIndex));
        }

        ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
        rParam.setConnection(scon);
        rParam.setActualDestination(actualOutputImage);
        rParam.setSourceRegion(sourceRegion);
        rParam.setDestination(outputImage);
        rParam.setOutputImageEnvelope(outputImageEnvelope);
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
        pb.add(new Integer(pyramidLevel));
        pb.add(Boolean.FALSE);
        pb.add(Boolean.FALSE);
        pb.add(Boolean.FALSE);
        pb.add(null);
        pb.add(null);
        pb.add(rParam);
        pb.add(imageIOReader);
        return rParam;
    }

    /**
     * Returns the image where to actually perform the painting.
     * <p>
     * May or may not match the {@code actualOutputImage}. If not a subImage sharing the internal
     * buffer is returned.
     * </p>
     * 
     * @param rasterGridInfo
     * @param actualOutputImage
     *            the full sized image
     * @return possibly a subimage of actualOutputImage if needs to be shifted to accomodate to the
     *         raster grid info
     */
    private static BufferedImage getDrawingImage(final RasterQueryInfo rasterGridInfo,
            final BufferedImage actualOutputImage) {
        final BufferedImage outputImage;
        if (rasterGridInfo.image.x < 0 || rasterGridInfo.image.y < 0) {
            Point destOffset = new Point(0, 0);
            if (rasterGridInfo.image.x < 0) {
                destOffset.x = rasterGridInfo.image.x * -1;
            }
            if (rasterGridInfo.image.y < 0) {
                destOffset.y = rasterGridInfo.image.y * -1;
            }
            outputImage = actualOutputImage.getSubimage(destOffset.x, destOffset.y,
                    actualOutputImage.getWidth() - destOffset.x, actualOutputImage.getHeight()
                            - destOffset.y);

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("source region is offset by " + destOffset + " into the "
                        + actualOutputImage.getWidth() + "x" + actualOutputImage.getHeight()
                        + " output image.");
            }
        } else {
            outputImage = actualOutputImage;
        }
        return outputImage;
    }

    private static Rectangle calculateSourceRegion(final int pyramidLevel,
            final ArcSDEPyramid pyramidInfo, final Point levelZeroPRP,
            final RasterQueryInfo rasterGridInfo) throws NegativelyIndexedTileException {
        final int minImageX = Math.max(rasterGridInfo.image.x, 0);
        final int maxImageX = Math.min(rasterGridInfo.image.x + rasterGridInfo.image.width,
                pyramidInfo.getPyramidLevel(pyramidLevel).size.width);
        int minImageY = Math.max(rasterGridInfo.image.y, 0);
        int maxImageY = Math.min(rasterGridInfo.image.y + rasterGridInfo.image.height, pyramidInfo
                .getPyramidLevel(pyramidLevel).size.height);

        Rectangle sourceRegion = new Rectangle(minImageX, minImageY, maxImageX - minImageX,
                maxImageY - minImageY);
        // check for inaccessible negative-indexed level-zero tiles.
        // Shift to level 1 if necessary.
        if (pyramidLevel == 0 && levelZeroPRP != null) {
            if ((maxImageY > levelZeroPRP.y && minImageY < levelZeroPRP.y)
                    || (maxImageX > levelZeroPRP.x && minImageX < levelZeroPRP.x)) {
                throw new NegativelyIndexedTileException("");
            } else if (maxImageY > levelZeroPRP.y && maxImageX > levelZeroPRP.x) {
                // we're on the south side of the PRP...need to shift
                // everything up
                sourceRegion.translate(levelZeroPRP.x * -1, levelZeroPRP.y * -1);
            } else {
                // all the data we want is negatively indexed on one axis or another. Since
                // we can't get at it, we'll have to shift up to level 1;
                throw new NegativelyIndexedTileException("");
            }
        }

        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("Expanded request to cover source region [" + sourceRegion + "] in level "
                    + pyramidLevel + ".  Spatial extent of this source region is "
                    + rasterGridInfo.envelope);
        return sourceRegion;
    }

    /**
     * Gets the coordinate system that will be associated to the {@link GridCoverage}.
     * 
     * @param rasterAttributes
     */
    public static CoordinateReferenceSystem findCompatibleCRS(final SeCoordinateReference seCoordRef)
            throws DataSourceException {

        try {
            final PeCoordinateSystem coordSys = seCoordRef.getCoordSys();

            int epsgCode = -1;
            final int[] seEpsgCodes;
            if (coordSys instanceof PeGeographicCS) {
                seEpsgCodes = PeFactory.geogcsCodelist();
            } else if (coordSys instanceof PeProjectedCS) {
                seEpsgCodes = PeFactory.projcsCodelist();
            } else {
                throw new RuntimeException("Shouldnt happen!: Unnkown SeCoordSys type");
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

    /**
     * Create a one-band {@link BufferedImage} compatible with the {@code cellType} and
     * IndexColorModel, if any.
     * 
     * @param width
     * @param height
     * @param cellType
     * @param colorMap
     *            if non-null, the color model for the buffered image
     * @return
     * @throws DataSourceException
     */
    public static BufferedImage createSingleBandCompatibleImage(final int width, final int height,
            final RasterCellType cellType, IndexColorModel colorMap) throws DataSourceException {

        final BufferedImage compatibleImage;
        final boolean hasColorMap = colorMap != null;
        LOGGER.fine("creating compatible image with single-band " + cellType + " image with "
                + (hasColorMap ? "" : "NO") + " Color Map");

        if (hasColorMap) {
            compatibleImage = createColorMappedImage(width, height, cellType, colorMap);
        } else if (cellType == TYPE_1BIT) {
            compatibleImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        } else if (cellType == TYPE_8BIT_U) {
            compatibleImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        } else {
            compatibleImage = createSingleBandImageWithCustomColorModel(width, height, cellType);
        }
        return compatibleImage;
    }

    private static BufferedImage createColorMappedImage(final int width, final int height,
            final RasterCellType cellType, final IndexColorModel colorMap) {
        final boolean is8Bit = cellType == TYPE_8BIT_U || cellType == TYPE_8BIT_S;
        final boolean is16Bit = cellType == TYPE_16BIT_S || cellType == TYPE_16BIT_U;
        final BufferedImage compatibleImage;
        if (is8Bit) {
            // Hold on adding 8-bit colormapped support until we figure out the
            // deadlock inside SeRasterBand.getColorMap()
            if (true) {
                throw new IllegalArgumentException(
                        "8-bit colormapped raster layers are not supported");
            }
            LOGGER.fine("Discovered 8-bit single-band raster with colormap. "
                    + " Using return image type: TYPE_BYTE_INDEX");
            // cache the colormodel
            compatibleImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED,
                    colorMap);

        } else if (is16Bit) {
            throw new IllegalArgumentException(
                    "One-band 16-bit rasters with color map are not yet supported");
        } else {
            throw new IllegalArgumentException("Color mapped images with pixel type " + cellType
                    + " are not supported (nor allowed by ArcSDE!!)");
        }
        return compatibleImage;
    }

    private static BufferedImage createSingleBandImageWithCustomColorModel(final int width,
            final int height, final RasterCellType cellType) {
        final int dataType;
        final DataBuffer dataBuffer;
        switch (cellType) {
        case TYPE_16BIT_U:
            dataType = DataBuffer.TYPE_USHORT;
            dataBuffer = new DataBufferUShort(width * height);
            break;
        case TYPE_16BIT_S:
            dataType = DataBuffer.TYPE_SHORT;
            dataBuffer = new DataBufferShort(width * height);
            break;
        case TYPE_32BIT_REAL:
            dataType = DataBuffer.TYPE_FLOAT;
            dataBuffer = new DataBufferFloat(width * height);
            break;
        default:
            throw new IllegalArgumentException(
                    "Don't know how to create a single-band image for pixel type " + cellType);
        }
        final WritableRaster raster;
        final ColorModel colorModel;
        {
            final int pixelStride = 1;
            final int scanLineStride = width;
            final int[] bandOffsets = new int[] { 0 };
            final SampleModel sampleModel = new ComponentSampleModel(dataType, width, height,
                    pixelStride, scanLineStride, bandOffsets);
            raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);
        }
        {
            final ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            final boolean hasAlpha = false;
            final boolean isAlphaPremultiplied = true;
            colorModel = new ComponentColorModel(colorSpace, hasAlpha, isAlphaPremultiplied,
                    Transparency.OPAQUE, dataType);
        }

        final boolean isRasterPremultiplied = false;
        BufferedImage compatibleImage = new BufferedImage(colorModel, raster,
                isRasterPremultiplied, null);
        return compatibleImage;
    }

    private static void initalize(BufferedImage img) {
        WritableRaster raster = img.getRaster();
        SampleModel sampleModel = raster.getSampleModel();
    }

    /**
     * @param width
     * @param height
     * @param numBands
     * @param cellType
     * @param colorMap
     * @return
     * @throws DataSourceException
     */
    public static BufferedImage createCompatibleBufferedImage(final int width, final int height,
            final int numBands, final RasterCellType cellType, final IndexColorModel colorMap)
            throws DataSourceException {
        if (numBands == 1) {
            return createSingleBandCompatibleImage(width, height, cellType, colorMap);
        }

        if (numBands == 3 || numBands == 4) {
            if (cellType != TYPE_8BIT_U) {
                throw new IllegalArgumentException("3 or 4 band rasters are only supported"
                        + " if they have pixel type 8-bit unsigned pixels.");
            }
            LOGGER.fine("Three or four banded non-colormapped raster detected.  Assuming "
                    + "bands 1,2 and 3 constitue a 3-band RGB image.  Using return "
                    + "image type: TYPE_INT_ARGB (alpha will be used to support "
                    + "no-data pixels)");
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        } else {
            StringBuffer errmsg = new StringBuffer();
            errmsg.append("ArcSDERasterReader doesn't support ");
            errmsg.append(numBands);
            errmsg.append("-banded images of type ");
            errmsg.append(cellType);
            throw new IllegalArgumentException(errmsg.toString());
        }
    }

    public static IndexColorModel sdeColorMapToJavaColorModel(SeRasterBandColorMap sdeColorMap) {
        // TODO implement sdeColorMapToJavaColorModel
        IndexColorModel ret = new IndexColorModel(8, 3, new byte[] { 0x0 }, new byte[] { 0x0 },
                new byte[] { 0x0 });
        return ret;
    }

    /**
     * 
     * @param pixelType
     * @param numberOfBands
     *            the total number of bands in the raster, regardless of which one(s) to make
     *            visible
     * @return
     */
    public static ImageTypeSpecifier createImageTypeSpec(final RasterCellType pixelType,
            final int numberOfBands, final int width, final int height) {

        final ImageTypeSpecifier its;

        ColorModel colorModel;
        SampleModel sampleModel;

        final int[] bandOffsets;
        final int transferType = pixelType.getDataBufferType();
        {
            final ColorSpace colorSpace;
            final boolean isAlphaPremultiplied = false;
            // final SampleModel sampleModel = colorModel.createCompatibleSampleModel(width,
            // height);
            switch (numberOfBands) {
            case 3:
                colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
                bandOffsets = new int[] { 0, 1, 2 };
                break;
            default:
                colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
                bandOffsets = new int[]{0};
                break;
            }

            final boolean hasAlpha = false;
            final int transparencyKey = Transparency.OPAQUE;
            // if (numBands > 3) {
            // transparencyKey = Transparency.TRANSLUCENT;
            // hasAlpha = true;
            // }
            colorModel = new ComponentColorModelJAI(colorSpace, hasAlpha, isAlphaPremultiplied,
                    transparencyKey, transferType);
        }

        final int pixelStride = 1;
        final int scanLineStride = width;

        sampleModel = new ComponentSampleModel(transferType, width, height, pixelStride,
                scanLineStride, bandOffsets);

        its = new ImageTypeSpecifier(colorModel, sampleModel);
        return its;
    }

}
