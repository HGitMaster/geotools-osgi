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
package org.geotools.process;

import java.util.Map;

import org.geotools.data.Parameter;
import org.opengis.util.InternationalString;

/**
 * Used to describe the parameters needed for a Process, and for creating a Process to use.
 *
 * @author gdavis
 */
public interface ProcessFactory {
	
    /**
     * Unique name (non human readable) that can be used to
     * refer to this implementation.
     * <p>
     * This name is used to advertise the availability of a Process
     * in a WPS; while the Title and Description will change depending
     * on the users locale; this name will be consistent.
     * </p>
     * It is up to the implementor to ensure this name is unique
     * @return name of this process factory
     */
	public String getName();	
	
    /** Human readable title suitable for display.
     * <p>
     * Please note that this title is *not* stable across locale; if you want
     * to remember a ProcessFactory between runs please use getName (which is
     * dependent on the implementor to guarantee uniqueness) or use the classname
     */
	public InternationalString getTitle();
	
	/**
	 * Human readable description of this process.
	 * @return
	 */
	public InternationalString getDescription();
	
	/**
	 * Description of the Map parameter to use when executing.
	 * @return Description of required parameters
	 */
	public Map<String,Parameter<?>> getParameterInfo();
	
	/**
	 * Create a process for execution.
	 * @return Process implementation
	 */
	public Process create();
	
	public Map<String,Parameter<?>> getResultInfo(Map<String, Object> parameters) throws IllegalArgumentException;
	
	/**
	 * It is up to the process implementors to implement progress on the task,
	 * this method is used to see if the process has progress monitoring implemented
	 * @return true if it supports progress monitoring
	 */
	public boolean supportsProgress();
	
	/**
	 * Return the version of the process
	 * @return String version
	 */
	public String getVersion();	
}
