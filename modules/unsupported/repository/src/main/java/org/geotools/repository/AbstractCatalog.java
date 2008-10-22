/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.io.IOException;
import java.util.Map;

import org.geotools.util.ProgressListener;

/**
 * Abstract implementation of Catalog.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/AbstractCatalog.java $
 */
public abstract class AbstractCatalog implements Catalog {
	
	/** user data */
    private Map userData;

    
    /**
     * Catalogs do not have a parent so null is returned.
     * <p>
     * We can consider adding a global 'root' parent - but we will wait until we find a need, or if
     * users request.
     * </p>
     * 
     * @return null as catalogs do not have a parent
     */
    public Resolve parent( ProgressListener monitor ) {
        return null;
    }
    
    /**
     * Aquire info on this Catalog.
     * <p>
     * This is functionally equivalent to: <core>resolve(ICatalogInfo.class,monitor)</code>
     * </p>
     * 
     * @see Catalog#resolve(Class, ProgressListener)
     * @return ICatalogInfo resolve(ICatalogInfo.class,ProgressListener monitor);
     */
    public CatalogInfo getInfo( ProgressListener monitor ) throws IOException {
        return (CatalogInfo) resolve(CatalogInfo.class, monitor);
    }

    /**
     * @return The user / application specific data.
     */
    public Map getUserData() {
    		return userData;
    }
    
    /**
     * Indicate class and id.
     * 
     * @return string representing this IResolve
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        String classname = getClass().getName();
        String name = classname.substring(classname.lastIndexOf('.') + 1);
        buf.append(name);
        buf.append("("); //$NON-NLS-1$
        buf.append(getIdentifier());
        buf.append(")"); //$NON-NLS-1$
        return buf.toString();
    }
    
    /**
     * This method does nothing. Sublcasses should override if events are 
     * supported.
     */
    public void addListener(ResolveChangeListener listener) {
    	// do nothing
    }
    
    /**
     * This method does nothing. Sublcasses should override if events are 
     * supported.
     */
    public void removeListener(ResolveChangeListener listener) {
    	// do nothing
    }
    
    /**
     * This method does nothing. Sublcasses should override if events are 
     * supported.
     */
    public void fire(ResolveChangeEvent event) {
    	// do nothing
    }
}
