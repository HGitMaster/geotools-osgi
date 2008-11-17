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
package org.geotools.process.feature;

import java.util.Map;

import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Process which buffers an entire feature collection.
 * 
 * @author Justin Deoliveira, OpenGEO
 * @since 2.6
 */
public class BufferFeatureCollectionProcess extends AbstractFeatureCollectionProcess {

    @Override
    protected void processFeature(SimpleFeature feature, Map<String, Object> input) throws Exception {
       Double buffer = (Double) input.get( BufferFeatureCollectionFactory.BUFFER.key );
       
       Geometry g = (Geometry) feature.getDefaultGeometry();
       g = g.buffer( buffer );
       
       feature.setDefaultGeometry( g );
   }

}
