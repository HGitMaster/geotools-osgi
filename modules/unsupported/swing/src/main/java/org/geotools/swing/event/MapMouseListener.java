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

package org.geotools.swing.event;

/**
 * Interface for classes that listen to JMapPaneMouseEvents
 *
 * @author Michael Bedward
 * @since 2.6
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/swing/src/main/java/org/geotools/swing/event/MapMouseListener.java $
 * @version $Id: MapMouseListener.java 34138 2009-10-13 06:35:19Z mbedward $
 */
public interface MapMouseListener {
    
    /**
     * Set the JMapPane instance for this tool
     */
    //public void setMapPane(JMapPane pane);
    
    /**
     * Respond to a mouse click event received from the map pane
     *
     * @param ev the mouse event
     */
    public void onMouseClicked(MapMouseEvent ev);

    /**
     * Respond to a mouse dragged event received from the map pane
     *
     * @param ev the mouse event
     */
    public void onMouseDragged(MapMouseEvent ev);

    /**
     * Respond to a mouse entered event received from the map pane
     *
     * @param ev the mouse event
     */
    public void onMouseEntered(MapMouseEvent ev);

    /**
     * Respond to a mouse exited event received from the map pane
     *
     * @param ev the mouse event
     */
    public void onMouseExited(MapMouseEvent ev);

    /**
     * Respond to a mouse movement event received from the map pane
     *
     * @param ev the mouse event
     */
    public void onMouseMoved(MapMouseEvent ev);

    /**
     * Respond to a mouse button press event received from the map pane
     *
     * @param ev the mouse event
     */
    public void onMousePressed(MapMouseEvent ev);

    /**
     * Respond to a mouse button release event received from the map pane
     *
     * @param ev the mouse event
     */
    public void onMouseReleased(MapMouseEvent ev);

    /**
     * Respond to a mouse wheel scroll event received from the map pane
     *
     * @param ev the mouse event
     */
    public void onMouseWheelMoved(MapMouseEvent ev);

}
