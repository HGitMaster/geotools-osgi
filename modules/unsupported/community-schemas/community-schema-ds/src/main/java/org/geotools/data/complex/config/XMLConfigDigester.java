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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

/**
 * DOCUMENT ME!
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: XMLConfigDigester.java 31784 2008-11-06 06:20:21Z bencd $
 * @source $URL:
 *         http://svn.geotools.org/trunk/modules/unsupported/community-schemas/community-schema-ds/src/main/java/org/geotools/data/complex/config/XMLConfigDigester.java $
 * @since 2.4
 */
public class XMLConfigDigester {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(XMLConfigDigester.class.getPackage().getName());

    /** Namespace URI for the ComplexDataStore configuration files */
    private static final String CONFIG_NS_URI = "http://www.geotools.org/complex";

    /**
     * Creates a new XMLConfigReader object.
     */
    public XMLConfigDigester() {
        super();
    }

    /**
     * Parses a complex datastore configuration file in xml format into a
     * {@link AppSchemaDataAccessDTO}
     * 
     * @param dataStoreConfigUrl
     *                config file location
     * 
     * @return a DTO object representing the datastore's configuration
     * 
     * @throws IOException
     *                 if an error occurs parsing the file
     */
    public AppSchemaDataAccessDTO parse(URL dataStoreConfigUrl) throws IOException {
        AppSchemaDataAccessDTO config = digest(dataStoreConfigUrl);
        return config;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param dataStoreConfigUrl
     *                DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws IOException
     *                 DOCUMENT ME!
     * @throws NullPointerException
     *                 DOCUMENT ME!
     */
    private AppSchemaDataAccessDTO digest(final URL dataStoreConfigUrl) throws IOException {
        if (dataStoreConfigUrl == null) {
            throw new NullPointerException("datastore config url");
        }

        InputStream configStream = dataStoreConfigUrl.openStream();

        if (configStream == null) {
            throw new IOException("Can't open datastore config file " + dataStoreConfigUrl);
        }

        XMLConfigDigester.LOGGER.fine("parsing complex datastore config: "
                + dataStoreConfigUrl.toExternalForm());

        Digester digester = new Digester();
        XMLConfigDigester.LOGGER.fine("digester created");
        // URL schema = getClass()
        // .getResource("../test-data/ComplexDataStore.xsd");
        // digester.setSchema(schema.toExternalForm());
        digester.setValidating(false);
        digester.setNamespaceAware(true);
        digester.setRuleNamespaceURI(XMLConfigDigester.CONFIG_NS_URI);

        // digester.setRuleNamespaceURI(OGC_NS_URI);
        AppSchemaDataAccessDTO configDto = new AppSchemaDataAccessDTO();
        configDto.setBaseSchemasUrl(dataStoreConfigUrl.toExternalForm());

        digester.push(configDto);

        try {
            setNamespacesRules(digester);

            setSourceDataStoresRules(digester);

            setTargetSchemaUriRules(digester);

            setTypeMappingsRules(digester);

        } catch (Exception e) {
            e.printStackTrace();
            XMLConfigDigester.LOGGER.log(Level.SEVERE, "setting digester properties: ", e);
            throw new IOException("Error setting digester properties: " + e.getMessage());
        }

        try {
            digester.parse(configStream);
        } catch (SAXException e) {
            e.printStackTrace();
            XMLConfigDigester.LOGGER.log(Level.SEVERE, "parsing " + dataStoreConfigUrl, e);

            IOException ioe = new IOException("Can't parse complex datastore config. ");
            ioe.initCause(e);
            throw ioe;
        }

        AppSchemaDataAccessDTO config = (AppSchemaDataAccessDTO) digester.getRoot();

        return config;
    }

    private void setTypeMappingsRules(Digester digester) {
        final String mappings = "ComplexDataStore/typeMappings";
        digester.addObjectCreate(mappings, XMLConfigDigester.CONFIG_NS_URI, HashSet.class);

        final String typeMapping = mappings + "/FeatureTypeMapping";

        digester.addObjectCreate(typeMapping, XMLConfigDigester.CONFIG_NS_URI, TypeMapping.class);
        digester.addCallMethod(typeMapping + "/sourceDataStore", "setSourceDataStore", 1);
        digester.addCallParam(typeMapping + "/sourceDataStore", 0);
        digester.addCallMethod(typeMapping + "/sourceType", "setSourceTypeName", 1);
        digester.addCallParam(typeMapping + "/sourceType", 0);
        digester.addCallMethod(typeMapping + "/targetElement", "setTargetElementName", 1);
        digester.addCallParam(typeMapping + "/targetElement", 0);

        // create attribute mappings
        final String attMappings = typeMapping + "/attributeMappings";
        digester.addObjectCreate(attMappings, XMLConfigDigester.CONFIG_NS_URI, ArrayList.class);

        final String attMap = attMappings + "/AttributeMapping";
        digester.addObjectCreate(attMap, XMLConfigDigester.CONFIG_NS_URI, AttributeMapping.class);

        digester.addCallMethod(attMap + "/isMultiple", "setMultiple", 1);
        digester.addCallParam(attMap + "/isMultiple", 0);

        digester.addCallMethod(attMap + "/targetAttribute", "setTargetAttributePath", 1);
        digester.addCallParam(attMap + "/targetAttribute", 0);

        digester.addCallMethod(attMap + "/targetAttributeNode", "setTargetAttributeSchemaElement",
                1);
        digester.addCallParam(attMap + "/targetAttributeNode", 0);

        digester.addCallMethod(attMap + "/idExpression/OCQL", "setIdentifierExpression", 1);
        digester.addCallParam(attMap + "/idExpression/OCQL", 0);

        digester.addCallMethod(attMap + "/sourceExpression/OCQL", "setSourceExpression", 1);
        digester.addCallParam(attMap + "/sourceExpression/OCQL", 0);

        digester.addCallMethod(attMap + "/ClientProperty", "putClientProperty", 2);
        digester.addCallParam(attMap + "/ClientProperty/name", 0);
        digester.addCallParam(attMap + "/ClientProperty/value", 1);

        // add the AttributeMapping to the list
        digester.addSetNext(attMap, "add");

        // set attribute mappings
        digester.addSetNext(attMappings, "setAttributeMappings");

        // add the TypeMapping to the Set
        digester.addSetNext(typeMapping, "add");

        // set the TypeMapping on ComplexDataStoreDTO
        digester.addSetNext(mappings, "setTypeMappings");
    }

    private void setTargetSchemaUriRules(Digester digester) {
        final String targetSchemas = "ComplexDataStore/targetTypes";

        digester.addBeanPropertySetter("ComplexDataStore/catalog");
        // digester.addCallMethod(targetSchemas + "/Catalog", "setCatalog", 1);
        // digester.addCallParam(targetSchemas + "/Catalog", 0);

        digester.addObjectCreate(targetSchemas, XMLConfigDigester.CONFIG_NS_URI, ArrayList.class);

        final String schema = targetSchemas + "/FeatureType/schemaUri";
        digester.addCallMethod(schema, "add", 1);
        digester.addCallParam(schema, 0);
        // set the list of XSD file uris on ComplexDataStoreDTO
        digester.addSetNext(targetSchemas, "setTargetSchemasUris");
    }

    private void setSourceDataStoresRules(Digester digester) {
        final String dataStores = "ComplexDataStore/sourceDataStores";
        digester.addObjectCreate(dataStores, XMLConfigDigester.CONFIG_NS_URI, ArrayList.class);

        // create a SourceDataStore for each DataStore tag
        digester.addObjectCreate(dataStores + "/DataStore", XMLConfigDigester.CONFIG_NS_URI,
                SourceDataStore.class);
        digester.addCallMethod(dataStores + "/DataStore/id", "setId", 1);
        digester.addCallParam(dataStores + "/DataStore/id", 0);

        digester.addObjectCreate(dataStores + "/DataStore/parameters",
                XMLConfigDigester.CONFIG_NS_URI, HashMap.class);
        digester.addCallMethod(dataStores + "/DataStore/parameters/Parameter", "put", 2);
        digester.addCallParam(dataStores + "/DataStore/parameters/Parameter/name", 0);
        digester.addCallParam(dataStores + "/DataStore/parameters/Parameter/value", 1);
        digester.addSetNext(dataStores + "/DataStore/parameters", "setParams");

        // add the SourceDataStore to the list
        digester.addSetNext(dataStores + "/DataStore", "add");

        // set the list of SourceDataStores for ComlexDataStoreDTO
        digester.addSetNext(dataStores, "setSourceDataStores");
    }

    private void setNamespacesRules(Digester digester) {
        final String ns = "ComplexDataStore/namespaces";
        digester.addObjectCreate(ns, XMLConfigDigester.CONFIG_NS_URI, HashMap.class);
        digester.addCallMethod(ns + "/Namespace", "put", 2);
        digester.addCallParam(ns + "/Namespace/prefix", 0);
        digester.addCallParam(ns + "/Namespace/uri", 1);
        digester.addSetNext(ns, "setNamespaces");
    }
}
