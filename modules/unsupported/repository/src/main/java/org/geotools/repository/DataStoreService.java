/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.repository;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.repository.defaults.DefaultServiceInfo;
import org.geotools.util.ProgressListener;

/**
 * Service implementation for services which resolve to a 
 * {@link org.geotools.data.DataStore}.
 * <p>
 * Subclasses may wish to override the following methods.
 * <ul>
 * 	<li>{@link org.geotools.catalog.Resolve#getIdentifier()}
 * 	<li>{@link #createDataStoreFactory()}
 *  <li>{@link #createMetaData(DataStore, ProgressListener)}
 *  <li>{@link #createGeoResource(String, DataStore)}
 * </ul>
 * In addition, subclasses may wish to  <b>extend</b> the following methods in 
 * order to support additional resolves.
 * <ul>
 * 	<li>{@link #canResolve(Class)}
 * 	<li>{@link #resolve(Class, ProgressListener)}
 * </ul>
 * 
 * For example, the following implements an additional resolve to a particular
 * data store implementation.
 * <br>
 * <code>
 * 	<pre>
 * 	class ShapefileService extends DataStoreService {
 *   ...
 *     boolean canResolve( Class adaptee ) {
 *       if (adaptee != null) {
 *         if ( adaptee.isAssignableFrom( ShapefileDataStore.class ) ) { 
 *            return true;
 *         }
 *       }
 *       
 *       return super.canResolve( adaptee );
 *     }
 *   ...
 *  }
 * 	</pre>
 * </code>
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class DataStoreService extends AbstractService {

	/**
	 * Factory used to create datastores.
	 */
	DataStoreFactorySpi dataStoreFactory;
	
	/**
	 * The underying datastore, cached.
	 */
	DataStore dataStore;
	
	/**
	 * cached service info.
	 */
	ServiceInfo info;
	
	public DataStoreService(Catalog parent, Map params, DataStoreFactorySpi dataStoreFactory ) {
		super(parent, params);
		this.dataStoreFactory = dataStoreFactory;
	}

	/**
	 * @return The datastore factory.
	 */
	public DataStoreFactorySpi getDataStoreFactory() {
		return dataStoreFactory;
	}
	
	/**
	 * Returns a uri constructed from {@link DataStoreFactorySpi#getDisplayName()}, 
	 * sublcasses may wish to override.
	 */
	public URI getIdentifier() {
		try {
			return new URI( dataStoreFactory.getDisplayName() );
		} 
		catch (URISyntaxException e) {
			throw new RuntimeException( e );
		}
	}
	
	/**
	 * Supports the required Service resolves with an additional resolve to 
	 * {@link DataStore}.
	 * <p>
	 * Subclasses may wish to extend this method.
	 * </p>
	 */
	public boolean canResolve(Class adaptee) {
		if ( adaptee == null )
			return false;
		
		return adaptee.isAssignableFrom( ServiceInfo.class ) || 
			adaptee.isAssignableFrom( List.class ) || 
			adaptee.isAssignableFrom( DataStore.class );
	}
	
	/**
	 * Supports the required Service resolves with an additional resolve to 
	 * {@link DataStore}.
	 * <p>
	 * Subclasses may wish to extend this method.
	 * </p>
	 */
	public Object resolve(Class adaptee, ProgressListener monitor)
			throws IOException {
		
		if ( adaptee == null )
			return null;
		
		if ( adaptee.isAssignableFrom( ServiceInfo.class ) ) {
			return getInfo( monitor );
		}
		
		if ( adaptee.isAssignableFrom( List.class ) ) {
			return members( monitor );
		}
		
		if ( adaptee.isAssignableFrom( DataStore.class ) ) {
			return dataStore( monitor );
		}
		
		return null;
	}

	public List members(ProgressListener monitor) throws IOException {
		if ( getMembers() == null ) {
			DataStore dataStore = dataStore( monitor );
			synchronized ( dataStore ) {
				if ( getMembers() == null ) {
					ArrayList members = new ArrayList();
					String[] typeNames = dataStore.getTypeNames();
					for ( int i = 0; i < typeNames.length; i++ ) {
						String typeName = typeNames[i];
						GeoResource resource = createGeoResource( typeName, dataStore );
						if ( resource != null ) {
							members.add( resource );
						}
						else {
							String msg = "Unable to create resource for " + typeName;
							logger.warning( msg );
						}
					}
					
					setMembers( members );
				}
			}
		}
		
		return getMembers();
	}
	
	public ServiceInfo getInfo(ProgressListener monitor) throws IOException {
		if ( info == null ) {
			DataStore dataStore = dataStore( monitor );
			synchronized ( dataStore ) {
				if ( info == null ) {
					try {
						info = createMetaData( dataStore, null );
						setMessage( null );
					}
					catch ( Throwable t ) {
						String msg = "Error retreiving medatata.";
						logger.log( Level.SEVERE, msg, t );
						setMessage( t );
					}
				}
			}
		}
		
		return info;
	}
	

	protected DataStore dataStore( ProgressListener monitor ) {
		if ( dataStore == null ) {
			
			synchronized ( dataStoreFactory.getClass() ) {
				if ( dataStore == null ) {
					try {
						dataStore = dataStoreFactory.createDataStore( getConnectionParams() );
						if ( dataStore == null ) {
							throw new NullPointerException();
						}
						
						setMessage( null );
					}
					catch( Throwable t ) {
						String msg = "Unable to resolve datastore";
						logger.log( Level.SEVERE, msg, t );
						setMessage( t );
					}
				}
			}
		}
		
		return dataStore;
	}
	
	/**
	 * Creates the service metadata.
	 * <p>
	 * Sublcasses should override to supply custom metadata specific to 
	 * a particular DataStore. This default implementation supplies the 
	 * following metadata mappings in an instance of {@link DefaultServiceInfo}.
	 * 	<ul>
	 * 		<li>{@link DataStoreFactorySpi#getDisplayName()} -> {@link ServiceInfo#getTitle()}
	 * 		<li>{@link DataStoreFactorySpi#getDescription()} -> {@link ServiceInfo#getDescription()}.
	 * 	</ul>
	 * </p>
	 * 
	 * @param dataStore The underlying datastore.
	 * @param monitor Progress monitor for blocking calls.  
	 * 
	 * @return An instance of {@link DefaultServiceInfo}.
	 */
	protected ServiceInfo createMetaData( DataStore dataStore, ProgressListener monitor ) {
        return new DefaultServiceInfo( 
    		dataStoreFactory.getDisplayName(), dataStoreFactory.getDescription(), null, null, 
    		null, null, null, null
		);
	}
	
	/**
	 * Creates the GeoResource handle for a specified feature type name.
	 * <p>
	 * Subclasses should override this method if they need to create a custom geo resource.
	 * The default implementation returns a new instance of {@link FeatureSourceGeoResource}.
	 * </p>
	 * @param typeName The name of the feature type.
	 * @param dataStore The datastore containing the feature type.
	 * 
	 * @return The GeoResource handle, or null if it could not be created.
	 */
	protected GeoResource createGeoResource( String typeName, DataStore dataStore ) {
		return new FeatureSourceGeoResource( this, typeName );
	}
	
	
}
