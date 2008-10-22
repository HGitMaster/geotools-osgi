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
package org.geotools.gce.imagemosaic;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;

/**
 * {@link AbstractGridFormat} sublass for controlling {@link ImageMosaicReader} creation. As the
 * name says, it handles mosaic of georeferenced images, which means
 * <ol>
 * <li>tiff+tfw+prj</li>
 * <li>jpeg+tfw+prj</li>
 * <li>png+tfw+prj</li>
 * <li>geotiff</li>
 * </ol>
 * This does not mean that you throw there a couple of images and it will do the trick no matter how
 * these images are. Requirements are:
 * <ul>
 * <li>(almost) equal spatial resolution</li>
 * <li>same number of bands</li>
 * <li>same data type</li>
 * <li>same projection</li>
 * </ul>
 * The first requirement can be relaxed a little but if they have the same spatial resolution the
 * performances are much better. There are parameters that you can use to control the behaviour of
 * the mosaic in terms of thresholding and transparency. They are as follows:
 * <ul>
 * <li>--DefaultParameterDescriptor FINAL_ALPHA = new DefaultParameterDescriptor( "FinalAlpha",
 * Boolean.class, null, Boolean.FALSE)-- It asks the plugin to add transparency on the final created
 * mosaic. IT simply performs a threshonding looking for areas where there is no data, i.e.,
 * intensity is really low and transform them into transparent areas. It is obvious that depending
 * on the nature of the input images it might interfere with the original values.</li>
 * <li>---ALPHA_THRESHOLD = new DefaultParameterDescriptor( "AlphaThreshold", Double.class, null,
 * new Double(1));--- Controls the transparency addition by specifying the treshold to use.</li>
 * <li>INPUT_IMAGE_THRESHOLD = new DefaultParameterDescriptor( "InputImageROI", Boolean.class,
 * null, Boolean.FALSE)--- INPUT_IMAGE_THRESHOLD_VALUE = new DefaultParameterDescriptor(
 * "InputImageROIThreshold", Integer.class, null, new Integer(1));--- These two can be used to
 * control the application of ROIs on the input images based on tresholding values. Basically using
 * the threshold you can ask the mosaic plugin to load or not certain pixels of the original images.</li>
 * 
 * @author Simone Giannecchini (simboss), GeoSolutions
 * @since 2.3
 */
@SuppressWarnings("deprecation")
public final class ImageMosaicFormat extends AbstractGridFormat implements Format {

    /** Logger. */
    private final static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.gce.imagemosaic");

    /** Control the type of the final mosaic. */
    public static final ParameterDescriptor<Boolean> FADING = new DefaultParameterDescriptor<Boolean>(
            "Fading", Boolean.class, new Boolean[]{Boolean.TRUE,Boolean.FALSE}, Boolean.FALSE);

    /** Control the transparency of the input coverages. */
    public static final ParameterDescriptor<Color> INPUT_TRANSPARENT_COLOR = new DefaultParameterDescriptor<Color>(
            "InputTransparentColor", Color.class, null, null);

    /** Control the transparency of the output coverage. */
    public static final ParameterDescriptor<Color> OUTPUT_TRANSPARENT_COLOR = new DefaultParameterDescriptor<Color>(
            "OutputTransparentColor", Color.class, null, null);

    /** Control the thresholding on the input coverage */
    public static final ParameterDescriptor<Double> INPUT_IMAGE_THRESHOLD_VALUE = new DefaultParameterDescriptor<Double>(
            "InputImageThresholdValue", Double.class, null, new Double(Double.NaN));
    
    /** Control the thresholding on the input coverage */
    public static final ParameterDescriptor<Integer> MAX_ALLOWED_TILES = new DefaultParameterDescriptor<Integer>(
            "MaxAllowedTiles", Integer.class, null, new Integer(Integer.MAX_VALUE));

    /**
     * Creates an instance and sets the metadata.
     */
    public ImageMosaicFormat() {
        setInfo();
    }

    /**
     * Sets the metadata information.
     */
    private void setInfo() {
        final HashMap<String,String> info = new HashMap<String,String> ();
        info.put("name", "ImageMosaic");
        info.put("description", "Image mosaicking plugin");
        info.put("vendor", "Geotools");
        info.put("docURL", "");
        info.put("version", "1.0");
        mInfo = info;

        // reading parameters
        readParameters = new ParameterGroup(new DefaultParameterDescriptorGroup(mInfo,
                new GeneralParameterDescriptor[]{READ_GRIDGEOMETRY2D, INPUT_TRANSPARENT_COLOR,
                        INPUT_IMAGE_THRESHOLD_VALUE, OUTPUT_TRANSPARENT_COLOR}));

        // reading parameters
        writeParameters = null;
    }

    /**
     * @see org.geotools.data.coverage.grid.AbstractGridFormat#getReader(Object)
     */
    public GridCoverageReader getReader( Object source ) {
        return getReader(source, null);
    }

    /**
     * 
     */
    public GridCoverageWriter getWriter( Object destination ) {
        throw new UnsupportedOperationException("This plugin does not support writing.");
    }

    /**
     * @see org.geotools.data.coverage.grid.AbstractGridFormat#accepts(Object input)
     */
    public boolean accepts( Object source ) {
        try {

            URL sourceURL;
            // /////////////////////////////////////////////////////////////////////
            //
            // Check source
            //
            // /////////////////////////////////////////////////////////////////////
            if (source instanceof File)
                sourceURL = ((File) source).toURL();
            else if (source instanceof URL)
                sourceURL = (URL) source;
            else if (source instanceof String) {
                final File tempFile = new File((String) source);
                if (tempFile.exists()) {
                    sourceURL = tempFile.toURL();
                } else
                    try {
                        sourceURL = new URL(URLDecoder.decode((String) source, "UTF8"));
                        if (sourceURL.getProtocol() != "file") {
                            return false;

                        }
                    } catch (MalformedURLException e) {
                        if (LOGGER.isLoggable(Level.FINE))
                            LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
                        return false;

                    } catch (UnsupportedEncodingException e) {
                        if (LOGGER.isLoggable(Level.FINE))
                            LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
                        return false;

                    }

            } else
                return false;
            // /////////////////////////////////////////////////////////////////////
            //
            // Load tiles informations, especially the bounds, which will be
            // reused
            //
            // /////////////////////////////////////////////////////////////////////
            final ShapefileDataStore tileIndexStore = new ShapefileDataStore(sourceURL);
            final String[] typeNames = tileIndexStore.getTypeNames();
            if (typeNames.length <= 0)
                return false;
            final String typeName = typeNames[0];
            final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = tileIndexStore.getFeatureSource(typeName);
            final SimpleFeatureType schema = featureSource.getSchema();
            // looking for the location attribute
            if (schema.getDescriptor("location") == null)
                return false;

            // /////////////////////////////////////////////////////////////////////
            //
            // Now look for the properties file and try to parse relevant fields
            //
            // /////////////////////////////////////////////////////////////////////
            String temp = URLDecoder.decode(sourceURL.getFile(), "UTF8");
            final int index = temp.lastIndexOf(".");
            if (index != -1)
                temp = temp.substring(0, index);
            final File propertiesFile = new File(new StringBuffer(temp).append(".properties")
                    .toString());
            if( !propertiesFile.exists() || !propertiesFile.isFile() ){
                throw new FileNotFoundException("Properties file, descibing the ImageMoasic, does not exist:"+propertiesFile);
            }
            
            final Properties properties = new Properties();
            properties.load(new BufferedInputStream(new FileInputStream(propertiesFile)));

            // load the envelope
            final String envelope = properties.getProperty("Envelope2D");
            try {
                String[] pairs = envelope.split(" ");
                final double cornersV[][] = new double[2][2];
                String pair[];
                for( int i = 0; i < 2; i++ ) {
                    pair = pairs[i].split(",");
                    cornersV[i][0] = Double.parseDouble(pair[0]);
                    cornersV[i][1] = Double.parseDouble(pair[1]);
                }
            }
            catch( IndexOutOfBoundsException formatException ){
                throw (IOException) new IOException("Could not parse Envelope2D="+envelope+"(check uses of space and comma)").initCause(formatException );
            }
            catch( Exception unExpected ){
                throw (IOException) new IOException("Could not parse Envelope2D="+envelope).initCause(unExpected );
            }
            // resolutions levels
            final String levels = properties.getProperty("Levels");            
            try {
                Integer.parseInt(properties.getProperty("LevelsNum"));
                String[] pairs = levels.split(" ");
                String[] pair = pairs[0].split(",");
                Double.parseDouble(pair[0]);
                Double.parseDouble(pair[1]);
                if (!properties.containsKey("Name"))
                    return false;
                try {
                    if (!properties.containsKey("ExpandToRGB")) {
                        if (LOGGER.isLoggable(Level.INFO))
                            LOGGER
                                    .info("Unable to find ExpandToRGB field. This mosaic may malfunction if the field was forgotten.");
                        return true;
                    }
                } catch (Throwable t) {
                }
                return true;
            }
            catch( IndexOutOfBoundsException formatException ){
                throw (IOException) new IOException("Could not parse Levels="+levels+"(check uses of space and comma)").initCause( formatException );
            }
            catch( Exception unExpected ){
                throw (IOException) new IOException("Could not parse LevelsNum and Levels information").initCause( unExpected );
            }
       } catch (Exception e) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
            return false;

        }

    }
    /**
     * @see AbstractGridFormat#getReader(Object, Hints)
     */
    public GridCoverageReader getReader( Object source, Hints hints ) {
        try {

            return new ImageMosaicReader(source, hints);
        } catch (MalformedURLException e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            return null;
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
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
