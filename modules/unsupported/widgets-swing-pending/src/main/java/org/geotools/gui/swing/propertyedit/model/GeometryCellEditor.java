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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.vividsolutions.jts.geom.Geometry;

/**
 * geometry cell editor
 * 
 * @author Johann Sorel
 */
public class GeometryCellEditor extends AbstractCellEditor implements TableCellEditor{
    
    private JButton button = new JButton();
    private Geometry geom;
    
    /**
     * Creates a new instance of GeometryCellEditor
     */
    public GeometryCellEditor(){
        super();
        //button.setText( TextBundle.getResource().getString("edit"));
        
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                JPropertyDialog dia = new JPropertyDialog();
//                GeometryPanel edit = new GeometryPanel();
//                dia.addEditPanel(edit);
//                dia.setVisible(true);
            }
        });
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        geom = (Geometry)value;
        
        return button;
    }

    public Object getCellEditorValue() {
        return geom;
    }

       
    
}
