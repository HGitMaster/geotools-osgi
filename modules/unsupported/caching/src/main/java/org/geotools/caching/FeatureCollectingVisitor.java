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

import org.geotools.caching.spatialindex.Data;
import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.Visitor;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;


public class FeatureCollectingVisitor implements Visitor {
    FeatureCollection fc;
    int visited_nodes = 0;

    public FeatureCollectingVisitor(FeatureType type) {
        fc = new DefaultFeatureCollection("FeatureCollectingVisitor", type);
    }

    public void visitData(Data d) {
        fc.add((Feature) d.getData());
    }

    public void visitNode(Node n) {
        visited_nodes++;
    }

    public FeatureCollection getCollection() {
        return fc;
    }

    public int getVisitedNodes() {
        return visited_nodes;
    }

    public boolean isDataVisitor() {
        return true;
    }
}
