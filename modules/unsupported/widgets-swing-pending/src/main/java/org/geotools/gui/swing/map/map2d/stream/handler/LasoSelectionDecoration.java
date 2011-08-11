/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

import javax.swing.JComponent;

import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.gui.swing.map.map2d.decoration.MapDecoration;

/**
 * Selection decoration
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/map/map2d/stream/handler/LasoSelectionDecoration.java $
 */
public class LasoSelectionDecoration extends JComponent implements MapDecoration{

    private final Color borderColor = new Color(0,255,0);
    
    List<Point> points = null;
    
    
    public LasoSelectionDecoration(){}
    
    
    
    public void setPoints(List<Point> points){
        this.points = points;
        repaint();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        if(points != null && points.size() > 1){
                    
            g.setColor(borderColor);
            
            for(int i=0;i<points.size()-1;i++){
                Point p1 = points.get(i);
                Point p2 = points.get(i+1);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
            
            }
    }

    public void refresh() {
        repaint();
    }

    public JComponent geComponent() {
        return this;
    }
    
    public void setMap2D(Map2D map) {
        
    }

    public Map2D getMap2D() {
        return null;
    }

    public void dispose() {
    }
}
