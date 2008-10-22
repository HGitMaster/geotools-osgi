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

import java.util.Set;

import org.geotools.resources.Utilities;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.util.InternationalString;

public class AssociationTypeImpl extends PropertyTypeImpl implements AssociationType {

	final protected boolean isIdentified;
	
	final protected AssociationType superType;

	final protected AttributeType referenceType;
	
	public AssociationTypeImpl(
            Name name, AttributeType referenceType, boolean isIdentified, boolean isAbstract,
		Set/*<Filter>*/ restrictions, AssociationType superType, 
		InternationalString description		
	) {
		super(name, isAbstract, restrictions, description);
		
		this.referenceType = referenceType;
		this.isIdentified = isIdentified;
		this.superType = superType;
	}
	
	public boolean isIdentified() {
		return isAbstract;
	}

	public AssociationType getSuper() {
		return superType;
	}

	public int hashCode() {
        
		return 17 * (getName() == null ? 0 : getName().hashCode()) 
				^ (getReferenceType() == null ? 0 : getReferenceType().hashCode());
	}

	public boolean equals(Object other) {
		if (!(other instanceof AssociationTypeImpl)) {
			return false;
		}

		if (!super.equals(other)) 
			return false;
		
		AssociationType ass /*(tee hee)*/ = (AssociationType) other;

		if (!Utilities.equals(referenceType, ass.getReferenceType())) {
			return false;
		}
		
		if (!Utilities.equals(superType, ass.getSuper())) {
			return false;
		}
	
		return true;
	}

	public AttributeType getReferenceType() {
		return referenceType;
	}
	
	public String toString(){
		return "AssociationType "+getName().getLocalPart() + " reference to " + getReferenceType().getName().getLocalPart();
	}

}
