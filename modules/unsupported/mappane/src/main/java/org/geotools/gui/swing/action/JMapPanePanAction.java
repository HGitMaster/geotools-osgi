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
import org.geotools.gui.swing.tool.JMapPanePanTool;
import org.geotools.gui.swing.tool.JMapPaneTool;

/**
 * An action for connect a control (probably a JButton) to
 * the JMapPanePanTool for panning the map with mouse drags.
 * 
 * @author Michael Bedward
 * @since 2.6
 */
public class JMapPanePanAction extends JMapPaneAction {
    
    /**
     * Constructor - when used with a JButton the button will
     * display a small icon only
     * 
     * @param pane the map pane being serviced by this action
     */
    public JMapPanePanAction(JMapPane pane) {
        this(pane, JMapPaneTool.SMALL_ICON, false);
    }

    /**
     * Constructor
     * 
     * @param pane the map pane being serviced by this action
     * @param toolIcon specifies which, if any, icon the control (e.g. JButton)
     * will display; one of JMapPaneTool.NO_ICON, JMapPaneTool.SMALL_ICON or
     * JMapPaneTool.LARGE_ICON.
     * @param showToolName set to true for the control to display the tool name
     */
    public JMapPanePanAction(JMapPane pane, int toolIcon, boolean showToolName) {
        String toolName = showToolName ? JMapPanePanTool.TOOL_NAME : null;
        
        String iconImagePath = null;
        switch (toolIcon) {
            case JMapPaneTool.LARGE_ICON:
                iconImagePath = JMapPanePanTool.ICON_IMAGE_LARGE;
                break;
                
            case JMapPaneTool.SMALL_ICON:
                iconImagePath = JMapPanePanTool.ICON_IMAGE_SMALL;
                break;
        }
        
        super.init(pane, toolName, JMapPanePanTool.TOOL_TIP, iconImagePath);
    }
    
    /**
     * Called when the associated control is activated. Leads to the
     * map pane's cursor tool being set to a JMapPanePanTool object
     */
    public void actionPerformed(ActionEvent e) {
        pane.setCursorTool(new JMapPanePanTool());
    }

}
