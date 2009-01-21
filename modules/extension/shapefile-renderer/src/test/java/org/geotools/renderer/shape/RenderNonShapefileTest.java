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
package org.geotools.renderer.shape;

import junit.framework.TestCase;

import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Shapefile renderer delegates to Streaming Renderer if a layer is not a Shapefile layer.  This tests that. 
 * @author Jesse
 */
public class RenderNonShapefileTest extends TestCase {
	
	public void testRender() throws Exception {
		MemoryDataStore store=new MemoryDataStore();
		
		IndexedShapefileDataStore polys = TestUtilites.getPolygons();
		
		FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = polys.getFeatureSource().getFeatures();
		store.createSchema(polys.getSchema());
		
		FeatureSource<SimpleFeatureType, SimpleFeature> target = store.getFeatureSource(store.getTypeNames()[0]);
		((FeatureStore<SimpleFeatureType, SimpleFeature>)target).addFeatures(featureCollection);
		
		Style testStyle = TestUtilites.createTestStyle(target.getSchema().getTypeName(), null);
        MapLayer layer=new DefaultMapLayer(target,testStyle);
		MapContext context=new DefaultMapContext(new MapLayer[]{layer});
		
		ShapefileRenderer renderer=new ShapefileRenderer(context);
		
        Envelope env = context.getLayerBounds();
        env = new Envelope(env.getMinX(), env.getMaxX(), env.getMinY(),
                env.getMaxY());
        TestUtilites.showRender("testSimpleRender", renderer, 1000, env);

	}
}
