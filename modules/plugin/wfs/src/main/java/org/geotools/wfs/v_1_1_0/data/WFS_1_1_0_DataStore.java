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
package org.geotools.wfs.v_1_1_0.data;

import static org.geotools.data.wfs.HttpMethod.GET;
import static org.geotools.data.wfs.HttpMethod.POST;
import static org.geotools.data.wfs.WFSOperationType.DESCRIBE_FEATURETYPE;
import static org.geotools.data.wfs.WFSOperationType.GET_FEATURE;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.geotools.data.DataAccess;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.EmptyFeatureReader;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.LockingManager;
import org.geotools.data.MaxFeatureReader;
import org.geotools.data.Query;
import org.geotools.data.ReTypeFeatureReader;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.Transaction;
import org.geotools.data.view.DefaultView;
import org.geotools.data.wfs.ExceptionReportParser;
import org.geotools.data.wfs.GetFeatureParser;
import org.geotools.data.wfs.HttpMethod;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSExtensions;
import org.geotools.data.wfs.WFSOperationType;
import org.geotools.data.wfs.WFSProtocol;
import org.geotools.data.wfs.WFSResponse;
import org.geotools.data.wfs.WFSResponseParser;
import org.geotools.data.wfs.WFSServiceInfo;
import org.geotools.feature.NameImpl;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 * A WFS 1.1 DataStore implementation.
 * 
 * @author Gabriel Roldan
 * @version $Id: WFS_1_1_0_DataStore.java 31720 2008-10-24 22:57:22Z groldan $
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/main/java/org/geotools
 *         /wfs/v_1_1_0/data/WFSDataStore.java $
 */
public final class WFS_1_1_0_DataStore implements WFSDataStore {
    private static final String DEFAULT_OUTPUT_FORMAT = "text/xml; subtype=gml/3.1.1";

    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");

    private WFSProtocol wfs;

    private Map<String, SimpleFeatureType> byTypeNameTypes;

    private Integer maxFeaturesHardLimit;

    private boolean preferPostOverGet = false;

    /**
     * The WFS capabilities document.
     * 
     * @param capabilities
     */
    @SuppressWarnings("unchecked")
    public WFS_1_1_0_DataStore(final WFSProtocol wfs) {
        this.wfs = wfs;
        byTypeNameTypes = Collections.synchronizedMap(new HashMap<String, SimpleFeatureType>());
        maxFeaturesHardLimit = Integer.valueOf(0); // not set
    }

    public WFSServiceInfo getInfo() {
        return new CapabilitiesServiceInfo(this);
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
     * @param prefixedTypeName
     *            the type name as stated in the WFS capabilities document
     * @return the GeoTools FeatureType for the {@code typeName} as stated on the capabilities
     *         document.
     * @see org.geotools.data.DataStore#getSchema(java.lang.String)
     */
    public SimpleFeatureType getSchema(final String prefixedTypeName) throws IOException {
        SimpleFeatureType ftype = byTypeNameTypes.get(prefixedTypeName);
        if (ftype == null) {
            String outputFormat = DEFAULT_OUTPUT_FORMAT;
            HttpMethod method = findWhichMethodToUseFor(DESCRIBE_FEATURETYPE);
            WFSResponse response = wfs.describeFeatureType(prefixedTypeName, outputFormat, method);

            throw new UnsupportedOperationException("finish implementing this method!");
            // byTypeNameTypes.put(prefixedTypeName, ftype);
        }
        return ftype;
    }

    /**
     * @see DataAccess#getSchema(Name)
     * @see #getSchema(String)
     */
    public SimpleFeatureType getSchema(Name name) throws IOException {
        Set<QName> featureTypeNames = wfs.getFeatureTypeNames();

        String namespaceURI;
        String localPart;
        for (QName qname : featureTypeNames) {
            namespaceURI = name.getNamespaceURI();
            localPart = name.getLocalPart();
            if (namespaceURI.equals(qname.getNamespaceURI())
                    && localPart.equals(qname.getLocalPart())) {
                String prefixedName = qname.getPrefix() + ":" + localPart;
                return getSchema(prefixedName);
            }
        }
        throw new SchemaNotFoundException(name.getURI());
    }

    /**
     * @see DataAccess#getNames()
     */
    public List<Name> getNames() throws IOException {
        Set<QName> featureTypeNames = wfs.getFeatureTypeNames();
        List<Name> names = new ArrayList<Name>(featureTypeNames.size());
        String namespaceURI;
        String localPart;
        for (QName name : featureTypeNames) {
            namespaceURI = name.getNamespaceURI();
            localPart = name.getLocalPart();
            names.add(new NameImpl(namespaceURI, localPart));
        }
        return names;
    }

    /**
     * @see org.geotools.data.DataStore#getTypeNames()
     */
    public String[] getTypeNames() throws IOException {
        Set<QName> featureTypeNames = wfs.getFeatureTypeNames();
        List<String> sorted = new ArrayList<String>(featureTypeNames.size());
        for (QName name : featureTypeNames) {
            sorted.add(name.getPrefix() + ":" + name.getLocalPart());
        }
        Collections.sort(sorted);
        return sorted.toArray(new String[sorted.size()]);
    }

    /**
     * @see org.geotools.data.DataStore#dispose()
     */
    public void dispose() {
        if (wfs != null) {
            wfs = null;
        }
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.data.Query,
     *      org.geotools.data.Transaction)
     */
    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(Query query,
            Transaction transaction) throws IOException {
        // TODO: handle output format preferences
        String outputFormat = DEFAULT_OUTPUT_FORMAT;

        // TODO: split filters! WFSProtocol is not responsible of doing so
        final HttpMethod method = findWhichMethodToUseFor(GET_FEATURE);
        final WFSResponse response = wfs.getFeature(query, outputFormat, method);

        final WFSResponseParser parser = WFSExtensions.findParser(response);
        if (parser instanceof ExceptionReportParser) {
            IOException exception = ((ExceptionReportParser) parser).parse(response);
            throw exception;
        } else if (!(parser instanceof GetFeatureParser)) {
            throw new IOException("Unknown parser for " + response + ": " + parser);
        }

        FeatureReader<SimpleFeatureType, SimpleFeature> reader;
        reader = new WFSFeatureReader((GetFeatureParser) parser);

        final SimpleFeatureType contentType = getQueryType(query);

        if (!reader.hasNext()) {
            return new EmptyFeatureReader<SimpleFeatureType, SimpleFeature>(contentType);
        }

        final SimpleFeatureType readerType = reader.getFeatureType();
        if (!contentType.equals(readerType)) {
            final boolean cloneContents = false;
            reader = new ReTypeFeatureReader(reader, contentType, cloneContents);
        }

        // if (Filter.EXCLUDE != unsupportedFilter) {
        // TODO: split filters!!
        if (Filter.INCLUDE != query.getFilter()) {
            reader = new FilteringFeatureReader<SimpleFeatureType, SimpleFeature>(reader, query
                    .getFilter());
        }

        if (this.maxFeaturesHardLimit.intValue() > 0 || query.getMaxFeatures() != Integer.MAX_VALUE) {
            int maxFeatures = Math.min(maxFeaturesHardLimit.intValue(), query.getMaxFeatures());
            reader = new MaxFeatureReader<SimpleFeatureType, SimpleFeature>(reader, maxFeatures);
        }
        return reader;
    }

    /**
     * Returns the feature type that shall result of issueing the given request, adapting the
     * original feature type for the request's type name in terms of the query CRS and requested
     * attributes
     * 
     * @param query
     * @return
     * @throws IOException
     */
    SimpleFeatureType getQueryType(final Query query) throws IOException {
        final String typeName = query.getTypeName();
        final SimpleFeatureType featureType = getSchema(typeName);
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
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     */
    public WFSFeatureSource getFeatureSource(final String typeName) throws IOException {
        return new WFSFeatureSource(this, typeName);
    }

    /**
     * @return {@code null}, no lock support so far
     * @see org.geotools.data.DataStore#getLockingManager()
     */
    public LockingManager getLockingManager() {
        return null;
    }

    /**
     * @see org.geotools.data.DataStore#getView(org.geotools.data.Query)
     * @see DefaultView
     */
    public FeatureSource<SimpleFeatureType, SimpleFeature> getView(final Query query)
            throws IOException, SchemaException {
        final String typeName = query.getTypeName();
        final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = this
                .getFeatureSource(typeName);
        return new DefaultView(featureSource, query);
    }

    /**
     * Not supported.
     * 
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.opengis.filter.Filter, org.geotools.data.Transaction)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(String typeName,
            Filter filter, Transaction transaction) throws IOException {
        throw new UnsupportedOperationException("This is a read only DataStore");
    }

    /**
     * Not supported.
     * 
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.geotools.data.Transaction)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(String typeName,
            Transaction transaction) throws IOException {
        throw new UnsupportedOperationException("This is a read only DataStore");
    }

    /**
     * Not supported.
     * 
     * @see org.geotools.data.DataStore#getFeatureWriterAppend(java.lang.String,
     *      org.geotools.data.Transaction)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriterAppend(String typeName,
            Transaction transaction) throws IOException {
        throw new UnsupportedOperationException("This is a read only DataStore");
    }

    public FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource(Name typeName)
            throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @see DataAccess#updateSchema(Name, org.opengis.feature.type.FeatureType)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public void updateSchema(Name typeName, SimpleFeatureType featureType) throws IOException {
        throw new UnsupportedOperationException("WFS does not support update schema");
    }

    /**
     * @see org.geotools.data.DataStore#updateSchema(java.lang.String,
     *      org.opengis.feature.simple.SimpleFeatureType)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public void updateSchema(String typeName, SimpleFeatureType featureType) throws IOException {
        throw new UnsupportedOperationException("WFS does not support update schema");
    }

    /**
     * @see org.geotools.data.DataStore#createSchema(org.opengis.feature.simple.SimpleFeatureType)
     * @throws UnsupportedOperationException
     *             always since this operation does not apply to a WFS backend
     */
    public void createSchema(SimpleFeatureType featureType) throws IOException {
        throw new UnsupportedOperationException("WFS DataStore does not support createSchema");
    }

    public String getFeatureTypeTitle(String typeName) {
        return wfs.getFeatureTypeTitle(typeName);
    }

    public String getFeatureTypeAbstract(String typeName) {
        return wfs.getFeatureTypeAbstract(typeName);
    }

    public ReferencedEnvelope getFeatureTypeBounds(String typeName) {
        final ReferencedEnvelope wgs84Bounds = wfs.getFeatureTypeWGS84Bounds(typeName);
        final CoordinateReferenceSystem ftypeCrs = getFeatureTypeCRS(typeName);

        ReferencedEnvelope nativeBounds;
        try {
            nativeBounds = wgs84Bounds.transform(ftypeCrs, true);
        } catch (TransformException e) {
            LOGGER.log(Level.WARNING, "Can't transform bounds of " + typeName + " to "
                    + wfs.getDefaultCRS(typeName), e);
            nativeBounds = new ReferencedEnvelope(ftypeCrs);
        } catch (FactoryException e) {
            LOGGER.log(Level.WARNING, "Can't transform bounds of " + typeName + " to "
                    + wfs.getDefaultCRS(typeName), e);
            nativeBounds = new ReferencedEnvelope(ftypeCrs);
        }
        return nativeBounds;
    }

    public CoordinateReferenceSystem getFeatureTypeCRS(String typeName) {
        final String defaultCRS = wfs.getDefaultCRS(typeName);
        CoordinateReferenceSystem crs = null;
        try {
            crs = CRS.decode(defaultCRS);
        } catch (NoSuchAuthorityCodeException e) {
            LOGGER.info("Authority not found for " + typeName + " CRS: " + defaultCRS);
            // HACK HACK HACK!: remove when
            // http://jira.codehaus.org/browse/GEOT-1659 is fixed
            if (defaultCRS.toUpperCase().startsWith("URN")) {
                String code = defaultCRS.substring(defaultCRS.lastIndexOf(":") + 1);
                String epsgCode = "EPSG:" + code;
                try {
                    crs = CRS.decode(epsgCode);
                } catch (Exception e1) {
                    LOGGER.log(Level.WARNING, "can't decode CRS " + epsgCode + " for " + typeName);
                }
            }
        } catch (FactoryException e) {
            LOGGER.log(Level.WARNING, "Error creating CRS " + typeName + ": " + defaultCRS, e);
        }
        return crs;
    }

    public Set<String> getFeatureTypeKeywords(String typeName) {
        return wfs.getFeatureTypeKeywords(typeName);
    }

    public URL getDescribeFeatureTypeURL(String typeName) {
        return wfs.getDescribeFeatureTypeURLGet(typeName);
    }

    public String getServiceAbstract() {
        return wfs.getServiceAbstract();
    }

    public Set<String> getServiceKeywords() {
        return wfs.getServiceKeywords();
    }

    public URI getServiceProviderUri() {
        return wfs.getServiceProviderUri();
    }

    public boolean supportsOperation(WFSOperationType operation, HttpMethod method) {
        return wfs.supportsOperation(operation, method);
    }

    public URL getOperationURL(WFSOperationType operation, HttpMethod method) {
        return wfs.getOperationURL(operation, method);
    }

    public String getServiceTitle() {
        return wfs.getServiceTitle();
    }

    public String getServiceVersion() {
        return wfs.getServiceVersion().toString();
    }

    /**
     * Only returns the bounds of the query (ie, the bounds of the whole feature type) if the query
     * has no filter set, otherwise the bounds may be too expensive to acquire.
     * 
     * @param query
     * @return The bounding box of the datasource in the CRS required by the query, or {@code null}
     *         if unknown and too expensive for the method to calculate or any errors occur.
     */
    public ReferencedEnvelope getBounds(final Query query) throws IOException {
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
     * @return the number of features returned by a GetFeature?resultType=hits request, or {@code
     *         -1} if not supported
     */
    public int getCount(final Query query) throws IOException {
        // TODO: issue only if filter is fully supported
        int hits = wfs.getFeatureHits(query);
        return hits;
    }

    /**
     * @return which http method to use depending on the {@link #preferPostOverGet} preference and
     *         what the server actually supports
     */
    private HttpMethod findWhichMethodToUseFor(final WFSOperationType operation) {
        if (preferPostOverGet) {
            if (wfs.supportsOperation(operation, POST)) {
                return POST;
            }
        }
        if (wfs.supportsOperation(operation, GET)) {
            return GET;
        }
        throw new IllegalArgumentException("Neither POST nor GET method is supported for the "
                + operation + " operation by the server");
    }
}
