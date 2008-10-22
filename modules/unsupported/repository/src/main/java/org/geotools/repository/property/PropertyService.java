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
package org.geotools.repository.property;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;

import org.geotools.data.DataStore;
import org.geotools.data.property.PropertyDataStoreFactory;
import org.geotools.repository.Catalog;
import org.geotools.repository.DataStoreService;
import org.geotools.repository.GeoResource;
import org.geotools.repository.ServiceInfo;
import org.geotools.repository.defaults.DefaultServiceInfo;
import org.geotools.util.ProgressListener;

/**
 * Service handle for PropertyDataStore.
 *  
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class PropertyService extends DataStoreService {

	File directory;
	
	public PropertyService(
		Catalog parent, Map params, File directory, PropertyDataStoreFactory propDataStoreFactory 
	) {
		super(parent, params, propDataStoreFactory );
		this.directory = directory;
	}

	public boolean canResolve(Class adaptee) {
		if ( adaptee != null && adaptee.isAssignableFrom( File.class ) ) 
			return true;
		
		return super.canResolve( adaptee );
	}
	
	public Object resolve(Class adaptee, ProgressListener monitor) throws IOException {
		if ( adaptee != null && adaptee.isAssignableFrom( File.class ) ) {
			return directory;
		}
		
		return super.resolve( adaptee, monitor );
	}
	
	protected GeoResource createGeoResource(String typeName, DataStore dataStore) {
		return new PropertyGeoResource( this, typeName );
	}

	protected ServiceInfo createMetaData(DataStore dataStore, ProgressListener monitor) {
		
		String namespace = null;
		try {
			namespace = (String) PropertyDataStoreFactory.NAMESPACE.lookUp( getConnectionParams() );
		} 
		catch (IOException e) {
			logger.log( Level.WARNING, "Error looking up namespace", e );
		}
		
		if ( namespace == null ) {
			return super.createMetaData( dataStore, monitor );
		}
		
		DefaultServiceInfo info = (DefaultServiceInfo) super.createMetaData( dataStore, monitor );
		
		try {
			info.setSchema( new URI( namespace ) );
		}
		catch( URISyntaxException e ) {
			logger.log( Level.WARNING, "Error parsing namespace:" + namespace, e );
		}
		
		return info;
	}
	
	
	public URI getIdentifier() {
		return directory.toURI();
	}

}
