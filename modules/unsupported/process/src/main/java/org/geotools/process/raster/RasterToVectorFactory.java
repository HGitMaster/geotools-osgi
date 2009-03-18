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
package org.geotools.process.raster;

import java.util.HashMap;
import java.util.Map;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.Parameter;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.process.Process;
import org.geotools.process.ProcessFactory;
import org.geotools.text.Text;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

import com.vividsolutions.jts.geom.Polygon;
import java.util.Collections;
import java.util.TreeMap;


/**
 * Process for converting a raster regions to vector polygons.
 * <p>
 * The algorithm used is adapted from the GRASS raster to vector C code. It moves
 * a 2x2 kernel over the input raster. The data in the kernel are matched to a
 * table of the 12 possible configurations indicating which horizontal and/or
 * vertical pixel boundaries need to be traced.
 * 
 * @author Michael Bedward, Jody Garnett
 * @since 2.6
 */
public class RasterToVectorFactory implements ProcessFactory {

    private static final String VERSION_STRING = "0.0.3";
    
    /** Grid coverage to vectorize */
    public static final Parameter<GridCoverage2D> RASTER = new Parameter<GridCoverage2D>(
        "raster", GridCoverage2D.class, Text.text("Raster"), 
        Text.text("Grid coverage to vectorize"));
    
    /** Index of the band with data to vectorize */
    public static final Parameter<Integer> BAND = new Parameter<Integer>(
        "band", Integer.class, Text.text("Band"), Text.text("Index of band to vectorize"));
    
    /** 
     * The code(s) representing NODATA or outside the regions to be vectorized,
     * which will be supplied as a {@linkplain java.util.Collection} of Double values to the
     * {@linkplain RasterToVectorProcess#execute(java.util.Map, org.opengis.util.ProgressListener) }
     * method.
     */
    public static final Parameter<Double> OUTSIDE = new Parameter<Double>(
            "nodata", Double.class, Text.text("NoData"), 
            Text.text("Value representing NODATA or outside"),
            true, 1, -1, null, null);

    private static final Map<String, Parameter<?>> parameterInfo = new TreeMap<String, Parameter<?>>();
    static {
        parameterInfo.put(RASTER.key, RASTER);
        parameterInfo.put(BAND.key, BAND);
        parameterInfo.put(OUTSIDE.key, OUTSIDE);
    }
    
    /**
     * Parameter to retrieve the vectorized features from the results map returned
     * by {@linkplain RasterToVectorProcess#execute(java.util.Map, org.opengis.util.ProgressListener) }
     */
    public static final Parameter<FeatureCollection> RESULT_FEATURES;
    
    private static final Map<String, Parameter<?>> resultInfo = new TreeMap<String, Parameter<?>>();
    static {
        // Jody: we should be able to record the FeatureType here; but it is not well
        // defined in a public schema?
        SimpleFeatureType schema = getSchema(null);
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Parameter.FEATURE_TYPE, schema);

        RESULT_FEATURES  = new Parameter<FeatureCollection>(
            "features", FeatureCollection.class, Text.text("Features"),
            Text.text("Vectorized region boundaries as polygon features"),
            metadata);

        resultInfo.put(RESULT_FEATURES.key, RESULT_FEATURES);
    }

    /**
     * Return a new instance of a RasterToVectorProcess
     */
    public RasterToVectorProcess create() {
        return new RasterToVectorProcess(this);
    }

    /**
     * Get the description of this process
     * @return the string: Raster region to vector polygon conversion
     */
    public InternationalString getDescription() {
        return Text.text("Raster region to vector polygon conversion");
    }

    /**
     * Get the name of this process
     * @return the string: RasterToVectorProcess
     */
    public String getName() {
        return "RasterToVectorProcess";
    }

    /**
     * Get a map of input parameters required by the 
     * {@linkplain RasterToVectorProcess#execute(java.util.Map, org.opengis.util.ProgressListener) }
     * method
     *
     * @return a Map of input parameters
     */
    public Map<String, Parameter<?>> getParameterInfo() {
        return Collections.unmodifiableMap(parameterInfo);
    }

    /**
     * Get information about the results that are returned as Map by the
     * {@linkplain RasterToVectorProcess#execute(java.util.Map, org.opengis.util.ProgressListener) }
     * method
     * @param parameters ignored at present so may be null
     * @return a Map of output parameters
     * @throws java.lang.IllegalArgumentException
     */
    public Map<String, Parameter<?>> getResultInfo(Map<String, Object> parameters)
            throws IllegalArgumentException {
        return Collections.unmodifiableMap(resultInfo);
    }

    /**
     * Get the process title
     * @return title
     */
    public InternationalString getTitle() {
        return Text.text("Vectorize raster regions");
    }

    /**
     * Get the version of this process
     * @return version as a string
     */
    public String getVersion() {
        return VERSION_STRING;
    }

    /**
     * Check if this process supports a progress listener
     * @return always returns true
     */
    public boolean supportsProgress() {
        return true;
    }

    /**
     * Return the feature type of the vectorized polygons.
     * Note: We can generate a schema; but we need to know the CoordinateReferenceSystem.
     * 
     * @param crs a coorindate reference system for the features
     * @return a new SimpleFeatureType with name: r2vPolygons and two attributes:
     * shape (Polygon) and code (Integer)
     */
    public static SimpleFeatureType getSchema(CoordinateReferenceSystem crs) {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("r2vPolygons");
        if (crs != null) {
            typeBuilder.setCRS(crs);
        }
        typeBuilder.add("shape", Polygon.class, (CoordinateReferenceSystem) null);
        typeBuilder.add("code", Integer.class);
        SimpleFeatureType type = typeBuilder.buildFeatureType();
        return type;
    }
}

