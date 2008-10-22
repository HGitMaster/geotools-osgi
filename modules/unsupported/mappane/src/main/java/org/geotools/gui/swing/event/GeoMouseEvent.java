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

//import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

//import org.geotools.ct.MathTransform;
import org.opengis.referencing.operation.MathTransform;

// OpenGIS dependencies
import org.opengis.referencing.operation.TransformException;

//import org.geotools.pt.CoordinatePoint;
import org.geotools.geometry.GeneralDirectPosition;

//import org.geotools.cs.CoordinateSystem;
import org.geotools.referencing.crs.AbstractCRS;


/**
 * A MouseEvent which contains methods to obtain coordinates in real world
 * CoordinateSystem as well as Screen Coordinates.
 * All {@link MouseListener}s that have registered for
 * {@link org.geotools.gui.swing.MapPaneImpl} mouseEvents will receive
 * events of this class.
 * Listeners implementations can implements their code as below:
 *
 * <blockquote><pre>
 * &nbsp;public void mouseClicked(MouseEvent e) {
 * &nbsp;    GeoMouseEvent event = (GeoMouseEvent) e;
 * &nbsp;    // Process event here...
 * &nbsp;}
 * </pre></blockquote>
 *
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/mappane/src/main/java/org/geotools/gui/swing/event/GeoMouseEvent.java $
 * @version $Id: GeoMouseEvent.java 30701 2008-06-13 14:51:15Z acuster $
 * @author Cameron Shorter
 */
public final class GeoMouseEvent extends MouseEvent {
    /**
     * The transform which will convert screenCoordinates
     * to CoordinateSystem coordinates.
     */
    final MathTransform transform;

    /**
     * The coordinate system for ({@link #x},{@link #y}) or <code>null</code>
     * if the coordinate has not yet been computed. This coordinate system
     * must be two-dimensional.
     */

    //private transient AbstractCRS coordinateSystem;

    /**
     * A mouseClick event which also contains methods to transform from
     * pixels to the Coordinate System of the Renderer.
     * @param event    The original mouse event.
     * @param transform The transform which will convert screenCoordinates
     * to CoordinateSystem coordinates.
     */
    public GeoMouseEvent(final MouseEvent event, final MathTransform transform) {
        super(event.getComponent(), // the Component that originated the event
            event.getID(), // the integer that identifies the event
            event.getWhen(), // a long int that gives the time the
                             // event occurred
            event.getModifiers(), // the modifier keys down during event
                                  // (shift, ctrl, alt, meta)
            event.getX(), // the horizontal x coordinate for the
                          // mouse location
            event.getY(), // the vertical y coordinate for the mouse
                          // location
            event.getClickCount(), // the number of mouse clicks associated
                                   // with event
            event.isPopupTrigger(), // a boolean, true if this event is a
                                    // trigger for a popup-menu
            event.getButton()); // which of the mouse buttons has changed
                                // state (JDK 1.4 only).

        this.transform = transform;
    }

    /**
     * Returns the "real world" mouse's position. The coordinates are expressed
     * in Context's CoordinateSystem.
     *
     * @param  dest A pre-allocated variable to store the mouse's location
     * in CoordinateSystems, can be set to <code>null</code>.
     * @return The mouse's location in CoordinateSystem coordinates.
     * @throws TransformException when transform is invalid.
     */
    public GeneralDirectPosition getMapCoordinate(GeneralDirectPosition dest)
        throws TransformException {
        if (dest == null) {
            dest = new GeneralDirectPosition(getX(), getY());
        } else {
            dest.setLocation(new Point2D.Double(getX(), getY()));
        }

        transform.transform(dest, dest);

        return dest;
    }
}
