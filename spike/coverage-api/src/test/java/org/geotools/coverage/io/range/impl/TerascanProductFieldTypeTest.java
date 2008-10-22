/**
 * 
 */
package org.geotools.coverage.io.range.impl;


import javax.measure.quantity.Dimensionless;
import javax.measure.unit.SI;

import junit.framework.Assert;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.io.impl.range.BandIndexMeasure;
import org.geotools.coverage.io.impl.range.SimpleScalarAxis;
import org.geotools.coverage.io.impl.range.TerascanProductFieldType;
import org.geotools.coverage.io.range.Axis;
import org.geotools.coverage.io.range.FieldType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple test case for {@link TerascanProductFieldType}.s
 * 
 * @author Simone Giannecchini, GeoSolutions
 *
 */
public class TerascanProductFieldTypeTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testMCSST(){
		// is MCSST supported?
		Assert.assertEquals(true, TerascanProductFieldType.isSupportedProduct("mcsst")) ;
		
		
		//create a FieldType for the MCSST product
		final FieldType ft= new TerascanProductFieldType("mcsst",new GridSampleDimension("mcsst:sd"));
		
		//check the UoM
		Assert.assertEquals(SI.CELSIUS, ft.getUnitOfMeasure());
		
		//check the id 
		Assert.assertEquals("mcsst",ft.getName().toString());
		
		
		//check the axes
		Assert.assertEquals(1, ft.getAxes().size());
		Assert.assertTrue(ft.getAxes().get(0) instanceof SimpleScalarAxis);
		Axis<?, ?> axis= ft.getAxes().get(0);
		Assert.assertNotNull(axis);
		Assert.assertEquals(1,axis.getNumKeys());
		Assert.assertEquals(Dimensionless.UNIT,axis.getUnitOfMeasure());
		Assert.assertTrue(axis.getKey(0) instanceof BandIndexMeasure);
	}
}
