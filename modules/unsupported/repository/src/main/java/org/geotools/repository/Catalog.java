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

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.geotools.util.ProgressListener;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Extension of Resolve which represents a local catalog or web registry
 * service.
 * <p>
 * Conceptually provides a searchable Catalog of "Spatial Data Sources".
 * Metadata search is abitrary.
 * </p>
 *
 * @author David Zwiers, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 * @since 0.7.0
 *
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/Catalog.java $
 */
public interface Catalog extends Resolve {
    /**
     * Will attempt to morph into the adaptee, and return that object. Required adaptions:
     * <ul>
     * <li>ICatalogInfo.class
     * <li>List.class <IService>
     * </ul>
     * May Block.
     *
     * @param adaptee
     * @param monitor May Be Null
     * @see CatalogInfo
     * @see IService
     */
    Object resolve(Class adaptee, ProgressListener monitor)
        throws IOException;

    /**
     * Adds the specified entry to this catalog. In some cases the catalog will be backed onto a
     * server, which may not allow for additions.
     * <p>
     * An IService may belong to more than one Catalog.
     * </p>
     *
     * @param service the Service to add to the catalog
     * @throws UnsupportedOperationException
     */
    void add(Service service) throws UnsupportedOperationException;

    /**
     * Removes the specified entry to this catalog. In some cases the catalog will be backed onto a
     * server, which may not allow for deletions.
     *
     * @param service
     * @throws UnsupportedOperationException
     */
    void remove(Service service) throws UnsupportedOperationException;

    /**
     * Replaces the specified entry in this catalog. In some cases the catalog will be backed onto a
     * server, which may not allow for deletions.
     *
     * @param id
     * @param service
     * @throws UnsupportedOperationException
     */
    void replace(URI id, Service service) throws UnsupportedOperationException;

    /**
     * Find resources matching this id directly from this Catalog.
     *
     * @param id used to match resolves
     * @param monitor used to show the progress of the find.
     *
     * @return List (possibly empty) of resolves (objects implementing the
     * Resolve interface)
     */
    List find(URI id, ProgressListener monitor);

    /**
     * Find Service matching this id directly from this Catalog.  This method is guaranteed to be non-blocking.
     *
     * @param query   a URI used to match resolves
     * @param monitor monitor used to watch progress
     *
     * @return a List (possibly empty) of matching services (objects of type
     * Service).
     */
    List findService(URI query, ProgressListener monitor);

    /**
     * Performs a search on this catalog based on the specified inputs.
     * <p>
     * The pattern uses the following conventions:
     * <ul>
     * <li>
     * <li> use " " to surround a phase
     * <li> use + to represent 'AND'
     * <li> use - to represent 'OR'
     * <li> use ! to represent 'NOT'
     * <li> use ( ) to designate scope
     * </ul>
     * The bbox provided shall be in Lat - Long, or null if the search is not to be contained within
     * a specified area.
     * </p>
     *
     * @param pattern Search pattern (see above)
     * @param bbox The bbox in Lat-Long (ESPG 4269), or null
     * @param monitor for progress, or null if monitoring is not desired
     *
     * @return List containg objects of type Resolve.
     */
    List search(String pattern, Envelope bbox, ProgressListener monitor)
        throws IOException;

    /**
     * Aquire info on this Catalog.
     * <p>
     * This is functionally equivalent to: <core>resolve(ICatalogInfo.class,monitor)</code>
     * </p>
     *
     * @see Catalog#resolve(Class, ProgressListener)
     * @return ICatalogInfo resolve(ICatalogInfo.class,ProgressListener monitor);
     */
    CatalogInfo getInfo(ProgressListener monitor) throws IOException;
}
