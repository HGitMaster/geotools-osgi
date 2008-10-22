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

package org.geotools.data.complex.config;

import org.apache.xml.resolver.Catalog;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xs.XSConfiguration;

/**
 * An xml configuration for application schemas just like {@link ApplicationSchemaConfiguration},
 * but with OASIS catalog support.
 * <p>
 * This Configuration expects the namespace and schema location URI of the main xsd file for a given
 * application schema and is able to resolve the schema location for the includes and imports as
 * well as they're defined as relative paths and the provided <code>schemaLocation</code> is a
 * file URI.
 * </p>
 * 
 * @see ApplicationSchemaConfiguration
 */
public class CatalogApplicationSchemaConfiguration extends Configuration {

    /**
     * @param namespace
     *                namespace URI
     * @param schemaLocation
     *                URI of main xsd file for the application schema
     * @param catalog
     *                OASIS catalog to be used if other means of locating schema fail, or null if
     *                none
     */
    public CatalogApplicationSchemaConfiguration(String namespace, String schemaLocation,
            Catalog catalog) {
        super(new CatalogApplicationSchemaXSD(namespace, schemaLocation, catalog));
        addDependency(new XSConfiguration());
        addDependency(new GMLConfiguration());
    }

}
