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


import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.gce.imageio.ArcSDEPyramid;
import org.geotools.arcsde.gce.imageio.ArcSDEPyramidLevel;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.data.DataSourceException;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
import org.geotools.resources.image.ColorUtilities;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.grid.GridCoverage;
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

/**
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.5.4
 * @version $Id: RasterUtils.java 32460 2009-02-10 05:23:31Z groldan $
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

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder("[Raster query info:");
            s.append("\n\tpyramid level        : ").append(pyramidLevel);
            s.append("\n\trequested envelope   : ").append(requestedEnvelope);
            s.append("\n\trequested dimension  : ").append(requestedDim);
            s.append("\n\tmatching tiles       : ").append(matchingTiles);
            s.append("\n\tresult envelope      : ").append(resultEnvelope);
            s.append("\n\tresult dimension     : ").append(resultDimension);
            s.append("\n]");
            return s.toString();
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

        public Rectangle getResultDimension() {
            return resultDimension;
        }
    }

    public static QueryInfo fitRequestToRaster(final GeneralEnvelope requestedEnvelope,
            final Rectangle requestedDim, final ArcSDEPyramid pyramidInfo, final int pyramidLevel) {

        QueryInfo queryInfo = new QueryInfo();
        queryInfo.requestedEnvelope = requestedEnvelope;
        queryInfo.requestedDim = requestedDim;
        queryInfo.pyramidLevel = pyramidLevel;

        calculateQueryDimensionAndEnvelope(queryInfo, pyramidInfo);

        return queryInfo;
    }

    private static void calculateQueryDimensionAndEnvelope(final QueryInfo queryInfo,
            final ArcSDEPyramid pyramid) {

        // final Rectangle matchingTiles = queryInfo.matchingTiles;
        final int pyramidLevel = queryInfo.pyramidLevel;
        final GeneralEnvelope requestedEnvelope = queryInfo.requestedEnvelope;
        final ArcSDEPyramidLevel level = pyramid.getPyramidLevel(pyramidLevel);
        final ReferencedEnvelope levelEnvelope = level.getEnvelope();

        final int offsetX = level.getXOffset();
        final int offsetY = level.getYOffset();

        // get the range of this pyramid level in pixel space
        final Rectangle levelRange = new Rectangle(offsetX, offsetY, level.size.width,
                level.size.height);

        // create a raster to model transform, from this tile pixel space to the tile's geographic
        // extent
        GridToEnvelopeMapper geMapper = new GridToEnvelopeMapper(new GeneralGridRange(levelRange),
                new GeneralEnvelope(levelEnvelope));
        geMapper.setPixelAnchor(PixelInCell.CELL_CORNER);

        final MathTransform rasterToModel = geMapper.createTransform();

        int levelMinPixelX;
        int levelMaxPixelX;
        int levelMinPixelY;
        int levelMaxPixelY;
        try {
            // use a model to raster transform to find out which pixel range at the specified level
            // better match the requested extent
            MathTransform modelToRaster = rasterToModel.inverse();
            GeneralEnvelope requestedPixels;
            requestedPixels = CRS.transform(modelToRaster, requestedEnvelope);

            levelMinPixelX = (int) Math.floor(requestedPixels.getMinimum(0));
            levelMaxPixelX = (int) Math.ceil(requestedPixels.getMaximum(0));

            levelMinPixelY = (int) Math.floor(requestedPixels.getMinimum(1));
            levelMaxPixelY = (int) Math.ceil(requestedPixels.getMaximum(1));

        } catch (NoninvertibleTransformException e) {
            throw new IllegalArgumentException(e);
        } catch (TransformException e) {
            throw new IllegalArgumentException(e);
        }

        // adapt the requested pixel extent to what the tile level can serve
        levelMinPixelX = Math.max(levelMinPixelX, levelRange.x);
        levelMinPixelY = Math.max(levelMinPixelY, levelRange.y);

        levelMaxPixelX = Math.min(levelMaxPixelX, levelRange.x + levelRange.width);
        levelMaxPixelY = Math.min(levelMaxPixelY, levelRange.y + levelRange.height);

        // obtain the resulting geographical extent for the final pixels to query at this pyramid
        // level
        GeneralEnvelope resultEnvelope;
        try {
            resultEnvelope = CRS.transform(rasterToModel, new ReferencedEnvelope(levelMinPixelX,
                    levelMaxPixelX, levelMinPixelY, levelMaxPixelY, requestedEnvelope
                            .getCoordinateReferenceSystem()));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        resultEnvelope.setCoordinateReferenceSystem(requestedEnvelope
                .getCoordinateReferenceSystem());

        queryInfo.resultEnvelope = resultEnvelope;

        // finally, figure out which tile range (int tile space) fit the required pixel range
        final int tileWidth = pyramid.getTileWidth();
        final int tileHeight = pyramid.getTileHeight();
        final int numTilesWide = level.getNumTilesWide();
        final int numTilesHigh = level.getNumTilesHigh();

        int minTileX = Integer.MIN_VALUE;
        int minTileY = Integer.MIN_VALUE;
        int maxTileX = Integer.MAX_VALUE;
        int maxTileY = Integer.MAX_VALUE;
        for (int tileX = 0; tileX < numTilesWide; tileX++) {
            int tileMinX = tileX * tileWidth;
            int tileMaxX = tileMinX + tileWidth;
            if (tileMinX <= levelMinPixelX) {
                minTileX = Math.max(minTileX, tileX);
            }
            if (tileMaxX >= levelMaxPixelX) {
                maxTileX = Math.min(maxTileX, tileX);
            }
        }
        for (int tileY = 0; tileY < numTilesHigh; tileY++) {
            int tileMinY = tileY * tileHeight;
            int tileMaxY = tileMinY + tileHeight;
            if (tileMinY <= levelMinPixelY) {
                minTileY = Math.max(minTileY, tileY);
            }
            if (tileMaxY >= levelMaxPixelY) {
                maxTileY = Math.min(maxTileY, tileY);
            }
        }

        final Rectangle requiredTiles = new Rectangle(minTileX, minTileY, maxTileX - minTileX,
                maxTileY - minTileY);
        queryInfo.matchingTiles = requiredTiles;

        /*
         * the subset of pixels from the resulting image given by the full matching tiles that are
         * the actual dimensions of the resulting image
         */
        int minCropX = levelMinPixelX - (minTileX * tileWidth);
        int minCropY = levelMinPixelY - (minTileY * tileWidth);
        int cropWidth = levelMaxPixelX - levelMinPixelX;
        int cropHeight = levelMaxPixelY - levelMinPixelY;
        final Rectangle cropTo = new Rectangle(minCropX, minCropY, cropWidth, cropHeight);

        queryInfo.resultDimension = cropTo;
    }

    public static IndexColorModel sdeColorMapToJavaColorModel(final int bitsPerPixel,
            final DataBuffer colorMapData) {
        if (colorMapData == null) {
            throw new NullPointerException("colorMapData");
        }

        if (colorMapData.getNumBanks() < 3 || colorMapData.getNumBanks() > 4) {
            throw new IllegalArgumentException("colorMapData shall have 3 or 4 banks: "
                    + colorMapData.getNumBanks());
        }

        final int numBanks = colorMapData.getNumBanks();
        final int mapSize = colorMapData.getSize();

        int[] ARGB = new int[mapSize];
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

        IndexColorModel colorModel = ColorUtilities.getIndexColorModel(ARGB);

        return colorModel;
    }
}
