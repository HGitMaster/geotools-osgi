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

import org.geotools.gui.swing.map.map2d.stream.event.MapEvent;

/**
 * Map2DListener used to listen to Map2D events
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/map/map2d/stream/listener/MapListener.java $
 */
public interface MapListener extends EventListener{

    /**
     * called when Rendering strategy change
     * @param mapEvent 
     */
    public void mapStrategyChanged(MapEvent mapEvent);
    
    /**
     * called when action state of the map changed
     * @param mapEvent 
     */
    public void mapActionStateChanged(MapEvent mapEvent);
    
}
