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

import static org.junit.Assert.assertSame;

import java.awt.image.ColorModel;
import java.awt.image.SampleModel;

import javax.imageio.ImageTypeSpecifier;

import org.geotools.arcsde.gce.imageio.RasterCellType;
import org.geotools.referencing.CRS;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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
    public void testCreateImageTypeSpecifier_NoColorMap() throws Exception {
//        for (RasterCellType pixelType : RasterCellType.values()) {
//            testCreateImageTypeSpecifier(pixelType);
//        }

            testCreateImageTypeSpecifier(RasterCellType.TYPE_1BIT);
    }

    @Test
    @Ignore
    public void testCreateImageTypeSpecifier_ColorMapped() throws Exception {
        //TODO
    }

    private void testCreateImageTypeSpecifier(RasterCellType cellType) throws Exception {
        final int minBands = 1;
        final int maxBands = 7;
        ImageTypeSpecifier its;
        for (int numBands = minBands; numBands <= maxBands; numBands++) {
            System.out.println(cellType + " - " + numBands + "-Band");
            its = RasterUtils.createImageTypeSpec(cellType, numBands, 10, 10, 5, 5);
            SampleModel sampleModel = its.getSampleModel(256, 128);
            ColorModel colorModel = its.getColorModel();
        }

    }
}
