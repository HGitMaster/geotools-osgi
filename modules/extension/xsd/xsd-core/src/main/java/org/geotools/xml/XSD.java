/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.xml;

import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.xs.XS;


/**
 * Xml Schema for a particular namespace.
 * <p>
 * This class should is subclasses for the xs, gml, filter, sld, etc... schemas.
 * Subclasses should be implemented as singletons.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 * @since 2.5
 */
public abstract class XSD {
    /**
     * logging instance
     */
    protected static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.xml");

    /**
     * schema contents
     */
    protected XSDSchema schema;

    /**
     * dependencies
     */
    private Set /*<XSD>*/ dependencies;

    protected XSD() {
    }

    /**
     * The namespace uri of the schema.
     */
    public abstract String getNamespaceURI();

    /**
     * The location on the local disk of the top level .xsd file which defines
     * the schema.
     */
    public abstract String getSchemaLocation();

    /**
     * The dependencies of this schema.
     */
    public final Set getDependencies() {
        if (dependencies == null) {
            synchronized (this) {
                if (dependencies == null) {
                    dependencies = new LinkedHashSet();

                    //bootstrap, every xsd depends on XS
                    dependencies.add(XS.getInstance());

                    //call subclass hook
                    addDependencies(dependencies);
                }
            }
        }

        return dependencies;
    }

    protected List allDependencies() {
        LinkedList unpacked = new LinkedList();

        Stack stack = new Stack();
        stack.addAll(getDependencies());

        while (!stack.isEmpty()) {
            XSD xsd = (XSD) stack.pop();

            if (!unpacked.contains(xsd)) {
                unpacked.addFirst(xsd);
                stack.addAll(xsd.getDependencies());
            }
        }

        return unpacked;
    }

    /**
     * Subclass hook to add additional dependencies.
     */
    protected void addDependencies(Set dependencies) {
    }

    /**
     * Returns the XSD object representing the contents of the schema.
     */
    public final XSDSchema getSchema() throws IOException {
        if (schema == null) {
            synchronized (this) {
                if (schema == null) {
                    LOGGER.fine("building schema for schema: " + getNamespaceURI());
                    schema = buildSchema();
                }
            }
        }

        return schema;
    }

    /**
     * Builds the schema from the .xsd file specified by {@link #getSchemaLocation()}
     * <p>
     * This method may be extended, but should not be overridden.
     * </p>
     */
    protected XSDSchema buildSchema() throws IOException {
        //grab all the dependencies and create schema locators from the build
        // schemas
        List locators = new ArrayList();
        List resolvers = new ArrayList();

        for (Iterator d = allDependencies().iterator(); d.hasNext();) {
            XSD dependency = (XSD) d.next();
            SchemaLocator locator = dependency.createSchemaLocator();

            if (locator != null) {
                locators.add(locator);
            }

            SchemaLocationResolver resolver = dependency.createSchemaLocationResolver();

            if (resolver != null) {
                resolvers.add(resolver);
            }
        }

        SchemaLocationResolver resolver = createSchemaLocationResolver();

        if (resolver != null) {
            resolvers.add(resolver);
        }

        //parse the location of the xsd with all the locators for dependent
        // schemas
        return Schemas.parse(getSchemaLocation(), locators, resolvers);
    }

    protected SchemaLocator createSchemaLocator() {
        return new SchemaLocator(this);
    }

    protected SchemaLocationResolver createSchemaLocationResolver() {
        return new SchemaLocationResolver(this);
    }

    /**
     * Implementation of equals, equality is based soley on {@link #getNamespaceURI()}.
     */
    public final boolean equals(Object obj) {
        if (obj instanceof XSD) {
            XSD other = (XSD) obj;

            return getNamespaceURI().equals(other.getNamespaceURI());
        }

        return false;
    }

    public final int hashCode() {
        return getNamespaceURI().hashCode();
    }

    public String toString() {
        return getNamespaceURI();
    }
}
