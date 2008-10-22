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
import java.util.List;
import java.util.Map;


/**
 * Builds service proxies or clones (with an id).
 * <p>
 * Where not specified, sensible defaults will be added to the create options. aka Magic will occur
 * here :-)
 * </p>
 *
 * @author David Zwiers, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/ServiceFinder.java $
 */
public interface ServiceFinder {
    /**
     * This will create a new IService magically. In some cases sensible default parameters may be
     * added, in addition to parameters removed. An ID will be generated.
     *
     * @param params
     * @return List<Service>
     */
    List aquire(Map params); // may look up authentication

    /**
     * This method generates a default set of params, and calls aquire(params).
     *
     * @param target
     * @return List<IService>
     * @see #aquire(params)
     */
    List aquire(URI target); // creates a map, may look up authentication

    /**
     * This methos is intended to be used when replacing an IService entry in a catalog, or for
     * cloning. This allows you to retain the URI id, while providing new parameters. This is also
     * intended for persistence frameworks to use. WARNING: This may have undesired
     * results/conflicts when added to a ICatalog if care is not taken when using this method.
     *
     * @param id
     * @param params
     * @return List<IService>
     */
    List aquire(URI id, Map params); // may not look up
                                     // authentication
}
