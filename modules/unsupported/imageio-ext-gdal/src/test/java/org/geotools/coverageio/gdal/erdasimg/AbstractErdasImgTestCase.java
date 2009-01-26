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
package org.geotools.coverageio.gdal.erdasimg;

import java.io.File;
import java.util.logging.Logger;

import org.geotools.coverageio.gdal.AbstractGDALBasedTestCase;
import org.geotools.test.TestData;


/**
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 *
 * Base ErdasImg testing class.
 */
public abstract class AbstractErdasImgTestCase extends AbstractGDALBasedTestCase {
    protected final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(
            "org.geotools.coverageio.gdal.erdasimg");

    public AbstractErdasImgTestCase(String name) {
        super(name, "ErdasImagine", new ErdasImgFormatFactory());
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        File file = TestData.file(this, "test.zip");
        assertTrue(file.exists());

        // unzip it
        TestData.unzipFile(this, "test.zip");
    }
}
