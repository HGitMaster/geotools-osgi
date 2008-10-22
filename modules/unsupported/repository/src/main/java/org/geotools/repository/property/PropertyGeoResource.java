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

import org.geotools.repository.FeatureSourceGeoResource;
import org.geotools.util.ProgressListener;

public class PropertyGeoResource extends FeatureSourceGeoResource {

	public PropertyGeoResource( PropertyService service, String name ) {
		super( service, name );
	}

	public boolean canResolve(Class adaptee) {
		if ( adaptee == null) 
			return false;
	
		
		if ( adaptee.isAssignableFrom( File.class ) ) {
			return true;
		}
		
		return super.canResolve( adaptee );
	}
	
	public Object resolve(Class adaptee, ProgressListener monitor) throws IOException {
		if ( adaptee == null ) 
			return null;
		
		if ( adaptee.isAssignableFrom( File.class ) ) {
			PropertyService service = (PropertyService) parent( monitor );
			return new File( service.directory, getName() + ".properties" );
		}
		
		return super.resolve( adaptee, monitor );
	}
	
}
