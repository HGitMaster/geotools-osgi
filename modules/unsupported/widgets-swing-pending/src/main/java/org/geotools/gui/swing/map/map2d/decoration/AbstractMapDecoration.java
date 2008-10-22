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
package org.geotools.gui.swing.map.map2d.decoration;

import org.geotools.gui.swing.map.map2d.Map2D;

/**
 * Abstract implementation of MapDecoration, handle the 
 * getMap2D and setMap2D methods.
 * 
 * @author Johann Sorel
 */
public abstract class AbstractMapDecoration implements MapDecoration{

    protected Map2D map = null;
    
    public void setMap2D(Map2D map) {
        this.map = map;
    }

    public Map2D getMap2D() {
        return map;
    }

    public void dispose() {
    }
    

}
