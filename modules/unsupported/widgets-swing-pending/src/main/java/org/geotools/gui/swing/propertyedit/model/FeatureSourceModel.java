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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureIterator;
import org.geotools.map.MapLayer;
import org.jdesktop.swingx.JXTable;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

/**
 * Feature source model
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/propertyedit/model/FeatureSourceModel.java $
 */
public class FeatureSourceModel implements TableModel {

    private ArrayList<PropertyDescriptor> columns = new ArrayList<PropertyDescriptor>();
    private ArrayList<Feature> features = new ArrayList<Feature>();
    private MapLayer layer;
    private JXTable tab;
    private Query query = Query.ALL; 

    /** Creates a new instance of BasicTableModel
     * @param tab
     * @param layer 
     */
    public FeatureSourceModel(JXTable tab, MapLayer layer) {
        super();
        this.tab = tab;
        this.layer = layer;

        setQuery(layer.getQuery());
    }

    public void setQuery(Query query) {
        this.query = query;
        
        columns.clear();
        features.clear();

        FeatureType ft = layer.getFeatureSource().getSchema();

        Collection<PropertyDescriptor> cols = ft.getDescriptors();
        Iterator<PropertyDescriptor> ite = cols.iterator();

        while (ite.hasNext()) {
            columns.add(ite.next());
        }
        
        try {
            FeatureIterator<SimpleFeature> fi = (FeatureIterator<SimpleFeature>) layer.getFeatureSource().getFeatures(query.getFilter()).features();            
            while (fi.hasNext()) {
                features.add(fi.next());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    public int getColumnCount() {
        return columns.size();
    }

    public Class getColumnClass(int column) {
        return columns.get(column).getType().getBinding();
    }

    public String getColumnName(int column) {
        return columns.get(column).getName().toString();
    }

    public int getRowCount() {
        return features.size();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return features.get(rowIndex).getProperty(columns.get(columnIndex).getName()).getValue();
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        FeatureStore<SimpleFeatureType, SimpleFeature> store;
        if (layer.getFeatureSource() instanceof FeatureStore) {

            store = (FeatureStore<SimpleFeatureType, SimpleFeature>) layer.getFeatureSource();
            DefaultTransaction transaction = new DefaultTransaction("trans_maj");


            store.setTransaction(transaction);
            FilterFactory ff = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());
            Filter filter = ff.id(Collections.singleton(features.get(rowIndex).getIdentifier()));            
            FeatureType schema = store.getSchema();
            
            AttributeDescriptor NAME = (AttributeDescriptor) schema.getDescriptor(getColumnName(columnIndex));
                        
            try {
                store.modifyFeatures(NAME, aValue, filter);
                transaction.commit();
            } catch (IOException ex) {
                ex.printStackTrace();
                try {
                    transaction.rollback();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            setQuery(query);

        }
    }

    public void addTableModelListener(TableModelListener l) {
    }

    public void removeTableModelListener(TableModelListener l) {
    }
}
