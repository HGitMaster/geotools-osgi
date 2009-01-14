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
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
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
import com.esri.sde.sdk.client.SeRasterConsumer;
import com.esri.sde.sdk.client.SeRasterProducer;
import com.esri.sde.sdk.client.SeRasterRenderedImage;
import com.esri.sde.sdk.client.SeRegistration;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeTable;
import com.esri.sde.sdk.pe.PeFactory;
import com.esri.sde.sdk.pe.PePCSDefs;
import com.esri.sde.sdk.pe.PeProjectedCS;

public class RasterTestData {

    /**
     * Enumeration to create the raster test table names from
     * 
     * @see RasterTestData#getRasterTableName(RasterTableName)
     */
    public enum RasterTableName {
        ONEBIT, RGB, RGB_CM, RGBA, GRAYSCALE, FLOAT

    }

    private TestData testData;

    private Logger LOGGER = Logging.getLogger(this.getClass());

    private ArcSDEConnectionPool _pool;

    public void setUp() throws IOException {
        // load a raster dataset into SDE
        testData = new TestData();
        testData.setUp();
    }

    public void tearDown() throws Exception {
        // destroy all sample tables;
        for(RasterTableName table : RasterTableName.values()){
            String tableName = getRasterTableName(table);
            testData.deleteTable(tableName);
        }
    }

    public ArcSDEConnectionPool getConnectionPool() throws DataSourceException {
        if (this._pool == null) {
            ArcSDEConnectionPoolFactory pfac = ArcSDEConnectionPoolFactory.getInstance();
            ArcSDEConnectionConfig config = new ArcSDEConnectionConfig(testData.getConProps());
            this._pool = pfac.createPool(config);
        }
        return this._pool;
    }

    public String getRasterTableName(RasterTableName forTable) throws IOException {
        String testTableName = testData.getTempTableName() + "_RASTER_" + forTable;
        return testTableName;
    }

    public String getRasterTestDataProperty(String propName) {
        return testData.getConProps().getProperty(propName);
    }

    // public void load1bitRaster() throws Exception {
    // final String tableName = getRasterTableName(RasterTableName.ONEBIT);
    // final int numberOfBands = 1;
    // final int pixelType = SeRaster.SE_PIXEL_TYPE_1BIT;
    // final boolean pyramiding = true;
    // final boolean skipLevelOne = false;
    // final int interpolationType = SeRaster.SE_INTERPOLATION_NEAREST;
    // final IndexColorModel colorModel = null;
    // loadTestRaster(tableName, numberOfBands, pixelType, colorModel, pyramiding, skipLevelOne,
    // interpolationType);
    // }

    /**
     * Loads the 1bit raster test data into the table given in
     * {@link RasterTestData#get1bitRasterTableName()}
     * 
     * @throws Exception
     */
    public void load1bitRaster() throws Exception {
        ArcSDEPooledConnection conn = getConnectionPool().getConnection();
        final String tableName = getRasterTableName(RasterTableName.ONEBIT);

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
        final String tableName = getRasterTableName(RasterTableName.RGB);

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
        final String tableName = getRasterTableName(RasterTableName.RGB_CM);

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
        final String tableName = getRasterTableName(RasterTableName.GRAYSCALE);

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
        final String tableName = getRasterTableName(RasterTableName.FLOAT);

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

    public void loadRGBARaster() throws Exception {
        final String tableName = getRasterTableName(RasterTableName.RGBA);
        final int numberOfBands = 4;
        final int pixelType = SeRaster.SE_PIXEL_TYPE_8BIT_U;
        final boolean pyramiding = true;
        final boolean skipLevelOne = false;
        final int interpolationType = SeRaster.SE_INTERPOLATION_NEAREST;
        final IndexColorModel colorModel = null;
        loadTestRaster(tableName, numberOfBands, pixelType, colorModel, pyramiding, skipLevelOne,
                interpolationType);
    }

    /**
     * Creates a a raster in the database with NO pyramiding, some default settings and the provided
     * parameters.
     * <p>
     * Default settings
     * <ul>
     * <li>CRS: PePCSDefs.PE_PCS_NAD_1983_HARN_MA_M
     * <li>Extent: minx=0, miny=0, maxx=512, maxy=512
     * <li>Width: 256
     * <li>Height: 256
     * <li>Tile size: 64x64 (being less than the recommended minimum of 128, but ok for our testing
     * purposes)
     * <li>Compression: none
     * <li>Pyramid: none
     * </ul>
     * </p>
     * 
     * @param tableName
     *            the name of the table to create
     * @param numberOfBands
     *            the number of bands of the raster
     * @param pixelType
     *            the pixel (cell) depth of the raster bands (one of the {@code
     *            SeRaster#SE_PIXEL_TYPE_*} constants)
     * @param colorModel
     *            the color model to apply to the raster, may be {@code null}. A non null value adds
     *            as precondition that {@code numberOfBands == 1}
     * @throws Exception
     */
    public void loadTestRaster(final String tableName, final int numberOfBands,
            final int pixelType, final IndexColorModel colorModel) throws Exception {
        final boolean pyramiding = false;
        final boolean skipLevelOne = false;
        final int interpolationType = SeRaster.SE_INTERPOLATION_NONE;
        loadTestRaster(tableName, numberOfBands, pixelType, colorModel, pyramiding, skipLevelOne,
                interpolationType);
    }

    /**
     * Creates a a raster in the database with some default settings and the provided parameters.
     * <p>
     * Default settings
     * <ul>
     * <li>CRS: PePCSDefs.PE_PCS_NAD_1983_HARN_MA_M
     * <li>Extent: minx=0, miny=0, maxx=512, maxy=512
     * <li>Width: 256
     * <li>Height: 256
     * <li>Tile size: 64x64 (being less than the recommended minimum of 128, but ok for our testing
     * purposes)
     * <li>Compression: none
     * </ul>
     * </p>
     * 
     * @param tableName
     *            the name of the table to create
     * @param numberOfBands
     *            the number of bands of the raster
     * @param pixelType
     *            the pixel (cell) depth of the raster bands (one of the {@code
     *            SeRaster#SE_PIXEL_TYPE_*} constants)
     * @param colorModel
     *            the color model to apply to the raster, may be {@code null}. A non null value adds
     *            as precondition that {@code numberOfBands == 1}
     * @param pyramiding
     *            whether to create tiles or not for the raster. If {@code true} and {@code
     *            skipLevelOne == true} a pyramid with three levels will be created, avoiding to
     *            create the pyramid tiles for level 0 (same dimension as the source raster). If
     *            {@code skipLevelOne == false}, even for level 0 the pyramid tiles will be created,
     *            that is, four levels.
     * @param skipLevelOne
     *            only relevant if {@code pyramiding == true}, {@code true} indicates not to create
     *            pyramid tiles for the first level, since its equal in dimension than the source
     *            raster
     * @param interpolationType
     *            only relevant if {@code pyramiding == true}, indicates which interpolation method
     *            to use in building the pyramid tiles. Shall be one of
     *            {@link SeRaster#SE_INTERPOLATION_NONE}, {@link SeRaster#SE_INTERPOLATION_BICUBIC},
     *            {@link SeRaster#SE_INTERPOLATION_BILINEAR},
     *            {@link SeRaster#SE_INTERPOLATION_NEAREST}.
     * @throws Exception
     */
    public void loadTestRaster(final String tableName, final int numberOfBands,
            final int pixelType, final IndexColorModel colorModel, final boolean pyramiding,
            final boolean skipLevelOne, final int interpolationType) throws Exception {

        if (colorModel != null && numberOfBands > 1) {
            throw new IllegalArgumentException(
                    "Indexed rasters shall contain a single band. numberOfBands = " + numberOfBands);
        }
        {
            // clean out the table if it's currently in-place
            testData.deleteTable(tableName);
            // build the base business table. We'll add the raster data to it in a bit
            // Note that this DOESN'T LOAD THE COLORMAP RIGHT NOW.
            ArcSDEPooledConnection conn = getConnectionPool().getConnection();
            try {
                createRasterBusinessTempTable(tableName, conn);
            } finally {
                conn.close();
            }
        }

        final SeCoordinateReference crs = getSeCRSFromPeProjectedCSId(PePCSDefs.PE_PCS_NAD_1983_HARN_MA_M);

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
            final int imageWidth = 256;
            final int imageHeight = 256;

            SeRasterAttr attr = new SeRasterAttr(true);
            attr.setImageSize(imageWidth, imageHeight, numberOfBands);
            attr.setTileSize(64, 64); // this is lower than the recommended minimum of 128,128 but
            // it's ok for our testing purposes
            attr.setPixelType(pixelType);
            attr.setCompressionType(SeRaster.SE_COMPRESSION_NONE);
            if (pyramiding) {
                final int numOfLevels = skipLevelOne ? 3 : 4;
                attr.setPyramidInfo(numOfLevels, skipLevelOne, interpolationType);
            }
            attr.setMaskMode(false);
            attr.setImportMode(false);

            SeExtent extent = new SeExtent(0, 0, 2 * imageWidth, 2 * imageHeight);
            attr.setExtent(extent);
            // attr.setImageOrigin();

            SeRasterProducer prod = new SeRasterProducer() {
                public void addConsumer(SeRasterConsumer consumer) {
                }

                public boolean isConsumer(SeRasterConsumer consumer) {
                    return false;
                }

                public void removeConsumer(SeRasterConsumer consumer) {
                }

                /**
                 * Note that due to some synchronization problems inherent in the SDE api code, the
                 * startProduction() method MUST return before consumer.setScanLines() or
                 * consumer.setRasterTiles() is called. Hence the thread implementation.
                 */
                public void startProduction(final SeRasterConsumer consumer) {
                    if (!(consumer instanceof SeRasterRenderedImage)) {
                        throw new IllegalArgumentException(
                                "You must set SeRasterAttr.setImportMode(false) to "
                                        + "load data using this SeProducer implementation.");
                    }

                    Thread runme = new Thread() {
                        @Override
                        public void run() {
                            try {
                                PixelSampler sampler = PixelSampler.getSampler(pixelType);
                                // for each band...
                                for (int bandN = 0; bandN < numberOfBands; bandN++) {
                                    final byte[] imgBandData;
                                    imgBandData = sampler.getImgBandData(imageWidth, imageHeight);
                                    consumer.setScanLines(imageHeight, imgBandData, null);
                                    consumer.rasterComplete(SeRasterConsumer.SINGLEFRAMEDONE);
                                }
                                consumer.rasterComplete(SeRasterConsumer.STATICIMAGEDONE);
                            } catch (Exception se) {
                                se.printStackTrace();
                                consumer.rasterComplete(SeRasterConsumer.IMAGEERROR);
                            }
                        }
                    };
                    runme.start();
                }
            };
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
            // if (colorModel != null) {
            // attr = getRasterAttributes(tableName, new Rectangle(0, 0, 0, 0), 0, new int[] { 1 });
            // // attr.getBands()[0].setColorMap(SeRaster.SE_COLORMAP_DATA_BYTE, );
            // // NOT IMPLEMENTED FOR NOW!
            // }
        } finally {
            conn.close();
        }
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

    private static abstract class PixelSampler {

        private static Map<Integer, PixelSampler> byPixelTypeSamplers = new HashMap<Integer, PixelSampler>();
        static {
            byPixelTypeSamplers.put(Integer.valueOf(SeRaster.SE_PIXEL_TYPE_1BIT),
                    new SamplerType1Bit());
            byPixelTypeSamplers.put(Integer.valueOf(SeRaster.SE_PIXEL_TYPE_4BIT),
                    new SamplerType4Bit());
            byPixelTypeSamplers.put(Integer.valueOf(SeRaster.SE_PIXEL_TYPE_8BIT_U),
                    new SamplerType8BitUnsigned());
            byPixelTypeSamplers.put(Integer.valueOf(SeRaster.SE_PIXEL_TYPE_8BIT_S),
                    new SamplerType8BitSigned());
            byPixelTypeSamplers.put(Integer.valueOf(SeRaster.SE_PIXEL_TYPE_16BIT_U),
                    new SamplerType16BitUnsigned());
            byPixelTypeSamplers.put(Integer.valueOf(SeRaster.SE_PIXEL_TYPE_16BIT_S),
                    new SamplerType16BitSigned());
            byPixelTypeSamplers.put(Integer.valueOf(SeRaster.SE_PIXEL_TYPE_32BIT_U),
                    new SamplerType32BitUnsigned());
            byPixelTypeSamplers.put(Integer.valueOf(SeRaster.SE_PIXEL_TYPE_32BIT_S),
                    new SamplerType32BitSigned());
            byPixelTypeSamplers.put(Integer.valueOf(SeRaster.SE_PIXEL_TYPE_32BIT_REAL),
                    new SamplerType32BitReal());
            byPixelTypeSamplers.put(Integer.valueOf(SeRaster.SE_PIXEL_TYPE_64BIT_REAL),
                    new SamplerType64BitReal());
        }

        public static PixelSampler getSampler(final int sePixelType) {
            PixelSampler sampler = byPixelTypeSamplers.get(Integer.valueOf(sePixelType));
            if (sampler == null) {
                throw new NoSuchElementException("no pixel sampler exists for pixel type "
                        + sePixelType);
            }
            return sampler;
        }

        public abstract byte[] getImgBandData(final int imgWidth, final int imgHeight);

        /**
         * Pixel sampler for creating test data with pixel type {@link SeRaster#SE_PIXEL_TYPE_1BIT}
         * 
         * @see PixelSampler#getImgBandData(int, int)
         */
        private static class SamplerType1Bit extends PixelSampler {
            @Override
            public byte[] getImgBandData(int imgWidth, int imgHeight) {
                final byte[] imgBandData = new byte[(imgWidth * imgHeight) / 8];
                for (int bytePos = 0; bytePos < imgBandData.length; bytePos++) {
                    if (bytePos % 2 == 0) {
                        imgBandData[bytePos] = (byte) 0xFF;
                    } else {
                        imgBandData[bytePos] = (byte) 0x00;
                    }
                }
                return imgBandData;
            }
        }

        /**
         * Pixel sampler for creating test data with pixel type {@link SeRaster#SE_PIXEL_TYPE_4BIT}
         * 
         * @see PixelSampler#getImgBandData(int, int)
         */
        private static class SamplerType4Bit extends PixelSampler {
            @Override
            public byte[] getImgBandData(int imgWidth, int imgHeight) {
                throw new UnsupportedOperationException(
                        "sampler for pixel type 4BIT not yet implemented");
            }
        }

        /**
         * Pixel sampler for creating test data with pixel type
         * {@link SeRaster#SE_PIXEL_TYPE_8BIT_U}
         * 
         * @see PixelSampler#getImgBandData(int, int)
         */
        private static class SamplerType8BitUnsigned extends PixelSampler {
            @Override
            public byte[] getImgBandData(int imgWidth, int imgHeight) {
                // luckily the byte-packed data format in MultiPixelPackedSampleModel is identical
                // to the one-bit-per-pixel format expected by ArcSDE.
                final byte[] imgBandData = new byte[imgWidth * imgHeight];
                for (int w = 0; w < imgWidth; w++) {
                    for (int h = 0; h < imgHeight; h++) {
                        imgBandData[(w * imgHeight) + h] = (byte) h;
                    }
                }
                return imgBandData;
            }
        }

        /**
         * Pixel sampler for creating test data with pixel type
         * {@link SeRaster#SE_PIXEL_TYPE_8BIT_S}
         * 
         * @see PixelSampler#getImgBandData(int, int)
         */
        private static class SamplerType8BitSigned extends PixelSampler {
            @Override
            public byte[] getImgBandData(int imgWidth, int imgHeight) {
                throw new UnsupportedOperationException(
                        "sampler for pixel type 8BIT_S not yet implemented");
            }
        }

        /**
         * Pixel sampler for creating test data with pixel type
         * {@link SeRaster#SE_PIXEL_TYPE_16BIT_U}
         * 
         * @see PixelSampler#getImgBandData(int, int)
         */
        private static class SamplerType16BitUnsigned extends PixelSampler {
            @Override
            public byte[] getImgBandData(int imgWidth, int imgHeight) {
                throw new UnsupportedOperationException(
                        "sampler for pixel type 16BIT_U not yet implemented");
            }
        }

        /**
         * Pixel sampler for creating test data with pixel type
         * {@link SeRaster#SE_PIXEL_TYPE_16BIT_S}
         * 
         * @see PixelSampler#getImgBandData(int, int)
         */
        private static class SamplerType16BitSigned extends PixelSampler {
            @Override
            public byte[] getImgBandData(int imgWidth, int imgHeight) {
                throw new UnsupportedOperationException(
                        "sampler for pixel type 16BIT_S not yet implemented");
            }
        }

        /**
         * Pixel sampler for creating test data with pixel type
         * {@link SeRaster#SE_PIXEL_TYPE_32BIT_U}
         * 
         * @see PixelSampler#getImgBandData(int, int)
         */
        private static class SamplerType32BitUnsigned extends PixelSampler {
            @Override
            public byte[] getImgBandData(int imgWidth, int imgHeight) {
                throw new UnsupportedOperationException(
                        "sampler for pixel type 32BIT_U not yet implemented");
            }
        }

        /**
         * Pixel sampler for creating test data with pixel type
         * {@link SeRaster#SE_PIXEL_TYPE_32BIT_S}
         * 
         * @see PixelSampler#getImgBandData(int, int)
         */
        private static class SamplerType32BitSigned extends PixelSampler {
            @Override
            public byte[] getImgBandData(int imgWidth, int imgHeight) {
                throw new UnsupportedOperationException(
                        "sampler for pixel type 32BIT_S not yet implemented");
            }
        }

        /**
         * Pixel sampler for creating test data with pixel type
         * {@link SeRaster#SE_PIXEL_TYPE_32BIT_REAL}
         * 
         * @see PixelSampler#getImgBandData(int, int)
         */
        private static class SamplerType32BitReal extends PixelSampler {
            @Override
            public byte[] getImgBandData(int imgWidth, int imgHeight) {
                throw new UnsupportedOperationException(
                        "sampler for pixel type 16BIT_REAL not yet implemented");
            }
        }

        /**
         * Pixel sampler for creating test data with pixel type
         * {@link SeRaster#SE_PIXEL_TYPE_64BIT_REAL}
         * 
         * @see PixelSampler#getImgBandData(int, int)
         */
        private static class SamplerType64BitReal extends PixelSampler {
            @Override
            public byte[] getImgBandData(int imgWidth, int imgHeight) {
                throw new UnsupportedOperationException(
                        "sampler for pixel type 64BIT_REAL not yet implemented");
            }
        }
    }
}
