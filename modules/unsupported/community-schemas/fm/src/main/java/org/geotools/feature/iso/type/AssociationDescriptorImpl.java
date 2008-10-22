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

import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyType;

public class AssociationDescriptorImpl extends StructuralDescriptorImpl
		implements AssociationDescriptor {

	AssociationType type;
	
	public AssociationDescriptorImpl(AssociationType type, Name name, int min, int max) {
		super(name,min,max);
		this.type = type;
	}
	
	public AssociationType getType() {
		return type;
	}
	
    public PropertyType type() {
        return getType();
    }
    
	public int hashCode(){
		return (37 * minOccurs + 37 * maxOccurs ) ^ 
            (type != null ? type.hashCode() : 0) ^ 
            (name != null ? name.hashCode() : 0);
	}
	
	public boolean equals(Object o){
		if(!(o instanceof AttributeDescriptorImpl))
			return false;
		
		AttributeDescriptorImpl d = (AttributeDescriptorImpl)o;
		return minOccurs == d.minOccurs && 
			maxOccurs == d.maxOccurs && 
			name.equals(d.name) && 
			type.equals(d.type);
			
	}
	
	public String toString(){
		return "AssociationDescriptor "+getName().getLocalPart()+" to "+getType();
	}

}