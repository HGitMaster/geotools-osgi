package org.geotools.geometry.jts;

import static org.junit.Assert.*;

import org.geotools.factory.CommonFactoryFinder;
import org.junit.Test;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.io.WKTReader;

public class GeometryCollectorTest {

    FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

    @Test
    public void testCollectNull() {
        GeometryCollector collector = new GeometryCollector();
        collector.add(null);
        GeometryCollection result = collector.collect();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCollectNone() {
        GeometryCollector collector = new GeometryCollector();
        GeometryCollection result = collector.collect();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testTwo() throws Exception {
        WKTReader reader = new WKTReader();

        GeometryCollector collector = new GeometryCollector();
        collector.setFactory(null);
        final Geometry p0 = reader.read("POINT(0 0)");
        collector.add(p0);
        final Geometry p1 = reader.read("POINT(1 1)");
        collector.add(p1);

        GeometryCollection result = collector.collect();
        assertEquals(2, result.getNumGeometries());
        assertSame(p0, result.getGeometryN(0));
        assertSame(p1, result.getGeometryN(1));
    }

    @Test
    public void testTooMany() throws Exception {
        WKTReader reader = new WKTReader();

        GeometryCollector collector = new GeometryCollector();
        collector.setMaxCoordinates(1);
        final Geometry p0 = reader.read("POINT(0 0)");
        collector.add(p0);
        final Geometry p1 = reader.read("POINT(1 1)");
        try {
            collector.add(p1);
            fail("Should have complained about too many coordinates");
        } catch (IllegalStateException e) {
            // fine
        }
    }

}
