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
package org.geotools.gui.swing.map.map2d.stream;

import org.geotools.gui.swing.map.map2d.stream.handler.NavigationHandler;
import org.geotools.gui.swing.map.map2d.stream.listener.NavigationListener;

/**
 * interface for map2d widget how handle Navigation
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/map/map2d/stream/NavigableMap2D.java $
 */
public interface NavigableMap2D extends StreamingMap2D{
        
    /**
     * get to the previous maparea is there was one
     */
    public void previousMapArea();    
    /**
     * get to the next maparea is there is one
     */
    public void nextMapArea();
    
    /**
     * the NavigationHandler is managing the selection decoration and the related listeners.
     * @param handler
     */
    public void setNavigationHandler(NavigationHandler handler);    
    /**
     * 
     * @return NavigationHandler
     */
    public NavigationHandler getNavigationHandler();
    
      
    /**
     * add a NavigableMap2DListener
     * @param listener : NavigableMap2DListener to add
     */
    public void addNavigableMap2DListener(NavigationListener listener);    
    /**
     * remove a NavigableMap2DListener
     * @param listener : NavigableMap2DListener to remove
     */
    public void removeNavigableMap2DListener(NavigationListener listener);
    /**
     * get an array of NavigableMap2DListener
     * @return array of NavigableMap2DListener
     */
    public NavigationListener[] getNavigableMap2DListeners();
    
}
