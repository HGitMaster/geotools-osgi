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
package org.geotools.referencing.piecewise;

import it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageReader;
import it.geosolutions.imageio.plugins.arcgrid.spi.AsciiGridsImageReaderSpi;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.stream.FileImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.ExtremaDescriptor;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.TestData;
import org.geotools.geometry.DirectPosition1D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.image.ImageWorker;
import org.geotools.referencing.operation.transform.LinearTransform1D;
import org.geotools.renderer.lite.gridcoverage2d.RasterSymbolizerTest;
import org.geotools.util.NumberRange;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;

/**
 * @author Simone Giannecchini, GeoSolutions.
 * 
 */
public class TestPiecewise extends TestCase {

	/**
	 * @param name
	 */
	public TestPiecewise(String name) {
		super(name);
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new TestPiecewise("testLookupByte"));
		suite.addTest(new TestPiecewise("testSWANLOGARITHMIC"));
		suite.addTest(new TestPiecewise("testPiecewiseLogarithm"));
		suite.addTest(new TestPiecewise("testDefaultTransform"));
		suite.addTest(new TestPiecewise("testPassthroughTransform"));
		suite.addTest(new TestPiecewise("testConstantTransform"));
		suite.addTest(new TestPiecewise("testLinearTransform"));
		suite.addTest(new TestPiecewise("testMathTransform1DAdapter"));
		



		return suite;
	}

	/**
	 * Testing {@link DefaultConstantPiecewiseTransformElement}.
	 * 
	 * @throws IOException
	 * @throws TransformException
	 */
	public void testLinearTransform() throws IOException, TransformException {

		// /////////////////////////////////////////////////////////////////////
		//
		// byte
		//
		// /////////////////////////////////////////////////////////////////////
		DefaultPiecewiseTransform1DElement e0 = DefaultPiecewiseTransform1DElement
				.create("zero", NumberRange.create(0,100),NumberRange.create(0,200));
		assertTrue(e0 instanceof DefaultLinearPiecewiseTransform1DElement);
		// checks
		assertEquals(((DefaultLinearPiecewiseTransform1DElement)e0).getOutputMinimum(), e0.transform(0),0.0);
		assertEquals(((DefaultLinearPiecewiseTransform1DElement)e0).getOutputMaximum(), e0.transform(e0.getInputMaximum()),0.0);
		assertEquals(0.0, ((DefaultLinearPiecewiseTransform1DElement)e0).getOffset(),0.0);
		assertEquals(2.0, ((DefaultLinearPiecewiseTransform1DElement)e0).getScale(),0.0);
		assertFalse(e0.isIdentity());
		assertEquals(1, e0.getSourceDimensions());
		assertEquals(1, e0.getTargetDimensions());

		try{
			assertEquals(e0.transform(Double.POSITIVE_INFINITY),0.0,0.0);
			assertTrue(false);
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		
		DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement> transform = 
		    new DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement>(new DefaultPiecewiseTransform1DElement[] {e0});

		// checks
		assertEquals(0.0, transform.transform(0),0);
		try{
			assertFalse(transform.isIdentity());
			assertTrue(false);
		}catch (Exception e) {
			// TODO: handle exception
		}
		assertEquals(1, transform.getSourceDimensions());
		assertEquals(1, transform.getTargetDimensions());
		


	}
	/**
	 * Testing {@link MathTransform1DAdapter}.
	 * 
	 * @throws IOException
	 * @throws TransformException
	 */
	public void testMathTransform1DAdapter() throws IOException, TransformException {
		//default adapter
		final MathTransform1DAdapter defaultAdapter = new MathTransform1DAdapter();
		assertEquals(defaultAdapter.getSourceDimensions(), 1);
		assertEquals(defaultAdapter.getTargetDimensions(), 1);


		try{
			defaultAdapter.inverse();
			assertFalse(true);
		}catch (UnsupportedOperationException e) {
			
		}
		
		try{
			defaultAdapter.transform(0.0);
			assertFalse(true);
		}catch (UnsupportedOperationException e) {
			
		}
		
		try{
			defaultAdapter.transform(new double[]{0},0,(double[])null,0,1);
			assertFalse(true);
		}catch (UnsupportedOperationException e) {
			
		}
		
		
		try{
			defaultAdapter.transform(new float[]{0},0,(float[])null,0,1);
			assertFalse(true);
		}catch (UnsupportedOperationException e) {
			
		}
		
		try{
			defaultAdapter.derivative(0.0);
			assertFalse(true);
		}catch (UnsupportedOperationException e) {
			
		}
		
		try{
			defaultAdapter.derivative(new DirectPosition1D(0));
			assertFalse(true);
		}catch (UnsupportedOperationException e) {
			
		}
		
		
		try{
			defaultAdapter.toWKT();
			assertFalse(true);
		}catch (UnsupportedOperationException e) {
			
		}
		
		try{
			defaultAdapter.isIdentity();
			assertFalse(true);
		}catch (UnsupportedOperationException e) {
			
		}
	}
	/**
	 * Testing {@link DefaultConstantPiecewiseTransformElement}.
	 * 
	 * @throws IOException
	 * @throws TransformException
	 */
	public void testConstantTransform() throws IOException, TransformException {

		// /////////////////////////////////////////////////////////////////////
		//
		// byte
		//
		// /////////////////////////////////////////////////////////////////////
		DefaultPiecewiseTransform1DElement e0 = 
		    DefaultPiecewiseTransform1DElement.create(
		            "zero", 
		            NumberRange.create(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
		            ((byte) 0));
		assertTrue(e0 instanceof DefaultConstantPiecewiseTransformElement);
		// checks
		assertEquals(0.0, e0.transform(0),0.0);
		assertEquals(e0.transform(Double.POSITIVE_INFINITY),0.0,0.0);
		try{
			e0.inverse();
			assertTrue(false);
		}catch (Exception e) {
			
		}
		
		
		DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement> transform = 
		    new DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement>(new DefaultPiecewiseTransform1DElement[] {e0});
		// checks
		assertEquals(0.0, transform.transform(0),0);
		assertEquals( transform.transform(Double.POSITIVE_INFINITY),0.0,0.0);
		try{
			transform.inverse();
			assertTrue(false);
		}catch (Exception e) {
			// TODO: handle exception
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// int
		//
		// /////////////////////////////////////////////////////////////////////
		e0 = DefaultPiecewiseTransform1DElement.create(
		        "zero", 
		        NumberRange.create(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
		        0);
		assertTrue(e0 instanceof DefaultConstantPiecewiseTransformElement);
		// checks
		assertEquals(0.0, e0.transform(0),0.0);
		assertEquals(e0.transform(Double.POSITIVE_INFINITY),0.0,0.0);
		try{
			e0.inverse();
			assertTrue(false);
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		
		DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement> transform1 = new DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement>(new DefaultPiecewiseTransform1DElement[] {e0});

		// checks
		assertEquals(0.0, transform1.transform(0),0);
		assertEquals( transform1.transform(Double.POSITIVE_INFINITY),0.0,0.0);
		try{
			transform1.inverse();
			assertTrue(false);
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		//hashcode and equals
		assertFalse(transform.equals(transform1));
		assertFalse(transform1.equals(transform));
		assertFalse(transform.equals(transform));
		assertFalse(transform1.equals(transform1));
		assertEquals(transform1.hashCode(), transform.hashCode());
		
		// /////////////////////////////////////////////////////////////////////
		//
		// double
		//
		// /////////////////////////////////////////////////////////////////////
		e0 = DefaultPiecewiseTransform1DElement.create(
		        "zero", 
		        NumberRange.create(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), 
		        0.0);
		assertTrue(e0 instanceof DefaultConstantPiecewiseTransformElement);
		// checks
		assertEquals(0.0, e0.transform(0),0.0);
		assertEquals(e0.transform(Double.POSITIVE_INFINITY),0.0,0.0);
		try{
			e0.inverse();
			assertTrue(false);
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		
		transform = new DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement>(new DefaultPiecewiseTransform1DElement[] {e0});

		// checks
		assertEquals(0.0, transform.transform(0),0);
		assertEquals( transform.transform(Double.POSITIVE_INFINITY),0.0,0.0);
		try{
			transform.inverse();
			assertTrue(false);
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		
		// /////////////////////////////////////////////////////////////////////
		//
		// invertible
		//
		// /////////////////////////////////////////////////////////////////////
		e0 = DefaultPiecewiseTransform1DElement
				.create("zero", NumberRange.create(3, 3), 0.0);
		assertTrue(e0 instanceof DefaultConstantPiecewiseTransformElement);
		// checks
		assertEquals(0.0, e0.transform(3),0.0);
		assertEquals(3, e0.inverse().transform(new DirectPosition1D(0),null).getOrdinate(0),0);
		
		
		transform = new DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement>(new DefaultPiecewiseTransform1DElement[] {e0});

		// checks
		assertEquals(0.0, e0.transform(3),0);
		assertEquals( transform.transform(3),0.0,0.0);



		
	}
	/**
	 * Testing testPiecewiseLogarithm.
	 * 
	 * @throws IOException
	 * @throws TransformException
	 */
	public void testPiecewiseLogarithm() throws IOException, TransformException {

		// /////////////////////////////////////////////////////////////////////
		//
		// prepare the transform without no data management, which means gaps
		// won't be filled and exception will be thrown when trying to
		//
		// /////////////////////////////////////////////////////////////////////
		final DefaultPiecewiseTransform1DElement zero = DefaultPiecewiseTransform1DElement
				.create("zero", NumberRange.create(0, 0), 0);
		final DefaultPiecewiseTransform1DElement mainElement = new DefaultPiecewiseTransform1DElement(
				"natural logarithm", NumberRange.create(0, false, 255, true),
				new MathTransform1D() {

					public double derivative(double arg0)
							throws TransformException {
						
						return 1/arg0;
					}

					public double transform(double arg0)
							throws TransformException {
						return Math.log(arg0);
					}

					public Matrix derivative(DirectPosition arg0)
							throws MismatchedDimensionException,
							TransformException {
						
						return null;
					}

					public int getSourceDimensions() {
						
						return 01;
					}

					public int getTargetDimensions() {
						
						return 1;
					}

					public MathTransform1D inverse()
							throws NoninvertibleTransformException {
						
						return null;
					}

					public boolean isIdentity() {
						return false;
					}

					public String toWKT() throws UnsupportedOperationException {
						
						return null;
					}

					public DirectPosition transform(DirectPosition arg0,
							DirectPosition arg1)
							throws MismatchedDimensionException,
							TransformException {
						
						return null;
					}

					public void transform(double[] arg0, int arg1,
							double[] arg2, int arg3, int arg4)
							throws TransformException {
						
						
					}

					public void transform(float[] arg0, int arg1, float[] arg2,
							int arg3, int arg4) throws TransformException {
						
						
					}

					public void transform(float[] arg0, int arg1, double[] arg2,
							int arg3, int arg4) throws TransformException {
						
						
					}

					public void transform(double[] arg0, int arg1, float[] arg2,
							int arg3, int arg4) throws TransformException {
						
						
					}
				});
		DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement> transform = 
		    new DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement>(new DefaultPiecewiseTransform1DElement[] {zero,mainElement});

		// checks
		assertEquals(0.0, transform.transform(0),0);
		assertEquals(0.0, transform.transform(1),0);
		assertEquals(Math.log(255.0), transform.transform(255),0);
		assertEquals(Math.log(124.0), transform.transform(124),0);
		try {
			assertEquals(Math.log(255.0), transform.transform(256),0);
			assertTrue(false);
		} catch (TransformException e) {
			
		}

		
		// /////////////////////////////////////////////////////////////////////
		//
		// prepare the transform without no data management, which means gaps
		// won't be filled and exception will be thrown when trying to
		//
		// /////////////////////////////////////////////////////////////////////
		final DefaultPiecewiseTransform1DElement nodata = DefaultPiecewiseTransform1DElement
				.create("no-data", NumberRange.create(-1, -1), Double.NaN);
		transform = 
		    new DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement>(new DefaultPiecewiseTransform1DElement[] {zero,mainElement,nodata});

		// checks
		assertEquals(0.0, transform.transform(0),0);
		assertEquals(0.0, transform.transform(1),0);
		assertEquals(Math.log(255.0), transform.transform(255),0);
		assertEquals(Math.log(124.0), transform.transform(124),0);
		try {
			assertTrue(Double.isNaN(transform.transform(256)));
			assertTrue(false);
		} catch (TransformException e) {
			assertTrue(true);
		}
		
		// /////////////////////////////////////////////////////////////////////
		//
		// prepare the transform with categories that overlap, we should try an exception
		//
		// /////////////////////////////////////////////////////////////////////
		final DefaultPiecewiseTransform1DElement overlap = DefaultPiecewiseTransform1DElement
				.create("overlap", NumberRange.create(-100, 12), Double.NaN);
		try {
		transform = 
		    new DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement>(new DefaultPiecewiseTransform1DElement[] {zero,mainElement,overlap,nodata});
				assertTrue(false);
		} catch (Throwable e) {
			assertTrue(true);
		}


		
	}

		
		/**
		 * Testing DefaultPiecewiseTransform1DElement.
		 * 
		 * @throws IOException
		 * @throws TransformException
		 */
		public void testDefaultTransform() throws IOException,
				TransformException {
			////
			//
			// Create first element and test it
			//
			/////
			DefaultPiecewiseTransform1DElement t0 = new DefaultPiecewiseTransform1DElement(
					"t0", NumberRange.create(0.0, true, 1.0, true), PiecewiseUtilities
							.createLinearTransform1D(NumberRange.create(0.0, true, 1.0,
									true), NumberRange.create(200, 201)));
			assertEquals(t0.transform(0.5), 200.5, 0.0);
			assertTrue(t0.contains(0.5));
			assertTrue(t0.contains(NumberRange.create(0.1,0.9)));
			assertFalse(t0.contains(1.5));
			assertFalse(t0.contains(NumberRange.create(0.1,1.9)));
			assertTrue(t0.equals(t0));
			assertEquals(t0.transform(
					new GeneralDirectPosition(new double[] { 0.5 }), null)
					.getOrdinate(0), 200.5, 0.0);
			assertEquals(t0.inverse().transform(
					new GeneralDirectPosition(new double[] { 200.5 }), null)
					.getOrdinate(0), 0.5, 0.0);
			assertEquals(t0.derivative(1.0), 1.0, 0.0);
			
			t0 = DefaultPiecewiseTransform1DElement.create("t0", NumberRange.create(0.0, true, 1.0, true),  NumberRange.create(200, 201));
			assertFalse(t0.equals(DefaultPiecewiseTransform1DElement.create("t0", NumberRange.create(0.0, true, 1.0, true),  NumberRange.create(200, 202))));
			assertEquals(t0.transform(0.5), 200.5, 0.0);
			assertEquals(t0.transform(
					new GeneralDirectPosition(new double[] { 0.5 }), null)
					.getOrdinate(0), 200.5, 0.0);
			assertEquals(t0.inverse().transform(
					new GeneralDirectPosition(new double[] { 200.5 }), null)
					.getOrdinate(0), 0.5, 0.0);
			assertEquals(t0.derivative(1.0), 1.0, 0.0);
			
			////
			//
			// Create second element and test it
			//
			/////
			DefaultPiecewiseTransform1DElement t1 = DefaultPiecewiseTransform1DElement.create(
					"t1", NumberRange.create(1.0, false, 2.0, true), 201);
			assertEquals(t1.transform(1.5), 201, 0.0);
			assertEquals(t1.transform(1.6), 201, 0.0);
			assertFalse(t0.equals(t1));
			assertEquals(t1.transform(
					new GeneralDirectPosition(new double[] { 1.8 }), null)
					.getOrdinate(0), 201, 0.0);
			try{
				assertEquals(t1.inverse().transform(
						new GeneralDirectPosition(new double[] { 201 }), null)
						.getOrdinate(0), 0.5, 0.0);
				assertTrue(false);
			}catch (UnsupportedOperationException e) {
			}
			assertEquals(t1.derivative(2.0), 0.0, 0.0);
			
			t1 = new DefaultConstantPiecewiseTransformElement(
					"t1", NumberRange.create(1.0, false, 2.0, true), 201);
			assertEquals(t1.transform(1.5), 201, 0.0);
			assertEquals(t1.transform(1.6), 201, 0.0);
			assertEquals(t1.transform(
					new GeneralDirectPosition(new double[] { 1.8 }), null)
					.getOrdinate(0), 201, 0.0);
			try{
				assertEquals(t1.inverse().transform(
						new GeneralDirectPosition(new double[] { 201 }), null)
						.getOrdinate(0), 0.5, 0.0);
				assertTrue(false);
			}catch (UnsupportedOperationException e) {
				// TODO: handle exception
			}
			assertEquals(t1.derivative(2.0), 0.0, 0.0);
	

	
			DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement> transform = 
			    new DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement>(new DefaultPiecewiseTransform1DElement[] { t1 },12);
			assertEquals(transform.getName().toString(),t1.getName().toString());
			assertEquals(transform.getApproximateDomainRange().getMinimum(), 1.0, 0.0);
			assertEquals(transform.getApproximateDomainRange().getMaximum(), 2.0, 0.0);
			assertEquals(transform.transform(1.5), 201, 0.0);
			assertEquals(transform.transform(
					new GeneralDirectPosition(new double[] { 1.5 }), null)
					.getOrdinate(0), 201, 0.0);
			assertEquals(transform.transform(2.5), 0.0, 12.0);
			
			
			////
			//
			// test bad cases
			//
			/////
			try{
			transform = new DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement>(
					new DefaultPiecewiseTransform1DElement[]{
				DefaultLinearPiecewiseTransform1DElement.create("",
						NumberRange.create(0, 100),
						NumberRange.create(Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY))});
				assertTrue(false);
			}
			catch (IllegalArgumentException e) {
				
			}
			
		}

		/**
		 * Testing DefaultPassthroughPiecewiseTransform1DElement .
		 * 
		 * @throws IOException
		 * @throws TransformException
		 */
		public void testPassthroughTransform() throws IOException,
				TransformException {
			////
			//
			//testing the passthrough through direct instantion
			//
			////
			final DefaultPassthroughPiecewiseTransform1DElement p0 = new DefaultPassthroughPiecewiseTransform1DElement(
					"p0", NumberRange.create(0.0, true, 1.0, true));
			assertEquals(p0.getTargetDimensions(), 1);
			assertEquals(p0.getSourceDimensions(), 1);
			assertTrue(p0.isIdentity());
			assertEquals(p0.inverse(), LinearTransform1D.IDENTITY);
			assertEquals(p0.transform(0.5), 0.5, 0.0);
			assertEquals(p0.transform(
					new GeneralDirectPosition(new double[] { 0.5 }), null)
					.getOrdinate(0), 0.5, 0.0);
			assertEquals(p0.inverse().transform(
					new GeneralDirectPosition(new double[] { 0.5 }), null)
					.getOrdinate(0), 0.5, 0.0);
			assertEquals(p0.derivative(1.0), 1.0, 0.0);
			final DirectPosition1D inDP = new DirectPosition1D(0.6);
			final DirectPosition outDP= p0.transform(inDP, null);
			assertTrue(outDP.getOrdinate(0)==0.6);
			try{
		
				p0.transform(new double[]{0.5},0,(double[])null,0,0);
				assertFalse(true);
			}
			catch (UnsupportedOperationException e) {
				// TODO: handle exception
			}
			
			try{
		
				p0.transform(new float[]{0.5f},0,(float[])null,0,0);
				assertFalse(true);
			}
			catch (UnsupportedOperationException e) {
				// TODO: handle exception
			}
			
			
			try{
				p0.toWKT();
				assertFalse(true);
			}
			catch (UnsupportedOperationException e) {
				// TODO: handle exception
			}
			Matrix m= p0.derivative(inDP);
			assertTrue(m.getNumCol()==1);
			assertTrue(m.getNumRow()==1);
			assertTrue(m.getElement(0, 0)==1);
			
			////
			//
			//testing the transform 
			//
			////
			final DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement> piecewise = 
			    new DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement>(new DefaultPiecewiseTransform1DElement[] { p0 },11);
		
			assertEquals(piecewise.getApproximateDomainRange().getMinimum(), 0.0, 0.0);
			assertEquals(piecewise.getApproximateDomainRange().getMaximum(), 1.0, 0.0);
			assertEquals(piecewise.transform(0.5), 0.5, 0.0);
			assertEquals(piecewise.transform(
					new GeneralDirectPosition(new double[] { 0.5 }), null)
					.getOrdinate(0), 0.5, 0.0);
			assertEquals(piecewise.transform(1.5), 0.0, 11.0);
			
			try{
		
				piecewise.transform(new double[]{0.5},0,(double[])null,0,0);
				assertFalse(true);
			}
			catch (UnsupportedOperationException e) {
				// TODO: handle exception
			}
			
			try{
		
				piecewise.transform(new float[]{0.5f},0,(float[])null,0,0);
				assertFalse(true);
			}
			catch (UnsupportedOperationException e) {
				// TODO: handle exception
			}
			
			
			try{
				piecewise.toWKT();
				assertFalse(true);
			}
			catch (UnsupportedOperationException e) {
				// TODO: handle exception
			}
			
			try{
				m= piecewise.derivative(inDP);
				assertFalse(true);
			}
			catch (UnsupportedOperationException e) {
				// TODO: handle exception
			}
			
			////
			//
			//testing the passthrough through indirect instantion
			//
			////
			final DefaultPassthroughPiecewiseTransform1DElement p1 = new DefaultPassthroughPiecewiseTransform1DElement(
					"p1");
			assertEquals(p1.getTargetDimensions(), 1);
			assertEquals(p1.getSourceDimensions(), 1);
			assertTrue(p1.isIdentity());
			assertEquals(p1.inverse(), LinearTransform1D.IDENTITY);
			assertEquals(p1.transform(0.5), 0.5, 0.0);
			assertEquals(p1.transform(111.5), 111.5, 0.0);
			assertEquals(p1.transform(
					new GeneralDirectPosition(new double[] { 123.5 }), null)
					.getOrdinate(0), 123.5, 0.0);
			assertEquals(p1.inverse().transform(
					new GeneralDirectPosition(new double[] { 657.5 }), null)
					.getOrdinate(0), 657.5, 0.0);
			assertEquals(p1.derivative(1.0), 1.0, 0.0);
			final DirectPosition1D inDP1 = new DirectPosition1D(0.6);
			final DirectPosition outDP1= p1.transform(inDP1, null);
			assertTrue(outDP1.getOrdinate(0)==0.6);
			try{
		
				p1.transform(new double[]{1233444.5},0,(double[])null,0,0);
				assertFalse(true);
			}
			catch (UnsupportedOperationException e) {
				// TODO: handle exception
			}
			
			try{
		
				p1.transform(new float[]{6595.5f},0,(float[])null,0,0);
				assertFalse(true);
			}
			catch (UnsupportedOperationException e) {
				// TODO: handle exception
			}
			
			
			try{
				p1.toWKT();
				assertFalse(true);
			}
			catch (UnsupportedOperationException e) {
				// TODO: handle exception
			}
			Matrix m1= p1.derivative(inDP1);
			assertTrue(m1.getNumCol()==1);
			assertTrue(m1.getNumRow()==1);
			assertTrue(m1.getElement(0, 0)==1);
		
		}

		/**
			 * Testing Short input values.
			 * 
			 * @throws IOException
			 * @throws TransformException
			 */
			public void testLookupByte() throws IOException, TransformException {
		
				// /////////////////////////////////////////////////////////////////////
				//
				//
				//
				// /////////////////////////////////////////////////////////////////////
				final RenderedImage image = new ImageWorker(JAI.create("ImageRead", TestData.file(this,
						"usa.png"))).forceComponentColorModel().retainFirstBand().getRenderedImage();
				if (TestData.isInteractiveTest())
					RasterSymbolizerTest.visualize(image, "testLookupByte");
		
				// /////////////////////////////////////////////////////////////////////
				//
				// Build the categories
				//
				// /////////////////////////////////////////////////////////////////////
				final DefaultPiecewiseTransform1DElement c1 = DefaultLinearPiecewiseTransform1DElement
						.create("c1",NumberRange.create(1, 128), NumberRange.create(1, 255));
				final DefaultPiecewiseTransform1DElement c0 = DefaultLinearPiecewiseTransform1DElement
				.create("c0", NumberRange.create(129, 255), NumberRange.create(255,255));
				final DefaultPiecewiseTransform1DElement nodata = DefaultLinearPiecewiseTransform1DElement
						.create("nodata", NumberRange.create(0, 0),
								0);
				final DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement> list = 
				    new DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement>(new DefaultPiecewiseTransform1DElement[] { c0 ,c1,nodata});
				final ParameterBlockJAI pbj = new ParameterBlockJAI(
						GenericPiecewise.OPERATION_NAME);
				pbj.addSource(image);
				pbj.setParameter("Domain1D", list);
				final RenderedOp finalimage = JAI.create(
						GenericPiecewise.OPERATION_NAME, pbj);
		
				if (TestData.isInteractiveTest())
					RasterSymbolizerTest.visualize(finalimage, "testLookupByte");
				else
					finalimage.getTiles();
		
			}

		/**
		 * SWAN test-case.
		 * 
		 * @throws IOException
		 */
		public void testSWANLOGARITHMIC() throws IOException {
			// /////////////////////////////////////////////////////////////////////
			//
			//
			// /////////////////////////////////////////////////////////////////////
			final RenderedImage image = getSWAN();
			if (TestData.isInteractiveTest())
				RasterSymbolizerTest.visualize(image, "testSWANLOGARITHMIC");
			final RenderedOp statistics = ExtremaDescriptor.create(image, new ROI(new ImageWorker(image).binarize(0).getRenderedImage()),
					new Integer(1), new Integer(1), Boolean.FALSE, new Integer(1),
					null);
			final double[] minimum=(double[]) statistics.getProperty("minimum");
			final double[] maximum=(double[]) statistics.getProperty("maximum");
			
			
			final DefaultPiecewiseTransform1DElement mainElement = new DefaultPiecewiseTransform1DElement(
					"natural logarithm", NumberRange.create(minimum[0],  maximum[0]),
					new MathTransform1DAdapter() {
			
						public double derivative(double arg0)
								throws TransformException {
							
							return 1/arg0;
						}
			
						public double transform(double arg0)
								throws TransformException {
		
							return minimum[0]+
								1.2*Math.log(arg0/minimum[0])*
								((maximum[0]-minimum[0])/(Math.log(maximum[0]/minimum[0]))
										);
						}
			
					
			
						public boolean isIdentity() {
							return false;
						}
			
					});
			DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement> transform =
			    new DefaultPiecewiseTransform1D<DefaultPiecewiseTransform1DElement>(new DefaultPiecewiseTransform1DElement[] {mainElement},0);

				final ParameterBlockJAI pbj = new ParameterBlockJAI(
						GenericPiecewise.OPERATION_NAME);
				pbj.addSource(image);
				pbj.setParameter("Domain1D", transform);
		
				try {
					// //
					// forcing a bad band selection ...
					// //
					pbj.setParameter("bandIndex", new Integer(2));
					final RenderedOp d = JAI.create(
							GenericPiecewise.OPERATION_NAME, pbj);
					d.getTiles();
					// we should not be here!
					assertTrue(false);
				} catch (Exception e) {
					// //
					// ... ok, Exception wanted!
					// //
				}
		
				pbj.setParameter("bandIndex", new Integer(0));
				final RenderedOp finalImage = JAI.create(
						GenericPiecewise.OPERATION_NAME, pbj);
				if (TestData.isInteractiveTest())
					RasterSymbolizerTest.visualize(finalImage, "testSWANLOGARITHMIC");
				else
					finalImage.getTiles();
				finalImage.dispose();
			
		
		}

		/**
		 * Building an image based on SWAN data.
		 * 
		 * @return {@linkplain BufferedImage}
		 * 
		 * @throws IOException
		 * @throws FileNotFoundException
		 */
		private RenderedImage getSWAN() throws IOException, FileNotFoundException {
			final AsciiGridsImageReader reader = (AsciiGridsImageReader) new AsciiGridsImageReaderSpi()
					.createReaderInstance();
			reader.setInput(new FileImageInputStream(TestData.file(this,
					"arcgrid/SWAN_NURC_LigurianSeaL07_HSIGN.asc")));
			final RenderedImage image = reader.readAsRenderedImage(0, null);
			return image;
		}

		protected void setUp() throws Exception {
			try{
				new ParameterBlockJAI(GenericPiecewise.OPERATION_NAME);
				
			}catch (Exception e) {
				GenericPiecewise.register(JAI.getDefaultInstance());
			}
			
			// check that it exisits
			File file = TestData.copy(this, "arcgrid/arcgrid.zip");
			assertTrue(file.exists());

			// unzip it
			TestData.unzipFile(this, "arcgrid/arcgrid.zip");

			
			super.setUp();
		}

}
