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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.geotools.data.DataAccess;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureSource;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;

/**
 * A registry that stores all app schema data access instances per application. This allows mappings
 * from different data accesses to be accessed globally.
 * 
 * @author Rini Angreani, Curtin University of Technology
 */
public final class AppSchemaDataAccessRegistry {
    static ArrayList<AppSchemaDataAccess> registry;

    /**
     * Sole constructor
     */
    private AppSchemaDataAccessRegistry() {
        registry = new ArrayList<AppSchemaDataAccess>();
    }

    /**
     * Public method to create a new registry.
     * 
     * @return An instance of this class
     */
    public static AppSchemaDataAccessRegistry newInstance() {
        return new AppSchemaDataAccessRegistry();
    }

    /**
     * Registers a data access
     * 
     * @param dataAccess
     *            Data access to be registered
     */
    public static void register(AppSchemaDataAccess dataAccess) {
        if (registry == null) {
            throw new NullPointerException(
                    "App schema data access registry needs to be instantiated before usage!");
        }
        registry.add(dataAccess);
    }

    /**
     * Unregister a data access
     * 
     * @param dataAccess
     *            Data access to be unregistered
     */
    public static void unregister(DataAccess<FeatureType, Feature> dataAccess) {
        if (registry == null) {
            throw new NullPointerException(
                    "App schema data access registry needs to be instantiated before usage!");
        }
        registry.remove(dataAccess);
    }

    /**
     * Get a feature type mapping
     * 
     * @param featureTypeName
     * @return feature type mapping
     * @throws IOException
     */
    public static FeatureTypeMapping getMapping(Name featureTypeName) throws IOException {
        if (registry == null) {
            throw new NullPointerException(
                    "App schema data access registry needs to be instantiated before usage!");
        }
        for (AppSchemaDataAccess dataAccess : registry) {
            if (dataAccess.hasMapping(featureTypeName)) {
                return dataAccess.getMapping(featureTypeName);
            }
        }
        throwDataSourceException(featureTypeName);

        return null;
    }

    /**
     * Get a feature source for simple features with supplied feature type name.
     * 
     * @param featureTypeName
     * @return feature source
     * @throws IOException
     */
    public static FeatureSource<SimpleFeatureType, SimpleFeature> getSimpleFeatureSource(
            Name featureTypeName) throws IOException {
        return getMapping(featureTypeName).getSource();
    }

    /**
     * Get a feature source for built features with supplied feature type name.
     * 
     * @param featureTypeName
     * @return feature source
     * @throws IOException
     */
    public static MappingFeatureSource getMappingFeatureSource(Name featureTypeName)
            throws IOException {
        if (registry == null) {
            throw new NullPointerException(
                    "App schema data access registry needs to be instantiated before usage!");
        }
        for (AppSchemaDataAccess dataAccess : registry) {
            if (dataAccess.hasMapping(featureTypeName)) {
                return (MappingFeatureSource) dataAccess.getFeatureSource(featureTypeName);
            }
        }
        throwDataSourceException(featureTypeName);

        return null;
    }

    /**
     * Throws data source exception if mapping is not found.
     * 
     * @param featureTypeName
     *            Name of feature type
     * @throws IOException
     */
    private static void throwDataSourceException(Name featureTypeName) throws IOException {
        StringBuffer availables = new StringBuffer("[");
        for (AppSchemaDataAccess dataAccess : registry) {
            List<Name> typeNames = Arrays.asList(dataAccess.getTypeNames());
            for (Iterator<Name> it = typeNames.iterator(); it.hasNext();) {
                availables.append(it.next());
                availables.append(it.hasNext() ? ", " : "");
            }
        }
        availables.append("]");
        throw new DataSourceException(featureTypeName + " not found" + availables);
    }
}
