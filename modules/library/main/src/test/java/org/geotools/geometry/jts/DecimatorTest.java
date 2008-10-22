package org.geotools.geometry.jts;

import junit.framework.TestCase;

import com.vividsolutions.jts.geom.GeometryFactory;
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
}
