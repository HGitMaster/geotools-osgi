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

package org.geotools.feature.iso.type;

import java.util.Collection;
import java.util.Set;

import org.geotools.resources.Utilities;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.util.InternationalString;

/**
 * Base class for attribute types.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class AttributeTypeImpl extends PropertyTypeImpl implements AttributeType {
	
	final protected boolean IDENTIFIED;

	final protected Class BINDING;

	final protected AttributeType SUPER;

	public AttributeTypeImpl(
		Name name, Class binding, boolean identified, boolean isAbstract,
		Set/*<Filter>*/ restrictions, AttributeType superType, InternationalString description
	) {
		super(name, isAbstract, restrictions, description);
		
		if(binding == null){
			throw new NullPointerException("binding");
		}
		
		BINDING = binding;
		IDENTIFIED = identified;
		SUPER = superType;
	}

	public boolean isIdentified() {
		return IDENTIFIED;
	}

	public Class getBinding() {
		return BINDING;
	}

	public AttributeType getSuper() {
		return SUPER;
	}

	public Collection getOperations() {
		throw new UnsupportedOperationException("Operations not implemented");
	}
	
	/**
	 * Allows this AttributeType to convert an argument to its prefered storage
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
	public Object parse(Object value) throws IllegalArgumentException {
		//do nothing, sublcasses should override
		return value;
	}
	
	public Object createDefaultValue() {
		return null;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName());
			sb.append("[name=").append(getName()).append(", binding=").append(BINDING)
			.append(", abstrsct=, ").append(isAbstract()).append(", identified=")
			.append(IDENTIFIED).append(", restrictions=").append(getRestrictions())
			.append(", superType=").append(SUPER).append("]");

		return sb.toString();
	}

	/**
	 * Override of hashCode.
	 * 
	 * @return hashCode for this object.
	 */
	public int hashCode() {
		return name.hashCode() ^ BINDING.hashCode();
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
		if (!(other instanceof AttributeType)) {
			return false;
		}

		if (!super.equals(other)) 
			return false;
		
		AttributeType att = (AttributeType) other;

		if (!Utilities.equals(BINDING,att.getBinding())) {
			return false;
		}
		
		if (IDENTIFIED != att.isIdentified()) {
			return false;
		}

		if (!Utilities.equals(SUPER, att.getSuper())) {
			return false;
		}
	
		return true;
	}

}
