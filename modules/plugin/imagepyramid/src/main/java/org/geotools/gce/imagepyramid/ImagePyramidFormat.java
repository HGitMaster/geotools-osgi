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
package org.geotools.gce.imagepyramid;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.data.DataSourceException;
import org.geotools.data.PrjFileReader;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This class implements the basic format capabilities for a coverage format.
 * 
 * @author Simone Giannecchini (simboss)
 */
public final class ImagePyramidFormat extends AbstractGridFormat implements
		Format {

	/** Logger. */
	private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.gce.imagepyramid");

	
	/** Control the transparency of the output coverage. */
	public static final DefaultParameterDescriptor OUTPUT_TRANSPARENT_COLOR = new DefaultParameterDescriptor(
			"OutputTransparentColor", Color.class, null,null);
	
	/** Control the type of the final mosaic. */
	public static final DefaultParameterDescriptor FADING = new DefaultParameterDescriptor(
			"Fading", Boolean.class, null, Boolean.FALSE);

	/** Control the transparency of the output coverage. */
	public static final DefaultParameterDescriptor INPUT_TRANSPARENT_COLOR = new DefaultParameterDescriptor(
			"InputTransparentColor", Color.class, null, null);

	/** Control the thresholding on the input coverage */
	public static final DefaultParameterDescriptor INPUT_IMAGE_THRESHOLD_VALUE = new DefaultParameterDescriptor(
			"InputImageThresholdValue", Double.class, null, new Double(
					Double.NaN));

	/**
	 * Creates an instance and sets the metadata.
	 */
	public ImagePyramidFormat() {
		setInfo();
	}

	/**
	 * Sets the metadata information for this format
	 */
	private void setInfo() {
		HashMap info = new HashMap();

		info.put("name", "ImagePyramid");
		info.put("description", "Image pyramidal plugin");
		info.put("vendor", "Geotools");
		info.put("docURL", "");
		info.put("version", "1.0");
		mInfo = info;

		// reading parameters
		readParameters = new ParameterGroup(
				new DefaultParameterDescriptorGroup(mInfo,
						new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D,
						INPUT_TRANSPARENT_COLOR,
								INPUT_IMAGE_THRESHOLD_VALUE ,OUTPUT_TRANSPARENT_COLOR}));

		// reading parameters
		writeParameters = null;
	}

	/**
	 * Retrieves a reader for this source object in case the provided source can
	 * be read using this plugin.
	 * 
	 * @param source
	 *            Object
	 * @return An {@link ImagePyramidReader} if the provided object can be read
	 *         using this plugin or null.
	 */
	public GridCoverageReader getReader(Object source) {
		return getReader(source, null);
	}

	/**
	 * This methods throw an {@link UnsupportedOperationException} because this
	 * plugiin si read only.
	 */
	public GridCoverageWriter getWriter(Object destination) {
		throw new UnsupportedOperationException(
				"This plugin is a read only plugin!");
	}

	/**
	 * @see org.geotools.data.coverage.grid.AbstractGridFormat#accepts(Object
	 *      input)
	 */
	public boolean accepts(Object source) {
		try {

			File sourceFile;
			// /////////////////////////////////////////////////////////////////////
			//
			// Check source
			//
			// /////////////////////////////////////////////////////////////////////
			if (source instanceof File)
				sourceFile = (File) source;
			else if (source instanceof URL) {

				final URL sourceURL = (URL) source;
				if (sourceURL.getProtocol() != "file")
					return false;

				final String temp = URLDecoder.decode(sourceURL.getFile(),
						"UTF8");

				sourceFile = new File(temp);
			} else if (source instanceof String) {
				sourceFile = new File((String) source);
				if (!sourceFile.exists())
					return false;
			} else
				return false;
			// ///////////////////////////////////////////////////////////////////
			//
			// Trying to load informations
			//
			//
			// ///////////////////////////////////////////////////////////////////
			// //
			//
			// get the crs if able to
			//
			// //
			String fileName = sourceFile.getAbsolutePath();
			final int index = fileName.lastIndexOf('.');
			if (index != -1)
				fileName = fileName.substring(0, index);
			PrjFileReader crsReader;
			try {
				crsReader = new PrjFileReader(new RandomAccessFile(
						new StringBuffer(fileName).append(".prj").toString(),
						"r").getChannel());
			} catch (FactoryException e) {
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
				throw new DataSourceException(e);
			}
			CoordinateReferenceSystem tempcrs = crsReader.getCoordinateReferenceSystem();
			if (tempcrs == null) {
				// use the default crs
				tempcrs = AbstractGridFormat.getDefaultCRS();
				LOGGER
						.log(
								Level.FINE,
								new StringBuffer(
										"Unable to find a CRS for this coverage, using a default one: ")
										.append(tempcrs.toWKT()).toString());
			}
			//
			// ///////////////////////////////////////////////////////////////////
			//
			// Load properties file with information about levels and envelope
			//
			//
			// ///////////////////////////////////////////////////////////////////
			// property file
			final Properties properties = new Properties();
			BufferedInputStream propertyStream = null;
			try {
				propertyStream = new BufferedInputStream(
				new FileInputStream(sourceFile));
				properties.load(propertyStream);
			} catch (Exception e) {
				if(propertyStream!=null)
					propertyStream.close();
				return false;
			}

			// load the envelope
			final String envelope = properties.getProperty("Envelope2D");
			if (envelope == null) return false;
			String[] pairs = envelope.split(" ");
			final double cornersV[][] = new double[2][2];
			String pair[];
			for (int i = 0; i < 2; i++) {
				pair = pairs[i].split(",");
				cornersV[i][0] = Double.parseDouble(pair[0]);
				cornersV[i][1] = Double.parseDouble(pair[1]);
			}

			// overviews dir
			int numOverviews = Integer.parseInt(properties
					.getProperty("LevelsNum")) - 1;

			// resolutions levels
			final String levels = properties.getProperty("Levels");
			pairs = levels.split(" ");
			double[][] overViewResolutions = numOverviews > 1 ? new double[numOverviews][2]
					: null;
			pair = pairs[0].split(",");
			double[] highestRes = new double[2];
			highestRes[0] = Double.parseDouble(pair[0]);
			highestRes[1] = Double.parseDouble(pair[1]);
			for (int i = 1; i < numOverviews + 1; i++) {
				pair = pairs[i].split(",");
				overViewResolutions[i - 1][0] = Double.parseDouble(pair[0]);
				overViewResolutions[i - 1][1] = Double.parseDouble(pair[1]);
			}

			// name
			if (properties.getProperty("Name") == null)
				return false;

			return true;
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
			return false;

		} catch (NumberFormatException e) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
			return false;
		}

	}

	/**
	 * Retrieves a reader for this source object in case the provided source can
	 * be read using this plugin.
	 * 
	 * @param source
	 *            Object
	 * @param hints
	 *            {@link Hints} to control the reader behaviour.
	 * @return An {@link ImagePyramidReader} if the provided object can be read
	 *         using this plugin or null.
	 */
	public GridCoverageReader getReader(Object source, Hints hints) {
		try {

			return new ImagePyramidReader(source, hints);
		} catch (MalformedURLException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER
						.severe(new StringBuffer(
								"impossible to get a reader for the provided source. The error is ")
								.append(e.getLocalizedMessage()).toString());
			return null;
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER
						.severe(new StringBuffer(
								"impossible to get a reader for the provided source. The error is ")
								.append(e.getLocalizedMessage()).toString());
			return null;
		}
	}

	/**
	 * Throw an exception since this plugin is readonly.
	 * 
	 * @return nothing.
	 */
	public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
		throw new UnsupportedOperationException("Unsupported method.");
	}

}
