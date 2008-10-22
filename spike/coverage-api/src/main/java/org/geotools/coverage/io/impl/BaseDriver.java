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
package org.geotools.coverage.io.impl;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.geotools.coverage.io.CoverageAccess;
import org.geotools.coverage.io.Driver;
import org.geotools.data.Parameter;
import org.geotools.factory.Hints;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.InternationalString;
import org.opengis.util.ProgressListener;

/**
 * Base Implementation for the {@link Driver} interface.
 */
public abstract class BaseDriver implements Driver {

    private String name;

    private InternationalString description;

    private InternationalString title;

    private Map<Key, ?> implementationHints;

    private Map<String, Parameter<?>> connectParameterInfo;
	private Map<String, Parameter<?>> createParameterInfo;

    protected BaseDriver(final String name, final String description,
            final String title, final Hints implementationHints) {
        this.name = name;
        this.description = new SimpleInternationalString(description);
        this.title = new SimpleInternationalString(title);
    }

    public String getName() {
        return this.name;
    }

    public InternationalString getTitle() {
        return this.title;
    }

    /**
     * Implementation hints provided during construction.
     * <p>
     * Often these hints are configuration and factory settings
     * used to intergrate the driver with application services.
     */
    public Map<Key, ?> getImplementationHints() {
        return this.implementationHints;
    }
    public InternationalString getDescription() {
        return this.description;
    }


    public synchronized Map<String, Parameter<?>> getConnectParameterInfo() {
		if( connectParameterInfo == null ){
			connectParameterInfo = defineConnectParameterInfo();
			if( connectParameterInfo == null ){
				connectParameterInfo = Collections.emptyMap();
			}
		}
		return connectParameterInfo;
	}

	/**
	 * Called to define the value returned by getConnectionParameterInfo().
	 * <p>
	 * Subclasses should provide an implementation of this method
	 * indicating the parameters they require.
	 * </p>
	 */
    protected abstract Map<String, Parameter<?>> defineConnectParameterInfo();

    /**
     * Subclass can override to support create operations.
     * @return false - subclass can override when create is implemented
     */
    public boolean isCreateSupported() {
    	return false;
    }
    
	public synchronized Map<String, Parameter<?>> getCreateParameterInfo() {
		if( createParameterInfo == null ){
			createParameterInfo = defineCreateParameterInfo();
			if( createParameterInfo == null ){
				createParameterInfo = Collections.emptyMap();
			}
		}
		return createParameterInfo;
	}

	/**
	 * Define the parameters required for creation.
	 * <p>
	 * Subclasses should override this method when changing isCreatedSupported to
	 * true.
	 * </p>
	 * @return The default implementation returns an empty map.
	 */
    protected Map<String, Parameter<?>> defineCreateParameterInfo() {
		return Collections.emptyMap();
	}

	/** Subclass can override to support create operations
     * @return false - subclass can override when create is implemented
     */
    public boolean canCreate(Map<String, Serializable> params) {
    	return false;
    }
    /** Subclass can override to support create operations */
    public CoverageAccess create(Map<String, Serializable> params, Hints hints,
    		ProgressListener listener) throws IOException {
    	if( isCreateSupported()){    		
    		throw new UnsupportedOperationException( getTitle()+" does not implement create operation");
    	}
    	else {
    		throw new UnsupportedOperationException( getTitle()+" does not support create operation");
    	}
    }
    
    /** Subclass can override to support delete operations */
    public boolean isDeleteSupported() {
    	return false;
    }
    /**
     * Subclass can override to support delete operations.
     * @return false - subclass can override when delete is implemented
     */
    public boolean canDelete(Map<String, Serializable> params)
    		throws IOException {
    	return false;
    }
    /** Subclass can override to support delete operations.
     * @return false - subclass can override when delete is implemented
     */
    public boolean delete(Map<String, Serializable> params,
    		ProgressListener listener, boolean failIfNotExists)
    		throws IOException {
    	if( isDeleteSupported()){    		
    		throw new UnsupportedOperationException( getTitle()+" does not implement delete operation");
    	}
    	else {
    		throw new UnsupportedOperationException( getTitle()+" does not support delete operation");
    	}
    }
  
}
