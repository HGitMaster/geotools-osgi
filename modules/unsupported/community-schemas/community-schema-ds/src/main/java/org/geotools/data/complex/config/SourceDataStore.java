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

package org.geotools.data.complex.config;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.util.CheckedHashMap;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: SourceDataStore.java 31374 2008-09-03 07:26:50Z bencd $
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/community-schemas/community-schema-ds/src/main/java/org/geotools/data/complex/config/SourceDataStore.java $
 * @since 2.4
 */
public class SourceDataStore implements Serializable{
	private static final long serialVersionUID = 8540617713675342340L;
	private String id;
	private Map params = Collections.EMPTY_MAP;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Map getParams() {
		return new HashMap( params);
	}
	public void setParams(Map params) {
		this.params = new CheckedHashMap(Serializable.class, Serializable.class);
		if(params != null){
			this.params.putAll(params);
		}
	}
}
