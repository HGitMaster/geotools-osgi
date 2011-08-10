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
 * Style column, glyph legend
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/contexttree/column/StyleTreeTableColumn.java $
 */
public final class StyleTreeTableColumn extends TreeTableColumn {
    
    
    /**
     * Creates a new instance 
     */
    public StyleTreeTableColumn() {
        super();
        
        setCellEditor( new DefaultCellEditor( new StyleComponent()));
        setCellRenderer( new DefaultCellRenderer( new StyleComponent()));
                
        String name = BUNDLE.getString("col_symbol");                
        setHeaderValue( new HeaderInfo(name,null,IconBundle.getResource().getIcon("16_style") ));
                
        setEditable(true);
        setResizable(false);
        setMaxWidth(25);
        setMinWidth(25);
        setPreferredWidth(25);
        setWidth(25);
        
        setEditableOnMouseOver(true);
    }
    
    
    
    public void setValue(Object target, Object value) {}
    
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

    
    

