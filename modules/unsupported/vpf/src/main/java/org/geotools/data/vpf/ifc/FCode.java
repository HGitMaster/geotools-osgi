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
package org.geotools.data.vpf.ifc;

import java.util.Arrays;
import java.util.List;

/**
 * Defines constants related to FACC Code processing.  
 *
 * @author John Meagher
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/unsupported/vpf/src/main/java/org/geotools/data/vpf/ifc/FCode.java $
 * @version $Id: FCode.java 37306 2011-05-25 06:13:21Z mbedward $
 */
public interface FCode {

    /**
     * Array of attributes the FACC Code information is allowed to be stored under.
     * The default allowed attribute names are "f_code" and "facc".  This can be
     * be overridden using the system property "gt.vpf.allowedFCodeAttributes"
     * with a comma separated list of the desired values.  
     **/
    public static final String[] ALLOWED_FCODE_ATTRIBUTES =
        System.getProperty( "gt.vpf.allowedFCodeAttributes", "f_code,facc" ).split( "," );

    /**
     * The ALLOWED_FCODE_ATTRIBUTES array in list form.
     * @see #ALLOWED_FCODE_ATTRIBUTES
     **/
    public static final List ALLOWED_FCODE_ATTRIBUTES_LIST = Arrays.asList( ALLOWED_FCODE_ATTRIBUTES );
}
