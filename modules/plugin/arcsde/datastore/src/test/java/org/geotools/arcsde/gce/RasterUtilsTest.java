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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeObjectId;

public class RasterUtilsTest {

    @Test
    public void testFindCompatibleCRS_Projected() throws Exception {

        SeCoordinateReference seCoordRefSys = new SeCoordinateReference();
        seCoordRefSys.setCoordSysByID(new SeObjectId(23030));

        CoordinateReferenceSystem expectedCRS = CRS.decode("EPSG:23030");
        CoordinateReferenceSystem compatibleCRS = RasterUtils.findCompatibleCRS(seCoordRefSys);

        assertSame(expectedCRS, compatibleCRS);
    }

    @Test
    public void testFindCompatibleCRS_Geographic() throws Exception {
        SeCoordinateReference seCoordRefSys = new SeCoordinateReference();
        seCoordRefSys.setCoordSysByID(new SeObjectId(4326));

        CoordinateReferenceSystem expectedCRS = CRS.decode("EPSG:4326");
        CoordinateReferenceSystem compatibleCRS = RasterUtils.findCompatibleCRS(seCoordRefSys);

        assertSame(expectedCRS, compatibleCRS);
    }

    @Test
    public void testCalculateMatchingLevelDimensionFullyCovered() {
        Rectangle2D levelExtent = new Rectangle(0, 0, 100, 100);
        Rectangle levelGridRange = new Rectangle(0, 0, 10, 10);
        Rectangle2D requestEnvelope = new Rectangle(20, 20, 60, 60);

        Rectangle expectedGridRange = new Rectangle(2, 2, 7, 7);

        testCalculateMatchingLevelDimension(levelExtent, levelGridRange, requestEnvelope,
                expectedGridRange);
    }

    @Test
    public void testCalculateMatchingLevelDimensionNonOverlapping() {
        Rectangle2D levelExtent = new Rectangle(0, 0, 100, 100);
        Rectangle levelGridRange = new Rectangle(0, 0, 10, 10);
        Rectangle2D requestEnvelope = new Rectangle(101, 101, 100, 100);

        Rectangle expectedGridRange = new Rectangle(0, 0, 0, 0);

        testCalculateMatchingLevelDimension(levelExtent, levelGridRange, requestEnvelope,
                expectedGridRange);
    }

    @Test
    public void testCalculateMatchingLevelDimensionTouches() {
        Rectangle2D levelExtent = new Rectangle(0, 0, 100, 100);
        Rectangle levelGridRange = new Rectangle(0, 0, 10, 10);
        // req envelope touches the right edge, but are adyacent, not overlapping
        Rectangle2D requestEnvelope = new Rectangle(100, 0, 1, 100);

        // a zero width range still means not to retrieve the pixel at index 10 on the x axis
        Rectangle expectedGridRange = new Rectangle(10, 0, 0, 10);

        testCalculateMatchingLevelDimension(levelExtent, levelGridRange, requestEnvelope,
                expectedGridRange);
    }

    @Test
    public void testCalculateMatchingLevelDimensionIntersects() {
        Rectangle2D levelExtent = new Rectangle(0, 0, 100, 100);
        Rectangle levelGridRange = new Rectangle(0, 0, 10, 10);

        Rectangle2D requestEnvelope = new Rectangle(99, 99, 1, 1);

        Rectangle expectedGridRange = new Rectangle(10, 0, 0, 1);

        testCalculateMatchingLevelDimension(levelExtent, levelGridRange, requestEnvelope,
                expectedGridRange);
    }

    /**
     * 
     * @param requestEnvelope
     * @param levelExtent
     *            the actual geographical extent for the level
     * @param levelGridRange
     * @param expectedGridRange
     */
    private void testCalculateMatchingLevelDimension(final Rectangle2D levelExtent,
            final Rectangle levelGridRange, final Rectangle2D requestEnvelope,
            final Rectangle expectedGridRange) {

        GeneralEnvelope requestedEnvelope = new GeneralEnvelope(DefaultGeographicCRS.WGS84);
        requestedEnvelope.setEnvelope(requestEnvelope.getMinX(), requestEnvelope.getMinY(),
                requestEnvelope.getMaxX(), requestEnvelope.getMaxY());

        GeneralEnvelope levelEnvelope = new GeneralEnvelope(DefaultGeographicCRS.WGS84);
        levelEnvelope.setEnvelope(levelExtent.getMinX(), levelExtent.getMinY(), levelExtent
                .getMaxX(), levelExtent.getMaxY());

        MathTransform rasterToModel = RasterUtils.createRasterToModel(levelGridRange, levelEnvelope);

        Rectangle matchingDimension = RasterUtils.calculateMatchingDimension(
                requestedEnvelope, rasterToModel, levelGridRange);

        assertEquals(expectedGridRange.x, matchingDimension.x);
        assertEquals(expectedGridRange.y, matchingDimension.y);
        // we use PixelInCell.CELL_CORNER for the math transform, so we expect a width of 101
        // instead of 100 for this corner
        assertEquals(expectedGridRange.width, matchingDimension.width);
        assertEquals(expectedGridRange.height, matchingDimension.height);
    }
}
