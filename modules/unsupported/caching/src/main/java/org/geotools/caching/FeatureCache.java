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
package org.geotools.caching;

import java.io.IOException;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;


public interface FeatureCache extends FeatureSource {
    public void clear();

    public void put(FeatureCollection fc, Envelope e) throws CacheOversizedException;

    public void put(FeatureCollection fc) throws CacheOversizedException;

    public FeatureCollection get(Envelope e) throws IOException;

    public FeatureCollection peek(Envelope e);

    public void remove(Envelope e);
}
