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

import org.geotools.gui.swing.map.map2d.stream.event.EditionEvent;

/**
 * EditableMap2DListener used to listen to Map2D edition events
 * 
 * @author Johann Sorel
 */
public interface EditionListener extends EventListener{
    
    /**
     * called when the edited layer change
     * @param event : Map2DEditionEvent
     */
    public void editedLayerChanged(EditionEvent event);
    
    /**
     * called when edition handler change
     * @param event : Map2DEditionEvent
     */
    public void editionHandlerChanged(EditionEvent event);
    
}
