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

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
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
 * @version $Id: ArcSDEPyramidTest.java 32472 2009-02-11 17:32:48Z groldan $
 */
public class ArcSDEPyramidTest {

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {

    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {

    }

    /**
     * test case for {@link PyramidInfo#pickOptimalRasterLevel(ReferencedEnvelope, Rectangle)}
     */
    @Test
    public void testPickOptimalPyramidLevel() {

    }

    /**
     * Creates a pyramid equivalent to the one created by ArcSDE: <code>
     * <pre>
     * ArcSDEPyramid[Nº levels: 10, tile size: 63x63
     * Levels:
     *  [0 size: 1013x1021  xRes: 2.0000000000000004  yRes: 2.0  xOffset: 0  yOffset: 0  extent: ReferencedEnvelope[0.0 : 2026.0000000000002, 0.0 : 2042.0]  tilesWide: 17  tilesHigh: 17]
     *  [1 size: 507x511  xRes: 3.996055226824458  yRes: 3.996086105675147  xOffset: 0  yOffset: 0  extent: ReferencedEnvelope[0.0 : 2026.0000000000002, 0.0 : 2042.0]  tilesWide: 9  tilesHigh: 9]
     *  [2 size: 254x256  xRes: 7.976377952755906  yRes: 7.9765625  xOffset: 0  yOffset: 0  extent: ReferencedEnvelope[2.0019762845849804 : 2028.0019762845852, -2.0019607843137237 : 2039.9980392156863]  tilesWide: 5  tilesHigh: 5]
     *  [3 size: 127x128  xRes: 15.889701534343782  yRes: 15.890563725490196  xOffset: 0  yOffset: 0  extent: ReferencedEnvelope[6.005928853754941 : 2023.9980237154152, 2.0019607843137237 : 2035.9941176470588]  tilesWide: 3  tilesHigh: 3]
     *  [4 size: 64x64  xRes: 31.53112648221344  yRes: 31.530882352941177  xOffset: 0  yOffset: 0  extent: ReferencedEnvelope[14.013833992094863 : 2032.005928853755, 10.009803921568619 : 2027.986274509804]  tilesWide: 2  tilesHigh: 2]
     *  [5 size: 32x32  xRes: 62.06126482213439  yRes: 62.06078431372549  xOffset: 0  yOffset: 0  extent: ReferencedEnvelope[30.029644268774707 : 2015.9901185770752, 26.025490196078408 : 2011.9705882352941]  tilesWide: 1  tilesHigh: 1]
     *  [6 size: 16x16  xRes: 120.11857707509883  yRes: 120.11764705882354  xOffset: 0  yOffset: 0  extent: ReferencedEnvelope[62.06126482213439 : 1983.9584980237157, 58.05686274509799 : 1979.9392156862746]  tilesWide: 1  tilesHigh: 1]
     * ]
     * </pre>
     * </code>
     * 
     * @return
     */
    private PyramidInfo createPyramid() {

        PyramidInfo pyramid = new PyramidInfo(1013, 1021);

        final CoordinateReferenceSystem crs = DefaultEngineeringCRS.CARTESIAN_2D;

        pyramid.addPyramidLevel(0, new ReferencedEnvelope(0, 2026, 0, 2042, crs), 0, 0, 17, 17,
                new Dimension(1013, 1021));
        pyramid.addPyramidLevel(1, new ReferencedEnvelope(0, 2026.0000000000002, 0, 2042, crs), 0,
                0, 9, 9, new Dimension(507, 511));

        pyramid.addPyramidLevel(2, new ReferencedEnvelope(2.0019762845849804, 2028.0019762845852,
                -2.0019607843137237, 2039.9980392156863, crs), 0, 0, 5, 5, new Dimension(254, 256));

        pyramid.addPyramidLevel(3, new ReferencedEnvelope(6.005928853754941, 2023.9980237154152,
                2.0019607843137237, 2035.9941176470588, crs), 0, 0, 3, 3, new Dimension(127, 128));

        pyramid.addPyramidLevel(4, new ReferencedEnvelope(4.013833992094863, 2032.005928853755,
                10.009803921568619, 2027.986274509804, crs), 0, 0, 2, 2, new Dimension(64, 64));

        pyramid.addPyramidLevel(5, new ReferencedEnvelope(30.029644268774707, 2015.9901185770752,
                26.025490196078408, 2011.9705882352941, crs), 0, 0, 1, 1, new Dimension(32, 23));

        pyramid.addPyramidLevel(6, new ReferencedEnvelope(62.06126482213439, 1983.9584980237157,
                58.05686274509799, 1979.9392156862746, crs), 0, 0, 1, 1, new Dimension(16, 16));
        return pyramid;
    }

    @Test
    public void testArcSDEPyramidHypothetical() throws Exception {
        PyramidInfo pyramid = createPyramid();
        System.out.println(pyramid);

        RasterTestData testData = new RasterTestData();
        testData.setUp();
        String tableName = testData.getRasterTableName(RasterCellType.TYPE_8BIT_U, 1, false);
        testData.loadTestRaster(tableName, 1, 1013, 1021, RasterCellType.TYPE_8BIT_U, null, true,
                false, SeRaster.SE_INTERPOLATION_NEAREST, 9);

        ArcSDEConnectionPool pool = testData.getConnectionPool();
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
        pyramid = new PyramidInfo(rAttr, crs);
        conn.close();

        System.out.println(pyramid);

        /*
         * ArcSDEPyramid pyramid = new ArcSDEPyramid(10, 10); pyramid.addPyramidLevel(0, new
         * SeExtent(0, 0, 100, 100), null, null, 10, 10, new Dimension( 100, 100));
         * pyramid.addPyramidLevel(1, new SeExtent(0, 0, 100, 100), null, null, 5, 5, new Dimension(
         * 50, 50));
         * 
         * RasterQueryInfo ret = pyramid.fitExtentToRasterPixelGrid(new ReferencedEnvelope(0, 10, 0,
         * 10, null), 0); assertTrue(ret.envelope.equals(new ReferencedEnvelope(0, 10, 0, 10,
         * null))); assertTrue(ret.image.width == 10 && ret.image.height == 10);
         * 
         * ret = pyramid.fitExtentToRasterPixelGrid(new ReferencedEnvelope(0, 9, 0, 9, null), 0);
         * assertTrue(ret.envelope.intersects((BoundingBox) new ReferencedEnvelope(0, 9, 0, 9,
         * null))); assertTrue(ret.image.width == 9 && ret.image.height == 9);
         * 
         * ret = pyramid.fitExtentToRasterPixelGrid(new ReferencedEnvelope(15, 300, 15, 300, null),
         * 1); assertTrue(ret.envelope.equals(new ReferencedEnvelope(14, 300, 14, 300, null)));
         * assertTrue(ret.image.width == 143 && ret.image.height == 143);
         * 
         * ret = pyramid.fitExtentToRasterPixelGrid( new ReferencedEnvelope(-100, 200, -100, 200,
         * null), 1); assertTrue(ret.envelope.equals(new ReferencedEnvelope(-100, 200, -100, 200,
         * null))); assertTrue(ret.image.width == 150 && ret.image.height == 150);
         */
    }

}