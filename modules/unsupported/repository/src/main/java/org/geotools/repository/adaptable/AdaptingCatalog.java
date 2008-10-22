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
import java.util.List;

import org.geotools.repository.Catalog;
import org.geotools.repository.CatalogInfo;
import org.geotools.repository.Service;
import org.geotools.util.ProgressListener;

import com.vividsolutions.jts.geom.Envelope;

public class AdaptingCatalog extends AdaptingResolve
	implements Catalog {
	
	
	protected AdaptingCatalog( Catalog catalog, ResolveAdapterFactoryFinder adapter ) {
		super(catalog, adapter);
		
	}
	
	protected Catalog catalog() {
		return (Catalog) resolve;
	}

	public void add(Service service) throws UnsupportedOperationException {
		if ( !( service instanceof AdaptingResolve ) ) {
			service = new AdaptingService( service, finder );
		}
		
		catalog().add( service );
	}

	public void remove(Service service) throws UnsupportedOperationException {
		catalog().remove( service );
	}

	public void replace(URI id, Service service) throws UnsupportedOperationException {
		catalog().replace( id, service );
	}

	public List find(URI id, ProgressListener monitor) {
		return catalog().find( id, monitor );
	}

	public List findService(URI query, ProgressListener monitor) {
		return catalog().findService( query, monitor );
	}

	public List search(String pattern, Envelope bbox, ProgressListener monitor) throws IOException {
		return catalog().search( pattern, bbox, monitor );
	}

	public CatalogInfo getInfo(ProgressListener monitor) throws IOException {
		return catalog().getInfo( monitor );
	}

}
