package org.geotools.data.wfs.v1_1_0;
import static org.geotools.data.wfs.protocol.http.HttpMethod.GET;
import static org.geotools.data.wfs.protocol.http.HttpMethod.POST;
import static org.geotools.data.wfs.protocol.wfs.WFSOperationType.DESCRIBE_FEATURETYPE;
import static org.geotools.data.wfs.v1_1_0.DataTestSupport.CUBEWERX_GOVUNITCE;
import static org.geotools.data.wfs.v1_1_0.DataTestSupport.CUBEWERX_ROADSEG;
import static org.geotools.data.wfs.v1_1_0.DataTestSupport.GEOS_ARCHSITES;
import static org.geotools.data.wfs.v1_1_0.DataTestSupport.GEOS_POI;
import static org.geotools.data.wfs.v1_1_0.DataTestSupport.GEOS_ROADS;
import static org.geotools.data.wfs.v1_1_0.DataTestSupport.GEOS_STATES;
import static org.geotools.data.wfs.v1_1_0.DataTestSupport.GEOS_TASMANIA_CITIES;
import static org.geotools.data.wfs.v1_1_0.DataTestSupport.GEOS_TIGER_ROADS;
import static org.geotools.data.wfs.v1_1_0.DataTestSupport.createProtocolHandler;
import static org.geotools.data.wfs.v1_1_0.DataTestSupport.protocolHandler;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.data.wfs.protocol.http.DefaultHTTPProtocol;
import org.geotools.data.wfs.protocol.http.HTTPProtocol;
import org.geotools.data.wfs.protocol.http.HTTPResponse;
import org.geotools.data.wfs.protocol.http.HttpMethod;
import org.geotools.data.wfs.protocol.wfs.Version;
import org.geotools.data.wfs.protocol.wfs.WFSResponse;
import org.geotools.data.wfs.v1_1_0.DataTestSupport.TestHttpResponse;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test suite for {@link WFS_1_1_0_Protocol}
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.6.x
 */
public class WFS_1_1_0_ProtocolTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for
     * {@link WFS_1_1_0_Protocol#WFS_1_1_0_Protocol(java.io.InputStream, org.geotools.data.wfs.protocol.http.HTTPProtocol)}
     * .
     * 
     * @throws IOException
     */
    @SuppressWarnings("nls")
    @Test
    public void testWFS_1_1_0_Protocol() throws IOException {
        try {
            createProtocolHandler(GEOS_STATES.SCHEMA);
            fail("Excpected IOException as a capabilities document was not provided");
        } catch (IOException e) {
            assertTrue(true);
        }
        try {
            InputStream badData = new ByteArrayInputStream(new byte[1024]);
            HTTPProtocol connFac = new DefaultHTTPProtocol();
            protocolHandler = new WFS_1_1_0_Protocol(badData, connFac);
            fail("Excpected IOException as a capabilities document was not provided");
        } catch (IOException e) {
            assertTrue(true);
        }

        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        assertNotNull(protocolHandler);
        assertNotNull(((WFS_1_1_0_Protocol) protocolHandler).capabilities);

    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getServiceVersion()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetServiceVersion() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        assertSame(Version.v1_1_0, protocolHandler.getServiceVersion());
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getServiceTitle()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetServiceTitle() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        assertEquals("My GeoServer WFS", protocolHandler.getServiceTitle());
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getServiceAbstract()} .
     * 
     * @throws IOException
     */
    @Test
    public void testGetServiceAbstract() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        assertEquals("This is a description of your Web Feature Server.", protocolHandler
                .getServiceAbstract().trim());
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getServiceKeywords()} .
     * 
     * @throws IOException
     */
    @Test
    public void testGetServiceKeywords() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        Set<String> serviceKeywords = protocolHandler.getServiceKeywords();
        assertNotNull(serviceKeywords);
        assertEquals(3, serviceKeywords.size());
        assertTrue(serviceKeywords.contains("WFS"));
        assertTrue(serviceKeywords.contains("WMS"));
        assertTrue(serviceKeywords.contains("GEOSERVER"));
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getServiceProviderUri()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetServiceProviderUri() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        assertNotNull(protocolHandler.getServiceProviderUri());
        assertEquals("http://www.geoserver.org", protocolHandler.getServiceProviderUri().toString());
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getSupportedGetFeatureOutputFormats()} .
     * 
     * @throws IOException
     */
    @Test
    public void testGetSupportedGetFeatureOutputFormats() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        Set<String> supportedOutputFormats = protocolHandler.getSupportedGetFeatureOutputFormats();
        assertNotNull(supportedOutputFormats);
        assertEquals(2, supportedOutputFormats.size()); // should be 2 once GEOT-2096 is fixed

        assertTrue(supportedOutputFormats.contains("text/gml; subtype=gml/3.1.1"));
        assertTrue(supportedOutputFormats.contains("text/xml; subtype=gml/2.1.2"));
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getSupportedOutputFormats(java.lang.String)} .
     * 
     * @throws IOException
     */
    @Test
    public void testGetSupportedOutputFormatsByFeatureType() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        Set<String> archSitesOutputFormats = protocolHandler
                .getSupportedOutputFormats(GEOS_ARCHSITES.FEATURETYPENAME);
        assertNotNull(archSitesOutputFormats);
        assertEquals(8, archSitesOutputFormats.size());

        assertTrue(archSitesOutputFormats.contains("GML2"));
        assertTrue(archSitesOutputFormats.contains("text/xml; subtype=gml/2.1.2"));
        assertTrue(archSitesOutputFormats.contains("GML2-GZIP"));
        assertTrue(archSitesOutputFormats.contains("text/xml; subtype=gml/3.1.1"));
        assertTrue(archSitesOutputFormats.contains("gml3"));
        assertTrue(archSitesOutputFormats.contains("SHAPE-ZIP"));
        assertTrue(archSitesOutputFormats.contains("json"));
        assertTrue(archSitesOutputFormats.contains("text/gml; subtype=gml/3.1.1"));
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getFeatureTypeNames()}.
     * 
     * @throws IOException
     */
    @SuppressWarnings("nls")
    @Test
    public void testGetFeatureTypeNames() throws IOException {

        // test against a geoserver capabilities
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        Set<QName> featureTypeNames = protocolHandler.getFeatureTypeNames();
        assertEquals(6, featureTypeNames.size());

        for (QName name : featureTypeNames) {
            assertFalse(name.toString(), XMLConstants.DEFAULT_NS_PREFIX.equals(name.getPrefix()));
        }
        assertTrue(featureTypeNames.contains(GEOS_ARCHSITES.TYPENAME));
        assertTrue(featureTypeNames.contains(GEOS_POI.TYPENAME));
        assertTrue(featureTypeNames.contains(GEOS_ROADS.TYPENAME));
        assertTrue(featureTypeNames.contains(GEOS_STATES.TYPENAME));
        assertTrue(featureTypeNames.contains(GEOS_TASMANIA_CITIES.TYPENAME));
        assertTrue(featureTypeNames.contains(GEOS_TIGER_ROADS.TYPENAME));

        // test against a cubewerx capabilities
        createProtocolHandler(CUBEWERX_GOVUNITCE.CAPABILITIES);
        featureTypeNames = protocolHandler.getFeatureTypeNames();
        // there are 14 featuretypes in the capabilities document
        assertEquals(14, featureTypeNames.size());

        for (QName name : featureTypeNames) {
            assertFalse(name.toString(), XMLConstants.DEFAULT_NS_PREFIX.equals(name.getPrefix()));
        }
        assertTrue(featureTypeNames.contains(CUBEWERX_GOVUNITCE.TYPENAME));
        assertTrue(featureTypeNames.contains(CUBEWERX_ROADSEG.TYPENAME));
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getFeatureTypeName(java.lang.String)} .
     * 
     * @throws IOException
     */
    @SuppressWarnings("nls")
    @Test
    public void testGetFeatureTypeNameGeoServer() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);

        try {
            protocolHandler.getFeatureTypeName("nonExistentTypeName");
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        // test against a geoserver capabilities
        assertEquals(GEOS_ARCHSITES.TYPENAME, protocolHandler
                .getFeatureTypeName(GEOS_ARCHSITES.FEATURETYPENAME));
        assertEquals(GEOS_POI.TYPENAME, protocolHandler
                .getFeatureTypeName(GEOS_POI.FEATURETYPENAME));
        assertEquals(GEOS_ROADS.TYPENAME, protocolHandler
                .getFeatureTypeName(GEOS_ROADS.FEATURETYPENAME));
        assertEquals(GEOS_STATES.TYPENAME, protocolHandler
                .getFeatureTypeName(GEOS_STATES.FEATURETYPENAME));
        assertEquals(GEOS_TASMANIA_CITIES.TYPENAME, protocolHandler
                .getFeatureTypeName(GEOS_TASMANIA_CITIES.FEATURETYPENAME));
        assertEquals(GEOS_TIGER_ROADS.TYPENAME, protocolHandler
                .getFeatureTypeName(GEOS_TIGER_ROADS.FEATURETYPENAME));

        // test against a cubewerx capabilities
        createProtocolHandler(CUBEWERX_GOVUNITCE.CAPABILITIES);

        assertEquals(CUBEWERX_GOVUNITCE.TYPENAME, protocolHandler
                .getFeatureTypeName(CUBEWERX_GOVUNITCE.FEATURETYPENAME));
        assertEquals(CUBEWERX_ROADSEG.TYPENAME, protocolHandler
                .getFeatureTypeName(CUBEWERX_ROADSEG.FEATURETYPENAME));

    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getFilterCapabilities()}.
     * 
     * @throws IOException
     */
    @Test
    public void testGetFilterCapabilities() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        try {
            protocolHandler.getFilterCapabilities();
            fail("expected 'not yet implemented'... he");
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
            System.out
                    .println("testGetFilterCapabilities(): Remember to implement getFilterCapabilities()!!!");
        }
    }

    /**
     * Test method for
     * {@link WFS_1_1_0_Protocol#supportsOperation(org.geotools.data.wfs.protocol.wfs.WFSOperationType, org.geotools.data.wfs.protocol.http.HttpMethod)}
     * .
     * 
     * @throws IOException
     */
    @Test
    public void testSupportsOperation() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        assertTrue(protocolHandler.supportsOperation(DESCRIBE_FEATURETYPE, GET));
        // post was deliberately left off on the test capabilities file
        assertFalse(protocolHandler.supportsOperation(DESCRIBE_FEATURETYPE, POST));

        createProtocolHandler(CUBEWERX_GOVUNITCE.CAPABILITIES);
        assertTrue(protocolHandler.supportsOperation(DESCRIBE_FEATURETYPE, GET));
        assertTrue(protocolHandler.supportsOperation(DESCRIBE_FEATURETYPE, POST));
    }

    /**
     * Test method for
     * {@link WFS_1_1_0_Protocol#getOperationURL(org.geotools.data.wfs.protocol.wfs.WFSOperationType, org.geotools.data.wfs.protocol.http.HttpMethod)}
     * .
     * 
     * @throws IOException
     */
    @SuppressWarnings("nls")
    @Test
    public void testGetOperationURL() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        URL operationURL = protocolHandler.getOperationURL(DESCRIBE_FEATURETYPE, GET);
        assertNotNull(operationURL);
        assertEquals("http://localhost:8080/geoserver/wfs?", operationURL.toExternalForm());
        // post was deliberately left off on the test capabilities file
        assertNull(protocolHandler.getOperationURL(DESCRIBE_FEATURETYPE, POST));
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getFeatureTypeTitle(java.lang.String)} .
     * 
     * @throws IOException
     */
    @SuppressWarnings("nls")
    @Test
    public void testGetFeatureTypeTitle() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        assertEquals("archsites_Type", protocolHandler
                .getFeatureTypeTitle(GEOS_ARCHSITES.FEATURETYPENAME));

        createProtocolHandler(CUBEWERX_GOVUNITCE.CAPABILITIES);
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getFeatureTypeAbstract(java.lang.String)} .
     * 
     * @throws IOException
     */
    @SuppressWarnings("nls")
    @Test
    public void testGetFeatureTypeAbstract() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        assertEquals("Generated from sfArchsites", protocolHandler
                .getFeatureTypeAbstract(GEOS_ARCHSITES.FEATURETYPENAME));

        createProtocolHandler(CUBEWERX_GOVUNITCE.CAPABILITIES);
        assertNull(protocolHandler.getFeatureTypeAbstract(CUBEWERX_GOVUNITCE.FEATURETYPENAME));

        try {
            protocolHandler.getFeatureTypeAbstract("nonExistentTypeName");
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getFeatureTypeWGS84Bounds(java.lang.String)} .
     * 
     * @throws IOException
     */
    @SuppressWarnings("nls")
    @Test
    public void testGetFeatureTypeWGS84Bounds() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        try {
            protocolHandler.getFeatureTypeAbstract("nonExistentTypeName");
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        ReferencedEnvelope wgs84Bounds = protocolHandler
                .getFeatureTypeWGS84Bounds(GEOS_ARCHSITES.FEATURETYPENAME);

        assertNotNull(wgs84Bounds);
        assertSame(DefaultGeographicCRS.WGS84, wgs84Bounds.getCoordinateReferenceSystem());
        assertEquals(-103D, wgs84Bounds.getMinX(), 1.0e-3);
        assertEquals(44D, wgs84Bounds.getMinY(), 1.0e-3);
        assertEquals(-102D, wgs84Bounds.getMaxX(), 1.0e-3);
        assertEquals(45D, wgs84Bounds.getMaxY(), 1.0e-3);

        createProtocolHandler(CUBEWERX_GOVUNITCE.CAPABILITIES);
        assertNotNull(protocolHandler.getFeatureTypeWGS84Bounds(CUBEWERX_GOVUNITCE.FEATURETYPENAME));
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getDefaultCRS(java.lang.String)}.
     * 
     * @throws IOException
     */
    @SuppressWarnings("nls")
    @Test
    public void testGetDefaultCRS() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        try {
            protocolHandler.getDefaultCRS("nonExistentTypeName");
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        assertEquals("EPSG:26713", protocolHandler.getDefaultCRS(GEOS_ARCHSITES.FEATURETYPENAME));
        assertEquals("EPSG:4326", protocolHandler.getDefaultCRS(GEOS_STATES.FEATURETYPENAME));
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getSupportedCRSIdentifiers(java.lang.String)} .
     * 
     * @throws IOException
     */
    @SuppressWarnings("nls")
    @Test
    public void testGetSupportedCRSIdentifiers() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        try {
            protocolHandler.getSupportedCRSIdentifiers("nonExistentTypeName");
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        Set<String> supportedCRSs;
        supportedCRSs = protocolHandler.getSupportedCRSIdentifiers(GEOS_ARCHSITES.FEATURETYPENAME);

        // capabilities doesn't set other crs's for this feature type than the default one...
        assertNotNull(supportedCRSs);
        assertEquals(1, supportedCRSs.size());
        assertTrue(supportedCRSs.contains("EPSG:26713"));

        createProtocolHandler(CUBEWERX_GOVUNITCE.CAPABILITIES);
        supportedCRSs = protocolHandler
                .getSupportedCRSIdentifiers(CUBEWERX_GOVUNITCE.FEATURETYPENAME);
        // capabilities defines more crs's for this ftype
        assertNotNull(supportedCRSs);
        assertEquals(2, supportedCRSs.size());
        assertTrue(supportedCRSs.contains("EPSG:4269"));
        assertTrue(supportedCRSs.contains("EPSG:4326"));
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getFeatureTypeKeywords(java.lang.String)} .
     * 
     * @throws IOException
     */
    @SuppressWarnings("nls")
    @Test
    public void testGetFeatureTypeKeywords() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        try {
            protocolHandler.getFeatureTypeKeywords("nonExistentTypeName");
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        Set<String> keywords;
        keywords = protocolHandler.getFeatureTypeKeywords(GEOS_ARCHSITES.FEATURETYPENAME);

        assertNotNull(keywords);
        assertEquals(1, keywords.size());
        assertTrue(keywords.contains("archsites sfArchsites"));
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getDescribeFeatureTypeURLGet(java.lang.String)} .
     * 
     * @throws IOException
     */
    @SuppressWarnings("nls")
    @Test
    public void testGetDescribeFeatureTypeURLGet() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        try {
            protocolHandler.getDescribeFeatureTypeURLGet("nonExistentTypeName");
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        URL url;
        url = protocolHandler.getDescribeFeatureTypeURLGet(GEOS_ARCHSITES.FEATURETYPENAME);
        assertNotNull(url);
        String externalForm = url.toExternalForm();
        externalForm = URLDecoder.decode(externalForm, "UTF-8");

        assertTrue(externalForm.startsWith("http://localhost:8080/geoserver/wfs?"));
        assertTrue(externalForm.contains("REQUEST=DescribeFeatureType"));
        assertTrue(externalForm.contains("TYPENAME=sf:archsites"));
        assertTrue(externalForm.contains("VERSION=1.1.0"));
        assertTrue(externalForm.contains("SERVICE=WFS"));
        assertTrue(externalForm.contains("NAMESPACE=xmlns(sf=http://www.openplans.org/spearfish)"));
        assertTrue(externalForm.contains("OUTPUTFORMAT=text/xml; subtype=gml/3.1.1"));
    }

    /**
     * Test method for
     * {@link WFS_1_1_0_Protocol#describeFeatureType(java.lang.String, java.lang.String)} .
     * 
     * @throws IOException
     */
    @SuppressWarnings("nls")
    @Test
    public void testDescribeFeatureType_HTTP_GET() throws IOException {
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES);
        try {
            protocolHandler.describeFeatureType("nonExistentTypeName",
                    "text/xml; subtype=gml/3.1.1", HttpMethod.GET);
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        HTTPProtocol mockHttp = new DefaultHTTPProtocol() {
            @Override
            public HTTPResponse issueGet(final URL baseUrl, final Map<String, String> kvp)
                    throws IOException {
                assertNotNull(baseUrl);
                String externalForm = baseUrl.toExternalForm();
                externalForm = URLDecoder.decode(externalForm, "UTF-8");

                assertTrue(externalForm.startsWith("http://localhost:8080/geoserver/wfs?"));
                assertTrue(externalForm.contains("REQUEST=DescribeFeatureType"));
                assertTrue(externalForm.contains("TYPENAME=sf:archsites"));
                assertTrue(externalForm.contains("VERSION=1.1.0"));
                assertTrue(externalForm.contains("SERVICE=WFS"));
                assertTrue(externalForm
                        .contains("NAMESPACE=xmlns(sf=http://www.openplans.org/spearfish)"));
                assertTrue(externalForm.contains("OUTPUTFORMAT=text/xml; subtype=gml/3.1.1"));

                HTTPResponse httpResponse = new TestHttpResponse("text/xml; subtype=gml/3.1.1",
                        null, "mock-content");
                return httpResponse;
            }
        };
        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES, mockHttp);

        WFSResponse wfsResponse;

        wfsResponse = protocolHandler.describeFeatureType(GEOS_ARCHSITES.FEATURETYPENAME,
                "text/xml; subtype=gml/3.1.1", HttpMethod.GET);
        assertNotNull(wfsResponse);
        assertEquals(Charset.forName("UTF-8"), wfsResponse.getCharacterEncoding());
        assertEquals("text/xml; subtype=gml/3.1.1", wfsResponse.getContentType());
        assertNotNull(wfsResponse.getInputStream());
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getFeatureHits(org.geotools.data.Query)} .
     * <p>
     * If the server returns a FeatureCollection with numberOfFeatures=N attribute, {@code
     * getFeatureHits} shall return N
     * </p>
     * 
     * @throws IOException
     */
    @Test
    public void testGetFeatureHitsSupported() throws IOException {
        String responseContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<wfs:FeatureCollection numberOfFeatures=\"217\" timeStamp=\"2008-10-24T13:53:53.034-04:00\" "
                + "xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" "
                + "xmlns:wfs=\"http://www.opengis.net/wfs\" "
                + "xmlns:topp=\"http://www.openplans.org/topp\" "
                + "xmlns:seb=\"http://seb.com\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns:ows=\"http://www.opengis.net/ows\" "
                + "xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>";

        final TestHttpResponse response = new TestHttpResponse("text/xml; subtype=gml/3.1.1",
                "UTF-8", responseContent);

        HTTPProtocol mockHttp = new DefaultHTTPProtocol() {
            @Override
            public HTTPResponse issueGet(final URL baseUrl, final Map<String, String> kvp)
                    throws IOException {
                return response;
            }
        };

        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES, mockHttp);
        DefaultQuery query = new DefaultQuery(GEOS_ARCHSITES.FEATURETYPENAME);

        int featureHits = protocolHandler.getFeatureHits(query);
        assertEquals(217, featureHits);
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getFeatureHits(org.geotools.data.Query)} .
     * <p>
     * If the server returns an exception report, {@code getFeatureHits} shall throw an IOException
     * </p>
     * 
     * @throws IOException
     */
    // @Test
    public void testGetFeatureHitsException() throws IOException {
        String responseContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<ows:ExceptionReport version=\"1.0.0\" "
                + "xsi:schemaLocation=\"http://www.opengis.net/ows http://localhost:8080/geoserver/schemas/ows/1.0.0/owsExceptionReport.xsd\""
                + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ows=\"http://www.opengis.net/ows\">"
                + "  <ows:Exception exceptionCode=\"mockExceptionCode\" locator=\"mockLocatorName\">"
                + "    <ows:ExceptionText>Feature type sf:archsites2 unknown</ows:ExceptionText>"
                + "   <ows:ExceptionText>Details:</ows:ExceptionText>"
                + "   <ows:ExceptionText>mock exception report</ows:ExceptionText>"
                + " </ows:Exception></ows:ExceptionReport>";

        final TestHttpResponse response = new TestHttpResponse(
                "application/vnd.ogc.se_xml;chatset=UTF-8", "UTF-8", responseContent);

        HTTPProtocol mockHttp = new DefaultHTTPProtocol() {
            @Override
            public HTTPResponse issueGet(final URL baseUrl, final Map<String, String> kvp)
                    throws IOException {
                return response;
            }
        };

        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES, mockHttp);
        DefaultQuery query = new DefaultQuery(GEOS_ARCHSITES.FEATURETYPENAME);

        try {
            protocolHandler.getFeatureHits(query);
            fail("Expected IOException if the server returned an exception report");
        } catch (IOException e) {
            // make sure the error message propagates
            assertEquals("mock exception report", e.getMessage());
        }
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getFeatureHits(org.geotools.data.Query)} .
     * <p>
     * May the server not support resultType=hits even if declared in the capabilities document (eg.
     * CubeWerx) and hence return the FeatureCollection with full contents and no {@code
     * numberOfFeatures} attribute. In this case return -1.
     * </p>
     * 
     * @throws IOException
     */
    @Test
    public void testGetFeatureHitsNotSupported() throws IOException {
        String responseContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<wfs:FeatureCollection timeStamp=\"2008-10-24T13:53:53.034-04:00\" "
                + "xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" "
                + "xmlns:wfs=\"http://www.opengis.net/wfs\" "
                + "xmlns:topp=\"http://www.openplans.org/topp\" "
                + "xmlns:seb=\"http://seb.com\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns:ows=\"http://www.opengis.net/ows\" "
                + "xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>";

        final TestHttpResponse response = new TestHttpResponse("text/xml; subtype=gml/3.1.1",
                "UTF-8", responseContent);

        HTTPProtocol mockHttp = new DefaultHTTPProtocol() {
            @Override
            public HTTPResponse issueGet(final URL baseUrl, final Map<String, String> kvp)
                    throws IOException {
                return response;
            }
        };

        createProtocolHandler(GEOS_ARCHSITES.CAPABILITIES, mockHttp);
        DefaultQuery query = new DefaultQuery(GEOS_ARCHSITES.FEATURETYPENAME);

        int featureHits = protocolHandler.getFeatureHits(query);
        assertEquals(-1, featureHits);
    }

    /**
     * Test method for {@link WFS_1_1_0_Protocol#getFeature(Query, String, HttpMethod)} .
     */
    // @Test
    public void testGetFeature_GET() {
        fail("Not yet implemented");
    }

}
