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
import org.eclipse.xsd.util.XSDSchemaLocationResolver;
import java.io.File;
import java.net.URL;


/**
 * Resolves a physical schema location from a namespace uri.
 * <p>
 * This class works from a {@link org.geotools.xml.XSD} instance from which it
 * resolves location on disk relative to.
 * </p>
 * <p>
 * Example usage:
 *
 * <code>
 *         <pre>
 *         XSD xsd = ...
 *         String namespaceURI = xsd.getNamesapceURI();
 *
 *         SchemaLocationResolver resolver = new SchemaLocationResolver( xsd );
 *         String schemaLocation = locator.resolveSchemaLocation( null, namespaceURI, "mySchema.xsd" );
 *         </pre>
 * </code>
 *
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class SchemaLocationResolver implements XSDSchemaLocationResolver {
    /**
     * the xsd instance
     */
    protected XSD xsd;

    /**
     * Creates the new schema location resolver.
     *
     * @param xsd The xsd to resolve filenames relative to.
     */
    public SchemaLocationResolver(XSD xsd) {
        this.xsd = xsd;
    }

    /**
     * Determines if the locator can resolve the schema location for a particular 
     * namespace uri and schema location.
     * 
     * @return true if it can handle, otherwise false.
     */
    public boolean canHandle( XSDSchema schema, String uri, String location ) {
        if ( xsd.getNamespaceURI().equals(uri) ) {
            //strip off the filename and do a resource lookup
            String fileName = new File(location).getName();
            URL xsdLocation = xsd.getClass().getResource(fileName);

            return xsdLocation != null;
        }
        
        return false;
    }
    
    /**
     * Resolves <param>location<param> to a physical location.
     * <p>
     * Resolution is performed by stripping the filename off of <param>location</param>
     * and looking up a resource located in the same package as the xsd.
     * </p>
     */
    public String resolveSchemaLocation(XSDSchema schema, String uri, String location) {
        if (location == null) {
            return null;
        }

        //if no namespace given, assume default for the current schema
        if (((uri == null) || "".equals(uri)) && (schema != null)) {
            uri = schema.getTargetNamespace();
        }

        //namespace match?
        if (canHandle(schema, uri, location)) {
            String fileName = new File(location).getName();
            return xsd.getClass().getResource(fileName).toString();
        }

        return null;
    }

    public String toString() {
        return xsd.toString();
    }
}
