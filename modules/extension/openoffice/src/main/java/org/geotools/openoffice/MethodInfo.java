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
package org.geotools.openoffice;


/**
 * Information about a method to be exported as <A HREF="http://www.openoffice.org">OpenOffice</A>
 * add-in.
 *
 * @since 2.2
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/extension/openoffice/src/main/java/org/geotools/openoffice/MethodInfo.java $
 * @version $Id: MethodInfo.java 30654 2008-06-12 20:19:03Z acuster $
 * @author Martin Desruisseaux (IRD)
 */
public final class MethodInfo {
    /** The category name. */
    final String category;

    /** The display name. */
    final String display;

    /** A description of the exported method. */
    final String description;

    /** Arguments names (even index) and descriptions (odd index). */
    final String[] arguments;

    /**
     * Constructs method informations.
     *
     * @param category    The category name.
     * @param display     The display name.
     * @param description A description of the exported method.
     * @param arguments   Arguments names (even index) and descriptions (odd index).
     */
    public MethodInfo(final String   category,
                      final String   display,
                      final String   description,
                      final String[] arguments)
    {
        this.category    = category;
        this.display     = display;
        this.description = description;
        this.arguments   = arguments;
    }
}
