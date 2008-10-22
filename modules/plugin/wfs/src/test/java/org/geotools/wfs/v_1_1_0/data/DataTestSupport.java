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
package org.geotools.wfs.v_1_1_0.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.geotools.test.TestData;
import org.geotools.wfs.protocol.ConnectionFactory;
import org.geotools.wfs.protocol.DefaultConnectionFactory;

public abstract class DataTestSupport extends TestCase {

    /**
     * A class holding the type name and test data location for a feature type
     */
    public static class TestDataType {
        /**
         * Location of a test data capabilities file
         */
        final String CAPABILITIES;

        /**
         * Location of a test DescribeFeatureType response for the given feature
         * type
         */
        final String SCHEMA;

        /**
         * The type name, including namespace
         */
        final QName TYPENAME;

        /**
         * The type name as referred in the capabilities (ej, "topp:states")
         */
        final String FEATURETYPENAME;

        /**
         * The FeatureType CRS as declared in the capabilities
         */
        final String CRS;

        /**
         * Location of a sample GetFeature response for this feature type
         */
        final String DATA;

        /**
         * 
         * @param folder
         *            the folder name under {@code test-data} where the test
         *            files for this feature type are stored
         * @param qName
         *            the qualified type name (ns + local name)
         * @param featureTypeName
         *            the name as stated in the capabilities
         * @param crs
         *            the default feature type CRS as stated in the capabilities
         */
        TestDataType(final String folder, final QName qName, final String featureTypeName,
                final String crs) {
            TYPENAME = qName;
            FEATURETYPENAME = featureTypeName;
            CRS = crs;
            CAPABILITIES = folder + "/GetCapabilities_1_1_0.xml";
            SCHEMA = folder + "/DescribeFeatureType_" + qName.getLocalPart() + ".xsd";
            DATA = folder + "/GetFeature_" + qName.getLocalPart() + ".xml";

            checkResource(CAPABILITIES);
            checkResource(SCHEMA);
            checkResource(DATA);
        }

        private void checkResource(String resource) {
            try {
                TestData.url(this, resource);
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }

    }

    public static final TestDataType GEOS_ARCHSITES = new TestDataType("geoserver", new QName(
            "http://www.openplans.org/spearfish", "archsites"), "sf:archsites", "EPSG:26713");

    public static final TestDataType GEOS_POI = new TestDataType("geoserver", new QName(
            "http://www.census.gov", "poi"), "tiger:poi", "EPSG:4326");

    public static final TestDataType GEOS_ROADS = new TestDataType("geoserver", new QName(
            "http://www.openplans.org/spearfish", "roads"), "sf:roads", "EPSG:26713");
    
    public static final TestDataType GEOS_STATES = new TestDataType("geoserver", new QName(
            "http://www.openplans.org/topp", "states"), "topp:states", "EPSG:4326");

    public static final TestDataType GEOS_TASMANIA_CITIES = new TestDataType("geoserver", new QName(
            "http://www.openplans.org/topp", "tasmania_cities"), "topp:tasmania_cities", "EPSG:4326");
    
    public static final TestDataType CUBEWERX_GOVUNITCE = new TestDataType("CubeWerx_nsdi",
            new QName("http://www.fgdc.gov/framework/073004/gubs", "GovernmentalUnitCE"),
            "gubs:GovernmentalUnitCE", "EPSG:4269");

    public static final TestDataType CUBEWERX_ROADSEG = new TestDataType("CubeWerx_nsdi",
            new QName("http://www.fgdc.gov/framework/073004/transportation", "RoadSeg"),
            "trans:RoadSeg", "EPSG:4269");

    protected WFS110ProtocolHandler protocolHandler;

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        protocolHandler = null;
    }

    /**
     * Creates the test {@link #protocolHandler} with a default connection
     * factory that parses the capabilities object from the test xml file
     * pointed out by {@code capabilitiesFileName}
     * <p>
     * Tests methods call this one to set up a protocolHandler to test
     * </p>
     * 
     * @param capabilitiesFileName
     *            the relative path under {@code test-data} for the file
     *            containing the WFS_Capabilities document.
     * @throws IOException
     */
    protected void createProtocolHandler(String capabilitiesFileName) throws IOException {
        ConnectionFactory connFac = new DefaultConnectionFactory();
        createProtocolHandler(capabilitiesFileName, connFac);
    }

    /**
     * Creates the test {@link #protocolHandler} with the provided connection
     * factory that parses the capabilities object from the test xml file
     * pointed out by {@code capabilitiesFileName}
     * <p>
     * Tests methods call this one to set up a protocolHandler to test
     * </p>
     * 
     * @param capabilitiesFileName
     *            the relative path under {@code test-data} for the file
     *            containing the WFS_Capabilities document.
     * @throws IOException
     */
    protected void createProtocolHandler(String capabilitiesFileName, ConnectionFactory connFac)
            throws IOException {
        InputStream stream = TestData.openStream(this, capabilitiesFileName);
        protocolHandler = new WFS110ProtocolHandler(stream, connFac, Integer.valueOf(0));
    }

}
