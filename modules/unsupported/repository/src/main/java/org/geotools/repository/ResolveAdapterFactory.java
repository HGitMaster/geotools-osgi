/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
 * Adapts a resolve handle into another type of object.
 *
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public interface ResolveAdapterFactory {
    /**
     * Determines if a perticular adaptation is supported.
     *
     * @param resolve The handle being adapted.
     * @param adapter The adapting class.
     *
     * @return True if supported, otherwise false.
     */
    boolean canAdapt(Resolve resolve, Class adapter);

    /**
     * Performs an adaptation to a particular adapter.
     *
     * @param resolve The handle being adapted.
     * @param adapter The adapting class.
     * @param monitor Progress monitor for blocking class.
     *
     * @return The adapter, or null if adapation not possible.
     *
     * @throws IOException Any I/O errors that occur.
     */
    Object adapt(Resolve resolve, Class adapter, ProgressListener monitor)
        throws IOException;
}
