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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.geotools.data.Parameter;
import org.geotools.text.Text;
import org.geotools.util.Converters;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Field that uses the converter API to hack away at a text representation of the provided value.
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/process/JField.java $
 */
public class JField extends AbstractParamWidget {
    private JTextArea text;

    public JField( Parameter< ? > parameter ) {
        super(parameter);
    }

    public JComponent doLayout() {
        text = new JTextArea(40, 2);
        text.addKeyListener(new KeyAdapter(){
            public void keyReleased( KeyEvent e ) {
                validate();
            }
        });
        text.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(400, 80));
        return scroll;
    }

    public Object getValue() {
        String txt = text.getText();
        if (txt.length() == 0) {
            return null;
        }
        Object value = Converters.convert(txt, parameter.type);
        return value;
    }

    /**
     * Determine the number of dimensions based on the CRS metadata.
     * 
     * @return Number of dimensions expected based on metadata, default of 2
     */
    int getD() {
        CoordinateReferenceSystem crs = (CoordinateReferenceSystem) parameter.metadata
                .get(Parameter.CRS);
        if (crs == null) {
            return 2;
        } else {
            return crs.getCoordinateSystem().getDimension();
        }
    }

    public void setValue( Object value ) {
        String txt = (String) Converters.convert(value, String.class);

        text.setText(txt);
    }

    public boolean validate() {
        String txt = text.getText();
        if (txt.length() == 0) {
            return true;
        }
        Object value = Converters.convert(txt, parameter.type);
        if (value == null) {
            text.setToolTipText( "Could not create "+parameter.type );
            text.setForeground(Color.RED);
            return false;
        } else {
            text.setToolTipText(null);
            text.setForeground(Color.BLACK);
            return true;
        }
    }

}
