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
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/map/map2d/stream/control/JStreamNavigationBar.java $
 */
public class JStreamNavigationBar extends JToolBar {

    
    
    private static final ImageIcon ICON_ZOOM_ALL = IconBundle.getResource().getIcon("16_zoom_all");
    private static final ImageIcon ICON_NEXT = IconBundle.getResource().getIcon("16_next_maparea");
    private static final ImageIcon ICON_PREVIOUS = IconBundle.getResource().getIcon("16_previous_maparea");
    private static final ImageIcon ICON_ZOOM_IN = IconBundle.getResource().getIcon("16_zoom_in");
    private static final ImageIcon ICON_ZOOM_OUT = IconBundle.getResource().getIcon("16_zoom_out");
    private static final ImageIcon ICON_ZOOM_PAN = IconBundle.getResource().getIcon("16_zoom_pan");
    private static final ImageIcon ICON_REFRESH = IconBundle.getResource().getIcon("16_data_reload");
    
    private final StreamZoomAll ACTION_ZOOM_ALL = new StreamZoomAll();
    private final StreamNextArea ACTION_NEXT = new StreamNextArea();
    private final StreamPreviousArea ACTION_PREVIOUS = new StreamPreviousArea();
    private final StreamZoomIn ACTION_ZOOM_IN = new StreamZoomIn();
    private final StreamZoomOut ACTION_ZOOM_OUT = new StreamZoomOut();
    private final StreamPan ACTION_ZOOM_PAN = new StreamPan();
    private final StreamRefresh ACTION_REFRESH = new StreamRefresh();
    
    
    private StreamingMap2D map = null;
    private final JButton gui_zoomAll = buildButton(ICON_ZOOM_ALL, ACTION_ZOOM_ALL);
    private final JButton gui_nextArea = buildButton(ICON_NEXT, ACTION_NEXT);
    private final JButton gui_previousArea = buildButton(ICON_PREVIOUS, ACTION_PREVIOUS);
    private final JButton gui_zoomIn = buildButton(ICON_ZOOM_IN, ACTION_ZOOM_IN);
    private final JButton gui_zoomOut = buildButton(ICON_ZOOM_OUT, ACTION_ZOOM_OUT);
    private final JButton gui_zoomPan = buildButton(ICON_ZOOM_PAN, ACTION_ZOOM_PAN);
    private final JButton gui_refresh = buildButton(ICON_REFRESH, ACTION_REFRESH);
    private final int largeur = 2;

    /**
     * Creates a new instance of JMap2DControlBar
     */
    public JStreamNavigationBar() {
        this(null);
    }

    /**
     * Creates a new instance of JMap2DControlBar
     * @param pane : related Map2D or null
     */
    public JStreamNavigationBar(StreamingMap2D pane) {
        setMap(pane);
        init();
    }

    private void init() {
        add(gui_zoomAll);
        add(gui_refresh);
        add(gui_previousArea);
        add(gui_nextArea);
        add(gui_zoomIn);
        add(gui_zoomOut);
        add(gui_zoomPan);
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
        ACTION_NEXT.setMap(map);
        ACTION_PREVIOUS.setMap(map);
        ACTION_REFRESH.setMap(map);
        ACTION_ZOOM_ALL.setMap(map);
        ACTION_ZOOM_IN.setMap(map);
        ACTION_ZOOM_OUT.setMap(map);
        ACTION_ZOOM_PAN.setMap(map);        
    }
}
