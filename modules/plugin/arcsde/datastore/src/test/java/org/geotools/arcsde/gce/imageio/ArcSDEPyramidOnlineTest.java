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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Dimension;
import java.awt.Rectangle;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.gce.RasterTestData;
import org.geotools.arcsde.gce.imageio.ArcSDEPyramid.RasterQueryInfo;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeConnection;
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
 * @version $Id: ArcSDEPyramidOnlineTest.java 32417 2009-02-05 16:51:43Z groldan $
 */
public class ArcSDEPyramidOnlineTest {

    private static RasterTestData testData;

    private static ArcSDEConnectionPool pool;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        testData = new RasterTestData();
        testData.setUp();
        testData.load1bitRaster();
        pool = testData.getConnectionPool();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        testData.tearDown();
    }

    /*
     * NEED TO PORT TO NEW RASTER TEST FRAMEWORK (use RasterTestData, loadable sample data, etc)
     */
    @Test
    public void testArcSDEPyramidThreeBand() throws Exception {

        final String tableName = testData.loadRGBRaster();
        SeConnection conn = pool.getConnection();
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

        final String tableName = testData.loadRGBARaster();

        SeConnection conn = pool.getConnection();
        SeRasterAttr rAttr;
        try {
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
