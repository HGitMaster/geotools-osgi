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
import org.geotools.gui.swing.event.MapMouseEvent;

/**
 * A convenience class that can be sub-classed by JMapPane cursor tool classes
 * that do not need to implement all of the mouse event methods defined in
 * MapTool.
 * 
 * @author Michael Bedward
 * @since 2.6
 */
public abstract class MapToolAdapter implements MapTool {

    /**
     * Get the name assigned to this tool (e.g. "Zoom in")
     */
    public abstract String getName();
    
    /**
     * Set the JMapPane instance for this tool
     */
    public abstract void setMapPane(JMapPane pane);
    
    /**
     * Get the 32x32 pixel icon for this tool to be used with JButtons
     */
    public abstract Icon getIconLarge();
    
    /**
     * Get the 24x24 pixel icon for this tool to be used with JButtons
     */
    public abstract Icon getIconSmall();
    
    /**
     * Respond to a mouse click event received from the map pane.
     * Empty method.
     */
    public void onMouseClicked(MapMouseEvent pme) {
    }

    /**
     * Respond to a mouse dragged event received from the map pane.
     * Empty method.
     */
    public void onMouseDragged(MapMouseEvent pme) {
    }

    /**
     * Respond to a mouse entered event received from the map pane.
     * Empty method.
     */
    public void onMouseEntered(MapMouseEvent pme) {
    }

    /**
     * Respond to a mouse exited event received from the map pane.
     * Empty method.
     */
    public void onMouseExited(MapMouseEvent pme) {
    }

    /**
     * Respond to a mouse moved event received from the map pane.
     * Empty method.
     */
    public void onMouseMoved(MapMouseEvent pme) {
    }

    /**
     * Respond to a mouse button pressed event received from the map pane.
     * Empty method.
     */
    public void onMousePressed(MapMouseEvent pme) {
    }

    /**
     * Respond to a mouse button released event received from the map pane.
     * Empty method.
     */
    public void onMouseReleased(MapMouseEvent pme) {
    }

    /**
     * Respond to a mouse wheel event received from the map pane.
     * Empty method.
     */
    public void onMouseWheelMoved(MapMouseEvent pme) {
    }

}
