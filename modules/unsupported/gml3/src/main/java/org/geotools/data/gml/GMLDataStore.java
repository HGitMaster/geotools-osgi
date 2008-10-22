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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geotools.data.DataSourceException;
import org.geotools.data.store.AbstractDataStore2;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.gml3.GML;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.gml3.bindings.GML3ParsingUtils;
import org.geotools.xml.BindingWalkerFactory;
import org.geotools.xml.Configuration;
import org.geotools.xml.impl.BindingLoader;
import org.geotools.xml.impl.BindingWalkerFactoryImpl;
import org.geotools.xml.impl.TypeWalker;
import org.opengis.feature.simple.SimpleFeatureType;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class GMLDataStore extends AbstractDataStore2 {

	/**
	 * Document location
	 */
	String location;
	/**
	 * Application schema / parser configuration
	 */
	Configuration configuration;
	
	/**
	 * Creates a new datastore from an instance document.
	 * <p>
	 * Using this constructor forces the datastore to infer the application schema directly 
	 * from the instance document.
	 * </p>
	 * 
	 * @param location The location ( as a uri ) of the instance document.
	 */
	public GMLDataStore ( String location ) {
		this( location, null );
	}
	
	/**
	 * Creates a new datastore from an instance document and application schema configuration.
	 * 
	 * @param location The location ( as a uri ) of the instance document.
	 * @param configuration The application schema configuration.
	 */
	public GMLDataStore( String location, Configuration configuration ) {
		this.location = location;
		this.configuration = configuration;
	}
	
	/**
	 * @return The location of the instance document to parse.
	 */
	String getLocation() {
		return location;
	}
	
	/**
	 * @return the application schema configuration
	 */
	Configuration getConfiguration() {
		return configuration;
	}
	
	/**
	 * Configuration accessor, which ensures a configuration is returned by infering it from
	 * the instance document if need be, or throwing an exception otherwise.
	 * 
	 * @return
	 * @throws DataSourceException
	 */
	Configuration configuration() throws DataSourceException {
		if ( configuration == null ) {
			synchronized ( this ) {
				if ( configuration == null ) {
					try {
						//parse some of the instance document to find out the schema location
						InputStream input = document();
						
						//create stream parser
						XmlPullParser parser = null;
						
						XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
						factory.setNamespaceAware(true);
						factory.setValidating(false);
							
						//parse root element
						parser = factory.newPullParser();
						parser.setInput( input, "UTF-8" );
						parser.nextTag();
						
						//look for schema location
						for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
							if ( "schemaLocation".equals( parser.getAttributeName( i ) ) ) {
								String xsiSchemaLocation = parser.getAttributeValue( i );
								String[] split = xsiSchemaLocation.split( " " );
								if ( split.length > 2 ) {
									String msg = "Multiple schemaLocations not supported";
									throw new DataSourceException( msg );
								}
								
								String namespace = split[ 0 ];
								String schemaLocation = split[ 1 ];
								
								configuration = 
									new ApplicationSchemaConfiguration( namespace, schemaLocation );
								
								break;
							}
						}
						
						//reset input stream
						parser.setInput( null );
						input.close();
					} 
					catch (Exception e) {
						String msg = "Unable to determine schema from instance document";
						throw new DataSourceException( msg, e );
					}
				}
			}
		}
		
		return configuration;
	}
	
	/**
	 * @return The location of the application schema.
	 * 
	 */
	String schemaLocation() throws DataSourceException {
		return configuration().getSchemaFileURL();
	}
	
	protected List createContents() {
		//TODO: this method should create content lazily.
		try {
			List contents = new ArrayList();
			Configuration configuration = configuration();
			XSDSchema schema = configuration.schema();
			
			//look for elements in the schema which are of type AbstractFeatureType
			for ( Iterator e = schema.getElementDeclarations().iterator(); e.hasNext(); ) {
				XSDElementDeclaration element = (XSDElementDeclaration) e.next();
				if ( !configuration.getNamespaceURI().equals( element.getTargetNamespace() ) ) 
					continue;
				
				final ArrayList isFeatureType = new ArrayList();
				TypeWalker.Visitor visitor = new TypeWalker.Visitor() {

					public boolean visit(XSDTypeDefinition type) {
						if ( GML.NAMESPACE.equals( type.getTargetNamespace() ) && 
								GML.AbstractFeatureCollectionType.getLocalPart().equals( type.getName() ) ) {
							return false;
						}
						
						if ( GML.NAMESPACE.equals( type.getTargetNamespace() ) && 
								GML.AbstractFeatureType.getLocalPart().equals( type.getName() ) ) {
							isFeatureType.add( Boolean.TRUE );
							return false;
						}
						
						return true;
					}
					
				};
				
				XSDTypeDefinition type = element.getType().getBaseType();
				new TypeWalker().walk( type, visitor );
				
				if ( !isFeatureType.isEmpty() ) {
					SimpleFeatureType featureType = featureType( element );
					contents.add( new GMLTypeEntry( this, featureType, null ) );
				}
			}
			
			return contents;
		} 
		catch (IOException e) {
			throw new RuntimeException( e );
		}
	}
	
	/**
	 * Helper method for transforming an xml feautre type to a geotools feature type.
	 * @param element
	 * @return
	 * @throws IOException
	 */
	private SimpleFeatureType featureType( XSDElementDeclaration element )
		throws IOException {
		
		//load up the bindings for type conversion
		GMLConfiguration configuration = new GMLConfiguration();
		
		BindingLoader bindingLoader = new BindingLoader();
		bindingLoader.setContainer( configuration.setupBindings( bindingLoader.getContainer() ) );
		
		MutablePicoContainer context = new DefaultPicoContainer();
		context = configuration.setupContext( context );
		
		BindingWalkerFactory bwFactory = new BindingWalkerFactoryImpl( bindingLoader, context );
		try {
			return GML3ParsingUtils.featureType( element, bwFactory );
		} 
		catch (Exception e) {
			throw (IOException) new IOException().initCause( e );
		}
	}
	
	/**
	 * @return An input stream for hte document.
	 */
	InputStream document() throws IOException {
		File location;
		try {
			location = new File( new URI( getLocation() ) );
		} 
		catch (URISyntaxException e) {
			throw (IOException) new IOException().initCause( e );
		}
		return new BufferedInputStream( new FileInputStream( location ) );
	}
	
	/**
	 * Helper method which lazily parses the application schema.
	 * 
	 * @throws IOException
	 */
	XSDSchema schema() throws IOException {
		return configuration().schema();
	}
//	
//		if ( schema == null ) {
//			synchronized ( this ) {
//				if ( schema == null ) {
//					GMLConfiguration configuration = new GMLConfiguration();
//					
//					//get all the necessary schema locations
//					List dependencies = configuration.allDependencies();
//					List resolvers = new ArrayList();
//					for ( Iterator d = dependencies.iterator(); d.hasNext(); ) {
//						Configuration dependency = (Configuration) d.next();
//						XSDSchemaLocationResolver resolver = dependency.getSchemaLocationResolver();
//						if ( resolver != null ) {
//							resolvers.add( resolver );
//						}
//					}
//					
//					//if a schema location was specified, add one for it
//					if ( schemaLocation == null ) {				
//						//parse some of the instance document to find out the schema location
//						InputStream input = document();
//						
//						//create stream parser
//						XmlPullParser parser = null;
//						
//						try {
//							XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//							factory.setNamespaceAware(true);
//							factory.setValidating(false);
//								
//							//parse root element
//							parser = factory.newPullParser();
//							parser.setInput( input, "UTF-8" );
//							parser.nextTag();
//							
//							//look for schema location
//							for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
//								if ( "schemaLocation".equals( parser.getAttributeName( i ) ) ) {
//									String xsiSchemaLocation = parser.getAttributeValue( i );
//									String[] split = xsiSchemaLocation.split( " " );
//									for ( int j = 0; j < split.length; j += 2 ) {
//										if ( namespace.equals( split[ j ] ) ) {
//											schemaLocation = split[ j + 1 ];
//											break;
//										}
//									}
//									
//									break;
//								}
//							}
//							
//							//reset input stream
//							parser.setInput( null );
//							input.close();
//						} 
//						catch (XmlPullParserException e) {
//							throw (IOException) new IOException().initCause( e );
//						}
//					}
//				
//					if ( schemaLocation == null ) {
//						throw new DataSourceException( "Unable to determine application schema location ");
//					}
//					
//					schema = Schemas.parse( schemaLocation, null, resolvers );
//				}
//			}
//		}
//		
//		return schema;
//	}
}
