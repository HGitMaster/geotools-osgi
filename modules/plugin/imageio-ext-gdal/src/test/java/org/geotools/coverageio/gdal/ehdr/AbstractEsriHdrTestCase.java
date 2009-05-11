/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.coverageio.gdal.ehdr;

import java.util.logging.Logger;

import org.geotools.coverageio.gdal.AbstractGDALBasedTestCase;


/**
 * @author Alex Petkov, Missoula Fire Sciences Laboratory
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 *
 * Base EHdr testing class.
 */
public abstract class AbstractEsriHdrTestCase extends AbstractGDALBasedTestCase {
    protected final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(
            "org.geotools.coverageio.gdal.ehdr");

    public AbstractEsriHdrTestCase(String name) {
        super(name, "EHdr", new EsriHdrFormatFactory());
    }
    
    protected void setUp() throws Exception {
        super.setUp();

    }
}
