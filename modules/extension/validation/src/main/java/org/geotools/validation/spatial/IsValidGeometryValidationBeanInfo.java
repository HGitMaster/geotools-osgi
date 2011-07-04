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

import java.beans.PropertyDescriptor;

import org.geotools.validation.DefaultFeatureValidationBeanInfo;


/**
 * IsValidGeometryValidationBeanInfo purpose.
 * 
 * <p>
 * Description of IsValidGeometryValidationBeanInfo ...
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/extension/validation/src/main/java/org/geotools/validation/spatial/IsValidGeometryValidationBeanInfo.java $
 * @version $Id: IsValidGeometryValidationBeanInfo.java 37300 2011-05-25 05:32:39Z mbedward $
 */
public class IsValidGeometryValidationBeanInfo extends DefaultFeatureValidationBeanInfo {
    /**
     * IsValidGeometryValidationBeanInfo constructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public IsValidGeometryValidationBeanInfo() {
        super();
    }

    /**
     * Implementation of getPropertyDescriptors.
     *
     *
     * @see java.beans.BeanInfo#getPropertyDescriptors()
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return super.getPropertyDescriptors();
    }
}
