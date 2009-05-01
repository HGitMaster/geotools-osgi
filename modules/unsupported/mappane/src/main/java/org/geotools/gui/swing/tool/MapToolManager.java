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

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.MouseInputListener;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.event.MapMouseEvent;
import org.geotools.gui.swing.event.MapMouseListener;

/**
 * Receives mouse events from a JMapPane instance, converts them to
 * JMapPaneMouseEvents, and sends these to the active map pane 
 * tools.
 * 
 * @author Michael Bedward
 * @since 2.6
 */
public class MapToolManager implements MouseInputListener, MouseWheelListener {

    private JMapPane pane;
    private Set<MapMouseListener> listeners = new HashSet<MapMouseListener>();
    private CursorTool cursorTool;

    /**
     * Constructor
     * 
     * @param pane the map pane that owns this listener
     */
    public MapToolManager(JMapPane pane) {
        this.pane = pane;
    }

    /**
     * Unset the current cursor tool
     */
    public void setNoCursorTool() {
        listeners.remove(cursorTool);
        cursorTool = null;
    }

    /**
     * Set the active cursor tool
     * 
     * @param tool the tool to set
     * @return true if successful; false otherwise
     * @throws IllegalArgumentException if the tool argument is null
     */
    public boolean setCursorTool(CursorTool tool) {
        if (tool == null) {
            throw new IllegalArgumentException("argument must not be null");
        }

        if (cursorTool != null) {
            listeners.remove(cursorTool);
        }

        cursorTool = tool;
        return listeners.add(tool);
    }

    /**
     * Add a listener for JMapPaneMouseEvents
     *
     * @return true if successful; false otherwise
     * @throws IllegalArgumentException if the tool argument is null
     */
    public boolean addMouseListener(MapMouseListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("argument must not be null");
        }

        return listeners.add(listener);
    }

    /**
     * Remove a MapMouseListener from the active listeners
     *
     * @return true if successful; false otherwise
     * @throws IllegalArgumentException if the tool argument is null
     */
    public boolean removeMouseListener(MapMouseListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("argument must not be null");
        }

        return listeners.remove(listener);
    }

    public void mouseClicked(MouseEvent e) {
        MapMouseEvent pme = convertEvent(e);
        if (pme != null) {
            for (MapMouseListener listener : listeners) {
                listener.onMouseClicked(pme);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        MapMouseEvent pme = convertEvent(e);
        if (pme != null) {
            for (MapMouseListener listener : listeners) {
                listener.onMousePressed(pme);
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        MapMouseEvent pme = convertEvent(e);
        if (pme != null) {
            for (MapMouseListener listener : listeners) {
                listener.onMouseReleased(pme);
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
        MapMouseEvent pme = convertEvent(e);
        if (pme != null) {
            for (MapMouseListener listener : listeners) {
                listener.onMouseEntered(pme);
            }
        }
    }

    public void mouseExited(MouseEvent e) {
        MapMouseEvent pme = convertEvent(e);
        if (pme != null) {
            for (MapMouseListener listener : listeners) {
                listener.onMouseExited(pme);
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        MapMouseEvent pme = convertEvent(e);
        if (pme != null) {
            for (MapMouseListener listener : listeners) {
                listener.onMouseDragged(pme);
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        MapMouseEvent pme = convertEvent(e);
        if (pme != null) {
            for (MapMouseListener listener : listeners) {
                listener.onMouseMoved(pme);
            }
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        MapMouseEvent pme = convertEvent(e);
        if (pme != null) {
            for (MapMouseListener listener : listeners) {
                listener.onMouseWheelMoved(pme);
            }
        }
    }

    private MapMouseEvent convertEvent(MouseEvent e) {
        MapMouseEvent pme = null;
        if (pane.getScreenToWorldTransform() != null) {
            pme = new MapMouseEvent(pane, e);
        }

        return pme;
    }

    private MapMouseEvent convertEvent(MouseWheelEvent e) {
        MapMouseEvent pme = null;
        if (pane.getScreenToWorldTransform() != null) {
            pme = new MapMouseEvent(pane, e);
        }

        return pme;
    }
}
