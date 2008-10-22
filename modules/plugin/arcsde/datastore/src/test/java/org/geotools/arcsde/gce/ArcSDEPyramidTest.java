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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.InputStream;
import java.util.Properties;

import junit.framework.TestCase;

import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.arcsde.pool.SessionPool;
import org.geotools.arcsde.pool.SessionPoolFactory;
import org.geotools.arcsde.pool.ISession;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

/**
 * Tests the functionality of the ArcSDE raster-display package to read rasters from an ArcSDE
 * database
 * 
 * @author Saul Farber, (based on ArcSDEPoolTest by Gabriel Roldan)
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/test/java/org/geotools/arcsde/gce/ArcSDEPyramidTest.java $
 * @version $Id: ArcSDEPyramidTest.java 30722 2008-06-13 18:15:42Z acuster $
 */
public class ArcSDEPyramidTest extends TestCase {

    private SessionPool pool;

    private Properties conProps;

    public ArcSDEPyramidTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        conProps = new Properties();
        InputStream in = org.geotools.test.TestData.url(null, "raster-testparams.properties")
                .openStream();
        conProps.load(in);
        in.close();
        pool = SessionPoolFactory.getInstance().createSharedPool(
                new ArcSDEConnectionConfig(conProps));

    }

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
    public void donttestArcSDEPyramidThreeBand() throws Exception {

        ISession session = pool.getSession();
        SeRasterAttr rAttr;
        try {
            SeQuery q = session.createAndExecuteQuery(new String[] { "RASTER" },
                    new SeSqlConstruct(conProps.getProperty("threebandtable")));
            SeRow r = q.fetch();
            rAttr = r.getRaster(0);
        } catch (SeException se) {
            session.dispose();
            throw new RuntimeException(se.getSeError().getErrDesc(), se);
        }

        CoordinateReferenceSystem crs = CRS.decode(conProps.getProperty("tableCRS"));
        ArcSDEPyramid pyramid = new ArcSDEPyramid(rAttr, crs);
        session.dispose();

        assertTrue(pyramid.getPyramidLevel(0).getYOffset() != 0);

        ReferencedEnvelope env = new ReferencedEnvelope(33000.25, 48000.225, 774000.25, 783400.225,
                crs);
        Rectangle imageSize = new Rectangle(256, 128);
        int imgLevel = pyramid.pickOptimalRasterLevel(env, imageSize);
        RasterQueryInfo ret = pyramid.fitExtentToRasterPixelGrid(env, imgLevel);
        assertTrue(imgLevel == 6);
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

    /*
     * NEED TO PORT TO NEW RASTER TEST FRAMEWORK (use RasterTestData, loadable sample data, etc)
     */
    public void testArcSDEPyramidFourBand() throws Exception {

        ISession session = pool.getSession();
        SeRasterAttr rAttr;
        try {
            String tableName = conProps.getProperty("fourbandtable");
            if( tableName == null ) return;
			SeQuery q = session.createAndExecuteQuery(new String[] { "RASTER" },
                    new SeSqlConstruct(tableName));
            SeRow r = q.fetch();
            rAttr = r.getRaster(0);
        } catch (SeException se) {
            session.dispose();
            throw new RuntimeException(se.getSeError().getErrDesc(), se);
        }

        CoordinateReferenceSystem crs = CRS.decode(conProps.getProperty("tableCRS"));
        ArcSDEPyramid pyramid = new ArcSDEPyramid(rAttr, crs);
        session.dispose();

        assertTrue(pyramid.getPyramidLevel(0).getYOffset() != 0);

        ReferencedEnvelope env = new ReferencedEnvelope(33000.25, 48000.225, 774000.25, 783400.225,
                crs);
        Rectangle imageSize = new Rectangle(256, 128);
        int imgLevel = pyramid.pickOptimalRasterLevel(env, imageSize);
        RasterQueryInfo ret = pyramid.fitExtentToRasterPixelGrid(env, imgLevel);
        assertTrue(imgLevel == 6);
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

}
