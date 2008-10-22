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

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.repository.Service;
import org.geotools.repository.ServiceFinder;

/**
 * ServiceFinder decorator for adapting catalog.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class AdaptingServiceFinder implements ServiceFinder {

	AdaptingCatalog catalog;
	ServiceFinder finder;
	
	public AdaptingServiceFinder( AdaptingCatalog catalog, ServiceFinder finder ) {
		this.catalog = catalog;
		this.finder = finder;
	}
	
	public List aquire( Map params ) {
		return wrap( finder.aquire( params ) );
	}

	public List aquire( URI target ) {
		return wrap( finder.aquire( target ) );
	}

	public List aquire( URI id, Map params ) {
		return wrap( finder.aquire( id, params ) );
	}
	
	public List wrap ( List services ) {
		
		if ( services == null || services.isEmpty() )
			return services;
		
		List adapting = new ArrayList( services.size() );
		for ( Iterator s = services.iterator(); s.hasNext(); ) {
			Service service = (Service) s.next();
			adapting.add( new AdaptingService( service, catalog.getResolveAdapterFactoryFinder() ) );
		}
		
		return adapting;
	}

}
