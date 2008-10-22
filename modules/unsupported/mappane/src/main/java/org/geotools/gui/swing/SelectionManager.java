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


import javax.swing.event.EventListenerList;

import org.geotools.gui.swing.event.SelectionChangeListener;
import org.geotools.gui.swing.event.SelectionChangedEvent;
import org.geotools.map.MapLayer;
import org.opengis.filter.Filter;


/**
 * a simple selection manager
 * @author Ian Turton
 */
public class SelectionManager {
    EventListenerList listeners = new EventListenerList();

    /**
     * the layer to select
     */
    MapLayer selectionLayer;


    public SelectionManager(MapLayer layer) {
        setSelectionLayer(layer);
    }


    /**
     * add a SelectionChangedListener to this manager, note this need not
     * be a map which is why we pass a filter back not a map point.
     * @param l - the listener
     */
    public void addSelectionChangeListener(SelectionChangeListener l) {
        listeners.add(SelectionChangeListener.class, l);
    }

    /**
     * Remove a selectionlistener
     * @param l - the listener
     */
    public void removeSelectionChangeListener(SelectionChangeListener l) {
        listeners.remove(SelectionChangeListener.class, l);
    }

    /**
     * notify listeners that the selection has changed
     * @param source - where the mousemovement came from
     * @param filter - the filter which selects the
     * selected feature(s)
     */
    public void selectionChanged(Object source, Filter filter) {
        SelectionChangeListener[] l = (SelectionChangeListener[]) listeners.getListeners(SelectionChangeListener.class);
        SelectionChangedEvent ev = new SelectionChangedEvent(source, filter);

        for (int i = 0; i < l.length; i++) {
            l[i].selectionChanged(ev);
        }
    }

    /**
     * get the selected layer
     * @return - the layer
     */
    public MapLayer getSelectionLayer() {
        return selectionLayer;
    }

    /**
     * sets the selection layer
     * @param selectionLayer - the layer
     */
    public void setSelectionLayer(MapLayer selectionLayer) {
        this.selectionLayer = selectionLayer;

    }
}
