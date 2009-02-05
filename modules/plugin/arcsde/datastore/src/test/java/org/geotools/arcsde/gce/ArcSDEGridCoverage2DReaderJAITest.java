/**
 * 
 */
package org.geotools.arcsde.gce;

import static org.junit.Assert.*;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.geotools.arcsde.ArcSDERasterFormatFactory;
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
import org.geotools.referencing.CRS;
import org.geotools.test.TestData;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author groldan
 * 
 */
@SuppressWarnings( { "deprecation", "nls" })
public class ArcSDEGridCoverage2DReaderJAITest {

    static RasterTestData rasterTestData;

    private static String tableName;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        rasterTestData = new RasterTestData();
        rasterTestData.setUp();
        tableName = rasterTestData.loadRGBRaster();
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
    public void testGetInfo() {
        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link org.geotools.arcsde.gce.ArcSDEGridCoverage2DReaderJAI#read(org.opengis.parameter.GeneralParameterValue[])}
     * .
     */
    @Test
    public void tesReadFullLevel0() throws Exception {

        final AbstractGridCoverage2DReader reader = getReader();
        assertNotNull(reader);

        final GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();
        final GeneralGridRange originalGridRange = reader.getOriginalGridRange();
        int origWidth = originalGridRange.getLength(0);
        int origHeight = originalGridRange.getLength(1);

        final GridCoverage2D coverage = readCoverage(reader, origWidth, origHeight,
                originalEnvelope);
        assertNotNull(coverage);

        String fileName = "tesReadFullLevel0";

        final RenderedImage image = writeToDisk(coverage, fileName);

        System.out.println(image);

        writeToDisk(coverage, fileName + "_2");
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
    public void tesReadDisplaced() throws Exception {
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
            double minx = originalEnvelope.getMinimum(0);

            double shiftX = originalEnvelope.getSpan(0) / 2;
            double miny = originalEnvelope.getMinimum(1);
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

        final String fileName = "tesReadDisplaced_Level0";

        final RenderedImage image = writeToDisk(coverage, fileName);

        System.out.println(image);

        assertSame(originalCrs, coverage.getCoordinateReferenceSystem());

        Envelope envelope = coverage.getEnvelope();

        System.out.println("Requested width : " + requestedWidth + 
                         "\nReturned width  :" + coverage.getRenderedImage().getWidth() + 
                         "\nRequested height:" + requestedHeight + 
                         "\nReturned height :" + coverage.getRenderedImage().getHeight());
        
        System.out.println("Original envelope  : " + originalEnvelope + "\n requested envelope :"
                + requestedEnvelope + "\n returned envelope  :" + envelope);

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

    private RenderedImage writeToDisk(final GridCoverage2D coverage, String fileName) {
        final RenderedImage image = coverage.view(ViewType.PHOTOGRAPHIC).getRenderedImage();
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
