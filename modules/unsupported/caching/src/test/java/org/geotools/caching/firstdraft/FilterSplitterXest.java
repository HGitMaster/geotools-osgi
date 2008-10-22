/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.caching.firstdraft;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.ArrayList;
import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.geotools.caching.firstdraft.util.FilterSplitter;
import org.geotools.filter.FilterFactoryImpl;


public class FilterSplitterXest extends TestCase {
    protected FilterFactory ff;
    protected Filter bb;
    protected Filter bb2;
    protected Filter att;
    protected Filter bso;
    protected Envelope bbenv;
    protected Envelope bb2env;
    protected Envelope bsoenv;

    protected void setUp() {
        ff = new FilterFactoryImpl();
        bb = ff.bbox("geom", 0, 10, 1000, 1100, "srs");
        bbenv = new Envelope(0, 1000, 10, 1100);
        bb2 = ff.bbox("geom", 500, 510, 1500, 1600, "srs");
        bb2env = new Envelope(500, 1500, 510, 1600);
        att = ff.like(ff.property("dummydata"), "Id: 1*");

        Polygon p = createPolygon();
        bso = ((FilterFactoryImpl) ff).intersects(ff.property("geom"), ff.literal(p));
        bsoenv = p.getEnvelopeInternal();
    }

    protected static Polygon createPolygon() {
        Coordinate[] coords = new Coordinate[] {
                new Coordinate(200, 210), new Coordinate(200, 500), new Coordinate(2000, 2100),
                new Coordinate(500, 510), new Coordinate(200, 210)
            };
        CoordinateArraySequence seq = new CoordinateArraySequence(coords);
        LinearRing ls = new LinearRing(seq, new GeometryFactory());
        Polygon ret = new Polygon(ls, null, new GeometryFactory());

        return ret;
    }

    public static Test suite() {
        return new TestSuite(FilterSplitterXest.class);
    }

    public void testBBoxFilter() {
        FilterSplitter splitter = new FilterSplitter();
        bb.accept(splitter, null);
        assertEquals(bbenv, splitter.getEnvelope());

        Filter result = splitter.getSpatialRestriction();
        assertEquals(bb, result);
    }

    public void testAndFilter() {
        FilterSplitter splitter = new FilterSplitter();
        Filter and = ff.and(bb, att);
        and.accept(splitter, null);
        assertEquals(bbenv, splitter.getEnvelope());
        assertEquals(bb, splitter.getSpatialRestriction());
        assertEquals(att, splitter.getOtherRestriction());
        and = ff.and(bb, bb2);
        splitter = new FilterSplitter();
        and.accept(splitter, null);

        Envelope i = bbenv.intersection(bb2env);
        Envelope s = splitter.getEnvelope();
        assertEquals(i, s);
        assertEquals(Filter.INCLUDE, splitter.getOtherRestriction());
    }

    public void testOrFilter() {
        FilterSplitter splitter = new FilterSplitter();
        Filter or = ff.or(bb, att);
        or.accept(splitter, null);
        assertNull(splitter.getEnvelope());
        assertEquals(Filter.INCLUDE, splitter.getSpatialRestriction());
        assertEquals(or, splitter.getOtherRestriction());
        or = ff.or(bb, bb2);
        splitter = new FilterSplitter();
        or.accept(splitter, null);
        bbenv.expandToInclude(bb2env);
        assertEquals(bbenv, splitter.getEnvelope());
        assertEquals(or, splitter.getOtherRestriction());
    }

    public void testNotFilter() {
        FilterSplitter splitter = new FilterSplitter();
        Filter not = ff.not(bb);
        not.accept(splitter, null);
        assertNull(splitter.getEnvelope());
    }

    public void testBinarySpatialOperator() {
        FilterSplitter splitter = new FilterSplitter();
        Filter and = ff.and(bb, bso);
        and.accept(splitter, null);
        assertEquals(bso, splitter.getOtherRestriction());
        assertEquals(bbenv.intersection(bsoenv), splitter.getEnvelope());
    }

    public void testAndManyFilters() {
        FilterSplitter splitter = new FilterSplitter();
        List filters = new ArrayList();
        filters.add(bb);
        filters.add(bb2);
        filters.add(att);
        filters.add(bso);

        Filter and = ff.and(filters);
        and.accept(splitter, null);
        assertEquals(bbenv.intersection(bb2env), splitter.getEnvelope());
        assertEquals(ff.and(att, bso), splitter.getOtherRestriction());
    }

    public void testOrManyFilters() {
        FilterSplitter splitter = new FilterSplitter();
        List filters = new ArrayList();
        filters.add(bb);
        filters.add(bb2);
        filters.add(att);
        filters.add(bso);

        Filter or = ff.or(filters);
        or.accept(splitter, null);

        Envelope e = new Envelope(bbenv);
        e.expandToInclude(bb2env);
        e.expandToInclude(bsoenv);
        assertEquals(e, splitter.getEnvelope());
        assertEquals(or, splitter.getOtherRestriction());
    }

    public void testOrAndAndManyFilters() {
        // TODO write test
    }
}
