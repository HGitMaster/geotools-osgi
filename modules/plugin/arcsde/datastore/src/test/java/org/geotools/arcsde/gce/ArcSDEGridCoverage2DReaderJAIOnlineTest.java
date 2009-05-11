/**
 * 
 */
package org.geotools.arcsde.gce;

import static org.geotools.arcsde.gce.RasterCellType.TYPE_16BIT_S;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_16BIT_U;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_1BIT;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_32BIT_REAL;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_32BIT_S;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_32BIT_U;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_4BIT;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_64BIT_REAL;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_8BIT_S;
import static org.geotools.arcsde.gce.RasterCellType.TYPE_8BIT_U;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.geotools.arcsde.ArcSDERasterFormatFactory;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.GridRange2D;
import org.geotools.coverage.grid.ViewType;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DataSourceException;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.resources.image.ColorUtilities;
import org.geotools.util.logging.Logging;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SeRaster;

/**
 * @author groldan
 * 
 */
@SuppressWarnings( { "deprecation", "nls" })
public class ArcSDEGridCoverage2DReaderJAIOnlineTest {

    private static final String RASTER_TEST_DEBUG_TO_DISK = "raster.test.debugToDisk";

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    /**
     * Whether to write the fetched rasters to disk or not
     */
    private static boolean DEBUG;

    static RasterTestData rasterTestData;

    private static String tableName;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        rasterTestData = new RasterTestData();
        rasterTestData.setUp();
        DEBUG = Boolean
                .valueOf(rasterTestData.getRasterTestDataProperty(RASTER_TEST_DEBUG_TO_DISK));
        rasterTestData.setOverrideExistingTestTables(false);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        rasterTestData.tearDown();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // nothing to do
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        try {
            LOGGER.info("tearDown: deleting " + tableName);
            rasterTestData.deleteTable(tableName);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Error deleting test table " + tableName, e);
        }
    }

    /**
     * Test method for {@link org.geotools.arcsde.gce.ArcSDEGridCoverage2DReaderJAI#getInfo()}.
     */
    @Test
    @Ignore
    public void testGetInfo() {
        fail("Not yet implemented");
    }

    @Test
    public void testRead_01bit_1Band() throws Exception {
        testReadFullLevel0(TYPE_1BIT, 1);
    }

    @Test
    public void testRead_01bit_MoreThanOneBandIsUnsupported() throws Exception {
        try {
            testReadFullLevel0(TYPE_1BIT, 2);
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /*
     * 4bit rasters are not supported by now, need to check exactly what color model/sample model
     * combination makes JAI happy, or to unpack the incoming samples into full bytes
     */
    @Test
    @Ignore
    public void testRead_04bit_1Band() throws Exception {
        testReadFullLevel0(TYPE_4BIT, 1);
    }

    @Test
    public void testRead_04bit_MoreThanOneBandIsUnsupported() throws Exception {
        try {
            testReadFullLevel0(TYPE_4BIT, 2);
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testRead_08bit_U_1Band() throws Exception {
        testReadFullLevel0(TYPE_8BIT_U, 1);
    }

    @Test
    public void testRead_08bit_U_7Band() throws Exception {
        testReadFullLevel0(TYPE_8BIT_U, 7);
    }

    @Test
    public void testRead_08bitU_ColorMapped() throws Exception {
        tableName = rasterTestData.loadRGBColorMappedRaster();
        testReadFullLevel0(TYPE_8BIT_U, 1, "testRead_8bitU_RGBColorMappedRaster");
    }

    @Test
    public void testRead_08bit_S_1Band() throws Exception {
        testReadFullLevel0(TYPE_8BIT_S, 1);
    }

    @Test
    public void testRead_08bit_S_7Band() throws Exception {
        testReadFullLevel0(TYPE_8BIT_S, 7);
    }

    @Test
    public void testRead_16bit_S_1Band() throws Exception {
        testReadFullLevel0(TYPE_16BIT_S, 1);
    }

    @Test
    public void testRead_16bit_S_7Band() throws Exception {
        testReadFullLevel0(TYPE_16BIT_S, 7);
    }

    @Test
    public void testRead_16bit_U_1Band() throws Exception {
        testReadFullLevel0(TYPE_16BIT_U, 1);
    }

    @Test
    public void testRead_16bit_U_7Band() throws Exception {
        testReadFullLevel0(TYPE_16BIT_U, 7);
    }

    @Test
    public void testRead_16bit_S_ColorMapped() throws Exception {
        int[] ARGB = new int[65536];
        ColorUtilities.expand(new Color[] { Color.BLACK, Color.WHITE }, ARGB, 0, ARGB.length);
        IndexColorModel colorModel = ColorUtilities.getIndexColorModel(ARGB);
        tableName = rasterTestData.getRasterTableName(TYPE_16BIT_S, 1, true);
        rasterTestData.loadTestRaster(tableName, 1, TYPE_16BIT_S, colorModel);
        testReadFullLevel0(TYPE_16BIT_S, 1, "testRead_16bit_S_ColorMapped");
    }

    @Test
    public void testRead_32bit_REAL_1Band() throws Exception {
        testReadFullLevel0(TYPE_32BIT_REAL, 1);
    }

    @Test
    public void testRead_32bit_REAL_7Band() throws Exception {
        testReadFullLevel0(TYPE_32BIT_REAL, 7);
    }

    @Test
    public void testRead_32bit_U_1Band() throws Exception {
        testReadFullLevel0(TYPE_32BIT_U, 1);
    }

    @Test
    public void testRead_32bit_U_7Band() throws Exception {
        testReadFullLevel0(TYPE_32BIT_U, 7);
    }

    @Test
    public void testRead_32bit_S_1Band() throws Exception {
        testReadFullLevel0(TYPE_32BIT_S, 1);
    }

    @Test
    public void testRead_32bit_S_7Band() throws Exception {
        testReadFullLevel0(TYPE_32BIT_S, 7);
    }

    @Test
    public void testRead_64bit_REAL_1Band() throws Exception {
        testReadFullLevel0(TYPE_64BIT_REAL, 1);
    }

    @Test
    public void testRead_64bit_REAL_7Band() throws Exception {
        testReadFullLevel0(TYPE_64BIT_REAL, 7);
    }

    @Test
    public void testReadSampleRGB() throws Exception {
        tableName = rasterTestData.loadRGBRaster();
        testReadFullLevel0(TYPE_8BIT_U, 3, "sampleRGB");
    }


    @Test
    public void testReadRasterCatalogFull() throws Exception {
        tableName = rasterTestData.loadRasterCatalog();
        GridCoverage2D coverage = testReadFullLevel0(TYPE_8BIT_U, 3, "RasterCatalog");

        GridGeometry2D gridGeometry = coverage.getGridGeometry();
        Envelope2D envelope2D = gridGeometry.getEnvelope2D();
        GridRange2D gridRange2D = gridGeometry.getGridRange2D();

        // assertEquals(0, envelope2D.getMinX(), 1);
        // assertEquals(0, envelope2D.getMinY(), 1);
        // assertEquals(512, envelope2D.getMaxX(), 1);
        // assertEquals(512, envelope2D.getMaxY(), 1);
        //        
        // assertEquals(512, gridRange2D.width);
        // assertEquals(512, gridRange2D.height);
    }

    @Test
    public void testReadRasterCatalogSubset() throws Exception {
        tableName = rasterTestData.loadRasterCatalog();
        final AbstractGridCoverage2DReader reader = getReader();
        assertNotNull("Couldn't obtain a reader for " + tableName, reader);

        final GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();
        final GeneralGridRange originalGridRange = reader.getOriginalGridRange();

        final int reqWidth = originalGridRange.getLength(0) / 2;
        final int reqHeight = originalGridRange.getLength(1) / 2;

        GeneralEnvelope reqEnvelope = new GeneralEnvelope(originalEnvelope
                .getCoordinateReferenceSystem());
        double deltaX = originalEnvelope.getSpan(0) / 6;
        double deltaY = originalEnvelope.getSpan(1) / 6;

        double minx = originalEnvelope.getMinimum(0) + deltaX;
        double miny = originalEnvelope.getMinimum(1) + deltaY;
        double maxx = originalEnvelope.getMaximum(0) - deltaX;
        double maxy = originalEnvelope.getMaximum(1) - deltaY;
        reqEnvelope.setEnvelope(minx, miny, maxx, maxy);

        assertTrue(originalEnvelope.intersects(reqEnvelope, true));

        final GridCoverage2D coverage = readCoverage(reader, reqWidth, reqHeight, reqEnvelope);
        assertNotNull("read coverage returned null", coverage);

        RenderedImage image = coverage.getRenderedImage();
        writeToDisk(image, "testReadRasterCatalogSubset");
    }

    @Test
    public void testReadRasterCatalog2() throws Exception {
        tableName = rasterTestData.loadRasterCatalog();
        final AbstractGridCoverage2DReader reader = getReader();
        assertNotNull("Couldn't obtain a reader for " + tableName, reader);

        final GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();
        final GeneralGridRange originalGridRange = reader.getOriginalGridRange();

        final int reqWidth = originalGridRange.getLength(0) / 10;
        final int reqHeight = originalGridRange.getLength(1) / 10;

        GeneralEnvelope reqEnvelope = new GeneralEnvelope(originalEnvelope
                .getCoordinateReferenceSystem());
        double minx = originalEnvelope.getMinimum(0);
        double miny = originalEnvelope.getMinimum(1);
        double maxx = minx + originalEnvelope.getSpan(0);// / 2;
        double maxy = miny + originalEnvelope.getSpan(1);// / 2;
        reqEnvelope.setEnvelope(minx, miny, maxx, maxy);

        assertTrue(originalEnvelope.intersects(reqEnvelope, true));

        final GridCoverage2D coverage = readCoverage(reader, reqWidth, reqHeight, reqEnvelope);
        assertNotNull("read coverage returned null", coverage);

        RenderedImage image = coverage.getRenderedImage();
        writeToDisk(image, "testReadRasterCatalog2");
    }

    private void testReadFullLevel0(final RasterCellType cellType, final int numBands)
            throws Exception {

        tableName = rasterTestData.getRasterTableName(cellType, numBands, false);
        rasterTestData.loadTestRaster(tableName, numBands, cellType, null);
        testReadFullLevel0(cellType, numBands, tableName + "_" + numBands + "-Band");
    }

    private GridCoverage2D testReadFullLevel0(final RasterCellType cellType, final int numBands,
            final String fileNamePostFix) throws Exception {

        final AbstractGridCoverage2DReader reader = getReader();
        assertNotNull("Couldn't obtain a reader for " + fileNamePostFix, reader);

        final GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();
        final GeneralGridRange originalGridRange = reader.getOriginalGridRange();

        final int origWidth = originalGridRange.getLength(0);
        final int origHeight = originalGridRange.getLength(1);

        final GridCoverage2D coverage = readCoverage(reader, origWidth, origHeight,
                originalEnvelope);
        assertNotNull("read coverage returned null", coverage);

        assertEquals(numBands, coverage.getNumSampleDimensions());
        // for (int i = 0; i < numBands; i++) {
        // NumberRange<?> range = cellType.getSampleValueRange();
        // GridSampleDimension sampleDimension = coverage.getSampleDimension(i);
        // assertNotNull("Sample dimension #" + i, sampleDimension);
        // assertEquals(range, sampleDimension.getRange());
        // }

        assertNotNull(coverage.getEnvelope());
        GeneralEnvelope envelope = (GeneralEnvelope) coverage.getEnvelope();
        assertTrue(originalEnvelope.intersects(envelope, true));

        GridGeometry2D gridGeometry = coverage.getGridGeometry();

        // ///////////////////////////////////////////////////////////assertEquals(originalGridRange,
        // gridGeometry.getGridRange());

        final RenderedImage image = coverage.view(ViewType.GEOPHYSICS).getRenderedImage();
        assertNotNull(image);

        // ////assertEquals(cellType.getDataBufferType(), image.getSampleModel().getDataType());
        final int[] sampleSize = image.getSampleModel().getSampleSize();
        final ColorModel colorModel = image.getColorModel();
        if (colorModel instanceof IndexColorModel) {
            switch (cellType) {
            case TYPE_1BIT:
                assertEquals(1, sampleSize[0]);
                break;
            case TYPE_8BIT_U:
                assertEquals("8-bit indexed image should have been "
                        + "promoted to 16bit to account for no-data values", 16, sampleSize[0]);
                break;
            default:
                throw new IllegalArgumentException(cellType.toString());
            }
        } else {
            for (int band = 0; band < numBands; band++) {
                assertEquals(cellType.getBitsPerSample(), sampleSize[band]);
            }
        }

        final String fileName = "testReadFullLevel0_" + fileNamePostFix;
        writeToDisk(image, fileName);

        return coverage;
    }

    @Test
    public void tesReadOverlapsSampleRGBIamge() throws Exception {
        tableName = rasterTestData.getRasterTableName(RasterCellType.TYPE_8BIT_U, 3);
        rasterTestData.loadRGBRaster();
        final AbstractGridCoverage2DReader reader = getReader();
        assertNotNull(reader);

        final GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();

        final CoordinateReferenceSystem originalCrs = originalEnvelope
                .getCoordinateReferenceSystem();
        final GeneralGridRange originalGridRange = reader.getOriginalGridRange();
        final int requestedWidth = originalGridRange.getLength(0);
        final int requestedHeight = originalGridRange.getLength(1);

        final GeneralEnvelope requestedEnvelope;
        final GridCoverage2D coverage;
        {
            final double minx = originalEnvelope.getMinimum(0);
            final double miny = originalEnvelope.getMinimum(1);

            double shiftX = originalEnvelope.getSpan(0) / 2;
            double shiftY = originalEnvelope.getSpan(1) / 2;

            double x1 = minx - shiftX;
            double x2 = minx + shiftX;
            double y1 = miny + shiftY;
            double y2 = miny + 2 * shiftY;

            requestedEnvelope = new GeneralEnvelope(new ReferencedEnvelope(x1, x2, y1, y2,
                    originalCrs));
            coverage = readCoverage(reader, requestedWidth, requestedHeight, requestedEnvelope);
        }
        assertNotNull(coverage);
        assertNotNull(coverage.getRenderedImage());
        CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem();
        assertNotNull(crs);

        final String fileName = "tesReadOverlapsSampleRGBIamge";

        final RenderedImage image = coverage.view(ViewType.GEOPHYSICS).getRenderedImage();
        assertNotNull(image);
        writeToDisk(image, fileName);

        assertSame(originalCrs, crs);

        final Envelope returnedEnvelope = coverage.getEnvelope();

        // these ones should equal to the tile dimension in the arcsde raster
        int tileWidth = image.getTileWidth();
        int tileHeight = image.getTileHeight();
        assertTrue(tileWidth > 0);
        assertTrue(tileHeight > 0);

        int fullWidth = originalGridRange.getSpan(0);
        int fullHeight = originalGridRange.getSpan(1);

        GeneralEnvelope expectedEnvelope = new GeneralEnvelope(originalCrs);
        expectedEnvelope.setRange(0, originalEnvelope.getMinimum(0), originalEnvelope.getMinimum(0)
                + (originalEnvelope.getSpan(0) / 2));

        expectedEnvelope.setRange(1, originalEnvelope.getMinimum(1), originalEnvelope.getMinimum(1)
                + (originalEnvelope.getSpan(1) / 2));

        LOGGER.info("\nRequested width : " + requestedWidth + "\nReturned width  :"
                + image.getWidth() + "\nRequested height:" + requestedHeight
                + "\nReturned height :" + image.getHeight());

        LOGGER.info("\nOriginal envelope  : " + originalEnvelope + "\n requested envelope :"
                + requestedEnvelope + "\n expected envelope  :" + expectedEnvelope
                + "\n returned envelope  :" + returnedEnvelope);

        assertEquals(501, image.getWidth());
        assertEquals(501, image.getHeight());
        // assertEquals(expectedEnvelope, returnedEnvelope);
    }

    @Test
    public void tesReadOverlaps() throws Exception {
        tableName = rasterTestData.getRasterTableName(RasterCellType.TYPE_8BIT_U, 1);
        rasterTestData.loadTestRaster(tableName, 1, 100, 100, TYPE_8BIT_U, null, true, false,
                SeRaster.SE_INTERPOLATION_NEAREST, null);
        final AbstractGridCoverage2DReader reader = getReader();
        assertNotNull(reader);

        final GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();

        final CoordinateReferenceSystem originalCrs = originalEnvelope
                .getCoordinateReferenceSystem();
        final GeneralGridRange originalGridRange = reader.getOriginalGridRange();
        final int requestedWidth = originalGridRange.getLength(0);
        final int requestedHeight = originalGridRange.getLength(1);

        final GeneralEnvelope requestedEnvelope;
        requestedEnvelope = new GeneralEnvelope(new ReferencedEnvelope(-100, 100, -100, 100,
                originalCrs));

        final GridCoverage2D coverage;
        coverage = readCoverage(reader, requestedWidth, requestedHeight, requestedEnvelope);

        assertNotNull(coverage);
        assertNotNull(coverage.getRenderedImage());

        final String fileName = "tesReadOverlaps_Level0_8BitU_1-Band";

        final RenderedImage image = coverage.view(ViewType.GEOPHYSICS).getRenderedImage();
        assertNotNull(image);
        writeToDisk(image, fileName);

        final Envelope returnedEnvelope = coverage.getEnvelope();

        // these ones should equal to the tile dimension in the arcsde raster
        int tileWidth = image.getTileWidth();
        int tileHeight = image.getTileHeight();
        assertTrue(tileWidth > 0);
        assertTrue(tileHeight > 0);

        int fullWidth = originalGridRange.getSpan(0);
        int fullHeight = originalGridRange.getSpan(1);

        GeneralEnvelope expectedEnvelope = new GeneralEnvelope(originalCrs);
        expectedEnvelope.setRange(0, 0, 100);
        expectedEnvelope.setRange(1, 0, 100);

        LOGGER.info("\nRequested width : " + requestedWidth + "\nReturned width  :"
                + image.getWidth() + "\nRequested height:" + requestedHeight
                + "\nReturned height :" + image.getHeight());

        LOGGER.info("\nOriginal envelope  : " + originalEnvelope + "\n requested envelope :"
                + requestedEnvelope + "\n expected envelope  :" + expectedEnvelope
                + "\n returned envelope  :" + returnedEnvelope);

        assertEquals(51, image.getWidth());
        assertEquals(50, image.getHeight());
        // assertEquals(expectedEnvelope, returnedEnvelope);
    }

    @Test
    public void tesReadNotOverlaps() throws Exception {
        tableName = rasterTestData.getRasterTableName(RasterCellType.TYPE_8BIT_U, 1);
        rasterTestData.loadTestRaster(tableName, 1, 100, 100, TYPE_8BIT_U, null, true, false,
                SeRaster.SE_INTERPOLATION_NEAREST, null);
        final AbstractGridCoverage2DReader reader = getReader();
        assertNotNull(reader);

        final GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();

        final CoordinateReferenceSystem originalCrs = originalEnvelope
                .getCoordinateReferenceSystem();
        final GeneralGridRange originalGridRange = reader.getOriginalGridRange();
        final int requestedWidth = originalGridRange.getLength(0);
        final int requestedHeight = originalGridRange.getLength(1);

        final GeneralEnvelope nonOverlappingEnvelope;
        nonOverlappingEnvelope = new GeneralEnvelope(new ReferencedEnvelope(300, 500, 300, 500,
                originalCrs));

        final GridCoverage2D coverage;
        try {
            coverage = readCoverage(reader, requestedWidth, requestedHeight, nonOverlappingEnvelope);
            fail("Expected IAE, envelopes does not overlap");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    private void writeToDisk(final RenderedImage image, String fileName) throws Exception {
        if (!DEBUG) {
            LOGGER.fine("DEBUG == false, not writing image to disk");
            return;
        }
        String file = System.getProperty("user.home");
        file += File.separator + "arcsde_test" + File.separator + fileName + ".tiff";
        File path = new File(file);
        path.getParentFile().mkdirs();
        System.out.println("\n --- Writing to " + file);
        try {
            long t = System.currentTimeMillis();
            ImageIO.write(image, "TIFF", path);
            t = System.currentTimeMillis() - t;
            System.out.println(" - wrote in " + t + "ms" + file);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private GridCoverage2D readCoverage(final AbstractGridCoverage2DReader reader,
            final int reqWidth, final int reqHeight, final Envelope reqEnv) throws Exception {

        GeneralParameterValue[] requestParams = new Parameter[2];
        final CoordinateReferenceSystem crs = reader.getCrs();

        GridGeometry2D gg2d;
        gg2d = new GridGeometry2D(new GeneralGridRange(new Rectangle(reqWidth, reqHeight)), reqEnv);

        requestParams[0] = new Parameter<GridGeometry2D>(AbstractGridFormat.READ_GRIDGEOMETRY2D,
                gg2d);
        requestParams[1] = new Parameter<OverviewPolicy>(AbstractGridFormat.OVERVIEW_POLICY,
                OverviewPolicy.SPEED);

        final GridCoverage2D coverage;
        coverage = (GridCoverage2D) reader.read(requestParams);

        return coverage;
    }

    private AbstractGridCoverage2DReader getReader() throws DataSourceException {
        final ArcSDEConnectionConfig config = rasterTestData.getConnectionPool().getConfig();

        final String rgbUrl = "sde://" + config.getUserName() + ":" + config.getUserPassword()
                + "@" + config.getServerName() + ":" + config.getPortNumber() + "/"
                + config.getDatabaseName() + "#" + tableName;

        final ArcSDERasterFormat format = new ArcSDERasterFormatFactory().createFormat();

        AbstractGridCoverage2DReader reader = format.getReader(rgbUrl);
        return reader;
    }

}
