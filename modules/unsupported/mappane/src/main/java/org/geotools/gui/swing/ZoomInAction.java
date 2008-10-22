/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;


public class ZoomInAction extends AbstractAction {
    /**
     * a simple zoom in action
     */
    private static final long serialVersionUID = 5757407203303739037L;
    private ImageIcon icon;
    JMapPane map;

    public ZoomInAction(JMapPane map) {
        URL url = this.getClass().getResource("resources/ZoomIn16.gif"); //$NON-NLS-1$
        icon = new ImageIcon(url);
        this.putValue(Action.SMALL_ICON, icon);
        this.putValue(Action.NAME, Messages.getString("ZoomInAction.1")); //$NON-NLS-1$

        this.map = map;
    }

    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        map.setState(JMapPane.ZoomIn);
        map.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
}
