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

import org.geotools.gui.swing.contexttree.renderer.DefaultCellEditor;
import org.geotools.gui.swing.contexttree.renderer.DefaultCellRenderer;
import org.geotools.gui.swing.contexttree.renderer.HeaderInfo;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapLayer;
import org.geotools.styling.Symbolizer;


/**
 * opacity column for jcontexttree, with slider
 * 
 * @author Johann Sorel
 */
public final class OpacityTreeTableColumn extends TreeTableColumn {


    /**
     * Creates a new instance 
     */
    public OpacityTreeTableColumn() {
        super();
        
        setCellRenderer(new DefaultCellRenderer( new OpacityComponent()));
        setCellEditor(new DefaultCellEditor( new OpacityComponent()));

        String name = BUNDLE.getString("col_opacity");                
        setHeaderValue( new HeaderInfo(name,null,IconBundle.getResource().getIcon("16_opacity") ));
        
        setEditable(true);
        setResizable(false);
        setMaxWidth(60);
        setMinWidth(60);
        setPreferredWidth(60);
        setWidth(25);
        
        setEditableOnMouseOver(true);
    }

    public void setValue(Object target, Object value) {

    }

    public Object getValue(Object target) {

        if (target instanceof MapLayer || target instanceof Symbolizer) {
            return target;
        } else {
            return "n/a";
        }
    }

   
    public boolean isCellEditable(Object target) {

        if (target instanceof MapLayer || target instanceof Symbolizer) {
            return isEditable();
        } else {
            return false;
        }
    }

    public Class getColumnClass() {
        return Boolean.class;
    }

}

