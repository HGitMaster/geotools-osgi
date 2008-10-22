/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.repository;

import java.net.URI;
import java.util.Map;


/**
 * This is the required addition on the part of a data provider. We also use
 * this interface internally, so look in this plugin for examples.
 *
 * @author David Zwiers, Refractions Research
 *
 * @since 0.6
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/ServiceFactory.java $
 */
public interface ServiceFactory {
    /**
     * Creates an IService based on the params provided. This may or may not
     * return a singleton, caching is optional. Error messages can be
     * retrieved using the getStatus and getMessage methods. It is important
     * to note that this method must inspect the url to determine if it can be
     * used to create the service. If it cannot, null must be returned.
     *
     * @param parent The catalog containing the service, may be null
     * @param id The sugested service id, should be generated when null.
     * @param params The set of connection params. These param values may
     *        either be parsed, or unparsed (String).
     *
     * @return the IService created, or null when a service cannot be created
     *         from these params.
     *
     * @see IService#getStatus()
     * @see IService#getMessage()
     */
    Service createService(Catalog parent, URI id, Map params);

    /**
     * Determines if the ServiceExtension can process the specified uri and use
     * it to create a set of connection paramters.
     *
     * @param uri The uri representing the service.
     *
     * @return true if the uri can be processed, otherwise false.
     */
    boolean canProcess(URI uri);

    /**
     * The primary intention is for drag 'n' drop. This generates a set of
     * params for the given URL ... in most cases this will be passed to the
     * createService method. It is important to note that this method must
     * inspect the url to determine if it can be used to create the service.
     * If it cannot, null must be returned.
     *
     * @param uri The potential source of params.
     *
     * @return Map of params to be used for creation, null if the URL cannot be
     *         used.
     */
    Map createParams(URI uri);
}
