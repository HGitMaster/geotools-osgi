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

package org.geotools.gui.swing.action;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.geotools.gui.swing.JMapPane;


/**
 * Base class for map pane actions; just provides a common initializing method and
 * a reference to the map pane being serviced.
 * 
 * @author Michael Bedward
 * @since 2.6
 */
public abstract class MapAction extends AbstractAction {

    protected JMapPane pane;

    protected void init(JMapPane pane, String toolName, String toolTip, String iconImagePath) {
        this.pane = pane;

        if (toolName != null) {
            this.putValue(Action.NAME, toolName);
        }

        this.putValue(Action.SHORT_DESCRIPTION, toolTip);

        if (iconImagePath != null) {
            this.putValue(Action.SMALL_ICON, new ImageIcon(MapAction.class.getResource(iconImagePath)));
        }
    }
}
