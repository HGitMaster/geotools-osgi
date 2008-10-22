/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2005, David Zwiers
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
package org.geotools.wfs.v_1_1_0.data;

import static org.geotools.wfs.v_1_1_0.data.DataTestSupport.GEOS_STATES;

import java.io.IOException;
import java.util.Collections;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class GeoServerOnlineTest extends AbstractWfsDataStoreOnlineTest {

    public static final String SERVER_URL = "http://localhost:8080/geoserver/wfs?service=WFS&request=GetCapabilities&version=1.1.0"; //$NON-NLS-1$

    public GeoServerOnlineTest() {
        super(SERVER_URL, GEOS_STATES, "the_geom", MultiPolygon.class, 49, ff.id(Collections
                .singleton(ff.featureId("states.1"))));
    }

    public void testFeatureSourceGetFeaturesFilter() throws IOException {
        if (Boolean.FALSE.equals(serviceAvailable)) {
            return;
        }

        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;
        featureSource = wfs.getFeatureSource(testType.FEATURETYPENAME);
        assertNotNull(featureSource);

        DefaultQuery query = new DefaultQuery(testType.FEATURETYPENAME);

        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coordinates = { new Coordinate(-107, 39), new Coordinate(-107, 38),
                new Coordinate(-104, 38), new Coordinate(-104, 39), new Coordinate(-107, 39) };
        LinearRing shell = gf.createLinearRing(coordinates);
        Polygon polygon = gf.createPolygon(shell, null);
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        Filter filter = ff.intersects(ff.property(defaultGeometryName), ff.literal(polygon));
        //System.out.println(filter);
        query.setFilter(filter);

        FeatureCollection<SimpleFeatureType, SimpleFeature> features;
        features = featureSource.getFeatures(query);
        assertNotNull(features);

        SimpleFeatureType schema = features.getSchema();
        assertNotNull(schema);

        FeatureIterator<SimpleFeature> iterator = features.features();
        assertNotNull(iterator);
        try {
            assertTrue(iterator.hasNext());
            SimpleFeature next = iterator.next();
            assertNotNull(next);
            assertNotNull(next.getDefaultGeometry());
            assertFalse(iterator.hasNext());
        } finally {
            iterator.close();
        }
    }

}
