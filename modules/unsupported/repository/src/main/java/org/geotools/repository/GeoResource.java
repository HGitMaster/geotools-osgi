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

import org.geotools.util.ProgressListener;


/**
 * Represents a handle to a spatial resource.
 *
 * <p>
 * The resource is not guaranteed to exist, nor do we guarantee that we can
 * connect with the resource. Some/All potions of this handle may be loaded as
 * required. This resource handle may also be the result a metadata service
 * query.
 * </p>
 *
 * @author David Zwiers, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 *
 * @since 0.6
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/GeoResource.java $
 */
public interface GeoResource extends Resolve {
    /**
     * Blocking operation to resolve into the adaptee, if available.
     *
     * <p>
     * Required adaptions:
     *
     * <ul>
     * <li>
     * IGeoResourceInfo.class
     * </li>
     * <li>
     * IService.class
     * </li>
     * </ul>
     * </p>
     *
     * <p>
     * Example (no casting required):
     * <pre><code>
     * IGeoResourceInfo info = resovle(IGeoResourceInfo.class);
     * </code></pre>
     * </p>
     *
     * <p>
     * Recommendated adaptions:
     *
     * <ul>
     * <li>
     * ImageDescriptor.class (for icon provided by external service)
     * </li>
     * </ul>
     * </p>
     *
     * @param adaptee
     * @param monitor
     *
     * @return instance of adaptee, or null if unavailable (IGeoResourceInfo
     *         and IService must be supported)
     *
     * @throws IOException DOCUMENT ME!
     *
     * @see GeoResourceInfo
     * @see IService
     * @see IResolve#resolve(Class, ProgressListener)
     */
    Object resolve(Class adaptee, ProgressListener monitor)
        throws IOException;

    /**
     * Blocking operation to describe this service.
     *
     * <p>
     * As an example this method is used by LabelDecorators to aquire title,
     * and icon.
     * </p>
     *
     * @param monitor DOCUMENT ME!
     *
     * @return IGeoResourceInfo resolve(IGeoResourceInfo.class,ProgressListener
     *         monitor);
     *
     * @throws IOException DOCUMENT ME!
     *
     * @see AbstractGeoResource#resolve(Class, ProgressListener)
     */
    GeoResourceInfo getInfo(ProgressListener monitor) throws IOException;
}
