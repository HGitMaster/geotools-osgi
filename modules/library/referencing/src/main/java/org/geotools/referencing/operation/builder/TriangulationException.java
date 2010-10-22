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
package org.geotools.referencing.operation.builder;


/**
 * Thrown when it is unable to generate TIN.
 *
 * @since 2.4
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/library/referencing/src/main/java/org/geotools/referencing/operation/builder/TriangulationException.java $
 * @version $Id: TriangulationException.java 30641 2008-06-12 17:42:27Z acuster $
 * @author Jan Jezek
 */
public class TriangulationException extends RuntimeException {
    private static final long serialVersionUID = -3134565178815225915L;

    /**
     * Constructs an exception with the specified detail message.
     *
     * @param  message The cause for this exception. The cause is saved
     *         for later retrieval by the {@link #getCause()} method.
     */
    public TriangulationException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with no detail message.
     */
    public TriangulationException() {
        super();
    }
}
