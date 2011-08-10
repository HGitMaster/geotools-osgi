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
import java.text.DateFormat;
import java.text.Format;
import java.util.Date;
import java.util.TimeZone;

import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.quantity.Duration;
import javax.measure.converter.UnitConverter;
import javax.measure.converter.ConversionException;

import org.geotools.util.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * A graduation using dates on a linear axis.
 *
 * @since 2.0
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing/src/main/java/org/geotools/axis/DateGraduation.java $
 * @version $Id: DateGraduation.java 30760 2008-06-18 14:28:24Z desruisseaux $
 * @author Martin Desruisseaux (PMO, IRD)
 */
public class DateGraduation extends AbstractGraduation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -7590383805990568769L;

    /**
     * The unit for millisecond.
     */
    public static final Unit<Duration> MILLISECOND = SI.MILLI(SI.SECOND);

    /**
     * The minimal value for this graduation, in milliseconds ellapsed since January 1st,
     * 1970 (no matter what the graduation units are). Default to current time (today).
     */
    private long minimum = System.currentTimeMillis();

    /**
     * The maximal value for this graduation, in milliseconds ellapsed since January 1st,
     * 1970 (no matter what the graduation units are). Default to tomorrow.
     */
    private long maximum = minimum + 24*60*60*1000L;

    /**
     * The time zone for graduation labels.
     */
    private TimeZone timezone;

    /**
     * The converter from {@link #MILLISECOND} to {@link #getUnit}.
     * Will be created only when first needed.
     */
    private transient UnitConverter fromMillis;

    /**
     * The converter from {@link #getUnit} to {@link #MILLISECOND}.
     * Will be created only when first needed.
     */
    private transient UnitConverter toMillis;

    /**
     * Construct a graduation with the supplied time zone.
     * Unit default to {@linkplain #MILLISECOND milliseconds}.
     *
     * @param  timezone The timezone.
     */
    public DateGraduation(final TimeZone timezone) {
        this(timezone, MILLISECOND);
    }

    /**
     * Construct a graduation with the supplied time zone and unit.
     *
     * @param  timezone The timezone.
     * @param  unit The unit. Must be compatible with {@linkplain #MILLISECOND milliseconds}.
     * @throws ConversionException if the supplied unit is not a time unit.
     */
    public DateGraduation(final TimeZone timezone, final Unit<Duration> unit)
            throws ConversionException
    {
        super(unit);
        ensureTimeUnit(unit);
        this.timezone = (TimeZone) timezone.clone();
    }

    /**
     * Checks if the specified unit is a time unit.
     *
     * @param the unit to check.
     * @throws ConversionException if the specified unit is not a time unit.
     */
    private static void ensureTimeUnit(final Unit<?> unit) throws ConversionException {
        if (unit == null || !MILLISECOND.isCompatible(unit)) {
            throw new ConversionException(Errors.format(
                    ErrorKeys.ILLEGAL_ARGUMENT_$2, "unit", unit));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked") // Checked by constructor and setters.
    public Unit<Duration> getUnit() {
        return (Unit) super.getUnit();
    }

    /**
     * Returns the converter from {@link #MILLISECOND} to {@link #getUnit}.
     */
    private UnitConverter fromMillis() {
        if (fromMillis == null) {
            Unit<Duration> unit = getUnit();
            if (unit == null) {
                unit = MILLISECOND;
            }
            fromMillis = MILLISECOND.getConverterTo(unit);
        }
        return fromMillis;
    }

    /**
     * Returns the converter from {@link #getUnit} to {@link #MILLISECOND}.
     */
    private UnitConverter toMillis() {
        if (toMillis == null) {
            Unit<Duration> unit = getUnit();
            if (unit == null) {
                unit = MILLISECOND;
            }
            toMillis = unit.getConverterTo(MILLISECOND);
        }
        return toMillis;
    }

    /**
     * Set the minimum value for this graduation. If the new minimum is greater than the current
     * maximum, then the maximum will also be set to a value greater than or equals to the minimum.
     *
     * @param  time The new minimum.
     * @return {@code true} if the state of this graduation changed as a result of this call, or
     *         {@code false} if the new value is identical to the previous one.
     *
     * @see #setMaximum(Date)
     */
    public synchronized boolean setMinimum(final Date time) {
        final long value = time.getTime();
        long old = minimum;
        minimum = value;
        firePropertyChange("minimum", old, time);
        if (maximum < value) {
            old = maximum;
            maximum = value;
            firePropertyChange("maximum", old, time);
            return true;
        }
        return value != old;
    }

    /**
     * Set the maximum value for this graduation. If the new maximum is less than the current
     * minimum, then the minimum will also be set to a value less than or equals to the maximum.
     *
     * @param  time The new maximum.
     * @return {@code true} if the state of this graduation changed as a result of this call, or
     *         {@code false} if the new value is identical to the previous one.
     *
     * @see #setMinimum(Date)
     */
    public synchronized boolean setMaximum(final Date time) {
        final long value = time.getTime();
        long old = maximum;
        maximum = value;
        firePropertyChange("maximum", old, time);
        if (minimum > value) {
            old = minimum;
            minimum = value;
            firePropertyChange("minimum", old, time);
            return true;
        }
        return value != old;
    }

    /**
     * Set the minimum value as a real number. This method converts the value to
     * {@linkplain #MILLISECOND milliseconds} and invokes {@link #setMinimum(Date)}.
     */
    public final synchronized boolean setMinimum(final double value) {
        ensureFinite("minimum", value);
        return setMinimum(new Date(Math.round(toMillis().convert(value))));
    }

    /**
     * Set the maximum value as a real number. This method converts the value to
     * {@linkplain #MILLISECOND milliseconds} and invokes {@link #setMaximum(Date)}.
     */
    public final synchronized boolean setMaximum(final double value) {
        ensureFinite("maximum", value);
        return setMaximum(new Date(Math.round(toMillis().convert(value))));
    }

    /**
     * Returns the minimal value for this graduation. The value is in units of {@link #getUnit}.
     * By default, it is the number of millisecondes ellapsed since January 1st, 1970 at 00:00 UTC.
     *
     * @see #setMinimum(double)
     * @see #getMaximum
     * @see #getRange
     */
    public double getMinimum() {
        return fromMillis().convert(minimum);
    }

    /**
     * Returns the maximal value for this graduation. The value is in units of {@link #getUnit}.
     * By default, it is the number of millisecondes ellapsed since January 1st, 1970 at 00:00 UTC.
     *
     * @see #setMaximum(double)
     * @see #getMinimum
     * @see #getRange
     */
    public double getMaximum() {
        return fromMillis().convert(maximum);
    }

    /**
     * Returns the graduation's range. This is equivalents to computing
     * <code>{@link #getMaximum}-{@link #getMinimum}</code>, but using integer arithmetic.
     */
    public synchronized double getRange() {
        if (getUnit() == MILLISECOND) {
            return maximum - minimum;
        } else {
            // TODO: we would need something similar to AffineTransform.deltaTransform(...)
            //       here in order to performs the conversion in a more efficient way.
            final UnitConverter toMillis = toMillis();
            return toMillis.convert(maximum) - toMillis.convert(minimum);
        }
    }

    /**
     * Returns the timezone for this graduation.
     *
     * @return The current timezone.
     */
    public TimeZone getTimeZone() {
        return timezone;
    }

    /**
     * Sets the time zone for this graduation. This affect only the way labels are displayed.
     *
     * @param timezone The new timezone.
     */
    public void setTimeZone(final TimeZone timezone) {
        this.timezone = (TimeZone) timezone.clone();
    }

    /**
     * Returns a string representation of the time zone for this graduation.
     */
    @Override
    String getSymbol() {
        return getTimeZone().getDisplayName();
    }

    /**
     * Changes the graduation's units. This method will automatically convert minimum and maximum
     * values from the old units to the new one.
     *
     * @param unit The new units, or {@code null} if unknow. If null, minimum and maximum values
     *             are not converted.
     * @throws ConversionException if the specified unit is not a time unit.
     */
    @Override
    public void setUnit(final Unit<?> unit) throws ConversionException {
        ensureTimeUnit(unit);
        fromMillis = null;
        toMillis   = null;
        // Nothing to convert here. The conversions are performed
        // on the fly by 'getMinimum()' / 'getMaximum()'.
        super.setUnit(unit);
    }

    /**
     * Returns the format to use for formatting labels. The format really used by
     * {@link TickIterator#currentLabel} may not be the same. For example, some
     * iterators may choose to show or hide hours, minutes and seconds.
     */
    public Format getFormat() {
        final DateFormat format = DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT, getLocale());
        format.setTimeZone(timezone);
        return format;
    }

    /**
     * Returns an iterator object that iterates along the graduation ticks
     * and provides access to the graduation values. If an optional {@link
     * RenderingHints} is specified, tick locations are adjusted according
     * values for {@link #VISUAL_AXIS_LENGTH} and {@link #VISUAL_TICK_SPACING}
     * keys.
     *
     * @param  hints Rendering hints, or {@code null} for the default hints.
     * @param  reuse An iterator to reuse if possible, or {@code null}
     *         to create a new one. A non-null object may help to reduce the
     *         number of object garbage-collected when rendering the axis.
     * @return A iterator to use for iterating through the graduation. This
     *         iterator may or may not be the {@code reuse} object.
     */
    public synchronized TickIterator getTickIterator(final RenderingHints hints,
                                                     final TickIterator   reuse)
    {
        final float visualAxisLength  = getVisualAxisLength (hints);
        final float visualTickSpacing = getVisualTickSpacing(hints);
        long minimum = this.minimum;
        long maximum = this.maximum;
        if (!(minimum < maximum)) { // Uses '!' for catching NaN.
            minimum = (minimum+maximum)/2 - 12*60*60*1000L;
            maximum = minimum + 24*60*60*1000L;
        }
        final DateIterator it;
        if (reuse instanceof DateIterator) {
            it = (DateIterator) reuse;
            it.setLocale(getLocale());
            it.setTimeZone(getTimeZone());
        } else {
            it = new DateIterator(getTimeZone(), getLocale());
        }
        it.init(minimum, maximum, visualAxisLength, visualTickSpacing);
        return it;
    }

    /**
     * Support for reporting property changes. This method can be called when a property
     * has changed. It will send the appropriate {@link java.beans.PropertyChangeEvent}
     * to any registered {@link PropertyChangeListeners}.
     *
     * @param propertyName The property whose value has changed.
     * @param oldValue     The property's previous value.
     * @param newValue     The property's new value.
     */
    private final void firePropertyChange(final String propertyName,
                                          final long oldValue, final Date newValue)
    {
        if (oldValue != newValue.getTime()) {
            listenerList.firePropertyChange(propertyName, new Date(oldValue), newValue);
        }
    }

    /**
     * Compares this graduation with the specified object for equality.
     * This method do not compare registered listeners.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final DateGraduation that = (DateGraduation) object;
            return this.minimum == that.minimum &&
                   this.maximum == that.maximum &&
                   Utilities.equals(this.timezone, that.timezone);
        }
        return false;
    }

    /**
     * Returns a hash value for this graduation.
     */
    @Override
    public int hashCode() {
        final long lcode = minimum + 37*maximum;
        int code = (int)lcode ^ (int)(lcode >>> 32);
        if (timezone != null) {
            code ^= timezone.hashCode();
        }
        return code ^ super.hashCode();
    }
}
