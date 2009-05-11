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
package org.geotools.gce.imagemosaic.jdbc;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;

import org.geotools.factory.Hints;

import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;

import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;

import org.opengis.parameter.GeneralParameterDescriptor;

import java.awt.Color;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.net.URLDecoder;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * {@link AbstractGridFormat} sublass for controlling
 * {@link ImageMosaicJDBCReader} creation.
 *
 * As the name says, it handles mosaicing georeferenced images and image
 * pyramids, based on tiles stored in a JDBC database.
 *
 * The stored tiles in the database could have any format jai can decode.
 *
 *
 * <ul>
 *
 * <li> DefaultParameterDescriptor OUTPUT_TRANSPARENT_COLOR = new
 * DefaultParameterDescriptor("OutputTransparentColor", Color.class, null,
 * null); The default value is white
 * </ul>
 *
 *
 * @author mcr
 * @since 2.5
 */
public class ImageMosaicJDBCFormat extends AbstractGridFormat implements Format {
    /** Logger. */
    private final static Logger LOGGER = Logger.getLogger(ImageMosaicJDBCFormat.class.getPackage()
                                                                                     .getName());

    /** Control the transparency of the output coverage. */
    public static final DefaultParameterDescriptor OUTPUT_TRANSPARENT_COLOR = new DefaultParameterDescriptor("OutputTransparentColor",
            Color.class, null, null);

    /**
     * Creates an instance and sets the metadata.
     */
    public ImageMosaicJDBCFormat() {
        setInfo();
    }

    public static URL getURLFromSource(Object source) {
        if (source == null) {
            return null;
        }

        URL sourceURL = null;

        try {
            if (source instanceof File) {
                sourceURL = ((File) source).toURI().toURL();
            } else if (source instanceof URL) {
                sourceURL = (URL) source;
            } else if (source instanceof String) {
                final File tempFile = new File((String) source);

                if (tempFile.exists()) {
                    sourceURL = tempFile.toURI().toURL();
                } else {
                    sourceURL = new URL(URLDecoder.decode((String) source,
                                "UTF8"));

//                    if (sourceURL.getProtocol().equals("file") == false) {
//                        return null;
//                    }
                }
            }
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
            }

            return null;
        }

        return sourceURL;
    }

    /**
     * Sets the metadata information.
     */
    private void setInfo() {
        HashMap<String, String> info = new HashMap<String, String>();

        info.put("name", "ImageMosaicJDBC");
        info.put("description", "Image mosaicking/pyramidal jdbc plugin");
        info.put("vendor", "Geotools");
        info.put("docURL", "");
        info.put("version", "1.0");
        mInfo = info;

        // reading parameters
        readParameters = new ParameterGroup(new DefaultParameterDescriptorGroup(
                    mInfo,
                    new GeneralParameterDescriptor[] {
                        READ_GRIDGEOMETRY2D, OUTPUT_TRANSPARENT_COLOR
                    }));

        // reading parameters
        writeParameters = null;
    }

    /**
     * @see org.geotools.data.coverage.grid.AbstractGridFormat#getReader(Object)
     */
    @Override
    public GridCoverageReader getReader(Object source) {
        return getReader(source, null);
    }

    /**
     *
     */
    @Override
    public GridCoverageWriter getWriter(Object destination) {
        throw new UnsupportedOperationException(
            "This plugin does not support writing.");
    }

    /**
     * @see org.geotools.data.coverage.grid.AbstractGridFormat#accepts(Object
     *      input)
     */
    @Override
    public boolean accepts(Object source) {
        if (source == null) {
            return false;
        }

        URL sourceUrl = getURLFromSource(source);

        if (sourceUrl == null) {
            return false;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            InputStream in = (InputStream) sourceUrl.getContent();
            int c;

            while ((c = in.read()) != -1)
                out.write(c);

            in.close();
            out.close();
        } catch (IOException e) {
            return false;
        }

        return out.toString().indexOf("coverageName") != -1;
    }

    /**
     * @see AbstractGridFormat#getReader(Object, Hints)
     */
    @Override
    public GridCoverageReader getReader(Object source, Hints hints) {
        try {
            return new ImageMosaicJDBCReader(source, hints);
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            }

            return null;
        }
    }

    /**
     * Throw an exception since this plugin is readonly.
     *
     * @return nothing.
     */
    @Override
    public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
        throw new UnsupportedOperationException("Unsupported method.");
    }
}
