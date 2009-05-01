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
import org.geotools.gui.swing.tool.CursorTool;
import org.geotools.gui.swing.tool.ZoomInTool;

/**
 * An action for connect a control (probably a JButton) to
 * the ZoomInTool for zooming the map with mouse clicks
 * or drags.
 * 
 * @author Michael Bedward
 * @since 2.6
 */
public class ZoomInAction extends MapAction {
    
    /**
     * Constructor - when used with a JButton the button will
     * display a small icon only
     * 
     * @param pane the map pane being serviced by this action
     */
    public ZoomInAction(JMapPane pane) {
        this(pane, CursorTool.SMALL_ICON, false);
    }

    /**
     * Constructor
     * 
     * @param pane the map pane being serviced by this action
     * @param toolIcon specifies which, if any, icon the control (e.g. JButton)
     * will display; one of CursorTool.NO_ICON, CursorTool.SMALL_ICON or
     * CursorTool.LARGE_ICON.
     * @param showToolName set to true for the control to display the tool name
     */
    public ZoomInAction(JMapPane pane, int toolIcon, boolean showToolName) {
        String toolName = showToolName ? ZoomInTool.TOOL_NAME : null;
        
        String iconImagePath = null;
        switch (toolIcon) {
            case CursorTool.LARGE_ICON:
                iconImagePath = ZoomInTool.ICON_IMAGE_LARGE;
                break;
                
            case CursorTool.SMALL_ICON:
                iconImagePath = ZoomInTool.ICON_IMAGE_SMALL;
                break;
        }
        
        super.init(pane, toolName, ZoomInTool.TOOL_TIP, iconImagePath);
    }
    
    /**
     * Called when the associated control is activated. Leads to the
     * map pane's cursor tool being set to a new ZoomInTool object
     */
    public void actionPerformed(ActionEvent e) {
        pane.setCursorTool(new ZoomInTool(pane));
    }

}
