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

import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;

import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.gui.swing.map.map2d.stream.NavigableMap2D;
import org.geotools.map.MapLayer;

/**
 * Default popup control for zoom on MapLayer, use for JContextTreePopup
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/contexttree/popup/LayerZoomItem.java $
 */
public class LayerZoomItem extends JMenuItem implements TreePopupItem {

    private MapLayer layer;
    private NavigableMap2D map;

    /** Creates a new instance
     * @param map 
     */
    public LayerZoomItem(Map2D map) {
        this.setText(BUNDLE.getString("zoom_to_layer"));
        init();        
        setMap(map);
    }

    public void setMap(Map2D map) {
        if(map instanceof NavigableMap2D){
            this.map = (NavigableMap2D) map;
        }
    }

    public NavigableMap2D getMap() {
        return map;
    }
   
    private void init() {

        addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        if (map != null && layer != null) {
                            try {
                                map.getRenderingStrategy().setMapArea(layer.getBounds());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
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
        this.setEnabled((map != null));

        return this;
    }
}
