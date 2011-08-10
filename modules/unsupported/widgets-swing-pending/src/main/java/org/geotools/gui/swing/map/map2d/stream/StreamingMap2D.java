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

import org.geotools.gui.swing.map.map2d.*;
import org.geotools.gui.swing.map.map2d.stream.listener.MapListener;
import org.geotools.gui.swing.map.map2d.stream.strategy.StreamingStrategy;

/**
 * Map2D interface, used for mapcontext viewing
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/map/map2d/stream/StreamingMap2D.java $
 */
public interface StreamingMap2D extends Map2D{
       
    /**
     * Possible actions states available for a map
     */
    public static enum ACTION_STATE{
        NAVIGATE,
        SELECT,
        EDIT,
        NONE
    };
    
    
    /**
     * set the rendering strategy
     * @param strategy : throw nullpointexception if strategy is null
     */
    public void setRenderingStrategy(StreamingStrategy strategy);    
    /**
     * get the map2d rendering strategy
     * @return RenderingStrategy : should never return null;
     */
    public StreamingStrategy getRenderingStrategy();
        
    /**
     * add a Map2DListener
     * @param listener : Map2Dlistener to add
     */
    public void addMap2DListener(MapListener listener);        
    /**
     * remove a Map2DListener
     * @param listener : Map2DListener to remove
     */
    public void removeMap2DListener(MapListener listener);    
    /**
     * 
     * @return array of Map2DListener
     */
    public MapListener[] getMap2DListeners();
    
    //------------------Action State--------------------------------------------    
    /**
     * set the action state. Pan, ZoomIn, ZoomOut ...
     * @param state : MapConstants.ACTION_STATE
     */
    public void setActionState(ACTION_STATE state);    
    /**
     * get the actual action state
     * @return MapConstants.ACTION_STATE
     */
    public ACTION_STATE getActionState();
            
        
}
