/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

import javax.swing.Icon;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.event.JMapPaneMouseEvent;

/**
 * The interface for tools working with a JMapPane
 *
 */
public interface JMapPaneTool {
    
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
    
    /**
     * Get the name assigned to this tool (e.g. "Zoom in")
     */
    public String getName();

    /**
     * Set the JMapPane instance for this tool
     */
    public void setMapPane(JMapPane pane);

    /**
     * Get the 32x32 pixel icon for this tool to be used with JButtons
     */
    public Icon getIconLarge();
    
    /**
     * Get the 24x24 pixel icon for this tool to be used with JButtons
     */
    public Icon getIconSmall();
    
    /**
     * Respond to a mouse click event received from the map pane
     */
    public void onMouseClicked(JMapPaneMouseEvent pme);

    /**
     * Respond to a mouse dragged event received from the map pane
     */
    public void onMouseDragged(JMapPaneMouseEvent pme);

    /**
     * Respond to a mouse entered event received from the map pane
     */
    public void onMouseEntered(JMapPaneMouseEvent pme);

    /**
     * Respond to a mouse exited event received from the map pane
     */
    public void onMouseExited(JMapPaneMouseEvent pme);

    /**
     * Respond to a mouse movement event received from the map pane
     */
    public void onMouseMoved(JMapPaneMouseEvent pme);

    /**
     * Respond to a mouse button press event received from the map pane
     */
    public void onMousePressed(JMapPaneMouseEvent pme);

    /**
     * Respond to a mouse button release event received from the map pane
     */
    public void onMouseReleased(JMapPaneMouseEvent pme);

    /**
     * Respond to a mouse wheel scroll event received from the map pane
     */
    public void onMouseWheelMoved(JMapPaneMouseEvent pme);

}
