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
package org.geotools.gml;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.xml.sax.helpers.XMLFilterImpl;


/**
 * DOCUMENT ME!
 *
 * @author Darren Edmonds
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/library/main/src/main/java/org/geotools/gml/GMLReceiver.java $
 */
public class GMLReceiver extends XMLFilterImpl implements GMLHandlerFeature {
    /** */
    private FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection;

    /**
     * Creates a new instance of GMLReceiver
     *
     * @param FeatureCollection<SimpleFeatureType, SimpleFeature> sets the FeatureCollection
     */
    public GMLReceiver(FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection) {
        this.featureCollection = featureCollection;
    }

    /**
     * Receives an OGC feature and adds it into the collection
     *
     * @param feature the OGC feature
     */
    public void feature(SimpleFeature feature) {
        featureCollection.add(feature);
    }
}
