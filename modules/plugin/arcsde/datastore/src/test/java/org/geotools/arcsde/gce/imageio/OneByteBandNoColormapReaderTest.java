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
package org.geotools.arcsde.gce.imageio;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.gce.ArcSDERasterReaderSpi;
import org.geotools.arcsde.gce.RasterTestData;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterColumn;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

public class OneByteBandNoColormapReaderTest {

    static RasterTestData rasterTestData;

    static HashMap<String, Object> readerProps;

    static Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    private static String tableName;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        rasterTestData = new RasterTestData();
        rasterTestData.setUp();
        tableName = rasterTestData.loadOneByteGrayScaleRaster();

        ArcSDEPooledConnection conn = null;
        SeQuery q = null;
        ArcSDEPyramid pyramid;
        SeRow r;
        try {

            // Set up a pyramid and readerprops for the sample three-band imagery
            conn = rasterTestData.getConnectionPool().getConnection();
            q = new SeQuery(conn, new String[] { "RASTER" }, new SeSqlConstruct(tableName));
            q.prepareQuery();
            q.execute();
            r = q.fetch();
            SeRasterAttr rattrThreeBand = r.getRaster(0);
            q.close();

            SeRasterColumn rcol = new SeRasterColumn(conn, rattrThreeBand.getRasterColumnId());

            CoordinateReferenceSystem crs = CRS.parseWKT(rcol.getCoordRef()
                    .getCoordSysDescription());
            pyramid = new ArcSDEPyramid(rattrThreeBand, crs);

            readerProps = new HashMap<String, Object>();
            readerProps.put(ArcSDERasterReaderSpi.PYRAMID, pyramid);
            readerProps.put(ArcSDERasterReaderSpi.RASTER_TABLE, tableName);
            readerProps.put(ArcSDERasterReaderSpi.RASTER_COLUMN, "RASTER");
        } catch (SeException se) {
            LOGGER.log(Level.SEVERE, se.getSeError().getErrDesc(), se);
            throw se;
        } finally {
            if (q != null)
                q.close();
            if (conn != null) {
                conn.close();
            }
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        rasterTestData.tearDown();
    }

}
