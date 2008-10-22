/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.util;

import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.geotools.factory.Hints;

/**
 * Handles converting Dates to Strings
 * 
 * @author Gabriel Roldan, Axios Engineering.
 */
public class DateConverterFactory implements ConverterFactory {

    public Converter createConverter(Class source, Class target, Hints hints) {
        if (Date.class.isAssignableFrom(source) && String.class.equals(target)) {
            return new Converter() {
                public Object convert(Object source, Class target) throws Exception {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime((Date) source);
                    String printDate = DatatypeConverter.printDate(calendar);
                    return printDate;
                }
            };
        }
        return null;
    }

}
