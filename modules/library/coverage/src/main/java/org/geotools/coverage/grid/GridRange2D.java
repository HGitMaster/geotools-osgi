/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.coverage.grid;

import java.awt.Rectangle;
import org.opengis.coverage.grid.GridRange;


/**
 * Defines a range of two-dimensional grid coverage coordinates. This implementation extends
 * {@link Rectangle} for interoperability with Java2D. Note that at the opposite of
 * {@link GeneralGridRange}, this class is mutable.
 *
 * @since 2.5
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/coverage/src/main/java/org/geotools/coverage/grid/GridRange2D.java $
 * @version $Id: GridRange2D.java 30776 2008-06-20 17:00:11Z desruisseaux $
 * @author Martin Desruisseaux
 *
 * @see GeneralGridRange
 *
 * @deprecated Replaced by {@link GridEnvelope2D}. Be aware that in the later, high
 *             coordinate values are <strong>inclusive</strong> rather than exclusive.
 */
@Deprecated
public class GridRange2D extends GridEnvelope2D implements GridRange {
    /**
     * For cross-version interoperability.
     */
    private static final long serialVersionUID = 6899195945793291045L;

    /**
     * Creates an initially empty grid range.
     */
    public GridRange2D() {
    }

    /**
     * Creates a grid range initialized to the specified rectangle.
     */
    public GridRange2D(final Rectangle rectangle) {
        super(rectangle);
    }

    /**
     * Creates a grid range initialized to the specified rectangle.
     */
    public GridRange2D(final int x, final int y, final int width, final int height) {
        super(x, y, width, height);
    }

    /**
     * Returns the valid minimum inclusive grid coordinate along the specified dimension.
     */
    public int getLower(final int dimension) {
        return super.getLow(dimension);
    }

    /**
     * Returns the valid maximum exclusive grid coordinate along the specified dimension.
     */
    public int getUpper(final int dimension) {
        return super.getHigh(dimension) + 1;
    }

    /**
     * Returns the number of integer grid coordinates along the specified dimension.
     * This is equals to {@code getUpper(dimension)-getLower(dimension)}.
     */
    public int getLength(final int dimension) {
        return super.getSpan(dimension);
    }

    /**
     * Returns the valid minimum inclusive grid coordinate.
     */
    public GridCoordinates2D getLower() {
        return super.getLow();
    }

    /**
     * Returns the valid maximum exclusive grid coordinate.
     */
    public GridCoordinates2D getUpper() {
        return new GridCoordinates2D(x + width, y + height);
    }

    /**
     * Returns a clone of this grid range.
     */
    @Override
    public GridRange2D clone() {
        return (GridRange2D) super.clone();
    }
}
