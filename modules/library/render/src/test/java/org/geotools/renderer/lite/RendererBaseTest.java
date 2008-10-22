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
package org.geotools.renderer.lite;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.renderer.GTRenderer;
import org.geotools.test.TestData;

/**
 * DOCUMENT ME!
 * 
 * @author Simone Giannecchini
 */
public abstract class RendererBaseTest {

	public RendererBaseTest() {

	}

	/**
	 * bounds may be null
	 * 
	 * @param testName
	 *            DOCUMENT ME!
	 * @param renderer
	 *            DOCUMENT ME!
	 * @param timeOut
	 *            DOCUMENT ME!
	 * @param bounds
	 *            DOCUMENT ME!
	 * 
	 * @throws Exception
	 *             DOCUMENT ME!
	 */
    protected static void showRender(String testName, Object renderer,
            long timeOut, ReferencedEnvelope bounds) throws Exception {
        int w = 300;
        int h = 300;
        final BufferedImage image = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);
        render(renderer, g, new Rectangle(w, h), bounds);

        final String headless = System.getProperty("java.awt.headless", "false");
        if (!headless.equalsIgnoreCase("true") && TestData.isInteractiveTest()) {
            try {
                Frame frame = new Frame(testName);
                frame.addWindowListener(new WindowAdapter() {

                    public void windowClosing(WindowEvent e) {
                        e.getWindow().dispose();
                    }
                });

                Panel p = new Panel() {

                    /** <code>serialVersionUID</code> field */
                    private static final long serialVersionUID = 1L;

                    public void paint(Graphics g) {
                        g.drawImage(image, 0, 0, this);
                    }
                };

                frame.add(p);
                frame.setSize(w, h);
                frame.setVisible(true);

                Thread.sleep(timeOut);
                frame.dispose();
            } catch (HeadlessException exception) {
                // The test is running on a machine without X11 display. Ignore.
                return;
            }
        }
        boolean hasData = false; // All I can seem to check reliably.

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (image.getRGB(x, y) != -1) {
                    hasData = true;
                }
            }
        }

        assert (hasData);
    }

	/**
	 * responsible for actually rendering.
	 * 
	 * @param obj
	 *            DOCUMENT ME!
	 * @param g
	 * @param rect
	 *            DOCUMENT ME!
	 * @param bounds
	 */
	private static void render(Object obj, Graphics g, Rectangle rect,
			ReferencedEnvelope bounds) {
		if (obj instanceof GTRenderer) {
			GTRenderer renderer = (GTRenderer) obj;

			if (bounds == null) {
				renderer.paint((Graphics2D) g, rect, new AffineTransform());
			} else {
				renderer.paint((Graphics2D) g, rect, bounds);
			}
		}
	}
}
