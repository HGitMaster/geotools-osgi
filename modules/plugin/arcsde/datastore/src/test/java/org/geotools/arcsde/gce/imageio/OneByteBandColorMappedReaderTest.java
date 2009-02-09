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

import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.geotools.arcsde.gce.ArcSDERasterReader;
import org.geotools.arcsde.gce.ArcSDERasterReaderSpi;
import org.geotools.arcsde.gce.RasterTestData;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterBand;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;

/**
 * 
 * @author Gabriel Roldan
 */
public class OneByteBandColorMappedReaderTest {

    static RasterTestData rasterTestData;

    static SeRasterAttr rasterAttr;

    static HashMap<String, Object> readerProps;

    static Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    static String tableName;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        rasterTestData = new RasterTestData();
        rasterTestData.setUp();
        // rasterTestData.loadRGBColorMappedRaster();
        try {
            tableName = rasterTestData.load8bitUnsignedColorMappedRaster();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        SeQuery query = null;
        ArcSDEPyramid pyramid;
        SeRow row;

        final ArcSDEPooledConnection conn = rasterTestData.getConnectionPool().getConnection();
        try {
            query = new SeQuery(conn, new String[] { "RASTER" }, new SeSqlConstruct(tableName));
            query.prepareQuery();
            query.execute();
            row = query.fetch();
            SeColumnDefinition columnDef = row.getColumnDef(0);
            int type = columnDef.getType();
            rasterAttr = row.getRaster(0);

            SeRaster raster = rasterAttr.getRasterInfo();
            raster.getInfoById(raster.getRasterId());

            SeRasterBand[] bands = raster.getBands();
            SeRasterBand band = bands[0];
            assertTrue(band.hasColorMap());

            // DataBuffer colorMapData = band.getColorMapData();
            // System.err.println("retrieveing color map type");
            // int colorMapType = band.getColorMapType();
            // System.err.println("-- color map type: " + colorMapType);
            //
            // int colorMapNumBanks = band.getColorMapNumBanks();
            // int colorMapDataType = band.getColorMapDataType();
            // int colorMapNumEntries = band.getColorMapNumEntries();
            // SeRasterBandColorMap colorMap = band.getColorMap();
            // System.out.println("color map: " + colorMap);
            // query.close();

            // SeRasterColumn rcol = new SeRasterColumn(conn, rasterAttr.getRasterColumnId());

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
            if (query != null) {
                query.close();
            }
            conn.close();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        rasterTestData.tearDown();
    }

    @Test
    public void testRead8bitCM() throws Exception {
        ArcSDERasterReaderSpi spi = new ArcSDERasterReaderSpi();
        ArcSDERasterReader reader = spi.createReaderInstance(readerProps);

        ArcSDEPooledConnection conn = rasterTestData.getConnectionPool().getConnection();
        try {
            SeRasterBand[] bands = rasterAttr.getBands();
            // ckeckpoint
            assertTrue(bands[0].hasColorMap());

            HashMap<Integer, Integer> bandMapper = new HashMap<Integer, Integer>();

            bandMapper.put(Integer.valueOf((int) bands[0].getId().longValue()), Integer.valueOf(0));

            BufferedImage image;

            final Point dataOffset = new Point(0, 0);
            final Point imageOffset = new Point(0, 0);
            final int w = 256, h = 256;

            image = new BufferedImage(w + imageOffset.x, h + imageOffset.y,
                    BufferedImage.TYPE_BYTE_INDEXED);

            ArcSDERasterImageReadParam rParam = new ArcSDERasterImageReadParam();
            rParam.setSourceBands(new int[] { 1 });
            rParam.setConnection(conn);
            rParam.setSourceRegion(new Rectangle(dataOffset.x, dataOffset.y, w, h));
            rParam.setDestination(image);
            rParam.setDestinationOffset(imageOffset);
            rParam.setBandMapper(bandMapper);

            reader.read(0, rParam);
            {
                String pathname = "/tmp/"
                        + Thread.currentThread().getStackTrace()[1].getMethodName() + ".png";
                ImageIO.write(image, "PNG", new File(pathname));
                System.out.println(pathname);
            }
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
            conn.close();
        }
    }
}
