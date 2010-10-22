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

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

/**
 * DataBase model
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/datachooser/model/DBModel.java $
 */
public class DBModel implements TableModel{
    
    
    private ArrayList<String> datas = new ArrayList<String>();
    private JXTable tab;
    
    /** Creates a new instance of BasicTableModel 
     * @param tab 
     */
    public DBModel(JXTable tab) {
        super();
        this.tab = tab;
        init();
    }
    
    private void init(){
        tab.revalidate();
    }
    
    
    public void removeSelected(){
        for(int i=tab.getSelectedRows().length-1; i>=0; i--){            
            datas.remove(tab.getSelectedRows()[i]);
        }
        tab.revalidate();
        tab.repaint();
    }
    
    public void add(String name){
        datas.add(name);
        tab.revalidate();
        tab.repaint();
    }
    
    public void add(String[] names){
        for(int i=0;i<names.length;i++)
            datas.add(names[i]);
        
        tab.revalidate();
        tab.repaint();
    }
    
    public void clean(){
        datas.clear();
        tab.revalidate();
        tab.repaint();
    }
    
    public List<String> getNames(){
        return datas;
    }
    
    public int getColumnCount(){
        return 1;
    }
    
    public Class getColumnClass(int i){
        return String.class;
    }
    
    public String getColumnName(int column) {
        return "";
    }
    
    public int getRowCount() {
        return datas.size();
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        return datas.get(rowIndex);
    }
            
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
    public void addTableModelListener(TableModelListener l) {}
    public void removeTableModelListener(TableModelListener l) {}
    
}
