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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.geotools.repository.ResolveAdapterFactory;

/**
 * Class for locating instances of {@link ResolveAdapterFactory}.
 * <p>
 * 	This class should be subclassed to provide a particular lookup mechanism. 
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public abstract class ResolveAdapterFactoryFinder {

	/**
	 * Performs an adapter lookup. 
	 * <p>
	 * This method should be extended or overriden.
	 * </p>
	 * 
	 * @param decorator The adapting resolve decorator.
	 * @param adaptee The class being adapted to.
	 * 
	 * @return The adapting factory, otherwise <code>null</code> if non could 
	 * be found.
	 * 
	 * @throws IllegalStateException If multiple adapter factories are found 
	 * which can support the adaptation.
	 */
	public ResolveAdapterFactory find ( AdaptingResolve decorator, Class adaptee )
		throws IllegalStateException {
		
		if ( decorator.resolve == null ) 
			return null;
		
		Collection factories = getResolveAdapterFactories();
		List matches = new ArrayList();
		for ( Iterator f = factories.iterator(); f.hasNext(); ) {
			ResolveAdapterFactory factory = (ResolveAdapterFactory) f.next();
			if ( factory instanceof AdaptingResolveAware ) {
				((AdaptingResolveAware) factory).setAdaptingResolve( decorator );
			}
			
			if ( factory.canAdapt( decorator.resolve, adaptee) ) {
				matches.add( factory );
			}
		}
		
		if ( matches.isEmpty() )
			return null;
		
		if ( matches.size() != 1 ) {
			String msg = "Multiple adapters found.";
			throw new IllegalStateException( msg );
		}
		
		return (ResolveAdapterFactory) matches.get( 0 );
	}
	
	/**
	 * Located all available adapter factories.
	 * <p>
	 * This method is intended to be overiden.
	 * </p>
	 * @return A collection of adapters, may be empty, should not be null.
	 * 
	 * TODO: default to factory spi?
	 */
	public Collection getResolveAdapterFactories() {
		return Collections.EMPTY_LIST;
	}
}
