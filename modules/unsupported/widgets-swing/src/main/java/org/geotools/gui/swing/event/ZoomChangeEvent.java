/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2001-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing.event;

// Dependencies
import java.util.EventObject;
import java.awt.geom.AffineTransform;


/**
 * An event which indicates that a zoom occurred in a component.
 * This event is usually fired by {@link org.geotools.gui.swing.ZoomPane}.
 *
 * @since 2.0
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing/src/main/java/org/geotools/gui/swing/event/ZoomChangeEvent.java $
 * @version $Id: ZoomChangeEvent.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux (IRD)
 */
public class ZoomChangeEvent extends EventObject {
    /**
     * An affine transform indicating the zoom change. If {@code oldZoom} and {@code newZoom}
     * are the affine transforms before and after the change respectively, then the following
     * relation must hold (within the limits of rounding error):
     *
     * <code>newZoom = oldZoom.{@link AffineTransform#concatenate concatenate}(change)</code>
     */
    private final AffineTransform change;

    /**
     * Constructs a new event. If {@code oldZoom} and {@code newZoom} are the affine transforms
     * before and after the change respectively, then the following relation must hold (within
     * the limits of rounding error):
     *
     * <code>newZoom = oldZoom.{@link AffineTransform#concatenate concatenate}(change)</code>
     *
     * @param source The event source (usually a {@link org.geotools.gui.swing.ZoomPane}).
     * @param change An affine transform indicating the zoom change.
     */
    public ZoomChangeEvent(final Object source, final AffineTransform change) {
        super(source);
        this.change = change;
    }

    /**
     * Returns the affine transform indicating the zoom change.
     * <strong>Note:</strong> for performance reasons, this method does not clone
     * the returned transform. Do not change!
     */
    public AffineTransform getChange() {
        return change;
    }
}
