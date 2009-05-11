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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.geotools.data.Parameter;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.factory.Factory;
import org.geotools.factory.Hints;
import org.geotools.factory.OptionalFactory;
import org.opengis.util.InternationalString;
import org.opengis.util.ProgressListener;

/**
 * A driver adds the ability to work with a coverage format or service.
 * <p>
 * Classes implementing this interface basically act as factory for creating
 * connections to coverage sources like files, WCS services, WMS services,
 * databases, etc...
 * <p>
 * This class also offers basic create / delete functionality (which can be
 * useful for file based coverage formats).
 * <p>
 * Purpose of this class is to provide basic information about a certain
 * coverage service/format as well as about the parameters needed in order to
 * connect to a source which such a service/format is able to work against.
 * 
 * <p>
 * Notice that as part as the roll of a "factory" interface this class makes
 * available an {@link #isAvailable()} method which should check if all the
 * needed dependencies which can be jars as well as native libs or configuration
 * files.
 * 
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Jody Garnett
 * @since 2.5
 * 
 * 
 */
public interface Driver extends Factory, OptionalFactory {

    /**
     * Unique name (non human readable) that can be used to refer to this
     * implementation.
     * <p>
     * While the Title and Description will change depending on the users local
     * this name will be consistent.
     * </p>
     * Please note that a given file may be readable by several Drivers (the
     * description of each implementation should be provided to the user so they
     * can make an intelligent choice in the matter).
     * 
     * @return name of this {@link Driver}
     */
    public String getName();

    /**
     * Human readable title for this {@link Driver}.
     * 
     * @return human readable title for presentation in user interfaces
     */
    public InternationalString getTitle();

    /**
     * Describe the nature of this {@link Driver} implementation.
     * <p>
     * A description of this {@link Driver} type; the description should
     * indicate the format or service being made available in human readable
     * terms.
     * </p>
     * 
     * @return A human readable description that is suitable for inclusion in a
     *         list of available {@link Driver}s.
     */
    public InternationalString getDescription();

    /**
     * Test to see if this {@link Driver} is available, if it has all the
     * appropriate dependencies (jars or libraries).
     * <p>
     * One may ask how this is different than {@link #canConnect(Map)}, and
     * basically available can be used by finder mechanisms to list available
     * {@link Driver}s.
     * 
     * @return <code>true</code> if and only if this factory has all the
     *         appropriate dependencies on the classpath.
     */
    public boolean isAvailable();

    /**
     * The list of filename extensions handled by this driver.
     * <p>
     * This List may be empty if the Driver is not file based.
     * <p>
     * 
     * @return List of file extensions which can be read by this dataStore.
     */
    public List<String> getFileExtensions();

    /**
     * Describes the required (and optional) parameters that can be used to
     * connect to a {@link CoverageAccess}.
     * <p>
     * You can use this description to build up a valid Map<String,Serializable>
     * that is accepted by the connect / create / delete methods.
     * </p>
     * 
     * @return Param a {@link Map} describing the {@link Map} for
     *         {@link #connect(Map)}.
     */
    public Map<String, Parameter<?>> getConnectParameterInfo();

    /**
     * Test to see if this driver is suitable for processing the coverage
     * storage pointed to by the params map.
     * 
     * <p>
     * If this coverage storage requires a number of parameters then this method
     * should check that they are all present and that they are all valid. If
     * the coverage storage is a file reading data source then the extensions or
     * mime types of any files specified should be checked.
     * 
     * <p>
     * Note that this method will fail in case the {@link CoverageAccess} we are
     * trying to access does not exist.
     * 
     * @param params
     *                The full set of information needed to construct a live
     *                data source.
     * @return boolean <code>true</code> if and only if this driver can
     *         process the resource indicated by the param set and all the
     *         required params are Present.
     */
    public boolean canConnect(Map<String, Serializable> params);

    /**
     * Open up a connection to a {@link CoverageAccess}.
     * 
     * <p>
     * Note that, by mean of the <code>canCreate</code> parameter we can ask
     * this method whether to fail or not in case the {@link CoverageAccess} we
     * are trying to access does not exist.
     * 
     * @param params
     *                required {@link Param}s to connect to a certain
     *                {@link CoverageStore}
     *                
     * @return a {@link CoverageAccess} which
     * @throws IOException
     *                 in case something wrong happens during the connection.
     */
    public CoverageAccess connect(Map<String, Serializable> params,
            Hints hints, final ProgressListener listener)
            throws IOException;

    /**
     * Tells me whether or not this {@link Driver} supports removal of an
     * existing coverage storage.
     * 
     * @return <code>true</code> when removal of an existing coverage storage
     *         is supported, <code>false</code> otherwise.
     */
    public boolean isDeleteSupported();

    /**
     * Test to see if this driver is suitable for deleting the coverage
     * storage pointed to by the params map.
     * 
     * @param params
     *                The full set of information needed to delete a live
     *                data source.
     * @return boolean <code>true</code> if and only if this driver can
     *         delete the resource indicated by the param set and all the
     *         required params are Present.
     */
    public boolean canDelete(Map<String, Serializable> params)
            throws IOException;

    /**
     * Delete a certain {@link CoverageAccess}.
     * 
     * The {@link Driver} will attempt to delete the indicated
     * {@link CoverageAccess} in a format specific fashion. Full featured
     * drivers will delete all associated files, database objects, as
     * appropriate. The default behavior when no driver specific behavior is
     * provided is to attempt to delete source file.
     * 
     * <p>
     * It is unwise to have open {@link CoverageAccess} handles on this
     * {@link CoverageAccess} when it is deleted.
     * 
     * <p>
     * In case the underlying {@link CoverageAccess} is not present we can
     * instruct the {@link Driver} to fail with an {@link IOException} or to
     * simply silent ignore that an return.
     * 
     * @param params
     *                Map of <key,param> pairs used to specify how to connect to
     *                the target {@link CoverageAccess}. This is the same
     *                parameters described by getConnectParameters() - rather
     *                than making a connection we are going to remove the
     *                indicated resource.
     * @param listener
     *                which can be used to listen for progresses on this
     *                operation. It can be <code>null</code>.
     * @param failIfNotExists
     *                tells this {@link Driver} to fail in case the underlying
     *                {@link CoverageAccess} does not exist. Default behaviour
     *                is to do nothing.
     * @return <code>true</code> if everything goes fine, <code>false</code>
     *         otherwise.
     * @throws IOException
     *                 in case an error is encountered while trying to delete
     *                 the underlying {@link CoverageAccess}.
     */
    public boolean delete(Map<String, Serializable> params,
            final ProgressListener listener, boolean failIfNotExists)
            throws IOException;

    /**
     * Tells me whether or not this {@link Driver} supports creation of a new
     * coverage storage.
     * 
     * @return <code>true</code> when removal of of a new coverage storage is
     *         supported, <code>false</code> otherwise.
     */
    public boolean isCreateSupported();

    /**
     * Describes the required (and optional) parameters that can be used to
     * connect to a {@link CoverageAccess}.
     * <p>
     * 
     * @return Param a {@link Map} describing the {@link Map} for
     *         {@link #connect(Map)}.
     */
    public Map<String, Parameter<?>> getCreateParameterInfo();

    /**
     * Test to see if this driver is suitable for creating the coverage
     * storage pointed to by the params map.
     * 
     * <p>
     * If this coverage storage requires a number of parameters then this method
     * should check that they are all present and that they are all valid. 
     * 
     * @param params
     *                The full set of information needed to create a 
     *                data source.
     * @return boolean <code>true</code> if and only if this driver can
     *         create the storage indicated by the param set and all the
     *         required params are Present.
     */
    public boolean canCreate(Map<String, Serializable> params);

    /**
     * Create a {@link CoverageAccess}.
     * 
     * The {@link Driver} will attempt to create the named
     * {@link CoverageAccess} in a format specific fashion. Full featured
     * drivers will create all associated files, database objects, or whatever
     * is appropriate.
     * 
     * 
     * @param params
     *                Map of <key,value> pairs used to specify how to create the
     *                target {@link CoverageAccess}.
     * @param hints
     *                map of <key,value> pairs which can be used to control the
     *                behavior of the entire library.
     * @param listener
     *                which can be used to listen for progresses on this
     *                operation. It can be <code>null</code>.
     * @return a {@link CoverageAccess} instance which is connected to the newly
     *         created coverage storage.
     * @throws IOException
     *                 in case something bad happens.
     */
    public CoverageAccess create(Map<String, Serializable> params, Hints hints,
            final ProgressListener listener) throws IOException;

}
