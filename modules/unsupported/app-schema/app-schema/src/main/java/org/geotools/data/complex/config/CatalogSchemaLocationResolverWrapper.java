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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.xml.resolver.Catalog;
import org.eclipse.xsd.XSDSchema;
import org.geotools.xml.SchemaLocationResolver;

/**
 * Wrapper to perform OASIS catalogue lookup when resolving a schema location.
 * 
 * @author Ben Caradoc-Davies, CSIRO Exploration and Mining
 * @version $Id: CatalogSchemaLocationResolverWrapper.java 31815 2008-11-10 07:53:14Z bencd $
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/app-schema/app-schema/src/main/java/org/geotools/data/complex/config/CatalogSchemaLocationResolverWrapper.java $
 * @since 2.6
 */
public class CatalogSchemaLocationResolverWrapper extends SchemaLocationResolver {

    private final Catalog catalog;

    /*
     * This sucks. This should be XSDSchemaLocationResolver, but because XSD.schemaLocationResolver
     * returns SchemaLocationResolver not the interface, we are forced to use the concrete type. So
     * much for dependency injection.
     */
    private SchemaLocationResolver resolver;

    /**
     * @param catalog
     *                catalog to search, or null if none
     * @param resolver
     *                fallback resolver to use if not found in catalog
     */
    public CatalogSchemaLocationResolverWrapper(final Catalog catalog,
            final SchemaLocationResolver resolver) {
        super(null);
        this.catalog = catalog;
        this.resolver = resolver;
    }

    /**
     * @param schema
     *                the schema being resolved
     * @param uri
     *                the namespace being resolved. If its an empty string (i.e. the location refers
     *                to an include, and thus the uri to the same one than the schema), the schema
     *                one is used.
     * @param location
     *                the xsd location, either of <code>schema</code>, an import or an include,
     *                for which to try resolving it as a relative path of the <code>schema</code>
     *                location.
     * @return
     * 
     */
    @Override
    public String resolveSchemaLocation(final XSDSchema schema, final String uri,
            final String location) {
        /*
         * The old 2.4.x branch Configuration only did file handling. The new Configuration resolves
         * any "http:" URI to itself. This prevents using the OASIS catalogue for fallback, and is
         * arguably a bug. To workaround this, we try the catalogue first, if we have one.
         */
        String schemaLocation = CatalogUtilities.resolveSchemaLocation(catalog, location);
        if (schemaLocation != null) {
            return canonicalLocation(schemaLocation);
        } else {
            return canonicalLocation(resolver.resolveSchemaLocation(schema, uri, location));
        }
    }

    /**
     * Return true if the resolver can find the schema.
     * 
     * @see org.geotools.xml.SchemaLocationResolver#canHandle(org.eclipse.xsd.XSDSchema,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public boolean canHandle(XSDSchema schema, String uri, String location) {
        return resolveSchemaLocation(schema, uri, location) != null;
    }

    @Override
    public String toString() {
        return resolver.toString();
    }

    /**
     * Convert the schema file location URI to canonical form.
     * 
     * <p>
     * 
     * Canonicalisation is necessary to prevent infinite recursion when loading some schema families
     * with relative import paths. You know who you are.
     * 
     * @param schemaLocation
     *                a file URI string, for example "file:/whatever/./something.xsd"
     * @return canonical form of the file path, for example "file:/whatever/something.xsd"
     */
    static String canonicalLocation(String schemaLocation) {
        if (schemaLocation != null) {
            try {
                URI uri = new URI(schemaLocation);
                if (uri.getScheme().equals("file")) {
                    File file = new File(uri.getPath());
                    if (file.exists()) {
                        return file.getCanonicalFile().toURI().toString();
                    }
                }
            } catch (URISyntaxException e) {
                // ignore
            } catch (IOException e) {
                // ignore
            }
        }
        return schemaLocation;
    }

}