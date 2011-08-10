/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 1999-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.axis;

import java.awt.RenderingHints;


/**
 * Rendering hints for tick's graduation.
 *
 * @since 2.0
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing/src/main/java/org/geotools/axis/RenderingHintKey.java $
 * @version $Id: RenderingHintKey.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (PMO, IRD)
 */
final class RenderingHintKey extends RenderingHints.Key {
    /**
     * The required base class.
     */
    private final Class<?> type;

    /**
     * Construct a rendering hint key.
     */
    protected RenderingHintKey(final Class<?> type, final int key) {
        super(key);
        this.type = type;
    }

    /**
     * Returns {@code true} if the specified object is a valid value for this key.
     */
    public boolean isCompatibleValue(final Object value) {
        return value!=null && type.isAssignableFrom(value.getClass());
    }
}
