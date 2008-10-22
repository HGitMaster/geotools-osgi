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
package org.geotools.repository;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.repository.defaults.DefaultGeoResourceInfo;
import org.geotools.util.ProgressListener;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Resource implementation for resources which can map or resolve to a 
 * {@link org.geotools.data.FeatureSource}.
 * <p>
 * Subclasses may with to override the methods:
 * <ul>
 * 	<li>{@link #createMetaData(FeatureSource, ProgressListener)}
 * </ul>
 * 
 * In addition, subclasses may wish to  <b>extend</b> the following methods in 
 * order to support additional resolves.
 * <ul>
 * 	<li>{@link #canResolve(Class)}
 * 	<li>{@link #resolve(Class, ProgressListener)}
 * </ul>
 * 
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class FeatureSourceGeoResource extends AbstractGeoResource {

	/**
	 * Parent handle
	 */
	DataStoreService parent;
	/**
	 * Feature type name
	 */
	String name;
	/**
	 * Cached feature source
	 */
	FeatureSource<SimpleFeatureType, SimpleFeature> source;
	/**
	 * metadata object 
	 */
	GeoResourceInfo info;
	
	public FeatureSourceGeoResource ( DataStoreService parent, String name ) {
		this.parent = parent;
		this.name = name;
	}
	
	/**
	 * @return The name of the feature source, feature type.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Supports the required GeoResource resolves with an additional resolves 
	 * to:
	 * <ul>
	 * 	<li>{@link FeatureSourceGeoResource}
	 * 	<li>{@link SimpleFeatureType}
	 *  <li>{@link DataStore}
	 *  </ul>
	 * <p>
	 * Subclasses may wish to extend this method.
	 * </p>
	 */
	public boolean canResolve(Class adaptee) {
		if ( adaptee == null)
			return false;
		
		return adaptee.isAssignableFrom( Service.class ) || 
			adaptee.isAssignableFrom( GeoResourceInfo.class ) || 
			adaptee.isAssignableFrom( FeatureSource.class ) || 
			adaptee.isAssignableFrom( SimpleFeatureType.class ) || 
			adaptee.isAssignableFrom( DataStore.class );
	}
	
	/**
	 * Supports the required GeoResource resolves with an additional resolves 
	 * to:
	 * <ul>
	 * 	<li>{@link FeatureSourceGeoResource}
	 * 	<li>{@link SimpleFeatureType}
	 *  <li>{@link DataStore}
	 *  </ul>
	 * <p>
	 * Subclasses may wish to extend this method.
	 * </p>
	 */
	public Object resolve(Class adaptee, ProgressListener monitor)
			throws IOException {
		
		if ( adaptee == null )
			return null;
		
		if ( adaptee.isAssignableFrom( Service.class ) )
			return parent;
		
		if ( adaptee.isAssignableFrom( GeoResourceInfo.class ) )
			return getInfo( monitor );
		
		if ( adaptee.isAssignableFrom( FeatureSource.class ) ) 
			return featureSource( monitor );
		
		if ( adaptee.isAssignableFrom( SimpleFeatureType.class ) )
			return parent.dataStore( monitor ).getSchema( name );
		
		if ( adaptee.isAssignableFrom( DataStore.class) )
			return parent.dataStore( monitor );
		
		return null;
	}

	/**
	 * Returns the status of the handle based on the following.
	 * 
	 * 1. If a non-null error has been set with {@link #setMessage(Throwable)}
	 * then the handle is {@link Status#BROKEN}.
	 * 2. If {@link #source} is non-null the handle is {@link Status#CONNECTED}.
	 * 3. The handle is {@link Status#NOTCONNECTED}.
	 */
	public Status getStatus() {
		if ( getMessage() != null )  
			return Status.BROKEN;
		
		if ( source != null )
			return Status.CONNECTED;
		
		return Status.NOTCONNECTED;
	}
	
	public GeoResourceInfo getInfo(ProgressListener monitor) throws IOException {
		if ( info == null ) {
			
			DataStore dataStore = parent.dataStore( monitor );
			
			synchronized (dataStore) {
				if ( info == null ) {
					try {
						info = createMetaData( featureSource( monitor ), monitor );
						setMessage( null );
					}
					catch( Throwable t ) {
						String msg = "unable to create metadata";
						logger.log( Level.SEVERE, msg, t );
						setMessage( t );
					}
				}
			}
		}
		
		return info;
	}

	/**
	 * Creates the resource metadata.
	 * <p>
	 * Data providers providing custom metadata need to override this method. 
	 * The default implementation provided the following metadata mappings:
	 * 
	 * <ul>
	 * 	<li>{@link FeatureSource#getBounds()} -> {@link GeoResourceInfo#getBounds()}
	 * 	<li>{@link SimpleFeatureType#getTypeName()} -> {@link GeoResourceInfo#getName()}
	 * 	<li>{@link SimpleFeatureType#getNamespace()()} -> {@link GeoResourceInfo#getSchema()()}
	 * </ul>
	 * </p>
	 * 
	 * @param source  the underlying FeatureSource
	 * @param monitor a ProgressListener for blocking calls.  
	 * 
	 * @return The resource info.
	 */
	protected GeoResourceInfo createMetaData( FeatureSource<SimpleFeatureType, SimpleFeature> source, ProgressListener monitor ) 
		throws IOException {

		//calculate bounds
		ReferencedEnvelope rBounds = null;
		ReferencedEnvelope bounds = source.getBounds();
		if ( bounds != null ) {
			//we have an "optmized bounds", do we have a crs?
			if ( bounds instanceof ReferencedEnvelope ) {
				rBounds = (ReferencedEnvelope) bounds;
			}
			
			if ( rBounds == null ) {
				//since we had an optimized bounds from feature source, we would
				// like to avoid accessing the data, so check the type for 
				// crs info
				SimpleFeatureType schema = source.getSchema();
				if ( schema.getGeometryDescriptor() != null ) {
					CoordinateReferenceSystem crs = schema.getCoordinateReferenceSystem();					
					if ( crs != null ) {
						rBounds = new ReferencedEnvelope( bounds, crs );
					}
					else {
						rBounds = null;
					}
				}
			}
			
			if ( rBounds == null ) {
				rBounds = new ReferencedEnvelope( bounds, null );
			}
		}
		else {
		    
			//manually calculate the bounds
			bounds = new ReferencedEnvelope(source.getSchema().getCoordinateReferenceSystem());
			
			FeatureIterator<SimpleFeature> itr = source.getFeatures().features();
			while( itr.hasNext() ) {
				BoundingBox more = itr.next().getBounds();
                bounds.include( more );
			}
			
			
			SimpleFeatureType schema = source.getSchema();
			CoordinateReferenceSystem crs = null;
			if ( schema.getGeometryDescriptor() != null ) {
				crs = schema.getCoordinateReferenceSystem();
			}
			
			rBounds = new ReferencedEnvelope( bounds, crs );
		}
		
		String name = source.getSchema().getTypeName();
		URI schema;
        try {
            schema = new URI( source.getSchema().getName().getNamespaceURI() );
        } catch (URISyntaxException e) {
            schema = null;
        }		
		return new DefaultGeoResourceInfo( null, name, null, schema, rBounds, null, null );
	}
	
	/**
	 * Uses the parent identifer, and tacks {@link #name} on as a fragment.
	 */
	public URI getIdentifier() {
		URI uri = parent.getIdentifier();
		
		try {
			return new URI( uri.getScheme(), uri.getHost(), uri.getPath(), name );
		} 
		catch (URISyntaxException e) {
			String msg = "Unable to build uri identifer";
			logger.log( Level.WARNING, msg, e );
			setMessage( e );
		}
		
		try {
			return new URI( name );
		} 
		catch (URISyntaxException e) {
			//shoult not happen
		}
		
		return null;
		
	}

	protected FeatureSource<SimpleFeatureType, SimpleFeature> featureSource( ProgressListener monitor ) {
		
		if ( source == null ) {
			DataStore dataStore = parent.dataStore( monitor );
			try {
				synchronized ( dataStore ) {
					source = dataStore.getFeatureSource( name );
					if ( source == null )
						throw new NullPointerException();
					
					setMessage( null );
					return source;
				}
			}
			catch( Throwable t ) {
				String msg = "Unable to resolve feature source.";
				logger.log( Level.SEVERE, msg, t );
				setMessage( t );
			}
		}
		
		return source;
	}
	
	protected SimpleFeatureType featureType( ProgressListener monitor ) {
		 if ( featureSource( monitor ) != null ) 
			 return featureSource( monitor ).getSchema();
		 
		 return null;
	}
}
