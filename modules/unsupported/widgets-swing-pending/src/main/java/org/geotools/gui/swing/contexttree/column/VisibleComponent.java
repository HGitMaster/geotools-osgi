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
package org.geotools.gui.swing.contexttree.column;

import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import org.geotools.gui.swing.contexttree.renderer.RenderAndEditComponent;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapLayer;

/**
 * Component used to present layer visibility
 * 
 * @author Johann Sorel
 */
final class VisibleComponent extends RenderAndEditComponent{

    
    
    
    
    private final VisbleCheck check = new VisbleCheck(){
        
        
    };
    private MapLayer layer = null;
    
    VisibleComponent(){
        super();
        setLayout(new GridLayout(1,1));
        check.setOpaque(false);
        
        check.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if(layer != null){
                    layer.setVisible(check.isSelected());
                }
            }
        });
        
    }
    
    
    @Override
    public void parse(Object obj) {
       layer = null;
        
       
        removeAll();
        if(obj instanceof Boolean){
            check.setSelected((Boolean)obj);
            add(check);
        }
        else if(obj instanceof MapLayer){
            check.setSelected(((MapLayer)obj).isVisible());
            layer = (MapLayer) obj;
            add(check);
        }
        
    }

    @Override
    public Object getValue() {
        return check.isSelected();
    }

   
    
}


class VisbleCheck extends JCheckBox{
    
    
    private static final ImageIcon ICO_VISIBLE = IconBundle.getResource().getIcon("16_visible");
    private static final ImageIcon ICO_NOVISIBLE = IconBundle.getResource().getIcon("16_novisible");
    
     @Override
        public void paintComponent(Graphics g){
         
            int x = (getWidth()-16)/2;
            int y = (getHeight()-16)/2;
            g.drawImage( (isSelected())?ICO_VISIBLE.getImage() : ICO_NOVISIBLE.getImage(), x,y, this);
        }
}
