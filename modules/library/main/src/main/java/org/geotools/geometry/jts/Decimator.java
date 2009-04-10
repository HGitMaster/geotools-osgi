/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.geometry.jts;

import java.awt.Rectangle;

import org.geotools.referencing.operation.matrix.AffineTransform2D;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Accepts geometries and collapses all the vertices that will be rendered to
 * the same pixel. This class works only if the Geometries are based on 
 * {@link LiteCoordinateSequence} instances.
 * 
 * @author jeichar
 * @since 2.1.x
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/library/main/src/main/java/org/geotools/geometry/jts/Decimator.java $
 */
public final class Decimator {

	private double spanx = -1;

	private double spany = -1;
	
	/**
     * Builds a decimator that will generalize geometries so that two subsequent points
     * will be at least pixelDistance away from each other when painted on the screen.
     * Set pixelDistance to 0 if you don't want any generalization (but just a transformation)
     * 
     * @param screenToWorld
     * @param paintArea
     * @param pixelDistance 
     */
    public Decimator(MathTransform screenToWorld, Rectangle paintArea, double pixelDistance) {
        if (screenToWorld != null && pixelDistance > 0) {
            try {
                double[] spans = computeGeneralizationDistances(screenToWorld, paintArea, pixelDistance);
                this.spanx = spans[0];
                this.spany = spans[1];
            } catch(TransformException e) {
                throw new RuntimeException("Could not perform the generalization spans computation", e);
            }
        } else {
            this.spanx = 1;
            this.spany = 1;
        }
    }

	/**
	 * djb - noticed that the old way of finding out the decimation is based on
	 * the (0,0) location of the image. This is often wildly unrepresentitive of
	 * the scale of the entire map.
	 * 
	 * A better thing to do is to decimate this on a per-shape basis (and use
	 * the shape's center). Another option would be to sample the image at
	 * different locations (say 9) and choose the smallest spanx/spany you find.
	 * 
	 * Also, if the xform is an affine Xform, you can be a bit more aggressive
	 * in the decimation. If its not an affine xform (ie. its actually doing a
	 * CRS xform), you may find this is a bit too aggressive due to any number
	 * of mathematical issues.
	 * 
	 * This is just a simple method that uses the centre of the given rectangle
	 * instead of (0,0).
	 * 
	 * NOTE: this could need more work based on CRS, but the rectangle is in
	 * pixels so it should be fairly immune to all but crazy projections.
	 * 
	 * 
	 * @param screenToWorld
	 * @param paintArea
	 */
	public Decimator(MathTransform screenToWorld, Rectangle paintArea) {
	    // 0.8 is just so you don't decimate "too much". magic number.
		this(screenToWorld, paintArea, 0.8);
	}

	/**
	 * Given a full transformation from screen to world and the paint area computes a best
	 * guess of the maxium generalization distance that won't make the transformations induced
	 * by the generalization visible on screen. <p>In other words, it computes how long a pixel
	 * is in the native spatial reference system of the data</p>
	 * @param screenToWorld
	 * @param paintArea
	 * @return
	 * @throws TransformException
	 */
    public static double[] computeGeneralizationDistances(MathTransform screenToWorld, Rectangle paintArea, double pixelDistance)
            throws TransformException {
        double[] original = new double[] {
        		paintArea.x + paintArea.width / 2.0,
        		paintArea.y + paintArea.height / 2.0,
        		paintArea.x + paintArea.width / 2.0 + 1,
        		paintArea.y + paintArea.height / 2.0 + 1, };
        double[] coords = new double[4];
        	screenToWorld.transform(original, 0, coords, 0, 2);
        double[] spans = new double[2]; 
        spans[0] = Math.abs(coords[0] - coords[2]) * pixelDistance;
        spans[1] = Math.abs(coords[1] - coords[3]) * pixelDistance;
        return spans;
    }

	/**
	 * @throws TransformException
	 * @deprecated use the other constructor (with rectange) see javadox. This
	 *             works fine, but it the results are often poor if you're also
	 *             doing CRS xforms.
	 */
	public Decimator(MathTransform screenToWorld) {
		this(screenToWorld, new Rectangle()); // do at (0,0)
	}
	
	public Decimator(double spanx, double spany) {
	    this.spanx = spanx;
	    this.spany = spany;
	}

	public final void decimateTransformGeneralize(Geometry geometry,
			MathTransform transform) throws TransformException {
		if (geometry instanceof GeometryCollection) {
			GeometryCollection collection = (GeometryCollection) geometry;
			final int length = collection.getNumGeometries();
			for (int i = 0; i < length; i++) {
				decimateTransformGeneralize(collection.getGeometryN(i),
						transform);
			}
		} else if (geometry instanceof Point) {
			LiteCoordinateSequence seq = (LiteCoordinateSequence) ((Point) geometry)
					.getCoordinateSequence();
			decimateTransformGeneralize(seq, transform);
		} else if (geometry instanceof Polygon) {
			Polygon polygon = (Polygon) geometry;
			decimateTransformGeneralize(polygon.getExteriorRing(), transform);
			final int length = polygon.getNumInteriorRing();
			for (int i = 0; i < length; i++) {
				decimateTransformGeneralize(polygon.getInteriorRingN(i),
						transform);
			}
		} else if (geometry instanceof LineString) {
			LiteCoordinateSequence seq = (LiteCoordinateSequence) ((LineString) geometry)
					.getCoordinateSequence();
			decimateTransformGeneralize(seq, transform);
		}
	}

	/**
	 * decimates JTS geometries.
	 */
	public final void decimate(Geometry geom) {
		if (spanx == -1)
			return;
		if (geom instanceof MultiPoint) {
			// TODO check geometry and if its bbox is too small turn it into a 1
			// point geom
			return;
		}
		if (geom instanceof GeometryCollection) {
			// TODO check geometry and if its bbox is too small turn it into a
			// 1-2 point geom
			// takes a bit of work because the geometry will need to be
			// recreated.
			GeometryCollection collection = (GeometryCollection) geom;
			final int numGeometries = collection.getNumGeometries();
			for (int i = 0; i < numGeometries; i++) {
				decimate(collection.getGeometryN(i));
			}
		} else if (geom instanceof LineString) {
			LineString line = (LineString) geom;
			LiteCoordinateSequence seq = (LiteCoordinateSequence) line
					.getCoordinateSequence();
			if (decimateOnEnvelope(line, seq)) {
				return;
			}
			decimate(line, seq);
		} else if (geom instanceof Polygon) {
			Polygon line = (Polygon) geom;
			decimate(line.getExteriorRing());
			final int numRings = line.getNumInteriorRing();
			for (int i = 0; i < numRings; i++) {
				decimate(line.getInteriorRingN(i));
			}
		}
	}

	/**
	 * @param geom
	 * @param seq
	 */
	private boolean decimateOnEnvelope(Geometry geom, LiteCoordinateSequence seq) {
		Envelope env = geom.getEnvelopeInternal();
		if (env.getWidth() <= spanx && env.getHeight() <= spany) {
		    if(geom instanceof LinearRing) {
		        decimateRingFully(seq);
		        return true;
		    } else {
    			double[] coords = seq.getArray();
    			int dim = seq.getDimension();
    			double[] newcoords = new double[dim * 2];
    			for (int i = 0; i < dim; i++) {
    				newcoords[i] = coords[i];
    				newcoords[dim + i] = coords[coords.length - dim + i];
    			}
    			seq.setArray(newcoords);
    			return true;
		    }
		}
		return false;
	}
	
	/**
	 * Makes sure the ring is turned into a minimal 3 non equal points one
	 * @param ring
	 */
	private void decimateRingFully(LiteCoordinateSequence seq) {
	    double[] coords = seq.getArray();
        int dim = seq.getDimension();
        
        // degenerate one, it's not even a triangle, or just a triangle
        if(seq.size() <= 4)
            return;
        
        double[] newcoords = new double[dim * 4];
        // assuming the ring makes sense in the first place (i.e., it's at least a triangle),
        // we copy the first two and the last two points
        for (int i = 0; i < dim; i++) {
            newcoords[i] = coords[i];
            newcoords[dim + i] = coords[dim + i];
            newcoords[dim * 2 + i] = coords[coords.length - dim * 2 + i];
            newcoords[dim * 3 + i] = coords[coords.length - dim + i];
        }
        seq.setArray(newcoords);
	}

	/**
	 * 1. remove any points that are within the spanx,spany. We ALWAYS keep 1st
	 * and last point 2. transform to screen coordinates 3. remove any points
	 * that are close (span <1)
	 * 
	 * @param seq
	 * @param tranform
	 */
	private final void decimateTransformGeneralize(LiteCoordinateSequence seq,
			MathTransform transform) throws TransformException {
		// decimates before XFORM
		int ncoords = seq.size();
		double coords[] = seq.getXYArray(); // 2*#of points

		if (ncoords < 2) {
			if (ncoords == 1) // 1 coordinate -- just xform it
			{
				// double[] newCoordsXformed2 = new double[2];
			    if(transform != null) {
			        transform.transform(coords, 0, coords, 0, 1);
			        seq.setArray(coords);
			    }
				return;
			} else
				return; // ncoords =0
		}
		
		// if spanx/spany is -1, then no generalization should be done and all
        // coordinates can just be transformed directly
        if (spanx == -1 && spany == -1) {
            // do the xform if needed
            if ((transform != null) && (!transform.isIdentity())) {
                transform.transform(coords, 0, coords, 0, ncoords);
                seq.setArray(coords);
            }
            return;
        }


		int actualCoords = 1;
		double lastX = coords[0];
		double lastY = coords[1];
		for (int t = 1; t < (ncoords - 1); t++) {
			// see if this one should be added
			double x = coords[t * 2];
			double y = coords[t * 2 + 1];
			if ((Math.abs(x - lastX) > spanx) || (Math.abs(y - lastY)) > spany) {
				coords[actualCoords * 2] = x;
				coords[actualCoords * 2 + 1] = y;
				lastX = x;
				lastY = y;
				actualCoords++;
			}
		}
		// always have last one
		coords[actualCoords * 2] = coords[(ncoords - 1) * 2];
		coords[actualCoords * 2 + 1] = coords[(ncoords - 1) * 2 + 1];
		actualCoords++;

		// DO THE XFORM
		if ((transform == null) || (transform.isIdentity())) {
		    // no actual xform
		} else {
		    transform.transform(coords, 0, coords, 0, actualCoords);
		}

		int actualCoordsGen = 1;
		if(!(transform instanceof AffineTransform2D)) {
        		// GENERALIZE again -- we should be in screen space so spanx=spany=1.0
        		for (int t = 1; t < (actualCoords - 1); t++) {
        			// see if this one should be added
        			double x = coords[t * 2];
        			double y = coords[t * 2 + 1];
        			if ((Math.abs(x - lastX) > 0.75) || (Math.abs(y - lastY)) > 0.75) // 0.75
        			// instead of 1 just because it tends to look nicer for slightly
        			// more work. magic number.
        			{
        			    coords[actualCoordsGen * 2] = x;
        			    coords[actualCoordsGen * 2 + 1] = y;
        				lastX = x;
        				lastY = y;
        				actualCoordsGen++;
        			}
        		}
        		// always have last one
        		coords[actualCoordsGen * 2] = coords[(actualCoords - 1) * 2];
        		coords[actualCoordsGen * 2 + 1] = coords[(actualCoords - 1) * 2 + 1];
        		actualCoordsGen++;
		} else {
		    actualCoordsGen = actualCoords;
		}

		// stick back in
		if(actualCoordsGen * 2 < coords.length) {
		    double[] seqDouble = new double[2 * actualCoordsGen];
		    System.arraycopy(coords, 0, seqDouble, 0, actualCoordsGen * 2);
		    seq.setArray(seqDouble);
		} else {
		    seq.setArray(coords);
		}
	}

	private void decimate(Geometry g, LiteCoordinateSequence seq) {
		double[] coords = seq.getXYArray();
		int dim = seq.getDimension();
		int numDoubles = coords.length;
		int readDoubles = 0;
		double prevx, currx, prevy, curry, diffx, diffy;
		for (int currentDoubles = 0; currentDoubles < numDoubles; currentDoubles += dim) {
			if (currentDoubles >= dim && currentDoubles < numDoubles - dim) {
				prevx = coords[readDoubles - dim];
				currx = coords[currentDoubles];
				diffx = Math.abs(prevx - currx);
				prevy = coords[readDoubles - dim + 1];
				curry = coords[currentDoubles + 1];
				diffy = Math.abs(prevy - curry);
				if (diffx > spanx || diffy > spany) {
					readDoubles = copyCoordinate(coords, dim, readDoubles,
							currentDoubles);
				}
			} else {
				readDoubles = copyCoordinate(coords, dim, readDoubles,
						currentDoubles);
			}
		}
		if(g instanceof LinearRing && readDoubles < dim * 4) {
		    decimateRingFully(seq);
		} else {
    		if(readDoubles < numDoubles) {
    		    double[] newCoords = new double[readDoubles];
    		    System.arraycopy(coords, 0, newCoords, 0, readDoubles);
    		    seq.setArray(newCoords);
    		}
		}
	}

	/**
	 * @param coords
	 * @param dimension
	 * @param readDoubles
	 * @param currentDoubles
	 */
	private int copyCoordinate(double[] coords, int dimension, int readDoubles,
			int currentDoubles) {
		for (int i = 0; i < dimension; i++) {
			coords[readDoubles + i] = coords[currentDoubles + i];
		}
		readDoubles += dimension;
		return readDoubles;
	}
}
