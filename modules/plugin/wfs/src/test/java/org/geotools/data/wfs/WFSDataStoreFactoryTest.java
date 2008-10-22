/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.wfs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.geotools.test.TestData;
import org.geotools.wfs.protocol.ConnectionFactory;
import org.geotools.wfs.v_1_1_0.data.WFS_1_1_0_DataStore;

public class WFSDataStoreFactoryTest extends TestCase {

    private WFSDataStoreFactory dsf;

    private Map<String, Serializable> params;

    public WFSDataStoreFactoryTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dsf = new WFSDataStoreFactory();
        params = new HashMap<String, Serializable>();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        dsf = null;
        params = null;
    }

    public void testCanProcess() {
        // URL not set
        assertFalse(dsf.canProcess(params));

        params.put(WFSDataStoreFactory.URL.key,
                "http://someserver.example.org/wfs?request=GetCapabilities");
        assertTrue(dsf.canProcess(params));

        params.put(WFSDataStoreFactory.USERNAME.key, "groldan");
        assertFalse(dsf.canProcess(params));

        params.put(WFSDataStoreFactory.PASSWORD.key, "secret");
        assertTrue(dsf.canProcess(params));
    }

    public void testCreateDataStoreWFS_1_1_0() throws IOException {
        String capabilitiesFile;
        capabilitiesFile = "geoserver_capabilities_1_1_0.xml";
        testCreateDataStore_WFS_1_1_0(capabilitiesFile);

        capabilitiesFile = "deegree_capabilities_1_1_0.xml";
        testCreateDataStore_WFS_1_1_0(capabilitiesFile);
    }

    private void testCreateDataStore_WFS_1_1_0(final String capabilitiesFile) throws IOException {
        // override caps loading not to set up an http connection at all but to
        // load the test file
        final WFSDataStoreFactory dsf = new WFSDataStoreFactory() {
            @Override
            byte[] loadCapabilities(final URL capabilitiesUrl, final ConnectionFactory connectionFac)
                    throws IOException {
                InputStream in = capabilitiesUrl.openStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int aByte;
                while ((aByte = in.read()) != -1) {
                    out.write(aByte);
                }
                return out.toByteArray();
            }
        };
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        final URL capabilitiesUrl = TestData.getResource(this, capabilitiesFile);
        if(capabilitiesUrl == null){
            throw new IllegalArgumentException(capabilitiesFile + " not found");
        }
        params.put(WFSDataStoreFactory.URL.key, capabilitiesUrl);

        WFSDataStore dataStore = dsf.createDataStore(params);
        assertTrue(dataStore instanceof WFS_1_1_0_DataStore);
    }

}
