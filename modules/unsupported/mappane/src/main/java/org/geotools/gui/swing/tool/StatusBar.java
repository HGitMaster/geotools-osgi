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

import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.event.MapMouseEvent;
import org.geotools.gui.swing.event.MapMouseListener;

/**
 * A status bar that displays the mouse map position
 * 
 * @author Michael Bedward
 * @since 2.6
 */
public class StatusBar extends JPanel implements MapMouseListener {
    
    private JMapPane pane;
    private JLabel space1;
    private JLabel space2;

    public StatusBar() {
        space1 = new JLabel();
        space1.setSize(100, 30);
        this.add(space1);
        
        space2 = new JLabel();
        space2.setSize(-1, 30);
        this.add(space2);
        
        this.setPreferredSize(new Dimension(-1, 30));
    }

    public void setMapPane(JMapPane pane) {
        this.pane = pane;
    }

    public void onMouseClicked(MapMouseEvent e) {
    }

    public void onMouseDragged(MapMouseEvent e) {
    }

    public void onMouseEntered(MapMouseEvent e) {
    }

    public void onMouseExited(MapMouseEvent e) {
    }

    public void onMouseMoved(MapMouseEvent e) {
        space1.setText(formatCoords(e.getMapPosition()));
    }

    public void onMousePressed(MapMouseEvent e) {
    }

    public void onMouseReleased(MapMouseEvent e) {
    }

    public void onMouseWheelMoved(MapMouseEvent e) {
    }

    private String formatCoords(DirectPosition2D mapPos) {
        return String.format("%.4f %.4f", mapPos.x, mapPos.y);
    }

}
