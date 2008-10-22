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
package org.geotools.data.gml;

import java.util.HashMap;
import java.util.Map;


/**
 * Tests that the GMLDatastoreFactory will correctly create {@link
 * GMLDataStore} and {@link FileGMLDataStore} objects
 *
 * @author Jesse
 */
public class GMLDataStoreFactoryTest extends AbstractGMLTestCase {
    private GMLDataStoreFactory factory;

    protected void setUp() throws Exception {
    	super.setUp();
        factory = new GMLDataStoreFactory();
    }

    /**
     * Test method for {@link
     * org.geotools.data.gml.GMLDataStoreFactory#createDataStore(java.util.Map)}.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testCreateDataStoreMapFile() throws Exception {
        Map params = new HashMap();
		params.put(GMLDataStoreFactory.URLP.key,
            gmlFile.toURL());

        FileGMLDataStore ds = (FileGMLDataStore) factory
            .createDataStore(params);
        assertEquals(1, ds.getTypeNames().length);
        assertSame(ds, factory.createDataStore(params));
    }

    public void testCreateDataStoreMapDirectory() throws Exception {
        Map params = new HashMap();
		params.put(GMLDataStoreFactory.URLP.key, gmlFile.getParentFile().toURL());

        GMLDataStore ds = (GMLDataStore) factory.createDataStore(params);
        assertEquals(1, ds.getTypeNames().length);
        assertSame(ds, factory.createDataStore(params));
    }

    /**
     * Test method for {@link
     * org.geotools.data.gml.GMLDataStoreFactory#createNewDataStore(java.util.Map)}.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testCreateNewDataStoreFile() throws Exception {
        Map params = new HashMap();
        params.put(GMLDataStoreFactory.URLP.key,
        		gmlFile.toURL());

        FileGMLDataStore ds = (FileGMLDataStore) factory.createNewDataStore(params);
        assertEquals(1, ds.getTypeNames().length);
        assertNotSame(ds, factory.createNewDataStore(params));
    }

    public void testCreateNewDataStoreDirectory() throws Exception {
        Map params = new HashMap();
        params.put(GMLDataStoreFactory.URLP.key, gmlFile.getParentFile().toURL());

        GMLDataStore ds = (GMLDataStore) factory.createNewDataStore(params);
        assertEquals(1, ds.getTypeNames().length);
        assertNotSame(ds, factory.createNewDataStore(params));
    }
}
