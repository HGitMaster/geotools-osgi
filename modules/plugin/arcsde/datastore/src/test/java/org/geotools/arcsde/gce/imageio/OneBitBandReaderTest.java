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

import org.geotools.arcsde.gce.ArcSDEPyramid;
import org.geotools.arcsde.gce.RasterTestData;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterBand;
import com.esri.sde.sdk.client.SeRasterColumn;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

public class OneBitBandReaderTest {

    static RasterTestData rasterTestData;

    static SeRasterAttr rasterAttr;

    static HashMap<String, Object> readerProps;

    static Logger LOGGER = Logging.getLogger(OneBitBandReaderTest.class.getCanonicalName());

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        rasterTestData = new RasterTestData();
        rasterTestData.setUp();
        rasterTestData.load1bitRaster();

        ArcSDEPooledConnection conn = null;
        SeQuery q = null;
        ArcSDEPyramid pyramid;
        SeRow r;
        String tableName;
        try {

            // Set up a pyramid and readerprops for the sample three-band imagery
            conn = rasterTestData.getConnectionPool().getConnection();
            tableName = rasterTestData.get1bitRasterTableName();
            q = new SeQuery(conn, new String[] { "RASTER" }, new SeSqlConstruct(tableName));
            q.prepareQuery();
            q.execute();
            r = q.fetch();
            rasterAttr = r.getRaster(0);
            q.close();

            SeRasterColumn rcol = new SeRasterColumn(conn, rasterAttr.getRasterColumnId());

            CoordinateReferenceSystem crs = CRS.decode("EPSG:2805");
            pyramid = new ArcSDEPyramid(rasterAttr, crs);

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

    @Test
    public void testRead1bitImageTileAligned() throws Exception {
        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi()
                .createReaderInstance(readerProps);

        ArcSDEPooledConnection conn = null;
        try {
            conn = rasterTestData.getConnectionPool().getConnection();

            SeRasterBand[] bands = rasterAttr.getBands();
            HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));

            BufferedImage image;

            final Point dataOffset = new Point(0, 0);
            final Point imageOffset = new Point(0, 0);
            final int w = 256, h = 256;

            image = new BufferedImage(w + imageOffset.x, h + imageOffset.y,
                    BufferedImage.TYPE_BYTE_BINARY);

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1 });
            rParam.setConnection(conn);
            rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
            rParam.setDestination(image);
            rParam.setDestinationOffset(imageOffset);
            rParam.setBandMapper(bandMapper);

            reader.read(0, rParam);

            // ImageIO.write(image, "PNG", new File("/tmp/" +
            // Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
            final String rasFileName = rasterTestData
                    .getRasterTestDataProperty("sampledata.onebitraster");

            final int sourcemaxw = Math.min(rasterAttr.getImageWidthByLevel(0) - dataOffset.x, w);
            final int sourcemaxh = Math.min(rasterAttr.getImageHeightByLevel(0) - dataOffset.y, h);
            BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null,
                    rasFileName));
            originalImage = originalImage.getSubimage(dataOffset.x, dataOffset.y, sourcemaxw,
                    sourcemaxh);
            Assert.assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
                    image.getSubimage(imageOffset.x, imageOffset.y, sourcemaxw, sourcemaxh),
                    originalImage));

        } catch (Exception e) {
            throw e;
        } finally {
            if (conn != null && !conn.isClosed())
                conn.close();
        }
    }

    @Test
    public void testRead1bitImageByteAligned() throws Exception {
        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi()
                .createReaderInstance(readerProps);

        ArcSDEPooledConnection conn = null;
        try {
            conn = rasterTestData.getConnectionPool().getConnection();

            SeRasterBand[] bands = rasterAttr.getBands();
            HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));

            BufferedImage image;

            final Point dataOffset = new Point(8, 8);
            final Point imageOffset = new Point(0, 0);
            final int w = 256, h = 256;

            image = new BufferedImage(w + imageOffset.x, h + imageOffset.y,
                    BufferedImage.TYPE_BYTE_BINARY);

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1 });
            rParam.setConnection(conn);
            rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
            rParam.setDestination(image);
            rParam.setDestinationOffset(imageOffset);
            rParam.setBandMapper(bandMapper);

            reader.read(0, rParam);

            // ImageIO.write(image, "PNG", new File("/tmp/" +
            // Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
            final String rasFileName = rasterTestData
                    .getRasterTestDataProperty("sampledata.onebitraster");

            final int sourcemaxw = Math.min(rasterAttr.getImageWidthByLevel(0) - dataOffset.x, w);
            final int sourcemaxh = Math.min(rasterAttr.getImageHeightByLevel(0) - dataOffset.y, h);
            BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null,
                    rasFileName));
            originalImage = originalImage.getSubimage(dataOffset.x, dataOffset.y, sourcemaxw,
                    sourcemaxh);
            Assert.assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
                    image.getSubimage(imageOffset.x, imageOffset.y, sourcemaxw, sourcemaxh),
                    originalImage));

        } catch (Exception e) {
            throw e;
        } finally {
            if (conn != null && !conn.isClosed())
                conn.close();
        }
    }

    @Test
    public void testRead1bitImageDataOffset1() throws Exception {
        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi()
                .createReaderInstance(readerProps);

        ArcSDEPooledConnection conn = null;
        try {
            conn = rasterTestData.getConnectionPool().getConnection();

            SeRasterBand[] bands = rasterAttr.getBands();
            HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));

            BufferedImage image;

            final Point dataOffset = new Point(3, 3);
            final Point imageOffset = new Point(0, 0);
            final int w = 128, h = 128;

            image = new BufferedImage(w + imageOffset.x, h + imageOffset.y,
                    BufferedImage.TYPE_BYTE_BINARY);

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1 });
            rParam.setConnection(conn);
            rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
            rParam.setDestination(image);
            rParam.setDestinationOffset(imageOffset);
            rParam.setBandMapper(bandMapper);

            reader.read(0, rParam);

            // ImageIO.write(image, "PNG", new File("/tmp/" +
            // Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
            final String rasFileName = rasterTestData
                    .getRasterTestDataProperty("sampledata.onebitraster");

            final int sourcemaxw = Math.min(rasterAttr.getImageWidthByLevel(0) - dataOffset.x, w);
            final int sourcemaxh = Math.min(rasterAttr.getImageHeightByLevel(0) - dataOffset.y, h);
            BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null,
                    rasFileName));
            originalImage = originalImage.getSubimage(dataOffset.x, dataOffset.y, sourcemaxw,
                    sourcemaxh);
            Assert.assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
                    image.getSubimage(imageOffset.x, imageOffset.y, sourcemaxw, sourcemaxh),
                    originalImage));

        } catch (Exception e) {
            throw e;
        } finally {
            if (conn != null && !conn.isClosed())
                conn.close();
        }
    }

    @Test
    public void testRead1bitImageDataOffset2() throws Exception {
        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi()
                .createReaderInstance(readerProps);

        ArcSDEPooledConnection conn = null;
        try {
            conn = rasterTestData.getConnectionPool().getConnection();

            SeRasterBand[] bands = rasterAttr.getBands();
            HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));

            BufferedImage image;

            final Point dataOffset = new Point(15, 15);
            final Point imageOffset = new Point(0, 0);
            final int w = 176, h = 176;

            image = new BufferedImage(w + imageOffset.x, h + imageOffset.y,
                    BufferedImage.TYPE_BYTE_BINARY);

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1 });
            rParam.setConnection(conn);
            rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
            rParam.setDestination(image);
            rParam.setDestinationOffset(imageOffset);
            rParam.setBandMapper(bandMapper);

            reader.read(0, rParam);

            // ImageIO.write(image, "PNG", new File("/tmp/" +
            // Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
            final String rasFileName = rasterTestData
                    .getRasterTestDataProperty("sampledata.onebitraster");

            final int sourcemaxw = Math.min(rasterAttr.getImageWidthByLevel(0) - dataOffset.x, w);
            final int sourcemaxh = Math.min(rasterAttr.getImageHeightByLevel(0) - dataOffset.y, h);
            BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null,
                    rasFileName));
            // ImageIO.write(originalImage.getSubimage(0, 0, 200, 200), "PNG", new File("/tmp/" +
            // Thread.currentThread().getStackTrace()[1].getMethodName() + "-original.png"));
            originalImage = originalImage.getSubimage(dataOffset.x, dataOffset.y, sourcemaxw,
                    sourcemaxh);
            Assert.assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
                    image.getSubimage(imageOffset.x, imageOffset.y, sourcemaxw, sourcemaxh),
                    originalImage));

        } catch (Exception e) {
            throw e;
        } finally {
            if (conn != null && !conn.isClosed())
                conn.close();
        }
    }

    @Test
    public void testRead1bitImageTargetImageOffset1() throws Exception {
        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi()
                .createReaderInstance(readerProps);

        ArcSDEPooledConnection conn = null;
        try {
            conn = rasterTestData.getConnectionPool().getConnection();

            SeRasterBand[] bands = rasterAttr.getBands();
            HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));

            BufferedImage image;

            final Point dataOffset = new Point(0, 0);
            final Point imageOffset = new Point(5, 5);
            final int w = 176, h = 176;

            image = new BufferedImage(w + imageOffset.x, h + imageOffset.y,
                    BufferedImage.TYPE_BYTE_BINARY);

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1 });
            rParam.setConnection(conn);
            rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
            rParam.setDestination(image);
            rParam.setDestinationOffset(imageOffset);
            rParam.setBandMapper(bandMapper);

            reader.read(0, rParam);

            // ImageIO.write(image, "PNG", new File("/tmp/" +
            // Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
            final String rasFileName = rasterTestData
                    .getRasterTestDataProperty("sampledata.onebitraster");

            final int sourcemaxw = Math.min(rasterAttr.getImageWidthByLevel(0) - dataOffset.x, w);
            final int sourcemaxh = Math.min(rasterAttr.getImageHeightByLevel(0) - dataOffset.y, h);
            BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null,
                    rasFileName));
            // ImageIO.write(originalImage.getSubimage(0, 0, 200, 200), "PNG", new File("/tmp/" +
            // Thread.currentThread().getStackTrace()[1].getMethodName() + "-original.png"));
            originalImage = originalImage.getSubimage(dataOffset.x, dataOffset.y, sourcemaxw,
                    sourcemaxh);
            Assert.assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
                    image.getSubimage(imageOffset.x, imageOffset.y, sourcemaxw, sourcemaxh),
                    originalImage));

        } catch (Exception e) {
            throw e;
        } finally {
            if (conn != null && !conn.isClosed())
                conn.close();
        }
    }

    @Test
    public void testRead1bitImageTargetImageBeyondBoundaries1() throws Exception {
        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi()
                .createReaderInstance(readerProps);

        ArcSDEPooledConnection conn = null;
        try {
            conn = rasterTestData.getConnectionPool().getConnection();

            SeRasterBand[] bands = rasterAttr.getBands();
            HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));

            BufferedImage image;

            final Point dataOffset = new Point(400, 400);
            final Point imageOffset = new Point(30, 30);
            final int w = 176, h = 176;

            image = new BufferedImage(w + imageOffset.x, h + imageOffset.y,
                    BufferedImage.TYPE_BYTE_BINARY);

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1 });
            rParam.setConnection(conn);
            rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
            rParam.setDestination(image);
            rParam.setDestinationOffset(imageOffset);
            rParam.setBandMapper(bandMapper);

            reader.read(0, rParam);

            // ImageIO.write(image, "PNG", new File("/tmp/" +
            // Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
            final String rasFileName = rasterTestData
                    .getRasterTestDataProperty("sampledata.onebitraster");

            final int sourcemaxw = Math.min(rasterAttr.getImageWidthByLevel(0) - dataOffset.x, w);
            final int sourcemaxh = Math.min(rasterAttr.getImageHeightByLevel(0) - dataOffset.y, h);
            BufferedImage originalImage = ImageIO.read(org.geotools.test.TestData.getResource(null,
                    rasFileName));
            // ImageIO.write(originalImage.getSubimage(0, 0, 200, 200), "PNG", new File("/tmp/" +
            // Thread.currentThread().getStackTrace()[1].getMethodName() + "-original.png"));
            originalImage = originalImage.getSubimage(dataOffset.x, dataOffset.y, sourcemaxw,
                    sourcemaxh);
            Assert.assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
                    image.getSubimage(imageOffset.x, imageOffset.y, sourcemaxw, sourcemaxh),
                    originalImage));

        } catch (Exception e) {
            throw e;
        } finally {
            if (conn != null && !conn.isClosed())
                conn.close();
        }
    }

}
