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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.geotools.data.DataSourceException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SDEPoint;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeRasterAttr;

/**
 * This class represents an ArcSDE Raster Pyramid. Basically, it wraps the SeRasterAttr object and
 * implements some convenience methods for doing calculations with it.
 * 
 * @author Saul Farber
 * 
 */
public class ArcSDEPyramid {

    ArrayList<ArcSDEPyramidLevel> pyramidList;

    int tileWidth, tileHeight;

    /**
     * Creates an in-memory representation of an ArcSDE Raster Pyramid. Basically it wraps the
     * supplide SeRasterAttr object and implements some convenience logic for extracting
     * information/ doing calculations with it.
     * 
     * @param rasterAttributes
     *            the SeRasterAttr object for the raster of interest.
     * @param crs
     * @throws DataSourceException
     */
    public ArcSDEPyramid(SeRasterAttr rasterAttributes, CoordinateReferenceSystem crs)
            throws DataSourceException {
        try {
            final int numLevels = rasterAttributes.getMaxLevel() + 1;
            pyramidList = new ArrayList<ArcSDEPyramidLevel>(numLevels);

            tileWidth = rasterAttributes.getTileWidth();
            tileHeight = rasterAttributes.getTileHeight();

            for (int i = 0; i < numLevels; i++) {
                if (i == 1 && rasterAttributes.skipLevelOne()) {
                    continue;
                }

                ReferencedEnvelope levelExtent = new ReferencedEnvelope(crs);
                SeExtent slExtent = rasterAttributes.getExtentByLevel(i);
                levelExtent.expandToInclude(slExtent.getMinX(), slExtent.getMinY());
                levelExtent.expandToInclude(slExtent.getMaxX(), slExtent.getMaxY());

                final int imageWidth = rasterAttributes.getImageWidthByLevel(i);
                final int imageHeight = rasterAttributes.getImageHeightByLevel(i);

                Dimension size = new Dimension(imageWidth, imageHeight);

                addPyramidLevel(i, rasterAttributes.getExtentByLevel(i), crs, rasterAttributes
                        .getImageOffsetByLevel(i), rasterAttributes.getTilesPerRowByLevel(i),
                        rasterAttributes.getTilesPerColByLevel(i), size);
            }

        } catch (SeException se) {
            throw new DataSourceException(se);
        }

    }

    public Dimension getTileDimension() {
        return new Dimension(tileWidth, tileHeight);
    }

    public ArcSDEPyramidLevel getPyramidLevel(int level) {
        return pyramidList.get(level);
    }

    public int getNumLevels() {
        return pyramidList.size();
    }

    /**
     * Given this raster's pyramid info this method picks the optimal pyramid level for rendering
     * this request.
     * 
     * @param requestEnvelope
     *            The requested geographical extent
     * @param pixelDimensions
     *            The request pixel size of the image
     * @return the integer number of the raster level most appropriate for this request.
     */
    public int pickOptimalRasterLevel(ReferencedEnvelope requestEnvelope, Rectangle pixelDimensions)
            throws DataSourceException {

        double reqXRes = requestEnvelope.getWidth() / pixelDimensions.width;
        double reqYRes = requestEnvelope.getHeight() / pixelDimensions.height;

        ArcSDEPyramidLevel[] pyramidInfo = pyramidList.toArray(new ArcSDEPyramidLevel[pyramidList
                .size()]);

        int targetLevel = 0;
        for (int i = 0; i < pyramidInfo.length; i++) {
            if (reqXRes >= pyramidInfo[i].getXRes() && reqYRes >= pyramidInfo[i].getYRes()) {
                targetLevel = i;
            } else {
                break;
            }
        }

        return targetLevel;
    }

    /**
     * Given a requested envelope and a chosen raster level, figure out and return the actual SDE
     * raster tiles, image size and the exact envelope of that image.
     * 
     * @param reqEnv
     *            The original requested envelope.
     * @param rasterLvl
     *            The chosen pyramid level at which to best-fit the requsted envelope.
     * @return
     */
    public RasterQueryInfo fitExtentToRasterPixelGrid(ReferencedEnvelope reqEnv, int rasterLvl) {
        final RasterQueryInfo ret = new RasterQueryInfo();
        final ArcSDEPyramidLevel pLevel = getPyramidLevel(rasterLvl);

        double delta = reqEnv.getMinX() - pLevel.getEnvelope().getMinX();
        final int xMinPixel = (int) Math.floor(delta / pLevel.getXRes());

        delta = reqEnv.getMaxX() - pLevel.getEnvelope().getMinX();
        final int xMaxPixel = (int) Math.ceil(delta / pLevel.getXRes());

        delta = pLevel.getEnvelope().getMaxY() - reqEnv.getMaxY();
        // Distance in pixels from the top of the whole pyramid image to the top
        // of our AOI.
        // If we're off the top, this number will be negative.
        final int yMinPixel = (int) Math.floor(delta / pLevel.getYRes());

        delta = pLevel.getEnvelope().getMaxY() - reqEnv.getMinY();
        final int yMaxPixel = (int) Math.ceil(delta / pLevel.getYRes());

        final int widthPixel = xMaxPixel - xMinPixel;
        final int heightPixel = yMaxPixel - yMinPixel;

        final double xMinGeo = pLevel.getEnvelope().getMinX() + pLevel.getXRes() * xMinPixel;
        final double yMinGeo = pLevel.getEnvelope().getMaxY() - pLevel.getYRes()
                * (yMinPixel + heightPixel);
        final double widthGeo = pLevel.getXRes() * widthPixel;
        final double heightGeo = pLevel.getYRes() * heightPixel;

        ret.envelope = new ReferencedEnvelope(xMinGeo, xMinGeo + widthGeo, yMinGeo, yMinGeo
                + heightGeo, reqEnv.getCoordinateReferenceSystem());
        ret.image = new Rectangle(xMinPixel, yMinPixel, widthPixel, heightPixel);

        return ret;
    }

    /**
     * Don't use this method. It's only public for unit testing purposes.
     * 
     * @param level
     *            DON'T USE
     * @param extent
     *            DON'T USE
     * @param crs
     *            DON'T USE
     * @param offset
     *            DON'T USE
     * @param xTiles
     *            DON'T USE
     * @param yTiles
     *            DON'T USE
     * @param imageSize
     *            DON'T USE
     */
    public void addPyramidLevel(int level, SeExtent extent, CoordinateReferenceSystem crs,
            SDEPoint offset, int xTiles, int yTiles, Dimension imageSize) {

        pyramidList.add(level, new ArcSDEPyramidLevel(level, extent, crs, offset, xTiles, yTiles,
                imageSize));

        Collections.sort(pyramidList, new Comparator() {
            public int compare(Object arg0, Object arg1) {
                ArcSDEPyramidLevel p0, p1;
                p0 = (ArcSDEPyramidLevel) arg0;
                p1 = (ArcSDEPyramidLevel) arg1;
                return (p0.getLevel() - p1.getLevel());

            }
        });
    }

    /**
     * Don't use this constructor. It only exists for unit testing purposes.
     * 
     * @param tileWidth
     *            DON'T USE
     * @param tileHeight
     *            DON'T USE
     * @param numLayers
     *            DON'T USE
     */
    public ArcSDEPyramid(int tileWidth, int tileHeight, int numLayers) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        pyramidList = new ArrayList(numLayers);
    }

}

class RasterQueryInfo {

    public Rectangle image;

    public ReferencedEnvelope envelope;

}