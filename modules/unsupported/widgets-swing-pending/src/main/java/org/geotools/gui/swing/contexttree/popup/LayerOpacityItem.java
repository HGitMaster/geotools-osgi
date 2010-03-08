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

import javax.swing.tree.TreePath;

import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.gui.swing.contexttree.column.OpacityComponent;
import org.geotools.map.MapLayer;

/**
 * layer opacity item
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/contexttree/popup/LayerOpacityItem.java $
 */
public class LayerOpacityItem implements TreePopupItem{

    private final OpacityComponent comp = new OpacityComponent();
    
    /**
     * create new instance
     */
    public LayerOpacityItem(){
        comp.setOpaque(false);
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
        MapLayer layer = (MapLayer) node.getUserObject() ;        
        comp.parse(layer);
        return comp;
    }

}
