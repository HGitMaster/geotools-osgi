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

import java.util.EventListener;


/**
 * Allows client code to notice catalog changes.
 *
 * <p>
 * You can use various visitors to explore the changes
 * </p>
 *
 * @author David Zwiers, Refractions Research
 *
 * @since 0.6
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/ResolveChangeListener.java $
 */
public interface ResolveChangeListener extends EventListener {
    /**
     * Notifies this listener that some changes are happening, or have already
     * happened.
     *
     * <p>
     * The supplied event gives details. This event object (and the resource
     * delta within it) is valid only for the duration of the invocation of
     * this method.
     * </p>
     *
     * <p>
     * Note that during resource change event notification, further changes to
     * resources may be disallowed.
     * </p>
     *
     * <p>
     * Note that this method is not guaranteed to execute in the UI thread, so
     * UI work must be performed with Display.getDefault().asyncExec();
     * </p>
     *
     * @param event the resource change event
     *
     * @see IResourceDelta
     */
    void changed(ResolveChangeEvent event);
}
