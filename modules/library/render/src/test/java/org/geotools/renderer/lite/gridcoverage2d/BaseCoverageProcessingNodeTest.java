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
import org.geotools.renderer.lite.gridcoverage2d.BaseCoverageProcessingNode;
import org.geotools.util.SimpleInternationalString;

/**
 * {@link TestCase}    subclass for    {@link BaseCoverageProcessingNode}    .
 * @author    Simone Giannecchini, GeoSlutions.
 */
public class BaseCoverageProcessingNodeTest extends TestCase {

	private BaseCoverageProcessingNode testedObject;

	private BaseCoverageProcessingNode testedObject2;

	public BaseCoverageProcessingNodeTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.testedObject = new BaseCoverageProcessingNode(1,
				SimpleInternationalString.wrap("fake node"),
				SimpleInternationalString.wrap("fake node")) {
			protected GridCoverage2D execute() {
				return CoverageFactoryFinder.getGridCoverageFactory(null).create(
						"name",
						PlanarImage.wrapRenderedImage(RasterSymbolizerTest
								.getSynthetic(Double.NaN)),
						new GeneralEnvelope(new double[] { -90, -180 },
								new double[] { 90, 180 }),
						new GridSampleDimension[] { new GridSampleDimension(
								"sd", new Category[] { new Category("",
										Color.BLACK, 0) }, null) }, null, null);
			}
		};
		this.testedObject2 = new BaseCoverageProcessingNode(1,
				SimpleInternationalString.wrap("fake node"),
				SimpleInternationalString.wrap("fake node")) {

			protected GridCoverage2D execute() {
				return CoverageFactoryFinder.getGridCoverageFactory(null).create(
						"name",
						PlanarImage.wrapRenderedImage(RasterSymbolizerTest
								.getSynthetic(Double.NaN)),
						new GeneralEnvelope(new double[] { -90, -180 },
								new double[] { 90, 180 }),
						new GridSampleDimension[] { new GridSampleDimension(
								"sd", new Category[] { new Category("",
										Color.BLACK, 0) }, null) }, null, null);
			}
		};
	}

	public final void testExecute() {
		// execute
		assertNotNull(testedObject2.getOutput());
		// do nothing
		assertNotNull(testedObject2.getOutput());

		// add source clean output
		testedObject2.addSource(testedObject);
		testedObject2.addSink(testedObject);
		// recompute
		assertNotNull(testedObject2.getOutput());

		// dispose
		testedObject2.dispose(true);
	}

	public final void testDispose() {

		assertNotNull(testedObject.getOutput());
		// dispose
		testedObject.dispose(true);
		// do nothing
		testedObject.dispose(true);
		try {
			// trying to get the output from a disposed coverage should throw an
			// error
			testedObject.getOutput();
			assertTrue(false);
		} catch (Exception e) {

		}
	}

	public final void testAddSource() {
		// execute
		assertNotNull(testedObject2.getOutput());
		// do nothing since we have already executed
		assertNotNull(testedObject2.getOutput());

		// add source clean output but we also create a cycle which kills our
		// small framework
		testedObject2.addSource((testedObject));
		testedObject.addSink((testedObject2));
		try {
			testedObject2.addSink((testedObject));
			assertTrue(false);
		} catch (IllegalStateException e) {
			// TODO: handle exception
		}
		// recompute
		assertNotNull(testedObject2.getOutput());

		// dispose
		testedObject2.dispose(true);
	}

}
