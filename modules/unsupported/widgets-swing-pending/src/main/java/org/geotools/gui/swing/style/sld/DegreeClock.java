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
package org.geotools.gui.swing.style.sld;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.SwingUtilities;

/**
 * Degree clock
 * 
 * @author Johann Sorel
 */
class DegreeClock extends javax.swing.JPanel implements MouseMotionListener, MouseListener{

    private double degree = 0;
    public JDegreePane pan = null;
    int X = WIDTH/2;
    int Y = HEIGHT/2;
    
    /**
     * clock component to edit degrees
     */
    DegreeClock(){
        super();
        init();
    }
    
    private void init(){
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        setDegree(0);
    }
    
    /**
     * 
     * @param pan related JdegreePanel
     */
    void setPan(JDegreePane pan) {
        this.pan = pan;
    }
    
    @Override
    public void paintComponent(Graphics g){
       super.paintComponent(g);
       
       Graphics2D g2d = (Graphics2D) g;
       g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
       
       g2d.setColor(Color.WHITE);
       g2d.fillOval(0, 0, getWidth()-2, getHeight()-2);
       g2d.setColor(Color.GRAY);
       g2d.drawOval(0, 0, getWidth()-2, getHeight()-2);
             
       g2d.setColor(Color.BLACK);
       g2d.drawLine(getWidth()/2, getHeight()/2, X, Y);
       
    }

    void update(MouseEvent e){
        
        int mouseX = e.getX();
        int mouseY = e.getY();
                
        Point A = new Point(getWidth()/2,0);
        Point B = new Point(getWidth()/2,getHeight()/2);
        Point C = new Point(mouseX,mouseY);
        
        double a = Math.pow(    Math.pow( (C.x - B.x) , 2) +  Math.pow( (C.y - B.y) , 2)    ,0.5d);
        double b = Math.pow(    Math.pow( (A.x - C.x) , 2) +  Math.pow( (A.y - C.y) , 2)    ,0.5d);
        double c = Math.pow(    Math.pow( (A.x - B.x) , 2) +  Math.pow( (A.y - B.y) , 2)    ,0.5d);
                
        double angleA = Math.acos(  ( Math.pow(b, 2) + Math.pow(c, 2) - Math.pow(a, 2) )/(2*b*c) );
        double angleB = Math.acos(  ( Math.pow(a, 2) + Math.pow(c, 2) - Math.pow(b, 2) )/(2*a*c) );
        double angleC = Math.acos(  ( Math.pow(a, 2) + Math.pow(b, 2) - Math.pow(c, 2) )/(2*a*b) );
        
        angleB = Math.toDegrees(angleB);
        if(mouseX < (getWidth()/2) ){
            angleB = (180-angleB) + 180;
        }
        
        degree = angleB;
        
         if(pan!= null){
            pan.update();
        }
        
        setDegree(angleB);    
        
    }
    
    void setDegree(double degree){
                       
        double cos = Math.cos( Math.toRadians(degree));
        double sin = Math.sin( Math.toRadians(degree));
        
        cos *= getHeight()/2;
        sin *= getWidth()/2;
        
        X = getWidth()/2;
        Y = getHeight()/2;
        
        X += sin;
        Y -= cos;
        
       
        
         SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                repaint();
            }
        });        
    }
    
    double getDegree(){
        return degree;
    }
    
    
    public void mouseDragged(MouseEvent e) {
        update(e);
    }

    public void mousePressed(MouseEvent e) {
        update(e);
    }

    public void mouseReleased(MouseEvent e) {
        update(e);
    }

    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}

    
    
}
