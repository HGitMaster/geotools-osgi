/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter.function;

import org.geotools.data.DataUtilities;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

/**
 * 
 * @author Cory Horner, Refractions Research Inc.
 *
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/main/src/test/java/org/geotools/filter/function/QuantileFunctionTest.java $
 */
public class QuantileFunctionTest extends FunctionTestSupport {
   
    
    public QuantileFunctionTest(String testName) {
        super(testName);
    }
    
    protected void tearDown() throws java.lang.Exception {
    }
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(QuantileFunctionTest.class);
        
        return suite;
    }
    
    public void testInstance() {
        Function equInt = ff.function("Quantile", ff.literal(FeatureCollections.newCollection()));
        assertNotNull(equInt);
    }
    
    public void testGetName() {
        Function qInt = ff.function("Quantile", ff.literal(FeatureCollections.newCollection()));
        assertEquals("Quantile",qInt.getName());
    }
    
    public void testSetParameters() throws Exception{
        Literal classes = ff.literal(3);
        PropertyName expr = ff.property("foo");
        QuantileFunction func = (QuantileFunction) ff.function("Quantile", expr, classes);
        assertEquals(3, func.getClasses());
        classes = ff.literal(12);
        func = (QuantileFunction) ff.function("Quantile", expr, classes);
        assertEquals(12,func.getClasses());
        //deprecated still works?
        classes = ff.literal(5);
        func = (QuantileFunction) ff.function("Quantile", expr, classes);
        assertEquals(5, func.getClasses());
    }
    
    public void testEvaluateWithExpressions() throws Exception{
        Literal classes = ff.literal(2);
        PropertyName exp = ff.property("foo");
        Function func = ff.function("Quantile", exp, classes);
        
        Object value = func.evaluate(featureCollection);
        assertTrue(value instanceof RangedClassifier);
        RangedClassifier ranged = (RangedClassifier) value;
        assertEquals(2, ranged.getSize());
        assertEquals("4..20", ranged.getTitle(0));
        assertEquals("20..90", ranged.getTitle(1));
    }
    public void testEvaluateWithStrings() throws Exception {
        org.opengis.filter.expression.Expression function = ff.function("Quantile", ff.property("group"), ff.literal(2)  );
        Classifier classifier = (Classifier) function.evaluate( featureCollection );
        assertNotNull( classifier );

        Classifier classifier2 = (Classifier) function.evaluate( featureCollection, Classifier.class );
        assertNotNull( classifier2 );
        
        Integer number = (Integer) function.evaluate( featureCollection, Integer.class );
        assertNull( number );
    }
    
    public void xtestNullNaNHandling() throws Exception {
    	//create a feature collection
    	SimpleFeatureType ft = DataUtilities.createType("classification.nullnan",
        "id:0,foo:int,bar:double");
    	Integer iVal[] = new Integer[] {
    			new Integer(0),
    			new Integer(0),
    			new Integer(0),
    			new Integer(13),
    			new Integer(13),
    			new Integer(13),
    			null,
    			null,
    			null};
    	Double dVal[] = new Double[] {
    			new Double(0.0),
    			new Double(50.01),
    			null,
    			new Double(0.0),
    			new Double(50.01),
    			null,
    			new Double(0.0),
    			new Double(50.01),
    			null};

    	SimpleFeature[] testFeatures = new SimpleFeature[iVal.length];

    	for(int i=0; i< iVal.length; i++){
    		testFeatures[i] = SimpleFeatureBuilder.build(ft, new Object[] {
    				new Integer(i+1),
    				iVal[i],
    				dVal[i],
    		},"nantest.t"+(i+1));
    	}
    	MemoryDataStore store = new MemoryDataStore();
    	store.createSchema(ft);
    	store.addFeatures(testFeatures);
    	FeatureCollection<SimpleFeatureType, SimpleFeature> thisFC = store.getFeatureSource("nullnan").getFeatures();

    	//create the expression
        Divide divide = ff.divide(ff.property("foo"), ff.property("bar"));
        QuantileFunction qf = (QuantileFunction) ff.function("Quantile", divide, ff.literal(3));
        
        RangedClassifier range = (RangedClassifier) qf.evaluate(thisFC);
        assertEquals(2, range.getSize()); //2 or 3?
        assertEquals("0..0", range.getTitle(0));
        assertEquals("0..0.25995", range.getTitle(1));
    }
}
