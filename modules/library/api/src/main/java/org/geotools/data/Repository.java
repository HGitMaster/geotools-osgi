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
package org.geotools.data;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


/**
 * Provides a Repository of available FeatureTypes allowing Catalog metadata queries.
 *
 * <p>
 * Currently GeoServer is providing requirements:
 * </p>
 *
 * <ul>
 * <li>
 * Manage cross DataStore concepts (like Locks)
 * </li>
 * <li>
 * Provide metadata information on FeatureType
 * </li>
 * </ul>
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/api/src/main/java/org/geotools/data/Repository.java $
 */
public interface Repository {
    /**
     * All FeatureSources by typeRef ( aka dataStoreId:typeName)
     */
    public SortedMap<String,FeatureSource<?,?>> getFeatureSources();

    /**
     * Retrieve Set of Namespaces prefixes registered by DataStores in this
     * Catalog.
     *
     * <p>
     * Namespace seems to be the gml prefix used when writing out GML. We may
     * need to promote this to a "first class" object.
     * </p>
     *
     * <p>
     * GeoServer maintains the following information in a  NamespaceInfo
     * object:
     * </p>
     *
     * <ul>
     * <li>
     * prefix: uml prefix representing the namespace
     * </li>
     * <li>
     * uri: uri used to reference namespace
     * </li>
     * <li>
     * default: true if this is the "Default" namespace for the Catalog
     * </li>
     * </ul>
     *
     * <p>
     * GeoServer global.Data implements this interface. You may use the
     * namespace strings returned by this method to look up NamespaceInfo
     * objets by prefix.
     * </p>
     *
     * @return Set of available Namespace prefixes.
     */
    Set<String> getPrefixes() throws IOException;

    /**
     * The default Namespace prefix for this Catalog.
     * @return Namespace prefix to be used as a default
     */

    //String getDefaultPrefix();    

    /**
     * FeatureSoruce access.
     * </p>
     * @param dataStoreId
     * @param typeName
     */
    FeatureSource<?,?> source(String dataStoreId, String typeName)
        throws IOException;

    /**
     * Registers all FeatureTypes provided by dataStore with this catalog
     * service.
     *
     * <p>
     * Catalog can be seen as aggregating multiple DataStores and providing
     * higher level functionality. Such as derived metadata like lat long
     * bounding box information.
     * </p>
     *
     * <p>
     * The Catalog may choose to supplement the information provided by the
     * DataStore with information provided from elsewhere (like config files).
     * </p>
     *
     * <p>
     * The namespace declared by the FeatureTypes will be lazly created if it
     * has not already been provided. There may be no duplication of typeName
     * within one Namespace.
     * </p>
     *
     * @param namespace Catalog namespace
     * @param dataStore Datastore providing FeatureTypes
     *
     * @throws IOException If registration fails such as for namespace conflict
     */

    //void register( String dataStoreId, DataStore dataStore) throws IOException;

    /**
     * Access to the DataStores registed to this Catalog.
     *
     * @return Map of registered dataStoreId:DataStore
     */
    Map<String,DataStore> getDataStores();

    //
    // Lock Management
    //

    /**
     * Refresh feature lock as indicated by the WFS locking specification.
     *
     * <p>
     * Refresh the indicated locks for each each DataStore managed by this
     * Catalog.
     * </p>
     *
     * @param lockID Authorization identifing lock
     * @param transaction Transaction with authorization for lock
     *
     * @return true if lock was found and refreshed
     *
     * @throws IOException If a problem occurs
     */
    boolean lockRefresh(String lockID, Transaction transaction)
        throws IOException;

    /**
     * Release feature lock by lockID.
     * <p>
     * Release the indicated locks for each each DataStore managed by this
     * Catalog.
     * </p>
     *
     * @param lockID Authorization identifing lock
     * @param transaction Transaction with authorization for lock
     *
     * @return true if lock was found and released
     *
     * @throws IOException If a problem occurs
     */
    boolean lockRelease(String lockID, Transaction transaction)
        throws IOException;

    /**
     * Tests if a lock exists in this Catalog.
     *
     * <p>
     * This method will search all the DataStores to see if the indicated lock
     * exists.
     * </p>
     *
     * @param lockID Authorization identifing lock
     *
     * @return true if lock was found
     */
    boolean lockExists(String lockID);
}
