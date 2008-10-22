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

import org.geotools.resources.Utilities;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.StructuralDescriptor;

public abstract class StructuralDescriptorImpl extends PropertyDescriptorImpl implements
		StructuralDescriptor {

	final protected Name name;
	final protected int minOccurs;
	final protected int maxOccurs;
	
	
	public StructuralDescriptorImpl(Name name, int minOccurs, int maxOccurs) {
		this.name = name;
		this.minOccurs = minOccurs;
		this.maxOccurs = maxOccurs;
	}
	
	public int getMinOccurs() {
		return minOccurs;
	}

	public int getMaxOccurs() {
		return maxOccurs;
	}

	public Name getName() {
		return name;
	}

	public Name name() {
		return getName();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof StructuralDescriptorImpl)) {
			return false;
		}
		
		StructuralDescriptorImpl other = (StructuralDescriptorImpl) obj;
		return Utilities.equals(name,other.name) && 
			(minOccurs == other.minOccurs) && (maxOccurs == other.maxOccurs);
	}
	
	public int hashCode() {
		return (37 * minOccurs + 37 * maxOccurs ) ^ ( name.hashCode() );
	}
	

}
