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

package org.geotools.gui.swing.event;

import org.geotools.gui.swing.JMapPane;

/**
 * Interface for classes that listen to JMapPaneMouseEvents
 */
public interface JMapPaneMouseListener {
    
    /**
     * Set the JMapPane instance for this tool
     */
    public void setMapPane(JMapPane pane);
    
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
