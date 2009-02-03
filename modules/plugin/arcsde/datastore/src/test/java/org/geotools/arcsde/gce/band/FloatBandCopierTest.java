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

import static org.geotools.arcsde.gce.imageio.RasterCellType.TYPE_32BIT_REAL;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.gce.RasterTestData;
import org.geotools.arcsde.gce.imageio.InterleaveType;
import org.geotools.arcsde.gce.imageio.RasterCellType;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.util.logging.Logging;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterConstraint;
import com.esri.sde.sdk.client.SeRasterTile;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

public class FloatBandCopierTest {

    static RasterTestData rasterTestData;

    static Logger LOGGER;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        LOGGER = Logging.getLogger("org.geotools.arcsde.gce");
        if (rasterTestData == null) {
            rasterTestData = new RasterTestData();
            rasterTestData.setUp();
            rasterTestData.loadFloatRaster();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        rasterTestData.tearDown();
    }

    @Test
    public void testReadAlignedFloatTile() throws Exception {
        final String tableName = rasterTestData.getRasterTableName(TYPE_32BIT_REAL, 1);

        ArcSDEPooledConnection conn = null;
        try {
            ArcSDEConnectionPool pool = rasterTestData.getConnectionPool();

            conn = pool.getConnection();
            SeQuery q = new SeQuery(conn, new String[] { "RASTER" }, new SeSqlConstruct(tableName));
            q.prepareQuery();
            q.execute();
            SeRow r = q.fetch();
            SeRasterAttr rAttr = r.getRaster(0);

            int[] bands = new int[] { 1 };
            SeRasterConstraint rConstraint = new SeRasterConstraint();
            rConstraint.setBands(bands);
            rConstraint.setLevel(0);
            rConstraint.setEnvelope(0, 0, 0, 0);
            final int interleave = rAttr.getInterleave();
            System.out.println(InterleaveType.valueOf(interleave));
            rConstraint.setInterleave(interleave);

            q.queryRasterTile(rConstraint);

            // building a greyscale float image isn't totally straightforward...
            final int w = 128;
            final int h = 128;

            SampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_FLOAT, w, h, 1, w,
                    new int[] { 0 });
            DataBuffer db = new DataBufferFloat(w * h);
            WritableRaster wr = Raster.createWritableRaster(sm, db, null);
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            ColorModel cm = new ComponentColorModel(cs, false, true, Transparency.OPAQUE,
                    DataBuffer.TYPE_FLOAT);
            final BufferedImage fromSdeImage = new BufferedImage(cm, wr, false, null);

            final RasterCellType pixelType = RasterCellType.valueOf(rAttr.getPixelType());
            ArcSDERasterBandCopier bandCopier = ArcSDERasterBandCopier.getInstance(pixelType, rAttr
                    .getTileWidth(), rAttr.getTileHeight());

            SeRasterTile rTile = r.getRasterTile();
            for (int i = 0; i < bands.length; i++) {
                bandCopier.copyPixelData(rTile, fromSdeImage.getRaster(), 0, 0, i);
                rTile = r.getRasterTile();
            }

            ImageIO.write(fromSdeImage, "TIF", new File("/tmp/"
                    + Thread.currentThread().getStackTrace()[1].getMethodName() + ".tiff"));
            final File originalRasterFile = org.geotools.test.TestData.file(null, rasterTestData
                    .getRasterTestDataProperty("sampledata.floatraster"));
            BufferedImage originalImage = ImageIO.read(originalRasterFile);
            ImageIO
                    .write(originalImage.getSubimage(0, 0, 128, 128), "TIF", new File("/tmp/"
                            + Thread.currentThread().getStackTrace()[1].getMethodName()
                            + "-original.tiff"));

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
