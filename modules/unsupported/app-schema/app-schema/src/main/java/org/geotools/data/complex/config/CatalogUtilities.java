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
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;

/**
 * Implementation help for use of OASIS catalog.
 * 
 * @author Ben Caradoc-Davies, CSIRO Exploration and Mining
 * @version $Id: CatalogUtilities.java 33534 2009-07-09 08:31:36Z bencaradocdavies $
 * @source $URL:
 *         http://svn.osgeo.org/geotools/trunk/modules/unsupported/app-schema/app-schema/src/main
 *         /java/org/geotools/data/complex/config/CatalogUtilities.java $
 * @since 2.6
 */
public class CatalogUtilities {

    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(CatalogUtilities.class.getPackage().getName());

    /**
     * Return schema location resolved to local file if possible.
     * 
     * @param catalog
     *            can be null if no catalog
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

    /**
     * Build a private {@link Catalog}, that is, not the static instance that {@link CatalogManager}
     * returns by default.
     * 
     * <p>
     * 
     * Care must be taken to use only private {@link Catalog} instances if there will ever be more
     * than one OASIS Catalog used in a single class loader (i.e. a single maven test run),
     * otherwise {@link Catalog} contents will be an amalgam of the entries of both OASIS Catalog
     * files, with likely unintended or incorrect results. See GEOT-2497.
     * 
     * @param catalogLocation
     *            URL of OASIS Catalog
     * @return a private Catalog
     */
    public static Catalog buildPrivateCatalog(URL catalogLocation) {
        CatalogManager catalogManager = new CatalogManager();
        catalogManager.setUseStaticCatalog(false);
        catalogManager.setVerbosity(0);
        catalogManager.setIgnoreMissingProperties(true);
        Catalog catalog = catalogManager.getCatalog();
        try {
            catalog.parseCatalog(catalogLocation);
        } catch (IOException e) {
            throw new RuntimeException("Error trying to load OASIS catalog from URL "
                    + catalogLocation.toString(), e);
        }
        return catalog;
    }

}
