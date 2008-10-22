/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.mif;

import java.io.IOException;
import java.net.URI;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataAccessFactory.Param;
import org.opengis.feature.simple.SimpleFeatureType;


/**
 * TestCase class for MIFDataStoreFactory
 *
 * @author Luca S. Percich, AMA-MI
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/mif/src/test/java/org/geotools/data/mif/MIFDataStoreFactoryTest.java $
 */
public class MIFDataStoreFactoryTest extends TestCase {
    private MIFDataStoreFactory dsFactory = null;
    private URI uri = null;

    /**
     */
    public static void main(java.lang.String[] args) throws Exception {
        junit.textui.TestRunner.run(new TestSuite(MIFDataStoreFactoryTest.class));
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        dsFactory = new MIFDataStoreFactory();
        uri = null;
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        dsFactory = null;
        super.tearDown();
    }

    /**
     */
    public void testGetDisplayName() {
        assertEquals("MIFDataStore", dsFactory.getDisplayName());
    }

    /**
     * Creates a MIFDataStore using DataStoreFinder
     */
    public void testDataStoreFinder() {
        DataStore ds = null;

        try {
            ds = DataStoreFinder.getDataStore(MIFTestUtils.getParams("mif",
                        MIFTestUtils.fileName(""), uri));
        } catch (IOException e) {
            fail(e.getMessage());
        }

        assertNotNull("Can't create datastore using DSFinder", ds);
        assertEquals("Bad class: " + ds.getClass(), true,
            ds.getClass() == MIFDataStore.class);
    }

    /**
     */
    public void testCreateDataStore() {
        DataStore ds = null;

        try {
            String strURI = "root-mifdatastore";
            uri = new URI(strURI);
            ds = dsFactory.createDataStore(MIFTestUtils.getParams("mif",
                        MIFTestUtils.fileName(""), uri));

            SimpleFeatureType ft = ds.getSchema("grafo");
            assertEquals("Bad URI", strURI, ft.getName().getNamespaceURI());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(ds);
        assertEquals(true, ds.getClass() == MIFDataStore.class);
    }

    /**
     */
    public void testGetDescription() {
        assertEquals("MapInfo MIF/MID format datastore",
            dsFactory.getDescription());
    }

    /**
     * Test the canProcess() method with different sets of (possibly wrong)
     * parameters
     */
    public void testCanProcessPath() {
        // Opens the test-data directory
        assertEquals(true,
            dsFactory.canProcess(MIFTestUtils.getParams("mif",
                    MIFTestUtils.fileName(""), uri)));
    }

    /**
     */
    public void testCanProcessWrongDBType() {
        // fails because dbtype != "mif"
        assertEquals(false,
            dsFactory.canProcess(MIFTestUtils.getParams("miffooobar",
                    MIFTestUtils.fileName(""), uri)));
    }

    /**
     */
    public void testCanProcessMIF() {
        // Opens a single mif file; works with or without extension, and regardless the extension's case.
        assertEquals(true,
            dsFactory.canProcess(MIFTestUtils.getParams("mif",
                    MIFTestUtils.fileName("grafo"), uri)));
        assertEquals(true,
            dsFactory.canProcess(MIFTestUtils.getParams("mif",
                    MIFTestUtils.fileName("grafo.MIF"), uri)));
        assertEquals(true,
            dsFactory.canProcess(MIFTestUtils.getParams("mif",
                    MIFTestUtils.fileName("grafo.mif"), uri)));
    }

    /**
     */
    public void testCanProcessWrongPath() {
        // Fails because an extension other than ".mif" was specified
        assertEquals(false,
            dsFactory.canProcess(MIFTestUtils.getParams("mif",
                    MIFTestUtils.fileName("grafo.zip"), uri)));

        // fails because the path is non-existent
        assertEquals(false,
            dsFactory.canProcess(MIFTestUtils.getParams("mif",
                    MIFTestUtils.fileName("some_non_existent_file"), uri)));
    }

    /**
     */
    public void testIsAvailable() {
        assertEquals(true, dsFactory.isAvailable());
    }

    /**
     */
    public void testGetParametersInfo() {
        Param[] pars = dsFactory.getParametersInfo();
        assertNotNull(pars);
        assertEquals(pars[3].key, MIFDataStore.PARAM_FIELDCASE);
        assertEquals(pars.length, 15);
    }

    /**
     * DOCUMENT ME!
     */
    public void testGetImplementationHints() {
        assertNotNull(dsFactory.getImplementationHints());
    }
}
