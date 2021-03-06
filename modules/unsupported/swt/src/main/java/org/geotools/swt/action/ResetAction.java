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

package org.geotools.swt.action;

import org.geotools.swt.SwtMapPane;
import org.geotools.swt.utils.ImageCache;
import org.geotools.swt.utils.Messages;

/**
 * Action that triggers view reset for the current {@link SwtMapPane map pane}.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/unsupported/swt/src/main/java/org/geotools/swt/action/ResetAction.java $
 */
public class ResetAction extends MapAction {
    /** Name for this tool */
    public static final String TOOL_NAME = Messages.getString("tool_name_reset");
    /** Tool tip text */
    public static final String TOOL_TIP = Messages.getString("tool_tip_reset");

    public ResetAction() {
        super(TOOL_NAME + "@A", TOOL_TIP, ImageCache.getInstance().getImage(ImageCache.IMAGE_FULLEXTENT));
    }

    public void run() {
        getMapPane().reset();
    }

}
