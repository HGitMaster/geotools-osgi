/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
 *
 */
package org.geotools.arcsde.gce;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;

/**
 * An implementation of the ArcSDE Raster Format. Based on the ArcGrid module.
 * 
 * @author Saul Farber (saul.farber)
 * @author jeichar
 * @author Simone Giannecchini (simboss)
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/gce/ArcSDERasterFormat.java $
 */
public class ArcSDERasterFormat extends AbstractGridFormat implements Format {

    protected static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(ArcSDERasterFormat.class.getPackage().getName());

    /**
     * Creates an instance and sets the metadata.
     */
    public ArcSDERasterFormat() {
        setInfo();
    }

    /**
     * Sets the metadata information.
     */
    private void setInfo() {
        HashMap info = new HashMap();

        info.put("name", "ArcSDE Raster");
        info.put("description", "ArcSDE Raster Format");
        info.put("vendor", "Geotools");
        info.put("docURL", "");
        info.put("version", "0.1-alpha");
        mInfo = info;

        readParameters = new ParameterGroup(new DefaultParameterDescriptorGroup(mInfo,
                new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D }));
    }

    /**
     * @see org.geotools.data.coverage.grid.AbstractGridFormat#getReader(Object source)
     */
    @Override
    public GridCoverageReader getReader(Object source) {
        return getReader(source, null);
    }

    @Override
    public GridCoverageReader getReader(Object source, Hints hints) {
        try {
            return new ArcSDERasterGridCoverage2DReader(source, hints);
        } catch (IOException dse) {
            LOGGER
                    .log(Level.SEVERE, "Unable to creata ArcSDERasterReader for " + source + ".",
                            dse);
            return null;
        }
    }

    /**
     * @see org.geotools.data.coverage.grid.AbstractGridFormat#createWriter(java.lang.Object
     *      destination)
     */
    @Override
    public GridCoverageWriter getWriter(Object destination) {
        // return new ArcGridWriter(destination);
        return null;
    }

    /**
     * @see org.geotools.data.coverage.grid.AbstractGridFormat#accepts(Object input)
     */
    @Override
    public boolean accepts(Object input) {
        StringBuffer url;
        if (input instanceof File) {
            url = new StringBuffer(((File) input).getPath());
        } else if (input instanceof String) {
            url = new StringBuffer((String) input);
        } else {
            return false;
        }
        try {
            ArcSDERasterGridCoverage2DReader.sdeURLToConnectionConfig(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @see org.opengis.coverage.grid.Format#getName()
     */
    @Override
    public String getName() {
        return this.mInfo.get("name");
    }

    /**
     * @see org.opengis.coverage.grid.Format#getDescription()
     */
    @Override
    public String getDescription() {
        return this.mInfo.get("description");
    }

    /**
     * @see org.opengis.coverage.grid.Format#getVendor()
     */
    @Override
    public String getVendor() {
        return this.mInfo.get("vendor");
    }

    /**
     * @see org.opengis.coverage.grid.Format#getDocURL()
     */
    @Override
    public String getDocURL() {
        return this.mInfo.get("docURL");
    }

    /**
     * @see org.opengis.coverage.grid.Format#getVersion()
     */
    @Override
    public String getVersion() {
        return this.mInfo.get("version");
    }

    /**
     * Retrieves the default instance for the {@link ArcSDERasterFormat} of the
     * {@link GeoToolsWriteParams} to control the writing process.
     * 
     * @return a default instance for the {@link ArcSDERasterFormat} of the
     *         {@link GeoToolsWriteParams} to control the writing process.
     */
    @Override
    public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
        throw new UnsupportedOperationException("ArcSDE Rasters are read only for now.");
    }
}
