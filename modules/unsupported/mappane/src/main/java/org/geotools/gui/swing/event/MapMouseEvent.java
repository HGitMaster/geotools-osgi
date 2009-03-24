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

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.gui.swing.JMapPane;

/**
 * A MouseEvent which contains methods to obtain coordinates in real world
 * CoordinateSystem as well as Screen Coordinates.
 *
 * @author Michael Bedward (adapted from code by Cameron Shorter)
 * @since 2.6
 */
public final class MapMouseEvent extends MouseEvent {
    
    private DirectPosition2D geoCoords;
    private boolean isWheelEvent;
    private int wheelAmount;
    

    /**
     * Constructor. Calculates the map position of the mouse event.
     * 
     * @param pane the map pane sending this event
     * @param event the mouse event
     */
    public MapMouseEvent(JMapPane pane, MouseEvent event) {
        super(event.getComponent(),
            event.getID(),
            event.getWhen(),
            event.getModifiers(),
            event.getX(),
            event.getY(),
            event.getClickCount(),
            event.isPopupTrigger(),
            event.getButton());

        isWheelEvent = false;
        wheelAmount = 0;
        
        AffineTransform tr = pane.getScreenToWorldTransform();
        geoCoords = new DirectPosition2D(event.getX(), event.getY());
        tr.transform(geoCoords, geoCoords);
    }
    
    /**
     * Constructor for mouse wheel events.
     * 
     * @todo do we need to calculate map position for a mouse wheel
     * event ?
     * 
     * @param pane
     * @param event
     */
    public MapMouseEvent(JMapPane pane, MouseWheelEvent event) {
        super(event.getComponent(),
            event.getID(),
            event.getWhen(),
            event.getModifiers(),
            event.getX(),
            event.getY(),
            event.getClickCount(),
            event.isPopupTrigger());
        
        isWheelEvent = true;
        wheelAmount = event.getWheelRotation();
    }

    /**
     * Get the position, in map (world) coordinates of this mouse event
     * @return
     */
    public DirectPosition2D getMapPosition() {
        return new DirectPosition2D(geoCoords.x, geoCoords.y);
    }
}
