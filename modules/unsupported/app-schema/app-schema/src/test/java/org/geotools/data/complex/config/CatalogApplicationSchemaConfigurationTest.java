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

import java.net.URL;

import junit.framework.TestCase;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.tools.ResolvingXMLReader;
import org.geotools.xml.Configuration;

/**
 * Tests for {@link CatalogApplicationSchemaConfiguration}.
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/app-schema/app-schema/src/test/java/org/geotools/data/complex/config/CatalogApplicationSchemaConfigurationTest.java $
 */
public class CatalogApplicationSchemaConfigurationTest extends TestCase {

    final String schemaBase = "/test-data/";

    /**
     * Test that a schema known to be in the catalog is resolved to the expected local file.
     */
    public void testCatalogSchemaResolution() throws Exception {
        URL catalogLocation = getClass().getResource(schemaBase + "mappedPolygons.oasis.xml");
        Catalog catalog = CatalogUtilities.buildPrivateCatalog(catalogLocation);
        String namespace = "http://www.cgi-iugs.org/xml/GeoSciML/2";
        String schemaLocation = "http://schemas.opengis.net/GeoSciML/geosciml.xsd";
        Configuration config = new CatalogApplicationSchemaConfiguration(namespace, schemaLocation,
                catalog);
        String resolvedSchemaLocation = config.getXSD().getSchemaLocation();
        assertTrue(resolvedSchemaLocation.startsWith("file:/"));
        assertTrue(resolvedSchemaLocation.endsWith(schemaBase
                + "commonSchemas_new/GeoSciML/geosciml.xsd"));
    }

}
