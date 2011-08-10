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

package org.geotools.swing.styling;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * An Icon to display a color. Used by {@code JSimpleStyleDialog}.
 *
 * @author Michael Bedward
 * @since 2.6
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/unsupported/swing/src/main/java/org/geotools/swing/styling/JColorIcon.java $
 * @version $Id: JColorIcon.java 37306 2011-05-25 06:13:21Z mbedward $
 */
public class JColorIcon implements Icon {
    
    private int width;
    private int height;
    private Color color;

    /**
     * Construtor.
     *
     * @param width icon width
     * @param height icon height
     * @param color initial color
     */
    public JColorIcon(int width, int height, Color color) {
        this.width = width;
        this.height = height;
        this.color = color;
    }

    /**
     * Paint the icon using the current color. This method is
     * invoked by the System.
     *
     * @param c arg presently ignored
     * @param g graphics object
     * @param x x position
     * @param y y position
     */
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);
        g.fillRect(x, y, width, height);

        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
    }

    /**
     * Get the icon's width
     *
     * @return width in pixels
     */
    public int getIconWidth() {
        return width;
    }

    /**
     * Get the icon's height
     *
     * @return height in pixels
     */
    public int getIconHeight() {
        return height;
    }

    /**
     * Set the color
     * @param color new color
     */
    public void setColor(Color color) {
        this.color = color;
    }

}