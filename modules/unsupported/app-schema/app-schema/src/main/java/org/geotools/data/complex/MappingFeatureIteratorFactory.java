/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2009, Open Source Geospatial Foundation (OSGeo)
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

import org.geotools.data.Query;

/**
 * @author Russell Petty, GSV
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/app-schema/app-schema/src/main/java/org/geotools/data/complex/MappingFeatureIteratorFactory.java $
 */
public class MappingFeatureIteratorFactory {

    public static IMappingFeatureIterator getInstance(AppSchemaDataAccess store, FeatureTypeMapping mapping, Query query)
            throws IOException {
                
        if (mapping instanceof XmlFeatureTypeMapping) {
            return new XmlMappingFeatureIterator(store, mapping, query);        
        }
        
        return new DataAccessMappingFeatureIterator(store, mapping, query);        
    }
}
