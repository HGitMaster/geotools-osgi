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
 * An adapter class that implements all of the mouse event handling methods
 * defined in the MapMouseListener interface as empty methods, allowing sub-classes
 * to just override the methods they need. Note that this class is abstract and
 * sub-classes are required to implement
 * {@linkplain MapMouseListener#setMapPane(org.geotools.gui.swing.JMapPane) }.
 *
 */
public class MapMouseAdapter implements MapMouseListener {

//    private JMapPane pane;


    /**
     * Set the JMapPane instance for this tool
     */
    /*public void setMapPane(JMapPane pane) {
        this.pane = pane;
    }*/

    /**
     * Respond to a mouse click event received from the map pane
     */
    public void onMouseClicked(MapMouseEvent ev) {}

    /**
     * Respond to a mouse dragged event received from the map pane
     */
    public void onMouseDragged(MapMouseEvent ev) {}

    /**
     * Respond to a mouse entered event received from the map pane
     */
    public void onMouseEntered(MapMouseEvent ev) {}

    /**
     * Respond to a mouse exited event received from the map pane
     */
    public void onMouseExited(MapMouseEvent ev) {}

    /**
     * Respond to a mouse movement event received from the map pane
     */
    public void onMouseMoved(MapMouseEvent ev) {}

    /**
     * Respond to a mouse button press event received from the map pane
     */
    public void onMousePressed(MapMouseEvent ev) {}

    /**
     * Respond to a mouse button release event received from the map pane
     */
    public void onMouseReleased(MapMouseEvent ev) {}

    /**
     * Respond to a mouse wheel scroll event received from the map pane
     */
    public void onMouseWheelMoved(MapMouseEvent ev) {}

}
