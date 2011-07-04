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

package org.geotools.swing;

import java.awt.event.WindowEvent;

/**
 * Implemented by objects that wish to receive events published
 * by a {@code JTextReporter}.
 *
 * @author Michael Bedward
 * @since 2.6
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/unsupported/swing/src/main/java/org/geotools/swing/TextReporterListener.java $
 * @version $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/unsupported/swing/src/main/java/org/geotools/swing/TextReporterListener.java $
 */
public interface TextReporterListener {

    /**
     * Called by the reporter when it is being closed
     *
     * @param ev the window event issued by the system
     */
    public void onReporterClosed(WindowEvent ev);

    /**
     * Called by the text reporter when text has been appended
     *
     * @param newTextStartLine the line number at which the newly
     *        appended text begins
     */
    public void onReporterUpdated(int newTextStartLine);
}
