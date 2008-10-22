/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing.process;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SpringLayout;

import org.geotools.data.Parameter;
import org.geotools.process.ProcessFactory;

import com.vividsolutions.jts.geom.Geometry;

/**
 * This page is responsible making a user interface based on the provided ProcessFactory.
 * 
 * @author Jody
 */
public class InputParameterPage extends JPage {
    ProcessFactory factory;
    Map<String, Object> input;
    Map<String, ParamWidget> fields = new HashMap<String, ParamWidget>();

    public InputParameterPage( ProcessFactory factory ) {
        this(factory, null);
    }
    public InputParameterPage( ProcessFactory factory, Map<String, Object> input ) {
        super("input");
        this.factory = factory;
    }

    public String getBackPageIdentifier() {
        return DEFAULT;
    }
    public String getNextPageIdentifier() {
        return FINISH;
    }
    public void aboutToDisplayPanel() {
        page.removeAll();
        page.setLayout(new GridLayout(0, 2));

        JLabel title = new JLabel(factory.getTitle().toString());
        page.add(title);
        JLabel description = new JLabel(factory.getDescription().toString());
        page.add(description);
        for( Entry<String, Parameter< ? >> entry : factory.getParameterInfo().entrySet() ) {
            Parameter< ? > parameter = entry.getValue();
            JLabel label = new JLabel(parameter.key);
            page.add(label);

            ParamWidget widget;
            if (Double.class.isAssignableFrom( parameter.type )) {
                widget = new JDoubleField(parameter);
            } else if (Geometry.class.isAssignableFrom( parameter.type)){
                widget = new JGeometryField(parameter);
            }
            else {
                // We got nothing special hope the converter api can deal
                widget = new JField( parameter );
            }
            JComponent field = widget.doLayout();
            page.add(field);
            
            fields.put( parameter.key, widget );
        }
    }
}
