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
package org.geotools.gui.swing.map.map2d.stream;

import java.util.ArrayList;
import java.util.List;

import org.geotools.gui.swing.map.map2d.stream.event.StrategyEvent;
import org.geotools.gui.swing.map.map2d.stream.handler.NavigationHandler;
import org.geotools.gui.swing.map.map2d.stream.listener.NavigationListener;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.gui.swing.map.map2d.stream.event.NavigationEvent;
import org.geotools.gui.swing.map.map2d.stream.handler.DefaultPanHandler;
import org.geotools.gui.swing.map.map2d.stream.strategy.StreamingStrategy;

/**
 * Default implementation of NavigableMap2D
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/map/map2d/stream/JStreamNavMap.java $
 */
public class JStreamNavMap extends JStreamMap implements NavigableMap2D {

    private final List<Envelope> mapAreas = new ArrayList<Envelope>();
    private Envelope lastMapArea = null;
    private NavigationHandler navigationHandler = new DefaultPanHandler();

    /**
     * create a default JDefaultNavigableMap2D
     */
    public JStreamNavMap() {
        super();
    }
    
    private void fireHandlerChanged(NavigationHandler oldhandler, NavigationHandler newhandler) {
        NavigationEvent mce = new NavigationEvent(this, oldhandler, newhandler);

        NavigationListener[] lst = getNavigableMap2DListeners();

        for (NavigationListener l : lst) {
            l.navigationHandlerChanged(mce);
        }

    }
    
    //----------------------Map2d override--------------------------------------
    @Override
    protected void mapContextChanged(StrategyEvent event) {
        super.mapContextChanged(event);        
        mapAreas.clear();
        lastMapArea = getRenderingStrategy().getMapArea();
    }

    @Override
    protected void mapAreaChanged(StrategyEvent event) {
        super.mapAreaChanged(event);

        while (mapAreas.size() > 10) {
            mapAreas.remove(0);
        }

        Envelope newMapArea = event.getMapArea();
        lastMapArea = newMapArea;

        if (!mapAreas.contains(newMapArea)) {
            mapAreas.add(newMapArea);
        } 

    }

    @Override
    public void setRenderingStrategy(StreamingStrategy stratege) {
        if (actionState == ACTION_STATE.NAVIGATE && navigationHandler.isInstalled()) {
            navigationHandler.uninstall();
        }
        
        super.setRenderingStrategy(stratege);
        
        if (actionState == ACTION_STATE.NAVIGATE) {
            navigationHandler.install(this);
        }

    }
    
    @Override
    public void setActionState(ACTION_STATE state) {
                        
        if (state == ACTION_STATE.NAVIGATE && !navigationHandler.isInstalled()) {
            navigationHandler.install(this);
        } else if (state != ACTION_STATE.NAVIGATE && navigationHandler.isInstalled()) {
            navigationHandler.uninstall();
        }
        
        super.setActionState(state);

    }
    
    //-----------------------NAVIGABLEMAP2D-------------------------------------
        
    public void setNavigationHandler(NavigationHandler newHandler) {
        if (newHandler == null) {
            throw new NullPointerException();
        } else if (newHandler != navigationHandler) {

            NavigationHandler oldHandler = navigationHandler;
            
            if (navigationHandler.isInstalled()) {
                navigationHandler.uninstall();
            }

            navigationHandler = newHandler;

            if (actionState == ACTION_STATE.NAVIGATE) {
                navigationHandler.install(this);
            }

            fireHandlerChanged(oldHandler,newHandler);
        }
    }

    public NavigationHandler getNavigationHandler() {
        return navigationHandler;
    }

    public void previousMapArea() {
        if (lastMapArea != null) {
            int index = mapAreas.indexOf(lastMapArea);

            index--;
            if (index >= 0) {
                getRenderingStrategy().setMapArea(mapAreas.get(index));
            }
        }
    }

    public void nextMapArea() {
        if (lastMapArea != null) {
            int index = mapAreas.indexOf(lastMapArea);

            index++;
            if (index < mapAreas.size()) {
                getRenderingStrategy().setMapArea(mapAreas.get(index));
            }
        }
    }

    public void addNavigableMap2DListener(NavigationListener listener) {
        MAP2DLISTENERS.add(NavigationListener.class, listener);
    }

    public void removeNavigableMap2DListener(NavigationListener listener) {
        MAP2DLISTENERS.remove(NavigationListener.class, listener);
    }

    public NavigationListener[] getNavigableMap2DListeners() {
        return MAP2DLISTENERS.getListeners(NavigationListener.class);
    }
}
