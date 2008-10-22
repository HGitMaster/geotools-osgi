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
 *    Created on October 15, 2003, 1:57 PM
 */

package org.geotools.filter.parser;


/**
 *
 * @author  Ian Schneider
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/main/java/org/geotools/filter/parser/ExpressionException.java $
 */
public class ExpressionException extends ParseException {
    
    Throwable cause;
    
    public ExpressionException(String message,Token token) {
        this(message,token,null);
    }
    
    public ExpressionException(String message,Token token,Throwable cause) {
        super(message);
        this.currentToken = token;
        this.cause = cause;
    }
    
    public Throwable getCause() {
        return cause;
    }
    
    public String getMessage() {
        if (currentToken == null) return super.getMessage();
        
        return super.getMessage() + ", Current Token : " + currentToken.image;
    }
}
