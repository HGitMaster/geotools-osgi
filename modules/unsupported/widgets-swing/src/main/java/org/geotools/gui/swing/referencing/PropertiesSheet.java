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
package org.geotools.gui.swing.referencing;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.opengis.referencing.IdentifiedObject;

import org.geotools.resources.Classes;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.referencing.wkt.UnformattableObjectException;


/**
 * Display informations about a CRS object. Current implementation only display the
 * <cite>Well Known Text</cite> (WKT). We may provide more informations in a future
 * version.
 *
 * @since 2.3
 * @version $Id: PropertiesSheet.java 30655 2008-06-12 20:24:25Z acuster $
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing/src/main/java/org/geotools/gui/swing/referencing/PropertiesSheet.java $
 * @author Martin Desruisseaux (IRD)
 */
@SuppressWarnings("serial")
public class PropertiesSheet extends JComponent {
    /**
     * Provides different view of the CRS object (properties, WKT, etc.).
     */
    private final JTabbedPane tabs;

    /**
     * The <cite>Well Known Text</cite> area.
     */
    private final JTextArea wktArea;

    /**
     * Creates a new, initially empty, property sheet.
     */
    public PropertiesSheet() {
        tabs    = new JTabbedPane();
        wktArea = new JTextArea();
        wktArea.setEditable(false);
        tabs.addTab("WKT", new JScrollPane(wktArea));
        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }

    /**
     * Sets the object to display in this property sheet.
     *
     * @param item The object to display info about.
     */
    public void setIdentifiedObject(final IdentifiedObject item) {
        String text;
        try {
            text = item.toWKT();
        } catch (UnsupportedOperationException e) {
            text = e.getLocalizedMessage();
            if (text == null) {
                text = Classes.getShortClassName(e);
            }
            final String lineSeparator = System.getProperty("line.separator", "\n");
            if (e instanceof UnformattableObjectException) {
                text = Vocabulary.format(VocabularyKeys.WARNING) + ": " + text +
                        lineSeparator + lineSeparator + item + lineSeparator;
            } else {
                text = Vocabulary.format(VocabularyKeys.ERROR) + ": " + text + lineSeparator;
            }
        }
        wktArea.setText(text);
    }

    /**
     * Sets an error message to display instead of the current identified object.
     *
     * @param message The error message.
     */
    public void setErrorMessage(final String message) {
        wktArea.setText(Vocabulary.format(VocabularyKeys.ERROR_$1, message));
    }
}
