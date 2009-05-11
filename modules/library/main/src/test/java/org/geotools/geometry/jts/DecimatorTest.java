package org.geotools.geometry.jts;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import junit.framework.TestCase;

import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;

public class DecimatorTest extends TestCase {

    GeometryFactory gf = new GeometryFactory();
    LiteCoordinateSequenceFactory csf = new LiteCoordinateSequenceFactory();

    /**
     * http://jira.codehaus.org/browse/GEOT-1923
     */
    public void testDecimateRing() {
        // a long rectangle made of 5 coordinates
        LinearRing g = gf.createLinearRing(csf.create(new double[] {0,0,0,10,2,10,2,0,0,0}));
        assertTrue(g.isValid());
        
        Decimator d = new Decimator(4, 4);
        d.decimate(g);
        g.geometryChanged();
        assertTrue(g.isValid());
        assertEquals(4, g.getCoordinateSequence().size());
    }
    
    /**
     * http://jira.codehaus.org/browse/GEOT-1923
     */
    public void testDecimateRingEnvelope() {
        // acute triangle
        LinearRing g = gf.createLinearRing(csf.create(new double[] {0,0,0,10,2,10,2,0,0,0}));
        assertTrue(g.isValid());
        
        Decimator d = new Decimator(20, 20);
        d.decimate(g);
        g.geometryChanged();
        assertTrue(g.isValid());
        assertEquals(4, g.getCoordinateSequence().size());
    }
    
    public void testNoDecimation() {
        // acute triangle
        LinearRing g = gf.createLinearRing(csf.create(new double[] {0,0,0,10,2,10,2,0,0,0}));
        LinearRing original = (LinearRing) g.clone();
        assertTrue(g.isValid());
        
        Decimator d = new Decimator(-1, -1);
        d.decimate(g);
        g.geometryChanged();
        assertTrue(g.isValid());
        assertTrue(original.equals(g));
    }
    
    public void testDistance() throws Exception {
        LineString ls = gf.createLineString(csf.create(new double[] {0,0,1,1,2,2,3,3,4,4,5,5}));
        
        MathTransform identity = new AffineTransform2D(new AffineTransform());
        
        Decimator d = new Decimator(identity, new Rectangle(0,0,5,5), 0.8);
        d.decimateTransformGeneralize((Geometry) ls.clone(), identity);
        assertEquals(6, ls.getNumPoints());
        
        d = new Decimator(identity, new Rectangle(0,0,5,5), 1);
        d.decimateTransformGeneralize(ls, identity);
        assertEquals(4, ls.getNumPoints());
        
        d = new Decimator(identity, new Rectangle(0,0,5,5), 6);
        d.decimateTransformGeneralize(ls, identity);
        assertEquals(2, ls.getNumPoints());
    }
}
