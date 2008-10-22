/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.caching.spatialindex;

import java.util.Properties;


public interface Storage {
    public static final String STORAGE_TYPE_PROPERTY = "Storage.Type";

    public void put(Node n);

    public void remove(NodeIdentifier id);

    public Node get(NodeIdentifier id);

    public void clear();

    public void setParent(SpatialIndex index);

    public Properties getPropertySet();

    public void flush();

    public NodeIdentifier findUniqueInstance(NodeIdentifier id);
}
