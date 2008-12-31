/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
 */

package org.geotools.process.raster;

import com.vividsolutions.jts.algorithm.InteriorPointArea;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.process.ProcessFactory;
import org.geotools.process.impl.AbstractProcess;
import org.geotools.util.NullProgressListener;
import org.geotools.util.SubProgressListener;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.ProgressListener;

/**
 * A class to vectorize discrete regions of uniform data in the specified band of a GridCoverage2D
 * object. Data are treated as double values regardless of the data type of the input grid coverage.
 * <p>
 * Instances of this class are created with {@linkplain RasterToVectorFactory#create() }.
 * <p>
 * Simple example of use:
 * 
 * <pre>{@code \u0000
 * GridCoverage2D grid = doSomething();
 * RasterToVectorFactory factory = new RasterToVectorFactory();
 * RasterToVectorProcess r2v = factory.create();
 * 
 * Map<String, Object> params = new HashMap<String, Object>()
 * params.put(RasterToVectorFactory.RASTER.key, grid);
 * params.put(RasterToVectorFactory.BAND.key, 0);
 * Set<Double> outside = new HashSet<Double>();  // can be any Collection class
 * outside.add(0d);
 * outside.add(2d);
 * outside.add(3d);
 * params.put(RasterToVectorFactory.OUTSIDE.key, outside);
 *  
 * Map<String, Object results = r2v.execute(params, new NullProgressListener());
 * FeatureCollection boundaries = (FeatureCollection) results.get(RasterToVectorFactory.RESULT_FEATURES.key);
 *}</pre>
 * 
 * @author Michael Bedward, Jody Garnett
 * @since 2.6
 */
public class RasterToVectorProcess extends AbstractProcess {

    /* the JTS object that does all the topological work for us */
    private Polygonizer polygonizer;

    /* the coverage presently being processed */
    private GridCoverage2D coverage;

    // the raster retrieved from the coverage
    // private Raster raster;

    /**
     * The transform to use to convert pixel lower right corner positions to real world coordinates
     */
    private MathTransform2D transformLR;

    // raster row and col bounds
    private int minRasterRow, maxRasterRow;

    private int minRasterCol, maxRasterCol;

    // positions in curData matrix just to avoid confusion
    private static final int TL = 0;
    private static final int TR = 1;
    private static final int BL = 2;
    private static final int BR = 3;

    // these are used to identify the orientation of corner touches
    // between possibly separate polygons with the same value
    private static final int TL_BR = 4;
    private static final int TR_BL = 5;
    private static final int CROSS = 6;

    // Precision of comparison in the function different(a, b)
    private static final double EPSILON = 1.0e-10d;

    /*
     * array of Coor objects that store end-points of vertical lines under construction
     */
    private Map<Integer, LineSegment> vertLines;

    /*
     * end-points of horizontal line under construction
     */
    private LineSegment horizLine;

    /*
     * collection of line strings on the boundary of raster regions
     */
    private List<LineString> lines;
    
    /*
     * list of corner touches between possibly separate polygons of
     * the same value. Each Coordinate has x:y = col:row and z set
     * to either TL_BR or TR_BL to indicate the orientation of the
     * corner touch.
     */
    List<Coordinate> cornerTouches;

    /*
     * input image
     */
    private TiledImage image;
    
    /**
     * Package-access constructor. Client code should use the public
     * {@linkplain RasterToVectorFactory#create } method
     * @param factory
     */
    RasterToVectorProcess(ProcessFactory factory) {
        super(factory);
    }

    /**
     * Run the process and return a Map of result objects. 
     * <p>
     * Presently, the returned Map will contain a single object: the FeatureCollection
     * of vector polygons which can be retrieved as follows
     * <p>
     * {@code FeatureCollection features = 
     * (FeatureCollection) resultsMap.get(RasterToVectorFactory.RESULT_FEATURES.key);}
     * 
     * @param input a map of the following input parameters:
     * <table>
     * <tr>
     * <td>Key</td><td>Description</td>
     * </table>
     * @param monitor
     * @return a Map containing result objects 
     */
    public Map<String, Object> execute(Map<String, Object> input, ProgressListener monitor) {
        if (monitor == null) {
            monitor = new NullProgressListener();
        }
        try {
            GridCoverage2D raster = (GridCoverage2D) input.get(RasterToVectorFactory.RASTER.key);
            int band = (Integer) input.get(RasterToVectorFactory.BAND.key);
            Collection<Double> outsideValues = (Collection<Double>)input.get(
                    RasterToVectorFactory.OUTSIDE.key);

            FeatureCollection features = convert(raster, band, outsideValues, monitor);

            Map<String, Object> results = new HashMap<String, Object>();
            results.put(RasterToVectorFactory.RESULT_FEATURES.key, features);
            return results;
        } finally {
            monitor.complete();
        }
    }
    

    /**
     * Convert the input raster coverage to vector polygons. This is a package-access method.
     * Client code should start the process via the 
     * {@linkplain org.geotools.process.Process#execute } method.
     * 
     * @param cover
     *            the input coverage
     * @param band
     *            the index of the band to be vectorized
     * @param outside
     *            a collection of one or more values which represent 'outside' or no data
     *
     * @return a FeatureCollection containing simple polygon features
     * 
     */
    private FeatureCollection convert(GridCoverage2D grid, int band, Collection<Double> outsideValues,
            ProgressListener progress) {
        if (progress == null)
            progress = new NullProgressListener();
        
        initialize(grid, new SubProgressListener(progress, 0.3f));
        vectorizeAndCollectBoundaries(band, outsideValues, new SubProgressListener(progress, 0.3f));

        /***********************************************************
         * Assemble the LineStringss into Polygons, and create the collection of features to return
         * 
         ***********************************************************/
        SimpleFeatureType schema = RasterToVectorFactory.getSchema(grid
                .getCoordinateReferenceSystem());
        FeatureCollection features = assembleFeatures(grid, band, schema, new SubProgressListener(
                progress, 0.4f));

        return features;
    }

    /**
     * Assemble a feature collection by polygonizing the boundary segments that
     * have been collected by the vectorizing algorithm.
     * 
     * @param grid the input grid coverage
     * @param band the band containing the data to vectorize
     * @param type feature type
     * @param progress a progress listener (may be null)
     * @return a new FeatureCollection containing the boundary polygons
     */
    private FeatureCollection assembleFeatures(GridCoverage2D grid, int band,
            SimpleFeatureType type, ProgressListener progress) {
        if (progress == null)
            progress = new NullProgressListener();
        FeatureCollection features = FeatureCollections.newCollection();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);

        InteriorPointArea ipa;
        Point2D p = new Point2D.Double();
        double[] bandData = new double[grid.getNumSampleDimensions()];

        polygonizer.add(lines);
        Collection polygons = polygonizer.getPolygons();
        int size = polygons.size();
        try {
            progress.started();

            int index = 0;
            for (Iterator i = polygons.iterator(); i.hasNext(); index++) {

                if (progress.isCanceled()) {
                    throw new CancellationException();
                }
                progress.progress(((float) index) / ((float) size));

                Polygon poly = (Polygon) i.next();
                ipa = new InteriorPointArea(poly);
                Coordinate c = ipa.getInteriorPoint();
                p.setLocation(c.x, c.y);
                bandData = grid.evaluate(p, bandData);

                builder.add(poly);
                builder.add((int) bandData[band]);

                features.add(builder.buildFeature(null));
            }
            return features;
        } finally {
            progress.complete();
        }
    }

    /**
     * Set convenience data fields and create the data objects
     * @param coverage the input grid coverage
     * @param progress a progress listener (may be null)
     */
    private void initialize(GridCoverage2D coverage, ProgressListener progress) {
        if (progress == null)
            progress = new NullProgressListener();
        try {
            progress.started();
            this.coverage = coverage;
            // note we cannot call getData() since that would load
            // the entire raster into memory
            // this.raster = coverage.getRenderedImage().getData();

            // image used to sample the grid coverage
            image = new TiledImage(coverage.getRenderedImage(), true);

            this.transformLR = coverage.getGridGeometry().getGridToCRS2D(
                    PixelOrientation.LOWER_RIGHT);
            progress.progress(0.3f);

            minRasterRow = image.getMinY();
            maxRasterRow = minRasterRow + image.getHeight() - 1;
            minRasterCol = image.getMinX();
            maxRasterCol = minRasterCol + image.getWidth() - 1;

            lines = new ArrayList<LineString>();
            polygonizer = new Polygonizer();

            progress.progress(0.8f);

            vertLines = new HashMap<Integer, LineSegment>();
            
            cornerTouches = new ArrayList<Coordinate>();
            
        } finally {
            progress.complete();
        }
    }

    /**
     * Vectorize the boundaries of regions of uniform value in the input grid coverage
     * and collect the boundaries as LineStrings
     * @param band index of the band which contains the data to be vectorized
     * @param outside a double value indicating 'outside' or nodata
     * @param progress a progress listener (may be null)
     */
    private void vectorizeAndCollectBoundaries(int band, Collection<Double> outsideValues, ProgressListener progress) {
        if (progress == null)
            progress = new NullProgressListener();

        try {
            // a 2x2 matrix of double values used as a moving window
            double[] curData = new double[4];
            RandomIter imageIter = RandomIterFactory.create(image, null);
            
            double defOutside = (Double) outsideValues.toArray()[0];

            // we add a virtual border, one cell wide, coded as 'outside'
            // around the raster
            for (int row = minRasterRow - 1; row <= maxRasterRow; row++) {
                if (progress.isCanceled()) {
                    throw new CancellationException();
                }
                progress.progress(((float) row) / ((float) maxRasterRow));
                curData[TR] = curData[BR] = defOutside;
                for (int col = minRasterCol - 1; col <= maxRasterCol; col++) {
                    boolean[] ok = inDataWindow(row, col);

                    curData[TL] = curData[TR];
                    curData[BL] = curData[BR];
                    
                    curData[TR] = (ok[TR] ? imageIter.getSampleDouble(col + 1, row, band) : defOutside);
                    if (outsideValues.contains(curData[TR])) {
                        curData[TR] = defOutside;
                    }

                    curData[BR] = (ok[BR] ? imageIter.getSampleDouble(col + 1, row + 1, band) : defOutside);
                    if (outsideValues.contains(curData[BR])) {
                        curData[BR] = defOutside;
                    }

                    updateCoordList(row, col, curData);
                }
            }
        } finally {
            progress.complete();
        }
    }

    /**
     * Check the position of the data window with regard to the grid coverage
     * boundaries. We do this because a virtual, single-cell-width border is
     * placed around the input data.
     *
     * @param row index of the image row in the top left cell of the data window
     * @param col index of the image col in the top left cell of the data window
     * @return an array of four boolean values to be indexed with the TL, TR, BL
     * and BR constants.
     */
    private boolean[] inDataWindow(int row, int col) {
        boolean[] ok = new boolean[4];

        int rowflag = (row < minRasterRow ? -1 : (row >= maxRasterRow ? 1 : 0));
        int colflag = (col < minRasterCol ? -1 : (col >= maxRasterCol ? 1 : 0));

        ok[TL] = rowflag >= 0 && colflag >= 0;
        ok[TR] = rowflag >= 0 && colflag < 1;
        ok[BL] = rowflag < 1 && colflag >= 0;
        ok[BR] = rowflag < 1 && colflag < 1;

        return ok;
    }

    /**
     * This method controls the construction of line segments that border regions of uniform data
     * in the raster. See the nbrConfig method for more details.
     *
     * @param row index of the image row in the top left cell of the 2x2 data window
     * @param col index of the image col in the top left cell of the 2x2 data window
     * @param curData values in the current data window
     */
    private void updateCoordList(int row, int col, double[] curData) {
        LineSegment seg;

        switch (nbrConfig(curData)) {
        case 0:
            // vertical line continuing
            // nothing to do
            break;

        case 1:
            // bottom right corner
            // new horizontal and vertical lines
            horizLine = new LineSegment();
            horizLine.p0.x = col;

            seg = new LineSegment();
            seg.p0.y = row;
            vertLines.put(col, seg);
            break;

        case 2:
            // horizontal line continuing
            // nothing to do
            break;

        case 3:
            // bottom left corner
            // end of horizontal line; start of new vertical line
            horizLine.p1.x = col;
            addHorizLine(row);
            horizLine = null;

            seg = new LineSegment();
            seg.p0.y = row;
            vertLines.put(col, seg);
            break;

        case 4:
            // top left corner
            // end of horizontal line; end of vertical line
            horizLine.p1.x = col;
            addHorizLine(row);
            horizLine = null;

            seg = vertLines.get(col);
            seg.p1.y = row;
            addVertLine(col);
            vertLines.remove(col);
            break;

        case 5:
            // top right corner
            // start horiztonal line; end vertical line
            horizLine = new LineSegment();
            horizLine.p0.x = col;

            seg = vertLines.get(col);
            seg.p1.y = row;
            addVertLine(col);
            vertLines.remove(col);
            break;

        case 6:
            // inverted T in upper half
            // end horiztonal line; start new horizontal line; end vertical line
            horizLine.p1.x = col;
            addHorizLine(row);

            horizLine.p0.x = col;

            seg = vertLines.get(col);
            seg.p1.y = row;
            addVertLine(col);
            vertLines.remove(col);
            break;

        case 7:
            // T in lower half
            // end horizontal line; start new horizontal line; start new vertical line
            horizLine.p1.x = col;
            addHorizLine(row);

            horizLine.p0.x = col;

            seg = new LineSegment();
            seg.p0.y = row;
            vertLines.put(col, seg);
            break;

        case 8:
            // T pointing left
            // end horizontal line; end vertical line; start new vertical line
            horizLine.p1.x = col;
            addHorizLine(row);
            horizLine = null;

            seg = vertLines.get(col);
            seg.p1.y = row;
            addVertLine(col);

            seg = new LineSegment();
            seg.p0.y = row;
            vertLines.put(col, seg);
            break;

        case 9:
            // T pointing right
            // start new horizontal line; end vertical line; start new vertical line
            horizLine = new LineSegment();
            horizLine.p0.x = col;

            seg = vertLines.get(col);
            seg.p1.y = row;
            addVertLine(col);

            seg = new LineSegment();
            seg.p0.y = row;
            vertLines.put(col, seg);
            break;

        case 10:
            // cross
            // end horizontal line; start new horizontal line
            // end vertical line; start new vertical line
            horizLine.p1.x = col;
            addHorizLine(row);

            horizLine.p0.x = col;

            seg = vertLines.get(col);
            seg.p1.y = row;
            addVertLine(col);

            seg = new LineSegment();
            seg.p0.y = row;
            vertLines.put(col, seg);

            int z = -1;
            if (different(curData[TL], curData[BR])) {
                if (!different(curData[TR], curData[BL])) {
                    z = CROSS;
                }
            } else {
                if (different(curData[TR], curData[BL])) {
                    z = TL_BR;
                } else {
                    z = TR_BL;
                }
            }
            if (z != -1) {
                cornerTouches.add(new Coordinate(col, row, z));
            }
            break;

        case 11:
            // uniform
            // nothing to do
            break;
        }
    }

    /**
     * Examine the values in the 2x2 kernel and match to one of
     * the cases in the table below:
     * <pre>
     *  0) AB   1) AA   2) AA   3) AA
     *     AB      AB      BB      BA
     *
     *  4) AB   5) AB   6) AB   7) AA
     *     BB      AA      CC      BC
     *
     *  8) AB   9) AB  10) AB  11) AA
     *     CB      AC      CD      AA
     * </pre>
     * These patterns are those used in the GRASS raster to vector routine.
     * @param curData array of current data window values
     * @return integer id of the matching configuration
     */
    private int nbrConfig(double[] curData) {
        if (different(curData[TL], curData[TR])) { // 0, 4, 5, 6, 8, 9, 10
            if (different(curData[TL], curData[BL])) { // 4, 6, 8, 10
                if (different(curData[BL], curData[BR])) { // 8, 10
                    if (different(curData[TR], curData[BR])) {
                        return 10;
                    } else {
                        return 8;
                    }
                } else { // 4, 6
                    if (different(curData[TR], curData[BR])) {
                        return 6;
                    } else {
                        return 4;
                    }
                }
            } else { // 0, 5, 9
                if (different(curData[BL], curData[BR])) { // 0, 9
                    if (different(curData[TR], curData[BR])) {
                        return 9;
                    } else {
                        return 0;
                    }
                } else {
                    return 5;
                }
            }
        } else { // 1, 2, 3, 7, 11
            if (different(curData[TL], curData[BL])) { // 2, 3, 7
                if (different(curData[BL], curData[BR])) { // 3, 7
                    if (different(curData[TR], curData[BR])) {
                        return 7;
                    } else {
                        return 3;
                    }
                } else {
                    return 2;
                }
            } else { // 1, 11
                if (different(curData[TR], curData[BR])) {
                    return 1;
                } else {
                    return 11;
                }
            }
        }
    }

    /**
     * Create a LineString for a newly constructed horizontal border segment
     * @param row index of the image row in the top left cell of the current data window
     */
    private void addHorizLine(int row) {
        Point2D pixelStart = new Point2D.Double(horizLine.p0.x, row);
        Point2D pixelEnd = new Point2D.Double(horizLine.p1.x, row);
        Point2D rwStart = new Point2D.Double();
        Point2D rwEnd = new Point2D.Double();

        try {
            transformLR.transform(pixelStart, rwStart);
            transformLR.transform(pixelEnd, rwEnd);
        } catch (TransformException ex) {
            Logger.getLogger(RasterToVectorProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        Coordinate[] coords = new Coordinate[] { new Coordinate(rwStart.getX(), rwStart.getY()),
                new Coordinate(rwEnd.getX(), rwEnd.getY()) };

        GeometryFactory gf = new GeometryFactory();
        lines.add(gf.createLineString(coords));
    }

    /**
     * Create a LineString for a newly constructed vertical border segment
     * @param col index of the image column in the top-left cell of the current data window
     */
    private void addVertLine(int col) {
        Point2D pixelStart = new Point2D.Double(col, vertLines.get(col).p0.y);
        Point2D pixelEnd = new Point2D.Double(col, vertLines.get(col).p1.y);
        Point2D rwStart = new Point2D.Double();
        Point2D rwEnd = new Point2D.Double();

        try {
            transformLR.transform(pixelStart, rwStart);
            transformLR.transform(pixelEnd, rwEnd);
        } catch (TransformException ex) {
            Logger.getLogger(RasterToVectorProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        Coordinate[] coords = new Coordinate[] { new Coordinate(rwStart.getX(), rwStart.getY()),
                new Coordinate(rwEnd.getX(), rwEnd.getY()) };

        GeometryFactory gf = new GeometryFactory();
        lines.add(gf.createLineString(coords));
    }

    /**
     * Check for difference between two double values within a set tolerance
     * @param a first value
     * @param b second value
     * @return true if the values are different; false otherwise
     */
    private boolean different(double a, double b) {
        if (Math.abs(a - b) > EPSILON) {
            return true;
        } else {
            return false;
        }
    }

}
