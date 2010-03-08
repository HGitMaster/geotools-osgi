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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;

import org.geotools.gui.swing.contexttree.JContextTree;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;

/**
 * Paste item for treetable
 *
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/contexttree/popup/PasteItem.java $
 */
public class PasteItem implements TreePopupItem {

    private JMenuItem pasteitem = null;
    private JContextTree tree = null;
    private List<MapContext> contexts = new ArrayList<MapContext>();
    private List<MapLayer> layers = new ArrayList<MapLayer>();

    /**
     * Paste menuitem for jcontextpopup 
     * @param tree
     */
    public PasteItem(final JContextTree tree) {
        this.tree = tree;

        pasteitem = new JMenuItem(BUNDLE.getString("paste"));
        pasteitem.setIcon(IconBundle.getResource().getIcon("16_paste"));
        pasteitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));

        pasteitem.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        tree.pasteBuffer();
                    }
                });
    }

    private String buildToolTip(Object[] buffer) {
        String tooltip = "<html>";

        for (Object obj : buffer) {

            if (obj instanceof MapLayer) {
                layers.add((MapLayer) obj);
            }

            if (obj instanceof MapContext) {
                contexts.add((MapContext) obj);
            }
        }


        if (contexts.size() > 0) {
            tooltip += "<b>&nbsp " + BUNDLE.getString("contexts") + "</b> : &nbsp";

            for (MapContext context : contexts) {
                tooltip += "<br>&nbsp &nbsp - " + context.getTitle() +"&nbsp &nbsp &nbsp";
            }
        }



        if (layers.size() > 0) {

            if (tooltip.length() > 6) {
                tooltip += "<br>";
            }

            tooltip += "<b>&nbsp " + BUNDLE.getString("layers") + "</b> : &nbsp";

            for (MapLayer layer : layers) {
                tooltip += "<br>&nbsp &nbsp - " + layer.getTitle() +"&nbsp &nbsp &nbsp";
            }
        }
        
        tooltip += "</html>";
        
        contexts.clear();
        layers.clear();
        return tooltip;
    }

    public boolean isValid(TreePath[] selection) {
        return tree.selectionContainOnlyContexts() || tree.selectionContainOnlyLayers();
    }

    public Component getComponent(TreePath[] selection) {
        pasteitem.setEnabled(tree.canPasteBuffer());
        pasteitem.setToolTipText(buildToolTip(tree.getBuffer()));

        return pasteitem;
    }
}
