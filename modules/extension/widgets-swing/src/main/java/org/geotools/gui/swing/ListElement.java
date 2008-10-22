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
package org.geotools.gui.swing;

// J2SE dependencies
import java.util.Locale;
import java.util.TimeZone;
import java.io.Serializable;

// OpenGIS dependencies
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * An element to be put in a list with a string representation more elaborated than the
 * element's default {@link Object#toString} method. This is specially usefull for
 * localizable elements.
 *
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/extension/widgets-swing/src/main/java/org/geotools/gui/swing/ListElement.java $
 * @version $Id: ListElement.java 30655 2008-06-12 20:24:25Z acuster $
 * @author Martin Desruisseaux
 */
final class ListElement implements Comparable, Serializable {
    /**
     * For compatibility with different versions.
     */
    private static final long serialVersionUID = -2601983786759359556L;

    /**
     * The element wrapped by this {@code ListElement}.
     */
    public final Object element;

    /**
     * The string representation of the element. Will be created again if the locale change.
     */
    private String string;
    
    /**
     * Creates a new instance of list element for the specified object and locale.
     */
    public ListElement(final Object element, final Locale locale) {
        this.element = element;
        setLocale(locale);
    }

    /**
     * Sets the locale for the string representation.
     */
    public void setLocale(final Locale locale) {
        if (element instanceof InternationalString) {
            string = ((InternationalString) element).toString(locale);
        } else if (element instanceof Locale) {
            string = ((Locale) element).getDisplayName(locale);
        } else if (element instanceof TimeZone) {
            string = ((TimeZone) element).getDisplayName(locale);
        } else {
            string = String.valueOf(element);
        }
    }

    /**
     * Returns the {@linkplain #element} of the specified object, or the object itself if it is
     * not an instance of {@code ListElement}.
     */
    public static Object unwrap(final Object object) {
        return (object instanceof ListElement) ? ((ListElement) object).element : object;
    }

    /**
     * Compares this element with the specified object for ordering.
     */
    public int compareTo(final Object other) {
        if (other instanceof ListElement) {
            final ListElement that = (ListElement) other;
            if (element instanceof Comparable) {
                return ((Comparable) element).compareTo(that.element);
            } else {
                return string.compareTo(that.string);
            }
        } else if (element instanceof Comparable) {
            return ((Comparable) element).compareTo(other);
        } else {
            return string.compareTo(String.valueOf(other));
        }
    }

    /**
     * Compares this element with the specified object for equality.
     */
    public boolean equals(final Object other) {
        if (other instanceof ListElement) {
            final ListElement that = (ListElement) other;
            return Utilities.equals(this.element, that.element) &&
                   Utilities.equals(this.string,  that.string);
        }
        return false;
    }

    /**
     * Returns a hash code value for this element.
     */
    public int hashCode() {
        int code = (element!=null) ? element.hashCode() : 0;
        code ^= string.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this element.
     */
    public String toString() {
        return string;
    }
}
