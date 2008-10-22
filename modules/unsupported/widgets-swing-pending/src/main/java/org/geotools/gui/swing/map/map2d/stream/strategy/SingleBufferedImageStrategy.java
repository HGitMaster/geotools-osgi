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
import java.awt.geom.AffineTransform;

/**
 * Not optimize Strategy, use a single bufferedImage. slow.
 * Must repaint everything each time.
 * 
 * @author Johann Sorel
 */
public class SingleBufferedImageStrategy extends AbstractRenderingStrategy {

    private GTRenderer renderer = null;
    private DrawingThread thread;
    private BufferComponent comp;
    private MapContext buffercontext;
    private GraphicsConfiguration GC;
    private boolean mustupdate;

    @Override
    protected JComponent init() {

        mustupdate = false;

        thread = new DrawingThread();
        comp = new BufferComponent();
        buffercontext = new OneLayerContext();
        thread.start();
        return comp;
    }

    private void testRenderer() {
        if (renderer == null) {
            renderer = new ShapefileRenderer();
            opimizeRenderer();
        }
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
        testRenderer();
        GraphicsConfiguration GC = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

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

    public void setAffineTransform(AffineTransform affineTransform) {
        this.affineTransform = affineTransform;

        testRenderer();
        GraphicsConfiguration GC = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

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

                try {
                    renderer.paint((Graphics2D) ig, mapRectangle, affineTransform);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                comp.setBuffer(buf);
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
    public void dispose() {
        super.dispose();
        thread.dispose();
    }

    //-------------layer events-------------------------------------------------
    public void layerAdded(MapLayerListEvent event) {

//        if (getContext().getLayerCount() == 1) {
//            try {
//                ReferencedEnvelope env = getContext().getLayerBounds();
//                if (env != null) {
//                    setMapArea(env);
//                }
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        } else {
        testRefresh();
//        }
    }

    public void layerRemoved(MapLayerListEvent event) {
        testRefresh();
    }

    public void layerChanged(MapLayerListEvent event) {
        testRefresh();
    }

    public void layerMoved(MapLayerListEvent event) {
        testRefresh();
    }

    //----------private classes-------------------------------------------------
    private class DrawingThread extends Thread {

        private boolean dispose = false;

        public void dispose() {
            dispose = true;
            wake();
        }

        @Override
        public void run() {

            while (true && !dispose) {

                if (dispose) {
                    break;
                }

                if (mustupdate) {
                    setPainting(true);
                    comp.setBuffer(createBufferImage(getContext()));
                    mustupdate = false;
                    setPainting(false);
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

    private class BufferComponent extends javax.swing.JComponent {

        private BufferedImage img = null;

        public void setBuffer(BufferedImage buf) {
            img = buf;
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    repaint();
                }
            });

        }

        public BufferedImage getBuffer() {
            return img;
        }

        @Override
        public void paintComponent(Graphics g) {
            if (img != null) {
                g.drawImage(img, 0, 0, this);
            }
        }
        }
}
    

    
