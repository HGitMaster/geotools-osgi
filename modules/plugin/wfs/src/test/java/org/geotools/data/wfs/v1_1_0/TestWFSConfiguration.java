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
package org.geotools.data.wfs.v1_1_0;

import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.xml.Configuration;

/**
 * A WFS configuration for unit test support, that resolves schemas to the test
 * data dir.
 * 
 * @author Gabriel Roldan
 * @version $Id: TestWFSConfiguration.java 31730 2008-10-29 13:29:21Z groldan $
 * @since 2.5.x
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/plugin/wfs/src/test/java/org/geotools/data/wfs/v1_1_0/TestWFSConfiguration.java $
 */
public class TestWFSConfiguration extends ApplicationSchemaConfiguration {

    private static Configuration wfsConfiguration;

    public TestWFSConfiguration(String namespace, String schemaLocation) {
        super(namespace, schemaLocation);
        synchronized (TestWFSConfiguration.class) {
            if (wfsConfiguration == null) {
                wfsConfiguration = new WFSConfiguration();
            }
        }
        addDependency(wfsConfiguration);
    }

}
