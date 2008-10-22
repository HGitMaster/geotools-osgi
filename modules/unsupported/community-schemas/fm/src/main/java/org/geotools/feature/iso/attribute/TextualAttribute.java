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

import org.geotools.feature.iso.AttributeImpl;
import org.opengis.feature.simple.TextAttribute;
import org.opengis.feature.type.AttributeDescriptor;

public class TextualAttribute extends AttributeImpl implements TextAttribute {

    public TextualAttribute(CharSequence value, AttributeDescriptor desc) {
        super(value, desc, null);
    }

    public Object parse(Object value) throws IllegalArgumentException {
        if (value == null) {
            return value;
        }

        // string is immutable, so lets keep it
        if (value instanceof String) {
            return value;
        }

        // other char sequences are not mutable, create a String from it.
        // this also covers any other cases...
        return value.toString();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("TextualAttribute[");
        sb.append("TYPE=").append(getType().getName()).append(", content='").append(getValue())
                .append("']");
        return sb.toString();
    }

    public CharSequence getText() {
        return (CharSequence) getValue();
    }

    public void setText(CharSequence newValue) {
        setValue(newValue);
    }
}
