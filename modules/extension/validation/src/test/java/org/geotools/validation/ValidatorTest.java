/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.Filter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;


/**
 * ValidatorTest<br>
 *
 * @author bowens<br> Created Jun 28, 2004<br>
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/extension/validation/src/test/java/org/geotools/validation/ValidatorTest.java $
 * @version <br><b>Puropse:</b><br><p><b>Description:</b><br><p><b>Usage:</b><br><p>
 */
public class ValidatorTest extends TestCase {
    TestFixture fixture;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        fixture = new TestFixture();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        fixture = null;
    }

    public void testRepositoryGeneration() throws Exception {
        //DefaultRepository dataRepository = new DefaultRepository();               
        assertNotNull(fixture.repository.datastore("LAKES"));
        assertNotNull(fixture.repository.datastore("STREAMS"));
        assertNotNull(fixture.repository.datastore("SWAMPS"));
        assertNotNull(fixture.repository.datastore("RIVERS"));

        Map types = fixture.repository.types();
        assertTrue( types.containsKey( "LAKES:lakes" ) );
        assertTrue( types.containsKey( "STREAMS:streams" ) );
        assertTrue( types.containsKey( "SWAMPS:swamps" ) );
        assertTrue( types.containsKey( "RIVERS:rivers" ) );        
    }

    public void testFeatureValidation() throws Exception {
    	FeatureSource<SimpleFeatureType, SimpleFeature> lakes = fixture.repository.source( "LAKES", "lakes" );
    	FeatureCollection<SimpleFeatureType, SimpleFeature> features = lakes.getFeatures();
		DefaultFeatureResults results = new DefaultFeatureResults();    	
    	fixture.processor.runFeatureTests( "LAKES", features, results );
    		
    	assertEquals( "lakes test", 0, results.error.size() );
    }
    public SimpleFeature createInvalidLake() throws Exception {
    	FeatureSource<SimpleFeatureType, SimpleFeature> lakes = fixture.repository.source( "LAKES", "lakes" );
    	
    	FeatureIterator<SimpleFeature> features = lakes.getFeatures( new DefaultQuery("lakes", Filter.INCLUDE, 1, null, null) ).features();
    	SimpleFeature feature = features.next();
    	features.close();
    	
    	SimpleFeatureType LAKE = lakes.getSchema();
    	Object array[] = new Object[ LAKE.getAttributeCount() ];
    	for( int i=0; i<LAKE.getAttributeCount(); i++){
    		AttributeDescriptor attr = LAKE.getDescriptor( i );
    		// System.out.println( i+" "+attr.getType()+":"+attr.getName()+"="+feature.getAttribute( i )  );
    		if( LAKE.getGeometryDescriptor() == attr ){
    			GeometryFactory factory = new GeometryFactory();
    			Coordinate coords[] = new Coordinate[]{
    					new Coordinate( 1, 1 ),new Coordinate( 2, 2 ),
						new Coordinate( 2, 1 ),new Coordinate( 1, 2 ),
						new Coordinate( 1, 1 ),
    			};
    			LinearRing ring = factory.createLinearRing( coords );
    			Polygon poly = factory.createPolygon( ring, null );
    			array[i] = factory.createMultiPolygon( new Polygon[]{ poly, } ); 
    		}
    		else {
    			array[i] = feature.getAttribute( i );
    		}
    	}
    	return SimpleFeatureBuilder.build(LAKE, array, "splash");
    }
    public void testFeatureValidation2() throws Exception {
        FeatureSource<SimpleFeatureType, SimpleFeature> lakes = fixture.repository.source( "LAKES", "lakes" );
    	SimpleFeature newFeature = createInvalidLake();
    	    	
    	FeatureCollection<SimpleFeatureType, SimpleFeature> add = DataUtilities.collection( new SimpleFeature[]{ newFeature, } );
    	
    	DefaultFeatureResults results = new DefaultFeatureResults();    	
    	fixture.processor.runFeatureTests( "LAKES", add, results );
    	
    	assertEquals( "lakes test", 2, results.error.size() );
    }

    public void testIntegrityValidation() throws Exception {
    	DefaultFeatureResults results = new DefaultFeatureResults();
    	Set set = fixture.repository.types().keySet();
    	Map map = new HashMap();
    	for( Iterator i=set.iterator(); i.hasNext(); ){
    		String typeRef = (String) i.next();
    		String split[] = typeRef.split(":");
    		map.put( typeRef, fixture.repository.source( split[0], split[1] ) );
    	}    	
    	fixture.processor.runIntegrityTests( set, map, null, results );    	    	
    	assertEquals( "integrity test", 0, results.error.size() );
    }
    public void testValidator() throws Exception {
    	Validator validator = new Validator( fixture.repository, fixture.processor );

    	FeatureSource<SimpleFeatureType, SimpleFeature> lakes = fixture.repository.source( "LAKES", "lakes" );
    	FeatureCollection<SimpleFeatureType, SimpleFeature> features = lakes.getFeatures();
    	DefaultFeatureResults results = new DefaultFeatureResults();
    	validator.featureValidation( "LAKES", features, results );
    	
    	assertEquals( 0, results.error.size() );
    }
    public void testValidator2() throws Exception {
    	Validator validator = new Validator( fixture.repository, fixture.processor );
    	
    	FeatureSource<SimpleFeatureType, SimpleFeature> lakes = fixture.repository.source( "LAKES", "lakes" );
    	SimpleFeature newFeature = createInvalidLake();
        
    	FeatureCollection<SimpleFeatureType, SimpleFeature> add = DataUtilities.collection( new SimpleFeature[]{ newFeature, } );
    	DefaultFeatureResults results = new DefaultFeatureResults();
    	fixture.processor.runFeatureTests( "LAKES", add, results );
    	
    	System.out.println( results.error );    	
    	assertEquals( "lakes test", 2, results.error.size() );
    	
    	//results = new DefaultFeatureResults();
    	validator.featureValidation( "LAKES", add, results );
    	assertEquals( "lakes test2", 5, results.error.size() );
    }
    
    public void testIntegrityValidator() throws Exception {
    	Validator validator = new Validator( fixture.repository, fixture.processor );
    	
    	DefaultFeatureResults results = new DefaultFeatureResults();
    	Set set = fixture.repository.types().keySet();
    	Map map = new HashMap();
    	for( Iterator i=set.iterator(); i.hasNext(); ){
    		String typeRef = (String) i.next();
    		String split[] = typeRef.split(":");
    		map.put( typeRef, fixture.repository.source( split[0], split[1] ) );
    	}    	
    	validator.integrityValidation( map, null, results );    	    	
    	assertEquals( "integrity test", 0, results.error.size() );
    }
    public void testIntegrityValidator2() throws Exception {
    	Validator validator = new Validator( fixture.repository, fixture.processor );
    	
    	DefaultFeatureResults results = new DefaultFeatureResults();
    	Set set = new HashSet();
    	Map map = new HashMap();
    	set.add( "RIVERS:rivers" );
    	map.put( "RIVERS:rivers", fixture.repository.source( "RIVERS", "rivers" ));
    	
    	validator.integrityValidation( map, null, results );    	    	
    	assertEquals( "integrity test", 0, results.error.size() );
    }       
}
