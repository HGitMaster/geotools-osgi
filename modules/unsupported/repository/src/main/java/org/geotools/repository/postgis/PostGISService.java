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
 *
 */
package org.geotools.repository.postgis;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.repository.Catalog;
import org.geotools.repository.DataStoreService;
import org.geotools.repository.GeoResource;
import org.geotools.repository.ServiceInfo;
import org.geotools.repository.defaults.DefaultServiceInfo;
import org.geotools.util.ProgressListener;

public class PostGISService extends DataStoreService {

	public PostGISService( Catalog parent, Map params, PostgisDataStoreFactory dataStoreFactory ) {
		super(parent, params, dataStoreFactory);
	}

	protected GeoResource createGeoResource( String typeName, DataStore dataStore ) {
		return new PostGISGeoResource( this, typeName );
	}

	public URI getIdentifier() {
		//return the jdbc uri
		Map params = getConnectionParams();
		String host = (String) params.get( PostgisDataStoreFactory.HOST.key );
		String port = (String) params.get( PostgisDataStoreFactory.PORT.key );
		String database = (String) params.get( PostgisDataStoreFactory.DATABASE.key );
		
		try {
			return new URI( null, host, database, null );
		} 
		catch (URISyntaxException e) {
			//should not happen
			return null;
		}
	}

	protected ServiceInfo createMetaData(DataStore dataStore, ProgressListener monitor) {
		//set the namespace
		URI schema = null;
		try {
			String ns = (String) PostgisDataStoreFactory.NAMESPACE.lookUp( getConnectionParams() );
			if ( ns != null ) {
				schema = new URI( ns );
			}
		}
		catch( Exception e ) {
			//do nothing
		}
		 
		ServiceInfo info = super.createMetaData( dataStore, monitor );
		
		return new DefaultServiceInfo( 
			info.getTitle(), info.getDescription(), info.getAbstract(), info.getSource(), 
			info.getPublisher(), schema, info.getKeywords(), info.getIcon()
		);
	
	}
	
}
