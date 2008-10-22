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

package org.geotools.feature.iso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.resources.Utilities;
import org.opengis.feature.Attribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;

/**
 * Simple, mutable class to store attributes.
 * 
 * @author Rob Hranac, VFNY
 * @author Chris Holmes, TOPP
 * @author Ian Schneider
 * @author Jody Garnett
 * @author Gabriel Roldan
 * @version $Id: AttributeImpl.java 31447 2008-09-08 03:52:18Z bencd $
 */
public class AttributeImpl implements Attribute, UserData {

    protected Object content;

    protected AttributeDescriptor DESCRIPTOR;

    protected final AttributeType TYPE;

    protected final String ID;

    private Map userData = new HashMap();

    public AttributeImpl(Object content, AttributeDescriptor descriptor,
            String id) {
        this(content, (AttributeType) descriptor.type(), id);

        DESCRIPTOR = descriptor;
    }

    public AttributeImpl(Object content, AttributeType type, String id) {
        // if (type.isAbstract()) {
        // throw new UnsupportedOperationException(type.getName()
        // + " is abstract");
        // }
        TYPE = type;
        ID = id;
        setValue(content);
    }

    public String getID() {
        return ID;
    }

    public Object getValue() {
        return content;
    }

    public AttributeDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    public PropertyDescriptor descriptor() {
        return getDescriptor();
    }

    public AttributeType getType() {
        return TYPE;
    }

    public Name name() {
        return DESCRIPTOR != null ? DESCRIPTOR.getName() : null;
    }

    public boolean nillable() {
        if (getDescriptor() != null) {
            return getDescriptor().isNillable();
        }

        return true;
    }

    /**
     * 
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     *             if the value has been parsed and validated, yet this
     *             Attribute does not passes the restrictions imposed by its
     *             AttributeType
     */
    public void setValue(Object newValue) throws IllegalArgumentException,
            IllegalStateException {

        newValue = parse(newValue);

        try {
            Types.validate(getType(), this, newValue);
        } catch (IllegalAttributeException e) {
            throw (IllegalArgumentException) new IllegalArgumentException()
                    .initCause(e);
        }

        content = newValue;
    }

    /**
     * Override of hashCode.
     * 
     * @return hashCode for this object.
     */
    public int hashCode() {
        return 37 * (DESCRIPTOR == null ? 0 : DESCRIPTOR.hashCode())
                + (37 * (TYPE == null ? 0 : TYPE.hashCode()))
                + (37 * (ID == null ? 0 : ID.hashCode()))
                + (37 * (content == null ? 0 : content.hashCode()))
                + 37 * userData.hashCode();
    }

    /**
     * Override of equals.
     * 
     * @param other
     *            the object to be tested for equality.
     * 
     * @return whether other is equal to this attribute Type.
     */
    public boolean equals(Object other) {
        if (!(other instanceof AttributeImpl)) {
            return false;
        }

        AttributeImpl att = (AttributeImpl) other;

        if (!Utilities.equals(DESCRIPTOR, att.DESCRIPTOR))
            return false;

        if (!Utilities.equals(TYPE, att.TYPE))
            return false;

        if (!Utilities.equals(ID, att.ID))
            return false;

        if (!Utilities.deepEquals(content, att.content))
            return false;

        return userData.equals(att.userData);
    }

    /**
     * Allows this Attribute to convert an argument to its prefered storage
     * type. If no parsing is possible, returns the original value. If a parse
     * is attempted, yet fails (i.e. a poor decimal format) throw the Exception.
     * This is mostly for use internally in Features, but implementors should
     * simply follow the rules to be safe.
     * 
     * @param value
     *            the object to attempt parsing of.
     * 
     * @return <code>value</code> converted to the preferred storage of this
     *         <code>AttributeType</code>. If no parsing was possible then
     *         the same object is returned.
     * 
     * @throws IllegalArgumentException
     *             if parsing is attempted and is unsuccessful.
     */
    protected Object parse(Object value) throws IllegalArgumentException {
        return value;
    }

    public Object operation(Name arg0, List arg1) {
        throw new UnsupportedOperationException("operation not supported yet");
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Attribute[");
        sb.append(DESCRIPTOR == null ? "" : DESCRIPTOR.getName()
                        .getLocalPart());
        sb.append(":");
        sb.append(TYPE.getName().getLocalPart());
        sb.append(":@");
        sb.append(ID == null? "" : ID);
        sb.append(":");
        sb.append(content);
        sb.append("]");
//        LOGGER.fine("converting value for unbound Attribute (possibly null value) " + sb.toString() );
        return (content == null ? "" : content.toString() );
    }

    public Object getUserData(Object key) {
        return userData.get(key);
    }

    public void putUserData(Object key, Object data) {
        this.userData.put(key, data);
    }

}
