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
package org.geotools.gui.swing.style.sld;

import java.util.HashMap;
import java.util.Map;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.geotools.styling.Symbolizer;

/**
 * demo table model
 * 
 * @param T 
 * @author Johann Sorel
 */
public class DemoTableModel<T extends Symbolizer> extends AbstractTableModel implements TableModel {
    
    private Map<T,String> map = new HashMap<T,String>();

    /**
     * 
     * @param demofile sld file containing style exemples
     */
    public DemoTableModel() {
        super();

//        Configuration configuration = new SLDConfiguration();
//        Parser parser = new Parser(configuration);
//
//        InputStream input = DemoTableModel.class.getResourceAsStream(demofile);
//
//        try {
//            BUNDLE = ResourceBundle.getBundle("org/geotools/gui/swing/propertyedit/styleproperty/defaultset/Bundle");
//            
//            StyledLayerDescriptor sld = (StyledLayerDescriptor) parser.parse( input );
//            map = SLD.styles(sld)[0].getFeatureTypeStyles()[0].getRules();
//        } catch (Exception e) {
//            map = new Rule[0];
////            e.printStackTrace();
//        }
    }
    
    public void setMap(Map<T,String> map){
        this.map = new HashMap<T, String>(map);
        fireTableDataChanged();
    }
    
    public Map<T,String> getMap(){
        return new HashMap<T, String>(map);
    }
    

    public int getRowCount() {
        return map.size();
    }

    public int getColumnCount() {
        return 2;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
                
        if (columnIndex == 0) {
            return map.keySet().toArray()[rowIndex];
        } else if (columnIndex == 1) {                   
            return map.get(map.keySet().toArray()[rowIndex]);
        }
        return "n/a";
    }

    @Override
    public String getColumnName(int columnIndex) {
        return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }
}
