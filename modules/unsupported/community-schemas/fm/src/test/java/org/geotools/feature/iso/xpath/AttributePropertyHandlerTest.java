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

package org.geotools.feature.iso.xpath;

import junit.framework.TestCase;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.geotools.feature.iso.AttributeBuilder;
import org.geotools.feature.iso.AttributeFactoryImpl;
import org.geotools.feature.iso.FeatureImpl;
import org.geotools.feature.iso.TypeBuilder;
import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.geotools.feature.type.TypeName;
import org.geotools.util.GTContainer;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.TypeFactory;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

public class AttributePropertyHandlerTest extends TestCase {

    MutablePicoContainer container;

    TypeBuilder tb;

    AttributeBuilder fb;

    FeatureType type;

    Feature f;

    protected void setUp() throws Exception {
        super.setUp();

        container = GTContainer.simple();
        container = new DefaultPicoContainer();
        container.registerComponentImplementation(AttributeFactoryImpl.class);
        container.registerComponentImplementation(TypeFactoryImpl.class);

        container.registerComponentImplementation(TypeBuilder.class);
        container.registerComponentImplementation(AttributeBuilder.class);

        tb = (TypeBuilder) container
                .getComponentInstanceOfType(TypeBuilder.class);
        fb = (AttributeBuilder) container
                .getComponentInstanceOfType(AttributeBuilder.class);

        TypeFactory tf = tb.getTypeFactory();
        AttributeType integerType = tf.createAttributeType(new TypeName(
                "integerType"), Integer.class, false, false, null, null, null);
        AttributeType stringType = tf.createAttributeType(new TypeName(
                "stringType"), String.class, false, false, null, null, null);
        AttributeType doubleType = tf.createAttributeType(new TypeName(
                "doubleType"), Double.class, false, false, null, null, null);
        tb.addBinding(Integer.class, integerType);
        tb.addBinding(String.class, stringType);
        tb.addBinding(Double.class, doubleType);

        tb.init();
        tb.attribute("id", Integer.class);
        tb.attribute("name", String.class);
        tb.setName("type");
        type = tb.feature();

        fb.init();
        fb.setType(type);
        fb.add(new Integer(1), "id");
        fb.add("foo", "name");
        f = (Feature) fb.build("f1");
    }

    public void testSimpleFeature() {

        JXPathIntrospector.registerDynamicClass(FeatureImpl.class,
                AttributePropertyHandler.class);

        JXPathContext context = JXPathContext.newContext(f);

         Attribute id = (Attribute) context.getValue("id");
         assertNotNull(id);
         assertEquals(new Integer(1), id.getValue());

//        Object id = context.getValue("id");
//        assertNotNull(id);
//        assertEquals(new Integer(1), id);

        Attribute name = (Attribute) context.getValue("name");
        assertNotNull(name);
        assertEquals("foo", name.getValue());

//        Object name = context.getValue("name");
//        assertNotNull(name);
//        assertEquals("foo", name);
    }

    public void testNonSimpleFeature() {
        tb.init();

        tb.attribute("xid", Double.class);
        tb.attribute("feature", type);
        tb.name("ctype");
        FeatureType ctype = tb.feature();

        fb.init();
        fb.setType(ctype);
        fb.add(new Double(1), "xid");
        fb.add(f.getValue(), "feature");

        Feature cf = (Feature) fb.build("f1");

        JXPathIntrospector.registerDynamicClass(FeatureImpl.class,
                AttributePropertyHandler.class);

        JXPathContext context = JXPathContext.newContext(cf);

        Attribute xid = (Attribute) context.getValue("xid");
        assertNotNull(xid);
        assertEquals(new Double(1), xid.getValue());

//        Object xid = context.getValue("xid");
//        assertNotNull(xid);
//        assertEquals(new Double(1), xid);

        Attribute id = (Attribute) context.getValue("feature/id");
        assertNotNull(id);
        assertEquals(new Integer(1), id.getValue());

//        Object id = context.getValue("feature/id");
//        assertNotNull(id);
//        assertEquals(new Integer(1), id);

        Attribute name = (Attribute) context.getValue("feature/name");
        assertNotNull(name);
        assertEquals("foo", name.getValue());

//        Object name = context.getValue("feature/name");
//        assertNotNull(name);
//        assertEquals("foo", name);
    }

}
