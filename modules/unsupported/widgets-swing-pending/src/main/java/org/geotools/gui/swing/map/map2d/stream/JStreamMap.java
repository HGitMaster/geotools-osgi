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

import com.vividsolutions.jts.geom.Envelope;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.EventListenerList;

import org.geotools.gui.swing.map.map2d.AbstractMap2D;
import org.geotools.gui.swing.map.map2d.stream.event.StrategyEvent;
import org.geotools.gui.swing.map.map2d.stream.listener.MapListener;
import org.geotools.gui.swing.map.map2d.stream.listener.StrategyListener;
import org.geotools.gui.swing.map.map2d.stream.strategy.StreamingStrategy;
import org.geotools.gui.swing.map.map2d.stream.strategy.SingleBufferedImageStrategy;
import org.geotools.map.MapContext;
import org.geotools.gui.swing.map.map2d.stream.event.MapEvent;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;

/**
 * Default implementation of Map2D
 * 
 * @author Johann Sorel
 */
abstract class JStreamMap extends AbstractMap2D implements StreamingMap2D, MapLayerListListener, PropertyChangeListener {

    /**
     * Action state of the map widget
     */
    protected ACTION_STATE actionState = ACTION_STATE.NONE;
    /**
     * EventListenerList to manage all possible Listeners
     */
    protected final EventListenerList MAP2DLISTENERS = new EventListenerList();
    /**
     * Map2D reference , same as "this" but needed to explicitly point to the 
     * map2d object when coding a private class
     */
    protected final JStreamMap THIS_MAP;
    /**
     * Rendering Strategy of the map2d widget, should never be null
     */
    protected StreamingStrategy renderingStrategy = new SingleBufferedImageStrategy();
    
    private final StrategyListener strategylisten = new StrategyListen();

    /**
     * create a default JDefaultMap2D
     */
    protected JStreamMap() {
        super();
        this.THIS_MAP = this;
        setMapComponent(renderingStrategy.getComponent());
    }
    
    private void fireStrategyChanged(StreamingStrategy oldOne, StreamingStrategy newOne) {
        MapEvent mce = new MapEvent(this, actionState, oldOne, newOne);

        MapListener[] lst = getMap2DListeners();

        for (MapListener l : lst) {
            l.mapStrategyChanged(mce);
        }

    }

    private void fireActionStateChanged(ACTION_STATE oldone, ACTION_STATE newone) {
        MapEvent mce = new MapEvent(this, oldone, newone, renderingStrategy);

        MapListener[] lst = getMap2DListeners();

        for (MapListener l : lst) {
            l.mapActionStateChanged(mce);
        }

    }

    //----------------------Use as extend for subclasses------------------------
    protected void mapAreaChanged(StrategyEvent event) {

    }

    protected void mapContextChanged(StrategyEvent event) {
        event.getPreviousContext().removePropertyChangeListener(this);
        event.getContext().addPropertyChangeListener(this);
        
        event.getPreviousContext().removeMapLayerListListener(this);
        event.getContext().addMapLayerListListener(this);
    }

    public void propertyChange(PropertyChangeEvent arg0) {

    }


    //-----------------------------MAP2D----------------------------------------
    public void dispose(){
        renderingStrategy.getContext().removePropertyChangeListener(this);
        renderingStrategy.getContext().removeMapLayerListListener(this);
        renderingStrategy.dispose();
    }
    
    public void setActionState(ACTION_STATE newstate) {

        if (actionState != newstate) {
            ACTION_STATE oldstate = actionState;
            actionState = newstate;
            fireActionStateChanged(oldstate, newstate);
        }

    }

    public ACTION_STATE getActionState() {
        return actionState;
    }

    public void setRenderingStrategy(StreamingStrategy newStrategy) {

        if (newStrategy == null) {
            throw new NullPointerException();
        }

        StreamingStrategy oldStrategy = renderingStrategy;

        //removing old strategy
        MapContext context = renderingStrategy.getContext();
        Envelope area = renderingStrategy.getMapArea();
        renderingStrategy.removeStrategyListener(strategylisten);
        renderingStrategy.dispose();

        //adding new strategy
        renderingStrategy = newStrategy;
        renderingStrategy.addStrategyListener(strategylisten);
        renderingStrategy.setContext(context);
        
        setMapComponent(renderingStrategy.getComponent());
        
        renderingStrategy.setMapArea(area);

        fireStrategyChanged(oldStrategy, newStrategy);

    }

    public StreamingStrategy getRenderingStrategy() {
        return renderingStrategy;
    }

    public void addMap2DListener(MapListener listener) {
        MAP2DLISTENERS.add(MapListener.class, listener);
    }

    public void removeMap2DListener(MapListener listener) {
        MAP2DLISTENERS.remove(MapListener.class, listener);
    }

    public MapListener[] getMap2DListeners() {
        return MAP2DLISTENERS.getListeners(MapListener.class);
    }

    //---------------------- PRIVATE CLASSES------------------------------------    
    
    private class StrategyListen implements StrategyListener {

        public void setRendering(boolean rendering) {
            THIS_MAP.setRendering(rendering);
        }

        public void mapContextChanged(StrategyEvent event) {
            THIS_MAP.mapContextChanged(event);
        }

        public void mapAreaChanged(StrategyEvent event) {
            THIS_MAP.mapAreaChanged(event);
        }
    }

    //--------------------MapLayerListListener----------------------------------
    public void layerAdded(MapLayerListEvent event) {
    }

    public void layerRemoved(MapLayerListEvent event) {
    }

    public void layerChanged(MapLayerListEvent event) {
    }

    public void layerMoved(MapLayerListEvent event) {
    }
}

