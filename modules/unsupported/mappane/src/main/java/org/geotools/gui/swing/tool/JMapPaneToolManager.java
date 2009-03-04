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
import org.geotools.gui.swing.event.JMapPaneMouseEvent;
import org.geotools.gui.swing.event.JMapPaneMouseListener;

/**
 * Receives mouse events from a JMapPane instance, converts them to
 * JMapPaneMouseEvents, and sends these to the active map pane 
 * tools.
 * 
 * @author Michael Bedward
 * @since 2.6
 */
public class JMapPaneToolManager implements MouseInputListener, MouseWheelListener {
    
    private JMapPane pane;
    private Set<JMapPaneMouseListener> listeners = new HashSet<JMapPaneMouseListener>();
    private JMapPaneCursorTool cursorTool;
    
    /**
     * Constructor
     * 
     * @param pane the map pane that owns this listener
     */
    public JMapPaneToolManager(JMapPane pane) {
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
    public boolean setCursorTool(JMapPaneCursorTool tool) {
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
     * Add a tool to the set of active tools
     * 
     * @param tool tool to add
     * @return true if successful; false otherwise
     * @throws IllegalArgumentException if the tool argument is null
     */
    public boolean addTool(JMapPaneTool tool) {
        if (tool == null) {
            throw new IllegalArgumentException("argument must not be null");
        }
        
        return listeners.add(tool);
    }
    
    /**
     * Remove a tool from the set of active tools. It is safe to
     * call this method speculatively since it just returns false
     * if the tool is not in the active set.
     * 
     * @param tool tool to remove
     * 
     * @return true if successful; false otherwise
     * @throws IllegalArgumentException if the tool argument is null
     */
    public boolean removeTool(JMapPaneTool tool) {
        if (tool == null) {
            throw new IllegalArgumentException("argument must not be null");
        }
        
        return listeners.remove(tool);
    }

    /**
     * Add a listener for JMapPaneMouseEvents
     */
    public void addMouseListener(JMapPaneMouseListener listener) {
    }
    
    public void mouseClicked(MouseEvent e) {
        JMapPaneMouseEvent pme = convertEvent(e);
        if (pme != null) {
            for (JMapPaneMouseListener listener : listeners) {
                listener.onMouseClicked(pme);
            }
        }
    }
    
    public void mousePressed(MouseEvent e) {
        JMapPaneMouseEvent pme = convertEvent(e);
        for (JMapPaneMouseListener listener : listeners) {
            listener.onMousePressed(pme);
        }
    }

    public void mouseReleased(MouseEvent e) {
        JMapPaneMouseEvent pme = convertEvent(e);
        for (JMapPaneMouseListener listener : listeners) {
            listener.onMouseReleased(pme);
        }
    }

    public void mouseEntered(MouseEvent e) {
        JMapPaneMouseEvent pme = convertEvent(e);
        for (JMapPaneMouseListener listener : listeners) {
            listener.onMouseEntered(pme);
        }
    }

    public void mouseExited(MouseEvent e) {
        JMapPaneMouseEvent pme = convertEvent(e);
        for (JMapPaneMouseListener listener : listeners) {
            listener.onMouseExited(pme);
        }
    }

    public void mouseDragged(MouseEvent e) {
        JMapPaneMouseEvent pme = convertEvent(e);
        for (JMapPaneMouseListener listener : listeners) {
            listener.onMouseDragged(pme);
        }
    }

    public void mouseMoved(MouseEvent e) {
        JMapPaneMouseEvent pme = convertEvent(e);
        for (JMapPaneMouseListener listener : listeners) {
            listener.onMouseMoved(pme);
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        JMapPaneMouseEvent pme = convertEvent(e);
        for (JMapPaneMouseListener listener : listeners) {
            listener.onMouseWheelMoved(pme);
        }
    }

    private JMapPaneMouseEvent convertEvent(MouseEvent e) {
        JMapPaneMouseEvent pme = null;
        if (pane.getScreenToWorldTransform() != null) {
            pme = new JMapPaneMouseEvent(pane, e);
        }
        
        return pme;
    }

    private JMapPaneMouseEvent convertEvent(MouseWheelEvent e) {
        JMapPaneMouseEvent pme = null;
        if (pane.getScreenToWorldTransform() != null) {
            pme = new JMapPaneMouseEvent(pane, e);
        }
        
        return pme;
    }

}
