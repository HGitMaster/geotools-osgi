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
import java.util.List;
import java.util.Map;

import org.geotools.util.ProgressListener;


/**
 * Represents a geo spatial service handle. Follows the same design as IResource.
 * <p>
 * Represents a spatial service, which may be lazily loaded. The existance of this object does not
 * ensure that the advertized data is guaranteed to exist, nor does this interface guarantee that
 * the service exists based on this object's existance. We should also note the resource management
 * is left to the user, and that resolve() is not guaranteed to return the same instance object from
 * two subsequent calls, but may. This is merely a handle to some information about a service, and a
 * method of aquiring an instance of the service ...
 * </p>
 * <p>
 * NOTE: This may be the result of communications with a metadata service, and as such this service
 * handle may not have been validated yet. Remember to check the service status.
 * </p>
 *
 * @author David Zwiers, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 * @since 0.6
 * @see ServiceInfo
 * @see ServiceFinder
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/Service.java $
 */
public interface Service extends Resolve {
    /**
     * Return list of IGeoResources managed by this service. This method must
     * return the same result as the following:
     *
     * <pre>
     *   <code>
     *   (List)resolve(List.class,monitor);
     *   </code>
     * </pre>
     * <p>
     * Many file based serivces will just contain a single IGeoResource.
     * </p>
     *
     * @return A list of type GeoResource.
     */
    List members(ProgressListener monitor) throws IOException;

    /**
     * Will attempt to morph into the adaptee, and return that object.
     * Required adaptions:
     * <ul>
     * <li>IServiceInfo.class
     * <li>List.class <IGeoResource>
     * </ul>
     * May Block.
     *
     * @param adaptee
     * @param monitor
     * @return instance of adaptee, or null if unavailable (IServiceInfo and List<IGeoResource>
     *         must be supported)
     * @see ServiceInfo
     * @see GeoResource
     * @see IResolve#resolve(Class, ProgressListener)
     */
    Object resolve(Class adaptee, ProgressListener monitor)
        throws IOException;

    /**
     * Accessor to the set of params used to create this entry. There is no guarantee that these
     * params created a usable service (@see getStatus() ). These params may have been modified
     * within the factory during creation. This method is intended to be used for cloning (@see
     * IServiceFactory) or for persistence between sessions.
     *
     * @see ServiceFinder
     *
     * @return A map with key of type String, and value of type Serializable.
     */
    Map getConnectionParams();

    /**
     * @return IServiceInfo resolve(IServiceInfo.class,ProgressListener monitor);
     * @see IService#resolve(Class, ProgressListener)
     */
    ServiceInfo getInfo(ProgressListener monitor) throws IOException;
}
