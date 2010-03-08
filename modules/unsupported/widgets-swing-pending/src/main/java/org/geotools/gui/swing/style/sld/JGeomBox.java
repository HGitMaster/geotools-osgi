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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.geotools.map.MapLayer;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;

/**
 * Geometrie box attribut
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/style/sld/JGeomBox.java $
 */
public class JGeomBox extends JComboBox {

    private MapLayer layer = null;
    private String selectedGeom = "";

    public JGeomBox() {
        setEnabled(false);
        setOpaque(false);
    }

    public void setLayer(MapLayer layer) {

        this.layer = layer;

        setEnabled(layer != null);

        if (layer != null) {
            Collection<PropertyDescriptor> col = layer.getFeatureSource().getSchema().getDescriptors();
            Iterator<PropertyDescriptor> ite = col.iterator();

            List<String> geoms = new ArrayList<String>();

            while (ite.hasNext()) {
                PropertyDescriptor desc = ite.next();
                if (desc instanceof GeometryDescriptor) {
                    geoms.add(desc.getName().toString());
                }
            }

            ComboBoxModel model = new GeoModel(geoms);
            setModel(model);

            if (!selectedGeom.equals("")) {
                setSelectedItem(selectedGeom);
            } else {
                setSelectedIndex(0);
            }
        }

    }

    public MapLayer getLayer() {
        return layer;
    }

    public String getGeom() {
        return (String) getSelectedItem();
    }

    public void setGeom(String name) {
        selectedGeom = name;
        if (layer != null) {
            setSelectedItem(name);
        }
    }
}

class GeoModel extends DefaultComboBoxModel {

    private List<String> geoms;

    GeoModel(List<String> geoms) {
        this.geoms = geoms;
    }

    @Override
    public int getSize() {
        return geoms.size();
    }

    @Override
    public Object getElementAt(int index) {
        return geoms.get(index);
    }
}

