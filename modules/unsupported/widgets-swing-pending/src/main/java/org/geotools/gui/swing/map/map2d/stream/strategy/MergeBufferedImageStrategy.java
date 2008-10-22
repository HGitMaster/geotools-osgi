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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.shape.ShapefileRenderer;

import com.vividsolutions.jts.geom.Envelope;
import java.lang.ref.WeakReference;



/**
 * Optimize Strategy for edition. high memory needed
 * 
 * @author Johann Sorel
 */
public class MergeBufferedImageStrategy extends AbstractRenderingStrategy {

    private GTRenderer renderer ;
    private DrawingThread thread ;
    private BufferComponent comp ;
    private MapContext buffercontext ;
    private GraphicsConfiguration GC ;
    
    boolean mustupdate ;    
    Map<MapLayer, BufferedImage> stock ;

    @Override
    protected JComponent init() {
        renderer = new ShapefileRenderer();
        thread = new DrawingThread(this);
        comp = new BufferComponent();
        buffercontext = new OneLayerContext();
        GC = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        mustupdate = false;
        stock = new HashMap<MapLayer, BufferedImage>();

        opimizeRenderer();
        thread.start();
        return comp;
    }

    private void opimizeRenderer() {
        Map rendererParams = new HashMap();
        rendererParams.put("optimizedDataLoadingEnabled", new Boolean(true));
        rendererParams.put("maxFiltersToSendToDatastore", new Integer(20));
        //rendererParams.put(ShapefileRenderer.TEXT_RENDERING_KEY, ShapefileRenderer.TEXT_RENDERING_STRING);
        // rendererParams.put(ShapefileRenderer.TEXT_RENDERING_KEY, ShapefileRenderer.TEXT_RENDERING_OUTLINE);
        rendererParams.put(ShapefileRenderer.SCALE_COMPUTATION_METHOD_KEY, ShapefileRenderer.SCALE_OGC);
        renderer.setRendererHints(rendererParams);

        RenderingHints rh;
        rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.add(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
        rh.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED));
        rh.add(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED));
        rh.add(new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF));
        rh.add(new RenderingHints(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE));
        renderer.setJava2DHints(rh);
    }
    

    private synchronized VolatileImage createBackBuffer() {

        Rectangle newRect = comp.getBounds();
        Rectangle mapRectangle = new Rectangle(newRect.width, newRect.height);


        if (mapRectangle.width > 0 && mapRectangle.height > 0) {
            return GC.createCompatibleVolatileImage(mapRectangle.width, mapRectangle.height, VolatileImage.TRANSLUCENT);
        } else {
            return GC.createCompatibleVolatileImage(1, 1, VolatileImage.TRANSLUCENT);
        }
    }

    void mergeBuffers() {
        VolatileImage vi = createBackBuffer();
        Graphics2D g2d = (Graphics2D) vi.getGraphics();

        MapLayer[] layers = getContext().getLayers();

        for (int i = 0,  max = layers.length; i < max; i++) {
//        for (int i= layers.length-1; i>=0; i--){
            MapLayer layer = layers[i];
            BufferedImage img = stock.get(layer);
            if (img != null) {
                g2d.drawImage(img, null, 0, 0);
            }
        }

        comp.setBuffer(vi);
    }
    
    
    //-----------------abstract-------------------------------------------------

    public synchronized BufferedImage createBufferImage(MapLayer layer) {

        try {
            buffercontext.setCoordinateReferenceSystem(getContext().getCoordinateReferenceSystem());
        } catch (Exception e) {
        }

        buffercontext.addLayer(layer);
        BufferedImage buf = createBufferImage(buffercontext);
        buffercontext.clearLayerList();
        return buf;

    }

    public synchronized BufferedImage createBufferImage(MapContext context) {


        synchronized (renderer) {
            Rectangle newRect = comp.getBounds();
            Rectangle mapRectangle = new Rectangle(newRect.width, newRect.height);

            if (mapRectangle.width > 0 && mapRectangle.height > 0) {
                //NOT OPTIMIZED
//            BufferedImage buf = new BufferedImage(mapRectangle.width, mapRectangle.height, BufferedImage.TYPE_INT_ARGB);
//            Graphics2D ig = buf.createGraphics();
                //GraphicsConfiguration ACCELERATION 
                BufferedImage buf = GC.createCompatibleImage(mapRectangle.width, mapRectangle.height, BufferedImage.TRANSLUCENT);
                Graphics2D ig = buf.createGraphics();

                renderer.stopRendering();
                renderer.setContext(context);

                Envelope env = getMapArea();

                if (isValidEnvelope(env)) {
                    try {
                        renderer.paint((Graphics2D) ig, mapRectangle, env);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return buf;
            } else {
                return null;
            }
        }

    }

    @Override
    public BufferedImage getSnapShot() {
        return comp.getBuffer();
    }

    @Override
    public void refresh() {
        try {
            mapArea = fixAspectRatio(comp.getBounds(), getMapArea());
        } catch (Exception e) {
        }

        mustupdate = true;
        thread.wake();
    }
    
    
    @Override
    public void dispose(){
        super.dispose();
        thread.dispose();
    }

    //-------------layer events-------------------------------------------------

    public void layerAdded(MapLayerListEvent event) {

        MapLayer layer = event.getLayer();
        stock.put(layer, createBufferImage(layer));

        mergeBuffers();

        MapContext context = getContext();
//        if (context.getLayerCount() == 1) {
//            try {
//                ReferencedEnvelope env = context.getLayerBounds();
//
//                if (env != null) {
//                    setMapArea(env);
//                }
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
    }

    public void layerRemoved(MapLayerListEvent event) {
        stock.remove(event.getLayer());
        mergeBuffers();
    }

    public void layerChanged(MapLayerListEvent event) {
        MapLayer layer = event.getLayer();
        stock.put(layer, createBufferImage(layer));
        mergeBuffers();
    }

    public void layerMoved(MapLayerListEvent event) {
        mergeBuffers();
    }

    //----------private classes-------------------------------------------------
        
    private class BufferComponent extends JComponent {

        private VolatileImage img = null;

        public void setBuffer(VolatileImage buf) {
            img = buf;
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    repaint();
                }
            });

        }

        public BufferedImage getBuffer() {
            if (img != null) {
                return img.getSnapshot();
            } else {
                return null;
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            if (img != null) {
                g.drawImage(img, 0, 0, this);
            }
        }
        }

    
    
}
class DrawingThread extends Thread {

        private boolean dispose = false;

        private final WeakReference<MergeBufferedImageStrategy> ref;
        
        DrawingThread(MergeBufferedImageStrategy strategy){
            this.ref = new WeakReference<MergeBufferedImageStrategy>(strategy);
        }
        
        
        public void dispose(){
            dispose = true;
            wake();
        }
        
        @Override
        public void run() {

            while (true && !dispose) {
                
                MergeBufferedImageStrategy st = ref.get();
                
                if(dispose || st == null){
                    break;
                }
                
                
                if (st.mustupdate) {
                    st.setPainting(true);

                    st.stock.clear();
                    MapLayer[] layers = st.getContext().getLayers();

                    for (MapLayer layer : layers) {
                        st.stock.put(layer, st.createBufferImage(layer));
                        st.mergeBuffers();
                    }

                    st.mustupdate = false;
                    st.setPainting(false);
                }

                block();
            }
        }

        public synchronized void wake() {
            notifyAll();
        }

        private synchronized void block() {
            try {
                wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    
