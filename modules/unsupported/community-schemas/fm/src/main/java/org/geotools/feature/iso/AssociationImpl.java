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

import org.geotools.resources.Utilities;
import org.opengis.feature.Association;
import org.opengis.feature.Attribute;
import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;

public class AssociationImpl implements Association {

    /** 
     * The related attribute.
     */
    Attribute related;
    
    /**
     * The descriptor, this can never be null for association.
     */
    AssociationDescriptor descriptor;
    
    public AssociationImpl(Attribute related, AssociationDescriptor descriptor) {
        this.related = related;
        this.descriptor = descriptor;
    }
    
    public AssociationDescriptor getDescriptor() {
        return descriptor;
    }

    public PropertyDescriptor descriptor() {
        return getDescriptor();
    }
    
    public AssociationType getType() {
        return (AssociationType) descriptor.type();
    }

    public AttributeType getRelatedType() {
        return getType().getReferenceType();
    }

    public Attribute getRelated() {
        return related;
    }

    public void setRelated(Attribute related) {
        this.related = related;
    }

    public Name name() {
        return getDescriptor().getName();   
    }

    public boolean equals(Object other) {
        if (!(other instanceof AssociationImpl)) {
            return false;
        }

        AssociationImpl assoc = (AssociationImpl) other;

        if (!Utilities.equals(descriptor,assoc.descriptor))
            return false;

        if (!Utilities.equals(related,assoc.related))
            return false;
        
        return true;
    }
    
    public int hashCode() {
        return 37 * (descriptor.hashCode())  
        + (37 * (related == null ? 0 : related.hashCode()));
        
    }
}
