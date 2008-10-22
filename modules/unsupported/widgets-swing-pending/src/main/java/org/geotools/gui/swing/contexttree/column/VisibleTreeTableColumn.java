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

/**
 * Default visibility column
 * 
 * @author Johann Sorel
 */
public final class VisibleTreeTableColumn extends TreeTableColumn {
    
    
    /**
     * column with checkbox for jcontexttree
     */
    public VisibleTreeTableColumn() {
       
        setCellEditor( new DefaultCellEditor(new VisibleComponent()));
        setCellRenderer( new DefaultCellRenderer(new VisibleComponent()));
                
        String name = BUNDLE.getString("col_visible");                
        setHeaderValue( new HeaderInfo(name,null,IconBundle.getResource().getIcon("16_visible") ));
        
        setEditable(true);
        setResizable(false);
        setMaxWidth(25);
        setMinWidth(25);
        setPreferredWidth(25);
        setWidth(25);
        
        setEditableOnMouseOver(true);
    }
         
    
   
    public void setValue(Object target, Object value) {
    }
    
    
    public Object getValue(Object target) {
        
        if(target instanceof MapLayer)
            return (MapLayer)target;
        else
            return "n/a";
    }
    
    
        
    public boolean isCellEditable(Object target){
        
         if(target instanceof MapLayer)
            return isEditable();
        else
            return false;
    }
    
    
    public Class getColumnClass() {
        return MapLayer.class;
    }

    
}
