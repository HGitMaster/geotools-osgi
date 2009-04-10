/**
 * 
 */
package org.geotools.arcsde.gce;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Rectangle;
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
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DataSourceException;
import org.geotools.geometry.GeneralEnvelope;
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

/**
 * Tests over legacy data that should not be deleted
 * 
 */
@SuppressWarnings( { "deprecation", "nls" })
@Ignore
public class ArcSDEGridCoverage2DReaderJAILegacyOnlineTest {

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
            // wait I may delete an actual business table, comment out until this suite is fully
            // based on fake data rasterTestData.deleteTable(tableName);
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
    public void testReadRasterCatalogOnline() throws Exception {
        tableName = "SDE.IMG_USGSQUAD_SGBASE";
        final AbstractGridCoverage2DReader reader = getReader();
        assertNotNull("Couldn't obtain a reader for " + tableName, reader);

        final GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();
        final GeneralGridRange originalGridRange = reader.getOriginalGridRange();

        final int reqWidth = originalGridRange.getLength(0) / 50;
        final int reqHeight = originalGridRange.getLength(1) / 50;

        GeneralEnvelope reqEnvelope = new GeneralEnvelope(originalEnvelope
                .getCoordinateReferenceSystem());
        double deltaX = originalEnvelope.getSpan(0) / 1;
        double deltaY = originalEnvelope.getSpan(1) / 1;

        double minx = originalEnvelope.getMedian(0) - deltaX;
        double miny = originalEnvelope.getMedian(1) - deltaY;
        double maxx = minx + 2 * deltaX;
        double maxy = miny + 2 * deltaY;
        reqEnvelope.setEnvelope(minx, miny, maxx, maxy);

        assertTrue(originalEnvelope.intersects(reqEnvelope, true));

        final GridCoverage2D coverage = readCoverage(reader, reqWidth, reqHeight, reqEnvelope);
        assertNotNull("read coverage returned null", coverage);

        RenderedImage image = coverage.getRenderedImage();
        writeToDisk(image, "testReadRasterCatalogOnline");
    }

    @Test
    public void testReadRasterCatalogOnline2() throws Exception {
        tableName = "SDE.IMG_USGSQUAD_SGBASE";
        final AbstractGridCoverage2DReader reader = getReader();
        assertNotNull("Couldn't obtain a reader for " + tableName, reader);

        final GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();
        final GeneralGridRange originalGridRange = reader.getOriginalGridRange();

        final int reqWidth = originalGridRange.getLength(0) / 20;
        final int reqHeight = originalGridRange.getLength(1) / 20;

        GeneralEnvelope reqEnvelope = new GeneralEnvelope(originalEnvelope
                .getCoordinateReferenceSystem());
        double deltaX = originalEnvelope.getSpan(0) / 20;
        double deltaY = originalEnvelope.getSpan(1) / 20;

        double minx = originalEnvelope.getMedian(0) - deltaX;
        double miny = originalEnvelope.getMedian(1) - deltaY;
        double maxx = minx + 2 * deltaX;
        double maxy = miny + 2 * deltaY;
        reqEnvelope.setEnvelope(minx, miny, maxx, maxy);

        assertTrue(originalEnvelope.intersects(reqEnvelope, true));

        final GridCoverage2D coverage = readCoverage(reader, reqWidth, reqHeight, reqEnvelope);
        assertNotNull("read coverage returned null", coverage);

        RenderedImage image = coverage.getRenderedImage();
        writeToDisk(image, "testReadRasterCatalogOnline2");
    }
    
    

    @Test
    public void testReadRaster() throws Exception {
        tableName = "SDE.IMG_USGSQUADM";
        final AbstractGridCoverage2DReader reader = getReader();
        assertNotNull("Couldn't obtain a reader for " + tableName, reader);

        final GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();
        final GeneralGridRange originalGridRange = reader.getOriginalGridRange();

        final int reqWidth = originalGridRange.getLength(0) / 200;
        final int reqHeight = originalGridRange.getLength(1) / 200;

        final GridCoverage2D coverage = readCoverage(reader, reqWidth, reqHeight, originalEnvelope);
        assertNotNull("read coverage returned null", coverage);

        RenderedImage image = coverage.getRenderedImage();
        writeToDisk(image, "testRead_" + tableName);
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
