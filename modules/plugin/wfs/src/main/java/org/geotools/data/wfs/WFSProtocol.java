package org.geotools.data.wfs;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.data.Query;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.capability.FilterCapabilities;
import org.opengis.filter.spatial.BBOX;

/**
 * Facade interface to interoperate with a WFS instance.
 * <p>
 * </p>
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.6.x
 */
public interface WFSProtocol {

    /**
     * Returns the WFS protocol version this facade talks to the WFS instance.
     * 
     * @return the protocol version in use by this facade
     */
    public Version getServiceVersion();

    /**
     * Returns service title as stated in the capabilities document
     * 
     * @return the service title
     */
    public String getServiceTitle();

    /**
     * Returns service abstract as stated in the capabilities document
     * 
     * @return the service abstract, may be {@code null}
     */
    public String getServiceAbstract();

    /**
     * Returns service keywords as stated in the capabilities document
     * 
     * @return the service keywords, may be empty
     */
    public Set<String> getServiceKeywords();

    /**
     * Returns service provider URI as stated in the capabilities document
     * 
     * @return the service provider URI
     */
    public URI getServiceProviderUri();

    /**
     * Returns the output format names declared in the GetFeature operation metadata section of the
     * WFS capabilities document
     * 
     * @return the global GetFeature output formats
     */
    public Set<String> getSupportedGetFeatureOutputFormats();

    /**
     * Returns the union of {@link #getSupportedGetFeatureOutputFormats()} and the output formats
     * declared for the feature type specifically in the FeatureTypeList section of the capabilities
     * document for the given feature type
     * 
     * @param typeName
     *            the feature type name for which to return the supported output formats
     * @return the output formats supported by {@code typeName}
     */
    public Set<String> getSupportedOutputFormats(final String typeName);

    /**
     * Returns the set of type names as extracted from the capabilities document, including the
     * namespace and prefix.
     * 
     * @return the set of feature type names as extracted from the capabilities document
     */
    public Set<QName> getFeatureTypeNames();

    /**
     * Returns the full feature type name for the {@code typeName} as declared in the {@code
     * FeatureTypeList/FeatureType/Name} element of the capabilities document.
     * <p>
     * The returned QName contains the namespace, localname as well as the prefix. {@code typeName}
     * is known to be {@code prefix:localName}.
     * </p>
     * 
     * @param typeName
     *            the prefixed type name to get the full name for
     * @return the full name of the given feature type
     */
    public QName getFeatureTypeName(final String typeName);

    /**
     * Returns the parsed version of the FilterCapabilities section in the capabilities document
     * 
     * @return a {@link FilterCapabilities} out of the FilterCapabilities section in the
     *         getcapabilities document
     */
    public FilterCapabilities getFilterCapabilities();

    /**
     * Returns whether the service supports the given operation for the given HTTP method.
     * 
     * @param operation
     *            the operation to check if the server supports
     * @param method
     *            the HTTP method to check if the server supports for the given operation
     * @return {@code true} if the operation/method is supported as stated in the WFS capabilities
     */
    public boolean supportsOperation(final WFSOperationType operation, final HttpMethod method);

    /**
     * Returns the URL for the given operation name and HTTP protocol as stated in the WFS
     * capabilities.
     * 
     * @param operation
     *            the name of the WFS operation
     * @param method
     *            the HTTP method
     * @return The URL access point for the given operation and method or {@code null} if the
     *         capabilities does not declare an access point for the operation/method combination
     * @see #supportsOperation(WFSOperationType, HttpMethod)
     */
    public URL getOperationURL(final WFSOperationType operation, final HttpMethod method);

    /**
     * Returns the title of the given feature type as declared in the corresponding FeatureType
     * element in the capabilities document.
     * 
     * @param typeName
     *            the featuretype name as declared in the FeatureType/Name element of the WFS
     *            capabilities
     * @return the title for the given feature type
     */
    public String getFeatureTypeTitle(final String typeName);

    /**
     * Returns the abstract of the given feature type as declared in the corresponding FeatureType
     * element in the capabilities document.
     * 
     * @param typeName
     *            the featuretype name as declared in the FeatureType/Name element of the WFS
     *            capabilities
     * @return the abstract for the given feature type
     */
    public String getFeatureTypeAbstract(final String typeName);

    /**
     * Returns the lat lon envelope of the given feature type as declared in the corresponding
     * FeatureType element in the capabilities document.
     * 
     * @param typeName
     *            the featuretype name as declared in the FeatureType/Name element of the WFS
     *            capabilities
     * @return a WGS84 envelope representing the bounds declared for the feature type in the
     *         capabilities document
     */
    public ReferencedEnvelope getFeatureTypeWGS84Bounds(final String typeName);

    /**
     * Returns the CRS identifier of the default CRS for the given feature type as declared in the
     * corresponding FeatureType element in the capabilities document.
     * 
     * @param typeName
     *            the featuretype name as declared in the FeatureType/Name element of the WFS
     *            capabilities
     * @return the default CRS for the given feature type
     */
    public String getDefaultCRS(final String typeName);

    /**
     * Returns the union of the default CRS and the other supported CRS's of the given feature type
     * as declared in the corresponding FeatureType element in the capabilities document.
     * 
     * @param typeName
     *            the featuretype name as declared in the FeatureType/Name element of the WFS
     *            capabilities
     * @return the list of supported CRS identifiers for the given feature type
     */
    public Set<String> getSupportedCRSIdentifiers(final String typeName);

    /**
     * Returns the list of keywords of the given feature type as declared in the corresponding
     * FeatureType element in the capabilities document.
     * 
     * @param typeName
     *            the featuretype name as declared in the FeatureType/Name element of the WFS
     *            capabilities
     * @return the keywords for the given feature type
     */
    public Set<String> getFeatureTypeKeywords(final String typeName);

    /**
     * Returns the http GET request to get the gml schema for the given type name
     * 
     * @param typeName
     * @return
     */
    public URL getDescribeFeatureTypeURLGet(final String typeName);

    public WFSResponse describeFeatureType(final String typeName, final String outputFormat,
            final HttpMethod method) throws IOException, UnsupportedOperationException;

    /**
     * Issues a GetFeature request for the given {@link Query}, {@code outputFormat} and HTTP method
     * <p>
     * The query is a GeoTools query and shall already be adapted to what the server supports in
     * terms of filter capabilities and CRS reprojection. The {@code WFSProtocol} implementation is
     * not required to check if the query filter is fully supported nor if the
     * {@link Query#getCoordinateSystem() CRS} is supported for the {@link Query#getTypeName()}
     * feature type.
     * </p>
     * <p>
     * The query properties are mapped to GetFeature arguments as this:
     * <ul>
     * <li>{@link Query#getTypeName()}: TYPENAME argument (we request a single feature type at a
     * time)
     * <li>{@link Query#getCoordinateSystem()}: SRSNAME argument
     * <li>{@link Query#getFilter()}: FILTER argument, may be FEATUREID if its an {@link Id} filter
     * or a BBOX argument if it's a {@link BBOX} filter, at the implementation's discretion
     * <li>{@link Query#getHandle()}: (ignored)
     * <li>{@link Query#getPropertyNames()}: if non empty nor null, PROPERTYNAME argument
     * <li>{@link Query#getMaxFeatures()}: MAXFEATURES argument
     * <li>{@link Query#getNamespace()}: (ignored)
     * <li>{@link Query#getSortBy()}: SORTBY argument
     * <li>{@link Query#getStartIndex()}: (ignored)
     * <li>{@link Query#getVersion()}: (ignored, no feature version support so far)
     * </ul>
     * </p>
     * 
     * @param query
     *            the query containing the feature type name to request and filtering criteria
     * @param outputFormat
     *            the format to request the results in
     * @param method
     *            the HTTP method to issue the GetFeature request in
     * @return the server response as a {@link WFSResponse} instance
     * @throws IOException
     *             if a communication error occurs with the WFS
     * @throws UnsupportedOperationException
     *             the the required HTTP method is not supported for the GetFeature operation
     */
    public WFSResponse getFeature(final Query query, final String outputFormat,
            final HttpMethod method) throws IOException, UnsupportedOperationException;

    /**
     * Issues a GetFeature request with {@code resultType=hits} if supported by the server and
     * returns the number of features declared in the response, or {@code -1} otherwise.
     * <p>
     * The argument is a GeoTools {@link Query} and shall already be appropriate for the WFS
     * capabilities, the {@link WFSProtocol} implementation is not required to perform any
     * {@link Filter} splitting depending on what filter capabilities the server supports, etc.
     * </p>
     * <p>
     * How to send the request (GET or POST method) and what output format to require the response
     * on is up to the {@code WFSProtocol} implementation. This method is meant for the protocol to
     * leverage an easy parsing of the {@code numberOfFeatures} attribute in a {@code
     * FeatureCollection} gml response, or whatever other means of getting the results hits the
     * protocol may leverage.
     * </p>
     * <p>
     * The {@link Query} to GetFeature arguments mapping is the same as for the
     * {@link #getFeature(Query, String, HttpMethod)} method.
     * </p>
     * 
     * @return the number of features returned by a getfeature request with resultType=hits or
     *         {@code -1} if not supported.
     * @throws IOException
     *             if a communication error occurs with the WFS
     */
    public int getFeatureHits(final Query query) throws IOException;

}
