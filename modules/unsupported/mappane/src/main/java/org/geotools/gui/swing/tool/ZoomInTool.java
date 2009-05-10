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
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.gui.swing.event.MapMouseEvent;

/**
 * A zoom-in tool for JMapPane.
 * <p>
 * For mouse clicks, the display will be zoomed-in such that the 
 * map centre is the position of the mouse click and the map
 * width and height are calculated as:
 * <pre>   {@code len = len.old / z} </pre>
 * where {@code z} is the linear zoom increment (>= 1.0)
 * <p>
 * The tool also responds to the user drawing a box on the map pane with 
 * mouse click-and-drag to define the zoomed-in area.
 * 
 * @author Michael Bedward
 * @since 2.6
 */
public class ZoomInTool extends AbstractZoomTool {
    
    public static final String TOOL_NAME = "Zoom in";
    public static final String TOOL_TIP = "Click to zoom in or drag to define the zoom area";
    public static final String CURSOR_IMAGE = "/org/geotools/gui/swing/images/zoom_in_cursor_32.gif";
    public static final Point CURSOR_HOTSPOT = new Point(13, 11);

    public static final String ICON_IMAGE_LARGE = "/org/geotools/gui/swing/images/zoom_in_32.png";
    public static final String ICON_IMAGE_SMALL = "/org/geotools/gui/swing/images/zoom_in_24.png";
    
    private Cursor cursor;
    private Icon iconLarge;
    private Icon iconSmall;
    
    private Point2D startDragPos;
    private boolean dragged;
    
    /**
     * Constructor
     */
    public ZoomInTool() {
        iconLarge = new ImageIcon(getClass().getResource(ICON_IMAGE_LARGE));
        iconSmall = new ImageIcon(getClass().getResource(ICON_IMAGE_SMALL));

        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon imgIcon = new ImageIcon(getClass().getResource(CURSOR_IMAGE));
        cursor = tk.createCustomCursor(imgIcon.getImage(), CURSOR_HOTSPOT, "Zoom In");
        
        startDragPos = new DirectPosition2D();
        dragged = false;
    }
    
    /**
     * Zoom in by the currently set increment, with the map
     * centred at the location (in world coords) of the mouse
     * click
     * 
     * @param e map pane mouse event
     */
    @Override
    public void onMouseClicked(MapMouseEvent e) {
        Rectangle paneArea = pane.getVisibleRect();
        DirectPosition2D mapPos = e.getMapPosition();

        double scale = pane.getWorldToScreenTransform().getScaleX();
        double newScale = scale * zoom;

        DirectPosition2D corner = new DirectPosition2D(
                mapPos.getX() - 0.5d * paneArea.getWidth() / newScale,
                mapPos.getY() + 0.5d * paneArea.getHeight() / newScale);
        
        Envelope2D newMapArea = new Envelope2D();
        newMapArea.setFrameFromCenter(mapPos, corner);
        pane.setMapArea(newMapArea);
    }
    
    /**
     * Records the map position of the mouse event in case this
     * button press is the beginning of a mouse drag
     */
    @Override
    public void onMousePressed(MapMouseEvent e) {
        startDragPos = new DirectPosition2D();
        startDragPos.setLocation(e.getMapPosition());
    }

    /**
     * Records that the mouse is being dragged
     */
    @Override
    public void onMouseDragged(MapMouseEvent e) {
        dragged = true;
    }

    /**
     * If the mouse was dragged, determines the bounds of the
     * box that the user defined and passes this to the pane's
     * {@link org.geotools.gui.swing.JMapPane#setMapArea(org.opengis.geometry.Envelope) }
     * method
     */
    @Override
    public void onMouseReleased(MapMouseEvent e) {
        if (dragged && !e.getPoint().equals(startDragPos)) {
            Envelope2D env = new Envelope2D();
            env.setFrameFromDiagonal(startDragPos, e.getMapPosition());
            dragged = false;
            pane.setMapArea(env);
        }
    }

    /**
     * Get the name assigned to this tool
     * @return "Zoom in"
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
