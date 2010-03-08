/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
package org.geotools.map;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.ows.CRSEnvelope;
import org.geotools.data.ows.Layer;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.event.MapBoundsEvent;
import org.geotools.map.event.MapBoundsListener;
import org.geotools.map.event.MapLayerEvent;
import org.geotools.referencing.CRS;
import org.geotools.resources.coverage.FeatureUtilities;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class WMSMapLayer extends DefaultMapLayer implements MapLayer, MapBoundsListener,
        ComponentListener {
    /** The logger for the map module. */
    static public final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.map");

    static GridCoverageFactory gcf = new GridCoverageFactory();

    private Layer layer;

    private WebMapServer wms;

    GridCoverage2D grid;

    private ReferencedEnvelope viewport;

    private ReferencedEnvelope bounds;

    private int width = 400;

    private int height = 200;

    private StyleFactory factory = CommonFactoryFinder.getStyleFactory(null);

    private boolean transparent = true;

    private String bgColour;

    private String exceptions = "application/vnd.ogc.se_inimage";

    private static ExecutorService executor = Executors.newCachedThreadPool();

    Runnable getmap = new Runnable() {
        public void run() {
            try {
                getmap();
            } catch (Throwable t) {
                LOGGER.log(Level.FINE, "Unable to refresh getmap", t);
            }
        }
    };

    public WMSMapLayer(WebMapServer wms, Layer layer) {
        super((FeatureSource<SimpleFeatureType, SimpleFeature>) null, null, "");
        this.layer = layer;
        this.wms = wms;
        setDefaultStyle();
        executor.execute(getmap);
    }

    public void setDefaultStyle() {
        RasterSymbolizer symbolizer = factory.createRasterSymbolizer();

        // SLDParser stylereader = new SLDParser(factory,sld);
        // org.geotools.styling.Style[] style = stylereader.readXML();
        Style style = factory.createStyle();
        Rule[] rules = new Rule[1];
        rules[0] = factory.createRule();
        rules[0].setSymbolizers(new Symbolizer[] { symbolizer });

        FeatureTypeStyle type = factory.createFeatureTypeStyle(rules);
        style.addFeatureTypeStyle(type);
        setStyle(style);
    }

    private static ReferencedEnvelope calculateRequestBBox(Layer layer,
            ReferencedEnvelope viewport, CoordinateReferenceSystem crs) throws Exception {
        
        GeneralEnvelope general = layer.getEnvelope(crs);
        ReferencedEnvelope layersBBox = new ReferencedEnvelope(general);
        if (layersBBox.isNull()) {
            layersBBox = null;
        }
        if( viewport == null ){
            viewport = layersBBox;
        }
        
        ReferencedEnvelope reprojectedViewportBBox = viewport.transform(crs, true);
        if (reprojectedViewportBBox.isNull()) {
        }
        ReferencedEnvelope interestBBox;
        if (layersBBox == null) {
            interestBBox = reprojectedViewportBBox;
        } else {
            interestBBox = new ReferencedEnvelope(reprojectedViewportBBox.intersection(layersBBox),
                    crs);
        }
        if (interestBBox.isNull()) {
            return null; // don't draw we are outside the viewscreen
        }
        return interestBBox;
    }

    private void getmap() throws Exception {
        GetMapRequest mapRequest = wms.createGetMapRequest();
        mapRequest.addLayer(layer);

        // System.out.println(width + " " + height);
        mapRequest.setDimensions(getWidth(), getHeight());
        mapRequest.setFormat("image/png");

        if (bgColour != null) {
            mapRequest.setBGColour(bgColour);
        }

        mapRequest.setExceptions(exceptions);

        Set<String> srs = layer.getSrs();
        String srsName = "EPSG:4326";

        if (srs.contains("EPSG:4326")) { // really we should get the underlying
            // map pane CRS
            srsName = "EPSG:4326";
        } else {
            srsName = (String) srs.iterator().next();
        }
        CoordinateReferenceSystem crs = CRS.decode(srsName);

        // fix the bounds for the shape of the window.
        ReferencedEnvelope requestBBox;
        if( viewport != null ){
            requestBBox = calculateRequestBBox(layer, viewport, crs);
        }
        else {
            requestBBox = getBounds();
        }

        // System.out.println(bbox.toString());
        mapRequest.setSRS(srsName);
        mapRequest.setBBox(requestBBox);
        mapRequest.setTransparent(transparent);

        URL request = mapRequest.getFinalURL();

        System.out.println(request.toString());
        InputStream is = null;

        try {
            URLConnection connection = request.openConnection();
            String type = connection.getContentType();
            is = connection.getInputStream();

            if (type.equalsIgnoreCase("image/png")) {
                BufferedImage image = ImageIO.read(is);
                System.out.println("get map completed");
                grid = gcf.create(layer.getTitle(), image, requestBBox);
                // System.out.println("fetched new grid");
                // if (featureSource == null)
                featureSource = DataUtilities.source(FeatureUtilities.wrapGridCoverage(grid));
                if( viewport == null ){
                    System.out.println("get map complete but viewport not yet established");
                }
                else {
                    fireMapLayerListenerLayerChanged(new MapLayerEvent(this, MapLayerEvent.DATA_CHANGED));                    
                }
            } else {
                System.out.println("error content type is " + type);

                if (StringUtils.contains(type, "text") || StringUtils.contains(type, "xml")) {
                    String line = "";
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                    }
                }
            }        
        } finally {
            if (is != null) {
                    is.close();
            }
        }
    }

    public void mapBoundsChanged(MapBoundsEvent event) {
        viewport = ((MapContext) event.getSource()).getAreaOfInterest();

        // System.out.println("old:" + bbox + "\n" + "new:"
        // + event.getOldAreaOfInterest());
        if (!viewport.equals(event.getOldAreaOfInterest())) {
            System.out.println("bbox changed - fetching new grid");

            executor.execute(getmap);
        }
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public GridCoverage2D getGrid() {
        return grid;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    public void componentHidden(ComponentEvent e) {
        // TODO Auto-generated method stub
    }

    public void componentMoved(ComponentEvent e) {
        // TODO Auto-generated method stub
    }

    public void componentResized(ComponentEvent e) {
        Component c = (Component) e.getSource();
        width = c.getWidth();
        height = c.getHeight();
    }

    public void componentShown(ComponentEvent e) {
        Component c = (Component) e.getSource();
        width = c.getWidth();
        height = c.getHeight();
    }

    public String getBgColour() {
        return bgColour;
    }

    public void setBgColour(String bgColour) {
        this.bgColour = bgColour;
    }

    public String getExceptions() {
        return exceptions;
    }

    /**
     * Set the type of exception reports. Valid values are: "application/vnd.ogc.se_xml" (the
     * default) "application/vnd.ogc.se_inimage" "application/vnd.ogc.se_blank"
     * 
     * @param exceptions
     */
    public void setExceptions(String exceptions) {
        this.exceptions = exceptions;
    }

    public synchronized ReferencedEnvelope getBounds() {
        if (bounds == null) {
            HashMap<Object, CRSEnvelope> bboxes = layer.getBoundingBoxes();

            Set<String> srs = layer.getSrs();
            String srsName = "EPSG:4326";

            if (srs.contains("EPSG:4326")) {
                // really we should get the underlying
                // map pane CRS from viewport
                srsName = "EPSG:4326";
            } else {
                srsName = (String) srs.iterator().next();
            }
            CoordinateReferenceSystem crs = null;
            try {
                crs = CRS.decode(srsName);
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Bounds unavailable for layer" + layer);
                return null;
            }
            GeneralEnvelope general = layer.getEnvelope(crs);
            bounds = new ReferencedEnvelope(general);
            /*
             * CRSEnvelope bb = (CRSEnvelope) bboxes.get(srsName); if (bb == null) { // something
             * bad happened bb = layer.getLatLonBoundingBox(); bb.setEPSGCode("EPSG:4326"); // for
             * some reason WMS doesn't set this srsName = "EPSG:4326"; } bounds = new
             * ReferencedEnvelope(bb.getMinX(), bb.getMaxX(), bb.getMinY(), bb.getMaxY(),
             * coordinateReferenceSystem);
             */
        }
        return bounds;
    }
}