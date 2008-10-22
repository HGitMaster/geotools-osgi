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
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.gui.swing.map.map2d.decoration.MapDecoration;
import org.geotools.gui.swing.map.map2d.stream.event.StrategyEvent;
import org.geotools.gui.swing.map.map2d.stream.event.SelectionEvent;
import org.geotools.gui.swing.map.map2d.stream.handler.DefaultSelectionHandler;
import org.geotools.gui.swing.map.map2d.stream.handler.SelectionHandler;
import org.geotools.gui.swing.map.map2d.stream.listener.SelectionListener;
import org.geotools.gui.swing.map.map2d.stream.strategy.StreamingStrategy;
import org.geotools.gui.swing.map.map2d.stream.strategy.SingleBufferedImageStrategy;
import org.geotools.gui.swing.misc.FacilitiesFactory;
import org.geotools.gui.swing.misc.GeometryClassFilter;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Default implementation of selectableMap2D
 * 
 * @author Johann Sorel
 */
class JStreamSelectMap extends JStreamNavMap implements SelectableMap2D {

    /**
     * Geometry factory for JTS geometry creation
     */
    protected GeometryFactory GEOMETRY_FACTORY = null;
    /**
     * Facilities factory to duplicate MapLayers
     */
    protected FacilitiesFactory FACILITIES_FACTORY = null;
    /**
     * Style builder for sld style creation
     */
    protected StyleBuilder STYLE_BUILDER = null;
    /**
     * Filter factory 2
     */
    protected FilterFactory2 FILTER_FACTORY_2 = null;
    
    private final StreamingStrategy selectionStrategy = new SingleBufferedImageStrategy();
    private final MapContext selectionMapContext = new DefaultMapContext(DefaultGeographicCRS.WGS84);
    private final BufferComponent selectedDecoration = new BufferComponent();
    private final Map<MapLayer, MapLayer> copies = new HashMap<MapLayer, MapLayer>();
    private MapContext oldMapcontext = null;
    private Color selectionStyleColor = Color.GREEN;
    private Geometry selectionGeometrie = null;
    private SelectionHandler selectionHandler = new DefaultSelectionHandler();
    private SELECTION_FILTER selectionFilter = SELECTION_FILTER.INTERSECTS;

    /**
     * create a default JDefaultSelectableMap2D
     */
    protected JStreamSelectMap() {
        super();
        
        //I made it like this so that matisse handle this widget
        try{
            GEOMETRY_FACTORY = new GeometryFactory();
            FACILITIES_FACTORY = new FacilitiesFactory();
            STYLE_BUILDER = new StyleBuilder();
            FILTER_FACTORY_2 = (FilterFactory2) CommonFactoryFinder.getFilterFactory(null);
        }catch(Exception e){}
        init();
    }
    
    private void init(){
        selectionStrategy.setContext(selectionMapContext);
        addMapDecoration(selectedDecoration);        
    }

    /**
     *  transform a mouse coordinate in JTS Geometry using the CRS of the mapcontext
     * @param mx : x coordinate of the mouse on the map (in pixel)
     * @param my : y coordinate of the mouse on the map (in pixel)
     * @return JTS geometry (corresponding to a square of 6x6 pixel around mouse coordinate)
     */
    protected Geometry mousePositionToGeometry(int mx, int my) {
        Coordinate[] coord = new Coordinate[5];

        int taille = 4;

        coord[0] = renderingStrategy.toMapCoord(mx - taille, my - taille);
        coord[1] = renderingStrategy.toMapCoord(mx - taille, my + taille);
        coord[2] = renderingStrategy.toMapCoord(mx + taille, my + taille);
        coord[3] = renderingStrategy.toMapCoord(mx + taille, my - taille);
        coord[4] = coord[0];

        LinearRing lr1 = GEOMETRY_FACTORY.createLinearRing(coord);
        return GEOMETRY_FACTORY.createPolygon(lr1, null);
    }

    /**
     * create a filter corresponding to the layer features intersecting the geom
     * @param geom : the intersect JTS geometry used by the filter
     * @param layer : MapLayer for which the filter is made
     * @return Filter
     */
    public Filter createFilter(Geometry geom, SELECTION_FILTER filter, MapLayer layer) {
        Filter f = null;

        geom = FACILITIES_FACTORY.projectGeometry(geom, renderingStrategy.getContext(), layer);

        try {
            String name = layer.getFeatureSource().getSchema().getGeometryDescriptor().getLocalName();
            if (name.equals("")) {
                name = "the_geom";
            }
            Expression exp1 = FILTER_FACTORY_2.property(name);
            Expression exp2 = FILTER_FACTORY_2.literal(geom);
            switch (filter) {
                case CONTAINS:
                    f = FILTER_FACTORY_2.contains(exp1, exp2);
                    break;
                case CROSSES:
                    f = FILTER_FACTORY_2.crosses(exp1, exp2);
                    break;
                case DISJOINT:
                    f = FILTER_FACTORY_2.disjoint(exp1, exp2);
                    break;
                case INTERSECTS:
                    f = FILTER_FACTORY_2.intersects(exp1, exp2);
                    break;
                case OVERLAPS:
                    f = FILTER_FACTORY_2.overlaps(exp1, exp2);
                    break;
                case TOUCHES:
                    f = FILTER_FACTORY_2.touches(exp1, exp2);
                    break;
                case WITHIN:
                    f = FILTER_FACTORY_2.within(exp1, exp2);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return f;
    }

    

    private Style createStyle(MapLayer layer) {

        Class jtsClass = layer.getFeatureSource().getSchema().getGeometryDescriptor().getType().getBinding();

        if (jtsClass.equals(Point.class) || jtsClass.equals(MultiPoint.class)) {
            Fill fill = STYLE_BUILDER.createFill(selectionStyleColor, 0.6f);
            Stroke stroke = STYLE_BUILDER.createStroke(selectionStyleColor, 2);
            stroke.setOpacity(STYLE_BUILDER.literalExpression(1f));

            Mark mark = STYLE_BUILDER.createMark("circle", fill, stroke);
            Graphic gra = STYLE_BUILDER.createGraphic();
            gra.setOpacity(STYLE_BUILDER.literalExpression(1f));
            gra.setMarks(new Mark[]{mark});
            gra.setSize(STYLE_BUILDER.literalExpression(15));

            PointSymbolizer ps = STYLE_BUILDER.createPointSymbolizer(gra);

            Style pointSelectionStyle = STYLE_BUILDER.createStyle();
            pointSelectionStyle.addFeatureTypeStyle(STYLE_BUILDER.createFeatureTypeStyle(ps));

            return pointSelectionStyle;

        } else if (jtsClass.equals(LineString.class) || jtsClass.equals(MultiLineString.class)) {
            Fill fill = STYLE_BUILDER.createFill(selectionStyleColor, 0.6f);
            Stroke stroke = STYLE_BUILDER.createStroke(selectionStyleColor, 2);
            stroke.setOpacity(STYLE_BUILDER.literalExpression(1f));

            Mark mark = STYLE_BUILDER.createMark("circle", fill, stroke);
            Graphic gra = STYLE_BUILDER.createGraphic();
            gra.setOpacity(STYLE_BUILDER.literalExpression(1f));
            gra.setMarks(new Mark[]{mark});
            gra.setSize(STYLE_BUILDER.literalExpression(5));

            PointSymbolizer ps = STYLE_BUILDER.createPointSymbolizer(gra);
            LineSymbolizer ls = STYLE_BUILDER.createLineSymbolizer(stroke);

            Rule r1 = STYLE_BUILDER.createRule(new Symbolizer[]{ps});
            Rule r2 = STYLE_BUILDER.createRule(new Symbolizer[]{ls});

            Style lineSelectionStyle = STYLE_BUILDER.createStyle();
            lineSelectionStyle.addFeatureTypeStyle(STYLE_BUILDER.createFeatureTypeStyle(null, new Rule[]{r1, r2}));

            return lineSelectionStyle;

        } else if (jtsClass.equals(Polygon.class) || jtsClass.equals(MultiPolygon.class)) {
            Fill fill = STYLE_BUILDER.createFill(selectionStyleColor, 0.6f);
            Stroke stroke = STYLE_BUILDER.createStroke(selectionStyleColor, 2);
            stroke.setOpacity(STYLE_BUILDER.literalExpression(1f));

            PolygonSymbolizer pls = STYLE_BUILDER.createPolygonSymbolizer(stroke, fill);

            Mark mark = STYLE_BUILDER.createMark("circle", fill, stroke);
            Graphic gra = STYLE_BUILDER.createGraphic();
            gra.setOpacity(STYLE_BUILDER.literalExpression(1f));
            gra.setMarks(new Mark[]{mark});
            gra.setSize(STYLE_BUILDER.literalExpression(5));
            PointSymbolizer ps = STYLE_BUILDER.createPointSymbolizer(gra);


            Rule r1 = STYLE_BUILDER.createRule(new Symbolizer[]{ps});
            Rule r3 = STYLE_BUILDER.createRule(new Symbolizer[]{pls});

            Style polySelectionStyle = STYLE_BUILDER.createStyle();
            polySelectionStyle.addFeatureTypeStyle(STYLE_BUILDER.createFeatureTypeStyle(null, new Rule[]{r1, r3}));

            return polySelectionStyle;

        }


        Fill fill = STYLE_BUILDER.createFill(selectionStyleColor, 0.4f);
        Stroke stroke = STYLE_BUILDER.createStroke(selectionStyleColor, 2);
        stroke.setOpacity(STYLE_BUILDER.literalExpression(0.6f));

        PolygonSymbolizer pls = STYLE_BUILDER.createPolygonSymbolizer(stroke, fill);

        Mark mark = STYLE_BUILDER.createMark("circle", fill, stroke);
        Graphic gra = STYLE_BUILDER.createGraphic();
        gra.setOpacity(STYLE_BUILDER.literalExpression(0.6f));
        gra.setMarks(new Mark[]{mark});
        gra.setSize(STYLE_BUILDER.literalExpression(14));
        PointSymbolizer ps = STYLE_BUILDER.createPointSymbolizer(gra);

        LineSymbolizer ls = STYLE_BUILDER.createLineSymbolizer(stroke);

        Rule r1 = STYLE_BUILDER.createRule(new Symbolizer[]{ps});
        r1.setFilter(new GeometryClassFilter(Point.class, MultiPoint.class));
        Rule r2 = STYLE_BUILDER.createRule(new Symbolizer[]{ls});
        r2.setFilter(new GeometryClassFilter(LineString.class, MultiLineString.class));
        Rule r3 = STYLE_BUILDER.createRule(new Symbolizer[]{pls});
        r3.setFilter(new GeometryClassFilter(Polygon.class, MultiPolygon.class));


        Style LineSelectionStyle = STYLE_BUILDER.createStyle();
        LineSelectionStyle.addFeatureTypeStyle(STYLE_BUILDER.createFeatureTypeStyle(null, new Rule[]{r1, r2, r3}));

        return LineSelectionStyle;
    }

    private void applyStyleFilter(Style style, Filter f) {

        for (FeatureTypeStyle fts : style.getFeatureTypeStyles()) {
            for (Rule r : fts.getRules()) {
                r.setFilter(f);
            }
        }

    }

    private void fireSelectionChanged(Geometry oldgeo, Geometry newgeo) {
        SelectionEvent mce = new SelectionEvent(this, oldgeo,newgeo, selectionFilter, selectionHandler);

        SelectionListener[] lst = getSelectableMap2DListeners();

        for (SelectionListener l : lst) {
            l.selectionChanged(mce);
        }

    }

    private void fireFilterChanged(SELECTION_FILTER oldfilter,SELECTION_FILTER newfilter) {
        SelectionEvent mce = new SelectionEvent(this, selectionGeometrie, oldfilter, newfilter, selectionHandler);

        SelectionListener[] lst = getSelectableMap2DListeners();

        for (SelectionListener l : lst) {
            l.selectionFilterChanged(mce);
        }

    }

    private void fireHandlerChanged(SelectionHandler oldhandler, SelectionHandler newhandler) {
        SelectionEvent mce = new SelectionEvent(this, selectionGeometrie, selectionFilter, oldhandler, newhandler);

        SelectionListener[] lst = getSelectableMap2DListeners();

        for (SelectionListener l : lst) {
            l.selectionHandlerChanged(mce);
        }

    }

    //---------------------MAP2D OVERLOAD---------------------------------------  
    @Override
    public void setActionState(ACTION_STATE state) {
        
        if (state == ACTION_STATE.SELECT && !selectionHandler.isInstalled()) {
            selectionHandler.install(this);
        } else if (state != ACTION_STATE.SELECT && selectionHandler.isInstalled()) {
            selectionHandler.uninstall();
        }
        
        super.setActionState(state);        
    }

    @Override
    protected void mapAreaChanged(StrategyEvent event) {
        super.mapAreaChanged(event);

        MapContext context = renderingStrategy.getContext();

        try {
            selectionMapContext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
        } catch (TransformException ex) {
            ex.printStackTrace();
        } catch (FactoryException ex) {
            ex.printStackTrace();
        }

        selectionStrategy.setMapArea(event.getMapArea());
    }

    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        super.propertyChange(arg0);
        
        MapContext context = renderingStrategy.getContext();

        try {
            selectionMapContext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
        } catch (TransformException ex) {
            ex.printStackTrace();
        } catch (FactoryException ex) {
            ex.printStackTrace();
        }
        
        selectionStrategy.refresh();

    }

    @Override
    protected void mapContextChanged(StrategyEvent event) {
        super.mapContextChanged(event);
                
        if (event.getContext() != oldMapcontext) {
            oldMapcontext = event.getContext();

            selectionMapContext.clearLayerList();
            copies.clear();

            MapContext context = event.getContext();

            try {
                selectionMapContext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
                selectionStrategy.setMapArea(renderingStrategy.getMapArea());

            } catch (TransformException ex) {
                ex.printStackTrace();
            } catch (FactoryException ex) {
                ex.printStackTrace();
            }

        }

    }

    @Override
    public void setRenderingStrategy(StreamingStrategy stratege) {
        
        if (actionState == ACTION_STATE.SELECT && selectionHandler.isInstalled()) {
            selectionHandler.uninstall();
        }
        
        super.setRenderingStrategy(stratege);
        
        if (actionState == ACTION_STATE.SELECT) {
            selectionHandler.install(this);
        }

    }

    @Override
    protected void setRendering(boolean render) {
        super.setRendering(render);

        MapContext context = renderingStrategy.getContext();

        try {
            selectionMapContext.setCoordinateReferenceSystem(context.getCoordinateReferenceSystem());
        } catch (TransformException ex) {
            ex.printStackTrace();
        } catch (FactoryException ex) {
            ex.printStackTrace();
        }

        selectionStrategy.setMapArea(renderingStrategy.getMapArea());
        selectionStrategy.refresh();

    }

    @Override
    public void dispose() {
        super.dispose();
        selectionStrategy.dispose();
    }

    @Override
    public void layerRemoved(MapLayerListEvent event) {
        super.layerRemoved(event);
        removeSelectableLayer(event.getLayer());
    }

    

    //----------------------SELECTABLE MAP2D------------------------------------
    private void addSelectableLayerNU(MapLayer layer) {
        if (layer != null) {
            MapLayer copy = FACILITIES_FACTORY.duplicateLayer(layer);

            copy.setStyle(createStyle(layer));

            if (selectionGeometrie != null) {
                try {
                    Filter f = createFilter(selectionGeometrie, selectionFilter, copy);
                    applyStyleFilter(copy.getStyle(), f);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                applyStyleFilter(copy.getStyle(), Filter.EXCLUDE);
            }

            selectionMapContext.addLayer(copy);

            if (selectionMapContext.getLayerCount() == 1) {
                selectionStrategy.setMapArea(renderingStrategy.getMapArea());
            }

            copies.put(layer, copy);
        }
    }

    public void addSelectableLayer(MapLayer layer) {
        addSelectableLayerNU(layer);
        selectionStrategy.refresh();
    }

    public void addSelectableLayer(MapLayer[] layers) {
        if (layers != null) {
            for (MapLayer layer : layers) {
                addSelectableLayerNU(layer);
            }
            selectionStrategy.refresh();
        }
    }

    public void removeSelectableLayer(MapLayer layer) {
        MapLayer copy = copies.remove(layer);
        selectionMapContext.removeLayer(copy);
    }

    public MapLayer[] getSelectableLayer() {
        return copies.keySet().toArray(new MapLayer[0]);
    }

    public boolean isLayerSelectable(MapLayer layer) {
        return copies.containsKey(layer);
    }

    public void setSelectionFilter(SELECTION_FILTER newFilter) {
        if (newFilter == null) {
            throw new NullPointerException();
        } else if (newFilter != selectionFilter) {
            SELECTION_FILTER oldFilter = selectionFilter;
            selectionFilter = newFilter;
            fireFilterChanged(oldFilter,newFilter);
        }
    }

    public SELECTION_FILTER getSelectionFilter() {
        return selectionFilter;
    }

    public void setSelectionHandler(SelectionHandler newHandler) {
        if (newHandler == null) {
            throw new NullPointerException();
        } else if (newHandler != selectionHandler) {

            if (selectionHandler.isInstalled()) {
                selectionHandler.uninstall();
            }

            SelectionHandler oldHandler = selectionHandler;
            selectionHandler = newHandler;

            if (actionState == ACTION_STATE.SELECT) {
                selectionHandler.install(this);
            }

            fireHandlerChanged(oldHandler,newHandler);
        }
    }

    public SelectionHandler getSelectionHandler() {
        return selectionHandler;
    }

    public void doSelection(double x, double y) {
        Geometry geometry = GEOMETRY_FACTORY.createPoint(new Coordinate(x, y));
        doSelection(geometry);
    }

    public void doSelection(Geometry newGeo) {
        Geometry oldGeo = selectionGeometrie;
        selectionGeometrie = newGeo;

        Filter f = null;

        if (selectionMapContext.getLayerCount() == 0) {
            return;
        }

        for (MapLayer layer : selectionMapContext.getLayers()) {

            try {
                f = createFilter(newGeo, selectionFilter, layer);
                applyStyleFilter(layer.getStyle(), f);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        selectionStrategy.refresh();

        fireSelectionChanged(oldGeo,newGeo);

    }

    public void addSelectableMap2DListener(SelectionListener listener) {
        MAP2DLISTENERS.add(SelectionListener.class, listener);
    }

    public void removeSelectableMap2DListener(SelectionListener listener) {
        MAP2DLISTENERS.remove(SelectionListener.class, listener);
    }

    public SelectionListener[] getSelectableMap2DListeners() {
        return MAP2DLISTENERS.getListeners(SelectionListener.class);
    }

    //---------------------PRIVATE CLASSES--------------------------------------        
   
    private class BufferComponent extends JComponent implements MapDecoration {

        public BufferComponent() {
            setLayout(new BorderLayout());
            add(selectionStrategy.getComponent());
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





