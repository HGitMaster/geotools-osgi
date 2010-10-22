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

import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.map.map2d.stream.handler.SelectionHandler;
import org.geotools.gui.swing.map.map2d.stream.listener.SelectionListener;
import org.geotools.map.MapLayer;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Geometry;

/**
 * interface for map2d widget how handle Selection
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/map/map2d/stream/SelectableMap2D.java $
 */
public interface SelectableMap2D extends NavigableMap2D{
        
    public static enum SELECTION_FILTER{
        CONTAINS("contains","16_filter_contains"),
        CROSSES("crosses","16_filter_crosses"),
        DISJOINT("disjoint","16_filter_disjoint"),
        INTERSECTS("intersects","16_filter_intersects"),
        OVERLAPS("overlaps","16_filter_overlaps"),
        TOUCHES("touches","16_filter_touches"),
        WITHIN("within","16_filter_within");
        
        private final String title;
        private final ImageIcon icon;
        
        SELECTION_FILTER(String key,String logokey){
            title = ResourceBundle.getBundle("org/geotools/gui/swing/map/map2d/Bundle").getString(key);
            icon = IconBundle.getResource().getIcon(logokey);
        }
        
        public String getTitle(){
            return title;
        }
        
        public ImageIcon getIcon(){
            return icon;
        }
        
        
    };
    
    
    /**
     * add a MapLayer in the selection list
     * @param layer : Maplayer to add
     */
    public void addSelectableLayer(MapLayer layer);    
    /**
     * add MapLayers in the selection list
     * @param layer : array of MapLayer to add
     */
    public void addSelectableLayer(MapLayer[] layer);    
    /**
     * remove a MapLayer from selection list
     * @param layer : MapLayer to remove
     */
    public void removeSelectableLayer(MapLayer layer);    
    /**
     * get an array of the selectable layers
     * @return array of MapLayer
     */
    public MapLayer[] getSelectableLayer();    
    /**
     * know if a MapLayer is selectable
     * @param layer : MapLayer to test
     * @return true if layer is in the selection list, false if not
     */
    public boolean isLayerSelectable(MapLayer layer);
        
    /**
     * the filter will determine the featurefilter used
     * most used filters are are SELECTION_FILTER.WITHIN and SELECTION_FILTER.INTERSECTS
     * @param filter
     */
    public void setSelectionFilter(SELECTION_FILTER filter);    
    /**
     * 
     * @return SELECTION_FILTER
     */
    public SELECTION_FILTER getSelectionFilter();
    
    /**
     * the SelectionHandler is managing the selection decoration and the related listeners.
     * @param handler
     */
    public void setSelectionHandler(SelectionHandler handler);    
    /**
     * 
     * @return SelectionHandler
     */
    public SelectionHandler getSelectionHandler();
        
    /**
     * create a filter for a MapLayer
     * @param geo
     * @param filter SELECTION_FILTER
     * @param layer MapLayer
     * @return
     */
    public Filter createFilter(Geometry geo, SELECTION_FILTER filter , MapLayer layer);
    
    /**
     * make a selection with x,y coordinate
     * @param x : X coordinate of the point selection
     * @param y : Y coordinate of the point selection
     */
    public void doSelection(double x, double y);    
    /**
     * make a selection with a JTS geometry
     * @param geo : JTS Geometry
     */
    public void doSelection(Geometry geo);
        
    /**
     * add a SelectableMap2DListener
     * @param listener : SelectableMap2DListener to add
     */
    public void addSelectableMap2DListener(SelectionListener listener);    
    /**
     * remove a SelectableMap2DListener 
     * @param listener : SelectableMap2DListener to remove
     */
    public void removeSelectableMap2DListener(SelectionListener listener);    
    /**
     * get an array of SelectableMap2DListener
     * @return array of SelectableMap2DListener
     */
    public SelectionListener[] getSelectableMap2DListeners();
    
      
}
