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
package org.geotools.gui.swing.contexttree.renderer;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * DefaultCellEditot for JContextTree columns
 * 
 * @author Johann Sorel
 */
public class DefaultCellEditor extends AbstractCellEditor implements TableCellEditor{

    private RenderAndEditComponent view = null;
    
    /**
     * DefaultCellEditot for JContextTree columns
     * @param view
     */
    public DefaultCellEditor(RenderAndEditComponent view){
        this.view = view;
    }
        
    public Object getCellEditorValue() {
        return view.getValue();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        view.parse(value);
        return view;
    }
    
    
    
}
