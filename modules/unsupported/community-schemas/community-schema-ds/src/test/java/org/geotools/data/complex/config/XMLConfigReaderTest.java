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

package org.geotools.data.complex.config;

import java.net.URL;
import java.util.Set;
import java.util.logging.Level;

import junit.framework.TestCase;

import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.util.logging.Logging;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: XMLConfigReaderTest.java 31607 2008-10-01 07:55:36Z bencd $
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/community-schemas/community-schema-ds/src/test/java/org/geotools/data/complex/config/XMLConfigReaderTest.java $
 * @since 2.4
 */
public class XMLConfigReaderTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'org.geotools.data.complex.config.XMLConfigReader.parse(URL)'
	 */
	public void testParseURL() throws Exception{
		XMLConfigDigester reader = new XMLConfigDigester();
		URL url = XMLConfigDigester.class.getResource("/test-data/roadsegments.xml");
		ComplexDataStoreDTO config = reader.parse(url);
		
		Set mappings = ComplexDataStoreConfigurator.buildMappings(config);
		
		assertNotNull(mappings);
		assertEquals(1, mappings.size());
		FeatureTypeMapping mapping = (FeatureTypeMapping)mappings.iterator().next();
		
		assertEquals(6, mapping.getAttributeMappings().size());
		assertNotNull(mapping.getTargetFeature());
		assertNotNull(mapping.getSource());
		
		//Map/*<String, Expression>*/idMappings = mapping.getIdMappings();
		//assertEquals(idMappings.get("RoadSegment"), ExpressionBuilder.parse("getId()"));
	}

}
