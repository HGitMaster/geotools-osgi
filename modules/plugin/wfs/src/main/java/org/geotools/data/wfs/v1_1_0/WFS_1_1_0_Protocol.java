package org.geotools.data.wfs.v1_1_0;

import static org.geotools.data.wfs.protocol.http.HttpMethod.GET;
import static org.geotools.data.wfs.protocol.http.HttpMethod.POST;
import static org.geotools.data.wfs.protocol.wfs.WFSOperationType.DESCRIBE_FEATURETYPE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.ows10.DCPType;
import net.opengis.ows10.DomainType;
import net.opengis.ows10.KeywordsType;
import net.opengis.ows10.OnlineResourceType;
import net.opengis.ows10.OperationType;
import net.opengis.ows10.OperationsMetadataType;
import net.opengis.ows10.RequestMethodType;
import net.opengis.ows10.ServiceProviderType;
import net.opengis.ows10.WGS84BoundingBoxType;
import net.opengis.wfs.BaseRequestType;
import net.opengis.wfs.FeatureTypeType;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.OutputFormatListType;
import net.opengis.wfs.QueryType;
import net.opengis.wfs.WFSCapabilitiesType;

import org.geotools.data.DataSourceException;
import org.geotools.data.Query;
import org.geotools.data.wfs.protocol.http.HTTPProtocol;
import org.geotools.data.wfs.protocol.http.HTTPResponse;
import org.geotools.data.wfs.protocol.http.HttpMethod;
import org.geotools.data.wfs.protocol.wfs.Version;
import org.geotools.data.wfs.protocol.wfs.WFSOperationType;
import org.geotools.data.wfs.protocol.wfs.WFSProtocol;
import org.geotools.data.wfs.protocol.wfs.WFSResponse;
import org.geotools.filter.v1_1.OGC;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gml2.bindings.GML2EncodingUtils;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.capability.FilterCapabilities;
import org.opengis.filter.identity.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * {@link WFSProtocol} implementation to talk to a WFS 1.1.0 server leveraging the GeoTools {@code
 * xml-xsd} subsystem for schema assisted parsing and encoding of WFS requests and responses.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.6.x
 */
@SuppressWarnings({"unchecked", "nls"})
public class WFS_1_1_0_Protocol implements WFSProtocol {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");

    private static final String DEFAULT_OUTPUT_FORMAT = "text/xml; subtype=gml/3.1.1";

    /**
     * Configuration used to encode filters
     */
    Configuration filterConfig;

    /**
     * WFS 1.1 configuration used for XML parsing and encoding
     */
    private static final WFSConfiguration configuration = new WFSConfiguration();

    /**
     * The WFS GetCapabilities document. Final by now, as we're not handling updatesequence, so will
     * not ask the server for an updated capabilities during the life-time of this datastore.
     */
    final WFSCapabilitiesType capabilities;

    /**
     * Per featuretype name Map of capabilities feature type information. Not to be used directly
     * but through {@link #getFeatureTypeInfo(String)}
     */
    private final Map<String, FeatureTypeType> typeInfos;

    private HTTPProtocol http;

    public WFS_1_1_0_Protocol( InputStream capabilitiesReader, HTTPProtocol http )
            throws IOException {
        this.capabilities = parseCapabilities(capabilitiesReader);
        this.http = http;
        this.typeInfos = new HashMap<String, FeatureTypeType>();
        filterConfig = new org.geotools.filter.v1_1.OGCConfiguration();

        final List<FeatureTypeType> ftypes = capabilities.getFeatureTypeList().getFeatureType();
        QName typeName;
        for( FeatureTypeType ftype : ftypes ) {
            typeName = ftype.getName();
            assert !("".equals(typeName.getPrefix()));
            String prefixedTypeName = typeName.getPrefix() + ":" + typeName.getLocalPart();
            typeInfos.put(prefixedTypeName, ftype);
        }
    }

    /**
     * @return {@code "text/xml; subtype=gml/3.1.1"}
     * @see WFSProtocol#getDefaultOutputFormat()
     */
    public String getDefaultOutputFormat() {
        return DEFAULT_OUTPUT_FORMAT;
    }

    /**
     * @return {@link Version#v1_1_0}
     * @see WFSProtocol#getServiceVersion()
     */
    public Version getServiceVersion() {
        return Version.v1_1_0;
    }

    /**
     * @see WFSProtocol#getServiceTitle()
     */
    public String getServiceTitle() {
        return capabilities.getServiceIdentification().getTitle();
    }

    /**
     * @see WFSProtocol#getServiceAbstract()
     */
    public String getServiceAbstract() {
        return capabilities.getServiceIdentification().getAbstract();
    }

    /**
     * @see WFSProtocol#getServiceKeywords()
     */
    public Set<String> getServiceKeywords() {
        List<KeywordsType> capsKeywords = capabilities.getServiceIdentification().getKeywords();
        return extractKeywords(capsKeywords);
    }

    /**
     * @see WFSProtocol#getServiceProviderUri()
     */
    public URI getServiceProviderUri() {
        ServiceProviderType serviceProvider = capabilities.getServiceProvider();
        if (serviceProvider == null) {
            return null;
        }
        OnlineResourceType providerSite = serviceProvider.getProviderSite();
        if (providerSite == null) {
            return null;
        }
        String href = providerSite.getHref();
        if (href == null) {
            return null;
        }
        try {
            return new URI(href);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * @see WFSProtocol#getSupportedGetFeatureOutputFormats()
     */
    public Set<String> getSupportedGetFeatureOutputFormats() {
        OperationType operationMetadata = getOperationMetadata(WFSOperationType.GET_FEATURE);
        List<DomainType> parameters = operationMetadata.getParameter();

        Set<String> outputFormats = new HashSet<String>();
        for( DomainType param : parameters ) {
            String paramName = param.getName();
            if ("outputFormat".equals(paramName)) {
                List value = param.getValue();
                outputFormats.addAll(value);
            }
        }
        return outputFormats;
    }

    /**
     * @see WFSProtocol#getSupportedOutputFormats(String)
     */
    public Set<String> getSupportedOutputFormats( String typeName ) {
        final Set<String> serviceOutputFormats = getSupportedGetFeatureOutputFormats();
        final FeatureTypeType typeInfo = getFeatureTypeInfo(typeName);
        final OutputFormatListType outputFormats = typeInfo.getOutputFormats();

        Set<String> ftypeFormats = new HashSet<String>();
        if (outputFormats != null) {
            List<String> ftypeDeclaredFormats = outputFormats.getFormat();
            ftypeFormats.addAll(ftypeDeclaredFormats);
        }

        ftypeFormats.addAll(serviceOutputFormats);
        return ftypeFormats;
    }

    /**
     * @see WFSProtocol#getFeatureTypeNames()
     */
    public Set<QName> getFeatureTypeNames() {
        Set<QName> typeNames = new HashSet<QName>();
        for( FeatureTypeType typeInfo : typeInfos.values() ) {
            QName name = typeInfo.getName();
            typeNames.add(name);
        }
        return typeNames;
    }

    /**
     * @see WFSProtocol#getFeatureTypeName(String)
     */
    public QName getFeatureTypeName( String typeName ) {
        FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        QName name = featureTypeInfo.getName();
        return name;
    }

    /**
     * @see WFSProtocol#getFilterCapabilities()
     */
    public FilterCapabilities getFilterCapabilities() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * @see WFSProtocol#supportsOperation(WFSOperationType, boolean)
     */
    public boolean supportsOperation( WFSOperationType operation, boolean post ) {
        HttpMethod method = post ? POST : GET;
        return null != getOperationURI(operation, method);
    }

    /**
     * @see WFSProtocol#getOperationURL(WFSOperationType, boolean)
     */
    public URL getOperationURL( WFSOperationType operation, boolean post ) {
        HttpMethod method = post ? POST : GET;
        String href = getOperationURI(operation, method);
        if (href != null) {
            try {
                return new URL(href);
            } catch (MalformedURLException e) {
                // Log error and let the search continue
                LOGGER.log(Level.INFO, "Malformed " + method + " URL for " + operation, e);
            }
        }
        return null;
    }

    /**
     * @see WFSProtocol#getFeatureTypeTitle(String)
     */
    public String getFeatureTypeTitle( String typeName ) {
        FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        return featureTypeInfo.getTitle();
    }

    /**
     * @see WFSProtocol#getFeatureTypeAbstract(String)
     */
    public String getFeatureTypeAbstract( String typeName ) {
        FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        return featureTypeInfo.getAbstract();
    }

    /**
     * @see WFSProtocol#getFeatureTypeWGS84Bounds(String)
     */
    public ReferencedEnvelope getFeatureTypeWGS84Bounds( String typeName ) {
        final FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        List<WGS84BoundingBoxType> bboxList = featureTypeInfo.getWGS84BoundingBox();
        if (bboxList != null && bboxList.size() > 0) {
            WGS84BoundingBoxType bboxType = bboxList.get(0);
            List lowerCorner = bboxType.getLowerCorner();
            List upperCorner = bboxType.getUpperCorner();
            double minLon = (Double) lowerCorner.get(0);
            double minLat = (Double) lowerCorner.get(1);
            double maxLon = (Double) upperCorner.get(0);
            double maxLat = (Double) upperCorner.get(1);

            ReferencedEnvelope latLonBounds = new ReferencedEnvelope(minLon, maxLon, minLat,
                    maxLat, DefaultGeographicCRS.WGS84);

            return latLonBounds;
        }
        throw new IllegalStateException(
                "The capabilities document does not supply the ows:WGS84BoundingBox element");
    }

    /**
     * @see WFSProtocol#getDefaultCRS(String)
     */
    public String getDefaultCRS( String typeName ) {
        FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        String defaultSRS = featureTypeInfo.getDefaultSRS();
        return defaultSRS;
    }

    /**
     * @see WFSProtocol#getSupportedCRSIdentifiers(String)
     */
    public Set<String> getSupportedCRSIdentifiers( String typeName ) {
        FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        // TODO: another wrong emf mapping: getOtherSRS():String? should be a list
        String defaultSRS = featureTypeInfo.getDefaultSRS();
        List<String> otherSRS = featureTypeInfo.getOtherSRS();

        Set<String> ftypeCrss = new HashSet<String>();
        ftypeCrss.add(defaultSRS);
        ftypeCrss.addAll(otherSRS);
        return ftypeCrss;
    }

    /**
     * @see WFSProtocol#getFeatureTypeKeywords(String)
     */
    public Set<String> getFeatureTypeKeywords( String typeName ) {
        FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        List<KeywordsType> ftKeywords = featureTypeInfo.getKeywords();
        Set<String> ftypeKeywords = extractKeywords(ftKeywords);
        return ftypeKeywords;
    }

    /**
     * @see WFSProtocol#getDescribeFeatureTypeURLGet(String)
     */
    public URL getDescribeFeatureTypeURLGet( String typeName ) {
        final String outputFormat = "text/xml; subtype=gml/3.1.1";
        return getDescribeFeatureTypeURLGet(typeName, outputFormat);
    }

    /**
     * @throws IOException
     * @see WFSProtocol#describeFeatureTypeGET(String, String)
     */
    public WFSResponse describeFeatureTypeGET( String typeName, String outputFormat )
            throws IOException {
        if (!supportsOperation(DESCRIBE_FEATURETYPE, false)) {
            throw new UnsupportedOperationException(
                    "The server does not support DescribeFeatureType for HTTP method GET");
        }

        URL url = getDescribeFeatureTypeURLGet(typeName, outputFormat);
        WFSResponse response = issueGetRequest(null, url, Collections.EMPTY_MAP);
        return response;
    }

    /**
     * @throws IOException
     * @see WFSProtocol#describeFeatureTypePOST(String, String)
     */
    public WFSResponse describeFeatureTypePOST( String typeName, String outputFormat )
            throws IOException {
        throw new UnsupportedOperationException("POST not implemented yet for DescribeFeatureType");

    }
    /**
     * @see WFSProtocol#getFeatureHits(Query)
     */
    public int getFeatureHits( Query query ) throws IOException {
        URL url = getOperationURL(WFSOperationType.GET_FEATURE, false);
        Map<String, String> getFeatureKvp = buildGetFeatureParametersForGet(query,
                DEFAULT_OUTPUT_FORMAT);

        getFeatureKvp.put("RESULTTYPE", "hits");
        WFSResponse response = issueGetRequest(null, url, getFeatureKvp);

        int featureCount = -1;

        final InputStream responseStream = response.getInputStream();
        try {
            XmlPullParser parser = null;
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);

            // parse root element
            parser = factory.newPullParser();
            parser.setInput(responseStream, "UTF-8");
            parser.nextTag();
            final String rootElementName = parser.getName();
            if ("FeatureCollection".equals(rootElementName)) {
                String numberOfFeatures = null;
                for( int i = 0; i < parser.getAttributeCount(); i++ ) {
                    if ("numberOfFeatures".equals(parser.getAttributeName(i))) {
                        numberOfFeatures = parser.getAttributeValue(i);
                        break;
                    }
                }
                // reset input stream
                parser.setInput(null);
                if (numberOfFeatures == null) {
                    LOGGER.finer("Response din't return numberOfFeatures");
                } else {
                    featureCount = Integer.valueOf(numberOfFeatures);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Error calculating query count", e);
        } finally {
            responseStream.close();
        }
        return featureCount;
    }

    /**
     * @see WFSProtocol#getFeatureGET(Query, String)
     */
    public WFSResponse getFeatureGET( final GetFeatureType request ) throws IOException {
        if (!supportsOperation(WFSOperationType.GET_FEATURE, false)) {
            throw new UnsupportedOperationException(
                    "The server does not support GetFeature for HTTP method GET");
        }
        URL url = getOperationURL(WFSOperationType.GET_FEATURE, false);
        Map<String, String> getFeatureKvp = buildGetFeatureParametersForGet(request);
        System.out.println(" > getFeatureGET: Request url: " + url + ". Parameters: " + getFeatureKvp);
        WFSResponse response = issueGetRequest(request, url, getFeatureKvp);

        return response;
    }

    /**
     * @see WFSProtocol#getFeatureGET(Query, String)
     */
    public WFSResponse getFeatureGET( Query query, String outputFormat ) throws IOException {
        if (!supportsOperation(WFSOperationType.GET_FEATURE, false)) {
            throw new UnsupportedOperationException(
                    "The server does not support GetFeature for HTTP method GET");
        }

        URL url = getOperationURL(WFSOperationType.GET_FEATURE, false);
        Map<String, String> getFeatureKvp = buildGetFeatureParametersForGet(query, outputFormat);

        WFSResponse response = issueGetRequest(null, url, getFeatureKvp);

        return response;
    }

    /**
     * @see WFSProtocol#getFeaturePOST(Query, String)
     */
    public WFSResponse getFeaturePOST( final GetFeatureType request ) throws IOException {
        throw new UnsupportedOperationException("POST not implemented yet for GetFeature");
    }

    /**
     * Returns the feature type metadata object parsed from the capabilities document for the given
     * {@code typeName}
     * <p>
     * NOTE: this method is package protected just to be also accessed by unit test.
     * </p>
     * 
     * @param typeName the typeName as stated in the capabilities {@code FeatureTypeList} to get the
     *        info for
     * @return the WFS capabilities metadata {@link FeatureTypeType metadata} for {@code typeName}
     * @throws IllegalArgumentException if {@code typeName} is not the name of a FeatureType stated
     *         in the capabilities document.
     */
    private FeatureTypeType getFeatureTypeInfo( final String typeName ) {
        if (!typeInfos.containsKey(typeName)) {
            throw new IllegalArgumentException("Type name not found: " + typeName);
        }
        return typeInfos.get(typeName);
    }

    private WFSCapabilitiesType parseCapabilities( InputStream capabilitiesReader )
            throws IOException {
        final Parser parser = new Parser(configuration);
        final Object parsed;
        try {
            parsed = parser.parse(capabilitiesReader);
        } catch (SAXException e) {
            throw new DataSourceException("Exception parsing WFS 1.1.0 capabilities", e);
        } catch (ParserConfigurationException e) {
            throw new DataSourceException("WFS 1.1.0 parsing configuration error", e);
        }
        if (parsed == null) {
            throw new DataSourceException("WFS 1.1.0 capabilities was not parsed");
        }
        if (!(parsed instanceof WFSCapabilitiesType)) {
            throw new DataSourceException("Expected WFS Capabilities, got " + parsed);
        }
        return (WFSCapabilitiesType) parsed;
    }

    private Set<String> extractKeywords( List<KeywordsType> keywordsList ) {
        Set<String> keywords = new HashSet<String>();
        for( KeywordsType keys : keywordsList ) {
            keywords.addAll(keys.getKeyword());
        }
        return keywords;
    }

    private OperationType getOperationMetadata( WFSOperationType operation ) {
        final OperationsMetadataType operationsMetadata = capabilities.getOperationsMetadata();
        final List<OperationType> operations = operationsMetadata.getOperation();
        final String expectedOperationName = operation.getName();
        for( OperationType operationType : operations ) {
            String operationName = operationType.getName();
            if (expectedOperationName.equalsIgnoreCase(operationName)) {
                return operationType;
            }
        }
        throw new NoSuchElementException("Operation metadata not found for "
                + expectedOperationName + " in the capabilities document");
    }

    private URL getDescribeFeatureTypeURLGet( String typeName, String outputFormat ) {
        final FeatureTypeType typeInfo = getFeatureTypeInfo(typeName);

        final URL describeFeatureTypeUrl = getOperationURL(DESCRIBE_FEATURETYPE, false);

        Map<String, String> kvp = new HashMap<String, String>();
        kvp.put("SERVICE", "WFS");
        kvp.put("VERSION", getServiceVersion().toString());
        kvp.put("REQUEST", "DescribeFeatureType");
        kvp.put("TYPENAME", typeName);

        QName name = typeInfo.getName();
        if (!XMLConstants.DEFAULT_NS_PREFIX.equals(name.getPrefix())) {
            String nsUri = name.getNamespaceURI();
            kvp.put("NAMESPACE", "xmlns(" + name.getPrefix() + "=" + nsUri + ")");
            kvp.put("OUTPUTFORMAT", outputFormat);
        }

        URL url;
        try {
            url = http.createUrl(describeFeatureTypeUrl, kvp);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return url;
    }

    private WFSResponse issueGetRequest( BaseRequestType request, URL url, Map<String, String> kvp )
            throws IOException {
        WFSResponse response;
        HTTPResponse httpResponse = http.issueGet(url, kvp);

        String responseCharset = httpResponse.getResponseCharset();
        Charset charset = responseCharset == null ? null : Charset.forName(responseCharset);
        String contentType = httpResponse.getContentType();
        InputStream responseStream = httpResponse.getResponseStream();
        response = new WFSResponse(request, charset, contentType, responseStream);
        return response;
    }

    private Map<String, String> buildGetFeatureParametersForGet( GetFeatureType request )
            throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        map.put("SERVICE", "WFS");
        map.put("VERSION", getServiceVersion().toString());
        map.put("REQUEST", "GetFeature");
        map.put("OUTPUTFORMAT", request.getOutputFormat());

        if (request.getMaxFeatures() != null) {
            map.put("MAXFEATURES", String.valueOf(request.getMaxFeatures()));
        }

        final QueryType query = (QueryType) request.getQuery().get(0);
        final String typeName = (String) query.getTypeName().get(0);
        map.put("TYPENAME", typeName);

        if (query.getPropertyName().size() > 0) {
            List<String> propertyNames = query.getPropertyName();
            StringBuilder pnames = new StringBuilder();
            for( Iterator<String> it = propertyNames.iterator(); it.hasNext(); ) {
                pnames.append(it.next());
                if (it.hasNext()) {
                    pnames.append(',');
                }
            }
            map.put("PROPERTYNAME", pnames.toString());
        }

        // SRSNAME parameter. Let the server reproject.
        // TODO: should check if the server supports the required crs
        URI srsName = query.getSrsName();
        if (srsName != null) {
            map.put("SRSNAME", srsName.toString());
        }
        final Filter filter = query.getFilter();

        if (filter != null && Filter.INCLUDE != filter) {
            if (filter instanceof Id) {
                final Set<Identifier> identifiers = ((Id) filter).getIdentifiers();
                StringBuffer idValues = new StringBuffer();
                for( Iterator<Identifier> it = identifiers.iterator(); it.hasNext(); ) {
                    Object id = it.next().getID();
                    // REVISIT: should URL encode the id?
                    idValues.append(String.valueOf(id));
                    if (it.hasNext()) {
                        idValues.append(",");
                    }
                }
                map.put("FEATUREID", idValues.toString());
            } else {
                String xmlEncodedFilter = encodeGetFeatureGetFilter(filter);
                map.put("FILTER", xmlEncodedFilter);
            }
        }

        return map;
    }

    private Map<String, String> buildGetFeatureParametersForGet( Query query, String outputFormat )
            throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        map.put("SERVICE", "WFS");
        map.put("VERSION", getServiceVersion().toString());
        map.put("REQUEST", "GetFeature");
        map.put("TYPENAME", query.getTypeName());
        map.put("OUTPUTFORMAT", outputFormat);

        if (Query.DEFAULT_MAX != query.getMaxFeatures()) {
            map.put("MAXFEATURES", String.valueOf(query.getMaxFeatures()));
        }
        if (!query.retrieveAllProperties()) {
            String[] propertyNames = query.getPropertyNames();
            StringBuilder pnames = new StringBuilder();
            for( int i = 0; i < propertyNames.length; i++ ) {
                pnames.append(propertyNames[i]);
                if (i < propertyNames.length - 1) {
                    pnames.append(',');
                }
            }
            map.put("PROPERTYNAME", pnames.toString());
        }

        // SRSNAME parameter. Let the server reproject.
        // TODO: should check if the server supports the required crs
        final CoordinateReferenceSystem requestCrs = query.getCoordinateSystem();
        String epsgCode = GML2EncodingUtils.epsgCode(requestCrs);
        if (epsgCode != null) {
            epsgCode = "EPSG:" + epsgCode;
            map.put("SRSNAME", epsgCode);
        }

        final Filter filter = query.getFilter();

        if (filter != null && Filter.INCLUDE != filter) {
            if (filter instanceof Id) {
                final Set<Identifier> identifiers = ((Id) filter).getIdentifiers();
                StringBuffer idValues = new StringBuffer();
                for( Iterator<Identifier> it = identifiers.iterator(); it.hasNext(); ) {
                    Object id = it.next().getID();
                    // REVISIT: should URL encode the id?
                    idValues.append(String.valueOf(id));
                    if (it.hasNext()) {
                        idValues.append(",");
                    }
                }
                map.put("FEATUREID", idValues.toString());
            } else {
                String xmlEncodedFilter = encodeGetFeatureGetFilter(filter);
                map.put("FILTER", xmlEncodedFilter);
            }
        }

        return map;
    }

    private String encodeGetFeatureGetFilter( final Filter filter ) throws IOException {
        Encoder encoder = new Encoder(filterConfig);
        // do not write the xml declaration
        encoder.setOmitXMLDeclaration(true);

        OutputStream out = new ByteArrayOutputStream();
        encoder.encode(filter, OGC.Filter, out);
        String encoded = out.toString();
        encoded = encoded.replaceAll("\n", "");
        return encoded;
    }

    /**
     * Returns the operation URI for the given operation/http method as a String to avoid creating a
     * URL instance when not needed
     */
    private String getOperationURI( WFSOperationType operation, HttpMethod method ) {
        final OperationType operationType = getOperationMetadata(operation);
        final List<DCPType> dcps = operationType.getDCP();
        for( DCPType dcp : dcps ) {
            List<RequestMethodType> requests;
            if (GET == method) {
                requests = dcp.getHTTP().getGet();
            } else {
                requests = dcp.getHTTP().getPost();
            }
            for( RequestMethodType req : requests ) {
                String href = req.getHref();
                return href;
            }
        }
        return null;
    }
}
