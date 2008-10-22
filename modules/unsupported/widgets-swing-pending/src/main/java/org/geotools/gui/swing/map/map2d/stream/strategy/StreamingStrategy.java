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
package org.geotools.gui.swing.map.map2d.stream.strategy;

import com.vividsolutions.jts.geom.Coordinate;
import java.awt.image.BufferedImage;

import org.geotools.gui.swing.map.map2d.stream.listener.StrategyListener;
import org.geotools.map.MapContext;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Component;
import java.awt.Point;
import java.awt.geom.AffineTransform;

/**
 * Rendering Strategy is used to specify a memory management
 * technic and different solutions to answer a need (low memory, high drawing speed, smooth edition ...)
 * 
 * @author Johann Sorel
 */
public interface StreamingStrategy {
    
    
    //---------------------Basic functions--------------------------------------    
    /**
     * transform a mouse coordinate in JTS Coordinate using the CRS of the mapcontext
     * @param mx : x coordinate of the mouse on the map (in pixel)
     * @param my : y coordinate of the mouse on the map (in pixel)
     * @return JTS Coordinate
     */
    public Coordinate toMapCoord(int mx, int my);    
    /**
     * transform a JTS Coordinate in an pixel x/y coordinate
     * @param coord
     * @return Java2D Point
     */
    public Point toComponentCoord(Coordinate coord);
       
    /**
     * create a bufferedImage of what is actually visible on the map widget
     * @return BufferedImage
     */
    public BufferedImage getSnapShot();

    /**
     * set the MapContext
     * @param context : can not be null 
     */
    public void setContext(MapContext context);    
    /**
     * get the MapContext
     * @return MapContext or null if no mapContext
     */
    public MapContext getContext();
    
    /**
     * set the maparea to look at
     * @param area : can not be null 
     * @deprecated use AffineTransform not Envelope.
     * <b>Envelope doesn't handle many interesting parameters and are
     * uncorrect uses to obtain a good rendering</b>
     */
    @Deprecated
    public void setMapArea(Envelope area);    
    /**
     * get the maparea to look at
     * @return Envelope or null if no MapArea
     * @deprecated use AffineTransform not Envelope.
     * <b>Envelope doesn't handle many interesting parameters and are
     * uncorrect uses to obtain a good rendering</b>
     */
    @Deprecated
    public Envelope getMapArea();
    
    /**
     * set affinetransform
     * @param transform
     */
    public void setAffineTransform(AffineTransform transform);    
    /**
     * get the actual affinetransfrom
     * @return AffineTransform
     */
    public AffineTransform getAffineTransform();
    
        
    /**
     * get the visual component 
     * @return Component
     */
    public Component getComponent();
    
    /**
     * add a StrategyListener
     * @param listener : StrategyListener to add
     */
    public void addStrategyListener(StrategyListener listener);    
    /**
     * remove a StrategyListener
     * @param listener : StrategyListener to remove
     */
    public void removeStrategyListener(StrategyListener listener);    
    /**
     * get an array of StrategyListener
     * @return array of StrategyListener
     */
    public StrategyListener[] getStrategyListeners();
    
    /**
     * to enable automatic refreshing of the map, if not you must call
     * manualy the refresh method
     * @param refresh 
     */
    public void setAutoRefreshEnabled(boolean refresh);    
    /**
     * to see if the strategy is in auto refresh mode
     * @return boolean
     */
    public boolean isAutoRefresh();                
    /**
     * use for a complete reset of the strategy
     */
    public void refresh();
    /**
     * true if the strategy is currently painting
     * @return boolean
     */
    public boolean isPainting();    
    /**
     * must be call to remove all reference on the renderingstrategy.
     * to avoid memory leack.
     */
    public void dispose();
}
