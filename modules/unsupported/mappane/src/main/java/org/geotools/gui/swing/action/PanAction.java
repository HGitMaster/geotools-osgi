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

import java.awt.event.ActionEvent;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.tool.PanTool;
import org.geotools.gui.swing.tool.MapTool;

/**
 * An action for connect a control (probably a JButton) to
 * the PanTool for panning the map with mouse drags.
 * 
 * @author Michael Bedward
 * @since 2.6
 */
public class PanAction extends MapAction {
    
    /**
     * Constructor - when used with a JButton the button will
     * display a small icon only
     * 
     * @param pane the map pane being serviced by this action
     */
    public PanAction(JMapPane pane) {
        this(pane, MapTool.SMALL_ICON, false);
    }

    /**
     * Constructor
     * 
     * @param pane the map pane being serviced by this action
     * @param toolIcon specifies which, if any, icon the control (e.g. JButton)
     * will display; one of MapTool.NO_ICON, MapTool.SMALL_ICON or
     * MapTool.LARGE_ICON.
     * @param showToolName set to true for the control to display the tool name
     */
    public PanAction(JMapPane pane, int toolIcon, boolean showToolName) {
        String toolName = showToolName ? PanTool.TOOL_NAME : null;
        
        String iconImagePath = null;
        switch (toolIcon) {
            case MapTool.LARGE_ICON:
                iconImagePath = PanTool.ICON_IMAGE_LARGE;
                break;
                
            case MapTool.SMALL_ICON:
                iconImagePath = PanTool.ICON_IMAGE_SMALL;
                break;
        }
        
        super.init(pane, toolName, PanTool.TOOL_TIP, iconImagePath);
    }
    
    /**
     * Called when the associated control is activated. Leads to the
     * map pane's cursor tool being set to a PanTool object
     */
    public void actionPerformed(ActionEvent e) {
        pane.setCursorTool(new PanTool());
    }

}
