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
package org.geotools.gui.swing;

// J2SE dependencies
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.util.Locale;
import javax.swing.JFrame;

// JAI dependencies
import javax.media.jai.operator.AddConstDescriptor;
import javax.media.jai.operator.ConstantDescriptor;
import javax.media.jai.operator.GradientMagnitudeDescriptor;
import javax.media.jai.operator.MultiplyConstDescriptor;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Geotools dependencies
import org.geotools.measure.AngleFormat;
import org.geotools.resources.Arguments;
import org.geotools.resources.image.ColorUtilities;
import org.geotools.gui.swing.referencing.CoordinateChooser;
import org.geotools.gui.swing.image.OperationTreeBrowser;
import org.geotools.gui.swing.image.GradientKernelEditor;
import org.geotools.gui.swing.image.KernelEditor;
import org.geotools.gui.swing.image.ColorRamp;


/**
 * Tests a set of widgets. Widgets will be displayed only if the test is run from the main
 * method. Otherwise (i.e. if run from Maven), widgets are invisibles.
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing/src/test/java/org/geotools/gui/swing/MiscellaneousWidgetTest.java $
 * @version $Id: MiscellaneousWidgetTest.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (IRD)
 */
public class MiscellaneousWidgetTest extends TestBase {
    /**
     * Constructs the test case.
     */
    public MiscellaneousWidgetTest(final String name) {
        super(name);
    }

    /**
     * Run the test case from the command line.
     */
    public static void main(final String[] args) {
        main(args, suite());
    }

    /**
     * Returns the suite of tests.
     */
    public static Test suite() {
        return new TestSuite(MiscellaneousWidgetTest.class);
    }

    /**
     * Tests the {@link About} dialog.
     */
    public void testAbout() {
        show(new About());
    }

    /**
     * Tests the {@link DisjointLists}.
     */
    public void testDisjointLists() {
        final DisjointLists test = new DisjointLists();
        test.addElements(Locale.getAvailableLocales());
        show(test);
    }

    /**
     * Tests the {@link FormatChooser}.
     */
    public void testFormatChooser() {
        FormatChooser test = new FormatChooser(new AngleFormat());
        show(test);
    }

    /**
     * Tests the {@link CoordinateChooser}.
     */
    public void testCoordinateChooser() {
        CoordinateChooser test = new CoordinateChooser();
        show(test);
    }

    /**
     * Tests the {@link KernelEditor}.
     */
    public void testKernelEditor() {
        KernelEditor test = new KernelEditor();
        test.addDefaultKernels();
        show(test);
    }

    /**
     * Tests the {@link GradientKernelEditor}.
     */
    public void testGradientKernelEditor() {
        GradientKernelEditor test = new GradientKernelEditor();
        test.addDefaultKernels();
        show(test);
    }

    /**
     * Tests the {@link ColorRamp}.
     */
    public void testColorRamp() {
        ColorRamp test = new ColorRamp();
        final int[] ARGB = new int[256];
        ColorUtilities.expand(new Color[] {Color.RED, Color.ORANGE, Color.YELLOW, Color.CYAN},
                              ARGB, 0, ARGB.length);
        test.setColors(ColorUtilities.getIndexColorModel(ARGB));
        show(test);
    }

    /**
     * Tests the {@link Plot2D}.
     */
//    public void testPlot2D() {
//        final Random random = new Random();
//        Plot2D test = new Plot2D(true, false);
//        test.newAxis(0, "Some x values");
//        test.newAxis(1, "Some y values");
//        for (int j=0; j<2; j++) {
//            final float[] x = new float[800];
//            final float[] y = new float[800];
//            for (int i=0; i<x.length; i++) {
//                x[i] = i/10f;
//                y[i] = (float)random.nextGaussian();
//                if (i!=0) {
//                    y[i] += y[i-1];
//                }
//            }
//            test.addSeries("Random values", x, y);
//        }
//        test.setPaintingWhileAdjusting(true);
//        show(test.createScrollPane());
//    }

    /**
     * Tests the {@link ZoomPane}.
     */
    public void testZoomPane() {
        final Rectangle rect = new Rectangle(100,200,100,93);
        final Polygon   poly = new Polygon(new int[] {125,175,150}, new int[] {225,225,268}, 3);
        final ZoomPane  pane = new ZoomPane(ZoomPane.UNIFORM_SCALE |
                                            ZoomPane.TRANSLATE_X   |
                                            ZoomPane.TRANSLATE_Y   |
                                            ZoomPane.ROTATE        |
                                            ZoomPane.RESET         |
                                            ZoomPane.DEFAULT_ZOOM)
        {
            public Rectangle2D getArea() {
                return rect;
            }

            protected void paintComponent(final Graphics2D graphics) {
                graphics.transform(zoom);
                graphics.setColor(Color.RED);
                graphics.fill(poly);
                graphics.setColor(Color.BLUE);
                graphics.draw(poly);
                graphics.draw(rect);
            }
        };
        pane.setPaintingWhileAdjusting(true);
        show(pane, "ZoomPane");
    }

    /**
     * Tests the {@link OperationTreeBrowser}.
     */
    public void testOperationTree() {
        RenderedImage image;
        final Float size = new Float(200);
        final Byte value = new Byte((byte)10);
        image = ConstantDescriptor.create(size,size, new Byte[]{value}, null);
        image = MultiplyConstDescriptor.create(image, new double[] {2}, null);
        image = GradientMagnitudeDescriptor.create(image, null, null, null);
        image = AddConstDescriptor.create(image, new double[] {35}, null);
        show(new OperationTreeBrowser(image));
    }
}
