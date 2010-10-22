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
package org.geotools.gui.swing.datachooser.model;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.geotools.data.DataAccessFactory.Param;

/**
 * Keys model
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/datachooser/model/KeyModel.java $
 */
public class KeyModel implements TableModel {

    private Param[] param ={};
    private Object[] value = {};
    private JTable tab;

    /** Creates a new instance of BasicTableModel 
     * @param tab 
     */
    public KeyModel(JTable tab) {
        super();
        this.tab = tab;
        init();
    }

    private void init() {
        tab.revalidate();
    }

    public void setParam(Param[] param) {
        this.param = param;
        tab.revalidate();
        tab.repaint();
        
        value = new Object[param.length];
        for(int i=0;i<param.length;i++){
            value[i] = param[i].sample;
        }
        
    }

    public Map getProperties() {

        Map config = new HashMap();
        
        for(int i=0;i<param.length;i++){
            config.put(param[i].key, value[i]);
        }
        
        return config;
    }
    
    public void parse(Map map){
        
        for(int i=0;i<getRowCount();i++){            
            Object key = getValueAt(i, 0);            
            if(map.containsKey(key)){                
                setValueAt(map.get(key), i, 1);
            }            
        }
        
    }
    

    public int getColumnCount() {
        return 2;
    }

    public Class getColumnClass(int i) {
        return String.class;
    }

    public String getColumnName(int column) {
        if (column == 0) {
            return "key";
        } else {
            return "value";
        }
    }

    public int getRowCount() {
        return param.length;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex > 0) {
            return true;
        } else {
            return false;
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {

        if (columnIndex == 0) {
            return param[rowIndex].key;
        } else {
            return value[rowIndex];
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        value[rowIndex] = aValue;
    }

    public void addTableModelListener(TableModelListener l) {
    }

    public void removeTableModelListener(TableModelListener l) {
    }
}
