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

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.opengis.util.ProgressListener;
import org.geotools.resources.Arguments;


/**
 * Tests {@link ProgressWindow}. The window will be displayed only if this test
 * is executed through its {@link #main main} method.
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing/src/test/java/org/geotools/gui/swing/ProgressWindowTest.java $
 * @version $Id: ProgressWindowTest.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (IRD)
 */
public class ProgressWindowTest extends TestCase {
    /** The description, if any.           */ private static String description;
    /** The source, if any.                */ private static String source;
    /** Text to put in the margin, if any. */ private static String margin;
    /** Warning to print, if any.          */ private static String warning;
    /** {@code true} for enabling display. */ private static boolean display = false;

    /**
     * Construct the test case.
     */
    public ProgressWindowTest(final String name) {
        super(name);
    }

    /**
     * Run the test case from the command line.
     */
    public static void main(final String[] args) throws Exception {
        final Arguments arguments = new Arguments(args);
        Locale.setDefault(arguments.locale);
        description = arguments.getOptionalString("-description");
        source      = arguments.getOptionalString("-source");
        margin      = arguments.getOptionalString("-margin");
        warning     = arguments.getOptionalString("-warning");
        display     = true;
        if (description == null) description = "Some description";
        if (source      == null) source      = "Some source";
        if (margin      == null) margin      = "(1)";
        if (warning     == null) warning     = "Some warning";
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the suite of tests.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(ProgressWindowTest.class);
        return suite;
    }

    /**
     * Test the progress listener with a progress ranging from 0 to 100%
     */
    public void testProgress() throws InterruptedException {
        if (display) try {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY-2);
            final ProgressListener progress = new ProgressWindow(null);
            progress.setDescription(description);
            progress.started();
            for (int i=0; i<=100; i++) {
                progress.progress(i);
                Thread.sleep(20);
                if ((i==40 || i==80) && warning!=null) {
                    progress.warningOccurred(source, margin, warning);
                }
            }
            progress.complete();
        } catch (HeadlessException e) {
            // do nothing
        }
    }
}
