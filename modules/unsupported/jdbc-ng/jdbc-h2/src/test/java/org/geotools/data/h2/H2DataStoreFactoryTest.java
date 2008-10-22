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
package org.geotools.data.h2;

import java.util.HashMap;

import junit.framework.TestCase;

import org.geotools.jdbc.JDBCDataStoreFactory;


public class H2DataStoreFactoryTest extends TestCase {
    H2DataStoreFactory factory;

    protected void setUp() throws Exception {
        factory = new H2DataStoreFactory();
    }

    public void testCanProcess() throws Exception {
        HashMap params = new HashMap();
        assertFalse(factory.canProcess(params));

        params.put(JDBCDataStoreFactory.NAMESPACE.key, "http://www.geotools.org/test");
        params.put(JDBCDataStoreFactory.DATABASE.key, "geotools");
        params.put(JDBCDataStoreFactory.DBTYPE.key, "h2");

        assertTrue(factory.canProcess(params));
    }
}
