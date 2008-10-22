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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xml.resolver.Catalog;

/**
 * Implementation help for use of OASIS catalog.
 */
public class CatalogUtilities {

    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(CatalogUtilities.class.getPackage().getName());

    /**
     * Return schema location resolved to local file if possible.
     * 
     * @param catalog
     *                can be null if no catalog
     * @param location
     * @return null if catalog is null or location not found in catalog
     */
    public static String resolveSchemaLocation(Catalog catalog, String location) {
        if (catalog == null) {
            return null;
        } else {
            String schemaLocation = null;
            try {
                LOGGER.finest("resolving " + location);
                /*
                 * See discussion of rewriteSystem versus rewriteURI:
                 * https://www.seegrid.csiro.au/twiki/bin/view/AppSchemas/ConfiguringXMLProcessors
                 * Old version used rewriteSystem.
                 */
                schemaLocation = catalog.resolveURI(location);
                if (schemaLocation != null) {
                    LOGGER.finer("Verifying existence of catalog resolved location "
                            + schemaLocation);
                    try {
                        File f = new File(new URI(schemaLocation));
                        if (!f.exists()) {
                            LOGGER.info("Cannot locate " + schemaLocation);
                            schemaLocation = null;
                        }
                    } catch (URISyntaxException e) {
                        schemaLocation = null;
                        LOGGER.log(Level.WARNING, "Exception resolving " + schemaLocation, e);
                    }
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return schemaLocation;
        }
    }

}
