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

package org.geotools;

import org.geotools.feature.iso.FeatureTypes;
import org.geotools.feature.iso.AttributeFactoryImpl;
import org.geotools.feature.iso.TypeBuilder;
import org.geotools.feature.iso.simple.SimpleFeatureTypes;
import org.geotools.feature.iso.simple.SimpleFeatures;
import org.geotools.feature.iso.simple.SimpleFeatureBuilder;
import org.geotools.feature.iso.simple.SimpleTypeBuilder;
import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.geotools.filter.FilterFactoryImpl;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapter;
import org.picocontainer.defaults.DefaultPicoContainer;

import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * This class sets up a sample container with the default implementations
 * implemented by module main.
 * <p>
 * For the test container we are using PicoContainer because it is well suited
 * to quick sanity checks like test cases. For a real industrial strength
 * solution please use something like Spring or JBoss containers (both of which
 * can be used in a client application and can be configured via XML).
 * </p>
 * <p>
 * The following are avaiable:
 * <ul>
 * <li>TypeFactory - TypeFactoryImpl
 * <li>AttributeFactory - TypeFactoryImpl
 * </ul>
 * <p>
 * <p>
 * With this as a base instances of the following builders can be requested,
 * they will arrive ready to go with no additional setup.
 * <ul>
 * <li>TypeBuilder - TypeBuilderImpl
 * <li>ComplexAttributeBuilder - ComplexAttributeBuilderImpl
 * </ul>
 * <p>
 * Here is a sample use:
 * 
 * <pre><code>
 * TestContainer container = new TestContainer();
 * ComplexAttributeBuilder builder = TestContainer
 * 		.getComponentInstanceOfType(ComplexAttributeBuilder.class);
 * 
 * build.setType(roadFeatureType); // aka a FeatureType
 * Feature road1, road2;
 * 
 * road1 = builder.add(&quot;name&quot;, &quot;hwy 31a&quot;).add(&quot;geom&quot;, hwy1).build(&quot;Road.31a&quot;);
 * road2 = builder.add(&quot;name&quot;, &quot;hwy 32&quot;).add(&quot;geom&quot;, hwy2).build(&quot;Road.32&quot;);
 * </code></pre>
 * 
 * For a discussion of the pros/cons using a container please review the
 * developers documentation. Or try writing any geotools program using the
 * "traditional" FactoryFinder approach.
 * </p>
 * 
 * @author Jody Garnett, Refractions Research Inc.
 */
public class TestContainer extends DefaultPicoContainer {
	private static final long serialVersionUID = 654566278905574585L;

	private TestContainer() {
	}

	/**
	 * This will produce a TestContainer for use with the "default"
	 * implementation of the feature model.
	 * <ul>
	 * <li>TypeFactoryImpl - creates "default" implementation
	 * <li>AttributeFactoryImpl - creates "default" implementations
	 * </ul>
	 * We have also included support for the following builders interfaces and
	 * utility classes "out of the box".
	 * <ul>
	 * <li>AttributeTypeBuilder
	 * <li>GeometryTypeBuilder
	 * <li>ComplexTypeBuilder
	 * <li>FeatureTypeBuilder
	 * <li>FeatureCollectionTypeBuilder
	 * <li>FeatureBuilder
	 * <li>FeatureCollectionBuilder
	 * <li>FeatureTypes
	 * </ul>
	 * 
	 * @return Test Container used to test the "default" implementation of the
	 *         type system.
	 */
	public static TestContainer normal() {
		TestContainer c = new TestContainer();

		// factories
		c.registerComponentImplementation(TypeFactoryImpl.class);
		c.registerComponentImplementation(AttributeFactoryImpl.class);
		c.registerComponentImplementation(GeometryFactory.class);
        c.registerComponentImplementation(FilterFactoryImpl.class);
        
		// builders (utility classes for creation)
		c.registerComponent(new ConstructorInjectionComponentAdapter(
				TypeBuilder.class, TypeBuilder.class));

		// utilities
		c.registerComponentImplementation(FeatureTypes.class);

		return c;
	}

	/**
	 * TestContainer used to work with the "simple" implementation of the
	 * feature model.
	 * <ul>
	 * <li>TypeFactorySimple - TODO implement simple factory support
	 * <li>AttributeFactorySimple - TODO implement simple feature support
	 * </ul>
	 * We have also included support for the following builders and utility
	 * classes "out of the box".
	 * <ul>
	 * <li>SimpleFeatureTypeBuilder
	 * <li>SimpleFeatureCollectionTypeBuilder
	 * <li>SimpleFeatureBuilder
	 * <li>SimpleFeatureCollectionBuilder
	 * <li>SimpleFeatureTypes
	 * <li>SimpleFeatures
	 * </ul>
	 * 
	 * @return
	 */
	public static TestContainer simple() {
		TestContainer c = new TestContainer();

		// factories
		c.registerComponentImplementation(TypeFactoryImpl.class);
		c.registerComponentImplementation(AttributeFactoryImpl.class);
        c.registerComponentImplementation(GeometryFactory.class);
        c.registerComponentImplementation(FilterFactoryImpl.class);
        
		// builders (utility classes for creation)
        c.registerComponentImplementation(SimpleTypeBuilder.class);
        c.registerComponentImplementation(SimpleFeatureBuilder.class);
        
		// utilities
		c.registerComponentImplementation(SimpleFeatureTypes.class);
		c.registerComponentImplementation(SimpleFeatures.class);

		return c;
	}

}