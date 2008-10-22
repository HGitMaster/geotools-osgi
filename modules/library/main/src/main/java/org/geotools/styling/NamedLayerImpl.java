/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
 *
 * Created on November 3, 2003, 10:10 AM
 */
package org.geotools.styling;

import java.util.ArrayList;
import java.util.List;

import org.geotools.resources.Utilities;


/**
 * Default implementation of named layer.
 *
 * @author jamesm
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/main/java/org/geotools/styling/NamedLayerImpl.java $
 */
public class NamedLayerImpl extends StyledLayerImpl implements NamedLayer {
    List<Style> styles = new ArrayList<Style>();

    FeatureTypeConstraint[] featureTypeConstraints = new FeatureTypeConstraint[0];
    
    public FeatureTypeConstraint[] getLayerFeatureConstraints() {
        return featureTypeConstraints;
    }

    public void setLayerFeatureConstraints(FeatureTypeConstraint[] featureTypeConstraints) {
    	this.featureTypeConstraints = featureTypeConstraints;
    }
    
    public Style[] getStyles() {
        return (Style[]) styles.toArray(new Style[0]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param sl may be a StyleImpl or a NamedStyle
     */
    public void addStyle(Style sl) {
        styles.add(sl);
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

	public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        
        if (oth instanceof NamedLayerImpl) {
        	NamedLayerImpl other = (NamedLayerImpl) oth;

        	if (!Utilities.equals(styles, other.styles))
        		return false;
        	
        	if (featureTypeConstraints.length != other.featureTypeConstraints.length) return false;
        	
        	for (int i = 0; i < featureTypeConstraints.length; i++) {
        		if (!Utilities.equals(featureTypeConstraints[i], other.featureTypeConstraints[i]))
        			return false;
        	}
        	return true;
        }

        return false;
	}
}
