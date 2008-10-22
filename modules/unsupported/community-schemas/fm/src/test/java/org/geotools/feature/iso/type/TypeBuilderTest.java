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

package org.geotools.feature.iso.type;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import junit.framework.TestCase;

import org.geotools.feature.iso.TypeBuilder;
import org.geotools.util.GTContainer;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.StructuralDescriptor;
import org.picocontainer.defaults.DefaultPicoContainer;

import com.vividsolutions.jts.geom.Point;

/**
 * This test cases will check that the typeBuilder workds as advertised.
 * <p>
 * This test uses the *TestContainer* and makes no assumptions about
 * implementation. If you wish to subclass this you may reuse the test methods
 * with alternate implementations.
 * </p>
 * 
 * @author Jody
 */
public class TypeBuilderTest extends TestCase {
	/* do you remember gopher? */
	static final String URI = "gopher://localhost/test/";

	DefaultPicoContainer gt;

	private TypeBuilder builder;

	protected void setUp() throws Exception {
		super.setUp();
		gt = GTContainer.normal();
		builder = (TypeBuilder) gt.getComponentInstance(TypeBuilder.class);

	}

	/**
	 * Defines a simple setup of Address, Fullname, Person and then defines a
	 * collection of Person as a Country.
	 * 
	 * <pre><code>
	 *       +--------------------+
	 *       | Address (Complex)  | 
	 *       +--------------------+
	 *       | attention: Fullname|
	 *       | suite: Number      |
	 *       | address: Text      |
	 *       | province: Text     |
	 *       | code: Number       |
	 *       +--------------------+
	 *                |0..2
	 *                |
	 *                |
	 *       +-------------------+       +-------------------+
	 *       | Person (Feature)  |       | Fullname (Complex)|
	 *       +-------------------+      1+-------------------+
	 *       |location: Location |-------|first: Text        |
	 *       |name: Fullname     |       |middle: Text 0:*   |
	 *       |home: Address      |       |last: Text         |
	 *       |work: Address      |       +-------------------+
	 *       |birthdat: Date     |
	 *       +-------------------+
	 *                *|
	 *                 |population:Citizen
	 *                 |
	 *       +----------------------------+
	 *       | Country (FeatureCollection)|
	 *       +----------------------------+
	 *       | curency: Text              |
	 *       +----------------------------+
	 * </code></pre>
	 * 
	 * <p>
	 * Things to note in this example:
	 * <ul>
	 * <li>Definition of "atomic" types like Text and Number that bind directly
	 * to Java classes
	 * <li>Definition of "simple" type like Fullname made strictly from atomic
	 * types
	 * <li>Definition of "complex" type like Address made from mixed content
	 * <li>Definition of a "feature" type that includes information for display
	 * <li>Definition of an "association" type describing the way in which two
	 * types may be related
	 * <li>Definition of a "feature collection" type defining both attributes
	 * and allowable associations with members
	 * </ul>
	 */
	public void testBuilding() {

		builder.setNamespaceURI(URI);
		builder.setName("Text");
		builder.setBinding(String.class);
		AttributeType TEXT = builder.attribute();

		assertNotNull(TEXT);

		AttributeType NUMBER = builder.name("Number").bind(Integer.class)
				.attribute();

		builder.setName("FullName");
		builder.setProperties(new HashSet());
		builder.addAttribute("first", TEXT);
		builder.setMinOccurs(0);
		builder.setMaxOccurs(Integer.MAX_VALUE);
		builder.addAttribute("middle", TEXT);
		builder.addAttribute("last", TEXT);
		ComplexType FULLNAME = builder.complex();

		assertEquals(3, FULLNAME.attributes().size());
		assertEquals("cardinality correct", 0, search(FULLNAME, "middle")
				.getMinOccurs());
		assertEquals("cardinality correct", Integer.MAX_VALUE, search(FULLNAME,
				"middle").getMaxOccurs());
		assertEquals("cardinality clear", 1, search(FULLNAME, "last")
				.getMaxOccurs());

		builder.setName("Address");
		builder.addAttribute("attention", FULLNAME);
		builder.addAttribute("suite", NUMBER);
		builder.addAttribute("address", TEXT);
		builder.addAttribute("province", TEXT);
		builder.addAttribute("code", NUMBER);
		ComplexType ADDRESS = builder.complex();

		assertEquals(5, ADDRESS.attributes().size());

		builder.setName("Date");
		builder.setBinding(Date.class);
		AttributeType DATE = builder.attribute();

		builder.setName("Location");
		builder.setBinding(Point.class);
		GeometryType LOCATION = builder.geometry();

		builder.setName("Person");
		builder.addAttribute("location", LOCATION);
		builder.addAttribute("name", FULLNAME);
		builder.addAttribute("home", ADDRESS);
		builder.addAttribute("work", ADDRESS);
		builder.addAttribute("birthday", DATE);
		builder.setDefaultGeometry("location");
		FeatureType PERSON = builder.feature();

		assertNotNull(PERSON);

		builder.setName("Citizen");
		builder.setReferenceType(PERSON);
		AssociationType CITIZEN = builder.association();

		builder.setName("Country");
		builder.addAttribute("curency", TEXT);
		builder.addMemberType("population", CITIZEN);
		FeatureCollectionType COUNTRY = builder.collection();

		assertNotNull(COUNTRY);
		assertEquals(1, COUNTRY.getMembers().size());
		assertEquals(1, COUNTRY.getProperties().size());
		assertEquals(1, COUNTRY.attributes().size());
		assertEquals(0, COUNTRY.associations().size());
	}

	/**
	 * Search for "first" descriptor matching name, include super class in
	 * search.
	 * 
	 * @param type
	 * @param name
	 * @return PropertyDescriptor (or null).
	 */
	StructuralDescriptor search(ComplexType type, String name) {
		while (type != null) {
			for (Iterator i = type.getProperties().iterator(); i.hasNext();) {
				StructuralDescriptor property = (StructuralDescriptor) i.next();
				if (property.getName().toString().endsWith(name)) {
					return property;
				}
			}
			type = (ComplexType) type.getSuper();
		}
		return null;
	}

	/**
	 * See how well we do when building things "on one line".
	 */
	public void testTerseBuilding() {
		// Some initial set up
		builder.setNamespaceURI(URI);

		AttributeType TEXT = builder.name("Text").bind(String.class)
				.attribute();
		assertNotNull(TEXT);

		AttributeType NUMBER = builder.name("Number").bind(Integer.class)
				.attribute();
		ComplexType FULLNAME = builder.name("FullName")
				.attribute("first", TEXT).cardinality(0, Integer.MAX_VALUE)
				.attribute("middle", TEXT).attribute("last", TEXT).complex();

		assertEquals(3, FULLNAME.attributes().size());
		assertEquals("cardinality correct", 0, search(FULLNAME, "middle")
				.getMinOccurs());
		assertEquals("cardinality correct", Integer.MAX_VALUE, search(FULLNAME,
				"middle").getMaxOccurs());
		assertEquals("cardinality clear", 1, search(FULLNAME, "last")
				.getMaxOccurs());

		ComplexType ADDRESS = builder.name("Address").attribute("attention",
				FULLNAME).attribute("suite", NUMBER).attribute("address", TEXT)
				.attribute("province", TEXT).attribute("code", NUMBER)
				.complex();

		assertEquals(5, ADDRESS.attributes().size());

		AttributeType DATE = builder.name("Date").bind(Date.class).attribute();
		GeometryType LOCATION = builder.name("Location").bind(Point.class)
				.geometry();

		FeatureType PERSON = builder.name("Person").attribute("location",
				LOCATION).attribute("name", FULLNAME)
				.attribute("home", ADDRESS).attribute("work", ADDRESS)
				.attribute("birthday", DATE).defaultGeometry("location")
				.feature();
		assertNotNull(PERSON);

		AssociationType CITIZEN = builder.name("Citizen").referenceType(PERSON)
				.association();

		FeatureCollectionType COUNTRY = builder.name("Country").attribute(
				"curency", TEXT).member("population", CITIZEN).collection();

		assertNotNull(COUNTRY);
		assertEquals(1, COUNTRY.getMembers().size());
		assertEquals(1, COUNTRY.getProperties().size());
		assertEquals(1, COUNTRY.attributes().size());
		assertEquals(0, COUNTRY.associations().size());
	}
}