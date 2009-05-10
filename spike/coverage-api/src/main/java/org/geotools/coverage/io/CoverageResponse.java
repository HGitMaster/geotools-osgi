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

import org.geotools.coverage.io.impl.DefaultCoverageRequest;
import org.opengis.coverage.Coverage;
import org.opengis.util.ProgressListener;

/**
 * A coverage response; please check the status before assuming any data is
 * available.
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 */
public interface CoverageResponse {

    /**
     * @author Simone Giannecchini, GeoSolutions
     */
    public enum Status {
        FAILURE, WARNING, SUCCESS, UNAVAILABLE
    }

    /**
     * The handle attribute is included to allow a client to associate a
     * mnemonic name to the Query request. The purpose of the handle attribute
     * is to provide an error handling mechanism for locating a statement that
     * might fail.
     * 
     * @return the mnemonic name of the query request.
     */
    public String getHandle();

    /**
     * Get the status of this coverage response. It should always be checked
     * before assuming any data is available.
     * 
     * @return the {@linkplain Status status} of this coverage response.
     */
    public Status getStatus();

    
    public Collection<? extends Exception> getExceptions();

    /**
     * @return the {@link DefaultCoverageRequest} originating that
     *         {@link CoverageResponse}.
     */
    public CoverageRequest getRequest();

    /**
     * Returns the Coverages available with this coverage response.
     * 
     * @param listener
     * @return a collection of coverages.
     */
    public Collection<? extends Coverage> getResults(
            final ProgressListener listener);

}
