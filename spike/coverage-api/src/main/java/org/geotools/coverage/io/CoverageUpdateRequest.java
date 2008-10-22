/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.coverage.io;

import java.util.Collection;
import java.util.Map;

import org.opengis.coverage.Coverage;

public interface CoverageUpdateRequest extends CoverageRequest{

	public abstract java.lang.String[] getMetadataNames()
			throws java.io.IOException;

	public abstract java.lang.String getMetadataValue(java.lang.String arg0)
			throws java.io.IOException;

	/**
	 * @param metadata
	 * @throws java.io.IOException
	 * @uml.property  name="metadata"
	 */
	public abstract void setMetadata(Map<String, String> metadata)
			throws java.io.IOException;

	/**
	 * @return
	 * @throws java.io.IOException
	 * @uml.property  name="metadata"
	 */
	public abstract Map<String, String> getMetadata()
			throws java.io.IOException;

	/**
	 * @param  metadata
	 * @uml.property  name="data"
	 */
	public abstract void setData(Collection<? extends Coverage> data);

	/**
	 * @return
	 * @uml.property  name="data"
	 */
	public abstract Collection<? extends Coverage> getData();

}