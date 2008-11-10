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
import org.geotools.gml3.ApplicationSchemaXSD;
import org.geotools.xml.SchemaLocationResolver;

/**
 * Schema with support for resolution in OASIS catalog.
 * 
 * @author Ben Caradoc-Davies, CSIRO Exploration and Mining
 * @version $Id: CatalogApplicationSchemaXSD.java 31815 2008-11-10 07:53:14Z bencd $
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/app-schema/app-schema/src/main/java/org/geotools/data/complex/config/CatalogApplicationSchemaXSD.java $
 * @since 2.6
 */
public class CatalogApplicationSchemaXSD extends ApplicationSchemaXSD {

    private final Catalog catalog;

    /**
     * @param namespaceURI
     *                namespace of the schema
     * @param schemaLocation
     *                the schema location
     * @param catalog
     *                OASIS Catalog for lookup, or null if none
     */
    public CatalogApplicationSchemaXSD(String namespaceURI, String schemaLocation, Catalog catalog) {
        super(namespaceURI, resolveSchemaLocation(catalog, schemaLocation));
        this.catalog = catalog;
    }

    /**
     * @see org.geotools.gml3.ApplicationSchemaXSD#createSchemaLocationResolver()
     */
    @Override
    protected SchemaLocationResolver createSchemaLocationResolver() {
        return new CatalogSchemaLocationResolverWrapper(catalog, super
                .createSchemaLocationResolver());
    }

    /**
     * Translate a schema location to a local file if found in an OASIS catalog, else leave it
     * alone.
     * 
     * @param catalog
     * @param schemaLocation
     * @return translated local file name, or original if not found
     */
    private static String resolveSchemaLocation(Catalog catalog, String schemaLocation) {
        String location = CatalogUtilities.resolveSchemaLocation(catalog, schemaLocation);
        if (location == null) {
            return schemaLocation;
        } else {
            return location;
        }

    }

}
