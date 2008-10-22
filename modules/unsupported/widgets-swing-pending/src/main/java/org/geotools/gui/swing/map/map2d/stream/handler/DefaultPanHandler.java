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
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.event.MouseInputListener;

import org.geotools.gui.swing.icon.IconBundle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import org.geotools.gui.swing.map.map2d.stream.NavigableMap2D;

/**
 * Panoramic handler
 * 
 * @author Johann Sorel
 */
public class DefaultPanHandler implements NavigationHandler {

    private static final ImageIcon ICON = IconBundle.getResource().getIcon("16_select_default");
    private Cursor CUR_ZOOM_PAN;
    private static final String title = ResourceBundle.getBundle("org/geotools/gui/swing/map/map2d/handler/Bundle").getString("default");
    protected final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private final MouseListen mouseInputListener = new MouseListen();
    private final ZoomPanDecoration zoompanPanel = new ZoomPanDecoration();
    private NavigableMap2D map2D = null;
    private boolean installed = false;

    public DefaultPanHandler() {
        buildCursors();
    }

    private void buildCursors() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        ImageIcon ico_zoomPan = IconBundle.getResource().getIcon("16_zoom_pan");

        BufferedImage img3 = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        img3.getGraphics().drawImage(ico_zoomPan.getImage(), 0, 0, null);
        CUR_ZOOM_PAN = tk.createCustomCursor(img3, new Point(1, 1), "in");
    }

    public void install(NavigableMap2D map) {
        installed = true;
        map2D = map;
        map2D.addDecoration(zoompanPanel);
        map2D.getComponent().addMouseListener(mouseInputListener);
        map2D.getComponent().addMouseMotionListener(mouseInputListener);
        map2D.getComponent().addMouseWheelListener(mouseInputListener);
    }

    public void uninstall() {
        map2D.removeDecoration(zoompanPanel);
        map2D.getComponent().removeMouseListener(mouseInputListener);
        map2D.getComponent().removeMouseMotionListener(mouseInputListener);
        map2D.getComponent().removeMouseWheelListener(mouseInputListener);
        map2D = null;
        installed = false;
    }

    public boolean isInstalled() {
        return installed;
    }

    public String getTitle() {
        return title;
    }

    public ImageIcon getIcon() {
        return ICON;
    }

    //---------------------PRIVATE CLASSES--------------------------------------
    private class MouseListen implements MouseInputListener, MouseWheelListener {

        private int startX;
        private int startY;
        private int lastX;
        private int lastY;
        private int mousebutton = 0;

        private void processDrag(int x1, int y1, int x2, int y2, boolean pan) {

            Envelope mapArea = map2D.getRenderingStrategy().getMapArea();

            if ((x1 == x2) && (y1 == y2)) {
                return;
            }

            Rectangle bounds = map2D.getComponent().getBounds();

            double mapWidth = mapArea.getWidth();
            double mapHeight = mapArea.getHeight();

            double startX = ((x1 * mapWidth) / (double) bounds.width) + mapArea.getMinX();
            double startY = (((bounds.getHeight() - y1) * mapHeight) / (double) bounds.height) + mapArea.getMinY();
            double endX = ((x2 * mapWidth) / (double) bounds.width) + mapArea.getMinX();
            double endY = (((bounds.getHeight() - y2) * mapHeight) / (double) bounds.height) + mapArea.getMinY();

            double left;
            double right;
            double bottom;
            double top;
            Coordinate ll;
            Coordinate ur;

            if (!pan) {
                // move the image with the mouse
                // calculate X offsets from start point to the end Point
                double deltaX1 = endX - startX;

                // System.out.println("deltaX " + deltaX1);
                // new edges
                left = mapArea.getMinX() - deltaX1;
                right = mapArea.getMaxX() - deltaX1;

                // now for Y
                double deltaY1 = endY - startY;

                // System.out.println("deltaY " + deltaY1);
                bottom = mapArea.getMinY() - deltaY1;
                top = mapArea.getMaxY() - deltaY1;
                ll = new Coordinate(left, bottom);
                ur = new Coordinate(right, top);

                map2D.getRenderingStrategy().setMapArea(new Envelope(ll, ur));
            //mapArea = fixAspectRatio(getBounds(), new Envelope(ll, ur));

            } else {
                // move the image with the mouse
                // calculate X offsets from start point to the end Point
                double deltaX1 = endX - startX;

                // System.out.println("deltaX " + deltaX1);
                // new edges
                left = mapArea.getMinX() - deltaX1;
                right = mapArea.getMaxX() - deltaX1;

                // now for Y
                double deltaY1 = endY - startY;

                // System.out.println("deltaY " + deltaY1);
                bottom = mapArea.getMinY() - deltaY1;
                top = mapArea.getMaxY() - deltaY1;
                ll = new Coordinate(left, bottom);
                ur = new Coordinate(right, top);

                map2D.getRenderingStrategy().setMapArea(new Envelope(ll, ur));

            }
        }

        public void mouseClicked(MouseEvent e) {

            mousebutton = e.getButton();

            Envelope mapArea = map2D.getRenderingStrategy().getMapArea();

            Rectangle bounds = map2D.getComponent().getBounds();
            double x = (double) (e.getX());
            double y = (double) (e.getY());
            double width = mapArea.getWidth();
            double height = mapArea.getHeight();
            double width2 = mapArea.getWidth() / 2.0;
            double height2 = mapArea.getHeight() / 2.0;

            double mapX = ((x * width) / (double) bounds.width) + mapArea.getMinX();
            double mapY = (((bounds.getHeight() - y) * height) / (double) bounds.height) + mapArea.getMinY();

            double zlevel = 1.0;

            zlevel = 1.0;
            Coordinate ll = new Coordinate(mapX - (width2 / zlevel), mapY - (height2 / zlevel));
            Coordinate ur = new Coordinate(mapX + (width2 / zlevel), mapY + (height2 / zlevel));
            map2D.getRenderingStrategy().setMapArea(new Envelope(ll, ur));
        }

        public void mousePressed(MouseEvent e) {
            startX = e.getX();
            startY = e.getY();
            lastX = 0;
            lastY = 0;

            mousebutton = e.getButton();
            zoompanPanel.setCoord(0, 0, map2D.getComponent().getWidth(), map2D.getComponent().getHeight(), true);
        }

        public void mouseReleased(MouseEvent e) {
            int endX = e.getX();
            int endY = e.getY();

            if (mousebutton == MouseEvent.BUTTON1) {
                zoompanPanel.setFill(false);
                zoompanPanel.setCoord(0, 0, 0, 0, false);
                processDrag(startX, startY, endX, endY, false);

            } //right mouse button : pan action
            else if (mousebutton == MouseEvent.BUTTON3) {
                zoompanPanel.setFill(false);
                zoompanPanel.setCoord(0, 0, 0, 0, false);
                processDrag(startX, startY, endX, endY, true);
            }

            lastX = 0;
            lastY = 0;
        }

        public void mouseEntered(MouseEvent e) {
            map2D.getComponent().setCursor(CUR_ZOOM_PAN);
        }

        public void mouseExited(MouseEvent e) {
            zoompanPanel.setFill(false);
            zoompanPanel.setCoord(0, 0, 0, 0, true);
        }

        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            if ((lastX > 0) && (lastY > 0)) {
                int dx = lastX - startX;
                int dy = lastY - startY;
                zoompanPanel.setFill(false);
                zoompanPanel.setCoord(dx, dy, map2D.getComponent().getWidth(), map2D.getComponent().getHeight(), true);
            }

            lastX = x;
            lastY = y;
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            int val = e.getWheelRotation();

            Coordinate coord = map2D.getRenderingStrategy().toMapCoord(e.getX(), e.getY());

            if (val > 0) {
                Envelope env = map2D.getRenderingStrategy().getMapArea();
                double width = env.getWidth();
                double height = env.getHeight();

                Coordinate nw = new Coordinate(coord);
                nw.x -= width;
                nw.y -= height;
                Coordinate se = new Coordinate(coord);
                se.x += width;
                se.y += height;

                Envelope env2 = new Envelope(nw, se);
                map2D.getRenderingStrategy().setMapArea(env2);

            } else if (val < 0) {
                Envelope env = map2D.getRenderingStrategy().getMapArea();
                double width = env.getWidth();
                double height = env.getHeight();

                Coordinate nw = new Coordinate(coord);
                nw.x -= width / 4;
                nw.y -= height / 4;
                Coordinate se = new Coordinate(coord);
                se.x += width / 4;
                se.y += height / 4;

                Envelope env2 = new Envelope(nw, se);
                map2D.getRenderingStrategy().setMapArea(env2);
            }

        }
    }
}
