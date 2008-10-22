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

import org.geotools.gui.swing.map.map2d.stream.event.SelectionEvent;

/**
 * SelectableMap2DListener used to listen to Map2D Selection events 
 * 
 * @author Johann Sorel
 */
public interface SelectionListener extends EventListener{

    /**
     * called when the map2D selection changed
     * @param event : Map2DSelectionEvent
     */
    public void selectionChanged(SelectionEvent event);
    
    /**
     * called when filter changed
     * @param event : Map2DSelectionEvent
     */
    public void selectionFilterChanged(SelectionEvent event);
    
    /**
     * called when selection handler changed
     * @param event : Map2DSelectionEvent
     */
    public void selectionHandlerChanged(SelectionEvent event);
    
}
