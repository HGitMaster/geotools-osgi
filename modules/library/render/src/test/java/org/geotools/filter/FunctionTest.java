package org.geotools.filter;

import java.util.ArrayList;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import junit.framework.TestCase;

/**
 * Quick test case to see if the functions defined by this module work.
 * 
 * @author Jody
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/library/render/src/test/java/org/geotools/filter/FunctionTest.java $
 */
public class FunctionTest extends TestCase {
	
	/**
	 * Build a feature based on the provided number.
	 * @return Feature with a geom and a value
	 */
	private SimpleFeature feature(int number) throws Exception {
		SimpleFeatureType type = DataUtilities.createType("Feature","geom:Point,value:Integer");
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder( type );
		
		GeometryFactory factory = new GeometryFactory();
		Coordinate coord = new Coordinate( number, number );
		
		builder.add( factory.createPoint(coord ) );
		builder.add( number );
		return builder.buildFeature("Feature."+number);
	}
        
	public void testCategorization() throws Exception {
		FilterFactoryImpl ff = (FilterFactoryImpl) CommonFactoryFinder.getFilterFactory2(null);
		
		Literal fallback = ff.literal("NOT_FOUND");
		List<Expression> parameters = new ArrayList<Expression>();
		parameters.add( ff.property("value") );
		parameters.add( ff.literal( 12.0 ));
		Function function = ff.function( "categorize", parameters, fallback);
		
		
		Object value = function.evaluate( feature(2) );
		assertFalse( "Could not locate 'categorize' function", "NOT_FOUND".equals(value) );
		
		Integer number = function.evaluate( feature(2), Integer.class );
		assertEquals( 12, number.intValue() );	
		
		parameters = new ArrayList<Expression>();
		parameters.add( ff.property("value") );
		parameters.add( ff.literal( "low" ));		
		parameters.add( ff.literal( 0.0 ));		
		parameters.add( ff.literal( "mid" ));		
		parameters.add( ff.literal( 50.0 ));
		parameters.add( ff.literal( "high" ));				
		parameters.add( ff.literal( 100.0 ));
		parameters.add( ff.literal( "super" ));
		
		function = ff.function( "categorize", parameters, fallback);
		
		// normal
		assertEquals( "low", function.evaluate( feature(-1) )); 
		assertEquals( "mid", function.evaluate( feature(20) ));
		assertEquals( "high", function.evaluate( feature(60) ));
		assertEquals( "super", function.evaluate( feature(110) ));
		
		// boundary
		assertEquals( "mid", function.evaluate( feature(0) )); 
		assertEquals( "high", function.evaluate( feature(50) ));
		assertEquals( "super", function.evaluate( feature(100) ));
		
		parameters = new ArrayList<Expression>();
		parameters.add( ff.property("value") );
		parameters.add( ff.literal( "low" ));		
		parameters.add( ff.literal( 0.0 ));		
		parameters.add( ff.literal( "mid" ));		
		parameters.add( ff.literal( 50.0 ));
		parameters.add( ff.literal( "high" ));				
		parameters.add( ff.literal( 100.0 ));
		parameters.add( ff.literal( "super" ));
		parameters.add( ff.literal( "preceding"));
		
		function = ff.function( "categorize", parameters, fallback);
		
		// normal
		assertEquals( "low", function.evaluate( feature(-1) )); 
		assertEquals( "mid", function.evaluate( feature(20) ));
		assertEquals( "high", function.evaluate( feature(60) ));
		assertEquals( "super", function.evaluate( feature(110) ));
		
		// boundary
		assertEquals( "low", function.evaluate( feature(0) )); 
		assertEquals( "mid", function.evaluate( feature(50) ));
		assertEquals( "high", function.evaluate( feature(100) ));
	}
	/** Test if RasterData Expression works */
	public void XtestRasterData(){
	    FilterFactoryImpl ff = (FilterFactoryImpl) CommonFactoryFinder.getFilterFactory2(null);
            
	    float pixelBandValue = 255.6f;
            Expression rasterData = ff.createLiteralExpression("RasterData");
            
            float pixafter = rasterData.evaluate(pixelBandValue,float.class);
            assertEquals(pixelBandValue, pixafter);
	}
}
