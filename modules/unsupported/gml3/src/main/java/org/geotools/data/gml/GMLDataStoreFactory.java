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
package org.geotools.data.gml;

import java.io.IOException;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.xml.Configuration;

/**
 * Datastore factory for gml datastore.
 * <p>
 * <br>
 * <h2>Usage</h2>
 * <br>
 * <br>
 * When creating an instance of the datastore, the {@link #LOCATION} parameter must 
 * be specified. 
 * <pre>
 * <code>
 * GMLDataStoreFactory factory = new GMLDataStoreFactory();
 * 
 * Map params = new HashMap();
 * params.put( GMLDataStoreFactory.LOCATION, "instanceDocument.xml" );
 * 
 * GMLDataStore dataStore = factory.createDataStore( params );
 * </code>
 * </pre>
 * Creating a datastore with only the location parameter causes the datastore to deduce 
 * all the information about the schema from the instance document directly. Which means 
 * the schemaLocation must be properly specifed in the instance document. As this is not 
 * always the case, there are ways to specify information about the schema to the datastore.
 * <br>
 * <ol>
 *  <li>Specifying the targetNamespace and schemaLocation directly.
 *  <li>Specifying a {@link org.geotools.xml.Configuration} 
 * </ol>
 * </p>
 * <br>
 * <p>
 * <h3>Target namespace and Schema Location</h3>
 * <pre>
 * <code>
 * GMLDataStoreFactory factory = new GMLDataStoreFactory();
 * 
 * Map params = new HashMap();
 * params.put( GMLDataStoreFactory.LOCATION, "instanceDocument.xml" );
 * params.put( GMLDataStoreFactory.NAMESPACE, "http://myApplicationSchemaNamespaceUri" );
 * params.put( GMLDataStoreFactory.SCHEMALOCATION, "/path/to/applicationSchema.xsd" );
 * 
 * GMLDataStore dataStore = factory.createDataStore( params );
 * </code>
 * </pre>
 * </p>
 * <p>
 * <h3>Configuration</h3>
 * <br>
 * A {@link org.geotools.xml.Configuration} is used to specify information about a schema and 
 * configure the xml parser to parse instances of the schema. The 
 * {@link org.geotools.gml3.ApplicationSchemaConfiguration} is a subclass that can be extented
 * in order to create a configuration specific to an application schema:
 * <pre>
 * <code>
 * class MyApplictionSchemaConfiguration extends ApplicationSchemaConfiguration {
 *   
 *   public MyApplicationSchema() {
 *     super( "http://myApplicationSchemaNamespaceUri", "/path/to/applicationSchema.xsd" );
 *   }
 *   
 * }
 * </pre>
 * </code>
 * The class of the application schema can then be supplied with the {@link #CONFIGURATION}
 * paramter:
 * <pre>
 * <code>
 * GMLDataStoreFactory factory = new GMLDataStoreFactory();
 * 
 * Map params = new HashMap();
 * params.put( GMLDataStoreFactory.LOCATION, "instanceDocument.xml" );
 * params.put( GMLDataStoreFactory.CONFIGURATION, MyApplicationSchemaConfiguration.class );
 * 
 * GMLDataStore dataStore = factory.createDataStore( params );
 * </code>
 * </pre>
 * It is important to note that when using the CONFIGURATION parameter the configuration class
 * must have a no-argument constructor.
 * </p>
 * <br>
 * <p>
 * TODO: document support for application schema defined types
 * 
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class GMLDataStoreFactory implements DataStoreFactorySpi {

	/**
	 * The location of the instance document.
	 */
	public static Param LOCATION = 
		new Param( "location", String.class, "Instance document location", true );
	
	/**
	 * The application schema configuration
	 */
	public static Param CONFIGURATION =
		new Param( "configuration", Class.class, "Application schema configuration", false );
	
	/**
	 * Application schema namespace
	 */
	public static Param NAMESPACE = 
		new Param( "namespace", String.class, "Application schema namespace", false);
	/**
	 * Location of application schema
	 */
	public static Param SCHEMALOCATION = 
		new Param( "schemaLocation", String.class, "Application schema location", false );
	
	public DataStore createDataStore(Map params) throws IOException {
		String location = (String) LOCATION.lookUp( params );
		
		if ( location != null ) {
			Class configuration = (Class) CONFIGURATION.lookUp( params );
			if ( configuration != null ) {
				try {
					return new GMLDataStore( location, (Configuration) configuration.newInstance() );
				} 
				catch( Exception e ){
					throw (IOException) new IOException().initCause( e );
				}
			}
			else {
				String namespace = (String) NAMESPACE.lookUp( params );
				String schemaLocation = (String) SCHEMALOCATION.lookUp( params );
				
				if ( namespace != null && schemaLocation != null ) {
					return new GMLDataStore( 
						location, new ApplicationSchemaConfiguration ( namespace, schemaLocation ) 
					);
				}
			}
			
			return new GMLDataStore( location );
		}
		
		return null;
	}

	public DataStore createNewDataStore(Map params) throws IOException {
		throw new UnsupportedOperationException();
	}

	public String getDisplayName() {
		return "GML";
	}

	public String getDescription() {
		return "Geographic Markup Language";
	}

	public Param[] getParametersInfo() {
		return new Param[] {
			LOCATION, CONFIGURATION, NAMESPACE, SCHEMALOCATION
		};
	}

	public boolean canProcess(Map params) {
		return params.containsKey( LOCATION.key );
	}

	public boolean isAvailable() {
		return true;
	}

	public Map getImplementationHints() {
		return null;
	}

}
