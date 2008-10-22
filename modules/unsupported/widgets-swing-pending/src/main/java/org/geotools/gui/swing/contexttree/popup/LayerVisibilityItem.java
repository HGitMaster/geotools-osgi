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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;

import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.contexttree.column.OpacityComponent;
import org.geotools.map.MapLayer;



/**
 * popup control for visibility of MapLayer, use for JContextTreePopup
 *
 * @author Johann Sorel
 */
public class LayerVisibilityItem extends JPanel implements TreePopupItem{
    
    private MapLayer layer;
    private JCheckBox jck = new JCheckBox();
    private OpacityComponent opa = new OpacityComponent();
    
    
    /** 
     * Creates a new instance of LayerVisibleControl 
     */
    public LayerVisibilityItem() {
        init();
    }
            
    private void init(){
        setLayout(new BorderLayout());
        
        setOpaque(false);
        opa.setOpaque(false);
        opa.setPreferredSize(new Dimension(30,20));
        
        jck.setOpaque(false);
        jck.setText( BUNDLE.getString("visible"));
        jck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                layer.setVisible(jck.isSelected());
            }
        });
        
        add(BorderLayout.WEST,jck);
        add(BorderLayout.CENTER,opa);
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
        jck.setSelected(layer.isVisible());
        opa.parse(layer);
        
        return this;
    }
    
}
