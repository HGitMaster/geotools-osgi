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
package org.geotools.styling;

import org.geotools.util.Utilities;
import org.opengis.style.Description;
import org.opengis.style.StyleVisitor;
import org.opengis.util.InternationalString;

/**
 *
 * @author Johann Sorel
 */
public class DescriptionImpl implements Description{

    private InternationalString title;
    private InternationalString desc;
    
    public DescriptionImpl(InternationalString title, InternationalString desc){
        this.title = title;
        this.desc = desc;
    }
    
    DescriptionImpl(DescriptionImpl imp){
        this.title = imp.title;
        this.desc = imp.desc;
    }
    
    public DescriptionImpl(Description description) {
        this.title = description.getTitle();
        this.desc = description.getAbstract();
    }

    public InternationalString getTitle() {
        return title;
    }
    
    public void setTitle(InternationalString title){
        this.title = title;
    }

    public InternationalString getAbstract() {
        return desc;
    }

    public void setAbstract(InternationalString abs){
        this.desc = abs;
    }
    
    public Object accept(StyleVisitor visitor,Object data) {
        return visitor.visit(this,data);
    }

    @Override
    public String toString() {
        return title.toString() + ", " + desc.toString();
    }

    @Override
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof DescriptionImpl) {
            DescriptionImpl other = (DescriptionImpl) oth;

            return Utilities.equals(title, other.title)
            && Utilities.equals(desc, other.desc);
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;

        if (title != null) {
            result = (PRIME * result) + title.hashCode();
        }
        
        if (desc != null) {
            result = (PRIME * result) + desc.hashCode();
        }

        return result;
    }
    
    
    
    
}
