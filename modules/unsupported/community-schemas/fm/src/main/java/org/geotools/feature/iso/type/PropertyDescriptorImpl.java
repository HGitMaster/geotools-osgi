/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.feature.iso.type;

import java.util.HashMap;
import java.util.Map;

import org.opengis.feature.type.PropertyDescriptor;

public abstract class PropertyDescriptorImpl implements PropertyDescriptor {

	Map properties = new HashMap();
	
	
	public void putUserData(Object key, Object value) {
		properties.put(key,value);

	}

	public Object getUserData(Object key) {
		return properties.get(key);
	}


}