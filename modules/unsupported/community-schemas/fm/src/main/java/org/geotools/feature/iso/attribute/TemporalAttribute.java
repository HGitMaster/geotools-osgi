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

package org.geotools.feature.iso.attribute;

import java.util.Date;

import org.geotools.feature.iso.AttributeImpl;
import org.opengis.feature.type.AttributeDescriptor;


/**
 * A Default class that represents a Temporal attribute.
 */
public class TemporalAttribute extends AttributeImpl 
	implements org.opengis.feature.simple.TemporalAttribute {
	
    // this might be right, maybe not, but anyway, its a default formatting
    static java.text.DateFormat format = java.text.DateFormat.getInstance();

    public TemporalAttribute(Date value, AttributeDescriptor desc) {
        super(value,desc, null);
    }

    public Object parse(Object value) throws IllegalArgumentException {
        if (value == null) {
            return value;
        }
        
        Class type = getType().getBinding();

        if (type.isAssignableFrom(value.getClass())) {
            return value;
        }

        if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        }

        if (value instanceof java.util.Calendar) {
            return ((java.util.Calendar) value).getTime();
        }

        try {
            return format.parse(value.toString());
        } catch (java.text.ParseException pe) {
            throw new IllegalArgumentException("unable to parse " + value
                + " as Date");
        }
    }

	public Date getDate() {
		return (Date)getValue();
	}

	public void setDate(Date newValue) {
		setValue(newValue);
	}

}
