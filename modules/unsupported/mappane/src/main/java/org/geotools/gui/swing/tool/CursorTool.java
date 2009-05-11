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
import javax.swing.Icon;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.event.MapMouseAdapter;

/**
 * The base class for map pane cursor tools. Simply adds a getCursor
 * method to the MapToolAdapter
 * 
 * @author Michael Bedward
 * @since 2.6
 */
public abstract class CursorTool extends MapMouseAdapter {
    /**
     * Used with tool constructors to specify that the GUI control
     * (e.g. JButton) should not display an icon for this tool
     */
    public static final int NO_ICON = 0;

    /**
     * Used with tool constructors to specify that the GUI control
     * (e.g. JButton) display a 24x24 pixel icon for this tool
     */
    public static final int SMALL_ICON = 1;

    /**
     * Used with tool constructors to specify that the GUI control
     * (e.g. JButton) display a 32x32 pixel icon for this tool
     */
    public static final int LARGE_ICON = 2;

    protected JMapPane pane;

    /**
     * Set the map pane that this cursor tool is associated with
     * @param pane the map pane
     * @throws IllegalArgumentException if pane is null
     */
    public void setMapPane(JMapPane pane) {
        if (pane == null) {
            throw new IllegalArgumentException("map pane argument must not be null");
        }

        this.pane = pane;
    }

    /**
     * Get the name assigned to this tool (e.g. "Zoom in")
     */
    public abstract String getName();

    /**
     * Get the 32x32 pixel icon for this tool to be used with JButtons
     */
    public abstract Icon getIconLarge();

    /**
     * Get the 24x24 pixel icon for this tool to be used with JButtons
     */
    public abstract Icon getIconSmall();


    /**
     * Get the cursor for this tool
     */
    public abstract Cursor getCursor();
    
}
