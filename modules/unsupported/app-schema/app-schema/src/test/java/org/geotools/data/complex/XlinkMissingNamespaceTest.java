package org.geotools.data.complex;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotSame;

import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.data.complex.config.AppSchemaDataAccessConfigurator;
import org.geotools.data.complex.config.AppSchemaDataAccessDTO;
import org.geotools.data.complex.AttributeMapping;
import org.geotools.data.complex.config.XMLConfigDigester;

import org.junit.Test;

import org.opengis.feature.type.Name;
import org.opengis.filter.expression.Expression;

/**
 * To test client properties of app-schema mapping file
 * 
 * @author Jacqui Githaiga, Curtin University of Technology
 */
public class XlinkMissingNamespaceTest {
	/**
	 * Illustrates that if xlink namespace has not been declared in the
	 * app-schema mapping file,the client property is set as if for href (in the
	 * no-name namespace).
	 * 
	 * This test shows correct behaviour by throwing an exception reporting the
	 * undeclared namespace.
	 * 
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testGetClientProperties() throws IOException {

		try {

			XMLConfigDigester reader = new XMLConfigDigester();

			URL url = getClass().getResource(
					"/test-data/MappedFeatureMissingNamespaceXlink.xml");
			AppSchemaDataAccessDTO config = reader.parse(url);
			Set mappings = AppSchemaDataAccessConfigurator
					.buildMappings(config);
			fail("No exception caught");

		} catch (Exception ex) {
			assertSame(java.lang.IllegalArgumentException.class, ex.getClass());

		}

	}
}
