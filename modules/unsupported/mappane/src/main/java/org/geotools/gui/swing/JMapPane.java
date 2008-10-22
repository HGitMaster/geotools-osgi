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
package org.geotools.gui.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.IllegalFilterException;
import org.geotools.gui.swing.event.HighlightChangeListener;
import org.geotools.gui.swing.event.HighlightChangedEvent;
import org.geotools.gui.swing.event.SelectionChangeListener;
import org.geotools.gui.swing.event.SelectionChangedEvent;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.LabelCache;
import org.geotools.renderer.lite.LabelCacheDefault;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * A simple map container that is a JPanel with a map in. provides simple
 * pan,zoom, highlight and selection The mappane stores an image of the map
 * (drawn from the context) and an image of the slected feature(s) to speed up
 * rendering of the highlights. Thus the whole map is only redrawn when the bbox
 * changes, selection is only redrawn when the selected feature changes.
 *
 *
 * @author Ian Turton
 *
 */
public class JMapPane extends JPanel implements MouseListener,
        MouseMotionListener, HighlightChangeListener,SelectionChangeListener, PropertyChangeListener,
        MapLayerListListener {
    /**
     *
     */
    private static final long serialVersionUID = -8647971481359690499L;

    public static final int Reset = 0;

    public static final int ZoomIn = 1;

    public static final int ZoomOut = 2;

    public static final int Pan = 3;

    public static final int Select = 4;

    private static final int POLYGON = 0;

    private static final int LINE = 1;

    private static final int POINT = 2;

    /**
     * what renders the map
     */
    GTRenderer renderer;

    private GTRenderer highlightRenderer, selectionRenderer;

    /**
     * the map context to render
     */
    MapContext context;

    private MapContext selectionContext;

    /**
     * the area of the map to draw
     */
    Envelope mapArea;

    /**
     * the size of the pane last time we drew
     */
    private Rectangle oldRect = null;

    /**
     * the last map area drawn.
     */
    private Envelope oldMapArea = null;

    /**
     * the base image of the map
     */
    private BufferedImage baseImage;

    /**
     * image of selection
     */
    private BufferedImage selectImage;

    /**
     * style for selected items
     */
    private Style selectionStyle;

    /**
     * layer that selection works on
     */
    private MapLayer selectionLayer;

    /**
     * layer that highlight works on
     */
    private MapLayer highlightLayer;

    /**
     * the object which manages highlighting
     */
    private HighlightManager highlightManager;

    /**
     * is highlighting on or off
     */
    private boolean highlight = true;

    /**
     * a factory for filters
     */
    FilterFactory2 ff;

    /**
     * a factory for geometries
     */
    GeometryFactory gf = new GeometryFactory(); // FactoryFinder.getGeometryFactory(null);

    /**
     * the collections of features to be selected or highlighted
     */
    FeatureCollection selection;

    /**
     * the collections of features to be selected or highlighted
     */
    FeatureCollection highlightFeature;

    private int state = ZoomIn;

    /**
     * how far to zoom in or out
     */
    private double zoomFactor = 2.0;

    Style lineHighlightStyle;

    Style pointHighlightStyle;

    Style polygonHighlightStyle;

    Style polygonSelectionStyle;

    Style pointSelectionStyle;

    Style lineSelectionStyle;

    boolean changed = true;

    LabelCache labelCache = new LabelCacheDefault();

    private boolean reset = false;

    int startX;

    int startY;

    private boolean clickable;

    int lastX;

    int lastY;

    private SelectionManager selectionManager;

    public JMapPane() {
        this(null, true, null, null);
    }

    /**
     * create a basic JMapPane
     *
     * @param render -
     *            how to draw the map
     * @param context -
     *            the map context to display
     */
    public JMapPane(GTRenderer render, MapContext context) {
        this(null, true, render, context);
    }

    /**
     * full constructor extending JPanel
     *
     * @param layout -
     *            layout (probably shouldn't be set)
     * @param isDoubleBuffered -
     *            a Swing thing I don't really understand
     * @param render -
     *            what to draw the map with
     * @param context -
     *            what to draw
     */
    public JMapPane(LayoutManager layout, boolean isDoubleBuffered,
            GTRenderer render, MapContext context) {
        super(layout, isDoubleBuffered);

        ff = (FilterFactory2) org.geotools.factory.CommonFactoryFinder
                .getFilterFactory(null);
        setRenderer(render);

        setContext(context);

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        setHighlightManager(new HighlightManager(highlightLayer));
        setSelectionManager(new SelectionManager(selectionLayer));
        lineHighlightStyle = setupStyle(LINE, Color.red);

        pointHighlightStyle = setupStyle(POINT, Color.red);

        polygonHighlightStyle = setupStyle(POLYGON, Color.red);

        polygonSelectionStyle = setupStyle(POLYGON, Color.cyan);

        pointSelectionStyle = setupStyle(POINT, Color.cyan);

        lineSelectionStyle = setupStyle(LINE, Color.cyan);
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * get the renderer
     */
    public GTRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(GTRenderer renderer) {
        Map hints = new HashMap();
        if (renderer instanceof StreamingRenderer) {
            hints = renderer.getRendererHints();
            if (hints == null) {
                hints = new HashMap();
            }
            if (hints.containsKey(StreamingRenderer.LABEL_CACHE_KEY)) {
                labelCache = (LabelCache) hints
                        .get(StreamingRenderer.LABEL_CACHE_KEY);
            } else {
                hints.put(StreamingRenderer.LABEL_CACHE_KEY, labelCache);
            }
            renderer.setRendererHints(hints);
        }

        this.renderer = renderer;
        this.highlightRenderer = new StreamingRenderer();
        this.selectionRenderer = new StreamingRenderer();

        hints.put("memoryPreloadingEnabled", Boolean.FALSE);
        highlightRenderer.setRendererHints(hints);
        selectionRenderer.setRendererHints(hints);

        if (this.context != null) {
            this.renderer.setContext(this.context);
        }
    }

    public MapContext getContext() {
        return context;
    }

    public void setContext(MapContext context) {
        if (this.context != null) {
            this.context.removeMapLayerListListener(this);
        }

        this.context = context;

        if (context != null) {
            this.context.addMapLayerListListener(this);
        }

        if (renderer != null) {
            renderer.setContext(this.context);
        }
    }

    public Envelope getMapArea() {
        return mapArea;
    }

    public void setMapArea(Envelope mapArea) {
        this.mapArea = mapArea;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;

        // System.out.println("State: " + state);
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public MapLayer getSelectionLayer() {
        return selectionLayer;
    }

    public void setSelectionLayer(MapLayer selectionLayer) {
        this.selectionLayer = selectionLayer;
        if(selectionManager!=null) {
            selectionManager.setSelectionLayer(selectionLayer);
        }
    }

    public boolean isHighlight() {
        return highlight;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public MapLayer getHighlightLayer() {
        return highlightLayer;
    }

    public void setHighlightLayer(MapLayer highlightLayer) {
        this.highlightLayer = highlightLayer;

        if (highlightManager != null) {
            highlightManager.setHighlightLayer(highlightLayer);
        }
    }

    public HighlightManager getHighlightManager() {
        return highlightManager;
    }

    public void setHighlightManager(HighlightManager highlightManager) {
        this.highlightManager = highlightManager;
        this.highlightManager.addHighlightChangeListener(this);
        this.addMouseMotionListener(this.highlightManager);
    }

    public Style getLineHighlightStyle() {
        return lineHighlightStyle;
    }

    public void setLineHighlightStyle(Style lineHighlightStyle) {
        this.lineHighlightStyle = lineHighlightStyle;
    }

    public Style getLineSelectionStyle() {
        return lineSelectionStyle;
    }

    public void setLineSelectionStyle(Style lineSelectionStyle) {
        this.lineSelectionStyle = lineSelectionStyle;
    }

    public Style getPointHighlightStyle() {
        return pointHighlightStyle;
    }

    public void setPointHighlightStyle(Style pointHighlightStyle) {
        this.pointHighlightStyle = pointHighlightStyle;
    }

    public Style getPointSelectionStyle() {
        return pointSelectionStyle;
    }

    public void setPointSelectionStyle(Style pointSelectionStyle) {
        this.pointSelectionStyle = pointSelectionStyle;
    }

    public Style getPolygonHighlightStyle() {
        return polygonHighlightStyle;
    }

    public void setPolygonHighlightStyle(Style polygonHighlightStyle) {
        this.polygonHighlightStyle = polygonHighlightStyle;
    }

    public Style getPolygonSelectionStyle() {
        return polygonSelectionStyle;
    }

    public void setPolygonSelectionStyle(Style polygonSelectionStyle) {
        this.polygonSelectionStyle = polygonSelectionStyle;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if ((renderer == null) || (mapArea == null)) {
            return;
        }

        Rectangle r = getBounds();
        Rectangle dr = new Rectangle(r.width, r.height);

        if (!r.equals(oldRect) || reset) {
        	if(!r.equals(oldRect)) {
        		try {
					mapArea=context.getLayerBounds();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
            /* either the viewer size has changed or we've done a reset */
            changed = true; /* note we need to redraw */
            reset = false; /* forget about the reset */
            oldRect = r; /* store what the current size is */
            mapArea = fixAspectRatio(r, mapArea);
        }

        if (!mapArea.equals(oldMapArea)) { /* did the map extent change? */
            changed = true;
            oldMapArea = mapArea;
//          when we tell the context that the bounds have changed WMSLayers
            // can refresh them selves
            context.setAreaOfInterest(mapArea, context
                    .getCoordinateReferenceSystem());
        }

        if (changed) { /* if the map changed then redraw */
            changed = false;
            baseImage = new BufferedImage(dr.width, dr.height,
                    BufferedImage.TYPE_INT_ARGB);

            Graphics2D ig = baseImage.createGraphics();
            /* System.out.println("rendering"); */
            renderer.setContext(context);
            labelCache.clear(); // work around anoying labelcache bug


            // draw the map
            renderer.paint((Graphics2D) ig, dr, mapArea);
        }

        ((Graphics2D) g).drawImage(baseImage, 0, 0, this);

        if ((selection != null) && (selection.size() > 0)) {
            // paint selection

            String type = selectionLayer.getFeatureSource().getSchema()
            .getGeometryDescriptor().getType().getBinding().getName();
            /*String type = selection.getDefaultGeometry().getGeometryType();*/
            /*System.out.println(type);*/
            if (type == null)
                type = "polygon";

            /* String type = "point"; */

            if (type.toLowerCase().endsWith("polygon")) {
                selectionStyle = polygonSelectionStyle;
            } else if (type.toLowerCase().endsWith("point")) {
                selectionStyle = pointSelectionStyle;
            } else if (type.toLowerCase().endsWith("line")) {
                selectionStyle = lineSelectionStyle;
            }

            selectionContext = new DefaultMapContext(DefaultGeographicCRS.WGS84);

            selectionContext.addLayer(selection, selectionStyle);
            selectionRenderer.setContext(selectionContext);

            selectImage = new BufferedImage(dr.width, dr.height,
                    BufferedImage.TYPE_INT_ARGB);

            Graphics2D ig = selectImage.createGraphics();
            /* System.out.println("rendering selection"); */
            selectionRenderer.paint((Graphics2D) ig, dr, mapArea);

            ((Graphics2D) g).drawImage(selectImage, 0, 0, this);
        }

        if (highlight && (highlightFeature != null)
                && (highlightFeature.size() > 0)) {
            /*
             * String type = selection.getDefaultGeometry().getGeometryType();
             * System.out.println(type); if(type==null) type="polygon";
             */
            String type = highlightLayer.getFeatureSource().getSchema()
            .getGeometryDescriptor().getType().getBinding().getName();
            /*String type = selection.getDefaultGeometry().getGeometryType();*/
            //System.out.println(type);
            if (type == null)
                type = "polygon";

            /* String type = "point"; */
            Style highlightStyle = null;
            if (type.toLowerCase().endsWith("polygon")) {
                highlightStyle = polygonHighlightStyle;
            } else if (type.toLowerCase().endsWith("point")) {
                highlightStyle = pointHighlightStyle;
            } else if (type.toLowerCase().endsWith("line")) {
                highlightStyle = lineHighlightStyle;
            }




            MapContext highlightContext = new DefaultMapContext(
                    DefaultGeographicCRS.WGS84);

            highlightContext.addLayer(highlightFeature, highlightStyle);
            highlightRenderer.setContext(highlightContext);

            /* System.out.println("rendering highlight"); */
            highlightRenderer.paint((Graphics2D) g, dr, mapArea);
        }
    }

    private Envelope fixAspectRatio(Rectangle r, Envelope mapArea) {
        double mapWidth = mapArea.getWidth(); /* get the extent of the map */
        double mapHeight = mapArea.getHeight();
        double scaleX = r.getWidth() / mapArea.getWidth(); /*
                                                             * calculate the new
                                                             * scale
                                                             */

        double scaleY = r.getHeight() / mapArea.getHeight();
        double scale = 1.0; // stupid compiler!

        if (scaleX < scaleY) { /* pick the smaller scale */
            scale = scaleX;
        } else {
            scale = scaleY;
        }

        /* calculate the difference in width and height of the new extent */
        double deltaX = /* Math.abs */((r.getWidth() / scale) - mapWidth);
        double deltaY = /* Math.abs */((r.getHeight() / scale) - mapHeight);

        /*
         * System.out.println("delta x " + deltaX); System.out.println("delta y " +
         * deltaY);
         */

        /* create the new extent */
        Coordinate ll = new Coordinate(mapArea.getMinX() - (deltaX / 2.0),
                mapArea.getMinY() - (deltaY / 2.0));
        Coordinate ur = new Coordinate(mapArea.getMaxX() + (deltaX / 2.0),
                mapArea.getMaxY() + (deltaY / 2.0));

        return new Envelope(ll, ur);
    }

    public void doSelection(double x, double y, MapLayer layer) {

        Geometry geometry = gf.createPoint(new Coordinate(x, y));

        // org.opengis.geometry.Geometry geometry = new Point();

            findFeature(geometry, layer);

    }

    /**
     * @param geometry -
     *            a geometry to construct the filter with
     * @param i -
     *            the index of the layer to search
     * @throws IndexOutOfBoundsException
     */
    private void findFeature(Geometry geometry, MapLayer layer)
            throws IndexOutOfBoundsException {
        org.opengis.filter.spatial.BinarySpatialOperator f = null;


        if ((context == null) || (layer==null)) {
            return ;
        }



        try {
            String name = layer.getFeatureSource().getSchema()
                    .getGeometryDescriptor().getLocalName();

            if (name == "") {
                name = "the_geom";
            }

            try {
                f = ff.contains(ff.property(name), ff.literal(geometry));
                if(selectionManager!=null) {
                    System.out.println("selection changed");
                    selectionManager.selectionChanged(this, f);

                }
            } catch (IllegalFilterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            /*// f.addLeftGeometry(ff.property(name));
            // System.out.println("looking with " + f);
            FeatureCollection<SimpleFeatureType, SimpleFeature> fc = layer.getFeatureSource().getFeatures(f);



            if (fcol == null) {
                fcol = fc;

                // here we should set the defaultgeom type
            } else {
                fcol.addAll(fc);
            }*/

            /*
             * GeometryAttributeType gat =
             * layer.getFeatureSource().getSchema().getDefaultGeometry();
             * fcol.setDefaultGeometry((Geometry)gat.createDefaultValue());
             */

            /*
             * Iterator fi = fc.iterator(); while (fi.hasNext()) { Feature feat =
             * (Feature) fi.next(); System.out.println("selected " +
             * feat.getAttribute("STATE_NAME")); }
             */
        } catch (IllegalFilterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ;
    }

    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        // System.out.println("before area "+mapArea+"\nw:"+mapArea.getWidth()+"
        // h:"+mapArea.getHeight());
        Rectangle bounds = this.getBounds();
        double x = (double) (e.getX());
        double y = (double) (e.getY());
        double width = mapArea.getWidth();
        double height = mapArea.getHeight();
        double width2 = mapArea.getWidth() / 2.0;
        double height2 = mapArea.getHeight() / 2.0;

        double mapX = ((x * width) / (double) bounds.width) + mapArea.getMinX();
        double mapY = (((bounds.getHeight() - y) * height) / (double) bounds.height)
                + mapArea.getMinY();

        /*
         * System.out.println(""+x+"->"+mapX);
         * System.out.println(""+y+"->"+mapY);
         */

        /*
         * Coordinate ll = new Coordinate(mapArea.getMinX(), mapArea.getMinY());
         * Coordinate ur = new Coordinate(mapArea.getMaxX(), mapArea.getMaxY());
         */
        double zlevel = 1.0;

        switch (state) {
        case Pan:
            zlevel = 1.0;

            break;

        case ZoomIn:
            zlevel = zoomFactor;

            break;

        case ZoomOut:
            zlevel = 1.0 / zoomFactor;

            break;

        case Select:
            doSelection(mapX, mapY, selectionLayer);


            return;

        default:
            return;
        }

        Coordinate ll = new Coordinate(mapX - (width2 / zlevel), mapY
                - (height2 / zlevel));
        Coordinate ur = new Coordinate(mapX + (width2 / zlevel), mapY
                + (height2 / zlevel));

        mapArea = new Envelope(ll, ur);
        // System.out.println("after area "+mapArea+"\nw:"+mapArea.getWidth()+"
        // h:"+mapArea.getHeight());
        repaint();
    }

    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    public void mousePressed(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();
        lastX = 0;
        lastY = 0;
    }

    public void mouseReleased(MouseEvent e) {
        int endX = e.getX();
        int endY = e.getY();

        if ((state == JMapPane.ZoomIn) || (state == JMapPane.ZoomOut)) {
            drawRectangle(this.getGraphics());
        }


        processDrag(startX, startY, endX, endY);
        lastX = 0;
        lastY = 0;
    }

    public void mouseDragged(MouseEvent e) {
        Graphics graphics = this.getGraphics();
        int x = e.getX();
        int y = e.getY();

        if (state == JMapPane.Pan) {
            // move the image with the mouse
            if ((lastX > 0) && (lastY > 0)) {
                int dx = lastX - startX;
                int dy = lastY - startY;
                // System.out.println("translate "+dx+","+dy);
                graphics.clearRect(0, 0, this.getWidth(), this.getHeight());
                ((Graphics2D) graphics).drawImage(baseImage, dx, dy, this);
            }

            lastX = x;
            lastY = y;
        } else if ((state == JMapPane.ZoomIn) || (state == JMapPane.ZoomOut)) {
            graphics.setXORMode(Color.RED);

            if ((lastX > 0) && (lastY > 0)) {
                drawRectangle(graphics);
            }

            // draw new box
            lastX = x;
            lastY = y;
            drawRectangle(graphics);
        } else if (state == JMapPane.Select && selectionLayer != null) {

            // construct a new bbox filter
            Rectangle bounds = this.getBounds();

            double mapWidth = mapArea.getWidth();
            double mapHeight = mapArea.getHeight();

            double x1 = ((this.startX * mapWidth) / (double) bounds.width)
                    + mapArea.getMinX();
            double y1 = (((bounds.getHeight() - this.startY) * mapHeight) / (double) bounds.height)
                    + mapArea.getMinY();
            double x2 = ((x * mapWidth) / (double) bounds.width)
                    + mapArea.getMinX();
            double y2 = (((bounds.getHeight() - y) * mapHeight) / (double) bounds.height)
                    + mapArea.getMinY();
            double left = Math.min(x1, x2);
            double right = Math.max(x1, x2);
            double bottom = Math.min(y1, y2);
            double top = Math.max(y1, y2);


            String name = selectionLayer.getFeatureSource().getSchema()
                    .getGeometryDescriptor().getLocalName();

            if (name == "") {
                name = "the_geom";
            }
            Filter bb = ff.bbox(ff.property(name), left, bottom, right, top,
                    getContext().getCoordinateReferenceSystem().toString());
            if(selectionManager!=null) {
                selectionManager.selectionChanged(this, bb);
            }

            graphics.setXORMode(Color.green);

            /*
             * if ((lastX > 0) && (lastY > 0)) { drawRectangle(graphics); }
             */

            // draw new box
            lastX = x;
            lastY = y;
            drawRectangle(graphics);
        }
    }

    private void processDrag(int x1, int y1, int x2, int y2) {
        // System.out.println("processing drag from " + x1 + "," + y1 + " -> "
        // + x2 + "," + y2);
        if ((x1 == x2) && (y1 == y2)) {
            if (isClickable()) {
                mouseClicked(new MouseEvent(this, 0, new Date().getTime(), 0,
                        x1, y1, y2, false));
            }

            return;
        }

        Rectangle bounds = this.getBounds();

        double mapWidth = mapArea.getWidth();
        double mapHeight = mapArea.getHeight();

        double startX = ((x1 * mapWidth) / (double) bounds.width)
                + mapArea.getMinX();
        double startY = (((bounds.getHeight() - y1) * mapHeight) / (double) bounds.height)
                + mapArea.getMinY();
        double endX = ((x2 * mapWidth) / (double) bounds.width)
                + mapArea.getMinX();
        double endY = (((bounds.getHeight() - y2) * mapHeight) / (double) bounds.height)
                + mapArea.getMinY();

        if (state == JMapPane.Pan) {
            // move the image with the mouse
            // calculate X offsets from start point to the end Point
            double deltaX1 = endX - startX;

            // System.out.println("deltaX " + deltaX1);
            // new edges
            double left = mapArea.getMinX() - deltaX1;
            double right = mapArea.getMaxX() - deltaX1;

            // now for Y
            double deltaY1 = endY - startY;

            // System.out.println("deltaY " + deltaY1);
            double bottom = mapArea.getMinY() - deltaY1;
            double top = mapArea.getMaxY() - deltaY1;
            Coordinate ll = new Coordinate(left, bottom);
            Coordinate ur = new Coordinate(right, top);

            mapArea = fixAspectRatio(this.getBounds(), new Envelope(ll, ur));
        } else if (state == JMapPane.ZoomIn) {
            // make the dragged rectangle (in map coords) the new BBOX
            double left = Math.min(startX, endX);
            double right = Math.max(startX, endX);
            double bottom = Math.min(startY, endY);
            double top = Math.max(startY, endY);
            Coordinate ll = new Coordinate(left, bottom);
            Coordinate ur = new Coordinate(right, top);

            mapArea = fixAspectRatio(this.getBounds(), new Envelope(ll, ur));
        } else if (state == JMapPane.ZoomOut) {
            // make the dragged rectangle in screen coords the new map size?
            double left = Math.min(startX, endX);
            double right = Math.max(startX, endX);
            double bottom = Math.min(startY, endY);
            double top = Math.max(startY, endY);
            double nWidth = (mapWidth * mapWidth) / (right - left);
            double nHeight = (mapHeight * mapHeight) / (top - bottom);
            double deltaX1 = left - mapArea.getMinX();
            double nDeltaX1 = (deltaX1 * nWidth) / mapWidth;
            double deltaY1 = bottom - mapArea.getMinY();
            double nDeltaY1 = (deltaY1 * nHeight) / mapHeight;
            Coordinate ll = new Coordinate(mapArea.getMinX() - nDeltaX1,
                    mapArea.getMinY() - nDeltaY1);
            double deltaX2 = mapArea.getMaxX() - right;
            double nDeltaX2 = (deltaX2 * nWidth) / mapWidth;
            double deltaY2 = mapArea.getMaxY() - top;
            double nDeltaY2 = (deltaY2 * nHeight) / mapHeight;
            Coordinate ur = new Coordinate(mapArea.getMaxX() + nDeltaX2,
                    mapArea.getMaxY() + nDeltaY2);
            mapArea = fixAspectRatio(this.getBounds(), new Envelope(ll, ur));
        } else if (state == JMapPane.Select && selectionLayer !=null) {
            double left = Math.min(startX, endX);
            double right = Math.max(startX, endX);
            double bottom = Math.min(startY, endY);
            double top = Math.max(startY, endY);


            String name = selectionLayer.getFeatureSource().getSchema()
                    .getGeometryDescriptor().getLocalName();

            if (name == "") {
                name = "the_geom";
            }
            Filter bb = ff.bbox(ff.property(name), left, bottom, right, top,
                    getContext().getCoordinateReferenceSystem().toString());
            //System.out.println(bb.toString());
            if(selectionManager!=null) {
                selectionManager.selectionChanged(this, bb);
            }
            /*FeatureCollection fc;
            selection = null;
            try {
                fc = selectionLayer.getFeatureSource().getFeatures(bb);
                selection = fc;
            } catch (IOException e) {
                e.printStackTrace();
            }
*/
        }

        repaint();
    }

    private boolean isClickable() {
        // TODO Auto-generated method stub
        return clickable;
    }

    private org.geotools.styling.Style setupStyle(int type, Color color) {
        StyleFactory sf = org.geotools.factory.CommonFactoryFinder
                .getStyleFactory(null);
        StyleBuilder sb = new StyleBuilder();

        org.geotools.styling.Style s = sf.createStyle();
        s.setTitle("selection");

        // TODO parameterise the color
        PolygonSymbolizer ps = sb.createPolygonSymbolizer(color);
        ps.setStroke(sb.createStroke(color));

        LineSymbolizer ls = sb.createLineSymbolizer(color);
        Graphic h = sb.createGraphic();
        h.setMarks(new Mark[] { sb.createMark("square", color) });

        PointSymbolizer pts = sb.createPointSymbolizer(h);

        // Rule r = sb.createRule(new Symbolizer[]{ps,ls,pts});
        switch (type) {
        case POLYGON:
            s = sb.createStyle(ps);

            break;

        case POINT:
            s = sb.createStyle(pts);

            break;

        case LINE:
            s = sb.createStyle(ls);
        }

        return s;
    }

    public void highlightChanged(HighlightChangedEvent e) {
        // TODO Auto-generated method stub
        org.opengis.filter.Filter f = e.getFilter();

        try {
            highlightFeature = highlightLayer.getFeatureSource().getFeatures(f);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        repaint();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        // TODO Auto-generated method stub
        String prop = evt.getPropertyName();

        if (prop.equalsIgnoreCase("crs")) {
            context.setAreaOfInterest(context.getAreaOfInterest(),
                    (CoordinateReferenceSystem) evt.getNewValue());
        }
    }

    public boolean isReset() {
        return reset;
    }

    public void setReset(boolean reset) {
        this.reset = reset;
    }

    public void layerAdded(MapLayerListEvent event) {
        changed = true;

        if (context.getLayers().length == 1) { // the first one

            try {
                mapArea = context.getLayerBounds();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            reset = true;
        }

        repaint();
    }

    public void layerRemoved(MapLayerListEvent event) {
        changed = true;
        repaint();
    }

    public void layerChanged(MapLayerListEvent event) {
        changed = true;
        // System.out.println("layer changed - repaint");
        repaint();
    }

    public void layerMoved(MapLayerListEvent event) {
        changed = true;
        repaint();
    }

    private void drawRectangle(Graphics graphics) {
        // undraw last box/draw new box
        int left = Math.min(startX, lastX);
        int right = Math.max(startX, lastX);
        int top = Math.max(startY, lastY);
        int bottom = Math.min(startY, lastY);
        int width = right - left;
        int height = top - bottom;
        // System.out.println("drawing rect("+left+","+bottom+","+ width+","+
        // height+")");
        graphics.drawRect(left, bottom, width, height);
    }

    /**
     * if clickable is set to true then a single click on the map pane will zoom
     * or pan the map.
     *
     * @param clickable
     */
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    public FeatureCollection<? extends FeatureType, ? extends Feature> getSelection() {
        return selection;
    }

    public void setSelection(FeatureCollection<? extends FeatureType, ? extends Feature> selection) {
        this.selection = selection;
        repaint();
    }

    /* (non-Javadoc)
     * @see org.geotools.gui.swing.event.SelectionChangeListener#selectionChanged(org.geotools.gui.swing.event.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent e) {

        try {
            selection = selectionLayer.getFeatureSource().getFeatures(e.getFilter());
            repaint();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public void setSelectionManager(SelectionManager selectionManager) {
        this.selectionManager = selectionManager;
        this.selectionManager.addSelectionChangeListener(this);

    }
}
