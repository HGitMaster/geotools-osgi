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

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import junit.framework.TestCase;

import org.geotools.test.TestData;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryFinder;

/**
 * @author    pc
 */
public class SVGGlyphRendererTest extends TestCase {

    private SVGGlyphRenderer renderer;

    protected void setUp() throws Exception {
        super.setUp();
        renderer = new SVGGlyphRenderer();
    }

    public void testCanRender() {
        // test against the resurgence or
        // http://jira.codehaus.org/browse/GEOT-920
        assertTrue(renderer.canRender("image/svg+xml"));
    }

    // shape and gradient
    public void testRenderGradient() throws InterruptedException {
        StyleFactory sf = StyleFactoryFinder.createStyleFactory();
        ExternalGraphic eg = sf.createExternalGraphic(this.getClass().getResource(
                "test-data/gradient.svg"), "image/svg+xml");
        BufferedImage image = renderer.render(null, eg, null,-1);
        assertNotNull(image);
        showImage("Opacity", 1000, image);
    }

    // shape and gradient
    public void testRenderCss() throws InterruptedException {
        StyleFactory sf = StyleFactoryFinder.createStyleFactory();
        ExternalGraphic eg = sf.createExternalGraphic(this.getClass().getResource(
                "test-data/squarecss.svg"), "image/svg+xml");
        BufferedImage image = renderer.render(null, eg, null,-1);
        assertNotNull(image);
        showImage("Opacity", 1000, image);
    }

    // text rendering (added just to make sure we have all the jars needed for
    // batik
    // since a few things are needed only when using certain SVG features it
    // seems)
    public void testRenderText() throws InterruptedException {
        StyleFactory sf = StyleFactoryFinder.createStyleFactory();
        ExternalGraphic eg = sf.createExternalGraphic(this.getClass().getResource(
                "test-data/text.svg"), "image/svg+xml");
        BufferedImage image = renderer.render(null, eg, null,-1);
        assertNotNull(image);
        showImage("Opacity", 1000, image);
    }

    private void showImage(final String testName, final int timeout, final BufferedImage image)
            throws InterruptedException {
        if ((System.getProperty("java.awt.headless") == null || !System.getProperty(
                "java.awt.headless").equals("true"))
                && TestData.isInteractiveTest()) {
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
            frame.setSize(image.getWidth() + 50, image.getHeight() + 50);
            frame.setVisible(true);

            Thread.sleep(timeout);
            frame.dispose();
        }
    }
}
