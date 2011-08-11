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
package org.geotools.data.mysql;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.DataAccessFactory.Param;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Test Params used by PostgisDataStoreFactory.
 *
 * @author jgarnett, Refractions Research, Inc.
 * @author $Author: aaime $ (last modification)
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/mysql/src/test/java/org/geotools/data/mysql/MySQLDataStoreFactoryTest.java $
 * @version $Id: MySQLDataStoreFactoryTest.java 30703 2008-06-13 15:03:59Z acuster $
 */
public class MySQLDataStoreFactoryTest extends TestCase {
    static MySQLDataStoreFactory factory = new MySQLDataStoreFactory();
    Map local;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(MySQLDataStoreFactoryTest.class);

        return suite;
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        Properties resource = new Properties();
        resource.load(this.getClass().getResourceAsStream("fixture.properties"));
        this.local = resource;
    }

    public void testLocal() throws Exception {
        assertTrue("canProcess", factory.canProcess(local));

        try {
            DataStore temp = factory.createDataStore(local);
            assertNotNull("created", temp);
        } catch (DataSourceException expected) {
            expected.printStackTrace();
            assertEquals("Could not get connection", expected.getMessage());
        }
    }
    
    public void testNamespace() throws Exception {
        DataStore ds = factory.createDataStore(local);
        SimpleFeatureType ft = ds.getSchema("road");
        assertEquals(local.get("namespace"), ft.getName().getNamespaceURI());
    }
}
