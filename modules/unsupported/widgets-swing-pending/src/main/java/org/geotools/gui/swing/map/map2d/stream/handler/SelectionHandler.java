/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing.map.map2d.stream.handler;

import javax.swing.ImageIcon;

import org.geotools.gui.swing.map.map2d.stream.SelectableMap2D;

/**
 * selection handler
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/map/map2d/stream/handler/SelectionHandler.java $
 */
public interface SelectionHandler {

        
    /**
     * 
     * @param map2d
     * @param mapComponent
     */
    void install(SelectableMap2D map2d);
    
    /**
     * 
     */
    void uninstall();
    
    /**
     * 
     * @return
     */
    boolean isInstalled();
    
    String getTitle();
    
    ImageIcon getIcon();
    
}
