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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.gce.ArcSDEPyramid;
import org.geotools.arcsde.gce.RasterTestData;
import org.geotools.arcsde.gce.RasterTestData.RasterTableName;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterBand;
import com.esri.sde.sdk.client.SeRasterColumn;

public class ThreeByteBandRGBReaderTest {

    static RasterTestData rasterTestData;

    static HashMap<String, Object> readerProps;

    static Logger LOGGER = Logging.getLogger(ThreeByteBandRGBReaderTest.class.getCanonicalName());

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        rasterTestData = new RasterTestData();
        rasterTestData.setUp();
        rasterTestData.loadRGBRaster();

        ArcSDEPyramid pyramid;
        String tableName;
        ArcSDEPooledConnection conn = null;
        try {
            tableName = rasterTestData.getRasterTableName(RasterTableName.RGB);
            SeRasterAttr rattr = rasterTestData.getRasterAttributes(tableName, new Rectangle(0, 0,
                    0, 0), 0, new int[] { 1, 2, 3 });

            conn = rasterTestData.getConnectionPool().getConnection();
            SeRasterColumn rcol = new SeRasterColumn(conn, rattr.getRasterColumnId());

            CoordinateReferenceSystem crs = CRS.decode("EPSG:2805");
            pyramid = new ArcSDEPyramid(rattr, crs);

            readerProps = new HashMap<String, Object>();
            readerProps.put(ArcSDERasterReaderSpi.PYRAMID, pyramid);
            readerProps.put(ArcSDERasterReaderSpi.RASTER_TABLE, tableName);
            readerProps.put(ArcSDERasterReaderSpi.RASTER_COLUMN, "RASTER");
        } catch (ArcSdeException se) {
            LOGGER.log(Level.SEVERE, se.getSeError().getErrDesc(), se);
            throw se;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        rasterTestData.tearDown();
    }

    /**
     * Tests reading the first three bands of a 4-band image (1 = RED, 2 = GREEN, 3 = BLUE, 4 =
     * NEAR_INFRARED) into a TYPE_INT_RGB image. Bands are mapped as follows: rasterband 1 => image
     * band 0 rasterband 2 => image band 1 rasterband 3 => image band 2
     */
    @Test
    public void testReadOutsideImageBounds() throws Exception {

        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi()
                .createReaderInstance(readerProps);

        ArcSDEPooledConnection conn = null;
        try {
            conn = rasterTestData.getConnectionPool().getConnection();

            SeRasterAttr rattr = rasterTestData.getRasterAttributes(rasterTestData
                    .getRasterTableName(RasterTableName.RGB), new Rectangle(0, 0, 0, 0), 0,
                    new int[] { 1, 2, 3 });

            SeRasterBand[] bands = rattr.getBands();
            HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));
            bandMapper.put(Integer.valueOf((int) bands[1].getId().longValue()), Integer.valueOf(1));
            bandMapper.put(Integer.valueOf((int) bands[2].getId().longValue()), Integer.valueOf(2));

            BufferedImage image;
            // int[] opaque;

            final Point dataOffset = new Point(950, 950);
            final Point imageOffset = new Point(0, 0);
            final int w = 300, h = 300;

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1, 2, 3 });
            rParam.setConnection(conn);
            rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    image.setRGB(x, y, 0xffffffff);
                }
            }
            rParam.setDestination(image);
            rParam.setBandMapper(bandMapper);

            reader.read(0, rParam);

            // ImageIO.write(image, "PNG", new File("/tmp/" +
            // Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
            final String rasFileName = rasterTestData
                    .getRasterTestDataProperty("sampledata.rgbraster");
            BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null,
                    rasFileName));

            final int sourcemaxw = Math.min(rattr.getImageWidthByLevel(0) - dataOffset.x, w);
            final int sourcemaxh = Math.min(rattr.getImageHeightByLevel(0) - dataOffset.y, h);
            Assert.assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
                    image.getSubimage(imageOffset.x, imageOffset.y, sourcemaxw, sourcemaxh),
                    originalImage.getSubimage(dataOffset.x, dataOffset.y, sourcemaxw, sourcemaxh)));

        } catch (Exception e) {
            throw e;
        } finally {
            if (conn != null && !conn.isClosed())
                conn.close();
        }
    }

    /**
     * Tests reading the first three bands of a 4-band image (1 = RED, 2 = GREEN, 3 = BLUE, 4 =
     * NEAR_INFRARED) into a TYPE_INT_RGB image. Bands are mapped as follows: rasterband 1 => image
     * band 0 rasterband 2 => image band 1 rasterband 3 => image band 2
     */
    @Test
    public void testReadOffsetImage() throws Exception {

        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi()
                .createReaderInstance(readerProps);

        ArcSDEPooledConnection conn = null;
        try {
            conn = rasterTestData.getConnectionPool().getConnection();

            SeRasterAttr rattr = rasterTestData.getRasterAttributes(rasterTestData
                    .getRasterTableName(RasterTableName.RGB), new Rectangle(0, 0, 0, 0), 0,
                    new int[] { 1, 2, 3 });

            SeRasterBand[] bands = rattr.getBands();
            HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));
            bandMapper.put(Integer.valueOf((int) bands[1].getId().longValue()), Integer.valueOf(1));
            bandMapper.put(Integer.valueOf((int) bands[2].getId().longValue()), Integer.valueOf(2));

            BufferedImage image;

            final Point dataOffset = new Point(0, 0);
            final Point imageOffset = new Point(100, 100);
            final int w = 1200, h = 1200;

            image = new BufferedImage(w + imageOffset.x, h + imageOffset.y,
                    BufferedImage.TYPE_INT_RGB);

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1, 2, 3 });
            rParam.setConnection(conn);
            rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
            rParam.setDestination(image);
            rParam.setDestinationOffset(imageOffset);
            rParam.setBandMapper(bandMapper);

            reader.read(0, rParam);

            // ImageIO.write(image, "PNG", new File("/tmp/" +
            // Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
            final String rasFileName = rasterTestData
                    .getRasterTestDataProperty("sampledata.rgbraster");
            BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null,
                    rasFileName));

            final int sourcemaxw = Math.min(rattr.getImageWidthByLevel(0) - dataOffset.x, w);
            final int sourcemaxh = Math.min(rattr.getImageHeightByLevel(0) - dataOffset.y, h);
            Assert.assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
                    image.getSubimage(imageOffset.x, imageOffset.y, sourcemaxw, sourcemaxh),
                    originalImage.getSubimage(dataOffset.x, dataOffset.y, sourcemaxw, sourcemaxh)));

        } catch (Exception e) {
            throw e;
        } finally {
            if (conn != null && !conn.isClosed())
                conn.close();
        }
    }

    @Test
    public void testReadRGBIntoTYPE_3BYTE_BGRImage() throws Exception {

        String imgPrefix = "type_3byte_bgr-3band-image";

        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi()
                .createReaderInstance(readerProps);

        ArcSDEPooledConnection conn = null;
        try {
            conn = rasterTestData.getConnectionPool().getConnection();
            SeRasterAttr rattr = rasterTestData.getRasterAttributes(rasterTestData
                    .getRasterTableName(RasterTableName.RGB), new Rectangle(0, 0, 0, 0), 0,
                    new int[] { 1, 2, 3 });
            ArcSDEPyramid p = new ArcSDEPyramid(rattr, CRS.decode("EPSG:4326"));

            SeRasterBand[] bands = rattr.getBands();
            HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();
            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));
            bandMapper.put(Integer.valueOf((int) bands[1].getId().longValue()), Integer.valueOf(1));
            bandMapper.put(Integer.valueOf((int) bands[2].getId().longValue()), Integer.valueOf(2));

            BufferedImage image;

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1, 2, 3 });
            rParam.setConnection(conn);
            rParam.setSourceRegion(new Rectangle(p.getPyramidLevel(0).getSize()));
            image = new BufferedImage(p.getPyramidLevel(0).getSize().width, p.getPyramidLevel(0)
                    .getSize().height, BufferedImage.TYPE_3BYTE_BGR);
            rParam.setDestination(image);
            rParam.setBandMapper(bandMapper);

            reader.read(0, rParam);

            final String rasFileName = rasterTestData
                    .getRasterTestDataProperty("sampledata.rgbraster");
            BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null,
                    rasFileName));
            // ImageIO.write(image, "PNG", new File(imgPrefix + "1.png"));
            Assert.assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
                    image, originalImage));
        } catch (Exception e) {
            throw e;
        } finally {
            if (conn != null && !conn.isClosed())
                conn.close();
        }
    }
}
