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


/**
 * Represents a bean style metadata accessor for metadata about a catalog. This
 * may be the result of a request to a metadata service. All methods within an
 * implementation of this interface should NOT block. Much of this is based on
 * Dublin Core and the RDF application profile.
 *
 * @author David Zwiers, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 *
 * @since 0.6
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/CatalogInfo.java $
 */
public interface CatalogInfo {
    /**
     * returns the catalog title May Not Block.
     *
     */
    String getTitle();

    /**
     * returns the keywords assocaited with this catalog May Not Block. Maps to
     * Dublin Core's Subject element
     *
     */
    String[] getKeywords();

    /**
     * returns the catalog description.
     *
     */
    String getDescription();

    /**
     * Returns the catalog source. May Not Block. Maps to the Dublin Core
     * Server Element
     *
     */
    URI getSource();
}
