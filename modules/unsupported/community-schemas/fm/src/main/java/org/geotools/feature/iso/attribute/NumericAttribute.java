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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.geotools.feature.iso.AttributeImpl;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;

/**
 * Class that represents a Numeric.
 *
 * @author Ian Schneider
 * @author Chris Holmes, TOPP
 */
public class NumericAttribute extends AttributeImpl
	implements org.opengis.feature.simple.NumericAttribute {
	
    /**
     * @param descriptor
     * @param content
     * @throws IllegalArgumentException
     */
    public NumericAttribute(Number content, AttributeDescriptor descriptor)
        throws IllegalArgumentException {
    	
    	super(content, descriptor, null);
    	
    	if (!Number.class.isAssignableFrom(((AttributeType)descriptor.type()).getBinding())) {
             throw new IllegalArgumentException(
                 "Numeric requires Number class, " + "not " + ((AttributeType)descriptor.type()).getBinding());
        }
    }

    public String toString(){
    	StringBuffer sb = new StringBuffer("NumericAttribute[");
    	sb.append("TYPE=").
    	append(getType().getName())
    	.append(", content='")
    	.append(getValue())
    	.append("']");
    	return sb.toString();
    }
    /**
     * Allows this AttributeType to convert an argument to its prefered storage
     * type. If no parsing is possible, returns the original value. If a parse
     * is attempted, yet fails (i.e. a poor decimal format) throw the
     * Exception. This is mostly for use internally in Features, but
     * implementors should simply follow the rules to be safe.
     *
     * @param value the object to attempt parsing of.
     *
     * @return <code>value</code> converted to the preferred storage of this
     *         <code>AttributeType</code>.  If no parsing was possible then
     *         the same object is returned.
     *
     * @throws IllegalArgumentException if parsing is attempted and is
     *         unsuccessful.
     *
     * @task REVISIT: When type is Number, should we always be using Double?
     *       (What else would we do? - IanS)
     */
    protected Object parse(Object value) throws IllegalArgumentException {
        
    	    Class type = getType().getBinding();
       
        // handle null values first
        if (value == null) {
            return value;
        }

        // no parse needed here if types are compatable
        if ((value.getClass() == type)
                || type.isAssignableFrom(value.getClass())) {
            return value;
        }

        // convert one Number to our preferred type
        if (value instanceof Number) {
            return convertNumber((Number) value);
        }

        // parse a String to our preferred type
        // note, this is the final parsing attempt !
        String str = value.toString();

        try {
            Object parsed = parseFromString(str);

            if (parsed != null) {
                return parsed;
            }
        } catch (IllegalArgumentException iae) {
            // do nothing
        }

        // check empty string or black space
        if ((str.length() == 0) || (str.trim().length() == 0)) {
            Object parsed = parseFromString("0");

            if (parsed != null) {
                return parsed;
            }
        }

        // nothing else to do
        throw new IllegalArgumentException("Cannot parse " + value.getClass());
    }

    protected Object parseFromString(String value)
        throws IllegalArgumentException {
    	Class type = ((AttributeType)DESCRIPTOR.type()).getBinding();
        if (type == Byte.class) {
            return Byte.decode(value);
        }

        if (type == Short.class) {
            return Short.decode(value);
        }

        if (type == Integer.class) {
            return Integer.decode(value);
        }

        if (type == Float.class) {
            return Float.valueOf(value);
        }

        if (type == Double.class) {
            return Double.valueOf(value);
        }

        if (type == Long.class) {
            return Long.decode(value);
        }

        if (type == BigInteger.class) {
            return new BigInteger(value);
        }

        if (type == BigDecimal.class) {
            return new BigDecimal(value);
        }

        if (Number.class.isAssignableFrom(type)) {
            return new Double(value);
        }

        return null;
    }

    protected Object convertNumber(Number number) {
    	   Class type = getType().getBinding();
           
        if (type == Byte.class) {
            return new Byte(number.byteValue());
        }

        if (type == Short.class) {
            return new Short(number.shortValue());
        }

        if (type == Integer.class) {
            return new Integer(number.intValue());
        }

        if (type == Float.class) {
            return new Float(number.floatValue());
        }

        if (type == Double.class) {
            return new Double(number.doubleValue());
        }

        if (type == Long.class) {
            return new Long(number.longValue());
        }

        if (type == BigInteger.class) {
            return BigInteger.valueOf(number.longValue());
        }

        if (type == BigDecimal.class) {
            return BigDecimal.valueOf(number.longValue());
        }

        throw new RuntimeException("NumericAttribute cannot parse " + number);
    }

	public Number getNumber() {
		return (Number)getValue();
	}

	public void setNumber(Number newValue) {
		setValue(newValue);
	}
}
