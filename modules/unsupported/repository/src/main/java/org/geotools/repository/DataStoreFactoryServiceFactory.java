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

import java.net.URI;
import java.util.Map;

import org.geotools.data.DataStoreFactorySpi;

/**
 * Wraps up a {@link org.geotools.data.DataStoreFactorySpi}.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class DataStoreFactoryServiceFactory implements ServiceFactory {

	DataStoreFactorySpi factory;
	
	public DataStoreFactoryServiceFactory( DataStoreFactorySpi factory ) {
		this.factory = factory;
	}
	
	public Service createService(Catalog parent, URI id, Map params) {
		if ( factory.canProcess( params ) ) {
			return new DataStoreService( parent, params, factory );
		}
		
		return null;
	}

	public boolean canProcess(URI uri) {
		return false;
	}

	public Map createParams(URI uri) {
		return null;
	}

}
