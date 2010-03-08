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

import org.geotools.gui.swing.map.map2d.*;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JComponent;

import org.geotools.gui.swing.map.map2d.decoration.MapDecoration;
import org.geotools.gui.swing.map.map2d.stream.event.StrategyEvent;
import org.geotools.gui.swing.map.map2d.stream.handler.DefaultEditionHandler;
import org.geotools.gui.swing.map.map2d.stream.handler.EditionHandler;
import org.geotools.gui.swing.map.map2d.stream.listener.EditionListener;
import org.geotools.gui.swing.map.map2d.stream.strategy.SingleBufferedImageStrategy;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Stroke;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import org.geotools.gui.swing.map.map2d.stream.event.EditionEvent;

/**
 * Default implementation of EditableMap2D
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/map/map2d/stream/JStreamEditMap.java $
 */
public class JStreamEditMap extends JStreamSelectMap implements EditableMap2D {

    private final SingleBufferedImageStrategy memoryStrategy = new SingleBufferedImageStrategy();
    private MapContext memoryMapContext = null;
    private final BufferComponent memoryPane = new BufferComponent();
    private MapLayer editionLayer = null;
    private EditionHandler editionHandler = null;
    
    private PointSymbolizer pointSymbol = null;
    private LineSymbolizer lineSymbol = null;
    private PolygonSymbolizer polygonSymbol = null;

    /**
     * create a default JDefaultEditableMap2D
     */
    public JStreamEditMap() {
        super();
        init();
        
    }
    
    private void init(){
        
        //I made it like this so that matisse handle this widget
        try{
            memoryMapContext = new DefaultMapContext(DefaultGeographicCRS.WGS84);
            editionHandler = new DefaultEditionHandler();
            initSymbols();
        }catch(Exception e){}
        
        
        
        addMapDecoration(memoryPane);

        // memory strategy------------------------------------------------------
        memoryStrategy.setAutoRefreshEnabled(false);
        memoryStrategy.setContext(memoryMapContext);

        try{
            editionHandler.install(this);
        }catch(Exception e){}
    }
    
    private void initSymbols(){
        Color editionStyleColor = Color.RED;
        
        Fill fill = STYLE_BUILDER.createFill(editionStyleColor, 1f);
        Stroke stroke = STYLE_BUILDER.createStroke(editionStyleColor, 1);
        stroke.setOpacity(STYLE_BUILDER.literalExpression(1f));

        Mark mark = STYLE_BUILDER.createMark("cross", fill, stroke);
        Graphic gra = STYLE_BUILDER.createGraphic();
        gra.setOpacity(STYLE_BUILDER.literalExpression(1f));
        gra.setMarks(new Mark[]{mark});
        gra.setSize(STYLE_BUILDER.literalExpression(14));
       
        pointSymbol = STYLE_BUILDER.createPointSymbolizer(gra);
                
        fill = STYLE_BUILDER.createFill(editionStyleColor, 0.4f);
        stroke = STYLE_BUILDER.createStroke(editionStyleColor, 2);
        stroke.setOpacity(STYLE_BUILDER.literalExpression(0.6f));

        polygonSymbol = STYLE_BUILDER.createPolygonSymbolizer(stroke, fill);
        lineSymbol = STYLE_BUILDER.createLineSymbolizer(stroke);
        
    }

    private void adjusteContexts() {
        MapContext context = renderingStrategy.getContext();

        if (!context.getCoordinateReferenceSystem().equals(memoryMapContext.getCoordinateReferenceSystem())) {
            try {
                memoryMapContext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
            } catch (TransformException ex) {
                ex.printStackTrace();
            } catch (FactoryException ex) {
                ex.printStackTrace();
            }
        }

        memoryStrategy.setMapArea(renderingStrategy.getMapArea());
        memoryStrategy.refresh();
    }

    private void fireEditLayerChanged(MapLayer oldone, MapLayer newone) {
        EditionEvent mce = new EditionEvent(this, oldone, newone,editionHandler);

        EditionListener[] lst = getEditableMap2DListeners();

        for (EditionListener l : lst) {
            l.editedLayerChanged(mce);
        }

    }

    private void fireHandlerChanged(EditionHandler oldhandler, EditionHandler newhandler) {
        EditionEvent mce = new EditionEvent(this, editionLayer,oldhandler,newhandler);
        
        EditionListener[] lst = getEditableMap2DListeners();

        for (EditionListener l : lst) {
            l.editionHandlerChanged(mce);
        }
    }

    //---------------------MAP2D OVERLOAD---------------------------------------
    @Override
    public void setActionState(ACTION_STATE state) {

        if (state == ACTION_STATE.EDIT ) {
            editionHandler.installListeners(this);
        } else {
            editionHandler.uninstallListeners();
        }

        super.setActionState(state);
    }

    @Override
    protected void setRendering(boolean render) {
        super.setRendering(render);
        adjusteContexts();
    }

    @Override
    protected void mapAreaChanged(StrategyEvent event) {
        super.mapAreaChanged(event);
        adjusteContexts();
    }

    @Override
    protected void mapContextChanged(StrategyEvent event) {
        super.mapContextChanged(event);
        
    }

    @Override
    public void dispose() {
        super.dispose();
        
        memoryStrategy.dispose();
    }

    @Override
    public void layerRemoved(MapLayerListEvent event) {
        super.layerRemoved(event);
        
        if (editionLayer == event.getLayer()) {
                setEditedMapLayer(null);
            }
    }

    

    //--------------------EDITABLE MAP2D----------------------------------------
    
    public void setPointSymbolizer(PointSymbolizer symbol) {
        if(symbol == null){
            throw new NullPointerException("symbol can't be null");
        }        
        pointSymbol = symbol;
    }

    public void setLineSymbolizer(LineSymbolizer symbol) {
        if(symbol == null){
            throw new NullPointerException("symbol can't be null");
        }        
        lineSymbol = symbol;
    }

    public void setPolygonSymbolizer(PolygonSymbolizer symbol) {
        if(symbol == null){
            throw new NullPointerException("symbol can't be null");
        }        
        polygonSymbol = symbol;
    }

    public LineSymbolizer getLineSymbolizer() {
        return lineSymbol;
    }

    public PointSymbolizer getPointSymbolizer() {
        return pointSymbol;
    }
    
    public PolygonSymbolizer getPolygonSymbolizer() {
        return polygonSymbol;
    }
        
    public void setEditionHandler(EditionHandler newHandler) {
        if (newHandler == null) {
            throw new NullPointerException();
        } else if (newHandler != editionHandler) {

            editionHandler.cancelEdition();
            editionHandler.uninstallListeners();
            editionHandler.uninstall();

            EditionHandler oldHandler = editionHandler;
            editionHandler = newHandler;

            editionHandler.install(this);

            if (actionState == ACTION_STATE.EDIT) {
                editionHandler.installListeners(this);
            }

            fireHandlerChanged(oldHandler,newHandler);
        }
    }

    public EditionHandler getEditionHandler() {
        return editionHandler;
    }

    public void setEditedMapLayer(MapLayer newLayer) {

        if (editionLayer != newLayer) {
            editionHandler.cancelEdition();

            MapLayer oldLayer = editionLayer;
            editionLayer = newLayer;
            
            fireEditLayerChanged(oldLayer, newLayer);            
        }

    }

    public MapLayer getEditedMapLayer() {
        return editionLayer;
    }

    public void setMemoryLayers(MapLayer[] layers) {
        memoryMapContext.clearLayerList();
        memoryMapContext.addLayers(layers);
        adjusteContexts();
    }

    public void repaintMemoryDecoration() {
        adjusteContexts();
    }

    public void addEditableMap2DListener(EditionListener listener) {
        MAP2DLISTENERS.add(EditionListener.class, listener);
    }

    public void removeEditableMap2DListener(EditionListener listener) {
        MAP2DLISTENERS.remove(EditionListener.class, listener);
    }

    public EditionListener[] getEditableMap2DListeners() {
        return MAP2DLISTENERS.getListeners(EditionListener.class);
    }

    //---------------------PRIVATE CLASSES--------------------------------------
    

    private class BufferComponent extends javax.swing.JComponent implements MapDecoration {

        public BufferComponent() {
            setLayout(new BorderLayout());
            add(memoryStrategy.getComponent());
        }

        public void refresh() {
        }

        public JComponent geComponent() {
            return this;
        }

        public void setMap2D(Map2D map) {

        }

        public Map2D getMap2D() {
            return null;
        }

        public void dispose() {
        }
    }

    
}

