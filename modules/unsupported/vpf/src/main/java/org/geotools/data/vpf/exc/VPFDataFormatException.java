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
 */
package org.geotools.data.vpf.exc;

/**
 * Class VPFDataFormatException.java is responsible for
 *
 * <p>
 * Created: Wed Jan 29 10:28:53 2003
 * </p>
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/vpf/src/main/java/org/geotools/data/vpf/exc/VPFDataFormatException.java $
 * @version $Id: VPFDataFormatException.java 30687 2008-06-13 11:03:06Z acuster $
 */
public class VPFDataFormatException extends RuntimeException {
    /**
     * Creates a new VPFDataFormatException object.
     */
    public VPFDataFormatException() {
        super();
    }

    /**
     * Creates a new VPFDataFormatException object.
     *
     * @param message DOCUMENT ME!
     */
    public VPFDataFormatException(String message) {
        super(message);
    }

    /**
     * Creates a new VPFDataFormatException object.
     *
     * @param message DOCUMENT ME!
     * @param cause DOCUMENT ME!
     */
    public VPFDataFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new VPFDataFormatException object.
     *
     * @param cause DOCUMENT ME!
     */
    public VPFDataFormatException(Throwable cause) {
        super(cause);
    }
}

// VPFDataFormatException
