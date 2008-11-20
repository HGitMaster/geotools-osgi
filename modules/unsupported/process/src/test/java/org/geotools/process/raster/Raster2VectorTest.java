/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.process.raster;

import com.vividsolutions.jts.geom.Polygon;
import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.CRS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import static org.junit.Assert.*;
import org.opengis.util.ProgressListener;

/**
 *
 * @author Michael Bedward <michael.bedward@gmail.com>
 */
public class Raster2VectorTest {
    private GridCoverage2D grid = null;
    private static final int[] DATA = {
            1, 1, 0, 1,
            0, 1, 0, 0,
            0, 1, 1, 1,
            1, 0, 0, 1
        };

    private static final int CELL_WIDTH = 100;
    private static final int PERIMETER = 2400;
    private static final int AREA = 90000;

    private static final String PROJ_WKT =
            "PROJCS[\"WGS 84 / UTM zone 55S\", " +
            "GEOGCS[\"WGS 84\", " +
            "DATUM[\"World Geodetic System 1984\", " +
            "SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]], " +
            "AUTHORITY[\"EPSG\",\"6326\"]], " +
            "PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], " +
            "UNIT[\"degree\", 0.017453292519943295], " +
            "AXIS[\"Geodetic latitude\", NORTH], " +
            "AXIS[\"Geodetic longitude\", EAST], " +
            "AUTHORITY[\"EPSG\",\"4326\"]], " +
            "PROJECTION[\"Transverse Mercator\", AUTHORITY[\"EPSG\",\"9807\"]], " +
            "PARAMETER[\"central_meridian\", 147.0], " +
            "PARAMETER[\"latitude_of_origin\", 0.0], " +
            "PARAMETER[\"scale_factor\", 0.9996], " +
            "PARAMETER[\"false_easting\", 500000.0], " +
            "PARAMETER[\"false_northing\", 10000000.0], " +
            "UNIT[\"m\", 1.0], " +
            "AXIS[\"Easting\", EAST], " +
            "AXIS[\"Northing\", NORTH], " +
            "AUTHORITY[\"EPSG\",\"32755\"]]";

    public Raster2VectorTest() {
    }

    @Before
    public void setUp() {
        int rows = 4, cols = 4;

        /*
         * small raster with 3 regions, two which touch at a corner
         */
        double minX =  700000;
        double maxX =  700400;
        double minY = 5500000;
        double maxY = 5500400;
        CoordinateReferenceSystem crs;
        try {
            crs = CRS.parseWKT(PROJ_WKT);
        } catch(Exception ex) {
            throw new IllegalStateException(ex);
        }

        DataBuffer buffer = new DataBufferInt(DATA, rows * cols);
        SampleModel sample = new BandedSampleModel(DataBuffer.TYPE_INT, cols, rows, 1);
        WritableRaster raster = Raster.createWritableRaster(sample, buffer, null);
        Envelope2D envelope = new Envelope2D(crs, minX, minY, maxX - minX, maxY - minY);

        GridCoverageFactory factory = CoverageFactoryFinder.getGridCoverageFactory(null);
        grid = factory.create("testcov", raster, envelope);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of convert method, of class Raster2Vector.
     */
    @Test
    public void testConvert() {
        System.out.println("convert");
        int band = 0;
        double outside = 0.0d;
        ProgressListener progress = null;
        Raster2Vector instance = new Raster2Vector();
        FeatureCollection result = instance.convert(grid, band, outside, progress);

        double perimeter = 0;
        double area = 0;

        FeatureIterator iter = result.features();
        try {
            while (iter.hasNext()) {
                SimpleFeature feature = (SimpleFeature)iter.next();
                Polygon poly = (Polygon) feature.getDefaultGeometry();
                perimeter += poly.getLength();
                area += poly.getArea();
            }
        } finally {
            iter.close();
        }

        assertTrue(AREA == (int)Math.round(area) && PERIMETER == (int)Math.round(perimeter));
    }

}