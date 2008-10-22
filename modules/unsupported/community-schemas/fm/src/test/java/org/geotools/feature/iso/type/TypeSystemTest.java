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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.geotools.feature.iso.Types;
import org.geotools.util.GTContainer;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.Namespace;
import org.opengis.feature.type.Schema;
import org.opengis.feature.type.TypeFactory;
import org.picocontainer.defaults.DefaultPicoContainer;

import com.vividsolutions.jts.geom.Point;

/**
 * This test cases will check that the core type system is opperating according
 * to specification.
 * <p>
 * This test uses the *TestContainer* and makes no assumptions about
 * implementation. If you wish to subclass this you may reuse the test methods
 * with alternate implementations.
 * </p>
 * <p>
 * The following are tested:
 * <ul>
 * <li>TypeFactory system
 * <li>Schema
 * <li>Naming
 * </ul>
 * 
 * @author Jody
 */
public class TypeSystemTest extends TestCase {
    /* do you remember gopher? */
    static final String URI = "gopher://localhost/test/";

    DefaultPicoContainer gt;

    protected void setUp() throws Exception {
        super.setUp();
        gt = GTContainer.normal();
    }

    /**
     * We are running our own ISO 19103 work alike, lets confirm it works.
     * 
     * @throws Exception
     */
    public void testNaming() throws Exception {
        Name n1 = name("gopher://localhost/test/", "this");
        Name n2 = name("gopher://localhost/test/", "that");

        assertFalse("do names differ", n1.equals(n2));
        assertFalse("name equals handle null", n1.equals(null));
        assertTrue("name equals", n1.equals(n1));

        assertEquals(name("gopher://localhost/", "test/this"), n1);

        Namespace scope = space("gopher://localhost/test/");
        Name n4 = name(scope, "this");
        assertEquals("does creating name based on scope work", n1, n4);
        assertTrue("has n4 been added to scope", scope.contains(n4));
        assertSame("can we find based on local", n4, scope.lookup("this"));

        // test namespace handling of equals
        assertEquals(n1, scope.lookup("this"));
        assertTrue(scope.contains(n1));

        // test namespace handing of URI
        Name n5 = name("gopher://localhost/", "test/this");
        assertEquals(n5, scope.lookup("this"));
        assertTrue(scope.contains(n5));

        // serious test namespace handling of URI
        Name n6 = name("gopher://localhost/", "test/that");
        scope.add(n6); // do you check uri on add justin?
        assertTrue(scope.contains(n6));
        assertTrue(scope.contains(n2));

        assertNull(scope.lookup("foo"));
        assertNull(scope.lookup("test/that"));
        assertNotNull(scope.lookup("that"));

        assertEquals(n6, scope.lookup("that")); // do you need to?
        assertSame(n6, scope.lookup("that")); // this says n6 was added last
        assertEquals(n2, scope.lookup("that")); // do you need to?
    }

    /**
     * Types in a Map? keySet as Namespace.
     * <p>
     * Looks like we should make add( Type ) just make us happy, can always
     * reject types that do not agree with the schema uri.
     * </p>
     */
    public void testSchema() {
        TypeFactory f = (TypeFactory) gt
                .getComponentInstanceOfType(TypeFactory.class);

        AttributeType TEXT = f.createAttributeType(typeName(URI, "Text"),
                String.class, false, false, Collections.EMPTY_SET, null, null);
        AttributeType NUMBER = f.createAttributeType(typeName(URI, "Number"),
                Integer.class, false, false, Collections.EMPTY_SET, null, null);

        Schema schema = new org.geotools.feature.iso.type.SchemaImpl(URI);
        try {
            schema.put(TEXT.getName().getURI(), TEXT); // this is wrong!
            fail("read the javadocs we map typename to type");
        } catch (IllegalArgumentException expected) {
        }
        schema.put(TEXT.getName(), TEXT);
        schema.put(NUMBER.getName(), NUMBER);

        Namespace space = schema.namespace();
        assertEquals(schema.keySet(), space);
        assertSame(schema.keySet(), space);

        assertTrue("Text in namespace", space.contains(TEXT.getName()));
        assertEquals("Text lookup", TEXT.getName(), space.lookup("Text"));

        assertTrue(space.contains(NUMBER.getName()));
        assertEquals(NUMBER.getName(), space.lookup("Number"));

        Name name = typeName(URI, "Text");
        assertTrue(space.contains(name));
        assertEquals(TEXT, schema.get(name));

        AttributeType EVIL = f.createAttributeType(
                typeName("/dev/null", "Evil"), Void.class, false, false,
                Collections.EMPTY_SET, null, null);
        try {
            schema.put(EVIL.getName(), EVIL);
            fail("Should not be able to put EVIL due to namespace conflict with schema");
        } catch (Exception good) {
            // good triumphs over evil!
        }
    }

    /**
     * In the interests of speed I am making one long test case
     */
    public void testTypeFactory() {
        TypeFactory f = (TypeFactory) gt
                .getComponentInstanceOfType(TypeFactory.class);

        assertNotNull(f);
        AttributeType TEXT = f.createAttributeType(typeName(URI, "Text"),
                String.class, false, false, Collections.EMPTY_SET, null, null);
        assertNotNull(TEXT);
        assertSame("binding", String.class, TEXT.getBinding());
        assertEquals("typeName", "Text", TEXT.getName().getLocalPart());
        assertEquals("No restrictions", 0, TEXT.getRestrictions().size());

        AttributeType NUMBER = f.createAttributeType(typeName(URI, "Number"),
                Integer.class, false, false, Collections.EMPTY_SET, null, null);

        List address = new ArrayList();
        address.add(attribute("name", TEXT));
        address.add(attribute("suite", NUMBER));
        address.add(attribute("address", TEXT));
        address.add(attribute("province", TEXT));
        address.add(attribute("code", NUMBER));

        ComplexType ADDRESS = f.createComplexType(typeName("Person"), address,
                true, false, null, null, null);
        assertNotNull(ADDRESS);
        assertEquals("has attributes", 5, ADDRESS.attributes().size());

        AttributeType DATE = f.createAttributeType(typeName(URI, "Date"),
                Date.class, false, false, Collections.EMPTY_SET, null, null);
        AttributeType LOCATION = f.createGeometryType(
                typeName(URI, "Location"), Point.class, null, false, false,
                null, null, null);

        AttributeDescriptor location = attribute("location", LOCATION);
        assertNotNull(location.type());

        List person = new ArrayList();
        person.add(attribute("name", TEXT));
        person.add(attribute("home", ADDRESS));
        person.add(attribute("work", ADDRESS));
        person.add(attribute("birthday", DATE));
        person.add(location);

        // FIXME: this method will always require a cast for java 1.4 code
        FeatureType PERSON = f.createFeatureType(typeName("Person"), person,
                location, null, false, null, null, null);
        assertNotNull(PERSON);

        assertEquals("has attributes", 5, PERSON.attributes().size());
    }

    //
    // utility methods as we want to test the type system
    // not the naming system
    //
    static final Namespace space(String uri) {
        return new org.geotools.util.Namespace(uri);
    }

    static final Name name(Namespace scope, String local) {
        Name name = name(scope.getURI(), local);
        scope.add(name);
        return name;
    }

    static final Name name(String local) {
        return typeName(local);
    }

    static final Name name(String uri, String local) {
        return typeName(uri, local);
    }

    static final Name typeName(String uri, String type) {
        return Types.typeName(uri, type);
    }

    static final Name typeName(String name) {
        return Types.typeName(name);
    }

    static final AttributeDescriptor attribute(String name, AttributeType type) {
        Name attributeName = typeName("irc://localhost/#bot", name);
        return new AttributeDescriptorImpl(type, attributeName, 1, 1, true, null);
    }
}
