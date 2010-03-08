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
package org.geotools.gce.imagemosaic.base;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
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
public abstract class AbstractImageMosaicFormat extends AbstractGridFormat implements Format {

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
    public AbstractImageMosaicFormat() {
        setInfo();
    }

    /**
     * Sets the metadata information.
     */
    protected void setInfo() {
        Map<String,String> info = new HashMap<String,String> ();
        info.put("name", "ImageMosaicBase");
        info.put("description", "Abstract Image Mosaicking Plugin");
        info.put("vendor", "Geotools");
        info.put("docURL", "");
        info.put("version", "2.6.0");
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
     * Throw an exception since this plugin is readonly.
     * 
     * @return nothing.
     */
    public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
        throw new UnsupportedOperationException("Unsupported method.");
    }

}
