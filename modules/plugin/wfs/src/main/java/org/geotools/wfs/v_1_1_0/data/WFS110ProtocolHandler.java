/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

import static org.geotools.data.wfs.HttpMethod.GET;
import static org.geotools.data.wfs.HttpMethod.POST;
import static org.geotools.data.wfs.WFSOperationType.GET_FEATURE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.ows10.DCPType;
import net.opengis.ows10.KeywordsType;
import net.opengis.ows10.OnlineResourceType;
import net.opengis.ows10.OperationType;
import net.opengis.ows10.OperationsMetadataType;
import net.opengis.ows10.RequestMethodType;
import net.opengis.ows10.ServiceProviderType;
import net.opengis.ows10.WGS84BoundingBoxType;
import net.opengis.wfs.FeatureTypeType;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.QueryType;
import net.opengis.wfs.ResultTypeType;
import net.opengis.wfs.WFSCapabilitiesType;
import net.opengis.wfs.WfsFactory;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureReader;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.MaxFeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.wfs.GetFeatureParser;
import org.geotools.data.wfs.HttpMethod;
import org.geotools.data.wfs.Version;
import org.geotools.data.wfs.WFSOperationType;
import org.geotools.data.wfs.WFSProtocol;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.v1_1.OGC;
import org.geotools.filter.v1_1.OGCConfiguration;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.gml2.bindings.GML2EncodingUtils;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.WFS;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.wfs.protocol.ConnectionFactory;
import org.geotools.wfs.protocol.WFSProtocolHandler;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.opengis.filter.spatial.BBOX;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Handles the interaction with a WFS 1.1 server by constructing the WFS 1.1 protocol specific
 * requests.
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id: WFS110ProtocolHandler.java 31720 2008-10-24 22:57:22Z groldan $
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/trunk/modules/plugin/wfs/src/main/java/org/geotools/wfs/v_1_1_0
 *         /data/XmlSimpleFeatureParser.java $
 * @deprecated we use {@link WFSProtocol} now
 */
public class WFS110ProtocolHandler extends WFSProtocolHandler {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");

    /**
     * WFS 1.1 configuration used for XML parsing and encoding
     */
    private static final WFSConfiguration configuration = new WFSConfiguration();

    /**
     * The WFS GetCapabilities document. Final by now, as we're not handling updatesequence, so will
     * not ask the server for an updated capabilities during the life-time of this datastore.
     */
    private final WFSCapabilitiesType capabilities;

    /**
     * Per featuretype name Map of capabilities feature type information. Not to be used directly
     * but through {@link #getFeatureTypeInfo(String)}
     */
    private final Map<String, FeatureTypeType> typeInfos;

    private final Map<String, SimpleFeatureType> featureTypeCache;

    /**
     * Hard limit for maxFeatures parameter. 0 means no limit.
     */
    private Integer maxFeaturesHardLimit;

    private boolean usePullParser;

    /**
     * Creates the protocol handler by parsing the capabilities document from the provided input
     * stream.
     * 
     * @param capabilitiesReader
     * @param tryGzip
     * @param auth
     * @param encoding
     * @param maxFeatures
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public WFS110ProtocolHandler( InputStream capabilitiesReader, ConnectionFactory connectionFac,
            Integer maxFeatures ) throws IOException {
        super(Version.v1_1_0, connectionFac);
        this.maxFeaturesHardLimit = maxFeatures;
        this.capabilities = parseCapabilities(capabilitiesReader);
        this.typeInfos = new HashMap<String, FeatureTypeType>();
        this.featureTypeCache = new HashMap<String, SimpleFeatureType>();

        final List<FeatureTypeType> ftypes = capabilities.getFeatureTypeList().getFeatureType();
        QName typeName;
        for( FeatureTypeType ftype : ftypes ) {
            typeName = ftype.getName();
            assert !("".equals(typeName.getPrefix()));
            String prefixedTypeName = typeName.getPrefix() + ":" + typeName.getLocalPart();
            typeInfos.put(prefixedTypeName, ftype);
        }
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

    /**
     * Returns the URL representing the service entry point for the required WFS operation and HTTP
     * method.
     */
    @SuppressWarnings("unchecked")
    @Override
    public URL getOperationURL( final WFSOperationType operation, final HttpMethod method )
            throws UnsupportedOperationException {
        final OperationsMetadataType operationsMetadata = capabilities.getOperationsMetadata();
        final List<OperationType> operations = operationsMetadata.getOperation();
        for( OperationType operationType : operations ) {
            String operationName = operationType.getName();
            if (operation.getName().equalsIgnoreCase(operationName)) {
                List<DCPType> dcps = operationType.getDCP();
                for( DCPType dcp : dcps ) {
                    List<RequestMethodType> requests;
                    if (GET == method) {
                        requests = dcp.getHTTP().getGet();
                    } else {
                        requests = dcp.getHTTP().getPost();
                    }
                    for( RequestMethodType req : requests ) {
                        String href = req.getHref();
                        if (href != null) {
                            try {
                                return new URL(href);
                            } catch (MalformedURLException e) {
                                // Log error and let the search continue
                                LOGGER.log(Level.INFO, "Malformed " + method + " URL for "
                                        + operationName, e);
                            }
                        }
                    }
                }
            }
        }
        throw new UnsupportedOperationException("Operation metadata not found for " + operation
                + " with HTTP " + method + " method");
    }

    /**
     * Checks whether the WFS capabilities provides a service entry point for the given operation
     * and HTTP method.
     */
    @Override
    public boolean supports( WFSOperationType operation, HttpMethod method ) {
        try {
            getOperationURL(operation, method);
            return true;
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    public String getServiceAbstract() {
        return capabilities.getServiceIdentification().getAbstract();
    }

    /**
     * @return service metadata keywords
     */
    @SuppressWarnings("unchecked")
    public Set<String> getKeywords() {
        List<KeywordsType> capsKeywords = capabilities.getServiceIdentification().getKeywords();
        return getKeyWords(capsKeywords);
    }

    /**
     * @param typeName type name to return the keyword list for
     * @return the keywords of {@code typeName} in the capabilities document
     */
    @SuppressWarnings("unchecked")
    public Set<String> getKeywords( final String typeName ) {
        FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        List<KeywordsType> ftKeywords = featureTypeInfo.getKeywords();
        return getKeyWords(ftKeywords);
    }

    @SuppressWarnings("unchecked")
    private Set<String> getKeyWords( List<KeywordsType> keywordsList ) {
        Set<String> keywords = new HashSet<String>();
        for( KeywordsType keys : keywordsList ) {
            keywords.addAll(keys.getKeyword());
        }
        return keywords;
    }

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

    public String getServiceTitle() {
        return capabilities.getServiceIdentification().getTitle();
    }

    /**
     * Makes a {@code DescribeFeatureType} request for {@code typeName} feature type, parses the
     * server response into a {@link SimpleFeatureType} and returns it.
     * <p>
     * Due to a current limitation widely spread through the GeoTools library, the parsed
     * FeatureType will be adapted to share the same name than the Features produced for it. For
     * example, if the actual feature type name is {@code Streams_Type} and the features name (i.e.
     * which is the FeatureType name as stated in the WFS capabilities document) is {@code Stream},
     * the returned feature type name will also be {@code Stream}.
     * </p>
     * 
     * @param typeName
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public SimpleFeatureType parseDescribeFeatureType( final String typeName ) throws IOException {
        final FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        final QName featureDescriptorName = featureTypeInfo.getName();

        final URL describeUrl = getDescribeFeatureTypeURLGet(typeName);

        final SimpleFeatureType featureType;
        CoordinateReferenceSystem crs = getFeatureTypeCRS(typeName);
        featureType = EmfAppSchemaParser.parseSimpleFeatureType(configuration,
                featureDescriptorName, describeUrl, crs);

        // adapt the feature type name
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.init(featureType);
        builder.setName(typeName);
        builder.setNamespaceURI(featureTypeInfo.getName().getNamespaceURI());
        GeometryDescriptor defaultGeometry = featureType.getGeometryDescriptor();
        if (defaultGeometry != null) {
            builder.setDefaultGeometry(defaultGeometry.getLocalName());
            builder.setCRS(defaultGeometry.getCoordinateReferenceSystem());
        }
        final SimpleFeatureType adaptedFeatureType = builder.buildFeatureType();
        return adaptedFeatureType;
    }

    @Override
    public URL getDescribeFeatureTypeURLGet( final String typeName ) throws MalformedURLException {
        URL v100StyleUrl = super.getDescribeFeatureTypeURLGet(typeName);
        FeatureTypeType typeInfo = getFeatureTypeInfo(typeName);
        QName name = typeInfo.getName();
        if (XMLConstants.DEFAULT_NS_PREFIX.equals(name.getPrefix())) {
            return v100StyleUrl;
        }
        String raw = v100StyleUrl.toExternalForm();
        String nsUri;
        String outputFormat;
        try {
            nsUri = URLEncoder.encode(name.getNamespaceURI(), "UTF-8");
            outputFormat = URLEncoder.encode("text/xml; subtype=gml/3.1.1", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        raw += "&NAMESPACE=xmlns(" + name.getPrefix() + "=" + nsUri + ")";
        raw += "&OUTPUTFORMAT=" + outputFormat;
        URL v110StyleUrl = new URL(raw);
        return v110StyleUrl;
    }

    private Object parse( final URL url, final HttpMethod method ) throws IOException {

        final HttpURLConnection connection = connectionFac.getConnection(url, method);
        String contentEncoding = connection.getContentEncoding();
        Charset charset = Charset.forName("UTF-8"); // TODO: un-hardcode
        if (null != contentEncoding) {
            try {
                charset = Charset.forName(contentEncoding);
            } catch (UnsupportedCharsetException e) {
                LOGGER.warning("Can't handle response encoding: " + contentEncoding
                        + ". Trying with default");
            }
        }
        Parser parser = new Parser(configuration);
        InputStream in = connectionFac.getInputStream(connection);
        Reader reader = new InputStreamReader(in, charset);
        Object parsed;
        try {
            parsed = parser.parse(reader);
        } catch (SAXException e) {
            throw new DataSourceException(e);
        } catch (ParserConfigurationException e) {
            throw new DataSourceException(e);
        } finally {
            reader.close();
        }
        return parsed;
    }

    public String[] getCapabilitiesTypeNames() {
        List<String> typeNames = new ArrayList<String>(typeInfos.keySet());
        Collections.sort(typeNames);
        return typeNames.toArray(new String[typeNames.size()]);
    }

    public String getFeatureTypeTitle( final String typeName ) {
        FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        return featureTypeInfo.getTitle();
    }

    public String getFeatureTypeAbstract( final String typeName ) {
        FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        return featureTypeInfo.getAbstract();
    }

    /**
     * Returns the bounds of the given feature type stated in the capabilities document but in the
     * feature type default CRS.
     * 
     * @param typeName
     * @return the {@code ows:WGS84BoundingBox} capabilities bounds for {@code typeName} but in the
     *         native CRS (that is, transformed to the CRS declared as the feature type's {@code
     *         DefaultSRS})
     * @throws IllegalStateException if the capabilities document does not supply the required
     *         information.
     */
    @SuppressWarnings("unchecked")
    public ReferencedEnvelope getFeatureTypeBounds( final String typeName ) {
        FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
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

            CoordinateReferenceSystem dataCrs = getFeatureTypeCRS(typeName);

            ReferencedEnvelope bounds;
            try {
                bounds = latLonBounds.transform(dataCrs, true);
            } catch (Exception e) {
                throw new IllegalStateException("Error transforming WGS84 BoundingBox for "
                        + typeName + " to its default CRS", e);
            }
            return bounds;
        }
        throw new IllegalStateException(
                "The capabilities document does not supply the ows:WGS84BoundingBox element");
    }

    public CoordinateReferenceSystem getFeatureTypeCRS( final String typeName ) {
        FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);
        String defaultSRS = featureTypeInfo.getDefaultSRS();
        try {
            return CRS.decode(defaultSRS);
        } catch (NoSuchAuthorityCodeException e) {
            LOGGER.info("Authority not found for " + typeName + " CRS: " + defaultSRS);
            // HACK HACK HACK!: remove when
            // http://jira.codehaus.org/browse/GEOT-1659 is fixed
            if (defaultSRS.toUpperCase().startsWith("URN")) {
                String code = defaultSRS.substring(defaultSRS.lastIndexOf(":") + 1);
                try {
                    return CRS.decode("EPSG:" + code);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return null;
                }
            }
        } catch (FactoryException e) {
            LOGGER.log(Level.INFO, "Error creating CRS " + typeName + ": " + defaultSRS, e);
        }
        return null;
    }

    /**
     * Only returns the bounds of the query (ie, the bounds of the whole feature type) if the query
     * has no filter set, otherwise the bounds may be too expensive to acquire.
     * 
     * @param query
     * @return The bounding box of the datasource in the CRS required by the query, or {@code null}
     *         if unknown and too expensive for the method to calculate or any errors occur.
     */
    public ReferencedEnvelope getBounds( final Query query ) throws IOException {
        if (!Filter.INCLUDE.equals(query.getFilter())) {
            return null;
        }
        final String typeName = query.getTypeName();

        ReferencedEnvelope featureTypeBounds;

        featureTypeBounds = getFeatureTypeBounds(typeName);

        final CoordinateReferenceSystem featureTypeCrs = featureTypeBounds
                .getCoordinateReferenceSystem();
        final CoordinateReferenceSystem queryCrs = query.getCoordinateSystem();
        if (queryCrs != null && !CRS.equalsIgnoreMetadata(queryCrs, featureTypeCrs)) {
            try {
                featureTypeBounds = featureTypeBounds.transform(queryCrs, true);
            } catch (TransformException e) {
                LOGGER.log(Level.INFO, "Error transforming bounds for " + typeName, e);
                featureTypeBounds = null;
            } catch (FactoryException e) {
                LOGGER.log(Level.INFO, "Error transforming bounds for " + typeName, e);
                featureTypeBounds = null;
            }
        }
        return featureTypeBounds;
    }

    /**
     * If the query is fully supported, makes a {@code GetFeature} request with {@code
     * resultType=hits} and returns the counts returned by the server, otherwise returns {@code -1}
     * as the result is too expensive to calculate.
     * 
     * @param query
     * @return
     */
    public int getCount( final Query query ) throws IOException {
        final String typeName = query.getTypeName();
        // TODO: check if filter is fully supported, return -1 if not
        final Filter filter = query.getFilter();

        int featureCount = -1;

        final InputStream responseStream;
        responseStream = sendGetFeatures(query, typeName, filter);
        try {
            XmlPullParser parser = null;
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);

            // parse root element
            parser = factory.newPullParser();
            parser.setInput(responseStream, "UTF-8");
            parser.nextTag();

            String numberOfFeatures = null;
            // look for schema location
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
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Error calculating query count", e);
        } finally {
            responseStream.close();
        }
        return featureCount;
    }

    /**
     * Makes a WFS GetFeature request for the given geotools query and returns a feature reader
     * whose content is accordingly limited by the query, even if the backend WFS can't cope up with
     * the full query.
     * 
     * @param query
     * @param transaction
     * @return a FeatureReader<SimpleFeatureType, SimpleFeature> correctly set up to return the
     *         contents as per requested by the query
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader( final Query query,
            final Transaction transaction ) throws IOException {
        final SimpleFeatureType contentType = getQueryType(query);

        // by now encode the full query to be sent to the server.
        // TODO: implement filter splitting for server supported/unsupported

        final String typeName = query.getTypeName();
        final FeatureTypeType featureTypeInfo = getFeatureTypeInfo(typeName);

        Filter supportedFilter;
        Filter unsupportedFilter;
        {
            Filter filter = query.getFilter();
            if (null == filter) {
                // what do we have the null objects for if query can return
                // null?
                filter = Filter.INCLUDE;
            }
            if (Filter.EXCLUDE.equals(filter)) {
                return new EmptyFeatureReader<SimpleFeatureType, SimpleFeature>(contentType);
            }
            // TODO: split filters!!
            // We need to create a geotools FilterCapabilities out of the
            // GetCapabilities response's FitlerCapabilities element and use it
            // with a PostPreProcessFilterSplittingVisitor in order to split the
            // filter in the supported and unsupported parts
            if (filter instanceof BBOX || filter instanceof Id) {
                supportedFilter = filter;
                unsupportedFilter = Filter.EXCLUDE;
            } else {
                // HACK: by now just sent the whole filter to the WFS
                // supportedFilter = filter;

                // Double HACK: cubewerx does not seem to understand the geometry filters
                // we send other than bbox. By now grab all the features and apply the filter
                // at runtime. This needs to be fixed/worked around asap though.
                supportedFilter = Filter.INCLUDE;
                unsupportedFilter = filter;
            }
        }

        final InputStream responseStream;
        responseStream = sendGetFeatures(query, typeName, supportedFilter);
        final QName name = featureTypeInfo.getName();
        final Configuration configuration = getConfiguration();

        URL describeFeatureTypeURL = getDescribeFeatureTypeURLGet(typeName);
        GetFeatureParser parser;
        if (this.usePullParser) {
            parser = new XmlSimpleFeatureParser(responseStream, name, contentType);
        } else {
            parser = new StreamingParserFeatureReader(configuration, responseStream, name,
                    describeFeatureTypeURL);
        }
        FeatureReader<SimpleFeatureType, SimpleFeature> reader = new WFSFeatureReader(parser);

        if (!reader.hasNext()) {
            return new EmptyFeatureReader<SimpleFeatureType, SimpleFeature>(contentType);
        }

        SimpleFeatureType readerType = reader.getFeatureType();
        if (!contentType.equals(readerType)) {
            final boolean cloneContents = false;
            // reader = new ReTypeFeatureReader(reader, contentType,
            // cloneContents);
        }

        if (Filter.EXCLUDE != unsupportedFilter) {
            reader = new FilteringFeatureReader<SimpleFeatureType, SimpleFeature>(reader,
                    unsupportedFilter);
        }

        if (this.maxFeaturesHardLimit.intValue() > 0 || query.getMaxFeatures() != Integer.MAX_VALUE) {
            int maxFeatures = Math.min(maxFeaturesHardLimit.intValue(), query.getMaxFeatures());
            reader = new MaxFeatureReader<SimpleFeatureType, SimpleFeature>(reader, maxFeatures);
        }
        return reader;
    }

    public SimpleFeatureType getQueryType( final Query query ) throws IOException {
        String typeName = query.getTypeName();
        final SimpleFeatureType featureType = getFeatureType(typeName);
        final CoordinateReferenceSystem coordinateSystemReproject = query
                .getCoordinateSystemReproject();

        String[] propertyNames = query.getPropertyNames();

        SimpleFeatureType queryType = featureType;
        if (propertyNames != null && propertyNames.length > 0) {
            try {
                queryType = DataUtilities.createSubType(queryType, propertyNames);
            } catch (SchemaException e) {
                throw new DataSourceException(e);
            }
        } else {
            propertyNames = DataUtilities.attributeNames(featureType);
        }

        if (coordinateSystemReproject != null) {
            try {
                queryType = DataUtilities.createSubType(queryType, propertyNames,
                        coordinateSystemReproject);
            } catch (SchemaException e) {
                throw new DataSourceException(e);
            }
        }

        return queryType;
    }

    /**
     * Protected to be easily overriden by a unit test friendly subclass
     * 
     * @return
     */
    Configuration getConfiguration() {
        return configuration;
    }

    private InputStream sendGetFeatures( final Query query, final String typeName,
            final Filter filter ) throws IOException, MalformedURLException {
        final InputStream responseStream;

        final FeatureTypeType typeInfo = getFeatureTypeInfo(typeName);

        // TODO: enable POST
        if (false && supports(GET_FEATURE, POST)) {
            QueryType wfsQuery = createWfsQuery(typeName, filter);
            GetFeatureType wfsRequest = createGetFeature(wfsQuery, false);
            URL getFeaturePostUrl = getOperationURL(GET_FEATURE, POST);
            responseStream = sendPost(getFeaturePostUrl, wfsRequest, WFS.GetFeature);
        } else {
            URL getFeatureGetUrl;
            String[] propNames = query.getPropertyNames();
            List<String> propertyNames;
            if (propNames == null || propNames.length == 0) {
                propertyNames = Collections.emptyList();
            } else {
                propertyNames = Arrays.asList(propNames);
            }
            int maxFeatures = query.getMaxFeatures();
            if (this.maxFeaturesHardLimit.intValue() > 0) {
                maxFeatures = Math.min(maxFeatures, this.maxFeaturesHardLimit.intValue());
            }

            CoordinateReferenceSystem crs = query.getCoordinateSystem();
            if (crs == null) {
                crs = getFeatureTypeCRS(typeName);
            }

            List<SortBy> sortBy = (List<SortBy>) (query.getSortBy() == null ? Collections
                    .emptyList() : Arrays.asList(query.getSortBy()));
            getFeatureGetUrl = createGetFeatureGet(typeName, propertyNames, filter, crs,
                    maxFeatures, sortBy, false);

            System.err.println("Request: " + getFeatureGetUrl.toExternalForm());

            responseStream = connectionFac.getInputStream(getFeatureGetUrl, GET);
        }
        return responseStream;
    }

    /**
     * @param typeName
     * @param propertyNames
     * @param filter the filter to apply to the request, shall not be {@code Filter.EXCLUDE}
     * @param crs
     * @param maxFeatures
     * @param sortBy
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private URL createGetFeatureGet( final String typeName, final List<String> propertyNames,
            final Filter filter, final CoordinateReferenceSystem crs, final int maxFeatures,
            final List<SortBy> sortBy, boolean hits ) throws IOException {
        final URL getFeatureGetUrl = getOperationURL(GET_FEATURE, GET);
        Map<String, String> kvpMap = new LinkedHashMap<String, String>();
        {
            String query = getFeatureGetUrl.getQuery();
            if (query != null) {
                String[] split = query.split("&");
                for( String kvp : split ) {
                    String[] keyAndValue = kvp.split("=");
                    String key = keyAndValue[0];
                    if ("".equals(key)) {
                        continue;
                    }
                    String value = keyAndValue.length == 1 ? null : keyAndValue[1];
                    kvpMap.put(key, value);
                }
            }
        }

        kvpMap.put("SERVICE", "WFS");
        kvpMap.put("VERSION", "1.1.0");
        kvpMap.put("REQUEST", "GetFeature");
        kvpMap.put("TYPENAME", typeName);
        // TODO: consider other output formats
        // try {
        // kvpMap.put("OUTPUTFORMAT",
        // URLEncoder.encode("text/xml;subtype=gml/3.1.1", "UTF-8"));
        // } catch (UnsupportedEncodingException e) {
        // throw new RuntimeException(e);
        // }

        if (hits) {
            kvpMap.put("RESULTTYPE", "hits");
        }

        if (Integer.MAX_VALUE != maxFeatures) {
            kvpMap.put("MAXFEATURES", String.valueOf(maxFeatures));
        }

        List<String> properties = propertyNames;
        if (propertyNames.size() == 0) {
            // HACK: explicitly setting the property names to avoid retrieving
            // complex attributes while we don't support them. This behaviour is
            // dependant on the fact we're creating a simple feature view of the
            // original types
            SimpleFeatureType featureType = getFeatureType(typeName);
            properties = new ArrayList<String>(featureType.getAttributeCount());
            for( AttributeDescriptor att : featureType.getAttributeDescriptors() ) {
                properties.add(att.getLocalName());
            }
        }
        {
            StringBuffer sb = new StringBuffer();
            for( Iterator<String> it = properties.iterator(); it.hasNext(); ) {
                sb.append(it.next());
                if (it.hasNext()) {
                    sb.append(",");
                }
            }
            kvpMap.put("PROPERTYNAME", sb.toString());
        }

        // SRSNAME parameter
        String epsgCode = GML2EncodingUtils.epsgCode(crs);
        if (epsgCode != null) {
            epsgCode = "EPSG:" + epsgCode;
            kvpMap.put("SRSNAME", epsgCode);
        }

        if (Filter.INCLUDE != filter) {
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
                kvpMap.put("FEATUREID", idValues.toString());
            } else {
                String xmlEncodedFilter = encodeGetFeatureGetFilter(filter);
                String urlEncodedFilter;
                try {
                    urlEncodedFilter = URLEncoder.encode(xmlEncodedFilter, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                kvpMap.put("FILTER", urlEncodedFilter);
            }
        }

        if (sortBy.size() > 0) {
            StringBuffer sb = new StringBuffer();
            SortBy next;
            PropertyName propertyName;
            String sortOrder;
            for( Iterator<SortBy> it = sortBy.iterator(); it.hasNext(); ) {
                next = it.next();
                propertyName = next.getPropertyName();
                sortOrder = SortOrder.ASCENDING == next.getSortOrder() ? "A" : "D";
                sb.append(propertyName.getPropertyName());
                sb.append(" ");
                sb.append(sortOrder);
                if (it.hasNext()) {
                    sb.append(",");
                }
            }
            kvpMap.put("SORTBY", sb.toString());
        }

        StringBuffer queryString = new StringBuffer();
        for( Map.Entry<String, String> kvp : kvpMap.entrySet() ) {
            queryString.append(kvp.getKey());
            queryString.append("=");
            queryString.append(kvp.getValue());
            queryString.append("&");
        }

        String entryPoint = getFeatureGetUrl.toExternalForm();
        String getFeatureQueryString = queryString.toString();

        if (!entryPoint.endsWith("&") && !entryPoint.endsWith("?")) {
            entryPoint += "?";
        }
        String url = entryPoint + getFeatureQueryString;
        final URL getFeatureRequest = new URL(url);
        return getFeatureRequest;
    }

    public static String encodeGetFeatureGetFilter( final Filter filter ) throws IOException {

        OGCConfiguration filterConfig = new OGCConfiguration();
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
     * Sends a POST request to {@code destination} whose content is the XML encoded representation
     * of {@code object} and returns an input stream from which to get the server response.
     * 
     * @param destination
     * @param object
     * @param name
     * @return
     * @throws IOException
     */
    private InputStream sendPost( final URL destination, final Object object, QName name )
            throws IOException {
        Encoder encoder = new Encoder(configuration);
        encoder.setNamespaceAware(true);
        encoder.setIndenting(true);
        encoder.setIndentSize(2);

        HttpURLConnection connection = connectionFac.getConnection(destination, POST);
        OutputStream outputStream = connection.getOutputStream();
        try {
            encoder.encode(object, name, outputStream);
        } finally {
            outputStream.close();
        }
        InputStream responseStream = connectionFac.getInputStream(connection);
        return responseStream;
    }

    @SuppressWarnings("unchecked")
    private GetFeatureType createGetFeature( QueryType wfsQuery, boolean hits ) {
        String outputFormat = null;
        String typeName = (String) wfsQuery.getTypeName().get(0);

        GetFeatureType request = WfsFactory.eINSTANCE.createGetFeatureType();
        request.setHandle("geotools-wfs-client " + typeName);
        request.setOutputFormat(outputFormat);
        request.setResultType(hits ? ResultTypeType.HITS_LITERAL : ResultTypeType.RESULTS_LITERAL);
        request.setService("WFS");// TODO: un-hardcode
        request.setVersion("1.1.0");// TODO: un-hardcode
        request.getQuery().add(wfsQuery);
        return request;
    }

    private QueryType createWfsQuery( String typeName, Filter filter ) {
        return null;
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
    FeatureTypeType getFeatureTypeInfo( final String typeName ) {
        if (!typeInfos.containsKey(typeName)) {
            throw new IllegalArgumentException("Type name not found: " + typeName);
        }
        return typeInfos.get(typeName);
    }

    public SimpleFeatureType getFeatureType( final String typeName ) throws IOException {
        if (featureTypeCache.containsKey(typeName)) {
            return featureTypeCache.get(typeName);
        }

        SimpleFeatureType ftype = parseDescribeFeatureType(typeName);
        synchronized (featureTypeCache) {
            featureTypeCache.put(typeName, ftype);
        }
        return ftype;
    }

    /**
     * Sets whether to use {@link XmlSimpleFeatureParser} or {@link StreamingParserFeatureReader}
     * 
     * @param usePullParser if {@code true}, {@code XmlSimpleFeatureParser} will be used to parse
     *        GetFeature responses.
     */
    public void setUsePullParser( boolean usePullParser ) {
        this.usePullParser = usePullParser;
    }

}
