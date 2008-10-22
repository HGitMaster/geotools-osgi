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

import org.geotools.gui.swing.map.map2d.stream.event.StrategyEvent;

/**
 * StrategyListener used to listen to RenderingStrategy events 
 * 
 * @author Johann Sorel
 */
public interface StrategyListener extends EventListener{

    /**
     * set the actual state of the strategy
     * @param rendering : true if the strategy strat working, false when it stops
     */
    public void setRendering(boolean rendering);    
    /**
     * called when Map2d MapArea changed
     * @param event : RenderingStrategyEvent
     */
    public void mapAreaChanged(StrategyEvent event);    
    /**
     * called when MapContext changed
     * @param event : RenderingStrategyEvent
     */
    public void mapContextChanged(StrategyEvent event);
    
}
