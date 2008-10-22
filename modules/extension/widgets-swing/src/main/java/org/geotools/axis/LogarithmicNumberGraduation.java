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

import java.util.Locale;
import javax.measure.unit.Unit;


/**
 * A graduation using numbers on a logarithmic axis.
 *
 * @since 2.0
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/extension/widgets-swing/src/main/java/org/geotools/axis/LogarithmicNumberGraduation.java $
 * @version $Id: LogarithmicNumberGraduation.java 30760 2008-06-18 14:28:24Z desruisseaux $
 * @author Martin Desruisseaux (PMO, IRD)
 */
public class LogarithmicNumberGraduation extends NumberGraduation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8514854171546232887L;

    /**
     * Contructs a new logarithmic graduation with the supplied units.
     *
     * @param unit The graduation unit.
     */
    public LogarithmicNumberGraduation(final Unit<?> unit) {
        super(unit);
    }

    /**
     * Constructs or reuses an iterator. This method override
     * the default {@link NumberGraduation} implementation.
     */
    @Override
    NumberIterator getTickIterator(final TickIterator reuse, final Locale locale) {
        if (reuse instanceof LogarithmicNumberIterator) {
            final NumberIterator it = (NumberIterator) reuse;
            it.setLocale(locale);
            return it;
        } else {
            return new LogarithmicNumberIterator(locale);
        }
    }
}
