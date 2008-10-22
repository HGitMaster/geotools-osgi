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
package org.geotools.gui.swing.propertyedit.model;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.geotools.gui.swing.icon.IconBundle;

/**
 * Cell Renderer for JXMapContextTree
 * 
 * @author Johann Sorel
 */
public class GeometryCellRenderer extends JPanel implements TableCellRenderer{
    
    private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    private ImageIcon icon;
    
    /** 
     * Creates a new instance of JXMapContextTableNodeRenderer 
     */
    public GeometryCellRenderer() {
        icon = IconBundle.getResource().getIcon("edit_geom");
        JLabel lbl = new JLabel( );
        lbl.setBorder(null);        
        setLayout(new GridLayout(1,1));
        
        setBorder(noFocusBorder);
        
        add(lbl);
    }
    
    
    /**
     * return component for rendering
     * @return 
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        Object o = table.getValueAt(row,column);
   
                
        return this ;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawRect(0,0,getWidth(),getHeight());
        
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(new GradientPaint(0,0,Color.LIGHT_GRAY,getWidth()-1,getHeight()-1,Color.WHITE));
        g2.fillRect(0, 0, getWidth()-1,getHeight()-1);

        paintChildren(g);
    }
    
    
}
