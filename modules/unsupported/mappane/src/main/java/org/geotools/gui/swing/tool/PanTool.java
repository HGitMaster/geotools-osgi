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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.event.MapMouseEvent;

/**
 * A map panning tool for JMapPane.  Allows the user to drag the map
 * with the mouse.
 * 
 * @author Michael Bedward
 * @since 2.6
 */
public class PanTool extends CursorTool {
    
    public static final String TOOL_NAME = "Pan";
    public static final String TOOL_TIP = "Click and drag to pan";
    public static final String CURSOR_IMAGE = "/org/geotools/gui/swing/images/pan_cursor_32.gif";
    public static final Point CURSOR_HOTSPOT = new Point(15, 15);

    public static final String ICON_IMAGE_LARGE = "/org/geotools/gui/swing/images/pan_32.png";
    public static final String ICON_IMAGE_SMALL = "/org/geotools/gui/swing/images/pan_24.png";
    
    private Cursor cursor;
    private Icon iconLarge;
    private Icon iconSmall;

    private Point panePos;
    boolean panning;
    
    /**
     * Constructor
     */
    public PanTool() {
        iconLarge = new ImageIcon(getClass().getResource(ICON_IMAGE_LARGE));
        iconSmall = new ImageIcon(getClass().getResource(ICON_IMAGE_SMALL));

        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon imgIcon = new ImageIcon(getClass().getResource(CURSOR_IMAGE));
        cursor = tk.createCustomCursor(imgIcon.getImage(), CURSOR_HOTSPOT, "Pan");

        panning = false;
    }

    /**
     * Respond to a mouse button press event from the map pane. This may
     * signal the start of a mouse drag. Records the event's window position.
     */
    @Override
    public void onMousePressed(MapMouseEvent pme) {
        panePos = pme.getPoint();
        panning = true;
    }

    /**
     * Respond to a mouse dragged event. Calls {@link org.geotools.gui.swing.JMapPane#moveImage()}
     */
    @Override
    public void onMouseDragged(MapMouseEvent pme) {
        if (panning) {
            Point pos = pme.getPoint();
            if (!pos.equals(panePos)) {
                pane.moveImage(pos.x - panePos.x, pos.y - panePos.y);
                panePos = pos;
            }
        }
    }

    /**
     * If this button release is the end of a mouse dragged event, requests the
     * map pane to repaint the display
     */
    @Override
    public void onMouseReleased(MapMouseEvent pme) {
        panning = false;
        pane.repaint();
    }

    /**
     * Get the name assigned to this tool
     * @return "Pan"
     */
    @Override
    public String getName() {
        return TOOL_NAME;
    }

    /**
     * Get the mouse cursor for this tool
     */
    public Cursor getCursor() {
        return cursor;
    }
    
    /**
     * Get the 32x32 pixel icon for this tool
     */
    public Icon getIconLarge() {
        return iconLarge;
    }
    
    /**
     * Get the 24x24 pixel icon for this tool
     */
    public Icon getIconSmall() {
        return iconSmall;
    }
}
