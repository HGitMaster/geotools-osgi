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

package org.geotools.data.complex;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.complex.config.AppSchemaDataAccessConfigurator;
import org.geotools.data.complex.config.AppSchemaDataAccessDTO;
import org.geotools.data.complex.config.XMLConfigDigester;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

/**
 * DataStoreFactory for ComplexDataStore.
 * 
 * NOTE: currently this one is not registered through the geotools datastore plugin mechanism.
 * Instead, we're directly using DataAccessFactory
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: AppSchemaDataAccessFactory.java 31882 2008-11-20 07:20:42Z bencd $
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/app-schema/app-schema/src/main/java/org/geotools/data/complex/AppSchemaDataAccessFactory.java $
 * @since 2.4
 */
public class AppSchemaDataAccessFactory implements DataAccessFactory {

    public static final DataStoreFactorySpi.Param DBTYPE = new DataStoreFactorySpi.Param("dbtype",
            String.class, "Fixed value 'app-schema'", true, "app-schema");

    public static final DataStoreFactorySpi.Param URL = new DataStoreFactorySpi.Param("url",
            URL.class, "URL to an application schema datastore XML configuration file", true);

    public AppSchemaDataAccessFactory() {
        // no-op
    }

    public DataAccess<FeatureType, Feature> createDataStore(Map params) throws IOException {
        Set<FeatureTypeMapping> mappings;
        AppSchemaDataAccess dataStore;

        URL configFileUrl = (URL) AppSchemaDataAccessFactory.URL.lookUp(params);
        XMLConfigDigester configReader = new XMLConfigDigester();
        AppSchemaDataAccessDTO config = configReader.parse(configFileUrl);
        mappings = AppSchemaDataAccessConfigurator.buildMappings(config);

        dataStore = new AppSchemaDataAccess(mappings);

        return dataStore;
    }

    public DataStore createNewDataStore(Map params) throws IOException {
        throw new UnsupportedOperationException();
    }

    public String getDisplayName() {
        return "Application Schema DataAccess";
    }

    public String getDescription() {
        return "Application Schema DataStore allows mapping of FeatureTypes to externally defined Output Schemas";
    }

    public DataStoreFactorySpi.Param[] getParametersInfo() {
        return new DataStoreFactorySpi.Param[] { AppSchemaDataAccessFactory.DBTYPE,
                AppSchemaDataAccessFactory.URL };
    }

    public boolean canProcess(Map params) {
        try {
            Object dbType = AppSchemaDataAccessFactory.DBTYPE.lookUp(params);
            Object configUrl = AppSchemaDataAccessFactory.URL.lookUp(params);
            return "app-schema".equals(dbType) && configUrl != null;
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return false;
    }

    public boolean isAvailable() {
        return true;
    }

    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }

}
