/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */

package org.example.geotools.base;

import java.io.IOException;

import junit.framework.TestCase;

import org.geotools.catalog.Catalog;
import org.geotools.catalog.Service;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.styling.Style;

public class DemoTest extends TestCase {


    DemoBase demoBase;
    
    protected void setUp() throws Exception {
        demoBase = new DemoBase();
    }
    
    public void testCreateLondonPointFeatureFromScratch(){
        
        Feature f = demoBase.createLondonPointFeatureFromScratch();
        assertNotNull(f);
        
        FeatureCollection<SimpleFeatureType, SimpleFeature> fc = demoBase.makeLondonFeatureCollection(f);
        assertNotNull(fc);
        assertFalse( fc.isEmpty() );
        
    }
    
    public void testLoadShapefileIntoCatalog() throws IOException {
        
        demoBase.loadShapefileIntoCatalog(demoBase.SHAPEFILENAME);
        Catalog catalog = demoBase.demoData.getLocalCatalog();
        assertEquals( 1, catalog.members( null ).size() );
        
        Service service = (Service) catalog.members( null ).get( 0 );
        assertTrue( service.canResolve( ShapefileDataStore.class ) );
        
    }
    
    public void testLoadShapefileFeatureSource() throws IOException {
        
        demoBase.loadShapefileIntoCatalog(demoBase.SHAPEFILENAME);
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = 
                               demoBase.getAShapefileFeatureSourceFromCatalog();
        assertNotNull( FeatureSource<SimpleFeatureType, SimpleFeature> );
        assertFalse( featureSource.getFeatures().isEmpty() );
        
    }
    
    public void testCreateStyleFromScratch() throws Exception {
        
        Style style = demoBase.createLondonStyleFromScratch();
        assertNotNull( style );
        assertEquals( 1, style.getFeatureTypeStyles().length );
        
    }
    
    public void testCreateStyleFromFile() throws Exception {
        
        Style style = 
            demoBase.createShapefileStyleFromSLDFile(demoBase.SHAPEFILESLDNAME);
        assertNotNull( style );
        assertEquals( 1, style.getFeatureTypeStyles().length ); 
    }
	
}
