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

package org.geotools.gui.swing.map.map2d.stream.listener;

import java.util.EventListener;

import org.geotools.gui.swing.map.map2d.stream.event.NavigationEvent;

/**
 * NavigableMap2DListener used to listen to Map2D Navigation events 
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/map/map2d/stream/listener/NavigationListener.java $
 */
public interface NavigationListener extends EventListener{
    
    /**
     * called when the navigation handler change
     * @param mce : Map2DNavigationEvent
     */
    public void navigationHandlerChanged(NavigationEvent mce);
    
    
}
