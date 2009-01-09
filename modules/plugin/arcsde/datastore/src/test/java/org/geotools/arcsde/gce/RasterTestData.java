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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.geotools.arcsde.data.TestData;
import org.geotools.arcsde.gce.producer.ArcSDERasterFloatProducerImpl;
import org.geotools.arcsde.gce.producer.ArcSDERasterOneBitPerBandProducerImpl;
import org.geotools.arcsde.gce.producer.ArcSDERasterOneBytePerBandProducerImpl;
import org.geotools.arcsde.gce.producer.ArcSDERasterProducer;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEConnectionPoolFactory;
import org.geotools.arcsde.pool.ArcSDEPooledConnection;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.data.DataSourceException;
import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRaster;
import com.esri.sde.sdk.client.SeRasterAttr;
import com.esri.sde.sdk.client.SeRasterColumn;
import com.esri.sde.sdk.client.SeRasterConstraint;
import com.esri.sde.sdk.client.SeRegistration;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeTable;
import com.esri.sde.sdk.pe.PeFactory;
import com.esri.sde.sdk.pe.PePCSDefs;
import com.esri.sde.sdk.pe.PeProjectedCS;

public class RasterTestData {

    private TestData testData;

    private Logger LOGGER = Logging.getLogger(this.getClass());

    private ArcSDEConnectionPool _pool;

    public void setUp() throws IOException {
        // load a raster dataset into SDE
        testData = new TestData();
        testData.setUp();
    }

    public ArcSDEConnectionPool getConnectionPool() throws DataSourceException {
        if (this._pool == null) {
            ArcSDEConnectionPoolFactory pfac = ArcSDEConnectionPoolFactory.getInstance();
            ArcSDEConnectionConfig config = new ArcSDEConnectionConfig(testData.getConProps());
            this._pool = pfac.createPool(config);
        }
        return this._pool;
    }

    /*
     * Names for the raster data test tables
     */
    public String get1bitRasterTableName() throws UnavailableArcSDEConnectionException, IOException {
        return testData.getTempTableName() + "_ONEBITRASTER";
    }

    public String getRGBRasterTableName() throws UnavailableArcSDEConnectionException, IOException {
        return testData.getTempTableName() + "_RGBRASTER";
    }

    public String getRGBARasterTableName() throws UnavailableArcSDEConnectionException, IOException {
        return testData.getTempTableName() + "_RGBARASTER";
    }

    public String getRGBColorMappedRasterTableName() throws UnavailableArcSDEConnectionException,
            IOException {
        return testData.getTempTableName() + "_RGBRASTER_CM";
    }

    public String getGrayScaleOneByteRasterTableName() throws UnavailableArcSDEConnectionException,
            IOException {
        return testData.getTempTableName() + "_GRAYSCALERASTER";
    }

    public String getFloatRasterTableName() throws UnavailableArcSDEConnectionException,
            IOException {
        return testData.getTempTableName() + "_FLOATRASTER";
    }

    public String getRasterTestDataProperty(String propName) {
        return testData.getConProps().getProperty(propName);
    }

    /**
     * Loads the 1bit raster test data into the table given in
     * {@link RasterTestData#get1bitRasterTableName()}
     * 
     * @throws Exception
     */
    public void load1bitRaster() throws Exception {
        ArcSDEPooledConnection conn = getConnectionPool().getConnection();
        final String tableName = get1bitRasterTableName();

        // clean out the table if it's currently in-place
        testData.deleteTable(tableName);
        // build the base business table. We'll add the raster data to it in a bit
        createRasterBusinessTempTable(tableName, conn);
        conn.close();

        SeExtent imgExtent = new SeExtent(231000, 898000, 231000 + 500, 898000 + 500);
        SeCoordinateReference crs = getSeCRSFromPeProjectedCSId(PePCSDefs.PE_PCS_NAD_1983_HARN_MA_M);
        String rasterFilename = testData.getConProps().getProperty("sampledata.onebitraster");
        ArcSDERasterProducer producer = new ArcSDERasterOneBitPerBandProducerImpl();

        importRasterImage(tableName, crs, rasterFilename, SeRaster.SE_PIXEL_TYPE_1BIT, imgExtent,
                producer);
    }

    /**
     * Loads the 1bit raster test data into the table given in
     * {@link RasterTestData#get1bitRasterTableName()}
     * 
     * @throws Exception
     */
    public void loadRGBRaster() throws Exception {
        final String tableName = getRGBRasterTableName();

        // clean out the table if it's currently in-place
        testData.deleteTable(tableName);
        // build the base business table. We'll add the raster data to it in a bit
        ArcSDEPooledConnection conn = getConnectionPool().getConnection();
        createRasterBusinessTempTable(tableName, conn);
        conn.close();

        SeExtent imgExtent = new SeExtent(231000, 898000, 231000 + 501, 898000 + 501);
        SeCoordinateReference crs = getSeCRSFromPeProjectedCSId(PePCSDefs.PE_PCS_NAD_1983_HARN_MA_M);
        String rasterFilename = testData.getConProps().getProperty("sampledata.rgbraster");
        ArcSDERasterProducer prod = new ArcSDERasterOneBytePerBandProducerImpl();

        importRasterImage(tableName, crs, rasterFilename, SeRaster.SE_PIXEL_TYPE_8BIT_U, imgExtent,
                prod);
    }

    public void loadRGBColorMappedRaster() throws Exception {
        final String tableName = getRGBColorMappedRasterTableName();

        // clean out the table if it's currently in-place
        testData.deleteTable(tableName);

        // build the base business table. We'll add the raster data to it in a bit
        // Note that this DOESN'T LOAD THE COLORMAP RIGHT NOW.
        ArcSDEPooledConnection conn = getConnectionPool().getConnection();
        createRasterBusinessTempTable(tableName, conn);
        conn.close();

        SeExtent imgExtent = new SeExtent(231000, 898000, 231000 + 500, 898000 + 500);
        SeCoordinateReference crs = getSeCRSFromPeProjectedCSId(PePCSDefs.PE_PCS_NAD_1983_HARN_MA_M);
        String rasterFilename = testData.getConProps().getProperty(
                "sampledata.rgbraster-colormapped");
        ArcSDERasterProducer prod = new ArcSDERasterOneBytePerBandProducerImpl();

        importRasterImage(tableName, crs, rasterFilename, SeRaster.SE_PIXEL_TYPE_8BIT_U, imgExtent,
                prod);
    }

    public void loadOneByteGrayScaleRaster() throws Exception {
        final String tableName = getGrayScaleOneByteRasterTableName();

        // clean out the table if it's currently in-place
        testData.deleteTable(tableName);
        // build the base business table. We'll add the raster data to it in a bit
        // Note that this DOESN'T LOAD THE COLORMAP RIGHT NOW.
        ArcSDEPooledConnection conn = getConnectionPool().getConnection();
        createRasterBusinessTempTable(tableName, conn);
        conn.close();

        SeExtent imgExtent = new SeExtent(231000, 898000, 231000 + 500, 898000 + 500);
        SeCoordinateReference crs = getSeCRSFromPeProjectedCSId(PePCSDefs.PE_PCS_NAD_1983_HARN_MA_M);
        String rasterFilename = testData.getConProps().getProperty(
                "sampledata.onebyteonebandraster");
        ArcSDERasterProducer prod = new ArcSDERasterOneBytePerBandProducerImpl();

        importRasterImage(tableName, crs, rasterFilename, SeRaster.SE_PIXEL_TYPE_8BIT_U, imgExtent,
                prod);
    }

    public void loadFloatRaster() throws Exception {
        final String tableName = getFloatRasterTableName();

        // clean out the table if it's currently in-place
        testData.deleteTable(tableName);
        // build the base business table. We'll add the raster data to it in a bit
        // Note that this DOESN'T LOAD THE COLORMAP RIGHT NOW.
        ArcSDEPooledConnection conn = getConnectionPool().getConnection();
        createRasterBusinessTempTable(tableName, conn);
        conn.close();

        SeExtent imgExtent = new SeExtent(245900, 899600, 246300, 900000);
        SeCoordinateReference crs = getSeCRSFromPeProjectedCSId(PePCSDefs.PE_PCS_NAD_1983_HARN_MA_M);
        String rasterFilename = testData.getConProps().getProperty("sampledata.floatraster");
        ArcSDERasterProducer prod = new ArcSDERasterFloatProducerImpl();

        importRasterImage(tableName, crs, rasterFilename, SeRaster.SE_PIXEL_TYPE_32BIT_REAL,
                imgExtent, prod);
    }

    public SeCoordinateReference getSeCRSFromPeProjectedCSId(int PeProjectedCSId) {
        SeCoordinateReference crs;
        try {
            PeProjectedCS pcs = (PeProjectedCS) PeFactory.factory(PeProjectedCSId);
            crs = new SeCoordinateReference();
            crs.setCoordSysByDescription(pcs.toString());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return crs;
    }

    public void createRasterBusinessTempTable(String tableName, ArcSDEPooledConnection conn)
            throws Exception {

        SeColumnDefinition[] colDefs = new SeColumnDefinition[1];
        SeTable table = new SeTable(conn, tableName);

        // first column to be SDE managed feature id
        colDefs[0] = new SeColumnDefinition("ROW_ID", SeColumnDefinition.TYPE_INTEGER, 10, 0, false);
        table.create(colDefs, testData.getConfigKeyword());

        /*
         * Register the column to be used as feature id and managed by sde
         */
        SeRegistration reg = new SeRegistration(conn, table.getName());
        LOGGER.fine("setting rowIdColumnName to ROW_ID in table " + reg.getTableName());
        reg.setRowIdColumnName("ROW_ID");
        final int rowIdColumnType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_SDE;
        reg.setRowIdColumnType(rowIdColumnType);
        reg.alter();
    }

    public void importRasterImage(final String tableName, SeCoordinateReference crs,
            final String rasterFilename, final int sePixelType, SeExtent extent,
            ArcSDERasterProducer prod) throws Exception {
        importRasterImage(tableName, crs, rasterFilename, sePixelType, extent, prod, null);
    }

    public void importRasterImage(final String tableName, final SeCoordinateReference crs,
            final String rasterFilename, final int sePixelType, final SeExtent extent,
            final ArcSDERasterProducer prod, final IndexColorModel colorModel) throws Exception {

        final ArcSDEPooledConnection conn = getConnectionPool().getConnection();
        try {
            // much of this code is from
            // http://edndoc.esri.com/arcsde/9.2/concepts/rasters/dataloading/dataloading.htm
            SeRasterColumn rasCol = new SeRasterColumn(conn);
            rasCol.setTableName(tableName);
            rasCol.setDescription("Sample geotools ArcSDE raster test-suite data.");
            rasCol.setRasterColumnName("RASTER");
            rasCol.setCoordRef(crs);
            rasCol.setConfigurationKeyword(testData.getConfigKeyword());

            rasCol.create();

            // now start loading the actual raster data
            BufferedImage sampleImage = ImageIO.read(org.geotools.test.TestData.getResource(null,
                    rasterFilename));

            int imageWidth = sampleImage.getWidth(), imageHeight = sampleImage.getHeight();

            SeRasterAttr attr = new SeRasterAttr(true);
            attr.setImageSize(imageWidth, imageHeight, sampleImage.getSampleModel().getNumBands());
            attr.setTileSize(128, 128);
            attr.setPixelType(sePixelType);
            attr.setCompressionType(SeRaster.SE_COMPRESSION_NONE);
            // no pyramiding
            // attr.setPyramidInfo(3, true, SeRaster.SE_INTERPOLATION_BILINEAR);
            attr.setMaskMode(false);
            attr.setImportMode(false);

            attr.setExtent(extent);
            // attr.setImageOrigin();

            prod.setSeRasterAttr(attr);
            prod.setSourceImage(sampleImage);
            attr.setRasterProducer(prod);

            try {
                SeInsert insert = new SeInsert(conn);
                insert.intoTable(tableName, new String[] { "RASTER" });
                // no buffered writes on raster loads
                insert.setWriteMode(false);
                SeRow row = insert.getRowToSet();
                row.setRaster(0, attr);

                insert.execute();
                insert.close();
            } catch (SeException se) {
                se.printStackTrace();
                throw se;
            }

            // if there's a colormap to insert, let's add that too
            if (colorModel != null) {
                attr = getRasterAttributes(tableName, new Rectangle(0, 0, 0, 0), 0, new int[] { 1 });
                // attr.getBands()[0].setColorMap(SeRaster.SE_COLORMAP_DATA_BYTE, );
                // NOT IMPLEMENTED FOR NOW!
            }
        } finally {
            conn.close();
        }
    }

    public void tearDown() throws Exception {
        // destroy all sample tables;
        testData.deleteTable(get1bitRasterTableName());
        testData.deleteTable(getRGBRasterTableName());
        testData.deleteTable(getRGBARasterTableName());
        testData.deleteTable(getGrayScaleOneByteRasterTableName());
        testData.deleteTable(getRGBColorMappedRasterTableName());
        testData.deleteTable(getFloatRasterTableName());
    }

    /**
     * convenience method to test if two images are identical in their RGB pixel values
     * 
     * @param image
     * @param fileName
     * @return
     * @throws IOException
     */
    public static boolean imageEquals(RenderedImage image, String fileName) throws IOException {
        InputStream in = org.geotools.test.TestData.url(null, fileName).openStream();
        BufferedImage expected = ImageIO.read(in);

        return imageEquals(image, expected);
    }

    public static boolean imageEquals(RenderedImage image1, RenderedImage image2) {
        return imageEquals(image1, image2, true);
    }

    /**
     * convenience method to test if two images are identical in their RGB pixel values
     * 
     * @param image1
     * @param image2
     * @return
     */
    public static boolean imageEquals(RenderedImage image1, RenderedImage image2,
            boolean ignoreAlpha) {

        final int h = image1.getHeight();
        final int w = image2.getWidth();

        int skipBand = -1;
        if (ignoreAlpha) {
            skipBand = 3;
        }

        for (int b = 0; b < image1.getData().getNumBands(); b++) {
            if (b == skipBand)
                continue;
            int[] img1data = image1.getData().getSamples(0, 0, image1.getWidth(),
                    image1.getHeight(), b, new int[image1.getHeight() * image1.getWidth()]);
            int[] img2data = image2.getData().getSamples(0, 0, image1.getWidth(),
                    image1.getHeight(), b, new int[image1.getHeight() * image1.getWidth()]);

            if (!Arrays.equals(img1data, img2data)) {
                // try to figure out which pixel (exactly) was different
                for (int i = 0; i < img1data.length; i++) {
                    if (img1data[i] != img2data[i]) {
                        final int x = i % image1.getWidth();
                        final int y = i / image1.getHeight();
                        System.out.println("pixel " + i + " (possibly " + x + "," + y
                                + ") differs: " + img1data[i] + " != " + img2data[i]);
                        return false;
                    }
                }
            }

            /*
             * for (int xpos = 0; xpos < image1.getWidth(); xpos++) { System.out.println("checking
             * column " + xpos); int[] img1data = image1.getData().getSamples(xpos, 0, 1,
             * image1.getHeight(), b, new int[image1.getHeight()]); int[] img2data =
             * image2.getData().getSamples(xpos, 0, 1, image1.getHeight(), b, new
             * int[image1.getHeight()]); if (!Arrays.equals(img1data, img2data)) {
             * System.out.println("pixels in column " + xpos + " are different"); return false; } }
             */

        }
        return true;
    }

    public SeRasterAttr getRasterAttributes(final String rasterName, Rectangle tiles, int level,
            int[] bands) throws IOException, UnavailableArcSDEConnectionException {

        ArcSDEPooledConnection conn = getConnectionPool().getConnection();

        try {
            SeQuery query = new SeQuery(conn, new String[] { conn.getRasterColumn(rasterName)
                    .getName() }, new SeSqlConstruct(rasterName));
            query.prepareQuery();
            query.execute();
            final SeRow r = query.fetch();

            // Now build a SeRasterConstraint object which queries the db for
            // the right tiles/bands/pyramid level
            SeRasterConstraint rConstraint = new SeRasterConstraint();
            rConstraint.setEnvelope((int) tiles.getMinX(), (int) tiles.getMinY(), (int) tiles
                    .getMaxX(), (int) tiles.getMaxY());
            rConstraint.setLevel(level);
            rConstraint.setBands(bands);

            // Finally, execute the raster query aganist the already-opened
            // SeQuery object which already has an SeRow fetched against it.

            query.queryRasterTile(rConstraint);
            final SeRasterAttr rattr = r.getRaster(0);

            query.close();

            return rattr;
        } catch (SeException se) {
            throw new DataSourceException(se);
        } finally {
            conn.close();
        }
    }
}
