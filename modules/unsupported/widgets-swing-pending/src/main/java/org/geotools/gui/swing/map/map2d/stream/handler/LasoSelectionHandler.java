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
package org.geotools.gui.swing.map.map2d.stream.handler;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.event.MouseInputListener;

import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.map2d.stream.SelectableMap2D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import org.geotools.gui.swing.map.map2d.stream.strategy.StreamingStrategy;

/**
 * laso selection handler
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/map/map2d/stream/handler/LasoSelectionHandler.java $
 */
public class LasoSelectionHandler implements SelectionHandler {

    private static final ImageIcon ICON = IconBundle.getResource().getIcon("16_select_laso");
    private static final String title = ResourceBundle.getBundle("org/geotools/gui/swing/map/map2d/handler/Bundle").getString("laso");
    
    protected final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private final MouseInputListener mouseInputListener = new MouseListen();
    private final LasoSelectionDecoration selectionPane = new LasoSelectionDecoration();
    private SelectableMap2D map2D = null;
    private boolean installed = false;
    protected Cursor CUR_SELECT;

    public LasoSelectionHandler() {
        buildCursors();
    }

    private void buildCursors() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon ico_select = IconBundle.getResource().getIcon("16_select");

        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        img.getGraphics().drawImage(ico_select.getImage(), 0, 0, null);
        CUR_SELECT = tk.createCustomCursor(img, new java.awt.Point(1, 1), "select");

    }

    private void doMouseSelection(int mx, int my) {

        Geometry geometry = mousePositionToGeometry(mx, my);
        if (geometry != null) {
            map2D.doSelection(geometry);
        }
    }

    /**
     *  transform a mouse coordinate in JTS Geometry using the CRS of the mapcontext
     * @param mx : x coordinate of the mouse on the map (in pixel)
     * @param my : y coordinate of the mouse on the map (in pixel)
     * @return JTS geometry (corresponding to a square of 6x6 pixel around mouse coordinate)
     */
    private Geometry mousePositionToGeometry(int mx, int my) {
        Coordinate[] coord = new Coordinate[5];
        int taille = 4;
        StreamingStrategy strategy = map2D.getRenderingStrategy();
        coord[0] = strategy.toMapCoord(mx - taille, my - taille);
        coord[1] = strategy.toMapCoord(mx - taille, my + taille);
        coord[2] = strategy.toMapCoord(mx + taille, my + taille);
        coord[3] = strategy.toMapCoord(mx + taille, my - taille);
        coord[4] = coord[0];

        LinearRing lr1 = GEOMETRY_FACTORY.createLinearRing(coord);
        return GEOMETRY_FACTORY.createPolygon(lr1, null);
    }

    private void doSelection(List<Coordinate> lst) {

        if (lst.size() > 2) {
            Coordinate[] coord = new Coordinate[lst.size() + 1];

            int i = 0;
            for (int n = lst.size(); i < n; i++) {
                coord[i] = lst.get(i);
            }

            coord[i] = coord[0];

            LinearRing lr1 = GEOMETRY_FACTORY.createLinearRing(coord);
            Geometry geometry = GEOMETRY_FACTORY.createPolygon(lr1, null);

            map2D.doSelection(geometry);
        }


    }

    public void install(SelectableMap2D map) {
        installed = true;
        map2D = map;
        map2D.addDecoration(selectionPane);
        map2D.getComponent().addMouseListener(mouseInputListener);
        map2D.getComponent().addMouseMotionListener(mouseInputListener);
    }

    public void uninstall() {
        map2D.removeDecoration(selectionPane);
        map2D.getComponent().removeMouseListener(mouseInputListener);
        map2D.getComponent().removeMouseMotionListener(mouseInputListener);
        map2D = null;
        installed = false;
    }

    public boolean isInstalled() {
        return installed;
    }

    private class MouseListen implements MouseInputListener {

        List<Point> points = new ArrayList<Point>();
        List<Coordinate> coords = new ArrayList<Coordinate>();

        public void mouseClicked(MouseEvent e) {
            doMouseSelection(e.getX(), e.getY());
        }

        public void mousePressed(MouseEvent e) {
            points.clear();
            coords.clear();
            points.add(new Point(e.getX(), e.getY()));
            coords.add(map2D.getRenderingStrategy().toMapCoord(e.getX(), e.getY()));
        }

        public void mouseReleased(MouseEvent e) {
            points.add(new Point(e.getX(), e.getY()));
            coords.add(map2D.getRenderingStrategy().toMapCoord(e.getX(), e.getY()));

            doSelection(coords);

            selectionPane.setPoints(null);

            points.clear();
            coords.clear();
        }

        public void mouseEntered(MouseEvent e) {
            map2D.getComponent().setCursor(CUR_SELECT);
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            points.add(new Point(e.getX(), e.getY()));
            coords.add(map2D.getRenderingStrategy().toMapCoord(e.getX(), e.getY()));
            selectionPane.setPoints(new ArrayList<Point>(points));
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    public String getTitle() {
        return title;
    }

    public ImageIcon getIcon() {
        return ICON;
    }
}
