/**
 * 
 */
package org.geotools.arcsde.gce;

import static org.geotools.arcsde.gce.imageio.RasterCellType.TYPE_16BIT_S;
import static org.geotools.arcsde.gce.imageio.RasterCellType.TYPE_16BIT_U;
import static org.geotools.arcsde.gce.imageio.RasterCellType.TYPE_1BIT;
import static org.geotools.arcsde.gce.imageio.RasterCellType.TYPE_32BIT_REAL;
import static org.geotools.arcsde.gce.imageio.RasterCellType.TYPE_4BIT;
import static org.geotools.arcsde.gce.imageio.RasterCellType.TYPE_8BIT_S;
import static org.geotools.arcsde.gce.imageio.RasterCellType.TYPE_8BIT_U;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.geotools.arcsde.ArcSDERasterFormatFactory;
import org.geotools.arcsde.gce.imageio.RasterCellType;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.ViewType;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DataSourceException;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.Parameter;
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
public class ArcSDEGridCoverage2DReaderJAITest {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    static RasterTestData rasterTestData;

    private static String tableName;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        rasterTestData = new RasterTestData();
        rasterTestData.setUp();
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
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
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
    public void testRead_8bitU_ColorMapped() throws Exception {
        tableName = rasterTestData.loadRGBColorMappedRaster();
        testReadFullLevel0("testRead_8bitU_RGBColorMappedRaster");
    }

    @Test
    public void testRead_1bit_1Band() throws Exception {
        testReadFullLevel0(TYPE_1BIT, 1);
    }

    @Test
    public void testRead_1bit_2Band() throws Exception {
        testReadFullLevel0(TYPE_1BIT, 2);
    }

    @Test
    public void testRead_1bit_7Band() throws Exception {
        testReadFullLevel0(TYPE_1BIT, 7);
    }

    @Test
    public void testRead_4bit_1Band() throws Exception {
        testReadFullLevel0(TYPE_4BIT, 1);
    }

    @Test
    public void testRead_8bit_U_1Band() throws Exception {
        testReadFullLevel0(TYPE_8BIT_U, 1);
    }

    @Test
    public void testRead_8bit_U_7Band() throws Exception {
        testReadFullLevel0(TYPE_8BIT_U, 7);
    }

    @Test
    @Ignore
    public void testRead_8bit_S_1Band() throws Exception {
        testReadFullLevel0(TYPE_8BIT_S, 1);
    }

    @Test
    @Ignore
    public void testRead_8bit_S_7Band() throws Exception {
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
    public void testRead_32bit_REAL_1Band() throws Exception {
        testReadFullLevel0(TYPE_32BIT_REAL, 1);
    }

    @Test
    public void testRead_32bit_REAL_7Band() throws Exception {
        testReadFullLevel0(TYPE_32BIT_REAL, 7);
    }

    @Test
    public void testReadSampleRGB() throws Exception {
        tableName = rasterTestData.loadRGBRaster();
        testReadFullLevel0("sampleRGB");
    }

    private void testReadFullLevel0(final RasterCellType cellType, final int numBands)
            throws Exception {

        tableName = rasterTestData.getRasterTableName(cellType, numBands, false);
        rasterTestData.loadTestRaster(tableName, numBands, cellType, null);
        testReadFullLevel0(tableName + "_" + numBands + "-Band");
    }

    private void testReadFullLevel0(final String fileNamePostFix) throws Exception {

        final AbstractGridCoverage2DReader reader = getReader();
        assertNotNull(reader);

        final GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();
        final GeneralGridRange originalGridRange = reader.getOriginalGridRange();
        int origWidth = originalGridRange.getLength(0);
        int origHeight = originalGridRange.getLength(1);

        final GridCoverage2D coverage = readCoverage(reader, origWidth, origHeight,
                originalEnvelope);
        assertNotNull(coverage);

        String fileName = "tesReadFullLevel0_" + fileNamePostFix;

        final RenderedImage image = writeToDisk(coverage, fileName);

        System.out.println(image);

        // BufferedImage expected = ImageIO.read(TestData.file(null, rasterTestData
        // .getRasterTestDataProperty("sampledata.rgbraster")));
        // expected = expected.getSubimage(0, expected.getHeight() - 128, 256, 128);
        //
        // ImageIO.write(expected, "TIFF", new File(file + "-original.tiff"));
        //
        // Assert.assertTrue("Image from SDE isn't what we expected.", RasterTestData.imageEquals(
        // coverage.view(ViewType.PHOTOGRAPHIC).getRenderedImage(), expected));
        //
    }

    @Test
    public void tesReadDisplacedRGB() throws Exception {
        tableName = rasterTestData.getRasterTableName(RasterCellType.TYPE_8BIT_U, 3);
        // rasterTestData.loadRGBRaster();
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

        final String fileName = "tesReadDisplaced_Level0";

        final RenderedImage image = writeToDisk(coverage, fileName);

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
    public void tesReadDisplaced() throws Exception {
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

        final String fileName = "tesReadDisplaced_Level0_8BitU_1-Band";

        final RenderedImage image = writeToDisk(coverage, fileName);

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

        assertEquals(50, image.getWidth());
        assertEquals(50, image.getHeight());
        // assertEquals(expectedEnvelope, returnedEnvelope);
    }

    private RenderedImage writeToDisk(final GridCoverage2D coverage, String fileName) {
        final RenderedImage image = coverage.view(ViewType.GEOPHYSICS).getRenderedImage();
        assertNotNull(image);

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
        }
        return image;
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
                OverviewPolicy.QUALITY);

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
