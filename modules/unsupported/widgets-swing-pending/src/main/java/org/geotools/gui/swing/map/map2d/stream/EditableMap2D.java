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
package org.geotools.gui.swing.map.map2d.stream;

import org.geotools.gui.swing.map.map2d.stream.handler.EditionHandler;
import org.geotools.gui.swing.map.map2d.stream.listener.EditionListener;
import org.geotools.map.MapLayer;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;

/**
 * interface for map2d widget who handle Edition
 * 
 * @author Johann Sorel
 */
public interface EditableMap2D extends SelectableMap2D {

    /**
     * set the MapLayers to paint in the memory decoration
     * @param layers
     */
    public void setMemoryLayers(MapLayer[] layers);            
    /**
     * refresh the decoration with the memoryLayers
     */
    public void repaintMemoryDecoration();
    
    /**
     * set the PointSymbolizer
     * @param symbol , can't be null
     */
    public void setPointSymbolizer(PointSymbolizer symbol);    
    /**
     * set the LineSymbolizer
     * @param symbol , can't be null
     */
    public void setLineSymbolizer(LineSymbolizer symbol);    
    /**
     * set the PolygonSymbolizer 
     * @param symbol , can't be null
     */
    public void setPolygonSymbolizer(PolygonSymbolizer symbol);    
    /**
     * get the PointSymbolizer
     * @return PointSymbolizer
     */
    public PointSymbolizer getPointSymbolizer();    
    /**
     * get the LineSymbolizer
     * @return LineSymbolizer
     */
    public LineSymbolizer getLineSymbolizer();
    /**
     * get the PolygonSymbolizer
     * @return PolygonSymbolizer
     */
    public PolygonSymbolizer getPolygonSymbolizer();

    /**
     * set EditionHandler
     * @param handler
     */
    public void setEditionHandler(EditionHandler handler);    
    /**
     * get EditionHandler
     * @return EditionHandler
     */
    public EditionHandler getEditionHandler();
    
    /**
     * set the MapLayer to edit
     * @param layer : MapLayer to edit
     */
    public void setEditedMapLayer(MapLayer layer);
    /**
     * get the edited MapLayer
     * @return edited MapLayer
     */
    public MapLayer getEditedMapLayer();

    /**
     * add an EditableMap2DListener
     * @param listener : EditableMap2DListener to add
     */
    public void addEditableMap2DListener(EditionListener listener);
    /**
     * remove an EditableMap2DListener
     * @param listener : EditableMap2DListener to remove
     */
    public void removeEditableMap2DListener(EditionListener listener);
    /**
     * get an array of EditableMap2DListener
     * @return array of EditableMap2DListener
     */
    public EditionListener[] getEditableMap2DListeners();
}
