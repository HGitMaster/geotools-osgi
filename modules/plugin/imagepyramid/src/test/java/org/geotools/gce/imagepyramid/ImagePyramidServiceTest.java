/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gce.imagepyramid;

import java.util.Iterator;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.geotools.coverage.grid.io.GridFormatFinder;

/**
 * @author Simone Giannecchini
 * @since 2.3
 * 
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/plugin/imagepyramid/src/test/java/org/geotools/gce/imagepyramid/ImagePyramidServiceTest.java $
 */
public class ImagePyramidServiceTest extends TestCase {

	/**
	 * 
	 */
	public ImagePyramidServiceTest() {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestRunner.run(ImagePyramidServiceTest.class);

	}

	public void testIsAvailable() {
		final Iterator<GridFormatFactorySpi> list = GridFormatFinder.getAvailableFormats().iterator();
		boolean found = false;

		while (list.hasNext()) {
			final GridFormatFactorySpi fac = (GridFormatFactorySpi) list.next();

			if (fac instanceof ImagePyramidFormatFactory) {
				found = true;

				break;
			}
		}

		assertTrue("ImageMosaicFormatFactorySpi not registered", found);
	}
}
