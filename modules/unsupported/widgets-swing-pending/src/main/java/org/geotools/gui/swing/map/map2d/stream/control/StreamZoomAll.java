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
import java.io.IOException;

import javax.swing.AbstractAction;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gui.swing.map.map2d.stream.StreamingMap2D;
import org.geotools.map.MapContext;

/**
 * Zoom all action
 * 
 * @author Johann Sorel
 */
public class StreamZoomAll extends AbstractAction {

    private StreamingMap2D map = null;

    public void actionPerformed(ActionEvent arg0) {
        if (map != null) {


            MapContext context = map.getRenderingStrategy().getContext();

            try {
                ReferencedEnvelope env = context.getLayerBounds();

                if (env != null) {
                    map.getRenderingStrategy().setMapArea(env);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public StreamingMap2D getMap() {
        return map;
    }

    public void setMap(StreamingMap2D map) {
        this.map = map;
        setEnabled(map != null);
    }
}
