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
import com.vividsolutions.jts.geom.Envelope;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.Rectangle;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.event.EventListenerList;
import org.geotools.gui.swing.map.map2d.stream.event.StrategyEvent;
import org.geotools.gui.swing.map.map2d.stream.listener.StrategyListener;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.referencing.crs.DefaultGeographicCRS;

/**
 * Abstract rendering strategy
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/map/map2d/stream/strategy/AbstractRenderingStrategy.java $
 */
public abstract class AbstractRenderingStrategy implements StreamingStrategy, MapLayerListListener {

    private final EventListenerList STRATEGY_LISTENERS = new EventListenerList();
    protected MapContext context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
    private Envelope oldMapArea = null;
    private Rectangle oldRect = null;
    private final JComponent comp;
    private boolean autorefresh = true;
    private boolean isPainting = false;
    protected Envelope mapArea = new Envelope(1, 10, 1, 10);
    protected AffineTransform affineTransform = null;

    /**
     * create a default AbstractStrategy 
     */
    public AbstractRenderingStrategy() {
        comp = init();
        comp.addComponentListener(new ComponentListener() {

            public void componentResized(ComponentEvent arg0) {
                setMapArea(mapArea);
            }

            public void componentMoved(ComponentEvent arg0) {
            }

            public void componentShown(ComponentEvent arg0) {
                setMapArea(mapArea);
            }

            public void componentHidden(ComponentEvent arg0) {
            }
        });
    }

    protected abstract JComponent init();

    protected Envelope fixAspectRatio(Rectangle rect, Envelope area) {

        double mapWidth = area.getWidth(); /* get the extent of the map */
        double mapHeight = area.getHeight();
        double scaleX = rect.getWidth() / area.getWidth(); /*
         * calculate the new
         * scale
         */

        double scaleY = rect.getHeight() / area.getHeight();
        double scale = 1.0; // stupid compiler!

        if (scaleX < scaleY) { /* pick the smaller scale */
            scale = scaleX;
        } else {
            scale = scaleY;
        }

        /* calculate the difference in width and height of the new extent */
        double deltaX = /* Math.abs */ ((rect.getWidth() / scale) - mapWidth);
        double deltaY = /* Math.abs */ ((rect.getHeight() / scale) - mapHeight);


        /* create the new extent */
        Coordinate ll = new Coordinate(area.getMinX() - (deltaX / 2.0), area.getMinY() - (deltaY / 2.0));
        Coordinate ur = new Coordinate(area.getMaxX() + (deltaX / 2.0), area.getMaxY() + (deltaY / 2.0));

        return new Envelope(ll, ur);
    }

    protected void fit() {

        if (checkAspect()) {
            testRefresh();
        }
    }

    protected void testRefresh() {
        if (autorefresh) {
            refresh();
        }
    }

    protected boolean checkAspect() {
        boolean changed = false;

        Rectangle newRect = comp.getBounds();

        if (!newRect.equals(oldRect) || !mapArea.equals(oldMapArea)) {
            changed = true;
            oldRect = newRect;
            oldMapArea = mapArea;
        }

        return changed;
    }

    protected synchronized boolean isValidEnvelope(Envelope env) {
        boolean ok = false;
        

        if (env.isNull()) {
            ok = false;
        } else {

            if (  Double.isNaN(env.getMinX()) ||  Double.isNaN(env.getMinY()) ||  Double.isNaN(env.getMaxX()) ||  Double.isNaN(env.getMaxY()) ) {
                ok = false;
            }else{
                ok = true;
            }
            

        }
        return ok;

    }

    protected void setPainting(boolean b) {
        isPainting = b;
        fireRenderingEvent(isPainting);
    }

    //------------------TRIGGERS------------------------------------------------
    private void fireRenderingEvent(boolean isRendering) {
        StrategyListener[] lst = getStrategyListeners();

        for (StrategyListener l : lst) {
            l.setRendering(isRendering);
        }
    }

    protected void fireMapAreaChanged(Envelope oldone, Envelope newone) {

        StrategyEvent mce = new StrategyEvent(this,context, oldone, newone);

        StrategyListener[] lst = getStrategyListeners();

        for (StrategyListener l : lst) {
            l.mapAreaChanged(mce);
        }

    }

    protected void fireMapContextChanged(MapContext oldcontext, MapContext newContext) {
        StrategyEvent mce = new StrategyEvent(this, oldcontext, newContext,mapArea);

        StrategyListener[] lst = getStrategyListeners();

        for (StrategyListener l : lst) {
            l.mapContextChanged(mce);
        }

    }

    //-----------------------RenderingStrategy----------------------------------
    
    public Coordinate toMapCoord(int mx, int my) {

        Rectangle bounds = comp.getBounds();
        double width = mapArea.getWidth();
        double height = mapArea.getHeight();
        return toMapCoord(mx, my, width, height, bounds);
    }
    
    public Point toComponentCoord(Coordinate coord){
        
        Rectangle bounds = comp.getBounds();
        
        double width = mapArea.getWidth();
        double height = mapArea.getHeight();
        
        double xval = bounds.width/width;
        double yval = bounds.height/height;
        
        double minX = coord.x - mapArea.getMinX();
        double minY = mapArea.getMaxY() - coord.y;
        
        int x = (int)(minX*xval);
        int y = (int)(minY*yval);
        
        return new Point(x,y);        
    }

    private Coordinate toMapCoord(double mx, double my, double width, double height, Rectangle bounds) {
        
        double mapX = ((mx * width) / (double) bounds.width) + mapArea.getMinX();
        double mapY = (((bounds.getHeight() - my) * height) / (double) bounds.height) + mapArea.getMinY();
        return new Coordinate(mapX, mapY);
    }
    
    
    public abstract BufferedImage createBufferImage(MapLayer layer);

    public abstract BufferedImage createBufferImage(MapContext context);

    public abstract BufferedImage getSnapShot();

    public abstract void refresh();

    public final JComponent getComponent() {
        return comp;
    }

    public final void setContext(MapContext newContext) {

        if (newContext == null) {
            throw new NullPointerException("Context can't be null");
        }

        if (this.context != newContext) {
            this.context.removeMapLayerListListener(this);

            MapContext oldContext = this.context;
            this.context = newContext;
            this.context.addMapLayerListListener(this);

            refresh();

            fireMapContextChanged(oldContext, newContext);
        }
    }

    public final MapContext getContext() {
        return context;
    }

    public final void setMapArea(Envelope area) {

        if (area == null) {
            throw new NullPointerException("Area can't be null.");
        }

        Envelope oldenv = mapArea;
        mapArea = fixAspectRatio(comp.getBounds(), area);

        fit();
        fireMapAreaChanged(oldenv, mapArea);

    }

    public final Envelope getMapArea() {
        return mapArea;
    }

    public final void addStrategyListener(StrategyListener listener) {
        STRATEGY_LISTENERS.add(StrategyListener.class, listener);
    }

    public final void removeStrategyListener(StrategyListener listener) {
        STRATEGY_LISTENERS.remove(StrategyListener.class, listener);
    }

    public final StrategyListener[] getStrategyListeners() {
        return STRATEGY_LISTENERS.getListeners(StrategyListener.class);
    }

    public final void setAutoRefreshEnabled(boolean ref) {
        autorefresh = ref;
    }

    public final boolean isAutoRefresh() {
        return autorefresh;
    }

    public final boolean isPainting() {
        return isPainting;
    }
    
    public void dispose(){
        this.context.removeMapLayerListListener(this);
    }

    //----------------------Layer events----------------------------------------
    public abstract void layerAdded(MapLayerListEvent event);

    public abstract void layerRemoved(MapLayerListEvent event);

    public abstract void layerChanged(MapLayerListEvent event);

    public abstract void layerMoved(MapLayerListEvent event);

    public AffineTransform getAffineTransform() {
        return affineTransform;
    }

    public void setAffineTransform(AffineTransform affineTransform) {
        this.affineTransform = affineTransform;
    }

}        
