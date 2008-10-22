/*
 *    GeoLBS - OpenSource Location Based Servces toolkit
 *    (C) 2004, Julian J. Ray, All Rights Reserved
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
 */

package org.geotools.data.geomedia;

/**
 * <p>
 * Title: GeoTools2 Development
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) Julian J. Ray 2003
 * </p>
 * 
 * <P>
 * Used to pass connection parameters to a geomedia data source factory. This structure is used to allow the connection
 * factory to dynamically load and instantiate a JDBC Data Soruce driver for the particular GeoMedia database they
 * wish to access. Each instance contains the following:
 * 
 * <UL>
 * <li>
 * MethodName - Name of a method on the Data Source object to call
 * </li>
 * <li>
 * ClassType - Class name for the parameter such as String.class. Use Integer.TYPE or Double.TYPE for primitive types
 * such as int or double.
 * </li>
 * <li>
 * Method parameter.
 * </li>
 * </ul>
 * </p>
 *
 * @author Julian J. Ray
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/geomedia/src/main/java/org/geotools/data/geomedia/GeoMediaConnectionParam.java $
 * @version 1.0
 */
public class GeoMediaConnectionParam {
    /** DOCUMENT ME! */
    protected String mParam;

    /** DOCUMENT ME! */
    protected Class mClass;

    /** DOCUMENT ME! */
    protected Object mVal;

    /**
     * Creates a new GeoMediaConnectionParam object.
     *
     * @param param DOCUMENT ME!
     * @param classType DOCUMENT ME!
     * @param val DOCUMENT ME!
     */
    public GeoMediaConnectionParam(String param, Class classType, Object val) {
        mParam = param;
        mClass = classType;
        mVal = val;
    }

    /**
     * getMethodName - returns the name of the method to call.
     *
     * @return String
     */
    public String getMethodName() {
        return mParam;
    }

    /**
     * getClassType - returns the class type of an object passed as an argument to the method.
     *
     * @return Class
     */
    public Class getClassType() {
        return mClass;
    }

    /**
     * getParam - returns an Object representing the argument passed to the method.
     *
     * @return Object
     */
    public Object getParam() {
        return mVal;
    }
}
