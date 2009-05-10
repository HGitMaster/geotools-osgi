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
package org.geotools.coverageio.gdal.mrsid;

import java.util.Iterator;

import junit.framework.TestCase;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;


/**
 * Class for testing availability of MrSID format factory
 *
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 */
public class ServiceTest extends AbstractMrSIDTestCase {
    public ServiceTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(ServiceTest.class);
    }

    public void testIsAvailable() throws NoSuchAuthorityCodeException, FactoryException {
        if (!testingEnabled()) {
            return;
        }
        GridFormatFinder.scanForPlugins();

        Iterator list = GridFormatFinder.getAvailableFormats().iterator();
        boolean found = false;
        GridFormatFactorySpi fac = null;

        while (list.hasNext()) {
            fac = (GridFormatFactorySpi) list.next();

            if (fac instanceof MrSIDFormatFactory) {
                found = true;

                break;
            }
        }

        assertTrue("MrSIDFormatFactory not registered", found);
        assertTrue("MrSIDFormatFactory not available", fac.isAvailable());
        assertNotNull(new MrSIDFormatFactory().createFormat());
    }
}
