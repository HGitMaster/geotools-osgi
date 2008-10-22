/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2003-2004, Julian J. Ray, All Rights Reserved
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
package org.geotools.data.tiger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


/**
 * <p>
 * Title: GeoTools2 Development
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 *
 * @author Julian J. Ray
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/tiger/src/main/java/org/geotools/data/tiger/TigerDataStore.java $
 * @version 1.0
 */
public class TigerDataStore extends AbstractDataStore {
    /** DOCUMENT ME! */
    protected File directory;

    /**
     * Creates a new TigerDataStore object.
     *
     * @param dirName DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public TigerDataStore(String dirName) {
        this( new File( dirName ));
    }
    public TigerDataStore(File dir ) {
        // Do not allow writes
        super(false);
        directory = dir;

        if( !dir.exists() || !dir.isDirectory() ){
            throw new IllegalArgumentException(directory + " is not a directory!");
        }
    
    }
    /**
     * Returns a list of logical tiger files. This routine searches for tiger Type1 (RT1) files in the data store
     * directory and returns the file names. File search is case insensetive so RT1 and rt1 work equally well.
     *
     * @return String[]
     */
    public String[] getTypeNames() {
        String[] list = directory.list(new TigerFilenameFilter());
        String[] schemaTypeNames = TigerSchemaManager.getTypeNames();

        ArrayList array = new ArrayList();

        for (int i = 0; i < list.length; i++) {
            list[i] = list[i].substring(0, list[i].lastIndexOf('.'));

            for (int j = 0; j < schemaTypeNames.length; j++) {
                array.add(list[i] + "_" + schemaTypeNames[j]);
            }
        }

        String[] typeNames = new String[array.size()];

        for (int i = 0; i < array.size(); i++) {
            typeNames[i] = (String) array.get(i);
        }

        return typeNames;
    }

    /**
     * getSchema
     *
     * @param typeName String
     *
     * @return FeatureType
     *
     * @throws IOException
     * @throws DataSourceException DOCUMENT ME!
     */
    public SimpleFeatureType getSchema(String typeName) throws IOException {
        try {
            TigerSchemaManager manager = new TigerSchemaManager();

            SimpleFeatureType featureType = DataUtilities.createType(directory.getName() + "." + typeName,
                    manager.getTypeSpec(typeName));

            return featureType;
        } catch (SchemaException e) {
            e.printStackTrace();
            throw new DataSourceException(typeName + " schema format error", e);
        }
    }

    /**
     * getFeatureReader
     *
     * @param typeName String
     *
     * @return FeatureReader
     *
     * @throws IOException
     */
    protected  FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(String typeName)
        throws IOException {
        return new TigerFeatureReader(directory, typeName);
    }

    /**
     * <p>
     * Title: GeoTools2 Development
     * </p>
     * 
     * <p>
     * Description:
     * </p>
     * 
     * <p>
     * Copyright: Copyright (c) 2003
     * </p>
     * 
     * <p>
     * Company:
     * </p>
     *
     * @author Julian J. Ray
     * @version 1.0
     */
    private class TigerFilenameFilter implements FilenameFilter {
        //~ Constructors -----------------------------------------------------------------------------------------------

        /**
         * Creates a new TigerFilenameFilter object.
         */
        TigerFilenameFilter() {
        }

        //~ Methods ----------------------------------------------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param dir DOCUMENT ME!
         * @param name DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean accept(File dir, String name) {
            if (name.endsWith("RT1") || name.endsWith("rt1")) {
                return true;
            } else {
                return false;
            }
        }
    }
}
