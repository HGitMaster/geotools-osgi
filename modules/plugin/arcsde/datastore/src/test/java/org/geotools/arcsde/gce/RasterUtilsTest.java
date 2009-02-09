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

import org.geotools.referencing.CRS;
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


}
