/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008-2011, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.data;

import junit.framework.TestCase;

import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.SampleDataAccess;
import org.geotools.data.SampleDataAccessFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

/**
 * Test for {@link SampleDataAccessFactory}.
 * 
 * @author Ben Caradoc-Davies (CSIRO Earth Science and Resource Engineering)
 * @version $Id: SampleDataAccessFactoryTest.java 37417 2011-06-13 04:40:41Z bencaradocdavies $
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/extension/app-schema/sample-data-access/src/test/java/org/geotools/data/SampleDataAccessFactoryTest.java $
 * @since 2.6
 */
public class SampleDataAccessFactoryTest extends TestCase {

    /**
     * Test that {@link DataAccessFinder} can find {@link SampleDataAccessFactory} and use it to
     * create a {@link SampleDataAccess}.
     * 
     * @throws Exception
     */
    public static void testFindSamplesDataAccessFactory() throws Exception {
        DataAccess<FeatureType, Feature> dataAccess = DataAccessFinder
                .getDataStore(SampleDataAccessFactory.PARAMS);
        assertNotNull(dataAccess);
        assertEquals(SampleDataAccess.class, dataAccess.getClass());
    }

}
