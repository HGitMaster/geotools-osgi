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
package org.geotools.gml2;

import java.util.HashMap;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;


public class FeatureTypeCache {
    HashMap<Name, SimpleFeatureType> map = new HashMap<Name, SimpleFeatureType>();

    public SimpleFeatureType get(Name name) {
        synchronized (this) {
            return (SimpleFeatureType) map.get(name);
        }
    }

    public void put(SimpleFeatureType type) {
        synchronized (this) {
            if (map.get(type.getName()) != null) {
                SimpleFeatureType other = map.get(type.getName());

                if (!other.equals(type)) {
                    String msg = "Type with same name already exists in cache.";
                    throw new IllegalArgumentException(msg);
                }

                return;
            }

            map.put(type.getName(), type);
        }
    }
}
