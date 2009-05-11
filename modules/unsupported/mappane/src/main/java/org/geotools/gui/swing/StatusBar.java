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

package org.geotools.gui.swing;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.gui.swing.event.MapMouseAdapter;
import org.geotools.gui.swing.event.MapMouseEvent;
import org.geotools.gui.swing.event.MapMouseListener;

/**
 * A status bar that displays the mouse cursor position in
 * world coordinates.
 *
 * @todo Add the facility to display additional information in
 * the status bar. The notion of 'spaces' is in the present code
 * looking ahead to this facility.
 *
 * @author Michael Bedward
 * @since 2.6
 */
public class StatusBar extends JPanel {

    /*
     * TODO: display additional info in the status bar
     */
    public static final int MAX_SPACES = 2;

    private static final int MIN_HEIGHT = 40;
    private static final int BORDER_WIDTH = 2;
    private static final int SPACE_GAP = 5;

    private MapMouseListener mouseListener;
    private JLabel[] spaces;

    /**
     * Default constructor.
     * {@linkplain #setMapPane(org.geotools.gui.swing.JMapPane)} must be
     * called subsequently for the status bar to receive mouse events.
     */
    public StatusBar() {
        init();
    }

    /**
     * Constructor. Links the status bar to the specified map pane.
     *
     * @param pane the map pane that will send mouse events to this
     * status bar
     */
    public StatusBar(JMapPane pane) {
        init();
        setMapPane(pane);
    }

    /**
     * Register this status bar to receive mouse events from
     * the given map pane
     *
     * @param pane the map pane
     */
    public void setMapPane(JMapPane pane) {
        mouseListener = new MapMouseAdapter() {
            @Override
            public void onMouseMoved(MapMouseEvent ev) {
                displayCoords(ev.getMapPosition());
            }
        };
        
        pane.addMouseListener(mouseListener);
    }

    /**
     * Format and display the world coordinates of the mouse cursor
     * position in the first 'space'
     *
     * @param mapPos mouse cursor position (world coords)
     */
    private void displayCoords(DirectPosition2D mapPos) {
        if (spaces != null) {
            spaces[0].setText(String.format("%.4f %.4f", mapPos.x, mapPos.y));
        }
    }

    /**
     * Helper for constructors. Sets basic layout and creates
     * the first space for map coordinates.
     */
    private void init() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setMinimumSize(new Dimension(-1, MIN_HEIGHT));

        spaces = new JLabel[MAX_SPACES];

        // Space 0 is for map coords
        spaces[0] = new JLabel();
        spaces[0].setBorder(BorderFactory.createEmptyBorder(
                BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, SPACE_GAP));

        spaces[0].setMinimumSize(new Dimension(200, -1));
        spaces[0].setText("Hello");
        add(spaces[0]);
    }
    
}
