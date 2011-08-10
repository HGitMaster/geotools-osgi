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

import java.awt.Font;
import java.awt.RenderingHints;
import java.beans.PropertyChangeListener;
import java.text.Format;
import java.util.Locale;
import javax.measure.unit.Unit;


/**
 * An axis's graduation. A {@code Graduation} object encompass minimal
 * and maximal values for an axis in arbitrary units, and allow access to
 * tick locations and labels through a {@link TickIterator} object.
 * <p>
 * Different implementations may compute tick locations in different ways.
 * For example a graduation for dates is handled in a different way than a
 * graduation for numbers.
 *
 * @since 2.0
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing/src/main/java/org/geotools/axis/Graduation.java $
 * @version $Id: Graduation.java 30760 2008-06-18 14:28:24Z desruisseaux $
 * @author Martin Desruisseaux (PMO, IRD)
 */
public interface Graduation {
    /**
     * Rendering hint for the axis length, in pixels or points (1/72 of inch).
     * Values for this key must be {@link Number} objects. This hint is used
     * together with {@link #VISUAL_TICK_SPACING} during {@link TickIterator}
     * creation in order to compute a tick increment value.
     *
     * @see #getTickIterator
     */
    RenderingHints.Key VISUAL_AXIS_LENGTH = new RenderingHintKey(Number.class, 0);

    /**
     * Rendering hint for the preferred spacing between ticks, in pixels or points
     * (1/72 of inch). Values for this key must be {@link Number} objects. This hint
     * is used together with {@link #VISUAL_AXIS_LENGTH} during {@link TickIterator}
     * creation in order to compute a tick increment value. The tick spacing really
     * used may be slightly different, since {@link TickIterator} may choose a rounded
     * value.
     *
     * @see #getTickIterator
     */
    RenderingHints.Key VISUAL_TICK_SPACING = new RenderingHintKey(Number.class, 1);

    /**
     * The font to use for rendering tick labels. Value for this key must be a {@link Font}
     * object. If this hint is not provided, a default font will be used.
     *
     * @see Axis2D#paint
     */
    RenderingHints.Key TICK_LABEL_FONT = new RenderingHintKey(Font.class, 2);

    /**
     * The font to use for rendering the title. Value for this key must be a {@link Font} object.
     * If this hint is not provided, a default font will be used.
     *
     * @see Axis2D#paint
     */
    RenderingHints.Key AXIS_TITLE_FONT = new RenderingHintKey(Font.class, 3);

    /**
     * Returns the minimal value for this graduation.
     *
     * @return The minimal value in {@link #getUnit} units.
     *
     * @see #getMaximum
     * @see #getRange
     */
    double getMinimum();

    /**
     * Returns the maximal value for this graduation.
     *
     * @return The maximal value in {@link #getUnit} units.
     *
     * @see #getMinimum
     * @see #getRange
     */
    double getMaximum();

    /**
     * Returns the graduation's range. This is equivalents to computing
     * <code>{@link #getMaximum}-{@link #getMinimum}</code>. However, some
     * implementation may optimize this computation in order to avoid rounding errors.
     *
     * @return The graduation range.
     */
    double getRange();

    /**
     * Returns the axis title. If {@code includeUnits} is {@code true}, then the returned string
     * will includes units as in "Temperature (°C)", or time zone as in "Start time (UTC)". The
     * exact formatting is local-dependent.
     *
     * @param  includeSymbol {@code true} to format the unit or timezone symbol after the name.
     * @return The graduation name (also to be use as axis title).
     */
    String getTitle(final boolean includeSymbol);

    /**
     * Returns the graduation's units, or {@code null} if unknow.
     *
     * @return The graduation units, or {@code null}.
     */
    Unit<?> getUnit();

    /**
     * Returns the locale to use for formatting title and labels.
     *
     * @return The locale for formatting title and labels.
     */
    Locale getLocale();

    /**
     * Returns the format to use for formatting labels. The format really used by
     * {@link TickIterator#currentLabel} may not be the same. For example, some
     * iterators may adjust automatically the number of fraction digits.
     *
     * @return The labels format.
     */
    Format getFormat();

    /**
     * Returns an iterator object that iterates along the graduation ticks and provides access to
     * the graduation values. If an optional {@link RenderingHints} is specified, tick locations are
     * adjusted according values for {@link #VISUAL_AXIS_LENGTH} and {@link #VISUAL_TICK_SPACING}
     * keys.
     *
     * @param  hints Rendering hints for the axis, or {@code null} for the default hints.
     * @param  reuse An iterator to reuse if possible, or {@code null} to create a new one. A
     *         non-null object may help to reduce the number of object garbage-collected when
     *         rendering the axis.
     * @return A iterator to use for iterating through the graduation. This
     *         iterator may or may not be the {@code reuse} object.
     */
    TickIterator getTickIterator(RenderingHints hints, TickIterator reuse);

    /**
     * Adds a {@link PropertyChangeListener} to the listener list.
     * The listener is registered for all properties, such as "label"
     * and "locale".
     *
     * @param listener The listener to add.
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a {@link PropertyChangeListener} from the listener list.
     *
     * @param listener The listener to remove.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
}
