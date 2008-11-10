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
package org.geotools.data.wfs.v1_1_0;

import static org.geotools.data.wfs.protocol.wfs.WFSOperationType.GET_FEATURE;

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

import net.opengis.wfs.GetFeatureType;

import org.geotools.data.DataAccess;
import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultQuery;
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
import org.geotools.data.crs.ReprojectFeatureReader;
import org.geotools.data.view.DefaultView;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSServiceInfo;
import org.geotools.data.wfs.protocol.wfs.GetFeatureParser;
import org.geotools.data.wfs.protocol.wfs.WFSException;
import org.geotools.data.wfs.protocol.wfs.WFSExtensions;
import org.geotools.data.wfs.protocol.wfs.WFSOperationType;
import org.geotools.data.wfs.protocol.wfs.WFSProtocol;
import org.geotools.data.wfs.protocol.wfs.WFSResponse;
import org.geotools.data.wfs.v1_1_0.WFSStrategy.RequestComponents;
import org.geotools.data.wfs.v1_1_0.parsers.EmfAppSchemaParser;
import org.geotools.feature.NameImpl;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 * A WFS 1.1 DataStore implementation.
 * <p>
 * Note with the current design, this class is meant to be pulled up as the single WFS DataStore
 * implementation regardless of the WFS version, since the protocol version specifics is meant to be
 * handled by the {@link WFSProtocol} implementation provided to this class. For the time being,
 * while there are no resources to spend on porting the WFS 1.0.0 datastore to the new design, this
 * keeps here in this 1.1 specific package.
 * </p>
 * 
 * @author Gabriel Roldan
 * @version $Id: WFS_1_1_0_DataStore.java 31817 2008-11-10 22:21:18Z groldan $
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/main/java/org/geotools
 *         /wfs/v_1_1_0/data/WFSDataStore.java $
 */
@SuppressWarnings( { "nls" })
public final class WFS_1_1_0_DataStore implements WFSDataStore {
    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");

    private final WFSProtocol wfs;

    final WFSStrategy strategy;

    private final String DEFAULT_OUTPUT_FORMAT;

    private Map<String, SimpleFeatureType> byTypeNameTypes;

    private Integer maxFeaturesHardLimit;

    private boolean preferPostOverGet = false;

    /**
     * The WFS capabilities document.
     * 
     * @param capabilities
     */
    public WFS_1_1_0_DataStore(final WFSProtocol wfs, final WFSStrategy strategy) {
        this.wfs = wfs;
        this.strategy = strategy;
        this.DEFAULT_OUTPUT_FORMAT = wfs.getDefaultOutputFormat();
        byTypeNameTypes = Collections.synchronizedMap(new HashMap<String, SimpleFeatureType>());
        maxFeaturesHardLimit = Integer.valueOf(0); // not set
    }

    /**
     * @see WFSDataStore#setMaxFeatures(Integer)
     */
    public void setMaxFeatures(Integer maxFeatures) {
        this.maxFeaturesHardLimit = Integer.valueOf(maxFeatures.intValue());
    }

    public Integer getMaxFeatures() {
        return this.maxFeaturesHardLimit;
    }

    /**
     * @see WFSDataStore#getInfo()
     */
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
            // String outputFormat = DEFAULT_OUTPUT_FORMAT;
            // WFSResponse response;
            // if (useHttpPostFor(DESCRIBE_FEATURETYPE)) {
            // response = wfs.describeFeatureTypePOST(prefixedTypeName, outputFormat);
            // } else {
            // response = wfs.describeFeatureTypeGET(prefixedTypeName, outputFormat);
            // }
            //
            // WFSResponseParser parser = WFSExtensions.findParser(response);

            final QName featureDescriptorName;
            try {
                featureDescriptorName = wfs.getFeatureTypeName(prefixedTypeName);
            } catch (IllegalArgumentException e) {
                throw new SchemaNotFoundException(prefixedTypeName);
            }

            final URL describeUrl = wfs.getDescribeFeatureTypeURLGet(prefixedTypeName);
            // @TODO remove this
            System.err.println("DecribeFT URL for " + prefixedTypeName + ": " + describeUrl);

            final SimpleFeatureType featureType;
            CoordinateReferenceSystem crs = getFeatureTypeCRS(prefixedTypeName);
            featureType = EmfAppSchemaParser.parseSimpleFeatureType(featureDescriptorName,
                    describeUrl, crs);

            // adapt the feature type name
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.init(featureType);
            builder.setName(prefixedTypeName);
            builder.setNamespaceURI(featureDescriptorName.getNamespaceURI());
            GeometryDescriptor defaultGeometry = featureType.getGeometryDescriptor();
            if (defaultGeometry != null) {
                builder.setDefaultGeometry(defaultGeometry.getLocalName());
                builder.setCRS(defaultGeometry.getCoordinateReferenceSystem());
            }
            final SimpleFeatureType adaptedFeatureType = builder.buildFeatureType();
            ftype = adaptedFeatureType;
            byTypeNameTypes.put(prefixedTypeName, ftype);
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
        wfs.dispose();
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.data.Query,
     *      org.geotools.data.Transaction)
     */
    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(Query query,
            final Transaction transaction) throws IOException {

        if (Filter.EXCLUDE.equals(query.getFilter())) {
            return new EmptyFeatureReader<SimpleFeatureType, SimpleFeature>(getQueryType(query));
        }

        // TODO: handle output format preferences
        final String outputFormat = DEFAULT_OUTPUT_FORMAT;
        final CoordinateReferenceSystem queryCrs = query.getCoordinateSystem();

        query = new DefaultQuery(query);
        final RequestComponents requestParts = strategy.createGetFeatureRequest(this, wfs,
                (DefaultQuery) query, outputFormat);
        final GetFeatureType wfsRequest = requestParts.getServerRequest();
        final Filter postFilter = requestParts.getPostFilter();
        final WFSResponse response = sendGetFeatures(wfsRequest);

        Object result = WFSExtensions.process(this, response);

        GetFeatureParser parser;
        if (result instanceof WFSException) {
            // try to recover from common server implementation errors
            throw (WFSException) result;
        } else if (result instanceof GetFeatureParser) {
            parser = (GetFeatureParser) result;
        } else {
            throw new IllegalStateException("Unknown response result for GetFeature: " + result);
        }

        final SimpleFeatureType contentType = getQueryType(query);

        FeatureReader<SimpleFeatureType, SimpleFeature> reader;
        reader = new WFSFeatureReader((GetFeatureParser) parser);

        if (!reader.hasNext()) {
            return new EmptyFeatureReader<SimpleFeatureType, SimpleFeature>(contentType);
        }

        final SimpleFeatureType readerType = reader.getFeatureType();

        CoordinateReferenceSystem readerCrs = readerType.getCoordinateReferenceSystem();
        if (queryCrs != null && !queryCrs.equals(readerCrs)) {
            try {
                reader = new ReprojectFeatureReader(reader, queryCrs);
            } catch (Exception e) {
                throw new DataSourceException(e);
            }
        }

        if (Filter.EXCLUDE != postFilter) {
            reader = new FilteringFeatureReader<SimpleFeatureType, SimpleFeature>(reader, query
                    .getFilter());
        }

        if (!contentType.equals(readerType)) {
            final boolean cloneContents = false;
            reader = new ReTypeFeatureReader(reader, contentType, cloneContents);
        }

        if (this.maxFeaturesHardLimit.intValue() > 0 || query.getMaxFeatures() != Integer.MAX_VALUE) {
            int maxFeatures = Math.min(maxFeaturesHardLimit.intValue(), query.getMaxFeatures());
            reader = new MaxFeatureReader<SimpleFeatureType, SimpleFeature>(reader, maxFeatures);
        }
        return reader;
    }

    private WFSResponse sendGetFeatures(GetFeatureType request) throws IOException {
        // TODO: split filters! WFSProtocol is not responsible of doing so
        final WFSResponse response;
        if (useHttpPostFor(GET_FEATURE)) {
            response = wfs.getFeaturePOST(request);
        } else {
            response = wfs.getFeatureGET(request);
        }
        return response;
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
        return strategy.getQueryType(this, query);
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

    public QName getFeatureTypeName(String typeName) {
        return wfs.getFeatureTypeName(typeName);
    }

    public String getFeatureTypeTitle(String typeName) {
        return wfs.getFeatureTypeTitle(typeName);
    }

    public String getFeatureTypeAbstract(String typeName) {
        return wfs.getFeatureTypeAbstract(typeName);
    }

    public ReferencedEnvelope getFeatureTypeWGS84Bounds(String typeName) {
        return wfs.getFeatureTypeWGS84Bounds(typeName);
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

    public boolean supportsOperation(WFSOperationType operation, boolean post) {
        return wfs.supportsOperation(operation, post);
    }

    public URL getOperationURL(WFSOperationType operation, boolean post) {
        return wfs.getOperationURL(operation, post);
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
     * @return
     *         <code>true<code> if HTTP POST method should be used to issue the given WFS operation, <code>false</code>
     *         if HTTP GET method should be used instead
     */
    private boolean useHttpPostFor(final WFSOperationType operation) {
        if (preferPostOverGet) {
            if (wfs.supportsOperation(operation, true)) {
                return true;
            }
        }
        if (wfs.supportsOperation(operation, false)) {
            return false;
        }
        throw new IllegalArgumentException("Neither POST nor GET method is supported for the "
                + operation + " operation by the server");
    }
}
