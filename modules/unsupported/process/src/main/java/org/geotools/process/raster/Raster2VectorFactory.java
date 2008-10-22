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

import java.awt.geom.Point2D;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.Parameter;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.process.Process;
import org.geotools.process.ProcessFactory;
import org.geotools.process.impl.AbstractProcess;
import org.geotools.text.Text;
import org.geotools.util.NullProgressListener;
import org.geotools.util.SubProgressListener;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.InternationalString;
import org.opengis.util.ProgressListener;

import com.vividsolutions.jts.algorithm.InteriorPointArea;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

/**
 * Process for converting a raster to a vector.
 * 
 * As a learning exercise I've re-written the Java version of the GRASS raster
 * to vector routine. I'm using it currently with very modest rasters and so
 * far, so good. I'd be grateful for any comments or (especially) suggestions
 * for improvements. And if it's useful to you, in whole or part, please feel
 * free to do whatever.
 * 
 * @author Michael Bedward
 */
public class Raster2VectorFactory implements ProcessFactory {

	public Process create() {
		return new AbstractProcess(this){
			public Map<String, Object> execute(Map<String, Object> input,
					ProgressListener monitor) {
				if( monitor == null ) monitor = new NullProgressListener();
				try {
					GridCoverage2D raster = (GridCoverage2D) input.get("raster");
					int band = (Integer) input.get("band");
					double nodata = (Integer) input.get("nodata");
					
					Raster2Vector engine = new Raster2Vector();
					FeatureCollection features = engine.convert(raster, band, nodata, monitor );
					
					Map<String,Object> results = new HashMap<String,Object>();
					results.put("features", features );
					return results;
				}
				finally {
					monitor.complete();
				}
			}						
		};
	}

	public InternationalString getDescription() {
		return Text.text("Raster to Vector transformation");
	}

	public String getName() {
		return "Raster2Vector";
	}

	/**
	 * Description input parameters.
	 * <ul>
	 * <li>raster: the input coverage
	 * <li>band: the index of the band to be vectorized
	 * <li>nodata: a value to represent 'outside' or no data
	 * </ul>
	 * @return Map of Parameter describing valid input parameters
	 */
	public Map<String, Parameter<?>> getParameterInfo() {
		Map<String, Parameter<?>> info = new HashMap<String, Parameter<?>>();
		info.put("raster", new Parameter("raster", GridCoverage2D.class, Text
				.text("GridCoverage"), Text.text("The input coverage")));
		info.put("band", new Parameter("band", GridCoverage2D.class, Text
				.text("Band"), Text
				.text("the index of the band to be vectorized")));
		info.put("nodata", new Parameter("nodata", Double.class, Text
				.text("Outside"), Text
				.text("a value to represent 'outside' or no data")));
		return info;
	}

	public Map<String, Parameter<?>> getResultInfo(
			Map<String, Object> parameters) throws IllegalArgumentException {
		Map<String, Parameter<?>> info = new HashMap<String, Parameter<?>>();
		// we should be able to record the FeatureType here; but it is not well
		// defined in a public schema?
		SimpleFeatureType schema = getSchema( null );
		Map<String,Object> metadata = new HashMap<String, Object>();
		metadata.put( Parameter.FEATURE_TYPE, schema );		
		info.put("features", new Parameter("Features", FeatureCollection.class, Text
				.text("Features"), Text.text("The generated features"), metadata));
		return info;
	}

	public InternationalString getTitle() {
		return Text.text("Raster2Vector");
	}

	public String getVersion() {
		return "0.0.1";
	}

	public boolean supportsProgress() {
		return true;
	}
    /**
     * We can generate a schema; but we need to know the CoordinateReferenceSystem.
     * 
     * @param crs
     * @return
     */
	public static SimpleFeatureType getSchema(CoordinateReferenceSystem crs) {
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("R2Vpolygons");
        if( crs != null ){
        	typeBuilder.setCRS(crs);
        }
        typeBuilder.add("shape", Polygon.class, (CoordinateReferenceSystem)null);
        typeBuilder.add("code", Integer.class);
        SimpleFeatureType type = typeBuilder.buildFeatureType();
		return type;
	}
}

/**
 * A class to vectorize discrete regions of uniform data in the specified
 * band of a GridCoverage2D object.
 * 
 * @author Michael Bedward <michael.bedward@gmail.com>
 */
class Raster2Vector {

    // the JTS object that does all the topological work for us
    private Polygonizer polygonizer;
    
    // the coverage presently being processed
    private GridCoverage2D coverage;
    
    // the raster retrieved from the coverage
    private Raster raster;
    
    // the transform to use to convert pixel lower right corner
    // positions to real world coordinates
    private MathTransform2D transformLR;
    
    // raster row and col bounds
    private int minRasterRow, maxRasterRow;
    private int minRasterCol, maxRasterCol;
    
    // a 2x2 matrix of double values used as a moving window
    private double[] curData = new double[4];

    // positions in curData matrix just to avoid confusion
    private static final int TL = 0;
    private static final int TR = 1;
    private static final int BL = 2;
    private static final int BR = 3;
    
    // Precision of comparison in the function different(a, b)
    private static final double epsilon = 1.0e-8d;
    
    // array of Coor objects that store end-points of vertical lines 
    // under construction
    private Map<Integer, LineSeg> vertLines;
    
    // end-points of horizontal line under construction    
    private LineSeg horizLine;
    
    // collection of line strings on the boundary of raster regions
    private List<LineString> lines;
    
    
    public Raster2Vector() {
    }

    /**
     * Convert the input raster coverage to vector polygons.
     * @param cover the input coverage
     * @param band the index of the band to be vectorized
     * @param outside a value to represent 'outside' or no data
     * @return a FeatureCollection containing simple polygon features
     * 
     * @todo Presently it is assumed that there is only a single tile
     * in the coverage raster. Need to extend the code to handle mutliple
     * tiles.
     * 
     */
    public FeatureCollection convert(GridCoverage2D grid, int band, double outside, ProgressListener progress) {
        if( progress == null ) progress = new NullProgressListener();
        
    	initialize(grid, new SubProgressListener(progress,0.3f));
        vectorizeAndCollectBoundaries(band, outside, new SubProgressListener(progress,0.3f));
        
        /***********************************************************
         * Assemble the LineStringss into Polygons, and create
         * the collection of features to return
         *
         ***********************************************************/
        SimpleFeatureType schema = Raster2VectorFactory.getSchema(grid.getCoordinateReferenceSystem());        
        FeatureCollection features = assembleFeatures(grid, band, schema, new SubProgressListener(progress,0.4f));
        
        return features;
    }

	private FeatureCollection assembleFeatures(GridCoverage2D grid, int band,
			SimpleFeatureType type, ProgressListener progress) {
		if( progress == null ) progress = new NullProgressListener();
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
	        
        	int index=0;
			for (Iterator i=polygons.iterator(); i.hasNext(); index++) {
				
				if( progress.isCanceled() ){
					throw new CancellationException();
				}
				progress.progress( ((float)index) / ((float)size) );
				
				Polygon poly = (Polygon) i.next();
	            ipa = new InteriorPointArea(poly);
	            Coordinate c = ipa.getInteriorPoint();
	            p.setLocation(c.x, c.y);
	            bandData = grid.evaluate(p, bandData);
	            
	            builder.add(poly);
	            builder.add((int)bandData[band]);
	            
	            features.add(builder.buildFeature(null));
	        }
			return features;
        }
        finally {
        	progress.complete();
        }
	}


	
    /***********************************************************
     * Vectorize discrete regions in the raster and collect the
     * boundaries as LineStrings
     * 
     ***********************************************************/
	private void vectorizeAndCollectBoundaries(int band, double outside, ProgressListener progress) {
		if( progress == null) progress = new NullProgressListener();
		
		try {
			// we add a virtual border, one cell wide, coded as 'outside'
	        // around the raster
	        for (int row = minRasterRow-1; row <= maxRasterRow; row++) {	        	
	        	if( progress.isCanceled() ) {
	        		throw new CancellationException();
	        	}
	        	progress.progress( ((float)row) / ((float)maxRasterRow) );
	            for (int col = minRasterCol-1; col <= maxRasterCol; col++) {
	                boolean[] ok = inDataWindow(row, col);
	                if (ok[TL] && ok[TR] && ok[BL] && ok[BR]) {
	                    curData = raster.getSamples(col, row, 2, 2, band, curData);
	                } else {
	                    curData[TL] = (ok[TL] ? raster.getSampleDouble(col, row, band) : outside);
	                    curData[TR] = (ok[TR] ? raster.getSampleDouble(col+1, row, band) : outside);
	                    curData[BL] = (ok[BL] ? raster.getSampleDouble(col, row+1, band) : outside);
	                    curData[BR] = (ok[BR] ? raster.getSampleDouble(col+1, row+1, band) : outside);
	                }
	                updateCoordList(row, col);
	            }
	        }
		}
		finally {
			progress.complete();
		}
	}

    /*
     * Set convenience data fields and create the data objects
     * 
     */
    private void initialize(GridCoverage2D coverage, ProgressListener progress) {
    	if( progress == null ) progress = new NullProgressListener();
    	try {
    		progress.started();
    		
	        this.coverage = coverage; 
	        this.raster = coverage.getRenderedImage().getData();
	        this.transformLR = coverage.getGridGeometry().getGridToCRS2D(PixelOrientation.LOWER_RIGHT);
	        progress.progress(0.3f);
	        
	        minRasterRow = raster.getMinY();
	        maxRasterRow = minRasterRow + raster.getHeight() - 1;
	        minRasterCol = raster.getMinX();
	        maxRasterCol = minRasterCol + raster.getWidth() - 1;
	
	        lines = new ArrayList<LineString>();
	        polygonizer = new Polygonizer();
	
	        progress.progress(0.8f);
	        
	        vertLines = new HashMap<Integer, LineSeg>();
    	}
    	finally {
    		progress.complete();
    	}
    }
    
    /*
     * Check position of the 2x2 curData matrix with respect to the raster
     * bounds. The values of row,col are the position of curData[TL].
     */
    private boolean[] inDataWindow(int row, int col) {
        boolean[] ok = new boolean[4];

        int rowflag = (row < minRasterRow ? -1 : (row >= maxRasterRow ? 1 : 0));
        int colflag = (col < minRasterCol ? -1 : (col >= maxRasterCol ? 1 : 0));

        ok[TL] = rowflag >= 0 && colflag >= 0;
        ok[TR] = rowflag >= 0 && colflag < 1;
        ok[BL] = rowflag < 1  && colflag >= 0;
        ok[BR] = rowflag < 1  && colflag < 1;
        
        return ok;
    }
    
    /*
     * This function controls the construction of line segments that
     * border regions of uniform data in the raster. See the nbrConfig()
     * function for more details on how borders are inferred from the 
     * 2x2 curData matrix.
     * 
     */
    private void updateCoordList(int row, int col) {
        LineSeg seg;
        
        switch (nbrConfig()) {
            case 0:
                // vertical line continuing
                // nothing to do
                break;

            case 1:
                // bottom right corner
                // new horizontal and vertical lines
                horizLine = new LineSeg();
                horizLine.start = col;
                
                seg = new LineSeg();
                seg.start = row;
                vertLines.put(col, seg);
                break;

            case 2:
                // horizontal line continuing
                // nothing to do
                break;

            case 3:
                // bottom left corner
                // end of horizontal line; start of new vertical line
                horizLine.end = col;
                addHorizLine(row);
                horizLine = null;
                
                seg = new LineSeg();
                seg.start = row;
                vertLines.put(col, seg);
                break;

            case 4:
                // top left corner
                // end of horizontal line; end of vertical line
                horizLine.end = col;
                addHorizLine(row);
                horizLine = null;
                
                seg = vertLines.get(col);
                seg.end = row;
                addVertLine(col);
                vertLines.remove(col);
                break;

            case 5:
                // top right corner
                // start horiztonal line; end vertical line
                horizLine = new LineSeg();
                horizLine.start = col;
                
                seg = vertLines.get(col);
                seg.end = row;
                addVertLine(col);
                vertLines.remove(col);
                break;

            case 6:
                // inverted T in upper half
                // end horiztonal line; start new horizontal line; end vertical line 
                horizLine.end = col;
                addHorizLine(row);

                horizLine.start = col;
                
                seg = vertLines.get(col);
                seg.end = row;
                addVertLine(col);
                vertLines.remove(col);
                break;

            case 7:
                // T in lower half
                // end horizontal line; start new horizontal line; start new vertical line
                horizLine.end = col;
                addHorizLine(row);

                horizLine.start = col;

                seg = new LineSeg();
                seg.start = row;
                vertLines.put(col, seg);
                break;

            case 8:
                // T pointing left
                // end horizontal line; end vertical line; start new vertical line
                horizLine.end = col;
                addHorizLine(row);
                horizLine = null;

                seg = vertLines.get(col);
                seg.end = row;
                addVertLine(col);

                seg = new LineSeg();
                seg.start = row;
                vertLines.put(col, seg);
                break;

            case 9:
                // T pointing right
                // start new horizontal line; end vertical line; start new vertical line
                horizLine = new LineSeg();
                horizLine.start = col;
                
                seg = vertLines.get(col);
                seg.end = row;
                addVertLine(col);

                seg = new LineSeg();
                seg.start = row;
                vertLines.put(col, seg);
                break;

            case 10:
                // cross
                // end horizontal line; start new horizontal line
                // end vertical line; start new vertical line
                horizLine.end = col;
                addHorizLine(row);

                horizLine.start = col;
                
                seg = vertLines.get(col);
                seg.end = row;
                addVertLine(col);

                seg = new LineSeg();
                seg.start = row;
                vertLines.put(col, seg);
                break;
                
            case 11:
                // uniform
                // nothing to do
                break;
        }
    }

        
    /*
     * 
     * Check 2 x 2 matrix and return case from table below   
     * <pre>
     *    *--*--*      *--*--*      *--*--*      *--*--*      
     *    |  |  |      |     |      |     |      |     |      
     *    *  |  *      *  *--*      *-----*      *--*  *      
     *    |  |  |      |  |  |      |     |      |  |  |      
     *    *--*--*      *--*--*      *--*--*      *--*--*      
     *
     *       0            1            2            3         
     *
     *    *--*--*      *--*--*      *--*--*      *--*--*      
     *    |  |  |      |  |  |      |  |  |      |     |      
     *    *--*  *      *  *--*      *--*--*      *--*--*      
     *    |     |      |     |      |     |      |  |  |      
     *    *--*--*      *--*--*      *--*--*      *--*--*      
     *
     *       4            5            6            7         
     *
     *    *--*--*      *--*--*      *--*--*      *--*--*      
     *    |  |  |      |  |  |      |  |  |      |     |      
     *    *--*  *      *  *--*      *--*--*      *     *      
     *    |  |  |      |  |  |      |  |  |      |     |      
     *    *--*--*      *--*--*      *--*--*      *--*--*      
     *							  
     *       8            9            10           11        
     * </pre>
     * 
     * These patterns are those used in the GRASS raster to
     * vector routine.
     * 
     */
    private int nbrConfig() {
        if (different(curData[0], curData[1])) { // 0, 4, 5, 6, 8, 9, 10
            if (different(curData[0], curData[2])) {  // 4, 6, 8, 10
                if (different(curData[2], curData[3])) {  // 8, 10
                    if (different(curData[1], curData[3])) {
                        return 10;
                    } else {
                        return 8;
                    }
                } else {  // 4, 6
                    if (different(curData[1], curData[3])) {
                        return 6;
                    } else {
                        return 4;
                    }
                }
            } else {  // 0, 5, 9
                if (different(curData[2], curData[3])) {  // 0, 9
                    if (different(curData[1], curData[3])) {
                        return 9;
                    } else {
                        return 0;
                    }
                } else {
                    return 5;
                }
            }
        } else {  // 1, 2, 3, 7, 11
            if (different(curData[0], curData[2])) {  // 2, 3, 7
                if (different(curData[2], curData[3])) {  // 3, 7
                    if (different(curData[1], curData[3])) {
                        return 7;
                    } else {
                        return 3;
                    }
                } else {
                    return 2;
                }
            } else {  // 1, 11
                if (different(curData[1], curData[3])) {
                    return 1;
                } else {
                    return 11;
                }
            }
        }
    }
    

    /* 
     * Create a LineString for a newly constructed horizontal 
     * border segment
     */
    private void addHorizLine(int row) {
        Point2D pixelStart = new Point2D.Double(horizLine.start, row);
        Point2D pixelEnd = new Point2D.Double(horizLine.end, row);
        Point2D rwStart = new Point2D.Double();
        Point2D rwEnd = new Point2D.Double();

        try {
            transformLR.transform(pixelStart, rwStart);
            transformLR.transform(pixelEnd, rwEnd);
        } catch (TransformException ex) {
            Logger.getLogger(Raster2Vector.class.getName()).log(Level.SEVERE, null, ex);
        }

        Coordinate[] coords = new Coordinate[]{
            new Coordinate(rwStart.getX(), rwStart.getY()), 
            new Coordinate(rwEnd.getX(), rwEnd.getY())};

        GeometryFactory gf = new GeometryFactory();
        lines.add(gf.createLineString(coords));
    }


    /* 
     * Create a LineString for a newly constructed vertical
     * border segment
     */
    private void addVertLine(int col) {
        Point2D pixelStart = new Point2D.Double(col, vertLines.get(col).start);
        Point2D pixelEnd = new Point2D.Double(col, vertLines.get(col).end);
        Point2D rwStart = new Point2D.Double();
        Point2D rwEnd = new Point2D.Double();

        try {
            transformLR.transform(pixelStart, rwStart);
            transformLR.transform(pixelEnd, rwEnd);
        } catch (TransformException ex) {
            Logger.getLogger(Raster2Vector.class.getName()).log(Level.SEVERE, null, ex);
        }

        Coordinate[] coords = new Coordinate[]{
            new Coordinate(rwStart.getX(), rwStart.getY()),
            new Coordinate(rwEnd.getX(), rwEnd.getY())
        };
        
        GeometryFactory gf = new GeometryFactory();
        lines.add(gf.createLineString(coords));
    }

    
    /*
     * Compare two double values
     */
    private boolean different(double a, double b) {
        if (Math.abs(a - b) > epsilon) {
            return true;
        } else {
            return false;
        }
    }
}

/**
 * LineSegment.
 * (We should be able to replace this with a JTS class)
 */
class LineSeg {
    public int start; // col for horizontal line, row for vertical line
    public int end;   // ditto
    public int left;  // when going right for horizontal line, down for vertical line
    public int right; // ditto
}


