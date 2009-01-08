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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.arcsde.gce.imageio.ArcSDERasterImageReadParam;
import org.geotools.arcsde.gce.imageio.ArcSDERasterReader;
import org.geotools.arcsde.gce.imageio.ArcSDERasterReaderSpi;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterBand;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

/**
 * Tests the functionality of the ArcSDE raster-display package to read rasters from an ArcSDE
 * database THIS CLASS IS A LEGACY TEST CLASS. I plan to remove it soon. Don't use it! All the tests
 * have been broken out into the gcreader, band and imageio packages. Use the tests in those
 * directories instead!
 * 
 * @author Saul Farber, (based on ArcSDEPoolTest by Gabriel Roldan)
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/test/java
 *         /org/geotools/arcsde/gce/ArcSDEImageIOReaderOutputFormatsTest.java $
 * @version $Id: ArcSDEImageIOReaderOutputFormatsTest.java 32181 2009-01-08 15:44:06Z groldan $
 */
public class ArcSDEImageIOReaderOutputFormatsTest extends TestCase {

    private static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.arcsde.gce");

    private ArcSDEConnectionPool pool = null;

    private HashMap fourBandReaderProps, threeBandReaderProps;

    private SeRasterAttr rasterAttr;

    /**
     * Creates a new SessionPoolTest object.
     */
    public ArcSDEImageIOReaderOutputFormatsTest(String name) throws Exception {
        super(name);
    }

    /**
     * loads {@code test-data/testparams.properties} to get connection parameters and sets up an
     * SessionPool
     * 
     * @throws Exception
     *             DOCUMENT ME!
     * @throws IllegalStateException
     *             DOCUMENT ME!
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // do setup one time only
        if (pool != null)
            return;

        RasterTestData testData = new RasterTestData();
        testData.setUp();

        pool = testData.getConnectionPool();

        ArcSDEPooledConnection con = null;
        SeQuery q = null;
        ArcSDEPyramid pyramid;
        SeRow r;
        CoordinateReferenceSystem crs = CRS.decode("EPSG:26986");
        String tableName;
        tableName = testData.getRasterTestDataProperty("fourbandtable");
        if (tableName == null) {
            return;
        }
        try {
            // Set up a pyramid and readerprops for the four-band 2005 imagery
            con = pool.getConnection();
            SeSqlConstruct seSqlConstruct = new SeSqlConstruct(tableName);
            q = new SeQuery(con, new String[] { "RASTER" }, seSqlConstruct);
            q.prepareQuery();
            q.execute();
            r = q.fetch();
            rasterAttr = r.getRaster(0);
            pyramid = new ArcSDEPyramid(rasterAttr, crs);

            fourBandReaderProps = new HashMap();
            fourBandReaderProps.put(ArcSDERasterReaderSpi.PYRAMID, pyramid);
            fourBandReaderProps.put(ArcSDERasterReaderSpi.RASTER_TABLE, tableName);
            fourBandReaderProps.put(ArcSDERasterReaderSpi.RASTER_COLUMN, "RASTER");
        } catch (IOException se) {
            LOGGER.log(Level.SEVERE, se.getMessage(), se);
            throw se;
        } finally {
            if (q != null) {
                q.close();
            }
            if (con != null) {
                con.close();
            }
        }

        try {
            // Set up a pyramid and readerprops for the three-band 2001 imagery
            con = pool.getConnection();
            testData.getRasterTestDataProperty("threebandtable");
            SeSqlConstruct seSqlConstruct = new SeSqlConstruct(tableName);

            q = new SeQuery(con, new String[] { "RASTER" }, seSqlConstruct);
            q.prepareQuery();
            q.execute();
            r = q.fetch();
            rasterAttr = r.getRaster(0);

            pyramid = new ArcSDEPyramid(rasterAttr, crs);

            threeBandReaderProps = new HashMap();
            threeBandReaderProps.put(ArcSDERasterReaderSpi.PYRAMID, pyramid);
            threeBandReaderProps.put(ArcSDERasterReaderSpi.RASTER_TABLE, tableName);
            threeBandReaderProps.put(ArcSDERasterReaderSpi.RASTER_COLUMN, "RASTER");
        } catch (IOException se) {
            LOGGER.log(Level.SEVERE, se.getMessage(), se);
            throw se;
        } finally {
            if (q != null) {
                q.close();
            }
            if (con != null) {
                con.close();
            }
        }
    }

    /**
     * closes the connection pool if it's still open
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    @Override
    protected void tearDown() throws Exception {
        // do-nothing
    }

    /**
     * Tests reading the first three bands of a 4-band image (1 = RED, 2 = GREEN, 3 = BLUE, 4 =
     * NEAR_INFRARED) into a TYPE_INT_RGB image. Bands are mapped as follows: rasterband 1 => image
     * band 0 rasterband 2 => image band 1 rasterband 3 => image band 2
     */
    public void testRead4BandIntoTYPE_INT_RGBImage() throws Exception {

        String imgPrefix = "type_int_rgb-fourband-image";
        if (fourBandReaderProps == null) {
            return;
        }
        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi()
                .createReaderInstance(fourBandReaderProps);

        ArcSDEPooledConnection con = null;
        try {
            con = pool.getConnection();

            SeRasterBand[] bands = rasterAttr.getBands();
            HashMap bandMapper = new HashMap();
            // red band
            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));
            // blue band
            bandMapper.put(Integer.valueOf((int) bands[1].getId().longValue()), Integer.valueOf(1));
            // green band
            bandMapper.put(Integer.valueOf((int) bands[2].getId().longValue()), Integer.valueOf(2));

            BufferedImage image;

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1, 2, 3 });
            rParam.setConnection(con);
            rParam.setSourceRegion(new Rectangle(0, 0, 1000, 1000));
            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
            rParam.setDestination(image);
            rParam.setBandMapper(bandMapper);

            reader.read(9, rParam);
            // ImageIO.write(image, "PNG", new File(imgPrefix + "1.png"));
            assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(image,
                    imgPrefix + "1.png"));

            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
            rParam.setDestination(image);
            rParam.setDestinationOffset(new Point(100, 100));
            reader.read(8, rParam);
            // ImageIO.write(image, "PNG", new File(imgPrefix + "2.png"));
            assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(image,
                    imgPrefix + "2.png"));

            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
            rParam.setDestination(image);
            rParam.setSourceRegion(new Rectangle(43, 30, 1000, 1000));
            rParam.setDestinationOffset(new Point(0, 0));
            reader.read(8, rParam);
            // ImageIO.write(image, "PNG", new File(imgPrefix + "3.png"));
            assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(image,
                    imgPrefix + "3.png"));
        } finally {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        }
    }

    /**
     * Tests reading the first three bands of a 4-band image (1 = RED, 2 = GREEN, 3 = BLUE, 4 =
     * NEAR_INFRARED) into a TYPE_INT_ARGB image. Bands are mapped as follows: rasterband 1 => image
     * band 1 (red, hopefully!) rasterband 2 => image band 2 (green, hopefully!) rasterband 3 =>
     * image band 3 (blue, hopefully!) Question: what do we do about image band 0 (the alpha band?)
     * Ignoring it for now.
     */
    public void testRead4BandIntoTYPE_INT_ARGBImage() throws Exception {

        String imgPrefix = "type_int_argb-fourband-image";
        if (fourBandReaderProps == null) {
            return;
        }
        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi()
                .createReaderInstance(fourBandReaderProps);

        ArcSDEPooledConnection conn = null;
        try {
            conn = pool.getConnection();

            SeRasterBand[] bands = rasterAttr.getBands();
            HashMap bandMapper = new HashMap();
            // red band
            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));
            // blue band
            bandMapper.put(Integer.valueOf((int) bands[1].getId().longValue()), Integer.valueOf(1));
            // green band
            bandMapper.put(Integer.valueOf((int) bands[2].getId().longValue()), Integer.valueOf(2));

            BufferedImage image;

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1, 2, 3 });
            rParam.setConnection(conn);
            rParam.setSourceRegion(new Rectangle(0, 0, 1000, 1000));
            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
            int[] opaque = new int[image.getWidth() * image.getHeight()];
            for (int i = 0; i < opaque.length; i++) {
                opaque[i] = 0xff;
            }
            image.getSampleModel().setSamples(0, 0, image.getWidth(), image.getHeight(), 3, opaque,
                    image.getRaster().getDataBuffer());
            rParam.setDestination(image);
            rParam.setBandMapper(bandMapper);

            reader.read(9, rParam);
            // ImageIO.write(image, "PNG", new File(imgPrefix + "1.png"));
            assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(image,
                    imgPrefix + "1.png"));

            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
            opaque = new int[image.getWidth() * image.getHeight()];
            for (int i = 0; i < opaque.length; i++) {
                opaque[i] = 0xff;
            }
            image.getSampleModel().setSamples(0, 0, image.getWidth(), image.getHeight(), 3, opaque,
                    image.getRaster().getDataBuffer());
            rParam.setDestination(image);
            rParam.setDestinationOffset(new Point(100, 100));
            reader.read(8, rParam);
            // ImageIO.write(image, "PNG", new File(imgPrefix + "2.png"));
            assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(image,
                    imgPrefix + "2.png"));

            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
            opaque = new int[image.getWidth() * image.getHeight()];
            for (int i = 0; i < opaque.length; i++) {
                opaque[i] = 0xff;
            }
            image.getSampleModel().setSamples(0, 0, image.getWidth(), image.getHeight(), 3, opaque,
                    image.getRaster().getDataBuffer());
            rParam.setDestination(image);
            rParam.setSourceRegion(new Rectangle(43, 30, 1000, 1000));
            rParam.setDestinationOffset(new Point(0, 0));
            reader.read(8, rParam);
            // ImageIO.write(image, "PNG", new File(imgPrefix + "3.png"));
            assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(image,
                    imgPrefix + "3.png"));
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Tests reading the first three bands of a 4-band image (1 = RED, 2 = GREEN, 3 = BLUE, 4 =
     * NEAR_INFRARED) into a TYPE_INT_ARGB image. Bands are mapped as follows: rasterband 1 => image
     * band 1 (red, hopefully!) rasterband 2 => image band 2 (green, hopefully!) rasterband 3 =>
     * image band 3 (blue, hopefully!) Question: what do we do about image band 0 (the alpha band?)
     * Ignoring it for now.
     */
    public void testRead4BandIntoTYPE_3BYTE_BGRImage() throws Exception {

        String imgPrefix = "type_3byte_bgr-4band-image";
        if (fourBandReaderProps == null) {
            return;
        }
        ArcSDERasterReader reader = (ArcSDERasterReader) new ArcSDERasterReaderSpi()
                .createReaderInstance(fourBandReaderProps);

        ArcSDEPooledConnection conn = null;
        try {
            conn = pool.getConnection();
            SeRasterBand[] bands = rasterAttr.getBands();
            HashMap bandMapper = new HashMap();
            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));
            bandMapper.put(Integer.valueOf((int) bands[1].getId().longValue()), Integer.valueOf(1));
            bandMapper.put(Integer.valueOf((int) bands[2].getId().longValue()), Integer.valueOf(2));

            BufferedImage image;

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1, 2, 3 });
            rParam.setConnection(conn);
            rParam.setSourceRegion(new Rectangle(0, 0, 1000, 1000));
            image = new BufferedImage(1000, 1000, BufferedImage.TYPE_4BYTE_ABGR);
            int[] opaque = new int[image.getWidth() * image.getHeight()];
            for (int i = 0; i < opaque.length; i++) {
                opaque[i] = 0xff;
            }
            image.getSampleModel().setSamples(0, 0, image.getWidth(), image.getHeight(), 3, opaque,
                    image.getRaster().getDataBuffer());
            rParam.setDestination(image);
            rParam.setBandMapper(bandMapper);

            reader.read(9, rParam);
            // ImageIO.write(image, "PNG", new File(imgPrefix + "1.png"));
            assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(image,
                    imgPrefix + "1.png"));
        } finally {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
    }
}
