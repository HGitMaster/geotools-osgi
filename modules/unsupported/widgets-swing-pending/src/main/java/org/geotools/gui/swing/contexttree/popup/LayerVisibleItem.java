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
package org.geotools.gui.swing.contexttree.popup;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.tree.TreePath;

import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.map.MapLayer;



/**
 * Default popup control for visibility of MapLayer, use for JContextTreePopup
 * 
 * @author Johann Sorel
 */
public class LayerVisibleItem extends JCheckBoxMenuItem implements TreePopupItem{
    
    private MapLayer layer;
    
    
    /** 
     * Creates a new instance of LayerVisibleControl 
     */
    public LayerVisibleItem() {
        this.setText( BUNDLE.getString("visible"));
        init();
    }
        
    private void init(){
        
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                layer.setVisible(isSelected());
            }
        });
    }
     
    public boolean isValid(TreePath[] selection) {
        if (selection.length == 1) {
            ContextTreeNode node = (ContextTreeNode) selection[0].getLastPathComponent();            
            return ( node.getUserObject() instanceof MapLayer ) ;
        }
        return false;
    }

    public Component getComponent(TreePath[] selection) {
        ContextTreeNode node = (ContextTreeNode) selection[0].getLastPathComponent();  
        layer = (MapLayer) node.getUserObject() ;
        this.setSelected(layer.isVisible());
        
        return this;
    }
    
}
