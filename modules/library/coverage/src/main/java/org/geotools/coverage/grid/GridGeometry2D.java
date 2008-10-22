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
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Locale;

import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.grid.GridRange;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;

import org.geotools.factory.Hints;
import org.geotools.geometry.Envelope2D;
import org.geotools.metadata.iso.spatial.PixelTranslation;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.geotools.referencing.operation.transform.DimensionFilter;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.Classes;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Describes the valid range of grid coordinates and the math transform, in the special case
 * where only 2 dimensions are in use. By "in use", we means dimension with more than 1 pixel.
 * For example a grid size of 512&times;512&times;1 pixels can be represented by this
 * {@code GridGeometry2D} class (some peoples said 2.5D) because a two-dimensional grid
 * coordinate is enough for referencing a pixel without ambiguity. But a grid size of
 * 512&times;512&times;2 pixels can not be represented by this {@code GridGeometry2D},
 * because a three-dimensional coordinate is mandatory for referencing a pixel without
 * ambiguity.
 *
 * @since 2.1
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/coverage/src/main/java/org/geotools/coverage/grid/GridGeometry2D.java $
 * @version $Id: GridGeometry2D.java 31486 2008-09-09 20:46:25Z desruisseaux $
 * @author Martin Desruisseaux (IRD)
 *
 * @see ImageGeometry
 * @see GeneralGridGeometry
 */
public class GridGeometry2D extends GeneralGridGeometry {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3989363771504614419L;

    /**
     * Helpers methods for 2D CRS creation. Will be constructed only when first needed.
     */
    private static ReferencingFactoryContainer FACTORIES;

    /**
     * The two-dimensional part of the coordinate reference system.
     *
     * @see #getCoordinateReferenceSystem2D
     */
    private final CoordinateReferenceSystem crs2D;

    /**
     * Index of column ({@link #gridDimensionX}) and row ({@link #gridDimensionY}) ordinates
     * in a grid point. They are the index of the first two dimensions with a {@linkplain
     * GridEnvelope#getSpan span} greater than 1 in the {@linkplain #getGridRange grid range}.
     * Their values are usually 0 and 1 respectively.
     * <p>
     * It is garanteed that {@link #gridDimensionX} &lt; {@link #gridDimensionY}.
     */
    public final int gridDimensionX, gridDimensionY;

    /**
     * The ({@link #gridDimensionX}, {@link #gridDimensionY}) dimensions in the envelope space.
     * They are index of (<var>x</var>, <var>y</var>) ordinates in a direct position after the
     * {@linkplain #getGridToCoordinateSystem grid to CRS} transform.
     * <p>
     * There is no garantee that {@link #gridDimensionX} maps to {@link #axisDimensionX} and
     * {@link #gridDimensionY} maps to {@link #axisDimensionY}, since axis may be interchanged.
     * <p>
     * It is garanteed that {@link #axisDimensionX} &lt; {@link #axisDimensionY}.
     */
    public final int axisDimensionX, axisDimensionY;

    /**
     * A math transform mapping only two dimensions of {@link #gridToCRS gridToCRS}.
     * Is {@code null} if and only if {@link #gridToCRS} is null.
     */
    private final MathTransform2D gridToCRS2D;

    /**
     * The inverse of {@code gridToCRS2D}.
     * Is {@code null} if and only if {@link #gridToCRS2D} is null.
     */
    private final MathTransform2D gridFromCRS2D;

    /**
     * {@link #gridToCRS2D} cached in the {@link PixelOrientation#UPPER_LEFT} case.
     * This field is serialized because it may be user-provided, in which case it is
     * likely to be more accurate than what we would compute. If {@code null}, will
     * be computed when first needed.
     */
    private MathTransform2D cornerToCRS2D;

    /**
     * Tests the validity of this grid geometry.
     */
    private boolean isValid() {
        if (gridToCRS != null) {
            final int sourceDim = gridToCRS.getSourceDimensions();
            final int targetDim = gridToCRS.getTargetDimensions();
            assert gridToCRS.equals(gridToCRS2D) == (sourceDim == 2 && targetDim == 2);
            assert !gridToCRS2D.equals(cornerToCRS2D);
            assert gridRange == null || sourceDim == gridRange.getDimension() : gridRange;
            assert envelope  == null || targetDim ==  envelope.getDimension() : envelope;
            assert gridDimensionY < sourceDim : gridDimensionY;
            assert axisDimensionY < targetDim : axisDimensionY;
        }
        assert gridDimensionX < gridDimensionY : gridDimensionX;
        assert axisDimensionX < axisDimensionY : axisDimensionX;
        return crs2D == null || crs2D.getCoordinateSystem().getDimension() == 2;
    }

    /**
     * Constructs a new grid geometry identical to the specified one except for the CRS.
     * Note that this constructor just defines the CRS; it does <strong>not</strong> reproject
     * the envelope. For this reason, this constructor should not be public. It is for internal
     * use by {@link GridCoverageFactory} only.
     */
    GridGeometry2D(final GridGeometry2D gm, final CoordinateReferenceSystem crs) {
        super(gm, crs);
        gridDimensionX = gm.gridDimensionX;
        gridDimensionY = gm.gridDimensionY;
        axisDimensionX = gm.axisDimensionX;
        axisDimensionY = gm.axisDimensionY;
        gridFromCRS2D  = gm.gridFromCRS2D;
        gridToCRS2D    = gm.gridToCRS2D;
        cornerToCRS2D  = gm.cornerToCRS2D;
        crs2D          = createCRS2D();
        assert isValid() : this;
    }

    /**
     * Creates a new grid geometry with the same values than the given grid geometry. This
     * is a copy constructor useful when the instance must be a {@code GridGeometry2D}.
     *
     * @param other The other grid geometry to copy.
     *
     * @see #wrap
     *
     * @since 2.5
     */
    public GridGeometry2D(final GridGeometry other) {
        super(other);
        if (other instanceof GridGeometry2D) {
            final GridGeometry2D gg = (GridGeometry2D) other;
            gridToCRS2D    = gg.gridToCRS2D;
            gridFromCRS2D  = gg.gridFromCRS2D;
            gridDimensionX = gg.gridDimensionX;
            gridDimensionY = gg.gridDimensionY;
            axisDimensionX = gg.axisDimensionX;
            axisDimensionY = gg.axisDimensionY;
            crs2D          = gg.crs2D;
            cornerToCRS2D  = gg.cornerToCRS2D;
        } else {
            final int[] dimensions;
            dimensions     = new int[4];
            gridToCRS2D    = getMathTransform2D(gridToCRS, gridRange, dimensions, null);
            gridFromCRS2D  = inverse(gridToCRS2D);
            gridDimensionX = dimensions[0];
            gridDimensionY = dimensions[1];
            axisDimensionX = dimensions[2];
            axisDimensionY = dimensions[3];
            crs2D          = createCRS2D();
        }
        assert isValid() : this;
    }

    /**
     * Constructs a new grid geometry from a grid range and a math transform. The arguments are
     * passed unchanged to the {@linkplain GeneralGridGeometry#GeneralGridGeometry(GridEnvelope,
     * MathTransform, CoordinateReferenceSystem) super-class constructor}. However, they must
     * obey to the following additional constraints:
     * <p>
     * <ul>
     *   <li>Only two dimensions in the grid range can have a
     *       {@linkplain GridEnvelope#getSpan span} larger than 1.</li>
     * </ul>
     *
     * @param  gridRange The valid coordinate range of a grid coverage, or {@code null} if none.
     *         The lowest valid grid coordinate is zero for {@link java.awt.image.BufferedImage},
     *         but may be non-zero for arbitrary {@link java.awt.image.RenderedImage}. A grid with
     *         512 cells can have a minimum coordinate of 0 and maximum of 512, with 511 as the
     *         highest valid index.
     * @param  gridToCRS The math transform which allows for the transformations
     *         from grid coordinates (pixel's <em>center</em>) to real world earth coordinates.
     * @param  crs The coordinate reference system for the "real world" coordinates, or {@code null}
     *         if unknown. This CRS is given to the {@linkplain #getEnvelope envelope}.
     *
     * @throws MismatchedDimensionException if the math transform and the CRS don't have
     *         consistent dimensions.
     * @throws IllegalArgumentException if {@code gridRange} has more than 2 dimensions with
     *         a {@linkplain GridEnvelope#getSpan span} larger than 1, or if the math transform
     *         can't transform coordinates in the domain of the specified grid range.
     *
     * @since 2.2
     */
    public GridGeometry2D(final GridEnvelope        gridRange,
                          final MathTransform       gridToCRS,
                          final CoordinateReferenceSystem crs)
            throws IllegalArgumentException, MismatchedDimensionException
    {
        this(gridRange, PixelInCell.CELL_CENTER, gridToCRS, crs, null);
    }

    /**
     * Constructs a new grid geometry from a math transform. This constructor is similar to
     * <code>{@linkplain #GridGeometry2D(GridEnvelope, MathTransform, CoordinateReferenceSystem)
     * GridGeometry2D}(gridRange, gridToCRS, crs)</code> with the addition of an explicit anchor
     * and an optional set of hints giving more control on the {@link MathTransform2D} to be
     * inferred from the <var>n</var>-dimensional transform.
     * <p>
     * The {@code anchor} argument tells whatever the {@code gridToCRS} transform maps {@linkplain
     * PixelInCell#CELL_CENTER cell center} (OGC convention) or {@linkplain PixelInCell#CELL_CORNER
     * cell corner} (Java2D/JAI convention). At the opposite of the constructor expecting a {@link
     * PixelOrientation} argument, the translation (if any) applies to every dimensions, not just
     * the ones mapping the 2D part.
     *
     * @param gridRange The valid coordinate range of a grid coverage, or {@code null} if none.
     * @param anchor    Whatever the {@code gridToCRS} transform maps
     *                  {@linkplain PixelInCell#CELL_CENTER cell center} (OGC convention) or
     *                  {@linkplain PixelInCell#CELL_CORNER cell corner} (Java2D/JAI convention).
     * @param gridToCRS The math transform which allows for the transformations from grid
     *                  coordinates to real world earth coordinates.
     * @param crs       The coordinate reference system for the "real world" coordinates, or
     *                  {@code null} if unknown. This CRS is given to the
     *                  {@linkplain #getEnvelope envelope}.
     * @param  hints    An optional set of hints controlling the {@link DimensionFilter} to be
     *                  used for deriving the {@link MathTransform2D} instance from the given
     *                  {@code gridToCRS} transform.
     *
     * @throws MismatchedDimensionException if the math transform and the CRS don't have
     *         consistent dimensions.
     * @throws IllegalArgumentException if the math transform can't transform coordinates
     *         in the domain of the specified grid range.
     *
     * @since 2.5
     */
    public GridGeometry2D(final GridEnvelope  gridRange,
                          final PixelInCell   anchor,
                          final MathTransform gridToCRS,
                          final CoordinateReferenceSystem crs,
                          final Hints hints)
            throws MismatchedDimensionException, IllegalArgumentException
    {
        super(gridRange, anchor, gridToCRS, crs);
        final int[] dimensions;
        dimensions     = new int[4];
        gridToCRS2D    = getMathTransform2D(super.gridToCRS, gridRange, dimensions, hints);
        gridFromCRS2D  = inverse(gridToCRS2D);
        gridDimensionX = dimensions[0];
        gridDimensionY = dimensions[1];
        axisDimensionX = dimensions[2];
        axisDimensionY = dimensions[3];
        crs2D          = createCRS2D();
        if (PixelInCell.CELL_CORNER.equals(anchor)) {
            cornerToCRS2D = getMathTransform2D(gridToCRS, gridRange, dimensions, hints);
        }
        assert isValid() : this;
    }

    /**
     * Constructs a new grid geometry from a math transform. This constructor is similar to
     * <code>{@linkplain #GridGeometry2D(GridEnvelope, MathTransform, CoordinateReferenceSystem)
     * GridGeometry2D}(gridRange, gridToCRS, crs)</code> with the addition of an explicit anchor
     * and an optional set of hints giving more control on the {@link MathTransform2D} to be
     * inferred from the <var>n</var>-dimensional transform.
     * <p>
     * The {@code anchor} argument tells whatever the {@code gridToCRS} transform maps pixel
     * center or some corner. Use {@link PixelOrientation#CENTER CENTER} for OGC conventions or
     * {@link PixelOrientation#UPPER_LEFT UPPER_LEFT} for Java2D/JAI conventions. A translation
     * (if needed) is applied only on the {@link #gridDimensionX} and {@link #gridDimensionY}
     * parts of the transform - all other dimensions are assumed mapping pixel center.
     *
     * @param  gridRange   The valid coordinate range of a grid coverage, or {@code null} if none.
     * @param  anchor      Whatever the two-dimensional part of the {@code gridToCRS} transform
     *                     maps pixel center or some corner.
     * @param  gridToCRS   The math transform from grid coordinates to real world earth coordinates.
     * @param  crs         The coordinate reference system for the "real world" coordinates, or
     *                     {@code null} if unknown.
     * @param  hints       An optional set of hints controlling the {@link DimensionFilter} to be
     *                     used for deriving the {@link MathTransform2D} instance from the given
     *                     {@code gridToCRS} transform.
     *
     * @throws MismatchedDimensionException if the math transform and the CRS don't have
     *         consistent dimensions.
     * @throws IllegalArgumentException if {@code gridRange} has more than 2 dimensions with
     *         a {@linkplain GridEnvelope#getSpan span} larger than 1, or if the math transform
     *         can't transform coordinates in the domain of the specified grid range.
     *
     * @since 2.5
     */
    public GridGeometry2D(final GridEnvelope        gridRange,
                          final PixelOrientation    anchor,
                          final MathTransform       gridToCRS,
                          final CoordinateReferenceSystem crs,
                          final Hints hints)
            throws IllegalArgumentException, MismatchedDimensionException
    {
        this(gridRange, anchor, gridToCRS, new int[4], crs, hints);
    }

    /**
     * Workaround for RFE #4093999 ("Relax constraint on placement of this()/super()
     * call in constructors"). We could write this code in a less convolved way if only
     * this requested was honored...
     */
    private GridGeometry2D(final GridEnvelope     gridRange,
                           final PixelOrientation anchor,
                           final MathTransform    gridToCRS,
                           final int[]            dimensions,  // Allocated by caller.
                           final CoordinateReferenceSystem crs,
                           final Hints hints)
    {
        this(gridRange, anchor, (gridToCRS != null) && (gridToCRS.getSourceDimensions() == 2) &&
             (gridToCRS.getTargetDimensions() == 2) && PixelOrientation.UPPER_LEFT.equals(anchor) ?
             PixelInCell.CELL_CORNER : PixelInCell.CELL_CENTER, gridToCRS,
             getMathTransform2D(gridToCRS, gridRange, dimensions, hints), dimensions, crs);
    }

    /**
     * Workaround for RFE #4093999 ("Relax constraint on placement of this()/super()
     * call in constructors").
     */
    private GridGeometry2D(final GridEnvelope     gridRange,
                           final PixelOrientation anchor,
                           final PixelInCell      anchorND,     // Computed by caller
                           final MathTransform    gridToCRS,
                           final MathTransform2D  gridToCRS2D,  // Computed by caller
                           final int[]            dimensions,   // Computed by caller
                           final CoordinateReferenceSystem crs)
    {
        super(gridRange, anchorND, PixelTranslation.translate(gridToCRS, anchor,
              PixelTranslation.getPixelOrientation(anchorND), dimensions[0], dimensions[1]), crs);
        gridDimensionX = dimensions[0];
        gridDimensionY = dimensions[1];
        axisDimensionX = dimensions[2];
        axisDimensionY = dimensions[3];
        if (gridToCRS == gridToCRS2D) {
            // Recycles existing instance if we can (common case)
            this.gridToCRS2D = (MathTransform2D) super.gridToCRS;
        } else {
            final int xdim = (gridDimensionX < gridDimensionY) ? 0 : 1;
            this.gridToCRS2D = (MathTransform2D) PixelTranslation.translate(
                    gridToCRS2D, anchor, PixelOrientation.CENTER, xdim, xdim ^ 1);
        }
        gridFromCRS2D = inverse(this.gridToCRS2D);
        crs2D         = createCRS2D();
        assert isValid() : this;
    }

    /**
     * Constructs a new grid geometry from an envelope and a {@linkplain MathTransform math
     * transform}. According OGC specification, the math transform should map {@linkplain
     * PixelInCell#CELL_CENTER pixel center}. But in Java2D/JAI conventions, the transform
     * is rather expected to maps {@linkplain PixelInCell#CELL_CORNER pixel corner}. The
     * convention to follow can be specified by the {@code anchor} argument.
     *
     * @param anchor    {@link PixelInCell#CELL_CENTER CELL_CENTER} for OGC conventions or
     *                  {@link PixelInCell#CELL_CORNER CELL_CORNER} for Java2D/JAI conventions.
     * @param gridToCRS The math transform which allows for the transformations from grid
     *                  coordinates to real world earth coordinates. May be {@code null},
     *                  but this is not recommended.
     * @param envelope  The envelope (including CRS) of a grid coverage, or {@code null} if none.
     * @param hints     An optional set of hints controlling the {@link DimensionFilter} to be
     *                  used for deriving the {@link MathTransform2D} instance from the given
     *                  {@code gridToCRS} transform.
     *
     * @throws MismatchedDimensionException if the math transform and the envelope doesn't have
     *         consistent dimensions.
     * @throws IllegalArgumentException if the math transform can't transform coordinates
     *         in the domain of the grid range.
     *
     * @since 2.5
     */
    public GridGeometry2D(final PixelInCell   anchor,
                          final MathTransform gridToCRS,
                          final Envelope      envelope,
                          final Hints         hints)
            throws MismatchedDimensionException, IllegalArgumentException
    {
        super(anchor, gridToCRS, envelope);
        final int[] dimensions;
        dimensions     = new int[4];
        gridToCRS2D    = getMathTransform2D(this.gridToCRS, gridRange, dimensions, hints);
        gridFromCRS2D  = inverse(gridToCRS2D);
        gridDimensionX = dimensions[0];
        gridDimensionY = dimensions[1];
        axisDimensionX = dimensions[2];
        axisDimensionY = dimensions[3];
        crs2D          = createCRS2D();
        if (PixelInCell.CELL_CORNER.equals(anchor)) {
            cornerToCRS2D = getMathTransform2D(gridToCRS, gridRange, dimensions, hints);
        }
        assert isValid() : this;
    }

    /**
     * Constructs a new grid geometry from an envelope. This constructors applies the same heuristic
     * rules than the {@linkplain GeneralGridGeometry#GeneralGridGeometry(GridEnvelope,Envelope)
     * super-class constructor}. However, they must obey to the same additional constraints than
     * the {@linkplain #GridGeometry2D(GridEnvelope, MathTransform, CoordinateReferenceSystem) main
     * constructor}.
     *
     * @param gridRange The valid coordinate range of a grid coverage.
     * @param userRange The corresponding coordinate range in user coordinate.
     *
     * @throws IllegalArgumentException if {@code gridRange} has more than 2 dimensions with
     *         a {@linkplain GridEnvelope#getLength length} larger than 1.
     * @throws MismatchedDimensionException if the grid range and the CRS doesn't have
     *         consistent dimensions.
     *
     * @since 2.2
     */
    public GridGeometry2D(final GridEnvelope gridRange, final Envelope userRange)
            throws IllegalArgumentException, MismatchedDimensionException
    {
        this(gridRange, userRange, null, false, true);
    }

    /**
     * Constructs a new grid geometry from an envelope. The argument are passed unchanged to the
     * {@linkplain GeneralGridGeometry#GeneralGridGeometry(GridEnvelope,Envelope,boolean[],boolean)
     * super-class constructor}. However, they must obey to the same additional constraints than
     * the {@linkplain #GridGeometry2D(GridEnvelope, MathTransform, CoordinateReferenceSystem) main
     * constructor}.
     *
     * @param gridRange The valid coordinate range of a grid coverage.
     * @param userRange The corresponding coordinate range in user coordinate.
     * @param reverse   Tells for each axis in <cite>user</cite> space whatever or not its
     *                  direction should be reversed. A {@code null} value reverse no axis.
     * @param swapXY    If {@code true}, then the two first axis will be interchanged.
     *
     * @throws IllegalArgumentException if {@code gridRange} has more than 2 dimensions with
     *         a {@linkplain GridEnvelope#getSpan span} larger than 1.
     * @throws MismatchedDimensionException if the grid range and the CRS doesn't have
     *         consistent dimensions.
     *
     * @since 2.2
     *
     * @deprecated Use {@link GridToEnvelopeMapper} instead, which provides more control.
     */
    @Deprecated
    public GridGeometry2D(final GridEnvelope gridRange,
                          final Envelope  userRange,
                          final boolean[] reverse,
                          final boolean   swapXY)
            throws IllegalArgumentException, MismatchedDimensionException
    {
        this(gridRange, userRange, reverse, swapXY, false);
    }

    /**
     * Implementation of heuristic constructors.
     */
    private GridGeometry2D(final GridEnvelope gridRange,
                           final Envelope  userRange,
                           final boolean[] reverse,
                           final boolean   swapXY,
                           final boolean   automatic)
            throws IllegalArgumentException, MismatchedDimensionException
    {
        super(gridRange, userRange, reverse, swapXY, automatic);
        final int[] dimensions;
        dimensions     = new int[4];
        gridToCRS2D    = getMathTransform2D(gridToCRS, gridRange, dimensions, null);
        gridFromCRS2D  = inverse(gridToCRS2D);
        gridDimensionX = dimensions[0];
        gridDimensionY = dimensions[1];
        axisDimensionX = dimensions[2];
        axisDimensionY = dimensions[3];
        crs2D          = createCRS2D();
        assert isValid() : this;
    }

    /**
     * Constructs a new two-dimensional grid geometry. A math transform will be computed
     * automatically with an inverted <var>y</var> axis (i.e. {@code gridRange} and
     * {@code userRange} are assumed to have <var>y</var> axis in opposite direction).
     *
     * @param gridRange The valid coordinate range of a grid coverage.
     *                  Increasing <var>x</var> values goes right and
     *                  increasing <var>y</var> values goes <strong>down</strong>.
     * @param userRange The corresponding coordinate range in user coordinate.
     *                  Increasing <var>x</var> values goes right and
     *                  increasing <var>y</var> values goes <strong>up</strong>.
     *                  This rectangle must contains entirely all pixels, i.e.
     *                  the rectangle's upper left corner must coincide with
     *                  the upper left corner of the first pixel and the rectangle's
     *                  lower right corner must coincide with the lower right corner
     *                  of the last pixel.
     */
    public GridGeometry2D(final Rectangle gridRange, final Rectangle2D userRange) {
        this(new GeneralGridRange(gridRange), getMathTransform(gridRange, userRange),
             (CoordinateReferenceSystem) null);
    }

    /**
     * Returns the given grid geometry as a {@code GridGeometry2D}. If the given
     * object is already an instance of {@code GridGeometry2D}, then it is returned
     * unchanged. Otherwise a new {@code GridGeometry2D} instance is created using the
     * {@linkplain #GridGeometry2D(GridGeometry) copy constructor}.
     *
     * @param  other The grid geometry to wrap.
     * @return The wrapped geometry, or {@code null} if {@code other} was null.
     *
     * @since 2.5
     */
    public static GridGeometry2D wrap(final GridGeometry other) {
        if (other == null || other instanceof GridGeometry2D) {
            return (GridGeometry2D) other;
        }
        return new GridGeometry2D(other);
    }

    /**
     * Workaround for RFE #4093999 ("Relax constraint on placement of this()/super()
     * call in constructors").
     */
    private static MathTransform getMathTransform(final Rectangle   gridRange,
                                                  final Rectangle2D userRange)
    {
        final double scaleX = userRange.getWidth()  / gridRange.getWidth();
        final double scaleY = userRange.getHeight() / gridRange.getHeight();
        final double transX = userRange.getMinX()   - gridRange.x*scaleX;
        final double transY = userRange.getMaxY()   + gridRange.y*scaleY;
        final AffineTransform tr = new AffineTransform(scaleX, 0, 0, -scaleY, transX, transY);
        tr.translate(0.5, 0.5); // Maps to pixel center
        return ProjectiveTransform.create(tr);
    }

    /**
     * Returns the math transform for two dimensions of the specified transform. This methods
     * search for the grid dimensions in the given grid range having a length greater than 1.
     * The corresponding CRS dimensions are inferred from the transform itself.
     *
     * @param  gridRange The grid range, or {@code null} if unknown.
     * @param  transform The transform, or {@code null} if none.
     * @param  axis An array of length 4 initialized to 0. This is the array where to store
     *         {@link #gridDimensionX}, {@link #gridDimensionY}, {@link #axisDimensionX} and
     *         {@link #axisDimensionY} values. This argument is actually a workaround for a
     *         Java language limitation (no multiple return values). If we could, we would
     *         have returned directly the arrays computed in the body of this method.
     * @param  hints An optional set of hints for {@link DimensionFilter} creation.
     * @return The {@link MathTransform2D} part of {@code transform}, or {@code null}
     *         if and only if {@code gridToCRS} was null..
     * @throws IllegalArgumentException if the 2D part is not separable.
     */
    private static MathTransform2D getMathTransform2D(final MathTransform transform,
                                                      final GridEnvelope gridRange,
                                                      final int[] axis, final Hints hints)
            throws IllegalArgumentException
    {
        if (transform==null || transform instanceof MathTransform2D) {
            axis[1] = axis[3] = 1; // Identity: (0,1) --> (0,1)
            return (MathTransform2D) transform;
        }
        /*
         * Finds the axis for the two dimensional parts. We infer them from the grid range.
         * If no grid range were specified, then we assume that they are the 2 first dimensions.
         */
        final DimensionFilter filter = DimensionFilter.getInstance(hints);
        if (gridRange != null) {
            final int dimension = gridRange.getDimension();
            for (int i=0; i<dimension; i++) {
                if (gridRange.getSpan(i) > 1) {
                    filter.addSourceDimension(i);
                }
            }
        } else {
            filter.addSourceDimensionRange(0, 2);
        }
        Exception cause = null;
        int[] dimensions = filter.getSourceDimensions();
        /*
         * Select a math transform that operate only on the two dimensions choosen above.
         * If such a math transform doesn't have exactly 2 output dimensions, then select
         * the same output dimensions than the input ones.
         */
        MathTransform candidate;
        if (dimensions.length == 2) {
            axis[0] = dimensions[0]; // gridDimensionX
            axis[1] = dimensions[1]; // gridDimensionY
            try {
                candidate = filter.separate(transform);
                if (candidate.getTargetDimensions() != 2) {
                    filter.clear();
                    filter.addSourceDimensions(dimensions);
                    filter.addTargetDimensions(dimensions);
                    candidate = filter.separate(transform);
                }
                dimensions = filter.getTargetDimensions();
                axis[2] = dimensions[0]; // axisDimensionX
                axis[3] = dimensions[1]; // axisDimensionY
                try {
                    return (MathTransform2D) candidate;
                } catch (ClassCastException exception) {
                    cause = exception;
                }
            } catch (FactoryException exception) {
                cause = exception;
            }
        }
        throw new IllegalArgumentException(Errors.format(ErrorKeys.NO_TRANSFORM2D_AVAILABLE), cause);
    }

    /**
     * Inverses the specified math transform. This method is invoked by constructors only. It wraps
     * {@link NoninvertibleTransformException} into {@link IllegalArgumentException}, since failures
     * to inverse a transform are caused by an illegal user-supplied transform.
     *
     * @throws IllegalArgumentException if the transform is non-invertible.
     */
    private static MathTransform2D inverse(final MathTransform2D gridToCRS2D)
            throws IllegalArgumentException
    {
        if (gridToCRS2D == null) {
            return null;
        } else try {
            return gridToCRS2D.inverse();
        } catch (NoninvertibleTransformException exception) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_TRANSFORM_$1,
                    Classes.getClass(gridToCRS2D)), exception);
        }
    }

    /**
     * Constructs the two-dimensional CRS. This is usually identical to the user-supplied CRS.
     * However, the user is allowed to specify a wider CRS (for example a 3D one which includes
     * a time axis), in which case we infer which axis apply to the 2D image, and constructs a
     * 2D CRS with only those axis.
     *
     * @return The coordinate reference system, or {@code null} if none.
     * @throws InvalidGridGeometryException if the CRS can't be reduced.
     */
    private CoordinateReferenceSystem createCRS2D() throws InvalidGridGeometryException {
        if (!super.isDefined(CRS)) {
            return null;
        }
        CoordinateReferenceSystem crs = super.getCoordinateReferenceSystem();
        try {
            crs = reduce(crs);
        } catch (FactoryException exception) {
            throw new InvalidGridGeometryException(Errors.format(
                    ErrorKeys.ILLEGAL_ARGUMENT_$2, "crs", crs.getName()), exception);
        }
        return crs;
    }

    /**
     * Reduces the specified envelope to a two-dimensional one. If the given envelope has
     * more than two dimensions, then a new one is created using only the coordinates at
     * ({@link #axisDimensionX}, {@link #axisDimensionY}) index.
     * <p>
     * The {@link Envelope#getCoordinateReferenceSystem coordinate reference system} of the
     * source envelope is ignored. The coordinate reference system of the target envelope
     * will be {@link #getCoordinateReferenceSystem2D} or {@code null}.
     *
     * @param  envelope The envelope to reduce, or {@code null}. This envelope will not be modified.
     * @return An envelope with exactly 2 dimensions, or {@code null} if {@code envelope} was null.
     *         The returned envelope is always a new instance, so it can be modified safely.
     *
     * @since 2.5
     */
    public Envelope2D reduce(final Envelope envelope) {
        if (envelope == null) {
            return null;
        }
        return new Envelope2D(crs2D,
                envelope.getMinimum(axisDimensionX),
                envelope.getMinimum(axisDimensionY),
                envelope.getSpan   (axisDimensionX),
                envelope.getSpan   (axisDimensionY));
    }

    /**
     * Reduces the specified CRS to a two-dimensional one. If the given CRS has more than two
     * dimensions, then a new one is created using only the axis at ({@link #axisDimensionX},
     * {@link #axisDimensionY}) index.
     *
     * @param  crs The coordinate reference system to reduce, or {@code null}.
     * @return A coordinate reference system with no more than 2 dimensions,
     *         or {@code null} if {@code crs} was null.
     * @throws FactoryException if the given CRS can't be reduced to two dimensions.
     *
     * @since 2.5
     */
    public CoordinateReferenceSystem reduce(final CoordinateReferenceSystem crs)
            throws FactoryException
    {
        // Reminder: is is garanteed that axisDimensionX < axisDimensionY
        if (crs == null || crs.getCoordinateSystem().getDimension() <= 2) {
            return crs;
        }
        if (FACTORIES == null) {
            FACTORIES = ReferencingFactoryContainer.instance(null);
            // No need to synchronize: this is not a big deal if
            // two ReferencingFactoryContainer instances are created.
        }
        final CoordinateReferenceSystem crs2D;
        crs2D = FACTORIES.separate(crs, new int[] {axisDimensionX, axisDimensionY});
        assert crs2D.getCoordinateSystem().getDimension() == 2 : crs2D;
        return crs2D;
    }

    /**
     * Returns the two-dimensional part of this grid geometry CRS. If the
     * {@linkplain #getCoordinateReferenceSystem complete CRS} is two-dimensional, then this
     * method returns the same CRS. Otherwise it returns a CRS for ({@link #axisDimensionX},
     * {@link #axisDimensionY}) axis. Note that those axis are garanteed to appears in the
     * same order than in the complete CRS.
     *
     * @return The coordinate reference system (never {@code null}).
     * @throws InvalidGridGeometryException if this grid geometry has no CRS (i.e.
     *         <code>{@linkplain #isDefined isDefined}({@linkplain #CRS CRS})</code>
     *         returned {@code false}).
     *
     * @see #getCoordinateReferenceSystem
     *
     * @since 2.2
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem2D()
            throws InvalidGridGeometryException
    {
        if (crs2D != null) {
            assert isDefined(CRS);
            return crs2D;
        }
        assert !isDefined(CRS);
        throw new InvalidGridGeometryException(ErrorKeys.UNSPECIFIED_CRS);
    }

    /**
     * Returns the two-dimensional bounding box for the coverage domain in coordinate reference
     * system coordinates. If the coverage envelope has more than two dimensions, only the
     * dimensions used in the underlying rendered image are returned.
     *
     * @return The bounding box in "real world" coordinates (never {@code null}).
     * @throws InvalidGridGeometryException if this grid geometry has no envelope (i.e.
     *         <code>{@linkplain #isDefined isDefined}({@linkplain #ENVELOPE ENVELOPE})</code>
     *         returned {@code false}).
     *
     * @see #getEnvelope
     */
    public Envelope2D getEnvelope2D() throws InvalidGridGeometryException {
        if (envelope!=null && !envelope.isNull()) {
            assert isDefined(ENVELOPE);
            return new Envelope2D(crs2D,
                    envelope.getMinimum(axisDimensionX),
                    envelope.getMinimum(axisDimensionY),
                    envelope.getSpan   (axisDimensionX),
                    envelope.getSpan   (axisDimensionY));
            // Note: we didn't invoked reduce(Envelope) in order to make sure that
            //       our privated 'envelope' field is not exposed to subclasses.
        }
        assert !isDefined(ENVELOPE);
        throw new InvalidGridGeometryException(gridToCRS == null ?
                    ErrorKeys.UNSPECIFIED_TRANSFORM : ErrorKeys.UNSPECIFIED_IMAGE_SIZE);
    }

    /**
     * Returns the two-dimensional part of the {@linkplain #getGridRange grid range}
     * as a rectangle. Note that the returned object is a {@link Rectangle} subclass.
     *
     * @return The grid range (never {@code null}).
     * @throws InvalidGridGeometryException if this grid geometry has no grid range (i.e.
     *         <code>{@linkplain #isDefined isDefined}({@linkplain #GRID_RANGE GRID_RANGE})</code>
     *         returned {@code false}).
     *
     * @see #getGridRange
     */
    public GridRange2D getGridRange2D() throws InvalidGridGeometryException {
        if (gridRange != null) {
            assert isDefined(GRID_RANGE);
            return new GridRange2D(gridRange.getLow (gridDimensionX),
                                   gridRange.getLow (gridDimensionY),
                                   gridRange.getSpan (gridDimensionX),
                                   gridRange.getSpan (gridDimensionY));
        }
        assert !isDefined(GRID_RANGE);
        throw new InvalidGridGeometryException(ErrorKeys.UNSPECIFIED_IMAGE_SIZE);
    }

    /**
     * Returns a math transform for the two dimensional part. This is a convenience method for
     * working on horizontal data while ignoring vertical or temporal dimensions.
     *
     * @return The transform which allows for the transformations from grid coordinates
     *         to real world earth coordinates, operating only on two dimensions.
     *         The returned transform is often an instance of {@link AffineTransform}, which
     *         make it convenient for interoperability with Java2D.
     * @throws InvalidGridGeometryException if a two-dimensional transform is not available
     *         for this grid geometry.
     *
     * @see #getGridToCRS
     *
     * @since 2.3
     */
    public MathTransform2D getGridToCRS2D() throws InvalidGridGeometryException {
        if (gridToCRS2D != null) {
            return gridToCRS2D;
        }
        throw new InvalidGridGeometryException(ErrorKeys.NO_TRANSFORM2D_AVAILABLE);
    }

    /**
     * Returns a math transform for the two dimensional part. This method is similar
     * to {@link #getGridToCRS2D()} except that the transform may maps a pixel corner
     * instead of pixel center.
     *
     * @param  orientation The pixel part to map. The default value is
     *         {@link PixelOrientation#CENTER CENTER}.
     * @return The transform which allows for the transformations from grid coordinates
     *         to real world earth coordinates.
     * @throws InvalidGridGeometryException if a two-dimensional transform is not available
     *         for this grid geometry.
     *
     * @since 2.3
     */
    public MathTransform2D getGridToCRS2D(final PixelOrientation orientation) {
        if (gridToCRS2D == null) {
            throw new InvalidGridGeometryException(ErrorKeys.NO_TRANSFORM2D_AVAILABLE);
        }
        if (!PixelOrientation.UPPER_LEFT.equals(orientation)) {
            return computeGridToCRS2D(orientation);
        }
        synchronized (this) {
            if (cornerToCRS2D == null) {
                /*
                 * If the gridToCRS transform is 2-dimensional, reuse the existing instance
                 * (we will ensure in the assertion that it is suitable). Otherwise computes
                 * and caches a new instance. We cache only the UPPER_LEFT case since it is
                 * widely used; the other cases are rather unusual.
                 */
                if (gridToCRS.getSourceDimensions() == 2 && gridToCRS.getTargetDimensions() == 2) {
                    cornerToCRS2D = (MathTransform2D) super.getGridToCRS(PixelInCell.CELL_CORNER);
                } else {
                    cornerToCRS2D = computeGridToCRS2D(orientation);
                }
            }
        }
        return cornerToCRS2D;
    }

    /**
     * Computes the value to be returned by {@link #getGridToCRS2D}.
     */
    private MathTransform2D computeGridToCRS2D(final PixelOrientation orientation) {
        final int xdim = (gridDimensionX < gridDimensionY) ? 0 : 1;
        return (MathTransform2D) PixelTranslation.translate(gridToCRS2D,
                PixelOrientation.CENTER, orientation, xdim, xdim ^ 1);
    }

    /**
     * Returns a math transform mapping the specified pixel part. A translation (if needed) is
     * applied on the {@link #gridDimensionX} and {@link #gridDimensionY} parts of the transform;
     * all other dimensions are assumed mapping pixel center. For applying a translation on all
     * dimensions, use {@link #getGridToCRS(PixelInCell)} instead.
     *
     * @param  orientation The pixel part to map. The default value is
     *         {@link PixelOrientation#CENTER CENTER}.
     * @return The transform which allows for the transformations from grid coordinates
     *         to real world earth coordinates.
     * @throws InvalidGridGeometryException if a transform is not available
     *         for this grid geometry.
     *
     * @since 2.3
     */
    public MathTransform getGridToCRS(final PixelOrientation orientation) {
        if (gridToCRS == null) {
            throw new InvalidGridGeometryException(ErrorKeys.UNSPECIFIED_TRANSFORM);
        }
        return PixelTranslation.translate(gridToCRS, PixelOrientation.CENTER, orientation,
                gridDimensionX, gridDimensionY);
    }

    /**
     * Returns the specified position relative to the pixel center.
     * This method returns a value from the following table:
     * <p>
     * <table>
     *   <tr><th>Pixel orientation</th>                               <th>  x </th><th>  y </th></tr>
     *   <tr><td>{@link PixelOrientation#CENTER      CENTER}</td>     <td> 0.0</td><td> 0.0</td></tr>
     *   <tr><td>{@link PixelOrientation#UPPER_LEFT  UPPER_LEFT}</td> <td>-0.5</td><td>-0.5</td></tr>
     *   <tr><td>{@link PixelOrientation#UPPER_RIGHT UPPER_RIGHT}</td><td>+0.5</td><td>-0.5</td></tr>
     *   <tr><td>{@link PixelOrientation#LOWER_LEFT  LOWER_LEFT}</td> <td>-0.5</td><td>+0.5</td></tr>
     *   <tr><td>{@link PixelOrientation#LOWER_RIGHT LOWER_RIGHT}</td><td>+0.5</td><td>+0.5</td></tr>
     * </table>
     *
     * @param  orientation The pixel orientation.
     * @return The position relative to the pixel center.
     * @throws IllegalArgumentException if the specified orientation is not known.
     * @since 2.4
     *
     * @deprecated Moved to {@link PixelTranslation#getPixelTranslation(PixelOrientation)}.
     */
    @Deprecated
    public static Point2D getPixelTranslation(final PixelOrientation orientation)
            throws IllegalArgumentException
    {
        final PixelTranslation offset = PixelTranslation.getPixelTranslation(orientation);
        return new Point2D.Double(offset.dx, offset.dy);
    }

    /**
     * Transforms a point using the inverse of {@link #getGridToCRS2D()}.
     *
     * @param  point The point in logical coordinate system.
     * @return A new point in the grid coordinate system.
     * @throws InvalidGridGeometryException if a two-dimensional inverse
     *         transform is not available for this grid geometry.
     * @throws CannotEvaluateException if the transformation failed.
     */
    final Point2D inverseTransform(final Point2D point) throws InvalidGridGeometryException {
        if (gridFromCRS2D != null) {
            try {
                return gridFromCRS2D.transform(point, null);
            } catch (TransformException exception) {
                throw new CannotEvaluateException(Errors.format(ErrorKeys.CANT_EVALUATE_$1,
                          AbstractGridCoverage.toString(point, Locale.getDefault()), exception));
            }
        }
        throw new InvalidGridGeometryException(ErrorKeys.NO_TRANSFORM2D_AVAILABLE);
    }

    /**
     * Returns the pixel coordinate of a rectangle containing the
     * specified geographic area. If the rectangle can't be computed,
     * then this method returns {@code null}.
     */
    final Rectangle inverseTransform(Rectangle2D bounds) {
        if (bounds!=null && gridFromCRS2D!=null) {
            try {
                bounds = org.geotools.referencing.CRS.transform(gridFromCRS2D, bounds, null);
                final int xmin = (int) Math.floor(bounds.getMinX() - 0.5);
                final int ymin = (int) Math.floor(bounds.getMinY() - 0.5);
                final int xmax = (int) Math.ceil (bounds.getMaxX() - 0.5);
                final int ymax = (int) Math.ceil (bounds.getMaxY() - 0.5);
                return new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin);
            } catch (TransformException exception) {
                // Ignore, since this method is invoked from 'GridCoverage.prefetch' only.
                // It doesn't matter if the transformation failed; 'prefetch' is just a hint.
            }
        }
        return null;
    }

    /**
     * Compares the specified object with this grid geometry for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final GridGeometry2D that = (GridGeometry2D) object;
            return this.gridDimensionX == that.gridDimensionX &&
                   this.gridDimensionY == that.gridDimensionY &&
                   this.axisDimensionX == that.axisDimensionX &&
                   this.axisDimensionY == that.axisDimensionY;
            // Do not compare cornerToCRS2D since it may not be computed yet,
            // and should be strictly derived from gridToCRS2D anyway.
        }
        return false;
    }
}
