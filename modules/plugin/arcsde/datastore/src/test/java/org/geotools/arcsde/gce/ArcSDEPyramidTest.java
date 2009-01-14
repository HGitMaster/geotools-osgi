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
import static org.junit.Assert.assertTrue;

import java.awt.Dimension;
import java.awt.Rectangle;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.gce.RasterTestData.RasterTableName;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterColumn;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

/**
 * Tests the functionality of the ArcSDE raster-display package to read rasters from an ArcSDE
 * database
 * 
 * @author Saul Farber, (based on ArcSDEPoolTest by Gabriel Roldan)
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/test/java
 *         /org/geotools/arcsde/gce/ArcSDEPyramidTest.java $
 * @version $Id: ArcSDEPyramidTest.java 32216 2009-01-14 01:51:57Z groldan $
 */
public class ArcSDEPyramidTest {

    private static RasterTestData testData;

    private static ArcSDEConnectionPool pool;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        testData = new RasterTestData();
        testData.setUp();
        pool = testData.getConnectionPool();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        testData.tearDown();
    }

    @Test
    public void testArcSDEPyramidHypothetical() throws Exception {

        ArcSDEPyramid pyramid = new ArcSDEPyramid(10, 10, 2);
        pyramid.addPyramidLevel(0, new SeExtent(0, 0, 100, 100), null, null, 10, 10, new Dimension(
                100, 100));
        pyramid.addPyramidLevel(1, new SeExtent(0, 0, 100, 100), null, null, 5, 5, new Dimension(
                50, 50));

        RasterQueryInfo ret = pyramid.fitExtentToRasterPixelGrid(new ReferencedEnvelope(0, 10, 0,
                10, null), 0);
        assertTrue(ret.envelope.equals(new ReferencedEnvelope(0, 10, 0, 10, null)));
        assertTrue(ret.image.width == 10 && ret.image.height == 10);

        ret = pyramid.fitExtentToRasterPixelGrid(new ReferencedEnvelope(0, 9, 0, 9, null), 0);
        assertTrue(ret.envelope.intersects((BoundingBox) new ReferencedEnvelope(0, 9, 0, 9, null)));
        assertTrue(ret.image.width == 9 && ret.image.height == 9);

        ret = pyramid.fitExtentToRasterPixelGrid(new ReferencedEnvelope(15, 300, 15, 300, null), 1);
        assertTrue(ret.envelope.equals(new ReferencedEnvelope(14, 300, 14, 300, null)));
        assertTrue(ret.image.width == 143 && ret.image.height == 143);

        ret = pyramid.fitExtentToRasterPixelGrid(
                new ReferencedEnvelope(-100, 200, -100, 200, null), 1);
        assertTrue(ret.envelope.equals(new ReferencedEnvelope(-100, 200, -100, 200, null)));
        assertTrue(ret.image.width == 150 && ret.image.height == 150);
    }

    /*
     * NEED TO PORT TO NEW RASTER TEST FRAMEWORK (use RasterTestData, loadable sample data, etc)
     */
    @Test
    public void testArcSDEPyramidThreeBand() throws Exception {

        testData.loadRGBRaster();
        final String tableName = testData.getRasterTableName(RasterTableName.RGB);
        ArcSDEPooledConnection conn = pool.getConnection();
        final SeRasterAttr rAttr;
        try {
            SeQuery q = new SeQuery(conn, new String[] { "RASTER" }, new SeSqlConstruct(tableName));
            q.prepareQuery();
            q.execute();
            SeRow r = q.fetch();
            rAttr = r.getRaster(0);
        } catch (SeException se) {
            conn.close();
            throw new ArcSdeException(se);
        }

        SeObjectId rasterColumnId = rAttr.getRasterColumnId();
        SeRasterColumn rasterColumn = new SeRasterColumn(conn, rasterColumnId);
        SeCoordinateReference coordRef = rasterColumn.getCoordRef();
        String coordRefWKT = coordRef.getCoordSysDescription();
        CoordinateReferenceSystem crs = CRS.parseWKT(coordRefWKT); // CRS.decode(testData.
        // getRasterTestDataProperty
        // ("tableCRS"));
        ArcSDEPyramid pyramid = new ArcSDEPyramid(rAttr, crs);
        conn.close();

        int offset = pyramid.getPyramidLevel(0).getYOffset();
        // assertTrue(offset != 0);

        ReferencedEnvelope env = new ReferencedEnvelope(33000.25, 48000.225, 774000.25, 783400.225,
                crs);
        Rectangle imageSize = new Rectangle(256, 128);
        int imgLevel = pyramid.pickOptimalRasterLevel(env, imageSize);
        RasterQueryInfo ret = pyramid.fitExtentToRasterPixelGrid(env, imgLevel);
        assertEquals(6, imgLevel);
        // LOGGER.info(ret.image + "");
        // LOGGER.info(ret.envelope + "");
        assertTrue(ret.image.equals(new Rectangle(-1, 5581, 470, 295)));
        assertTrue(ret.envelope.contains((BoundingBox) env));

        env = new ReferencedEnvelope(40000.0, 41001.0, 800000.0, 801001.0, crs);
        imageSize = new Rectangle(1000, 1000);
        imgLevel = pyramid.pickOptimalRasterLevel(env, imageSize);
        ret = pyramid.fitExtentToRasterPixelGrid(env, imgLevel);
        assertTrue(imgLevel == 1);
        // LOGGER.info(ret.image + "");
        // LOGGER.info(ret.envelope + "");
        assertTrue(ret.image.equals(new Rectangle(6999, 160999, 1002, 1002)));
        assertTrue(ret.envelope.contains((BoundingBox) env));

    }

    @Test
    public void testArcSDEPyramidFourBand() throws Exception {

        testData.loadRGBARaster();
        
        ArcSDEPooledConnection conn = pool.getConnection();
        SeRasterAttr rAttr;
        try {
            String tableName = testData.getRasterTableName(RasterTableName.RGBA);
            SeQuery q = new SeQuery(conn, new String[] { "RASTER" }, new SeSqlConstruct(tableName));
            q.prepareQuery();
            q.execute();
            SeRow r = q.fetch();
            rAttr = r.getRaster(0);
        } catch (SeException se) {
            conn.close();
            throw new RuntimeException(se.getSeError().getErrDesc(), se);
        }

        SeRasterColumn column = new SeRasterColumn(conn, rAttr.getRasterColumnId());
        SeCoordinateReference coordRef = column.getCoordRef();
        String wkt = coordRef.getCoordSysDescription();
        CoordinateReferenceSystem crs = CRS.parseWKT(wkt);
        ArcSDEPyramid pyramid = new ArcSDEPyramid(rAttr, crs);
        conn.close();

        int offset = pyramid.getPyramidLevel(0).getYOffset();
        assertEquals(0, offset);

        // bigger than the image size
        ReferencedEnvelope env = new ReferencedEnvelope(0, 1024, 0, 512, crs);
        // actual image size
        Rectangle imageSize = new Rectangle(256, 128);
        int imgLevel = pyramid.pickOptimalRasterLevel(env, imageSize);
        RasterQueryInfo ret = pyramid.fitExtentToRasterPixelGrid(env, imgLevel);
        assertEquals(1, imgLevel);
        // LOGGER.info(ret.image + "");
        // LOGGER.info(ret.envelope + "");
        assertEquals(new Rectangle(0, 0, 258, 129), ret.image);
        assertTrue(ret.envelope.contains((BoundingBox) env));

        // TODO
        // env = new ReferencedEnvelope(40000, 40000, 800000, 801000, crs);
        // imageSize = new Rectangle(100000, 100000);
        // imgLevel = pyramid.pickOptimalRasterLevel(env, imageSize);
        // ret = pyramid.fitExtentToRasterPixelGrid(env, imgLevel);
        // assertEquals(2, imgLevel);
        // // LOGGER.info(ret.image + "");
        // // LOGGER.info(ret.envelope + "");
        // assertEquals(new Rectangle(6999, 160999, 1002, 1002), ret.image);
        // assertTrue(ret.envelope.contains((BoundingBox) env));

    }

}
