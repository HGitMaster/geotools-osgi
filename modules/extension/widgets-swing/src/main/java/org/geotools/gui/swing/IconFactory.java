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
package org.geotools.gui.swing;

// J2SE dependencies
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;


/**
 * A factory for {@link Icon}. This class caches some of the created icons. This factory should be
 * used only for small icons, since they may be cached for the JVM lifetime. This class is used
 * especially for <A HREF="http://java.sun.com/developer/techDocs/hi/repository/">Java look and
 * feel Graphics Repository</A>. This class is thread safe.
 *
 * @since 2.3
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/extension/widgets-swing/src/main/java/org/geotools/gui/swing/IconFactory.java $
 * @version $Id: IconFactory.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux
 */
public final class IconFactory {
    /**
     * The default factory instance.
     */
    public static final IconFactory DEFAULT = new IconFactory();

    /**
     * The class loader to uses.
     */
    private final ClassLoader loader;

    /**
     * The icons already loaded.
     */
    private final Map icons = new HashMap();

    /**
     * Do not allows instantiation of this class.
     */
    private IconFactory() {
        loader = getClass().getClassLoader();
    }

    /**
     * Returns the icon for the specified name, or {@code null} if none. If this method has
     * already been invoked for the specified path, then the previously created icon is returned.
     *
     * @param  path The icon path, relative to the application classpath.
     * @return The icon, or {@code null} if no image was found for the specified path.
     */
    public synchronized Icon getIcon(final String path) {
        Icon icon = (Icon) icons.get(path);
        if (icon == null) {
            URL url = loader.getResource(path);
            if (url != null) {
                icon = new ImageIcon(url);
                icons.put(path, icon);
            }
        }
        return icon;
    }

    /**
     * Returns an icon for the specified name and description, or {@code null} if none.
     *
     * @param  path The icon path, relative to the application classpath.
     * @param  description brief textual description of the image, or {@code null} if none.
     * @return The icon, or {@code null} if no image was found for the specified path.
     */
    public Icon getIcon(final String path, final String description) {
        Icon icon = getIcon(path);
        if (description != null) {
            if (icon instanceof ImageIcon) {
                icon = new ImageIcon(((ImageIcon) icon).getImage(), description);
            }
        }
        return icon;
    }

    /**
     * Returns a button with the specified image.
     *
     * @param  path The icon path, relative to the application classpath.
     * @param  description brief textual description of the image, or {@code null} if none.
     * @param  fallback A text to put in the button if the image were not found.
     */
    public JButton getButton(final String path, final String description, final String fallback) {
        final Icon icon = getIcon(path, description);
        final JButton button;
        if (icon != null) {
            button = new JButton(icon);
        } else {
            button = new JButton(fallback);
        }
        button.setToolTipText(description);
        return button;
    }
}
