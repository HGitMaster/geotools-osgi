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

package org.geotools.data.complex.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.tools.ResolvingXMLReader;
import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureSource;
import org.geotools.data.complex.AttributeMapping;
import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.data.complex.filter.XPath;
import org.geotools.data.complex.filter.XPath.Step;
import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.feature.Types;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.filter.expression.Expression;
import org.xml.sax.helpers.NamespaceSupport;
/**
 * Utility class to create a set of {@linkPlain org.geotools.data.complex.FeatureTypeMapping}
 * objects from a complex datastore's configuration object ({@link
 * org.geotools.data.complex.config.ComplexDataStoreDTO}).
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: ComplexDataStoreConfigurator.java 31514 2008-09-15 08:36:50Z bencd $
 * @source $URL:
 *         http://svn.geotools.org/trunk/modules/unsupported/community-schemas/community-schema-ds/src/main/java/org/geotools/data/complex/config/ComplexDataStoreConfigurator.java $
 * @since 2.4
 */
public class ComplexDataStoreConfigurator {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(ComplexDataStoreConfigurator.class.getPackage().getName());

    /** DOCUMENT ME! */
    private ComplexDataStoreDTO config;

    private Map typeRegistry;

    private Map descriptorRegistry;

    private Map sourceDataStores;

    /**
     * Placeholder for the prefix:namespaceURI mappings declared in the Namespaces section of the
     * mapping file.
     */
    private NamespaceSupport namespaces;

    /**
     * Creates a new ComplexDataStoreConfigurator object.
     * 
     * @param config
     *                DOCUMENT ME!
     */
    private ComplexDataStoreConfigurator(ComplexDataStoreDTO config) {
        this.config = config;
        namespaces = new NamespaceSupport();
        Map nsMap = config.getNamespaces();
        for (Iterator it = nsMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Entry) it.next();
            String prefix = (String) entry.getKey();
            String namespace = (String) entry.getValue();
            namespaces.declarePrefix(prefix, namespace);
        }
    }

    /**
     * Takes a config object and creates a set of mappings.
     * 
     * <p>
     * In the process will parse xml schemas to geotools' Feature Model types and descriptors,
     * connect to source datastores and build the mapping objects from source FeatureTypes to the
     * target ones.
     * </p>
     * 
     * @param config
     *                DOCUMENT ME!
     * 
     * @return a Set of {@link org.geotools.data.complex.FeatureTypeMapping} source to target
     *         FeatureType mapping definitions
     * 
     * @throws IOException
     *                 if any error occurs while creating the mappings
     */
    public static Set buildMappings(ComplexDataStoreDTO config) throws IOException {
        ComplexDataStoreConfigurator mappingsBuilder;

        mappingsBuilder = new ComplexDataStoreConfigurator(config);
        Set mappingObjects = mappingsBuilder.buildMappings();

        return mappingObjects;
    }

    /**
     * Actually builds the mappings from the config dto.
     * 
     * <p>
     * Build steps are: - parse xml schemas to FM types - connect to source datastores - build
     * mappings
     * </p>
     * 
     * @return
     * 
     * @throws IOException
     *                 DOCUMENT ME!
     */
    private Set buildMappings() throws IOException {
        // -parse target xml schemas, let parsed types on <code>registry</code>
        parseGmlSchemas();

        // -create source datastores
        sourceDataStores = aquireSourceDatastores();

        // -create FeatureType mappings
        Set featureTypeMappings = createFeatureTypeMappings();

        return featureTypeMappings;
    }

    private Set createFeatureTypeMappings() throws IOException {
        Set mappingsConfigs = config.getTypeMappings();

        Set featureTypeMappings = new HashSet();

        for (Iterator it = mappingsConfigs.iterator(); it.hasNext();) {
            TypeMapping dto = (TypeMapping) it.next();

            FeatureSource featureSource = getFeatureSource(dto);
            AttributeDescriptor target = getTargetDescriptor(dto);
            List attMappings = getAttributeMappings(target, dto.getAttributeMappings());

            FeatureTypeMapping mapping;

            mapping = new FeatureTypeMapping(featureSource, target, attMappings, namespaces);

            featureTypeMappings.add(mapping);
        }
        return featureTypeMappings;
    }

    private AttributeDescriptor getTargetDescriptor(TypeMapping dto) throws IOException {
        if (descriptorRegistry == null) {
            throw new IllegalStateException("schemas not yet parsed");
        }

        String prefixedTargetName = dto.getTargetElementName();
        Name targetNodeName = degloseName(prefixedTargetName);

        AttributeDescriptor targetDescriptor;
        targetDescriptor = (AttributeDescriptor) descriptorRegistry.get(targetNodeName);
        if (targetDescriptor == null) {
            throw new NoSuchElementException("descriptor " + targetNodeName
                    + " not found in parsed schema");
        }
        return targetDescriptor;
    }

    /**
     * Creates a list of {@link org.geotools.data.complex.AttributeMapping} from the attribute
     * mapping configurations in the provided list of {@link AttributeMapping}
     * 
     * @param attDtos
     * @return
     */
    private List getAttributeMappings(final AttributeDescriptor root, final List attDtos)
            throws IOException {
        List attMappings = new LinkedList();

        for (Iterator it = attDtos.iterator(); it.hasNext();) {

            org.geotools.data.complex.config.AttributeMapping attDto;
            attDto = (org.geotools.data.complex.config.AttributeMapping) it.next();

            String idExpr = attDto.getIdentifierExpression();
            String sourceExpr = attDto.getSourceExpression();
            String expectedInstanceTypeName = attDto.getTargetAttributeSchemaElement();

            final String targetXPath = attDto.getTargetAttributePath();
            final StepList targetXPathSteps = XPath.steps(root, targetXPath, namespaces);
            validateConfiguredNamespaces(targetXPathSteps);

            final boolean isMultiValued = attDto.isMultiple();

            final Expression idExpression = parseOgcCqlExpression(idExpr);
            final Expression sourceExpression = parseOgcCqlExpression(sourceExpr);

            final AttributeType expectedInstanceOf;

            final Map clientProperties = getClientProperties(attDto);

            if (expectedInstanceTypeName != null) {
                Name expectedNodeTypeName = null;
                expectedNodeTypeName = degloseTypeName(expectedInstanceTypeName);
                expectedInstanceOf = (AttributeType) typeRegistry.get(expectedNodeTypeName);
                if (expectedInstanceOf == null) {
                    String msg = "mapping expects and instance of " + expectedNodeTypeName
                            + " for attribute " + targetXPath
                            + " but the attribute descriptor was not found";
                    throw new DataSourceException(msg);
                }
            } else {
                expectedInstanceOf = null;
            }

            AttributeMapping attMapping = new AttributeMapping(idExpression, sourceExpression,
                    targetXPathSteps, expectedInstanceOf, isMultiValued, clientProperties);
            attMappings.add(attMapping);
        }
        return attMappings;
    }

    /**
     * Throws an IllegalArgumentException if some Step in the given xpath StepList has a prefix for
     * which no prefix to namespace mapping were provided (as in the Namespaces section of the
     * mappings xml configuration file)
     * 
     * @param targetXPathSteps
     */
    private void validateConfiguredNamespaces(StepList targetXPathSteps) {
        for (Iterator it = targetXPathSteps.iterator(); it.hasNext();) {
            Step step = (Step) it.next();
            QName name = step.getName();
            if (!XMLConstants.DEFAULT_NS_PREFIX.equals(name.getPrefix())) {
                if (XMLConstants.DEFAULT_NS_PREFIX.equals(name.getNamespaceURI())) {
                    throw new IllegalArgumentException("location step " + step + " has prefix "
                            + name.getPrefix() + " for which no namespace was set. "
                            + "(Check the Namespaces section in the config file)");
                }
            }
        }
    }

    private Expression parseOgcCqlExpression(String sourceExpr) throws DataSourceException {
        Expression expression = Expression.NIL;
        if (sourceExpr != null && sourceExpr.trim().length() > 0) {
            try {
                expression = CQL.toExpression(sourceExpr);
            } catch (CQLException e) {
                String formattedErrorMessage = e.getMessage();
                ComplexDataStoreConfigurator.LOGGER.log(Level.SEVERE, formattedErrorMessage, e);
                throw new DataSourceException("Error parsing CQL expression " + sourceExpr + ":\n"
                        + formattedErrorMessage);
            } catch (Exception e) {
                e.printStackTrace();
                String msg = "parsing expression " + sourceExpr;
                ComplexDataStoreConfigurator.LOGGER.log(Level.SEVERE, msg, e);
                throw new DataSourceException(msg + ": " + e.getMessage(), e);
            }
        }
        return expression;
    }

    /**
     * 
     * @param dto
     * @return Map&lt;Name, Expression&gt; with the values per qualified name (attribute name in the
     *         mapping)
     * @throws DataSourceException
     */
    private Map getClientProperties(org.geotools.data.complex.config.AttributeMapping dto)
            throws DataSourceException {

        if (dto.getClientProperties().size() == 0) {
            return Collections.EMPTY_MAP;
        }

        Map clientProperties = new HashMap();
        for (Iterator it = dto.getClientProperties().entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String name = (String) entry.getKey();
            Name qName = degloseName(name);
            String cqlExpression = (String) entry.getValue();
            Expression expression = parseOgcCqlExpression(cqlExpression);
            clientProperties.put(qName, expression);
        }
        return clientProperties;
    }

    private FeatureSource getFeatureSource(TypeMapping dto) throws IOException {
        String dsId = dto.getSourceDataStore();
        String typeName = dto.getSourceTypeName();

        DataAccess sourceDataStore = (DataAccess) sourceDataStores.get(dsId);
        if (sourceDataStore == null) {
            throw new DataSourceException("datastore " + dsId + " not found for type mapping "
                    + dto);
        }

        ComplexDataStoreConfigurator.LOGGER.fine("asking datastore " + sourceDataStore
                + " for source type " + typeName);
        Name name = degloseName(typeName);
        FeatureSource fSource = (FeatureSource) sourceDataStore.getFeatureSource(name);
        ComplexDataStoreConfigurator.LOGGER.fine("found feature source for " + typeName);
        return fSource;
    }

    /**
     * Parses the target xml schema files and stores the generated types in {@link #typeRegistry}
     * and AttributeDescriptors in {@link #descriptorRegistry}.
     * 
     * <p>
     * The list of file names to parse is obtained from config.getTargetSchemasUris(). If a file
     * name contained in that list is a relative path (i.e., does not starts with file: or http:,
     * config.getBaseSchemasUrl() is used to resolve relative paths against.
     * </p>
     * 
     * @throws IOException
     */
    private void parseGmlSchemas() throws IOException {
        ComplexDataStoreConfigurator.LOGGER.finer("about to parse target schemas");

        final URL baseUrl = new URL(config.getBaseSchemasUrl());

        final List schemaFiles = config.getTargetSchemasUris();

        final Catalog oasisCatalog = getCatalog();
        EmfAppSchemaReader schemaParser;
        schemaParser = EmfAppSchemaReader.newInstance();
        schemaParser.setCatalog(oasisCatalog);

        for (Iterator it = schemaFiles.iterator(); it.hasNext();) {
            String schemaLocation = (String) it.next();
            final URL schemaUrl = resolveResourceLocation(baseUrl, schemaLocation);
            ComplexDataStoreConfigurator.LOGGER
                    .fine("parsing schema " + schemaUrl.toExternalForm());

            schemaParser.parse(schemaUrl);
        }

        typeRegistry = schemaParser.getTypeRegistry();
        descriptorRegistry = schemaParser.getDescriptorRegistry();
    }

    private Catalog getCatalog() throws MalformedURLException, IOException {
        Catalog oasisCatalog = null;
        String catalogLocation = config.getCatalog();
        if (catalogLocation != null) {
            final URL baseUrl = new URL(config.getBaseSchemasUrl());
            final URL resolvedResourceLocation = resolveResourceLocation(baseUrl, catalogLocation);
            catalogLocation = resolvedResourceLocation.toExternalForm();
            boolean exists = resourceExists(resolvedResourceLocation);
            if (!exists) {
                throw new FileNotFoundException("Catalog file does not exists: " + catalogLocation);
            }
            final ResolvingXMLReader reader = new ResolvingXMLReader();
            final Catalog catalog = reader.getCatalog();
            catalog.getCatalogManager().setVerbosity(9);
            catalog.getCatalogManager().setIgnoreMissingProperties(false);
            catalog.parseCatalog(catalogLocation);
            oasisCatalog = catalog;
        }
        return oasisCatalog;
    }

    private boolean resourceExists(final URL resolvedResourceLocation) {
        InputStream in;
        try {
            in = resolvedResourceLocation.openStream();
            in.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private URL resolveResourceLocation(final URL baseUrl, String schemaLocation)
            throws MalformedURLException {
        final URL schemaUrl;
        if (schemaLocation.startsWith("file:") || schemaLocation.startsWith("http:")) {
            ComplexDataStoreConfigurator.LOGGER.fine("using resource location as absolute path: "
                    + schemaLocation);
            schemaUrl = new URL(schemaLocation);
        } else {
            if (baseUrl == null) {
                schemaUrl = new URL(schemaLocation);
                ComplexDataStoreConfigurator.LOGGER
                        .warning("base url not provided, may be unable to locate" + schemaLocation
                                + ". Path resolved to: " + schemaUrl.toExternalForm());
            } else {
                ComplexDataStoreConfigurator.LOGGER.fine("using schema location " + schemaLocation
                        + " as relative to " + baseUrl);
                schemaUrl = new URL(baseUrl, schemaLocation);
            }
        }
        return schemaUrl;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return a Map&lt;String,DataStore&gt; where the key is the id given to the datastore in the
     *         configuration.
     * 
     * @throws IOException
     * @throws DataSourceException
     *                 DOCUMENT ME!
     */
    private Map/* <String, FeatureAccess> */aquireSourceDatastores() throws IOException {
        ComplexDataStoreConfigurator.LOGGER
                .entering(getClass().getName(), "aquireSourceDatastores");

        final Map datastores = new HashMap();
        final List dsParams = config.getSourceDataStores();
        String id;

        for (Iterator it = dsParams.iterator(); it.hasNext();) {
            SourceDataStore dsconfig = (SourceDataStore) it.next();
            id = dsconfig.getId();

            Map datastoreParams = dsconfig.getParams();

            datastoreParams = resolveRelativePaths(datastoreParams);

            ComplexDataStoreConfigurator.LOGGER.fine("looking for datastore " + id);

            DataAccess dataStore = DataAccessFinder.getDataStore(datastoreParams);

            if (dataStore == null) {
                throw new DataSourceException("Cannot find a DataAccess for parameters "
                        + datastoreParams);
            }

            ComplexDataStoreConfigurator.LOGGER.fine("got datastore " + dataStore);
            datastores.put(id, dataStore);
        }

        return datastores;
    }

    /**
     * Resolves any source datastore parameter settled as a file path relative to the location of
     * the xml mappings configuration file as an absolute path and returns a new Map with it.
     * 
     * @param datastoreParams
     * @return
     * @throws MalformedURLException
     */
    private Map resolveRelativePaths(final Map datastoreParams) throws MalformedURLException {
        Map resolvedParams = new HashMap();

        for (Iterator it = datastoreParams.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (value != null && value.startsWith("file:")) {
                value = value.substring("file:".length());
                File f = new File(value);
                if (!f.isAbsolute()) {
                    LOGGER.fine("resolving relative path " + value + " for dataURLstore parameter "
                            + key);
                    URL baseSchemasUrl = new URL(config.getBaseSchemasUrl());
                    URL resolvedUrl = new URL(baseSchemasUrl, value);
                    value = resolvedUrl.toExternalForm();
                    // HACK for shapefile: shapefile requires file:/...
                    if (!"url".equals(key) && value.startsWith("file:")) {
                        value = value.substring("file:".length());
                    }
                    LOGGER.fine("new value for " + key + ": " + value);
                }
            }

            resolvedParams.put(key, value);
        }

        return resolvedParams;
    }

    /**
     * Takes a prefixed attribute name and returns an {@link Name} by looking which namespace
     * belongs the prefix to in {@link ComplexDataStoreDTO#getNamespaces()}.
     * 
     * @param prefixedName
     * @return
     * @throws IllegalArgumentException
     *                 if <code>prefixedName</code> has no prefix.
     */
    private Name degloseTypeName(String prefixedName) throws IllegalArgumentException {
        Name name = null;

        if (prefixedName == null) {
            return null;
        }

        int prefixIdx = prefixedName.indexOf(':');
        if (prefixIdx == -1) {
            return Types.typeName(prefixedName);
            // throw new IllegalArgumentException(prefixedName + " is not
            // prefixed");
        }

        String nsPrefix = prefixedName.substring(0, prefixIdx);
        String localName = prefixedName.substring(prefixIdx + 1);
        String nsUri = namespaces.getURI(nsPrefix);

        name = Types.typeName(nsUri, localName);

        return name;
    }

    private Name degloseName(String prefixedName) throws IllegalArgumentException {
        Name name = null;

        if (prefixedName == null) {
            return null;
        }

        int prefixIdx = prefixedName.indexOf(':');
        if (prefixIdx == -1) {
            return Types.typeName(prefixedName);
            // throw new IllegalArgumentException(prefixedName + " is not
            // prefixed");
        }

        String nsPrefix = prefixedName.substring(0, prefixIdx);
        String localName = prefixedName.substring(prefixIdx + 1);
        String nsUri = namespaces.getURI(nsPrefix);

        name = Types.typeName(nsUri, localName);

        return name;
    }
}
