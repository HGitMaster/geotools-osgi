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


/**
 * Process for converting a raster to a vector.
 * <p>
 * The algorithm used is adapted from the GRASS raster to vector C code. It moves
 * a 2x2 kernel over the input raster. The data in the kernel are matched to a
 * table of the 12 possible configurations indicating which horizontal and/or
 * vertical pixel boundaries need to be traced.
 * 
 * @author Michael Bedward <michael.bedward@gmail.com>
 */
public class RasterToVectorFactory implements ProcessFactory {

    private static final String VERSION_STRING = "0.0.2";
    
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

    /**
     * Key to retrieve the vectorized features from the results map returned
     * by {@linkplain RasterToVectorProcess#execute(java.util.Map, org.opengis.util.ProgressListener) }
     */
    public static final String FEATURES = "features";
    
    public RasterToVectorFactory() {
    }

    /**
     * Create a new instance of RasterToVectorProcess
     */
    public Process create() {
        return new RasterToVectorProcess(this);
    }

    /**
     * Get a descrption for this proecess
     * @return the string: Raster to Vector transformation
     */
    public InternationalString getDescription() {
        return Text.text("Raster to Vector transformation");
    }

    /**
     * Get the name of this process
     * @return the string: RasterToVectorProcess
     */
    public String getName() {
        return "Raster2Vector";
    }

    /**
     * Get a description of the input parameters.
     *
     * @return a Map describing valid input parameters
     */
    public Map<String, Parameter<?>> getParameterInfo() {
        Map<String, Parameter<?>> info = new HashMap<String, Parameter<?>>();
        info.put(RASTER.key, RASTER);
        info.put(BAND.key, BAND);
        info.put(OUTSIDE.key, OUTSIDE);
        return info;
    }

    /**
     * Get information about the results
     * @param parameters ???
     * @return
     * @throws java.lang.IllegalArgumentException
     */
    public Map<String, Parameter<?>> getResultInfo(Map<String, Object> parameters)
            throws IllegalArgumentException {
        Map<String, Parameter<?>> info = new HashMap<String, Parameter<?>>();
        // we should be able to record the FeatureType here; but it is not well
        // defined in a public schema?
        SimpleFeatureType schema = getSchema(null);
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(Parameter.FEATURE_TYPE, schema);
        info.put("features", new Parameter("Features", FeatureCollection.class, Text
                .text("Features"), Text.text("The generated features"), metadata));
        return info;
    }

    /**
     * @return the string: RasterToVectorProcess
     */
    public InternationalString getTitle() {
        return Text.text("Raster2Vector");
    }

    /**
     * Get the version of this process
     * @return
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
     * We can generate a schema; but we need to know the CoordinateReferenceSystem.
     * 
     * @param crs a coorindate reference system for the features
     * @return a new SimpleFeatureType object
     */
    public static SimpleFeatureType getSchema(CoordinateReferenceSystem crs) {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("R2Vpolygons");
        if (crs != null) {
            typeBuilder.setCRS(crs);
        }
        typeBuilder.add("shape", Polygon.class, (CoordinateReferenceSystem) null);
        typeBuilder.add("code", Integer.class);
        SimpleFeatureType type = typeBuilder.buildFeatureType();
        return type;
    }
}

