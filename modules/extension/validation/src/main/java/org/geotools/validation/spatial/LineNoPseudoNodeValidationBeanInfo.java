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
package org.geotools.validation.spatial;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ResourceBundle;


/**
 * LineAbstractValidationBeanInfopurpose.
 * 
 * <p>
 * Description of LineAbstractValidationBeanInfo...
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/extension/validation/src/main/java/org/geotools/validation/spatial/LineNoPseudoNodeValidationBeanInfo.java $
 * @version $Id: LineNoPseudoNodeValidationBeanInfo.java 37300 2011-05-25 05:32:39Z mbedward $
 */
public class LineNoPseudoNodeValidationBeanInfo extends LineAbstractValidationBeanInfo{
    /**
     * LineAbstractValidationBeanInfoconstructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public LineNoPseudoNodeValidationBeanInfo(){
        super();
    }

    /**
     * Implementation of getPropertyDescriptors.
     *
     *
     * @see java.beans.BeanInfo#getPropertyDescriptors()
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
    	PropertyDescriptor[] pd2 = super.getPropertyDescriptors();
    	ResourceBundle resourceBundle = getResourceBundle(LineNoPseudoNodeValidation.class);

    	if (pd2 == null) {
    		pd2 = new PropertyDescriptor[0];
    	}

    	PropertyDescriptor[] pd = new PropertyDescriptor[pd2.length + 1];
    	int i = 0;

    	for (; i < pd2.length; i++)
    		pd[i] = pd2[i];

    	try {
    		pd[i] = createPropertyDescriptor("degreesAllowable",
    				LineNoPseudoNodeValidation.class, resourceBundle);
    		pd[i].setExpert(false);
    	} catch (IntrospectionException e) {
    		pd = pd2;

    		// TODO error, log here
    		e.printStackTrace();
    	}

    	return pd;
    }
}
