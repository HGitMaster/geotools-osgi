/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
import java.util.Locale;
import java.awt.Component;
import java.awt.HeadlessException;
import javax.swing.JFrame;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;

// Geotools dependencies
import org.geotools.resources.Arguments;
import org.geotools.resources.Classes;


/**
 * Base class for test on widgets. Widgets will be displayed only if the test is run
 * from the main method. Otherwise (i.e. if run from Maven), widgets are invisibles.
 *
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/extension/widgets-swing/src/test/java/org/geotools/gui/swing/TestBase.java $
 * @version $Id: TestBase.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux
 */
public class TestBase extends TestCase {    
    /**
     * Set to {@code true} if windows should be visible.
     */
    private static boolean display;

    /**
     * The location of the next frame to show.
     */
    private static volatile int location = 60;

    /**
     * Creates a new instance of {@code TestBase}.
     */
    public TestBase(final String name) {
        super(name);
    }

    /**
     * Run the test case from the command line. This method should be invoked
     * from the {@code main} method in subclasses.
     */
    protected static void main(String[] args, final Test suite) {
        display = true;
        final Arguments arguments = new Arguments(args);
        args = arguments.getRemainingArguments(0);
        Locale.setDefault(arguments.locale);
        junit.textui.TestRunner.run(suite);
    }

    /**
     * Show a component in a frame. The component will be shown only if the test suite
     * is executed from the {@link #main main} method.
     *
     * @param component The component to show.
     */
    protected static void show(final Component component) {
        show(component, Classes.getShortClassName(component));
    }    

    /**
     * Show a component in a frame. The component will be shown only if the test suite
     * is executed from the {@link #main main} method.
     *
     * @param component The component to show.
     * @param title The window title.
     */
    protected static void show(final Component component, final String title) {
        if (display) try {
            final JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(component);
            frame.setLocation(location, location);
            frame.pack();
            frame.setVisible(true);
            location += 30;
            if (false) try {
                Thread.sleep(500);
            } catch (InterruptedException exception) {
                // Ignore
            }
        } catch (HeadlessException exception) {
            // The test is running on a machine without display. Ignore.
        }
    }
}
