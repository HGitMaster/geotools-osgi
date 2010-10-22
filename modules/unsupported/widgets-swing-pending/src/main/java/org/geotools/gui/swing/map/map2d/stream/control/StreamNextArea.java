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
package org.geotools.gui.swing.map.map2d.stream.control;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.gui.swing.map.map2d.stream.NavigableMap2D;

/**
 * Next area action
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/map/map2d/stream/control/StreamNextArea.java $
 */
public class StreamNextArea extends AbstractAction {

    private Map2D map = null;

    public void actionPerformed(ActionEvent arg0) {
        if (map != null && map instanceof NavigableMap2D) {
            ((NavigableMap2D) map).nextMapArea();
        }
    }

    public Map2D getMap() {
        return map;
    }

    public void setMap(Map2D map) {
        this.map = map;
        setEnabled(map != null && map instanceof NavigableMap2D);
    }
}
