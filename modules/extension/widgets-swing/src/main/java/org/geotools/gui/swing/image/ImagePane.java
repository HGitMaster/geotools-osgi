/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing.image;

// J2SE dependencies
import java.awt.geom.Rectangle2D;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;

// JAI dependencies
import javax.media.jai.JAI;

// Geotools dependencies
import org.geotools.gui.swing.ZoomPane;


/**
 * A simple image viewer. This widget accepts either {@linkplain RenderedImage rendered} or
 * {@linkplain RenderableImage renderable} image. Rendered image are display immediately,
 * while renderable image will be rendered in a background thread when first requested.
 * This widget may scale down images for faster rendering. This is convenient for image
 * previews, but should not be used as a "real" renderer for full precision images.
 *
 * @since 2.3
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/extension/widgets-swing/src/main/java/org/geotools/gui/swing/image/ImagePane.java $
 * @version $Id: ImagePane.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (IRD)
 */
public class ImagePane extends ZoomPane implements Runnable {
    /**
     * The default size for rendered image produced by a {@link RenderableImage}.
     * This is also the maximum size for a {@link RenderedImage}; bigger image
     * will be scaled down using JAI's "Scale" operation for faster rendering.
     */
    private final int renderedSize;

    /**
     * The renderable image, or {@code null} if none. If non-null, then the {@link #run}
     * method will transform this renderable image into a rendered one when first requested.
     * Once the image is rendered, this field is set to {@code null}.
     */
    private RenderableImage renderable;

    /**
     * The rendered image, or {@code null} if none. This image may be explicitly set
     * by {@link #setImage(RenderedImage)}, or computed by {@link #run}.
     */
    private RenderedImage rendered;

    /**
     * {@code true} if the {@link #run} method has been invoked for the current image.
     * This field is used in order to avoid to start more than one thread for the same
     * {@linkplain #renderable} image.
     */
    private volatile boolean running;

    /**
     * Constructs an initially empty image pane with a default rendered image size.
     */
    public ImagePane() {
        this(512);
    }

    /**
     * Constructs an initially empty image pane with the specified rendered image size.
     * The {@code renderedSize} argument is the <em>maximum</em> width and height for
     * {@linkplain RenderedImage rendered image}. Images greater than this value will be
     * scaled down for faster rendering.
     */
    public ImagePane(final int renderedSize) {
        super(UNIFORM_SCALE | TRANSLATE_X | TRANSLATE_Y | ROTATE | RESET | DEFAULT_ZOOM);
        setResetPolicy(true);
        this.renderedSize = renderedSize;
    }

    /**
     * Sets the source renderable image.
     */
    public void setImage(final RenderableImage image) {
        renderable = image;
        rendered   = null;
        running    = false;
        reset();
        repaint();
    }

    /**
     * Sets the source rendered image.
     */
    public void setImage(RenderedImage image) {
        if (image != null) {
            final float scale = Math.min(((float)renderedSize) / image.getWidth(),
                                         ((float)renderedSize) / image.getHeight());
            if (scale < 1) {
                final Float sc = new Float(scale);
                image = JAI.create("Scale",
                                   new ParameterBlock().addSource(image).add(sc).add(sc));
            }
        }
        renderable = null;
        rendered   = image;
        running    = false;
        reset();
        repaint();
    }

    /**
     * Reset the default zoom. This method overrides the default implementation in
     * order to keep the <var>y</var> axis in its Java2D direction (<var>y</var>
     * value increasing down), which is the usual direction of most image.
     */
    public void reset() {
        reset(getZoomableBounds(null), false);
    }

    /**
     * Returns the image bounds, or {@code null} if none. This is used by
     * {@link ZoomPane} in order to set the initial zoom.
     */
    public Rectangle2D getArea() {
        final RenderedImage rendered = this.rendered; // Protect from change in an other thread
        if (rendered != null) {
            return new Rectangle(rendered.getMinX(),  rendered.getMinY(),
                                 rendered.getWidth(), rendered.getHeight());
        }
        return null;
    }

    /**
     * Paint the image. If the image was a {@link RenderableImage}, then a {@link RenderedImage}
     * will be computed in a background thread when this method is first invoked.
     */
    protected void paintComponent(final Graphics2D graphics) {
        final RenderedImage rendered = this.rendered; // Protect from change in an other thread
        if (rendered == null) {
            if (renderable!=null && !running) {
                running = true;
                final Thread runner = new Thread(this, "Renderer");
                runner.setPriority(Thread.NORM_PRIORITY-2);
                runner.start();
            }
        } else {
            graphics.drawRenderedImage(rendered, zoom);
        }
    }

    /**
     * Creates a {@linkplain RenderedImage rendered} view of the {@linkplain RenderableImage
     * renderable} image and notifies {@link ZoomPane} when the result is ready. This method
     * is run in a background thread and should not be invoked directly, unless the user wants
     * to trig the {@link RenderedImage} creation immediately.
     */
    public void run() {
        running = true;
        final RenderableImage producer = renderable; // Protect from change.
        if (producer != null) {
            final RenderedImage image = producer.createScaledRendering(renderedSize, 0, null);
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    if (producer == renderable) {
                        setImage(image);
                    }
                }
            });
        }
    }
}
