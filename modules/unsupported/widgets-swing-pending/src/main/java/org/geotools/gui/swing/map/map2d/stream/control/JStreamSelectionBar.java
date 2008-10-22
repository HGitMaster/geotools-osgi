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
package org.geotools.gui.swing.map.map2d.stream.control;

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.map2d.stream.StreamingMap2D;

/**
 * JMap2DControlBar is a JPanel to handle Navigation state for a NavigableMap2D
 * ZoomIn/Out, pan, selection, refresh ...
 * 
 * @author Johann Sorel
 */
public class JStreamSelectionBar extends JToolBar {

    
    private static final ImageIcon ICON_SELECT = IconBundle.getResource().getIcon("16_select");
    
    private final StreamSelect ACTION_SELECT = new StreamSelect();
    
    
    private StreamingMap2D map = null;
    private final JButton gui_select = buildButton(ICON_SELECT, ACTION_SELECT);
    private final SelectFilterChooser gui_filter = new SelectFilterChooser();
    private final SelectHandlerChooser gui_handler = new SelectHandlerChooser();
    private final int largeur = 2;

    /**
     * Creates a new instance of JMap2DControlBar
     */
    public JStreamSelectionBar() {
        this(null);
    }

    /**
     * Creates a new instance of JMap2DControlBar
     * @param pane : related Map2D or null
     */
    public JStreamSelectionBar(StreamingMap2D pane) {
        setMap(pane);
        init();
    }

    private void init() {
        add(gui_select);
        add(gui_filter);
        add(gui_handler);
    }
    
    
    private JButton buildButton(ImageIcon img,Action action) {
        JButton but = new JButton(action);
        but.setIcon(img);
        but.setBorder(new EmptyBorder(largeur, largeur, largeur, largeur));
        but.setBorderPainted(false);
        but.setContentAreaFilled(false);
        but.setPreferredSize(new Dimension(25, 25));
        but.setOpaque(false);
        return but;
    }
    

    /**
     * set the related Map2D
     * @param map2d : related Map2D
     */
    public void setMap(StreamingMap2D map2d) {
        map = map2d;        
        ACTION_SELECT.setMap(map);
        gui_filter.setMap(map);
        gui_handler.setMap(map);
    }
}
