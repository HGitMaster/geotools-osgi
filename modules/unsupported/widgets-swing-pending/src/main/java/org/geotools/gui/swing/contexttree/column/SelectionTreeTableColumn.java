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
import org.geotools.gui.swing.map.map2d.stream.SelectableMap2D;
import org.geotools.map.MapLayer;

/**
 * Default selection column.
 * 
 * @author Johann Sorel
 */
public final class SelectionTreeTableColumn extends TreeTableColumn {
    
    private SelectableMap2D map;
    private SelectionComponent renderComp = new SelectionComponent();
    private SelectionComponent editComp = new SelectionComponent();
    
    
    /**
     * column with checkbox for jcontexttree
     * @param map 
     */
    public SelectionTreeTableColumn(SelectableMap2D map) {
                
        setCellEditor( new DefaultCellEditor(renderComp));
        setCellRenderer( new DefaultCellRenderer(editComp));
                
        String name = BUNDLE.getString("col_selection");                
        setHeaderValue( new HeaderInfo(name,null,IconBundle.getResource().getIcon("16_select") ));
        
        setEditable(true);
        setResizable(false);
        setMaxWidth(25);
        setMinWidth(25);
        setPreferredWidth(25);
        setWidth(25);
        
        setEditableOnMouseOver(true);
        
        setMap(map);
    }
         
    public void setMap(SelectableMap2D map){
        this.map = map;
        editComp.setMap(map);
        renderComp.setMap(map);
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
