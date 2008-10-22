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
package org.geotools.renderer.lite.gridcoverage2d;

import java.awt.Color;

import javax.media.jai.PlanarImage;

import junit.framework.TestCase;

import org.geotools.coverage.Category;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.renderer.lite.gridcoverage2d.RootNode;

/**
 * Simple TestCase for      {@link RootNode}      class.
 * @author       Simone Giannecchini, GeoSolutions
 */
public class RootNodeTest extends TestCase {

	private RootNode root1;
	private RootNode root2;
	private RootNode root3;

	public RootNodeTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		// get a coverage
		final GridCoverage2D gc = CoverageFactoryFinder.getGridCoverageFactory(null)
				.create(
						"name",
						PlanarImage.wrapRenderedImage(RasterSymbolizerTest.getSynthetic(Double.NaN)),
						new GeneralEnvelope(new double[] { -90, -180 },new double[] { 90, 180 }),
						new GridSampleDimension[] { new GridSampleDimension("sd", new Category[] { new Category("",Color.BLACK, 0) }, null) },
						null,
						null);

		root1 = new RootNode(gc);
		root2 = new RootNode(gc);
		root3 = new RootNode(gc, null);

	}

	protected void tearDown() throws Exception {
		root1.dispose(true);
		root2.dispose(true);
		root3.dispose(true);
		super.tearDown();
	}

	public final void testExecute() {
		assertNotNull(root1.getOutput());
		assertNotNull(root2.getOutput());
		assertNotNull(root3.getOutput());
	}

	public final void testAddSource() {
	
		try {
			root3.addSource(root2);
			assertFalse(true);
		} catch (Exception e) {
			
		}

	}

	public final void testGetSource() {
		try {
			root1.getSource(2);
			assertFalse(true);
		} catch (Exception e) {
			
		}
		try {
			root2.getSource(2);
			assertFalse(true);
		} catch (Exception e) {
			
		}
		try {
			root3.getSource(2);
			assertFalse(true);
		} catch (Exception e) {
			
		}

		try {
			assertNotNull(root3.getSource(0));
			assertFalse(true);
		} catch (Exception e) {
			
		}
		try {
			assertNotNull(root2.getSource(0));
			assertFalse(true);
		} catch (Exception e) {
			
		}
		try {
			assertNotNull(root1.getSource(0));
			assertFalse(true);
		} catch (Exception e) {
			
		}

	}

	public final void testGetSources() {
		try {
			assertNotNull(root1.getSources());
			assertTrue(root1.getSources().size()==0);
		} catch (Exception e) {
		}
		try {
			assertNotNull(root2.getSources());
			assertTrue(root2.getSources().size()==0);
		} catch (Exception e) {
			
		}
		try {
			assertNotNull(root3.getSources());
			assertTrue(root3.getSources().size()==0);
		} catch (Exception e) {
			
		}

	}



}
