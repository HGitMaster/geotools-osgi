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

package org.geotools.feature.iso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import junit.framework.TestCase;

import org.geotools.util.GTContainer;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.PropertyDescriptor;
import org.picocontainer.defaults.DefaultPicoContainer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * This test cases is set up to exercise the "Data" system.
 * <p>
 * We will be making use of the same abstractions defined during the
 * TypeBuilderTest (so if you get an error during setup you can debug over there
 * first).
 * </p>
 * To review here is the type model we will be using during these tests: /**
 * Defines a simple setup of Address, Fullname, Person and then defines a
 * collection of Person as a Country.
 * 
 * <pre><code>
 *            +--------------------+
 *            | Address (Complex)  | 
 *            +--------------------+
 *            | attention: Fullname|
 *            | suite: Number      |
 *            | street: Text       |
 *            | city: Text         |
 *            | province: Text     |
 *            | code: Number       |
 *            +--------------------+
 *                     |0..2
 *                     |
 *                     |
 *            +-------------------+       +-------------------+
 *            | Person (Feature)  |       | Fullname (Complex)|
 *            +-------------------+      1+-------------------+
 *            |location: Location |-------|first: Text        |
 *            |name: Fullname     |       |last: Text         |
 *            |home: Address      |       +-------------------+
 *            |work: Address      |
 *            |birthdat: Date     |
 *            +-------------------+
 *                     *|
 *                      |population:Citizen
 *                      |
 *            +----------------------------+
 *            | Country (FeatureCollection)|
 *            +----------------------------+
 *            | currency: Text             |
 *            +----------------------------+
 * </code></pre>
 * 
 * </p>
 * <p>
 * 
 * @author Jody
 */
public class DataSystemTest extends TestCase {
	static final String URI = "gopher://localhost/test/";

	private DefaultPicoContainer gt;

	private TypeBuilder builder;

	private FeatureFactory factory;

	private AttributeType TEXT;

	private AttributeType NUMBER;

	private ComplexType FULLNAME;

	private ComplexType ADDRESS;

	private AttributeType DATE;

	private GeometryType LOCATION;

	private FeatureType PERSON;

	private AssociationType CITIZEN;

	private FeatureCollectionType COUNTRY;

	protected void setUp() throws Exception {
		super.setUp();
		//
		// Container
		// 
		gt = GTContainer.normal(); // ah so easy!
		//
		// Type Fixture
		//
		builder = (TypeBuilder) gt.getComponentInstance(TypeBuilder.class);
		builder.setNamespaceURI(URI);
		builder.setName("Text");
		builder.setBinding(String.class);

		TEXT = builder.attribute();
		NUMBER = builder.name("Number").bind(Integer.class).attribute();

		FULLNAME = builder.name("Fullname").attribute("first", TEXT).attribute("last", TEXT)
				.complex();

		builder.setName("Address");
		builder.attribute("attention", FULLNAME);
		builder.attribute("suite", NUMBER);
		builder.attribute("street", TEXT);
		builder.attribute("city", TEXT);
		builder.attribute("province", TEXT);
		builder.attribute("code", NUMBER);
		ADDRESS = builder.complex();

		DATE = builder.name("Date").bind(Date.class).attribute();
		LOCATION = builder.name("Location").bind(Point.class).geometry();

		builder.setName("Person");
		builder.attribute("location", LOCATION);
		builder.attribute("name", FULLNAME);
		builder.attribute("home", ADDRESS);
		builder.attribute("work", ADDRESS);
		builder.attribute("birthday", DATE);
		builder.setDefaultGeometry("location");
		PERSON = builder.feature();

		CITIZEN = builder.name("Citizen").referenceType(PERSON).association();

		COUNTRY = builder.name("Country").attribute("currency", TEXT).member(
				"population", CITIZEN).collection();
		//
		// Factory
		//
		factory = (FeatureFactory) gt
				.getComponentInstanceOfType(FeatureFactory.class);
	}

	/**
	 * Simple lookup method, the kinda thing that should belong in Types utility
	 * class
	 * 
	 * @param type
	 *            complex type from which we want a descriptor
	 * @param name
	 *            text to match against local part of descriptor name
	 * @return Descriptor with getName().getLocalPart() equals name
	 */
	PropertyDescriptor association(ComplexType type, String name) {
		for (Iterator i = type.getProperties().iterator(); i.hasNext();) {
			PropertyDescriptor descriptor = (PropertyDescriptor) i.next();
			if (name.equalsIgnoreCase(descriptor.getName().getLocalPart())) {
				return descriptor;
			}
		}
		return null;
	}

	AttributeDescriptor attribute(ComplexType type, String name) {
		for (Iterator i = type.attributes().iterator(); i.hasNext();) {
			AttributeDescriptor descriptor = (AttributeDescriptor) i.next();
			if (name.equalsIgnoreCase(descriptor.getName().getLocalPart())) {
				return descriptor;
			}
		}
		return null;
	}

	public void testAttribute() {
		Attribute first = factory.createAttribute("Jody", attribute(FULLNAME,
				"first"), null);

		assertNotNull(first);
		assertEquals(attribute(FULLNAME, "first"), first.getDescriptor());
		assertEquals(TEXT, first.getType());

		assertEquals("Jody", first.getValue());

	}

	Collection properties(Property property) {
		Collection properties = new ArrayList();
		properties.add(property);
		return properties;
	}

	Collection properties(Property property1, Property property2) {
		Collection properties = new ArrayList();
		properties.add(property1);
		properties.add(property2);
		return properties;
	}

	Collection properties(Property property1, Property property2,
			Property property3) {
		Collection properties = new ArrayList();
		properties.add(property1);
		properties.add(property2);
		properties.add(property3);
		return properties;
	}

	Collection properties(Property[] array) {
		Collection properties = new ArrayList();
		for (int i = 0; i < array.length; i++) {
			properties.add(array[i]);
		}
		return properties;
	}

	/** Tests using ComplexType */
	public void testSimpleComplexAttribute() {
		Attribute first = factory.createAttribute("Jody", attribute(FULLNAME,
				"first"), null);
		Attribute last = factory.createAttribute("Garnett", attribute(FULLNAME,
				"last"), null);

		ComplexAttribute fullname = factory.createComplexAttribute(properties(
				first, last), FULLNAME, null);

		assertNotNull(fullname);
		Collection contents = (Collection) fullname.getValue();
		assertTrue(contents.contains(first));
		assertTrue(contents.contains(last));
	}

	/** Creates using FULLNAME using ADDRESS descriptor "attention" */
	ComplexAttribute createFullname(ComplexType context, String entry,
			String aName) {
		int split = aName.indexOf(" ");
		String first = aName.substring(0, split);
		String last = aName.substring(split + 1);

		Attribute firstName = factory.createAttribute(first, attribute(
				FULLNAME, "first"), null);
		Attribute lastName = factory.createAttribute(last, attribute(FULLNAME,
				"last"), null);
		return factory.createComplexAttribute(properties(firstName, lastName),
				attribute(context, entry), null);
	}

	/** Tests ADDRESS using ComplexType directly */
	public void testCompountComplexAttribute() {
		ComplexAttribute attention = createFullname(ADDRESS, "attention",
				"Jody Garnett");
		Attribute suite = factory.createAttribute(new Integer(3), attribute(
				ADDRESS, "suite"), null);
		Attribute street = factory.createAttribute("645 Battery", attribute(
				ADDRESS, "street"), null);
		Attribute city = factory.createAttribute("Victora", attribute(ADDRESS,
				"city"), null);
		Attribute province = factory.createAttribute("British Columbia",
				attribute(ADDRESS, "province"), null);
		Attribute code = factory.createAttribute(new Integer(1234), attribute(
				ADDRESS, "code"), null);
		ComplexAttribute address = factory.createComplexAttribute(
				properties(new Property[] { attention, suite, street, city,
						province, code }), ADDRESS, null);

		assertNotNull(address);
		Collection contents = (Collection) address.getValue();
		assertTrue(contents.contains(city));
	}

	/** Creates using PERSON and either "work" or "home" descriptor */
	ComplexAttribute createPersonAddress(String location) {
		ComplexAttribute attention = createFullname(ADDRESS, "attention",
				"Jody Garnett");
		Attribute suite = factory.createAttribute(new Integer(3), attribute(
				ADDRESS, "suite"), null);
		Attribute street = factory.createAttribute("645 Battery", attribute(
				ADDRESS, "street"), null);
		Attribute city = factory.createAttribute("Victora", attribute(ADDRESS,
				"city"), null);
		Attribute province = factory.createAttribute("British Columbia",
				attribute(ADDRESS, "province"), null);
		Attribute code = factory.createAttribute(new Integer(1234), attribute(
				ADDRESS, "code"), null);
		return factory.createComplexAttribute(properties(new Property[] {
				attention, suite, street, city, province, code }), attribute(
				PERSON, location), null);
	}

	public void testGeometryAttribute() {
		GeometryFactory gf = (GeometryFactory) gt
				.getComponentInstance(GeometryFactory.class);

		// NB: we can only create geometry attirbute as part of a feature!
		GeometryAttribute location = factory.createGeometryAttribute(gf
				.createPoint(new Coordinate(0, 0)), attribute(PERSON,
				"location"), null, null);

		assertNotNull(location);
		assertEquals(0, ((Point) location.getValue()).getX(), 0);
	}

	public GeometryAttribute createLocation() {
		GeometryFactory gf = (GeometryFactory) gt
				.getComponentInstance(GeometryFactory.class);
		return factory.createGeometryAttribute(gf.createPoint(new Coordinate(0,
				0)), attribute(PERSON, "location"), null, null);
	}

	public void testFeature() {
		ComplexAttribute home = createPersonAddress("home");
		ComplexAttribute work = createPersonAddress("work");
		GeometryAttribute location = createLocation();
		ComplexAttribute name = createFullname(PERSON, "name", "Jody Garnett");
		Attribute date = factory.createAttribute(new Date(), attribute(PERSON,
				"birthday"), null);
		Feature person = factory.createFeature(properties(new Property[] {
				location, name, home, work, date }), PERSON, null);

		assertNotNull(person);
		Collection contents = (Collection) person.getValue();
		assertTrue(contents.contains(name));
	}

	Feature createPerson(String aName, String fid) {
		ComplexAttribute home = createPersonAddress("home");
		ComplexAttribute work = createPersonAddress("work");
		GeometryAttribute location = createLocation();
		ComplexAttribute name = createFullname(PERSON, "name", aName);
		Attribute date = factory.createAttribute(new Date(), attribute(PERSON,
				"birthday"), null);
		return factory.createFeature(properties(new Property[] { location,
				name, home, work, date }), PERSON, fid);
	}

	public void testFeatureCollection() {
		assertNotNull(COUNTRY);
		Attribute currency = factory.createAttribute("dollars", attribute(
				COUNTRY, "currency"), null);

        //TODO: explicit cast as FeatureCollection lost its ability to report content size
		FeatureCollectionImpl canada = (FeatureCollectionImpl) factory.createFeatureCollection(
				properties(currency), COUNTRY, null);
		Feature justin = createPerson("Justin DeOlivera","#2");
		Feature jody = createPerson("Jody Garnett","#1");
		
		canada.add(jody);
		canada.add(justin);
		
		assertEquals( 2, canada.size() );
	}
}