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

package org.geotools.util;

import org.geotools.feature.iso.AttributeFactoryImpl;
import org.geotools.feature.iso.FeatureTypes;
import org.geotools.feature.iso.TypeBuilder;
import org.geotools.feature.iso.simple.ArraySimpleFeatureFactory;
import org.geotools.feature.iso.simple.SimpleFeatureBuilder;
import org.geotools.feature.iso.simple.SimpleFeatureFactoryImpl;
import org.geotools.feature.iso.simple.SimpleFeatureTypes;
import org.geotools.feature.iso.simple.SimpleFeatures;
import org.geotools.feature.iso.simple.SimpleTypeBuilder;
import org.geotools.feature.iso.simple.SimpleTypeFactoryImpl;
import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.geotools.filter.FilterFactoryImpl;
import org.picocontainer.defaults.ConstructorInjectionComponentAdapter;
import org.picocontainer.defaults.DefaultPicoContainer;

import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * This "container" is used by several utility classes to get their game on.
 * <p>
 * XXX: Is this class API? or is just here for some utility classes to limp
 * along?
 * </p>
 */
public class GTContainer {
    private static final long serialVersionUID = 9115947621979056700L;

    /**
     * This will produce a Container for use with the "default" implementation
     * of the feature model.
     * <ul>
     * <li>TypeFactory
     * <li>FeatureFactory
     * <li>(GeometryFactory)
     * <li>(FilterFactory)
     * </ul>
     * We have also included support for the following builders interfaces and
     * utility classes "out of the box".
     * <ul>
     * <li>TypeBuilder
     * <li>FeatureTypes
     * <li>FeatureTypes
     * </ul>
     * 
     * <p>
     * TODO: Allow client code to configure
     * 
     * @return container for "normal" complex content content
     */
    public static DefaultPicoContainer normal() {
        DefaultPicoContainer c = new DefaultPicoContainer();

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
     * Container used to work with the "simple" implementation of the feature
     * model.
     * <ul>
     * <li>SimpleTypeFactory, also serves as a TypeFactory
     * <li>SimpleFeatureFactory, also serves as a FeatureFactory
     * <li>(GeometryFactory)
     * <li>(FilterFactory)
     * </ul>
     * We have also included support for the following builders and utility
     * classes "out of the box".
     * <ul>
     * <li>SimpleTypeBuilder, created each time!
     * <li>SimpleFeatureBuilder, created each time!
     * <li>SimpleFeatureTypes
     * <li>SimpleFeatures
     * </ul>
     * TODO: Allow client code to configure
     * 
     * @return container tweaked for for simple content
     */
    public static DefaultPicoContainer simple() {
        DefaultPicoContainer c = new DefaultPicoContainer();

        // factories
        c.registerComponentImplementation(SimpleTypeFactoryImpl.class);
        c.registerComponentImplementation(SimpleFeatureFactoryImpl.class);

        c.registerComponentImplementation(GeometryFactory.class);
        c.registerComponentImplementation(FilterFactoryImpl.class);

        // builders (utility classes for creation)
        c.registerComponent(new ConstructorInjectionComponentAdapter(
                SimpleTypeBuilder.class, SimpleTypeBuilder.class));
        c.registerComponent(new ConstructorInjectionComponentAdapter(
                SimpleFeatureBuilder.class, SimpleFeatureBuilder.class));

        // utilities
        c.registerComponentImplementation(SimpleFeatureTypes.class);
        c.registerComponentImplementation(SimpleFeatures.class);

        return c;
    }

    public static DefaultPicoContainer array() {
        DefaultPicoContainer c = new DefaultPicoContainer();

        // factories
        c.registerComponentImplementation(SimpleTypeFactoryImpl.class);
        c.registerComponentImplementation(ArraySimpleFeatureFactory.class);

        c.registerComponentImplementation(GeometryFactory.class);
        c.registerComponentImplementation(FilterFactoryImpl.class);

        // builders (utility classes for creation)
        c.registerComponent(new ConstructorInjectionComponentAdapter(
                SimpleTypeBuilder.class, SimpleTypeBuilder.class));
        c.registerComponent(new ConstructorInjectionComponentAdapter(
                SimpleFeatureBuilder.class, SimpleFeatureBuilder.class));

        // utilities
        c.registerComponentImplementation(SimpleFeatureTypes.class);
        c.registerComponentImplementation(SimpleFeatures.class);

        return c;
    }
}
