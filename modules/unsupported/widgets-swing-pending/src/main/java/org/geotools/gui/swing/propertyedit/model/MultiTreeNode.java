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

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.geotools.gui.swing.propertyedit.PropertyPane;

/**
 * multi tree node
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/propertyedit/model/MultiTreeNode.java $
 */
public class MultiTreeNode extends DefaultMutableTreeNode{
    
    private PropertyPane pan;
    
    /** Creates a new instance of MultiTreeNode 
     * @param panel 
     */
    public MultiTreeNode(PropertyPane panel) {
        super(panel);
        pan = panel;
    }
    
    public  ImageIcon getIcon(){
        return pan.getIcon();
    }

    public String getTitle() {
        return pan.getTitle();
    }

        
}
