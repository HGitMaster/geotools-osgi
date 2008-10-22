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

import java.awt.Color;

import java.util.Map;
import javax.swing.ListSelectionModel;

import org.geotools.gui.swing.contexttree.column.StyleComponent;
import org.geotools.gui.swing.contexttree.renderer.DefaultCellRenderer;
import org.geotools.styling.Symbolizer;

/**
 * Demo panel
 * 
 * @param T 
 * @author Johann Sorel
 */
public class JDemoTable<T extends Symbolizer> extends org.jdesktop.swingx.JXTable {

    private DemoTableModel model;

    /**
     * Table for style exemple
     */
    public JDemoTable() {
        super(new DemoTableModel<T>());
        init();        
    }
        
    private void init(){
        model = (DemoTableModel) getModel();

        setHorizontalScrollEnabled(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getColumnExt(0).setCellRenderer(new DefaultCellRenderer(new StyleComponent()));
        getColumnExt(0).setMaxWidth(25);
        getColumnExt(0).setMinWidth(25);
        getColumnExt(0).setPreferredWidth(25);
        getColumnExt(0).setWidth(25);
        setTableHeader(null);
        setGridColor(Color.LIGHT_GRAY);
        setShowVerticalLines(false);
        setColumnMargin(0);
        setRowMargin(0);
    }

    /**
     * @param map 
     */
    public void setMap(Map<T, String> map) {
        model.setMap(map);
    }

    public Map<T, String> getMap() {
        return model.getMap();
    }
}
