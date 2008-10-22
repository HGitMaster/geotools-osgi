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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.coverage.io.CoverageAccess;
import org.geotools.coverage.io.Driver;
import org.geotools.data.Parameter;
import org.geotools.factory.Hints;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.ProgressListener;

/**
 * Base class extending {@link BaseDriver} leveraging on URLs.
 */
public abstract class BaseFileDriver extends BaseDriver implements Driver {
	/**
	 * Parameter "url" used to indicate to a local file or remote resource being
	 * accessed as a coverage.
	 */
	public final static Parameter<URL> URL = new Parameter<URL>("url",
			java.net.URL.class, new SimpleInternationalString("URL"),
			new SimpleInternationalString(
					"Url to a local file or remote location"));

	/**
	 * Parameter "file" used to indicate to indicate a local file.
	 */
	public final static Parameter<File> FILE = new Parameter<File>("file",
			File.class, new SimpleInternationalString("File"),
			new SimpleInternationalString( "Local file"));

	/**
	 * Utility method to convert a URL to a file; or return null
	 * if not possible.
	 * @param url
	 * @return File or null if not available
	 */
	public static File toFile( URL url ){
		if( url == null ) return null;
		if (url.getProtocol().equalsIgnoreCase("file")) {
			try {
				return new File(URLDecoder.decode(url.getFile(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		}
		return null;				
	}
	
	/**
	 * Utility method to help convert a URL to a file if possible.
	 * 
	 * @param url
	 * @return File
	 */
	public static File urlToFile(URL url) {
		URI uri;
		try {
			// this is the step that can fail, and so
			// it should be this step that should be fixed
			uri = url.toURI();
		} catch (URISyntaxException e) {
			// OK if we are here, then obviously the URL did
			// not comply with RFC 2396. This can only
			// happen if we have illegal unescaped characters.
			// If we have one unescaped character, then
			// the only automated fix we can apply, is to assume
			// all characters are unescaped.
			// If we want to construct a URI from unescaped
			// characters, then we have to use the component
			// constructors:
			try {
				uri = new URI(url.getProtocol(), url.getUserInfo(), url
						.getHost(), url.getPort(), url.getPath(), url
						.getQuery(), url.getRef());
			} catch (URISyntaxException e1) {
				// The URL is broken beyond automatic repair
				throw new IllegalArgumentException("broken URL: " + url);
			}
		}
		return new File(uri);
	}

	private List<String> fileExtensions;

	protected BaseFileDriver(final String name, final String description,
			final String title, final Hints implementationHints,
			final List<String> fileExtensions) {
		super(name, description, title, implementationHints);
		this.fileExtensions = new ArrayList<String>(fileExtensions);
	}

	public List<String> getFileExtensions() {
		return new ArrayList<String>(fileExtensions);
	}

	/**
	 * Test to see if this driver is suitable for connecting to the coverage
	 * storage pointed to by the specified {@link URL} source.
	 * 
	 * @param source
	 *            URL a {@link URL} to a real file (may not be local)
	 * 
	 * @return True when this driver can resolve and read a coverage storage
	 *         specified by the {@link URL}.
	 */
	public abstract boolean canConnect(URL source);

	/**
	 * Subclass can override to define required parameters.
	 * <p>
	 * Default implementation expects a single URL indicating the
	 * location.
	 * 
	 * @return
	 */
	protected Map<String, Parameter<?>> defineConnectParameterInfo(){
		HashMap<String, Parameter<?>> info = new HashMap<String, Parameter<?>>();
		info.put(URL.key, URL);
		return info;
	}
	/**
	 * Open up a connection to a {@link CoverageAccess}.
	 * 
	 * <p>
	 * Note that, by mean of the <code>canCreate</code> parameter we can ask
	 * this method whether to fail or not in case the {@link CoverageAccess} we
	 * are trying to access does not exist.
	 * 
	 * @param params
	 *            required params to connect to a certain coverage store.
	 * 
	 * @param source
	 *            source of data (often a file or http url)
	 * @param params
	 *            Additional parameters
	 * @param hints
	 *            Implementation specific hints; please see the documentation
	 *            for the driver you are using
	 * @param listener
	 *            Used to report on progress during the conneciton process
	 * @return a {@link CoverageAccess} allowing data acess to the source
	 *         provided
	 * @throws IOException
	 *             in case something wrong happens during the connection.
	 */
	public abstract CoverageAccess connect(URL source,
			Map<String, Serializable> params, Hints hints,
			final ProgressListener listener) throws IOException;
	
	/**
	 * Test to see if this driver is suitable for creating a coverage storage
	 * pointed to by the specified {@link URL} source.
	 * 
	 * @param source
	 *            URL a {@link URL} to a real file (may not be local)
	 * 
	 * @return True when this driver can create a coverage storage specified by
	 *         the {@link URL}.
	 */
	public boolean canCreate(URL source) {
		return false;
	}

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
	 *            Map of <key,value> pairs used to specify how to create the
	 *            target {@link CoverageAccess}.
	 * @param hints
	 *            map of <key,value> pairs which can be used to control the
	 *            behaviour of the entire library.
	 * @param listener
	 *            which can be used to listen for progresses on this operation.
	 *            It can be <code>null</code>.
	 * @return a {@link CoverageAccess} instance which is connected to the newly
	 *         created coverage storage.
	 * @throws IOException
	 *             in case something bad happens.
	 */
	public CoverageAccess create(URL source, Map<String, Serializable> params,
			Hints hints, final ProgressListener listener) throws IOException {
		if (isCreateSupported()) {
			throw new UnsupportedOperationException(getTitle()
					+ " does not implement create operation");
		} else {
			throw new UnsupportedOperationException(getTitle()
					+ " does not support create operation");
		}
	}

	/**
	 * Test to see if this driver is suitable for deleting a coverage storage
	 * pointed to by the specified {@link URL} source.
	 * 
	 * @param source
	 *            URL a {@link URL} to a real file (may not be local)
	 * 
	 * @return True when this driver can delete a coverage storage specified by
	 *         the {@link URL}.
	 */
	public boolean canDelete(URL source) {
		return false;
	}

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
	 *            Map of <key,param> pairs used to specify how to connect to the
	 *            target {@link CoverageAccess}.
	 * @param listener
	 *            which can be used to listen for progresses on this operation.
	 *            It can be <code>null</code>.
	 * @param failIfNotExists
	 *            tells this {@link Driver} to fail in case the underlying
	 *            {@link CoverageAccess} does not exist. Default behaviour is to
	 *            do nothing.
	 * @return <code>true</code> if everything goes fine, <code>false</code>
	 *         otherwise.
	 * @throws IOException
	 *             in case an error is encountered while trying to delete the
	 *             underlying {@link CoverageAccess}.
	 */
	public boolean delete(URL source, Map<String, Serializable> params,
			final ProgressListener listener, boolean failIfNotExists)
			throws IOException {
		if (isDeleteSupported()) {
			throw new UnsupportedOperationException(getTitle()
					+ " does not implement delete operation");
		} else {
			throw new UnsupportedOperationException(getTitle()
					+ " does not support delete operation");
		}
	}
}
