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
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;

import org.geotools.gui.swing.contexttree.JContextTree;
import org.geotools.gui.swing.icon.IconBundle;

/**
 * cut item for treetable
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/contexttree/popup/CutItem.java $
 */
public class CutItem implements TreePopupItem{

    private JContextTree tree = null;
    private JMenuItem cutitem = null;
    
    /**
     * cut item for jcontexttreepopup
     * @param tree
     */
    public CutItem(final JContextTree tree){
        this.tree = tree;
        
        cutitem = new JMenuItem(BUNDLE.getString("cut"));
        cutitem.setIcon( IconBundle.getResource().getIcon("16_cut") );
        cutitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        
        cutitem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                tree.cutSelectionInBuffer();
            }
        });
        
    }
    
    public boolean isValid(TreePath[] selection) {
        return tree.selectionContainOnlyContexts() || tree.selectionContainOnlyLayers();
    }

    public Component getComponent(TreePath[] selection) {
        cutitem.setEnabled( tree.canCutSelection() );
        return cutitem;
    }

}
