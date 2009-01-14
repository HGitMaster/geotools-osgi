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
package org.geotools.arcsde.gce.band;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.gce.RasterTestData;
import org.geotools.arcsde.gce.RasterTestData.RasterTableName;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.util.logging.Logging;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterConstraint;
import com.esri.sde.sdk.client.SeRasterTile;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

public class UnsignedByteRGBBandCopierTest {

    static RasterTestData rasterTestData;

    static Logger LOGGER;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        LOGGER = Logging.getLogger(UnsignedByteRGBBandCopierTest.class.getCanonicalName());
        if (rasterTestData == null) {
            rasterTestData = new RasterTestData();
            rasterTestData.setUp();
            rasterTestData.load1bitRaster();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        rasterTestData.tearDown();
    }

    @Test
    public void testLiveRGBRasterTile() throws Exception {

        rasterTestData.loadRGBRaster();
        final String tableName = rasterTestData.getRasterTableName(RasterTableName.RGB);

        ArcSDEPooledConnection conn = null;
        try {
            ArcSDEConnectionPool pool = rasterTestData.getConnectionPool();

            conn = pool.getConnection();
            SeQuery q = new SeQuery(conn, new String[] { "RASTER" }, new SeSqlConstruct(tableName));
            q.prepareQuery();
            q.execute();
            SeRow r = q.fetch();
            SeRasterAttr rAttr = r.getRaster(0);

            int[] bands = new int[] { 1, 2, 3 };
            SeRasterConstraint rConstraint = new SeRasterConstraint();
            rConstraint.setBands(bands);
            rConstraint.setLevel(0);
            rConstraint.setEnvelope(0, 0, 0, 0);
            rConstraint.setInterleave(SeRaster.SE_RASTER_INTERLEAVE_BSQ);

            q.queryRasterTile(rConstraint);

            BufferedImage fromSdeImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
            ArcSDERasterBandCopier bandCopier = ArcSDERasterBandCopier.getInstance(rAttr
                    .getPixelType(), rAttr.getTileWidth(), rAttr.getTileHeight());

            SeRasterTile rTile = r.getRasterTile();
            for (int i = 0; i < bands.length; i++) {
                bandCopier.copyPixelData(rTile, fromSdeImage.getRaster(), 0, 0, i);
                rTile = r.getRasterTile();
            }

            final File originalRasterFile = org.geotools.test.TestData.file(null, rasterTestData
                    .getRasterTestDataProperty("sampledata.rgbraster"));
            BufferedImage originalImage = ImageIO.read(originalRasterFile);

            // Well, now we have an image tile. Does it have what we expect on it?
            Assert.assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
                    fromSdeImage, originalImage.getSubimage(0, 0, 128, 128)));

        } catch (SeException se) {
            throw new ArcSdeException(se);
        } finally {
            if (conn != null)
                conn.close();
        }
    }

}
