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
package org.geotools.gui.swing.crschooser;

// OpenGIS dependencies
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;


/**
 * An element in a {@link CodeList}. This element stores the {@linkplain #code code value}.
 * The description name will be fetched when first needed and returned by {@link #toString}.
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/crschooser/Code.java $
 * @version $Id: Code.java 30681 2008-06-13 10:25:54Z acuster $
 * @author Martin Desruisseaux (IRD)
 */
final class Code {
    /**
     * The sequential index.
     */
    final int index;

    /**
     * The authority code.
     */
    public final String code;

    /**
     * The CRS object description for the {@linkplain #code}.
     * Will be extracted only when first needed.
     */
    private String name;

    /**
     * The authority factory to use for fetching the name. Will be set to {@code null} after
     * {@linkplain #name} has been made available, in order to allow the garbage collector
     * to do its work if possible.
     */
    private AuthorityFactory factory;

    /**
     * Creates a code from the specified value.
     *
     * @param factory The authority factory.
     * @param code The authority code.
     */
    public Code(final AuthorityFactory factory, final String code, final int index) {
        this.factory = factory;
        this.code    = code;
        this.index   = index;
    }

    /**
     * Returns the name for this code.
     *
     * @todo Maybe we should use the widget Locale when invoking InternationalString.toString(...).
     */
    @Override
    public String toString() {
        if (name == null) try {
            name = code + " - " + factory.getDescriptionText(code).toString();
        } catch (FactoryException e) {
            name = code + " - " + e.getLocalizedMessage();
        }
        factory = null;
        return name;
    }
}
