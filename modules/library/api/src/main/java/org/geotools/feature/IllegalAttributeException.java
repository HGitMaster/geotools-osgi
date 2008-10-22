/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.feature;

import org.opengis.feature.type.AttributeDescriptor;


/**
 * Indicates client class has attempted to create an invalid feature.
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/api/src/main/java/org/geotools/feature/IllegalAttributeException.java $
 * @deprecated Please use org.opengis.feature.IllegalAttributeException
 */
public class IllegalAttributeException extends org.opengis.feature.IllegalAttributeException {
    /**
     * Constructor with message argument.
     *
     * @param message Reason for the exception being thrown
     */
    public IllegalAttributeException(String message) {
        super(null,null,message);
    }

    /**
     * Constructor that makes the message given the expected and invalid.
     *
     * @param expected the expected AttributeType.
     * @param invalid the attribute that does not validate against expected.
     */
    public IllegalAttributeException(AttributeDescriptor expected, Object invalid) {
        super(expected, invalid );
    }

    /**
     * Constructor that makes the message given the expected and invalid, along
     * with the root cause.
     *
     * @param expected the expected AttributeType.
     * @param invalid the attribute that does not validate against expected.
     * @param cause the root cause of the error.
     */
    public IllegalAttributeException(AttributeDescriptor expected, Object invalid, Throwable cause) {
        super( expected, invalid, cause );
    }

}
