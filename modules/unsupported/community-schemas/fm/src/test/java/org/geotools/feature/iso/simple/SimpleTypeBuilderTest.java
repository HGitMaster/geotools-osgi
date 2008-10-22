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

package org.geotools.feature.iso.simple;

import java.util.List;

import junit.framework.TestCase;

import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.GTContainer;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureType;
import org.picocontainer.defaults.DefaultPicoContainer;

import com.vividsolutions.jts.geom.LineString;

/**
 * This test cases will check that the typeBuilder works as advertised.
 * <p>
 * This test uses the container set up for simple types implementation. If you
 * wish to subclass this you may reuse the test methods with alternate
 * implementations.
 * </p>
 * 
 * @author Jody
 */
public class SimpleTypeBuilderTest extends TestCase {

    /* do you remember gopher? */
    static final String URI = "gopher://localhost/test/";

    DefaultPicoContainer gt;

    private SimpleTypeBuilder builder;

    protected void setUp() throws Exception {
        super.setUp();
        gt = GTContainer.simple();
        builder = (SimpleTypeBuilder) gt
                .getComponentInstance(SimpleTypeBuilder.class);
    }

    /**
     * Defines a simple setup of Address, Fullname, Person and then defines a
     * collection of Person as a Country.
     * 
     * <pre><code>
     *      +-------------------+
     *      | ROAD (Feature)    |
     *      +-------------------+
     *      |name: Text         |
     *      |route: Route       |
     *      +-------------------+
     *               *|
     *                |members
     *                |
     *      +--------------------------+
     *      | ROADS(FeatureCollection) |
     *      +--------------------------+
     * </code></pre>
     * 
     * <p>
     * Things to note in this example:
     * <ul>
     * <li>Definition of "atomic" types like Text and Number that bind directly
     * to Java classes
     * <li>Definition of "geometry" types like Route that bind to a geometry
     * implementation
     * <li>Definition of a "simple feature" made of atomic and geometry types
     * without support for descriptors or associations
     * <li>Definition of a "simple feature collection" able to hold a
     * collection of simple feature, but unable to hold attributes itself.
     * </ul>
     */
    public void testBuilding() throws Exception {
        builder.load(new SimpleSchema()); // load java types
        builder.setNamespaceURI(URI);
        builder.setCRS(DefaultGeographicCRS.WGS84);

        builder.setName("ROAD");
        builder.addAttribute("name", String.class);
        builder.addGeometry("route", LineString.class);
        SimpleFeatureType ROAD = builder.feature();

        assertEquals(2, ROAD.getAttributeCount());
        assertEquals(LineString.class, ROAD.getDefaultGeometryType().getBinding());
        assertTrue(List.class.isInstance(ROAD.attributes()));

        builder.setName("ROADS");
        builder.setMember(ROAD);

        SimpleFeatureCollectionType ROADS = builder.collection();
        assertEquals(0, ROADS.attributes().size());
        assertEquals(ROAD, ROADS.getMemberType());
    }

    public void testTerse() throws Exception {
        builder.load(new SimpleSchema()); // load java types
        builder.setNamespaceURI(URI);
        builder.setCRS(DefaultGeographicCRS.WGS84);

        SimpleFeatureType ROAD = builder.name("ROAD").attribute("name",
                String.class).geometry("route", LineString.class).feature();

        assertEquals(2, ROAD.getAttributeCount());
        assertEquals(LineString.class, ROAD.getDefaultGeometryType().getBinding());
        assertTrue(List.class.isInstance(ROAD.attributes()));
        assertEquals(DefaultGeographicCRS.WGS84, ROAD.getCRS());

        SimpleFeatureCollectionType ROADS = builder.name("ROADS").member(ROAD)
                .collection();

        assertEquals(0, ROADS.attributes().size());
        assertEquals(ROAD, ROADS.getMemberType());
        assertEquals(DefaultGeographicCRS.WGS84, ROADS.getCRS());
    }

}