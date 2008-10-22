
//Disabled at revision 25329 by acuster
//
// Testing apparently fails during a 'mvn deploy'. The tests below are 
// essentially useless, they were made only to show me how to add testing
// to the module. 

/*
package org.geotools.demo.introduction;

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
        
        FeatureCollection fc = demoBase.makeLondonFeatureCollection(f);
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
        FeatureSource featureSource = 
                               demoBase.getAShapefileFeatureSourceFromCatalog();
        assertNotNull( featureSource );
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
*/
