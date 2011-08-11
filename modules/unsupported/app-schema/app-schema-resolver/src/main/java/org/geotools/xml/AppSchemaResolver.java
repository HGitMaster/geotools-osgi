/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2010, Open Source Geospatial Foundation (OSGeo)
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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Application schema resolver that maps absolute URLs to local URL resources.
 * 
 * <p>
 * 
 * Resources are sought, in order:
 * 
 * <ol>
 * 
 * <li>In an <a href="http://www.oasis-open.org/committees/entity/spec-2001-08-06.html">OASIS
 * Catalog</a> (with URI resolution semantics), which maps URLs to arbitrary filesystem locations.</li>
 * 
 * <li>On the classpath, where resoruces are located by their Simple HTTP Resource Path (see
 * {@link #getSimpleHttpResourcePath(URI)}).
 * 
 * <li>In a cache, with optional downloading support.
 * 
 * </ol>
 * 
 * @author Ben Caradoc-Davies, CSIRO Earth Science and Resource Engineering
 */
public class AppSchemaResolver {

    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(AppSchemaResolver.class.getPackage().getName());

    /**
     * A local OASIS catalog (null if not present).
     */
    private AppSchemaCatalog catalog;

    /**
     * Cache of schemas with optional downloading support(null if not present).
     */
    private AppSchemaCache cache;

    /**
     * Maps a resolved location (a URL used to obtain a schema from a file or the classpath) to the
     * original HTTP URL used to obtain it. This is required so that relative imports can be
     * resolved if they cross resolution boundaries. For example, an import ../../../om/.. used to
     * import om in a schema, where one is supplied locally and the other must be downloaded and
     * cached. Yes, this does happen in real life.
     */
    private Map<String, String> resolvedLocationToLocationMap = new HashMap<String, String>();

    public AppSchemaResolver(AppSchemaCatalog catalog, AppSchemaCache cache) {
        this.catalog = catalog;
        this.cache = cache;
    }

    /**
     * Convenience constructor for a resolver with neither catalog nor cache (just classpath).
     */
    public AppSchemaResolver() {
        this(null, null);
    }

    /**
     * Convenience constructor for a resolver with no cache.
     * 
     * @param catalog
     */
    public AppSchemaResolver(AppSchemaCatalog catalog) {
        this(catalog, null);
    }

    /**
     * Convenience constructor for a resolver with no catalog.
     * 
     * @param cache
     */
    public AppSchemaResolver(AppSchemaCache cache) {
        this(null, cache);
    }

    /**
     * Resolve an absolute URL to a local file or jar URL.
     * 
     * @param location
     *            an absolute URL
     * @return the string representation of a file or jar URL
     * @throws RuntimeException
     *             if a local resource could not be found
     */
    public String resolve(String location) {
        String resolvedLocation = null;
        // already resolved?
        if (location.startsWith("file:") || location.startsWith("jar:file:")) {
            resolvedLocation = location;
        }
        // use catalog if one supplied
        if (resolvedLocation == null && catalog != null) {
            resolvedLocation = catalog.resolveLocation(location);
        }
        // look on classpath
        if (resolvedLocation == null) {
            resolvedLocation = resolveClasspathLocation(location);
        }
        // use download cache
        if (resolvedLocation == null && cache != null) {
            resolvedLocation = cache.resolveLocation(location);
        }
        // fail if still not resolved
        if (resolvedLocation == null) {
            throw new RuntimeException(String.format("Failed to resolve %s", location));
        }
        resolvedLocationToLocationMap.put(resolvedLocation, location);
        try {
            /*
             * This monstrous hack is required because org.geotools.xml.Schemas.parse(String, List,
             * List) uses URLDecoder.decode(String) to handle escaped spaces in a file: URL, causing
             * an invalid URL (not RFC 1738 compliant) to be passed to the underlying EMF
             * infrastructure. The workaround is to allow reverse lookups with the decoded string by
             * inserting the decoded version into the map as well as the undecoded version inserted
             * above). *Shudder*.
             */
            resolvedLocationToLocationMap.put(URLDecoder.decode(resolvedLocation, "UTF-8"),
                    location);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        LOGGER.fine(String.format("Resolved %s -> %s", location, resolvedLocation));
        return resolvedLocation;
    }

    /**
     * Maps a resolved location (a URL used to obtain a schema from a file or the classpath) to the
     * original HTTP URL used to obtain it. This is required so that relative imports can be
     * resolved if they cross resolution boundaries. For example, an import ../../../om/.. used to
     * import om in a schema, where one is supplied locally and the other must be downloaded and
     * cached. Yes, this does happen in real life.
     */
    public String unresolve(String resolvedLocation) {
        return resolvedLocationToLocationMap.get(resolvedLocation);
    }

    /**
     * Return the Simple HTTP Resource Path for an absolute http/https URL.
     * 
     * @param location
     *            not null
     * @return the resource path with a leading slash
     * @see #getSimpleHttpResourcePath(URI)
     */
    public static String getSimpleHttpResourcePath(String location) {
        URI locationUri;
        try {
            locationUri = new URI(location);
        } catch (URISyntaxException e) {
            return null;
        }
        return getSimpleHttpResourcePath(locationUri);
    }

    /**
     * Return the Simple HTTP Resource Path for an absolute http/https URL.
     * 
     * <p>
     * 
     * The Simple HTTP Resource Path maps an HTTP or HTTPS URL to a path on the classpath or
     * relative to some other root. To form the Simple HTTP Resource Path from an http/https URL:
     * 
     * <ol>
     * <li>Protocol, port, fragment, and query are ignored.</li>
     * <li>Take the host name, split it into its components, reverse their order, prepend a forward
     * slash to each, and concatenate them.</li>
     * <li>Append the path component of the URL.</li>
     * </ol>
     * 
     * For example <code>http://schemas.example.org/exampleml/exml.xsd</code> becomes
     * <code>/org/example/schemas/exampleml/exml.xsd</code> .
     * 
     * <p>
     * 
     * The Simple HTTP Resource Path always starts with a forward slash (if not null).
     * 
     * @param location
     *            not null
     * @return the Simple HTTP Resource Path as a string, or null if the URI is not an absolute
     *         HTTP/HTTPS URL.
     */
    public static String getSimpleHttpResourcePath(URI location) {
        String scheme = location.getScheme();
        if (scheme == null || !(scheme.equals("http") || scheme.equals("https"))) {
            return null;
        } else {
            String host = location.getHost();
            String path = location.getPath();
            String[] hostParts = host.split("\\.");
            StringBuffer buffer = new StringBuffer();
            for (int i = hostParts.length - 1; i >= 0; i--) {
                buffer.append("/");
                buffer.append(hostParts[i]);
            }
            buffer.append(path);
            return buffer.toString();
        }
    }

    /**
     * Return the URL for a resource found on the classpath at the Simple HTTP Resource Path. This
     * allows (for example) schema documents in jar files to be loaded from the classpath using
     * their canonical HTTP URLs.
     * 
     * @param location
     * @return the URL or null if not found
     */
    public static URL getClasspathResourceUrl(String location) {
        String path = getSimpleHttpResourcePath(location);
        if (path == null) {
            return null;
        } else {
            return AppSchemaResolver.class.getResource(path);
        }
    }

    /**
     * Return the string representation of URL for a resource found on the classpath at the Simple
     * HTTP Resource Path. This allows (for example) schema documents in jar files to be loaded from
     * the classpath using their canonical HTTP URLs.
     * 
     * @param location
     * @return the string representation of a classpath URL, or null if not found
     */
    public static String resolveClasspathLocation(String location) {
        URL url = getClasspathResourceUrl(location);
        if (url == null) {
            return null;
        } else {
            return getClasspathResourceUrl(location).toExternalForm();
        }
    }

}
