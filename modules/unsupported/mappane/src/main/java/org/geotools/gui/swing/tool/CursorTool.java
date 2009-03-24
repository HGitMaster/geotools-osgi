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

package org.geotools.gui.swing.tool;

import java.awt.Cursor;

/**
 * The base class for map pane cursor tools. Simply adds a getCursor
 * method to the MapToolAdapter
 * 
 * @author Michael Bedward
 * @since 2.6
 */
public abstract class CursorTool extends MapToolAdapter {

    /**
     * Get the cursor for this tool
     */
    public abstract Cursor getCursor();
    
}
