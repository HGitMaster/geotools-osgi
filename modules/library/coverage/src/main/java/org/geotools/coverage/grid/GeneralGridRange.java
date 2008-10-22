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
package org.geotools.coverage.grid;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;

import org.opengis.geometry.Envelope;
import org.opengis.coverage.grid.GridRange;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.coverage.grid.GridCoordinates;
import org.opengis.referencing.datum.PixelInCell;


/**
 * Defines a range of grid coverage coordinates.
 *
 * @since 2.1
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/coverage/src/main/java/org/geotools/coverage/grid/GeneralGridRange.java $
 * @version $Id: GeneralGridRange.java 30776 2008-06-20 17:00:11Z desruisseaux $
 * @author Martin Desruisseaux (IRD)
 *
 * @see GridRange2D
 *
 * @deprecated Replaced by {@link GeneralGridEnvelope}. Be aware that in the later, high
 *             coordinate values are <strong>inclusive</strong> rather than exclusive.
 */
@Deprecated
public class GeneralGridRange extends GeneralGridEnvelope implements GridRange {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1452569710967224145L;

    /**
     * The upper right corner. Will be created only when first needed.
     */
    private transient GridCoordinates upper;

    /**
     * Copy constructor.
     */
    GeneralGridRange(final GridEnvelope envelope) {
        super(envelope);
    }

    /**
     * Constructs one-dimensional grid range.
     *
     * @param lower The minimal inclusive value.
     * @param upper The maximal exclusive value.
     */
    public GeneralGridRange(final int lower, final int upper) {
        super(new int[] {lower}, new int[] {upper}, false);
    }

    /**
     * Constructs a new grid range.
     *
     * @param lower The valid minimum inclusive grid coordinate.
     *              The array contains a minimum value for each
     *              dimension of the grid coverage. The lowest
     *              valid grid coordinate is zero.
     * @param upper The valid maximum exclusive grid coordinate.
     *              The array contains a maximum value for each
     *              dimension of the grid coverage.
     *
     * @see #getLowers
     * @see #getUppers
     */
    public GeneralGridRange(final int[] lower, final int[] upper) {
        super(lower, upper, false);
    }

    /**
     * Constructs two-dimensional range defined by a {@link Rectangle}.
     */
    public GeneralGridRange(final Rectangle rect) {
        this(rect, 2);
    }

    /**
     * Constructs multi-dimensional range defined by a {@link Rectangle}.
     * The two first dimensions are set to the
     * [{@linkplain Rectangle#x x} .. x+{@linkplain Rectangle#width width}] and
     * [{@linkplain Rectangle#y y} .. x+{@linkplain Rectangle#height height}]
     * ranges respectively. Extra dimensions (if any) are set to the [0..1] range.
     *
     * @param rect The rectangle.
     * @param dimension Number of dimensions for this grid range. Must be equals or greater than 2.
     *
     * @since 2.5
     */
    public GeneralGridRange(final Rectangle rect, final int dimension) {
        super(rect, dimension);
    }

    /**
     * Constructs two-dimensional range defined by a {@link Raster}.
     */
    public GeneralGridRange(final Raster raster) {
        this(raster, 2);
    }

    /**
     * Constructs multi-dimensional range defined by a {@link Raster}.
     * The two first dimensions are set to the
     * [{@linkplain Raster#getMinX x} .. x+{@linkplain Raster#getWidth width}] and
     * [{@linkplain Raster#getMinY y} .. x+{@linkplain Raster#getHeight height}]
     * ranges respectively. Extra dimensions (if any) are set to the [0..1] range.
     *
     * @param raster The raster.
     * @param dimension Number of dimensions for this grid range. Must be equals or greater than 2.
     *
     * @since 2.5
     */
    public GeneralGridRange(final Raster raster, final int dimension) {
        super(raster, dimension);
    }

    /**
     * Constructs two-dimensional range defined by a {@link RenderedImage}.
     */
    public GeneralGridRange(final RenderedImage image) {
        this(image, 2);
    }

    /**
     * Constructs multi-dimensional range defined by a {@link RenderedImage}.
     * The two first dimensions are set to the
     * [{@linkplain RenderedImage#getMinX x} .. x+{@linkplain RenderedImage#getWidth width}] and
     * [{@linkplain RenderedImage#getMinY y} .. x+{@linkplain RenderedImage#getHeight height}]
     * ranges respectively. Extra dimensions (if any) are set to the [0..1] range.
     *
     * @param image The image.
     * @param dimension Number of dimensions for this grid range. Must be equals or greater than 2.
     *
     * @since 2.5
     */
    public GeneralGridRange(final RenderedImage image, final int dimension) {
        super(image, dimension);
    }

    /**
     * @deprecated Replaced by {@code new GeneralGridRange(envelope, PixelInCell.CELL_CORNER)}.
     *
     * @since 2.2
     */
    @Deprecated
    public GeneralGridRange(final Envelope envelope) {
        this(envelope, PixelInCell.CELL_CORNER);
    }

    /**
     * Casts the specified envelope into a grid range. This is sometime useful after an
     * envelope has been transformed from "real world" coordinates to grid coordinates using the
     * {@linkplain org.opengis.coverage.grid.GridGeometry#getGridToCoordinateSystem grid to CRS}
     * transform. The floating point values are rounded toward the nearest integers.
     * <p>
     * <b>Note about rounding mode</b><br>
     * It would have been possible to round the {@linkplain Envelope#getMinimum minimal value}
     * toward {@linkplain Math#floor floor} and the {@linkplain Envelope#getMaximum maximal value}
     * toward {@linkplain Math#ceil ceil} in order to make sure that the grid range encompass fully
     * the envelope - like what Java2D does when converting {@link java.awt.geom.Rectangle2D} to
     * {@link Rectangle}). But this approach may increase by 1 or 2 units the image
     * {@linkplain RenderedImage#getWidth width} or {@linkplain RenderedImage#getHeight height}. For
     * example the range {@code [-0.25 ... 99.75]} (which is exactly 100 units wide) would be casted
     * to {@code [-1 ... 100]}, which is 101 units wide. This leads to unexpected results when using
     * grid range with image operations like "{@link javax.media.jai.operator.AffineDescriptor Affine}".
     * For avoiding such changes in size, it is necessary to use the same rounding mode for both
     * minimal and maximal values. The selected rounding mode is {@linkplain Math#round nearest
     * integer} in this implementation.
     * <p>
     * <b>Grid type</b><br>
     * According OpenGIS specification, {@linkplain org.opengis.coverage.grid.GridGeometry grid
     * geometry} maps pixel's center. But envelopes typically encompass all pixels. This means
     * that grid coordinates (0,0) has an envelope starting at (-0.5, -0.5). In order to revert
     * back such envelope to a grid range, it is necessary to add 0.5 to every coordinates
     * (including the maximum value since it is exclusive in a grid range). This offset is applied
     * only if {@code anchor} is {@link PixelInCell#CELL_CENTER}. Users who don't want such
     * offset should specify {@link PixelInCell#CELL_CORNER}.
     * <p>
     * The convention is specified as a {@link PixelInCell} code instead than the more detailed
     * {@link org.opengis.metadata.spatial.PixelOrientation} because the latter is restricted to
     * the two-dimensional case while the former can be used for any number of dimensions.
     *
     * @param envelope
     *          The envelope to use for initializing this grid range.
     * @param anchor
     *          Whatever envelope coordinates map to pixel center or pixel corner. Should be
     *          {@link PixelInCell#CELL_CENTER} if an offset of 0.5 should be added to every
     *          envelope coordinate values, or {@link PixelInCell#CELL_CORNER} if no offset
     *          should be applied.
     * @throws IllegalArgumentException
     *          If {@code anchor} is not valid.
     *
     * @since 2.5
     *
     * @see org.geotools.referencing.GeneralEnvelope#GeneralEnvelope(GridRange, PixelInCell,
     *      org.opengis.referencing.operation.MathTransform,
     *      org.opengis.referencing.crs.CoordinateReferenceSystem)
     */
    public GeneralGridRange(final Envelope envelope, final PixelInCell anchor)
            throws IllegalArgumentException
    {
        super(envelope, anchor, false);
    }

    /**
     * Returns the valid minimum inclusive grid coordinate along the specified dimension.
     *
     * @see #getLowers
     */
    public int getLower(final int dimension) {
        return super.getLow(dimension);
    }

    /**
     * Returns the valid maximum exclusive grid coordinate along the specified dimension.
     *
     * @see #getUppers
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
     * The sequence contains a minimum value for each dimension of the grid coverage.
     *
     * @since 2.4
     */
    public GridCoordinates getLower() {
        return super.getLow();
    }

    /**
     * Returns the valid maximum exclusive grid coordinate.
     * The sequence contains a maximum value for each dimension of the grid coverage.
     *
     * @since 2.4
     */
    public GridCoordinates getUpper() {
        if (upper == null) {
            upper = new GeneralGridCoordinates.Immutable(super.getHigh());
            ((GeneralGridCoordinates.Immutable) upper).translate(+1);
        }
        return upper;
    }

    /**
     * Returns a new grid range that encompass only some dimensions of this grid range.
     * This method copy this grid range's index into a new grid range, beginning at
     * dimension {@code lower} and extending to dimension {@code upper-1}.
     * Thus the dimension of the subgrid range is {@code upper-lower}.
     *
     * @param  lower The first dimension to copy, inclusive.
     * @param  upper The last  dimension to copy, exclusive.
     * @return The subgrid range.
     * @throws IndexOutOfBoundsException if an index is out of bounds.
     */
    public GeneralGridRange getSubGridRange(final int lower, final int upper) {
        return new GeneralGridRange(super.getSubGridEnvelope(lower, upper));
    }
}
