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

/**
 * Implemented by {@link ResolveAdapterFactory} instances who need 
 * access to the adapting delegate of a resolve being adapted.
 *
 * <p>
 * This interfaces is used only inside of the {@link AdaptingResolve} 
 * framework, and nowhere else.
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public interface AdaptingResolveAware {

	/**
	 * Sets the adapting delegate.
	 * <p>
	 * This method is always called before either of:
	 * {@link ResolveAdapterFactory#canAdapt(Resolve, Class)}
	 * {@link ResolveAdapterFactory#adapt(Resolve, Class, ProgressListener)}
	 * </p>
	 * @param adaptingResolve The adapting resolve which is a delegate for 
	 * the resolve being adapted.
	 */
	void setAdaptingResolve( AdaptingResolve adaptingResolve );
}
