/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2011, Open Source Geospatial Foundation (OSGeo)
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

import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.DirectPosition;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the JTS utility class.
 * 
 * @author Michael Bedward
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/library/api/src/test/java/org/geotools/geometry/jts/JTSTest.java $
 * @version $Id: JTSTest.java 37551 2011-07-03 10:03:10Z aaime $
 * @since 2.8
 */
public class JTSTest extends JTSTestBase {
    
    @Test
    public void toGeometry_Shape_Poly() {
        Shape shape = new Polygon(XPOINTS, YPOINTS, NPOINTS);
        Geometry geom = JTS.toGeometry(shape);
        assertTrue(geom instanceof LinearRing);

        Coordinate[] coords = geom.getCoordinates();
        assertEquals(NPOINTS + 1, coords.length);

        CoordList list = new CoordList(coords);
        Coordinate c = new Coordinate();
        for (int i = 0; i < NPOINTS; i++) {
            c.x = XPOINTS[i];
            c.y = YPOINTS[i];
            assertTrue(list.contains(c));
        }
    }

    @Test
    public void toGeometry_Shape_Line() {
        GeneralPath path = new GeneralPath();
        
        path.moveTo(XPOINTS[0], YPOINTS[0]);
        for (int i = 1; i < NPOINTS; i++) {
            path.lineTo(XPOINTS[i], YPOINTS[i]);
        }
                
        Geometry geom = JTS.toGeometry(path);
        assertTrue(geom instanceof LineString);

        Coordinate[] coords = geom.getCoordinates();
        assertEquals(NPOINTS, coords.length);

        CoordList list = new CoordList(coords);
        Coordinate c = new Coordinate();
        for (int i = 0; i < NPOINTS; i++) {
            c.x = XPOINTS[i];
            c.y = YPOINTS[i];
            assertTrue(list.contains(c));
        }
    }

    @Test
    public void getEnvelope2D() {
        ReferencedEnvelope refEnv = new ReferencedEnvelope(
                -10, 10, -5, 5, DefaultGeographicCRS.WGS84);
        
        Envelope2D env2D = JTS.getEnvelope2D(refEnv, refEnv.getCoordinateReferenceSystem());
        
        CRS.equalsIgnoreMetadata(
                refEnv.getCoordinateReferenceSystem(),
                env2D.getCoordinateReferenceSystem());
        
        assertTrue(env2D.boundsEquals(refEnv, 0, 1, TOL));
    }

    @Test
    public void toGeometry_Envelope() {
        Envelope refEnv = new Envelope(-10, 10, -5, 5);
        Geometry geom = JTS.toGeometry(refEnv);
        assertTrue(geom instanceof com.vividsolutions.jts.geom.Polygon);
        
        Envelope geomEnv = geom.getEnvelopeInternal();
        assertEquals(-10.0, geomEnv.getMinX(), TOL);
        assertEquals(10.0, geomEnv.getMaxX(), TOL);
        assertEquals(-5.0, geomEnv.getMinY(), TOL);
        assertEquals(5.0, geomEnv.getMaxY(), TOL);
    }
    
    @Test
    public void toGeometry_ReferencedEnvelope() {
        ReferencedEnvelope refEnv = new ReferencedEnvelope(-10, 10, -5, 5, DefaultGeographicCRS.WGS84);
        Geometry geom = JTS.toGeometry(refEnv);
        assertTrue(geom instanceof com.vividsolutions.jts.geom.Polygon);
        
        Envelope geomEnv = geom.getEnvelopeInternal();
        assertEquals(-10.0, geomEnv.getMinX(), TOL);
        assertEquals(10.0, geomEnv.getMaxX(), TOL);
        assertEquals(-5.0, geomEnv.getMinY(), TOL);
        assertEquals(5.0, geomEnv.getMaxY(), TOL);
    }


    @Test
    public void toEnvelope() {
        Coordinate[] coords = getPolyCoords();
        GeometryFactory gf = new GeometryFactory();
        Geometry geom = gf.createPolygon(gf.createLinearRing(coords), null);
        
        ReferencedEnvelope refEnv = JTS.toEnvelope(geom);
        assertTrue(geom.getEnvelopeInternal().equals(refEnv));
    }
    
    @Test
    public void toDirectPosition() {
        Coordinate c = new Coordinate(40,40);
        DirectPosition wrapper = JTS.toDirectPosition(c, DefaultGeographicCRS.WGS84 );
        
        GeneralDirectPosition expected = new GeneralDirectPosition( DefaultGeographicCRS.WGS84);
        expected.setOrdinate(0,40);
        expected.setOrdinate(1,40);
        
        assertEquals( expected, wrapper );
    }
    @Test
    public void toGeometry_BoundingBox() {
        BoundingBox bbox = new ReferencedEnvelope(-10, 10, -5, 5, null);
        Geometry geom = JTS.toGeometry(bbox);
        assertTrue(geom instanceof com.vividsolutions.jts.geom.Polygon);
        
        Envelope geomEnv = geom.getEnvelopeInternal();
        assertEquals(-10.0, geomEnv.getMinX(), TOL);
        assertEquals(10.0, geomEnv.getMaxX(), TOL);
        assertEquals(-5.0, geomEnv.getMinY(), TOL);
        assertEquals(5.0, geomEnv.getMaxY(), TOL);
    }
 
}
