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
package org.geotools.repository.adaptable;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.geotools.repository.Catalog;
import org.geotools.repository.GeoResource;
import org.geotools.repository.Resolve;
import org.geotools.repository.ResolveAdapterFactory;
import org.geotools.repository.ResolveChangeEvent;
import org.geotools.repository.ResolveChangeListener;
import org.geotools.repository.Service;
import org.geotools.resources.Utilities;
import org.geotools.util.ProgressListener;

/**
 * Base class for "adapting" resolves.
 * 
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 * 
 */
public abstract class AdaptingResolve implements Resolve {

	/**
	 * The wrapped resolve.
	 */
	protected Resolve resolve;
	/**
	 * Adapter used to adapt the resolve.
	 */
	protected ResolveAdapterFactoryFinder finder;
	/**
	 * Cached wrapped parent
	 */
	AdaptingResolve parent;
	/**
	 * Cached wrapped members
	 */
	List members;
	/**
	 * Cached adaptations
	 */
	HashMap adapterCache;
	/**
	 * a tuple to be used as the key in the adapter cache.
	 */
	private static class Key {
		
		public Resolve resolve;
		public Class adaptee;
		
		public Key( Resolve resolve, Class adaptee ) {
			this.resolve = resolve;
			this.adaptee = adaptee;
		}
		
		public boolean equals(Object obj) {
			if ( obj instanceof Key ) {
				Key other = (Key) obj;
				return Utilities.equals( resolve, other.resolve) && 
					Utilities.equals( adaptee, other.adaptee );
			}
			
			return false;
		}
		
		public int hashCode() {
			int hash = 7;
			hash = 31 * hash + ( resolve != null ? resolve.hashCode() : 0 );
			hash = 31 * hash + ( adaptee != null ? adaptee.hashCode() : 0 );
			
			return hash;
		}
	}
	
	AdaptingResolve ( Resolve resolve, ResolveAdapterFactoryFinder finder ) {
		this.resolve = resolve;
		this.finder = finder;
		adapterCache = new HashMap();
	}
	
	public ResolveAdapterFactoryFinder getResolveAdapterFactoryFinder() {
		return finder;
	}
	
	/**
	 * Sublcasses need to override and ensure that returned members are 
	 * instances of {@link AdaptingResolve}.
	 * <p>
	 * As a convenience the {@link #wrap(List, Class)} method can be used as 
	 * follows:
	 * 
	 * <code>
	 * 	<pre>
	 * 		return wrap( resolve.members( monitor), AbstractResolve.class );
	 * 	</pre>
	 * </code>
	 * </p>
	 */
	public synchronized final List members(ProgressListener monitor) 
		throws IOException {
		
		//get members from underlying handle
		List rMembers = resolve.members( monitor );
		
		// reupdate cached members if 
		// 1. the cache is null
		// 2. the cache does match rMembers
		if ( members == null ) {
			members = wrap( rMembers );
		}
		else {
			boolean equal = true;
			if ( members.size() != rMembers.size() ) {
				equal = false;
			}
			else {
				Iterator r = rMembers.iterator();
				Iterator m = members.iterator();
				
				while( r.hasNext() && equal ) {
					Resolve rMember = (Resolve) r.next();
					AdaptingResolve member = (AdaptingResolve) m.next();
					
					equal = Utilities.equals( rMember, member );
					
				}
			}
			
			if ( !equal ) {
				members = wrap( rMembers );
			}
		}
		
		return members;
	}

	/**
	 * Wraps a list of catalog handles in an associated "adapting" implemenation. 
	 * 
	 * @param members List of catalog handles.
	 *
	 * @return The list of wrapped handles.
	 */
	protected List wrap( List members ) {
		List wrapped;
		try {
			wrapped = (List) members.getClass().newInstance();
		} 
		catch (Exception e) {
			wrapped = new ArrayList();
		} 
		
		
		for ( Iterator m = members.iterator(); m.hasNext(); ) {
			Resolve member = (Resolve) m.next();
			AdaptingResolve wrapper = wrap( member );
			if ( wrapper != null ) {
				wrapped.add( wrapper );
			}
		}
		
		return wrapped;
	}
	
	/**
	 * Wraps a resove handle in an adapting resolve handle.
	 * 
	 * <p>
	 * If <code>resolve</code> already extends {@link AdaptingResolve}, then it 
	 * is not wrapped. This method supports the following "wraps":
	 * <ul>
	 * 	<li>{@link Catalog} -> {@link AdaptingCatalog}.
	 *  <li>{@link Service} -> {@link AdaptingService}.
	 *  <li>{@link GeoResource} -> {@link AdaptingGeoResource}.
	 * </ul>
	 * </p>
	 * @param resolve The catalog handle being wrapped in an adapting version 
	 * of it.
	 * 
	 * @return The adapting catalog handle, or null if <code>resolve</code> is 
	 * not an instance of Catlaog,Serfvice,or GeoResource.
	 */
	protected AdaptingResolve wrap( Resolve resolve ) {
		if ( resolve instanceof AdaptingResolve ) {
			return (AdaptingResolve) resolve;
		}
		
		if ( resolve instanceof Catalog ) {
			return new AdaptingCatalog( (Catalog) resolve, finder );
		}
		
		if ( resolve instanceof Service ) {
			return new AdaptingService( (Service) resolve, finder );
		}
		
		if ( resolve instanceof GeoResource ) {
			return new AdaptingGeoResource( (GeoResource) resolve, finder );
		}
		
		return null;
	}
	
	public final boolean canResolve(Class adaptee) {
		if ( finder.find( this, adaptee ) != null )
			return true;
		
		return resolve.canResolve( adaptee );
	}
	
	public final Object resolve(Class adaptee, ProgressListener monitor) throws IOException {
		
		//check the cache
		Key key = new Key( resolve, adaptee );
		if ( adapterCache.containsKey( key) ) {
			return adapterCache.get( key );
		}
		
		ResolveAdapterFactory factory = finder.find( this, adaptee );
		if ( factory != null ) {
			if ( factory instanceof AdaptingResolveAware ) {
				((AdaptingResolveAware) factory).setAdaptingResolve( this );
			}
			Object adapter = factory.adapt( resolve, adaptee, monitor );
			if ( adapter != null ) {
				adapterCache.put( key, adapter );
				return adapter;
			}
		}
		
		if ( resolve.canResolve( adaptee ) ) {
			return resolve.resolve( adaptee, monitor );
		}
	
		return null;
	}

	public synchronized final Resolve parent(ProgressListener monitor) 
		throws IOException {
		
		Resolve rParent = resolve.parent( monitor );
		
		// update the parent cahce if 
		// 1. the cache is null 
		// OR
		// 2. the cached parents underling resolve does not match rParent
		if ( parent == null || !Utilities.equals( parent.resolve, rParent )) {
			parent = wrap( rParent );
		}
		
		return parent;
	}

	public Status getStatus() {
		return resolve.getStatus();
	}

	public Throwable getMessage() {
		return resolve.getMessage();
	}

	public URI getIdentifier() {
		return resolve.getIdentifier();
	}

	public void addListener(ResolveChangeListener listener) throws UnsupportedOperationException {
		resolve.addListener( listener );
	}

	public void removeListener(ResolveChangeListener listener) {
		resolve.removeListener( listener );
	}

	public void fire(ResolveChangeEvent event) {
		resolve.fire( event );
	}
}
